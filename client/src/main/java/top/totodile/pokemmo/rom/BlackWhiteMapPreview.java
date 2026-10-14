package top.totodile.pokemmo.rom;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.GdxNativesLoader;

import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

/** 单个 NDS 地形模型的 3D 绘制；纹理只在绘制线程创建与销毁。 */
public final class BlackWhiteMapPreview implements AutoCloseable {
    static {
        GdxNativesLoader.load();
    }

    private final NitroModel model;
    private final PerspectiveCamera camera = new PerspectiveCamera(35, 1024, 768);
    private final Map<Integer, NitroModel> buildings = new HashMap<>();
    private final List<NdsMaps.Placement> placements;

    public BlackWhiteMapPreview(NdsMaps maps, int header, int chunkId) throws IOException {
        GdxNativesLoader.load();
        var chunk = maps.chunk(chunkId);
        model = new NitroModel(chunk.model(), null, null, maps.textures(header, 0));
        try {
            placements = maps.placements(chunk);
            var textures = maps.buildingTextures(header, 0);
            for (var building : maps.buildings(header, 0)) {
                if (placements.stream().anyMatch(placement -> placement.model() == building.id())) {
                    buildings.put(building.id(), new NitroModel(building.model(), null, null, textures));
                }
            }
        } catch (IOException | RuntimeException exception) {
            close();
            throw new IOException("Unable to decode NDS scene: " + header + "/" + chunkId, exception);
        }
        camera.near = 0.01f;
        camera.far = 200;
    }

    public void render(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.position.set(-3, 5, 4);
        camera.lookAt(-3, 0, -2);
        camera.update();
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadMatrixf(camera.projection.val);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadMatrixf(camera.view.val);
        try {
            glDisable(GL_SCISSOR_TEST);
            glClearColor(0.1f, 0.1f, 0.1f, 1);
            glDepthMask(true);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
            glFrontFace(GL_CCW);
            glDisable(GL_LIGHTING);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_ALPHA_TEST);
            glAlphaFunc(GL_GREATER, 0.01f);
            model.draw(0, false);
            drawBuildings(false);
            model.draw(0, true);
            drawBuildings(true);
        } finally {
            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glPopAttrib();
            glMatrixMode(GL_MODELVIEW);
        }
    }

    private void drawBuildings(boolean translucent) {
        for (var placement : placements) {
            NitroModel building = buildings.get(placement.model());
            if (building == null) continue;
            glPushMatrix();
            glTranslatef(placement.x(), placement.y(), placement.z());
            glRotatef(placement.rotation() / 64f * 90, 0, 1, 0);
            building.draw(0, translucent);
            glPopMatrix();
        }
    }

    /** 在调用方设置的投影和模型视图中绘制，便于组合矩阵分块。 */
    public void draw(float frame, boolean translucent) {
        model.draw(frame, translucent);
        drawBuildings(translucent);
    }

    @Override
    public void close() {
        model.close();
        buildings.values().forEach(NitroModel::close);
    }
}
