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
    private final Viewport viewport;
    private final Viewport uiViewport;
    private final top.totodile.pokemmo.server.login.LoginService loginService;
    private LoginController loginController;
    private SpriteBatch batch;
    private ClientResources resources;
    private ScrollingBackground background;
    private ClientUi clientUi;
    private final Function<String, FileHandle> assets;
    private final LongSupplier clock;
    private final boolean mobile;
    private final Supplier<FileHandle> configDirectory;
    private final java.util.function.IntSupplier batteryPercent;
    private AssetManager sceneAssets;
    private top.totodile.pokemmo.server.game.GameService gameService;
    private RomFilePicker romFilePicker;

    public ClientApplication withRomFilePicker(RomFilePicker picker) { romFilePicker=picker; return this; }
    public RomResources romResources() { return resources == null ? null : resources.roms(); }

    public ClientApplication withGameService(top.totodile.pokemmo.server.game.GameService service) {
        gameService = java.util.Objects.requireNonNull(service);
        return this;
    }

    public ClientApplication(Function<String, FileHandle> assets, boolean mobile, LongSupplier clock,
                             top.totodile.pokemmo.server.login.LoginService loginService) {
        this(assets, mobile, clock, () -> Gdx.files.local("config"), loginService);
    }

    public ClientApplication(Function<String, FileHandle> assets, boolean mobile, LongSupplier clock,
                             Supplier<FileHandle> configDirectory,
                             top.totodile.pokemmo.server.login.LoginService loginService) {
        this(assets, mobile, clock, configDirectory, loginService, () -> -1);
    }

    public ClientApplication(Function<String, FileHandle> assets, boolean mobile, LongSupplier clock,
                             top.totodile.pokemmo.server.login.LoginService loginService,
                             java.util.function.IntSupplier batteryPercent) {
        this(assets, mobile, clock, () -> Gdx.files.local("config"), loginService, batteryPercent);
    }

    public ClientApplication(Function<String, FileHandle> assets, boolean mobile, LongSupplier clock,
                             Supplier<FileHandle> configDirectory,
                             top.totodile.pokemmo.server.login.LoginService loginService,
                             java.util.function.IntSupplier batteryPercent) {
        this.loginService = java.util.Objects.requireNonNull(loginService);
        this.batteryPercent = java.util.Objects.requireNonNull(batteryPercent);
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
        resources = new ClientResources(assets, configDirectory.get(), mobile);
        resources.setRomFilePicker(romFilePicker);
        sceneAssets = new AssetManager(resources::apply);
        for (int i = 0; i < 3; i++) {
            sceneAssets.load("sprites/textures/bg_0" + i + ".png", Texture.class);
        }
    }

    private void loadScene() {
        Texture[] tiles = new Texture[3];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = sceneAssets.get("sprites/textures/bg_0" + i + ".png", Texture.class);
        }
        background = new ScrollingBackground(tiles, clock);
        loginController = new LoginController(loginService, Gdx.app::postRunnable);
        clientUi = new ClientUi(uiViewport, resources, mobile, SavedAccounts.open(configDirectory.get()),
                () -> System.nanoTime() / 1_000_000L, loginController, batteryPercent, () -> Gdx.app.exit(), gameService);
    }

    /** 后续角色/世界入口从此取得选择结果，不需要访问登录界面或密码。 */
    public top.totodile.pokemmo.server.login.LoginService.Selection selectedServer() {
        return loginController == null ? null : loginController.selection();
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
            if (batch == null) batch = new SpriteBatch();
            loadScene();
        }
        // 两端加载期间均保持黑色，资源和登录界面准备完成后一起显示。
        if (background == null) return;
        viewport.apply();
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        background.draw(batch, width, height, viewport.getScreenWidth());
        batch.end();
        // 所有前景共用安全区视口，布局、弹出列表和触摸坐标使用同一坐标系。
        uiViewport.apply();
        float uiWidth = uiViewport.getWorldWidth(), uiHeight = uiViewport.getWorldHeight();
        clientUi.draw(uiWidth, uiHeight, 0, 0);
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
        if (clientUi != null) clientUi.dispose();
        if (gameService != null) gameService.close();
        // 背景纹理由 AssetManager 统一释放。
        if (sceneAssets != null) sceneAssets.dispose();
        if (batch != null) batch.dispose();
        if (resources != null) resources.close();
    }
}
