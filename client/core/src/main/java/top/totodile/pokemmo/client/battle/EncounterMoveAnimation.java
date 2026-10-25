package top.totodile.pokemmo.client.battle;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;

import top.totodile.pokemmo.client.battle.programs.ExtendedMoveScripts;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.client.ui.pokemon.AttributeParticleScene;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipInputStream;

/**
 * BaseBattleAnimation adapter for the single-player preview. ReferenceMoveScripts preserves the
 * original nested timelines. Audio calls are intentionally inert.
 */
public final class EncounterMoveAnimation implements AutoCloseable {

    static final class Combatant {

        final Vector3 center;

        float scaleX = 1, scaleY = 1, shade, opacity = 1, animationSpeed = 1;

        double animationSeconds;

        boolean frozen, hideHud;

        final Color color = new Color(Color.BLACK);

        Combatant(float x, float y, float z) {
            center = new Vector3(x, y, z);
        }
    }

    private record Effect(AttributeParticleScene particles, float start, float duration) {}

    public final Object casterHandle = new Object();

    public final Object targetHandle = new Object();

    public int chargeCount;

    ReferenceMoveTimeline timeline;

    final Combatant own = new Combatant(3, 3.1f, 4), enemy = new Combatant(5, 3.08f, 1);
    final Vector3 cameraPosition = new Vector3(3.5f, 3.4f, 2.5f),
            cameraTarget = new Vector3(3.5f, 3.25f, 4);

    float backgroundShade;

    final Color backgroundColor = new Color(Color.BLACK);

    final Color[] modelTints = {
        new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 0)
    };

    final Color screenOverlay = new Color(0, 0, 0, 0),
            backgroundImageColor = new Color(Color.WHITE);
    boolean hidePlatforms, useBackground, backgroundEnabled;

    int backgroundImage = -1;

    float progressX, progressY, velocityX, velocityY, scrollLimit;

    private float previousTime;

    private final Map<String, String> particleConfigurations = new HashMap<>();

    private final List<Effect> effects = new ArrayList<>();

    private final PokedexImages images;

    private final long started;

    private float time;

    private int moveId;

    private boolean pendingAttack;

    private float stageStarted;

    private boolean playerSide = true;
    private java.util.List<Vector3> targetPositions = java.util.List.of(new Vector3(5, 3.08f, 1));
    private int particleDirection = 8;

    private boolean particleTargetSpawn = true;

    /** The runtime registry is the catalogue authority, including custom event IDs. */
    public static java.util.List<Integer> registeredMoves() {
        return java.util.stream.IntStream.rangeClosed(0, Short.MAX_VALUE)
                .filter(EncounterMoveAnimation::supports)
                .boxed()
                .toList();
    }

    static boolean supports(int move) {
        return ReferenceMoveScripts.effects(move) != null
                || ExtendedMoveScripts.effects(move) != null;
    }

    private final int heldItem, casterType1, casterType2;

    EncounterMoveAnimation(
            ClientResources resources,
            PokedexImages images,
            int move,
            long now,
            top.totodile.pokemmo.server.game.PokemonData.Pokemon caster,
            top.totodile.pokemmo.client.rom.nds.bw.BwPokedex dex)
            throws IOException {
        this(resources, images, move, now, caster, dex, new Vector3(5, 3.08f, 1));
    }

    EncounterMoveAnimation(
            ClientResources resources,
            PokedexImages images,
            int move,
            long now,
            top.totodile.pokemmo.server.game.PokemonData.Pokemon caster,
            top.totodile.pokemmo.client.rom.nds.bw.BwPokedex dex,
            Vector3 targetPosition)
            throws IOException {
        this(
                resources,
                images,
                move,
                now,
                caster,
                dex,
                new Vector3(3, 3.1f, 4),
                java.util.List.of(targetPosition),
                true);
    }

    EncounterMoveAnimation(
            ClientResources resources,
            PokedexImages images,
            int move,
            long now,
            top.totodile.pokemmo.server.game.PokemonData.Pokemon caster,
            top.totodile.pokemmo.client.rom.nds.bw.BwPokedex dex,
            Vector3 casterPosition,
            java.util.List<Vector3> targets,
            boolean playerSide)
            throws IOException {
        this.playerSide = playerSide;
        this.targetPositions = targets.stream().map(Vector3::cpy).toList();
        own.center.set(casterPosition);
        enemy.center.set(targets.get(0));
        if (!supports(move))
            throw new IllegalArgumentException("No reference animation adapter for move " + move);
        this.images = images;
        started = now;
        own.animationSeconds = enemy.animationSeconds = now / 1000d;
        heldItem = caster.item();
        casterType1 = dex.get(caster.species()).type1();
        casterType2 = dex.get(caster.species()).type2();
        // preloadParticleEffect/Jv: each emitParticles selects a controller, never the entire
        // effect.
        Set<String> needed = new HashSet<>();
        var previous = ReferenceMoveScripts.effects(move);
        if (previous != null) for (int id : previous) needed.add(effectPath(id));
        else needed.addAll(ExtendedMoveScripts.assets(move));
        if (!needed.isEmpty())
            try (var zip = new ZipInputStream(resources.apply("sprites/particles.pak").read())) {
                for (var e = zip.getNextEntry(); e != null; e = zip.getNextEntry())
                    for (String path : needed)
                        if (e.getName().equals("particle/" + path))
                            particleConfigurations.put(
                                    path, new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            }
        if (particleConfigurations.size() != needed.size())
            throw new IOException("Missing reference move particle resources " + needed);
        moveId = move;
        pendingAttack =
                Set.of(13, 19, 76, 91, 117, 130, 143, 291, 340, 467, 507, 553, 554).contains(move);
        timeline =
                pendingAttack
                        ? ExtendedMoveScripts.buildCharge(this, move)
                        : buildMoveAnimation(move);
        if (timeline == null) timeline = ReferenceMoveTimeline.sequence();
    }

    /** ShinyParticleEffect: the six original controller emissions run in parallel. */
    static EncounterMoveAnimation shinyEntrance(
            ClientResources resources, PokedexImages images, Vector3 origin) throws IOException {
        Map<String, String> sources = new HashMap<>();
        for (int id : new int[] {152, 422})
            sources.put(effectPath(id), EncounterVanityEffect.read(resources, effectPath(id)));
        var animation = new EncounterMoveAnimation(images, sources);
        animation.own.center.set(origin);
        animation.enemy.center.set(origin);
        animation.timeline = ReferenceMoveTimeline.parallel();
        for (int spawn : new int[] {9, 13}) {
            animation.timeline.append(animation.emitParticles(-1, 152, 0, spawn, 8, .4f));
            animation.timeline.append(animation.emitParticles(-1, 152, 1, spawn, 8, .4f));
            animation.timeline.append(animation.emitParticles(-1, 422, 2, spawn, 8, .4f));
        }
        return animation;
    }

    /** BattleKs0Animation.us, played on HP restoration by BattleMultiHitSequenceTask. */
    static EncounterMoveAnimation healing(
            ClientResources resources, PokedexImages images, Vector3 origin) throws IOException {
        var animation =
                new EncounterMoveAnimation(
                        images,
                        Map.of(
                                effectPath(155),
                                EncounterVanityEffect.read(resources, effectPath(155))));
        animation.own.center.set(origin);
        animation.enemy.center.set(origin);
        animation.timeline =
                ReferenceMoveTimeline.sequence()
                        .waitSeconds(.6f)
                        .beginParallel()
                        .append(animation.emitParticles(-1, 155, 1, 9, 8, .4f))
                        .append(
                                ReferenceMoveTimeline.sequence()
                                        .waitSeconds(.6f)
                                        .append(animation.emitParticles(-1, 155, 0, 9, 8, .4f)))
                        .endGroup()
                        .waitSeconds(.6f);
        return animation;
    }

    private EncounterMoveAnimation(PokedexImages images, Map<String, String> sources) {
        this.images = images;
        started = 0;
        heldItem = casterType1 = casterType2 = 0;
        particleConfigurations.putAll(sources);
    }

    /** BattleAction024Packet mode 1: use the special recurring effect, never the move animation. */
    static EncounterMoveAnimation trappingResidual(
            ClientResources resources, PokedexImages images, Vector3 origin, int move)
            throws IOException {
        var sources = new java.util.HashMap<String, String>();
        for (int effect :
                top.totodile.pokemmo.client.battle.programs.TrappingResidualPrograms.effects(move))
            sources.put(
                    effectPath(effect), EncounterVanityEffect.read(resources, effectPath(effect)));
        var animation = new EncounterMoveAnimation(images, sources);
        animation.own.center.set(origin);
        animation.enemy.center.set(origin);
        animation.timeline =
                top.totodile.pokemmo.client.battle.programs.TrappingResidualPrograms.build(
                        animation, move);
        return animation;
    }

    /** Data-only construction for validating every imported recipe without a GL context. */
    EncounterMoveAnimation(Map<String, String> particleSources, int item, int type1, int type2) {
        images = null;
        started = 0;
        heldItem = item;
        casterType1 = type1;
        casterType2 = type2;
        particleConfigurations.putAll(particleSources);
    }

    /** Live battle phases are separate turns; only legacy animation previews concatenate them. */
    void enginePhase(boolean charging) {
        pendingAttack = false;
        timeline =
                charging
                        ? ExtendedMoveScripts.buildCharge(this, moveId)
                        : buildMoveAnimation(moveId);
        if (timeline == null) timeline = ReferenceMoveTimeline.sequence();
    }

    void update(long now) {
        time = Math.max(0, now - started) / 1000f;
        timeline.update(time - stageStarted);
        float delta = time - previousTime;
        previousTime = time;
        if (progressX < scrollLimit) progressX += delta;
        if (progressY < scrollLimit) progressY += delta;
        if (!own.frozen) own.animationSeconds += delta * own.animationSpeed;
        if (!enemy.frozen) enemy.animationSeconds += delta * enemy.animationSpeed;
        if (pendingAttack && stageComplete(time)) {
            pendingAttack = false;
            stageStarted = time;
            effects.forEach(effect -> effect.particles.close());
            effects.clear();
            timeline = buildMoveAnimation(moveId);
            if (timeline == null) timeline = ReferenceMoveTimeline.sequence();
        }
    }

    private boolean stageComplete(float elapsed) {
        return elapsed - stageStarted >= timeline.duration()
                && effects.stream().allMatch(effect -> effect.particles.isComplete());
    }

    boolean complete(long now) {
        return !pendingAttack && stageComplete((now - started) / 1000f);
    }

    void camera(PerspectiveCamera camera) {
        // CameraTweenAccessor 4/9 and OverworldCamera.JP: logical position + Rg0 (3), PB(-10).
        camera.up.set(Vector3.Y);
        camera.position.set(cameraPosition).add(0, 0, 3);
        camera.lookAt(cameraTarget);
        camera.rotateAround(cameraTarget, camera.direction.cpy().crs(camera.up).nor(), -10);
        camera.update();
    }

    void draw(
            GdxTwlRenderer renderer,
            EncounterScene scene,
            long currentTime,
            int viewportX,
            int viewportY,
            int viewportWidth,
            int viewportHeight) {
        for (var effect : effects)
            effect.particles.drawAt(
                    renderer,
                    Math.max(0, time - effect.start),
                    viewportX,
                    viewportY,
                    viewportWidth,
                    viewportHeight,
                    scene.camera());
    }

    void drawBackground(
            GdxTwlRenderer renderer, int viewportX, int viewportY, int width, int height) {
        if (backgroundImage >= 0 && backgroundEnabled && useBackground)
            renderer.drawBattleBackground(
                    images.battleBackground(backgroundImage),
                    viewportX,
                    viewportY,
                    width,
                    height,
                    (int) (progressX * 100 * velocityX),
                    (int) (progressY * 100 * velocityY),
                    backgroundImageColor);
    }

    private Combatant target(int targetId) {
        return targetId == 14 ? own : enemy;
    }

    public ReferenceMoveTimeline focusCamera(int focusTarget, float duration) {
        // BaseBattleAnimation.focusCamera: player is the local (Ez0) side.
        var center = (focusTarget == 0 ? own : enemy).center.cpy();
        if (focusTarget == 2 || focusTarget == 3) center.set(3, 3.02f, 4);
        else if (focusTarget == 4) center.set(4, 3.12f, 4.4f);
        boolean far = focusTarget == 1;
        var look = center.cpy().add(0, far ? -.25f : 0, far ? -.5f : -.25f);
        var position =
                center.cpy()
                        .add(
                                0,
                                far ? .075f : .25f,
                                far || focusTarget == 2 || focusTarget == 3 ? -.5f : -1);
        return ReferenceMoveTimeline.parallel()
                .append(vector(this.cameraTarget, look, duration))
                .append(vector(cameraPosition, position, duration));
    }

    public ReferenceMoveTimeline restoreCamera(float duration) {
        return ReferenceMoveTimeline.parallel()
                .append(vector(cameraPosition, new Vector3(3.5f, 3.4f, 2.5f), duration))
                .append(vector(cameraTarget, new Vector3(3.5f, 3.25f, 4), duration));
    }

    private ReferenceMoveTimeline vector(
            Vector3 currentPosition, Vector3 targetPosition, float duration) {
        return ReferenceMoveTimeline.tween(
                duration,
                () -> new float[] {currentPosition.x, currentPosition.y, currentPosition.z},
                components -> currentPosition.set(components[0], components[1], components[2]),
                targetPosition.x,
                targetPosition.y,
                targetPosition.z);
    }

    public Runnable preloadParticleEffect(int id) {
        return () -> {};
    }

    private static String effectPath(int id) {
        return (id >= 1000 ? "custom/" : "auto/") + id + ".vfx";
    }

    public int heldItem() {
        return heldItem;
    }

    public int casterType() {
        return casterType1;
    }

    /**
     * SecretPowerAnimation dispatch. The current encounter simulator has no terrain packet, so it
     * takes the reference's unspecified-environment branch.
     */
    public int secretPowerMove() {
        return secretPowerMove(-1);
    }

    public static int secretPowerMove(int terrain) {
        return switch (terrain) {
            case 7, 8, 6 -> 88;
            case 4, 5, 3 -> 352;
            case 0, 1 -> 302;
            case 2 -> 28;
            default -> 34;
        };
    }

    public ReferenceMoveTimeline buildMoveAnimation(int move) {
        return ReferenceMoveScripts.effects(move) != null
                ? ReferenceMoveScripts.build(this, move)
                : ExtendedMoveScripts.build(this, move);
    }

    public boolean hasCasterType(int type) {
        return casterType1 == type || casterType2 == type;
    }

    public boolean isPlayerSide() {
        return playerSide;
    }

    public void resetSpriteState() {
        own.opacity = 1;
        chargeCount = 0;
    }

    public ReferenceMoveTimeline animateSpriteSpeed(int targetId, float duration, float speed) {
        var actor = target(targetId);
        return scalar(
                duration, () -> actor.animationSpeed, value -> actor.animationSpeed = value, speed);
    }

    public Runnable playCustomParticles(int id) {
        return playCustomParticles(Integer.toString(id));
    }

    public Runnable playCustomParticles(String name) {
        return () -> {
            var effect =
                    new JsonReader().parse(particleConfigurations.get("custom/" + name + ".vfx"));
            float length = 0;
            for (var controller : effect.get("resource").get("controllers"))
                length = Math.max(length, duration(controller));
            effects.add(
                    new Effect(
                            new AttributeParticleScene(
                                    images,
                                    effect,
                                    own.center,
                                    own.center,
                                    enemy.center,
                                    false,
                                    8,
                                    true),
                            stageStarted + timeline.time(),
                            length));
        };
    }

    public Runnable sceneFlag(int sceneFlag, boolean enabled) {
        return new ReferenceSceneFlag(this, sceneFlag, enabled);
    }

    public ReferenceMoveTimeline setCombatantVisibility(int targetId, int mode) {
        return ReferenceMoveTimeline.call(
                () -> {
                    if (mode == 3) target(targetId).opacity = 0;
                    else if (mode == 4) target(targetId).opacity = 1;
                });
    }

    public Runnable loadBattleBackground(int id) {
        return loadTintedBattleBackground(id, Color.WHITE);
    }

    public Runnable loadTintedBattleBackground(int id, Color color) {
        return () -> {
            images.battleBackground(id);
            backgroundImage = id;
            backgroundImageColor.set(color);
            progressX = progressY = velocityX = velocityY = 0;
        };
    }

    public ReferenceMoveTimeline scrollBattleBackground(int type, int x, int y, float duration) {
        return ReferenceMoveTimeline.call(
                        () -> {
                            if (type == 0) {
                                progressX = x;
                                progressY = y;
                            } else if (type == 1) {
                                velocityX = x / 60f;
                                velocityY = y / 60f;
                            } else if (type == 4) {
                                velocityX = x / 4f;
                                velocityY = y / 4f;
                            }
                            scrollLimit = duration * 1000;
                        })
                .waitSeconds(duration);
    }

    public ReferenceMoveTimeline emitParticles(
            int targetIndex,
            int effectId,
            int controllerIndex,
            int spawnType,
            int direction,
            float heightOffset) {
        return particle(
                targetIndex, effectId, controllerIndex, spawnType, direction, heightOffset, 0);
    }

    public ReferenceMoveTimeline launchParticles(
            int effectId,
            int controllerIndex,
            int spawnType,
            int direction,
            float heightOffset,
            float travelMillis) {
        return particle(
                -1, effectId, controllerIndex, spawnType, direction, heightOffset, travelMillis);
    }

    private ReferenceMoveTimeline particle(
            int targetIndex,
            int effectId,
            int controllerIndex,
            int spawnType,
            int direction,
            float heightOffset,
            float travelMillis) {
        if (targetIndex >= targetPositions.size() || !Set.of(0, 1, 8, 9, 11).contains(direction))
            throw new IllegalArgumentException("Unsupported reference particle target");
        // The source preserves the previous spawn side for unknown target codes
        // (including SunnyDayAnimation's literal -4096), rather than resetting it.
        if (Set.of(0, 1, 2, 3, 4, 9, 11, 13).contains(spawnType))
            particleTargetSpawn = Set.of(1, 2, 3, 11).contains(spawnType);
        final boolean targetSpawn = particleTargetSpawn;
        if (direction != 0) particleDirection = direction;
        final int resolvedDirection = particleDirection;
        var original = new JsonReader().parse(particleConfigurations.get(effectPath(effectId)));
        var config = original.get("resource").get("controllers").get(controllerIndex);
        if (config == null) return ReferenceMoveTimeline.parallel();
        float duration = duration(config);
        return ReferenceMoveTimeline.call(
                        () -> {
                            var effect =
                                    new JsonReader()
                                            .parse(
                                                    particleConfigurations.get(
                                                            effectPath(effectId)));
                            var effectControllers = effect.get("resource").get("controllers");
                            var selectedController = effectControllers.get(controllerIndex);
                            int trailControllerIndex =
                                    selectedController.getInt("trailController", -1);
                            var trailController =
                                    trailControllerIndex < 0
                                            ? null
                                            : effectControllers.get(trailControllerIndex);
                            var batch =
                                    effect.get("unique").get("billboardBatchExt" + controllerIndex);
                            var childBatch =
                                    trailControllerIndex < 0
                                            ? null
                                            : effect.get("unique")
                                                    .get(
                                                            "billboardBatchExt"
                                                                    + trailControllerIndex);
                            var unique = new JsonValue(JsonValue.ValueType.object);
                            batch.remove();
                            unique.addChild("billboardBatchExt0", batch);
                            var controllers = new JsonValue(JsonValue.ValueType.array);
                            selectedController.remove();
                            controllers.addChild(selectedController);
                            if (trailController != null) {
                                trailController.remove();
                                controllers.addChild(trailController);
                                childBatch.remove();
                                unique.addChild("billboardBatchExt1", childBatch);
                                selectedController.get("trailController").set(new JsonValue(1));
                            }
                            effect.get("resource").remove("controllers");
                            effect.get("resource").addChild("controllers", controllers);
                            effect.remove("unique");
                            effect.addChild("unique", unique);
                            for (var influence : selectedController.get("influencers"))
                                if (influence
                                        .getString("class")
                                        .endsWith("ControllerSpawnInfluencer")) {
                                    influence.get("spawnType").get("value").set(new JsonValue(0));
                                    var adjustment = influence.get("spawnAdjustment");
                                    adjustment.remove("y");
                                    adjustment.addChild("y", new JsonValue(heightOffset - .5f));
                                }
                            if (travelMillis > 0)
                                injectTravel(selectedController, travelMillis, resolvedDirection);
                            boolean multiple =
                                    targetIndex < 0
                                            && (targetSpawn || travelMillis > 0)
                                            && spawnType != 3
                                            && spawnType != 4;
                            var positions =
                                    multiple
                                            ? targetPositions
                                            : java.util.List.of(
                                                    targetPositions.get(Math.max(0, targetIndex)));
                            for (var position : positions) {
                                var actualTarget =
                                        enemy.center
                                                .cpy()
                                                .add(position.cpy().sub(targetPositions.get(0)));
                                var origin = (targetSpawn ? actualTarget : own.center).cpy();
                                if (targetIndex >= 0) origin.set(actualTarget);
                                else if (spawnType == 3) origin.set(3, 3.02f, 4);
                                else if (spawnType == 4) origin.set(5, 3.02f, 1);
                                effects.add(
                                        new Effect(
                                                new AttributeParticleScene(
                                                        images,
                                                        effect,
                                                        origin,
                                                        own.center,
                                                        actualTarget,
                                                        targetSpawn,
                                                        resolvedDirection),
                                                stageStarted + timeline.time(),
                                                duration
                                                        + (trailController == null
                                                                ? 0
                                                                : duration(trailController))));
                            }
                        })
                .waitSeconds(duration);
    }

    /**
     * BaseBattleAnimation.launchParticles/createVectorPathModifier, using its default two endpoint
     * spline.
     */
    private void injectTravel(JsonValue controllerConfig, float travelMillis, int direction) {
        int directionalForceCount = 0;
        JsonValue dynamicsInfluencer = null;
        for (var influence : controllerConfig.get("influencers"))
            if (influence.getString("class").endsWith("DynamicsInfluencerExt")) {
                dynamicsInfluencer = influence;
                for (var modifier : influence.get("velocities")) {
                    String name = modifier.getString("class");
                    if (name.endsWith("$VectorPathModifier")) return;
                    if (name.endsWith("$DragForce")
                            || name.endsWith("$CentripetalAccelerationExt")
                            || name.endsWith("$CircuralModifier")) directionalForceCount++;
                }
            }
        if (dynamicsInfluencer == null)
            throw new IllegalArgumentException("Missing reference dynamics influencer for travel");
        var pathModifier =
                new JsonReader()
                        .parse(
                                "{class:com.badlogic.gdx.graphics.g3d.particles.DynamicsModifierExt$VectorPathModifier,isGlobal:false,strengthValue:{active:false,lowMin:0,lowMax:0,highMin:0,highMax:0,relative:false,scaling:[1],timeline:[0]},vectorArray:[{pos:{},offset:{},modifier:1},{pos:{},offset:{},modifier:2}],isContinuous:false,pathType:0,rotateTarget:false,degree:{value:0},speed:{active:true,value:1}}");
        pathModifier.get("strengthValue").get("relative").set(directionalForceCount > 0);
        pathModifier.get("speed").get("value").set(new JsonValue(travelMillis));
        if (direction != 11 && direction != 1) {
            pathModifier.get("vectorArray").get(0).get("modifier").set(new JsonValue(2));
            pathModifier.get("vectorArray").get(1).get("modifier").set(new JsonValue(1));
        }
        dynamicsInfluencer.get("velocities").addChild(pathModifier);
    }

    private float duration(JsonValue controllerConfig) {
        var emitterConfig = controllerConfig.get("emitter");
        return (emitterConfig.get("delay").getFloat("lowMax", 0)
                        + emitterConfig.get("duration").getFloat("lowMax", 0)
                        + emitterConfig.get("life").getFloat("highMax", 0))
                / 1000f;
    }

    public ReferenceMoveTimeline setSpriteAnimationPaused(int targetId, int pauseMode) {
        return ReferenceMoveTimeline.call(() -> target(targetId).frozen = pauseMode == 1);
    }

    public Runnable setHealthHudHidden(int id, boolean hidden) {
        return () -> {
            if (id == 14 || id == 18) own.hideHud = hidden;
            if (id == 16 || id == 18) enemy.hideHud = hidden;
        };
    }

    public ReferenceMoveTimeline tintCombatant(
            int targetId,
            float duration,
            float initialOpacity,
            float finalOpacity,
            Color overlayColor) {
        var actor = target(targetId);
        return ReferenceMoveTimeline.sequence()
                .onStart(
                        () -> {
                            actor.shade = initialOpacity;
                            actor.color.set(overlayColor);
                        })
                .append(
                        ReferenceMoveTimeline.tween(
                                duration,
                                () -> new float[] {actor.shade},
                                v -> actor.shade = v[0],
                                finalOpacity));
    }

    public ReferenceMoveTimeline tintEnvironment(
            int tintGroup,
            int colorValue,
            float initialOpacity,
            float finalOpacity,
            float duration) {
        if (tintGroup < 0 || tintGroup > 4)
            throw new IllegalArgumentException("Unsupported reference environment tint");
        var result = ReferenceMoveTimeline.parallel();
        if (tintGroup == 0 || tintGroup == 2 || tintGroup == 4) {
            result.append(tint(modelTints[1], colorValue, finalOpacity, duration * 10));
            result.append(tint(modelTints[2], colorValue, finalOpacity, duration * 10));
        }
        if (tintGroup == 1 || tintGroup == 2 || tintGroup == 4)
            result.append(tint(modelTints[0], colorValue, finalOpacity, duration * 10));
        if (tintGroup == 3 || tintGroup == 4)
            result.append(tint(screenOverlay, colorValue, initialOpacity, duration * 10));
        return result;
    }

    private ReferenceMoveTimeline tint(
            Color tint, int rgb555, float targetOpacity, float duration) {
        var targetColor = color(rgb555);
        return ReferenceMoveTimeline.tween(
                duration,
                () -> new float[] {tint.r, tint.g, tint.b, tint.a},
                components -> tint.set(components[0], components[1], components[2], components[3]),
                targetColor.r,
                targetColor.g,
                targetColor.b,
                targetOpacity);
    }

    public ReferenceMoveTimeline shakeCombatant(
            int targetId,
            int displacementMode,
            int repeatCount,
            float initialDuration,
            float finalDuration,
            float horizontalOffset,
            float verticalOffset) {
        if (displacementMode == 5) return ReferenceMoveTimeline.sequence();
        if (displacementMode < 0 || displacementMode > 4)
            throw new IllegalArgumentException("Unsupported reference displacement");
        var actor = target(targetId);
        float basePositionX = actor.center.x,
                baseSecondaryPosition = displacementMode == 3 ? actor.center.z : actor.center.y;
        var shakeTimeline = ReferenceMoveTimeline.sequence();
        float halfDuration = (initialDuration + finalDuration) / 2;
        int cycles = displacementMode == 2 || displacementMode == 3 ? repeatCount + 1 : 1;
        for (int i = 0; i < cycles; i++) {
            var axisTimeline =
                    ReferenceMoveTimeline.parallel()
                            .append(
                                    ReferenceMoveTimeline.sequence()
                                            .append(
                                                    scalar(
                                                            halfDuration,
                                                            () -> actor.center.x,
                                                            v -> actor.center.x = v,
                                                            basePositionX + horizontalOffset / 4))
                                            .append(
                                                    scalar(
                                                            halfDuration,
                                                            () -> actor.center.x,
                                                            v -> actor.center.x = v,
                                                            displacementMode == 3
                                                                    ? basePositionX
                                                                            - horizontalOffset / 4
                                                                    : basePositionX)));
            if (displacementMode >= 1 && displacementMode <= 3)
                axisTimeline.append(
                        ReferenceMoveTimeline.sequence()
                                .append(
                                        scalar(
                                                halfDuration,
                                                () ->
                                                        displacementMode == 3
                                                                ? actor.center.z
                                                                : actor.center.y,
                                                v -> {
                                                    if (displacementMode == 3) actor.center.z = v;
                                                    else actor.center.y = v;
                                                },
                                                baseSecondaryPosition + verticalOffset / 4))
                                .append(
                                        scalar(
                                                halfDuration,
                                                () ->
                                                        displacementMode == 3
                                                                ? actor.center.z
                                                                : actor.center.y,
                                                v -> {
                                                    if (displacementMode == 3) actor.center.z = v;
                                                    else actor.center.y = v;
                                                },
                                                displacementMode == 3
                                                        ? baseSecondaryPosition - verticalOffset / 4
                                                        : baseSecondaryPosition)));
            shakeTimeline.append(axisTimeline);
        }
        if (displacementMode == 3)
            shakeTimeline
                    .append(
                            scalar(
                                    halfDuration,
                                    () -> actor.center.x,
                                    v -> actor.center.x = v,
                                    basePositionX))
                    .append(
                            scalar(
                                    halfDuration,
                                    () -> actor.center.z,
                                    v -> actor.center.z = v,
                                    baseSecondaryPosition));
        return shakeTimeline;
    }

    public ReferenceMoveTimeline scaleCombatant(
            int targetId,
            int scaleMode,
            int repeatCount,
            float initialDuration,
            float finalDuration,
            float horizontalScale,
            float verticalScale) {
        if (scaleMode < 0 || scaleMode > 3)
            throw new IllegalArgumentException("Unsupported reference scale animation");
        var actor = target(targetId);
        float baseY = actor.center.y;
        float duration = initialDuration + finalDuration;
        var scaleTimeline = ReferenceMoveTimeline.sequence();
        // scaleCombatant mode 2 repeats count-1 times only when count-1 > 1 in the reference.
        int cycles = scaleMode == 0 || scaleMode == 3 || repeatCount <= 2 ? 1 : repeatCount;
        for (int i = 0; i < cycles; i++) {
            float
                    scaleX =
                            scaleMode == 0
                                    ? actor.scaleX * horizontalScale
                                    : scaleMode == 1 ? horizontalScale : 1 + horizontalScale,
                    scaleY =
                            scaleMode == 0
                                    ? actor.scaleY * verticalScale
                                    : scaleMode == 1 ? verticalScale : 1 + verticalScale,
                    centerY = scaleMode == 0 ? baseY - scaleY : baseY + (scaleY - 1) / 2;
            scaleTimeline.append(scale(actor, duration, scaleX, scaleY, centerY));
            if (scaleMode == 3)
                scaleTimeline.append(
                        scale(
                                actor,
                                duration,
                                1 - horizontalScale,
                                1 - verticalScale,
                                baseY - verticalScale / 2));
            if (scaleMode >= 2) scaleTimeline.append(scale(actor, duration, 1, 1, baseY));
        }
        return scaleTimeline;
    }

    private ReferenceMoveTimeline scale(
            Combatant combatant,
            float duration,
            float horizontalScale,
            float verticalScale,
            float centerY) {
        return ReferenceMoveTimeline.parallel()
                .append(
                        ReferenceMoveTimeline.tween(
                                duration,
                                () -> new float[] {combatant.scaleX, combatant.scaleY},
                                v -> {
                                    combatant.scaleX = v[0];
                                    combatant.scaleY = v[1];
                                },
                                horizontalScale,
                                verticalScale))
                .append(
                        scalar(
                                duration,
                                () -> combatant.center.y,
                                v -> combatant.center.y = v,
                                centerY));
    }

    private ReferenceMoveTimeline scalar(
            float duration,
            java.util.function.Supplier<Float> readValue,
            java.util.function.Consumer<Float> writeValue,
            float targetValue) {
        return ReferenceMoveTimeline.tween(
                duration,
                () -> new float[] {readValue.get()},
                components -> writeValue.accept(components[0]),
                targetValue);
    }

    public ReferenceMoveTimeline scheduleSound(
            byte soundRegion,
            int soundId,
            int soundChannel,
            int targetId,
            float delayMillis,
            float volume,
            Object soundActor) {
        return ReferenceMoveTimeline.sequence();
    }

    public ReferenceMoveTimeline adjustSoundTrack(
            int soundChannel, int adjustmentType, float delayMillis, float durationMillis) {
        return ReferenceMoveTimeline.sequence();
    }

    // Reference cry callback; audio is outside the requested PC preview.
    public ReferenceMoveTimeline playPokemonCry(boolean wait, float delay) {
        return ReferenceMoveTimeline.sequence();
    }

    public static Color color(int rgb555) {
        return new Color(
                (rgb555 & 31) / 31f, ((rgb555 >> 5) & 31) / 31f, ((rgb555 >> 10) & 31) / 31f, 1);
    }

    public void close() {
        for (var effect : effects) effect.particles.close();
        effects.clear();
    }
}
