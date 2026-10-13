package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.protocol.CharacterPackets.CharacterInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

/** 角色卡片与训练家预览；重建列表时释放旧纹理。 */
final class CharacterSelectionWindow extends ResizableFrame {
    private final List<CharacterCard> cards = new ArrayList<>();
    private final Button create = new Button(ClientStrings.get(1050));
    private final Button delete = new Button(ClientStrings.get(1065));
    private final Button logout = new Button(ClientStrings.get(1159));
    private AvatarAssets assets;
    private LongConsumer onDelete;
    private java.util.function.Consumer<CharacterInfo> onEnter;
    private long selectedId;
    private boolean busy;
    private int cardWidth = 200, cardHeight = 120, gap = 5, padding = 14, contentTop = 48, buttonHeight = 32, actionGap = 8;

    CharacterSelectionWindow(Runnable onCreate, Runnable onLogout) {
        setTheme("characterselect-window");
        setTitle(ClientStrings.get(1049));
        setResizableAxis(ResizableAxis.NONE);
        setDraggable(false);
        create.setTheme("button");
        delete.setTheme("button");
        logout.setTheme("button");
        create.addCallback(onCreate);
        delete.addCallback(this::confirmDelete);
        logout.addCallback(onLogout);
        add(create);
        add(delete);
        add(logout);
        setCharacters(List.of());
    }

    void setAssets(AvatarAssets assets) { this.assets = assets; }
    void setOnEnter(java.util.function.Consumer<CharacterInfo> callback) { onEnter = callback; }
    void setOnDelete(LongConsumer callback) { onDelete = callback; }

    void setCharacters(List<CharacterInfo> characters) {
        close();
        for (CharacterCard card : cards) removeChild(card);
        cards.clear();
        for (CharacterInfo character : characters) {
            CharacterCard card = new CharacterCard(character);
            cards.add(card);
            add(card);
        }
        if (characters.stream().noneMatch(character -> character.id() == selectedId)) {
            selectedId = characters.isEmpty() ? 0 : characters.getFirst().id();
        }
        select(selectedId);
        setBusy(false);
        invalidateLayout();
    }

    private void select(long id) {
        selectedId = id;
        for (CharacterCard card : cards) card.setActive(card.character.id() == id);
    }

    void setBusy(boolean value) {
        busy = value;
        create.setEnabled(!value && cards.size() < top.totodile.pokemmo.protocol.CharacterPackets.LIMIT);
        delete.setVisible(!cards.isEmpty());
        delete.setEnabled(!value && selectedId != 0);
        logout.setEnabled(!value);
        cards.forEach(card -> card.setEnabled(!value));
    }

    private void confirmDelete() {
        if (busy || onDelete == null || cards.isEmpty()) return;
        showDeleteDialog(ClientStrings.get(1066), 16, null, entered -> {
            CharacterCard target = cards.stream().filter(card ->
                    card.character.appearance().name().equalsIgnoreCase(entered)).findFirst().orElse(null);
            if (target == null) {
                showDeleteDialog(ClientStrings.get(6015), 0, null, ignored -> { });
                return;
            }
            String confirmation = ClientStrings.get(1068);
            String message = ClientStrings.get(1067).replace("{00}", target.character.appearance().name())
                    .replace("{01}", confirmation).replace("\\n", "\n");
            showDeleteDialog(message, 16, confirmation, ignored -> onDelete.accept(target.character.id()));
        });
    }

    private void showDeleteDialog(String text, int maximumLength, String required,
                                  java.util.function.Consumer<String> confirmed) {
        DialogLayout dialog = new DialogLayout();
        dialog.setTheme(maximumLength > 0 ? "character-delete-panel" : "character-delete-error");
        Label message = new Label(text);
        message.setTheme("label");
        LoginEditField input = new LoginEditField();
        input.setTheme("editfield");
        input.setMaxTextLength(maximumLength);
        Button ok = new Button(ClientStrings.get(52));
        Button cancel = new Button(ClientStrings.get(53));
        PopupWindow popup = new PopupWindow(this) {
            @Override protected void layout() {
                dialog.adjustSize();
                dialog.setPosition(getX() + (getWidth() - dialog.getWidth()) / 2,
                        getY() + (getHeight() - dialog.getHeight()) / 2);
            }
        };
        popup.setTheme("/rom-modal");
        popup.setCloseOnClickedOutside(false);
        ok.setEnabled(required == null);
        input.addCallback(key -> {
            boolean matches = required == null || required.equalsIgnoreCase(input.getText());
            ok.setEnabled(matches);
            if (key == Event.KEY_RETURN && matches) { popup.closePopup(); confirmed.accept(input.getText()); }
        });
        ok.addCallback(() -> { popup.closePopup(); confirmed.accept(input.getText()); });
        cancel.addCallback(popup::closePopup);
        DialogLayout.Group horizontal = dialog.createParallelGroup().addWidget(message).addWidget(ok);
        DialogLayout.Group vertical = dialog.createSequentialGroup().addWidget(message);
        if (maximumLength > 0) {
            horizontal.addWidget(input).addWidget(cancel);
            vertical.addGap(5).addWidget(input).addGap(5).addWidget(ok).addGap(5).addWidget(cancel);
        } else vertical.addGap(5).addWidget(ok);
        dialog.setHorizontalGroup(horizontal);
        dialog.setVerticalGroup(vertical);
        popup.add(dialog);
        if (popup.openPopup()) {
            popup.setPosition(0, 0);
            popup.setSize(getGUI().getWidth(), getGUI().getHeight());
            if (maximumLength > 0) input.requestKeyboardFocus();
            else ok.requestKeyboardFocus();
        }
    }

    @Override protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        cardWidth = theme.getParameter("cardWidth", 200);
        cardHeight = theme.getParameter("cardHeight", 120);
        gap = theme.getParameter("cardGap", 5);
        padding = theme.getParameter("contentPadding", 14);
        contentTop = theme.getParameter("contentTop", 48);
        buttonHeight = theme.getParameter("actionHeight", 32);
        actionGap = theme.getParameter("actionGap", 8);
    }

    @Override public int getPreferredInnerWidth() {
        return cards.isEmpty() ? Math.max(create.getPreferredWidth(), logout.getPreferredWidth()) + padding * 2
                : cards.size() * cardWidth + (cards.size() - 1) * gap + padding * 2;
    }
    @Override public int getPreferredInnerHeight() {
        return contentTop + (cards.isEmpty() ? 0 : cardHeight + actionGap) + (cards.isEmpty() ? 2 : 3) * (buttonHeight + actionGap) + padding - actionGap;
    }
    @Override protected void layout() {
        super.layout();
        int x = getX() + padding, y = getY() + contentTop;
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setPosition(x + i * (cardWidth + gap), y);
            cards.get(i).setSize(cardWidth, cardHeight);
        }
        if (!cards.isEmpty()) y += cardHeight + actionGap;
        int width = getWidth() - padding * 2;
        for (Button action : new Button[]{create, delete, logout}) {
            if (!action.isVisible()) continue;
            action.setPosition(x, y);
            action.setSize(width, buttonHeight);
            y += buttonHeight + actionGap;
        }
    }

    void close() { cards.forEach(CharacterCard::release); }

    private final class CharacterCard extends ToggleButton {
        private final CharacterInfo character;
        private final Label name;
        private Texture texture;
        private Image image;
        private int avatarTop = 27;
        private int avatarLeft;
        private boolean failed;
        private CharacterCard(CharacterInfo character) {
            this.character = character;
            setTheme("character-card");
            name = new Label(character.appearance().name());
            name.setTheme("name");
            add(name);
            addCallback(() -> {
                select(character.id());
                if (!busy && onEnter != null) onEnter.accept(character);
            });
        }
        @Override protected void applyTheme(ThemeInfo theme) {
            super.applyTheme(theme);
            avatarTop = theme.getParameter("avatarTop", 27);
            avatarLeft = theme.getParameter("avatarLeft", 0);
        }
        @Override protected void layout() {
            super.layout();
            name.setPosition(getX() + 7, getY() + 7);
            name.setSize(getWidth() - 14, 32);
        }
        @Override protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if (assets == null || failed) return;
            if (image == null) {
                try {
                    var appearance = character.appearance();
                    int[] ids = appearance.items();
                    AvatarAssets.Item[] selected = new AvatarAssets.Item[12];
                    for (int category = 1; category < 12; category++) {
                        for (var item : assets.choices(category, appearance.gender())) {
                            if (item.id == ids[category]) { selected[category] = item; break; }
                        }
                    }
                    var pixels = assets.render(appearance.skin(), selected, appearance.colors(), 2, appearance.gender());
                    texture = ((GlfwTwlRenderer) gui.getRenderer()).uploadImage(pixels);
                    image = texture.getImage(0, 0, pixels.getWidth(), pixels.getHeight(), Color.WHITE, false, Texture.Rotation.NONE);
                } catch (IOException exception) {
                    failed = true;
                    System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Unable to render character card", exception);
                    return;
                }
            }
            image.draw(getAnimationState(), getX() + avatarLeft, getY() + avatarTop);
        }
        private void release() {
            if (texture != null) texture.destroy();
            texture = null;
            image = null;
        }
    }
}
