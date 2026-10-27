package top.totodile.pokemmo.client.rom.gba;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.RomInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/** GbaTextDecoder.TG0 / BinaryArchiveHeaderReader: canonical offsets are ROM-revision specific. */
public final class GbaStoryText {
    private final byte[] rom;
    private final Map<Integer, Integer> offsets;

    public GbaStoryText(ClientResources resources, RomInfo info) throws IOException {
        rom = Files.readAllBytes(info.path());
        var file = resources.apply("offsets/" + info.code() + "v1." + info.revision() + ".dat");
        if (!file.exists()) {
            if ((!info.code().equals("BPRE") && !info.code().equals("BPEE")) || info.revision() != 0)
                throw new IOException("Missing ROM story offset conversion");
            offsets = null;
            return;
        }
        try (var input = new DataInputStream(file.read())) {
            if (!new String(input.readNBytes(4), StandardCharsets.US_ASCII).equals(info.code())
                    || input.readUnsignedByte() != info.revision())
                throw new IOException("Invalid story offset header");
            int count = input.readInt();
            if (count < 0 || count > 100000) throw new IOException("Invalid story offset count");
            var values = new HashMap<Integer, Integer>();
            for (int i = 0; i < count; i++) values.put(input.readInt(), input.readInt());
            offsets = Map.copyOf(values);
        }
    }

    public String get(int canonical) {
        Integer offset = offsets == null ? canonical : offsets.get(canonical);
        if (offset == null)
            throw new IllegalArgumentException("Missing story text conversion: " + canonical);
        return GbaText.decode(rom, offset);
    }
}
