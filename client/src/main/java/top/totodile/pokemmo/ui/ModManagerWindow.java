package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientStrings;

/** MOD 管理窗口的空列表界面，操作按钮仅展示，不读写文件或配置。 */
final class ModManagerWindow extends ResizableFrame {
    private final Label empty = new Label();
    private final Button browse = new Button();
    private final Button importMod = new Button();
    private final Button openFolder = new Button();
    private final ToggleButton verbose = new TooltipToggleButton();
    private final Label verboseLabel = new TooltipLabel();
    private final Button save = new Button();

    ModManagerWindow(Runnable onClose) {
        setTheme("mods-panel");
        setResizableAxis(ResizableAxis.NONE);
        setDraggable(false);
        addCloseCallback(onClose);

        DialogLayout list = new DialogLayout();
        list.setTheme("settings-label-area");
        list.setHorizontalGroup(list.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE).addWidget(empty).addGap(0, 0, Short.MAX_VALUE));
        list.setVerticalGroup(list.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE).addWidget(empty).addGap(0, 0, Short.MAX_VALUE));
        ScrollPane scroll = new ScrollPane(list);
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
        refreshTexts();
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
    }

    private static void positionTooltip(Widget widget, GUI.TooltipPositionAdjuster.TooltipPositionData position) {
        // 提示框底边对齐控件顶边，水平中心跟随控件而非鼠标。
        position.x = widget.getX() + widget.getWidth() / 2;
        position.y = widget.getY();
        position.alignment = Alignment.BOTTOM;
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
