package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;
import org.lwjgl.glfw.GLFW;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.ClientSettings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.function.IntFunction;
import java.util.function.IntConsumer;
import java.util.function.Consumer;

/** 音乐与帧率显示设置即时生效，点击保存后写入配置；其余选项仅保存在控件中。 */
final class GameSettingsWindow extends ResizableFrame {
    private static final int[] CATEGORY_IDS = {1201, 1202, 1210, 1204, 1203, 1206, 1208, 1209, 1211};
    private final List<ToggleButton> categories = new ArrayList<>();
    private final List<ScrollPane> pages = new ArrayList<>();
    private final Button cancel = new Button(ClientStrings.get(53));
    private final Button save = new Button(ClientStrings.get(54));
    private ComboBox<String> interfaceScale;
    private int scaleWidth;
    private int scaleHeight;
    private IntConsumer musicVolumeChanged = value -> { };
    private Consumer<Boolean> focusMuteChanged = value -> { };
    private final ClientSettings clientSettings = new ClientSettings();
    private Consumer<String> saveFailed = message -> { };
    private Consumer<Boolean> fpsVisibilityChanged = visible -> { };

    int fpsLimit() {
        return clientSettings.fpsLimit();
    }

    void setFpsVisibilityChanged(Consumer<Boolean> changed) {
        fpsVisibilityChanged = changed;
        changed.accept(clientSettings.showFps());
    }

    void setAudioCallbacks(IntConsumer musicVolumeChanged, Consumer<Boolean> focusMuteChanged) {
        this.musicVolumeChanged = musicVolumeChanged;
        this.focusMuteChanged = focusMuteChanged;
        musicVolumeChanged.accept(clientSettings.volume());
        focusMuteChanged.accept(clientSettings.muteWhenUnfocused());
    }

    void setSaveFailed(Consumer<String> saveFailed) {
        this.saveFailed = saveFailed;
    }

    GameSettingsWindow(Runnable onClose) {
        setTheme("/settings-panel");
        setTitle(ClientStrings.get(1200));
        setResizableAxis(ResizableAxis.NONE);
        setDraggable(false);
        addCloseCallback(onClose);
        for (int i = 0; i < CATEGORY_IDS.length; i++) {
            int index = i;
            ToggleButton category = new ToggleButton(ClientStrings.get(CATEGORY_IDS[i]));
            category.setTheme("category");
            category.addCallback(() -> selectPage(index));
            categories.add(category);
            add(category);
        }
        buildVideo();
        buildAudio();
        buildInterface();
        buildGame();
        buildControls();
        buildLanguage();
        buildChat();
        buildTools();
        page().toggle(1387, true, 0);
        cancel.addCallback(onClose);
        save.addCallback(() -> {
            try {
                clientSettings.save();
                onClose.run();
            } catch (java.io.IOException exception) {
                saveFailed.accept(ClientStrings.get(2000));
            }
        });
        add(cancel);
        add(save);
        selectPage(0);
    }

    private SettingsPage page() {
        SettingsPage page = new SettingsPage();
        ScrollPane scroll = new ScrollPane(page);
        scroll.setTheme("settings-scrollpane");
        scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        pages.add(scroll);
        add(scroll);
        return page;
    }

    private void buildVideo() {
        SettingsPage page = page();
        page.combo(1270, 0, 1271, 1272, 1273);
        String[] resolutions = resolutionOptions();
        int selectedResolution = List.of(resolutions).indexOf("1024 x 768");
        page.row(1250, combo(selectedResolution, resolutions));
        page.toggle("V-Sync", true);
        page.row(1217, combo(0, "OpenGL", "ANGLE"), 1218);
        page.number(1254, 10, 1000, clientSettings.fpsLimit(), 1, Integer::toString, clientSettings::setFpsLimit);
        page.number(1221, 3, 8, 3, 1, value -> value == 3 ? ClientStrings.get(1220) : Integer.toString(value));
        page.tooltip(1222);
        page.toggle(1297, false, 1298);
        int samples = org.lwjgl.opengl.GL.getCapabilities().OpenGL30
                ? org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL30.GL_MAX_SAMPLES) : 0;
        String vendor = org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_VENDOR);
        if (vendor != null && vendor.toLowerCase(java.util.Locale.ROOT).contains("intel")) samples = Math.min(samples, 8);
        page.number(1264, 0, (int) Math.round(Math.sqrt(Math.min(samples, 16))), 0, 1,
                value -> value == 0 ? ClientStrings.get(1265) : (1 << value) + "x");
        page.number(1241, 0, 100, 0, 1,
                value -> value == 0 || value == 100 ? ClientStrings.get(1240) : Integer.toString(value));
        page.tooltip(1242);
        page.toggle(1257, true, 1268);
        page.toggle(1255, true, 1269);
        page.combo(1253, 1, 1287, 1289);
        page.toggle(1252, true, 1267);
        page.number(1237, 1, 4, 1, 1, value -> value + "x");
        page.number(1238, 1, 4, 1, 1, value -> value + "x");
        page.toggle(1233, true, 0);
    }

    private void buildAudio() {
        SettingsPage page = page();
        page.number(1276, 0, 100, clientSettings.volume(), 1, Integer::toString, value -> {
            clientSettings.setVolume(value);
            musicVolumeChanged.accept(value);
        });
        page.number(1277, 0, 100, 100, 1);
        page.number(1279, 0, 100, 100, 1);
        page.combo(1365, 0, 1366);
        ToggleButton mute = new ToggleButton();
        mute.setActive(clientSettings.muteWhenUnfocused());
        mute.addCallback(() -> {
            clientSettings.setMuteWhenUnfocused(mute.isActive());
            focusMuteChanged.accept(mute.isActive());
        });
        page.row(1282, mute);
        page.number(1283, 0, 100, 100, 1, value -> value + "%");
        page.toggle(1343, true, 0);
    }

    private void buildInterface() {
        SettingsPage page = page();
        page.row(1260, combo(0, "Default"));
        interfaceScale = combo(0, "1.0x");
        page.row(1247, interfaceScale);
        page.number(1274, 0, 100, 100, 1);
        page.toggle(1266, true, 0);
        page.toggle(1245, true, 1246);
        page.toggle(1236, false, 0);
        page.toggle(1261, false, 0);
        ToggleButton showFps = new ToggleButton();
        showFps.setActive(clientSettings.showFps());
        showFps.addCallback(() -> {
            clientSettings.setShowFps(showFps.isActive());
            fpsVisibilityChanged.accept(showFps.isActive());
        });
        page.row(1258, showFps);
        page.toggle(1248, true, 1249);
        page.combo(1319, 1, 1320, 1321, 1322);
        page.combo(1318, 1, 1320, 1321, 1322);
    }

    private void buildGame() {
        SettingsPage page = page();
        page.number(1325, 1, 6, 3, 1, value -> value == 6 ? ClientStrings.get(1394) : value + "x");
        page.toggle(1345, false, 0);
        page.toggle(1330, true, 0);
        page.toggle(6160, true, 0);
        page.toggle(1347, true, 0);
        page.toggle(1284, false, 1285);
        page.toggle(1213, true, 1214);
        page.toggle(1234, false, 1235);
        page.toggle(1317, false, 0);
        page.combo(6156, 1, 6153, 6154, 6155);
        page.combo(1315, 1, 50, 1316, 51);
    }

    private void buildControls() {
        SettingsPage page = page();
        page.toggle(1349, false, 0);
        page.full(new Button(ClientStrings.get(1388)));
        int[] ids = {1300, 1301, 1368, 1369, 1302, 1303, 1304, 1305};
        String[] keys = {"Z", "X", "S", "A", "Up", "Down", "Left", "Right"};
        for (int i = 0; i < ids.length; i++) {
            page.row(ids[i], new Button(keys[i]));
        }
        for (int i = 1; i <= 9; i++) {
            page.row(ClientStrings.get(1306) + " " + i + ":", new Button(Integer.toString(i)));
        }
        int[] otherIds = {1309, 1308, 1307, 1, 1310, 1312, 1313, 8000, 1314, 1311};
        String[] otherKeys = {"N", "B", "C", "V", "D", "J", "H", "L", "F12", "F11"};
        for (int i = 0; i < otherIds.length; i++) {
            page.row(otherIds[i], new Button(otherKeys[i]));
        }
    }

    private void buildLanguage() {
        SettingsPage page = page();
        String[] names = ClientStrings.languageNames();
        for (int i = 0; i < names.length; i++) names[i] = names[i].split(" \\(", 2)[0];
        page.row(1332, combo(ClientStrings.selectedIndex(), names));
        page.row(1339, new Button(ClientStrings.get(1390)));
        page.row(1340, new Button(ClientStrings.get(1358)), 1357);
        Button translate = new Button(ClientStrings.get(1324));
        translate.setTheme("translate-link");
        page.full(translate);
    }

    private void buildChat() {
        SettingsPage page = page();
        page.toggle(1326, true, 0);
        page.combo(1342, 2, 71, 72, 73, 48);
        page.toggle(1335, true, 0);
        page.toggle(1336, true, 0);
        page.toggle(1338, true, 0);
        page.toggle(1346, true, 0);
    }

    private void buildTools() {
        SettingsPage page = page();
        for (int id : new int[]{1376, 1382, 1380}) page.row(id, new Button(ClientStrings.get(1377)));
        page.toggle(1219, false, 0);
    }

    private static ComboBox<String> combo(int selected, String... options) {
        ComboBox<String> combo = new ComboBox<>(new SimpleChangableListModel<>(options)) {
            @Override
            protected void setPopupSize() {
                ListBox<?> list = (ListBox<?>) popup.getChild(0);
                int height = Math.min(8, list.getNumEntries()) * list.getCellHeight() + list.getBorderVertical();
                popup.setMinSize(popup.getMinWidth(), height);
                popup.setMaxSize(popup.getMaxWidth(), height);
                super.setPopupSize();
            }
        };
        combo.setSelected(selected);
        return combo;
    }

    /** 合并显示器模式和当前窗口尺寸，按高度、宽度从大到小排列。 */
    private static String[] resolutionOptions() {
        TreeSet<int[]> sizes = new TreeSet<>(Comparator.<int[]>comparingInt(size -> size[1])
                .thenComparingInt(size -> size[0]).reversed());
        sizes.add(new int[]{1024, 768});
        var modes = GLFW.glfwGetVideoModes(GLFW.glfwGetPrimaryMonitor());
        if (modes != null) {
            for (int i = 0; i < modes.limit(); i++) {
                var mode = modes.get(i);
                if (mode.height() >= 768 || mode.width() >= 1024) {
                    sizes.add(new int[]{mode.width(), mode.height()});
                }
            }
        }
        return sizes.stream().map(size -> size[0] + " x " + size[1]).toArray(String[]::new);
    }

    private void updateInterfaceScales() {
        GUI gui = getGUI();
        if (gui == null || gui.getWidth() == scaleWidth && gui.getHeight() == scaleHeight) {
            return;
        }
        scaleWidth = gui.getWidth();
        scaleHeight = gui.getHeight();
        List<String> options = new ArrayList<>();
        for (float scale : new float[]{1.0f, 1.25f, 1.5f, 1.75f, 2.0f}) {
            if (scale == 1.0f || (int) (scaleWidth / scale) > 500 && (int) (scaleHeight / scale) > 640) {
                options.add(scale + "x");
            }
        }
        interfaceScale.setModel(new SimpleChangableListModel<>(options));
        interfaceScale.setSelected(0);
    }

    /** 每次打开时从影像页顶部开始浏览。 */
    void resetView() {
        selectPage(0);
        for (ScrollPane page : pages) {
            page.setScrollPositionX(0);
            page.setScrollPositionY(0);
        }
    }

    private void selectPage(int index) {
        for (int i = 0; i < pages.size(); i++) {
            categories.get(i).setActive(i == index);
            pages.get(i).setVisible(i == index);
        }
    }

    @Override
    protected void layout() {
        super.layout();
        updateInterfaceScales();
        for (int i = 0; i < categories.size(); i++) {
            categories.get(i).setPosition(getX() + 25, getY() + 50 + i * 36);
            categories.get(i).setSize(50, 31);
            pages.get(i).setPosition(getX() + 80, getY() + 45);
            pages.get(i).setSize(getWidth() - 90, getHeight() - 78);
        }
        save.setSize(43, 26);
        save.setPosition(getRight() - 50, getBottom() - 31);
        cancel.setSize(43, 26);
        cancel.setPosition(save.getX() - 51, save.getY());
    }

    private static final class SettingsPage extends Widget {
        private final List<Widget[]> rows = new ArrayList<>();

        private SettingsPage() {
            setTheme("settings-label-area");
        }

        void tooltip(int id) {
            Widget title = rows.get(rows.size() - 1)[0];
            title.setTooltipContent(ClientStrings.get(id));
            title.setTooltipDelay(100);
        }

        void row(int id, Widget value, int tooltipId) {
            Label title = new Label(ClientStrings.get(id));
            title.setTheme("label-settings-title");
            title.setLabelFor(value);
            if (tooltipId != 0) {
                title.setTooltipContent(ClientStrings.get(tooltipId));
                title.setTooltipDelay(100);
            }
            rows.add(new Widget[]{title, value});
            add(title);
            add(value);
        }

        void row(int id, Widget value) {
            row(id, value, 0);
        }

        void row(String text, Widget value) {
            Label title = new Label(text);
            title.setTheme("label-settings-title");
            title.setLabelFor(value);
            rows.add(new Widget[]{title, value});
            add(title);
            add(value);
        }

        void full(Widget value) {
            rows.add(new Widget[]{value});
            add(value);
        }

        void combo(int id, int selected, int... optionIds) {
            String[] options = new String[optionIds.length];
            for (int i = 0; i < optionIds.length; i++) options[i] = ClientStrings.get(optionIds[i]);
            choices(id, options, selected, 0);
        }

        void choices(int id, String[] options, int selected, int tooltipId) {
            row(id, GameSettingsWindow.combo(selected, options), tooltipId);
        }

        void toggle(int id, boolean active, int tooltipId) {
            ToggleButton toggle = new ToggleButton();
            toggle.setActive(active);
            row(id, toggle, tooltipId);
        }

        void toggle(String text, boolean active) {
            ToggleButton toggle = new ToggleButton();
            toggle.setActive(active);
            row(text, toggle);
        }

        void number(int id, int min, int max, int value, int step) {
            number(id, min, max, value, step, Integer::toString);
        }

        void number(int id, int min, int max, int value, int step, IntFunction<String> format) {
            number(id, min, max, value, step, format, current -> { });
        }

        void number(int id, int min, int max, int value, int step, IntFunction<String> format,
                    IntConsumer changed) {
            SimpleIntegerModel model = new SimpleIntegerModel(min, max, value);
            ValueAdjusterInt number = new ValueAdjusterInt(model) {
                @Override
                protected String formatText() {
                    return format == null ? Integer.toString(getValue()) : format.apply(getValue());
                }

                @Override
                protected boolean onEditEnd(String text) {
                    try {
                        setValue(parse(text));
                        return true;
                    } catch (NumberFormatException exception) {
                        return false;
                    }
                }

                @Override
                protected String validateEdit(String text) {
                    try {
                        parse(text);
                        return null;
                    } catch (NumberFormatException exception) {
                        return exception.toString();
                    }
                }

                private int parse(String text) {
                    // 保留已显示的特殊值，并接受带单位的数值输入。
                    if (text.equals(format.apply(getValue()))) return getValue();
                    if (id == 1221 && text.equals(ClientStrings.get(1220))) return min;
                    if (id == 1241 && text.equals(ClientStrings.get(1240))) return min;
                    if (id == 1264 && text.equals(ClientStrings.get(1265))) return min;
                    if (id == 1325 && text.equals(ClientStrings.get(1394))) return max;
                    int parsed = Integer.parseInt(text.replace("x", "").replace("%", "").trim());
                    if (id == 1264 || id == 1237 || id == 1238) {
                        if (parsed < 0) throw new NumberFormatException("Negative scale");
                        parsed = (int) Math.round(Math.sqrt(parsed));
                    }
                    return parsed;
                }

                @Override
                protected void doDecrement() {
                    setValue(getValue() - step);
                }

                @Override
                protected void doIncrement() {
                    setValue(getValue() + step);
                }
            };
            number.setTheme("value-number");
            // 点击数值编辑，回车或失焦提交，Esc 取消；模型负责限制数值范围。
            ((Button) number.getChild(2)).setText("-");
            ((Button) number.getChild(3)).setText("+");
            model.addCallback(() -> changed.accept(model.getValue()));
            row(id, number);
        }

        @Override
        public int getPreferredInnerWidth() {
            return 480;
        }

        @Override
        public int getPreferredInnerHeight() {
            int height = 10;
            for (Widget[] row : rows) {
                height += rowHeight(row) + 9;
            }
            return Math.max(10, height - 9);
        }

        private int rowHeight(Widget[] row) {
            return row.length > 1 && isNumber(row[1]) ? 31 : 28;
        }

        private boolean isNumber(Widget widget) {
            return widget instanceof ValueAdjusterInt;
        }

        @Override
        protected void layout() {
            int y = getY() + 5;
            for (int i = 0; i < rows.size(); i++) {
                Widget[] row = rows.get(i);
                int height = rowHeight(row);
                row[0].setPosition(getX() + 2, y);
                row[0].setSize(row.length == 1 ? 475 : 220, height);
                if (row.length > 1) {
                    boolean number = isNumber(row[1]);
                    row[1].setPosition(getX() + 232, number ? y : y + 1);
                    row[1].setSize(250, number ? height : 26);
                }
                y += height + 9;
            }
        }
    }
}
