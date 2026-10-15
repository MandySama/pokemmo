package top.totodile.pokemmo.rom;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.nio.ByteBuffer;

import static top.totodile.pokemmo.rom.NdsArchive.*;
import static top.totodile.pokemmo.rom.NitroData.*;

/** NSBCA 骨骼动画：支持常量、逐帧以及二倍/四倍采样通道。 */
final class NitroAnimation {
    private final int frames;
    private final Channel[] channels;

    NitroAnimation(ByteBuffer data) {
        if (!text(data, 0, 4).equals("BCA0")) throw new IllegalArgumentException("Invalid NSBCA");
        int section = data.getInt(16);
        int base = section + data.getInt(dictionary(data, section + 8, 4).getFirst().offset());
        if (data.getInt(base) != 1128333386) throw new IllegalArgumentException("Invalid joint animation");
        frames = ushort(data, base + 4);
        int count = ushort(data, base + 6);
        int pivotTable = base + data.getInt(base + 12);
        int rotationTable = base + data.getInt(base + 16);
        if (frames < 1) throw new IllegalArgumentException("Empty joint animation");
        channels = new Channel[count];
        for (int i = 0; i < count; i++) {
            data.position(base + ushort(data, base + 20 + i * 2));
            int flags = data.getShort() & 65535;
            data.getShort();
            Scalar[] translation = null;
            Rotation rotation = null;
            Scalar[] scale = null;
            if ((flags & 7) == 0) {
                translation = new Scalar[3];
                for (int axis = 0; axis < 3; axis++) {
                    translation[axis] = scalar(data, base, (flags & (8 << axis)) != 0, false);
                }
            }
            if ((flags & 193) == 0) {
                rotation = rotation(data, base, pivotTable, rotationTable, (flags & 256) != 0);
            }
            if ((flags & 1537) == 0) {
                scale = new Scalar[3];
                for (int axis = 0; axis < 3; axis++) {
                    scale[axis] = scalar(data, base, (flags & (2048 << axis)) != 0, true);
                }
            }
            channels[i] = new Channel(flags, translation, rotation, scale);
        }
    }

    void apply(int index, float frame, Vector3 translation, Quaternion rotation, Vector3 scale) {
        if (index >= channels.length) return;
        Channel channel = channels[index];
        if ((channel.flags & 1) != 0) return;
        float time = Math.min(frame, frames - 1);
        if ((channel.flags & 2) != 0) translation.setZero();
        else if (channel.translation != null) {
            translation.set(channel.translation[0].value(time), channel.translation[1].value(time),
                    channel.translation[2].value(time));
        }
        if ((channel.flags & 64) != 0) rotation.idt();
        else if (channel.rotation != null) rotation.set(channel.rotation.value(time));
        if ((channel.flags & 512) != 0) scale.set(1, 1, 1);
        else if (channel.scale != null) {
            scale.set(channel.scale[0].value(time), channel.scale[1].value(time), channel.scale[2].value(time));
        }
    }

    private record Channel(int flags, Scalar[] translation, Rotation rotation, Scalar[] scale) {
    }

    private Scalar scalar(ByteBuffer data, int base, boolean constant, boolean scale) {
        if (constant) {
            float value = fixed(data);
            if (scale) data.getInt();
            return new Scalar(new float[]{value});
        }
        int flags = data.getInt();
        int offset = base + data.getInt();
        int stride = (flags & 0x20000000) != 0 ? 2 : 4;
        float[] values = new float[frames];
        for (int frame = 0; frame < frames; frame++) {
            Sample sample = sample(flags, frame);
            float first = scalarAt(data, offset, stride, scale, sample.first);
            float next = scalarAt(data, offset, stride, scale, sample.second);
            values[frame] = first + (next - first) * sample.fraction;
        }
        return new Scalar(values);
    }

    private static float scalarAt(ByteBuffer data, int offset, int stride, boolean scale, int index) {
        int position = offset + index * stride * (scale ? 2 : 1);
        return (stride == 2 ? data.getShort(position) : data.getInt(position)) / 4096f;
    }

    private Rotation rotation(ByteBuffer data, int base, int pivots, int rotations, boolean constant) {
        if (constant) return new Rotation(new Quaternion[]{rotationAt(data, pivots, rotations, data.getInt())});
        int flags = data.getInt();
        int offset = base + data.getInt();
        Quaternion[] values = new Quaternion[frames];
        for (int frame = 0; frame < frames; frame++) {
            Sample sample = sample(flags, frame);
            Quaternion first = rotationAt(data, pivots, rotations, ushort(data, offset + sample.first * 2));
            Quaternion next = rotationAt(data, pivots, rotations, ushort(data, offset + sample.second * 2));
            values[frame] = first.slerp(next, sample.fraction);
        }
        return new Rotation(values);
    }

    private static Quaternion rotationAt(ByteBuffer data, int pivots, int rotations, int reference) {
        Matrix4 matrix;
        if ((reference & 32768) != 0) {
            int offset = pivots + (reference & 32767) * 6;
            int flags = ushort(data, offset);
            matrix = pivot(flags & 15, flags >> 4, data.getShort(offset + 2) / 4096f,
                    data.getShort(offset + 4) / 4096f);
        } else {
            matrix = packedRotation(data, rotations + (reference & 32767) * 10);
        }
        return new Quaternion().setFromMatrix(true, matrix).nor();
    }

    private static Sample sample(int flags, int frame) {
        int step = (flags & 0x40000000) != 0 ? 2 : (flags & 0x80000000) != 0 ? 4 : 1;
        int end = flags >>> 16 & 8191;
        if (frame > end && step > 1) {
            int index = end / step + frame % step;
            return new Sample(index, index, 0);
        }
        int first = frame / step;
        return new Sample(first, frame % step == 0 ? first : first + 1, (frame % step) / (float) step);
    }

    private record Sample(int first, int second, float fraction) {
    }

    private record Scalar(float[] values) {
        float value(float frame) {
            int first = Math.min((int) frame, values.length - 1);
            int next = Math.min(first + 1, values.length - 1);
            return values[first] + (values[next] - values[first]) * (frame - (int) frame);
        }
    }

    private record Rotation(Quaternion[] values) {
        Quaternion value(float frame) {
            int first = Math.min((int) frame, values.length - 1);
            int next = Math.min(first + 1, values.length - 1);
            return new Quaternion(values[first]).slerp(values[next], frame - (int) frame);
        }
    }
}
