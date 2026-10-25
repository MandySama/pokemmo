package top.totodile.pokemmo.client.ui.pokemon;

import com.badlogic.gdx.graphics.g3d.particles.*;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;

import java.util.*;

/**
 * Port of DynamicsModifierExt.VectorPathModifier; caster/target are explicit instead of co_1
 * globals.
 */
final class ReferenceParticlePath extends DynamicsModifier.Strength {
    private record Entry(Vector3 position, Vector3 offset, int modifier) {}

    private final Vector3 caster, target;
    private final boolean targetSpawn;
    private final List<Entry> entries = new ArrayList<>();
    private final Map<Integer, Path<Vector3>> paths = new HashMap<>();
    private final RandomXS128 random = new RandomXS128();
    private ParallelArray.FloatChannel position, previous, spawn, rotation;
    private ParallelArray.IntChannel pathIds;
    private int type, cacheId;
    private boolean continuous, rotate, spawnOnly, randomByIndex, useSpawn;
    private float travel, degree, currentLife;
    private String ease = "";

    ReferenceParticlePath(Vector3 caster, Vector3 target, boolean targetSpawn) {
        this.caster = caster.cpy();
        this.target = target.cpy();
        this.targetSpawn = targetSpawn;
    }

    private static Vector3 vector(JsonValue value) {
        return value == null
                ? new Vector3()
                : new Vector3(
                        value.getFloat("x", 0), value.getFloat("y", 0), value.getFloat("z", 0));
    }

    @Override
    public void read(Json json, JsonValue value) {
        super.read(json, value);
        entries.clear();
        if (value.has("vectorPath")) {
            int i = 0;
            for (var v : value.get("vectorPath"))
                entries.add(
                        new Entry(vector(v), new Vector3(), value.get("vectorMod").getInt(i++)));
        } else if (value.has("vectorArray")) {
            for (var v : value.get("vectorArray"))
                entries.add(
                        new Entry(
                                vector(v.get("pos")),
                                vector(v.get("offset")),
                                v.getInt("modifier", 0)));
        } else {
            entries.add(new Entry(new Vector3(), new Vector3(), 1));
            entries.add(new Entry(new Vector3(), new Vector3(), 2));
        }
        type = value.getInt("pathType", 0);
        continuous = value.getBoolean("isContinuous", false);
        rotate = value.getBoolean("rotateTarget", false);
        spawnOnly = value.getBoolean("spawnOnly", false);
        randomByIndex = value.getBoolean("randByIdx", false);
        useSpawn = value.getBoolean("useSpawnPosition", false);
        degree = value.has("degree") ? value.get("degree").getFloat("value", 0) : 0;
        travel =
                value.has("speed") && value.get("speed").getBoolean("active", false)
                        ? value.get("speed").getFloat("value", 1)
                        : 0;
        ease = value.has("ease") ? value.get("ease").getString("class", "") : "";
    }

    public void allocateChannels() {
        super.allocateChannels();
        position = controller.particles.addChannel(ParticleChannels.Position);
        previous = controller.particles.addChannel(ParticleChannels.PreviousPosition);
        controller.particles.addChannel(ParticleChannels.Acceleration);
        pathIds =
                controller.particles.addChannel(
                        new ParallelArray.ChannelDescriptor(
                                controller.particleChannels.newId(), int.class, 1));
        if (useSpawn)
            spawn =
                    controller.particles.addChannel(
                            new ParallelArray.ChannelDescriptor(
                                    controller.particleChannels.newId(), float.class, 3));
        if (rotate) rotation = controller.particles.addChannel(ParticleChannels.Rotation2D);
    }

    public void init() {
        super.init();
        paths.clear();
        random.setSeed((long) strengthValue.getHighMin());
        cacheId = 0;
        currentLife = 0;
    }

    public void start() {
        super.start();
        currentLife = 0;
    }

    private void initPath(int index) {
        if (entries.size() < 2) return;
        if (strengthValue.getLowMin() > 0) index = (int) (index % strengthValue.getLowMin());
        if (paths.containsKey(index)) return;
        var points = new Vector3[entries.size() + (type == 2 ? 0 : 2)];
        boolean flip = entries.get(0).modifier != 0 && isGlobal && targetSpawn;
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            if (type != 2 && randomByIndex)
                random.setSeed((long) index + (long) strengthValue.getHighMin());
            if (type != 2 && entry.modifier != 0 && isGlobal) flip = targetSpawn;
            boolean invert = type == 2 ? entry.modifier != 0 && isGlobal && targetSpawn : flip;
            var point = entry.position.cpy();
            if (invert) {
                point.x = -point.x;
                point.z = -point.z;
            }
            if (type != 2)
                point.add(
                        (random.nextFloat() * 2 - 1) * entry.offset.x,
                        (random.nextFloat() * 2 - 1) * entry.offset.y,
                        (random.nextFloat() * 2 - 1) * entry.offset.z);
            if (entry.modifier == 1) point.add(caster);
            else if (entry.modifier == 2) point.add(target);
            points[i + (type == 2 ? 0 : 1)] = point;
        }
        if (type != 2) {
            points[0] = points[1].cpy();
            points[points.length - 1] = points[points.length - 2].cpy();
        }
        Path<Vector3> path =
                switch (type) {
                    case 0 -> new CatmullRomSpline<>(points, continuous);
                    case 1 -> new BSpline<>(points, 3, continuous);
                    case 2 -> new Bezier<>(points);
                    default -> throw new IllegalArgumentException("Reference path type " + type);
                };
        paths.put(index, path);
    }

    public void activateParticles(int start, int count) {
        for (int i = start; i < start + count; i++) {
            initPath(i);
            int id =
                    strengthValue.getLowMin() > 0
                            ? (int) (cacheId++ % strengthValue.getLowMin())
                            : i;
            pathIds.data[i] = id;
            float t = 0;
            if (!strengthValue.isRelative() || spawnOnly) {
                t = controller.emitter.percent;
                if (spawnOnly) {
                    t =
                            ((com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter)
                                                    controller.emitter)
                                            .durationValue.getLowMax()
                                    / lifeChannel.data[i * lifeChannel.strideSize]
                                    * t;
                    while (t > 1) t--;
                }
            }
            var point = new Vector3();
            var path = paths.get(id);
            if (path != null) path.valueAt(point, t);
            int at = i * position.strideSize;
            var base = new Vector3(position.data[at], position.data[at + 1], position.data[at + 2]);
            if (useSpawn) {
                int sp = i * spawn.strideSize;
                spawn.data[sp] = base.x;
                spawn.data[sp + 1] = base.y;
                spawn.data[sp + 2] = base.z;
            }
            if (!strengthValue.isRelative() && !spawnOnly) {
                if (useSpawn) point.add(base);
                position.data[at] = point.x;
                position.data[at + 1] = point.y;
                position.data[at + 2] = point.z;
            } else controller.transform.setTranslation(point);
        }
        super.activateParticles(start, count);
    }

    private float ease(float t) {
        if (ease.contains("Quad$1")) return t * t;
        if (ease.contains("Quad$2")) return -t * (t - 2);
        if (ease.contains("Quad$3")) return t < .5f ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);
        if (ease.contains("Quart$2")) {
            float x = t - 1;
            return 1 - x * x * x * x;
        }
        if (ease.isEmpty() || ease.contains("Linear$1")) return t;
        throw new IllegalArgumentException("Reference path ease " + ease);
    }

    public void update() {
        if (spawnOnly) return;
        currentLife += controller.deltaTime;
        var translation = controller.transform.getTranslation(new Vector3());
        for (int i = 0; i < controller.particles.size; i++) {
            int id = pathIds.data[i];
            if (id < 0) continue;
            var path = paths.get(id);
            if (path == null) {
                initPath(0);
                path = paths.get(0);
            }
            if (path == null) return;
            float t =
                    ease(
                            travel > 0
                                    ? Math.min(1, currentLife * 1000 / travel)
                                    : lifeChannel.data[i * lifeChannel.strideSize + 2]);
            var point = path.valueAt(new Vector3(), t);
            var before = rotate ? path.valueAt(new Vector3(), t - .025f) : new Vector3();
            if (t < .025f) point.set(999, 999, 999);
            if (useSpawn) {
                int sp = i * spawn.strideSize;
                point.add(spawn.data[sp], spawn.data[sp + 1], spawn.data[sp + 2]).sub(translation);
            }
            if (t >= 1) pathIds.data[i] = -1;
            int at = i * position.strideSize;
            if (!strengthValue.isRelative()) {
                position.data[at] = point.x;
                position.data[at + 1] = point.y;
                position.data[at + 2] = point.z;
            } else controller.transform.setTranslation(point);
            if (rotate) {
                float angle =
                        degree
                                + MathUtils.atan2(point.z - before.z, point.x - before.x)
                                        * MathUtils.radiansToDegrees;
                int rot = i * rotation.strideSize;
                rotation.data[rot] = MathUtils.cosDeg(angle);
                rotation.data[rot + 1] = MathUtils.sinDeg(angle);
            }
            if (strengthValue.isRelative()) break;
        }
    }

    public ReferenceParticlePath copy() {
        throw new UnsupportedOperationException();
    }
}
