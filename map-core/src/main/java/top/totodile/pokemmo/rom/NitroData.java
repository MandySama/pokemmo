package top.totodile.pokemmo.rom;

import com.badlogic.gdx.math.Matrix4;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static top.totodile.pokemmo.rom.NdsArchive.*;

/** Nitro 字典及定点矩阵的公共读取规则。 */
final class NitroData {
    record Entry(String name, int offset) {
    }

    static List<Entry> dictionary(ByteBuffer data, int offset, int stride) {
        int count = data.get(offset + 1) & 255;
        int values = offset + 16 + count * 4;
        List<Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new Entry(text(data, values + count * stride + i * 16, 16), values + i * stride));
        }
        return entries;
    }

    static float fixed(ByteBuffer data) {
        return data.getInt() / 4096f;
    }

    static Matrix4 pivot(int axis, int signs, float a, float b) {
        float sign = (signs & 1) == 0 ? 1 : -1;
        float c = (signs & 2) == 0 ? b : -b;
        float d = (signs & 4) == 0 ? a : -a;
        float[][] rows = switch (axis) {
            case 0 -> new float[][]{{sign, 0, 0}, {0, a, b}, {0, c, d}};
            case 1 -> new float[][]{{0, sign, 0}, {a, 0, b}, {c, 0, d}};
            case 2 -> new float[][]{{0, 0, sign}, {a, b, 0}, {c, d, 0}};
            case 3 -> new float[][]{{0, a, b}, {sign, 0, 0}, {0, c, d}};
            case 4 -> new float[][]{{a, 0, b}, {0, sign, 0}, {c, 0, d}};
            case 5 -> new float[][]{{a, b, 0}, {0, 0, sign}, {c, d, 0}};
            case 6 -> new float[][]{{0, a, b}, {0, c, d}, {sign, 0, 0}};
            case 7 -> new float[][]{{a, 0, b}, {c, 0, d}, {0, sign, 0}};
            case 8 -> new float[][]{{a, b, 0}, {c, d, 0}, {0, 0, sign}};
            default -> throw new IllegalArgumentException("Invalid pivot rotation");
        };
        Matrix4 result = new Matrix4();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) result.val[col * 4 + row] = rows[col][row];
        }
        return result;
    }

    static Matrix4 packedRotation(ByteBuffer data, int offset) {
        int[] values = new int[5];
        for (int i = 0; i < 5; i++) values[i] = data.getShort(offset + i * 2);
        int packed = (values[4] & 7) << 12 | (values[0] & 7) << 9 | (values[1] & 7) << 6
                | (values[2] & 7) << 3 | values[3] & 7;
        float[] first = {values[0] >> 3, values[1] >> 3, values[2] >> 3};
        float[] second = {values[3] >> 3, values[4] >> 3, (short) (packed << 3) >> 3};
        Matrix4 matrix = new Matrix4();
        for (int i = 0; i < 3; i++) {
            matrix.val[i] = first[i] / 4096f;
            matrix.val[4 + i] = second[i] / 4096f;
        }
        matrix.val[8] = matrix.val[1] * matrix.val[6] - matrix.val[2] * matrix.val[5];
        matrix.val[9] = matrix.val[2] * matrix.val[4] - matrix.val[0] * matrix.val[6];
        matrix.val[10] = matrix.val[0] * matrix.val[5] - matrix.val[1] * matrix.val[4];
        return matrix;
    }
}
