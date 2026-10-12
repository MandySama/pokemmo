package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

/** 私钥由服务模块资源或指定的 PKCS#8 DER 文件加载。 */
public final class ServerKeys {
    private ServerKeys() {
    }

    public static PrivateKey load(Class<?> owner, String property) throws IOException, GeneralSecurityException {
        String path = System.getProperty(property);
        byte[] bytes;
        if (path != null) {
            bytes = Files.readAllBytes(Path.of(path));
        } else {
            try (var input = owner.getResourceAsStream("/login-root.private")) {
                if (input == null) throw new IOException("Missing server private key");
                bytes = input.readAllBytes();
            }
        }
        return KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }
}
