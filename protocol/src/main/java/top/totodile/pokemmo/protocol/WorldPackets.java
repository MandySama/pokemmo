package top.totodile.pokemmo.protocol;

import java.io.IOException;

import static top.totodile.pokemmo.protocol.CharacterPackets.*;

/** 移动携带起点与方向；地图、落点和耗时由游戏服务确认。 */
public final class WorldPackets {
    public static final int MOVE = 6;
    public static final int SAVE = 0x70;
    public static final int SETTINGS = 0x72;
    public static final int LOADED = 0x73;
    public static final int TELEPORT = 0x74;
    public static final int RESULT = 0x75;

    private WorldPackets() {
    }

    public record Move(int x, int y, int direction, boolean running) {
    }

    public record Result(Position position, boolean moved, boolean warp, int millis, Position approach) {
        public Result(Position position, boolean moved, boolean warp, int millis) {
            this(position, moved, warp, millis, null);
        }
    }

    public static byte[] move(int x, int y, int direction, boolean running) {
        return new PacketData(6).putByte(MOVE).putShort(x).putShort(y)
                .putByte(direction | (running ? 0x80 : 0)).written();
    }

    public static Move decodeMove(byte[] bytes) throws IOException {
        PacketData data = new PacketData(bytes);
        if (data.byteValue() != MOVE) throw new IOException("Invalid movement opcode");
        int x = data.shortValue(), y = data.shortValue(), flags = data.byteValue();
        data.end();
        if ((flags & 0x7C) != 0) throw new IOException("Invalid movement flags");
        return new Move(x, y, flags & 3, (flags & 0x80) != 0);
    }

    public static byte[] settings(boolean cycling, int multiplier, boolean noclip) {
        return new PacketData(4).putByte(SETTINGS).putByte(cycling ? 1 : 0)
                .putByte(multiplier).putByte(noclip ? 1 : 0).written();
    }

    public static byte[] result(Result result) {
        PacketData data = new PacketData(64).putByte(RESULT).putByte(result.moved() ? 1 : 0)
                .putByte(result.warp() ? 1 : 0).putShort(result.millis());
        CharacterPackets.writePosition(data, result.position());
        data.putByte(result.approach() == null ? 0 : 1);
        if (result.approach() != null) CharacterPackets.writePosition(data, result.approach());
        return data.written();
    }

    public static Result decodeResult(byte[] bytes) throws IOException {
        PacketData data = new PacketData(bytes);
        if (data.byteValue() != RESULT) throw new IOException("Invalid movement result");
        boolean moved = data.booleanValue(), warp = data.booleanValue();
        int millis = data.shortValue();
        if (millis < 1 || millis > 1000) throw new IOException("Invalid movement duration");
        Position position = CharacterPackets.readPosition(data);
        Position approach = data.booleanValue() ? CharacterPackets.readPosition(data) : null;
        data.end();
        if (approach != null && (!warp || !moved)) throw new IOException("Unexpected warp approach");
        return new Result(position, moved, warp, millis, approach);
    }
}
