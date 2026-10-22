package top.totodile.pokemmo.client.ui.management;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.rom.RomFilePicker;
import top.totodile.pokemmo.client.rom.RomInfo;
import top.totodile.pokemmo.client.rom.RomManager;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.ui.common.ClientWindow;

/** 客户端 ROM 状态、导入与窗口导航。 */
public final class ClientManagementWindow extends ClientWindow {
    private final boolean mobile;
    private final Button gear = new Button();
    private final ResizableFrame frame = new ResizableFrame() {
        private int headerActionRight;
        @Override protected void applyTheme(ThemeInfo theme) {
            super.applyTheme(theme);
            if (mobile) headerActionRight = theme.getParameter("headerActionRight", 68);
        }
        @Override protected void paintBackground(GUI gui) {
            if (mobile) mobileBackground.accept(getBackground()); else super.paintBackground(gui);
        }
        @Override protected boolean isFrameElement(Widget widget) {
            return widget == gear || (mobile && widget == message) || super.isFrameElement(widget);
        }
        @Override protected void layout() {
            super.layout();
            if (mobile) {
                gear.adjustSize();
                gear.setPosition(getInnerRight() - gear.getWidth() - headerActionRight, getInnerY());
                message.adjustSize();
                message.setPosition(getInnerX()+30, getInnerBottom()-50);
            }
        }
    };
    private final Runnable onClose;
    private final java.util.function.Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground;
    private final DialogLayout content = new DialogLayout();
    private final java.util.Map<Integer, Button> states = new java.util.LinkedHashMap<>();
    private final java.util.List<Button> chooseButtons = new java.util.ArrayList<>();
    private final Label message = new Label("");
    private final RomManager roms;
    private final RomFilePicker picker;
    private final Runnable changed;
    private final ClientStrings strings;
    private boolean busy, disposed;

    public ClientManagementWindow(Widget owner, ClientStrings strings, boolean mobile, Runnable onClose,
                           java.util.function.Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground,
                           RomManager roms, RomFilePicker picker, Runnable changed) {
        super(owner);
        this.roms=roms;
        this.picker=picker;
        this.changed=changed;
        this.strings=strings;
        this.mobile = mobile;
        this.onClose = onClose;
        this.mobileBackground = mobileBackground;
        setTheme("");
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
            states.put(type, state);
            chooseButtons.add(choose);
            choose.addCallback(() -> chooseRom(type));
            state.addCallback(() -> {
                RomInfo rom=roms.get(RomType.of(type));
                if (rom != null) showMessage(rom.title()+" / "+rom.code()+" / v"+rom.revision()+" / "+(rom.size()/1024/1024)+" MiB");
            });
            columns.addGroup(content.createSequentialGroup().addWidget(label).addGap(5)
                    .addWidget(choose).addGap(5).addWidget(state).addGap(5));
            rows.addGap(5).addGroup(content.createParallelGroup(label, choose, state));
        }
        Button first = new Button(strings.get(mobile ? 1162 : 1174));
        Button last = new Button(strings.get(mobile ? 1163 : 65));
        if (!mobile) last.addCallback(this::closePopup);
        if (!mobile) first.addCallback(() -> {
            if (picker == null) { showMessage("当前环境不支持打开文件夹"); return; }
            try { java.nio.file.Files.createDirectories(roms.directory());
                picker.openDirectory(roms.directory().toString(), text -> com.badlogic.gdx.Gdx.app.postRunnable(() -> showMessage(text)));
            } catch (java.io.IOException error) { showMessage(error.getMessage()); }
        });
        Button refresh = new Button("重新扫描 ROM");
        refresh.addCallback(() -> { if (!busy) refreshRoms(); });
        if (!mobile) {
            columns.addWidget(message);
            rows.addGap(5).addWidget(message);
        }
        var actions = content.createSequentialGroup();
        if (mobile) actions.addWidget(refresh).addGap(0, 0, Short.MAX_VALUE).addWidget(first).addGap(5).addWidget(last);
        else actions.addWidget(first).addGap(5).addWidget(refresh).addGap(0, 0, Short.MAX_VALUE).addWidget(last);
        columns.addGroup(actions.addGap(5));
        rows.addGap(5).addGroup(content.createParallelGroup(first, last, refresh)).addGap(5);
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
            message.setTheme("/label");
            frame.add(message);
        }
    }

    public void open() {
        if (isOpen()) { raiseWindow(); return; }
        if (!busy) refreshRoms();
        if (openPopup()) {
            positionWindow();
            frame.setHardVisible(true);
        }
    }

    private void refreshRoms() {
        roms.refresh();
        updateStates();
        changed.run();
        showMessage(String.join("\n", roms.errors()));
    }

    private void updateStates() {
        states.forEach((type, button) -> {
            RomInfo rom=roms.get(RomType.of(type));
            button.setText(rom==null ? strings.get(type==2?1168:1169) : "已加载 " + rom.code());
            button.setTooltipContent(rom==null ? strings.get(type==2?1168:1169)
                    : rom.path()+"\n"+rom.title()+" / v"+rom.revision()+"\n"+rom.size()+" bytes / "+rom.files().size()+" files");
        });
    }

    private void showMessage(String text) {
        if (disposed) return;
        if (text==null) text="ROM 读取失败";
        String preview=text.replace('\n', ' ').replace('\r', ' ');
        message.setText(preview.length()>44 ? preview.substring(0,44)+"…" : preview);
        message.setTooltipContent(text);
        content.invalidateLayout();
    }

    private void chooseRom(int type) {
        if (busy || disposed) return;
        if (picker==null) { showMessage("当前环境未提供文件选择器"); return; }
        busy=true;
        chooseButtons.forEach(button -> button.setEnabled(false));
        var selected=RomType.of(type);
        try {
            picker.choose(selected.extension, source -> {
                if (source==null) { com.badlogic.gdx.Gdx.app.postRunnable(() -> finish(null)); return; }
                com.badlogic.gdx.Gdx.app.postRunnable(() -> showMessage("正在读取 ROM…"));
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    String result;
                    try { roms.importRom(selected, source); result="ROM 导入成功"; }
                    catch (Exception error) { result=error.getMessage(); }
                    String text=result;
                    com.badlogic.gdx.Gdx.app.postRunnable(() -> finish(text));
                });
            }, text -> com.badlogic.gdx.Gdx.app.postRunnable(() -> finish(text)));
        } catch (RuntimeException error) { finish(error.getMessage()); }
    }

    private void finish(String text) {
        if (disposed) return;
        busy=false;
        chooseButtons.forEach(button -> button.setEnabled(true));
        updateStates(); changed.run(); showMessage(text==null?"":text);
        positionWindow();
    }

    public void dispose() { disposed=true; }

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
