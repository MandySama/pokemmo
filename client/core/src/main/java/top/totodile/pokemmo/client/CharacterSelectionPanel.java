package top.totodile.pokemmo.client;

import de.matthiasmann.twl.*;
import java.util.*;
import java.util.function.Function;
import top.totodile.pokemmo.server.game.GameService;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

/** 无角色时沿用参考的纵向按钮布局。 */
final class CharacterSelectionPanel extends Widget {
    private final ResizableFrame frame = new ResizableFrame();
    private final Button newCharacter;
    private final Button logout;
    private final Button deleteCharacter;
    private final DialogLayout content = new DialogLayout();
    private final java.util.List<Card> cards = new ArrayList<>();

    CharacterSelectionPanel(ClientStrings strings, LoginController controller) {
        setTheme("/characterselectgui");
        frame.setTheme("login-panel");
        frame.setTitle(strings.get(1049));
        frame.setDraggable(false);
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        content.setTheme("container");
        newCharacter = new Button(strings.get(1050));
        logout = new Button(strings.get(1159));
        deleteCharacter = new Button(strings.get(1065));
        logout.addCallback(controller::back);
        rebuild();
        frame.add(content);
        add(frame);
        setVisible(false);
    }

    void focusDefault() { if (cards.isEmpty()) newCharacter.requestKeyboardFocus(); else cards.get(0).requestKeyboardFocus(); }
    void onNewCharacter(Runnable action) { newCharacter.addCallback(action); }
    void onDeleteCharacter(Runnable action) { deleteCharacter.addCallback(action); }
    java.util.List<GameService.Character> characters() { return cards.stream().map(card -> card.character).toList(); }

    void characters(java.util.List<GameService.Character> values, ClientStrings strings, boolean mobile,
                    GdxTwlRenderer renderer, Function<GameService.Appearance,CharacterAppearance> appearances) {
        clearCards();
        for (var value : values) cards.add(new Card(value, strings, mobile, renderer, appearances.apply(value.appearance())));
        rebuild();
    }
    void clearCards() {
        for (var card : cards) card.close();
        cards.clear(); rebuild();
    }
    private void rebuild() {
        content.setHorizontalGroup(null); content.setVerticalGroup(null); content.removeAllChildren();
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
        frame.adjustSize();
        frame.setPosition(getInnerX() + (getInnerWidth() - frame.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - frame.getHeight()) / 2);
    }
}
