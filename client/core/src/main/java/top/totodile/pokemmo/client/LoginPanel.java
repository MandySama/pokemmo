package top.totodile.pokemmo.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.input.TextInputWrapper;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.theme.ThemeManager;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongSupplier;
import de.matthiasmann.twl.utils.TintAnimator;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

/** 由 TWL 控件和 XML 主题构建的登录界面。 */
final class LoginPanel implements Disposable {
    private final GdxTwlRenderer renderer;
    private final GUI gui;
    private final ThemeManager theme;
    private final ResizableFrame frame = new ResizableFrame();
    private final Widget root;
    private final LoginController controller;
    private final ServerSelectionPanel serverFrame;
    private final CharacterSelectionPanel characterPage;
    private final Label romInfo;
    private final Label fps = new Label("0.00");
    private final FrameRateCounter frameRate = new FrameRateCounter(System::nanoTime);
    private final EditField username;
    private final EditField password;
    private final InputAdapter input;
    private final ClientStrings strings;
    private final boolean mobile;
    private final SavedAccounts savedAccounts;
    private final ToggleButton remember = new ToggleButton();
    private final Label rememberLabel;
    private final Label userLabel;
    private final Label passwordLabel;
    private final Button login;
    private final Button clearPassword;
    private final AccountPicker accountPicker;
    private final DialogLayout form = new DialogLayout();
    private String selectedAccount;
    private final List<LoginNotification> notifications = new ArrayList<>();
    private final LongSupplier notificationClock;
    private long nextNotificationFadeAt;
    private LoginEditField nativeEditor;
    private boolean disposed;
    private int offsetX, offsetY;

    LoginPanel(Viewport viewport, Function<String, FileHandle> assets, boolean mobile,
               SavedAccounts savedAccounts, LoginController controller) {
        this(viewport, assets, mobile, savedAccounts, () -> System.nanoTime() / 1_000_000L, controller);
    }

    LoginPanel(Viewport viewport, Function<String, FileHandle> assets, boolean mobile,
               SavedAccounts savedAccounts, LongSupplier notificationClock, LoginController controller) {
        this.controller = controller;
        this.savedAccounts = savedAccounts;
        this.mobile = mobile;
        this.notificationClock = notificationClock;
        strings = ClientStrings.load(assets.apply("strings"), null);
        renderer = new GdxTwlRenderer(viewport, strings.typeface());
        username = new LoginEditField(mobile, true);
        password = new LoginEditField(mobile, false);
        username.setMaxTextLength(64);
        password.setMaxTextLength(128);
        password.setPasswordMasking(true);
        username.addCallback(key -> { if (key == Event.KEY_RETURN) submit(); });
        password.addCallback(key -> { if (key == Event.KEY_RETURN) submit(); });
        userLabel = new Label(strings.get(1001));
        passwordLabel = new Label(strings.get(1002));
        userLabel.setLabelFor(username);
        passwordLabel.setLabelFor(password);
        rememberLabel = new Label(strings.get(1025));
        rememberLabel.setLabelFor(remember);
        login = new Button(strings.get(1000));
        login.addCallback(this::submit);
        clearPassword = new Button(strings.get(1026));
        clearPassword.addCallback(this::clearSavedPassword);
        accountPicker = new AccountPicker(strings.get(1028), this::selectAccount);
        selectAccount(savedAccounts.selected());
        frame.setTheme("login-window");
        frame.setTitle(strings.get(1000));
        frame.setDraggable(false);
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        frame.add(form);
        serverFrame = new ServerSelectionPanel(strings, controller);
        characterPage = new CharacterSelectionPanel(strings, controller);
        // 参考登录页 Nq：当前尚未加载 ROM，两个平台列表均显示 --。
        romInfo = new Label(strings.get(1170) + "\nNDS: --\nGBA: --");
        fps.setTheme("/fpscounter");
        root = new Widget() {
            @Override protected void layout() {
                romInfo.adjustSize();
                romInfo.setPosition(getInnerX() + 3, getInnerY() + 3);
                fps.adjustSize();
                fps.setPosition(mobile ? getInnerX() : getInnerRight() - fps.getWidth(),
                        mobile ? getInnerBottom() - fps.getHeight() : getInnerY());
                if (frame.isVisible()) {
                    frame.adjustSize();
                    frame.setPosition((getInnerWidth()-frame.getWidth())/2+getInnerX()+offsetX,
                        (getInnerHeight()-frame.getHeight())/2+getInnerY()-offsetY);
                }
                if (serverFrame.isVisible()) {
                    serverFrame.adjustSize();
                    serverFrame.setPosition(getInnerX() + (getInnerWidth()-serverFrame.getWidth())/2,
                            getInnerY() + (getInnerHeight()-serverFrame.getHeight())/2);
                }
                characterPage.setPosition(getInnerX(), getInnerY());
                characterPage.setSize(getInnerWidth(), getInnerHeight());
                int bottomOffset = 100;
                for (LoginNotification notice : notifications) {
                    notice.adjustSize();
                    notice.setPosition(getInnerX() + (getInnerWidth() - notice.getWidth()) / 2,
                            getInnerBottom() - bottomOffset - notice.getHeight());
                    bottomOffset += notice.getHeight() + 5;
                }
            }
        };
        root.setTheme("logingui");
        root.add(frame);
        root.add(serverFrame);
        root.add(characterPage);
        root.add(romInfo);
        root.add(fps);
        gui = new GUI(root, renderer, null);
        try {
            URL url = new URL(null, "asset:/themes/"+(mobile?"android":"default")+"/theme.xml", new AssetUrlStreamHandler(assets));
            theme = ThemeManager.createThemeManager(url, renderer);
            gui.applyTheme(theme);
        } catch (IOException e) { throw new IllegalStateException("加载登录主题失败", e); }
        input = new InputAdapter() {
            private boolean pointer(int x,int y,int button,boolean pressed) {
                Vector2 point=viewport.unproject(new Vector2(x,y));
                return gui.handleMouse(Math.round(point.x), renderer.getHeight()-Math.round(point.y),button,pressed);
            }
            @Override public boolean touchDown(int x,int y,int pointer,int button){return pointer==0 && pointer(x,y,button,true);}
            @Override public boolean touchUp(int x,int y,int pointer,int button){return pointer==0 && pointer(x,y,button,false);}
            @Override public boolean touchDragged(int x,int y,int pointer){return pointer==0 && pointer(x,y,-1,false);}
            @Override public boolean mouseMoved(int x,int y){return pointer(x,y,-1,false);}
            @Override public boolean keyDown(int key){
                if(key==Input.Keys.ESCAPE || key==Input.Keys.BACK){
                    if(nativeEditor!=null)hideKeyboard();
                    else if(controller.state()!=LoginController.State.LOGIN)controller.back();
                    else if(!gui.handleKey(Event.KEY_ESCAPE,'\0',true))hideKeyboard();
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
        focusLoginPage();
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
    private void submit() {
        if (controller.state() != LoginController.State.LOGIN) return;
        if (nativeEditor != null) {
            // 原生输入栏先回写最新文本，再执行本地校验和保存。
            Gdx.input.closeTextInputField(false, confirm -> {
                if (!disposed) submit();
                return false;
            });
            return;
        }
        hideKeyboard();
        if (mobile) {
            username.giveupKeyboardFocus();
            password.giveupKeyboardFocus();
        }
        String name = selectedAccount != null ? selectedAccount : username.getText();
        String secret = selectedAccount != null ? savedAccounts.password(selectedAccount) : password.getText();
        if (name.isBlank() || secret == null || secret.isEmpty()) {
            showError(strings.get(2001) + " (2)");
            if (!mobile) focusAndEdit(username.getText().isBlank() ? username : password);
            return;
        }
        boolean existing = selectedAccount != null;
        boolean save = remember.isActive();
        controller.login(name, secret, session -> {
            if (existing) savedAccounts.select(name);
            else if (save) {
                savedAccounts.save(name, secret);
                selectAccount(name);
            }
        });
    }

    private void refreshLoginState() {
        if (disposed) return;
        boolean loginPage = controller.state() == LoginController.State.LOGIN
                || controller.state() == LoginController.State.LOGGING_IN;
        boolean enteringLogin = loginPage && !frame.isVisible();
        // 切换页面立即移除旧页面的命中区域，避免淡出中的窗口继续挡住点击。
        frame.setHardVisible(loginPage);
        boolean characters = controller.state() == LoginController.State.SELECTED;
        boolean enteringCharacters = characters && !characterPage.isVisible();
        serverFrame.setHardVisible(!loginPage && !characters);
        characterPage.setVisible(characters);
        romInfo.setVisible(!characters);
        form.setEnabled(controller.state() == LoginController.State.LOGIN);
        serverFrame.refresh();
        if (enteringCharacters) characterPage.focusDefault();
        if (enteringLogin && controller.state() == LoginController.State.LOGIN) {
            if (selectedAccount == null) {
                username.setText("");
                password.setText("");
            }
            focusLoginPage();
        }
        if (controller.failed() && controller.state() == LoginController.State.LOGIN)
            showError(strings.get(2001) + " (2)");
        root.invalidateLayout();
    }

    private void focusLoginPage() {
        // 参考登录页显示回调：移动端或已保存账号聚焦登录，PC 手动输入时聚焦用户名。
        if (mobile || selectedAccount != null) login.requestKeyboardFocus();
        else username.requestKeyboardFocus();
    }

    private void selectAccount(String name) {
        selectedAccount = name;
        boolean saved = name != null;
        accountPicker.setAccounts(savedAccounts.names(), name);
        userLabel.setLabelFor(saved ? accountPicker : username);
        password.setReadOnly(saved);
        password.setEnabled(!saved);
        password.setPasswordMasking(!saved);
        password.setText(saved ? strings.get(1027) : "");
        if (!saved) { username.setText(""); remember.setActive(false); }
        rebuildForm();
    }

    private void rebuildForm() {
        form.setHorizontalGroup(null);
        form.setVerticalGroup(null);
        form.removeAllChildren();
        Widget account = selectedAccount != null ? accountPicker : username;
        Widget action = selectedAccount != null ? clearPassword : remember;
        DialogLayout.Group buttons = form.createSequentialGroup().addWidget(action);
        DialogLayout.Group buttonRow = form.createParallelGroup().addWidget(action);
        if (selectedAccount == null) { buttons.addWidget(rememberLabel); buttonRow.addWidget(rememberLabel); }
        buttons.addGap(0, 0, Short.MAX_VALUE).addWidget(login);
        buttonRow.addWidget(login);
        form.setHorizontalGroup(form.createParallelGroup()
                .addGroup(form.createSequentialGroup()
                        .addGroup(form.createParallelGroup(userLabel, passwordLabel))
                        .addGroup(form.createParallelGroup(account, password)))
                .addGroup(buttons));
        form.setVerticalGroup(form.createSequentialGroup()
                .addGroup(form.createParallelGroup(userLabel, account))
                .addGroup(form.createParallelGroup(passwordLabel, password))
                .addGroup(buttonRow));
        if (root != null) root.invalidateLayout();
    }

    private void clearSavedPassword() {
        if (selectedAccount == null) return;
        savedAccounts.remove(selectedAccount);
        selectAccount(savedAccounts.selected());
        if (selectedAccount == null) focusLoginPage();
    }

    private void showError(String message) {
        LoginNotification notice = new LoginNotification(message);
        notifications.add(notice);
        root.add(notice);
        root.invalidateLayout();
    }

    private void updateNotifications() {
        long now = notificationClock.getAsLong();
        var iterator = notifications.iterator();
        while (iterator.hasNext()) {
            LoginNotification notice = iterator.next();
            if (notice.fading) {
                if (now >= notice.fadeStartedAt + 300) {
                    root.removeChild(notice);
                    iterator.remove();
                    root.invalidateLayout();
                }
            } else if (now >= notice.expiresAt && now >= nextNotificationFadeAt) {
                // 密集产生的提示逐条退场，避免同一渲染帧批量消失。
                // 保留参考客户端的 250ms 淡出和 300ms 移除周期。
                notice.fading = true;
                notice.fadeStartedAt = now;
                nextNotificationFadeAt = now + 300;
                notice.getTintAnimator().fadeToHide(250);
            }
        }
    }

    private final class LoginNotification extends Label {
        private long expiresAt = notificationClock.getAsLong() + 5000;
        private long fadeStartedAt;
        private boolean fading;

        LoginNotification(String message) {
            super(message.trim());
            setTheme("/login-notification");
            setTintAnimator(new TintAnimator(new TintAnimator.TimeSource() {
                private long startedAt;
                public void resetTime() { startedAt = notificationClock.getAsLong(); }
                public int getTime() { return (int) Math.min(Integer.MAX_VALUE, notificationClock.getAsLong() - startedAt); }
            }));
        }

        @Override public boolean handleEvent(Event event) {
            if ((event.getType() == Event.Type.MOUSE_BTNDOWN || event.getType() == Event.Type.MOUSE_CLICKED)
                    && (event.getMouseButton() == Event.MOUSE_LBUTTON || event.getMouseButton() == Event.MOUSE_RBUTTON)) {
                expiresAt = notificationClock.getAsLong();
                return true;
            }
            return super.handleEvent(event);
        }
    }
    private void focusAndEdit(EditField field){
        field.requestKeyboardFocus();
        ((LoginEditField)field).showNativeInput();
    }
    private void hideKeyboard(){
        if(nativeEditor!=null)Gdx.input.closeTextInputField(false);
        else Gdx.input.setOnscreenKeyboardVisible(false);
    }
    void draw(float width,float height,float x,float y) {
        fps.setText(frameRate.frame());
        if (renderer.updateFontScale()) invalidateFontLayout(gui);
        int nextX=Math.round(x),nextY=Math.round(y);
        if(nextX!=offsetX || nextY!=offsetY){offsetX=nextX;offsetY=nextY;root.invalidateLayout();}
        updateNotifications();
        gui.update();
    }
    private static void invalidateFontLayout(Widget widget) {
        if (widget instanceof TextWidget text) text.setFont(text.getFont());
        for (int index = 0; index < widget.getNumChildren(); index++) invalidateFontLayout(widget.getChild(index));
        widget.invalidateLayout();
    }
    @Override public void dispose() {
        disposed=true;
        controller.close();
        notifications.clear();
        if(nativeEditor!=null)Gdx.input.closeTextInputField(false);
        if(Gdx.input.getInputProcessor()==input)Gdx.input.setInputProcessor(null);
        gui.destroy();theme.destroy();renderer.close();
    }
    private final class LoginEditField extends EditField {
        private final boolean mobile, username;
        LoginEditField(boolean mobile,boolean username){
            this.mobile=mobile;this.username=username;setTheme("editfield");
        }
        @Override public void insertText(String text){
            if(username){
                // 与参考客户端的 Character.isWhitespace 过滤一致，覆盖中文全角空白。
                StringBuilder filtered=new StringBuilder(text.length());
                text.codePoints().filter(value->!Character.isWhitespace(value)).forEach(filtered::appendCodePoint);
                text=filtered.toString();
            }
            super.insertText(text);
        }
        @Override public de.matthiasmann.twl.renderer.MouseCursor getMouseCursor(Event event){
            return mobile ? super.getMouseCursor(event) : GdxTwlRenderer.SystemMouseCursor.TEXT;
        }
        @Override public boolean handleEvent(Event event){
            boolean handled=super.handleEvent(event);
            if(event.getType()==Event.Type.MOUSE_BTNDOWN)showNativeInput();
            return handled;
        }
        private void showNativeInput(){
            if(!mobile || isReadOnly() || !isEnabled() || Gdx.app.getType()!=ApplicationType.Android || disposed || nativeEditor==this)return;
            if(nativeEditor!=null){
                Gdx.input.closeTextInputField(false,confirm->{showNativeInput();return true;});
                return;
            }
            nativeEditor=this;
            // 原生输入栏支持输入法组合文本；用户名为普通文本，密码使用独立的密码类型。
            String initialText=getText();
            int initialCursor=Math.min(getCursorPos(),initialText.length());
            var config=new NativeInputConfiguration()
                .setType(username?Input.OnscreenKeyboardType.Default:Input.OnscreenKeyboardType.Password)
                .setPreventCorrection(true).setMultiLine(false).setMaskInput(!username)
                .setShowUnmaskButton(!username)
                .setMaxLength(getMaxTextLength()).setPlaceholder(strings.get(username?1001:1002))
                .setValidator(text->!username || text.codePoints().noneMatch(Character::isWhitespace))
                .setTextInputWrapper(new TextInputWrapper(){
                    public String getText(){return initialText;}
                    public int getSelectionStart(){return initialCursor;}
                    public int getSelectionEnd(){return initialCursor;}
                    public void writeResults(String text,int start,int end){
                        if(disposed)return;
                        setText(text);
                        setSelection(Math.min(start,text.length()),Math.min(end,text.length()));
                    }
                }).setCloseCallback(confirm->{
                    nativeEditor=null;
                    if(disposed)return false;
                    if(confirm && username){focusAndEdit(password);return true;}
                    return false;
                });
            Gdx.input.openTextInputField(config);
        }
    }
}
