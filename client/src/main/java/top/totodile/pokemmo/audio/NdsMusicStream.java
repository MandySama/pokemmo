package top.totodile.pokemmo.audio;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.pokeemu.sseqj.Sseqj;
import org.lwjgl.system.MemoryUtil;
import top.totodile.pokemmo.rom.NdsArchive;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/** 从黑白 SDAT 合成 48kHz 双声道 PCM，保留 SSEQ 中的原生循环点。 */
public final class NdsMusicStream implements AutoCloseable {
    public static final int SAMPLE_RATE = 48_000;
    public static final int FRAMES = 960;
    private static ByteBuffer loadedSdat;
    private static Path loadedRom;
    private static long loadedModified;
    private static boolean nativeLoaded;
    private static boolean active;
    private final int track;
    private long player;

    public NdsMusicStream(Path rom, int track) throws IOException {
        this.track = track;
        synchronized (NdsMusicStream.class) {
            if (active) throw new IOException("An NDS music stream is already active");
            try {
                if (!nativeLoaded) {
                    new SharedLibraryLoader().load("sseqj");
                    if (Sseqj.getApiLevel() != 182) throw new IOException("Unsupported SSEQ native API");
                    nativeLoaded = true;
                }
                Path absolute = rom.toAbsolutePath().normalize();
                long modified = Files.getLastModifiedTime(absolute).toMillis();
                if (!absolute.equals(loadedRom) || modified != loadedModified) {
                    ByteBuffer archive = NdsArchive.resource(absolute, "/wb_sound_data.sdat");
                    validate(archive, track);
                    ByteBuffer direct = ByteBuffer.allocateDirect(archive.remaining()).order(ByteOrder.LITTLE_ENDIAN);
                    direct.put(archive).flip();
                    if (Sseqj.loadSDAT((byte) 2, direct, direct.remaining()) == 0) {
                        throw new IOException("Unable to load Black/White sound archive");
                    }
                    // 原生库按区域保留声音库；强引用持续到下一次成功替换，避免悬空指针。
                    loadedSdat = direct;
                    loadedRom = absolute;
                    loadedModified = modified;
                } else {
                    validate(loadedSdat, track);
                }
                player = Sseqj.newPlayer(SAMPLE_RATE);
                if (player == 0) throw new IOException("Unable to allocate SSEQ player");
                if (Sseqj.loadSSEQ(player, (byte) 2, track, (short) -1) == 0) {
                    throw new IOException("Unable to load login music sequence");
                }
                Sseqj.setVolume(player, 1);
                active = true;
            } catch (RuntimeException | LinkageError exception) {
                releasePlayer();
                throw new IOException("Unable to initialize NDS music", exception);
            } catch (IOException exception) {
                releasePlayer();
                throw exception;
            }
        }
    }

    public void read(ByteBuffer output) throws IOException {
        if (player == 0 || output.remaining() != FRAMES * 4 || !output.isDirect()) {
            throw new IOException("Invalid PCM output buffer");
        }
        // 合成器向目标缓冲区混音，每次生成前必须清零，避免累加上一个音频块。
        MemoryUtil.memSet(output, 0);
        if (Sseqj.isStopped(player) && Sseqj.loadSSEQ(player, (byte) 2, track, (short) -1) == 0) {
            throw new IOException("Unable to restart login music");
        }
        if (Sseqj.generateSamples(player, output, FRAMES) == 0) throw new IOException("SSEQ synthesis failed");
        output.position(0);
    }

    private static void validate(ByteBuffer data, int track) throws IOException {
        try {
            if (data.limit() < 64 || data.getInt(0) != 0x54414453 || data.getShort(4) != (short) 0xFEFF
                    || data.getInt(8) != data.limit()) throw new IOException("Invalid SDAT header");
            String[] tags = {"SYMB", "INFO", "FAT ", "FILE"};
            for (int i = 0; i < 4; i++) {
                int offset = data.getInt(16 + i * 8);
                int length = data.getInt(20 + i * 8);
                if (i == 0 && offset == 0 && length == 0) continue;
                // SYMB 目录长度可以不含末尾的四字节对齐填充，块头长度包含填充。
                if (offset < 64 || length < 8 || (long) offset + length > data.limit()
                        || data.getInt(offset + 4) != ((length + 3L) & ~3L)
                        || (long) offset + data.getInt(offset + 4) > data.limit()) {
                    throw new IOException("Invalid SDAT block range");
                }
                for (int j = 0; j < 4; j++) {
                    if (data.get(offset + j) != tags[i].charAt(j)) throw new IOException("Invalid SDAT block tag");
                }
            }
            int info = data.getInt(24);
            int sequences = Math.addExact(info, data.getInt(info + 8));
            int count = data.getInt(sequences);
            if (track < 0 || track >= count || (long) sequences + 4L + count * 4L > data.limit()
                    || data.getInt(sequences + 4 + track * 4) == 0) throw new IOException("Missing music sequence");
            int fat = data.getInt(32);
            int files = data.getInt(fat + 8);
            if (files < 0 || (long) fat + 12L + files * 16L > (long) fat + data.getInt(36)) {
                throw new IOException("Invalid SDAT file table");
            }
            for (int i = 0; i < files; i++) {
                long start = Integer.toUnsignedLong(data.getInt(fat + 12 + i * 16));
                long length = Integer.toUnsignedLong(data.getInt(fat + 16 + i * 16));
                if (start + length > data.limit()) throw new IOException("Invalid SDAT file range");
            }
        } catch (IndexOutOfBoundsException | ArithmeticException exception) {
            throw new IOException("Invalid SDAT layout", exception);
        }
    }

    private void releasePlayer() {
        if (player != 0) {
            Sseqj.deletePlayer(player);
            player = 0;
        }
    }

    @Override
    public void close() {
        synchronized (NdsMusicStream.class) {
            if (player != 0) {
                releasePlayer();
                active = false;
            }
        }
    }
}
