package top.totodile.pokemmo.client.rom.nds.model;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

import java.io.IOException;
import java.nio.*;
import java.nio.charset.StandardCharsets;

/**
 * Nitro 骨骼 NSBCA。对应 NdsNitroModelParser.Xy、ResourceCacheIndexLookup、 KeyframeHermiteCurveChannel 和
 * GdxFrameBufferTextureProvider。 与参考相同，展开 ROM 的整数帧，再交给 libGDX 插值。
 */
public final class NdsJointAnimation {
    public static Animation read(byte[] modelBytes, byte[] animationBytes, ModelInstance instance)
            throws IOException {
        return read(modelBytes, animationBytes, name -> instance.getNode(name, true));
    }

    /** CPU 解码可在后台完成；真实模型节点在 GL 线程安装时绑定。 */
    public static Animation prepare(byte[] modelBytes, byte[] animationBytes) throws IOException {
        return read(
                modelBytes,
                animationBytes,
                name -> {
                    Node node = new Node();
                    node.id = name;
                    return node;
                });
    }

    private static Animation read(
            byte[] modelBytes,
            byte[] animationBytes,
            java.util.function.Function<String, Node> nodes)
            throws IOException {
        try {
            ByteBuffer model = ByteBuffer.wrap(modelBytes).order(ByteOrder.LITTLE_ENDIAN);
            int section = model.getInt(16), dictionary = section + 8;
            int base = section + model.getInt(records(model, dictionary));
            String[] names = names(model, base + 64);
            ByteBuffer data = ByteBuffer.wrap(animationBytes).order(ByteOrder.LITTLE_ENDIAN);
            section = data.getInt(16);
            dictionary = section + 8;
            base = section + data.getInt(records(data, dictionary));
            if (data.getInt(base) != 0x4341004a)
                throw new IOException("Missing Nitro joint animation");
            int frames = u16(data, base + 4), count = u16(data, base + 6);
            if (frames < 1 || count > names.length)
                throw new IOException("Invalid Nitro joint animation size");
            int pivot = base + data.getInt(base + 12), basis = base + data.getInt(base + 16);
            Animation animation = new Animation();
            animation.id = "rom-login";
            animation.duration = (frames - 1) / 30f;
            animation.nodeAnimations = new Array<>();
            for (int i = 0; i < count; i++) {
                data.position(base + u16(data, base + 20 + i * 2));
                int flags = Short.toUnsignedInt(data.getShort());
                data.getShort();
                if ((flags & 1) != 0) continue;
                NodeAnimation channel = new NodeAnimation();
                channel.node = nodes.apply(names[i]);
                if ((flags & 6) == 0)
                    channel.translation = vectors(data, base, frames, flags, 3, false);
                if ((flags & 192) == 0)
                    channel.rotation =
                            rotations(data, base, pivot, basis, frames, (flags & 256) != 0);
                if ((flags & 1536) == 0)
                    channel.scaling = vectors(data, base, frames, flags, 11, true);
                // 与参考解析器一样跳过未解析节点。
                if (channel.node != null) animation.nodeAnimations.add(channel);
            }
            return animation;
        } catch (IndexOutOfBoundsException
                | BufferUnderflowException
                | IllegalArgumentException error) {
            throw new IOException("Invalid Nitro login animation", error);
        }
    }

    private static int records(ByteBuffer b, int dictionary) throws IOException {
        int count = Byte.toUnsignedInt(b.get(dictionary + 1));
        if (count == 0) throw new IOException("Empty Nitro dictionary");
        return dictionary + 16 + count * 4;
    }

    private static String[] names(ByteBuffer b, int dictionary) throws IOException {
        int count = Byte.toUnsignedInt(b.get(dictionary + 1)),
                start = records(b, dictionary) + count * 4;
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            int p = start + i * 16, length = 0;
            while (length < 16 && b.get(p + length) != 0) length++;
            names[i] = new String(b.array(), p, length, StandardCharsets.US_ASCII);
        }
        return names;
    }

    private static int u16(ByteBuffer b, int offset) {
        return Short.toUnsignedInt(b.getShort(offset));
    }

    private record Sampling(int flags, int offset) {
        Sampling {
            // 已验证逐帧或隔帧采样；其它格式在有参考验证前明确拒绝。
            if ((flags >>> 30) > 1)
                throw new IllegalArgumentException("Unsupported login animation sampling rate");
        }

        int first(int frame) {
            if ((flags & 0x40000000) == 0) return frame;
            int boundary = (flags >>> 16) & 0x1fff;
            return (frame & 1) != 0 && frame > boundary ? (boundary >> 1) + 1 : frame >> 1;
        }

        boolean interpolate(int frame) {
            return (flags & 0x40000000) != 0
                    && (frame & 1) != 0
                    && frame <= ((flags >>> 16) & 0x1fff);
        }
    }

    private static Array<NodeKeyframe<Vector3>> vectors(
            ByteBuffer b, int base, int frames, int flags, int shift, boolean scale) {
        float[][] values = new float[3][frames];
        for (int axis = 0; axis < 3; axis++) {
            if ((flags & (1 << (shift + axis))) != 0) {
                float value = b.getInt() / 4096f;
                if (scale) b.getInt();
                java.util.Arrays.fill(values[axis], value);
            } else {
                Sampling sampling = new Sampling(b.getInt(), base + b.getInt());
                for (int frame = 0; frame < frames; frame++) {
                    int first = sampling.first(frame);
                    float value = scalar(b, sampling, first, scale);
                    if (sampling.interpolate(frame))
                        value = (value + scalar(b, sampling, first + 1, scale)) * .5f;
                    values[axis][frame] = value;
                }
            }
        }
        Array<NodeKeyframe<Vector3>> keys = new Array<>(frames);
        for (int frame = 0; frame < frames; frame++)
            keys.add(
                    new NodeKeyframe<>(
                            frame / 30f,
                            new Vector3(values[0][frame], values[1][frame], values[2][frame])));
        return keys;
    }

    private static float scalar(ByteBuffer b, Sampling s, int sample, boolean scale) {
        boolean shortValue = (s.flags & 0x20000000) != 0;
        int p = s.offset + sample * (shortValue ? 2 : 4) * (scale ? 2 : 1);
        return (shortValue ? b.getShort(p) : b.getInt(p)) / 4096f;
    }

    private static Array<NodeKeyframe<Quaternion>> rotations(
            ByteBuffer b, int base, int pivot, int basis, int frames, boolean constant) {
        Array<NodeKeyframe<Quaternion>> keys = new Array<>(constant ? 1 : frames);
        if (constant) {
            keys.add(new NodeKeyframe<>(0, quaternion(rotation(b, pivot, basis, b.getInt()))));
        } else {
            Sampling sampling = new Sampling(b.getInt(), base + b.getInt());
            for (int frame = 0; frame < frames; frame++) {
                int first = sampling.first(frame);
                Matrix4 matrix = rotation(b, pivot, basis, u16(b, sampling.offset + first * 2));
                if (sampling.interpolate(frame)) {
                    Matrix4 next =
                            rotation(b, pivot, basis, u16(b, sampling.offset + (first + 1) * 2));
                    Quaternion start = quaternion(matrix), end = quaternion(next);
                    boolean opposite = false;
                    // 参考 RJ.Dw0：矩阵列相加并归一化后，再转四元数。
                    for (int column = 0; column < 3; column++) {
                        int p = column * 4;
                        Vector3 v =
                                new Vector3(
                                                matrix.val[p] + next.val[p],
                                                matrix.val[p + 1] + next.val[p + 1],
                                                matrix.val[p + 2] + next.val[p + 2])
                                        .nor();
                        opposite |= v.isZero();
                        matrix.val[p] = v.x;
                        matrix.val[p + 1] = v.y;
                        matrix.val[p + 2] = v.z;
                    }
                    // 光束存在相邻采样相差 180° 的转向。列平均会产生零轴，
                    // 不能再归一化为四元数；仅在此奇点取同一对端点的球面中点。
                    if (opposite) {
                        keys.add(new NodeKeyframe<>(frame / 30f, start.slerp(end, .5f).nor()));
                        continue;
                    }
                }
                keys.add(new NodeKeyframe<>(frame / 30f, quaternion(matrix)));
            }
        }
        return keys;
    }

    private static Quaternion quaternion(Matrix4 matrix) {
        Quaternion q = new Quaternion().setFromMatrix(true, matrix).nor();
        if (!Float.isFinite(q.x)
                || !Float.isFinite(q.y)
                || !Float.isFinite(q.z)
                || !Float.isFinite(q.w))
            throw new IllegalArgumentException("Invalid Nitro rotation");
        return q;
    }

    private static Matrix4 rotation(ByteBuffer b, int pivot, int basis, int reference) {
        Matrix4 result = new Matrix4();
        float[] v = result.val;
        if ((reference & 0x8000) != 0) {
            int p = pivot + (reference & 32767) * 6, flags = u16(b, p), axis = flags & 15;
            if (axis > 8) throw new IllegalArgumentException("Invalid pivot rotation");
            float a = b.getShort(p + 2) / 4096f, c = b.getShort(p + 4) / 4096f;
            java.util.Arrays.fill(v, 0);
            v[15] = 1;
            // IndexedQuadMeshBatch.lpT2 的九种轴布局。
            int[][] slots = {
                {0, 5, 6, 9, 10},
                {1, 4, 6, 8, 10},
                {2, 4, 5, 8, 9},
                {4, 1, 2, 9, 10},
                {5, 0, 2, 8, 10},
                {6, 0, 1, 8, 9},
                {8, 1, 2, 5, 6},
                {9, 0, 2, 4, 6},
                {10, 0, 1, 4, 5}
            };
            int[] s = slots[axis];
            v[s[0]] = (flags & 16) == 0 ? 1 : -1;
            v[s[1]] = a;
            v[s[2]] = c;
            v[s[3]] = (flags & 32) == 0 ? c : -c;
            v[s[4]] = (flags & 64) == 0 ? a : -a;
        } else {
            int p = basis + (reference & 32767) * 10;
            short[] packed = new short[5];
            for (int i = 0; i < 5; i++) packed[i] = b.getShort(p + i * 2);
            int assembled =
                    (packed[4] & 7) << 12
                            | (packed[0] & 7) << 9
                            | (packed[1] & 7) << 6
                            | (packed[2] & 7) << 3
                            | packed[3] & 7;
            v[0] = (packed[0] >> 3) / 4096f;
            v[1] = (packed[1] >> 3) / 4096f;
            v[2] = (packed[2] >> 3) / 4096f;
            v[4] = (packed[3] >> 3) / 4096f;
            v[5] = (packed[4] >> 3) / 4096f;
            v[6] = ((short) (assembled << 3) >> 3) / 4096f;
            Vector3 cross = new Vector3(v[0], v[1], v[2]).crs(v[4], v[5], v[6]);
            v[8] = cross.x;
            v[9] = cross.y;
            v[10] = cross.z;
        }
        return result;
    }
}
