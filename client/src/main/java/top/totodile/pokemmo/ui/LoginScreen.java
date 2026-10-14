package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.theme.ThemeManager;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import org.lwjgl.glfw.GLFW;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.DebugSettings;
import top.totodile.pokemmo.HotbarSettings;
import top.totodile.pokemmo.SavedCredentials;
import top.totodile.pokemmo.audio.LoginMusic;
import top.totodile.pokemmo.net.LoginConnection;
import top.totodile.pokemmo.net.GameConnection;
import top.totodile.pokemmo.protocol.GameEndpoint;
import top.totodile.pokemmo.protocol.GameServerInfo;
import top.totodile.pokemmo.rom.BlackWhiteLoginScene;
import top.totodile.pokemmo.rom.FireRedAtlas;
import top.totodile.pokemmo.world.KantoWorld;
import top.totodile.pokemmo.world.ClientWorld;
import top.totodile.pokemmo.world.UnovaWorld;
import top.totodile.pokemmo.world.SinnohWorld;
import top.totodile.pokemmo.rom.PlatinumMaps;
import top.totodile.pokemmo.rom.BlackWhiteMaps;
import top.totodile.pokemmo.protocol.CharacterPackets.CharacterInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 登录画面的 TWL 控件树，包含背景、Logo、登录表单及边角信息。
 * 登录界面使用默认主题中的 logingui/logo 定义。
 * 使用官方 TWL 核心及登录界面所需的控件和主题扩展。
 */
public final class LoginScreen extends Widget implements AutoCloseable {
    private ClientWorld world;
    private CompletableFuture<CharacterInfo> enterResult;
    private CompletableFuture<?> worldMapResult;
    private static final int[] MENU_STRING_IDS = {1172, 1175, 1190, 1005, 1006, 1007, 1124, 1008, 1009};
    private final EditField account = new UsernameEditField();
    private final EditField password = new LoginEditField();
    private final Label accountLabel = new Label(ClientStrings.get(1001));
    private final Label passwordLabel = new Label(ClientStrings.get(1002));
    private final Button login = new Button(ClientStrings.get(1000));
    private final ToggleButton remember = new ToggleButton();
    private final Label rememberLabel = new Label(ClientStrings.get(1025));
    private final ResizableFrame loginWindow = new ResizableFrame();
    private final DialogLayout form = new DialogLayout();
    private final SimpleChangableListModel<String> savedAccounts = new SimpleChangableListModel<>();
    private final ComboBox<String> savedAccount = new ComboBox<>(savedAccounts);
    private final Button clearCredential = new Button(ClientStrings.get(1026));
    private final SavedCredentials savedCredentials;
    private SavedCredentials.Entry selectedCredential;
    private String submittedUsername;
    private boolean rebuildingAccounts;
    private final List<NotificationWindow> notifications = new ArrayList<>();
    private final Widget logo = new Widget();
    private final Label romInfo = new Label(ClientStrings.get(1170) + "\nNDS: --\nGBA: --");
    private String romInfoLines;
    private final Label fpsCounter = new Label("0.00");
    private final Label announcementTitle = new Label(ClientStrings.get(1004));
    private final List<Label> announcements = new ArrayList<>();
    private long fpsStartedAt = System.nanoTime();
    private int fpsFrames;
    private final List<Button> menuButtons = new ArrayList<>();
    private final ClientManagementWindow clientManagement = new ClientManagementWindow(
            this::closeClientManagement, this::refreshTexts, message -> showNotification(message, System.nanoTime()));
    private final ModManagerWindow modManager = new ModManagerWindow(this::closeModManager, this::restartClient,
            message -> showNotification(message, System.nanoTime()));
    private CompletableFuture<top.totodile.pokemmo.protocol.TrainerStats> trainerStatsResult;
    private ClientWorld trainerStatsWorld;
    private long nextTrainerStatsAt;
    private CompletableFuture<Boolean> dexChangeResult;
    private ClientWorld dexChangeWorld;
    private final java.util.Set<Widget> placedGameWindows = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    private final PokemonSummaryWindow pokemonSummary = new PokemonSummaryWindow(() -> pokemonSummaryVisible(false));
    private final PokedexWindow pokedex = new PokedexWindow(() -> pokedexVisible(false));
    private final GameHudBar gameHud = new GameHudBar(
            () -> { if (world != null) bagVisible(!this.bag.isVisible()); },
            () -> { if (world != null) trainerCardVisible(!this.trainerCard.isVisible()); },
            () -> { if (world != null) pokedexVisible(!pokedex.isVisible()); });
    private final BagWindow bag = new BagWindow(() -> bagVisible(false));
    private final TrainerCardWindow trainerCard = new TrainerCardWindow(() -> trainerCardVisible(false));
    private final TeleportMenuWindow teleportMenu = new TeleportMenuWindow(
            () -> teleportMenuVisible(false), this::teleport);
    private final TownMapWindow townMap = new TownMapWindow(() -> townMapVisible(false));
    private final GameMenuPopup gameMenu;
    private PopupWindow menuConfirmation;
    private CompletableFuture<java.util.List<CharacterInfo>> worldLogoutResult;
    private final WorldInfoPanel worldInfo = new WorldInfoPanel();
    private final ChatPanel chatPanel = new ChatPanel();
    private String chatHost = "127.0.0.1";
    private final PartyPanel partyPanel = new PartyPanel();
    private final GameItemBar gameItemBar = new GameItemBar();
    private top.totodile.pokemmo.protocol.CharacterPackets.Appearance battleAppearance;
    private final WildEncounterPanel encounterPanel = new WildEncounterPanel(
            () -> { if (world != null) world.clearKeys(); },
            message -> showNotification(message, System.nanoTime()));
    private final HotbarSettings hotbarSettings = new HotbarSettings();
    private final Button gmEntry = new Button(ClientStrings.get(9200));
    private final Button searchEntry = new Button(ClientStrings.get(9500));
    private final SearchWindow searchWindow = new SearchWindow(() -> searchVisible(false));
    private CompletableFuture<top.totodile.pokemmo.protocol.SearchPackets.Result> searchResult;
    private ClientWorld searchWorld;
    private boolean searchAdding;
    private boolean searchPokemon;
    private ClientWorld inventoryRequestedWorld;
    private final GmMenuWindow gmMenu = new GmMenuWindow(() -> gmMenuVisible(false));
    private final PcBoxWindow pcBox = new PcBoxWindow(() -> pcBoxVisible(false));
    private final PcDialog pcDialog = new PcDialog();
    private final MailWindow mailWindow = new MailWindow();
    private String characterName = "";
    private final GameSettingsWindow settings = new GameSettingsWindow(this::closeSettings);
    private final GlfwTwlRenderer renderer = new GlfwTwlRenderer();
    private final GUI gui;
    private final ThemeManager theme;
    private final ScrollingLoginBackground background;
    private final LoginMusic music;
    private BlackWhiteLoginScene scene;
    private CompletableFuture<BlackWhiteLoginScene> sceneResult;
    private Path scenePath;
    private long sceneStartedAt;
    private final long startedAt = System.nanoTime();
    private final LoginConnection connection = new LoginConnection();
    private CompletableFuture<LoginConnection.Result> loginResult;
    private boolean loggingIn;
    private final ServerSelectionWindow serverSelection = new ServerSelectionWindow(this::joinServer, this::backToLogin);
    private final CharacterSelectionWindow characterSelection = new CharacterSelectionWindow(this::openCharacterCreation, this::backToLogin);
    private final CharacterCreationWindow characterCreation = new CharacterCreationWindow(this::closeCharacterCreation);
    private boolean creatingCharacter;
    private final Label loading = new Label(ClientStrings.get(55));
    private CompletableFuture<GameEndpoint> selectionResult;
    private CompletableFuture<GameConnection.Result> gameResult;
    private CompletableFuture<List<GameServerInfo>> serverListResult;
    private java.util.concurrent.CompletableFuture<top.totodile.pokemmo.protocol.CharacterPackets.CreationResult> deletionResult;
    private GameConnection gameConnection;
    private java.util.concurrent.CompletableFuture<top.totodile.pokemmo.protocol.CharacterPackets.CreationResult> characterResult;
    private String selectedServerName;
    private boolean gameConnected;
    private boolean authenticated;
    private long refreshServersAt;
    private Integer queuedServerId;
    private long textCursor;
    private long activeCursor;
    private long window;
    private int mouseX = Integer.MIN_VALUE;
    private int mouseY = Integer.MIN_VALUE;

    public LoginScreen() throws IOException {
        savedCredentials = new SavedCredentials(ClientFiles.data("../config/savedcredentials.properties").normalize());
        // 父子主题名形成 logingui.logo 路径，Logo 图片由主题的 background 参数提供。
        setTheme("logingui");
        logo.setTheme("logo");
        logo.setMinSize(484, 143);
        logo.setMaxSize(484, 143);
        add(logo);
        account.setTheme("editfield");
        password.setTheme("editfield");
        password.setPasswordMasking(true);
        accountLabel.setLabelFor(account);
        passwordLabel.setLabelFor(password);
        login.addCallback(this::submit);
        account.addCallback(key -> {
            if (key == Event.KEY_RETURN) password.requestKeyboardFocus();
        });
        password.addCallback(key -> {
            if (key == Event.KEY_RETURN) submit();
        });
        rememberLabel.setLabelFor(remember);
        savedAccount.addCallback(() -> {
            if (rebuildingAccounts) return;
            int selected = savedAccount.getSelected();
            selectedCredential = selected >= 0 && selected < savedCredentials.entries().size()
                    ? savedCredentials.entries().get(selected) : null;
            rebuildForm();
        });
        clearCredential.addCallback(() -> {
            if (selectedCredential == null) return;
            try {
                savedCredentials.remove(selectedCredential.username());
                refreshSavedAccounts();
            } catch (IOException exception) {
                showNotification(ClientStrings.get(2000), System.nanoTime());
            }
        });
        loginWindow.setTheme("login-window");
        loginWindow.setTitle(ClientStrings.get(1000));
        loginWindow.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        loginWindow.setDraggable(false);
        loginWindow.add(form);
        refreshSavedAccounts();
        add(loginWindow);
        romInfo.setTheme("label");
        add(romInfo);
        announcementTitle.setTheme("announcement-title");
        add(announcementTitle);
        // 公告只展示文本，不接入点击操作或键盘焦点。
        for (int i = 0; i < 6; i++) {
            Label item = new Label(ClientStrings.get(9000 + i));
            item.setTheme("announcement-item");
            item.setCanAcceptKeyboardFocus(false);
            announcements.add(item);
            add(item);
        }
        for (int stringId : MENU_STRING_IDS) {
            Button button = new Button(ClientStrings.get(stringId));
            button.setTheme("menu-button");
            // 按钮功能在对应窗口或模块接入时添加。
            button.setCanAcceptKeyboardFocus(false);
            if (stringId == 1175) {
                button.addCallback(this::openClientManagement);
            } else if (stringId == 1190) {
                button.addCallback(this::openModManager);
            } else if (stringId == 1008) {
                button.addCallback(this::openSettings);
            } else if (stringId == 1009) {
                button.addCallback(() -> {
                    // 交由主循环退出并释放网络连接、UI 资源及窗口。
                    if (window != 0L) GLFW.glfwSetWindowShouldClose(window, true);
                });
            }
            menuButtons.add(button);
            add(button);
        }
        clientManagement.setHardVisible(false);
        add(clientManagement);
        modManager.setHardVisible(false);
        add(modManager);
        settings.setHardVisible(false);
        add(settings);
        add(gameHud);
        gameMenu = new GameMenuPopup(gameHud.menuButton(), this::openSettings,
                () -> confirmGameMenu(1160, this::logoutWorld),
                () -> confirmGameMenu(1115, () -> GLFW.glfwSetWindowShouldClose(window, true)));
        gameHud.menuButton().addCallback(this::openGameMenu);
        add(worldInfo);
        add(chatPanel);
        add(partyPanel);
        add(gameItemBar);
        add(encounterPanel);
        for (int slot = 0; slot < 9; slot++) gameItemBar.assign(slot, hotbarSettings.slot(slot));
        gameItemBar.setActivateListener(this::useHotbarItem);
        bag.setUseAction(id -> searchCommand(top.totodile.pokemmo.protocol.SearchPackets.USE_ITEM,id,1));
        bag.setDestroyAction((id,amount)->searchCommand(top.totodile.pokemmo.protocol.SearchPackets.DESTROY_ITEM,id,amount));
        bag.setHotbarBinding(new BagWindow.HotbarBinding() {
            @Override public int slotOf(int itemId) { return gameItemBar.slotOf(itemId); }
            @Override public void register(int itemId, int slot) { registerHotbarItem(itemId, slot); }
            @Override public void unregister(int itemId) { unregisterHotbarItem(itemId); }
        });
        add(pokemonSummary);
        pokemonSummary.setOpenDex(species -> {pokedexVisible(true);pokedex.selectSpecies(species);});
        partyPanel.setSummaryAction(member -> {
            if(world==null)return;
            world.clearKeys();pokemonSummary.open(member);raiseGameWindow(pokemonSummary);
        });
        partyPanel.setActions(species -> { pokedexVisible(true); pokedex.selectSpecies(species); },
                (from, to) -> gameConnection.moveParty(from, to),
                () -> { if (world != null) world.clearKeys(); },
                () -> showNotification(ClientStrings.get(9351), System.nanoTime()));
        gmEntry.setTheme("/button");
        gmEntry.setVisible(false);
        gmEntry.addCallback(() -> gmMenuVisible(true));
        add(gmEntry);
        searchEntry.setTheme("/button"); searchEntry.setVisible(false);
        searchEntry.addCallback(() -> searchVisible(!searchWindow.isVisible())); add(searchEntry);
        searchWindow.setAddAction(request -> searchCommand(request.action(),request.id(),request.amount()));
        add(searchWindow);
        gmMenu.setHardVisible(false);
        add(gmMenu);
        gmMenu.setDexChanged(this::changeDex);
        gmMenu.setNoclipChanged(enabled -> {
            if (world == null) return;
            world.setNoclip(enabled);
        });
        gmMenu.setSpeedChanged(multiplier -> {
            if (world == null) return;
            world.setSpeedMultiplier(multiplier);
        });
        gmMenu.setOpenTeleport(() -> { gmMenuVisible(false); teleportMenuVisible(true); });
        gmMenu.setOpenComputer(() -> { gmMenuVisible(false); pcBoxVisible(true); });
        pcBox.setCommands(this::pcParty, this::pcBoxPage, this::pcMove, this::pcCreateBox);
        // PC 搬移成功后拿到的权威队伍同步给世界队伍面板，免得只能注销重登才看到变化。
        pcBox.setPartyListener(list -> partyPanel.setParty(list));
        pcBox.setNotify(message -> showNotification(message, System.nanoTime()));
        pcBox.setHardVisible(false);
        add(pcBox);
        pcDialog.setOnChoose(this::choosePcDialog);
        add(pcDialog);
        mailWindow.addCloseCallback(() -> mailVisible(false));
        add(mailWindow);
        teleportMenu.setHardVisible(false);
        add(teleportMenu);
        townMap.setHardVisible(false);
        add(townMap);
        trainerCard.setVisible(false);
        add(trainerCard);
        bag.setHardVisible(false);
        add(bag);
        pokedex.setHardVisible(false);
        add(pokedex);
        serverSelection.setVisible(false);
        add(serverSelection);
        characterSelection.setVisible(false);
        add(characterSelection);
        characterCreation.setVisible(false);
        add(characterCreation);
        characterCreation.setOnCreate(this::createCharacter);
        characterSelection.setAssets(characterCreation.assets());
        characterSelection.setOnDelete(this::deleteCharacter);
        characterSelection.setOnEnter(this::enterCharacter);
        loading.setTheme("loading");
        loading.setVisible(false);
        add(loading);
        fpsCounter.setTheme("fpscounter");
        fpsCounter.setCanAcceptKeyboardFocus(false);
        add(fpsCounter);
        add(partyPanel.dragOverlay());
        add(pcBox.dragOverlay());
        // GLFW 回调通过 attachInput 接入；GUI 负责控件树布局和绘制。
        gui = new GUI(this, renderer, null);
        partyPanel.setFollowerActions(index -> gameConnection.selectFollower(index),
                member -> { if (world != null) world.setFollower(member); },
                (x, y) -> world != null && worldLogoutResult == null && !gui.hasOpenPopups() && getWidgetAt(x, y) == this,
                () -> showNotification(ClientStrings.get(9352), System.nanoTime()));

        // 主题从外部 data/themes/default 加载，图片 URL 相对于主题 XML 解析。
        theme = ThemeManager.createThemeManager(
                ClientFiles.data("themes/default/theme.xml").toUri().toURL(), renderer);
        try {
            // 复用主题缓存，关闭登录界面时统一释放 Logo 和背景纹理。
            background = new ScrollingLoginBackground(renderer);
            gui.applyTheme(theme);
            music = new LoginMusic();
            settings.setAudioCallbacks(music::setVolume, music::setMuteWhenUnfocused);
            settings.setSaveFailed(message -> showNotification(message, System.nanoTime()));
            settings.setFpsVisibilityChanged(fpsCounter::setVisible);
            refreshRomInfo();
        } catch (IOException | RuntimeException exception) {
            theme.destroy();
            throw exception;
        }
    }

    private void refreshTexts() {
        clearCredential.setText(ClientStrings.get(1026));
        refreshSavedAccounts();
        announcementTitle.setText(ClientStrings.get(1004));
        for (int i = 0; i < announcements.size(); i++) {
            announcements.get(i).setText(ClientStrings.get(9000 + i));
        }
        modManager.refreshTexts();
        accountLabel.setText(ClientStrings.get(1001));
        passwordLabel.setText(ClientStrings.get(1002));
        rememberLabel.setText(ClientStrings.get(1025));
        loginWindow.setTitle(ClientStrings.get(1000));
        login.setText(ClientStrings.get(loggingIn ? 1003 : 1000));
        refreshRomInfo();
        for (int i = 0; i < menuButtons.size(); i++) {
            menuButtons.get(i).setText(ClientStrings.get(MENU_STRING_IDS[i]));
        }
    }

    private void refreshSavedAccounts() {
        rebuildingAccounts = true;
        savedAccounts.clear();
        int selected = 0;
        List<SavedCredentials.Entry> entries = savedCredentials.entries();
        for (int i = 0; i < entries.size(); i++) {
            savedAccounts.addElement(entries.get(i).username());
            if (entries.get(i).username().equalsIgnoreCase(savedCredentials.lastUsername())) selected = i;
        }
        savedAccounts.addElement(ClientStrings.get(1028));
        savedAccount.setSelected(selected);
        selectedCredential = entries.isEmpty() ? null : entries.get(selected);
        rebuildingAccounts = false;
        rebuildForm();
    }

    private void rebuildForm() {
        // 已保存账号使用下拉与清除按钮，其他用户名使用输入框与记住我。
        boolean saved = selectedCredential != null;
        account.setVisible(!saved);
        savedAccount.setVisible(saved);
        remember.setVisible(!saved);
        rememberLabel.setVisible(!saved);
        clearCredential.setVisible(saved);
        password.setEnabled(!saved && !loggingIn);
        password.setPasswordMasking(!saved);
        password.setText(saved ? ClientStrings.get(1027) : "");
        accountLabel.setLabelFor(saved ? savedAccount : account);
        form.removeAllChildren();
        Widget username = saved ? savedAccount : account;
        DialogLayout.Group bottom = form.createSequentialGroup();
        if (saved) bottom.addWidget(clearCredential);
        else bottom.addWidget(remember).addWidget(rememberLabel);
        bottom.addGap(0, 0, Short.MAX_VALUE).addWidget(login);
        form.setHorizontalGroup(form.createParallelGroup()
                .addGroup(form.createSequentialGroup()
                        .addGroup(form.createParallelGroup(accountLabel, passwordLabel))
                        .addGroup(form.createParallelGroup(username, password)))
                .addGroup(bottom));
        form.setVerticalGroup(form.createSequentialGroup()
                .addGroup(form.createParallelGroup(accountLabel, username))
                .addGroup(form.createParallelGroup(passwordLabel, password))
                .addGroup(saved ? form.createParallelGroup(clearCredential, login)
                        : form.createParallelGroup(remember, rememberLabel, login)));
        invalidateLayout();
    }

    @Override
    protected void paintWidget(GUI gui) {
        // TWL 先绘制当前控件再绘制子控件，表单和 Logo 叠加在背景上方。
        if (world == null && scene == null) {
            background.draw(getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight(),
                    System.nanoTime() - startedAt);
        }
    }

    @Override
    protected void layout() {
        layoutMovableWindow(pokedex, 680, 448);
        if(pokemonSummary.isVisible())layoutMovableWindow(pokemonSummary,506,316);
        layoutMovableWindow(bag, 540, 386);
        int mailWidth = Math.min(mailWindow.displayWidth(),getInnerWidth());
        if(mailWindow.getWidth()!=mailWidth)placedGameWindows.remove(mailWindow);
        layoutMovableWindow(mailWindow, mailWidth, Math.min(556,getInnerHeight()));
        layoutMovableWindow(searchWindow, 660, 500);
        // 随身电脑按参考客户端的版式需要 1018×624 的内容区，客户区更小时再压缩。
        layoutMovableWindow(pcBox, Math.min(1024, getInnerWidth()), Math.min(652, getInnerHeight()));
        pcDialog.setPosition(getInnerX(), getInnerY());
        pcDialog.setSize(getInnerWidth(), getInnerHeight());
        layoutMovableWindow(trainerCard, 454, 287);
        teleportMenu.setSize(800, 500);
        teleportMenu.setPosition(getInnerX() + (getInnerWidth() - 800) / 2,
                getInnerY() + (getInnerHeight() - 500) / 2);
        if (townMap.isVisible()) layoutMovableWindow(townMap, 660, 620);
        gameHud.adjustSize();
        gameHud.setPosition(getInnerRight() - gameHud.getWidth() - 5, getInnerBottom() - gameHud.getHeight());
        partyPanel.adjustSize();
        partyPanel.place();
        worldInfo.adjustSize();
        worldInfo.setPosition(getInnerX(), getInnerY());
        chatPanel.place(getInnerX() + 2, getInnerBottom() - 2, getInnerWidth() - 4, getInnerHeight() - 4);
        encounterPanel.setPosition(getInnerX(), getInnerY());
        encounterPanel.setSize(getInnerWidth(), getInnerHeight());
        gameItemBar.adjustSize();
        gameItemBar.setPosition(getInnerX() + (getInnerWidth() - gameItemBar.getWidth()) / 2, getInnerY() + gameItemBar.topOffset());
        gmEntry.adjustSize();
        gmEntry.setPosition(getInnerX() + 5, worldInfo.getBottom() + 5);
        searchEntry.adjustSize(); searchEntry.setPosition(getInnerX()+5,gmEntry.getBottom()+5);
        gmMenu.setSize(810, 511);
        gmMenu.setPosition(getInnerX() + (getInnerWidth() - 810) / 2,
                getInnerY() + (getInnerHeight() - 511) / 2);
        partyPanel.dragOverlay().setPosition(getInnerX(), getInnerY());
        partyPanel.dragOverlay().setSize(getInnerWidth(), getInnerHeight());
        pcBox.dragOverlay().setPosition(getInnerX(), getInnerY());
        pcBox.dragOverlay().setSize(getInnerWidth(), getInnerHeight());
        fpsCounter.adjustSize();
        fpsCounter.setPosition(getInnerRight() - fpsCounter.getWidth(), getInnerY());
        // Logo 固定为 484×143，在客户区居中后向上偏移 200。
        logo.setSize(484, 143);
        logo.setPosition(getInnerX() + (getInnerWidth() - logo.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - logo.getHeight()) / 2 - 200);
        // 窗口使用主题和 DialogLayout 的首选尺寸，在客户区居中。
        loginWindow.adjustSize();
        loginWindow.setPosition(getInnerX() + (getInnerWidth() - loginWindow.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - loginWindow.getHeight()) / 2);
        romInfo.adjustSize();
        romInfo.setPosition(getInnerX() + 3, getInnerY() + 3);
        int announcementWidth = announcements.stream().mapToInt(Label::getPreferredWidth).max().orElse(0);
        int announcementY = getInnerBottom() - 5;
        for (int i = announcements.size() - 1; i >= 0; i--) {
            Label item = announcements.get(i);
            int height = item.getPreferredHeight();
            announcementY -= height;
            item.setSize(announcementWidth, height);
            item.setPosition(getInnerX() + 5, announcementY);
            announcementY -= 8;
        }
        announcementTitle.adjustSize();
        announcementTitle.setPosition(getInnerX() + 17, announcementY - announcementTitle.getHeight());
        clientManagement.setSize(528, 337);
        clientManagement.setPosition(getInnerX() + (getInnerWidth() - clientManagement.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - clientManagement.getHeight()) / 2);
        modManager.setSize(Math.min(getInnerWidth() - 20, Math.min(modManager.getMaxWidth(), modManager.getPreferredWidth())),
                Math.min(getInnerHeight() - 40, Math.min(modManager.getMaxHeight(), modManager.getPreferredHeight())));
        modManager.setPosition(getInnerX() + (getInnerWidth() - modManager.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - modManager.getHeight()) / 2);
        settings.setSize(585, 498);
        settings.setPosition(getInnerX() + (getInnerWidth() - settings.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - settings.getHeight()) / 2);
        serverSelection.adjustSize();
        serverSelection.setPosition(getInnerX() + (getInnerWidth() - serverSelection.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - serverSelection.getHeight()) / 2);
        characterSelection.adjustSize();
        characterSelection.setPosition(getInnerX() + (getInnerWidth() - characterSelection.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - characterSelection.getHeight()) / 2);
        characterCreation.setSize(characterCreation.getMinWidth(), characterCreation.getMinHeight());
        characterCreation.setPosition(getInnerX() + (getInnerWidth() - characterCreation.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - characterCreation.getHeight()) / 2);
        loading.adjustSize();
        loading.setPosition(getInnerX() + (getInnerWidth() - loading.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - loading.getHeight()) / 2);
        for (int i = 0; i < menuButtons.size(); i++) {
            Button button = menuButtons.get(i);
            button.setSize(200, 26);
            button.setPosition(getInnerRight() - 204,
                    getInnerBottom() - 4 - (menuButtons.size() - i) * 26);
        }
        layoutNotifications();
    }

    private void openClientManagement() {
        characterSelection.setVisible(false);
        characterCreation.setVisible(false);
        settings.setHardVisible(false);
        modManager.setHardVisible(false);
        loginWindow.setVisible(false);
        serverSelection.setVisible(false);
        clientManagement.setEnabled(true);
        clientManagement.setHardVisible(true);
        clientManagement.requestKeyboardFocus();
    }

    private void openModManager() {
        // 已显示时保留列表、滚动位置和后台操作状态，避免重复扫描造成控件重建。
        if (modManager.isVisible()) return;
        characterSelection.setVisible(false);
        characterCreation.setVisible(false);
        settings.setHardVisible(false);
        clientManagement.setHardVisible(false);
        loginWindow.setVisible(false);
        serverSelection.setVisible(false);
        modManager.setEnabled(true);
        modManager.setHardVisible(true);
        modManager.reload();
        modManager.requestKeyboardFocus();
    }

    private void closeModManager() {
        modManager.setEnabled(false);
        modManager.setHardVisible(false);
        restoreMainWindow();
    }

    private void openSettings() {
        characterSelection.setVisible(false);
        characterCreation.setVisible(false);
        if (world != null) { world.clearKeys(); raiseGameWindow(settings); }
        settings.resetView();
        clientManagement.setHardVisible(false);
        modManager.setHardVisible(false);
        loginWindow.setVisible(false);
        serverSelection.setVisible(false);
        settings.setEnabled(true);
        settings.setHardVisible(true);
        settings.requestKeyboardFocus();
    }

    private void closeSettings() {
        settings.setEnabled(false);
        settings.setHardVisible(false);
        restoreMainWindow();
    }

    private void closeClientManagement() {
        if (clientManagement.restartRequired()) {
            restartClient();
            return;
        }
        // 返回登录页时不恢复输入框焦点；启动和登录失败的焦点分别由对应流程处理。
        clientManagement.setEnabled(false);
        clientManagement.setHardVisible(false);
        restoreMainWindow();
        romInfo.setVisible(true);
        for (Button button : menuButtons) button.setVisible(true);
    }

    private void restartClient() {
        try {
            top.totodile.pokemmo.ClientRestart.request();
            GLFW.glfwSetWindowShouldClose(window, true);
        } catch (IOException | RuntimeException exception) {
            showNotification(ClientStrings.get(84), System.nanoTime());
        }
    }

    private void layoutNotifications() {
        // 旧提示在下，新提示向上排列，底部留 100 像素。
        int bottom = getInnerBottom() - 100;
        for (NotificationWindow notification : notifications) {
            notification.adjustSize();
            notification.setPosition(getInnerX() + (getInnerWidth() - notification.getWidth()) / 2,
                    bottom - notification.getHeight());
            bottom -= notification.getHeight() + 5;
        }
    }

    private void restoreMainWindow() {
        loginWindow.setVisible(!authenticated);
        serverSelection.setVisible(authenticated && !gameConnected && !loading.isVisible());
        characterSelection.setVisible(gameConnected && !creatingCharacter && world == null && enterResult == null);
        characterCreation.setVisible(gameConnected && creatingCharacter);
    }

    /** 每帧先同步尺寸，再让 TWL 更新时间、校验布局并绘制控件。 */
    public void render(int width, int height, int framebufferWidth, int framebufferHeight) {
        renderer.resize(width, height, framebufferWidth, framebufferHeight);
        long now = System.nanoTime();
        // 每 100 帧计算一次平均帧率，保留两位小数。
        if (++fpsFrames == 100) {
            long elapsed = Math.max(1, now - fpsStartedAt);
            long hundredths = Math.min(999999, (fpsFrames * 100_000_000_000L + elapsed / 2) / elapsed);
            fpsCounter.setText(String.format(java.util.Locale.ROOT, "%d.%02d", hundredths / 100, hundredths % 100));
            fpsStartedAt = now;
            fpsFrames = 0;
            invalidateLayout();
        }
        updateLoginState(now);
        updateWorldEntry(now);
        updateDexChange(now);
        updateSearch(now);
        updateTrainerStats(now);
        updateWorldLogout(now);
        // 入图资源可能在本帧创建，绘制时间须晚于新世界的计时起点。
        now = System.nanoTime();
        if (world != null && worldLogoutResult == null && enterResult == null) {
            try {
                world.render(framebufferWidth, framebufferHeight, now);
                if (world.takeFollowerFailure()) {
                    partyPanel.clearFollowerSelection();
                    showNotification(ClientStrings.get(world.region() >= 2 ? 9350 : 9353), now);
                }
            }
            catch (IOException exception) {
                backToLogin();
                showNotification(ClientStrings.get(9030), now);
            }
        } else updateScene(now);
        if (world == null && scene != null) {
            try {
                scene.draw(width, height, (now - sceneStartedAt) / 1_000_000_000.0);
            } catch (RuntimeException exception) {
                scene.close();
                scene = null;
                reportSceneFailure(exception);
            }
        }
        gmEntry.setVisible(world != null);
        gmEntry.setEnabled(worldLogoutResult == null && enterResult == null);
        searchEntry.setVisible(world != null); searchEntry.setEnabled(worldLogoutResult == null && enterResult == null);
        gameHud.setEnabled(worldLogoutResult == null && enterResult == null);
        worldInfo.setVisible(world != null);
        chatPanel.update(world != null, chatHost);
        partyPanel.setGameVisible(world != null);
        partyPanel.setEnabled(worldLogoutResult == null && enterResult == null);
        if (world != null) {
            worldInfo.update(world.locationName(), gameConnection == null ? -1 : gameConnection.worldSeconds());
            partyPanel.adjustSize();
            partyPanel.place();
            worldInfo.adjustSize();
        }
        gameHud.setGameVisible(world != null);
        gameItemBar.setGameVisible(world != null && worldLogoutResult == null);
        if (world != null && townMap.isVisible()) townMap.updateState(world.position(), world.locationName());
        if (world == null) {
            searchVisible(false);
            gmMenu.setHardVisible(false);
            teleportMenu.setHardVisible(false);
            townMap.close();
            trainerCardVisible(false);
            bagVisible(false);
            pokedexVisible(false); pokemonSummaryVisible(false);
            pcDialog.close();
        }
        if (world == null) encounterPanel.close();
        else if (encounterPanel.active()) world.clearKeys();
        encounterPanel.tick(now / 1_000_000);
        gui.update();
        if (world != null) world.renderEntryFade(framebufferWidth, framebufferHeight, now);
    }

    /** 在工作线程解析资源，仅在绘制线程创建和释放纹理。 */
    private void updateScene(long now) {
        music.update(clientManagement.selectedRom(0));
        if (sceneResult != null && sceneResult.isDone()) {
            try {
                scene = sceneResult.join();
                sceneStartedAt = now;
                refreshRomInfo();
            } catch (CompletionException exception) {
                reportSceneFailure(exception.getCause());
            }
            sceneResult = null;
        }
        Path selected = clientManagement.selectedRom(0);
        if (sceneResult != null || Objects.equals(selected, scenePath)) return;
        if (scene != null) scene.close();
        scene = null;
        scenePath = selected;
        refreshRomInfo();
        if (selected != null) {
            boolean whiteScene = ThreadLocalRandom.current().nextBoolean();
            sceneResult = CompletableFuture.supplyAsync(() -> {
                try {
                    return new BlackWhiteLoginScene(selected, whiteScene);
                } catch (IOException exception) {
                    throw new CompletionException(exception);
                }
            });
        }
    }

    private void refreshRomInfo() {
        // ROM 信息保留登录界面创建时的快照，导入后的变更在重启后显示。
        if (romInfoLines == null) {
            romInfoLines = "\nNDS: " + romTitles(false, 0, 3, 4)
                    + "\nGBA: " + romTitles(true, 1, 2);
        }
        romInfo.setText(ClientStrings.get(1170) + romInfoLines);
        invalidateLayout();
    }

    private String romTitles(boolean gba, int... indexes) {
        List<String> titles = new ArrayList<>();
        for (int position = 0; position < indexes.length; position++) {
            int index = indexes[position];
            Path file = clientManagement.selectedRom(index);
            if (file == null) {
                if (position == 0) titles.add("--");
                continue;
            }
            try (var input = java.nio.file.Files.newInputStream(file)) {
                if (gba) {
                    // GBA 显示 0xAC 处的游戏代码和 0xBC 处的修订号。
                    input.skipNBytes(0xAC);
                    byte[] code = input.readNBytes(4);
                    if (code.length != 4) throw new IOException("Incomplete GBA game code");
                    input.skipNBytes(12);
                    int revision = input.read();
                    if (revision < 0) throw new IOException("Missing GBA revision");
                    titles.add(new String(code, java.nio.charset.StandardCharsets.US_ASCII) + " v1." + revision);
                } else {
                    byte[] header = input.readNBytes(12);
                    if (header.length != 12) throw new IOException("Incomplete NDS title");
                    String title = new String(header, java.nio.charset.StandardCharsets.US_ASCII);
                    titles.add(position == 0 ? title : title.replaceAll("\\P{Print}", ""));
                }
            } catch (IOException exception) {
                if (position == 0) titles.add("--");
            }
        }
        return titles.isEmpty() ? "--" : String.join(", ", titles);
    }

    public int fpsLimit() {
        return settings.fpsLimit();
    }

    private void reportSceneFailure(Throwable exception) {
        System.err.println("Unable to load login scene: " + exception);
        refreshRomInfo();
        showNotification(ClientStrings.get(1182).replace("{00}", scenePath.getFileName().toString())
                .replace("{01}", ClientStrings.get(92)), System.nanoTime());
    }

    private void submit() {
        if (loggingIn || !loginWindow.isVisible()) return;
        loggingIn = true;
        login.setText(ClientStrings.get(1003));
        setFormEnabled(false);
        submittedUsername = selectedCredential == null ? account.getText() : selectedCredential.username();
        if (selectedCredential == null) {
            loginResult = connection.connect(submittedUsername, password.getText(), remember.isActive(),
                    ClientStrings.languageCode());
        } else {
            loginResult = connection.connectSaved(submittedUsername, selectedCredential.key(), ClientStrings.languageCode());
        }
    }

    private void joinServer(int serverId) {
        if (selectionResult != null || gameResult != null || gameConnected) return;
        if (serverListResult != null) {
            queuedServerId = serverId;
            return;
        }
        selectedServerName = connection.servers().stream().filter(server -> server.id() == serverId)
                .map(GameServerInfo::name).findFirst().orElse("");
        serverSelection.setBusy(serverId);
        serverSelection.setVisible(false);
        loading.setVisible(true);
        selectionResult = connection.select(serverId);
    }

    private void enterCharacter(CharacterInfo character) {
        if (gameConnection == null || enterResult != null || world != null) return;
        Path rom = clientManagement.selectedRom(character.appearance().region() == 0 ? 1 : character.appearance().region() == 1 ? 2 : character.appearance().region() == 2 ? 0 : character.appearance().region() == 4 ? 5 : 3);
        if (rom == null) { showNotification(ClientStrings.get(9030), System.nanoTime()); return; }
        characterSelection.setBusy(true);
        characterSelection.setVisible(false);
        setLoginDecorationsVisible(false);
        loading.setVisible(true);
        enterResult = gameConnection.enterCharacter(character.id());
        worldMapResult = CompletableFuture.supplyAsync(() -> {
            try { return character.appearance().region() == 2 ? new BlackWhiteMaps(rom) : character.appearance().region() == 3 ? new PlatinumMaps(rom) : new FireRedAtlas(rom, character.appearance().region() == 4); }
            catch (IOException exception) { throw new CompletionException(exception); }
        });
    }

    private void updateWorldEntry(long now) {
        if (enterResult == null || !enterResult.isDone() || !worldMapResult.isDone()) return;
        ClientWorld previousWorld = world;
        try {
            var character = enterResult.join();
            world = character.appearance().region() == 2
                    ? new UnovaWorld((BlackWhiteMaps) worldMapResult.join(), characterCreation.assets(), character.appearance())
                    : character.appearance().region() == 3
                    ? new SinnohWorld((PlatinumMaps) worldMapResult.join(), characterCreation.assets(), character.appearance())
                    : new KantoWorld((FireRedAtlas) worldMapResult.join(), characterCreation.assets(), character.appearance());
            world.restore(character.position());
            world.restoreCycling(character.cycling());
            // 首次移动前恢复此角色独立保存的穿墙与倍速。
            world.setNoclip(character.noclip());
            world.setSpeedMultiplier(character.speedMultiplier());
            battleAppearance = character.appearance();
            characterName = character.appearance().name();
            pokedex.setAllUnlocked(character.dexUnlocked());
            partyPanel.setParty(character.party());
            bag.setInventory(java.util.Map.of());
            gameItemBar.setInventory(java.util.Map.of());
            searchCommand(top.totodile.pokemmo.protocol.SearchPackets.INVENTORY,0,0);
            trainerCard.setCharacter(character, characterCreation.assets());
            placedGameWindows.remove(trainerCard);
            worldInfo.setMoney(character.stats().money());
            nextTrainerStatsAt = 0;
            var session = gameConnection;
            world.setAuthority(packet -> session.worldCommand(packet).whenComplete((result, error) -> {
                if (error != null) session.close();
            }));
            world.beginEntry(System.nanoTime());
        } catch (CompletionException | IOException exception) {
            System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Unable to enter character world", exception);
            if (world != null) { world.close(); world = null; }
            if (previousWorld != null) backToLogin();
            characterSelection.setVisible(gameConnected);
            setLoginDecorationsVisible(true);
            showNotification(ClientStrings.get(9030), now);
        } finally {
            if (previousWorld != null && previousWorld != world) previousWorld.close();
            loading.setVisible(false);
            teleportMenu.setEnabled(true);
            characterSelection.setBusy(false);
            enterResult = null;
            worldMapResult = null;
        }
    }

    private void backToLogin() {
        encounterPanel.close(); battleAppearance = null;
        if (worldLogoutResult != null) { worldLogoutResult.cancel(false); worldLogoutResult = null; }
        gameMenu.closePopup();
        if (menuConfirmation != null) menuConfirmation.closePopup();
        if (world != null) { world.close(); world = null; }
        if (enterResult != null) enterResult.cancel(true);
        if (worldMapResult != null) worldMapResult.cancel(true);
        enterResult = null;
        worldMapResult = null;
        creatingCharacter = false;
        setLoginDecorationsVisible(true);
        characterCreation.setVisible(false);
        authenticated = false;
        connection.disconnect();
        if (gameConnection != null) gameConnection.close();
        gameConnection = null;
        characterResult = null;
        deletionResult = null;
        characterSelection.close();
        gameConnected = false;
        selectionResult = null;
        gameResult = null;
        serverListResult = null;
        queuedServerId = null;
        serverSelection.setVisible(false);
        characterSelection.setVisible(false);
        loading.setVisible(false);
        for (Button button : menuButtons) button.setEnabled(true);
        serverSelection.resetMessage();
        settings.setHardVisible(false);
        modManager.setHardVisible(false);
        clientManagement.setHardVisible(false);
        loginWindow.setVisible(true);
        invalidateLayout();
    }

    private void updateServerSelection(long now) {
        if (!authenticated) return;
        if (deletionResult != null && deletionResult.isDone()) {
            try {
                var result = deletionResult.join();
                if (result.status() == top.totodile.pokemmo.protocol.CharacterPackets.Status.SUCCESS) {
                    characterSelection.setCharacters(result.characters());
                    invalidateLayout();
                } else showNotification(ClientStrings.get(9027), now);
            } catch (CompletionException | java.util.concurrent.CancellationException exception) {
                showNotification(ClientStrings.get(2014), now);
            }
            characterSelection.setBusy(false);
            deletionResult = null;
        }
        if (characterResult != null && characterResult.isDone()) {
            try {
                var result = characterResult.join();
                characterCreation.setSubmitting(false);
                if (result.status() == top.totodile.pokemmo.protocol.CharacterPackets.Status.SUCCESS) {
                    characterSelection.setCharacters(result.characters());
                    closeCharacterCreation();
                } else {
                    int id = switch (result.status()) {
                        case LIMIT_REACHED -> 9020;
                        case NAME_TAKEN -> 9021;
                        case INVALID -> 9022;
                        default -> 9023;
                    };
                    showNotification(ClientStrings.get(id), now);
                }
            } catch (CompletionException | java.util.concurrent.CancellationException exception) {
                characterCreation.setSubmitting(false);
                showNotification(ClientStrings.get(2014), now);
            }
            characterResult = null;
        }
        if (selectionResult != null && selectionResult.isDone()) {
            try {
                GameEndpoint endpoint = selectionResult.join();
                chatHost = endpoint.host();
                gameConnection = new GameConnection();
                gameResult = gameConnection.connect(endpoint);
            } catch (CompletionException | java.util.concurrent.CancellationException exception) {
                serverSelection.resetMessage();
                finishServerLoading();
                int stringId = exception.getCause() instanceof GameEndpoint.Rejected rejected
                        ? rejected.status() == 6 ? 2002 : 75 : 2014;
                showNotification(ClientStrings.get(stringId), now);
            }
            selectionResult = null;
        }
        if (gameResult != null && gameResult.isDone()) {
            GameConnection.Result result = gameResult.join();
            gameResult = null;
            if (result == GameConnection.Result.CONNECTED) {
                gameConnected = true;
                characterSelection.setCharacters(gameConnection.characters());
                finishServerLoading();
                characterSelection.setVisible(true);
                characterSelection.requestKeyboardFocus();
                connection.disconnect();
            } else {
                gameConnection.close();
                gameConnection = null;
                serverSelection.resetMessage();
                finishServerLoading();
                showNotification(ClientStrings.get(result == GameConnection.Result.REJECTED ? 50003 : 2014), now);
            }
        }
        if (gameConnected && !gameConnection.isConnected()) {
            backToLogin();
            showNotification(ClientStrings.get(2014), now);
            return;
        }
        // 空闲时每五秒更新状态；刷新与选择操作串行，避免封包响应相互错配。
        if (!gameConnected && selectionResult == null && gameResult == null) {
            if (serverListResult != null && serverListResult.isDone()) {
                try {
                    serverSelection.setServers(serverListResult.join());
                } catch (CompletionException | java.util.concurrent.CancellationException exception) {
                    backToLogin();
                    showNotification(ClientStrings.get(2009), now);
                    return;
                }
                serverListResult = null;
                if (queuedServerId != null) {
                    int serverId = queuedServerId;
                    queuedServerId = null;
                    joinServer(serverId);
                }
            }
            if (selectionResult == null && serverListResult == null && now >= refreshServersAt) {
                refreshServersAt = now + 5_000_000_000L;
                serverListResult = connection.refresh();
            }
        }
    }

    private void finishServerLoading() {
        loading.setVisible(false);
        for (Button button : menuButtons) button.setEnabled(true);
        serverSelection.setVisible(!gameConnected);
        invalidateLayout();
    }

    private void deleteCharacter(long id) {
        if (!gameConnected || deletionResult != null || characterResult != null) return;
        characterSelection.setBusy(true);
        deletionResult = gameConnection.deleteCharacter(id);
    }

    private void createCharacter(top.totodile.pokemmo.protocol.CharacterPackets.Appearance appearance) {
        if (!gameConnected || characterResult != null) return;
        characterCreation.setSubmitting(true);
        characterResult = gameConnection.createCharacter(appearance);
    }

    private void openCharacterCreation() {
        if (!gameConnected || creatingCharacter
                || gameConnection.characters().size() >= top.totodile.pokemmo.protocol.CharacterPackets.LIMIT) return;
        creatingCharacter = true;
        setLoginDecorationsVisible(false);
        characterCreation.reset();
        characterSelection.setVisible(false);
        characterCreation.setVisible(true);
        characterCreation.requestKeyboardFocus();
        invalidateLayout();
    }

    private void closeCharacterCreation() {
        creatingCharacter = false;
        setLoginDecorationsVisible(true);
        characterCreation.setVisible(false);
        restoreMainWindow();
        characterSelection.requestKeyboardFocus();
    }

    private void setLoginDecorationsVisible(boolean visible) {
        logo.setVisible(visible);
        romInfo.setVisible(visible);
        announcementTitle.setVisible(visible);
        for (Label announcement : announcements) announcement.setVisible(visible);
        for (Button button : menuButtons) button.setVisible(visible);
    }

    /** 网络线程只返回结果，TWL 控件仍在绘制线程更新。 */
    private void updateLoginState(long now) {
        if (loggingIn && loginResult.isDone()) {
            loggingIn = false;
            login.setText(ClientStrings.get(1000));
            setFormEnabled(true);
            LoginConnection.Result result;
            try {
                result = loginResult.join();
            } catch (CompletionException | java.util.concurrent.CancellationException exception) {
                result = LoginConnection.Result.COMMUNICATION;
            }
            if (result == LoginConnection.Result.AUTHENTICATED) {
                try {
                    String credential = connection.issuedCredential();
                    if (!credential.isEmpty()) {
                        savedCredentials.save(submittedUsername, java.util.Base64.getDecoder().decode(credential));
                        refreshSavedAccounts();
                    } else if (selectedCredential != null) {
                        savedCredentials.select(submittedUsername);
                    }
                } catch (IOException | IllegalArgumentException exception) {
                    showNotification(ClientStrings.get(2000), now);
                }
                authenticated = true;
                loginWindow.setVisible(false);
                serverSelection.resetMessage();
                serverSelection.setServers(connection.servers());
                serverSelection.setVisible(true);
                serverSelection.requestKeyboardFocus();
                refreshServersAt = now + 5_000_000_000L;
                invalidateLayout();
            } else {
                showNotification(result.getMessage(), now);
                // 失败后重新聚焦用户名，保留文本、光标和已有选区。
                if (selectedCredential == null) account.requestKeyboardFocus();
                else login.requestKeyboardFocus();
            }
            loginResult = null;
        }
        updateServerSelection(now);
        for (var iterator = notifications.iterator(); iterator.hasNext(); ) {
            NotificationWindow notification = iterator.next();
            if (notification.update(now)) {
                iterator.remove();
                removeChild(notification);
            }
        }
        layoutNotifications();
    }

    private void setFormEnabled(boolean enabled) {
        account.setEnabled(enabled);
        password.setEnabled(enabled && selectedCredential == null);
        login.setEnabled(enabled);
    }

    private void showNotification(String message, long now) {
        NotificationWindow notification = new NotificationWindow(renderer, message, now);
        notifications.add(notification);
        add(notification);
        layoutNotifications();
    }

    /** 把背包道具注册到快捷栏栏位（0 起），同一道具只保留一个栏位。 */
    private void registerHotbarItem(int itemId, int slot) {
        int previous = gameItemBar.slotOf(itemId);
        if (previous >= 0 && previous != slot) {
            gameItemBar.clear(previous);
            hotbarSettings.setSlot(previous, 0);
        }
        gameItemBar.assign(slot, itemId);
        hotbarSettings.setSlot(slot, itemId);
        saveHotbar();
        showNotification(ClientStrings.get(1415)
                .replace("{00}", ClientStrings.get(top.totodile.pokemmo.protocol.ItemCatalog.get(itemId).nameId()))
                .replace("{01}", Integer.toString(slot + 1)), System.nanoTime());
    }

    private void unregisterHotbarItem(int itemId) {
        int slot = gameItemBar.slotOf(itemId);
        if (slot < 0) return;
        gameItemBar.clear(slot);
        hotbarSettings.setSlot(slot, 0);
        saveHotbar();
        showNotification(ClientStrings.get(1416).replace("{00}", ClientStrings.get(top.totodile.pokemmo.protocol.ItemCatalog.get(itemId).nameId())),
                System.nanoTime());
    }

    private void saveHotbar() {
        try {
            hotbarSettings.save();
        } catch (IOException exception) {
            System.getLogger(LoginScreen.class.getName()).log(System.Logger.Level.WARNING,
                    "Unable to save hotbar bindings: {0}", exception.getClass().getSimpleName());
        }
    }

    /** 激活快捷栏栏位：自行车切换骑行状态，城镇地图打开区域图，容器道具请求服务端抽取奖励。 */
    private void useHotbarItem(int slot) {
        if (world == null || worldLogoutResult != null || encounterPanel.active()) return;
        int itemId = gameItemBar.itemId(slot);
        if (itemId == HotbarSettings.SWEET_SCENT_ITEM) {
            startWildEncounter();
            return;
        }
        if (itemId == HotbarSettings.BICYCLE_ITEM) { world.toggleBicycle(); return; }
        if (itemId == HotbarSettings.TOWN_MAP_ITEM) townMapVisible(true);
        else if(top.totodile.pokemmo.protocol.ItemLoot.usable(itemId))searchCommand(top.totodile.pokemmo.protocol.SearchPackets.USE_ITEM,itemId,1);
    }

    private void startWildEncounter() {
        if (world == null || battleAppearance == null || encounterPanel.active()) return;
        world.clearKeys();
        gmMenuVisible(false); teleportMenuVisible(false); bagVisible(false); trainerCardVisible(false);
        pokedexVisible(false); pokemonSummaryVisible(false); mailVisible(false); pcBoxVisible(false);
        townMap.close(); pcDialog.close(); gameMenu.closePopup();
        if (menuConfirmation != null) menuConfirmation.closePopup();
        encounterPanel.start(clientManagement.selectedRom(0), characterCreation.assets(), battleAppearance,
                partyPanel.party(), System.nanoTime() / 1_000_000);
        if (encounterPanel.active()) raiseGameWindow(encounterPanel);
    }

    /** 首次布局居中；后续布局保留拖动位置，缩小客户区时保持窗口可见。 */
    private void layoutMovableWindow(Widget widget, int width, int height) {
        if (widget == trainerCard && !widget.isVisible()) return;
        boolean first = placedGameWindows.add(widget);
        int x = first ? getInnerX() + (getInnerWidth() - width) / 2 : widget.getX();
        int y = first ? getInnerY() + (getInnerHeight() - height) / 2 : widget.getY();
        widget.setSize(width, height);
        widget.setPosition(Math.max(getInnerX(), Math.min(x, getInnerRight() - width)),
                Math.max(getInnerY(), Math.min(y, getInnerBottom() - height)));
    }

    private void raiseGameWindow(Widget widget) {
        int index = getChildIndex(widget);
        if (index >= 0 && index != getNumChildren() - 1) moveChild(index, getNumChildren() - 1);
    }

    @Override
    protected boolean requestKeyboardFocus(Widget child) {
        boolean accepted = super.requestKeyboardFocus(child);
        if (accepted && (child == bag || child == trainerCard || child == pokedex || child == pokemonSummary || child == searchWindow || child == pcBox || child == townMap)) raiseGameWindow(child);
        return accepted;
    }

    private void changeDex(boolean unlocked) {
        if (world == null || gameConnection == null || dexChangeResult != null) return;
        dexChangeWorld = world;
        dexChangeResult = gameConnection.setDexUnlocked(unlocked);
        gmMenu.setDexBusy(true);
    }

    private void searchVisible(boolean visible) {
        if (world != null) world.clearKeys();
        if (visible && world != null) {
            gmMenuVisible(false); teleportMenuVisible(false);
            searchWindow.open(); raiseGameWindow(searchWindow);
        }
        else { searchWindow.setHardVisible(false); searchWindow.close(); }
    }

    private void searchCommand(int action,int id,int amount) {
        if(world==null || gameConnection==null || searchResult!=null)return;
        searchWorld=world;searchAdding=action==1||action==2;
        searchPokemon=action==top.totodile.pokemmo.protocol.SearchPackets.ADD_POKEMON;
        if(action==top.totodile.pokemmo.protocol.SearchPackets.INVENTORY)inventoryRequestedWorld=world;
        searchResult=gameConnection.searchCommand(action,id,amount);searchWindow.setBusy(true);bag.setBusy(true);
    }

    private void updateSearch(long now) {
        if(world!=null && gameConnection!=null && inventoryRequestedWorld!=world && searchResult==null)
            searchCommand(top.totodile.pokemmo.protocol.SearchPackets.INVENTORY,0,0);
        if(searchResult==null || !searchResult.isDone())return;
        try {
            var result=searchResult.join();
            if(world==searchWorld) {
                if(result.status()==top.totodile.pokemmo.protocol.SearchPackets.Status.SUCCESS){
                    bag.setInventory(result.inventory());
                    gameItemBar.setInventory(result.inventory());
                    if(searchPokemon)partyPanel.setParty(result.party());
                    if(searchAdding)showNotification(ClientStrings.get(9512),now);
                    for(var reward:new java.util.TreeMap<>(result.rewards()).entrySet()) {
                        String message=ClientStrings.get(9610).replace("{00}",ClientStrings.get(top.totodile.pokemmo.protocol.ItemCatalog.get(reward.getKey()).nameId()))
                                .replace("{01}",Integer.toString(reward.getValue()));
                        showNotification(message,now);
                        chatPanel.addSystemMessage(message);
                    }
                } else {
                    int message=switch(result.status()){
                        case DISABLED -> 9513;case INVALID -> 9514;case PARTY_FULL -> 9515;case STACK_FULL -> 9516;default -> 2000;
                    };
                    showNotification(ClientStrings.get(message),now);
                }
            }
        } catch(CompletionException exception){if(world==searchWorld)showNotification(ClientStrings.get(2009),now);}
        finally{searchResult=null;searchWorld=null;searchWindow.setBusy(false);bag.setBusy(false);}
    }

    private void updateDexChange(long now) {
        if (dexChangeResult == null || !dexChangeResult.isDone()) return;
        try {
            boolean unlocked = dexChangeResult.join();
            if (world == dexChangeWorld) {
                pokedex.setAllUnlocked(unlocked);
                gmMenu.setDexUnlocked(unlocked);
                nextTrainerStatsAt = 0;
            }
        } catch (CompletionException exception) {
            if (world == dexChangeWorld) showNotification(ClientStrings.get(2000), now);
        } finally {
            dexChangeResult = null;
            dexChangeWorld = null;
            gmMenu.setDexBusy(false);
        }
    }

    private void pokemonSummaryVisible(boolean visible) {
        if(world!=null)world.clearKeys();
        pokemonSummary.setHardVisible(visible);
        if(!visible)pokemonSummary.close();
    }

    private void pokedexVisible(boolean visible) {
        if (world != null) world.clearKeys();
        if (visible) { pokedex.open(); raiseGameWindow(pokedex); }
        else { pokedex.setHardVisible(false); pokedex.close(); }
    }

    private Widget topGameWindow() {
        Widget top = null;
        for (Widget widget : new Widget[]{bag, trainerCard, pokedex, pokemonSummary, searchWindow, pcBox, townMap, mailWindow})
            if (widget.isVisible() && (top == null || getChildIndex(widget) > getChildIndex(top))) top = widget;
        return top;
    }

    private boolean editingGameSearch() {
        return bag.isVisible() && bag.editingSearch() || pokedex.isVisible() && pokedex.editingSearch()
                || searchWindow.isVisible() && searchWindow.editingSearch();
    }

    private void bagVisible(boolean visible) {
        if (world != null) world.clearKeys();
        if (visible) { bag.open(); raiseGameWindow(bag); }
        else { bag.setHardVisible(false); bag.close(); }
    }

    private void openGameMenu() {
        if (world == null || worldLogoutResult != null) return;
        world.clearKeys();
        gameMenu.showMenu();
    }

    private void confirmGameMenu(int messageId, Runnable confirmed) {
        if (world == null) return;
        world.clearKeys();
        DialogLayout dialog = new DialogLayout();
        dialog.setTheme("character-delete-error");
        Label message = new Label(ClientStrings.get(messageId));
        Button ok = new Button(ClientStrings.get(52));
        Button cancel = new Button(ClientStrings.get(53));
        PopupWindow popup = new PopupWindow(this) {
            @Override protected void layout() {
                dialog.adjustSize();
                dialog.setPosition(getX() + (getWidth() - dialog.getWidth()) / 2,
                        getY() + (getHeight() - dialog.getHeight()) / 2);
            }
        };
        popup.setTheme("/rom-modal");
        popup.setCloseOnClickedOutside(false);
        ok.addCallback(() -> { popup.closePopup(); confirmed.run(); });
        cancel.addCallback(popup::closePopup);
        dialog.setHorizontalGroup(dialog.createParallelGroup().addWidget(message)
                .addGroup(dialog.createSequentialGroup().addWidget(ok).addGap(5).addWidget(cancel)));
        dialog.setVerticalGroup(dialog.createSequentialGroup().addWidget(message).addGap(8)
                .addGroup(dialog.createParallelGroup(ok, cancel)));
        popup.add(dialog);
        menuConfirmation = popup;
        if (popup.openPopup()) {
            popup.setPosition(0, 0);
            popup.setSize(getGUI().getWidth(), getGUI().getHeight());
            cancel.requestKeyboardFocus();
        }
    }

    private void logoutWorld() {
        if (world == null || worldLogoutResult != null) return;
        world.clearKeys();
        if (gameConnection == null) { finishWorldLogout(null); return; }
        gmMenuVisible(false); teleportMenuVisible(false);
        bagVisible(false); trainerCardVisible(false); pokedexVisible(false); pokemonSummaryVisible(false);
        pcDialog.close();
        mailVisible(false);
        pcBoxVisible(false);
        loading.setVisible(true);
        worldLogoutResult = gameConnection.leaveWorld();
    }

    private void updateWorldLogout(long now) {
        if (worldLogoutResult == null || !worldLogoutResult.isDone()) return;
        try { finishWorldLogout(worldLogoutResult.join()); }
        catch (CompletionException error) { showNotification(ClientStrings.get(2000), now); }
        finally { worldLogoutResult = null; loading.setVisible(false); }
    }

    private void finishWorldLogout(java.util.List<CharacterInfo> characters) {
        encounterPanel.close(); battleAppearance = null;
        chatPanel.close();
        gameMenu.closePopup();
        if (world != null) { world.close(); world = null; }
        gmMenuVisible(false); teleportMenuVisible(false);
        bagVisible(false); trainerCardVisible(false); pokedexVisible(false); pokemonSummaryVisible(false);
        pcDialog.close();
        mailVisible(false);
        pcBoxVisible(false);
        settings.setHardVisible(false);
        if (characters != null) characterSelection.setCharacters(characters);
        characterSelection.setVisible(true);
        setLoginDecorationsVisible(true);
    }

    private void updateTrainerStats(long now) {
        if (trainerStatsResult != null && trainerStatsResult.isDone()) {
            try {
                var stats = trainerStatsResult.join();
                if (world == trainerStatsWorld) {
                    worldInfo.setMoney(stats.money());
                    if (trainerCard.isVisible()) trainerCard.setStats(stats);
                }
            } catch (CompletionException error) {
                if (world == trainerStatsWorld && trainerCard.isVisible())
                    showNotification(ClientStrings.get(2000), now);
            }
            trainerStatsResult = null;
            trainerStatsWorld = null;
        }
        if (world != null && worldLogoutResult == null && gameConnection != null
                && trainerStatsResult == null && now >= nextTrainerStatsAt) {
            trainerStatsWorld = world;
            trainerStatsResult = gameConnection.trainerStats();
            nextTrainerStatsAt = now + 5_000_000_000L;
        }
    }

    private void trainerCardVisible(boolean visible) {
        if (world != null) world.clearKeys();
        trainerCard.setVisible(visible);
        if (visible) { nextTrainerStatsAt = 0; invalidateLayout(); trainerCard.requestKeyboardFocus(); raiseGameWindow(trainerCard); }
        if (!visible) trainerCard.close();
    }

    private void teleportMenuVisible(boolean visible) {
        if (world != null) world.clearKeys();
        if (visible && world != null) teleportMenu.select(world.region());
        teleportMenu.setHardVisible(visible);
        if (visible) teleportMenu.requestKeyboardFocus();
    }

    /** 城镇地图只读取当前世界的地图目录与位置；关闭释放区域图纹理，像素缓存保留。 */
    private void townMapVisible(boolean visible) {
        if (visible && world != null && world.atlas() == null) { showNotification(ClientStrings.get(9350), System.nanoTime()); return; }
        if (world != null) world.clearKeys();
        if (visible && world != null && worldLogoutResult == null) {
            gmMenuVisible(false); teleportMenuVisible(false); pcBoxVisible(false);
            layoutMovableWindow(townMap, 660, 620);
            townMap.open(world.atlas(), world.position(), world.locationName());
            raiseGameWindow(townMap);
            townMap.requestKeyboardFocus();
        } else townMap.close();
    }

    private void teleport(TeleportMenuWindow.Destination destination) {
        if (world == null || enterResult != null) return;
        if (destination.region() != world.region()) {
            int region = destination.region();
            Path rom = clientManagement.selectedRom(region == 0 ? 1 : region == 1 ? 2 : region == 2 ? 0 : region == 4 ? 5 : 3);
            if (rom == null) { showNotification(ClientStrings.get(9030), System.nanoTime()); return; }
            world.clearKeys();
            townMap.close();
            pcDialog.close();
            loading.setVisible(true);
            teleportMenu.setEnabled(false);
            worldMapResult = CompletableFuture.supplyAsync(() -> {
                try { return region == 2 ? new BlackWhiteMaps(rom) : region == 3 ? new PlatinumMaps(rom) : new FireRedAtlas(rom, region == 4); }
                catch (IOException exception) { throw new CompletionException(exception); }
            });
            enterResult = worldMapResult.thenCompose(maps -> gameConnection.teleportRegion(region,
                    destination.group(), destination.map(), destination.x(), destination.y()));
            return;
        }
        try {
            world.teleport(destination.group(), destination.map(), destination.x(), destination.y(), System.nanoTime());
        } catch (IOException exception) {
            showNotification(ClientStrings.get(9030), System.nanoTime());
        }
    }

    /** 世界内按 J 打开电脑对话；打开前释放方向键，避免对话期间继续走格。 */
    private void openPcDialog() {
        if (world == null || worldLogoutResult != null || pcDialog.isVisible()) return;
        world.clearKeys();
        pcDialog.open(characterName);
        raiseGameWindow(pcDialog);
    }

    /** 确认当前选项：电脑沿用随身电脑窗口，取消只收起对话，其余功能尚未接通。 */
    private void choosePcDialog(int option) {
        pcDialog.close();
        if (world == null || worldLogoutResult != null) return;
        if (option == PcDialog.COMPUTER) pcBoxVisible(true);
        else if (option == PcDialog.MAIL) mailVisible(true);
        else if (option != PcDialog.CANCEL) showNotification(ClientStrings.get(9350), System.nanoTime());
    }

    /** 随身电脑只读取已认证角色；断开或离图时窗口整体关闭并丢弃未完成的请求。 */
    private void mailVisible(boolean visible) {
        if(world!=null)world.clearKeys();
        if(visible)mailWindow.open();else mailWindow.close();
        if(visible){raiseGameWindow(mailWindow);invalidateLayout();}
    }

    private void pcBoxVisible(boolean visible) {        if (world != null) world.clearKeys();
        if (visible && world != null) {
            gmMenuVisible(false); teleportMenuVisible(false);
            pcBox.open();
            raiseGameWindow(pcBox);
        } else pcBox.close();
    }

    private CompletableFuture<top.totodile.pokemmo.protocol.PcPackets.PartyState> pcParty() {
        if (world == null || gameConnection == null || worldLogoutResult != null)
            return CompletableFuture.failedFuture(new IOException("No active world"));
        return gameConnection.pcParty();
    }

    private CompletableFuture<top.totodile.pokemmo.protocol.PcPackets.BoxPage> pcBoxPage(int box, int start) {
        if (world == null || gameConnection == null || worldLogoutResult != null)
            return CompletableFuture.failedFuture(new IOException("No active world"));
        return gameConnection.pcBox(box, start);
    }

    private CompletableFuture<top.totodile.pokemmo.protocol.PcPackets.Status> pcMove(
            top.totodile.pokemmo.protocol.PcPackets.Move move) {
        if (world == null || gameConnection == null || worldLogoutResult != null)
            return CompletableFuture.failedFuture(new IOException("No active world"));
        return gameConnection.pcMove(move);
    }

    private CompletableFuture<top.totodile.pokemmo.protocol.PcPackets.Status> pcCreateBox() {
        if (world == null || gameConnection == null || worldLogoutResult != null)
            return CompletableFuture.failedFuture(new IOException("No active world"));
        return gameConnection.pcCreateBox();
    }

    private void gmMenuVisible(boolean visible) {
        if (visible) {
            searchVisible(false);
            pcBoxVisible(false);
            trainerCardVisible(false);
            bagVisible(false);
            pokedexVisible(false); pokemonSummaryVisible(false);
            teleportMenu.setHardVisible(false);
            gmMenu.setTeleportEnabled(world != null);
            gmMenu.setComputerEnabled(world != null);
            gmMenu.setDexUnlocked(pokedex.isUnlocked());
            gmMenu.setDexBusy(dexChangeResult != null);
            gmMenu.setNoclip(world != null && world.noclip());
            gmMenu.setSpeedMultiplier(world == null ? 1 : world.speedMultiplier());
        }
        if (world != null) world.clearKeys();
        gmMenu.setHardVisible(visible);
        if (visible) gmMenu.requestKeyboardFocus();
    }

    public void attachInput(long window) {
        this.window = window;
        music.setFocused(GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE);
        if (textCursor != 0) GLFW.glfwDestroyCursor(textCursor);
        textCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
        GLFW.glfwSetCursorPosCallback(window, (w, x, y) -> {
            int nextX = (int) x;
            int nextY = (int) y;
            // 相同位置的重复通知不属于移动，避免不断重置悬停提示计时。
            if (nextX != mouseX || nextY != mouseY) {
                mouseX = nextX;
                mouseY = nextY;
                gui.handleMouse(nextX, nextY, -1, false);
            }
            // 隐藏的输入框仍保留坐标，管理窗口或弹出菜单覆盖时不使用编辑指针。
            boolean overInput = loginWindow.isVisible() && !loggingIn && !gui.hasOpenPopups()
                    && (account.isInside((int) x, (int) y)
                    || password.isInside((int) x, (int) y));
            long cursor = overInput ? textCursor : 0;
            if (cursor != activeCursor) {
                activeCursor = cursor;
                GLFW.glfwSetCursor(w, cursor);
            }
        });
        GLFW.glfwSetMouseButtonCallback(window, (w, button, action, mods) -> {
            double[] x = new double[1], y = new double[1];
            GLFW.glfwGetCursorPos(w, x, y);
            // 点击对话气泡与选项之外的位置视为取消，不再传递给下层窗口。
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS && pcDialog.isVisible()
                    && !pcDialog.hits((int) x[0], (int) y[0])) {
                pcDialog.close();
                return;
            }
            if (button <= 2) gui.handleMouse((int) x[0], (int) y[0], button, action != 0);
        });
        GLFW.glfwSetScrollCallback(window, (w, xOffset, yOffset) -> {
            if (yOffset != 0) gui.handleMouseWheel((int) Math.signum(yOffset));
        });
        GLFW.glfwSetCharCallback(window, (w, codepoint) -> {
            if ((world == null || bag.isVisible() || pokedex.isVisible() || searchWindow.isVisible() || settings.isVisible()) && Character.isBmpCodePoint(codepoint)) {
                gui.handleKey(Event.KEY_NONE, (char) codepoint, true);
                gui.handleKey(Event.KEY_NONE, (char) codepoint, false);
            }
        });
        GLFW.glfwSetKeyCallback(window, (w, key, scan, action, mods) -> {
            if (encounterPanel.active()) {
                int translated = GlfwKeyMap.translate(key);
                if (translated != Event.KEY_NONE) gui.handleKey(translated, Event.CHAR_NONE, action != GLFW.GLFW_RELEASE);
                return;
            }
            if (world != null && (worldLogoutResult != null || enterResult != null)) return;
            if(world!=null && (searchWindow.isVisible() || bag.isVisible()) && gui.hasOpenPopups()){
                int translated=GlfwKeyMap.translate(key);
                if(translated!=Event.KEY_NONE)gui.handleKey(translated,Event.CHAR_NONE,action!=0);
                return;
            }
            if (world != null && (partyPanel.menuOpen() || gameMenu.isOpen() || menuConfirmation != null && menuConfirmation.isOpen() || settings.isVisible())) {
                if (settings.isVisible() && key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) closeSettings();
                else {
                    int translated = GlfwKeyMap.translate(key);
                    if (translated != Event.KEY_NONE) gui.handleKey(translated, Event.CHAR_NONE, action != 0);
                }
                return;
            }
            if (world != null && pcDialog.isVisible()) {
                // 对话期间键盘只服务于选项，方向键切换，J 或回车确认。
                if (action != GLFW.GLFW_RELEASE) {
                    if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) pcDialog.close();
                    else if (key == GLFW.GLFW_KEY_UP) pcDialog.move(-1);
                    else if (key == GLFW.GLFW_KEY_DOWN) pcDialog.move(1);
                    else if (action == GLFW.GLFW_PRESS && (key == GLFW.GLFW_KEY_J
                            || key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_SPACE)) {
                        choosePcDialog(pcDialog.selected());
                    }
                }
                return;
            }
            if (world != null && !gmMenu.isVisible() && !teleportMenu.isVisible()
                    && action == GLFW.GLFW_PRESS && !editingGameSearch()) {
                if (key == GLFW.GLFW_KEY_J && topGameWindow() == null) { openPcDialog(); return; }
                if (key == GLFW.GLFW_KEY_B) { bagVisible(!bag.isVisible()); return; }
                if (key == GLFW.GLFW_KEY_C) { trainerCardVisible(!trainerCard.isVisible()); return; }
                if (key == GLFW.GLFW_KEY_N) { pokedexVisible(!pokedex.isVisible()); return; }
                // 1-9 全部交给快捷栏：栏位 2 骑/取消自行车，栏位 5 打开城镇地图。
                if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9) {
                    gameItemBar.activate(key - GLFW.GLFW_KEY_1);
                    return;
                }
            }
            if (world != null && topGameWindow() != null) {
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                    Widget top = topGameWindow();
                    if (top == pokedex) { if (!pokedex.backToList()) pokedexVisible(false); }
                    else if (top == pokemonSummary) pokemonSummaryVisible(false);
                    else if (top == trainerCard) trainerCardVisible(false);
                    else if (top == searchWindow) searchVisible(false);
                    else if (top == mailWindow) mailVisible(false);
                    else if (top == pcBox) { if (!pcBox.cancelPick()) pcBoxVisible(false); }
                    else if (top == townMap) townMapVisible(false);
                    else bagVisible(false);
                } else if (key == GLFW.GLFW_KEY_TAB && action == GLFW.GLFW_PRESS && (mods & GLFW.GLFW_MOD_CONTROL) != 0) {
                    boolean backwards = (mods & GLFW.GLFW_MOD_SHIFT) != 0;
                    if (topGameWindow() == bag) bag.nextPocket(backwards);
                } else {
                    int translated = GlfwKeyMap.translate(key);
                    if (translated != Event.KEY_NONE) gui.handleKey(translated, Event.CHAR_NONE, action != 0);
                }
                return;
            }
            if (world != null && (gmMenu.isVisible() || teleportMenu.isVisible())) {
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                    if (teleportMenu.isVisible()) teleportMenuVisible(false);
                    else gmMenuVisible(false);
                }
                else {
                    int translated = GlfwKeyMap.translate(key);
                    if (translated != Event.KEY_NONE) gui.handleKey(translated, Event.CHAR_NONE, action != 0);
                }
                return;
            }
            if (world != null) {
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                    openGameMenu();
                } else if (action != GLFW.GLFW_REPEAT) world.key(key, action != GLFW.GLFW_RELEASE);
                return;
            }
            if (action == GLFW.GLFW_REPEAT) return;
            if (clientManagement.isVisible() || modManager.isVisible() || settings.isVisible()
                    || serverSelection.isVisible() || characterSelection.isVisible()
                    || characterCreation.isVisible() || loading.isVisible()) {
                int translated = GlfwKeyMap.translate(key);
                if (translated != Event.KEY_NONE) gui.handleKey(translated, Event.CHAR_NONE, action != 0);
                return;
            }
            if (key == GLFW.GLFW_KEY_TAB) {
                if (action == GLFW.GLFW_PRESS) {
                    Widget[] order = selectedCredential == null ? new Widget[]{account, password, remember, login}
                            : new Widget[]{savedAccount, clearCredential, login};
                    int index = 0;
                    while (index < order.length && !order[index].hasKeyboardFocus()) index++;
                    if (index == order.length) return;
                    int direction = (mods & GLFW.GLFW_MOD_SHIFT) == 0 ? 1 : -1;
                    for (int i = 0; i < order.length; i++) {
                        index = Math.floorMod(index + direction, order.length);
                        if (order[index].isEnabled()) {
                            order[index].requestKeyboardFocus();
                            break;
                        }
                    }
                }
                return;
            }
            int translated = GlfwKeyMap.translate(key);
            if (translated != Event.KEY_NONE) gui.handleKey(translated, Event.CHAR_NONE, action != 0);
        });
        GLFW.glfwSetWindowFocusCallback(window, (w, focused) -> {
            music.setFocused(focused);
            if (!focused) {
                if (world != null) world.clearKeys();
                gui.clearKeyboardState();
                gui.clearMouseState();
            }
        });
        if (selectedCredential == null) account.requestKeyboardFocus();
        else login.requestKeyboardFocus();
    }

    @Override
    public void close() {
        encounterPanel.close();
        mailWindow.close();
        chatPanel.close();
        if (world != null) world.close();
        if (worldMapResult != null) worldMapResult.cancel(true);
        gameHud.close();
        partyPanel.close();
        gameItemBar.close();
        townMap.close();
        pokedex.close();
        pokemonSummary.close();
        bag.close();
        searchWindow.close();
        pcBox.close();
        trainerCard.close();
        characterCreation.close();
        characterSelection.close();
        modManager.close();
        music.close();
        connection.close();
        if (gameConnection != null) gameConnection.close();
        if (sceneResult != null) sceneResult.cancel(true);
        if (scene != null) scene.close();
        // ThemeManager 销毁缓存上下文，连同主题加载的纹理一起释放。
        theme.destroy();
        if (textCursor != 0) {
            GLFW.glfwDestroyCursor(textCursor);
            textCursor = 0;
        }
    }
}
