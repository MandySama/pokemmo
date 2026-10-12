package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.HexFormat;

/** 有效期为一分钟的签名入服凭证；节点、账号与随机标识均包含在签名中。 */
public final class GameTicket {
    private GameTicket() {
    }

    public record Claims(int accountId, String username, int serverId, long expiresAt, String nonce) {
    }

    public static byte[] issue(int accountId, String username, int serverId, PrivateKey key)
            throws GeneralSecurityException {
        byte[] nonce = new byte[16];
        new SecureRandom().nextBytes(nonce);
        byte[] body = new PacketData(180).putByte(1).putInt(accountId).putString(username).putByte(serverId)
                .putLong(System.currentTimeMillis() + 60_000).putBytes(nonce).written();
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(key);
        signer.update(body);
        byte[] signature = signer.sign();
        return new PacketData(255).putShort(body.length).putBytes(body).putByte(signature.length)
                .putBytes(signature).written();
    }

    public static Claims verify(byte[] ticket, PublicKey key, int serverId, long now)
            throws IOException, GeneralSecurityException {
        if (ticket.length > 255) throw new IOException("Ticket too large");
        PacketData data = new PacketData(ticket);
        byte[] body = data.bytes(data.shortValue());
        byte[] signature = data.bytes(data.byteValue());
        data.end();
        Signature verifier = Signature.getInstance("SHA256withECDSA");
        verifier.initVerify(key);
        verifier.update(body);
        if (!verifier.verify(signature)) throw new IOException("Invalid ticket signature");
        PacketData fields = new PacketData(body);
        if (fields.byteValue() != 1) throw new IOException("Unsupported ticket version");
        Claims claims = new Claims(fields.intValue(), fields.string(64), fields.byteValue(), fields.longValue(),
                HexFormat.of().formatHex(fields.bytes(16)));
        fields.end();
        if (claims.accountId() < 1 || claims.username().isBlank() || claims.serverId() != serverId
                || claims.expiresAt() <= now || claims.expiresAt() > now + 60_000) {
            throw new IOException("Expired or mismatched game ticket");
        }
        return claims;
    }
}
