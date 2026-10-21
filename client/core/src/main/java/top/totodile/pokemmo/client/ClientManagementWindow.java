package top.totodile.pokemmo.client;

import de.matthiasmann.twl.*;

/** 客户端管理的展示与窗口导航；ROM、语言导入尚未接入。 */
final class ClientManagementWindow extends PopupWindow {
    private final boolean mobile;
    private final Button gear = new Button();
    private final ResizableFrame frame = new ResizableFrame() {
        @Override protected boolean isFrameElement(Widget widget) {
            return widget == gear || super.isFrameElement(widget);
        }
        @Override protected void layout() {
            super.layout();
            if (mobile) {
                gear.adjustSize();
                gear.setPosition(getInnerRight() - gear.getWidth() - 68, getInnerY());
            }
        }
    };
    private final Runnable onClose;
    private final DialogLayout content = new DialogLayout();

    ClientManagementWindow(Widget owner, ClientStrings strings, boolean mobile, Runnable onClose) {
        super(owner);
        this.mobile = mobile;
        this.onClose = onClose;
        setTheme("");
        setCloseOnClickedOutside(false);
        frame.setTheme("/rom-panel");
        frame.setTitle(strings.get(1175));
        frame.setDraggable(false);
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        frame.addCloseCallback(this::closePopup);
        DialogLayout outer = new DialogLayout();
        content.setAddDefaultGaps(false);
        var columns = content.createParallelGroup();
        var rows = content.createSequentialGroup();
        Label language = new Label(strings.get(1332));
        language.setTheme("label-settings-title");
        Widget languageValue = new LanguageDisplay(strings.languageName(strings.language()));
        columns.addGroup(content.createSequentialGroup().addWidget(language).addGap(5).addWidget(languageValue).addGap(5));
        rows.addGroup(content.createParallelGroup(language, languageValue));
        for (int type : new int[]{2, 0, 1, 3, 4}) {
            Label label = new Label(strings.get(90 + type) + ":");
            label.setTheme("label-settings-title");
            Button choose = new Button(strings.get(1176));
            Button state = new Button(strings.get(type == 2 ? 1168 : 1169));
            columns.addGroup(content.createSequentialGroup().addWidget(label).addGap(5)
                    .addWidget(choose).addGap(5).addWidget(state).addGap(5));
            rows.addGap(5).addGroup(content.createParallelGroup(label, choose, state));
        }
        Button first = new Button(strings.get(mobile ? 1162 : 1174));
        Button last = new Button(strings.get(mobile ? 1163 : 65));
        if (!mobile) last.addCallback(this::closePopup);
        var actions = content.createSequentialGroup();
        if (mobile) actions.addGap(0, 0, Short.MAX_VALUE).addWidget(first).addGap(5).addWidget(last);
        else actions.addWidget(first).addGap(0, 0, Short.MAX_VALUE).addWidget(last);
        columns.addGroup(actions.addGap(5));
        rows.addGap(5).addGroup(content.createParallelGroup(first, last)).addGap(5);
        content.setHorizontalGroup(columns);
        content.setVerticalGroup(rows);
        outer.setHorizontalGroup(outer.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
                .addWidget(content).addGap(0, 0, Short.MAX_VALUE));
        outer.setVerticalGroup(outer.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
                .addWidget(content).addGap(0, 0, Short.MAX_VALUE));
        frame.add(outer);
        add(frame);
        if (mobile) {
            gear.setTheme("/mobile-gear-icon");
            gear.setCanAcceptKeyboardFocus(false);
            frame.add(gear);
        }
    }

    void open() {
        if (isOpen()) return;
        if (openPopup()) {
            positionWindow();
            frame.setHardVisible(true);
        }
    }

    void positionWindow() {
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

    /** 使用参考 combobox 的 display/button 子主题，只展示当前语言。 */
    private static final class LanguageDisplay extends Widget {
        private final Label display;
        private final Widget arrow = new Widget();
        LanguageDisplay(String text) {
            setTheme("combobox");
            display = new Label(text);
            display.setTheme("display");
            arrow.setTheme("button");
            add(display);
            add(arrow);
        }
        @Override public int getPreferredInnerWidth() { return display.getPreferredWidth() + arrow.getPreferredWidth(); }
        @Override public int getPreferredInnerHeight() { return Math.max(display.getPreferredHeight(), arrow.getPreferredHeight()); }
        @Override protected void layout() {
            int width = arrow.getPreferredWidth();
            arrow.setPosition(getInnerRight() - width, getInnerY());
            arrow.setSize(width, getInnerHeight());
            display.setPosition(getInnerX(), getInnerY());
            display.setSize(Math.max(0, getInnerWidth() - width), getInnerHeight());
        }
    }
}
