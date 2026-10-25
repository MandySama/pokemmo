package top.totodile.pokemmo.client.startup;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.HexFormat;

/** 随 PC 包交付的 Sseqj 音乐合成库；按内容散列释放，运行时不依赖参考工程目录。 */
final class NativeTools {
    private NativeTools() {}

    static Path extract(Path cache, String name) throws IOException {
        if (!System.getProperty("os.name").startsWith("Windows")
                || !System.getProperty("os.arch").equals("amd64"))
            throw new IOException("ROM login native tools are currently packaged for Windows x64");
        byte[] bytes;
        try (InputStream input =
                NativeTools.class.getResourceAsStream("/native-tools/windows-x86_64/" + name)) {
            if (input == null) throw new FileNotFoundException("Missing bundled " + name);
            bytes = input.readAllBytes();
        }
        Path directory = cache.resolve("native").resolve(hash(bytes).substring(0, 16));
        Files.createDirectories(directory);
        Path target = directory.resolve(name);
        if (!Files.isRegularFile(target) || !hash(Files.readAllBytes(target)).equals(hash(bytes))) {
            Path temporary = Files.createTempFile(directory, "native-", ".tmp");
            try {
                Files.write(temporary, bytes);
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporary);
            }
        }
        return target;
    }

    static String hash(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }
}
