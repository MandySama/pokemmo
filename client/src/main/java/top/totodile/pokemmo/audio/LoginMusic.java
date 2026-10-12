package top.totodile.pokemmo.audio;

import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/** 登录音乐在独立线程合成和播放，管理窗口与服务器选择不重启音轨。 */
public final class LoginMusic implements AutoCloseable {
    private static final System.Logger LOGGER = System.getLogger(LoginMusic.class.getName());
    private final Object signal = new Object();
    private final Thread worker;
    private volatile Selection requested;
    private volatile boolean closed;
    private volatile boolean playing;
    private volatile int volume = 50;
    private volatile boolean muteWhenUnfocused = true;
    private volatile boolean focused;
    private long nextCheck;

    private record Selection(Path rom, long modified, int track) {
    }

    public LoginMusic() {
        worker = Thread.ofPlatform().daemon().name("login-music").start(this::run);
    }

    public void update(Path rom) {
        if (closed || System.nanoTime() < nextCheck) return;
        nextCheck = System.nanoTime() + 1_000_000_000L;
        Path absolute = rom == null ? null : rom.toAbsolutePath().normalize();
        long modified = 0;
        try {
            if (absolute != null) modified = Files.getLastModifiedTime(absolute).toMillis();
        } catch (IOException exception) {
            absolute = null;
        }
        Selection previous = requested;
        if (previous == null && absolute == null) return;
        if (previous != null && Objects.equals(previous.rom(), absolute) && previous.modified() == modified) return;
        synchronized (signal) {
            // 普通登录使用 1159，1/50 概率使用备用曲目 1157。
            requested = absolute == null ? null : new Selection(absolute, modified,
                    ThreadLocalRandom.current().nextInt(50) == 0 ? 1157 : 1159);
            worker.interrupt();
            signal.notifyAll();
        }
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
    }

    public void setMuteWhenUnfocused(boolean mute) {
        muteWhenUnfocused = mute;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isPlaying() {
        return playing;
    }

    private float gain() {
        if (muteWhenUnfocused && !focused) return 0;
        float level = volume / 100f;
        return level * level;
    }

    private void run() {
        ByteBuffer samples = MemoryUtil.memAlloc(NdsMusicStream.FRAMES * 4).order(ByteOrder.nativeOrder());
        try {
            while (!closed) {
                Selection selection = requested;
                if (selection == null) {
                    awaitChange(null);
                    continue;
                }
                Thread.interrupted();
                try (NdsMusicStream stream = new NdsMusicStream(selection.rom(), selection.track());
                     OpenAlMusicOutput output = new OpenAlMusicOutput()) {
                    while (!closed && requested == selection) {
                        samples.clear();
                        stream.read(samples);
                        output.write(samples, gain());
                        playing = output.isPlaying();
                    }
                } catch (IOException | RuntimeException | LinkageError exception) {
                    LOGGER.log(System.Logger.Level.WARNING, "Login music unavailable: {0}", exception.getMessage());
                    // 同一份资源失败后等待文件变化，不在每帧重复打开设备或输出错误。
                    playing = false;
                    awaitChange(selection);
                } catch (InterruptedException exception) {
                    Thread.interrupted();
                } finally {
                    playing = false;
                }
            }
        } finally {
            MemoryUtil.memFree(samples);
        }
    }

    private void awaitChange(Selection selection) {
        synchronized (signal) {
            while (!closed && requested == selection) {
                try {
                    signal.wait();
                } catch (InterruptedException exception) {
                    if (closed) return;
                }
            }
        }
    }

    @Override
    public void close() {
        closed = true;
        worker.interrupt();
        synchronized (signal) {
            signal.notifyAll();
        }
        try {
            worker.join(2000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
