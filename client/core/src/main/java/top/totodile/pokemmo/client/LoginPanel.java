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
import java.util.function.Function;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

/** 由 TWL 控件和 XML 主题构建的登录界面。 */
final class LoginPanel implements Disposable {
    private final GdxTwlRenderer renderer;
    private final GUI gui;
    private final ThemeManager theme;
    private final ResizableFrame frame = new ResizableFrame();
    private final Widget root;
    private final EditField username;
    private final EditField password;
    private final InputAdapter input;
    private final ClientStrings strings;
    private LoginEditField nativeEditor;
    private boolean disposed;
    private int offsetX, offsetY;

    LoginPanel(Viewport viewport, Function<String, FileHandle> assets, boolean mobile) {
        strings = new ClientStrings(assets.apply("strings/strings_zh.xml"));
        renderer = new GdxTwlRenderer(viewport);
        username = new LoginEditField(mobile, true);
        password = new LoginEditField(mobile, false);
        username.setMaxTextLength(64);
        password.setMaxTextLength(128);
        password.setPasswordMasking(true);
        username.addCallback(key -> { if (key == Event.KEY_RETURN) submit(); });
        password.addCallback(key -> { if (key == Event.KEY_RETURN) submit(); });
        Label userLabel = new Label(strings.get(1001));
        Label passwordLabel = new Label(strings.get(1002));
        userLabel.setLabelFor(username);
        passwordLabel.setLabelFor(password);
        ToggleButton remember = new ToggleButton();
        Label rememberLabel = new Label(strings.get(1025));
        rememberLabel.setLabelFor(remember);
        Button login = new Button(strings.get(1000));
        login.addCallback(this::submit);
        DialogLayout form = new DialogLayout();
        form.setHorizontalGroup(form.createParallelGroup()
            .addGroup(form.createSequentialGroup()
                .addGroup(form.createParallelGroup(userLabel, passwordLabel))
                .addGroup(form.createParallelGroup(username, password)))
            .addGroup(form.createSequentialGroup(remember, rememberLabel).addGap(0, 0, Short.MAX_VALUE).addWidget(login)));
        form.setVerticalGroup(form.createSequentialGroup()
            .addGroup(form.createParallelGroup(userLabel, username))
            .addGroup(form.createParallelGroup(passwordLabel, password))
            .addGroup(form.createParallelGroup(remember, rememberLabel, login)));
        frame.setTheme("login-window");
        frame.setTitle(strings.get(1000));
        frame.setDraggable(false);
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        frame.add(form);
        root = new Widget() {
            @Override protected void layout() {
                frame.adjustSize();
                frame.setPosition((getInnerWidth()-frame.getWidth())/2+getInnerX()+offsetX,
                    (getInnerHeight()-frame.getHeight())/2+getInnerY()-offsetY);
            }
        };
        root.setTheme("logingui");
        root.add(frame);
        gui = new GUI(root, renderer, null);
        try {
            URL url = new URL(null, "asset:/theme/"+(mobile?"android":"default")+"/theme.xml", new URLStreamHandler() {
                @Override protected URLConnection openConnection(URL value) {
                    return new URLConnection(value) {
                        public void connect() {}
                        public java.io.InputStream getInputStream() {
                            return assets.apply(value.getPath().substring(1)).read();
                        }
                    };
                }
            });
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
                if(key==Input.Keys.ESCAPE || key==Input.Keys.BACK){hideKeyboard();return true;}
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
    }
    private static int keyCode(int key) {
        return switch(key) {
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
        if(username.getText().isBlank())focusAndEdit(username);
        else if(password.getText().isEmpty())focusAndEdit(password);
        else hideKeyboard();
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
        int nextX=Math.round(x),nextY=Math.round(y);
        if(nextX!=offsetX || nextY!=offsetY){offsetX=nextX;offsetY=nextY;root.invalidateLayout();}
        gui.update();
    }
    @Override public void dispose() {
        disposed=true;
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
            if(!mobile || Gdx.app.getType()!=ApplicationType.Android || disposed || nativeEditor==this)return;
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
