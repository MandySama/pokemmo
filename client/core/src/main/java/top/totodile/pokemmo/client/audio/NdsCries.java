package top.totodile.pokemmo.client.audio;

import java.nio.*;

/** PokemonCrySoundManager.ts -> INFO wave archive -> first SWAR wave -> PCM/IMA ADPCM. */
public final class NdsCries {
    private NdsCries() {}

    public record Wave(int sampleRate, short[] samples) {}

    private static final int[] STEP = {
        7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66,
        73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408,
        449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
        2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630,
        9493, 10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794,
        32767
    };
    private static final int[] INDEX = {-1, -1, -1, -1, 2, 4, 6, 8};

    public static Wave decode(ByteBuffer bank, int species, int form) {
        var b = bank.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        int info = b.getInt(0x18), fat = b.getInt(0x20);
        int table = info + b.getInt(info + 8 + 3 * 4), count = b.getInt(table);
        if (species == 492 && form == 1) species = 650;
        if (species < 0 || species >= count) species = 1;
        int entry = info + b.getInt(table + 4 + species * 4);
        int file = Short.toUnsignedInt(b.getShort(entry));
        if (file >= b.getInt(fat + 8))
            throw new IllegalArgumentException("Invalid cry wave archive");
        int swar = b.getInt(fat + 12 + file * 16);
        if (b.getInt(swar) != 0x52415753 || b.getInt(swar + 56) < 1)
            throw new IllegalArgumentException("Missing cry SWAR");
        int wave = swar + b.getInt(swar + 60),
                format = b.get(wave) & 255,
                rate = Short.toUnsignedInt(b.getShort(wave + 2));
        int bytes = (Short.toUnsignedInt(b.getShort(wave + 6)) + b.getInt(wave + 8)) * 4;
        if (rate < 1 || bytes < 0 || wave + 12L + bytes > b.limit())
            throw new IllegalArgumentException("Invalid cry wave length");
        short[] samples;
        if (format == 0) {
            samples = new short[bytes];
            for (int i = 0; i < samples.length; i++)
                samples[i] = (short) (b.get(wave + 12 + i) << 8);
        } else if (format == 1) {
            samples = new short[bytes / 2];
            for (int i = 0; i < samples.length; i++) samples[i] = b.getShort(wave + 12 + i * 2);
        } else if (format == 2) {
            if (bytes < 4) throw new IllegalArgumentException("Truncated cry ADPCM");
            int predictor = b.getShort(wave + 12), index = Math.min(88, b.get(wave + 14) & 127);
            samples = new short[(bytes - 4) * 2 + 1];
            samples[0] = (short) predictor;
            for (int i = 1; i < samples.length; i++) {
                int packed = b.get(wave + 16 + (i - 1) / 2) & 255,
                        code = ((i - 1) & 1) == 0 ? packed & 15 : packed >>> 4;
                int step = STEP[index],
                        delta =
                                (step >> 3)
                                        + ((code & 1) != 0 ? step >> 2 : 0)
                                        + ((code & 2) != 0 ? step >> 1 : 0)
                                        + ((code & 4) != 0 ? step : 0);
                predictor =
                        Math.max(
                                -32768,
                                Math.min(32767, predictor + ((code & 8) == 0 ? delta : -delta)));
                index = Math.max(0, Math.min(88, index + INDEX[code & 7]));
                samples[i] = (short) predictor;
            }
        } else throw new IllegalArgumentException("Unknown cry encoding " + format);
        return new Wave(rate, samples);
    }
}
