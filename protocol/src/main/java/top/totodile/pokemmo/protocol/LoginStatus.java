package top.totodile.pokemmo.protocol;

import java.io.IOException;

/** 登录业务结果码，不包含连接失败等本地网络状态。 */
public enum LoginStatus {
    AUTHENTICATED(0),
    SYSTEM_ERROR(1),
    INVALID_PASSWORD(2);

    private final int code;

    LoginStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public byte[] encode() {
        PacketData data = new PacketData(4).putByte(1).putByte(code);
        if (this == AUTHENTICATED) data.putString("");
        return data.written();
    }

    public static LoginStatus decode(byte[] bytes) throws IOException {
        PacketData data = new PacketData(bytes);
        if (data.byteValue() != 1) throw new IOException("Unexpected login result opcode");
        int code = data.byteValue();
        for (LoginStatus status : values()) {
            if (status.code == code) {
                if (status == AUTHENTICATED) data.string(256);
                data.end();
                return status;
            }
        }
        throw new IOException("Unknown login result code");
    }
}
