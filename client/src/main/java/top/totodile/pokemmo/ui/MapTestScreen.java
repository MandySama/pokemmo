package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.theme.ThemeManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.rom.FireRedAtlas;
import top.totodile.pokemmo.rom.FireRedAtlas.Key;
import top.totodile.pokemmo.rom.GbaMapPreview;
import top.totodile.pokemmo.world.MapTestWorld;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

/** 独立地图浏览界面，镜头可拖拽，地图纹理按可见范围加载并限量缓存。 */
public final class MapTestScreen extends Widget implements AutoCloseable {
    private static final int BAR_HEIGHT = 44;
    private final FireRedAtlas atlas;
    private final MapTestWorld world;
    private final List<FireRedAtlas.Area> areas;
    private final LinkedHashMap<Key, GbaMapPreview> cache = new LinkedHashMap<>(16, 0.75f, true);
    private record NpcTexture(FireRedAtlas.Npc npc, int texture, int width, int height, float scale) {
    }
    private final Map<Key, List<NpcTexture>> npcTextures = new LinkedHashMap<>();
    private final GlfwTwlRenderer renderer = new GlfwTwlRenderer();
    private final GUI gui;
    private final ThemeManager theme;
    private final ComboBox<String> maps;
    private final Button home = new Button(ClientStrings.get(9101));
    private final Button back = new Button(ClientStrings.get(9102));
    private final ToggleButton enhanced = new ToggleButton(ClientStrings.get(9103));
    private final ToggleButton markers = new ToggleButton(ClientStrings.get(9104));
    private final Label status = new Label();
    private final long started = System.nanoTime();
    private final int season = Integer.getInteger("pokemmo.preview.season", 1);
    private final int hour = Integer.getInteger("pokemmo.preview.hour", 12);
    private final int minute = Integer.getInteger("pokemmo.preview.minute", 0);
    private int viewportWidth = 1024, viewportHeight = 768;
    private int scale = 2;
    private double mouseX, mouseY, pressX, pressY;
    private boolean pressed, dragged, updatingSelection;
    private Color warpColor = Color.WHITE;

    public MapTestScreen(Path rom) throws IOException {
        atlas = top.totodile.pokemmo.MapSources.gba(rom, false);
        world = new MapTestWorld(atlas);
        areas = atlas.areas();
        setTheme("map-test");
        String[] entries = areas.stream().map(area -> ClientStrings.get(9106)
                .replace("{00}", area.key().toString()).replace("{01}", Integer.toString(area.columns()))
                .replace("{02}", Integer.toString(area.rows()))
                + (area.key().equals(new Key(3, 0)) ? " · " + ClientStrings.get(9107) : "")).toArray(String[]::new);
        maps = new ComboBox<>(new SimpleChangableListModel<>(entries));
        maps.addCallback(() -> {
            if (!updatingSelection && maps.getSelected() >= 0) navigate(() -> world.browse(areas.get(maps.getSelected()).key()));
        });
        home.addCallback(() -> navigate(world::home));
        back.addCallback(() -> navigate(world::back));
        enhanced.setActive(Boolean.parseBoolean(System.getProperty("pokemmo.preview.enhanced", "true")));
        enhanced.addCallback(() -> {
            clearCache();
            updateStatus();
        });
        markers.setActive(true);
        add(maps);
        add(home);
        add(back);
        add(enhanced);
        add(markers);
        add(status);
        gui = new GUI(this, renderer, null);
        theme = ThemeManager.createThemeManager(ClientFiles.data("themes/default/theme.xml").toUri().toURL(), renderer);
        try {
            gui.applyTheme(theme);
            // 首张地图在展示窗口前验证，避免打开后才发现不可绘制的资源。
            preview(world.current());
            updateSelection();
            updateStatus();
        } catch (IOException | RuntimeException exception) {
            close();
            throw exception;
        }
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        warpColor = themeInfo.getParameter("warpColor", Color.WHITE);
    }

    private GbaMapPreview preview(Key key) throws IOException {
        GbaMapPreview result = cache.get(key);
        if (result != null) return result;
        result = new GbaMapPreview(atlas.load(key, enhanced.isActive()));
        cache.put(key, result);
        try {
            npcTextures.put(key, loadNpcs(key));
        } catch (IOException | RuntimeException exception) {
            cache.remove(key).close();
            throw exception;
        }
        while (cache.size() > 16) {
            var iterator = cache.entrySet().iterator();
            var oldest = iterator.next();
            oldest.getValue().close();
            releaseNpcs(oldest.getKey());
            iterator.remove();
        }
        return result;
    }

    public void render(int width, int height, int framebufferWidth, int framebufferHeight) throws IOException {
        viewportWidth = width;
        viewportHeight = height;
        renderer.resize(width, height, framebufferWidth, framebufferHeight);
        long elapsed = (System.nanoTime() - started) / 1_000_000;
        for (var entry : world.positions().entrySet()) {
            var area = atlas.area(entry.getKey());
            int left = screenX(entry.getValue().x() * 16.0);
            int top = screenY(entry.getValue().y() * 16.0);
            if (left >= width || top >= height || left + area.columns() * 16 * scale <= 0
                    || top + area.rows() * 16 * scale <= BAR_HEIGHT) continue;
            preview(entry.getKey()).renderWithActors(width, height, season, hour, minute, elapsed, left, top, scale,
                    layer -> drawNpcs(entry.getKey(), left, top, layer));
            if (markers.isActive()) drawWarps(area, left, top);
        }
        gui.update();
    }

    private void drawWarps(FireRedAtlas.Area area, int left, int top) {
        glDisable(GL_TEXTURE_2D);
        glColor4f(warpColor.getRedFloat(), warpColor.getGreenFloat(), warpColor.getBlueFloat(), warpColor.getAlphaFloat());
        for (var warp : area.warps()) {
            int x = left + warp.x() * 16 * scale, y = top + warp.y() * 16 * scale;
            glBegin(GL_LINE_LOOP);
            glVertex2i(x + 1, y + 1);
            glVertex2i(x + 16 * scale - 1, y + 1);
            glVertex2i(x + 16 * scale - 1, y + 16 * scale - 1);
            glVertex2i(x + 1, y + 16 * scale - 1);
            glEnd();
        }
        glColor4f(1, 1, 1, 1);
    }

    private int screenX(double x) {
        return (int) Math.round(viewportWidth / 2.0 + (x - world.centerX()) * scale);
    }

    private int screenY(double y) {
        return (int) Math.round((viewportHeight + BAR_HEIGHT) / 2.0 + (y - world.centerY()) * scale);
    }

    public void attachInput(long window) {
        GLFW.glfwSetCursorPosCallback(window, (w, x, y) -> mouseMove(x, y));
        GLFW.glfwSetMouseButtonCallback(window, (w, button, action, mods) -> mouseButton(button, action));
        GLFW.glfwSetScrollCallback(window, (w, x, y) -> {
            if (maps.hasOpenPopups() || mouseY < BAR_HEIGHT) {
                gui.handleMouseWheel((int) -Math.signum(y));
            } else {
                int next = Math.max(1, Math.min(4, scale + (int) Math.signum(y)));
                double dx = mouseX - viewportWidth / 2.0, dy = mouseY - (viewportHeight + BAR_HEIGHT) / 2.0;
                world.pan(dx / scale - dx / next, dy / scale - dy / next);
                scale = next;
            }
        });
        GLFW.glfwSetKeyCallback(window, (w, key, scan, action, mods) -> {
            if (action == GLFW.GLFW_PRESS && !maps.hasOpenPopups()) {
                if (key == GLFW.GLFW_KEY_HOME) navigate(world::home);
                if (key == GLFW.GLFW_KEY_BACKSPACE) navigate(world::back);
            }
            int translated = GlfwKeyMap.translate(key);
            if (translated != Event.KEY_NONE) gui.handleKey(translated, Event.CHAR_NONE, action != GLFW.GLFW_RELEASE);
        });
        GLFW.glfwSetCharCallback(window, (w, codepoint) -> {
            if (codepoint <= Character.MAX_VALUE) gui.handleKey(Event.KEY_NONE, (char) codepoint, true);
        });
        GLFW.glfwSetWindowFocusCallback(window, (w, focused) -> {
            if (!focused) {
                pressed = false;
                gui.clearKeyboardState();
                gui.clearMouseState();
            }
        });
    }

    public void mouseMove(double x, double y) {
        if (pressed) {
            if (Math.hypot(x - pressX, y - pressY) > 4) dragged = true;
            if (dragged) world.pan((mouseX - x) / scale, (mouseY - y) / scale);
        }
        mouseX = x;
        mouseY = y;
        gui.handleMouse((int) x, (int) y, -1, false);
    }

    public void mouseButton(int button, int action) {
        if (pressed && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_RELEASE) {
            pressed = false;
            if (!dragged && mouseY >= BAR_HEIGHT) {
                double x = world.centerX() + (mouseX - viewportWidth / 2.0) / scale;
                double y = world.centerY() + (mouseY - (viewportHeight + BAR_HEIGHT) / 2.0) / scale;
                navigate(() -> world.click(x, y));
            }
            return;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || mouseY < BAR_HEIGHT || maps.hasOpenPopups()) {
            gui.handleMouse((int) mouseX, (int) mouseY, button, action == GLFW.GLFW_PRESS);
            return;
        }
        if (action == GLFW.GLFW_PRESS) {
            pressed = true;
            dragged = false;
            pressX = mouseX;
            pressY = mouseY;
        }
    }

    private interface Navigation {
        void run() throws IOException;
    }

    private void navigate(Navigation action) {
        try {
            action.run();
            updateSelection();
            updateStatus();
        } catch (IOException exception) {
            status.setText(ClientStrings.get(9108).replace("{00}", exception.getMessage()));
        }
    }

    private void updateSelection() {
        updatingSelection = true;
        for (int i = 0; i < areas.size(); i++) {
            if (areas.get(i).key().equals(world.current())) maps.setSelected(i);
        }
        updatingSelection = false;
    }

    private void updateStatus() {
        status.setText(ClientStrings.get(9105).replace("{00}", world.current().toString())
                .replace("{01}", ClientStrings.get(enhanced.isActive() ? 9109 : 9110)));
    }

    @Override
    protected void layout() {
        maps.setPosition(8, 8);
        maps.setSize(230, 28);
        Widget[] buttons = {home, back, enhanced, markers};
        int x = 246;
        for (Widget button : buttons) {
            button.setPosition(x, 8);
            button.setSize(100, 28);
            x += 108;
        }
        status.setPosition(8, getInnerBottom() - 27);
        status.setSize(getInnerWidth() - 16, 25);
    }

    private void clearCache() {
        for (GbaMapPreview preview : cache.values()) preview.close();
        cache.clear();
        for (Key key : List.copyOf(npcTextures.keySet())) releaseNpcs(key);
    }

    private List<NpcTexture> loadNpcs(Key key) throws IOException {
        List<NpcTexture> result = new ArrayList<>();
        try {
            for (var npc : atlas.npcs(key)) {
                if (npc.flag() != 0 || !npc.hasStaticGraphics()) continue;
                var sprite = atlas.npcSprite(npc, enhanced.isActive(), season);
                var image = sprite.image();
                ByteBuffer pixels = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
                for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
                    int color = image.getRGB(x, y);
                    pixels.put((byte) (color >>> 16)).put((byte) (color >>> 8)).put((byte) color).put((byte) (color >>> 24));
                }
                pixels.flip();
                int texture = glGenTextures();
                result.add(new NpcTexture(npc, texture, image.getWidth(), image.getHeight(), sprite.scale()));
                glBindTexture(GL_TEXTURE_2D, texture);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            }
            result.sort(java.util.Comparator.comparingInt(sprite -> sprite.npc().y()));
            return result;
        } catch (IOException | RuntimeException exception) {
            for (var sprite : result) glDeleteTextures(sprite.texture());
            throw exception;
        } finally {
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    private void drawNpcs(Key key, int left, int top, int layer) {
        glColor4f(1, 1, 1, 1);
        for (var sprite : npcTextures.getOrDefault(key, List.of())) {
            int actorLayer = enhanced.isActive() && sprite.npc().elevation() < 3 ? 1 : 2;
            if (actorLayer != layer) continue;
            float width = sprite.width() * sprite.scale(), height = sprite.height() * sprite.scale();
            float x = left + (sprite.npc().x() * 16 + (sprite.scale() == 0.75f ? 0.5f : 0) - (width - 16) / 2) * scale;
            float y = top + (sprite.npc().y() * 16 - (height - 16)) * scale;
            float u = sprite.npc().direction() == 3 ? 1 : 0;
            glBindTexture(GL_TEXTURE_2D, sprite.texture());
            glBegin(GL_QUADS);
            glTexCoord2f(u, 0); glVertex2f(x, y);
            glTexCoord2f(1 - u, 0); glVertex2f(x + width * scale, y);
            glTexCoord2f(1 - u, 1); glVertex2f(x + width * scale, y + height * scale);
            glTexCoord2f(u, 1); glVertex2f(x, y + height * scale);
            glEnd();
        }
    }

    private void releaseNpcs(Key key) {
        var sprites = npcTextures.remove(key);
        if (sprites != null) for (var sprite : sprites) glDeleteTextures(sprite.texture());
    }

    @Override
    public void close() {
        clearCache();
        theme.destroy();
    }
}
