package top.totodile.pokemmo.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.input.TextInputWrapper;
import com.badlogic.gdx.utils.Disposable;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.utils.TintAnimator;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

/** 登录表单、账号记忆、原生输入栏及登录错误提示。 */
final class LoginPanel extends ResizableFrame implements Disposable {
    private final Widget notificationHost;
    private final LoginController controller;
    private final EditField username;
    private final EditField password;
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

    LoginPanel(Widget notificationHost, ClientStrings strings, boolean mobile,
               SavedAccounts savedAccounts, LongSupplier notificationClock, LoginController controller) {
        this.notificationHost = notificationHost;
        this.strings = strings;
        this.mobile = mobile;
        this.savedAccounts = savedAccounts;
        this.notificationClock = notificationClock;
        this.controller = controller;
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
        setTheme("login-window");
        setTitle(strings.get(1000));
        setDraggable(false);
        setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        add(form);
    }
    private void submit() {
        if (controller.state() != LoginController.State.LOGIN) return;
        if (nativeEditor != null) {
            // 原生输入栏先回写最新文本，再提交登录服务。
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

    void refreshLoginState() {
        if (disposed) return;
        boolean loginPage = controller.state() == LoginController.State.LOGIN
                || controller.state() == LoginController.State.LOGGING_IN;
        boolean enteringLogin = loginPage && !isVisible();
        // 切换页面立即移除旧页面的命中区域，避免淡出中的窗口继续挡住点击。
        setHardVisible(loginPage);
        if (enteringLogin && controller.state() == LoginController.State.LOGIN) {
            if (selectedAccount == null) {
                username.setText("");
                password.setText("");
            }
            focusDefault();
        }
        if (controller.failed() && controller.state() == LoginController.State.LOGIN) {
            showError(strings.get(2001) + " (2)");
            if (!mobile) {
                if (selectedAccount == null)
                    focusAndEdit(username.getText().isBlank() ? username : password);
                else login.requestKeyboardFocus();
            }
        }
        notificationHost.invalidateLayout();
    }

    void focusDefault() {
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
        if (notificationHost != null) notificationHost.invalidateLayout();
    }

    private void clearSavedPassword() {
        if (selectedAccount == null) return;
        savedAccounts.remove(selectedAccount);
        selectAccount(savedAccounts.selected());
        if (selectedAccount == null) focusDefault();
    }

    private void showError(String message) {
        LoginNotification notice = new LoginNotification(message);
        notifications.add(notice);
        notificationHost.add(notice);
        notificationHost.invalidateLayout();
    }

    void updateNotifications() {
        long now = notificationClock.getAsLong();
        var iterator = notifications.iterator();
        while (iterator.hasNext()) {
            LoginNotification notice = iterator.next();
            if (notice.fading) {
                if (now >= notice.fadeStartedAt + 300) {
                    notificationHost.removeChild(notice);
                    iterator.remove();
                    notificationHost.invalidateLayout();
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

    private final class LoginNotification extends ResizableFrame {
        private final Button message;
        private long expiresAt = notificationClock.getAsLong() + 5000;
        private long fadeStartedAt;
        private boolean fading;

        LoginNotification(String text) {
            setTheme("/notification");
            setDraggable(false);
            setResizableAxis(ResizableAxis.NONE);
            DialogLayout content = new DialogLayout();
            message = new Button(text.trim()) {
                @Override public boolean handleEvent(Event event) {
                    expireOnClick(event);
                    return super.handleEvent(event);
                }
            };
            message.setTheme("label");
            message.setCanAcceptKeyboardFocus(false);
            content.setHorizontalGroup(content.createParallelGroup(message));
            content.setVerticalGroup(content.createSequentialGroup(message));
            add(content);
            setTintAnimator(new TintAnimator(new TintAnimator.TimeSource() {
                private long startedAt;
                public void resetTime() { startedAt = notificationClock.getAsLong(); }
                public int getTime() { return (int) Math.min(Integer.MAX_VALUE, notificationClock.getAsLong() - startedAt); }
            }));
        }

        private void expireOnClick(Event event) {
            if (event.getType() == Event.Type.MOUSE_BTNDOWN
                    && (event.getMouseButton() == Event.MOUSE_LBUTTON || event.getMouseButton() == Event.MOUSE_RBUTTON))
                expiresAt = notificationClock.getAsLong();
        }

        @Override public boolean handleEvent(Event event) {
            expireOnClick(event);
            return super.handleEvent(event);
        }
    }
    private void focusAndEdit(EditField field){
        field.requestKeyboardFocus();
        ((LoginEditField)field).showNativeInput();
    }
    void hideKeyboard(){
        if(nativeEditor!=null)Gdx.input.closeTextInputField(false);
        else Gdx.input.setOnscreenKeyboardVisible(false);
    }
    void layoutNotifications() {
        int bottomOffset = 100;
        for (LoginNotification notice : notifications) {
            notice.adjustSize();
            notice.setPosition(notificationHost.getInnerX() + (notificationHost.getInnerWidth() - notice.getWidth()) / 2,
                    notificationHost.getInnerBottom() - bottomOffset - notice.getHeight());
            bottomOffset += notice.getHeight() + 5;
        }
    }

    boolean hasNativeEditor() { return nativeEditor != null; }

    @Override public void dispose() {
        disposed = true;
        hideKeyboard();
        for (LoginNotification notice : notifications) notificationHost.removeChild(notice);
        notifications.clear();
    }
    private final class LoginEditField extends EditField {
        private final boolean mobile, username;
        LoginEditField(boolean mobile,boolean username){
            this.mobile=mobile;this.username=username;setTheme("editfield");
        }
        @Override public de.matthiasmann.twl.renderer.MouseCursor getMouseCursor(Event event) {
            var cursor = super.getMouseCursor(event);
            return mobile || cursor != null ? cursor : GdxTwlRenderer.SystemMouseCursor.TEXT;
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
