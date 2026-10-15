package top.totodile.pokemmo.rom;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;

/** 战斗专用的 Nitro 场景与两座平台，独立维护视口及深度缓冲。 */
public final class BattleScene implements AutoCloseable {
    public record Resources(ByteBuffer platform, ByteBuffer scenery, ByteBuffer animation) { }

    static { com.badlogic.gdx.utils.GdxNativesLoader.load(); }

    private final NitroModel platform;
    private final NitroModel scenery;
    private final int animationFrames;
    public record ActorPose(float dx, float dy, float scale) { }

    private static final float[][] CAMERA_KEYS = {
            {3.5f, 3.25f, 4, 3.5f, 3.4f, 2.5f},
            {3, 3.25f, 4, 3, 3.4f, 3},
            {3.5f, 3.25f, 4, 3.5f, 3.4f, 2.5f},
            {4.25f, 3.25f, 2, 4.25f, 3.4f, .5f},
            {3.5f, 3.25f, 4, 3.5f, 3.4f, 2.5f},
            {3, 3.25f, 4, 2.5f, 3.5f, 2.5f},
            {3, 3.5f, 4, 2.5f, 3.65f, 2.5f},
            {3.5f, 3.25f, 4, 3.5f, 3.4f, 2.5f}
    };
    private final PerspectiveCamera baseline = new PerspectiveCamera(50, 760, 540);
    private final PerspectiveCamera camera = new PerspectiveCamera(50, 760, 540);

    public static Resources load(Path rom) throws IOException {
        NdsArchive archive = new NdsArchive(rom, "/a/0/1/1");
        return new Resources(archive.file(1), archive.file(46), archive.file(50));
    }

    public BattleScene(Resources resources) {
        platform = new NitroModel(resources.platform(), null, null);
        scenery = new NitroModel(resources.scenery(), resources.animation(), null);
        ByteBuffer animation = resources.animation();
        int section = animation.getInt(16);
        int base = section + animation.getInt(NitroData.dictionary(animation, section+8, 4).getFirst().offset());
        animationFrames = NdsArchive.ushort(animation, base+4);
        camera.near = .1f;
        camera.far = 100;
    }

    public void draw(int x, int y, int width, int height, int screenWidth, int screenHeight,
                     float seconds, float slide) {
        updateCamera(camera, width, height, seconds);
        updateCamera(baseline, width, height, 0);
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        float sx = viewport[2] / (float) screenWidth, sy = viewport[3] / (float) screenHeight;
        int vx = Math.round(x * sx), vy = Math.round((screenHeight-y-height)*sy);
        int vw = Math.round(width*sx), vh = Math.round(height*sy);
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glMatrixMode(GL_PROJECTION); glPushMatrix(); glLoadMatrixf(camera.projection.val);
        glMatrixMode(GL_MODELVIEW); glPushMatrix(); glLoadMatrixf(camera.view.val);
        try {
            glViewport(vx, vy, vw, vh);
            glEnable(GL_SCISSOR_TEST); glScissor(vx, vy, vw, vh);
            glDepthMask(true); glClearColor(.25f, .65f, .9f, 1);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST); glDepthFunc(GL_LEQUAL);
            glDisable(GL_LIGHTING); glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_ALPHA_TEST); glAlphaFunc(GL_GREATER, .01f);
            glColor4f(1, 1, 1, 1);
            for (boolean translucent : new boolean[]{false, true}) {
                glPushMatrix();
                glTranslatef(3, 2.5f, 3); glScalef(8, 8, 8);
                scenery.draw(seconds*60 % animationFrames, translucent);
                glPopMatrix();
                drawPlatform(3-(1-slide)*4, 4, seconds, translucent);
                drawPlatform(5.25f+(1-slide)*4, 1, seconds, translucent);
            }
        } finally {
            glPopMatrix(); glMatrixMode(GL_PROJECTION); glPopMatrix();
            glPopAttrib(); glMatrixMode(GL_MODELVIEW);
        }
    }

    private static void updateCamera(PerspectiveCamera camera, int width, int height, float seconds) {
        // 入场完成后开始七段往返，时间直接采样以避免帧率和暂停造成累计漂移。
        float time = Math.max(0, seconds - 2.9f) % 35;
        int segment = (int) (time / 5);
        float fraction = (1 - (float) Math.cos(Math.PI * (time / 5 - segment))) / 2;
        float[] from = CAMERA_KEYS[segment], to = CAMERA_KEYS[segment + 1];
        float[] value = new float[6];
        for (int i = 0; i < value.length; i++) value[i] = from[i] + (to[i] - from[i]) * fraction;
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.near = .1f;
        camera.far = 100;
        Vector3 target = new Vector3(value[0], value[1], value[2]);
        camera.position.set(value[3], value[4], value[5] + 3);
        camera.up.set(Vector3.Y);
        camera.lookAt(target);
        camera.rotateAround(target, Vector3.X, -10);
        camera.update();
    }

    /** 二维精灵以平台上的世界坐标计算位移和透视倍率，HUD 不参与镜头变换。 */
    public ActorPose actorPose(boolean own) {
        Vector3 point = new Vector3(own ? 3 : 5.25f, 2.5f, own ? 4 : 1);
        float depth = new Vector3(point).sub(camera.position).dot(camera.direction);
        float baseDepth = new Vector3(point).sub(baseline.position).dot(baseline.direction);
        if (depth <= camera.near) return new ActorPose(10000, 10000, 1);
        float zoom = baseDepth / depth;
        Vector3 current = camera.project(new Vector3(point), 0, 0, 760, 540);
        Vector3 initial = baseline.project(new Vector3(point), 0, 0, 760, 540);
        float offsetX = (own ? 377 : 740) - (134 + initial.x);
        float offsetY = (own ? 572 : 311) - (50 + 540 - initial.y);
        return new ActorPose(current.x - initial.x + offsetX * (zoom - 1),
                initial.y - current.y + offsetY * (zoom - 1), zoom);
    }

    private void drawPlatform(float x, float z, float seconds, boolean translucent) {
        glPushMatrix();
        glTranslatef(x, 2.5f, z); glScalef(16, 16, 16);
        platform.draw(seconds*30, translucent);
        glPopMatrix();
    }

    @Override public void close() { platform.close(); scenery.close(); }
}
