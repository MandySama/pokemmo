package top.totodile.pokemmo.android.audio;

import top.totodile.pokemmo.client.audio.RomLoginMusic;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.RomInfo;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.ui.startup.LoginScene;

/** 移动端仅接入参考登录 BGM，画面仍由现有滚动背景绘制。 */
public final class AndroidLoginAudio implements LoginScene {
    private final ClientResources resources;
    private RomInfo rom;
    private RomLoginMusic music;
    private boolean focused = true, closed;

    public AndroidLoginAudio(ClientResources resources) {
        this.resources = resources;
    }

    @Override
    public boolean draw(float delta, int width, int height) {
        if (closed) return false;
        RomInfo selected = resources.roms().get(RomType.BLACK_WHITE);
        if (selected != rom) {
            if (music != null) {
                music.close();
                music = null;
            }
            rom = selected;
            if (rom != null) {
                // 参考 Android APK 的随附库为 API 183；PC 随附库仍严格校验 182。
                music =
                        new RomLoginMusic(
                                rom,
                                resources.preferences(),
                                () -> System.loadLibrary("sseqj"),
                                183);
                music.setFocused(focused);
            }
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
        if (music != null) music.setFocused(focused);
    }

    @Override
    public void close() {
        closed = true;
        if (music != null) {
            music.close();
            music = null;
        }
        rom = null;
    }
}
