package top.totodile.pokemmo.client.rom.nds.model;

import com.badlogic.gdx.graphics.g3d.model.data.*;
import com.badlogic.gdx.utils.Array;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/** 参考 wh0_2 的直接二进制字段读写；独立格式适配当前 ModelData。 */
final class ModelCacheCodec {
    static List<NdsNitroModelParser.Parsed> parse(
            byte[] model, byte[] textures, List<byte[]> animations) throws IOException {
        var parsed = NdsNitroModelParser.readAll(model, textures);
        if (animations.isEmpty()) return parsed;
        var result = new java.util.ArrayList<NdsNitroModelParser.Parsed>();
        for (var p : parsed) {
            for (byte[] source : animations) {
                // NSBCA 骨骼动画转成当前 ModelData；其余 Nitro 材质动画原样保留。
                if (source.length < 4
                        || source[0] != 'B'
                        || source[1] != 'C'
                        || source[2] != 'A'
                        || source[3] != '0') continue;
                var decoded = NdsJointAnimation.prepare(model, source);
                var animation = new ModelAnimation();
                animation.id = "field-" + p.model().animations.size;
                animation.nodeAnimations = new Array<>();
                for (var channel : decoded.nodeAnimations) {
                    var node = new ModelNodeAnimation();
                    node.nodeId = channel.node.id;
                    node.translation = frames(channel.translation);
                    node.rotation = frames(channel.rotation);
                    node.scaling = frames(channel.scaling);
                    animation.nodeAnimations.add(node);
                }
                p.model().animations.add(animation);
            }
            result.add(
                    new NdsNitroModelParser.Parsed(
                            p.model(),
                            p.materials(),
                            p.textures(),
                            p.bones(),
                            p.positionScale(),
                            animations));
        }
        return List.copyOf(result);
    }

    private static <T> Array<ModelNodeKeyframe<T>> frames(
            Array<com.badlogic.gdx.graphics.g3d.model.NodeKeyframe<T>> source) {
        if (source == null) return null;
        var result = new Array<ModelNodeKeyframe<T>>(source.size);
        for (var key : source) {
            var target = new ModelNodeKeyframe<T>();
            target.keytime = key.keytime;
            target.value = key.value;
            result.add(target);
        }
        return result;
    }

    static byte[] encode(List<NdsNitroModelParser.Parsed> parsed) throws IOException {
        var bytes = new ByteArrayOutputStream();
        write(new DataOutputStream(bytes), parsed);
        return bytes.toByteArray();
    }

    static void write(DataOutput out, List<NdsNitroModelParser.Parsed> parsed) throws IOException {
        out.writeInt(parsed.size());
        for (var p : parsed) {
            BinaryModelData.write(out, p.model());
            out.writeInt(p.bones());
            out.writeFloat(p.positionScale());
            out.writeInt(p.materials().size());
            for (var m : p.materials()) {
                BinaryModelData.text(out, m.name);
                BinaryModelData.text(out, m.texture);
                BinaryModelData.text(out, m.palette);
                out.writeInt(m.polygon);
                out.writeInt(m.textureParams);
                out.writeInt(m.width);
                out.writeInt(m.height);
                out.writeInt(m.flags);
                out.writeFloat(m.scaleU);
                out.writeFloat(m.scaleV);
                BinaryModelData.color(out, m.ambient);
            }
            out.writeInt(p.resourceAnimations().size());
            for (byte[] animation : p.resourceAnimations()) {
                out.writeInt(animation.length);
                out.write(animation);
            }
        }
    }

    static List<NdsNitroModelParser.Parsed> decode(byte[] bytes, byte[] textures)
            throws IOException {
        return decode(ByteBuffer.wrap(bytes), textures);
    }

    static List<NdsNitroModelParser.Parsed> decode(ByteBuffer bytes, byte[] textures)
            throws IOException {
        var input =
                new DataInputStream(
                        new InputStream() {
                            @Override
                            public int read() {
                                return bytes.hasRemaining() ? bytes.get() & 255 : -1;
                            }

                            @Override
                            public int read(byte[] data, int offset, int length) {
                                if (length == 0) return 0;
                                if (!bytes.hasRemaining()) return -1;
                                int n = Math.min(length, bytes.remaining());
                                bytes.get(data, offset, n);
                                return n;
                            }
                        });
        try {
            var result = new ArrayList<NdsNitroModelParser.Parsed>();
            for (int n = BinaryModelData.count(input); n > 0; n--) {
                var model = BinaryModelData.read(input);
                int bones = input.readInt();
                float scale = input.readFloat();
                var materials = new ArrayList<NdsNitroModelParser.Material>();
                for (int count = BinaryModelData.count(input); count > 0; count--) {
                    var m = new NdsNitroModelParser.Material();
                    m.name = BinaryModelData.text(input);
                    m.texture = BinaryModelData.text(input);
                    m.palette = BinaryModelData.text(input);
                    m.polygon = input.readInt();
                    m.textureParams = input.readInt();
                    m.width = input.readInt();
                    m.height = input.readInt();
                    m.flags = input.readInt();
                    m.scaleU = input.readFloat();
                    m.scaleV = input.readFloat();
                    m.ambient = BinaryModelData.color(input);
                    materials.add(m);
                }
                var animations = new ArrayList<byte[]>();
                for (int count = BinaryModelData.count(input); count > 0; count--) {
                    int length = input.readInt();
                    if (length < 0 || length > bytes.remaining())
                        throw new IOException("Invalid model animation length");
                    byte[] data = new byte[length];
                    input.readFully(data);
                    animations.add(data);
                }
                var dictionary =
                        materials.stream().anyMatch(m -> m.texture != null)
                                ? new NitroTextureDictionary(textures)
                                : null;
                result.add(
                        new NdsNitroModelParser.Parsed(
                                model,
                                List.copyOf(materials),
                                dictionary,
                                bones,
                                scale,
                                List.copyOf(animations)));
            }
            if (bytes.hasRemaining()) throw new IOException("Unexpected model record data");
            return List.copyOf(result);
        } catch (RuntimeException error) {
            throw new IOException("Invalid cached model record", error);
        }
    }
}
