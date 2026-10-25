package top.totodile.pokemmo.client.startup;

import top.totodile.pokemmo.client.config.ClientSettings;
import top.totodile.pokemmo.client.rom.RomInfo;

import java.nio.ByteBuffer;
import java.nio.file.Path;

/** PC 原生库加载入口；ROM 合成与音量逻辑由两端共用。 */
final class RomLoginMusic implements AutoCloseable {
    private final top.totodile.pokemmo.client.audio.RomLoginMusic player;

    RomLoginMusic(RomInfo rom, Path cache, ClientSettings settings) {
        player =
                new top.totodile.pokemmo.client.audio.RomLoginMusic(
                        rom,
                        settings,
                        () -> System.load(NativeTools.extract(cache, "sseqj64.dll").toString()),
                        182);
    }

    static void generateSamples(long player, ByteBuffer buffer) {
        top.totodile.pokemmo.client.audio.RomLoginMusic.generateSamples(player, buffer);
    }

    static float volume(ClientSettings settings, boolean focused) {
        return top.totodile.pokemmo.client.audio.RomLoginMusic.volume(settings, focused);
    }

    void setFocused(boolean focused) {
        player.setFocused(focused);
    }

    @Override
    public void close() {
        player.close();
    }
}
