package top.totodile.pokemmo.client.world;

/** MapZoneConnectionDescriptor.oZ: stand behind, then consume the owner's preceding tiles. */
public final class FollowerMotion {
    private float fromX, fromY, targetX, targetY;
    private int direction;
    private boolean stepping;

    public void reset(float x, float y, int direction) {
        fromX = targetX = x;
        fromY = targetY = y;
        this.direction = direction;
        stepping = false;
    }

    public void step(PlayerMotion.Step step) {
        if (!step.continuous()) {
            reset(step.after().x(), step.after().y(), step.after().direction());
            return;
        }
        // Rebase the preceding follower tile at a connected-map boundary.
        fromX = targetX + step.sourceX() - step.before().x();
        fromY = targetY + step.sourceY() - step.before().y();
        targetX = step.sourceX();
        targetY = step.sourceY();
        float dx = targetX - fromX, dy = targetY - fromY;
        if (Math.abs(dx) + Math.abs(dy) > 2) {
            fromX = targetX;
            fromY = targetY;
        } else if (Math.abs(dx) + Math.abs(dy) > 0)
            direction = Math.abs(dx) > Math.abs(dy) ? dx < 0 ? 2 : 3 : dy > 0 ? 0 : 1;
        stepping = fromX != targetX || fromY != targetY;
    }

    public float x(float progress) {
        return fromX + (targetX - fromX) * progress;
    }

    public float y(float progress) {
        return fromY + (targetY - fromY) * progress;
    }

    public int direction() {
        return direction;
    }

    public boolean moving(PlayerMotion owner) {
        return stepping && owner.moving();
    }
}
