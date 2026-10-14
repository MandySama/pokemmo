package top.totodile.pokemmo.world;

import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.protocol.CharacterPackets.Appearance;
import top.totodile.pokemmo.rom.PlatinumMaps;
import java.io.IOException;

/** 神奥世界使用白金卡带资源。 */
public final class SinnohWorld extends NdsWorld {
    public SinnohWorld(PlatinumMaps maps, AvatarAssets assets, Appearance appearance) throws IOException {
        super(maps, assets, appearance);
    }
}
