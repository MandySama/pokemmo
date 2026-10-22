package top.totodile.pokemmo.client.world;

import java.util.function.Consumer;
import top.totodile.pokemmo.server.game.GameService.Position;

/** 本地地图移动时间轴。保留跨格剩余时间，帧率不影响移动速度。 */
public final class PlayerMotion {
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
    public void toggleRunning() { running=!running; }
    public void toggleBicycle() { bicycle=!bicycle; }
    public void setSpeed(float value) {
        if(!Float.isFinite(value) || value<=0 || value>16) throw new IllegalArgumentException("倍率必须大于 0 且不超过 16。");
        speed=value;
    }
    public float x() { return moving?fromX+(movement.position().x()-fromX)*Math.min(1,time/duration):movement.position().x(); }
    public float y() { return moving?fromY+(movement.position().y()-fromY)*Math.min(1,time/duration):movement.position().y(); }
    public boolean move(int dx,int dy) {
        if(moving) return false;
        Position before=movement.position();
        if(!movement.step(dx,dy)) return false;
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
        float remaining=Math.min(delta,.25f)*speed;
        for(int steps=0;steps<64;steps++) {
            if(!moving) {
                if(remaining<=0 || dx==0 && dy==0 || !move(dx,dy)) break;
                if(!moving) break; // 门和地图连接切换后从目标位置继续下一帧。
            }
            float advance=Math.min(remaining,duration-time);
            time+=advance; animationTime+=advance; remaining-=advance;
            if(time+.000001f>=duration) { time=duration; moving=false; }
            else break;
        }
    }
}
