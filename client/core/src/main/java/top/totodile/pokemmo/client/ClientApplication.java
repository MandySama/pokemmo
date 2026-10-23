package top.totodile.pokemmo.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import top.totodile.pokemmo.client.config.SavedAccounts;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.RomFilePicker;
import top.totodile.pokemmo.client.rom.RomManager;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.ui.character.CharacterAppearance;
import top.totodile.pokemmo.client.ui.character.OverworldCharacter;
import top.totodile.pokemmo.client.world.MapMovement;
import top.totodile.pokemmo.client.world.MobileMapControls;
import top.totodile.pokemmo.client.world.WorldScene;
import top.totodile.pokemmo.client.world.WorldMaps;
import top.totodile.pokemmo.client.world.WorldMapLoader;
import top.totodile.pokemmo.client.ui.ClientUi;
import top.totodile.pokemmo.client.ui.login.LoginController;
import top.totodile.pokemmo.client.ui.startup.ScrollingBackground;
import top.totodile.pokemmo.client.ui.startup.StartupBackground;

/** 组装平台登录场景、回退背景及客户端界面。 */
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
    private StartupBackground startupBackground;
    private top.totodile.pokemmo.server.game.GameService gameService;
    private RomFilePicker romFilePicker;
    private Runnable restart=() -> Gdx.app.exit();
    private Function<ClientResources, top.totodile.pokemmo.client.ui.startup.LoginScene> loginSceneFactory;
    private top.totodile.pokemmo.client.ui.startup.LoginScene loginScene;
    private boolean focused=true;
    private WorldScene world;
    private record LoadedWorld(WorldMaps maps,MapMovement movement) {}
    private volatile boolean disposed;
    private final java.util.Set<java.util.concurrent.CompletableFuture<Void>> pendingWorldEntries=
            java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final java.util.concurrent.ExecutorService worldLoader=java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
        Thread thread=new Thread(r,"world-maps"); thread.setDaemon(true); return thread;
    });

    public ClientApplication withLoginScene(Function<ClientResources, top.totodile.pokemmo.client.ui.startup.LoginScene> factory) {
        loginSceneFactory=java.util.Objects.requireNonNull(factory); return this;
    }
    public void setFocused(boolean focused) {
        this.focused=focused;
        if (loginScene!=null) loginScene.setFocused(focused);
        if(world!=null) world.setFocused(focused);
    }

    public ClientApplication withRomFilePicker(RomFilePicker picker) { romFilePicker=picker; return this; }
    public ClientApplication withRestart(Runnable action) { restart=java.util.Objects.requireNonNull(action); return this; }
    public ClientApplication withStartupBackground(StartupBackground background) {
        startupBackground = java.util.Objects.requireNonNull(background);
        return this;
    }
    public RomManager romResources() { return resources == null ? null : resources.roms(); }

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
        if (loginSceneFactory!=null) { loginScene=loginSceneFactory.apply(resources); loginScene.setFocused(focused); }
        if (startupBackground == null) {
            sceneAssets = new AssetManager(resources::apply);
            for (int i = 0; i < 3; i++) {
                sceneAssets.load("sprites/textures/bg_0" + i + ".png", Texture.class);
            }
        }
    }

    private void loadScene() {
        if (startupBackground == null) {
            Texture[] tiles = new Texture[3];
            for (int i = 0; i < tiles.length; i++) {
                tiles[i] = sceneAssets.get("sprites/textures/bg_0" + i + ".png", Texture.class);
            }
            background = new ScrollingBackground(tiles, clock);
        }
        loginController = new LoginController(loginService, Gdx.app::postRunnable);
        clientUi = new ClientUi(uiViewport, resources, mobile, SavedAccounts.open(configDirectory.get()),
                () -> System.nanoTime() / 1_000_000L, loginController, batteryPercent, () -> Gdx.app.exit(), gameService);
        clientUi.onEnterWorld(this::enterWorld);
        clientUi.onLeaveWorld(this::leaveWorld);
        clientUi.onRomRestart(restart);
    }

    private void leaveWorld() {
        if(world==null)return;
        world.setFocused(false);
        world.close();world=null;
        clientUi.showCharacters();
        if(loginSceneFactory!=null) {
            loginScene=loginSceneFactory.apply(resources);
            loginScene.setFocused(focused);
        }
    }

    private java.util.concurrent.CompletionStage<Void> enterWorld(top.totodile.pokemmo.server.game.GameService.Entry entry) {
        if(disposed)return java.util.concurrent.CompletableFuture.failedFuture(new java.util.concurrent.CancellationException());
        var selection=selectedServer();
        var type=RomType.of(entry.character().appearance().region());
        var rom=resources.roms().get(type);
        if(rom==null) return java.util.concurrent.CompletableFuture.failedFuture(new IllegalStateException(type+" ROM is not loaded"));
        var ready=new java.util.concurrent.CompletableFuture<Void>();
        pendingWorldEntries.add(ready);
        ready.whenComplete((ignored,failure)->pendingWorldEntries.remove(ready));
        if(disposed) {ready.cancel(false);return ready;}
        try {
            java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                if(disposed || ready.isDone())throw new java.util.concurrent.CancellationException();
                try {
                    var maps=WorldMapLoader.load(rom);
                    var movement=new MapMovement(maps,entry.position());
                    maps.tiles(movement.map());
                    if(maps.is3d())maps.geometry(movement.map().id());
                    return new LoadedWorld(maps,movement);
                } catch(java.io.IOException error) { throw new java.util.concurrent.CompletionException(error); }
            },worldLoader).whenComplete((loaded,error) -> {
                // The render queue may stop permanently during dispose; finish cancellation on this worker instead.
                if(disposed || ready.isDone()) {ready.cancel(false);return;}
                try {
                    Gdx.app.postRunnable(() -> {
                        if(disposed || ready.isDone() || selection!=selectedServer()) { ready.cancel(false); return; }
                        if(error!=null) { ready.completeExceptionally(error); return; }
                        OverworldCharacter sprite=null;
                        WorldScene scene=null;
                        try {
                            var catalog=new CharacterAppearance(resources.apply("sprites/addons.pak"),resources.strings());
                            try { sprite=new OverworldCharacter(catalog,entry.character().appearance(),resources.apply("sprites/game.pak")); }
                            finally { catalog.dispose(); }
                            scene=new WorldScene(loaded.maps(),loaded.movement(),sprite,resources.preferences(),mobile,
                                    position -> gameService.savePosition(selection,entry.character().id(),position)
                                        .exceptionally(failure -> { Gdx.app.error("world","Cannot save character position",failure); return null; }),
                                    entry.travelState(),state -> gameService.saveTravelState(selection,entry.character().id(),state)
                                        .exceptionally(failure -> { Gdx.app.error("world","Cannot save character travel state",failure); return null; }),
                                    entry.pokedexState(),state -> gameService.savePokedexState(selection,entry.character().id(),state)
                                        .exceptionally(failure -> { Gdx.app.error("world","Cannot save character Pokédex",failure); return null; }));
                            scene.setWorldInfo(entry.worldInfo());
                            if(mobile) scene.setControls(new MobileMapControls(resources.apply("buttons/controller.png"),resources.preferences()));
                            scene.setFocused(focused);
                            if(loginScene!=null) { loginScene.close(); loginScene=null; }
                            var worldInput=clientUi.showWorld(scene);
                            world=scene;
                            Gdx.input.setInputProcessor(worldInput);
                            ready.complete(null);
                        } catch(RuntimeException failure) {
                            if(scene!=null) scene.close(); else if(sprite!=null) sprite.close();
                            ready.completeExceptionally(failure);
                        }
                    });
                } catch(RuntimeException failure) {ready.completeExceptionally(failure);}
            });
        } catch(java.util.concurrent.RejectedExecutionException failure) {
            if(disposed)ready.cancel(false);else ready.completeExceptionally(failure);
        }
        return ready;
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
        if (clientUi == null && (startupBackground != null ? startupBackground.update() : sceneAssets.update())) {
            if (startupBackground == null && batch == null) batch = new SpriteBatch();
            loadScene();
        }
        // 普通启动等待资源就绪；移动端已加载的启动背景由外层保留并交接。
        if (clientUi == null) return;
        if(world!=null) {
            world.draw(Math.min(.1f,Gdx.graphics.getDeltaTime()),Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
            uiViewport.apply();
            clientUi.draw(uiViewport.getWorldWidth(),uiViewport.getWorldHeight(),0,0);
            return;
        }
        viewport.apply();
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();
        if (loginScene!=null && loginScene.draw(Gdx.graphics.getDeltaTime(),viewport.getScreenWidth(),viewport.getScreenHeight())) {
            // PC ROM 场景已绘制，前景继续使用现有 UI 视口。
        } else if (mobile || loginScene == null || resources.roms().get(RomType.BLACK_WHITE) == null) {
            // PC 仅在未加载黑白 ROM 时使用滚动背景；3D 准备期间保留清屏底色。
            if (startupBackground != null) startupBackground.draw();
            else {
                batch.setProjectionMatrix(viewport.getCamera().combined);
                batch.begin();
                background.draw(batch, width, height, viewport.getScreenWidth());
                batch.end();
            }
        }
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
    public void pause() { setFocused(false); }

    @Override
    public void resume() { setFocused(true); }

    @Override
    public void dispose() {
        if(disposed)return;
        disposed=true;
        // shutdownNow also removes queued loader jobs, so their dependent futures must be completed here.
        for(var pending:pendingWorldEntries)pending.cancel(false);
        worldLoader.shutdownNow();
        if(world!=null) {
            Gdx.input.setInputProcessor(null);
            world.close();
        }
        if (loginScene!=null) loginScene.close();
        if (clientUi != null) clientUi.dispose();
        if (gameService != null) gameService.close();
        // 背景纹理由 AssetManager 统一释放。
        if (sceneAssets != null) sceneAssets.dispose();
        if (startupBackground != null) startupBackground.close();
        if (batch != null) batch.dispose();
        if (resources != null) resources.close();
    }
}
