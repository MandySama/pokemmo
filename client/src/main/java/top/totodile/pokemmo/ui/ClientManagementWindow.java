package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import top.totodile.pokemmo.ClientStrings;

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
            new SimpleChangableListModel<>(ClientStrings.languageNames())) {
        @Override
        protected boolean openPopup() {
            // 从列表顶部计算可见范围，使当前选项滚动到最后一个可见行。
            ((ListBox<?>) popup.getChild(0)).setFirstVisible(0);
            return super.openPopup();
        }

        @Override
        protected void setPopupSize() {
            // 按已安装语言数量设置高度，最多显示八行。
            ListBox<?> list = (ListBox<?>) popup.getChild(0);
            int height = Math.min(8, list.getNumEntries()) * list.getCellHeight() + list.getBorderVertical();
            popup.setMinSize(popup.getMinWidth(), height);
            popup.setMaxSize(popup.getMaxWidth(), height);
            super.setPopupSize();
        }
    };
    private final Button openFolder = new Button(ClientStrings.get(1174));
    private final Button close = new Button(ClientStrings.get(65));

    ClientManagementWindow(Runnable onClose, Runnable onLanguageChanged) {
        setTheme("rom-panel");
        setTitle(ClientStrings.get(1175));
        setResizableAxis(ResizableAxis.NONE);
        setDraggable(false);
        addCloseCallback(onClose);
        content.setTheme("");
        add(content);
        for (int stringId : new int[]{1332, 92, 90, 91, 93, 94}) {
            Label label = new Label(ClientStrings.get(stringId) + (stringId == 1332 ? "" : ":"));
            label.setTheme("label-settings-title");
            labels.add(label);
            content.add(label);
        }
        language.setSelected(ClientStrings.selectedIndex());
        language.addCallback(() -> {
            ClientStrings.select(language.getSelected());
            refreshTexts();
            onLanguageChanged.run();
        });
        content.add(language);
        for (int i = 0; i < 5; i++) {
            Button select = new Button(ClientStrings.get(1176));
            Button info = new Button(ClientStrings.get(1177));
            selectButtons.add(select);
            infoButtons.add(info);
            content.add(select);
            content.add(info);
        }
        content.add(openFolder);
        close.addCallback(onClose);
        content.add(close);
    }

    private void refreshTexts() {
        setTitle(ClientStrings.get(1175));
        int[] ids = {1332, 92, 90, 91, 93, 94};
        for (int i = 0; i < ids.length; i++) {
            labels.get(i).setText(ClientStrings.get(ids[i]) + (i == 0 ? "" : ":"));
        }
        for (Button button : selectButtons) button.setText(ClientStrings.get(1176));
        for (Button button : infoButtons) button.setText(ClientStrings.get(1177));
        openFolder.setText(ClientStrings.get(1174));
        close.setText(ClientStrings.get(65));
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
