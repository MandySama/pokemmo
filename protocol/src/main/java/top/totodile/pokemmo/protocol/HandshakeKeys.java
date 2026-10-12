package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/** P-256 密钥与未压缩曲线点转换；根公钥仅用于验证服务端握手签名。 */
public final class HandshakeKeys {
    private HandshakeKeys() {
    }

    public static KeyPair generate() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        return generator.generateKeyPair();
    }

    public static PublicKey rootPublicKey() throws IOException, GeneralSecurityException {
        String path = System.getProperty("pokemmo.login.publicKey");
        byte[] encoded;
        if (path != null) {
            encoded = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(path));
        } else {
            try (var input = HandshakeKeys.class.getResourceAsStream("/login-root.public")) {
                if (input == null) throw new IOException("Missing login root public key");
                encoded = input.readAllBytes();
            }
        }
        return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(encoded));
    }

    public static byte[] point(PublicKey key) {
        ECPublicKey ec = (ECPublicKey) key;
        byte[] result = new byte[65];
        result[0] = 4;
        coordinate(ec.getW().getAffineX(), result, 1);
        coordinate(ec.getW().getAffineY(), result, 33);
        return result;
    }

    private static void coordinate(BigInteger value, byte[] result, int offset) {
        byte[] bytes = value.toByteArray();
        int count = Math.min(32, bytes.length);
        System.arraycopy(bytes, bytes.length - count, result, offset + 32 - count, count);
    }

    public static PublicKey publicKey(byte[] point) throws GeneralSecurityException, IOException {
        if (point.length != 65 || point[0] != 4) throw new IOException("Invalid P-256 public point");
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec("secp256r1"));
        ECPoint coordinates = new ECPoint(new BigInteger(1, Arrays.copyOfRange(point, 1, 33)),
                new BigInteger(1, Arrays.copyOfRange(point, 33, 65)));
        return KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(coordinates,
                parameters.getParameterSpec(ECParameterSpec.class)));
    }
}
