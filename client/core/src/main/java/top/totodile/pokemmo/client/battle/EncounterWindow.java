package top.totodile.pokemmo.client.battle;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.rom.nds.bw.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.server.game.EncounterData;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;

import java.io.IOException;
import java.util.function.LongSupplier;

/**
 * Single-wild-battle subset of BattleCombatLayout / BattlePokemonHealthHud. Fight opens a
 * visual-only move preview; the other menu callbacks still map to RUN.
 */
public final class EncounterWindow extends Widget implements AutoCloseable {

    private final Widget owner;

    private final ClientResources resources;

    private final GdxTwlRenderer renderer;

    private final Runnable finished;

    private final LongSupplier clock;

    private final Button[] actions = new Button[4];

    private final MoveButton[] moves = new MoveButton[4];

    private final Button moveReturn = new Button();

    private boolean choosingMove;

    private EncounterMoveAnimation moveAnimation;

    private final Label message = new Label();
    private String encounterText = "", escapeText = "";
    private int messageCharacterDelay;

    private final Widget field =
            new Widget() {

                {
                    setTheme("");
                    setClip(true);
                }

                @Override
                protected void paintWidget(GUI gui) {
                    paintField();
                }
            };

    private final Widget panel = new Widget();

    private final Widget party =
            new Widget() {

                {
                    setTheme("");
                }

                @Override
                protected void paintWidget(GUI gui) {
                    if (encounter == null) return;
                    // BattleCombatLayout.As / jg0: six 24px slots, 6px gaps; icons overflow at
                    // -8,-10.
                    for (int slot = 0; slot < 6; slot++) {
                        final int position = slot;
                        var pokemon =
                                encounter.party().stream()
                                        .filter(p -> p.position() == position)
                                        .findFirst()
                                        .orElse(null);
                        if (pokemon == null) {
                            images.ball(3)
                                    .createTintedVersion(new Color(0x20000000))
                                    .draw(
                                            getAnimationState(),
                                            getX() + slot * 30 + 4,
                                            getY() + 4,
                                            16,
                                            16);
                        } else {
                            var icon =
                                    images.partyIcon(
                                            pokemon.species(),
                                            pokemon.form(),
                                            pokemon.gender(),
                                            pokemon.egg(),
                                            false,
                                            pokemon.shiny(),
                                            pokemon.alpha());
                            if (pokemon.hp() == 0)
                                icon = icon.createTintedVersion(new Color(0x80000000));
                            icon.draw(
                                    getAnimationState(),
                                    getX() + slot * 30 - 8,
                                    getY() - 10,
                                    36,
                                    36);
                            // GlBufferStateManager.Xa / BattleCombatLayout.Gr0.
                            int statusIndex =
                                    switch (pokemon.status()) {
                                        case 32 -> 0;
                                        case 64 -> 1;
                                        case 16 -> 2;
                                        case 8 -> 3;
                                        case 7 -> 4;
                                        case -128, 128 -> 6;
                                        default -> -1;
                                    };
                            if (pokemon.hp() > 0 && statusIndex >= 0) {
                                var status = images.atlas("status_ailment", statusIndex);
                                if (status != null)
                                    status.draw(
                                            getAnimationState(),
                                            getX() + slot * 30 + 7,
                                            getY() + 14,
                                            16,
                                            16);
                            }
                        }
                    }
                }
            };

    private HealthHud playerHud, enemyHud;

    private Texture hudTexture;

    private BwPokedex dex;

    private PokedexImages images;

    private EncounterScene scene;

    private EncounterEntrance entrance;

    private EncounterData encounter;

    private long escapedAt;

    private boolean escaping;

    private final EncounterTransition transition = new EncounterTransition();

    private com.badlogic.gdx.graphics.g2d.SpriteBatch fadeBatch;

    private Texture fadePixel;

    private int fieldWidth, fieldHeight;

    public EncounterWindow(
            Widget owner,
            ClientResources resources,
            GdxTwlRenderer renderer,
            LongSupplier clock,
            Runnable finished) {
        this.owner = owner;
        this.resources = resources;
        this.renderer = renderer;
        this.clock = clock;
        this.finished = finished;
        setTheme("");
        panel.setTheme("/encounter-panel");
        add(field);
        add(panel);
        add(party);
        int[] labels = {5100, 5104, 0, 5106}, descriptions = {5101, 5105, 5103, 5107};
        for (int i = 0; i < 4; i++) {
            final int index = i;
            var button =
                    new Button(resources.strings().get(labels[i])) {

                        private final Label caption =
                                new DecorationLabel(resources.strings().get(descriptions[index]));

                        {
                            caption.setTheme("/encounter-caption");
                            caption.setCanAcceptKeyboardFocus(false);
                            add(caption);
                        }

                        @Override
                        protected void layout() {
                            caption.setPosition(getX(), getBottom() - 26);
                            caption.setSize(getWidth(), 18);
                        }
                    };
            button.setTheme("/encounter-action");
            button.addCallback(index == 0 ? this::chooseMoves : this::escape);
            actions[i] = button;
            panel.add(button);
            var move = new MoveButton();
            move.setTheme("/encounter-move");
            move.setVisible(false);
            move.addCallback(() -> playMove(index));
            moves[i] = move;
            panel.add(move);
        }
        moveReturn.setText(resources.strings().get(58));
        moveReturn.setTheme("/encounter-return");
        moveReturn.setVisible(false);
        moveReturn.addCallback(this::mainMenu);
        panel.add(moveReturn);
        message.setTheme("/encounter-message");
        message.setVisible(false);
        panel.add(message);
    }

    public boolean isOpen() {
        return getParent() != null;
    }

    public void open(
            EncounterData value,
            top.totodile.pokemmo.client.ui.character.CharacterAppearance appearance,
            top.totodile.pokemmo.server.game.GameService.Appearance saved)
            throws IOException {
        prepare();
        encounter = value;
        encounterText = dex.encounterMessage(value.wild().species());
        escapeText = dex.escapedMessage();
        int textSpeed;
        try {
            textSpeed =
                    Integer.parseInt(resources.preferences().get("client.gameplay.textspeed", "4"));
        } catch (NumberFormatException ignored) {
            textSpeed = 4;
        }
        messageCharacterDelay =
                switch (textSpeed) {
                    case 2 -> 20;
                    case 3 -> 15;
                    case 4 -> 10;
                    case 5 -> 5;
                    case 6 -> 0;
                    default -> 25;
                };
        transition.encounterMessageDuration(
                Math.max(
                        EncounterTransition.MESSAGE,
                        (long) encounterText.length() * messageCharacterDelay));
        escaping = false;
        escapedAt = 0;
        choosingMove = false;
        stopMove();
        for (var move : moves) move.setVisible(false);
        moveReturn.setVisible(false);
        if (entrance != null) {
            entrance.close();
            entrance = null;
        }
        entrance =
                new EncounterEntrance(
                        appearance,
                        saved.skin(),
                        saved.gender(),
                        new BwPokemonSprites(dex),
                        value.player(),
                        images,
                        resources,
                        renderer);
        transition.enter(
                clock.getAsLong(),
                Boolean.parseBoolean(
                        resources
                                .preferences()
                                .get("client.graphics.battle.flash.animation", "true")),
                Boolean.parseBoolean(
                                resources
                                        .preferences()
                                        .get("client.graphics.render.overworld_in_battle", "true"))
                        ? 180
                        : 255);
        if (playerHud != null) {
            removeChild(playerHud);
            removeChild(enemyHud);
        }
        playerHud = new HealthHud(value.player(), true);
        enemyHud = new HealthHud(value.wild(), false);
        add(playerHud);
        add(enemyHud);
        for (var button : actions) {
            button.setVisible(true);
            button.setEnabled(true);
        }
        message.setVisible(false);
        if (!isOpen()) owner.add(this);
        position();
        update();
        invalidateLayout();
    }

    private void prepare() throws IOException {
        if (scene != null) return;
        try {
            dex = new BwPokedex(resources.roms().get(RomType.BLACK_WHITE), resources.strings());
            dex.loadDataPackage(resources.apply("data.pak"));
            // LocalizationMessageFormatter: BW message bank 70 supplies species names.
            resources
                    .strings()
                    .registerRomMessages(
                            2, 0, 70, 150000, 0, BwText.decode(dex.archive("a/0/0/2").readRaw(70)));
            resources
                    .strings()
                    .registerRomMessages(
                            2,
                            0,
                            203,
                            110000,
                            0,
                            BwText.decode(dex.archive("a/0/0/2").readRaw(203)));
            images = new PokedexImages(new BwPokemonSprites(dex), renderer, resources);
            scene = new EncounterScene(dex, renderer);
            hudTexture = new Texture(resources.apply("themes/default/res/battle-hud.png"));
        } catch (IOException | RuntimeException error) {
            disposeAssets();
            throw error;
        }
    }

    public void position() {
        if (isOpen()) {
            setPosition(owner.getInnerX(), owner.getInnerY());
            setSize(owner.getInnerWidth(), owner.getInnerHeight());
        }
    }

    public void update() {
        position();
        if (!isOpen()) return;
        long now = clock.getAsLong();
        if (moveAnimation != null) moveAnimation.update(now);
        if (moveAnimation != null && moveAnimation.complete(now)) {
            stopMove();
            mainMenu();
        }
        if (transition.complete(now)) {
            dismiss();
            finished.run();
            return;
        }
        boolean visible = transition.fieldVisible(now), ready = transition.ready(now);
        field.setVisible(visible);
        boolean encounterMessage = transition.encounterMessageVisible(now);
        boolean menuAppearing =
                ready && !actions[0].isVisible() && !choosingMove && moveAnimation == null;
        message.setVisible(visible && (encounterMessage || escaping));
        if (encounterMessage || escaping) {
            String fullText = escaping ? escapeText : encounterText;
            long elapsed = escaping ? now - escapedAt : transition.encounterMessageTime(now);
            int characters =
                    messageCharacterDelay == 0
                            ? fullText.length()
                            : (int)
                                    Math.min(
                                            fullText.length(),
                                            Math.max(0, elapsed) / messageCharacterDelay);
            message.setText(fullText.substring(0, characters));
        }
        for (var action : actions)
            action.setVisible(ready && !choosingMove && moveAnimation == null);
        panel.setVisible(visible && (ready || escaping || encounterMessage));
        playerHud.setVisible(
                visible
                        && (ready || escaping)
                        && (moveAnimation == null || !moveAnimation.own.hideHud));
        enemyHud.setVisible(
                visible
                        && (ready || escaping)
                        && (moveAnimation == null || !moveAnimation.enemy.hideHud));
        party.setVisible(visible && (ready || escaping));
        for (var button : actions) button.setEnabled(ready && moveAnimation == null);
        if (menuAppearing) actions[0].requestKeyboardFocus();
        invalidateLayout();
    }

    /** Text decorations leave the entire battle card clickable. */
    private static final class DecorationLabel extends Label {
        DecorationLabel(String text) {
            super(text);
        }

        @Override
        public boolean isInside(int x, int y) {
            return false;
        }
    }

    /** PC layout from FormattedHeaderLabel.K8 and BattleCombatLayout's move cards. */
    private final class MoveButton extends Button {
        private final Label pp = new DecorationLabel("");
        private final Label effect = new DecorationLabel("");
        private Image type;

        MoveButton() {
            pp.setTheme("/encounter-pp");
            effect.setTheme("");
            pp.setCanAcceptKeyboardFocus(false);
            effect.setCanAcceptKeyboardFocus(false);
            add(effect);
            add(pp);
        }

        @Override
        protected void layout() {
            effect.setPosition(getX() + 10, getY() + 26);
            effect.setSize(104, 18);
            pp.setPosition(getX() + 112, getY() + 24);
            pp.setSize(getWidth() - 118, 20);
        }

        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if (type != null) type.draw(getAnimationState(), getX() + 153, getY() + 10);
        }
    }

    private void chooseMoves() {
        if (!transition.ready(clock.getAsLong()) || moveAnimation != null) return;
        choosingMove = true;
        for (var action : actions) action.setVisible(false);
        for (int i = 0; i < 4; i++) {
            int id = i < encounter.player().moves().size() ? encounter.player().moves().get(i) : 0;
            var button = moves[i];
            button.setVisible(id > 0);
            if (id <= 0) continue;
            var data = dex.move(id);
            int max = Math.min(64, data.pp() + data.pp() * encounter.player().ppUps().get(i) / 5);
            int pp = encounter.player().pp().get(i) < 0 ? max : encounter.player().pp().get(i);
            button.setText(dex.moveName(id));
            button.pp.setText(resources.strings().get(1852) + " " + pp + " / " + max);
            button.type = images.type(data.type());
            double multiplier = dex.effectiveness(data.type(), encounter.wild().species());
            String effectiveness =
                    data.category() == 0
                            ? "status"
                            : multiplier < 0.001
                                    ? "immune"
                                    : multiplier < 1 ? "nve" : multiplier < 2 ? "e" : "se";
            String language = resources.strings().language().split("-")[0];
            if (!java.util.Set.of("zh", "en", "es", "fr").contains(language)) language = "en";
            button.effect.setTheme("/encounter-eff-" + language + "-" + effectiveness);
            button.effect.reapplyTheme();
            button.setEnabled(pp > 0 && EncounterMoveAnimation.supports(id));
        }
        moveReturn.setVisible(true);
        moves[0].requestKeyboardFocus();
        invalidateLayout();
    }

    private void mainMenu() {
        choosingMove = false;
        for (var move : moves) move.setVisible(false);
        moveReturn.setVisible(false);
        for (var action : actions) action.setVisible(true);
        actions[0].requestKeyboardFocus();
        invalidateLayout();
    }

    private void playMove(int slot) {
        if (!choosingMove || moveAnimation != null || !moves[slot].isEnabled()) return;
        try {
            moveAnimation =
                    new EncounterMoveAnimation(
                            resources,
                            images,
                            encounter.player().moves().get(slot),
                            clock.getAsLong(),
                            encounter.player(),
                            dex);
        } catch (IOException | RuntimeException error) {
            com.badlogic.gdx.Gdx.app.error(
                    "Encounter", "Unable to load reference move animation", error);
            return;
        }
        for (var move : moves) move.setVisible(false);
        moveReturn.setVisible(false);
    }

    private void stopMove() {
        if (moveAnimation != null) moveAnimation.close();
        moveAnimation = null;
    }

    private void escape() {
        if (escaping || encounter == null || !transition.ready(clock.getAsLong())) return;
        if (encounter.command(EncounterData.RUN) != EncounterData.NORMAL_SUCCESS) return;
        escaping = true;
        escapedAt = clock.getAsLong();
        transition.escape(escapedAt);
        for (var button : actions) button.setVisible(false);
        message.setText("");
        message.setVisible(true);
        invalidateLayout();
    }

    public void dismiss() {
        stopMove();
        if (isOpen()) owner.removeChild(this);
        encounter = null;
        escaping = false;
    }

    @Override
    protected void layout() {
        if (encounter == null) return;
        int percent;
        try {
            percent =
                    Math.max(
                            0,
                            Math.min(
                                    100,
                                    Integer.parseInt(
                                            resources
                                                    .preferences()
                                                    .get("client.graphics.battle.size", "100"))));
        } catch (NumberFormatException ignored) {
            percent = 100;
        }
        // Modern_Battle_Fc02.q70 and BattleCombatLayout.Nl: PC viewport + two-row panel.
        float ratio = .3f + percent * .004f;
        int w = Math.min(getWidth(), Math.max(760, (int) Math.ceil(getWidth() * ratio)));
        int h =
                Math.max(
                        1,
                        Math.min(
                                getHeight() - 115,
                                Math.max(400, (int) Math.ceil(getHeight() * ratio))));
        int x = getX() + (getWidth() - w) / 2,
                y = getY() + Math.min(50, Math.max(0, getHeight() - h - 115));
        fieldWidth = w;
        fieldHeight = h;
        float expansion = transition.expansion(clock.getAsLong());
        int animatedWidth = Math.max(1, Math.round(1 + (w - 1) * expansion)),
                animatedHeight = Math.max(1, Math.round(1 + (h - 1) * expansion));
        int animatedX =
                Math.round(getX() + getWidth() / 2f + (x - getX() - getWidth() / 2f) * expansion);
        // Reference viewport Y is measured from the bottom of the screen.
        int bottom =
                Math.round(
                        getHeight() / 2f
                                + (getHeight() - (y - getY()) - h - getHeight() / 2f) * expansion);
        field.setPosition(animatedX, getY() + getHeight() - bottom - animatedHeight);
        field.setSize(animatedWidth, animatedHeight);
        panel.setPosition(x, y + h - 115);
        panel.setSize(w, 115);
        int bw = 209;
        for (int i = 0; i < 4; i++) {
            actions[i].setPosition(
                    panel.getX() + 7 + (i % 2) * (bw + 2), panel.getY() + 7 + (i / 2) * 54);
            actions[i].setSize(bw, 52);
        }
        for (int i = 0; i < 4; i++) {
            moves[i].setPosition(actions[i].getX(), actions[i].getY());
            moves[i].setSize(bw, 52);
        }
        moveReturn.setPosition(panel.getRight() - 103, panel.getBottom() - 37);
        moveReturn.setSize(96, 30);
        // BattlePokemonHealthHud.V6 applies an additional -55 for the normal PC self HUD.
        playerHud.setPosition(x + w - playerHud.getWidth(), y + h - 184 - 55);
        enemyHud.setPosition(x, getY() + 140);
        party.setPosition(x + w - 212, getY() + h - 100);
        party.setSize(174, 24);
        message.setPosition(panel.getX(), panel.getY());
        message.setSize(panel.getWidth(), panel.getHeight());
    }

    private void paintField() {
        if (encounter == null) return;
        long elapsed = transition.entranceTime(clock.getAsLong());
        // Sprite composition uses its own FBO. Finish it before entering the battle FBO.
        var wildImage = pokemonImage(encounter.wild(), false);
        var playerImage = pokemonImage(encounter.player(), true);
        if (moveAnimation != null)
            moveAnimation.drawBackground(
                    renderer, field.getX(), field.getY(), fieldWidth, fieldHeight);
        var backdrop =
                scene.image(
                        fieldWidth,
                        fieldHeight,
                        elapsed,
                        moveAnimation,
                        () -> {
                            drawPokemon(encounter.wild(), false, wildImage);
                            drawPokemon(encounter.player(), true, playerImage);
                        });
        backdrop.draw(
                getAnimationState(),
                field.getX(),
                field.getY(),
                field.getWidth(),
                field.getHeight());
        if (moveAnimation != null)
            moveAnimation.draw(
                    renderer,
                    scene,
                    clock.getAsLong(),
                    field.getX(),
                    field.getY(),
                    fieldWidth,
                    fieldHeight);
        // The trainer/ball animation starts after the PC viewport reaches full size.
        if (transition.expansion(clock.getAsLong()) >= 1) {
            entrance.drawTrainer(elapsed, field.getX(), field.getY(), fieldWidth, fieldHeight);
            entrance.drawBall(
                    elapsed,
                    scene,
                    renderer,
                    getAnimationState(),
                    field.getX(),
                    field.getY(),
                    fieldWidth,
                    fieldHeight);
            entrance.drawRelease(
                    elapsed, scene, renderer, field.getX(), field.getY(), fieldWidth, fieldHeight);
        }
        if (moveAnimation != null && fadePixel != null) {
            var tint = moveAnimation.screenOverlay;
            renderer.image(fadePixel)
                    .createTintedVersion(
                            new de.matthiasmann.twl.Color(
                                    (byte) (tint.r * 255),
                                    (byte) (tint.g * 255),
                                    (byte) (tint.b * 255),
                                    (byte) (tint.a * 255)))
                    .draw(
                            getAnimationState(),
                            field.getX(),
                            field.getY(),
                            field.getWidth(),
                            field.getHeight());
        }
    }

    private Image pokemonImage(Pokemon pokemon, boolean own) {
        var actor = moveAnimation == null ? null : own ? moveAnimation.own : moveAnimation.enemy;
        return images.animated(
                pokemon.species(),
                pokemon.form(),
                pokemon.shiny(),
                own,
                actor == null ? clock.getAsLong() / 1000d : actor.animationSeconds);
    }

    private void drawPokemon(Pokemon pokemon, boolean own, Image image) {
        var actor = moveAnimation == null ? null : own ? moveAnimation.own : moveAnimation.enemy;
        if (actor != null && actor.opacity == 0) return;
        long elapsed = transition.entranceTime(clock.getAsLong());
        float growing = own ? entrance.pokemonScale(elapsed) : 1;
        if (growing <= 0) return;
        // BattlePokemonCombatant.ZK: single battle MS / Lt0 centers and the normal vertical offset.
        var center =
                own
                        ? new com.badlogic.gdx.math.Vector3(3, 3.1f + entrance.height(elapsed), 4)
                        : new com.badlogic.gdx.math.Vector3(5, 3.08f, 1);
        if (actor != null) center.set(actor.center);
        float scale = (pokemon.alpha() ? .015f : .01f) * growing;
        float width = image.getWidth() * scale * (actor == null ? 1 : actor.scaleX),
                height = image.getHeight() * scale * (actor == null ? 1 : actor.scaleY);
        renderer.drawBattleCombatant(
                image,
                center,
                width,
                height,
                scene.camera(),
                actor == null ? (own ? 1 : 0) : actor.color.r,
                actor == null ? (own ? 1 : 0) : actor.color.g,
                actor == null ? (own ? 1 : 0) : actor.color.b,
                actor == null
                        ? (own ? entrance.white(elapsed) : entrance.wildShade(elapsed))
                        : actor.shade);
    }

    /** TwlGui paints TK/ik after the map and before battle/HUD. */
    public void drawMapTransition(int width, int height) {
        if (!isOpen()) return;
        if (fadeBatch == null) {
            var pixel =
                    new com.badlogic.gdx.graphics.Pixmap(
                            1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            try {
                pixel.setColor(com.badlogic.gdx.graphics.Color.WHITE);
                pixel.fill();
                fadePixel = new Texture(pixel);
            } finally {
                pixel.dispose();
            }
            fadeBatch = new com.badlogic.gdx.graphics.g2d.SpriteBatch();
        }
        fadeBatch.setProjectionMatrix(
                new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, width, height));
        long now = clock.getAsLong();
        fadeBatch.begin();
        fadeBatch.setColor(1, 1, 1, transition.white(now) / 255f);
        fadeBatch.draw(fadePixel, 0, 0, width, height);
        fadeBatch.setColor(0, 0, 0, transition.black(now) / 255f);
        fadeBatch.draw(fadePixel, 0, 0, width, height);
        fadeBatch.end();
    }

    private Image image(Texture texture, int x, int y, int w, int h) {
        return renderer.image(new TextureRegion(texture, x, y, w, h));
    }

    private String name(Pokemon p) {
        return p.name().isEmpty() ? dex.name(p.species()) : p.name();
    }

    private final class HealthHud extends Widget {

        private final Pokemon pokemon;

        private final boolean own;

        private final Label title = new Label(), health = new Label();

        HealthHud(Pokemon p, boolean own) {
            this.pokemon = p;
            this.own = own;
            setTheme("");
            setSize(299, own ? 40 : 28);
            // BattlePokemonHealthHud.le0: one measured name/level label, followed by a gender
            // image.
            title.setText(name(p) + " " + resources.strings().get(59) + " " + p.level());
            health.setText(own ? p.hp() + " / " + p.maxHp() : "");
            for (var label : new Label[] {title, health}) {
                label.setTheme("/encounter-label");
                add(label);
            }
            health.setTheme("/encounter-health");
        }

        @Override
        protected void layout() {
            title.setPosition(getX() + (own ? 40 : 45), getY() - 22);
            title.setSize(title.getPreferredWidth(), 20);
            health.setPosition(getX() + 40, getY() + 13);
            health.setSize(220, 20);
        }

        @Override
        protected void paintWidget(GUI gui) {
            // Official PC capture uses the compact HUD: retain native skin height, no vertical
            // stretch.
            image(hudTexture, 0, own ? 71 : 43, own ? 298 : 306, own ? 33 : 20)
                    .draw(getAnimationState(), getX(), getY(), getWidth(), own ? 33 : 20);
            if (pokemon.gender() == 0 || pokemon.gender() == 1) {
                var gender =
                        images.atlas(
                                pokemon.gender() == 0
                                        ? "icon_gender_large_male"
                                        : "icon_gender_large_female");
                if (gender != null)
                    gender.draw(getAnimationState(), title.getRight() + 3, getY() - 22, 19, 19);
            }
            // BattlePokemonHealthHud.Om0 only shows the held-item marker when an item exists.
            if (pokemon.item() > 0)
                images.heldItem()
                        .draw(
                                getAnimationState(),
                                title.getRight() + (pokemon.gender() < 0 ? 3 : 22),
                                getY() - 20,
                                16,
                                14);
            int width = 220, bx = getX() + (own ? 40 : 45), by = getY() + 2;
            // gfx.xml health-progressbar: preserve the end caps, stretch only the center.
            image(hudTexture, 35, 8, 2, 6).draw(getAnimationState(), bx, by, 2, 8);
            image(hudTexture, 37, 8, 1, 6).draw(getAnimationState(), bx + 2, by, width - 4, 8);
            image(hudTexture, 46, 8, 2, 6).draw(getAnimationState(), bx + width - 2, by, 2, 8);
            int color =
                    pokemon.hp() * 5 < pokemon.maxHp()
                            ? 20
                            : pokemon.hp() * 2 < pokemon.maxHp() ? 22 : 21;
            image(hudTexture, color, 9, 1, 4)
                    .draw(
                            getAnimationState(),
                            bx + 1,
                            by + 1,
                            Math.max(0, (width - 2) * pokemon.hp() / pokemon.maxHp()),
                            6);
        }
    }

    @Override
    protected boolean handleEvent(Event event) {
        return true;
    }

    private void disposeAssets() {
        if (entrance != null) entrance.close();
        entrance = null;
        if (scene != null) scene.close();
        if (images != null) images.close();
        if (hudTexture != null) hudTexture.dispose();
        scene = null;
        images = null;
        hudTexture = null;
    }

    @Override
    public void close() {
        dismiss();
        disposeAssets();
        if (fadeBatch != null) fadeBatch.dispose();
        if (fadePixel != null) fadePixel.dispose();
    }
}
