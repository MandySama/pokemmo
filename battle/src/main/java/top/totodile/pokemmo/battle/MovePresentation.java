package top.totodile.pokemmo.battle;

/** 招式展示的材质颜色与宿主坐标数据，不修改宝可梦数值。 */
public final class MovePresentation {
    public static final int TACKLE = 33;

    public record Actor(float x, float y, float scaleX, float scaleY, float tint,
                        float red, float green, float blue, float overlay, float z, float opacity) {
        public Actor(float x, float y, float scaleX, float scaleY, float tint) {
            this(x, y, scaleX, scaleY, tint, 0, 0, 0, 0, 0, 1);
        }
        public Actor(float x, float y, float scaleX, float scaleY, float tint,
                     float red, float green, float blue, float overlay) {
            this(x, y, scaleX, scaleY, tint, red, green, blue, overlay, 0, 1);
        }
    }
    public record Tint(float red, float green, float blue, float alpha) { }

    private MovePresentation() { }

    public static Tint backdropTint(int move, long milliseconds) {
        Tint tint = new Tint(0, 0, 0, 0);
        long latest = -1;
        for (var command : MoveAnimations.playback(move).commands()) {
            var a = command.arguments();
            long elapsed = milliseconds - command.start();
            if (elapsed >= 0 && command.start() >= latest && command.operation().equals("Ue0")
                    && (a.getFirst() == 0 || a.getFirst() == 1 || a.getFirst() == 2 || a.getFirst() == 4)) {
                float progress = ease(elapsed / Math.max(1, a.get(4) * 10000));
                int rgb = a.get(1).intValue();
                tint = new Tint((rgb & 31) / 31f, (rgb >> 5 & 31) / 31f, (rgb >> 10 & 31) / 31f,
                        a.get(2) + (a.get(3) - a.get(2)) * progress);
                latest = command.start();
            }
        }
        return tint;
    }

    private static float ease(float value) {
        float t = Math.clamp(value, 0, 1);
        return t < .5f ? 2 * t * t : -2 * t * t + 4 * t - 1;
    }
}
