package top.totodile.pokemmo.client.world;

import java.util.random.RandomGenerator;

/** PlayerAvatarMovementController.Vl, Nds3DCameraProjection and its GBA movement definitions. */
final class WorldNpcMotion {
    interface StepCheck { boolean allowed(WorldNpcMotion actor, int x, int y); }
    private static final int[] DX = {0, 0, -1, 1}, DY = {1, -1, 0, 0};
    final WorldMapObjects.ObjectSprite definition;
    private final RandomGenerator random;
    int x, y, fromX, fromY, direction, sequence;
    private float remaining, stepTime, animation;
    private boolean moving;
    private boolean scripted;
    private boolean talking;

    void talking(boolean value, int toward) {
        talking = value;
        if (value) direction = toward;
    }

    void storyPosition(int tx,int ty){fromX=x=tx;fromY=y=ty;moving=false;scripted=true;}
    boolean storyStep(int tx,int ty){
        scripted=true;
        if(moving)return false;
        if(x==tx&&y==ty)return true;
        int dx=Integer.compare(tx,x),dy=dx==0?Integer.compare(ty,y):0;
        direction=dx<0?2:dx>0?3:dy<0?1:0;fromX=x;fromY=y;x+=dx;y+=dy;stepTime=0;moving=true;return false;
    }

    WorldNpcMotion(WorldMapObjects.ObjectSprite definition, RandomGenerator random) {
        this.definition = definition;
        this.random = random;
        x = fromX = definition.x(); y = fromY = definition.y();
        direction = definition.direction();
        remaining = delay();
    }

    boolean visible() { return definition.graphics() != 0 && definition.movement() != 76; }
    boolean occupies(int tx, int ty, int elevation) {
        return visible() && (definition.elevation() == 0 || elevation == 0
                || definition.elevation() == elevation)
                && (tx == x && ty == y || moving && tx == fromX && ty == fromY);
    }
    float drawX() { return moving ? fromX + (x - fromX) * Math.min(1, stepTime / .275f) : x; }
    float drawY() { return moving ? fromY + (y - fromY) * Math.min(1, stepTime / .275f) : y; }
    int phase() { return moving ? (int)(animation / .160f) % 4 : 0; }

    void update(float delta, StepCheck check) {
        if (!Float.isFinite(delta) || delta <= 0 || !visible()) return;
        float elapsed = Math.min(delta, .25f);
        if (moving) {
            float used = Math.min(elapsed, .275f - stepTime);
            stepTime += used; animation += used; elapsed -= used;
            remaining = Math.max(0, remaining - used);
            if (stepTime < .275f - .000001f) return;
            moving = false;
        }
        float waited = Math.min(elapsed, Math.max(0, remaining));
        if(scripted || talking)return;
        remaining -= waited; elapsed -= waited;
        if (remaining > .000001f) return;
        int type = definition.movement();
        // PlayerAvatarMovementController suppresses automatic movement for GBA objects 92..108.
        if (definition.graphics() >= 92 && definition.graphics() <= 108) return;
        int[] choices = directions(type);
        if (choices.length == 0) return;
        boolean walk = type >= 2 && type <= 6 || type >= 25 && type <= 52;
        int next;
        if (type == 1 || type >= 13 && type <= 22 || type >= 2 && type <= 6) {
            int option = random.nextInt(choices.length + (type >= 3 && type <= 6 ? 1 : 0));
            next = option == choices.length ? -1 : choices[option];
        } else {
            int length = 0;
            for (int d : choices) length += walk ? range(d) : 1;
            int at = sequence % length;
            next = choices[0];
            for (int d : choices) {
                int count = walk ? range(d) : 1;
                if (at < count) { next = d; break; }
                at -= count;
            }
        }
        remaining = Math.max(0, delay() - elapsed);
        if (next < 0) { sequence++; return; }
        direction = next;
        if (!walk) { sequence++; return; }
        int nx = x + DX[next], ny = y + DY[next];
        if (!withinRoute(nx, ny, type, choices) || !check.allowed(this, nx, ny)) return;
        fromX = x; fromY = y; x = nx; y = ny;
        stepTime = elapsed; animation += elapsed; moving = true; sequence++;
    }

    private int range(int direction) {
        return Math.max(1, direction < 2 ? definition.rangeY() : definition.rangeX());
    }
    private float delay() {
        return definition.movement() >= 25 && definition.movement() <= 52
                ? 0 : random.nextInt(1000, 1500) / 1000f;
    }
    private boolean withinRoute(int tx, int ty, int type, int[] choices) {
        int ox = definition.x(), oy = definition.y();
        if (type >= 25) {
            for (int d : choices) {
                for (int step = 0; step < range(d); step++) {
                    ox += DX[d]; oy += DY[d];
                    if (tx == ox && ty == oy) return true;
                }
            }
            return tx == definition.x() && ty == definition.y();
        }
        return Math.abs(tx - ox) <= (type == 3 || type == 4 ? 0 : Math.max(1, definition.rangeX()))
                && Math.abs(ty - oy) <= (type == 5 || type == 6 ? 0 : Math.max(1, definition.rangeY()));
    }

    private static int[] directions(int type) {
        return switch (type) {
            case 1, 2 -> new int[]{0, 2, 3, 1};
            case 3, 4 -> new int[]{1, 0};
            case 5, 6 -> new int[]{2, 3};
            case 13 -> new int[]{1, 0}; case 14 -> new int[]{2, 3};
            case 15 -> new int[]{1, 2}; case 16 -> new int[]{1, 3};
            case 17 -> new int[]{0, 2}; case 18 -> new int[]{0, 3};
            case 19 -> new int[]{1, 0, 2}; case 20 -> new int[]{1, 0, 3};
            case 21 -> new int[]{1, 2, 3}; case 22 -> new int[]{0, 2, 3};
            case 23 -> new int[]{0, 3, 1, 2}; case 24 -> new int[]{0, 2, 1, 3};
            case 25 -> new int[]{1, 0}; case 26 -> new int[]{0, 1};
            case 27 -> new int[]{2, 3}; case 28 -> new int[]{3, 2};
            case 29 -> new int[]{1, 3, 2, 0};
            case 32, 37 -> new int[]{2, 1, 0, 3};
            case 33 -> new int[]{1, 2, 3, 0};
            case 36 -> new int[]{3, 0, 1, 2};
            case 40 -> new int[]{0, 3, 2, 1}; case 41 -> new int[]{3, 1, 0, 2};
            case 44 -> new int[]{0, 2, 3, 1};
            case 45 -> new int[]{1, 2, 0, 3}; case 46 -> new int[]{0, 3, 1, 2};
            case 47 -> new int[]{2, 0, 3, 1}; case 48 -> new int[]{3, 1, 2, 0};
            case 49 -> new int[]{1, 3, 0, 2}; case 50 -> new int[]{0, 2, 1, 3};
            case 51 -> new int[]{2, 1, 3, 0}; case 52 -> new int[]{3, 0, 2, 1};
            default -> new int[0];
        };
    }
}
