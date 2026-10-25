package top.totodile.pokemmo.client.ui.common;

import com.badlogic.gdx.input.GestureDetector;

import de.matthiasmann.twl.*;

/** 共享滚动：PC 沿用 MOD 窗口鼠标拖动，Android 沿用参考 pan/fling。 */
public final class TouchScrollPane extends ScrollPane {
    private boolean touching, panning, mouseDrag;
    private int pointerX, pointerY, startScrollX, startScrollY;
    private double positionX, velocityX;
    private double position, velocity, remaining;
    private long flingTime;
    private final GestureDetector gesture =
            new GestureDetector(
                    20,
                    .4f,
                    1.1f,
                    .15f,
                    new GestureDetector.GestureAdapter() {
                        @Override
                        public boolean pan(float x, float y, float dx, float dy) {
                            beginPan();
                            positionX = Math.max(0, Math.min(getMaxScrollPosX(), positionX - dx));
                            setScrollPositionX((int) Math.round(positionX));
                            position =
                                    Math.max(
                                            0,
                                            Math.min(
                                                    getVerticalScrollbar().getMaxValue(),
                                                    position - dy));
                            setScrollPositionY((int) Math.round(position));
                            return true;
                        }

                        @Override
                        public boolean fling(float vx, float vy, int button) {
                            if (Math.abs(vx) > 150 || Math.abs(vy) > 150) {
                                velocityX = -vx;
                                velocity = -vy;
                                remaining = .5;
                                positionX = getScrollPositionX();
                                position = getScrollPositionY();
                                flingTime = System.nanoTime();
                            }
                            return true;
                        }
                    });

    public TouchScrollPane() {
        setTheme("scrollpane");
    }

    public TouchScrollPane(Widget content) {
        super(content);
        setTheme("scrollpane");
    }

    public void beginTouch(int x, int y) {
        beginTouch(x, y, true);
    }

    public void beginTouch(int x, int y, boolean mobile) {
        touching = true;
        panning = false;
        mouseDrag = !mobile;
        remaining = 0;
        positionX = getScrollPositionX();
        position = getScrollPositionY();
        pointerX = x;
        pointerY = y;
        startScrollX = getScrollPositionX();
        startScrollY = getScrollPositionY();
        gesture.reset();
        if (mobile) gesture.touchDown(x, y, 0, 0);
    }

    public boolean drag(int x, int y) {
        if (!touching) return false;
        if (mouseDrag) {
            int dx = x - pointerX, dy = y - pointerY;
            if (!panning) {
                if ((getMaxScrollPosX() == 0 || Math.abs(dx) < 6)
                        && (getMaxScrollPosY() == 0 || Math.abs(dy) < 6)) return false;
                beginPan();
            }
            setScrollPositionX(Math.max(0, Math.min(getMaxScrollPosX(), startScrollX - dx)));
            setScrollPositionY(Math.max(0, Math.min(getMaxScrollPosY(), startScrollY - dy)));
        } else gesture.touchDragged(x, y, 0);
        return panning;
    }

    private void beginPan() {
        if (!panning) {
            panning = true;
            cancelPress(getContent());
            getGUI().handleMouse(-1, -1, Event.MOUSE_LBUTTON, false);
            cancelPress(getContent());
        }
    }

    public boolean endTouch(int x, int y) {
        if (!touching) return false;
        touching = false;
        if (!mouseDrag) gesture.touchUp(x, y, 0, 0);
        return panning;
    }

    public void cancelTouch() {
        touching = false;
        panning = false;
        remaining = 0;
        gesture.reset();
    }

    private static void cancelPress(Widget widget) {
        if (widget == null) return;
        if (widget instanceof Button button) {
            button.getModel().setArmed(false);
            button.getModel().setPressed(false);
            button.getModel().setHover(false);
        }
        for (int i = 0; i < widget.getNumChildren(); i++) cancelPress(widget.getChild(i));
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {
        cancelTouch();
        super.beforeRemoveFromGUI(gui);
    }

    @Override
    protected void paint(GUI gui) {
        if (remaining > 0) {
            long now = System.nanoTime();
            double elapsed = Math.min(remaining, (now - flingTime) / 1_000_000_000.0);
            positionX += velocityX * (remaining * elapsed - elapsed * elapsed / 2) / .5;
            position += velocity * (remaining * elapsed - elapsed * elapsed / 2) / .5;
            remaining -= elapsed;
            flingTime = now;
            positionX = Math.max(0, Math.min(getMaxScrollPosX(), positionX));
            setScrollPositionX((int) Math.round(positionX));
            position = Math.max(0, Math.min(getVerticalScrollbar().getMaxValue(), position));
            setScrollPositionY((int) Math.round(position));
        }
        super.paint(gui);
    }
}
