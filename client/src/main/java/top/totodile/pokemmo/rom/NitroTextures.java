package top.totodile.pokemmo.rom;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static top.totodile.pokemmo.rom.NdsArchive.*;
import static top.totodile.pokemmo.rom.NitroData.*;

/** TEX0 调色板纹理，在当前 OpenGL 上下文上传并缓存。 */
final class NitroTextures implements AutoCloseable {
    private final ByteBuffer data;
    private final int base;
    private final Map<String, Entry> textures = new HashMap<>();
    private final Map<String, Entry> palettes = new HashMap<>();
    private final Map<String, Integer> uploaded = new HashMap<>();

    NitroTextures(ByteBuffer data, int base) {
        this.data = data;
        this.base = base;
        for (Entry entry : dictionary(data, base + 60, 8)) textures.put(entry.name(), entry);
        int paletteDictionary = base + 60 + ushort(data, base + 62);
        for (Entry entry : dictionary(data, paletteDictionary, 4)) palettes.put(entry.name(), entry);
    }

    int texture(String name, String palette) {
        if (name == null) return 0;
        String key = name + ":" + palette;
        return uploaded.computeIfAbsent(key, ignored -> upload(name, palette));
    }

    boolean transparent(String name) {
        Entry entry = textures.get(name);
        if (entry == null) return false;
        int params = ushort(data, entry.offset() + 2);
        int format = params >> 10 & 7;
        return format == 1 || format == 6 || (params & 8192) != 0;
    }

    private int upload(String name, String palette) {
        Entry entry = textures.get(name);
        if (entry == null) throw new IllegalArgumentException("Missing Nitro texture: " + name);
        int params = ushort(data, entry.offset() + 2);
        int width = 8 << (params >> 4 & 7);
        int height = 8 << (params >> 7 & 7);
        int format = params >> 10 & 7;
        int pixelOffset = base + data.getInt(base + (format == 5 ? 36 : 20))
                + ushort(data, entry.offset()) * 8;
        Entry colors = palettes.get(palette);
        int paletteOffset = colors == null ? -1
                : base + data.getInt(base + 56) + (ushort(data, colors.offset()) & 8191) * 8;
        ByteBuffer rgba = BufferUtils.createByteBuffer(width * height * 4);
        for (int i = 0; i < width * height; i++) {
            int index;
            int alpha = 255;
            int packed = data.get(pixelOffset + switch (format) {
                case 2 -> i / 4;
                case 3 -> i / 2;
                case 7 -> i * 2;
                default -> i;
            }) & 255;
            switch (format) {
                case 1 -> {
                    index = packed & 31;
                    alpha = (packed >> 5) * 255 / 7;
                }
                case 2 -> index = packed >> (i % 4 * 2) & 3;
                case 3 -> index = packed >> (i % 2 * 4) & 15;
                case 4 -> index = packed;
                case 6 -> {
                    index = packed & 7;
                    alpha = (packed >> 3) * 255 / 31;
                }
                case 7 -> index = 0;
                default -> throw new IllegalArgumentException("Unsupported Nitro texture format: " + format);
            }
            int color;
            if (format == 7) {
                color = ushort(data, pixelOffset + i * 2);
                alpha = (color & 32768) == 0 ? 0 : 255;
            } else {
                if (paletteOffset < 0) throw new IllegalArgumentException("Missing Nitro palette: " + palette);
                color = ushort(data, paletteOffset + index * 2);
                if ((params & 8192) != 0 && index == 0 && format >= 2 && format <= 4) alpha = 0;
            }
            // BGR555 采用 8 倍展开，与卡带调色板的取值一致。
            rgba.put((byte) ((color & 31) * 8)).put((byte) ((color >> 5 & 31) * 8))
                    .put((byte) ((color >> 10 & 31) * 8)).put((byte) alpha);
        }
        rgba.flip();
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, rgba);
        return texture;
    }

    @Override
    public void close() {
        for (int texture : uploaded.values()) glDeleteTextures(texture);
        uploaded.clear();
    }
}
