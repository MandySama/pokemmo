package top.totodile.pokemmo.client.ui.management;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.rom.RomFilePicker;
import top.totodile.pokemmo.client.rom.RomInfo;
import top.totodile.pokemmo.client.rom.RomManager;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.ui.common.ClientWindow;
import top.totodile.pokemmo.client.ui.common.MessageDialog;
import top.totodile.pokemmo.client.ui.common.ImportProgressDialog;
import top.totodile.pokemmo.client.rom.RomImportFailure;

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
            return widget == gear || super.isFrameElement(widget);
        }
        @Override protected void layout() {
            super.layout();
            if (mobile) {
                gear.adjustSize();
                gear.setPosition(getInnerRight() - gear.getWidth() - headerActionRight, getInnerY());
            }
        }
    };
    private final Runnable onClose;
    private final java.util.function.Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground;
    private final DialogLayout content = new DialogLayout();
    private final java.util.Map<Integer, Button> states = new java.util.LinkedHashMap<>();
    private final java.util.List<Button> chooseButtons = new java.util.ArrayList<>();
    private final MessageDialog messageDialog;
    private final ImportProgressDialog progressDialog;
    private final RomManager roms;
    private final RomFilePicker picker;
    private final Runnable changed;
    private final java.util.function.Consumer<String> notification;
    private final ClientStrings strings;
    private boolean busy, disposed;
    private boolean restartRequired;
    private Runnable restart=() -> com.badlogic.gdx.Gdx.app.exit();
    private volatile double progressValue;
    private volatile int progressStage=1095;

    public void onRestart(Runnable restart) { this.restart=java.util.Objects.requireNonNull(restart); }
    public boolean isImporting() { return busy; }

    public ClientManagementWindow(Widget owner, ClientStrings strings, boolean mobile, Runnable onClose,
                           java.util.function.Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground,
                           RomManager roms, RomFilePicker picker, Runnable changed,
                           java.util.function.Consumer<String> notification) {
        super(owner);
        this.roms=roms;
        this.picker=picker;
        this.changed=changed;
        this.notification=notification;
        this.strings=strings;
        this.mobile = mobile;
        this.onClose = onClose;
        this.mobileBackground = mobileBackground;
        progressDialog=new ImportProgressDialog(owner,mobile,mobileBackground);
        messageDialog = new MessageDialog(owner, strings, mobile, mobileBackground, () -> {
            if (isOpen()) raiseWindow(); else onClose.run();
        });
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
            state.addCallback(() -> showRomInfo(RomType.of(type)));
            columns.addGroup(content.createSequentialGroup().addWidget(label).addGap(5)
                    .addWidget(choose).addGap(5).addWidget(state).addGap(5));
            rows.addGap(5).addGroup(content.createParallelGroup(label, choose, state));
        }
        Button first = new Button(strings.get(mobile ? 1162 : 1174));
        Button last = new Button(strings.get(mobile ? 1163 : 65));
        if (!mobile) last.addCallback(this::closePopup);
        if (!mobile) first.setTooltipContent(strings.get(1173));
        if (!mobile) first.addCallback(() -> {
            if (picker == null) { showMessage("当前环境不支持打开文件夹"); return; }
            try { java.nio.file.Files.createDirectories(roms.directory());
                picker.openDirectory(roms.directory().toString(), text -> com.badlogic.gdx.Gdx.app.postRunnable(() -> showMessage(text)));
            } catch (java.io.IOException error) { showMessage(error.getMessage()); }
        });
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

    public void open() {
        if (isOpen()) { raiseWindow(); return; }
        updateStates();
        if (openPopup()) {
            positionWindow();
            frame.setHardVisible(true);
        }
    }

    private void updateStates() {
        states.forEach((type, button) -> {
            RomInfo rom=roms.get(RomType.of(type));
            button.setText(rom==null ? strings.get(type==2?1168:1169) : strings.get(1177));
        });
    }

    private void showRomInfo(RomType type) {
        RomInfo rom=roms.get(type);
        String name=strings.get(90+type.id);
        if (rom==null) { showMessage(strings.format(1186,name)); return; }
        // 参考 ClientSettingsWindow.Xz0/gz；GBA 显示注册版本名，NDS 显示 ROM 类型名。
        if (type.extension.equals("gba")) {
            String base=type==RomType.FIRE_RED ? "Fire Red" : "Emerald";
            String region=switch (rom.code()) {
                case "BPRE", "BPEE" -> "USA";
                case "BPRF", "BPEF" -> "FR";
                case "BPRD", "BPED" -> "DE";
                case "BPRS", "BPES" -> "ES";
                case "BPRI", "BPEI" -> "IT";
                default -> "";
            };
            name=region.isEmpty() ? base : base+" v1."+rom.revision()+" "+region;
        }
        java.nio.file.Path path=rom.path();
        String displayPath=path.startsWith(roms.directory())
                ? roms.directory().getFileName().resolve(roms.directory().relativize(path)).toString()
                : "..."+java.io.File.separator+path.getFileName();
        showMessage(strings.format(1185,name,displayPath));
    }

    private void showMessage(String text) {
        if (!disposed) messageDialog.show(text==null ? strings.get(2103) : text);
    }

    public boolean closeMessage() {
        if (!messageDialog.isOpen()) return false;
        messageDialog.closePopup();
        return true;
    }

    private void chooseRom(int type) {
        if (busy || disposed) return;
        if (picker==null) { showMessage("当前环境未提供文件选择器"); return; }
        busy=true;
        var selected=RomType.of(type);
        try {
            picker.choose(selected.extension, source -> com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                if(disposed)return;
                if (source==null) {
                    finish(null);
                    notification.accept(strings.get(1085));
                    return;
                }
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    String result;
                    boolean imported=false;
                    boolean loaded=false;
                    String kind=strings.get(90+selected.id);
                    String filename=source.name();
                    if(filename==null)filename=kind;
                    // 只在提示中显示文件名，兼容选择器返回的 Windows/Android 路径。
                    filename=filename.substring(Math.max(filename.lastIndexOf('/'),filename.lastIndexOf('\\'))+1);
                    try {
                        boolean[] progressStarted={false};
                        var outcome=roms.importRom(selected,source,(stage,value) -> {
                            progressStage=stage;progressValue=value;
                            if(!progressStarted[0]) {
                                progressStarted[0]=true;
                                com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                                    if(!disposed && busy)progressDialog.show(strings.get(progressStage));
                                });
                            }
                        });
                        imported=outcome==RomManager.ImportResult.IMPORTED;
                        loaded=true;
                        result=strings.format(imported?1184:1180,kind);
                    } catch (RomImportFailure error) {
                        String game=kind.endsWith("ROM")?kind.substring(0,kind.length()-3):kind;
                        result=switch(error.stringId) {
                            case 1089,1181 -> strings.format(error.stringId,error.detail);
                            case 1090 -> strings.format(1090,error.detail,filename);
                            case 1092,1093 -> strings.format(error.stringId,filename);
                            default -> strings.format(error.stringId,filename,game);
                        };
                    } catch (Exception error) {
                        result=strings.format(1094,filename);
                    }
                    String text=result;
                    // 参考 Le0：成功选择并加载 ROM 即标记重启，已加载的相同文件也适用。
                    boolean changed=loaded;
                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                        if(changed)restartRequired=true;
                        finish(changed?null:text);
                        if(changed && !disposed)notification.accept(text);
                    });
                }).whenComplete((ignored,error) -> {
                    // CompletableFuture 会保存未捕获异常；统一结束 busy，避免界面永久卡在导入状态。
                    if(error!=null) {
                        com.badlogic.gdx.Gdx.app.error("ROM import","Unable to complete ROM import",error);
                        com.badlogic.gdx.Gdx.app.postRunnable(() -> finish(strings.get(2103)));
                    }
                });
            }), text -> com.badlogic.gdx.Gdx.app.postRunnable(() -> finish(text)));
        } catch (RuntimeException error) { finish(error.getMessage()); }
    }

    private void finish(String text) {
        if (disposed) return;
        progressDialog.closePopup();
        busy=false;
        updateStates(); changed.run();
        if (text!=null) showMessage(text);
        positionWindow();
    }

    public void dispose() { disposed=true; progressDialog.closePopup(); messageDialog.closePopup(); }

    public void positionWindow() {
        messageDialog.positionWindow();
        if(progressDialog.isOpen())progressDialog.update(strings.get(progressStage),progressValue);
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
        if(busy)return;
        if(!disposed && roms.get(RomType.BLACK_WHITE)==null) {
            showMessage(strings.format(1178,strings.get(92)));
            return;
        }
        if(restartRequired && !disposed) {
            messageDialog.show(strings.get(1179),() -> {
                if(disposed)return;
                try { restart.run(); }
                catch(RuntimeException error) { showMessage(strings.get(2103)); }
            });
            return;
        }
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
