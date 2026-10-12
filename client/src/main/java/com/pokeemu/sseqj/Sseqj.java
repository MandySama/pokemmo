package com.pokeemu.sseqj;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/** SSEQ 合成器的 JNI 入口；包名和方法签名与原生库导出符号对应。 */
public final class Sseqj {
    private Sseqj() {
    }

    public static native int getApiLevel();

    public static native long newPlayer(int sampleRate);

    public static native int loadSDAT(byte region, ByteBuffer data, int length);

    public static native int loadSSEQ(long player, byte region, int track, short archive);

    public static native int generateSamples(long player, Buffer output, int frames);

    public static native boolean isStopped(long player);

    public static native void setVolume(long player, float volume);

    public static native void deletePlayer(long player);
}
