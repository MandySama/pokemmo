package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.world.WorldCamera;

import java.util.*;

/** NPC 文本按段分页，交互期间由上层锁定世界输入。 */
final class NpcDialog extends Widget {
    private final Widget tail = new Widget() {
        private de.matthiasmann.twl.renderer.Image image;
        @Override protected void applyTheme(ThemeInfo info) {
            super.applyTheme(info);
            image = info.getImage("tailImage");
        }
        @Override protected void paintWidget(GUI gui) {
            if (image == null) return;
            float slant = Math.min(tailSlant, getHeight() / 2f);
            org.lwjgl.opengl.GL11.glPushMatrix();
            try {
                org.lwjgl.opengl.GL11.glTranslatef(getX() + slant, getY(), 0);
                org.lwjgl.opengl.GL11.glRotatef((float)Math.toDegrees(Math.atan2(slant, getHeight())), 0, 0, 1);
                image.draw(getAnimationState(), 0, 0, getWidth(), (int)Math.hypot(slant,getHeight()));
            } finally { org.lwjgl.opengl.GL11.glPopMatrix(); }
        }
    };
    private final Widget arrow = new Widget() {
        @Override protected boolean handleEvent(Event event) { return NpcDialog.this.handleEvent(event); }
    };
    private int minBubbleWidth = 600, maxBubbleWidth = 900, headGap = 140, margin = 24;
    private int minBubbleHeight = 150, tailSlant = 70, arrowSize = 32, arrowInset = 20, tailInset = 28, textRightReserve = 56;
    private final DialogLayout panel = new DialogLayout();
    private final Label message = new Label() {
        @Override
        protected boolean handleEvent(Event event) {
            return NpcDialog.this.handleEvent(event);
        }
    };
    private List<String> paragraphs = List.of();
    private int page;
    private boolean completed;
    boolean completed() { return completed; }
    private long pageRevision;
    private long renderedRevision = -1;
    private int autoAdvanceBaseMillis = 800;
    private int autoAdvancePerCharacterMillis = 30;
    private int autoAdvanceMaxMillis = 6000;
    private Runnable onClose = () -> {};
    private Runnable onAdvance = this::advance;

    NpcDialog() {
        setTheme("/npc-dialog");
        setCanAcceptKeyboardFocus(false);
        panel.setTheme("panel");
        message.setTheme("message");
        panel.setHorizontalGroup(panel.createParallelGroup().addWidget(message));
        panel.setVerticalGroup(panel.createSequentialGroup().addWidget(message));
        tail.setTheme("tail");
        arrow.setTheme("arrow");
        add(tail);
        add(panel);
        add(arrow);
        setVisible(false);
    }

    void setOnClose(Runnable callback) {
        onClose = callback;
    }

    void setOnAdvance(Runnable callback) {
        onAdvance = callback;
    }

    @Override
    protected boolean handleEvent(Event event) {
        if (event.isMouseEvent() && panel.isInside(event.getMouseX(), event.getMouseY())) {
            if (event.getType() == Event.Type.MOUSE_BTNDOWN
                    && event.getMouseButton() == Event.MOUSE_LBUTTON) {
                onAdvance.run();
            }
            return true;
        }
        return super.handleEvent(event);
    }

    void open(List<String> texts) {
        List<String> pages = new ArrayList<>();
        for (String text : texts)
            for (String paragraph : text.replace("\\n", "\n").split("\n\n")) {
                if (paragraph.isBlank()) continue;
                int width = textWidth();
                StringBuilder buffer = new StringBuilder(), line = new StringBuilder();
                int lines = 1;
                for (char ch : paragraph.toCharArray()) {
                    boolean wrap =
                            ch == '\n'
                                    || message.getFont() != null
                                            && line.length() > 0
                                            && message.getFont()
                                                            .computeTextWidth(line.toString() + ch)
                                                    > width;
                    if (wrap) {
                        buffer.append(line);
                        line.setLength(0);
                        if (lines++ >= 3) {
                            pages.add(buffer.toString());
                            buffer.setLength(0);
                            lines = 1;
                        } else buffer.append('\n');
                    }
                    if (ch != '\n') line.append(ch);
                }
                buffer.append(line);
                if (!buffer.isEmpty()) pages.add(buffer.toString());
            }
        if (pages.isEmpty()) return;
        paragraphs = List.copyOf(pages);
        page = 0;
        completed = false;
        pageRevision++;
        setVisible(true);
        invalidateLayout();
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        minBubbleWidth = themeInfo.getParameter("minBubbleWidth", 600);
        maxBubbleWidth = themeInfo.getParameter("maxBubbleWidth", 900);
        minBubbleHeight = themeInfo.getParameter("minBubbleHeight", 150);
        headGap = themeInfo.getParameter("headGap", 140);
        margin = themeInfo.getParameter("margin", 24);
        tailSlant = themeInfo.getParameter("tailSlant", 70);
        tailInset = themeInfo.getParameter("tailInset", 28);
        arrowSize = themeInfo.getParameter("arrowSize", 32);
        textRightReserve = themeInfo.getParameter("textRightReserve", 56);
        arrowInset = themeInfo.getParameter("arrowInset", 20);
        autoAdvanceBaseMillis = Math.max(1, themeInfo.getParameter("autoAdvanceBaseMillis", 800));
        autoAdvancePerCharacterMillis = Math.max(0, themeInfo.getParameter("autoAdvancePerCharacterMillis", 30));
        autoAdvanceMaxMillis = Math.max(autoAdvanceBaseMillis, themeInfo.getParameter("autoAdvanceMaxMillis", 6000));
    }

    @Override
    protected void paintWidget(GUI gui) {
        super.paintWidget(gui);
        renderedRevision = pageRevision;
    }

    long pageRevision() { return pageRevision; }
    boolean pageDisplayed() { return isVisible() && renderedRevision == pageRevision; }
    long autoAdvanceDelayNanos() {
        String text = paragraphs.isEmpty() ? "" : paragraphs.get(page);
        long millis = Math.min(autoAdvanceMaxMillis,
                autoAdvanceBaseMillis + (long) text.codePointCount(0, text.length()) * autoAdvancePerCharacterMillis);
        return millis * 1_000_000L;
    }

    boolean lastPage() { return page + 1 >= paragraphs.size(); }

    void advance() {
        pageRevision++;
        if (++page >= paragraphs.size()) finish(true);
        else invalidateLayout();
    }

    void close() { finish(false); }

    private void finish(boolean completed) {
        if (!isVisible()) return;
        this.completed = completed;
        setVisible(false);
        paragraphs = List.of();
        requestKeyboardFocus(null);
        onClose.run();
    }

    private int bubbleWidth() {
        return Math.max(1, Math.min(Math.max(1, getInnerWidth() - margin * 2),
                Math.max(minBubbleWidth, Math.min(maxBubbleWidth, getInnerWidth() / 2))));
    }

    private int textWidth() { return Math.max(1, bubbleWidth() - panel.getBorderHorizontal() - textRightReserve); }

    @Override
    protected void layout() {
        int width = bubbleWidth();
        String text =
                paragraphs.isEmpty() ? "" : paragraphs.get(Math.min(page, paragraphs.size() - 1));
        StringBuilder wrapped = new StringBuilder(), line = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (ch == '\n') {
                wrapped.append(line).append('\n');
                line.setLength(0);
                continue;
            }
            line.append(ch);
            if (message.getFont() != null
                    && message.getFont().computeTextWidth(line) > textWidth()
                    && line.length() > 1) {
                line.setLength(line.length() - 1);
                wrapped.append(line).append('\n');
                line.setLength(0);
                line.append(ch);
            }
        }
        message.setText(wrapped.append(line).toString());
        int height = Math.min(Math.max(1, getInnerHeight() - margin * 2),
                Math.max(minBubbleHeight, panel.getPreferredHeight()));
        int scale = WorldCamera.scale(Math.max(1, getInnerWidth()));
        int headY = getInnerY() + getInnerHeight() / 2 - 20 * scale;
        int bottom = Math.max(getInnerY() + margin + height, headY - headGap);
        bottom = Math.min(getInnerBottom() - margin, bottom);
        panel.setSize(width, height);
        panel.setPosition(getInnerX() + (getInnerWidth() - width) / 2, bottom - height);
        int tailHeight = Math.max(0, headY - 6 - (bottom - tailInset));
        tail.setVisible(tailHeight > 0);
        tail.setPosition(getInnerX() + getInnerWidth() / 2 - 8, bottom - tailInset);
        tail.setSize(Math.max(8, tailHeight * 15 / 100), tailHeight);
        arrow.setPosition(panel.getRight() - arrowInset - arrowSize, panel.getBottom() - arrowInset - arrowSize);
        arrow.setSize(arrowSize, arrowSize);
    }
}
