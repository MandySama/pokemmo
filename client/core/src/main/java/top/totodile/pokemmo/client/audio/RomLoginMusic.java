package top.totodile.pokemmo.client.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.utils.BufferUtils;
import com.pokeemu.sseqj.Sseqj;
import java.nio.*;
import top.totodile.pokemmo.client.config.ClientSettings;
import top.totodile.pokemmo.client.rom.*;

/** 参考 AudioManager.bk0 的登录默认曲目：合众 SDAT 1159，48kHz 立体声。 */
public final class RomLoginMusic implements AutoCloseable {
    @FunctionalInterface public interface NativeLibrary { void load() throws Exception; }
    private static boolean loaded;
    private static final Object SYNTH=new Object();
    private final Thread thread;
    private final ClientSettings settings;
    private volatile boolean closed, focused=true;
    private volatile AudioDevice output;
    private ByteBuffer archive;

    public RomLoginMusic(RomInfo rom, ClientSettings settings, NativeLibrary nativeLibrary,int expectedApi) {
        this.settings=settings;
        thread=new Thread(() -> play(rom,nativeLibrary,expectedApi),"login-music");
        thread.setDaemon(true); thread.setPriority(6); thread.start();
    }
    private void play(RomInfo rom,NativeLibrary nativeLibrary,int expectedApi) {
        long player=0;
        try {
            synchronized (SYNTH) {
                if (!loaded) { nativeLibrary.load(); loaded=true; }
                if (Sseqj.getApiLevel()!=expectedApi) throw new IllegalStateException("Mismatched Sseqj API level");
                byte[] bytes=RomReader.readFile(rom,"wb_sound_data.sdat");
                archive=ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.LITTLE_ENDIAN);
                archive.put(bytes).flip();
                if (Sseqj.loadSDAT((byte)2,archive,archive.remaining())==0) throw new IllegalStateException("Unable to load SDAT");
                player=Sseqj.newPlayer(48000);
                if (player==0 || Sseqj.loadSSEQ(player,(byte)2,1159,(short)-1)==0) throw new IllegalStateException("Unable to load login SSEQ");
            }
            if (closed) return;
            output=Gdx.audio.newAudioDevice(48000,false);
            ByteBuffer buffer=ByteBuffer.allocateDirect(960*4).order(ByteOrder.nativeOrder());
            short[] samples=new short[1920];
            while (!closed) {
                float volume=volume(settings,focused);
                synchronized (SYNTH) {
                    if (closed) break;
                    if (Sseqj.isStopped(player)) Sseqj.loadSSEQ(player,(byte)2,1159,(short)-1);
                    // 与 SseqNativeSoundPlayer.aw 一致：在合成阶段应用音量。
                    Sseqj.setVolume(player,volume);
                    generateSamples(player,buffer);
                }
                buffer.asShortBuffer().get(samples);
                if (!closed) output.writeSamples(samples,0,samples.length);
            }
        } catch (Exception | LinkageError error) {
            if (!closed) Gdx.app.error("Login music","Unable to play ROM login music",error);
        } finally {
            if (output!=null) { output.dispose(); output=null; }
            if (player!=0) synchronized (SYNTH) { Sseqj.deletePlayer(player); }
            archive=null;
        }
    }
    public static void generateSamples(long player,ByteBuffer buffer) {
        // Sseqj 在现有 PCM 上混音；clear() 只重置游标，不会清除上一批采样。
        buffer.clear();
        BufferUtils.clear(buffer,buffer.capacity());
        if (Sseqj.generateSamples(player,buffer,960)==0)
            throw new IllegalStateException("Unable to synthesize login music");
    }
    public static float volume(ClientSettings settings,boolean focused) {
        if (!focused && Boolean.parseBoolean(settings.get("client.sound_engine.music_mute_when_unfocused","true"))) return 0;
        float volume;
        try { volume=Float.parseFloat(settings.get("client.sound_engine.music_volume","50"))/100f; }
        catch (NumberFormatException invalid) { volume=0.5f; }
        volume=Math.max(0,Math.min(1,Float.isFinite(volume)?volume:0.5f));
        return volume*volume;
    }
    public void setFocused(boolean focused) { this.focused=focused; }
    @Override public void close() {
        closed=true;
        AudioDevice device=output;
        if (device!=null) device.setVolume(0);
        thread.interrupt();
        // 必须在 libGDX 销毁 OpenAL 上下文之前释放设备和原生句柄。
        try { thread.join(2000); }
        catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
    }
}
