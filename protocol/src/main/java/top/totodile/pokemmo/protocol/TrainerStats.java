package top.totodile.pokemmo.protocol;

import java.io.IOException;

/** 名片统计由游戏服务返回；驯服上限取当前地区的徽章进度。 */
public record TrainerStats(int caught, long money, long battlePoints, long onlineSeconds, int obedience) {
    public static final int OPCODE = 0x77;
    private static final int[][] CAPS = {{20, 26, 32, 37, 46, 47, 50, 55, 62, 100},
            {20, 24, 28, 33, 35, 38, 44, 48, 58, 100},
            {20, 24, 27, 31, 35, 38, 43, 46, 56, 100},
            {20, 27, 29, 34, 37, 43, 46, 52, 60, 100},
            {20, 24, 28, 33, 35, 38, 44, 48, 58, 100}};

    public static int obedience(int region, int badges) {
        if (region < 0 || region >= CAPS.length || badges < 0 || badges > 9)
            throw new IllegalArgumentException("Invalid badge progress");
        return CAPS[region][badges];
    }

    public boolean valid() {
        return caught >= 0 && caught <= 649 && money >= 0 && battlePoints >= 0
                && onlineSeconds >= 0 && obedience >= 1 && obedience <= 100;
    }

    public static TrainerStats initial(int region, boolean unlocked) {
        return new TrainerStats(unlocked ? 649 : 0, 0, 0, 0, obedience(region, 0));
    }

    static void write(PacketData data, TrainerStats stats) {
        if (!stats.valid()) throw new IllegalArgumentException("Invalid trainer stats");
        data.putShort(stats.caught).putLong(stats.money).putLong(stats.battlePoints)
                .putLong(stats.onlineSeconds).putByte(stats.obedience);
    }

    static TrainerStats read(PacketData data) throws IOException {
        var stats = new TrainerStats(data.shortValue(), data.longValue(), data.longValue(), data.longValue(), data.byteValue());
        if (!stats.valid()) throw new IOException("Invalid trainer stats");
        return stats;
    }

    public static byte[] response(TrainerStats stats) {
        var data = new PacketData(40).putByte(OPCODE);
        write(data, stats);
        data.putLong(java.time.Instant.now().getEpochSecond() * 4);
        return data.written();
    }

    public record Snapshot(TrainerStats stats, long worldSeconds) { }

    public static TrainerStats decode(byte[] packet) throws IOException { return decodeSnapshot(packet).stats(); }

    public static Snapshot decodeSnapshot(byte[] packet) throws IOException {
        var data = new PacketData(packet);
        if (data.byteValue() != OPCODE) throw new IOException("Unexpected trainer stats response");
        var stats = read(data);
        long seconds = data.longValue();
        if (seconds < 0) throw new IOException("Invalid world clock");
        data.end();
        return new Snapshot(stats, seconds);
    }
}
