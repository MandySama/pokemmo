package top.totodile.pokemmo.client.world;

import top.totodile.pokemmo.server.game.GameService.Position;

import java.util.function.Consumer;

/** 本地地图移动时间轴。保留跨格剩余时间，帧率不影响移动速度。 */
public final class PlayerMotion {
    private static final float TURN_SECONDS = .1f;

    // MapDirectionElevationUtils.li / NA / GP；冲浪沿用 GdxParticleEmitterNode.ud 的 100ms。
    public enum Mode {
        WALK(.275f, 0),
        RUN(.135f, 1),
        BICYCLE(.080f, 2),
        SURF(.100f, 3);
        public final float seconds;
        public final int animation;

        Mode(float seconds, int animation) {
            this.seconds = seconds;
            this.animation = animation;
        }
    }

    private final MapMovement movement;
    private final Consumer<Position> save;
    private boolean running, bicycle, moving, surfing;
    private float speed = 1, time, duration, fromX, fromY, animationTime;
    private Mode stepMode = Mode.WALK;
    private float idleTime = TURN_SECONDS, turnTime;
    private Runnable stateChanged = () -> {};

    public record Step(
            Position before, Position after, int sourceX, int sourceY, boolean continuous) {}

    private Consumer<Step> stepped = step -> {};
    private final java.util.List<Consumer<Step>> stepListeners=new java.util.ArrayList<>();
    private final java.util.List<Consumer<Position>> blockedListeners=new java.util.ArrayList<>();
    public void addBlockedListener(Consumer<Position> listener){blockedListeners.add(listener);}
    public void addStepListener(Consumer<Step> listener) { stepListeners.add(java.util.Objects.requireNonNull(listener)); }

    public void onStep(Consumer<Step> callback) {
        stepped = java.util.Objects.requireNonNull(callback);
    }

    public float jumpHeight(){return moving&&movement.jumping()?(float)Math.sin(Math.PI*stepProgress())*8:0;}

    public float stepProgress() {
        return moving ? Math.min(1, time / duration) : 1;
    }

    public PlayerMotion(MapMovement movement, boolean running, Consumer<Position> save) {
        this.movement = movement;
        this.running = running;
        this.save = save;
        surfing = movement.water();
    }

    public MapMovement movement() {
        return movement;
    }

    public boolean running() {
        return running;
    }

    public boolean bicycle() {
        return bicycle;
    }

    public boolean surfing() {
        return surfing;
    }

    public boolean allowsFollower() {
        return !bicycle && !surfing;
    }

    public boolean moving() {
        return moving;
    }

    public float speed() {
        return speed;
    }

    public float animationTime() {
        return animationTime;
    }

    public Mode mode() {
        return moving
                ? stepMode
                : surfing ? Mode.SURF : bicycle ? Mode.BICYCLE : running ? Mode.RUN : Mode.WALK;
    }

    public void onStateChanged(Runnable callback) {
        stateChanged = java.util.Objects.requireNonNull(callback);
    }

    public void toggleRunning() {
        running = !running;
        stateChanged.run();
    }

    public void toggleBicycle() {
        if (surfing) return;
        bicycle = !bicycle;
        stateChanged.run();
    }

    public void setSpeed(float value) {
        if (!Float.isFinite(value) || value <= 0 || value > 16)
            throw new IllegalArgumentException("倍率必须大于 0 且不超过 16。");
        if (speed != value) {
            speed = value;
            stateChanged.run();
        }
    }

    public float x() {
        return moving
                ? fromX + (movement.position().x() - fromX) * Math.min(1, time / duration)
                : movement.position().x();
    }

    public float y() {
        return moving
                ? fromY + (movement.position().y() - fromY) * Math.min(1, time / duration)
                : movement.position().y();
    }

    /** Server scene position correction ends any predicted step before scripted movement. */
    public void resetStep() {
        moving = false;
        time = turnTime = 0;
        idleTime = TURN_SECONDS;
    }

    public boolean move(int dx, int dy) {
        if (moving) return false;
        Position before = movement.position();
        if (!movement.step(dx, dy)) {
            blockedListeners.forEach(listener->listener.accept(movement.position()));
            if (before.direction() != movement.position().direction())
                save.accept(movement.position());
            return false;
        }
        Position after = movement.position();
        fromX = movement.stepFromX();
        fromY = movement.stepFromY();
        time = 0;
        boolean wasSurfing = surfing, wasBicycle = bicycle;
        surfing = movement.water();
        if (surfing) bicycle = false;
        if (surfing != wasSurfing || bicycle != wasBicycle) stateChanged.run();
        stepMode = surfing ? Mode.SURF : bicycle ? Mode.BICYCLE : running ? Mode.RUN : Mode.WALK;
        duration = stepMode.seconds*(movement.jumping()?2:1);
        moving = movement.continuousStep();
        stepped.accept(new Step(before, after, movement.stepFromX(), movement.stepFromY(), moving));
        var step=new Step(before,after,movement.stepFromX(),movement.stepFromY(),moving);
        stepListeners.forEach(listener->listener.accept(step));
        save.accept(after);
        return true;
    }

    public void update(float delta, int dx, int dy) {
        if (!Float.isFinite(delta) || delta < 0) return;
        float elapsed = Math.min(delta, .25f);
        if(elapsed<=0)return;
        int forced=movement.forcedDirection();
        if(forced>=0){int[] xs={0,0,-1,1},ys={1,-1,0,0};dx=xs[forced];dy=ys[forced];turnTime=0;}
        boolean input = Math.abs(dx) + Math.abs(dy) == 1;
        if (!input) turnTime = 0;
        if (elapsed <= 0) return;
        // AbstractClientProtocolContext.uh0 / GdxParticleEmitterNode.qc0：
        // 静止改变朝向是独立动作，之后 100ms 才能迈步；持续行走时不插入转身停顿。
        if (!moving
                && input
                && forced<0 && (idleTime >= TURN_SECONDS || turnTime > 0)
                && movement.face(dx, dy)) {
            save.accept(movement.position());
            turnTime = TURN_SECONDS;
            idleTime = 0;
        }
        if (turnTime > 0) {
            float consumed = Math.min(elapsed, turnTime);
            turnTime -= consumed;
            elapsed -= consumed;
            if (elapsed <= .000001f) return;
        }
        float remaining = elapsed * speed;
        for (int steps = 0; steps < 64; steps++) {
            if (!moving) {
                int nextForced=movement.forcedDirection();if(nextForced>=0){forced=nextForced;int[] xs={0,0,-1,1},ys={1,-1,0,0};dx=xs[forced];dy=ys[forced];}else if(forced>=0)break;
                if (remaining <= 0 || !input || !move(dx, dy)) break;
                idleTime = 0;
                if (!moving) break; // 真正的传送门切换后从目标位置继续下一帧。
            }
            idleTime = 0;
            float advance = Math.min(remaining, duration - time);
            time += advance;
            animationTime += advance;
            remaining -= advance;
            if (time + .000001f >= duration) {
                time = duration;
                moving = false;
            } else break;
        }
        if (!moving) idleTime += remaining / speed;
    }
}
