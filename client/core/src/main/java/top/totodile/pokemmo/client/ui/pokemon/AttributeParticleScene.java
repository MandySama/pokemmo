package top.totodile.pokemmo.client.ui.pokemon;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.particles.*;
import com.badlogic.gdx.graphics.g3d.particles.batches.*;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.*;
import com.badlogic.gdx.graphics.g3d.particles.renderers.*;
import com.badlogic.gdx.graphics.g3d.particles.values.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;

import de.matthiasmann.twl.renderer.Image;

import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

import java.util.*;

/**
 * Reference PokemonSummaryWindow.aUX / ControllerSpawnInfluencer / ScaleXYInfluencer. Simulation is
 * performed by libGDX's 3D particle controller, including Verlet integration. Billboard vertices
 * are expanded by the unmodified reference GPU shader.
 */
public final class AttributeParticleScene implements AutoCloseable {
    private static final ParallelArray.ChannelDescriptor XY =
            new ParallelArray.ChannelDescriptor(ParticleChannels.newGlobalId(), float.class, 2);
    private final List<Layer> layers = new ArrayList<>();
    private final PerspectiveCamera camera = new PerspectiveCamera(67, 224, 227);
    private final List<Billboard> drawList = new ArrayList<>();

    /**
     * Reference NDSRegionInfluencer and custom animated region replacements. Null keeps the atlas.
     */
    @FunctionalInterface
    public interface ParticleImageProvider {
        Image image(String controller, int controllerIndex, float seconds, float lifePercent);
    }

    private ParticleImageProvider particleImages;

    public void particleImages(ParticleImageProvider provider) {
        particleImages = provider;
    }

    private float accumulated;
    private float absoluteTime;
    private PerspectiveCamera simulationCamera = camera;
    static final float STEP = 0.016666668f;

    AttributeParticleScene(PokemonController owner, JsonValue effect) {
        this(owner.images(), effect, new Vector3());
    }

    public AttributeParticleScene(
            top.totodile.pokemmo.client.ui.pokedex.PokedexImages images,
            JsonValue effect,
            Vector3 origin) {
        this(images, effect, origin, origin, origin, false, 8);
    }

    public AttributeParticleScene(
            top.totodile.pokemmo.client.ui.pokedex.PokedexImages images,
            JsonValue effect,
            Vector3 origin,
            Vector3 caster,
            Vector3 target,
            boolean targetSpawn,
            int direction) {
        this(images, effect, origin, caster, target, targetSpawn, direction, false);
    }

    public AttributeParticleScene(
            top.totodile.pokemmo.client.ui.pokedex.PokedexImages images,
            JsonValue effect,
            Vector3 origin,
            Vector3 caster,
            Vector3 target,
            boolean targetSpawn,
            int direction,
            boolean controllerTargets) {
        var json = new Json();
        json.setIgnoreUnknownFields(true);
        int batchIndex = 0;
        var trailTargets = new ArrayList<Integer>();
        for (var config : effect.get("resource").get("controllers")) {
            boolean controllerTargetSpawn = targetSpawn;
            if (controllerTargets)
                for (var influence : config.get("influencers"))
                    if (influence.getString("class").endsWith("ControllerSpawnInfluencer")) {
                        int spawn = (int) influence.get("spawnType").getFloat("value", 2);
                        controllerTargetSpawn = spawn == 1 || spawn == 3;
                    }
            var emitter =
                    config.get("emitter").getString("class").endsWith("TrailEmitter")
                            ? new TrailEmitter()
                            : new RegularEmitter();
            emitter.read(json, config.get("emitter"));
            if (emitter instanceof TrailEmitter)
                emitter.setEmissionMode(RegularEmitter.EmissionMode.Disabled);
            var influences = new ArrayList<Influencer>();
            Matrix4 transform = new Matrix4();
            for (var value : config.get("influencers")) {
                String kind = value.getString("class");
                if (kind.endsWith(".SpawnInfluencerExt")) {
                    var shape = value.get("spawnShape");
                    String name = shape.getString("class");
                    PrimitiveSpawnShapeValue spawn;
                    if (name.endsWith("RectangleSpawnShapeValueExt"))
                        spawn = new SeededRectangle(shape);
                    else if (name.endsWith("EllipseSpawnShapeValueExt"))
                        spawn = new SeededEllipse(shape);
                    else if (name.endsWith("PointSpawnShapeValueExt"))
                        spawn = new PointSpawnShapeValue();
                    else if (name.endsWith("CylinderSpawnShapeValueExt"))
                        spawn = new SeededCylinder(shape);
                    else
                        throw new IllegalArgumentException(
                                "Unsupported attribute spawn shape " + name);
                    spawn.read(json, shape);
                    spawn.xOffsetValue = offset(json, shape.get("xOffsetValue"));
                    spawn.yOffsetValue = offset(json, shape.get("yOffsetValue"));
                    spawn.zOffsetValue = offset(json, shape.get("zOffsetValue"));
                    spawn.spawnWidthValue = scaled(json, shape.get("spawnWidthValue"));
                    spawn.spawnHeightValue = scaled(json, shape.get("spawnHeightValue"));
                    spawn.spawnDepthValue = scaled(json, shape.get("spawnDepthValue"));
                    influences.add(
                            new SpawnInfluencer(spawn) {
                                @Override
                                public void start() {
                                    super.start();
                                    var seed = shape.get("seed");
                                    if (seed != null && seed.getBoolean("active", false))
                                        OffsetValue.RANDOM.setSeed(seed.getLong("value", 0));
                                }
                            });
                } else if (kind.endsWith("ControllerSpawnInfluencer")) {
                    float spawnType = value.get("spawnType").getFloat("value", 2);
                    if (spawnType < 0 || spawnType > 4)
                        throw new IllegalArgumentException(
                                "Unsupported controller spawn type " + spawnType);
                    var adjustment = vector(value.get("spawnAdjustment"));
                    if (controllerTargets && value.getBoolean("invertSpawn", false)) {
                        adjustment.x = -adjustment.x;
                        adjustment.z = -adjustment.z;
                    }
                    transform.setToTranslation(adjustment);
                    if (spawnType == 0) transform.trn(controllerTargets ? caster : origin);
                    else if (spawnType == 1) transform.trn(target);
                    else if (spawnType == 3) transform.trn(3, 3.02f, 4);
                    else if (spawnType == 4) transform.trn(5, 3.02f, 1);
                    var rotation = vector(value.get("rotationAdjustment"));
                    transform
                            .rotate(Vector3.Z, rotation.z)
                            .rotate(Vector3.Y, rotation.y)
                            .rotate(Vector3.X, rotation.x);
                } else if (kind.endsWith("ScaleXYInfluencer"))
                    influences.add(new ScaleXY(json, value));
                else if (kind.endsWith("DynamicsInfluencerExt")) {
                    var dynamics =
                            new DampedDynamics(
                                    value.get("damp") == null
                                            ? 1
                                            : value.get("damp").getFloat("value", 1));
                    for (var modifier : value.get("velocities")) {
                        String name = modifier.getString("class");
                        DynamicsModifier velocity;
                        if (name.endsWith("$FaceModifier")) velocity = new FaceModifier();
                        else if (name.endsWith("$PolarAccelerationExt")) {
                            var force = vector(modifier.get("dir"));
                            if (direction != 8
                                    && ((direction == 11 || direction == 1)
                                            != controllerTargetSpawn)) {
                                float magnitude = force.x > 0 ? force.x : force.z;
                                force =
                                        (direction == 11 || direction == 1
                                                        ? target.cpy().sub(caster)
                                                        : caster.cpy().sub(target))
                                                .nor()
                                                .scl(magnitude * 1.5f, 1, magnitude * 1.5f);
                            }
                            velocity = new PolarDirection(force);
                        } else if (name.endsWith("$VectorPathModifier"))
                            velocity =
                                    new ReferenceParticlePath(
                                            caster, target, controllerTargetSpawn);
                        else if (name.endsWith("$DragForce"))
                            velocity = new HomingForce(controllerTargetSpawn ? caster : target);
                        else if (name.endsWith("$ProjectedDirection"))
                            velocity = new ProjectedDirection();
                        else if (name.endsWith("$Rotational2DStatic"))
                            velocity = new StaticRotation();
                        else if (name.endsWith("$RandomInitialRotation"))
                            velocity = new InitialRotation();
                        else if (name.endsWith("$CircularAcceleration"))
                            velocity = new CircularAcceleration();
                        else if (name.endsWith("$CircuralMotion"))
                            velocity = new CircularMotion(modifier.getInt("type", 0));
                        // Reference CentripetalAccelerationExt allocates a zero-valued private
                        // stop channel; the spawn effect never changes it, so its force is
                        // identical.
                        else if (name.endsWith("$CentripetalAccelerationExt"))
                            velocity = new DynamicsModifier.CentripetalAcceleration();
                        else
                            try {
                                velocity =
                                        (DynamicsModifier)
                                                Class.forName(name)
                                                        .getDeclaredConstructor()
                                                        .newInstance();
                            } catch (ReflectiveOperationException error) {
                                throw new IllegalArgumentException(name, error);
                            }
                        velocity.read(json, modifier);
                        dynamics.velocities.add(velocity);
                    }
                    influences.add(dynamics);
                } else if (kind.endsWith("TrailSpawnInfluencerExt"))
                    influences.add(new TrailSpawn(value));
                else if (kind.endsWith("TrailInfluencer"))
                    influences.add(new TrailSource(value.getBoolean("background", false)));
                else if (kind.endsWith("ColorInfluencerExt$AlphaOnly"))
                    influences.add(new AlphaOnly(json, value));
                else if (kind.endsWith("ColorInfluencerExt$RandomColor"))
                    influences.add(new RandomColor(json, value));
                else if (kind.endsWith("RegionInfluencerExt$AnimatedExt")) {
                    var influence = new AnimatedRegion();
                    influence.read(json, value);
                    influences.add(influence);
                } else {
                    try {
                        var influence =
                                (Influencer)
                                        Class.forName(kind).getDeclaredConstructor().newInstance();
                        influence.read(json, value);
                        influences.add(influence);
                    } catch (ReflectiveOperationException error) {
                        throw new IllegalArgumentException(kind, error);
                    }
                }
            }
            var renderer = new Channels();
            var controller =
                    new ParticleController(
                            config.getString("name", "attribute"),
                            emitter,
                            renderer,
                            influences.toArray(Influencer[]::new));
            controller.setTransform(transform);
            controller.init();
            controller.start();
            var batchConfig =
                    effect.get("unique")
                            .get("billboardBatchExt" + batchIndex++)
                            .get("data")
                            .get("cfg");
            var range = config.get("aps_texture_range");
            layers.add(
                    new Layer(
                            controller,
                            config.getString("type", "DEFAULT").equals("CUSTOM")
                                    ? images.customParticle(
                                            config.getInt("aps"),
                                            config.getBoolean("repeat_x", false),
                                            config.getBoolean("repeat_y", false),
                                            config.getBoolean("flip_x", false),
                                            config.getBoolean("flip_y", false))
                                    : images.particle(
                                            config.getInt("aps"),
                                            (int) range.getFloat("lowMin", 0),
                                            (int) range.getFloat("lowMax", 0),
                                            config.getBoolean("mix", false),
                                            config.getBoolean("repeat_x", false),
                                            config.getBoolean("repeat_y", false),
                                            config.getBoolean("flip_x", false),
                                            config.getBoolean("flip_y", false),
                                            config.getBoolean("avg", false)),
                            renderer,
                            batchConfig.getString("mode", "Screen"),
                            batchConfig.getString("origin", "Middle")));
            trailTargets.add(config.getInt("trailController", -1));
        }
        for (int i = 0; i < layers.size(); i++)
            if (trailTargets.get(i) >= 0 && trailTargets.get(i) < layers.size()) {
                var child = layers.get(trailTargets.get(i)).controller;
                for (var influence : layers.get(i).controller.influencers)
                    if (influence instanceof TrailSource source
                            && child.emitter instanceof TrailEmitter trail) source.emitter = trail;
            }
    }

    // Reference Ext values use independent shared random streams; spawn offsets are reseeded on
    // each start.
    private static final class OffsetValue extends RangedNumericValue {
        static final RandomXS128 RANDOM = new RandomXS128();

        @Override
        public float newLowValue() {
            return getLowMin() + RANDOM.nextFloat() * (getLowMax() - getLowMin());
        }
    }

    private static final class ScaleValue extends ScaledNumericValue {
        static final RandomXS128 RANDOM = new RandomXS128();

        @Override
        public float newLowValue() {
            return getLowMin() + RANDOM.nextFloat() * (getLowMax() - getLowMin());
        }

        @Override
        public float newHighValue() {
            return getHighMin() + RANDOM.nextFloat() * (getHighMax() - getHighMin());
        }
    }

    private static OffsetValue offset(Json json, JsonValue data) {
        var value = new OffsetValue();
        value.read(json, data);
        return value;
    }

    private static ScaleValue scaled(Json json, JsonValue data) {
        var value = new ScaleValue();
        value.read(json, data);
        return value;
    }

    private static Vector3 vector(JsonValue value) {
        return value == null
                ? new Vector3()
                : new Vector3(
                        value.getFloat("x", 0), value.getFloat("y", 0), value.getFloat("z", 0));
    }

    void reset() {
        accumulated = 0;
        for (var layer : layers) layer.controller.reset();
    }

    void advance(float delta) {
        accumulated += delta;
        while (accumulated >= STEP) {
            for (var layer : layers) layer.controller.update(STEP);
            accumulated -= STEP;
        }
        if (accumulated < 0) accumulated = 0;
    }

    void draw(
            GdxTwlRenderer renderer,
            float delta,
            int x,
            int y,
            int width,
            int height,
            int type,
            boolean storage) {
        configureCamera(camera, width, height, type, storage);
        draw(renderer, delta, x, y, width, height, camera);
    }

    public void draw(
            GdxTwlRenderer renderer,
            float delta,
            int x,
            int y,
            int width,
            int height,
            PerspectiveCamera camera) {
        simulationCamera = camera;
        advance(delta);
        drawList.clear();
        for (var layer : layers) {
            var c = layer.channels;
            for (int i = 0; i < layer.controller.particles.size; i++) {
                int pos = i * c.position.strideSize,
                        col = i * c.color.strideSize,
                        rot = i * c.rotation.strideSize,
                        sz = i * c.size.strideSize,
                        uv = i * c.region.strideSize;
                var center =
                        new Vector3(
                                c.position.data[pos],
                                c.position.data[pos + 1],
                                c.position.data[pos + 2]);
                Image image = layer.image;
                float[] region = Arrays.copyOfRange(c.region.data, uv, uv + 6);
                if (particleImages != null) {
                    ParallelArray.FloatChannel life =
                            layer.controller.particles.getChannel(ParticleChannels.Life);
                    Image replacement =
                            particleImages.image(
                                    layer.controller.name,
                                    layers.indexOf(layer),
                                    absoluteTime,
                                    life.data[i * life.strideSize + 2]);
                    if (replacement != null) {
                        image = replacement;
                        region =
                                new float[] {
                                    0, 0, 1, 1, .5f, .5f * image.getHeight() / image.getWidth()
                                };
                    }
                }
                drawList.add(
                        new Billboard(
                                image,
                                center,
                                c.size.data[sz],
                                c.size.data[sz + 1],
                                c.rotation.data[rot],
                                c.rotation.data[rot + 1],
                                Arrays.copyOfRange(c.color.data, col, col + 4),
                                region,
                                layer.mode,
                                layer.origin,
                                layers.indexOf(layer)));
            }
        }
        drawList.sort(
                Comparator.comparingInt(Billboard::batch)
                        .thenComparing(
                                Comparator.comparingDouble(
                                                (Billboard b) -> b.center.dot(camera.direction))
                                        .reversed()));
        renderer.beginParticleScene(camera.combined, x, y, width, height);
        try {
            for (var b : drawList)
                renderer.drawParticleBillboard(
                        b.image,
                        b.center,
                        b.width * b.region[4],
                        b.height * b.region[5],
                        b.cos,
                        b.sin,
                        b.region,
                        b.color,
                        b.mode,
                        b.origin,
                        camera);
        } finally {
            renderer.endParticleScene();
        }
    }

    /** SummaryWindow.aUX and GdxShaderUniformBindingListener.aUX use different cameras. */
    static void configureCamera(
            PerspectiveCamera camera, int width, int height, int type, boolean storage) {
        boolean tilted = type == 11 || type == 14 || type == 17;
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.up.set(Vector3.Y);
        camera.direction.set(0, 0, -1);
        var target = new Vector3(0, 0, -3.25f);
        camera.position.set(
                storage ? .25f : 0,
                storage && tilted ? .5f : 0,
                storage ? (tilted ? 2.5f : 1.5f) : 2);
        camera.lookAt(target);
        // OverworldCamera.JP restores Y up before yaw, then PB rotates around direction x up.
        camera.up.set(Vector3.Y);
        if (storage && tilted) camera.rotateAround(target, Vector3.Y, -7);
        if (tilted)
            camera.rotateAround(
                    target, new Vector3(camera.direction).crs(camera.up).nor(), storage ? -8 : -10);
        camera.near = 1;
        camera.far = 100;
        camera.update();
    }

    public void drawAt(
            GdxTwlRenderer renderer,
            float time,
            int x,
            int y,
            int width,
            int height,
            PerspectiveCamera camera) {
        float delta = Math.max(0, time - absoluteTime);
        absoluteTime = Math.max(absoluteTime, time);
        draw(renderer, delta, x, y, width, height, camera);
    }

    /** Map weather follows the camera without moving particles that were already emitted. */
    public void origin(Vector3 position) {
        for (var layer : layers) layer.controller.transform.setTranslation(position);
    }

    public boolean isComplete() {
        return layers.stream().allMatch(layer -> layer.controller.isComplete());
    }

    public void close() {
        for (var layer : layers) layer.controller.dispose();
        layers.clear();
    }

    private record Layer(
            ParticleController controller,
            Image image,
            Channels channels,
            String mode,
            String origin) {}

    /**
     * Reference TrailEmitter: disabled autonomous emission; parent particles explicitly spawn the
     * trail.
     */
    private static final class TrailEmitter extends RegularEmitter {
        public void init() {
            super.init();
            emissionDelta = (int) delay;
        }

        void spawn(TrailSource source, int particle) {
            emissionDelta += (int) (controller.deltaTime * 1000);
            if (emissionDelta < delay) return;
            emissionDelta = 0;
            int count = Math.min(emissionDiff, maxParticleCount - controller.particles.size);
            if (count <= 0) return;
            for (var influence : controller.influencers) {
                if (influence instanceof TrailSpawn spawn) {
                    spawn.parent = source;
                    spawn.parentIndex = particle;
                } else if (influence instanceof ScaleXY scale) {
                    scale.parentScale = source.size;
                    scale.parentOffset = particle * source.size.strideSize;
                }
            }
            int start = controller.particles.size;
            controller.activateParticles(start, count);
            controller.particles.size += count;
        }
    }

    private static final class TrailSource extends Influencer {
        final boolean back;
        TrailEmitter emitter;
        ParallelArray.FloatChannel position, size, color, rotation;

        TrailSource(boolean back) {
            this.back = back;
        }

        public void allocateChannels() {
            position = controller.particles.addChannel(ParticleChannels.Position);
            size = controller.particles.addChannel(XY);
            color =
                    controller.particles.addChannel(
                            ParticleChannels.Color, ParticleChannels.ColorInitializer.get());
            rotation =
                    controller.particles.addChannel(
                            ParticleChannels.Rotation2D,
                            ParticleChannels.Rotation2dInitializer.get());
        }

        public void update() {
            if (emitter != null)
                for (int i = 0; i < controller.particles.size; i++) emitter.spawn(this, i);
        }

        public TrailSource copy() {
            return new TrailSource(back);
        }
    }

    private static final class TrailSpawn extends Influencer {
        final boolean copyColor, copyAlpha, dynamicColor, copyRotation, dynamicRotation;
        TrailSource parent;
        int parentIndex;
        ParallelArray.FloatChannel position, color, rotation;
        ParallelArray.IntChannel indices;

        TrailSpawn(JsonValue value) {
            copyColor = value.getBoolean("copyParentColor", true);
            copyAlpha = value.getBoolean("copyParentColorAlpha", true);
            dynamicColor =
                    value.has("copyParentColorAlpha")
                            && value.getBoolean("copyParentColorDynamic", false);
            copyRotation = value.getBoolean("copyParentRotation", true);
            dynamicRotation = value.getBoolean("copyParentRotationDynamic", false);
        }

        public void allocateChannels() {
            position = controller.particles.addChannel(ParticleChannels.Position);
            color =
                    controller.particles.addChannel(
                            ParticleChannels.Color, ParticleChannels.ColorInitializer.get());
            rotation =
                    controller.particles.addChannel(
                            ParticleChannels.Rotation2D,
                            ParticleChannels.Rotation2dInitializer.get());
            indices =
                    controller.particles.addChannel(
                            new ParallelArray.ChannelDescriptor(
                                    controller.particleChannels.newId(), int.class, 1));
        }

        public void activateParticles(int start, int count) {
            for (int i = start; i < start + count; i++) {
                int p = parentIndex * parent.position.strideSize, at = i * position.strideSize;
                position.data[at] = parent.position.data[p];
                position.data[at + 1] = parent.position.data[p + 1];
                position.data[at + 2] =
                        parent.position.data[p + 2] + (parent.back ? .0125f : -.0125f);
                indices.data[i] = parentIndex;
                copy(i, parentIndex, true);
            }
        }

        private void copy(int i, int source, boolean birth) {
            int to = i * color.strideSize, from = source * parent.color.strideSize;
            if (birth || dynamicColor) {
                if (copyColor)
                    for (int c = 0; c < 3; c++) color.data[to + c] = parent.color.data[from + c];
                if (copyAlpha) color.data[to + 3] = parent.color.data[from + 3];
            }
            if (copyRotation && (birth || dynamicRotation)) {
                to = i * rotation.strideSize;
                from = source * parent.rotation.strideSize;
                rotation.data[to] = parent.rotation.data[from];
                rotation.data[to + 1] = parent.rotation.data[from + 1];
            }
        }

        public void update() {
            if (parent != null && (dynamicColor || dynamicRotation))
                for (int i = 0; i < controller.particles.size; i++) copy(i, indices.data[i], false);
        }

        public TrailSpawn copy() {
            throw new UnsupportedOperationException();
        }
    }

    private static class AlphaOnly extends Influencer {
        final ScaledNumericValue alpha;
        ParallelArray.FloatChannel color, life, interpolation;

        AlphaOnly(Json json, JsonValue value) {
            alpha = scaled(json, value.get("alpha"));
        }

        public void allocateChannels() {
            color =
                    controller.particles.addChannel(
                            ParticleChannels.Color, ParticleChannels.ColorInitializer.get());
            life = controller.particles.addChannel(ParticleChannels.Life);
            interpolation =
                    controller.particles.addChannel(
                            new ParallelArray.ChannelDescriptor(
                                    controller.particleChannels.newId(), float.class, 2));
        }

        public void activateParticles(int start, int count) {
            for (int i = start; i < start + count; i++) {
                int at = i * 2;
                float low = alpha.newLowValue(), diff = alpha.newHighValue() - low;
                interpolation.data[at] = low;
                interpolation.data[at + 1] = diff;
                color.data[i * color.strideSize + 3] =
                        low + diff * alpha.getScale(life.data[i * life.strideSize + 2]);
            }
        }

        public void update() {
            for (int i = 0; i < controller.particles.size; i++)
                color.data[i * color.strideSize + 3] =
                        interpolation.data[i * 2]
                                + interpolation.data[i * 2 + 1]
                                        * alpha.getScale(life.data[i * life.strideSize + 2]);
        }

        public AlphaOnly copy() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class RandomColor extends Influencer {
        final List<GradientColorValue> colors = new ArrayList<>();
        final List<ScaledNumericValue> alphas = new ArrayList<>();
        ParallelArray.FloatChannel color, life, interpolation;
        ParallelArray.IntChannel path;

        RandomColor(Json json, JsonValue value) {
            for (var c : value.get("color")) {
                var gradient = new GradientColorValue();
                gradient.read(json, c);
                colors.add(gradient);
            }
            for (var a : value.get("alpha")) alphas.add(scaled(json, a));
        }

        public void allocateChannels() {
            color = controller.particles.addChannel(ParticleChannels.Color);
            life = controller.particles.addChannel(ParticleChannels.Life);
            interpolation =
                    controller.particles.addChannel(
                            new ParallelArray.ChannelDescriptor(
                                    controller.particleChannels.newId(), float.class, 2));
            path =
                    controller.particles.addChannel(
                            new ParallelArray.ChannelDescriptor(
                                    controller.particleChannels.newId(), int.class, 1));
        }

        public void activateParticles(int start, int count) {
            for (int i = start; i < start + count; i++) {
                int index = MathUtils.random(colors.size() - 1);
                path.data[i] = index;
                var alpha = alphas.get(index);
                float low = alpha.newLowValue();
                interpolation.data[i * 2] = low;
                interpolation.data[i * 2 + 1] = alpha.newHighValue() - low;
                write(i);
            }
        }

        private void write(int i) {
            int index = path.data[i], at = i * color.strideSize;
            float percent = life.data[i * life.strideSize + 2];
            colors.get(index).getColor(percent, color.data, at);
            color.data[at + 3] =
                    interpolation.data[i * 2]
                            + interpolation.data[i * 2 + 1] * alphas.get(index).getScale(percent);
        }

        public void update() {
            for (int i = 0; i < controller.particles.size; i++) write(i);
        }

        public RandomColor copy() {
            throw new UnsupportedOperationException();
        }
    }

    /** CylinderSpawnShapeValueExt, including its seeded angle and radius sampling. */
    private static final class SeededCylinder extends PrimitiveSpawnShapeValue {
        final JsonValue config;
        final RandomXS128 random = new RandomXS128();

        SeededCylinder(JsonValue config) {
            this.config = config;
        }

        public void start() {
            super.start();
            var seed = config.get("seed");
            if (seed != null && seed.getBoolean("active", false))
                random.setSeed(seed.getLong("value", 0));
        }

        public void spawnAux(Vector3 out, float percent) {
            float width = sample(spawnWidthValue, percent, spawnWidthDiff, spawnWidth),
                    height = sample(spawnHeightValue, percent, spawnHeightDiff, spawnHeight),
                    depth = sample(spawnDepthValue, percent, spawnDepthDiff, spawnDepth);
            float y = random.nextFloat() * height - height / 2,
                    x = isEdges() ? width / 2 : random.nextFloat() * width / 2,
                    z = isEdges() ? depth / 2 : random.nextFloat() * depth / 2;
            float angle =
                    x == 0
                            ? (random.nextInt(2) == 0 ? -90 : 90)
                            : z == 0
                                    ? (random.nextInt(2) == 0 ? 0 : 180)
                                    : random.nextFloat() * 360;
            out.set(MathUtils.cosDeg(angle) * x, y, MathUtils.sinDeg(angle) * z);
        }

        public SeededCylinder copy() {
            return new SeededCylinder(config);
        }
    }

    /** DynamicsInfluencerExt: x' = x + (x - previous) * damp + acceleration * dt². */
    /** DynamicsModifierExt.DragForce, with battle endpoints supplied explicitly. */
    private static final class HomingForce extends DynamicsModifier.Strength {
        private final Vector3 destination;
        private ParallelArray.FloatChannel acceleration, position, previous, arrival;

        HomingForce(Vector3 destination) {
            this.destination = destination.cpy();
        }

        public void allocateChannels() {
            super.allocateChannels();
            acceleration = controller.particles.addChannel(ParticleChannels.Acceleration);
            position = controller.particles.addChannel(ParticleChannels.Position);
            previous = controller.particles.addChannel(ParticleChannels.PreviousPosition);
            var descriptor =
                    new ParallelArray.ChannelDescriptor(
                            controller.particleChannels.newId(), float.class, 4);
            arrival = controller.particles.addChannel(descriptor);
        }

        public void activateParticles(int start, int count) {
            super.activateParticles(start, count);
            for (int particle = start; particle < start + count; particle++) {
                int at = particle * arrival.strideSize;
                arrival.data[at] = 0;
                arrival.data[at + 1] = destination.x;
                arrival.data[at + 2] = destination.y;
                arrival.data[at + 3] = destination.z;
            }
        }

        public void update() {
            for (int particle = 0; particle < controller.particles.size; particle++) {
                int stop = particle * arrival.strideSize;
                if (arrival.data[stop] == 1) continue;
                int pos = particle * position.strideSize,
                        force = particle * acceleration.strideSize,
                        strength = particle * strengthChannel.strideSize;
                float life = lifeChannel.data[particle * lifeChannel.strideSize + 2];
                var delta =
                        new Vector3(
                                arrival.data[stop + 1] - position.data[pos],
                                arrival.data[stop + 2] - position.data[pos + 1],
                                arrival.data[stop + 3] - position.data[pos + 2]);
                float distance = delta.len(),
                        magnitude =
                                strengthChannel.data[strength]
                                        + strengthChannel.data[strength + 1]
                                                * strengthValue.getScale(life);
                delta.nor().scl(magnitude / (float) Math.sqrt(3));
                acceleration.data[force] += delta.x;
                acceleration.data[force + 1] += delta.y;
                acceleration.data[force + 2] += delta.z;
                if (distance < .05f) {
                    int last = particle * previous.strideSize;
                    System.arraycopy(position.data, pos, previous.data, last, 3);
                    float remaining = 1 - (float) Math.sin(life * Math.PI / 2);
                    // The reference's arrival branch indexes acceleration by particle,
                    // rather than the normal channel stride; retain that behavior.
                    acceleration.data[particle] = remaining * delta.x;
                    acceleration.data[particle + 1] = remaining * delta.y;
                    acceleration.data[particle + 2] = remaining * delta.z;
                    arrival.data[stop] = 1;
                }
            }
        }

        public HomingForce copy() {
            var result = new HomingForce(destination);
            result.strengthValue.load(strengthValue);
            result.isGlobal = isGlobal;
            return result;
        }
    }

    private static final class DampedDynamics extends DynamicsInfluencer {
        final float damp;
        ParallelArray.FloatChannel position, previous;
        float[] velocity = new float[0];

        DampedDynamics(float damp) {
            this.damp = damp;
        }

        public void allocateChannels() {
            super.allocateChannels();
            position = controller.particles.getChannel(ParticleChannels.Position);
            previous = controller.particles.getChannel(ParticleChannels.PreviousPosition);
        }

        public void update() {
            int size = controller.particles.size * 3;
            boolean adjust = damp != 1 && position != null && previous != null;
            if (adjust) {
                if (velocity.length < size) velocity = new float[size];
                for (int i = 0; i < size; i++) velocity[i] = position.data[i] - previous.data[i];
            }
            super.update();
            if (adjust) for (int i = 0; i < size; i++) position.data[i] += (damp - 1) * velocity[i];
        }
    }

    /**
     * DynamicsModifierExt.PolarAccelerationExt, with fE0 direction type 8 (unrotated world axes).
     */
    private static final class PolarDirection extends DynamicsModifier.Angular {
        final Vector3 direction;
        ParallelArray.FloatChannel acceleration;

        PolarDirection(Vector3 direction) {
            this.direction = direction;
        }

        public void allocateChannels() {
            super.allocateChannels();
            acceleration = controller.particles.addChannel(ParticleChannels.Acceleration);
            controller.particles.addChannel(ParticleChannels.PreviousPosition);
        }

        public void update() {
            for (int i = 0; i < controller.particles.size; i++) {
                float life = lifeChannel.data[i * lifeChannel.strideSize + 2];
                int s = i * strengthChannel.strideSize, a = i * angularChannel.strideSize;
                float strength =
                        strengthValue.isRelative()
                                ? strengthValue.getLowMax()
                                        + strengthValue.getHighMax() * strengthValue.getScale(life)
                                : strengthChannel.data[s]
                                        + strengthChannel.data[s + 1]
                                                * strengthValue.getScale(life);
                float
                        theta =
                                angularChannel.data[a]
                                        + angularChannel.data[a + 1] * thetaValue.getScale(life),
                        phi =
                                angularChannel.data[a + 2]
                                        + angularChannel.data[a + 3] * phiValue.getScale(life);
                var force =
                        direction.isZero()
                                ? new Vector3(
                                                MathUtils.cosDeg(theta) * MathUtils.sinDeg(phi),
                                                MathUtils.cosDeg(phi),
                                                MathUtils.sinDeg(theta) * MathUtils.sinDeg(phi))
                                        .nor()
                                : direction;
                int at = i * acceleration.strideSize;
                acceleration.data[at] += force.x * strength;
                acceleration.data[at + 1] += force.y * strength;
                acceleration.data[at + 2] += force.z * strength;
            }
        }

        public PolarDirection copy() {
            throw new UnsupportedOperationException();
        }
    }

    /** ProjectedDirection rotates the billboard along projected velocity, as in the reference. */
    private final class ProjectedDirection extends DynamicsModifier {
        ParallelArray.FloatChannel rotation, previous, position;

        public void allocateChannels() {
            rotation = controller.particles.addChannel(ParticleChannels.Rotation2D);
            previous = controller.particles.addChannel(ParticleChannels.PreviousPosition);
            position = controller.particles.addChannel(ParticleChannels.Position);
            controller.particles.addChannel(ParticleChannels.Acceleration);
        }

        public void update() {
            for (int i = 0; i < controller.particles.size; i++) {
                int at = i * position.strideSize, old = i * previous.strideSize;
                var p =
                        simulationCamera.project(
                                new Vector3(
                                        position.data[at],
                                        position.data[at + 1],
                                        position.data[at + 2]));
                var q =
                        simulationCamera.project(
                                new Vector3(
                                        previous.data[old],
                                        previous.data[old + 1],
                                        previous.data[old + 2]));
                float angle =
                        90 - MathUtils.atan2(p.y - q.y, p.x - q.x) * MathUtils.radiansToDegrees;
                int r = i * rotation.strideSize;
                rotation.data[r] = MathUtils.cosDeg(angle);
                rotation.data[r + 1] = MathUtils.sinDeg(angle);
            }
        }

        public ProjectedDirection copy() {
            throw new UnsupportedOperationException();
        }
    }

    /** DynamicsModifierExt.Rotational2DStatic: fixed angle and life-dependent UV repetition. */
    private static final class StaticRotation extends DynamicsModifier.Strength {
        ParallelArray.FloatChannel angularVelocity, rotation, region;
        boolean flipX, flipY;
        float angle;

        public void allocateChannels() {
            super.allocateChannels();
            angularVelocity = controller.particles.addChannel(ParticleChannels.AngularVelocity2D);
            rotation = controller.particles.addChannel(ParticleChannels.Rotation2D);
            region = controller.particles.addChannel(ParticleChannels.TextureRegion);
        }

        public void read(Json json, JsonValue data) {
            super.read(json, data);
            flipX = data.getBoolean("flip_x", false);
            flipY = data.getBoolean("flip_y", false);
            angle = data.getFloat("angle", 0);
        }

        public void update() {
            for (int i = 0; i < controller.particles.size; i++) {
                int strength = i * strengthChannel.strideSize,
                        life = i * lifeChannel.strideSize + 2;
                float scale = strengthValue.getScale(lifeChannel.data[life]);
                angularVelocity.data[i * angularVelocity.strideSize] +=
                        strengthChannel.data[strength] + strengthChannel.data[strength + 1] * scale;
                int at = i * rotation.strideSize;
                rotation.data[at] = MathUtils.cosDeg(angle);
                rotation.data[at + 1] = MathUtils.sinDeg(angle);
                int uv = i * region.strideSize;
                region.data[uv] = flipX ? 1 : 0;
                region.data[uv + 2] = flipX ? 0 : 1;
                region.data[uv + 1] = flipY ? 1 / scale : 0;
                region.data[uv + 3] = flipY ? 0 : 1 / scale;
            }
        }

        public StaticRotation copy() {
            throw new UnsupportedOperationException();
        }
    }

    /** Reference DynamicsModifierExt.RandomInitialRotation, degrees sampled once at birth. */
    private static final class InitialRotation extends DynamicsModifier.Strength {
        ParallelArray.FloatChannel rotation;

        public void allocateChannels() {
            super.allocateChannels();
            rotation = controller.particles.addChannel(ParticleChannels.Rotation2D);
        }

        public void activateParticles(int start, int count) {
            super.activateParticles(start, count);
            for (int i = start; i < start + count; i++) {
                float angle = strengthValue.newLowValue() * MathUtils.degreesToRadians;
                int at = i * rotation.strideSize;
                rotation.data[at] = MathUtils.cos(angle);
                rotation.data[at + 1] = MathUtils.sin(angle);
            }
        }

        public void update() {}

        public InitialRotation copy() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * DynamicsModifierExt.CircuralMotion: birth angle/radius, life-based angle and radial offset.
     */
    /** DynamicsModifierExt.CircularAcceleration, including its initial Verlet velocity. */
    private static final class CircularAcceleration extends DynamicsModifier.Angular {
        ParallelArray.FloatChannel position, previous, acceleration;

        public void allocateChannels() {
            super.allocateChannels();
            position = controller.particles.addChannel(ParticleChannels.Position);
            previous = controller.particles.addChannel(ParticleChannels.PreviousPosition);
            acceleration = controller.particles.addChannel(ParticleChannels.Acceleration);
        }

        private void apply(int particle, boolean initial) {
            int p = particle * position.strideSize;
            int s = particle * strengthChannel.strideSize;
            int a = particle * angularChannel.strideSize;
            float life = lifeChannel.data[particle * lifeChannel.strideSize + 2];
            float speed =
                    strengthChannel.data[s]
                            + strengthChannel.data[s + 1] * strengthValue.getScale(life);
            float theta =
                    angularChannel.data[a] + angularChannel.data[a + 1] * thetaValue.getScale(life);
            float phi =
                    angularChannel.data[a + 2]
                            + angularChannel.data[a + 3] * phiValue.getScale(life);
            var axis =
                    new Vector3(
                            MathUtils.cosDeg(theta) * MathUtils.sinDeg(phi),
                            MathUtils.cosDeg(phi),
                            MathUtils.sinDeg(theta) * MathUtils.sinDeg(phi));
            var relative =
                    new Vector3(position.data[p], position.data[p + 1], position.data[p + 2]);
            if (!isGlobal) {
                var rotation = controller.transform.getRotation(new Quaternion(), true).conjugate();
                var translation = controller.transform.getTranslation(new Vector3());
                if (initial) {
                    rotation.transform(relative);
                    relative.sub(translation);
                } else {
                    relative.sub(translation);
                    rotation.transform(relative);
                }
            }
            var tangent = relative.crs(axis);
            var inward = axis.cpy().scl(tangent.dot(axis)).sub(tangent);
            float radius = inward.len();
            if (radius <= 0) return;
            float force = speed * speed / radius;
            if (initial) {
                tangent.nor().scl(speed * controller.deltaTime);
                inward.nor().scl(.5f * force * controller.deltaTimeSqr);
                int offset = particle * previous.strideSize;
                previous.data[offset] -= tangent.x + inward.x;
                previous.data[offset + 1] -= tangent.y + inward.y;
                previous.data[offset + 2] -= tangent.z + inward.z;
            } else {
                inward.nor().scl(force);
                int offset = particle * acceleration.strideSize;
                acceleration.data[offset] += inward.x;
                acceleration.data[offset + 1] += inward.y;
                acceleration.data[offset + 2] += inward.z;
            }
        }

        public void activateParticles(int start, int count) {
            super.activateParticles(start, count);
            for (int i = start; i < start + count; i++) apply(i, true);
        }

        public void update() {
            for (int i = 0; i < controller.particles.size; i++) apply(i, false);
        }

        public CircularAcceleration copy() {
            return new CircularAcceleration();
        }
    }

    private static final class CircularMotion extends DynamicsModifier.Angular {
        final int type;
        ParallelArray.FloatChannel position, previous, angles;

        CircularMotion(int type) {
            this.type = type;
        }

        public void allocateChannels() {
            super.allocateChannels();
            position = controller.particles.addChannel(ParticleChannels.Position);
            previous = controller.particles.addChannel(ParticleChannels.PreviousPosition);
            controller.particles.addChannel(ParticleChannels.Acceleration);
            angles =
                    controller.particles.addChannel(
                            new ParallelArray.ChannelDescriptor(
                                    controller.particleChannels.newId(), float.class, 4));
        }

        public void activateParticles(int start, int count) {
            super.activateParticles(start, count);
            var origin = controller.transform.getTranslation(new Vector3());
            for (int i = start; i < start + count; i++) {
                int at = i * position.strideSize, index = i * angles.strideSize;
                var delta =
                        new Vector3(position.data[at], position.data[at + 1], position.data[at + 2])
                                .sub(origin);
                float angle =
                        (type == 1
                                        ? MathUtils.atan2(delta.y, delta.x)
                                        : type == 2
                                                ? MathUtils.atan2(delta.z, delta.y)
                                                : MathUtils.atan2(delta.z, delta.x))
                                * MathUtils.radiansToDegrees;
                angles.data[index] = angle < 0 ? angle + 360 : angle;
                angles.data[index + 1] = delta.len();
            }
        }

        public void update() {
            var origin = controller.transform.getTranslation(new Vector3());
            for (int i = 0; i < controller.particles.size; i++) {
                int at = i * position.strideSize,
                        index = i * angles.strideSize,
                        s = i * strengthChannel.strideSize,
                        a = i * angularChannel.strideSize;
                float life = lifeChannel.data[i * lifeChannel.strideSize + 2];
                float
                        strength =
                                strengthChannel.data[s]
                                        + strengthChannel.data[s + 1]
                                                * strengthValue.getScale(life),
                        theta =
                                angularChannel.data[a]
                                        + angularChannel.data[a + 1] * thetaValue.getScale(life);
                float angle = (angles.data[index] + strength) % 360,
                        radius = angles.data[index + 1] + theta,
                        x = MathUtils.cosDeg(angle) * radius,
                        y = MathUtils.sinDeg(angle) * radius;
                int first = type == 2 ? 1 : 0, second = type == 1 ? 1 : 2;
                position.data[at + first] = (first == 0 ? origin.x : origin.y) + x;
                position.data[at + second] = (second == 1 ? origin.y : origin.z) + y;
                int old = i * previous.strideSize;
                previous.data[old + first] = position.data[at + first];
                previous.data[old + second] = position.data[at + second];
            }
        }

        public CircularMotion copy() {
            throw new UnsupportedOperationException();
        }
    }

    /** Reference AnimatedExt uses count - 1, unlike libGDX's built-in Animated. */
    private static final class AnimatedRegion extends RegionInfluencer {
        ParallelArray.FloatChannel life, regionChannel;

        public void allocateChannels() {
            super.allocateChannels();
            regionChannel = controller.particles.addChannel(ParticleChannels.TextureRegion);
            life = controller.particles.addChannel(ParticleChannels.Life);
        }

        public void update() {
            for (int i = 0; i < controller.particles.size; i++) {
                var r =
                        regions.get(
                                Math.min(
                                        regions.size - 1,
                                        (int)
                                                (life.data[i * life.strideSize + 2]
                                                        * (regions.size - 1))));
                int at = i * regionChannel.strideSize;
                regionChannel.data[at] = r.u;
                regionChannel.data[at + 1] = r.v;
                regionChannel.data[at + 2] = r.u2;
                regionChannel.data[at + 3] = r.v2;
                regionChannel.data[at + 4] = .5f;
                regionChannel.data[at + 5] = r.halfInvAspectRatio;
            }
        }

        public AnimatedRegion copy() {
            throw new UnsupportedOperationException();
        }
    }

    private record Billboard(
            Image image,
            Vector3 center,
            float width,
            float height,
            float cos,
            float sin,
            float[] color,
            float[] region,
            String mode,
            String origin,
            int batch) {}

    private static final class Channels extends ParticleControllerRenderer {
        ParallelArray.FloatChannel position, color, rotation, size, region;

        public void allocateChannels() {
            position = controller.particles.addChannel(ParticleChannels.Position);
            color =
                    controller.particles.addChannel(
                            ParticleChannels.Color, ParticleChannels.ColorInitializer.get());
            rotation =
                    controller.particles.addChannel(
                            ParticleChannels.Rotation2D,
                            ParticleChannels.Rotation2dInitializer.get());
            size = controller.particles.addChannel(XY);
            region =
                    controller.particles.addChannel(
                            ParticleChannels.TextureRegion,
                            ParticleChannels.TextureRegionInitializer.get());
        }

        public void update() {}

        public boolean isCompatible(ParticleBatch batch) {
            return false;
        }

        public Channels copy() {
            return new Channels();
        }
    }

    private static final class ScaleXY extends Influencer {
        final ScaledNumericValue x, y;
        ParallelArray.FloatChannel life, size, interpolation, base;
        ParallelArray.FloatChannel parentScale;
        int parentOffset;

        ScaleXY(Json json, JsonValue value) {
            x = scaled(json, value.get("valuex"));
            y = scaled(json, value.get("valuey"));
        }

        public void allocateChannels() {
            life = controller.particles.addChannel(ParticleChannels.Life);
            size = controller.particles.addChannel(XY);
            interpolation =
                    controller.particles.addChannel(
                            new ParallelArray.ChannelDescriptor(
                                    controller.particleChannels.newId(), float.class, 4));
            base =
                    controller.particles.addChannel(
                            new ParallelArray.ChannelDescriptor(
                                    controller.particleChannels.newId(), float.class, 2));
        }

        public void activateParticles(int start, int count) {
            for (int i = start; i < start + count; i++) {
                int at = i * 4;
                float lx = x.newLowValue() * controller.scale.x,
                        hx = x.newHighValue() * controller.scale.x - lx,
                        ly = y.newLowValue() * controller.scale.y,
                        hy = y.newHighValue() * controller.scale.y - ly;
                interpolation.data[at] = lx;
                interpolation.data[at + 1] = hx;
                interpolation.data[at + 2] = x.isRelative() ? lx : ly;
                interpolation.data[at + 3] = x.isRelative() ? hx : hy;
                base.data[i * 2] = parentScale == null ? 1 : parentScale.data[parentOffset];
                base.data[i * 2 + 1] = parentScale == null ? 1 : parentScale.data[parentOffset + 1];
                size.data[i * 2] = (lx + hx * x.getScale(0)) * base.data[i * 2];
                size.data[i * 2 + 1] =
                        (x.isRelative() ? lx + hx * x.getScale(0) : ly + hy * y.getScale(0))
                                * base.data[i * 2 + 1];
            }
        }

        public void update() {
            for (int i = 0; i < controller.particles.size; i++) {
                int at = i * 4;
                float t = life.data[i * life.strideSize + 2];
                size.data[i * 2] =
                        (interpolation.data[at] + interpolation.data[at + 1] * x.getScale(t))
                                * base.data[i * 2];
                size.data[i * 2 + 1] =
                        (interpolation.data[at + 2]
                                        + interpolation.data[at + 3]
                                                * (x.isRelative() ? x : y).getScale(t))
                                * base.data[i * 2 + 1];
            }
        }

        public ScaleXY copy() {
            throw new UnsupportedOperationException();
        }
    }

    /** DynamicsModifierExt.FaceModifier: orient each billboard using its 3D acceleration. */
    private static final class FaceModifier extends DynamicsModifier {
        ParallelArray.FloatChannel acceleration, rotation;
        float angle = 1;
        int type;

        public void read(Json json, JsonValue value) {
            super.read(json, value);
            angle = value.getFloat("angle", 1);
            type = value.getInt("type", 0);
        }

        public void allocateChannels() {
            acceleration = controller.particles.addChannel(ParticleChannels.Acceleration);
            rotation =
                    controller.particles.addChannel(
                            ParticleChannels.Rotation2D,
                            ParticleChannels.Rotation2dInitializer.get());
        }

        public void update() {
            var origin = controller.transform.getTranslation(new Vector3());
            for (int i = 0; i < controller.particles.size; i++) {
                int a = i * 3, r = i * 2;
                float ax = origin.x - acceleration.data[a] * 1000,
                        ay = origin.y - acceleration.data[a + 1] * 1000,
                        az = origin.z - acceleration.data[a + 2] * 1000;
                float degrees =
                        type == 1
                                ? MathUtils.atan2(az, ax) * MathUtils.radiansToDegrees + 90
                                : MathUtils.atan2(ax, ay) * MathUtils.radiansToDegrees;
                float radians = (degrees * angle + 180) * MathUtils.degreesToRadians;
                rotation.data[r] = MathUtils.cos(radians);
                rotation.data[r + 1] = MathUtils.sin(radians);
            }
        }

        public FaceModifier copy() {
            return new FaceModifier();
        }
    }

    private static final class SeededRectangle extends PrimitiveSpawnShapeValue {
        final java.util.Random random = new RandomXS128();
        final JsonValue config;

        SeededRectangle(JsonValue config) {
            this.config = config;
        }

        public SpawnShapeValue copy() {
            return new SeededRectangle(config);
        }

        public void start() {
            super.start();
            var seed = config.get("seed");
            if (seed != null && seed.getBoolean("active", false))
                random.setSeed(seed.getLong("value", 0));
        }

        public void spawnAux(Vector3 out, float percent) {
            float w = spawnWidth + spawnWidthDiff * spawnWidthValue.getScale(percent),
                    h = spawnHeight + spawnHeightDiff * spawnHeightValue.getScale(percent),
                    d = spawnDepth + spawnDepthDiff * spawnDepthValue.getScale(percent);
            if (!isEdges()) {
                out.set(
                        random.nextFloat() * w - w / 2,
                        random.nextFloat() * h - h / 2,
                        random.nextFloat() * d - d / 2);
                return;
            }
            int side = random.nextInt(3) - 1;
            float x = 0, y = 0, z = 0;
            if (side == -1) {
                x = random.nextInt(2) == 0 ? -w / 2 : w / 2;
                if (x == 0) {
                    y = random.nextInt(2) == 0 ? -h / 2 : h / 2;
                    z = random.nextInt(2) == 0 ? -d / 2 : d / 2;
                } else {
                    y = random.nextFloat() * h - h / 2;
                    z = random.nextFloat() * d - d / 2;
                }
            } else if (side == 0) {
                z = random.nextInt(2) == 0 ? -d / 2 : d / 2;
                if (z == 0) {
                    y = random.nextInt(2) == 0 ? -h / 2 : h / 2;
                    x = random.nextInt(2) == 0 ? -w / 2 : w / 2;
                } else {
                    y = random.nextFloat() * h - h / 2;
                    x = random.nextFloat() * w - w / 2;
                }
            } else {
                y = random.nextInt(2) == 0 ? -h / 2 : h / 2;
                if (y == 0) {
                    x = random.nextInt(2) == 0 ? -w / 2 : w / 2;
                    z = random.nextInt(2) == 0 ? -d / 2 : d / 2;
                } else {
                    x = random.nextFloat() * w - w / 2;
                    z = random.nextFloat() * d - d / 2;
                }
            }
            out.set(x, y, z);
        }
    }

    private static float sample(ScaledNumericValue value, float t, float diff, float low) {
        return low + diff * value.getScale(t);
    }

    private static float randomValue(java.util.Random random, float max) {
        return random.nextFloat() * max;
    }

    private static float randomValue(java.util.Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private static final class SeededEllipse extends PrimitiveSpawnShapeValue {
        final JsonValue config;
        final java.util.Random random = new RandomXS128();
        float even, iter = -1, lastT, lastZ, lastRadiusX, lastRadiusY, lastRadiusZ;

        enum Side {
            top,
            bottom,
            both
        }

        Side side;
        NumericValue evenCount = new NumericValue(), evenAngleBonus = new NumericValue();
        boolean seeded;

        SeededEllipse(JsonValue config) {
            this.config = config;
            side = Side.valueOf(config.getString("side", "both"));
            if (config.get("evenCount") != null)
                evenCount.setValue(config.get("evenCount").getFloat("value", 0));
            if (config.get("evenAngleBonus") != null)
                evenAngleBonus.setValue(config.get("evenAngleBonus").getFloat("value", 0));
            seeded = config.get("seed") != null && config.get("seed").getBoolean("active", false);
        }

        public SpawnShapeValue copy() {
            return new SeededEllipse(config);
        }

        public void start() {
            super.start();
            even = 0;
            iter = -1;
            var seed = config.get("seed");
            if (seed != null && seed.getBoolean("active", false))
                random.setSeed(seed.getLong("value", 0));
        }

        public void spawnAux(Vector3 var1, float var2) {
            float var3 = super.spawnWidth;
            float var4 = super.spawnWidthDiff;
            var3 = sample(super.spawnWidthValue, var2, var4, var3);
            var4 = super.spawnHeight;
            float var5 = super.spawnHeightDiff;
            var4 = sample(super.spawnHeightValue, var2, var5, var4);
            var5 = super.spawnDepth;
            float var6 = super.spawnDepthDiff;
            var2 = sample(super.spawnDepthValue, var2, var6, var5);
            var5 = 0.0F;
            var6 = ((float) Math.PI * 2F);
            Side var7;
            if ((var7 = this.side) == Side.top) {
                var6 = (float) Math.PI;
            } else if (var7 == Side.bottom) {
                var6 = -(float) Math.PI;
            }

            boolean var45 = true;
            if (this.evenCount.getValue() <= 0.0F) {
                if (this.evenCount.getValue() != 0.0F && seeded) {
                    float var8;
                    if (!((var8 = this.iter) < 0.0F)
                            && var8 % (float) (-((int) this.evenCount.getValue())) != 0.0F) {
                        var45 = false;
                    } else {
                        this.iter = 0.0F;
                    }

                    ++this.iter;
                }

                if (var45) {
                    this.lastT = randomValue(this.random, var5, var6);
                }

                var5 = this.lastT;
            } else {
                var5 = this.even;
                this.even = var6 / this.evenCount.getValue() + var5;
                var5 = this.evenAngleBonus.getValue() + this.even;
                if (seeded) {
                    var5 +=
                            (float) config.get("seed").getFloat("value", 0)
                                            / 360.0F
                                            * ((float) Math.PI * 2F)
                                    + this.even;
                }
            }

            if (isEdges()) {
                if (var3 == 0.0F) {
                    float var15 = 0.0F;
                    float var21 = var4 / 2.0F;
                    float var22 = MathUtils.sin(var5) * var21;
                    var2 /= 2.0F;
                    float var16 = MathUtils.cos(var5) * var2;
                    var1.x = var15;
                    var1.y = var22;
                    var1.z = var16;
                    return;
                }

                if (var4 == 0.0F) {
                    float var12 = var3 / 2.0F;
                    var12 = MathUtils.cos(var5) * var12;
                    float var20 = 0.0F;
                    var2 /= 2.0F;
                    float var14 = MathUtils.sin(var5) * var2;
                    var1.x = var12;
                    var1.y = var20;
                    var1.z = var14;
                    return;
                }

                if (var2 == 0.0F) {
                    float var9 = var3 / 2.0F;
                    var9 = MathUtils.cos(var5) * var9;
                    float var18 = var4 / 2.0F;
                    float var11 = MathUtils.sin(var5) * var18;
                    float var19 = 0.0F;
                    var1.x = var9;
                    var1.y = var11;
                    var1.z = var19;
                    return;
                }

                var2 = var3 / 2.0F;
                var3 = var4 / 2.0F;
                var4 = var2 / 2.0F;
            } else {
                var3 = randomValue(this.random, var3 / 2.0F);
                var4 = randomValue(this.random, var4 / 2.0F);
                var2 = randomValue(this.random, var2 / 2.0F);
                var4 = var2;
                var3 = var4;
                var2 = var3;
            }

            var6 = randomValue(this.random, -1.0F, 1.0F);
            if (var45) {
                this.lastZ = var6;
                this.lastRadiusX = var2;
                this.lastRadiusY = var3;
                this.lastRadiusZ = var4;
            }

            float var10004 = this.lastZ;
            float var23 = (float) Math.sqrt((double) (1.0F - var10004 * var10004));
            var2 = this.lastRadiusX * var23;
            float var60 = MathUtils.cos(var5) * var2;
            float var24 = this.lastRadiusY * var23;
            float var25 = MathUtils.sin(var5) * var24;
            float var17 = this.lastRadiusZ * var6;
            var1.x = var60;
            var1.y = var25;
            var1.z = var17;
        }
    }
}
