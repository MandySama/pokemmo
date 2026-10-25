package top.totodile.pokemmo.client.rom.nds.model;

import static top.totodile.pokemmo.client.rom.nds.model.NitroDictionary.u16;

import com.badlogic.gdx.graphics.Pixmap;

import java.io.IOException;
import java.nio.*;
import java.util.*;

/** 移植参考 NitroTextureDictionary.Rd/Rt0、TrainerPartyRomChunk 与 ScriptBytecodeRomChunk。 */
public final class NitroTextureDictionary {
    public record TextureInfo(
            String name, int offset, int width, int height, int format, boolean transparent) {}

    private final ByteBuffer data;
    private final Map<String, TextureInfo> textures = new LinkedHashMap<>();
    private final Map<String, Integer> palettes = new LinkedHashMap<>();

    public NitroTextureDictionary(byte[] bytes) throws IOException {
        data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        try {
            int section = -1;
            for (int i = 0; i < u16(data, 14); i++) {
                int offset = data.getInt(16 + i * 4);
                if (data.getInt(offset) == 0x30584554) section = offset;
            }
            if (section < 0) throw new IOException("Missing TEX0");
            int texData = section + data.getInt(section + 20),
                    compressed = section + data.getInt(section + 36);
            int paletteData = section + data.getInt(section + 56);
            var tex = new NitroDictionary(data, section + 60, 8);
            for (int i = 0; i < tex.count; i++) {
                int record = tex.record(i),
                        flags = u16(data, record + 2),
                        format = flags >>> 10 & 7;
                textures.put(
                        tex.name(i),
                        new TextureInfo(
                                tex.name(i),
                                (format == 5 ? compressed : texData) + u16(data, record) * 8,
                                8 << (flags >>> 4 & 7),
                                8 << (flags >>> 7 & 7),
                                format,
                                (flags & 8192) != 0));
            }
            var pal = new NitroDictionary(data, tex.end, 4);
            for (int i = 0; i < pal.count; i++)
                palettes.put(pal.name(i), paletteData + (u16(data, pal.record(i)) & 8191) * 8);
        } catch (RuntimeException error) {
            throw new IOException("Invalid Nitro texture dictionary", error);
        }
    }

    public Set<String> paletteNames() {
        return Collections.unmodifiableSet(palettes.keySet());
    }

    /** BlackWhiteRom.Z10: sprites/sp.pak XOR patch for the first follower palette. */
    public void xorFirstPalette(byte[] patch, int modelIndex) {
        if (palettes.isEmpty() || textures.isEmpty() || modelIndex < 198 || modelIndex > 763)
            return;
        int size = textures.values().iterator().next().format() == 4 ? 512 : 32;
        int offset = (modelIndex - 198) * size, start = palettes.values().iterator().next();
        if (offset + size > patch.length || start + size > data.limit()) return;
        for (int i = 0; i < size; i++)
            data.put(start + i, (byte) (data.get(start + i) ^ patch[offset + i]));
    }

    public TextureInfo info(String name) {
        return textures.get(name);
    }

    public boolean has(String texture, String palette) {
        return textures.containsKey(texture) && palettes.containsKey(palette);
    }

    public Set<String> names() {
        return Collections.unmodifiableSet(textures.keySet());
    }

    public Pixmap pixels(String texture, String palette, boolean mirrorS, boolean mirrorT)
            throws IOException {
        TextureInfo info = textures.get(texture);
        Integer paletteOffset = palettes.get(palette);
        if (info == null || paletteOffset == null)
            throw new IOException("Missing Nitro texture/palette: " + texture + " / " + palette);
        int w = info.width, h = info.height;
        // NitroTextureDictionary.Rd：不透明索引纹理使用 RGB565，带透明度的纹理使用 RGBA8888。
        // 必须保留上传格式；直接改为 RGBA 会改变 GPU 展开后的亮部颜色。
        boolean opaqueIndexed = info.format >= 2 && info.format <= 4 && !info.transparent;
        Pixmap result =
                new Pixmap(
                        w * (mirrorS ? 2 : 1),
                        h * (mirrorT ? 2 : 1),
                        opaqueIndexed ? Pixmap.Format.RGB565 : Pixmap.Format.RGBA8888);
        result.setBlending(Pixmap.Blending.None);
        try {
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++) {
                    int pixel = y * w + x, index, alpha = 255;
                    switch (info.format) {
                        case 1 -> {
                            int value = data.get(info.offset + pixel) & 255;
                            index = value & 31;
                            int a = value >>> 5;
                            int a5 = (a << 2) | (a >>> 1);
                            alpha = (a5 << 3) | (a5 >>> 2);
                        }
                        case 2 -> {
                            index = (data.get(info.offset + pixel / 4) >> ((pixel & 3) * 2)) & 3;
                            if (info.transparent && index == 0) alpha = 0;
                        }
                        case 3 -> {
                            index = (data.get(info.offset + pixel / 2) >> ((pixel & 1) * 4)) & 15;
                            if (info.transparent && index == 0) alpha = 0;
                        }
                        case 4 -> {
                            index = data.get(info.offset + pixel) & 255;
                            if (info.transparent && index == 0) alpha = 0;
                        }
                        case 6 -> {
                            int value = data.get(info.offset + pixel) & 255;
                            index = value & 7;
                            int a = value >>> 3;
                            alpha = (a << 3) | (a >>> 2);
                        }
                        default ->
                                throw new IOException(
                                        "Texture format unsupported by reference"
                                                + " NitroTextureDictionary: "
                                                + info.format);
                    }
                    int packed = u16(data, paletteOffset + index * 2);
                    // 参考调色板使用 5 位通道乘 8；不引入 glTF 的 sRGB 转换。
                    int color =
                            ((packed & 31) * 8 << 24)
                                    | ((packed >>> 5 & 31) * 8 << 16)
                                    | ((packed >>> 10 & 31) * 8 << 8)
                                    | alpha;
                    result.drawPixel(x, y, color);
                    if (mirrorS) result.drawPixel(w * 2 - 1 - x, y, color);
                    if (mirrorT) result.drawPixel(x, h * 2 - 1 - y, color);
                    if (mirrorS && mirrorT) result.drawPixel(w * 2 - 1 - x, h * 2 - 1 - y, color);
                }
            return result;
        } catch (IOException | RuntimeException error) {
            result.dispose();
            throw new IOException("Unable to decode Nitro texture " + texture, error);
        }
    }
}
