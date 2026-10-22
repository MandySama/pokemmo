package top.totodile.pokemmo.client.ui.character;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.input.TextInputWrapper;
import de.matthiasmann.twl.*;
import java.util.List;
import java.util.function.Consumer;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.server.game.GameService;

/** 参考流程：输入角色名，再输入本地化的确认短语。 */
public final class CharacterDeleteWindow extends PopupWindow {
    private final DialogLayout panel = new DialogLayout();
    private final Label message = new Label();
    private final EditField input = new EditField() {
        @Override public de.matthiasmann.twl.renderer.MouseCursor getMouseCursor(Event event) {
            var cursor = super.getMouseCursor(event);
            return mobile || cursor != null ? cursor
                    : top.totodile.pokemmo.client.twl.GdxTwlRenderer.SystemMouseCursor.TEXT;
        }
        @Override public boolean handleEvent(Event event) {
            boolean handled = super.handleEvent(event);
            if (mobile && event.getType() == Event.Type.MOUSE_BTNDOWN && !nativeEditor
                    && Gdx.app.getType() == ApplicationType.Android) showKeyboard();
            return handled;
        }
    };
    private final Button confirm, cancel;
    private final ClientStrings strings;
    private final boolean mobile;
    private final Runnable restoreFocus;
    private final Consumer<de.matthiasmann.twl.renderer.Image> background;
    private final Consumer<GameService.Character> delete;
    private List<GameService.Character> characters = List.of();
    private GameService.Character target;
    private boolean nativeEditor, pending, alert;
    private long version;

    public CharacterDeleteWindow(Widget owner, ClientStrings strings, boolean mobile, Runnable restoreFocus,
                          Consumer<de.matthiasmann.twl.renderer.Image> background, Consumer<GameService.Character> delete) {
        super(owner);
        this.strings = strings; this.mobile = mobile; this.restoreFocus = restoreFocus;
        this.background = background; this.delete = delete;
        setTheme("/confirm-widget"); setCloseOnClickedOutside(false);
        panel.setTheme("/confirm-panel");
        confirm = new Button(strings.get(52)); cancel = new Button(strings.get(53));
        confirm.addCallback(this::submit); cancel.addCallback(this::closePopup);
        input.setTheme("editfield");
        input.addCallback(reason -> confirm.setEnabled(target == null || strings.get(1068).equalsIgnoreCase(input.getText())));
        layoutInput();
        add(panel);
    }
    private void layoutInput() {
        clearPanel();
        input.setVisible(true); cancel.setVisible(true);
        panel.setHorizontalGroup(panel.createParallelGroup(message,input).addGroup(panel.createParallelGroup(confirm,cancel)));
        panel.setVerticalGroup(panel.createSequentialGroup(message,input,confirm,cancel));
    }
    private void clearPanel() {
        panel.setHorizontalGroup(null); panel.setVerticalGroup(null); panel.removeAllChildren();
    }
    public void open(List<GameService.Character> characters) {
        if (isOpen() || characters.isEmpty()) return;
        this.characters = List.copyOf(characters); target = null; pending = false; alert = false; version++;
        layoutInput();
        message.setText(strings.get(1066)); input.setMaxTextLength(16); input.setText(""); confirm.setEnabled(true);
        if (openPopup()) { positionWindow(); input.requestKeyboardFocus(); }
    }
    public long version() { return version; }
    public void failure(int id) {
        hideKeyboard(); pending = false; alert = true;
        message.setText(strings.get(id)); input.setVisible(false); cancel.setVisible(false);
        confirm.setEnabled(true);
        clearPanel();
        panel.setHorizontalGroup(panel.createParallelGroup(message,confirm));
        panel.setVerticalGroup(panel.createSequentialGroup(message,confirm));
        invalidateLayout(); confirm.requestKeyboardFocus();
    }
    private void submit() {
        if (alert) { closePopup(); return; }
        if (pending) return;
        hideKeyboard();
        if (target == null) {
            target = characters.stream().filter(value -> value.name().equalsIgnoreCase(input.getText())).findFirst().orElse(null);
            if (target == null) { failure(6015); return; }
            message.setText(strings.format(1067,target.name(),strings.get(1068)));
            input.setMaxTextLength(strings.get(1068).length()); input.setText(""); confirm.setEnabled(false);
            invalidateLayout(); input.requestKeyboardFocus();
        } else if (strings.get(1068).equalsIgnoreCase(input.getText())) { pending = true; delete.accept(target); }
    }
    private void showKeyboard() {
        nativeEditor = true;
        String text = input.getText();
        int cursor = Math.min(input.getCursorPos(),text.length());
        Gdx.input.openTextInputField(new NativeInputConfiguration().setType(Input.OnscreenKeyboardType.Default)
                .setPreventCorrection(true).setMultiLine(false).setMaxLength(input.getMaxTextLength())
                .setPlaceholder(target == null ? strings.get(1066) : strings.get(1068))
                .setTextInputWrapper(new TextInputWrapper() {
                    public String getText() { return text; }
                    public int getSelectionStart() { return cursor; }
                    public int getSelectionEnd() { return cursor; }
                    public void writeResults(String value,int start,int end) {
                        if (!isOpen()) return;
                        input.setText(value); input.setSelection(Math.min(start,value.length()),Math.min(end,value.length()));
                    }
                }).setCloseCallback(accepted -> { nativeEditor = false; return false; }));
    }
    private void hideKeyboard() { if (nativeEditor) Gdx.input.closeTextInputField(false); }
    public void positionWindow() {
        if (!isOpen()) return;
        Widget owner = getOwner(); setPosition(owner.getInnerX(),owner.getInnerY()); setSize(owner.getInnerWidth(),owner.getInnerHeight());
    }
    @Override protected void layout() {
        panel.adjustSize(); panel.setPosition(getInnerX()+(getInnerWidth()-panel.getWidth())/2,getInnerY()+(getInnerHeight()-panel.getHeight())/2);
    }
    @Override protected void paintBackground(GUI gui) { if (mobile) background.accept(getBackground()); else super.paintBackground(gui); }
    @Override public void closePopup() {
        boolean open = isOpen(); hideKeyboard(); super.closePopup();
        if (open) { version++; characters=List.of(); target=null; restoreFocus.run(); }
    }
}
