package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;

/** 通知默认停留 5 秒，淡出 250 毫秒，300 毫秒后移除。 */
final class NotificationWindow extends Label {
    private static final long DURATION = 5_000_000_000L;
    private static final long FADE_DURATION = 250_000_000L;
    private static final long REMOVE_DELAY = 300_000_000L;

    private final GlfwTwlRenderer renderer;
    private long expiresAt;
    private long fadeStartedAt;
    private float opacity = 1;

    NotificationWindow(GlfwTwlRenderer renderer, String message, long now) {
        super(message.trim());
        this.renderer = renderer;
        expiresAt = now + DURATION;
        setTheme("notification");
        setCanAcceptKeyboardFocus(false);
    }

    boolean update(long now) {
        if (fadeStartedAt == 0) {
            if (now >= expiresAt) fadeStartedAt = now;
            // 本帧先启动淡出，下一帧才检查移除。
            return false;
        }
        opacity = Math.max(0, 1 - (float) (now - fadeStartedAt) / FADE_DURATION);
        return now >= expiresAt + REMOVE_DELAY;
    }

    @Override
    protected boolean handleEvent(Event event) {
        if (event.getType() == Event.Type.MOUSE_BTNUP
                && (event.getMouseButton() == Event.MOUSE_LBUTTON || event.getMouseButton() == Event.MOUSE_RBUTTON)) {
            expiresAt = Math.min(expiresAt, System.nanoTime());
            return true;
        }
        return event.isMouseEvent() || super.handleEvent(event);
    }

    @Override
    protected void paint(GUI gui) {
        renderer.pushGlobalTintColor(1, 1, 1, opacity);
        try {
            super.paint(gui);
        } finally {
            renderer.popGlobalTintColor();
        }
    }
}
