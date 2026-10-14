package top.totodile.pokemmo.world;

import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.protocol.CharacterPackets.Appearance;
import top.totodile.pokemmo.rom.BlackWhiteMaps;
import java.io.IOException;

/** 合众世界使用黑白卡带资源。 */
public final class UnovaWorld extends NdsWorld {
    public UnovaWorld(BlackWhiteMaps maps, AvatarAssets assets, Appearance appearance) throws IOException {
        super(maps, assets, appearance);
    }
}
