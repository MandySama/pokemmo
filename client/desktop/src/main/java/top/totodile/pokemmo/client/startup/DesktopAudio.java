package top.totodile.pokemmo.client.startup;

import top.totodile.pokemmo.client.audio.RomAudio;
import top.totodile.pokemmo.client.resources.ClientResources;

import java.nio.file.Path;

public final class DesktopAudio {
    private DesktopAudio() {}

    public static void install(ClientResources resources, Path cache) {
        resources.audio(
                new RomAudio(
                        resources,
                        () -> {
                            System.load(NativeTools.extract(cache, "sseqj64.dll").toString());
                            System.load(NativeTools.extract(cache, "agbplayj64.dll").toString());
                        },
                        182));
    }
}
