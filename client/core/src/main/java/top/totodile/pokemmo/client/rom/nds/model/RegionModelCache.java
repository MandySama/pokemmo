package top.totodile.pokemmo.client.rom.nds.model;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.zip.CRC32;

/**
 * RegionModelCacheManager/CW 及 NitroModelRenderer 的 ModelData 适配。 地区文件有完成标记、格式版本、ROM
 * 指纹和逐条校验；未完成的临时文件不参与读取。
 */
public final class RegionModelCache implements AutoCloseable {
    private static final int MAGIC = 0x504d4d43, VERSION = 4;

    private record Record(long offset, int size) {}

    private MappedByteBuffer data;
    private final Map<String, Record> records;
    private int hits, misses;

    private RegionModelCache(MappedByteBuffer data, Map<String, Record> records) {
        this.data = data;
        this.records = Map.copyOf(records);
    }

    public int size() {
        return records.size();
    }

    public int hits() {
        return hits;
    }

    public int misses() {
        return misses;
    }

    public static byte[] fingerprint(Path file) throws IOException {
        MessageDigest digest = digest();
        byte[] buffer = new byte[65536];
        try (InputStream input = Files.newInputStream(file)) {
            for (int n; (n = input.read(buffer)) != -1; ) digest.update(buffer, 0, n);
        }
        return digest.digest();
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static String key(byte[] model, byte[] texture, List<byte[]> animations) {
        MessageDigest d = digest();
        block(d, model);
        block(d, texture);
        for (byte[] animation : animations) block(d, animation);
        return HexFormat.of().formatHex(d.digest());
    }

    private static void block(MessageDigest digest, byte[] data) {
        int length = data == null ? -1 : data.length;
        digest.update(
                new byte[] {
                    (byte) (length >>> 24),
                    (byte) (length >>> 16),
                    (byte) (length >>> 8),
                    (byte) length
                });
        if (data != null) digest.update(data);
    }

    public static RegionModelCache open(Path file, int region, byte[] fingerprint)
            throws IOException {
        Map<String, Record> records = new HashMap<>();
        try (var input = new RandomAccessFile(file.toFile(), "r")) {
            long length = input.length();
            if (length < 48 || length > Integer.MAX_VALUE)
                throw new IOException("Invalid model cache size");
            if (input.readInt() != MAGIC || input.readInt() != VERSION || input.readInt() != region)
                throw new IOException("Model cache version mismatch");
            byte[] stored = new byte[32];
            input.readFully(stored);
            if (!Arrays.equals(stored, fingerprint))
                throw new IOException("Model cache ROM mismatch");
            int count = input.readInt();
            if (count <= 0 || count > 100000) throw new IOException("Incomplete model cache");
            // 校验使用可复用的小缓冲，不让启动校验将所有映射页载入进程工作集。
            byte[] buffer = new byte[65536];
            for (int i = 0; i < count; i++) {
                input.readFully(stored);
                String key = HexFormat.of().formatHex(stored);
                int size = input.readInt(), checksum = input.readInt();
                long offset = input.getFilePointer();
                if (size < 1 || size > 64 * 1024 * 1024 || size > length - offset)
                    throw new IOException("Truncated model cache");
                CRC32 crc = new CRC32();
                for (int left = size; left > 0; ) {
                    int n = Math.min(left, buffer.length);
                    input.readFully(buffer, 0, n);
                    crc.update(buffer, 0, n);
                    left -= n;
                }
                if ((int) crc.getValue() != checksum
                        || records.put(key, new Record(offset, size)) != null)
                    throw new IOException("Corrupt model cache");
            }
            if (input.getFilePointer() != length)
                throw new IOException("Invalid model cache length");
            // BinaryRegionModelCacheManager.CW：一次映射，运行时只访问当前需要的记录视图。
            var mapping = input.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, length);
            return new RegionModelCache(mapping, records);
        } catch (RuntimeException error) {
            throw new IOException("Invalid model cache", error);
        }
    }

    public List<NdsNitroModelParser.Parsed> read(byte[] model, byte[] textures) throws IOException {
        return read(model, textures, List.of());
    }

    public synchronized List<NdsNitroModelParser.Parsed> read(
            byte[] model, byte[] textures, List<byte[]> animations) throws IOException {
        if (data == null) throw new IOException("Model cache closed");
        Record record = records.get(key(model, textures, animations));
        if (record == null) {
            misses++;
            return NdsNitroModelParser.readAll(model, textures, animations);
        }
        var recordData = data.asReadOnlyBuffer().slice((int) record.offset, record.size);
        hits++;
        return ModelCacheCodec.decode(recordData, textures == null ? model : textures);
    }

    @Override
    public synchronized void close() {
        if (data != null) {
            unmap(data);
            data = null;
        }
    }

    // Windows 上必须明确解除映射，否则重建缓存的原子替换会被旧文件映射阻止。
    private static void unmap(ByteBuffer mapping) {
        try {
            Class<?> type = Class.forName("sun.misc.Unsafe");
            var field = type.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            type.getMethod("invokeCleaner", ByteBuffer.class).invoke(field.get(null), mapping);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Cannot release PC model cache mapping", error);
        }
    }

    public static final class Builder implements AutoCloseable {
        private final Path target, temporary;
        private final RandomAccessFile output;
        private final DataOutputStream recordsOutput;
        private final CRC32 recordCrc = new CRC32();
        private final Set<String> keys = new HashSet<>();
        private boolean committed;

        public Builder(Path target, int region, byte[] fingerprint) throws IOException {
            this.target = target;
            Files.createDirectories(target.getParent());
            temporary = Files.createTempFile(target.getParent(), "region-models-", ".tmp");
            output = new RandomAccessFile(temporary.toFile(), "rw");
            recordsOutput =
                    new DataOutputStream(
                            new BufferedOutputStream(
                                    new OutputStream() {
                                        @Override
                                        public void write(int value) throws IOException {
                                            output.write(value);
                                            recordCrc.update(value);
                                        }

                                        @Override
                                        public void write(byte[] bytes, int offset, int length)
                                                throws IOException {
                                            output.write(bytes, offset, length);
                                            recordCrc.update(bytes, offset, length);
                                        }
                                    },
                                    65536));
            output.writeInt(MAGIC);
            output.writeInt(VERSION);
            output.writeInt(region);
            output.write(fingerprint);
            output.writeInt(-1);
        }

        public void add(byte[] model, byte[] textures) throws IOException {
            add(model, textures, List.of());
        }

        public void add(byte[] model, byte[] textures, List<byte[]> animations) throws IOException {
            if (Thread.currentThread().isInterrupted())
                throw new InterruptedIOException("Model precache cancelled");
            String key = key(model, textures, animations);
            if (keys.contains(key)) return;
            var parsed = NdsNitroModelParser.readAll(model, textures, animations);
            output.write(HexFormat.of().parseHex(key));
            long header = output.getFilePointer();
            output.writeInt(0);
            output.writeInt(0);
            long start = output.getFilePointer();
            recordCrc.reset();
            // RomModelCacheBuilder：直接流式写入记录，避免整条模型的大字节数组及复制。
            ModelCacheCodec.write(recordsOutput, parsed);
            recordsOutput.flush();
            long end = output.getFilePointer();
            if (end - start < 1 || end - start > 64 * 1024 * 1024)
                throw new IOException("Invalid model cache record size");
            output.seek(header);
            output.writeInt((int) (end - start));
            output.writeInt((int) recordCrc.getValue());
            output.seek(end);
            keys.add(key);
        }

        public void commit() throws IOException {
            if (keys.isEmpty()) throw new IOException("Empty model cache");
            output.seek(44);
            output.writeInt(keys.size());
            output.getFD().sync();
            output.close();
            try {
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            committed = true;
        }

        @Override
        public void close() throws IOException {
            output.close();
            if (!committed) Files.deleteIfExists(temporary);
        }
    }
}
