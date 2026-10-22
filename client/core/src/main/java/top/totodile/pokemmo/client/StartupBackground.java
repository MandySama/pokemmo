package top.totodile.pokemmo.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import java.util.function.Function;
import java.util.function.LongSupplier;

/** 解压前加载的滚动背景；完成后交给客户端继续绘制，避免重新加载和滚动跳变。 */
public final class StartupBackground implements AutoCloseable {
    private final AssetManager assets;
    private final ExtendViewport viewport = new ExtendViewport(1280, 720);
    private final LongSupplier clock;
    private SpriteBatch batch;
    private ScrollingBackground background;

    public StartupBackground(Function<String, FileHandle> files, LongSupplier clock) {
        this.clock = clock;
        assets = new AssetManager(files::apply);
        for (int i = 0; i < 3; i++) assets.load("sprites/textures/bg_0" + i + ".png", Texture.class);
    }

    public boolean update() {
        if (background != null) return true;
        if (!assets.update()) return false;
        Texture[] tiles = new Texture[3];
        for (int i = 0; i < 3; i++) tiles[i] = assets.get("sprites/textures/bg_0" + i + ".png", Texture.class);
        background = new ScrollingBackground(tiles, clock);
        batch = new SpriteBatch();
        return true;
    }

    public void draw() {
        if (background == null) return;
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        background.draw(batch, viewport.getWorldWidth(), viewport.getWorldHeight(), viewport.getScreenWidth());
        batch.end();
    }

    @Override public void close() { assets.dispose(); if (batch != null) batch.dispose(); }
}
