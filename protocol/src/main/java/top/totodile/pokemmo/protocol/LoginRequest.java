package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** 登录请求 0x11；密码字段为 UTF-8 明文的 SHA-1 十六进制摘要。 */
public record LoginRequest(String username, String passwordDigest, boolean remember, String language, byte[] credential) {
    public LoginRequest(String username, String passwordDigest, boolean remember, String language) {
        this(username, passwordDigest, remember, language, new byte[0]);
    }

    public byte[] encode() {
        if (username.length() > 64 || (credential.length == 0 && passwordDigest.length() != 40)
                || credential.length > 255 || language.length() > 16) {
            throw new IllegalArgumentException("Invalid login fields");
        }
        PacketData data = new PacketData(1024).putByte(0x11).putString(username).putByte(0).putByte(0);
        if (credential.length > 0) {
            data.putByte(1).putByte(credential.length).putBytes(credential);
        } else {
            data.putByte(0).putString(passwordDigest).putByte(remember ? 1 : 0);
        }
        return data.putString(language).putInt(0).putInt(0).putByte(0).putByte(0).written();
    }

    public static LoginRequest decode(byte[] bytes) throws IOException {
        PacketData data = new PacketData(bytes);
        if (data.byteValue() != 0x11) throw new IOException("Unexpected login request opcode");
        String username = data.string(64);
        if (data.booleanValue()) throw new IOException("Reconnect login is not enabled");
        data.bytes(data.byteValue());
        boolean saved = data.booleanValue();
        byte[] credential = saved ? data.bytes(data.byteValue()) : new byte[0];
        if (saved && credential.length == 0) throw new IOException("Empty login credential");
        String digest = saved ? "" : data.string(40);
        boolean remember = !saved && data.booleanValue();
        String language = data.string(16);
        data.intValue();
        data.intValue();
        data.byteValue();
        data.bytes(data.byteValue());
        data.end();
        if (!saved && !digest.matches("[0-9a-fA-F]{40}")) throw new IOException("Invalid password digest");
        return new LoginRequest(username, digest, remember, language, credential);
    }

    public static String digest(String password) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
