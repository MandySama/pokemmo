package top.totodile.pokemmo.client.world;

import java.util.function.Consumer;
import top.totodile.pokemmo.server.game.GameService.Position;

/** 本地地图移动时间轴。保留跨格剩余时间，帧率不影响移动速度。 */
public final class PlayerMotion {
    private static final float TURN_SECONDS=.1f;
    // 参考 MapDirectionElevationUtils.li / NA / GP，单位为每格秒数。
    public enum Mode {
        WALK(.275f,0), RUN(.135f,1), BICYCLE(.080f,2);
        public final float seconds; public final int animation;
        Mode(float seconds,int animation) { this.seconds=seconds; this.animation=animation; }
    }
    private final MapMovement movement;
    private final Consumer<Position> save;
    private boolean running,bicycle,moving;
    private float speed=1,time,duration,fromX,fromY,animationTime;
    private Mode stepMode=Mode.WALK;
    private float idleTime=TURN_SECONDS,turnTime;
    private Runnable stateChanged=() -> {};
    public PlayerMotion(MapMovement movement,boolean running,Consumer<Position> save) {
        this.movement=movement; this.running=running; this.save=save;
    }
    public MapMovement movement() { return movement; }
    public boolean running() { return running; }
    public boolean bicycle() { return bicycle; }
    public boolean moving() { return moving; }
    public float speed() { return speed; }
    public float animationTime() { return animationTime; }
    public Mode mode() { return moving?stepMode:bicycle?Mode.BICYCLE:running?Mode.RUN:Mode.WALK; }
    public void onStateChanged(Runnable callback) { stateChanged=java.util.Objects.requireNonNull(callback); }
    public void toggleRunning() { running=!running;stateChanged.run(); }
    public void toggleBicycle() { bicycle=!bicycle;stateChanged.run(); }
    public void setSpeed(float value) {
        if(!Float.isFinite(value) || value<=0 || value>16) throw new IllegalArgumentException("倍率必须大于 0 且不超过 16。");
        if(speed!=value) { speed=value;stateChanged.run(); }
    }
    public float x() { return moving?fromX+(movement.position().x()-fromX)*Math.min(1,time/duration):movement.position().x(); }
    public float y() { return moving?fromY+(movement.position().y()-fromY)*Math.min(1,time/duration):movement.position().y(); }
    public boolean move(int dx,int dy) {
        if(moving) return false;
        Position before=movement.position();
        if(!movement.step(dx,dy)) {
            if(before.direction()!=movement.position().direction() && movement.canStand())save.accept(movement.position());
            return false;
        }
        Position after=movement.position();
        fromX=before.x(); fromY=before.y(); time=0;
        stepMode=bicycle?Mode.BICYCLE:running?Mode.RUN:Mode.WALK;
        duration=stepMode.seconds;
        moving=before.bank()==after.bank() && before.map()==after.map()
                && Math.abs(before.x()-after.x())+Math.abs(before.y()-after.y())==1;
        // 穿墙期间只保存正常地面，避免下次登录落在墙里而无法进入地图。
        if(movement.canStand()) save.accept(after);
        return true;
    }
    public void update(float delta,int dx,int dy) {
        if(!Float.isFinite(delta) || delta<0) return;
        float elapsed=Math.min(delta,.25f);
        boolean input=Math.abs(dx)+Math.abs(dy)==1;
        if(!input)turnTime=0;
        if(elapsed<=0)return;
        // AbstractClientProtocolContext.uh0 / GdxParticleEmitterNode.qc0：
        // 静止改变朝向是独立动作，之后 100ms 才能迈步；持续行走时不插入转身停顿。
        if(!moving && input && (idleTime>=TURN_SECONDS || turnTime>0) && movement.face(dx,dy)) {
            if(movement.canStand())save.accept(movement.position());
            turnTime=TURN_SECONDS;idleTime=0;
        }
        if(turnTime>0) {
            float consumed=Math.min(elapsed,turnTime);
            turnTime-=consumed;elapsed-=consumed;
            if(elapsed<=.000001f)return;
        }
        float remaining=elapsed*speed;
        for(int steps=0;steps<64;steps++) {
            if(!moving) {
                if(remaining<=0 || !input || !move(dx,dy)) break;
                idleTime=0;
                if(!moving) break; // 门和地图连接切换后从目标位置继续下一帧。
            }
            idleTime=0;
            float advance=Math.min(remaining,duration-time);
            time+=advance; animationTime+=advance; remaining-=advance;
            if(time+.000001f>=duration) { time=duration; moving=false; }
            else break;
        }
        if(!moving)idleTime+=remaining/speed;
    }
}
