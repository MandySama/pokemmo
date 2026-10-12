package top.totodile.pokemmo.protocol;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Arrays;

/** 小端长度帧、ECDSA/ECDH 握手及连续 AES-CTR 流；加密封包使用带序号的 HMAC。 */
public final class SecureChannel implements AutoCloseable {
    private static final long NONCE_MASK = 3214621489648854472L;
    private static final long TIME_MASK = -4214651440992349575L;
    private static final int MAX_FRAME = 4096;
    private final Socket socket;
    private final int hashSize;
    private final boolean gameFraming;
    private boolean serverSide;
    private Direction outgoing;
    private Direction incoming;

    public SecureChannel(Socket socket) {
        this(socket, false);
    }

    /** 游戏通道使用 2 字节 HMAC，服务端封包在 opcode 后带未压缩标记。 */
    public SecureChannel(Socket socket, boolean gameFraming) {
        this.socket = socket;
        this.gameFraming = gameFraming;
        hashSize = gameFraming ? 2 : 16;
    }

    public void clientHandshake(PublicKey trustedRoot) throws IOException, GeneralSecurityException {
        long nonce = new SecureRandom().nextLong();
        writeFrame(new PacketData(17).putByte(0).putLong(nonce ^ NONCE_MASK)
                .putLong(System.currentTimeMillis() ^ TIME_MASK ^ nonce).written());
        PacketData hello = new PacketData(readFrame());
        if (hello.byteValue() != 1) throw new IOException("Unexpected server hello");
        int pointLength = hello.shortValue();
        if (pointLength != 65) throw new IOException("Invalid server public key length");
        byte[] point = hello.bytes(pointLength);
        int signatureLength = hello.shortValue();
        if (signatureLength < 8 || signatureLength > 128) throw new IOException("Invalid root signature length");
        byte[] signature = hello.bytes(signatureLength);
        if (hello.byteValue() != hashSize) throw new IOException("Unsupported packet hash size");
        hello.end();
        Signature verifier = Signature.getInstance("SHA256withECDSA");
        verifier.initVerify(trustedRoot);
        verifier.update(point);
        if (!verifier.verify(signature)) throw new IOException("Untrusted login server signature");
        KeyPair connection = HandshakeKeys.generate();
        byte[] clientPoint = HandshakeKeys.point(connection.getPublic());
        writeFrame(new PacketData(68).putByte(2).putShort(clientPoint.length).putBytes(clientPoint).written());
        enable(connection.getPrivate(), HandshakeKeys.publicKey(point), true);
    }

    public void serverHandshake(PrivateKey rootKey) throws IOException, GeneralSecurityException {
        PacketData hello = new PacketData(readFrame());
        if (hello.byteValue() != 0) throw new IOException("Unexpected client hello");
        long nonce = hello.longValue() ^ NONCE_MASK;
        long timestamp = hello.longValue() ^ TIME_MASK ^ nonce;
        hello.end();
        long now = System.currentTimeMillis();
        if (timestamp < now - 30_000 || timestamp > now + 30_000) throw new IOException("Expired client hello");
        KeyPair connection = HandshakeKeys.generate();
        byte[] point = HandshakeKeys.point(connection.getPublic());
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(rootKey);
        signer.update(point);
        byte[] signature = signer.sign();
        writeFrame(new PacketData(256).putByte(1).putShort(point.length).putBytes(point)
                .putShort(signature.length).putBytes(signature).putByte(hashSize).written());
        PacketData ready = new PacketData(readFrame());
        if (ready.byteValue() != 2 || ready.shortValue() != 65) throw new IOException("Invalid client ready");
        PublicKey clientKey = HandshakeKeys.publicKey(ready.bytes(65));
        ready.end();
        enable(connection.getPrivate(), clientKey, false);
    }

    private void enable(PrivateKey privateKey, PublicKey publicKey, boolean client) throws GeneralSecurityException {
        serverSide = !client;
        KeyAgreement agreement = KeyAgreement.getInstance("ECDH");
        agreement.init(privateKey);
        agreement.doPhase(publicKey, true);
        byte[] secret = agreement.generateSecret();
        byte[] clientSeed = derive(secret, new byte[]{'K', 'e', 'y', 'S', 'a', 'l', 't', 1});
        byte[] serverSeed = derive(secret, new byte[]{'K', 'e', 'y', 'S', 'a', 'l', 't', 2});
        outgoing = new Direction(client ? clientSeed : serverSeed, Cipher.ENCRYPT_MODE);
        incoming = new Direction(client ? serverSeed : clientSeed, Cipher.DECRYPT_MODE);
        Arrays.fill(secret, (byte) 0);
    }

    private static byte[] derive(byte[] data, byte[] salt) throws GeneralSecurityException {
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        hash.update(salt);
        hash.update(data);
        hash.update(salt);
        return Arrays.copyOf(hash.digest(), 16);
    }

    public void send(byte[] packet) throws IOException {
        if (outgoing == null) throw new IllegalStateException("Handshake not completed");
        if (packet.length < 1 || packet.length + hashSize + 3 > MAX_FRAME) throw new IOException("Packet exceeds limit");
        if (gameFraming && serverSide) {
            byte[] framed = new byte[packet.length + 1];
            framed[0] = packet[0];
            System.arraycopy(packet, 1, framed, 2, packet.length - 1);
            packet = framed;
        }
        byte[] encrypted = outgoing.cipher.update(packet);
        byte[] frame = Arrays.copyOf(encrypted, encrypted.length + hashSize);
        System.arraycopy(outgoing.hash(encrypted, hashSize), 0, frame, encrypted.length, hashSize);
        writeFrame(frame);
    }

    public byte[] receive() throws IOException {
        if (incoming == null) throw new IllegalStateException("Handshake not completed");
        byte[] frame = readFrame();
        if (frame.length <= hashSize) throw new IOException("Encrypted packet is too short");
        byte[] encrypted = Arrays.copyOf(frame, frame.length - hashSize);
        byte[] hash = Arrays.copyOfRange(frame, encrypted.length, frame.length);
        if (!MessageDigest.isEqual(incoming.hash(encrypted, hashSize), hash)) throw new IOException("Packet integrity check failed");
        byte[] packet = incoming.cipher.update(encrypted);
        if (gameFraming && !serverSide) {
            if (packet.length < 2 || packet[1] != 0) throw new IOException("Unsupported game compression");
            byte[] payload = new byte[packet.length - 1];
            payload[0] = packet[0];
            System.arraycopy(packet, 2, payload, 1, packet.length - 2);
            return payload;
        }
        return packet;
    }

    private void writeFrame(byte[] packet) throws IOException {
        int length = packet.length + 2;
        if (length < 3 || length > MAX_FRAME) throw new IOException("Packet length exceeds limit");
        var output = socket.getOutputStream();
        output.write(length & 255);
        output.write(length >>> 8);
        output.write(packet);
        output.flush();
    }

    private byte[] readFrame() throws IOException {
        var input = socket.getInputStream();
        int low = input.read();
        int high = input.read();
        if (low < 0 || high < 0) throw new EOFException("Connection closed during frame header");
        int length = low | high << 8;
        if (length < 3 || length > MAX_FRAME) throw new IOException("Invalid packet length");
        byte[] packet = input.readNBytes(length - 2);
        if (packet.length != length - 2) throw new EOFException("Connection closed during frame body");
        return packet;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    private static final class Direction {
        private final Cipher cipher;
        private final Mac mac;
        private int round;

        private Direction(byte[] seed, int mode) throws GeneralSecurityException {
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(mode, new SecretKeySpec(seed, "AES"),
                    new IvParameterSpec(derive(seed, "IVDERIV".getBytes(StandardCharsets.US_ASCII))));
            mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(seed, "HmacSHA256"));
        }

        private byte[] hash(byte[] packet, int size) {
            mac.update(packet);
            mac.update(ByteBuffer.allocate(4).putInt(round++).array());
            return Arrays.copyOf(mac.doFinal(), size);
        }
    }
}
