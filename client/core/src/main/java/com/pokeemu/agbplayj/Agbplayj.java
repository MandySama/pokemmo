package com.pokeemu.agbplayj;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/** JNI signatures from the reference AgbplayNativeSoundPlayer / NdsSdatAudioMuxer. */
public abstract class Agbplayj {
    public static native int getApiLevel();

    public static native long newPlayer();

    public static native int loadRom(byte region, ByteBuffer data, int size);

    public static native int loadSong(long player, byte region, int song);

    public static native int generateSamples(long player, Buffer output, int frames);

    public static native boolean isStopped(long player);

    public static native void setVolume(long player, float volume);

    public static native void deletePlayer(long player);
}
