package top.totodile.pokemmo.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.theme.ThemeManager;
import java.io.IOException;
import java.net.*;
import java.util.function.Function;
import java.util.function.LongSupplier;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

/** 客户端公共界面、页面导航及 TWL 渲染和输入生命周期。 */
final class ClientUi implements Disposable {
    private final GdxTwlRenderer renderer;
    private final GUI gui;
    private final ThemeManager theme;
    private final ClientResources resources;
    private final boolean ownsResources;
    private final Widget logo = new Widget();
    private final java.util.Map<Integer,de.matthiasmann.twl.renderer.Image> itemIcons = new java.util.HashMap<>();
    private final LoginPanel loginPanel;
    private final Widget root;
    private final LoginController controller;
    private final ServerSelectionPanel serverFrame;
    private final CharacterSelectionPanel characterPage;
    private final Label romInfo;
    private final AnnouncementsPanel announcements;
    private final DesktopMenu desktopMenu;
    private final MobileStatusIcons mobileStatus;
    private final MobileSideMenu mobileMenu;
    private final ClientManagementWindow clientManagement;
    private final SettingsWindow settings;
    private final ExitConfirmation exitConfirmation;
    private final Label fps = new Label("0.00");
    private final FrameRateCounter frameRate = new FrameRateCounter(System::nanoTime);
    private final InputAdapter input;
    private final ClientStrings strings;
    private final boolean mobile;
    private final LongSupplier notificationClock;
    private int offsetX, offsetY;

    ClientUi(Viewport viewport, Function<String, FileHandle> assets, boolean mobile,
               SavedAccounts savedAccounts, LoginController controller) {
        this(viewport, assets, mobile, savedAccounts, () -> System.nanoTime() / 1_000_000L, controller);
    }

    ClientUi(Viewport viewport, Function<String, FileHandle> assets, boolean mobile,
               SavedAccounts savedAccounts, LongSupplier notificationClock, LoginController controller) {
        this(viewport, assets, mobile, savedAccounts, notificationClock, controller, () -> -1);
    }

    ClientUi(Viewport viewport, Function<String, FileHandle> assets, boolean mobile,
               SavedAccounts savedAccounts, LongSupplier notificationClock, LoginController controller,
               java.util.function.IntSupplier batteryPercent) {
        this(viewport, assets, mobile, savedAccounts, notificationClock, controller, batteryPercent, () -> Gdx.app.exit());
    }

    ClientUi(Viewport viewport, Function<String, FileHandle> assets, boolean mobile,
             SavedAccounts savedAccounts, LongSupplier notificationClock, LoginController controller,
             java.util.function.IntSupplier batteryPercent, Runnable exit) {
        this.controller = controller;
        this.mobile = mobile;
        this.notificationClock = notificationClock;
        ownsResources = !(assets instanceof ClientResources);
        resources = assets instanceof ClientResources ? (ClientResources) assets
                : new ClientResources(assets, savedAccounts.directory(), mobile);
        strings = resources.strings();
        renderer = new GdxTwlRenderer(viewport, strings.typeface());
        serverFrame = new ServerSelectionPanel(strings, controller);
        characterPage = new CharacterSelectionPanel(strings, controller);
        // 参考登录页 Nq：当前尚未加载 ROM，两个平台列表均显示 --。
        romInfo = new Label(strings.get(1170) + "\nNDS: --\nGBA: --");
        announcements = new AnnouncementsPanel(strings, mobile);
        desktopMenu = mobile ? null : new DesktopMenu(strings);
        mobileStatus = mobile ? new MobileStatusIcons(batteryPercent) : null;
        mobileMenu = mobile ? new MobileSideMenu(strings, mobileStatus, notificationClock, this::itemIcon) : null;
        fps.setTheme("/fpscounter");
        logo.setTheme("logo");
        root = new Widget() {
            @Override protected void layout() {
                logo.setSize(484,143);
                logo.setPosition(getInnerX()+(getInnerWidth()-484)/2,getInnerY()+(getInnerHeight()-143)/2-200);
                romInfo.adjustSize();
                romInfo.setPosition(getInnerX() + 3, getInnerY() + 3);
                fps.adjustSize();
                fps.setPosition(mobile ? getInnerX() : getInnerRight() - fps.getWidth(),
                        mobile ? getInnerBottom() - fps.getHeight() : getInnerY());
                announcements.adjustSize();
                announcements.setPosition(getInnerX(), getInnerBottom() - announcements.getHeight()
                        - (mobile ? fps.getHeight() : 0));
                if (desktopMenu != null) {
                    desktopMenu.adjustSize();
                    desktopMenu.setPosition(getInnerRight() - desktopMenu.getWidth() - 4,
                            getInnerBottom() - desktopMenu.getHeight() - 4);
                }
                if (mobileStatus != null) {
                    mobileStatus.adjustSize();
                    mobileStatus.setPosition(getInnerRight() - mobileStatus.getWidth() - 5, getInnerY() + 5);
                }
                if (mobileMenu != null) mobileMenu.position();
                if (loginPanel.isVisible()) {
                    loginPanel.adjustSize();
                    loginPanel.setPosition((getInnerWidth()-loginPanel.getWidth())/2+getInnerX()+offsetX,
                        (getInnerHeight()-loginPanel.getHeight())/2+getInnerY()-offsetY);
                }
                if (serverFrame.isVisible()) {
                    serverFrame.adjustSize();
                    serverFrame.setPosition(getInnerX() + (getInnerWidth()-serverFrame.getWidth())/2,
                            getInnerY() + (getInnerHeight()-serverFrame.getHeight())/2);
                }
                characterPage.setPosition(getInnerX(), getInnerY());
                characterPage.setSize(getInnerWidth(), getInnerHeight());
                loginPanel.layoutNotifications();
            }
        };
        loginPanel = new LoginPanel(root, strings, mobile, savedAccounts, notificationClock, controller);
        if (mobileStatus != null) mobileStatus.onMenu(() -> {
            loginPanel.hideKeyboard();
            mobileMenu.open();
        });
        root.setTheme("logingui");
        root.add(logo);
        root.add(loginPanel);
        root.add(serverFrame);
        root.add(characterPage);
        root.add(romInfo);
        root.add(announcements);
        if (desktopMenu != null) root.add(desktopMenu);
        root.add(fps);
        if (mobileStatus != null) root.add(mobileStatus);
        if (mobileMenu != null) root.add(mobileMenu);
        gui = new GUI(root, renderer, null);
        Runnable restoreFocus = this::restoreWindowFocus;
        java.util.function.Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground =
                image -> renderer.drawScreenBackground(image, root.getAnimationState());
        clientManagement = new ClientManagementWindow(root, strings, mobile, restoreFocus, mobileBackground);
        settings = new SettingsWindow(root, strings, mobile, restoreFocus, mobileBackground);
        exitConfirmation = mobile ? new ExitConfirmation(root, strings, exit, restoreFocus, mobileBackground) : null;
        Runnable requestExit = () -> {
            loginPanel.hideKeyboard();
            if (mobile) exitConfirmation.open(); else exit.run();
        };
        if (desktopMenu != null) desktopMenu.onExit(requestExit);
        if (mobileMenu != null) mobileMenu.onExit(requestExit);
        Runnable openManagement = () -> { loginPanel.hideKeyboard(); clientManagement.open(); };
        if (desktopMenu != null) desktopMenu.onClientManagement(openManagement);
        if (mobileMenu != null) mobileMenu.onClientManagement(openManagement);
        Runnable openSettings = () -> { loginPanel.hideKeyboard(); settings.open(); };
        if (desktopMenu != null) desktopMenu.onSettings(openSettings);
        if (mobileMenu != null) mobileMenu.onSettings(openSettings);
        theme = loadTheme(resources.selectedTheme());
        gui.applyTheme(theme);
        input = new InputAdapter() {
            private boolean pointer(int x,int y,int button,boolean pressed) {
                Vector2 point=viewport.unproject(new Vector2(x,y));
                return gui.handleMouse(Math.round(point.x), renderer.getHeight()-Math.round(point.y),button,pressed);
            }
            @Override public boolean touchDown(int x,int y,int pointer,int button){
                if (pointer != 0) return false;
                Vector2 point = viewport.unproject(new Vector2(x,y));
                if (exitConfirmation == null || !exitConfirmation.isOpen()) settings.touchDown(Math.round(point.x), renderer.getHeight()-Math.round(point.y), x, y, button);
                return pointer(x,y,button,true);
            }
            @Override public boolean touchUp(int x,int y,int pointer,int button){
                if (pointer != 0) return false;
                if (settings.touchUp(x,y,button)) return true;
                return pointer(x,y,button,false);
            }
            @Override public boolean touchDragged(int x,int y,int pointer){
                if (pointer != 0) return false;
                if (settings.touchDragged(x,y)) return true;
                return pointer(x,y,-1,false);
            }
            @Override public boolean touchCancelled(int x,int y,int pointer,int button){
                if (pointer != 0 || !settings.isOpen()) return false;
                settings.cancelTouch();
                return gui.handleMouse(-1,-1,button,false);
            }
            @Override public boolean mouseMoved(int x,int y){return pointer(x,y,-1,false);}
            @Override public boolean keyDown(int key){
                if(key==Input.Keys.ESCAPE || key==Input.Keys.BACK){
                    if(exitConfirmation != null && exitConfirmation.isOpen()) exitConfirmation.closePopup();
                    else if(settings.isAbove(clientManagement)) {
                        settings.back();
                    }
                    else if(clientManagement.isOpen())clientManagement.closePopup();
                    else if(mobileMenu!=null && mobileMenu.isVisible())mobileMenu.close();
                    else if(loginPanel.hasNativeEditor())loginPanel.hideKeyboard();
                    else if(controller.state()!=LoginController.State.LOGIN)controller.back();
                    else if(!gui.handleKey(Event.KEY_ESCAPE,'\0',true))loginPanel.hideKeyboard();
                    return true;
                }
                int code=keyCode(key);
                return code!=0 && gui.handleKey(code,'\0',true);
            }
            @Override public boolean keyUp(int key){int code=keyCode(key);return code!=0 && gui.handleKey(code,'\0',false);}
            @Override public boolean keyTyped(char value){
                if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))return false;
                if(value<32 || value==127)return false;
                boolean handled=gui.handleKey(Event.KEY_NONE,value,true);
                // 字符输入是一次完整事件，必须释放以免 TWL 持续自动重复末尾字符。
                gui.handleKey(Event.KEY_NONE,value,false);
                return handled;
            }
            @Override public boolean scrolled(float x,float y){return gui.handleMouseWheel(-(int)y);}
        };
        Gdx.input.setInputProcessor(input);
        Gdx.input.setCatchKey(Input.Keys.BACK,true);
        controller.listen(this::refreshLoginState);
        loginPanel.focusDefault();
    }
    private void restoreWindowFocus() {
        if (settings.isAbove(clientManagement)) settings.raiseWindow();
        else if (clientManagement.isOpen()) clientManagement.raiseWindow();
        else if (controller.state() == LoginController.State.LOGIN) loginPanel.focusDefault();
        else if (controller.state() == LoginController.State.SELECTED) characterPage.focusDefault();
        else if (controller.state() == LoginController.State.SERVERS) serverFrame.focusDefault();
    }

    private ThemeManager loadTheme(ClientResources.Theme selected) {
        try {
            return ThemeManager.createThemeManager(resources.themeUrl(selected), renderer, renderer.createNewCacheContext(),
                    new java.util.HashMap<String,Object>(resources.imageConstants(selected,strings.language())));
        } catch (IOException | RuntimeException error) {
            if (!selected.name().equals("default") && !selected.name().equals("android")) {
                java.util.logging.Logger.getLogger(ClientUi.class.getName()).log(java.util.logging.Level.WARNING,
                        "Unable to load theme " + selected.name() + "; using default theme", error);
                return loadTheme(resources.defaultTheme());
            }
            throw new IllegalStateException("加载登录主题失败",error);
        }
    }
    private de.matthiasmann.twl.renderer.Image itemIcon(int id) {
        return itemIcons.computeIfAbsent(id, key -> {
            try {
                URL url=new URL(null,"asset:/data/sprites/itemicons/"+key+".png",
                        new AssetUrlStreamHandler(path->resources.apply(path.startsWith("data/")?path.substring(5):path)));
                var normal=renderer.loadTexture(url,null,"nearest");
                var color=normal.getImage(0,0,normal.getWidth(),normal.getHeight(),Color.WHITE,false,
                        de.matthiasmann.twl.renderer.Texture.Rotation.NONE);
                var gray=renderer.grayscale(url);
                return new de.matthiasmann.twl.renderer.Image() {
                    public int getWidth(){return color.getWidth();}
                    public int getHeight(){return color.getHeight();}
                    public void draw(de.matthiasmann.twl.renderer.AnimationState state,int x,int y){draw(state,x,y,getWidth(),getHeight());}
                    public void draw(de.matthiasmann.twl.renderer.AnimationState state,int x,int y,int w,int h){
                        (state.getAnimationState(Button.STATE_HOVER)||state.getAnimationState(Widget.STATE_KEYBOARD_FOCUS)?color:gray).draw(state,x,y,w,h);
                    }
                    public de.matthiasmann.twl.renderer.Image createTintedVersion(Color tint){
                        return color.createTintedVersion(tint);
                    }
                };
            } catch(IOException error){throw new IllegalStateException("Cannot load menu icon",error);}
        });
    }
    private static int keyCode(int key) {
        return switch(key) {
            case Input.Keys.ESCAPE, Input.Keys.BACK -> Event.KEY_ESCAPE;
            case Input.Keys.ENTER -> Event.KEY_RETURN;
            case Input.Keys.TAB -> Event.KEY_TAB;
            case Input.Keys.DEL -> Event.KEY_BACK;
            case Input.Keys.FORWARD_DEL -> Event.KEY_DELETE;
            case Input.Keys.LEFT -> Event.KEY_LEFT;
            case Input.Keys.RIGHT -> Event.KEY_RIGHT;
            case Input.Keys.UP -> Event.KEY_UP;
            case Input.Keys.DOWN -> Event.KEY_DOWN;
            case Input.Keys.HOME -> Event.KEY_HOME;
            case Input.Keys.END -> Event.KEY_END;
            case Input.Keys.SHIFT_LEFT -> Event.KEY_LSHIFT;
            case Input.Keys.SHIFT_RIGHT -> Event.KEY_RSHIFT;
            case Input.Keys.CONTROL_LEFT -> Event.KEY_LCONTROL;
            case Input.Keys.CONTROL_RIGHT -> Event.KEY_RCONTROL;
            case Input.Keys.A -> Event.KEY_A;
            case Input.Keys.C -> Event.KEY_C;
            case Input.Keys.V -> Event.KEY_V;
            case Input.Keys.X -> Event.KEY_X;
            default -> 0;
        };
    }
    private void refreshLoginState() {
        loginPanel.refreshLoginState();
        boolean loginPage = controller.state() == LoginController.State.LOGIN
                || controller.state() == LoginController.State.LOGGING_IN;
        boolean characters = controller.state() == LoginController.State.SELECTED;
        logo.setVisible(!characters);
        boolean enteringCharacters = characters && !characterPage.isVisible();
        serverFrame.setHardVisible(!loginPage && !characters);
        characterPage.setVisible(characters);
        romInfo.setVisible(!characters);
        announcements.setHardVisible(!characters);
        if (desktopMenu != null) desktopMenu.setVisible(!characters);
        serverFrame.refresh();
        if (enteringCharacters) characterPage.focusDefault();
        root.invalidateLayout();
    }

    void draw(float width,float height,float x,float y) {
        if (mobileStatus != null) mobileStatus.update(System.nanoTime());
        if (mobileMenu != null) mobileMenu.update();
        fps.setText(frameRate.frame());
        if (renderer.updateFontScale()) invalidateFontLayout(gui);
        int nextX=Math.round(x),nextY=Math.round(y);
        if(nextX!=offsetX || nextY!=offsetY){offsetX=nextX;offsetY=nextY;root.invalidateLayout();}
        loginPanel.updateNotifications();
        gui.update();
        clientManagement.positionWindow();
        settings.positionWindow();
        if (exitConfirmation != null) exitConfirmation.positionWindow();
    }
    private static void invalidateFontLayout(Widget widget) {
        if (widget instanceof TextWidget text) text.setFont(text.getFont());
        for (int index = 0; index < widget.getNumChildren(); index++) invalidateFontLayout(widget.getChild(index));
        widget.invalidateLayout();
    }
    @Override public void dispose() {
        loginPanel.dispose();
        controller.close();
        if(Gdx.input.getInputProcessor()==input)Gdx.input.setInputProcessor(null);
        gui.destroy();theme.destroy();renderer.close();
        if (ownsResources) resources.close();
    }
}
