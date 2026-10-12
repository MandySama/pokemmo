package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.theme.ThemeManager;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import org.lwjgl.glfw.GLFW;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.SavedCredentials;
import top.totodile.pokemmo.audio.LoginMusic;
import top.totodile.pokemmo.net.LoginConnection;
import top.totodile.pokemmo.net.GameConnection;
import top.totodile.pokemmo.protocol.GameEndpoint;
import top.totodile.pokemmo.protocol.GameServerInfo;
import top.totodile.pokemmo.rom.BlackWhiteLoginScene;

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
    private final Label fpsCounter = new Label("0.00");
    private final Label announcementTitle = new Label(ClientStrings.get(1004));
    private final List<Label> announcements = new ArrayList<>();
    private long fpsStartedAt = System.nanoTime();
    private int fpsFrames;
    private final List<Button> menuButtons = new ArrayList<>();
    private final ClientManagementWindow clientManagement = new ClientManagementWindow(
            this::closeClientManagement, this::refreshTexts, message -> showNotification(message, System.nanoTime()));
    private final ModManagerWindow modManager = new ModManagerWindow(this::closeModManager);
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
    private CompletableFuture<GameEndpoint> selectionResult;
    private CompletableFuture<GameConnection.Result> gameResult;
    private CompletableFuture<List<GameServerInfo>> serverListResult;
    private GameConnection gameConnection;
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
        serverSelection.setVisible(false);
        add(serverSelection);
        fpsCounter.setTheme("fpscounter");
        fpsCounter.setCanAcceptKeyboardFocus(false);
        add(fpsCounter);
        // GLFW 回调通过 attachInput 接入；GUI 负责控件树布局和绘制。
        gui = new GUI(this, renderer, null);
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
        if (scene == null) {
            background.draw(getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight(),
                    System.nanoTime() - startedAt);
        }
    }

    @Override
    protected void layout() {
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
        clientManagement.setSize(528, 300);
        clientManagement.setPosition(getInnerX() + (getInnerWidth() - clientManagement.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - clientManagement.getHeight()) / 2);
        modManager.adjustSize();
        modManager.setPosition(getInnerX() + (getInnerWidth() - modManager.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - modManager.getHeight()) / 2);
        settings.setSize(585, 498);
        settings.setPosition(getInnerX() + (getInnerWidth() - settings.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - settings.getHeight()) / 2);
        serverSelection.adjustSize();
        serverSelection.setPosition(getInnerX() + (getInnerWidth() - serverSelection.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - serverSelection.getHeight()) / 2);
        for (int i = 0; i < menuButtons.size(); i++) {
            Button button = menuButtons.get(i);
            button.setSize(200, 26);
            button.setPosition(getInnerRight() - 204,
                    getInnerBottom() - 4 - (menuButtons.size() - i) * 26);
        }
        layoutNotifications();
    }

    private void openClientManagement() {
        settings.setHardVisible(false);
        modManager.setHardVisible(false);
        loginWindow.setVisible(false);
        serverSelection.setVisible(false);
        clientManagement.setEnabled(true);
        clientManagement.setHardVisible(true);
        clientManagement.requestKeyboardFocus();
    }

    private void openModManager() {
        settings.setHardVisible(false);
        clientManagement.setHardVisible(false);
        loginWindow.setVisible(false);
        serverSelection.setVisible(false);
        modManager.setEnabled(true);
        modManager.setHardVisible(true);
        modManager.requestKeyboardFocus();
    }

    private void closeModManager() {
        modManager.setEnabled(false);
        modManager.setHardVisible(false);
        restoreMainWindow();
    }

    private void openSettings() {
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
        // 返回登录页时不恢复输入框焦点；启动和登录失败的焦点分别由对应流程处理。
        clientManagement.setEnabled(false);
        clientManagement.setHardVisible(false);
        restoreMainWindow();
        romInfo.setVisible(true);
        for (Button button : menuButtons) button.setVisible(true);
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
        serverSelection.setVisible(authenticated);
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
        updateScene(now);
        if (scene != null) {
            try {
                scene.draw(width, height, (now - sceneStartedAt) / 1_000_000_000.0);
            } catch (RuntimeException exception) {
                scene.close();
                scene = null;
                reportSceneFailure(exception);
            }
        }
        gui.update();
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
        romInfo.setText(ClientStrings.get(1170) + "\nNDS: " + (scene == null ? "--" : scene.title()) + "\nGBA: --");
        invalidateLayout();
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
        selectionResult = connection.select(serverId);
    }

    private void backToLogin() {
        authenticated = false;
        connection.disconnect();
        if (gameConnection != null) gameConnection.close();
        gameConnection = null;
        gameConnected = false;
        selectionResult = null;
        gameResult = null;
        serverListResult = null;
        queuedServerId = null;
        serverSelection.setVisible(false);
        serverSelection.resetMessage();
        settings.setHardVisible(false);
        modManager.setHardVisible(false);
        clientManagement.setHardVisible(false);
        loginWindow.setVisible(true);
        invalidateLayout();
    }

    private void updateServerSelection(long now) {
        if (!authenticated) return;
        if (selectionResult != null && selectionResult.isDone()) {
            try {
                GameEndpoint endpoint = selectionResult.join();
                gameConnection = new GameConnection();
                gameResult = gameConnection.connect(endpoint);
            } catch (CompletionException | java.util.concurrent.CancellationException exception) {
                serverSelection.resetMessage();
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
                serverSelection.showConnected(selectedServerName);
                connection.disconnect();
            } else {
                gameConnection.close();
                gameConnection = null;
                serverSelection.resetMessage();
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
        savedAccount.setEnabled(enabled);
        clearCredential.setEnabled(enabled);
        login.setEnabled(enabled);
    }

    private void showNotification(String message, long now) {
        NotificationWindow notification = new NotificationWindow(renderer, message, now);
        notifications.add(notification);
        add(notification);
        layoutNotifications();
    }

    /** GLFW 使用不同的键码，转换后交给 TWL 处理焦点和编辑行为。 */
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
            if (button <= 2) gui.handleMouse((int) x[0], (int) y[0], button, action != 0);
        });
        GLFW.glfwSetScrollCallback(window, (w, xOffset, yOffset) -> {
            if (yOffset != 0) gui.handleMouseWheel((int) Math.signum(yOffset));
        });
        GLFW.glfwSetCharCallback(window, (w, codepoint) -> {
            if (Character.isBmpCodePoint(codepoint)) {
                gui.handleKey(Event.KEY_NONE, (char) codepoint, true);
                gui.handleKey(Event.KEY_NONE, (char) codepoint, false);
            }
        });
        GLFW.glfwSetKeyCallback(window, (w, key, scan, action, mods) -> {
            if (action == GLFW.GLFW_REPEAT) return;
            if (clientManagement.isVisible() || modManager.isVisible() || settings.isVisible()
                    || serverSelection.isVisible()) {
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
                gui.clearKeyboardState();
                gui.clearMouseState();
            }
        });
        if (selectedCredential == null) account.requestKeyboardFocus();
        else login.requestKeyboardFocus();
    }

    @Override
    public void close() {
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
