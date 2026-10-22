package top.totodile.pokemmo.android;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import java.io.File;
import top.totodile.pokemmo.client.ClientApplication;
import top.totodile.pokemmo.client.ResourceProgressScreen;
import top.totodile.pokemmo.client.StartupBackground;

/** 同一个 GL 窗口先绘制解压弹窗，安装完成后再进入正常客户端。 */
final class ResourceLoadingApplication extends ApplicationAdapter {
    private final ClientApplication client;
    private final BundledResourceInstaller.Source source;
    private final File root;
    private final String message;
    private volatile boolean checked;
    private volatile int percent;
    private volatile boolean ready;
    private ResourceProgressScreen screen;
    private StartupBackground background;
    private boolean installing, started, backgroundPresented;

    ResourceLoadingApplication(ClientApplication client, BundledResourceInstaller.Source source, File root, String message) {
        this.client = client; this.source = source; this.root = root; this.message = message;
    }

    @Override public void create() {
        background = new StartupBackground(path -> Gdx.files.internal("startup/" + path),
                android.os.SystemClock::elapsedRealtimeNanos);
        client.withStartupBackground(background);
        new Thread(() -> {
            try {
                ready = BundledResourceInstaller.isInstalled(source, root);
                checked = true;
            } catch (Exception error) { Gdx.app.error("resources", "Cannot prepare resource progress screen", error); }
        }, "PokeMMO-resource-bootstrap").start();
    }

    @Override public void render() {
        if (started) { client.render(); return; }
        ScreenUtils.clear(0, 0, 0, 1);
        if (!background.update()) return;
        background.draw();
        // 至少提交一帧背景画面，再绘制弹窗并开始解压。
        if (!backgroundPresented) { backgroundPresented = true; return; }
        if (!checked) return;
        if (ready && screen == null) { startClient(); client.render(); return; }
        if (screen == null) screen = new ResourceProgressScreen(
                path -> Gdx.files.internal("startup/" + path.substring("data/".length())), message);
        screen.draw(percent);
        if (ready) {
            startClient();
        } else if (!installing) {
            installing = true;
            new Thread(() -> {
                try {
                    BundledResourceInstaller.install(source, root, value -> percent = value);
                    ready = true;
                } catch (Exception error) { Gdx.app.error("resources", "Resource installation failed", error); }
            }, "PokeMMO-resource-install").start();
        }
    }

    private void startClient() {
        if (screen != null) { screen.close(); screen = null; }
        client.create(); client.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        started = true;
    }

    @Override public void resize(int width, int height) { if (started) client.resize(width, height); }
    @Override public void pause() { if (started) client.pause(); }
    @Override public void resume() { if (started) client.resume(); }
    @Override public void dispose() {
        if (screen != null) screen.close();
        client.dispose();
    }
}
