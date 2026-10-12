package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.theme.ThemeManager;
import org.lwjgl.glfw.GLFW;
import top.totodile.pokemmo.ClientFiles;
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
    private final EditField account = new UsernameEditField();
    private final EditField password = new LoginEditField();
    private final Label accountLabel = new Label("用户名:");
    private final Label passwordLabel = new Label("密 码:");
    private final Button login = new Button("登录");
    private final ToggleButton remember = new ToggleButton();
    private final Label rememberLabel = new Label("记住我");
    private final ResizableFrame loginWindow = new ResizableFrame();
    private final DialogLayout form = new DialogLayout();
    private final List<NotificationWindow> notifications = new ArrayList<>();
    private final Widget logo = new Widget();
    private final Label romInfo = new Label("已载入ROM:\nNDS: --\nGBA: --");
    private final List<Button> menuButtons = new ArrayList<>();
    private final ClientManagementWindow clientManagement = new ClientManagementWindow(this::closeClientManagement);
    private final GlfwTwlRenderer renderer = new GlfwTwlRenderer();
    private final GUI gui;
    private final ThemeManager theme;
    private final ScrollingLoginBackground background;
    private final long startedAt = System.nanoTime();
    private final LoginConnection connection = new LoginConnection();
    private CompletableFuture<LoginConnection.Failure> loginResult;
    private boolean loggingIn;
    private long textCursor;

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
        loginWindow.setTitle("登录");
        loginWindow.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        loginWindow.setDraggable(false);
        loginWindow.add(form);
        add(loginWindow);
        romInfo.setTheme("label");
        add(romInfo);
        for (String text : new String[]{
                "修复客户端",
                "客户端管理",
                "MOD管理",
                "注册",
                "忘记用户名",
                "忘记密码",
                "申请帮助",
                "设置",
                "关闭",
        }) {
            Button button = new Button(text);
            button.setTheme("menu-button");
            // 按钮功能在对应窗口或模块接入时添加。
            button.setCanAcceptKeyboardFocus(false);
            if (text.equals("客户端管理")) {
                button.addCallback(this::openClientManagement);
            }
            menuButtons.add(button);
            add(button);
        }
        clientManagement.setHardVisible(false);
        add(clientManagement);
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
        for (int i = 0; i < menuButtons.size(); i++) {
            Button button = menuButtons.get(i);
            button.setSize(200, 26);
            button.setPosition(getInnerRight() - 204,
                    getInnerBottom() - 4 - (menuButtons.size() - i) * 26);
        }
        layoutNotifications();
    }

    private void openClientManagement() {
        loginWindow.setVisible(false);
        clientManagement.setEnabled(true);
        clientManagement.setHardVisible(true);
        clientManagement.requestKeyboardFocus();
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
        login.setText("登录中...");
        setFormEnabled(false);
        loginResult = connection.connect();
    }

    /** 网络线程只返回结果，TWL 控件仍在绘制线程更新。 */
    private void updateLoginState(long now) {
        if (loggingIn && loginResult.isDone()) {
            loggingIn = false;
            login.setText("登录");
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
            gui.handleMouse((int) x, (int) y, -1, false);
            boolean overInput = !loggingIn && (account.isInside((int) x, (int) y)
                    || password.isInside((int) x, (int) y));
            GLFW.glfwSetCursor(w, overInput ? textCursor : 0);
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
            if (clientManagement.isVisible()) {
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
