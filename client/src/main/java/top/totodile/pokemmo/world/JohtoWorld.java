package top.totodile.pokemmo.world;

import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.protocol.CharacterPackets.Appearance;
import top.totodile.pokemmo.rom.HeartGoldMaps;

import java.io.IOException;

/** 城都世界使用心金／魂银卡带资源。 */
public final class JohtoWorld extends NdsWorld {
    public JohtoWorld(HeartGoldMaps maps, AvatarAssets assets, Appearance appearance)
            throws IOException {
        super(maps, assets, appearance);
    }
}
