package top.totodile.pokemmo.world;

/** 跟随精灵沿玩家经过的上一格插值，不参与玩家碰撞或服务端位置裁决。 */
public final class FollowerTrail {
    private double fromX, fromY, targetX, targetY;
    private long started, duration = 1;
    private int facing;
    public void reset(double x, double y, int direction) {
        fromX = targetX = x; fromY = targetY = y; facing = direction; started = 0; duration = 1;
    }
    public double x(long now) { return fromX + (targetX - fromX) * progress(now); }
    public double y(long now) { return fromY + (targetY - fromY) * progress(now); }
    private double progress(long now) { return Math.clamp((now - started) / (double) duration, 0, 1); }
    public boolean moving(long now) { return progress(now) < 1 && (fromX != targetX || fromY != targetY); }
    public int facing() { return facing; }
    public void follow(double x, double y, long start, long nanos, long now) {
        double currentX = x(now), currentY = y(now);
        double dx = x - currentX, dy = y - currentY;
        if (Math.abs(dx) > Math.abs(dy)) facing = dx < 0 ? 2 : 3;
        else if (dy != 0) facing = dy < 0 ? 1 : 0;
        fromX = currentX; fromY = currentY; targetX = x; targetY = y;
        started = start; duration = Math.max(1, nanos);
    }
    public void translate(double dx, double dy) {
        fromX += dx; targetX += dx; fromY += dy; targetY += dy;
    }
}