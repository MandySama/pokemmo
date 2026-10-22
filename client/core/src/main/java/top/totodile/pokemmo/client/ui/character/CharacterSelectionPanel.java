package top.totodile.pokemmo.client.ui.character;

import de.matthiasmann.twl.*;
import java.util.*;
import java.util.function.Function;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.login.LoginController;
import top.totodile.pokemmo.server.game.GameService;

/** 无角色时沿用参考的纵向按钮布局。 */
public final class CharacterSelectionPanel extends Widget {
    private final ResizableFrame frame = new ResizableFrame();
    private final Button newCharacter;
    private final Button logout;
    private final Button deleteCharacter;
    private final Label statusLabel = new Label();
    private final DialogLayout loadingPanel = new DialogLayout();
    private final Label loadingLabel;
    private final ClientStrings strings;
    private boolean loading;
    private String status;
    private final DialogLayout content = new DialogLayout();
    private final java.util.List<Card> cards = new ArrayList<>();

    public CharacterSelectionPanel(ClientStrings strings, LoginController controller) {
        this.strings = strings;
        setTheme("/characterselectgui");
        frame.setTheme("login-panel");
        frame.setTitle(strings.get(1049));
        frame.setDraggable(false);
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        content.setTheme("container");
        // 参考 NpcTradeDialogComponent：logingui/login-panel 是无标题的居中提示布局。
        loadingLabel = new Label(strings.get(74));
        loadingPanel.setTheme("/logingui.login-panel");
        loadingPanel.setHorizontalGroup(loadingPanel.createParallelGroup(loadingLabel));
        loadingPanel.setVerticalGroup(loadingPanel.createSequentialGroup(loadingLabel));
        newCharacter = new Button(strings.get(1050));
        logout = new Button(strings.get(1159));
        deleteCharacter = new Button(strings.get(1065));
        logout.addCallback(controller::back);
        rebuild();
        frame.add(content);
        add(frame);
        add(loadingPanel);
        setVisible(false);
    }

    public void focusDefault() {
        if (loading) requestKeyboardFocus();
        else if (status != null) logout.requestKeyboardFocus();
        else if (cards.isEmpty()) newCharacter.requestKeyboardFocus();
        else cards.get(0).requestKeyboardFocus();
    }
    boolean isLoading() { return loading; }
    public void beginLoading() {
        releaseCards();
        loading = true;
        status = null;
        rebuild();
    }
    public void loadFailed() {
        loading = false;
        status = strings.get(2103);
        rebuild();
    }
    public void onNewCharacter(Runnable action) { newCharacter.addCallback(action); }
    public void onDeleteCharacter(Runnable action) { deleteCharacter.addCallback(action); }
    public java.util.List<GameService.Character> characters() { return cards.stream().map(card -> card.character).toList(); }

    public void characters(java.util.List<GameService.Character> values, ClientStrings strings, boolean mobile,
                    GdxTwlRenderer renderer, Function<GameService.Appearance,CharacterAppearance> appearances) {
        releaseCards();
        loading = false;
        status = null;
        for (var value : values) cards.add(new Card(value, strings, mobile, renderer, appearances.apply(value.appearance())));
        rebuild();
    }
    public void clearCards() {
        releaseCards();
        loading = false;
        status = null;
        rebuild();
    }
    private void releaseCards() {
        for (var card : cards) card.close();
        cards.clear();
    }
    private void rebuild() {
        content.setHorizontalGroup(null); content.setVerticalGroup(null); content.removeAllChildren();
        // 按钮实例会复用；移出控件树时不一定收到 MOUSE_EXITED，不能保留上一页的悬停。
        for (Button button : new Button[]{newCharacter, deleteCharacter, logout}) {
            button.getModel().setArmed(false);
            button.getModel().setPressed(false);
            button.getModel().setHover(false);
        }
        frame.setHardVisible(!loading);
        loadingPanel.setVisible(loading);
        if (loading) {
            // 隐藏的角色窗口仍可能被 TWL 查询尺寸，空内容也必须有完整布局组。
            content.setHorizontalGroup(content.createParallelGroup());
            content.setVerticalGroup(content.createSequentialGroup());
            invalidateLayout();
            return;
        }
        if (status != null) {
            statusLabel.setText(status);
            var column = content.createParallelGroup(statusLabel);
            var rows = content.createSequentialGroup(statusLabel);
            if (!loading) { column.addWidget(logout); rows.addWidget(logout); }
            content.setHorizontalGroup(column);
            content.setVerticalGroup(rows);
            invalidateLayout();
            return;
        }
        var row = content.createSequentialGroup();
        var height = content.createParallelGroup();
        for (var card : cards) { row.addWidget(card); height.addWidget(card); }
        deleteCharacter.setVisible(!cards.isEmpty());
        var buttonsX = content.createParallelGroup(newCharacter, logout);
        var buttonsY = content.createSequentialGroup().addWidget(newCharacter);
        if (!cards.isEmpty()) { buttonsX.addWidget(deleteCharacter); buttonsY.addWidget(deleteCharacter); }
        buttonsY.addWidget(logout);
        content.setHorizontalGroup(content.createParallelGroup().addGroup(row)
                .addGroup(buttonsX));
        content.setVerticalGroup(content.createSequentialGroup().addGroup(height)
                .addGroup(buttonsY));
        invalidateLayout();
    }
    private static final class Card extends Button implements AutoCloseable {
        private final CharacterAppearance appearance;
        private final CharacterPreview preview;
        private final Label name;
        private final int scale;
        private final GameService.Character character;
        Card(GameService.Character character, ClientStrings strings, boolean mobile, GdxTwlRenderer renderer, CharacterAppearance appearance) {
            this.appearance = appearance; scale = mobile ? 2 : 1;
            this.character = character;
            setTheme("character-select-button");
            name = new Label(character.name());
            name.setTheme("/character-summary-label"); add(name);
            preview = new CharacterPreview(appearance, renderer, mobile, () -> character.appearance().skin(), () -> character.appearance().gender(), () -> 0);
            preview.portrait(); add(preview);
        }
        @Override protected void layout() {
            name.adjustSize(); name.setPosition(getX() + (getWidth() - name.getWidth()) / 2, getY() + 5);
            preview.setPosition(getX(), getY() + 16 * scale + 11); preview.setSize(80 * scale, 80 * scale);
        }
        @Override public void close() { preview.close(); appearance.dispose(); }
    }

    @Override protected void layout() {
        Widget visiblePanel = loading ? loadingPanel : frame;
        visiblePanel.adjustSize();
        visiblePanel.setPosition(getInnerX() + (getInnerWidth() - visiblePanel.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - visiblePanel.getHeight()) / 2);
    }
}
