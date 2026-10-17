package top.totodile.pokemmo.protocol;

/** 面前交互遵守卡带朝向与高度；脚下隐藏点由道具探测流程处理。 */
public final class GbaBackgroundEvents {
    private GbaBackgroundEvents() { }
    public static boolean isItemfinder(int item) { return item == 261 || item == 5471; }
    public static boolean matches(int kind, int elevation, int facing, int playerElevation, int hiddenItem) {
        if (elevation != 0 && elevation != playerElevation) return false;
        return switch (kind) {
            case 0 -> true;
            case 1 -> facing == 1;
            case 2 -> facing == 0;
            case 3 -> facing == 3;
            case 4 -> facing == 2;
            case 5, 6, 7 -> hiddenItem >= 0;
            default -> false;
        };
    }
}
