package top.totodile.pokemmo.client.ui.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleListModel;
import java.util.List;
import top.totodile.pokemmo.client.config.ClientSettings;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.common.ClientWindow;

/** 设置编辑与提交；关闭或取消时丢弃本次未保存的改动。 */
public final class SettingsWindow extends ClientWindow {
    private final boolean mobile;
    private final ClientStrings strings;
    private final Runnable onClose;
    private final java.util.function.Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground;
    private final SettingsTabs tabs;
    private final Button save;
    private final ClientSettings preferences;
    private final java.util.function.Consumer<String> notification;
    private record Binding(String initial, java.util.function.Supplier<String> read, java.util.function.Consumer<String> write) {}
    private final java.util.Map<String,Binding> bindings = new java.util.LinkedHashMap<>();
    private SettingsButton pendingKey;
    private final java.util.Map<SettingsButton,String> keyBindings = new java.util.HashMap<>();
    private final ResizableFrame frame = new ResizableFrame() {
        private int headerActionRight;
        @Override protected void applyTheme(ThemeInfo theme) {
            super.applyTheme(theme);
            if (mobile) headerActionRight = theme.getParameter("headerActionRight", 68);
        }
        @Override protected void paintBackground(GUI gui) {
            if (mobile) mobileBackground.accept(getBackground()); else super.paintBackground(gui);
        }
        @Override protected boolean isFrameElement(Widget child) { return mobile && child == save || super.isFrameElement(child); }
        @Override protected void layout() {
            super.layout();
            if (mobile) {
                save.adjustSize();
                save.setPosition(getInnerRight() - save.getWidth() - headerActionRight, getInnerY());
            }
        }
    };

    public SettingsWindow(Widget owner, ClientStrings strings, boolean mobile, Runnable onClose,
                   java.util.function.Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground,
                   ClientSettings preferences,java.util.function.Consumer<String> notification) {
        super(owner);
        this.strings = strings;
        this.mobile = mobile;
        this.tabs = new SettingsTabs(mobile);
        this.onClose = onClose;
        this.mobileBackground = mobileBackground;
        this.preferences = preferences;
        this.notification = notification;
        setTheme("");
        frame.setTheme("/settings-panel");
        frame.setTitle(strings.get(1200));
        frame.setDraggable(false);
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        frame.addCloseCallback(this::closePopup);
        save = new Button(strings.get(54));
        save.addCallback(this::saveSettings);
        var outer = new DialogLayout() {
            @Override protected void layout() {
                if (!mobile) { super.layout(); return; }
                int width = Math.min(getInnerWidth(), tabs.getPreferredWidth());
                tabs.setSize(width, getInnerHeight());
                tabs.setPosition(getInnerX() + (getInnerWidth() - width) / 2, getInnerY());
            }
        };
        outer.setTheme("dialoglayout");
        var horizontal = outer.createParallelGroup(tabs);
        var vertical = outer.createSequentialGroup(tabs);
        if (mobile) {
            save.setText("");
            save.setTheme("/mobile-save-icon");
            frame.add(save);
        } else {
            Button cancel = new Button(strings.get(53));
            cancel.addCallback(this::closePopup);
            horizontal.addGroup(outer.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addWidget(cancel).addWidget(save));
            vertical.addGroup(outer.createParallelGroup(cancel, save));
        }
        outer.setHorizontalGroup(horizontal);
        outer.setVerticalGroup(vertical);
        frame.add(outer);
        add(frame);
        buildPages();
    }

    private void bind(String key, java.util.function.Supplier<String> read, java.util.function.Consumer<String> write) {
        Binding binding = new Binding(read.get(),read,write);
        if (bindings.put(key,binding) != null) throw new IllegalArgumentException("Duplicate setting " + key);
        write.accept(preferences.get(key,binding.initial()));
    }
    private void restoreSaved() {
        pendingKey = null;
        bindings.forEach((key,binding) -> binding.write().accept(preferences.get(key,binding.initial())));
    }
    private void saveSettings() {
        java.util.Map<String,String> changes = new java.util.LinkedHashMap<>();
        bindings.forEach((key,binding) -> changes.put(key,binding.read().get()));
        try { preferences.save(changes); closePopup(); }
        catch (RuntimeException error) {
            Gdx.app.error("settings","Cannot save client settings",error);
            notification.accept(strings.get(2103));
        }
    }

    public boolean captureKey(int key) {
        if (pendingKey == null || !isOpen()) return false;
        if (key != com.badlogic.gdx.Input.Keys.ESCAPE && key != com.badlogic.gdx.Input.Keys.BACK) pendingKey.setText(com.badlogic.gdx.Input.Keys.toString(key));
        pendingKey = null; return true;
    }

    private void buildPages() {
        Page video = page(1201);
        if (!mobile) {
            video.choice(1270, 1271, 1272, 1273);
            video.choice(1250, Gdx.graphics.getWidth() + " x " + Gdx.graphics.getHeight());
            video.choice(1217, "OpenGL", "ANGLE");
        } else {
            video.toggle(600, false);
            video.toggle(1243, false);
        }
        video.number(1254, strings.get(1220));
        video.number(1221, strings.get(1220));
        video.toggle(1297, true);
        video.number(1264, strings.get(1265));
        video.number(1241, strings.get(1240));
        if (!mobile) { video.toggle(1257, true); video.toggle(1255, true); }
        video.choice(1253, 1289, 1287);
        video.toggle(1252, true);
        if (mobile) video.choice(7701, 1289, 1287);
        else { video.number(1237, "1x"); video.number(1238, "1x"); }
        video.toggle(1233, !mobile);

        Page sound = page(1202);
        sound.number(1276, "50"); sound.number(1277, "50"); sound.number(1279, "50");
        if (!mobile) sound.choice(1365, 1366);
        sound.toggle(1282, true); sound.number(1283, "100%"); sound.toggle(1343, true);

        Page ui = page(1210);
        ui.choice(1260, mobile ? "android" : "default");
        if (!mobile) {
            ui.choice(1247, "100%"); ui.number(1274, "100%"); ui.toggle(1266, false);
        }
        ui.toggle(609, true);
        ui.toggle(1245, true);
        if (strings.typeface() != null) ui.toggle(1236, true);
        if (!mobile) ui.toggle(1261, false);
        ui.toggle(1258, true); ui.toggle(1248, true);
        if (mobile) {
            ui.toggle(1299, true); ui.choice(1290, "1.0x"); ui.choice(1293, "20%");
            ui.choice(1295, 1287, 1286, 1288, 1289); ui.choice(1291, "1.0x");
            ui.toggle(1292, false); ui.toggle(1331, false);
        }
        ui.choice(1319, 1321, 1320, 1322); ui.choice(1318, 1322, 1320, 1321);

        Page game = page(1204);
        game.number(1325, "4x");
        game.toggle(1345, true); game.toggle(1330, true); game.toggle(6160, false); game.toggle(1347, true);
        game.toggle(1284, false);
        if (mobile) game.toggle(8603, false);
        game.toggle(1213, false);
        if (mobile) game.choice(1993, 1994, 1995, 1996);
        game.toggle(1234, false); game.toggle(1317, true);
        game.choice(6156, 6153, 6154, 6155); game.choice(1315, 1322, 1316, 1320);

        Page controls = page(1203);
        controls.toggle(1349, false); controls.button(1388);
        addKeys(controls);

        Page language = page(1206);
        language.choice(1332, strings.languageName(strings.language()).split(" \\(", 2)[0]);
        language.button(1339, 1390); language.button(1340, 1358); language.link(1324);

        Page chat = page(1208);
        chat.toggle(1326, true); chat.choice(1342, 72, 71, 73, 48);
        chat.toggle(1335, true); chat.toggle(1336, true); chat.toggle(1338, true); chat.toggle(1346, true);

        Page tools = page(1209);
        if (!mobile) { tools.button(1376, 1377); tools.button(1382, 1377); tools.button(1380, 1377); }
        tools.toggle(1219, false);
        if (mobile) tools.toggle(605, false);
        if (!mobile) page(1211).toggle(1387, false);
    }

    private void addKeys(Page page) {
        int[] labels = {1300,1301,1368,1369,1302,1303,1304,1305};
        String[] keys = {"Z","X","S","A","W","S","A","D"};
        for (int i = 0; i < labels.length; i++) page.key(labels[i], keys[i]);
        if (mobile) { page.key(1363, "Page Down"); page.key(1364, "Page Up"); }
        for (int i = 1; i <= 9; i++) page.keyRow(strings.get(1306) + " " + i + ":", "hotbar"+i, Integer.toString(i));
        int[] remaining = {1309,1308,1307,1,1310,1312,1313,8000,1314,1311};
        String[] remainingKeys = {"M","B","C","N","D","J","H","Space","F12","F11"};
        for (int i = 0; i < remaining.length; i++) page.key(remaining[i], remainingKeys[i]);
    }

    private Page page(int title) {
        Page content = new Page();
        SettingsScrollPane scroll = new SettingsScrollPane(content);
        scroll.setTheme("settings-scrollpane");
        scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);

        tabs.addTab(strings.get(title), scroll);
        return content;
    }

    private static DialogLayout value(Widget child) {
        var value = new DialogLayout();
        value.setTheme("label-settings-value");
        value.setHorizontalGroup(value.createParallelGroup(child));
        value.setVerticalGroup(value.createParallelGroup(child));
        return value;
    }

    private final class Page extends DialogLayout {
        private final Group columns = createParallelGroup();
        private final Group rows = createSequentialGroup();
        private int rowCount;
        private final java.util.ArrayList<Widget> rowControls = new java.util.ArrayList<>();
        private final java.util.ArrayList<Widget> spanningControls = new java.util.ArrayList<>();
        Page() {
            setTheme(mobile ? "dialoglayout" : "settings-label-area");
            setAddDefaultGaps(false);
            setHorizontalGroup(columns); setVerticalGroup(rows);
        }
        Label row(String title, Widget control) {
            prepareControl(control);
            rowControls.add(control);
            Label label = new Label(title) {
                @Override protected boolean handleEvent(Event event) {
                    return mobile ? false : super.handleEvent(event);
                }
            };
            label.setTheme("label-settings-title");
            columns.addGroup(createSequentialGroup().addWidget(label).addGap(DialogLayout.DEFAULT_GAP).addWidget(control));
            if (rowCount++ > 0) rows.addGap(DialogLayout.DEFAULT_GAP);
            rows.addGroup(createParallelGroup(label, control));
            return label;
        }
        void row(int title,Widget control) {
            Label label=row(strings.get(title),control);
            // GameSettingsWindow 的标签提示；ToggleSettingGroup.yO 使用 100ms。
            int description=switch(title) {
                case 1217->1218;case 1241->1242;case 1221->1222;
                case 1252->1267;case 1257->1268;case 1255->1269;case 1248->1249;
                case 1243->1244;case 1297->1298;case 1284->1285;case 1213->1214;
                case 1234->1235;case 1245->1246;case 1293->1294;case 1295->1296;
                default->0;
            };
            if(description!=0) {
                label.setTooltipContent(strings.get(description));
                if(title!=1217 && title!=1241 && title!=1221)label.setTooltipDelay(100);
            }
        }
        @Override protected void layout() {
            super.layout();
            // 独占一行的按钮与上方标签/选项两列对齐，不延伸到滚动区域的空白处。
            int right = getInnerX();
            for (Widget control : rowControls) right = Math.max(right, control.getRight());
            for (Widget control : spanningControls) {
                int width = Math.min(getInnerWidth(), right - getInnerX());
                if (control.getMaxWidth() > 0) width = Math.min(width, control.getMaxWidth());
                control.setSize(width, control.getHeight());
                // 受主题 maxWidth 限制时仍与选项列右端对齐。
                control.setPosition(right - width, control.getY());
            }
        }
        private void prepareControl(Widget widget) {
            if (!mobile) return;
            if (widget instanceof Button) widget.setCanAcceptKeyboardFocus(false);
            for (int i = 0; i < widget.getNumChildren(); i++) prepareControl(widget.getChild(i));
        }
        void toggle(int label, boolean active) {
            SettingsButton toggle = new SettingsButton("");
            toggle.setTheme("togglebutton");
            toggle.setModel(new de.matthiasmann.twl.model.ToggleButtonModel());
            toggle.getModel().setSelected(active);
            toggle.getAnimationState().dontAnimate(Button.STATE_SELECTED);
            bind(ClientSettings.key(label), () -> Boolean.toString(toggle.getModel().isSelected()), value -> {
                toggle.getModel().setSelected(value.equals("true") ? true : value.equals("false") ? false : active);
                toggle.getAnimationState().dontAnimate(Button.STATE_SELECTED);
            });
            row(label, value(toggle));
        }
        void key(int label, String key) {
            String name = switch (label) {
                case 1300 -> "key_a"; case 1301 -> "key_b"; case 1368 -> "key_x"; case 1369 -> "key_y";
                case 1302 -> "key_up"; case 1303 -> "key_down"; case 1304 -> "key_left"; case 1305 -> "key_right";
                case 1309 -> "friendlist"; case 1308 -> "bag"; case 1307 -> "trainer"; case 1 -> "codex";
                case 1310 -> "gamemenu"; case 1312 -> "faq"; case 1313 -> "team"; case 8000 -> "tradelink";
                case 1314 -> "screenshot"; case 1311 -> "hidegui"; case 1363 -> "key_forward"; case 1364 -> "key_backward";
                default -> throw new IllegalArgumentException();
            };
            keyRow(strings.get(label),name,key);
        }
        void keyRow(String title,String name,String key) {
            SettingsButton button = new SettingsButton(key);
            String property = "client.controls.gdx." + name;
            keyBindings.put(button,property);
            button.addCallback(() -> pendingKey = button);
            bind(property, () -> Integer.toString(com.badlogic.gdx.Input.Keys.valueOf(button.getText())), value -> {
                try { int code=Integer.parseInt(value); String text=com.badlogic.gdx.Input.Keys.toString(code);
                    button.setText(text == null || code < 0 || code > 255 ? key : text); }
                catch (NumberFormatException error) { button.setText(key); }
            });
            row(title,value(button));
        }
        void number(int label, String text) {
            NumberDisplay number = new NumberDisplay(label,text);
            bind(ClientSettings.key(label),number::stored,number::restore);
            row(label, number);
        }
        void choice(int label, int... options) {
            choice(label, java.util.Arrays.stream(options).mapToObj(strings::get).toArray(String[]::new));
        }
        void choice(int label, String... options) {
            if (label == 1260) options = new String[]{preferences.get(ClientSettings.key(label),mobile?"android":"default")};
            if (label == 1332) options = strings.languages().stream().map(code -> strings.languageName(code).split(" \\(",2)[0]).toArray(String[]::new);
            if (label == 1290 || label == 1291) options = new String[]{"0.7x","0.8x","0.9x","1.0x","1.1x","1.2x","1.3x","1.4x","1.5x"};
            if (label == 1293) options = new String[]{"10%","20%","30%","40%","50%","60%"};
            if (label == 1247) options = new String[]{"50%","75%","100%","125%","150%","175%","200%"};
            if (label == 1250) options = new String[]{preferences.get("client.graphics.width",Integer.toString(Gdx.graphics.getWidth())) + " x "
                    + preferences.get("client.graphics.height",Integer.toString(Gdx.graphics.getHeight()))};
            Choice choice = new Choice(options);
            if (label == 1250) {
                String[] dimensions=options[0].split(" x ");
                bind("client.graphics.width",()->dimensions[0], value -> {});
                bind("client.graphics.height",()->dimensions[1], value -> {});
            } else {
                String[] values = switch (label) {
                    case 1253 -> new String[]{"1","0"}; case 7701 -> new String[]{"2","0"};
                    case 1295 -> new String[]{"1","0","2","3"}; case 1319 -> new String[]{"1","0","2"};
                    case 1318 -> new String[]{"2","0","1"}; case 1315 -> new String[]{"2","1","0"};
                    case 1293,1247 -> java.util.Arrays.stream(options).map(value -> Double.toString(Double.parseDouble(value.replace("%",""))/100)).toArray(String[]::new);
                    case 1290,1291 -> java.util.Arrays.stream(options).map(value -> value.replace("x","")).toArray(String[]::new);
                    case 1332 -> strings.languages().toArray(String[]::new);
                    case 1365 -> new String[]{preferences.get(ClientSettings.key(label),"")};
                    case 1260,1217 -> options.clone();
                    default -> java.util.stream.IntStream.range(0,options.length).mapToObj(Integer::toString).toArray(String[]::new);
                };
                int defaultIndex = switch(label) {
                    case 1290,1291 -> 3; case 1293 -> 1; case 1247 -> 2;
                    case 1332 -> Math.max(0,java.util.Arrays.asList(values).indexOf(strings.language()));
                    default -> 0;
                };
                choice.select(defaultIndex);
                bind(ClientSettings.key(label), () -> values[choice.selected], value -> {
                    int index=java.util.Arrays.asList(values).indexOf(value);
                    if (index < 0 && (label==1290 || label==1291 || label==1293 || label==1247)) {
                        try { double numeric=Double.parseDouble(value);
                            for(int i=0;i<values.length;i++)if(Double.parseDouble(values[i])==numeric)index=i;
                        } catch(NumberFormatException ignored) {}
                    }
                    choice.select(index < 0 ? defaultIndex : index);
                });
            }
            row(label, value(choice));
        }
        void button(int label, int caption) { row(label, value(new SettingsButton(strings.get(caption)))); }
        void button(int caption) {
            Button button = new SettingsButton(strings.get(caption));
            if (caption == 1388) button.addCallback(() -> keyBindings.forEach((control,key) -> bindings.get(key).write().accept(bindings.get(key).initial())));
            prepareControl(button);
            columns.addWidget(button); rows.addGap(DialogLayout.DEFAULT_GAP).addWidget(button);
            setWidgetAlignment(button, Alignment.TOPLEFT); spanningControls.add(button);
        }
        void link(int caption) {
            Button link = new SettingsButton(strings.get(caption)); link.setTheme("translate-link");
            prepareControl(link);
            columns.addWidget(link); rows.addGap(DialogLayout.DEFAULT_GAP).addWidget(link);
            setWidgetAlignment(link, Alignment.TOPLEFT); spanningControls.add(link);
        }
    }

    private static class SettingsButton extends Button {
        SettingsButton(String text) { setTheme("button"); setText(text); }
    }

    /** 参考移动端的统一 pan/fling：逐次位移、GestureDetector 速度采样与半秒减速。 */
    private final class SettingsScrollPane extends ScrollPane {
        private boolean panning;
        private long flingTime;
        private double velocity, remaining, position;
        private final GestureDetector gesture = new GestureDetector(20, .4f, 1.1f, .15f,
                new GestureDetector.GestureAdapter() {
            @Override public boolean pan(float x, float y, float deltaX, float deltaY) {
                if (!panning) {
                    panning = true;
                    // 手势接管后取消控件按压，并释放 TWL 的鼠标捕获，避免抬手误点。
                    cancelPress(getContent());
                    getGUI().handleMouse(-1, -1, Event.MOUSE_LBUTTON, false);
                    cancelPress(getContent());
                }
                position = Math.max(0, Math.min(getVerticalScrollbar().getMaxValue(), position - deltaY));
                setScrollPositionY((int)Math.round(position));
                return true;
            }
            @Override public boolean fling(float velocityX, float velocityY, int button) {
                if (Math.abs(velocityY) > 150) {
                    velocity = -velocityY; remaining = .5;
                    position = getScrollPositionY(); flingTime = System.nanoTime();
                }
                return true;
            }
        });
        SettingsScrollPane(Widget content) { super(content); }
        void beginTouch(int x, int y, int button) {
            cancelFling(); panning = false; position = getScrollPositionY();
            gesture.reset(); gesture.touchDown(x, y, 0, button);
        }
        void cancelFling() { remaining = 0; velocity = 0; }
        @Override protected void beforeRemoveFromGUI(GUI gui) {
            gesture.reset(); panning = false; cancelFling(); super.beforeRemoveFromGUI(gui);
        }
        @Override protected void paint(GUI gui) {
            if (remaining > 0) {
                long now = System.nanoTime();
                double elapsed = Math.min(remaining, (now - flingTime) / 1_000_000_000.0);
                position += velocity * (remaining * elapsed - elapsed * elapsed / 2) / .5;
                remaining -= elapsed; flingTime = now;
                int max = getVerticalScrollbar().getMaxValue();
                setScrollPositionY((int)Math.round(Math.max(0, Math.min(max, position))));
                if (position <= 0 || position >= max) cancelFling();
            }
            super.paint(gui);
        }
    }

    private SettingsScrollPane touchScroll;
    private static void cancelPress(Widget widget) {
        if (widget instanceof Button button) {
            var model = button.getModel();
            model.setArmed(false); model.setPressed(false); model.setHover(false);
        }
        for (int i = 0; i < widget.getNumChildren(); i++) cancelPress(widget.getChild(i));
    }
    public void touchDown(int x, int y, int screenX, int screenY, int button) {
        clearTouchFocus(); touchScroll = null;
        if (!mobile || !isOpen() || hasOpenChoice(this)) return;
        var scroll = (SettingsScrollPane)tabs.selectedPage();
        // 滚动条维持 TWL 自己的拖拽；手势只接管内容区域。
        if (x >= scroll.getInnerX() && x < scroll.getInnerX() + scroll.getContentAreaWidth()
                && y >= scroll.getInnerY() && y < scroll.getInnerY() + scroll.getContentAreaHeight()) {
            // 参考 pan/fling 使用屏幕位移，不能再乘 UI 缩放；x/y 仅用于安全区命中。
            touchScroll = scroll; scroll.beginTouch(screenX, screenY, button);
        }
    }
    public boolean touchDragged(int x, int y) {
        if (touchScroll == null) return false;
        touchScroll.gesture.touchDragged(x, y, 0);
        return touchScroll.panning;
    }
    public boolean touchUp(int x, int y, int button) {
        if (touchScroll == null) return false;
        var scroll = touchScroll; touchScroll = null;
        boolean handled = scroll.panning;
        scroll.gesture.touchUp(x, y, 0, button);
        scroll.panning = false;
        return handled;
    }
    public void cancelTouch() {
        if (touchScroll != null) {
            touchScroll.gesture.reset(); touchScroll.cancelFling(); touchScroll = null;
        }
        cancelPress(this);
    }
    private static boolean hasOpenChoice(Widget widget) {
        if (widget instanceof Choice choice && choice.popup.isOpen()) return true;
        for (int i = 0; i < widget.getNumChildren(); i++) if (hasOpenChoice(widget.getChild(i))) return true;
        return false;
    }
    public void clearTouchFocus() {
        if (!mobile || !isOpen()) return;
        requestKeyboardFocus(null);
        ((SettingsScrollPane)tabs.selectedPage()).cancelFling();
    }

    private final class NumberDisplay extends Widget {
        private final Button minus = new SettingsButton("-"), plus = new SettingsButton("+"), display;
        private final int label, initial;
        private final int[] values;
        private int selected;
        NumberDisplay(int label,String text) {
            this.label = label;
            int min=0,max=100,step=1;
            switch (label) {
                case 1254 -> { max=1000; step=10; }
                case 1237,1238 -> { min=1;max=4; }
                case 1325 -> { min=1;max=6; }
                default -> {}
            }
            values = label == 1264 ? new int[]{0,2,4,8,16} : label == 1221 ? (mobile?new int[]{-1,3,4,5,6,7,8}:new int[]{-1,4,5,6,7,8})
                    : java.util.stream.IntStream.iterate(min,value -> value + stepValue(label)).limit((max-min)/step+1).toArray();
            int parsed;
            try { parsed = Integer.parseInt(text.replaceAll("[^0-9]","")); }
            catch (NumberFormatException error) { parsed = label == 1221 ? -1 : 0; }
            initial = parsed;
            setTheme("label-settings-vai");
            display = new SettingsButton(text);
            minus.setTheme("decButton"); plus.setTheme("incButton"); display.setTheme("valueDisplay");
            add(minus); add(display); add(plus);
            minus.addCallback(() -> change(-1)); plus.addCallback(() -> change(1));
            restore(label == 1283 ? Double.toString(initial/100.0) : Integer.toString(initial));
        }
        private int stepValue(int label) { return label == 1254 ? 10 : 1; }
        private void change(int delta) { selected=Math.max(0,Math.min(values.length-1,selected+delta)); updateText(); }
        String stored() { return label == 1283 ? Double.toString(values[selected]/100.0) : Integer.toString(values[selected]); }
        void restore(String text) {
            int number;
            try { number = label == 1283 ? (int)Math.round(Double.parseDouble(text)*100) : Integer.parseInt(text); }
            catch (NumberFormatException error) { number = initial; }
            selected = -1;
            for (int i=0;i<values.length;i++) if(values[i]==number)selected=i;
            if (selected < 0) {
                selected = 0;
                for (int i=0;i<values.length;i++) if(values[i]==initial)selected=i;
            }
            updateText();
        }
        private void updateText() {
            int number=values[selected];
            String text = switch (label) {
                case 1254 -> number==0 ? strings.get(1220) : Integer.toString(number);
                case 1221 -> number<0 ? strings.get(1220) : number+"x";
                case 1264 -> number==0 ? strings.get(1265) : number+"x";
                case 1241 -> number==0 ? strings.get(1240) : Integer.toString(number);
                case 1237,1238,1325 -> number+"x";
                case 1274,1283 -> number+"%";
                default -> Integer.toString(number);
            };
            display.setText(text); invalidateLayout();
        }
        @Override public int getPreferredInnerWidth() { return minus.getPreferredWidth() + display.getPreferredWidth() + plus.getPreferredWidth(); }
        @Override public int getPreferredInnerHeight() { return Math.max(display.getPreferredHeight(), Math.max(minus.getPreferredHeight(), plus.getPreferredHeight())); }
        @Override protected void layout() {
            int left = minus.getPreferredWidth(), right = plus.getPreferredWidth();
            minus.setPosition(getInnerX(), getInnerY()); minus.setSize(left, getInnerHeight());
            plus.setPosition(getInnerRight() - right, getInnerY()); plus.setSize(right, getInnerHeight());
            display.setPosition(getInnerX() + left, getInnerY()); display.setSize(Math.max(0, getInnerWidth() - left - right), getInnerHeight());
        }
    }

    /** 显示文本与保存值分别映射，配置不依赖当前语言。 */
    private static final class Choice extends SettingsButton {
        private final Label display = new Label(getAnimationState()) {
            @Override protected boolean handleEvent(Event event) { return false; }
        };
        private final Widget arrow = new Widget(getAnimationState());
        private final PopupWindow popup = new PopupWindow(this);
        private final List<String> options;
        private ListBox<String> list;
        private int popupHeight;
        private int selected;
        Choice(String[] options) {
            super("");
            this.options = List.of(options);
            setTheme("combobox"); display.setTheme("display"); arrow.setTheme("button");
            display.setClip(true); add(display); add(arrow); display.setText(options[0]);
            popup.setTheme("/comboboxPopup"); addCallback(this::openChoices);
        }
        void select(int index) { selected=index; display.setText(options.get(index)); }
        @Override protected void applyTheme(ThemeInfo theme) {
            super.applyTheme(theme); popupHeight = theme.getParameter("popupHeight", 150);
        }
        @Override protected void paintWidget(GUI gui) { }
        @Override public int getPreferredInnerWidth() { return display.getPreferredWidth() + arrow.getPreferredWidth(); }
        @Override public int getPreferredInnerHeight() { return Math.max(display.getPreferredHeight(), arrow.getPreferredHeight()); }
        @Override protected void layout() {
            int width = arrow.getPreferredWidth();
            arrow.setPosition(getInnerRight() - width, getInnerY()); arrow.setSize(width, getInnerHeight());
            display.setPosition(getInnerX(), getInnerY()); display.setSize(Math.max(0, getInnerWidth() - width), getInnerHeight());
        }
        private void openChoices() {
            if (popup.isOpen()) { popup.closePopup(); return; }
            list = new ListBox<>(new SimpleListModel<String>() {
                public int getNumEntries() { return options.size(); }
                public String getEntry(int index) { return options.get(index); }
            });
            list.setTheme("listbox"); list.setSelected(options.indexOf(display.getText()));
            list.addCallback(reason -> {
                if (reason.actionRequested() && list.getSelected() >= 0) {
                    select(list.getSelected()); popup.closePopup();
                }
            });
            popup.removeAllChildren(); popup.add(list);
            if (popup.openPopup()) {
                int height = Math.min(popupHeight, options.size() * list.getCellHeight() + list.getBorderVertical());
                popup.setSize(getWidth(), Math.min(height, getGUI().getHeight()));
                popup.setPosition(Math.max(0, Math.min(getX(), getGUI().getWidth() - popup.getWidth())),
                        getBottom() + height <= getGUI().getHeight() ? getBottom() : Math.max(0, getY() - height));
                list.requestKeyboardFocus();
            }
        }
    }

    public void back() {
        if (hasOpenChoice(this)) getGUI().handleKey(Event.KEY_ESCAPE, '\0', true);
        else closePopup();
    }

    public void open() {
        if (isOpen()) { raiseWindow(); return; }
        restoreSaved();
        tabs.select(0);
        tabs.selectedPage().setScrollPositionY(0);
        if (openPopup()) { positionWindow(); frame.setHardVisible(true); }
    }
    public void positionWindow() {
        if (!isOpen()) return;
        Widget owner = getOwner();
        if (mobile) { setPosition(owner.getInnerX(), owner.getInnerY()); setSize(owner.getInnerWidth(), owner.getInnerHeight()); }
        else {
            adjustSize();
            setPosition(owner.getInnerX() + (owner.getInnerWidth() - getWidth()) / 2,
                    owner.getInnerY() + (owner.getInnerHeight() - getHeight()) / 2);
        }
    }
    @Override public void closePopup() {
        cancelTouch();
        closeChoices(this);
        restoreSaved();
        boolean wasOpen = isOpen(); super.closePopup(); if (wasOpen) onClose.run();
    }
    private static void closeChoices(Widget widget) {
        if (widget instanceof Choice choice) choice.popup.closePopup();
        for (int i=0;i<widget.getNumChildren();i++) closeChoices(widget.getChild(i));
    }
}
