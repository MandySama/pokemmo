package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.world.WorldCamera;

/**
 * 世界内的电脑交互对话：白色气泡显示提示文字，尾部指向人物，下方纵向列出功能选项。
 * 控件只维护选中项、相对人物的位置和选择回调，是否打开随身电脑等行为由上层决定。
 * 控件覆盖整个客户区，因此始终由上层在合适的时机显示、收起或布置。
 */
final class PcDialog extends Widget implements AutoCloseable {
    /** 固定选项顺序，与主题中的选项按钮一一对应。 */
    static final int COMPUTER = 0;
    static final int MARKET = 1;
    static final int MAIL = 2;
    static final int CANCEL = 3;
    /** 首项由上层填入角色名，其余使用固定语言 ID。 */
    private static final int[] OPTION_STRINGS = {0, 2352, 2353, 53};

    private final DialogLayout bubble = new DialogLayout();
    private final Label message = new Label();
    private final Widget tail = new Widget();
    private final Widget panel = new Widget();
    private final Button[] options = new Button[OPTION_STRINGS.length];
    private int selected = COMPUTER;
    private int minBubbleWidth = 600, maxBubbleWidth = 900, bubbleHeadGap = 160, tailWidthPercent = 15;
    private int tailInset = 4, tailHeadGap = 6, panelWidth = 196, panelOffsetY = -40, rowHeight = 49;
    private java.util.function.IntConsumer onChoose = option -> { };

    PcDialog() {
        setTheme("/pc-dialog");
        setCanAcceptKeyboardFocus(false);
        bubble.setTheme("bubble");
        message.setTheme("message");
        message.setCanAcceptKeyboardFocus(false);
        message.setText(ClientStrings.get(2350));
        bubble.add(message);
        bubble.setHorizontalGroup(bubble.createParallelGroup(message));
        bubble.setVerticalGroup(bubble.createSequentialGroup(message));
        tail.setTheme("tail");
        tail.setCanAcceptKeyboardFocus(false);
        panel.setTheme("choices");
        panel.setCanAcceptKeyboardFocus(false);
        // 尾部先绘制，气泡底边压住接缝，避免缩放后出现空隙。
        add(tail);
        add(bubble);
        add(panel);
        for (int i = 0; i < options.length; i++) {
            options[i] = new Button(OPTION_STRINGS[i] == 0 ? "" : ClientStrings.get(OPTION_STRINGS[i]));
            options[i].setTheme("option");
            options[i].setCanAcceptKeyboardFocus(true);
            int index = i;
            options[i].addCallback(() -> onChoose.accept(index));
            panel.add(options[i]);
        }
        setVisible(false);
    }

    void setOnChoose(java.util.function.IntConsumer callback) { onChoose = callback; }

    int selected() { return selected; }

    /** 打开时把首项写为角色名并选中，键盘焦点决定高亮项。 */
    void open(String characterName) {
        options[COMPUTER].setText(ClientStrings.get(2351).replace("{01}", characterName));
        selected = COMPUTER;
        setVisible(true);
        invalidateLayout();
        options[COMPUTER].requestKeyboardFocus();
    }

    void move(int delta) {
        int index = Math.floorMod(selected + delta, options.length);
        selected = index;
        options[index].requestKeyboardFocus();
    }

    @Override
    public void close() {
        if (!isVisible()) return;
        setVisible(false);
        // 交出键盘焦点，避免隐藏后仍由选项按钮持有高亮状态。
        requestKeyboardFocus(null);
    }

    /** 点击气泡或选项面板之外的位置视为取消本次对话。 */
    boolean hits(int x, int y) {
        return bubble.isInside(x, y) || panel.isInside(x, y);
    }

    @Override
    protected void applyTheme(ThemeInfo info) {
        super.applyTheme(info);
        minBubbleWidth = info.getParameter("minBubbleWidth", minBubbleWidth);
        maxBubbleWidth = info.getParameter("maxBubbleWidth", maxBubbleWidth);
        bubbleHeadGap = info.getParameter("bubbleHeadGap", bubbleHeadGap);
        tailWidthPercent = info.getParameter("tailWidthPercent", tailWidthPercent);
        tailInset = info.getParameter("tailInset", tailInset);
        tailHeadGap = info.getParameter("tailHeadGap", tailHeadGap);
        panelWidth = info.getParameter("panelWidth", panelWidth);
        panelOffsetY = info.getParameter("panelOffsetY", panelOffsetY);
        rowHeight = info.getParameter("rowHeight", rowHeight);
    }

    /** 镜头以人物为中心，因此相对客户区中心的偏移可以跨分辨率沿用。 */
    @Override
    protected void layout() {
        int margin = 6;
        int centerX = getWidth() / 2;
        int scale = WorldCamera.scale(Math.max(1, getWidth()));
        int feetY = getHeight() / 2 + 4 * scale;
        int headY = feetY - 24 * scale;
        // 气泡宽度跟随客户区：取一半宽并夹在上下限之间，再收进客户区内。
        int bubbleWidth = Math.max(minBubbleWidth, Math.min(centerX, maxBubbleWidth));
        bubbleWidth = Math.min(bubbleWidth, Math.max(1, getWidth() - 2 * margin));
        int bubbleHeight = bubble.getPreferredHeight();
        int bubbleLeft = Math.max(margin, centerX - bubbleWidth / 2);
        int bubbleBottom = Math.max(bubbleHeight + margin, headY - bubbleHeadGap);
        bubble.setPosition(bubbleLeft, bubbleBottom - bubbleHeight);
        bubble.setSize(bubbleWidth, bubbleHeight);
        // 尾巴按贴图比例由高度反推宽度，上端压进气泡里，尖端停在头顶上方。
        int tailTop = bubbleBottom - tailInset;
        int tailHeight = Math.max(1, headY - tailHeadGap - tailTop);
        int tailWidth = Math.max(4, Math.round(tailHeight * tailWidthPercent / 100f));
        tail.setPosition(centerX - tailWidth / 2, tailTop);
        tail.setSize(tailWidth, tailHeight);
        int panelHeight = rowHeight * options.length + panel.getBorderVertical();
        panel.setPosition(Math.max(margin, centerX - panelWidth / 2),
                Math.max(margin, Math.min(feetY + panelOffsetY, getHeight() - panelHeight - margin)));
        panel.setSize(panelWidth, panelHeight);
        for (int i = 0; i < options.length; i++) {
            options[i].setPosition(panel.getInnerX(), panel.getInnerY() + i * rowHeight);
            options[i].setSize(panel.getInnerWidth(), rowHeight);
        }
    }
}
