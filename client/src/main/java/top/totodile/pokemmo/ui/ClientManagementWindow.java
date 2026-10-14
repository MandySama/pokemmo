package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.RomFiles;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** 客户端语言与 ROM 文件选择界面。 */
final class ClientManagementWindow extends ResizableFrame {
    private final Path[] selectedFiles = new Path[6];
    private final RomFiles romFiles;
    private final Consumer<String> notify;
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
    private final Button openFolder = new FolderButton(ClientStrings.get(1174));
    private final Button close = new Button(ClientStrings.get(65));

    private boolean restartRequired;
    private boolean importing;

    ClientManagementWindow(Runnable onClose, Runnable onLanguageChanged, Consumer<String> notify) {
        this.notify = notify;
        try {
            romFiles = new RomFiles(ClientFiles.data("../roms"));
        } catch (IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
        setTheme("rom-panel");
        setTitle(ClientStrings.get(1175));
        setResizableAxis(ResizableAxis.NONE);
        setDraggable(false);
        Runnable requestClose = () -> {
            if (importing) return;
            if (restartRequired) showMessage(ClientStrings.get(1179), onClose);
            else onClose.run();
        };
        addCloseCallback(requestClose);
        content.setTheme("");
        add(content);
        for (int stringId : new int[]{1332, 92, 90, 91, 93, 94, 250004}) {
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
        for (int i = 0; i < 6; i++) {
            int index = i;
            Button select = new Button(ClientStrings.get(1176));
            select.addCallback(() -> selectFile(index));
            Button info = new Button(ClientStrings.get(1177));
            info.addCallback(() -> showInformation(index));
            selectedFiles[i] = romFiles.selected(i);
            selectButtons.add(select);
            infoButtons.add(info);
            content.add(select);
            content.add(info);
        }
        openFolder.setTooltipContent(ClientStrings.get(1173));
        openFolder.addCallback(this::openRomFolder);
        content.add(openFolder);
        close.addCallback(requestClose);
        content.add(close);
        refreshTexts();
    }

    private void selectFile(int index) {
        // 系统文件选择器采用单选且不限制扩展名；取消时保留已有选择。
        String file = TinyFileDialogs.tinyfd_openFileDialog("", "", (PointerBuffer) null, "", false);
        handleSelection(index, file);
    }

    private void handleSelection(int index, String file) {
        if (file == null) {
            notify.accept(ClientStrings.get(1085));
            return;
        }
        importFile(index, Path.of(file));
    }

    private void importFile(int index, Path source) {
        GUI gui = getGUI();
        importing = true;
        for (Button button : selectButtons) button.setEnabled(false);
        Thread.startVirtualThread(() -> {
            try {
                Path imported = romFiles.importFile(index, source);
                gui.invokeLater(() -> {
                    selectedFiles[index] = imported;
                    restartRequired = true;
                    importing = false;
                    refreshTexts();
                    notify.accept(ClientStrings.get(1184).replace("{00}", ClientStrings.get(new int[]{92, 90, 91, 93, 94, 250004}[index])));
                    for (Button button : selectButtons) button.setEnabled(true);
                });
            } catch (IOException exception) {
                gui.invokeLater(() -> {
                    importing = false;
                    showMessage(exception.getMessage());
                    for (Button button : selectButtons) button.setEnabled(true);
                });
            }
        });
    }

    Path selectedRom(int index) {
        return selectedFiles[index];
    }

    boolean restartRequired() {
        return restartRequired;
    }

    private void showInformation(int index) {
        String kind = ClientStrings.get(new int[]{92, 90, 91, 93, 94, 250004}[index]);
        Path file = selectedFiles[index];
        showMessage(file == null ? ClientStrings.get(1186).replace("{00}", kind)
                : ClientStrings.get(1185).replace("{00}", kind).replace("{01}", "roms/" + file.getFileName()));
    }

    private void showMessage(String text) {
        showMessage(text, () -> {});
    }

    private void showMessage(String text, Runnable onConfirmed) {
        DialogLayout dialog = new DialogLayout();
        dialog.setTheme("confirm-panel");
        Label message = new Label(text);
        message.setTheme("label-lalign");
        Button ok = new Button(ClientStrings.get(52));
        PopupWindow popup = new PopupWindow(this) {
            @Override
            protected void layout() {
                dialog.adjustSize();
                dialog.setPosition(getX() + (getWidth() - dialog.getWidth()) / 2,
                        getY() + (getHeight() - dialog.getHeight()) / 2);
            }
        };
        popup.setTheme("/rom-modal");
        popup.setCloseOnClickedOutside(false);
        ok.addCallback(() -> {
            popup.closePopup();
            onConfirmed.run();
        });
        dialog.setHorizontalGroup(dialog.createParallelGroup(message, ok));
        dialog.setVerticalGroup(dialog.createSequentialGroup().addWidget(message).addGap(5).addWidget(ok));
        popup.add(dialog);
        if (popup.openPopup()) {
            int width = Math.min(getGUI().getWidth() - 40, 640);
            StringBuilder wrapped = new StringBuilder();
            StringBuilder line = new StringBuilder();
            for (char character : text.toCharArray()) {
                if (character == '\n') {
                    wrapped.append(line).append('\n');
                    line.setLength(0);
                } else {
                    if (message.getFont().computeTextWidth(line.toString() + character) > width - 40) {
                        wrapped.append(line).append('\n');
                        line.setLength(0);
                    }
                    line.append(character);
                }
            }
            message.setText(wrapped.append(line).toString());
            popup.setPosition(0, 0);
            popup.setSize(getGUI().getWidth(), getGUI().getHeight());
            ok.requestKeyboardFocus();
        }
    }

    private void openRomFolder() {
        GUI gui = getGUI();
        Thread.startVirtualThread(() -> {
            try {
                java.nio.file.Files.createDirectories(romFiles.directory());
                java.awt.Desktop.getDesktop().open(romFiles.directory().toFile());
            } catch (IOException | UnsupportedOperationException | SecurityException exception) {
                gui.invokeLater(() -> notify.accept(ClientStrings.get(84)));
            }
        });
    }

    private static final class FolderButton extends Button implements GUI.TooltipPositionAdjuster {
        private FolderButton(String text) {
            super(text);
            setTheme("button");
            setTooltipDelay(150);
        }

        @Override
        public void adjustTooltipPosition(TooltipPositionData position) {
            position.x = getX() + getWidth() / 2;
            position.y = getY();
            position.alignment = Alignment.BOTTOM;
        }
    }
    private void refreshTexts() {
        setTitle(ClientStrings.get(1175));
        int[] ids = {1332, 92, 90, 91, 93, 94, 250004};
        for (int i = 0; i < ids.length; i++) {
            labels.get(i).setText(ClientStrings.get(ids[i]) + (i == 0 ? "" : ":"));
        }
        for (Button button : selectButtons) button.setText(ClientStrings.get(1176));
        for (int i = 0; i < infoButtons.size(); i++) {
            infoButtons.get(i).setText(ClientStrings.get(selectedFiles[i] == null ? i == 0 ? 1168 : 1169 : 1177));
        }
        openFolder.setText(ClientStrings.get(1174));
        openFolder.setTooltipContent(ClientStrings.get(1173));
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
        place(openFolder, x + 11, y + 261, 133, 26);
        place(close, x + 382, y + 261, 132, 26);
    }

    private static void place(Widget widget, int x, int y, int width, int height) {
        widget.setPosition(x, y);
        widget.setSize(width, height);
    }
}
