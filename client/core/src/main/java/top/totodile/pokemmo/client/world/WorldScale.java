package top.totodile.pokemmo.client.world;

/** 参考 GdxParticleSystemRenderer.Bc：按窗口宽度和平台选择 GBA 整数缩放。 */
final class WorldScale {
    static int zoom(int configured, int width, boolean mobile) {
        if (configured > (mobile ? 2 : 3)) return configured;
        int adjustedWidth = width + (mobile ? 10 : -10);
        if (mobile && adjustedWidth < 1152) return 3;
        if (mobile ? adjustedWidth < 1920 : adjustedWidth <= 1920) return 4;
        if (adjustedWidth < 2560) return 5;
        return adjustedWidth < 3840 ? 6 : 8;
    }
}
