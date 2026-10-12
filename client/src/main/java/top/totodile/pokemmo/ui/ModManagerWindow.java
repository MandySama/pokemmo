package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ModFiles;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Path;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** 管理 MOD 包、元信息、启用顺序及配置，不加载游戏资源。 */
final class ModManagerWindow extends ResizableFrame implements AutoCloseable {
    private final Label empty = new Label();
    private final Button browse = new Button();
    private final Button importMod = new Button();
    private final Button openFolder = new Button();
    private final ToggleButton verbose = new TooltipToggleButton();
    private final Label verboseLabel = new TooltipLabel();
    private final Button save = new Button();
    private final ScrollPane scroll = new ScrollPane();
    private final List<Texture> iconTextures = new ArrayList<>();
    private final Runnable onClose;
    private final Runnable onRestart;
    private final Consumer<String> notify;
    private ModFiles files;
    private boolean busy;
    private boolean restartRequired;

    ModManagerWindow(Runnable onClose, Runnable onRestart, Consumer<String> notify) {
        this.onClose = onClose;
        this.onRestart = onRestart;
        this.notify = notify;
        setTheme("mods-panel");
        setResizableAxis(ResizableAxis.NONE);
        setDraggable(false);
        addCloseCallback(this::requestClose);

        DialogLayout list = new DialogLayout();
        list.setTheme("settings-label-area");
        list.setHorizontalGroup(list.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE).addWidget(empty).addGap(0, 0, Short.MAX_VALUE));
        list.setVerticalGroup(list.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE).addWidget(empty).addGap(0, 0, Short.MAX_VALUE));
        scroll.setContent(list);
        scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        scroll.setExpandContentSize(true);
        verboseLabel.setLabelFor(verbose);
        verbose.setTheme("togglebutton");
        verboseLabel.setTheme("label");
        verbose.setTooltipDelay(150);
        verboseLabel.setTooltipDelay(150);

        DialogLayout content = new DialogLayout();
        content.setHorizontalGroup(content.createParallelGroup()
                .addWidget(scroll)
                .addGroup(content.createSequentialGroup(browse, importMod, openFolder)
                        .addGap(0, 0, Short.MAX_VALUE).addWidget(verbose).addWidget(verboseLabel)
                        .addGap(0, 0, Short.MAX_VALUE).addWidget(save)));
        content.setVerticalGroup(content.createSequentialGroup()
                .addWidget(scroll)
                .addGroup(content.createParallelGroup(browse, importMod, openFolder,
                        verbose, verboseLabel, save))
                // 按钮行后另留 5 像素，叠加主题底边距后共 10 像素。
                .addGap(5));
        add(content);
        browse.addCallback(() -> openWebsite("https://forums.pokemmo.com/index.php?/forum/33-client-customization/"));
        importMod.addCallback(this::selectFile);
        openFolder.addCallback(() -> runTask(() -> {
            java.nio.file.Files.createDirectories(files.directory());
            java.awt.Desktop.getDesktop().open(files.directory().toFile());
        }, () -> {}));
        save.addCallback(this::save);
        refreshTexts();
    }

    void reload() {
        if (busy) {
            setEnabled(false);
            return;
        }
        runTask(() -> {
            if (files == null) files = new ModFiles(ClientFiles.data("mods"), ClientFiles.data("../config/main.properties"));
            files.scan();
        }, () -> {
            verbose.setActive(files.verbose());
        });
    }

    private void selectFile() {
        if (busy || files == null) return;
        String selected;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer patterns = stack.mallocPointer(2);
            patterns.put(stack.UTF8("*.mod")).put(stack.UTF8("*.zip")).flip();
            selected = TinyFileDialogs.tinyfd_openFileDialog("", "", patterns, "", false);
        }
        if (selected == null) {
            notify.accept(ClientStrings.get(1085));
            return;
        }
        importFile(Path.of(selected));
    }

    private void importFile(Path source) {
        files.setVerbose(verbose.isActive());
        runTask(() -> {
            files.importFile(source);
            restartRequired = true;
            files.save();
        }, () -> notify.accept(ClientStrings.get(1227)));
    }

    private void save() {
        if (busy || files == null) return;
        files.setVerbose(verbose.isActive());
        runTask(() -> {
            try {
                restartRequired |= files.save();
            } catch (IOException exception) {
                throw new IOException(ClientStrings.get(87), exception);
            }
        }, this::requestClose);
    }

    private void requestClose() {
        if (busy) return;
        if (restartRequired) showMessage(ClientStrings.get(1179), this::saveAndRestart, false);
        else onClose.run();
    }

    private void saveAndRestart() {
        files.setVerbose(verbose.isActive());
        runTask(() -> {
            try {
                files.save();
            } catch (IOException exception) {
                throw new IOException(ClientStrings.get(87), exception);
            }
        }, onRestart);
    }

    private void openWebsite(String website) {
        if (website.isEmpty()) {
            notify.accept(ClientStrings.get(1196));
            return;
        }
        runTask(() -> java.awt.Desktop.getDesktop().browse(URI.create(website)), () -> {});
    }

    @FunctionalInterface
    private interface Operation {
        void run() throws IOException;
    }

    /** 文件和系统操作在后台执行，控件与纹理仅在 GUI 线程更新。 */
    private void runTask(Operation operation, Runnable completed) {
        if (busy) return;
        GUI gui = getGUI();
        busy = true;
        setEnabled(false);
        importMod.setText(ClientStrings.get(1095));
        Thread.startVirtualThread(() -> {
            Exception failure = null;
            try {
                operation.run();
            } catch (IOException | RuntimeException exception) {
                failure = exception;
            }
            Exception result = failure;
            gui.invokeLater(() -> {
                busy = false;
                setEnabled(true);
                refreshTexts();
                if (result == null) completed.run();
                else {
                    String message = ClientStrings.get(84);
                    for (int id : new int[]{1226, 1232, 87}) {
                        if (ClientStrings.get(id).equals(result.getMessage())) message = result.getMessage();
                    }
                    showMessage(message, () -> {}, false);
                }
            });
        });
    }

    private void updateList() {
        close();
        if (empty.getParent() != null) empty.getParent().removeChild(empty);
        DialogLayout list = new DialogLayout();
        list.setTheme("settings-label-area");
        DialogLayout.Group horizontal = list.createParallelGroup();
        DialogLayout.Group vertical = list.createSequentialGroup();
        if (files == null || files.entries().isEmpty()) {
            horizontal.addGroup(list.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
                    .addWidget(empty).addGap(0, 0, Short.MAX_VALUE));
            vertical.addGap(0, 0, Short.MAX_VALUE).addWidget(empty).addGap(0, 0, Short.MAX_VALUE);
        } else {
            for (ModFiles.Entry entry : files.entries()) {
                DialogLayout row = new DialogLayout();
                row.setTheme("mod-row");
                Widget icon = new DescriptionIcon(null);
                icon.setTheme("icon");
                if (entry.icon() != null) {
                    Texture texture = ((GlfwTwlRenderer) getGUI().getRenderer()).uploadImage(entry.icon());
                    iconTextures.add(texture);
                    Image image = texture.getImage(0, 0, 64, 64, Color.WHITE, false, Texture.Rotation.NONE);
                    icon = new DescriptionIcon(image);
                    icon.setTheme("icon");
                }
                Label title = new DescriptionLabel(entry.name() + "\n" + ClientStrings.get(1192).replace("{00}", entry.author())
                        + "\n" + ClientStrings.get(1193).replace("{00}", entry.version()));
                title.setTheme("label-settings-title");
                title.setTooltipContent(entry.description());
                icon.setTooltipContent(entry.description());
                title.setTooltipDelay(250);
                icon.setTooltipDelay(250);
                ToggleButton active = new ModToggleButton();
                active.setTheme("togglebutton");
                active.setActive(files.enabled(entry));
                active.addCallback(() -> {
                    files.setEnabled(entry, active.isActive());
                    restartRequired = true;
                    updateList();
                });
                Button remove = new ModActionButton(ClientStrings.get(1228));
                remove.addCallback(() -> showMessage(ClientStrings.get(1229).replace("{00}", entry.name()),
                        () -> runTask(() -> {
                            files.delete(entry);
                            restartRequired = true;
                        }, () -> {}), true));
                Button website = new ModActionButton(ClientStrings.get(1195));
                website.addCallback(() -> openWebsite(entry.website()));
                DialogLayout actions = new DialogLayout();
                actions.setTheme("mod-actions");
                actions.setHorizontalGroup(actions.createParallelGroup(active, remove, website));
                actions.setVerticalGroup(actions.createSequentialGroup(active, remove, website));
                DialogLayout order = new DialogLayout();
                order.setTheme("mod-actions");
                order.setHorizontalGroup(order.createParallelGroup());
                order.setVerticalGroup(order.createSequentialGroup());
                if (files.enabled(entry)) {
                    Button up = new ModActionButton(ClientStrings.get(9010));
                    Button down = new ModActionButton(ClientStrings.get(9011));
                    up.setTheme("button-symbol");
                    down.setTheme("button-symbol");
                    // 不可移动的方向隐藏，但继续占据布局位置，避免另一方向跳动。
                    up.setVisible(files.order(entry) != 0 || files.enabledCount() == 1);
                    down.setVisible(files.order(entry) + 1 < files.enabledCount());
                    up.addCallback(() -> { files.move(entry, -1); updateList(); });
                    down.addCallback(() -> { files.move(entry, 1); updateList(); });
                    order.setHorizontalGroup(order.createParallelGroup(up, down));
                    order.setVerticalGroup(order.createSequentialGroup(up, down));
                }
                row.setHorizontalGroup(row.createSequentialGroup(icon, title, actions, order));
                row.setVerticalGroup(row.createParallelGroup(icon, title, actions, order));
                row.setWidgetAlignment(icon, Alignment.CENTER);
                row.setWidgetAlignment(actions, Alignment.TOP);
                row.setWidgetAlignment(order, Alignment.TOP);
                horizontal.addWidget(row);
                vertical.addWidget(row);
            }
            // 剩余高度留在列表底部，单个条目也从顶部开始排列。
            vertical.addGap(0, 0, Short.MAX_VALUE);
        }
        list.setHorizontalGroup(horizontal);
        list.setVerticalGroup(vertical);
        scroll.setContent(list);
        invalidateLayout();
    }

    private void showMessage(String text, Runnable confirmed, boolean cancellable) {
        DialogLayout dialog = new DialogLayout();
        dialog.setTheme("confirm-panel");
        Label message = new Label(text);
        message.setTheme("label-lalign");
        Button ok = new Button(ClientStrings.get(52));
        Button cancel = new Button(ClientStrings.get(53));
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
        ok.addCallback(() -> { popup.closePopup(); confirmed.run(); });
        cancel.addCallback(popup::closePopup);
        DialogLayout.Group buttons = dialog.createSequentialGroup().addWidget(ok);
        DialogLayout.Group height = dialog.createParallelGroup().addWidget(ok);
        if (cancellable) { buttons.addWidget(cancel); height.addWidget(cancel); }
        dialog.setHorizontalGroup(dialog.createParallelGroup().addWidget(message).addGroup(buttons));
        dialog.setVerticalGroup(dialog.createSequentialGroup().addWidget(message).addGap(5).addGroup(height));
        popup.add(dialog);
        if (popup.openPopup()) {
            int width = Math.max(40, Math.min(getGUI().getWidth() - 80, 600));
            StringBuilder wrapped = new StringBuilder();
            StringBuilder line = new StringBuilder();
            for (char character : text.toCharArray()) {
                if (character == '\n') {
                    wrapped.append(line).append('\n');
                    line.setLength(0);
                } else {
                    if (message.getFont().computeTextWidth(line.toString() + character) > width) {
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

    @Override
    public void close() {
        iconTextures.forEach(Texture::destroy);
        iconTextures.clear();
    }

    void refreshTexts() {
        setTitle(ClientStrings.get(1190));
        empty.setText(ClientStrings.get(1230));
        browse.setText(ClientStrings.get(1191));
        importMod.setText(ClientStrings.get(1225));
        openFolder.setText(ClientStrings.get(1189));
        verboseLabel.setText(ClientStrings.get(1198));
        verbose.setTooltipContent(ClientStrings.get(1199));
        verboseLabel.setTooltipContent(ClientStrings.get(1199));
        save.setText(ClientStrings.get(54));
        if (getGUI() != null) updateList();
    }

    private static void positionTooltip(Widget widget, GUI.TooltipPositionAdjuster.TooltipPositionData position) {
        // 提示框底边对齐控件顶边，水平中心跟随控件而非鼠标。
        position.x = widget.getX() + widget.getWidth() / 2;
        position.y = widget.getY();
        position.alignment = Alignment.BOTTOM;
    }

    private static final class DescriptionLabel extends Label implements GUI.TooltipPositionAdjuster {
        private DescriptionLabel(String text) {
            super(text);
        }

        @Override
        public void adjustTooltipPosition(TooltipPositionData position) {
            positionTooltip(this, position);
        }
    }

    /** 控件高度由主题限制，避免按钮图集的原始高度撑大 MOD 操作列。 */
    private static final class ModActionButton extends Button {
        private ModActionButton(String text) {
            super(text);
            setTheme("button");
        }

        @Override
        public int getPreferredHeight() {
            int height = super.getPreferredHeight();
            return getMaxHeight() > 0 ? Math.min(height, getMaxHeight()) : height;
        }
    }

    private static final class ModToggleButton extends ToggleButton {
        @Override
        public int getPreferredHeight() {
            int height = super.getPreferredHeight();
            return getMaxHeight() > 0 ? Math.min(height, getMaxHeight()) : height;
        }
    }

    private static final class DescriptionIcon extends Widget implements GUI.TooltipPositionAdjuster {
        private final Image image;

        private DescriptionIcon(Image image) {
            this.image = image;
        }

        @Override
        protected void paintWidget(GUI gui) {
            if (image != null) image.draw(getAnimationState(), getX(), getY(), getWidth(), getHeight());
        }

        @Override
        protected boolean handleEvent(Event event) {
            return event.isMouseEvent() && event.getType() != Event.Type.MOUSE_WHEEL;
        }

        @Override
        public void adjustTooltipPosition(TooltipPositionData position) {
            positionTooltip(this, position);
        }
    }

    private static final class TooltipToggleButton extends ToggleButton implements GUI.TooltipPositionAdjuster {
        private TooltipToggleButton() {
            super("    ");
        }

        @Override
        public void adjustTooltipPosition(TooltipPositionData position) {
            positionTooltip(this, position);
        }
    }

    private static final class TooltipLabel extends Label implements GUI.TooltipPositionAdjuster {
        @Override
        public void adjustTooltipPosition(TooltipPositionData position) {
            positionTooltip(this, position);
        }
    }
}
