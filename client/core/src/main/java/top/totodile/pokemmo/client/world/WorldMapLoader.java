package top.totodile.pokemmo.client.world;

import java.io.IOException;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.gba.FireRedMaps;
import top.totodile.pokemmo.client.rom.nds.bw.BwMaps;
import top.totodile.pokemmo.client.rom.nds.gen4.Gen4Maps;

/** The character's starting region selects its ROM; existing position IDs remain region-local. */
public final class WorldMapLoader {
    private WorldMapLoader() {}
    public static WorldMaps load(RomInfo rom) throws IOException {
        return switch(rom.type()) {
            case FIRE_RED,EMERALD -> new FireRedMaps(rom);
            case BLACK_WHITE -> new BwMaps(rom);
            case PLATINUM,HEART_GOLD_SOUL_SILVER -> new Gen4Maps(rom);
        };
    }
}
