package top.totodile.pokemmo.client.config;

import com.badlogic.gdx.Gdx;

/** 对齐参考 Client.main / GameSettingsWindow.hD0 的即时限帧配置。 */
public final class FrameRateLimit {
    private int applied = -1;
    private long nextFrame;

    public void update(ClientSettings settings, boolean focused) {
        int foreground = value(settings.get(ClientSettings.key(1254),
                settings.get("client.graphics.max_fpx", "60")), 60);
        int background = value(settings.get("client.graphics.max_fpx_unfocused", "20"), 20);
        int target = focused ? foreground : background;
        if (target != applied) {
            Gdx.graphics.setForegroundFPS(target);
            applied = target;
            nextFrame = 0;
        }
    }

    /** Android 后端的 setForegroundFPS 是空实现；沿用参考主循环的纳秒帧间隔。 */
    public void waitForFrame() {
        if (Gdx.app.getType() != com.badlogic.gdx.Application.ApplicationType.Android || applied <= 0) return;
        long now = System.nanoTime();
        if (nextFrame == 0) nextFrame = now;
        while (now < nextFrame) {
            java.util.concurrent.locks.LockSupport.parkNanos(nextFrame - now);
            if (Thread.currentThread().isInterrupted()) return;
            now = System.nanoTime();
        }
        nextFrame = Math.max(nextFrame + 1_000_000_000L / applied, now);
    }

    private static int value(String text, int fallback) {
        try {
            int value = Integer.parseInt(text);
            return value == 0 || value >= 10 && value <= 1000 ? value : fallback;
        } catch (NumberFormatException error) { return fallback; }
    }
}
