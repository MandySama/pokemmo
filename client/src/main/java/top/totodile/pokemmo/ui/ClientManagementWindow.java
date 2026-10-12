package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleChangableListModel;

import java.util.ArrayList;
import java.util.List;

/** 客户端语言与 ROM 管理界面，文件操作尚未接入。 */
final class ClientManagementWindow extends ResizableFrame {
    private final Widget content = new Widget() {
        @Override
        protected void layout() {
            layoutContent();
        }
    };
    private final List<Label> labels = new ArrayList<>();
    private final List<Button> selectButtons = new ArrayList<>();
    private final List<Button> infoButtons = new ArrayList<>();
    private final ComboBox<String> language = new ComboBox<>(
            new SimpleChangableListModel<>("English", "Français (French)", "Deutsch (German)",
                    "Español (Spanish)", "Português Brasileiro (Brazilian Portuguese)", "Italiano (Italian)",
                    "Polski (Polish)", "한국어 (Korean)", "日本語 (Japanese)",
                    "简体中文 (Simplified Chinese)", "繁體中文 (Traditional Chinese)")) {
        @Override
        protected boolean openPopup() {
            // 从列表顶部计算可见范围，使当前选项滚动到最后一个可见行。
            ((ListBox<?>) popup.getChild(0)).setFirstVisible(0);
            return super.openPopup();
        }
    };
    private final Button openFolder = new Button("打开ROM文件夹");
    private final Button close = new Button("关闭");

    ClientManagementWindow(Runnable onClose) {
        setTheme("rom-panel");
        setTitle("客户端管理");
        setResizableAxis(ResizableAxis.NONE);
        setDraggable(false);
        addCloseCallback(onClose);
        content.setTheme("");
        add(content);
        for (String text : new String[]{"客户端语言:", "黑/白ROM:", "火红ROM:", "绿宝石ROM:",
                "白金ROM:", "心金/魂银ROM:"}) {
            Label label = new Label(text);
            label.setTheme("label-settings-title");
            labels.add(label);
            content.add(label);
        }
        language.setSelected(9);
        content.add(language);
        for (int i = 0; i < 5; i++) {
            Button select = new Button("选择文件");
            Button info = new Button("信息");
            selectButtons.add(select);
            infoButtons.add(info);
            content.add(select);
            content.add(info);
        }
        content.add(openFolder);
        close.addCallback(onClose);
        content.add(close);
    }

    @Override
    protected void layout() {
        super.layout();
        content.setPosition(getX(), getY() + 40);
        content.setSize(getWidth(), getHeight() - 40);
    }

    private void layoutContent() {
        int x = content.getX();
        int y = content.getY();
        for (int i = 0; i < labels.size(); i++) {
            place(labels.get(i), x + 10, y + i * 37, 220, 31);
        }
        place(language, x + 239, y, 275, 31);
        for (int i = 0; i < selectButtons.size(); i++) {
            place(selectButtons.get(i), x + 239, y + 42 + i * 37, 133, 26);
            place(infoButtons.get(i), x + 382, y + 42 + i * 37, 132, 26);
        }
        place(openFolder, x + 11, y + 224, 133, 26);
        place(close, x + 382, y + 224, 132, 26);
    }

    private static void place(Widget widget, int x, int y, int width, int height) {
        widget.setPosition(x, y);
        widget.setSize(width, height);
    }
}
