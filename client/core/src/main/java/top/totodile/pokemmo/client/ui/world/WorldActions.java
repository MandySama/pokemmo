package top.totodile.pokemmo.client.ui.world;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.world.WorldScene;

/** 用户指定的本地地图快捷按钮，使用现有主题按钮。 */
public final class WorldActions extends Widget {
    private final WorldScene world;
    private final boolean mobile;
    private final Button noClip = new Button();
    private final Button speed = new Button();
    private final Button pokedex = new Button();
    private final Button portablePc = new Button("随身PC");
    private final Button randomEncounter = new Button("随机遭遇");
    private final Button npcEncounter = new Button("随机NPC对战");
    private final Button randomCounter = new Button("随机计数");
    private final Button tripleEncounter = new Button("三群遭遇：纯/杂"),
            quintupleEncounter = new Button("五群遭遇：纯/杂");
    private final Button teleport = new Button("传送"),
            item = new Button("添加道具"),
            randomItems = new Button("随机道具"),
            randomPokedex = new Button("随机图鉴"),
            randomOutfit = new Button("随机时装"),
            randomPokemon = new Button("随机宠物"),
            pokemon = new Button("添加宝可梦");
    private final Button time = new Button(), weather = new Button();
    private int buttonWidth;
    private final Button testMode = new Button();
    private final Button animationTests = new Button("招式测试");
    private final Button storyInspection = new Button("地图剧情");
    public void onStoryInspection(Runnable action){storyInspection.addCallback(action);}

    private final Button resources = new Button("资源工具"), encounters = new Button("遭遇测试");
    private MenuManager toolsMenu;
    private final java.util.Map<MenuAction, Button> menuActions = new java.util.LinkedHashMap<>();
    private java.util.function.BiConsumer<Integer, Boolean> hordeEncounter = (count, pure) -> {};

    public void onAnimationTests(Runnable action) {
        animationTests.addCallback(action);
    }

    private boolean testing;
    private java.util.function.Consumer<Boolean> testModeChanged = enabled -> {};

    public boolean testMode() {
        return testing;
    }

    public void onTestMode(java.util.function.Consumer<Boolean> action) {
        testModeChanged = action;
    }

    public WorldActions(WorldScene world) {
        this(world, false);
    }

    public WorldActions(WorldScene world, boolean mobile) {
        this.world = world;
        this.mobile = mobile;
        setTheme("");
        noClip.setTheme("/button");
        speed.setTheme("/button");
        pokedex.setTheme("/button");
        noClip.setCanAcceptKeyboardFocus(false);
        speed.setCanAcceptKeyboardFocus(false);
        noClip.addCallback(
                () -> {
                    world.movement().setNoClip(!world.movement().noClip());
                    refresh();
                });
        speed.addCallback(
                () -> {
                    float current = world.motion().speed();
                    world.motion()
                            .setSpeed(
                                    current >= 16
                                            ? 1
                                            : current < 2
                                                    ? 2
                                                    : current < 4 ? 4 : current < 8 ? 8 : 16);
                    refresh();
                });
        pokedex.setCanAcceptKeyboardFocus(false);
        pokedex.addCallback(
                () -> {
                    world.pokedex().setUnlocked(!world.pokedex().pokedexUnlocked());
                    refresh();
                });
        randomPokedex.addCallback(
                () -> {
                    world.pokedex().randomizePokedex(new java.util.Random());
                    refresh();
                });
        for (var button :
                new Button[] {
                    teleport,
                    randomItems,
                    randomPokedex,
                    randomOutfit,
                    randomPokemon,
                    item,
                    pokemon,
                    portablePc
                }) {
            button.setTheme("/button");
            button.setCanAcceptKeyboardFocus(false);

        }
        if (!mobile) {
            if (world.supportsAtmosphere())
                for (var button : new Button[] {time, weather}) {
                    button.setTheme("/button");
                    button.setCanAcceptKeyboardFocus(false);

                }
            time.addCallback(
                    () -> {
                        world.atmosphere().nextTime();
                        refresh();
                    });
            weather.addCallback(
                    () -> {
                        world.atmosphere().nextWeather();
                        refresh();
                    });
            randomEncounter.setTheme("/button");
            randomEncounter.setCanAcceptKeyboardFocus(false);

            for (var button : new Button[] {npcEncounter, tripleEncounter, quintupleEncounter, randomCounter}) {
                button.setTheme("/button");
                button.setCanAcceptKeyboardFocus(false);

            }
        }
        if (!mobile) {
            animationTests.setTheme("/button");
            animationTests.setCanAcceptKeyboardFocus(false);

        }
        testMode.setTheme("/button");
        testMode.setCanAcceptKeyboardFocus(false);
        testMode.addCallback(
                () -> {
                    testing = !testing;
                    refresh();
                    testModeChanged.accept(testing);
                });
        resources.addCallback(() -> openTools(resources, resourceMenu()));
        encounters.addCallback(() -> openTools(encounters, encounterMenu()));
        for (var button : buttons()) {
            button.setTheme("/button");
            button.setCanAcceptKeyboardFocus(false);
            add(button);
        }
        refresh();
    }

    /** Keep only the requested environment controls reachable through the battle input layer. */
    public boolean environmentButtonAt(int x, int y) {
        return isVisible()
                && (animationTests.isInside(x, y)
                        || testMode.isInside(x, y)
                        || world.supportsAtmosphere()
                                && (time.isInside(x, y) || weather.isInside(x, y)));
    }

    public void onPortablePc(Runnable action) {
        portablePc.addCallback(action);
    }

    public void onRandomCounter(Runnable action) {
        randomCounter.addCallback(action);
    }

    public void onRandomEncounter(Runnable action) {
        randomEncounter.addCallback(action);
    }

    public void onNpcEncounter(Runnable action) { npcEncounter.addCallback(action); }

    public void randomEncounterEnabled(boolean enabled) {
        randomEncounter.setEnabled(enabled);
        npcEncounter.setEnabled(enabled);
        tripleEncounter.setEnabled(enabled);
        quintupleEncounter.setEnabled(enabled);
    }

    public void onHordeEncounter(java.util.function.BiConsumer<Integer, Boolean> action) {
        hordeEncounter = action;
    }

    private MenuAction menuAction(Button button) {
        var item = new MenuAction(button.getText(), () -> {
            if (button.isEnabled()) button.getModel().fireActionCallback();
        });
        item.setEnabled(button.isEnabled());
        menuActions.put(item, button);
        return item;
    }

    private Menu resourceMenu() {
        menuActions.clear();
        var menu = new Menu();
        menu.add(new Menu("道具").add(menuAction(item)).add(menuAction(randomItems)));
        menu.add(new Menu("宝可梦").add(menuAction(pokemon)).add(menuAction(randomPokemon)));
        menu.add(new Menu("图鉴").add(menuAction(pokedex)).add(menuAction(randomPokedex)));
        menu.add(menuAction(randomOutfit));
        return menu;
    }

    private Menu encounterMenu() {
        menuActions.clear();
        var menu = new Menu();
        menu.add(menuAction(randomEncounter));
        menu.add(menuAction(npcEncounter));
        for (int count : new int[] {3, 5}) {
            var group = new Menu(count == 3 ? "三群遭遇" : "五群遭遇");
            var source = count == 3 ? tripleEncounter : quintupleEncounter;
            group.setEnabled(source.isEnabled());
            for (boolean pure : new boolean[] {true, false}) {
                var item = new MenuAction(pure ? "纯群" : "杂群", () -> {
                    if (source.isEnabled()) hordeEncounter.accept(count, pure);
                });
                item.setEnabled(source.isEnabled());
                menuActions.put(item, source);
                group.add(item);
            }
            menu.add(group);
        }
        menu.add(menuAction(randomCounter));
        return menu;
    }

    private void openTools(Button anchor, Menu menu) {
        if (toolsMenu != null) toolsMenu.closePopup();
        toolsMenu = menu.openPopupMenu(anchor, anchor.getRight(), anchor.getY());
    }

    public void onTeleport(Runnable action) {
        teleport.addCallback(action);
    }

    public void onItem(Runnable action) {
        item.addCallback(action);
    }

    public void onRandomItems(Runnable action) {
        randomItems.addCallback(action);
    }

    public void onRandomPokemon(Runnable action) {
        randomPokemon.addCallback(action);
    }

    public void randomPokemonEnabled(boolean enabled) {
        randomPokemon.setEnabled(enabled);
    }

    public void onRandomOutfit(Runnable action) {
        randomOutfit.addCallback(action);
    }

    public void randomOutfitEnabled(boolean enabled) {
        randomOutfit.setEnabled(enabled);
    }

    public void onPokemon(Runnable action) {
        pokemon.addCallback(action);
    }

    private Button[] buttons() {
        if (mobile) return new Button[] {testMode, noClip, speed, resources, teleport, portablePc, storyInspection};
        if (!world.supportsAtmosphere())
            return new Button[] {animationTests, testMode, noClip, speed, resources, encounters, teleport, portablePc, storyInspection};
        return new Button[] {animationTests, testMode, noClip, speed, resources, encounters,
                teleport, portablePc, time, weather, storyInspection};
    }

    @Override
    protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        buttonWidth = 0;
    }

    private int fixedButtonWidth() {
        if (buttonWidth == 0) {
            for (var button : buttons())
                buttonWidth = Math.max(buttonWidth, button.getPreferredWidth());
            // 状态切换只更改文字；为现有的最长状态一次性留出宽度。
            buttonWidth = Math.max(buttonWidth, stateWidth(testMode, "测试模式：关闭"));
            buttonWidth = Math.max(buttonWidth, stateWidth(noClip, "穿墙：开启"));
            buttonWidth = Math.max(buttonWidth, stateWidth(noClip, "穿墙：关闭"));
            buttonWidth = Math.max(buttonWidth, stateWidth(speed, "倍速：16×"));
            buttonWidth = Math.max(buttonWidth, stateWidth(weather, "天气：火山灰"));
            buttonWidth = Math.max(buttonWidth, stateWidth(pokedex, "图鉴：全解锁"));
            buttonWidth = Math.max(buttonWidth, stateWidth(pokedex, "图鉴：未解锁"));
        }
        return buttonWidth;
    }

    private static int stateWidth(Button button, String text) {
        return button.getFont() == null
                ? button.getPreferredWidth()
                : Math.max(
                        button.getMinWidth(),
                        button.getBorderHorizontal() + button.getFont().computeTextWidth(text));
    }

    private void refresh() {
        testMode.setText("测试模式：" + (testing ? "开启" : "关闭"));
        time.setText("时段：" + world.atmosphere().timeLabel());
        weather.setText("天气：" + world.atmosphere().weatherLabel());
        noClip.setText("穿墙：" + (world.movement().noClip() ? "开启" : "关闭"));
        speed.setText("倍速：" + (int) world.motion().speed() + "×");
        pokedex.setText("图鉴：" + (world.pokedex().pokedexUnlocked() ? "全解锁" : "未解锁"));
        invalidateLayout();
    }

    public void update() {
        // Async operations can disable an action while its popup is already open.
        menuActions.forEach((item, button) -> item.setEnabled(button.isEnabled()));
        String text = "图鉴：" + (world.pokedex().pokedexUnlocked() ? "全解锁" : "未解锁");
        if (!text.equals(pokedex.getText())) {
            pokedex.setText(text);
            invalidateLayout();
        }
    }

    private int rowHeight() {
        return java.util.Arrays.stream(buttons())
                .mapToInt(Button::getPreferredHeight)
                .max()
                .orElse(0);
    }

    public void position(int x, int y, int width, int height) {
        int rows = (buttons().length + 1) / 2;
        int h = rows * (rowHeight() + 5) - 5;
        setSize(fixedButtonWidth() * 2 + 5, h);
        setPosition(x + 8, mobile ? y + 70 : y + (height - h) / 2);
    }

    @Override
    protected void layout() {
        int width = fixedButtonWidth(), height = rowHeight();
        var buttons = buttons();
        for (int i = 0; i < buttons.length; i++) {
            var button = buttons[i];
            button.setPosition(
                    getInnerX() + (i % 2) * (width + 5), getInnerY() + (i / 2) * (height + 5));
            button.setSize(width, height);
        }
    }
}
