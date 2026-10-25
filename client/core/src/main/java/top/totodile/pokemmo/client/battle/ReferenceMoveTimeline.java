package top.totodile.pokemmo.client.battle;

import java.util.*;
import java.util.function.*;

/**
 * Composes sequential and parallel animation steps with quadratic easing. Reference identifiers are
 * translated by the import tool, outside the runtime API.
 */
public final class ReferenceMoveTimeline {

    private interface Node {

        float duration();

        void flatten(float start, List<Track> tracks);
    }

    private static final class Group implements Node {

        final boolean parallel;

        final Group parent;

        final List<Node> children = new ArrayList<>();

        Group(boolean parallel, Group parent) {
            this.parallel = parallel;
            this.parent = parent;
        }

        public float duration() {
            float totalDuration = 0;
            for (var child : children)
                totalDuration =
                        parallel
                                ? Math.max(totalDuration, child.duration())
                                : totalDuration + child.duration();
            return totalDuration;
        }

        public void flatten(float start, List<Track> tracks) {
            float timeOffset = 0;
            for (var child : children) {
                child.flatten(start + timeOffset, tracks);
                if (!parallel) timeOffset += child.duration();
            }
        }
    }

    private static final class Track implements Node {

        float start;

        final float duration;

        final Supplier<float[]> readValues;

        final Consumer<float[]> writeValues;

        final float[] targetValues;

        float[] initialValues;

        boolean complete;

        Track(
                float duration,
                Supplier<float[]> readValues,
                Consumer<float[]> writeValues,
                float[] targetValues) {
            this.duration = duration;
            this.readValues = readValues;
            this.writeValues = writeValues;
            this.targetValues = targetValues;
        }

        public float duration() {
            return duration;
        }

        public void flatten(float start, List<Track> tracks) {
            this.start = start;
            tracks.add(this);
        }

        void update(float targetTime) {
            if (targetTime < start || complete) return;
            if (initialValues == null) initialValues = readValues.get();
            float progress =
                    duration == 0 ? 1 : Math.min(1, Math.max(0, (targetTime - start) / duration));
            progress =
                    progress < .5f
                            ? 2 * progress * progress
                            : 1 - 2 * (1 - progress) * (1 - progress);
            float[] interpolatedValues = new float[targetValues.length];
            for (int i = 0; i < interpolatedValues.length; i++)
                interpolatedValues[i] =
                        initialValues[i] + (targetValues[i] - initialValues[i]) * progress;
            writeValues.accept(interpolatedValues);
            complete = targetTime >= start + duration;
        }
    }

    private final Group root;

    private Group current;

    private List<Track> tracks;

    private float elapsed;

    private ReferenceMoveTimeline(boolean parallel) {
        root = current = new Group(parallel, null);
    }

    public static ReferenceMoveTimeline sequence() {
        return new ReferenceMoveTimeline(false);
    }

    public static ReferenceMoveTimeline parallel() {
        return new ReferenceMoveTimeline(true);
    }

    public ReferenceMoveTimeline beginParallel() {
        return group(true);
    }

    public ReferenceMoveTimeline beginSequence() {
        return group(false);
    }

    private ReferenceMoveTimeline group(boolean parallel) {
        var child = new Group(parallel, current);
        current.children.add(child);
        current = child;
        return this;
    }

    public ReferenceMoveTimeline endGroup() {
        if (current.parent == null)
            throw new IllegalStateException("Unbalanced reference timeline");
        current = current.parent;
        return this;
    }

    public ReferenceMoveTimeline append(ReferenceMoveTimeline childTimeline) {
        if (childTimeline.current != childTimeline.root)
            throw new IllegalStateException("Unclosed reference timeline");
        current.children.add(childTimeline.root);
        return this;
    }

    public ReferenceMoveTimeline waitSeconds(float seconds) {
        current.children.add(new Track(seconds, () -> new float[0], v -> {}, new float[0]));
        return this;
    }

    public ReferenceMoveTimeline onStart(Runnable action) {
        current.children.add(new Track(0, () -> new float[0], v -> action.run(), new float[0]));
        return this;
    }

    static ReferenceMoveTimeline tween(
            float seconds,
            Supplier<float[]> readValues,
            Consumer<float[]> writeValues,
            float... targetValues) {
        var timeline = sequence();
        timeline.current.children.add(new Track(seconds, readValues, writeValues, targetValues));
        return timeline;
    }

    public static ReferenceMoveTimeline call(Runnable action) {
        return sequence().onStart(action);
    }

    public static Runnable callback(Runnable action) {
        return action;
    }

    public static ReferenceMoveTimeline appendVisibilityStep(
            EncounterMoveAnimation owner,
            int targetValues,
            int mode,
            ReferenceMoveTimeline timeline,
            float delay) {
        return timeline.append(owner.setCombatantVisibility(targetValues, mode))
                .endGroup()
                .endGroup()
                .beginSequence()
                .waitSeconds(delay)
                .beginParallel();
    }

    float duration() {
        return root.duration();
    }

    void validate() {
        if (current != root) throw new IllegalStateException("Unclosed move timeline");
        if (!Float.isFinite(duration()) || duration() < 0)
            throw new IllegalStateException("Invalid move duration");
    }

    float time() {
        return elapsed;
    }

    void update(float targetTime) {
        if (tracks == null) {
            if (current != root) throw new IllegalStateException("Unclosed move timeline");
            tracks = new ArrayList<>();
            root.flatten(0, tracks);
            tracks.sort(Comparator.comparingDouble(progress -> progress.start));
        }
        // Sample through boundaries so completion writes and subsequent captures remain ordered
        // after a stalled frame.
        while (elapsed < targetTime) {
            float previousTime = elapsed, nextTime = Math.min(targetTime, elapsed + 1 / 120f);
            elapsed = nextTime;
            for (var track : tracks) {
                if (previousTime <= track.start + track.duration && nextTime >= track.start)
                    track.update(Math.min(nextTime, track.start + track.duration));
            }
        }
    }

    public static ReferenceMoveTimeline appendThenNextParallel(
            ReferenceMoveTimeline parent, ReferenceMoveTimeline child) {
        return parent.append(child).endGroup().beginParallel();
    }

    public static ReferenceMoveTimeline appendThenOuterParallel(
            ReferenceMoveTimeline parent, ReferenceMoveTimeline child) {
        return parent.append(child).endGroup().endGroup().endGroup().beginParallel();
    }

    public static ReferenceMoveTimeline appendThenDelayedParallel(
            ReferenceMoveTimeline parent, ReferenceMoveTimeline child, float delay) {
        return parent.append(child).beginSequence().waitSeconds(delay).beginParallel();
    }

    public static ReferenceMoveTimeline appendThenDelayedOuterParallel(
            ReferenceMoveTimeline parent, ReferenceMoveTimeline child, float delay) {
        return parent.append(child)
                .endGroup()
                .endGroup()
                .beginSequence()
                .waitSeconds(delay)
                .beginParallel();
    }

    public static ReferenceMoveTimeline waitThenParallel(float delay) {
        return sequence().beginParallel().waitSeconds(delay).endGroup().beginParallel();
    }

    public static ReferenceMoveTimeline appendAndClose(
            ReferenceMoveTimeline parent, ReferenceMoveTimeline child) {
        return parent.append(child).endGroup().beginParallel().endGroup();
    }
}
