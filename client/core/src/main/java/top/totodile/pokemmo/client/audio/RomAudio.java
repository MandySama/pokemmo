package top.totodile.pokemmo.client.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.utils.BufferUtils;
import com.pokeemu.agbplayj.Agbplayj;
import com.pokeemu.sseqj.Sseqj;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.*;

import java.nio.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Reference AudioManager + NdsSdatAudioMuxer: ROM players mixed into one 48 kHz device. */
public final class RomAudio implements AutoCloseable {
    @FunctionalInterface
    public interface Libraries {
        void load() throws Exception;
    }

    public record Track(int region, int id) {}

    private record Command(
            Object scope, int slot, Track track, float pan, float volume, long due) {}

    private final ClientResources resources;
    private final Libraries libraries;
    private final int api;
    private final ConcurrentLinkedQueue<Command> commands = new ConcurrentLinkedQueue<>();
    private final List<Voice> voices = new ArrayList<>();
    private final Map<Integer, ByteBuffer> banks = new HashMap<>();
    private final Set<Track> failed = new HashSet<>();
    private final List<Command> pending = new ArrayList<>();
    private volatile boolean closed, focused = true;
    private volatile AudioDevice device;
    private Thread thread;
    private Track requestedMusic;
    private final Object musicScope = new Object();

    public RomAudio(ClientResources resources, Libraries libraries, int api) {
        this.resources = resources;
        this.libraries = libraries;
        this.api = api;
    }

    public synchronized void music(Track track) {
        if (Objects.equals(requestedMusic, track) || closed) return;
        requestedMusic = track;
        enqueue(new Command(musicScope, 0, track, 0, 1, 0));
    }

    public void sound(
            Object scope,
            int slot,
            int region,
            int id,
            float pan,
            float volume,
            float delayMillis) {
        if (id <= 0 || closed) return;
        enqueue(
                new Command(
                        scope,
                        slot,
                        new Track(region, id),
                        pan,
                        volume,
                        System.nanoTime() + (long) (Math.max(0, delayMillis) * 1_000_000L)));
    }

    public void stop(Object scope) {
        enqueue(new Command(scope, -1, null, 0, 0, 0));
    }

    public void stopEffects() {
        enqueue(new Command(null, -1, null, 0, 0, 0));
    }

    public void cry(Object scope, int species, int form, float pan, float delayMillis) {
        sound(scope, -2, -1, species + (form << 16), pan, 1, delayMillis);
    }

    public void stopSlot(Object scope, int slot) {
        enqueue(new Command(scope, slot, null, 0, 0, 0));
    }

    public void setFocused(boolean value) {
        focused = value;
    }

    private synchronized void enqueue(Command command) {
        if (closed) return;
        commands.add(command);
        if (thread == null) {
            thread = new Thread(this::run, "rom-sound-mixer");
            thread.setDaemon(true);
            thread.setPriority(6);
            thread.start();
        }
    }

    public static Track battle(int region, boolean trainer) {
        // TileElevationProfileRecord Qs (wild) / pn0 (trainer).
        int[] wild = {297, 476, 1168, 1115, 1117}, trainers = {298, 474, 1128, 1116, 1116};
        return new Track(region, (trainer ? trainers : wild)[region]);
    }

    public float panning(float pan) {
        String value = resources.preferences().get("client.sound_engine.battle_sfx.panning", "1.0");
        try {
            return Math.max(
                    -1,
                    Math.min(
                            1,
                            pan
                                    * Float.parseFloat(value.replace("%", ""))
                                    / (value.endsWith("%") ? 100 : 1)));
        } catch (NumberFormatException invalid) {
            return pan;
        }
    }

    private float volume(boolean music) {
        if (!focused
                && Boolean.parseBoolean(
                        resources
                                .preferences()
                                .get("client.sound_engine.music_mute_when_unfocused", "true")))
            return 0;
        String key = music ? "music_volume" : "sfx_volume";
        try {
            float v =
                    Float.parseFloat(
                                    resources.preferences().get("client.sound_engine." + key, "50"))
                            / 100;
            v = Float.isFinite(v) ? Math.max(0, Math.min(1, v)) : .5f;
            return v * v;
        } catch (NumberFormatException invalid) {
            return .25f;
        }
    }

    private void loadBank(int region) throws Exception {
        if (banks.containsKey(region)) return;
        var rom = resources.roms().get(RomType.of(region));
        if (rom == null) throw new IllegalStateException("Missing ROM audio region " + region);
        banks.put(region, RomAudioBanks.load(rom));
    }

    private final class Voice implements AutoCloseable {
        final Command command;
        final boolean music, nds;
        final boolean cry;
        NdsCries.Wave wave;
        double position;
        long handle;

        Voice(Command c) throws Exception {
            command = c;
            music = c.scope == musicScope;
            nds = c.track.region >= 2;
            cry = c.track.region == -1;
            if (cry) {
                loadBank(2);
                wave = NdsCries.decode(banks.get(2), c.track.id & 65535, c.track.id >>> 16);
                return;
            }
            loadBank(c.track.region);
            handle = nds ? Sseqj.newPlayer(48000) : Agbplayj.newPlayer();
            if (handle == 0) throw new IllegalStateException("Unable to allocate audio player");
            try {
                if (load() == 0)
                    throw new IllegalStateException("Unable to load audio track " + c.track);
                if (nds) {
                    Sseqj.setPan(handle, panning(c.pan));
                    Sseqj.setPitchShift(handle, 1);
                    // SseqNativeSoundPlayer removes the map's dynamic stems until explicitly
                    // enabled.
                    if (c.track.region == 2) {
                        int[] muted =
                                switch (c.track.id) {
                                    case 1055 -> new int[] {6, 7, 8, 9};
                                    case 1057 -> new int[] {7, 8};
                                    case 1063 -> new int[] {8};
                                    default -> new int[0];
                                };
                        for (int track : muted) Sseqj.setTrackMuted(handle, track, true);
                    }
                }
            } catch (Exception | LinkageError error) {
                close();
                throw error;
            }
        }

        int load() {
            return nds
                    ? Sseqj.loadSSEQ(
                            handle, (byte) command.track.region, command.track.id, (short) -1)
                    : Agbplayj.loadSong(handle, (byte) command.track.region, command.track.id);
        }

        boolean stopped() {
            return cry
                    ? position >= wave.samples().length
                    : nds ? Sseqj.isStopped(handle) : Agbplayj.isStopped(handle);
        }

        void mix(ByteBuffer buffer) {
            float v = volume(music) * command.volume;
            if (cry) {
                float pan = panning(command.pan),
                        left = v * Math.min(1, 1 - pan),
                        right = v * Math.min(1, 1 + pan);
                for (int i = 0;
                        i < 960 && position < wave.samples().length;
                        i++, position += wave.sampleRate() / 48000d) {
                    int index = (int) position;
                    float fraction = (float) (position - index);
                    float sample =
                            wave.samples()[index] * (1 - fraction)
                                    + wave.samples()[Math.min(index + 1, wave.samples().length - 1)]
                                            * fraction;
                    for (int side = 0; side < 2; side++) {
                        int offset = i * 4 + side * 2,
                                mixed =
                                        buffer.getShort(offset)
                                                + Math.round(sample * (side == 0 ? left : right));
                        buffer.putShort(offset, (short) Math.max(-32768, Math.min(32767, mixed)));
                    }
                }
                return;
            }
            if (nds) {
                Sseqj.setVolume(handle, v);
                Sseqj.generateSamples(handle, buffer, 960);
            } else {
                Agbplayj.setVolume(handle, v);
                Agbplayj.generateSamples(handle, buffer, 960);
            }
        }

        public void close() {
            if (handle != 0) {
                if (nds) Sseqj.deletePlayer(handle);
                else Agbplayj.deletePlayer(handle);
                handle = 0;
            }
        }
    }

    private void accept(Command c) {
        // Stop the previous voice in this animation's logical sound slot, not other actors' sounds.
        voices.removeIf(
                v -> {
                    if ((v.command.scope == c.scope || c.scope == null && !v.music)
                            && (c.track == null && c.slot == -1 || v.command.slot == c.slot)) {
                        v.close();
                        return true;
                    }
                    return false;
                });
        if (c.track == null || failed.contains(c.track)) return;
        try {
            if (silent(c.track)) return;
            voices.add(new Voice(c));
        } catch (Exception | LinkageError error) {
            failed.add(c.track);
            Gdx.app.error("Audio", "Cannot play " + c.track, error);
        }
    }

    private boolean silent(Track track) throws Exception {
        // AudioManager.Mb0 deliberately returns a silent player for placeholder sequences.
        if (track.region == -1) return false;
        if (track.id < 1
                || track.region == 0 && track.id > 346
                || track.region == 1 && track.id > 609) return true;
        if (track.region == 2 && track.id == 1351) return true;
        if (track.region >= 2) {
            loadBank(track.region);
            var b = banks.get(track.region).duplicate().order(ByteOrder.LITTLE_ENDIAN);
            int info = b.getInt(0x18), table = info + b.getInt(info + 8);
            if (track.id >= b.getInt(table)) return true;
            int offset = b.getInt(table + 4 + track.id * 4);
            return offset == 0 || Short.toUnsignedInt(b.getShort(info + offset)) == 20041;
        }
        return false;
    }

    private void run() {
        try {
            libraries.load();
            // Official Android Sseqj is API 183; its Agbplayj remains API 182.
            if (Sseqj.getApiLevel() != api || Agbplayj.getApiLevel() != 182)
                throw new IllegalStateException("Mismatched ROM audio native API");
            if (closed) return;
            device = Gdx.audio.newAudioDevice(48000, false);
            var buffer = ByteBuffer.allocateDirect(3840).order(ByteOrder.nativeOrder());
            short[] samples = new short[1920];
            while (!closed) {
                Command next;
                while ((next = commands.poll()) != null) {
                    if (next.track == null) {
                        Command stop = next;
                        pending.removeIf(
                                c ->
                                        (c.scope == stop.scope
                                                        || stop.scope == null
                                                                && c.scope != musicScope)
                                                && (stop.slot == -1 || c.slot == stop.slot));
                        accept(stop);
                    } else pending.add(next);
                }
                long now = System.nanoTime();
                for (var it = pending.iterator(); it.hasNext(); ) {
                    var c = it.next();
                    if (c.due <= now) {
                        accept(c);
                        it.remove();
                    }
                }
                buffer.clear();
                BufferUtils.clear(buffer, buffer.capacity());
                for (var it = voices.iterator(); it.hasNext(); ) {
                    var voice = it.next();
                    if (voice.stopped()) {
                        if (voice.music) voice.load();
                        else {
                            voice.close();
                            it.remove();
                            continue;
                        }
                    }
                    buffer.position(0);
                    voice.mix(buffer);
                }
                buffer.position(0);
                buffer.asShortBuffer().get(samples);
                if (!closed) device.writeSamples(samples, 0, samples.length);
            }
        } catch (Exception | LinkageError error) {
            if (!closed) Gdx.app.error("Audio", "ROM mixer failed", error);
        } finally {
            voices.forEach(Voice::close);
            voices.clear();
            pending.clear();
            commands.clear();
            if (device != null) {
                device.dispose();
                device = null;
            }
            banks.clear();
        }
    }

    @Override
    public void close() {
        closed = true;
        if (device != null) device.setVolume(0);
        Thread worker = thread;
        if (worker != null) {
            worker.interrupt();
            try {
                worker.join(2000);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
