package top.totodile.pokemmo.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** 显示滚动背景和游戏 Logo 的客户端窗口。 */
public final class ClientApplication extends ApplicationAdapter {
    private static final int LOGO_WIDTH = 484;
    private static final int LOGO_HEIGHT = 143;
    private static final int LOGO_CENTER_OFFSET_Y = 200;
    private final Viewport viewport;
    private final Viewport uiViewport;
    private SpriteBatch batch;
    private Texture logo;
    private ScrollingBackground background;
    private LoginPanel loginPanel;
    private final Function<String, FileHandle> assets;
    private final LongSupplier clock;
    private final boolean mobile;
    private final Supplier<FileHandle> configDirectory;
    private AssetManager sceneAssets;

    public ClientApplication(Function<String, FileHandle> assets, boolean mobile, LongSupplier clock) {
        this(assets, mobile, clock, () -> Gdx.files.local("config"));
    }

    public ClientApplication(Function<String, FileHandle> assets, boolean mobile, LongSupplier clock,
                             Supplier<FileHandle> configDirectory) {
        this.assets = assets;
        this.configDirectory = configDirectory;
        this.clock = clock;
        this.mobile = mobile;
        // 移动端沿用参考客户端的逻辑分辨率，额外屏幕空间扩展视口。
        this.viewport = mobile ? new ExtendViewport(1280, 720) : new ScreenViewport();
        this.uiViewport = mobile ? new ScreenViewport() : viewport;
    }

    @Override
    public void create() {
        sceneAssets = new AssetManager(assets::apply);
        for (int i = 0; i < 3; i++) {
            sceneAssets.load("sprites/textures/bg_0" + i + ".png", Texture.class);
        }
    }

    private void loadLogo() {
        batch = new SpriteBatch();
        logo = new Texture(assets.apply("themes/default/res/bg.png"));
        Texture.TextureFilter filter = mobile ? Texture.TextureFilter.Nearest : Texture.TextureFilter.Linear;
        logo.setFilter(filter, filter);
    }

    private void loadScene() {
        Texture[] tiles = new Texture[3];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = sceneAssets.get("sprites/textures/bg_0" + i + ".png", Texture.class);
        }
        background = new ScrollingBackground(tiles, clock);
        loginPanel = new LoginPanel(uiViewport, assets, mobile, SavedAccounts.open(configDirectory.get()));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        updateUiViewport();
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        // 以资源实际加载状态决定画面，不规定 Logo 或背景出现在哪一帧。
        // AssetManager 在后台解码图片，在渲染线程上传纹理。
        if (background == null && sceneAssets.update()) {
            if (batch == null) loadLogo();
            loadScene();
        }
        // 两端加载期间均保持黑色，资源和登录界面准备完成后一起显示。
        if (background == null) return;
        viewport.apply();
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();
        // 与参考客户端登录布局一致：Logo 固定为 484×143，居中后向上偏移 200 个逻辑单位。
        // 将左上角为原点的界面坐标转换为 SpriteBatch 使用的左下角原点坐标。
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        background.draw(batch, width, height, viewport.getScreenWidth());
        batch.end();
        // 所有前景共用安全区视口，布局、弹出列表和触摸坐标使用同一坐标系。
        uiViewport.apply();
        float uiWidth = uiViewport.getWorldWidth(), uiHeight = uiViewport.getWorldHeight();
        float logoX = ((int) uiWidth - LOGO_WIDTH) / 2f;
        float logoTop = ((int) uiHeight - LOGO_HEIGHT) / 2f - LOGO_CENTER_OFFSET_Y;
        float logoY = uiHeight - logoTop - LOGO_HEIGHT;
        if (mobile) {
            Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_SCISSOR_TEST);
            com.badlogic.gdx.graphics.glutils.HdpiUtils.glScissor(uiViewport.getScreenX(), uiViewport.getScreenY(),
                    uiViewport.getScreenWidth(), uiViewport.getScreenHeight());
        }
        batch.setProjectionMatrix(uiViewport.getCamera().combined);
        batch.begin();
        batch.draw(logo, logoX, logoY, (float) LOGO_WIDTH, (float) LOGO_HEIGHT);
        batch.end();
        if (mobile) Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_SCISSOR_TEST);
        loginPanel.draw(uiWidth, uiHeight, 0, 0);
    }

    private void updateUiViewport() {
        if (!mobile || viewport.getScreenWidth() <= 0 || viewport.getScreenHeight() <= 0) return;
        int left = Math.max(0, Gdx.graphics.getSafeInsetLeft());
        int right = Math.max(0, Gdx.graphics.getSafeInsetRight());
        int top = Math.max(0, Gdx.graphics.getSafeInsetTop());
        int bottom = Math.max(0, Gdx.graphics.getSafeInsetBottom());
        int safeWidth = Math.max(1, viewport.getScreenWidth() - left - right);
        int safeHeight = Math.max(1, viewport.getScreenHeight() - top - bottom);
        ScreenViewport safe = (ScreenViewport) uiViewport;
        float units = viewport.getWorldWidth() / viewport.getScreenWidth();
        if (safe.getScreenWidth() != safeWidth || safe.getScreenHeight() != safeHeight
                || safe.getScreenX() != left || safe.getScreenY() != bottom || safe.getUnitsPerPixel() != units) {
            safe.setUnitsPerPixel(units);
            safe.update(safeWidth, safeHeight, true);
            safe.setScreenPosition(left, bottom);
        }
    }

    @Override
    public void dispose() {
        if (loginPanel != null) loginPanel.dispose();
        // 背景纹理由 AssetManager 统一释放。
        if (sceneAssets != null) sceneAssets.dispose();
        if (logo != null) logo.dispose();
        if (batch != null) batch.dispose();
    }
}
