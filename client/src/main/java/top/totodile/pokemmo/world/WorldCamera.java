package top.totodile.pokemmo.world;

/** 桌面大世界的自动倍率和像素对齐正交镜头，不按地图大小缩放或夹紧。 */
public final class WorldCamera {
    private WorldCamera() { }

    public static int scale(int viewportWidth) {
        int width = viewportWidth - 10;
        if (width <= 1920) return 4;
        if (width < 2560) return 5;
        return width < 3840 ? 6 : 8;
    }

    public static int origin(int viewportSize, int cameraPixel, int scale) {
        // 奇数视口补半个屏幕像素，避免纹理边缘落在半像素上。
        return viewportSize / 2 - cameraPixel * scale;
    }
}
