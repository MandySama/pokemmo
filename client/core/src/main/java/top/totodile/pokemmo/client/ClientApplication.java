package top.totodile.pokemmo.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.util.function.Function;
import java.util.function.LongSupplier;
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
    private static final long MIN_MOBILE_LOADING_NANOS = 300_000_000L;
    private static final long MIN_DESKTOP_LOGO_NANOS = 600_000_000L;
    private final Viewport viewport;
    private SpriteBatch batch;
    private Texture logo;
    private ScrollingBackground background;
    private LoginPanel loginPanel;
    private final Function<String, FileHandle> assets;
    private final LongSupplier clock;
    private final boolean mobile;
    private boolean loadingFrameShown;
    private long loadingStartedAt;

    public ClientApplication(Function<String, FileHandle> assets, boolean mobile, LongSupplier clock) {
        this.assets = assets;
        this.clock = clock;
        this.mobile = mobile;
        // 移动端沿用参考客户端的逻辑分辨率，额外屏幕空间扩展视口。
        this.viewport = mobile ? new ExtendViewport(1280, 720) : new ScreenViewport();
    }

    @Override
    public void create() {
        if (!mobile) loadLogo();
    }

    private void loadLogo() {
        batch = new SpriteBatch();
        logo = new Texture(assets.apply("theme/default/res/bg.png"));
        Texture.TextureFilter filter = mobile ? Texture.TextureFilter.Nearest : Texture.TextureFilter.Linear;
        logo.setFilter(filter, filter);
    }

    private void loadScene() {
        background = new ScrollingBackground(assets, clock);
        loginPanel = new LoginPanel(viewport, assets, mobile);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        if (batch == null) {
            // 移动端先提交黑色首帧，再加载资源，避免白色底板停留到加载结束。
            if (!loadingFrameShown) {
                loadingFrameShown = true;
                loadingStartedAt = clock.getAsLong();
                return;
            }
            loadLogo();
            loadScene();
            return;
        }
        // 以首个黑色渲染帧计时，加载耗时包含在内，不阻塞线程或重复等待。
        if (mobile && clock.getAsLong() - loadingStartedAt < MIN_MOBILE_LOADING_NANOS) return;
        if (!mobile && loadingFrameShown && background == null) loadScene();
        // PC 首帧只绘制 Logo，下一帧开始加载场景；加载时间计入最小显示时长。
        boolean showScene = mobile || (loadingFrameShown
                && clock.getAsLong() - loadingStartedAt >= MIN_DESKTOP_LOGO_NANOS);
        viewport.apply();
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();
        // 与参考客户端登录布局一致：Logo 固定为 484×143，居中后向上偏移 200 个逻辑单位。
        // 将左上角为原点的界面坐标转换为 SpriteBatch 使用的左下角原点坐标。
        float logoX = (float) ((int) width - LOGO_WIDTH) / 2;
        float logoTop = (float) ((int) height - LOGO_HEIGHT) / 2 - LOGO_CENTER_OFFSET_Y;
        float logoY = height - logoTop - LOGO_HEIGHT;
        if (mobile) {
            // 在刘海安全区域内居中，将物理像素边距换算为逻辑坐标。
            float unitsPerPixelX = width / viewport.getScreenWidth();
            float unitsPerPixelY = height / viewport.getScreenHeight();
            logoX += (Gdx.graphics.getSafeInsetLeft() - Gdx.graphics.getSafeInsetRight()) * unitsPerPixelX / 2f;
            logoY += (Gdx.graphics.getSafeInsetBottom() - Gdx.graphics.getSafeInsetTop()) * unitsPerPixelY / 2f;
        }
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        if (showScene) background.draw(batch, width, height, viewport.getScreenWidth());
        batch.draw(logo, logoX, logoY, (float) LOGO_WIDTH, (float) LOGO_HEIGHT);
        batch.end();
        if (!mobile && !loadingFrameShown) {
            loadingFrameShown = true;
            loadingStartedAt = clock.getAsLong();
        }
        if (showScene) loginPanel.draw(width, height, logoX - ((int) width - LOGO_WIDTH) / 2f,
                logoY - (height - logoTop - LOGO_HEIGHT));
    }

    @Override
    public void dispose() {
        if (loginPanel != null) loginPanel.dispose();
        if (background != null) background.dispose();
        if (logo != null) logo.dispose();
        if (batch != null) batch.dispose();
    }
}
