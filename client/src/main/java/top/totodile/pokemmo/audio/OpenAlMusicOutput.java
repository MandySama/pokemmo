package top.totodile.pokemmo.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

/** 四个 20ms 缓冲区流式输出 PCM，所有 OpenAL 调用都在音频线程执行。 */
final class OpenAlMusicOutput implements AutoCloseable {
    private long device;
    private long context;
    private int source;
    private final int[] buffers = new int[4];
    private final ArrayDeque<Integer> available = new ArrayDeque<>();

    OpenAlMusicOutput() throws IOException {
        try {
            device = alcOpenDevice((ByteBuffer) null);
            if (device == 0) throw new IOException("No audio output device");
            context = alcCreateContext(device, (int[]) null);
            if (context == 0 || !alcMakeContextCurrent(context)) throw new IOException("Unable to create audio context");
            AL.createCapabilities(ALC.createCapabilities(device));
            source = alGenSources();
            alGenBuffers(buffers);
            for (int buffer : buffers) available.add(buffer);
            checkError();
        } catch (RuntimeException | LinkageError | IOException exception) {
            close();
            throw new IOException("Unable to open music output", exception);
        }
    }

    void write(ByteBuffer samples, float gain) throws IOException, InterruptedException {
        while (available.isEmpty()) {
            int processed = alGetSourcei(source, AL_BUFFERS_PROCESSED);
            while (processed-- > 0) available.add(alSourceUnqueueBuffers(source));
            if (available.isEmpty()) Thread.sleep(2);
        }
        int buffer = available.removeFirst();
        alBufferData(buffer, AL_FORMAT_STEREO16, samples, NdsMusicStream.SAMPLE_RATE);
        alSourcef(source, AL_GAIN, gain);
        alSourceQueueBuffers(source, buffer);
        if (alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING
                && alGetSourcei(source, AL_BUFFERS_QUEUED) >= 2) alSourcePlay(source);
        checkError();
    }

    boolean isPlaying() {
        return alGetSourcei(source, AL_SOURCE_STATE) == AL_PLAYING;
    }

    private static void checkError() throws IOException {
        int error = alGetError();
        if (error != AL_NO_ERROR) throw new IOException("OpenAL error: " + error);
    }

    @Override
    public void close() {
        if (source != 0) {
            alSourceStop(source);
            alDeleteSources(source);
            source = 0;
            alDeleteBuffers(buffers);
        }
        if (context != 0) {
            alcMakeContextCurrent(0);
            alcDestroyContext(context);
            context = 0;
            AL.setCurrentThread(null);
        }
        if (device != 0) {
            alcCloseDevice(device);
            device = 0;
        }
    }
}
