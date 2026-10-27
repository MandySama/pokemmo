package top.totodile.pokemmo.client.audio;

import com.pokeemu.agbplayj.Agbplayj;
import com.pokeemu.sseqj.Sseqj;

import top.totodile.pokemmo.client.rom.*;

import java.nio.*;
import java.nio.file.Files;
import java.util.*;

/** NdsSdatAudioMuxer.t60: retain each native bank for the lifetime of the audio engine. */
final class RomAudioBanks {
    private record Bank(RomInfo rom, ByteBuffer data) {}

    private static final Map<Integer, Bank> BANKS = new HashMap<>();

    static synchronized ByteBuffer load(RomInfo rom) throws Exception {
        int region = rom.type().id;
        var current = BANKS.get(region);
        if (current != null) {
            if (!current.rom.equals(rom))
                throw new IllegalStateException("ROM audio changed; restart required");
            return current.data;
        }
        byte[] data;
        if (region < 2) data = Files.readAllBytes(rom.path());
        else {
            String path =
                    switch (region) {
                        case 2 -> "wb_sound_data.sdat";
                        case 3 -> "data/sound/pl_sound_data.sdat";
                        default -> "data/sound/gs_sound_data.sdat";
                    };
            if (!rom.files().containsKey(path))
                path =
                        rom.files().keySet().stream()
                                .filter(n -> n.toLowerCase(Locale.ROOT).endsWith(".sdat"))
                                .findFirst()
                                .orElseThrow();
            data = RomReader.readFile(rom, path);
        }
        var buffer = ByteBuffer.allocateDirect(data.length).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(data).flip();
        int result =
                region < 2
                        ? Agbplayj.loadRom((byte) region, buffer, data.length)
                        : Sseqj.loadSDAT((byte) region, buffer, data.length);
        if (result == 0) throw new IllegalStateException("Unable to load audio bank " + region);
        BANKS.put(region, new Bank(rom, buffer));
        return buffer;
    }
}
