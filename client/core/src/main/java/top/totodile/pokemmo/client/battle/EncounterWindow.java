package top.totodile.pokemmo.client.battle;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.rom.nds.bw.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.inventory.InventoryItems;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.server.game.EncounterData;
import top.totodile.pokemmo.server.game.PlayerData.Item;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;
import top.totodile.pokemmo.server.game.battle.BattleEngine;
import top.totodile.pokemmo.server.game.battle.BattleState;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * Wild encounter subset of BattleCombatLayout / BattlePokemonHealthHud. Fight opens a visual-only
 * move preview; switching updates the active combatant without changing stored party order.
 */
public final class EncounterWindow extends Widget implements AutoCloseable {

    private boolean animationPreview, previewPaused;
    private long previewPausedAt, previewPauseTotal;
    private int previewMoveId = -1;
    private String previewStatus = "尚未播放";

    /**
     * Reference TestAnimationSlashCommand: play against current combatants without a turn request.
     */
    public String previewMove(int move, boolean enemySide, int targetSlot) {
        if (!isOpen() || !transition.ready(clock.getAsLong())) return "请先触发遭遇并等待出场结束";
        if (enginePending
                || pendingEngineState != null
                || settlementEvent != null
                || capturing()
                || switchTarget != null
                || escaping
                || activeAction != null && !animationPreview) return "请等待当前战斗操作结束";
        if (!EncounterMoveAnimation.supports(move)) return "没有对应参考动画：" + move;
        stopAnimationPreview();
        var caster = enemySide ? encounter.wild() : encounter.player();
        java.util.List<Pokemon> targets;
        BwPokedex.Move metadata = null;
        try {
            metadata = dex.move(move);
        } catch (IllegalArgumentException ignored) {
        }
        int targetType = metadata == null ? 0 : metadata.target();
        if (targetType == 1 || targetType == 6 || targetType == 7 || targetType == 12)
            targets = java.util.List.of(caster);
        else if (enemySide) targets = java.util.List.of(encounter.player());
        else if (targetSlot >= 0 && targetSlot < encounter.wilds().size())
            targets = java.util.List.of(encounter.wilds().get(targetSlot));
        else
            targets =
                    metadata != null && metadata.spread()
                            ? encounter.wilds()
                            : java.util.List.of(encounter.wild());
        animationPreview = true;
        previewPaused = false;
        previewPauseTotal = 0;
        previewMoveId = move;
        previewStatus = "播放中";
        hideCommandButtons();
        if (choices != null) choices.close();
        actionQueue.add(new BattleAction(caster, targets, move, !enemySide));
        startNextAction();
        return previewStatus;
    }

    public void pauseAnimationPreview() {
        if (!animationPreview) return;
        if (previewPaused) previewPauseTotal += clock.getAsLong() - previewPausedAt;
        else previewPausedAt = clock.getAsLong();
        previewPaused = !previewPaused;
        previewStatus = previewPaused ? "已暂停" : "播放中";
    }

    public void stopAnimationPreview() {
        if (!animationPreview) return;
        animationPreview = false;
        previewPaused = false;
        stopMove();
        actionQueue.clear();
        activeAction = null;
        previewStatus = "已停止";
        if (isOpen()) mainMenu();
    }

    public boolean animationPreviewPlaying() {
        return animationPreview;
    }

    public boolean animationPreviewPaused() {
        return previewPaused;
    }

    public long animationPreviewElapsed() {
        return Math.max(0, moveClock(clock.getAsLong()) - moveTextAt);
    }

    public String animationPreviewStatus() {
        return previewStatus;
    }

    public int animationPreviewMove() {
        return previewMoveId;
    }

    public int animationPreviewTargets() {
        return encounter == null ? 0 : encounter.wilds().size();
    }

    public String animationPreviewContext() {
        return environment + " / " + animationPreviewTargets() + "只敌方 / " + atmosphereSource.get();
    }

    private long moveClock(long now) {
        return animationPreview ? (previewPaused ? previewPausedAt : now) - previewPauseTotal : now;
    }

    private boolean effectTestActive, effectTestingSession, skipEffectTestAnimations;
    private String effectTestStatus = "尚未使用招式";
    private long effectTestStartedAt;

    public long effectTestElapsed() {
        return Math.max(0, clock.getAsLong() - effectTestStartedAt);
    }

    public void effectTestingSession(boolean enabled) {
        effectTestingSession = enabled;
    }

    public boolean effectTestPlaying() {
        return effectTestActive;
    }

    public String effectTestStatus() {
        return effectTestStatus;
    }

    public String testMoveEffect(int move, int targetSlot) {
        return testMoveEffect(move, targetSlot, false);
    }

    public String testMoveEffect(int move, int targetSlot, boolean skipAnimations) {
        if (!isOpen() || !transition.ready(clock.getAsLong())) return "请等待测试战斗出场结束";
        if (engineState == null) return "需要实际战斗引擎，请重新发起招式测试";
        if (engineState.ended()) return "战斗已结束，请生成新的随机怪";
        if (engineState.forceSwitch()) return "我方已濒死，请先在战斗中换宠";
        if (enginePending
                || pendingEngineState != null
                || activeAction != null
                || settlementEvent != null
                || switchTarget != null) return "请等待当前回合结束";
        BwPokedex.Move metadata;
        try {
            metadata = dex.move(move);
        } catch (IllegalArgumentException error) {
            return "此编号没有可结算的招式数据";
        }
        stopAnimationPreview();
        var action = BattleEngine.Action.testMove(move);
        if (!metadata.automaticTarget()
                && !metadata.spread()
                && targetSlot >= 0
                && targetSlot < encounter.wilds().size())
            action = action.withTarget(encounter.wilds().get(targetSlot).id());
        skipEffectTestAnimations = skipAnimations;
        effectTestActive = true;
        effectTestStartedAt = clock.getAsLong();
        effectTestStatus = "回合结算中";
        submitEngine(action, null);
        return effectTestStatus;
    }

    private final Widget owner;

    private final ClientResources resources;

    private final GdxTwlRenderer renderer;

    private final Runnable finished;

    private final LongSupplier clock;

    private final Button[] actions = new Button[4];

    private final MoveButton[] moves = new MoveButton[4];

    private final Button moveReturn = new Button();
    private final Button specifiedMove = new Button("指定招式（测试）");
    private BattleMovePicker testMovePicker;

    public boolean specifiedMovePickerOpen() {
        return testMovePicker != null && testMovePicker.isOpen();
    }

    public enum SelectionKind {
        SWITCH,
        ITEM,
        TARGET
    }

    /** Selected command; battle switching does not mutate the stored party. */
    public record Selection(
            SelectionKind kind, String pokemonId, String itemObjectId, int itemId, int moveSlot) {}

    private Selection selection;

    public Selection selection() {
        return selection;
    }

    private Pokemon switchTarget;
    private EncounterEntrance switchEntrance;
    private long switchStartedAt, switchSpawnAt = -1, activeSpawnAt = -1;
    private String switchText = "";

    private java.util.function.BiPredicate<Integer, Integer> environmentControls = (x, y) -> false;

    public void raiseWindow() {
        if (!isOpen()) return;
        var parent = getParent();
        if (parent.getChildIndex(this) != parent.getNumChildren() - 1) {
            parent.removeChild(this);
            parent.add(this);
        }
        requestKeyboardFocus();
    }

    public void environmentControls(java.util.function.BiPredicate<Integer, Integer> controls) {
        environmentControls = controls;
    }

    @Override
    public boolean isInside(int x, int y) {
        if (environmentControls.test(x, y) && !(field.isVisible() && field.isInside(x, y)))
            return false;
        return field.isVisible() && field.isInside(x, y)
                || panel.isVisible() && panel.isInside(x, y)
                || choices != null && choices.isVisible() && choices.isInside(x, y)
                || playerHud != null && playerHud.isVisible() && playerHud.isInside(x, y)
                || enemyHuds.stream().anyMatch(hud -> hud.isVisible() && hud.isInside(x, y));
    }

    private java.util.function.Function<
                    Selection,
                    java.util.concurrent.CompletionStage<
                            top.totodile.pokemmo.server.game.GameService.CaptureResult>>
            captureRequest;
    private java.util.function.Consumer<top.totodile.pokemmo.server.game.GameService.CaptureResult>
            capturedState = result -> {};
    private EncounterCaptureAnimation captureAnimation;
    private top.totodile.pokemmo.server.game.GameService.CaptureResult captureResult;
    private boolean capturePending, captureAnnounced;
    private long captureGeneration, captureMessageAt, captureMessageUntil;
    private String captureText = "";

    public void capture(
            java.util.function.Function<
                            Selection,
                            java.util.concurrent.CompletionStage<
                                    top.totodile.pokemmo.server.game.GameService.CaptureResult>>
                    request,
            java.util.function.Consumer<top.totodile.pokemmo.server.game.GameService.CaptureResult>
                    updated) {
        captureRequest = request;
        capturedState = updated;
    }

    private boolean capturing() {
        return capturePending || captureAnimation != null;
    }

    private final EncounterAtmosphere atmosphere = new EncounterAtmosphere();
    private java.util.function.Supplier<EncounterAtmosphere.State> atmosphereSource = () -> null;

    public void atmosphere(java.util.function.Supplier<EncounterAtmosphere.State> source) {
        atmosphereSource = source;
    }

    private final java.util.ArrayDeque<BattleState.Event> engineEvents =
            new java.util.ArrayDeque<>();
    private BattleState.Event settlementEvent;
    private BattleSettlementEffect settlementEffect;
    private boolean settlementHealthPending;
    private long settlementStarted, settlementUntil;
    private String settlementText = "";
    private boolean settlementLogged;
    private final java.util.Map<String, java.util.Map<String, Integer>> presentedBoosts =
            new java.util.HashMap<>();
    private final java.util.Map<String, Integer> presentedStatuses = new java.util.HashMap<>();

    private static int statusIcon(String status) {
        return switch (status) {
            case "frz" -> 0;
            case "par" -> 1;
            case "brn" -> 2;
            case "psn" -> 3;
            case "slp" -> 4;
            case "tox" -> 6;
            default -> -1;
        };
    }

    private void updateStatusHuds() {
        if (playerHud != null) playerHud.updateStatus();
        for (var hud : enemyHuds) hud.updateStatus();
    }

    /** Authoritative engine stages also cover entry abilities, Haze, Baton Pass and switches. */
    private void syncBoosts(BattleState state) {
        presentedBoosts.clear();
        presentedStatuses.clear();
        for (var pokemon : state.pokemon()) {
            presentedBoosts.put(pokemon.id(), new java.util.HashMap<>(pokemon.boosts()));
            presentedStatuses.put(pokemon.id(), statusIcon(pokemon.status()));
        }
        updateStatusHuds();
        if (playerHud != null) playerHud.updateBoostTooltip();
        for (var hud : enemyHuds) hud.updateBoostTooltip();
    }

    private boolean testMode;

    public void testMode(boolean enabled) {
        testMode = enabled;
        if (!enabled && settlementEvent != null && settlementEvent.type().equals("DAMAGE")) {
            settlementText = "";
            settlementUntil = clock.getAsLong();
        }
        if (playerHud != null) playerHud.updateHealth(clock.getAsLong());
        for (var hud : enemyHuds) hud.updateHealth(clock.getAsLong());
        invalidateLayout();
    }

    private final java.util.Set<String> faintedParticipants = new java.util.HashSet<>();

    private void syncFainted(BattleState state) {
        faintedParticipants.clear();
        for (var p : state.pokemon()) if (p.hp() <= 0) faintedParticipants.add(p.id());
    }

    private HealthHud healthHud(String id) {
        if (playerHud != null && playerHud.pokemon.id().equals(id)) return playerHud;
        return enemyHuds.stream().filter(h -> h.pokemon.id().equals(id)).findFirst().orElse(null);
    }

    private void stopSettlement() {
        if (settlementEffect != null) settlementEffect.close();
        settlementEffect = null;
        settlementHealthPending = false;
        settlementEvent = null;
        settlementText = "";
    }

    /** Consume ordered engine events between moves; health updates finish before the next event. */
    private boolean nextEngineEvent() {
        while (!engineEvents.isEmpty()) {
            var event = engineEvents.remove();
            var target = battlePokemon(event.targetId());
            if (event.type().equals("MOVE") || event.type().equals("CHARGE")) {
                chargingMoveAnimation = event.type().equals("CHARGE");
                suppressMoveAnimation = event.animationSuppressed();
                var caster = battlePokemon(event.casterId());
                if (caster != null && target != null)
                    actionQueue.add(
                            new BattleAction(
                                    caster,
                                    event.targetIds().isEmpty()
                                            ? java.util.List.of(target)
                                            : event.targetIds().stream()
                                                    .map(this::battlePokemon)
                                                    .filter(java.util.Objects::nonNull)
                                                    .toList(),
                                    event.move(),
                                    encounter.party().stream()
                                            .anyMatch(p -> p.id().equals(caster.id()))));
                return false;
            }
            if (target == null) continue;
            if (event.type().equals("STATUS") || event.type().equals("CURE_STATUS")) {
                presentedStatuses.put(
                        target.id(),
                        event.type().equals("STATUS") ? statusIcon(event.status()) : -1);
                updateStatusHuds();
                continue;
            }
            if (event.type().equals("FAINT")) {
                faintedParticipants.add(target.id());
                invalidateLayout();
                continue;
            }
            var hud = healthHud(target.id());
            long now = clock.getAsLong();
            settlementText = "";
            settlementUntil = now;
            settlementLogged = false;
            try {
                if (event.type().equals("DAMAGE")
                        || event.type().equals("HEAL")
                        || event.type().equals("SET_HP")) {
                    if (hud == null) continue;
                    int damage = hud.healthAnimation.hp() - event.hp();
                    boolean healing = damage < 0;
                    if (testMode && event.type().equals("DAMAGE") && damage > 0) {
                        settlementText = name(target) + "：-" + damage + " HP";
                        settlementUntil =
                                now
                                        + Math.max(
                                                1250L,
                                                (long) settlementText.length()
                                                        * messageCharacterDelay);
                    }
                    boolean trapping =
                            event.type().equals("DAMAGE") && !event.direct() && event.move() > 0;
                    if (trapping) {
                        boolean wild =
                                encounter.wilds().stream()
                                        .anyMatch(p -> p.id().equals(target.id()));
                        settlementText =
                                dex.trappingDamageMessage(name(target), wild, event.move());
                        settlementUntil =
                                now
                                        + Math.max(
                                                1250L,
                                                (long) settlementText.length()
                                                        * messageCharacterDelay);
                        if (!skipEffectTestAnimations) {
                            settlementEffect =
                                    BattleSettlementEffect.trapping(
                                            resources,
                                            images,
                                            combatPosition(target),
                                            settlementUntil,
                                            event.move());
                            settlementHealthPending = true;
                        }
                    }
                    if (!settlementHealthPending)
                        hud.healthAnimation.target(event.hp(), event.maxHp());
                    hud.updateHealth(now);
                    if (healing && !skipEffectTestAnimations)
                        settlementEffect =
                                BattleSettlementEffect.healing(
                                        resources, images, combatPosition(target), now);
                } else if (event.type().equals("BOOST")
                        || event.type().equals("UNBOOST")
                        || event.type().equals("SET_BOOST")) {
                    var boosts =
                            presentedBoosts.computeIfAbsent(
                                    target.id(), key -> new java.util.HashMap<>());
                    int previous = boosts.getOrDefault(event.stat(), 0);
                    int amount =
                            event.type().equals("SET_BOOST")
                                    ? event.amount() - previous
                                    : event.type().equals("UNBOOST")
                                            ? -event.amount()
                                            : event.amount();
                    if (amount == 0) continue;
                    boosts.put(event.stat(), previous + amount);
                    boolean wild =
                            encounter.wilds().stream().anyMatch(p -> p.id().equals(target.id()));
                    settlementText =
                            dex.statChangeMessage(name(target), wild, event.stat(), amount);
                    settlementUntil =
                            now
                                    + Math.max(
                                            1250L,
                                            (long) settlementText.length() * messageCharacterDelay);
                    if (!skipEffectTestAnimations)
                        settlementEffect =
                                BattleSettlementEffect.stat(
                                        resources,
                                        images,
                                        combatPosition(target),
                                        settlementUntil,
                                        amount > 0);
                } else if (event.type().equals("DISABLE_START")
                        || event.type().equals("DISABLE_END")
                        || event.type().equals("MOVE_DISABLED")) {
                    boolean wild =
                            encounter.wilds().stream().anyMatch(p -> p.id().equals(target.id()));
                    settlementText =
                            dex.disableMessage(name(target), wild, event.move(), event.type());
                    settlementUntil =
                            now
                                    + Math.max(
                                            1250L,
                                            (long) settlementText.length() * messageCharacterDelay);
                } else if (event.type().equals("CRIT")) {
                    boolean wild =
                            encounter.wilds().stream().anyMatch(p -> p.id().equals(target.id()));
                    settlementText =
                            dex.criticalHitMessage(
                                    name(target), wild, encounter.wilds().size() > 1);
                    settlementUntil =
                            now
                                    + Math.max(
                                            1250L,
                                            (long) settlementText.length() * messageCharacterDelay);
                } else if (event.type().equals("FAIL")
                        || event.type().equals("IMMUNE")
                        || event.type().equals("MISS")
                        || event.type().equals("BLOCK")) {
                    boolean wild =
                            encounter.wilds().stream().anyMatch(p -> p.id().equals(target.id()));
                    settlementText = dex.moveRejectedMessage(name(target), wild, event.type());
                    settlementUntil =
                            now
                                    + Math.max(
                                            1250L,
                                            (long) settlementText.length() * messageCharacterDelay);
                } else continue;
            } catch (IOException | RuntimeException error) {
                com.badlogic.gdx.Gdx.app.error(
                        "battle", "Cannot load reference settlement animation", error);
            }
            if (skipEffectTestAnimations) {
                if (hud != null
                        && (event.type().equals("DAMAGE")
                                || event.type().equals("HEAL")
                                || event.type().equals("SET_HP"))) {
                    hud.healthAnimation.snap(event.hp(), event.maxHp());
                    hud.updateHealth(now);
                }
                if (!settlementText.isEmpty()) battleMessages.accept(settlementText);
                continue;
            }
            settlementEvent = event;
            settlementStarted = now;
            activeAction = null;
            invalidateLayout();
            return true;
        }
        return false;
    }

    private void updateSettlement(long now) {
        if (settlementEvent == null) return;
        if (now >= settlementUntil + 750
                && (settlementEvent.type().equals("BOOST")
                        || settlementEvent.type().equals("UNBOOST")
                        || settlementEvent.type().equals("SET_BOOST"))) {
            var targetHud = healthHud(settlementEvent.targetId());
            if (targetHud != null) targetHud.updateBoostTooltip();
        }
        if (!settlementLogged && now >= settlementUntil) {
            settlementLogged = true;
            if (!settlementText.isEmpty()) battleMessages.accept(settlementText);
        }
        var hud = healthHud(settlementEvent.targetId());
        if (settlementHealthPending && settlementEffect != null && settlementEffect.complete(now)) {
            settlementHealthPending = false;
            if (hud != null)
                hud.healthAnimation.target(settlementEvent.hp(), settlementEvent.maxHp());
        }
        if ((hud == null || hud.healthAnimation.complete())
                && now >= settlementUntil
                && (settlementEffect == null || settlementEffect.complete(now))) {
            stopSettlement();
            startNextAction();
        }
    }

    private BattleState engineState, pendingEngineState;
    private boolean enginePending, engineVisualSwitch;
    private int engineVersion;
    private int loggedEngineTurn;
    private String battleTrainer = "";
    private java.util.function.Function<
                    BattleEngine.Action, java.util.concurrent.CompletionStage<BattleState>>
            engineChoose;
    private Runnable engineClose;
    private java.util.function.Consumer<Throwable> engineError =
            error -> com.badlogic.gdx.Gdx.app.error("battle", "Showdown action failed", error);

    public void engine(
            BattleState state,
            java.util.function.Function<
                            BattleEngine.Action, java.util.concurrent.CompletionStage<BattleState>>
                    choose,
            Runnable close,
            java.util.function.Consumer<Throwable> error) {
        engineVersion++;
        engineState = state;
        loggedEngineTurn = 0;
        engineChoose = choose;
        engineClose = close;
        engineError = error;
        pendingEngineState = null;
        enginePending = false;
        engineEvents.clear();
        syncBoosts(state);
        syncFainted(state);
        stopSettlement();
    }

    private Pokemon battlePokemon(String id) {
        return java.util.stream.Stream.concat(
                        encounter.party().stream(), encounter.wilds().stream())
                .filter(p -> p.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void submitEngine(BattleEngine.Action action, String switchId) {
        if (enginePending || pendingEngineState != null || engineState.ended()) return;
        enginePending = true;
        hideCommandButtons();
        int version = engineVersion;
        engineChoose
                .apply(action)
                .whenComplete(
                        (state, error) ->
                                com.badlogic.gdx.Gdx.app.postRunnable(
                                        () -> {
                                            if (version != engineVersion || !isOpen()) return;
                                            enginePending = false;
                                            if (error != null) {
                                                mainMenu();
                                                if (effectTestActive) {
                                                    effectTestActive = false;
                                                    effectTestStatus = "结算失败：" + error.getMessage();
                                                } else engineError.accept(error);
                                                return;
                                            }
                                            if (engineState.turn() > loggedEngineTurn
                                                    && action.type() != BattleEngine.Type.FORFEIT) {
                                                loggedEngineTurn = engineState.turn();
                                                if (battleTurns != null)
                                                    battleTurns.accept(loggedEngineTurn);
                                                else
                                                    battleMessages.accept(
                                                            resources
                                                                    .strings()
                                                                    .format(
                                                                            5075,
                                                                            Integer.toString(
                                                                                    loggedEngineTurn)));
                                            }
                                            pendingEngineState = state;
                                            actionQueue.clear();
                                            engineEvents.clear();
                                            engineEvents.addAll(state.events());
                                            if (switchId != null) {
                                                engineVisualSwitch = true;
                                                try {
                                                    switchPokemon(switchId);
                                                } finally {
                                                    engineVisualSwitch = false;
                                                }
                                            }
                                            if (switchTarget == null) startNextAction();
                                        }));
    }

    private void finishEngineTurn() {
        skipEffectTestAnimations = false;
        if (effectTestActive) {
            effectTestActive = false;
            effectTestStatus = "回合完成";
        }
        var state = pendingEngineState;
        pendingEngineState = null;
        engineState = state;
        syncBoosts(state);
        syncFainted(state);
        encounter = state.apply(encounter);
        removeChild(playerHud);
        playerHud = new HealthHud(encounter.player(), true);
        add(playerHud);
        for (var hud : enemyHuds) removeChild(hud);
        enemyHuds.clear();
        for (var wild : encounter.wilds()) {
            var hud = new HealthHud(wild, false);
            enemyHuds.add(hud);
            add(hud);
        }
        enemyHud = enemyHuds.get(0);
        invalidateLayout();
        if (state.ended()) {
            String winner =
                    state.winner().equals("Player") ? battleTrainer : name(encounter.wild());
            String loser = state.winner().equals("Player") ? name(encounter.wild()) : battleTrainer;
            escapeText = resources.strings().format(5017, winner, loser);
            escaping = true;
            escapedAt = clock.getAsLong();
            transition.escape(escapedAt);
            battleMessages.accept(escapeText);
            hideCommandButtons();
            message.setVisible(true);
        } else if (state.forceSwitch()) {
            mainMenu();
            showParty();
        } else mainMenu();
    }

    private record BattleAction(
            Pokemon caster, java.util.List<Pokemon> targets, int move, boolean playerSide) {}

    private final java.util.ArrayDeque<BattleAction> actionQueue = new java.util.ArrayDeque<>();
    private BattleAction activeAction;
    private java.util.function.Consumer<Pokemon> targetChosen;
    private Pokemon captureTarget;
    private Runnable targetCancelled;

    private com.badlogic.gdx.math.Vector3 combatPosition(Pokemon p) {
        return p.id().equals(encounter.player().id())
                ? new com.badlogic.gdx.math.Vector3(3, 3.1f, 4)
                : EncounterFormation.enemy(encounter.horde(), p.position());
    }

    private EncounterMoveAnimation.Combatant animationActor(Pokemon pokemon) {
        if (settlementEffect != null
                && settlementEvent != null
                && settlementEvent.targetId().equals(pokemon.id())) {
            var animation = settlementEffect.animation(clock.getAsLong());
            if (animation != null) return animation.enemy;
        }
        if (moveAnimation == null || activeAction == null) return null;
        if (pokemon.id().equals(activeAction.caster().id())) return moveAnimation.own;
        return activeAction.targets().stream().anyMatch(p -> p.id().equals(pokemon.id()))
                ? moveAnimation.enemy
                : null;
    }

    private void chooseTarget(java.util.function.Consumer<Pokemon> selected, Runnable cancelled) {
        targetCancelled = cancelled;
        hideCommandButtons();
        targetChosen = selected;
        removeChild(choices);
        add(choices);
        choices.openTargets(encounter);
    }

    private void submitTarget(String id) {
        var selected = targetChosen;
        targetChosen = null;
        targetCancelled = null;
        choices.close();
        var target =
                encounter.wilds().stream().filter(p -> p.id().equals(id)).findFirst().orElse(null);
        if (selected != null && target != null) selected.accept(target);
    }

    private boolean captureActor(Pokemon pokemon) {
        return captureAnimation != null
                && captureTarget != null
                && pokemon.id().equals(captureTarget.id());
    }

    private EncounterChoices choices;
    private InventoryItems battleItems;
    private Consumer<Consumer<List<Item>>> loadInventory;

    public void inventory(InventoryItems items, Consumer<Consumer<List<Item>>> load) {
        battleItems = items;
        loadInventory = load;
    }

    private boolean choosingCommand() {
        return choices != null && choices.isVisible();
    }

    private void showParty() {
        if (engineState != null
                && (enginePending || pendingEngineState != null || engineState.ended())) return;
        if (capturing()
                || switchTarget != null
                || !transition.ready(clock.getAsLong())
                || moveAnimation != null
                || escaping) return;
        hideCommandButtons();
        removeChild(choices);
        add(choices);
        choices.openSwitch(encounter);
        invalidateLayout();
    }

    private void showBag() {
        if (engineState != null) return;
        if (capturing()
                || !transition.ready(clock.getAsLong())
                || moveAnimation != null
                || escaping
                || switchTarget != null
                || battleItems == null
                || loadInventory == null) return;
        hideCommandButtons();
        removeChild(choices);
        add(choices);
        choices.openBag(encounter, battleItems, loadInventory);
        invalidateLayout();
    }

    private void hideCommandButtons() {
        choosingMove = false;
        for (var button : actions) button.setVisible(false);
        for (var button : moves) button.setVisible(false);
        moveReturn.setVisible(false);
        specifiedMove.setVisible(false);
    }

    private boolean choosingMove;

    private EncounterMoveAnimation moveAnimation;
    private EncounterVanityEffect playerVanity;
    private final java.util.List<EncounterVanityEffect> enemyVanities = new java.util.ArrayList<>();

    private void stopVanities() {
        if (playerVanity != null) playerVanity.close();
        playerVanity = null;
        enemyVanities.forEach(EncounterVanityEffect::close);
        enemyVanities.clear();
    }

    private final Label message =
            new Label() {
                @Override
                protected int computeTextY() {
                    // TwlFont uses flipped glyph metrics; our SpriteBatch font needs the cap
                    // baseline.
                    return super.computeTextY() - (getFont() == null ? 0 : getFont().getBaseLine());
                }
            };
    private java.util.function.Consumer<String> battleMessages = text -> {};
    private boolean loggedEncounter, loggedSendOut;

    public int commandBottom() {
        return panel.getBottom();
    }

    private java.util.function.IntConsumer battleTurns;

    public void onBattleTurn(java.util.function.IntConsumer listener) {
        battleTurns = listener;
    }

    public java.util.List<String> battleNames() {
        if (encounter == null) return java.util.List.of();
        return java.util.stream.Stream.concat(
                        encounter.party().stream(), encounter.wilds().stream())
                .map(this::name)
                .distinct()
                .toList();
    }

    public void onBattleMessage(java.util.function.Consumer<String> listener) {
        battleMessages = java.util.Objects.requireNonNull(listener);
    }

    private String encounterText = "", sendOutText = "", escapeText = "";
    private int messageCharacterDelay;
    private String moveText = "";
    private long moveTextAt, moveAnimationAt;
    private boolean moveTextLogged;
    private boolean suppressMoveAnimation;
    private boolean chargingMoveAnimation;

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
    private final java.util.List<HealthHud> enemyHuds = new java.util.ArrayList<>();

    private top.totodile.pokemmo.server.game.GameService.PokedexState pokedexState =
            top.totodile.pokemmo.server.game.GameService.PokedexState.defaults();

    public void pokedex(top.totodile.pokemmo.server.game.GameService.PokedexState state) {
        pokedexState = state;
    }

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
            button.addCallback(
                    switch (index) {
                        case 0 -> this::chooseMoves;
                        case 1 -> this::showBag;
                        case 2 -> this::showParty;
                        default -> this::escape;
                    });
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
        specifiedMove.setVisible(false);
        moveReturn.addCallback(this::mainMenu);
        panel.add(moveReturn);
        specifiedMove.setTheme("/encounter-return");
        specifiedMove.setVisible(false);
        specifiedMove.addCallback(
                () -> {
                    if (!choosingMove
                            || enginePending
                            || pendingEngineState != null
                            || moveAnimation != null) return;
                    if (testMovePicker == null)
                        testMovePicker =
                                new BattleMovePicker(
                                        specifiedMove,
                                        dex,
                                        resources.strings(),
                                        this::playSpecifiedMove);
                    testMovePicker.open();
                });
        panel.add(specifiedMove);
        message.setTheme("/encounter-message");
        message.setVisible(false);
        panel.add(message);
    }

    public boolean isOpen() {
        return getParent() != null;
    }

    private EncounterEnvironment environment;

    public void open(
            EncounterData value,
            top.totodile.pokemmo.client.ui.character.CharacterAppearance appearance,
            top.totodile.pokemmo.server.game.GameService.Appearance saved,
            String trainerName)
            throws IOException {
        open(value, appearance, saved, trainerName, EncounterEnvironment.preview());
    }

    public void open(
            EncounterData value,
            top.totodile.pokemmo.client.ui.character.CharacterAppearance appearance,
            top.totodile.pokemmo.server.game.GameService.Appearance saved,
            String trainerName,
            EncounterEnvironment selectedEnvironment)
            throws IOException {
        prepare();
        if (scene == null || !selectedEnvironment.equals(environment)) {
            var replacement = new EncounterScene(dex, renderer, selectedEnvironment);
            if (scene != null) scene.close();
            scene = replacement;
            environment = selectedEnvironment;
        }
        stopCapture();
        stopMove();
        actionQueue.clear();
        faintedParticipants.clear();
        activeAction = null;
        stopSwitch();
        activeSpawnAt = -1;
        atmosphere.close();
        encounter = value;
        battleTrainer = trainerName;
        selection = null;
        if (choices == null) {
            choices =
                    new EncounterChoices(
                            resources,
                            dex,
                            images,
                            valueSelected -> {
                                selection = valueSelected;
                                if (valueSelected.kind() == SelectionKind.TARGET) {
                                    submitTarget(valueSelected.pokemonId());
                                } else if (valueSelected.kind() == SelectionKind.SWITCH) {
                                    switchPokemon(valueSelected.pokemonId());
                                } else if (captureRequest != null
                                        && battleItems.ballType(valueSelected.itemId()) >= 0) {
                                    capturePokemon(valueSelected);
                                } else mainMenu();
                            },
                            () -> {
                                var cancelled = targetCancelled;
                                targetCancelled = null;
                                targetChosen = null;
                                if (cancelled != null) cancelled.run();
                                else mainMenu();
                            });
            add(choices);
        }
        choices.close();
        loggedEncounter = false;
        loggedSendOut = false;
        encounterText =
                value.horde()
                        ? resources.strings().get(5021)
                        : dex.encounterMessage(value.wild().species());
        sendOutText = dex.sendOutMessage(trainerName, name(value.player()));
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
        specifiedMove.setVisible(false);
        if (entrance != null) {
            entrance.close();
            entrance = null;
        }
        stopVanities();
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
        try {
            playerVanity =
                    new EncounterVanityEffect(
                            resources,
                            images,
                            renderer,
                            value.player(),
                            false,
                            new com.badlogic.gdx.math.Vector3(3, 3.1f, 4));
            for (var wild : value.wilds())
                enemyVanities.add(
                        new EncounterVanityEffect(
                                resources,
                                images,
                                renderer,
                                wild,
                                true,
                                EncounterFormation.enemy(value.horde(), wild.position())));
        } catch (IOException | RuntimeException failure) {
            stopVanities();
            throw failure;
        }
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
            for (var hud : enemyHuds) removeChild(hud);
            enemyHuds.clear();
        }
        playerHud = new HealthHud(value.player(), true);
        add(playerHud);
        for (var wild : value.wilds()) {
            var hud = new HealthHud(wild, false);
            enemyHuds.add(hud);
            add(hud);
        }
        enemyHud = enemyHuds.get(0);
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
            images.reserveAnimations(10);
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

    private void capturePokemon(Selection choice) {
        if (effectTestingSession) return;
        if (capturing()) return;
        if (encounter.horde() && choice.pokemonId().isEmpty()) {
            chooseTarget(
                    target ->
                            capturePokemon(
                                    new Selection(
                                            choice.kind(),
                                            target.id(),
                                            choice.itemObjectId(),
                                            choice.itemId(),
                                            choice.moveSlot())),
                    () -> showBag());
            return;
        }
        captureTarget =
                encounter.wilds().stream()
                        .filter(
                                p ->
                                        choice.pokemonId().isEmpty()
                                                || p.id().equals(choice.pokemonId()))
                        .findFirst()
                        .orElseThrow();
        choices.close();
        hideCommandButtons();
        capturePending = true;
        long generation = ++captureGeneration;
        captureRequest
                .apply(choice)
                .whenComplete(
                        (result, error) ->
                                com.badlogic.gdx.Gdx.app.postRunnable(
                                        () -> {
                                            if (generation != captureGeneration || !isOpen())
                                                return;
                                            capturePending = false;
                                            if (error != null) {
                                                com.badlogic.gdx.Gdx.app.error(
                                                        "Encounter",
                                                        "Capture transaction rejected",
                                                        error);
                                                mainMenu();
                                                return;
                                            }
                                            captureResult = result;
                                            captureAnnounced = false;
                                            try {
                                                captureAnimation =
                                                        new EncounterCaptureAnimation(
                                                                new BwPokemonSprites(dex),
                                                                renderer,
                                                                battleItems.ballType(
                                                                        choice.itemId()),
                                                                result.animationResult(),
                                                                clock.getAsLong(),
                                                                combatPosition(captureTarget));
                                                scene.capture = captureAnimation;
                                            } catch (IOException | RuntimeException failure) {
                                                com.badlogic.gdx.Gdx.app.error(
                                                        "Encounter",
                                                        "Unable to load capture animation",
                                                        failure);
                                                // The transaction already committed; finish the
                                                // result even if a resource is unavailable.
                                                finishCapture();
                                            }
                                        }));
    }

    private void finishCapture() {
        if (captureResult == null || captureAnnounced) return;
        captureAnnounced = true;
        capturedState.accept(captureResult);
        captureText = dex.captureMessage(captureResult.animationResult(), name(captureTarget));
        battleMessages.accept(captureText);
        captureMessageAt = clock.getAsLong();
        captureMessageUntil =
                captureMessageAt + Math.max(1250, captureText.length() * messageCharacterDelay);
        if (captureResult.captured() != null) {
            escapeText = captureText;
            escaping = true;
            escapedAt = captureMessageAt;
            transition.escape(escapedAt);
        }
    }

    private void stopCapture() {
        ++captureGeneration;
        capturePending = false;
        if (captureAnimation != null) captureAnimation.close();
        captureAnimation = null;
        captureResult = null;
        captureMessageUntil = 0;
        captureAnnounced = false;
        if (scene != null) scene.capture = null;
    }

    /** BattleActiveSlotModifier -> BattleBv00Animation -> BattleSoundEffectTask/J40. */
    private void switchPokemon(String pokemonId) {
        if (engineState != null && !engineVisualSwitch) {
            submitEngine(BattleEngine.Action.swap(pokemonId), pokemonId);
            return;
        }
        if (capturing() || switchTarget != null || moveAnimation != null || escaping) return;
        Pokemon target =
                encounter.party().stream()
                        .filter(p -> p.id().equals(pokemonId))
                        .findFirst()
                        .orElse(null);
        if (target == null
                || target.egg()
                || target.hp() <= 0
                || target.id().equals(encounter.player().id())) return;
        try {
            switchEntrance =
                    new EncounterEntrance(
                            null,
                            0,
                            0,
                            new BwPokemonSprites(dex),
                            target,
                            images,
                            resources,
                            renderer);
        } catch (IOException | RuntimeException error) {
            com.badlogic.gdx.Gdx.app.error("Encounter", "Unable to load switch entrance", error);
            return;
        }
        choices.close();
        hideCommandButtons();
        switchTarget = target;
        switchStartedAt = clock.getAsLong();
        switchSpawnAt = -1;
        switchText = dex.recallMessage(name(encounter.player()));
        battleMessages.accept(switchText);
        if (playerVanity != null) playerVanity.close();
        playerVanity = null;
        update();
    }

    private void updateSwitch(long now) {
        if (switchTarget == null) return;
        if (switchSpawnAt < 0 && now - switchStartedAt >= 1450) {
            switchSpawnAt = switchStartedAt + 1450;
            activeSpawnAt = switchSpawnAt;
            entrance.close();
            entrance = switchEntrance;
            switchEntrance = null;
            encounter = new EncounterData(switchTarget, encounter.wilds(), encounter.party());
            removeChild(playerHud);
            playerHud = new HealthHud(switchTarget, true);
            add(playerHud);
            switchText =
                    dex.switchMessage(
                            name(switchTarget), encounter.wild().hp(), encounter.wild().maxHp());
            battleMessages.accept(switchText);
            try {
                playerVanity =
                        new EncounterVanityEffect(
                                resources,
                                images,
                                renderer,
                                switchTarget,
                                false,
                                new com.badlogic.gdx.math.Vector3(3, 3.1f, 4));
            } catch (IOException | RuntimeException error) {
                com.badlogic.gdx.Gdx.app.error(
                        "Encounter", "Unable to load switch particle", error);
            }
        }
        if (switchSpawnAt >= 0 && now - switchSpawnAt >= entrance.sendOutDuration()) {
            switchTarget = null;
            if (pendingEngineState != null) startNextAction();
            else mainMenu();
        }
    }

    private void stopSwitch() {
        if (switchEntrance != null) switchEntrance.close();
        switchEntrance = null;
        switchTarget = null;
        switchSpawnAt = -1;
    }

    private long ownEntranceTime(long now) {
        return activeSpawnAt < 0
                ? transition.entranceTime(now)
                : EncounterEntrance.WILD + now - activeSpawnAt;
    }

    private float recallProgress() {
        if (switchTarget == null || switchSpawnAt >= 0) return 0;
        float t = Math.max(0, Math.min(1, (clock.getAsLong() - switchStartedAt - 250) / 1200f));
        return t < .5f ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);
    }

    public void update() {
        position();
        if (!isOpen()) return;
        long now = clock.getAsLong();
        atmosphere.update(atmosphereSource.get(), now, resources, images);
        scene.environmentTint.set(atmosphere.modelTint());
        scene.ambient(atmosphere.ambient());
        updateSwitch(now);
        if (playerHud != null) playerHud.updateHealth(now);
        for (var hud : enemyHuds) hud.updateHealth(now);
        updateSettlement(now);
        if (captureAnimation != null) {
            captureAnimation.update(now);
            if (captureAnimation.complete()) finishCapture();
        }
        if (captureAnnounced && !escaping && now >= captureMessageUntil) {
            stopCapture();
            mainMenu();
        }
        long moveNow = moveClock(now);
        if (activeAction != null && moveNow >= moveAnimationAt) {
            // BattleCombatLayout.wJ completes its text task before the animation task.
            if (!moveTextLogged) {
                moveTextLogged = true;
                battleMessages.accept(moveLogText());
            }
            if (moveAnimation != null) {
                try {
                    moveAnimation.update(moveNow);
                } catch (RuntimeException failure) {
                    if (!animationPreview) throw failure;
                    stopAnimationPreview();
                    previewStatus =
                            "播放失败："
                                    + failure.getClass().getSimpleName()
                                    + ": "
                                    + failure.getMessage();
                    com.badlogic.gdx.Gdx.app.error("Animation test", previewStatus, failure);
                }
            } else startNextAction();
        }
        if (moveAnimation != null
                && moveNow >= moveAnimationAt
                && moveAnimation.complete(moveNow)) {
            stopMove();
            startNextAction();
        }
        if (transition.complete(now)) {
            dismiss();
            finished.run();
            return;
        }
        boolean visible = transition.fieldVisible(now), ready = transition.ready(now);
        field.setVisible(visible);
        boolean encounterMessage = transition.encounterMessageVisible(now);
        long entranceElapsed = transition.entranceTime(now);
        boolean sendingOut = !escaping && !ready && entranceElapsed >= EncounterEntrance.WILD;
        boolean showingWildMessage =
                !escaping
                        && !ready
                        && entranceElapsed >= EncounterTransition.WILD_MESSAGE_AT
                        && entranceElapsed < EncounterEntrance.WILD;
        if (!escaping
                && entranceElapsed >= EncounterTransition.WILD_MESSAGE_AT
                && !loggedEncounter) {
            loggedEncounter = true;
            battleMessages.accept(encounterText);
        }
        if (!escaping && entranceElapsed >= EncounterEntrance.WILD && !loggedSendOut) {
            loggedSendOut = true;
            battleMessages.accept(sendOutText);
        }
        boolean switching = switchTarget != null;
        boolean enteringText = showingWildMessage || sendingOut || switching;
        boolean menuAppearing =
                ready
                        && !actions[0].isVisible()
                        && !choosingMove
                        && !choosingCommand()
                        && activeAction == null
                        && settlementEvent == null;
        boolean usingMove =
                activeAction != null || settlementEvent != null && !settlementText.isEmpty();
        message.setVisible(visible && (enteringText || escaping || usingMove));
        if (enteringText || escaping || usingMove) {
            String fullText =
                    switching
                            ? switchText
                            : escaping
                                    ? escapeText
                                    : usingMove
                                            ? settlementEvent != null ? settlementText : moveText
                                            : sendingOut ? sendOutText : encounterText;
            long elapsed =
                    switching
                            ? now - (switchSpawnAt < 0 ? switchStartedAt : switchSpawnAt)
                            : escaping
                                    ? now - escapedAt
                                    : usingMove
                                            ? (settlementEvent != null ? now : moveNow)
                                                    - (settlementEvent != null
                                                            ? settlementStarted
                                                            : moveTextAt)
                                            : sendingOut
                                                    ? entranceElapsed - EncounterEntrance.WILD
                                                    : transition.encounterMessageTime(now);
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
            action.setVisible(
                    ready
                            && !choosingMove
                            && !choosingCommand()
                            && activeAction == null
                            && settlementEvent == null
                            && switchTarget == null);
        panel.setVisible(visible && !choosingCommand() && (ready || escaping || enteringText));
        playerHud.setVisible(
                visible
                        && (ready || escaping)
                        && (animationActor(encounter.player()) == null
                                || !animationActor(encounter.player()).hideHud));
        for (var hud : enemyHuds)
            hud.setVisible(
                    visible
                            && !faintedParticipants.contains(hud.pokemon.id())
                            && (ready
                                    || escaping
                                    || !encounterMessage
                                            && entranceElapsed >= EncounterEntrance.WILD - 1000)
                            && (animationActor(hud.pokemon) == null
                                    || !animationActor(hud.pokemon).hideHud));
        party.setVisible(visible && (ready || escaping));
        for (int i = 0; i < actions.length; i++)
            actions[i].setEnabled(
                    ready
                            && activeAction == null
                            && switchTarget == null
                            && !enginePending
                            && pendingEngineState == null
                            && (engineState == null
                                    || !engineState.ended()
                                            && i != 1
                                            && (!engineState.forceSwitch() || i == 2 || i == 3)));
        if (capturing() || captureAnnounced) {
            for (var button : actions) button.setVisible(false);
            panel.setVisible(visible);
            message.setVisible(visible && captureAnnounced);
            if (captureAnnounced) {
                int count =
                        messageCharacterDelay == 0
                                ? captureText.length()
                                : (int)
                                        Math.min(
                                                captureText.length(),
                                                Math.max(0, now - captureMessageAt)
                                                        / messageCharacterDelay);
                message.setText(captureText.substring(0, count));
            }
            if (captureAnimation != null && captureAnimation.scale() < .01f)
                for (var hud : enemyHuds)
                    if (captureTarget != null && hud.pokemon.id().equals(captureTarget.id()))
                        hud.setVisible(false);
        }
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
        if (engineState != null
                && (enginePending
                        || pendingEngineState != null
                        || engineState.forceSwitch()
                        || engineState.ended())) return;
        if (capturing()
                || switchTarget != null
                || !transition.ready(clock.getAsLong())
                || moveAnimation != null) return;
        choosingMove = true;
        for (var action : actions) action.setVisible(false);
        for (int i = 0; i < 4; i++) {
            int id = i < encounter.player().moves().size() ? encounter.player().moves().get(i) : 0;
            if (engineState != null
                    && engineState.active(0).moves().size() == 1
                    && engineState.active(0).moves().get(0).id() == 165) id = i == 0 ? 165 : 0;
            final int selectedMove = id;
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
            var engineMove =
                    engineState == null
                            ? null
                            : engineState.active(0).moves().stream()
                                    .filter(m -> m.id() == selectedMove)
                                    .findFirst()
                                    .orElse(null);
            if (engineMove != null) {
                pp = engineMove.pp();
                max = engineMove.maxPp();
                button.pp.setText(resources.strings().get(1852) + " " + pp + " / " + max);
            }
            button.setEnabled(
                    engineState == null
                            ? pp > 0 && EncounterMoveAnimation.supports(id)
                            : engineMove != null && pp > 0);
        }
        moveReturn.setVisible(true);
        specifiedMove.setVisible(true);
        moves[0].requestKeyboardFocus();
        invalidateLayout();
    }

    private void mainMenu() {
        if (testMovePicker != null) testMovePicker.closePopup();
        targetChosen = null;
        targetCancelled = null;
        if (choices != null) choices.close();
        choosingMove = false;
        for (var move : moves) move.setVisible(false);
        moveReturn.setVisible(false);
        specifiedMove.setVisible(false);
        for (var action : actions) action.setVisible(true);
        actions[0].requestKeyboardFocus();
        invalidateLayout();
    }

    private void playMove(int slot) {
        if (engineState != null) {
            if (choosingMove && moves[slot].isEnabled()) {
                int move = encounter.player().moves().get(slot);
                var selected =
                        engineState.active(0).moves().stream()
                                .filter(m -> m.id() == move)
                                .findFirst()
                                .orElse(null);
                if (selected != null && selected.disabled()) {
                    String source = selected.disabledSource();
                    String text =
                            source != null
                                            && source.replace("move: ", "")
                                                    .equalsIgnoreCase("Disable")
                                    ? dex.disableMessage(
                                            name(encounter.player()), false, move, "MOVE_DISABLED")
                                    : dex.moveRejectedMessage(
                                            name(encounter.player()), false, "FAIL");
                    mainMenu();
                    settlementEvent =
                            new BattleState.Event(
                                    "MOVE_DISABLED",
                                    null,
                                    encounter.player().id(),
                                    move,
                                    0,
                                    0,
                                    null,
                                    0,
                                    null,
                                    false,
                                    false);
                    settlementText = text;
                    settlementStarted = clock.getAsLong();
                    settlementUntil =
                            settlementStarted
                                    + Math.max(1250L, (long) text.length() * messageCharacterDelay);
                    settlementLogged = false;
                } else
                    chooseEngineMove(BattleEngine.Action.move(slot), selected == null ? 165 : move);
            }
            return;
        }
        if (!choosingMove || moveAnimation != null || !moves[slot].isEnabled()) return;
        queueSelectedMove(encounter.player().moves().get(slot));
    }

    private void playSpecifiedMove(int id) {
        if (!choosingMove || enginePending || pendingEngineState != null || moveAnimation != null)
            return;
        if (engineState != null) chooseEngineMove(BattleEngine.Action.testMove(id), id);
        else queueSelectedMove(id);
    }

    private void chooseEngineMove(BattleEngine.Action action, int move) {
        var data = dex.move(move);
        if (encounter.horde() && !data.spread() && !data.automaticTarget()) {
            chooseTarget(
                    target -> submitEngine(action.withTarget(target.id()), null),
                    this::chooseMoves);
        } else submitEngine(action, null);
    }

    private void queueSelectedMove(int id) {
        var data = dex.move(id);
        // One active player Pokemon: ally-only moves have no legal target.
        if (data.target() == 2 || data.target() == 17) return;
        if (encounter.horde() && !data.spread() && !data.automaticTarget()) {
            chooseTarget(target -> queueTurn(id, java.util.List.of(target)), this::chooseMoves);
        } else {
            var targets =
                    data.spread()
                            ? encounter.wilds()
                            : java.util.List.of(
                                    data.target() == 1
                                                    || data.target() == 6
                                                    || data.target() == 7
                                                    || data.target() == 12
                                            ? encounter.player()
                                            : data.target() == 9
                                                    ? encounter
                                                            .wilds()
                                                            .get(
                                                                    java.util.concurrent
                                                                            .ThreadLocalRandom
                                                                            .current()
                                                                            .nextInt(
                                                                                    encounter
                                                                                            .wilds()
                                                                                            .size()))
                                                    : encounter.wild());
            queueTurn(id, targets);
        }
    }

    private void queueTurn(int id, java.util.List<Pokemon> targets) {
        actionQueue.clear();
        actionQueue.add(new BattleAction(encounter.player(), targets, id, true));
        var random = java.util.concurrent.ThreadLocalRandom.current();
        for (var wild : encounter.wilds()) {
            var learned =
                    wild.moves().stream()
                            .filter(m -> m > 0 && EncounterMoveAnimation.supports(m))
                            .toList();
            if (learned.isEmpty()) continue;
            int move = learned.get(random.nextInt(learned.size()));
            int target = dex.move(move).target();
            var allies = encounter.wilds().stream().filter(p -> !p.id().equals(wild.id())).toList();
            if ((target == 2 || target == 17) && allies.isEmpty()) continue;
            var opponent =
                    target == 1 || target == 6 || target == 7 || target == 12
                            ? wild
                            : target == 2 || target == 17
                                    ? allies.get(random.nextInt(allies.size()))
                                    : encounter.player();
            var targetsForMove = target == 6 ? encounter.wilds() : java.util.List.of(opponent);
            actionQueue.add(new BattleAction(wild, targetsForMove, move, false));
        }
        hideCommandButtons();
        startNextAction();
    }

    private void startNextAction() {
        activeAction = null;
        if (pendingEngineState != null && actionQueue.isEmpty() && nextEngineEvent()) return;
        activeAction = actionQueue.poll();
        if (activeAction == null) {
            if (animationPreview) {
                animationPreview = false;
                previewPaused = false;
                previewStatus = "播放完成";
            }
            if (pendingEngineState != null) finishEngineTurn();
            else mainMenu();
            return;
        }
        moveText = dex.moveUsedMessage(name(activeAction.caster()), activeAction.move());
        moveTextAt = moveClock(clock.getAsLong());
        // RichTextBuffer defaults to 1250 ms, or longer if typing has not finished.
        moveAnimationAt =
                moveTextAt
                        + (skipEffectTestAnimations
                                ? 0
                                : Math.max(
                                        1250L, (long) moveText.length() * messageCharacterDelay));
        moveTextLogged = false;
        if (!animationPreview
                && engineState != null
                && (skipEffectTestAnimations
                        || suppressMoveAnimation
                        || !EncounterMoveAnimation.supports(activeAction.move()))) return;
        try {
            moveAnimation =
                    new EncounterMoveAnimation(
                            resources,
                            images,
                            activeAction.move(),
                            moveAnimationAt,
                            activeAction.caster(),
                            dex,
                            combatPosition(activeAction.caster()),
                            activeAction.targets().stream().map(this::combatPosition).toList(),
                            activeAction.playerSide());
            if (!animationPreview && engineState != null)
                moveAnimation.enginePhase(chargingMoveAnimation);
        } catch (IOException | RuntimeException error) {
            com.badlogic.gdx.Gdx.app.error(
                    "Encounter", "Unable to load reference move animation", error);
            if (animationPreview) {
                previewStatus = "加载失败：" + error.getMessage();
                animationPreview = false;
                previewPaused = false;
                actionQueue.clear();
            }
            startNextAction();
        }
    }

    /** BattleFaintSlotModifier: append authoritative direct HP loss percentages to chat details. */
    private String moveLogText() {
        if (pendingEngineState == null) return moveText;
        var hp = new java.util.HashMap<String, Integer>();
        var losses = new java.util.LinkedHashMap<String, Double>();
        for (var event : engineEvents) {
            if (event.type().equals("MOVE") || event.type().equals("CHARGE")) break;
            if (!java.util.Set.of("DAMAGE", "HEAL", "SET_HP").contains(event.type())) continue;
            var pokemon = battlePokemon(event.targetId());
            if (pokemon == null) continue;
            int before =
                    hp.computeIfAbsent(
                            pokemon.id(),
                            id -> {
                                var hud = healthHud(id);
                                return hud == null ? pokemon.hp() : hud.healthAnimation.hp();
                            });
            hp.put(pokemon.id(), event.hp());
            if (event.type().equals("DAMAGE")
                    && event.direct()
                    && event.maxHp() > 0
                    && before > event.hp())
                losses.merge(
                        pokemon.id(), (before - event.hp()) * 100d / event.maxHp(), Double::sum);
        }
        if (losses.isEmpty()) return moveText;
        var details = new StringBuilder(moveText).append(" ( ");
        var number = java.text.NumberFormat.getNumberInstance();
        number.setMaximumFractionDigits(3);
        for (var entry : losses.entrySet()) {
            if (details.charAt(details.length() - 1) != ' ') details.append(" / ");
            details.append(name(battlePokemon(entry.getKey())))
                    .append(" -")
                    .append(number.format(entry.getValue()))
                    .append('%');
        }
        return details.append(" )").toString();
    }

    private void stopMove() {
        if (moveAnimation != null) moveAnimation.close();
        moveAnimation = null;
    }

    private void escape() {
        if (enginePending || pendingEngineState != null) return;
        if (capturing()
                || switchTarget != null
                || escaping
                || encounter == null
                || !transition.ready(clock.getAsLong())) return;
        if (encounter.command(EncounterData.RUN) != EncounterData.NORMAL_SUCCESS) return;
        escaping = true;
        battleMessages.accept(escapeText);
        escapedAt = clock.getAsLong();
        transition.escape(escapedAt);
        for (var button : actions) button.setVisible(false);
        message.setText("");
        message.setVisible(true);
        invalidateLayout();
    }

    public void dismiss() {
        skipEffectTestAnimations = false;
        effectTestActive = false;
        stopAnimationPreview();
        engineEvents.clear();
        presentedBoosts.clear();
        presentedStatuses.clear();
        stopSettlement();
        if (testMovePicker != null) testMovePicker.closePopup();
        engineVersion++;
        enginePending = false;
        pendingEngineState = null;
        engineState = null;
        engineChoose = null;
        if (engineClose != null) {
            var close = engineClose;
            engineClose = null;
            close.run();
        }
        actionQueue.clear();
        activeAction = null;
        targetChosen = null;
        stopCapture();
        atmosphere.close();
        stopSwitch();
        if (choices != null) choices.close();
        stopMove();
        stopVanities();
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
        if (choices != null) {
            choices.setPosition(x, y + h - choices.panelHeight());
            choices.setSize(w, choices.panelHeight());
        }
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
        specifiedMove.setPosition(moveReturn.getX() - 148, moveReturn.getY());
        specifiedMove.setSize(140, 30);
        // BattlePokemonHealthHud.V6 applies an additional -55 for the normal PC self HUD.
        playerHud.setPosition(x + w - playerHud.getWidth(), y + h - 184 - 55);
        int groupBoostHeight =
                enemyHuds.stream().mapToInt(HealthHud::boostRowsHeight).max().orElse(0);
        for (var hud : enemyHuds) {
            if (encounter.horde()) {
                int slot = hud.pokemon.position();
                hud.setSize(260, 33);
                // Official PC capture: three headers above, two below; native compact HUD width.
                hud.setPosition(
                        x + w / 2 - 130 - 10 + EncounterFormation.hudColumn(slot) * 280,
                        y
                                + Math.max(60, groupBoostHeight + 26)
                                + EncounterFormation.hudRow(slot)
                                        * Math.max(50, groupBoostHeight + 63));
            } else hud.setPosition(x, getY() + 140);
        }
        party.setPosition(x + w - 212, getY() + h - 100);
        party.setSize(174, 24);
        message.setPosition(panel.getX(), panel.getY());
        message.setSize(panel.getWidth(), panel.getHeight());
    }

    private void paintField() {
        if (encounter == null) return;
        var fieldAnimation =
                moveAnimation != null
                        ? moveAnimation
                        : settlementEffect == null
                                ? null
                                : settlementEffect.animation(clock.getAsLong());
        long elapsed = transition.entranceTime(clock.getAsLong());
        // Sprite composition uses its own FBO. Finish it before entering the battle FBO.
        var wildImages = encounter.wilds().stream().map(p -> pokemonImage(p, false)).toList();
        var playerImage = pokemonImage(encounter.player(), true);
        if (moveAnimation != null)
            moveAnimation.drawBackground(
                    renderer, field.getX(), field.getY(), fieldWidth, fieldHeight);
        var backdrop =
                scene.image(
                        fieldWidth,
                        fieldHeight,
                        elapsed,
                        fieldAnimation,
                        () -> {
                            var order = new java.util.ArrayList<Integer>();
                            for (int i = 0; i < encounter.wilds().size(); i++) order.add(i);
                            order.sort(
                                    java.util.Comparator.comparingDouble(
                                                    (Integer i) ->
                                                            scene.camera()
                                                                    .position
                                                                    .dst2(
                                                                            EncounterFormation
                                                                                    .enemy(
                                                                                            encounter
                                                                                                    .horde(),
                                                                                            encounter
                                                                                                    .wilds()
                                                                                                    .get(
                                                                                                            i)
                                                                                                    .position())))
                                            .reversed());
                            for (int i : order)
                                drawPokemon(encounter.wilds().get(i), false, wildImages.get(i));
                            drawPokemon(encounter.player(), true, playerImage);
                        });
        // Ambient light belongs to the model environment. Sprites and the composed frame are
        // not multiplied by it (GdxBattleShader only lights meshes with normals).
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
        if (settlementEffect != null)
            settlementEffect.draw(
                    clock.getAsLong(),
                    scene,
                    renderer,
                    field.getX(),
                    field.getY(),
                    fieldWidth,
                    fieldHeight);
        // The trainer/ball animation starts after the PC viewport reaches full size.
        if (transition.expansion(clock.getAsLong()) >= 1) {
            long ownElapsed = ownEntranceTime(clock.getAsLong());
            entrance.drawTrainer(ownElapsed, field.getX(), field.getY(), fieldWidth, fieldHeight);
            entrance.drawBall(
                    ownElapsed,
                    scene,
                    renderer,
                    getAnimationState(),
                    field.getX(),
                    field.getY(),
                    fieldWidth,
                    fieldHeight);
            entrance.drawRelease(
                    ownElapsed,
                    scene,
                    renderer,
                    field.getX(),
                    field.getY(),
                    fieldWidth,
                    fieldHeight);
        }
        if (switchTarget != null && switchSpawnAt < 0)
            entrance.drawRecall(
                    clock.getAsLong() - switchStartedAt,
                    scene,
                    renderer,
                    getAnimationState(),
                    field.getX(),
                    field.getY(),
                    fieldWidth,
                    fieldHeight);
        // Gdx3DModelBatch cases 13/15 queue vanity only after the corresponding appearance.
        if (elapsed >= 2500)
            for (var vanity : enemyVanities)
                vanity.draw(
                        clock.getAsLong(),
                        scene,
                        renderer,
                        field.getX(),
                        field.getY(),
                        fieldWidth,
                        fieldHeight);
        if (transition.ready(clock.getAsLong()) && switchTarget == null && playerVanity != null)
            playerVanity.draw(
                    clock.getAsLong(),
                    scene,
                    renderer,
                    field.getX(),
                    field.getY(),
                    fieldWidth,
                    fieldHeight);
        if (captureAnimation != null)
            captureAnimation.draw(
                    scene,
                    renderer,
                    getAnimationState(),
                    field.getX(),
                    field.getY(),
                    fieldWidth,
                    fieldHeight);
        atmosphere.draw(
                clock.getAsLong(),
                scene,
                renderer,
                field.getX(),
                field.getY(),
                fieldWidth,
                fieldHeight);
        // BaseBattleAnimation animates SE0, but Gdx3DModelBatch does not draw that state as a
        // foreground quad. Compositing it here hides backgrounds, combatants and particles.
    }

    private Image pokemonImage(Pokemon pokemon, boolean own) {
        var actor = animationActor(pokemon);
        return images.animated(
                pokemon.species(),
                pokemon.form(),
                pokemon.shiny(),
                own,
                actor == null ? clock.getAsLong() / 1000d : actor.animationSeconds);
    }

    private void drawPokemon(Pokemon pokemon, boolean own, Image image) {
        if (engineState != null && faintedParticipants.contains(pokemon.id())) return;
        var actor = animationActor(pokemon);
        if (actor != null && actor.opacity == 0) return;
        long elapsed = transition.entranceTime(clock.getAsLong());
        if (own) elapsed = ownEntranceTime(clock.getAsLong());
        float recall = own ? recallProgress() : 0;
        float growing = own ? entrance.pokemonScale(elapsed) * (1 - recall) : 1;
        if (captureActor(pokemon)) growing *= captureAnimation.scale();
        if (growing <= 0) return;
        // BattlePokemonCombatant.ZK: single battle MS / Lt0 centers and the normal vertical offset.
        var center =
                own
                        ? new com.badlogic.gdx.math.Vector3(
                                3, 3.1f + entrance.height(elapsed) - .5f * recall, 4)
                        : EncounterFormation.enemy(encounter.horde(), pokemon.position());
        if (actor != null) {
            center.set(actor.center);
            if (activeAction != null && !pokemon.id().equals(activeAction.caster().id()))
                center.add(
                        combatPosition(pokemon).sub(combatPosition(activeAction.targets().get(0))));
        }
        if (captureActor(pokemon)) center.y += captureAnimation.height();
        float scale = (pokemon.alpha() ? .015f : .01f) * growing;
        float width = image.getWidth() * scale * (actor == null ? 1 : actor.scaleX),
                height = image.getHeight() * scale * (actor == null ? 1 : actor.scaleY);
        // Default ROM sprites have PokemonSpriteManager.L4 == (0,0,0).
        // Gdx3DModelBatch.Vy subtracts its offset; the enemy therefore moves +0.075 on X.
        var shadowCenter = center.cpy();
        shadowCenter.x += own ? 0 : .075f;
        shadowCenter.y = 2.6f;
        shadowCenter.z -= own ? .65f : .8f;
        float shadowScale =
                .015f * (scale / .01f * (actor == null ? 1 : actor.scaleX) + (center.y - 3.1f) / 2);
        if (own || !encounter.horde())
            renderer.drawBattleShadow(
                    image,
                    shadowCenter,
                    image.getWidth() * shadowScale,
                    image.getHeight() * shadowScale,
                    scene.camera());
        renderer.drawBattleCombatant(
                image,
                center,
                width,
                height,
                scene.camera(),
                actor == null ? (own || captureActor(pokemon) ? 1 : 0) : actor.color.r,
                actor == null ? (own || captureActor(pokemon) ? 1 : 0) : actor.color.g,
                actor == null ? (own || captureActor(pokemon) ? 1 : 0) : actor.color.b,
                actor == null
                        ? (own
                                ? Math.max(recall, entrance.white(elapsed))
                                : captureActor(pokemon)
                                        ? captureAnimation.shade()
                                        : entrance.wildShade(elapsed))
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
        private final BattleHealthAnimation healthAnimation;

        private void updateHealth(long now) {
            healthAnimation.update(now);
            updateStatus();
            health.setText(
                    own || testMode ? healthAnimation.hp() + " / " + healthAnimation.maxHp() : "");
        }

        private final boolean own;

        private final Label title = new Label(), health = new Label();
        private final Label[] boostLabels = new Label[7];
        private final Widget statusIcon =
                new Widget() {
                    @Override
                    protected void paintWidget(GUI gui) {
                        int index = presentedStatuses.getOrDefault(pokemon.id(), -1);
                        if (index >= 0) {
                            var icon = images.atlas("status_ailment_large", index);
                            if (icon != null)
                                icon.draw(getAnimationState(), getX(), getY(), 16, 16);
                        }
                    }
                };

        /** BattlePokemonHealthHud.z2 / up: native icon and localized hover text, left of name. */
        private void updateStatus() {
            int index = presentedStatuses.getOrDefault(pokemon.id(), -1);
            int textId =
                    switch (index) {
                        case 0 -> 5223;
                        case 1 -> 5224;
                        case 2 -> 5222;
                        case 3 -> 5221;
                        case 4 -> 5220;
                        case 6 -> 5225;
                        default -> 0;
                    };
            statusIcon.setVisible(textId != 0 && healthAnimation.hp() > 0);
            statusIcon.setTooltipContent(textId == 0 ? null : resources.strings().get(textId));
        }

        @Override
        public boolean isInside(int x, int y) {
            return super.isInside(x, y)
                    || title.isInside(x, y)
                    || health.isInside(x, y)
                    || statusIcon.isVisible() && statusIcon.isInside(x, y)
                    || java.util.Arrays.stream(boostLabels)
                            .anyMatch(
                                    label ->
                                            label != null
                                                    && label.isVisible()
                                                    && label.isInside(x, y));
        }

        /** PokemonStatTextFormatter.vq / gc_2.mi: signed nonzero stages in native order. */
        private void updateBoostTooltip() {
            String[] keys = {"atk", "def", "spa", "spd", "spe", "evasion", "accuracy"};
            int[] labels = {501, 502, 504, 505, 503, 507, 506};
            String[] fallback = {
                "ATTACK", "DEFENSE", "SP. ATTACK", "SP. DEFENSE", "SPEED", "EVASION", "ACCURACY"
            };
            var boosts = presentedBoosts.getOrDefault(pokemon.id(), java.util.Map.of());
            var text = new StringBuilder(title.getText());
            boolean first = true;
            for (int i = 0; i < keys.length; i++) {
                int value = boosts.getOrDefault(keys[i], 0);
                var label = boostLabels[i];
                String statName = resources.strings().getOrDefault(labels[i], fallback[i]);
                if (label != null) {
                    label.setVisible(value != 0);
                    if (value != 0) {
                        String theme = value > 0 ? "/encounter-stat-boost" : "/encounter-stat-drop";
                        if (!theme.equals(label.getTheme())) {
                            label.setTheme(theme);
                            label.reapplyTheme();
                        }
                        label.setText((value > 0 ? "+" : "") + value + " " + statName);
                    }
                }
                if (value == 0) continue;
                if (first) {
                    text.append("\n");
                    first = false;
                }
                text.append("\n")
                        .append(resources.strings().getOrDefault(labels[i], fallback[i]))
                        .append(": ")
                        .append(value > 0 ? "+" : "")
                        .append(value);
            }
            String tooltip = text.toString();
            if (!tooltip.equals(title.getTooltipContent())) {
                title.setTooltipContent(tooltip);
                setTooltipContent(tooltip);
                invalidateLayout();
                EncounterWindow.this.invalidateLayout();
            }
        }

        HealthHud(Pokemon p, boolean own) {
            this.pokemon = p;
            this.own = own;
            healthAnimation = new BattleHealthAnimation(p.hp(), p.maxHp());
            setTheme("");
            setSize(299, own ? 40 : 28);
            // BattlePokemonHealthHud.le0: one measured name/level label, followed by a gender
            // image.
            title.setText(name(p) + " " + resources.strings().get(59) + " " + p.level());
            health.setText(own || testMode ? p.hp() + " / " + p.maxHp() : "");
            for (var label : new Label[] {title, health}) {
                label.setTheme("/encounter-label");
                add(label);
            }
            health.setTheme("/encounter-health");
            statusIcon.setTheme("");
            statusIcon.setCanAcceptKeyboardFocus(false);
            statusIcon.setTooltipDelay(100);
            add(statusIcon);
            updateStatus();
            for (int i = 0; i < boostLabels.length; i++) {
                var label = new Label();
                label.setTheme("/encounter-stat-boost");
                label.setCanAcceptKeyboardFocus(false);
                label.setVisible(false);
                boostLabels[i] = label;
                add(label);
            }
            title.setTooltipDelay(100);
            setTooltipDelay(100);
            updateBoostTooltip();
        }

        private int boostRowsHeight() {
            int width =
                    own || encounter.horde()
                            ? getWidth() - 40
                            : Math.max(40, EncounterWindow.this.getRight() - getRight() - 16);
            int x = 0, total = 0, row = 0;
            for (var label : boostLabels)
                if (label.isVisible()) {
                    int w = label.getPreferredWidth(), h = label.getPreferredHeight();
                    if (x > 0 && x + w > width) {
                        total += row + 3;
                        x = 0;
                        row = 0;
                    }
                    row = Math.max(row, h);
                    x += w + 3;
                }
            return total + row;
        }

        private void layoutBoostLabels(boolean floating) {
            boolean above = own || floating;
            int left = above ? title.getX() : getRight() + 8;
            int limit = above ? getX() + getWidth() : EncounterWindow.this.getRight() - 8;
            int x = left, y = above ? title.getY() - boostRowsHeight() - 4 : getY() - 4, row = 0;
            for (var label : boostLabels)
                if (label.isVisible()) {
                    int width = label.getPreferredWidth(), height = label.getPreferredHeight();
                    if (x > left && x + width > limit) {
                        x = left;
                        y += row + 3;
                        row = 0;
                    }
                    label.setPosition(x, y);
                    label.setSize(width, height);
                    x += width + 3;
                    row = Math.max(row, height);
                }
        }

        @Override
        protected void layout() {
            boolean floating = !own && encounter.horde();
            title.setPosition(
                    getX()
                            + (floating
                                    ? (getWidth() - title.getPreferredWidth()) / 2
                                    : own ? 40 : 45),
                    getY() - 22);
            title.setSize(title.getPreferredWidth(), 20);
            statusIcon.setPosition(title.getX() - 24, getY() - 15);
            statusIcon.setSize(16, 16);
            health.setPosition(
                    getX() + (floating ? 22 : own ? 40 : 45), getY() + (floating ? 19 : 13));
            health.setSize(floating ? getWidth() - 44 : 220, 20);
            layoutBoostLabels(floating);
        }

        @Override
        protected void paintWidget(GUI gui) {
            // Official PC capture uses the compact HUD: retain native skin height, no vertical
            // stretch.
            boolean floating = !own && encounter.horde();
            boolean caught = !own && pokedexState.has(pokemon.species(), 1);
            if (floating)
                image(hudTexture, 326, 65, 239, 28)
                        .draw(getAnimationState(), getX(), getY(), getWidth(), 28);
            else
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
                                title.getRight()
                                        + (caught
                                                ? (pokemon.gender() < 0 ? 31 : 45)
                                                : pokemon.gender() < 0 ? 3 : 22),
                                getY() - 20,
                                16,
                                14);
            // Reference Jx0: fixed Poke Ball #3 denotes a species already owned in the dex.
            if (caught)
                images.ball(3)
                        .draw(
                                getAnimationState(),
                                title.getRight() + (pokemon.gender() < 0 ? 8 : 22),
                                getY() - 19,
                                16,
                                16);
            // Official PC horde capture: the float skin has a centered, inset HP track.
            int width = floating ? getWidth() - 44 : 220,
                    bx = getX() + (floating ? 22 : own ? 40 : 45),
                    by = getY() + (floating ? 9 : 2);
            // gfx.xml health-progressbar: preserve the end caps, stretch only the center.
            image(hudTexture, 35, 8, 2, 6).draw(getAnimationState(), bx, by, 2, 8);
            image(hudTexture, 37, 8, 1, 6).draw(getAnimationState(), bx + 2, by, width - 4, 8);
            image(hudTexture, 46, 8, 2, 6).draw(getAnimationState(), bx + width - 2, by, 2, 8);
            int color =
                    healthAnimation.hp() * 4 <= healthAnimation.maxHp()
                            ? 20
                            : healthAnimation.hp() * 2 <= healthAnimation.maxHp() ? 22 : 21;
            image(hudTexture, color, 9, 1, 4)
                    .draw(
                            getAnimationState(),
                            bx + 1,
                            by + 1,
                            Math.max(
                                    0,
                                    (width - 2) * healthAnimation.hp() / healthAnimation.maxHp()),
                            6);
        }
    }

    @Override
    protected boolean handleEvent(Event event) {
        return super.handleEvent(event)
                || event.isMouseEvent() && isInside(event.getMouseX(), event.getMouseY());
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
