package top.totodile.pokemmo.client.ui.management;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.common.ClientWindow;

/** MOD 管理界面；文件导入及 MOD 加载尚未接入。 */
public final class ModManagementWindow extends ClientWindow {
    private final boolean mobile;
    private final Runnable onClose;
    private final java.util.function.Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground;
    private final Button save = new Button();
    private final ToggleButton verbose = new ToggleButton("    ");
    private final ResizableFrame frame = new ResizableFrame() {
        private int headerActionRight;
        @Override protected void applyTheme(ThemeInfo theme) {
            super.applyTheme(theme);
            if (mobile) headerActionRight = theme.getParameter("headerActionRight", 68);
        }
        @Override protected void paintBackground(GUI gui) {
            if (mobile) mobileBackground.accept(getBackground()); else super.paintBackground(gui);
        }
        @Override protected boolean isFrameElement(Widget child) {
            return mobile && child == save || super.isFrameElement(child);
        }
        @Override protected void layout() {
            super.layout();
            if (mobile) {
                save.adjustSize();
                save.setPosition(getInnerRight() - save.getWidth() - headerActionRight, getInnerY());
            }
        }
    };

    public ModManagementWindow(Widget owner, ClientStrings strings, boolean mobile, Runnable onClose,
                        java.util.function.Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground) {
        super(owner);
        this.mobile = mobile;
        this.onClose = onClose;
        this.mobileBackground = mobileBackground;
        frame.setTheme("/mods-panel");
        frame.setTitle(strings.get(1190));
        frame.setDraggable(false);
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        frame.addCloseCallback(this::closePopup);
        var content = new DialogLayout() {
            @Override public int getMinWidth() { return mobile ? super.getMinWidth() : Math.max(510, super.getMinWidth()); }
        };
        var list = new DialogLayout();
        list.setTheme("settings-label-area");
        var empty = new Label(strings.get(1230));
        list.setHorizontalGroup(list.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
                .addWidget(empty).addGap(0, 0, Short.MAX_VALUE));
        list.setVerticalGroup(list.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
                .addWidget(empty).addGap(0, 0, Short.MAX_VALUE));
        var scroll = new ScrollPane(list);
        scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        scroll.setExpandContentSize(true);
        var browse = new Button(strings.get(1191));
        var importMod = new Button(strings.get(1225));
        var folder = new Button(strings.get(mobile ? 1154 : 1189));
        verbose.setTooltipContent(strings.get(1199));
        var verboseLabel = new Label(strings.get(1198));
        verboseLabel.setTooltipContent(strings.get(1199));
        verboseLabel.setLabelFor(verbose);
        save.setText(strings.get(54));
        save.addCallback(this::closePopup);
        var actions = content.createSequentialGroup(browse, importMod, folder)
                .addGap(0, 0, Short.MAX_VALUE).addWidget(verbose).addWidget(verboseLabel);
        var actionRow = content.createParallelGroup(browse, importMod, folder, verbose, verboseLabel);
        if (mobile) {
            save.setText("");
            save.setTheme("/mobile-save-icon");
            frame.add(save);
        } else actions.addGap(0, 0, Short.MAX_VALUE).addWidget(save);
        if (!mobile) actionRow.addWidget(save);
        content.setHorizontalGroup(content.createParallelGroup(scroll).addGroup(actions));
        var rows = content.createSequentialGroup(scroll).addGroup(actionRow);
        // 参考布局的末尾 5 像素属于纵向组，留在操作行下方。
        if (!mobile) rows.addGap(5);
        content.setVerticalGroup(rows);
        if (!mobile) content.setMinSize(510, 100);
        frame.add(content);
        add(frame);
    }

    public void open() {
        if (isOpen()) { raiseWindow(); return; }
        verbose.setActive(false);
        if (openPopup()) {
            positionWindow();
            frame.setHardVisible(true);
        }
    }

    public void positionWindow() {
        if (!isOpen()) return;
        Widget owner = getOwner();
        if (mobile) {
            setPosition(owner.getInnerX(), owner.getInnerY());
            setSize(owner.getInnerWidth(), owner.getInnerHeight());
        } else {
            adjustSize();
            setPosition(owner.getInnerX() + (owner.getInnerWidth() - getWidth()) / 2,
                    owner.getInnerY() + (owner.getInnerHeight() - getHeight()) / 2);
        }
    }

    @Override public void closePopup() {
        boolean wasOpen = isOpen();
        super.closePopup();
        if (wasOpen) onClose.run();
    }
}
