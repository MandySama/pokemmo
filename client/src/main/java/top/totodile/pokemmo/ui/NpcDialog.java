package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;

import java.util.*;

/** NPC 文本按段分页，交互期间由上层锁定世界输入。 */
final class NpcDialog extends Widget {
    private final DialogLayout panel = new DialogLayout();
    private final Label message = new Label();
    private List<String> paragraphs = List.of();
    private int page;
    private Runnable onClose = () -> {};

    NpcDialog() {
        setTheme("/npc-dialog");
        setCanAcceptKeyboardFocus(false);
        panel.setTheme("panel");
        message.setTheme("message");
        panel.setHorizontalGroup(panel.createParallelGroup().addWidget(message));
        panel.setVerticalGroup(panel.createSequentialGroup().addWidget(message));
        add(panel);
        setVisible(false);
    }

    void setOnClose(Runnable callback) {
        onClose = callback;
    }

    void open(List<String> texts) {
        List<String> pages = new ArrayList<>();
        for (String text : texts)
            for (String paragraph : text.replace("\\n", "\n").split("\n\n")) {
                if (paragraph.isBlank()) continue;
                int width = Math.min(680, Math.max(1, getInnerWidth() - 24)) - 48;
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
                        if (lines++ >= 5) {
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
        setVisible(true);
        invalidateLayout();
    }

    void advance() {
        if (++page >= paragraphs.size()) close();
        else invalidateLayout();
    }

    void close() {
        if (!isVisible()) return;
        setVisible(false);
        paragraphs = List.of();
        requestKeyboardFocus(null);
        onClose.run();
    }

    @Override
    protected void layout() {
        int width = Math.min(680, Math.max(1, getInnerWidth() - 24));
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
                    && message.getFont().computeTextWidth(line) > width - 40
                    && line.length() > 1) {
                line.setLength(line.length() - 1);
                wrapped.append(line).append('\n');
                line.setLength(0);
                line.append(ch);
            }
        }
        message.setText(wrapped.append(line).toString());
        int height = Math.min(getInnerHeight() - 24, panel.getPreferredHeight());
        panel.setSize(width, Math.max(1, height));
        panel.setPosition(
                getInnerX() + (getInnerWidth() - width) / 2,
                getInnerBottom() - Math.max(1, height) - 12);
    }
}
