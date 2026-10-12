package top.totodile.pokemmo.login;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Arrays;

/** 不保存密码的登录凭据；账号密码或服务端密钥变化后自动失效。 */
final class SavedLoginCredential {
    private static final SecureRandom RANDOM = new SecureRandom();

    static byte[] issue(PrivateKey key, String username, byte[] passwordDigest) {
        byte[] token = new byte[64];
        byte[] nonce = new byte[32];
        RANDOM.nextBytes(nonce);
        System.arraycopy(nonce, 0, token, 0, 32);
        System.arraycopy(sign(key, username, passwordDigest, nonce), 0, token, 32, 32);
        return token;
    }

    static boolean verify(PrivateKey key, String username, byte[] passwordDigest, byte[] token) {
        return token.length == 64 && MessageDigest.isEqual(Arrays.copyOfRange(token, 32, 64),
                sign(key, username, passwordDigest, Arrays.copyOf(token, 32)));
    }

    private static byte[] sign(PrivateKey key, String username, byte[] passwordDigest, byte[] nonce) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getEncoded(), "HmacSHA256"));
            mac.update("saved-login\0".getBytes(StandardCharsets.UTF_8));
            mac.update(username.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) 0);
            mac.update(passwordDigest);
            return mac.doFinal(nonce);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
