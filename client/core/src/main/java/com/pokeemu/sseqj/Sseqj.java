package com.pokeemu.sseqj;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/** PC / Android 共用的 JNI 名称，与参考客户端的 Sseqj 合成器一致。 */
public abstract class Sseqj {
    public static native int getApiLevel();

    public static native long newPlayer(int sampleRate);

    public static native int loadSDAT(byte region, ByteBuffer data, int size);

    public static native int loadSSEQ(long player, byte region, int track, short archive);

    public static native int generateSamples(long player, Buffer output, int frames);

    public static native boolean isStopped(long player);

    public static native void setVolume(long player, float volume);

    public static native void setPan(long player, float pan);

    public static native void setTrackMuted(long player, int track, boolean muted);

    public static native void setPitchShift(long player, float pitch);

    public static native void deletePlayer(long player);
}
