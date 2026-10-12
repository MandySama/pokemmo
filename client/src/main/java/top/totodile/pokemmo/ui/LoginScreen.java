package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.theme.ThemeManager;
import org.lwjgl.glfw.GLFW;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.net.LoginConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    private final List<NotificationWindow> notifications = new ArrayList<>();
    private final Widget logo = new Widget();
    private final Label romInfo = new Label(ClientStrings.get(1170) + "\nNDS: --\nGBA: --");
    private final List<Button> menuButtons = new ArrayList<>();
    private final ClientManagementWindow clientManagement = new ClientManagementWindow(
            this::closeClientManagement, this::refreshTexts);
    private final ModManagerWindow modManager = new ModManagerWindow(this::closeModManager);
    private final GameSettingsWindow settings = new GameSettingsWindow(this::closeSettings);
    private final GlfwTwlRenderer renderer = new GlfwTwlRenderer();
    private final GUI gui;
    private final ThemeManager theme;
    private final ScrollingLoginBackground background;
    private final long startedAt = System.nanoTime();
    private final LoginConnection connection = new LoginConnection();
    private CompletableFuture<LoginConnection.Failure> loginResult;
    private boolean loggingIn;
    private long textCursor;
    private long activeCursor;
    private int mouseX = Integer.MIN_VALUE;
    private int mouseY = Integer.MIN_VALUE;

    public LoginScreen() throws IOException {
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
        // 标签和输入框分为两列，底部一行放记住我和登录。
        form.setHorizontalGroup(form.createParallelGroup()
                .addGroup(form.createSequentialGroup()
                        .addGroup(form.createParallelGroup(accountLabel, passwordLabel))
                        .addGroup(form.createParallelGroup(account, password)))
                .addGroup(form.createSequentialGroup(remember, rememberLabel)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addWidget(login)));
        form.setVerticalGroup(form.createSequentialGroup()
                .addGroup(form.createParallelGroup(accountLabel, account))
                .addGroup(form.createParallelGroup(passwordLabel, password))
                .addGroup(form.createParallelGroup(remember, rememberLabel, login)));
        loginWindow.setTheme("login-window");
        loginWindow.setTitle(ClientStrings.get(1000));
        loginWindow.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        loginWindow.setDraggable(false);
        loginWindow.add(form);
        add(loginWindow);
        romInfo.setTheme("label");
        add(romInfo);
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
        // GLFW 回调通过 attachInput 接入；GUI 负责控件树布局和绘制。
        gui = new GUI(this, renderer, null);
        // 主题从外部 data/themes/default 加载，图片 URL 相对于主题 XML 解析。
        theme = ThemeManager.createThemeManager(
                ClientFiles.data("themes/default/theme.xml").toUri().toURL(), renderer);
        try {
            // 复用主题缓存，关闭登录界面时统一释放 Logo 和背景纹理。
            background = new ScrollingLoginBackground(renderer);
            gui.applyTheme(theme);
        } catch (IOException | RuntimeException exception) {
            theme.destroy();
            throw exception;
        }
    }

    private void refreshTexts() {
        modManager.refreshTexts();
        accountLabel.setText(ClientStrings.get(1001));
        passwordLabel.setText(ClientStrings.get(1002));
        rememberLabel.setText(ClientStrings.get(1025));
        loginWindow.setTitle(ClientStrings.get(1000));
        login.setText(ClientStrings.get(loggingIn ? 1003 : 1000));
        romInfo.setText(ClientStrings.get(1170) + "\nNDS: --\nGBA: --");
        for (int i = 0; i < menuButtons.size(); i++) {
            menuButtons.get(i).setText(ClientStrings.get(MENU_STRING_IDS[i]));
        }
    }

    @Override
    protected void paintWidget(GUI gui) {
        // TWL 先绘制当前控件再绘制子控件，表单和 Logo 叠加在背景上方。
        background.draw(getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight(),
                System.nanoTime() - startedAt);
    }

    @Override
    protected void layout() {
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
        clientManagement.setSize(528, 300);
        clientManagement.setPosition(getInnerX() + (getInnerWidth() - clientManagement.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - clientManagement.getHeight()) / 2);
        modManager.adjustSize();
        modManager.setPosition(getInnerX() + (getInnerWidth() - modManager.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - modManager.getHeight()) / 2);
        settings.setSize(585, 498);
        settings.setPosition(getInnerX() + (getInnerWidth() - settings.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - settings.getHeight()) / 2);
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
        clientManagement.setEnabled(true);
        clientManagement.setHardVisible(true);
        clientManagement.requestKeyboardFocus();
    }

    private void openModManager() {
        settings.setHardVisible(false);
        clientManagement.setHardVisible(false);
        loginWindow.setVisible(false);
        modManager.setEnabled(true);
        modManager.setHardVisible(true);
        modManager.requestKeyboardFocus();
    }

    private void closeModManager() {
        modManager.setEnabled(false);
        modManager.setHardVisible(false);
        loginWindow.setVisible(true);
    }

    private void openSettings() {
        clientManagement.setHardVisible(false);
        modManager.setHardVisible(false);
        loginWindow.setVisible(false);
        settings.setEnabled(true);
        settings.setHardVisible(true);
        settings.requestKeyboardFocus();
    }

    private void closeSettings() {
        settings.setEnabled(false);
        settings.setHardVisible(false);
        loginWindow.setVisible(true);
    }

    private void closeClientManagement() {
        // 返回登录页时不恢复输入框焦点；启动和登录失败的焦点分别由对应流程处理。
        clientManagement.setEnabled(false);
        clientManagement.setHardVisible(false);
        loginWindow.setVisible(true);
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

    /** 每帧先同步尺寸，再让 TWL 更新时间、校验布局并绘制控件。 */
    public void render(int width, int height, int framebufferWidth, int framebufferHeight) {
        renderer.resize(width, height, framebufferWidth, framebufferHeight);
        updateLoginState(System.nanoTime());
        gui.update();
    }

    private void submit() {
        if (loggingIn) return;
        loggingIn = true;
        login.setText(ClientStrings.get(1003));
        setFormEnabled(false);
        loginResult = connection.connect();
    }

    /** 网络线程只返回结果，TWL 控件仍在绘制线程更新。 */
    private void updateLoginState(long now) {
        if (loggingIn && loginResult.isDone()) {
            loggingIn = false;
            login.setText(ClientStrings.get(1000));
            setFormEnabled(true);
            showNotification(loginResult.join().getMessage(), now);
            loginResult = null;
            // 失败后重新聚焦用户名，保留文本、光标和已有选区。
            account.requestKeyboardFocus();
        }
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
        password.setEnabled(enabled);
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
            if (clientManagement.isVisible() || modManager.isVisible() || settings.isVisible()) {
                int translated = GlfwKeyMap.translate(key);
                if (translated != Event.KEY_NONE) gui.handleKey(translated, Event.CHAR_NONE, action != 0);
                return;
            }
            if (key == GLFW.GLFW_KEY_TAB) {
                if (action == GLFW.GLFW_PRESS) {
                    Widget[] order = {account, password, remember, login};
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
            if (!focused) {
                gui.clearKeyboardState();
                gui.clearMouseState();
            }
        });
        account.requestKeyboardFocus();
    }

    @Override
    public void close() {
        connection.close();
        // ThemeManager 销毁缓存上下文，连同主题加载的纹理一起释放。
        theme.destroy();
        if (textCursor != 0) {
            GLFW.glfwDestroyCursor(textCursor);
            textCursor = 0;
        }
    }
}
