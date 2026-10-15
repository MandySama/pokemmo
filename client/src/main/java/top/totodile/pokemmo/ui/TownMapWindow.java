package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.protocol.CharacterPackets.Position;
import top.totodile.pokemmo.rom.FireRedAtlas;
import top.totodile.pokemmo.rom.FireRedAtlas.Area;
import top.totodile.pokemmo.rom.FireRedAtlas.Key;
import top.totodile.pokemmo.rom.GbaRegionMap;
import top.totodile.pokemmo.rom.NdsRegionMap;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 城镇地图窗口。卡带自带一张手绘区域图（关都火红为四张大区图，丰缘绿宝石为一整张），
 * 窗口按整数倍居中绘制它，并把玩家光标放在卡带地点网格给出的落点上；城镇圆点本来就画
 * 在图上，不需要额外标注。
 * 卡带没有区域图时退回按玩法地图连接拼接渲染，此时保留原窗口行为。
 * 纹理只在 GUI 线程创建和释放；关闭时释放纹理但保留像素缓存。
 */
final class TownMapWindow extends ResizableFrame implements AutoCloseable {
    /** 拼接回退时用于标出城镇的地图头地区类型。 */
    private static final int TOWN_TYPE = 1;

    private final Label location = new Label();
    private FireRedAtlas atlas;
    private Key current;
    private int playerX, playerY;
    private String locationText = "";

    /** 卡带区域图模式：当前大区、区域图与玩家落点。 */
    private GbaRegionMap regionMap;
    private final java.util.Map<String, NdsRegionMap> ndsMaps = new java.util.HashMap<>();
    private int regionIndex = -1;
    private BufferedImage regionPixels;
    private Texture regionTexture;
    private Image regionImage;
    private int[] regionSpot;
    /** 拼接回退模式的城镇标记。 */
    private final List<int[]> towns = new ArrayList<>();
    private CompletableFuture<BufferedImage> pending;
    private TownMapRegion.Layout layout;
    private BufferedImage composed;
    private int composedDivisor = -1;
    private int failedDivisor = -1;
    private Texture texture;
    private Image map;
    private boolean composing;

    private Texture markerTexture, playerTexture;
    private Image townMarker, playerMarker;
    private int[] playerMarkerAt;

    private int contentLeft = 14, contentTop = 50, contentRight = 14, contentBottom = 14;
    private int markerSize = 9, playerSize = 15, labelInsetX = 8, labelInsetY = 8;
    private Color markerColor = Color.WHITE, markerBorder = Color.WHITE;
    private Color playerColor = Color.WHITE, playerBorder = Color.WHITE;
    private Color playerDot = Color.WHITE;
    private int mapX, mapY, mapWidth, mapHeight;
    private int mapScale = 1;

    TownMapWindow(Runnable close) {
        setTheme("/town-map");
        setTitle(ClientStrings.get(245442));
        setResizableAxis(ResizableAxis.NONE);
        addCloseCallback(close);
        add(location);
        setHardVisible(false);
    }

    @Override
    protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        contentLeft = theme.getParameter("contentLeft", contentLeft);
        contentTop = theme.getParameter("contentTop", contentTop);
        contentRight = theme.getParameter("contentRight", contentRight);
        contentBottom = theme.getParameter("contentBottom", contentBottom);
        markerSize = theme.getParameter("markerSize", markerSize);
        playerSize = theme.getParameter("playerSize", playerSize);
        labelInsetX = theme.getParameter("labelInsetX", labelInsetX);
        labelInsetY = theme.getParameter("labelInsetY", labelInsetY);
        markerColor = theme.getParameter("markerColor", markerColor);
        markerBorder = theme.getParameter("markerBorderColor", markerBorder);
        playerColor = theme.getParameter("playerColor", playerColor);
        playerBorder = theme.getParameter("playerBorderColor", playerBorder);
        playerDot = theme.getParameter("playerDotColor", playerDot);
    }

    /** 打开地图：卡带区域图可用时立即定位，否则走拼接回退路径。 */
    void open(FireRedAtlas atlas, Position position, String name) {
        this.atlas = atlas;
        if (atlas == null) {
            close();
            return;
        }
        GbaRegionMap nextRegionMap = atlas.regionMap();
        if (regionMap != nextRegionMap) {
            close();
            regionIndex = -1;
            regionPixels = null;
        }
        regionMap = nextRegionMap;
        if (regionMap != null) {
            updateState(position, name);
            setHardVisible(true);
            invalidateLayout();
            return;
        }
        try {
            TownMapRegion.Layout next = TownMapRegion.layout(atlas, new Key(position.group(), position.map()));
            if (layout == null || !layout.tiles().equals(next.tiles())) {
                composed = null;
                composedDivisor = -1;
                failedDivisor = -1;
                towns.clear();
            }
            layout = next;
        } catch (IOException exception) {
            report(exception);
            close();
            return;
        }
        updateState(position, name);
        setHardVisible(true);
        invalidateLayout();
    }

    void open(java.nio.file.Path rom, int region, Position position, String name) throws IOException {
        if (rom == null) throw new IOException("Missing NDS region ROM");
        close();
        atlas = null;
        String key = region + ":" + rom.toAbsolutePath().normalize();
        NdsRegionMap decoded = ndsMaps.get(key);
        if (decoded == null) {
            decoded = NdsRegionMap.load(rom, region);
            ndsMaps.put(key, decoded);
        }
        regionMap = decoded;
        regionIndex = -1;
        regionPixels = null;
        updateState(position, name);
        setHardVisible(true);
        invalidateLayout();
    }

    /** 跟随玩家更新地名与位置标记，不重新合成整幅区域图。 */
    void updateState(Position position, String name) {
        current = new Key(position.group(), position.map());
        playerX = position.x();
        playerY = position.y();
        locationText = name;
        if (regionMap != null) {
            regionSpot = regionMap instanceof NdsRegionMap nds
                    ? nds.locate(position.group()*256+position.map(), playerX, playerY) : regionSpot(current);
            int nextRegion = regionSpot != null ? regionSpot[0] : regionIndex >= 0 ? regionIndex : 0;
            if (nextRegion != regionIndex || regionPixels == null) loadRegion(nextRegion);
        }
        boolean waiting = regionMap == null && (composing || map == null);
        String text = waiting ? ClientStrings.get(55) : locationText;
        if (!text.equals(location.getText())) {
            location.setText(text);
            location.adjustSize();
        }
        invalidateLayout();
    }

    /** 载入某张大区图；解码很快且落点变化很少，直接在调用线程完成。 */
    private void loadRegion(int region) {
        try {
            regionPixels = regionMap.image(region);
            regionIndex = region;
            if (regionTexture != null) regionTexture.destroy();
            regionTexture = null;
            regionImage = null;
        } catch (IOException exception) {
            report(exception);
        }
    }

    /**
     * 求玩家在区域图上的落点：先按当前地图的地区编号查卡带地点网格，
     * 查不到（室内、特殊区域）就沿传送事件向外逐层找到最近有编号的地图，
     * 动态出口（127/255）不作为路径。
     */
    private int[] regionSpot(Key start) {
        if (regionMap == null || atlas == null) return null;
        Set<Key> visited = new HashSet<>();
        ArrayDeque<Key> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        for (int hop = 0; hop < 16 && !queue.isEmpty(); hop++) {
            for (int level = queue.size(); level > 0; level--) {
                Key source = queue.removeFirst();
                Area area = area(source);
                if (area == null) continue;
                try {
                    int[] spot = regionMap.position(atlas.regionalSection(source));
                    if (spot != null) return spot;
                } catch (IOException exception) {
                    return null;
                }
                for (var warp : area.warps()) {
                    Key target = warp.target();
                    if (target.group() >= 127 || target.map() >= 127) continue;
                    if (visited.add(target)) queue.add(target);
                }
            }
        }
        return null;
    }

    /** 后台合成只在拼接回退模式下使用，并按内容区选择降采样步长。 */
    private void startCompose() {
        if (regionMap != null || layout == null || contentWidth() < 16 || contentHeight() < 16) return;
        int divisor = divisor();
        if (failedDivisor == divisor) return;
        if (composed != null || pending != null) {
            if (composedDivisor == divisor) return;
        }
        cancelPending();
        composedDivisor = divisor;
        composing = true;
        FireRedAtlas source = atlas;
        TownMapRegion.Layout target = layout;
        pending = CompletableFuture.supplyAsync(() -> {
            try {
                return TownMapRegion.compose(source, target, divisor);
            } catch (IOException exception) {
                report(exception);
                return null;
            }
        });
        rebuildTowns();
    }

    /** 降采样步长取内容区能容纳整幅区域图的最小整数。 */
    private int divisor() {
        int width = layout.maxX() * 16;
        int height = layout.maxY() * 16;
        return Math.max(1, Math.max(ceilDiv(width, contentWidth()), ceilDiv(height, contentHeight())));
    }

    private static int ceilDiv(int value, int divisor) {
        return (value + divisor - 1) / divisor;
    }

    /** 拼接回退模式下的城镇标记取地块中心，位置随缩放变化。 */
    private void rebuildTowns() {
        towns.clear();
        if (layout == null || composedDivisor < 1) return;
        for (var entry : layout.tiles().entrySet()) {
            Area area = area(entry.getKey());
            if (area == null || area.type() != TOWN_TYPE) continue;
            towns.add(new int[]{
                    (entry.getValue().x() * 16 + area.columns() * 8) / composedDivisor,
                    (entry.getValue().y() * 16 + area.rows() * 8) / composedDivisor});
        }
    }

    /** 拼接回退模式下当前地图不在区域图内时，改用通往它的出口定位。 */
    private int[] locate() {
        if (layout == null || composedDivisor < 1 || current == null) return null;
        Key key = current;
        int x = playerX, y = playerY;
        if (!layout.contains(key)) {
            int[] exit = exitTo(key);
            if (exit == null) return null;
            key = new Key(exit[0], exit[1]);
            x = exit[2];
            y = exit[3];
        }
        var tile = layout.tiles().get(key);
        if (tile == null) return null;
        return new int[]{
                (tile.x() * 16 + x * 16 + 8) / composedDivisor,
                (tile.y() * 16 + y * 16 + 8) / composedDivisor};
    }

    private int[] exitTo(Key start) {
        Set<Key> visited = new HashSet<>();
        ArrayDeque<Key> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        for (int hop = 0; hop < 16 && !queue.isEmpty(); hop++) {
            for (int level = queue.size(); level > 0; level--) {
                Key source = queue.removeFirst();
                Area area = area(source);
                if (area == null) continue;
                for (var warp : area.warps()) {
                    Key target = warp.target();
                    if (target.group() >= 127 || target.map() >= 127) continue;
                    if (layout.contains(target)) {
                        Area outdoor = area(target);
                        if (outdoor != null) for (var back : outdoor.warps())
                            if (back.target().equals(source))
                                return new int[]{target.group(), target.map(), back.x(), back.y()};
                        return new int[]{target.group(), target.map(), 0, 0};
                    }
                    if (visited.add(target)) queue.add(target);
                }
            }
        }
        return null;
    }

    private void cancelPending() {
        if (pending != null) pending.cancel(true);
        pending = null;
    }

    @Override
    protected void layout() {
        super.layout();
        startCompose();
        if (regionMap != null) {
            // 区域图按整数倍放大，像素保持锐利；尺寸不足时退回原尺寸。
            BufferedImage pixels = regionPixels;
            if (pixels == null) {
                mapWidth = mapHeight = mapScale = 0;
            } else {
                mapScale = Math.max(1, Math.min(contentWidth() / pixels.getWidth(),
                        contentHeight() / pixels.getHeight()));
                mapWidth = pixels.getWidth() * mapScale;
                mapHeight = pixels.getHeight() * mapScale;
            }
        } else if (layout != null && composedDivisor > 0) {
            mapScale = 1;
            mapWidth = layout.maxX() * 16 / composedDivisor;
            mapHeight = layout.maxY() * 16 / composedDivisor;
        } else {
            mapWidth = mapHeight = mapScale = 0;
        }
        mapX = getX() + contentLeft + Math.max(0, (contentWidth() - mapWidth) / 2);
        mapY = getY() + contentTop + Math.max(0, (contentHeight() - mapHeight) / 2);
        location.setPosition(mapX + labelInsetX, mapY + labelInsetY);
        // 框架会把子控件拉到整个内容区，地名标签按自身文字收缩到地图左上角。
        location.adjustSize();
        if (regionMap != null) {
            playerMarkerAt = regionSpot == null ? null
                    : new int[]{regionMap.pixelX(regionSpot[1]) * mapScale,
                    regionMap.pixelY(regionSpot[2]) * mapScale};
        } else {
            playerMarkerAt = locate();
        }
    }

    private int contentWidth() {
        return getWidth() - contentLeft - contentRight;
    }

    private int contentHeight() {
        return getHeight() - contentTop - contentBottom;
    }

    @Override
    protected void paintWidget(GUI gui) {
        super.paintWidget(gui);
        upload(gui);
        if (regionMap != null) {
            // 城镇圆点画在卡带区域图上，这里只叠玩家光标。
            if (regionImage != null) regionImage.draw(getAnimationState(), mapX, mapY, mapWidth, mapHeight);
        } else {
            if (map == null) return;
            map.draw(getAnimationState(), mapX, mapY);
            if (townMarker != null) for (int[] town : towns)
                townMarker.draw(getAnimationState(), mapX + town[0] - townMarker.getWidth() / 2,
                        mapY + town[1] - townMarker.getHeight() / 2);
        }
        if (playerMarker != null && playerMarkerAt != null)
            playerMarker.draw(getAnimationState(), mapX + playerMarkerAt[0] - playerMarker.getWidth() / 2,
                    mapY + playerMarkerAt[1] - playerMarker.getHeight() / 2);
    }

    /** 后台像素结果与标记贴图都在绘制线程创建纹理。 */
    private void upload(GUI gui) {
        var renderer = (GlfwTwlRenderer) gui.getRenderer();
        if (pending != null && pending.isDone()) {
            BufferedImage image = pending.getNow(null);
            boolean cancelled = pending.isCancelled();
            pending = null;
            composing = false;
            if (image != null) composed = image;
            else if (!cancelled) failedDivisor = composedDivisor;
        }
        if (regionMap != null) {
            if (regionPixels != null && regionTexture == null) {
                regionTexture = renderer.uploadImage(regionPixels);
                regionImage = regionTexture.getImage(0, 0, regionPixels.getWidth(), regionPixels.getHeight(),
                        Color.WHITE, false, Texture.Rotation.NONE);
            }
        } else if (composed != null && texture == null) {
            texture = renderer.uploadImage(composed);
            map = texture.getImage(0, 0, composed.getWidth(), composed.getHeight(), Color.WHITE, false,
                    Texture.Rotation.NONE);
        }
        // 拼接回退模式下城镇用圆点标注，与玩家光标区分开。
        if (markerTexture == null) {
            markerTexture = renderer.uploadImage(dot(markerSize, markerColor, markerBorder));
            townMarker = markerTexture.getImage(0, 0, markerSize, markerSize, Color.WHITE, false,
                    Texture.Rotation.NONE);
        }
        if (playerTexture == null) {
            playerTexture = renderer.uploadImage(pin(playerSize, playerColor, playerBorder, playerDot));
            playerMarker = playerTexture.getImage(0, 0, playerSize, playerSize, Color.WHITE, false,
                    Texture.Rotation.NONE);
        }
    }

    /** 圆点先画外圈再画内圈，尺寸来自主题。 */
    private static BufferedImage dot(int size, Color fill, Color border) {
        int diameter = Math.max(3, size);
        BufferedImage image = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(awt(border));
        graphics.fillOval(0, 0, diameter, diameter);
        int inset = Math.max(1, diameter / 5);
        graphics.setColor(awt(fill));
        graphics.fillOval(inset, inset, diameter - inset * 2, diameter - inset * 2);
        graphics.dispose();
        return image;
    }

    /** 玩家光标：深色外圈 + 亮色内圈 + 中心圆点，绿地和海面上都能看清。 */
    private static BufferedImage pin(int size, Color ring, Color fill, Color dot) {
        int diameter = Math.max(7, size);
        BufferedImage image = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(awt(ring));
        graphics.fillOval(0, 0, diameter, diameter);
        int inset = Math.max(1, diameter / 6);
        graphics.setColor(awt(fill));
        graphics.fillOval(inset, inset, diameter - inset * 2, diameter - inset * 2);
        int core = Math.max(2, diameter / 3);
        graphics.setColor(awt(dot));
        graphics.fillOval((diameter - core) / 2, (diameter - core) / 2, core, core);
        graphics.dispose();
        return image;
    }

    private static java.awt.Color awt(Color color) {
        return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private Area area(Key key) {
        try {
            return atlas.area(key);
        } catch (IOException exception) {
            return null;
        }
    }

    private void report(IOException exception) {
        System.getLogger(TownMapWindow.class.getName()).log(System.Logger.Level.WARNING,
                "Unable to build town map: {0}", exception.getClass().getSimpleName());
    }

    /** 关闭窗口并释放纹理；像素缓存保留，再次打开无需重新解码。 */
    @Override
    public void close() {
        cancelPending();
        composing = false;
        setHardVisible(false);
        if (texture != null) texture.destroy();
        texture = null;
        map = null;
        if (regionTexture != null) regionTexture.destroy();
        regionTexture = null;
        regionImage = null;
        regionSpot = null;
        playerMarkerAt = null;
        location.setText("");
    }
}
