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
    private float framingFieldOfView = 50;
    private float framingVerticalOffset;
    private float framingBlend;

    public void setGigantamaxFraming(float fieldOfView, float verticalOffset, float blend) {
        if (!Float.isFinite(fieldOfView) || fieldOfView < 50 || fieldOfView > 90
                || !Float.isFinite(verticalOffset) || Math.abs(verticalOffset) > 2
                || !Float.isFinite(blend) || blend < 0 || blend > 1) {
            throw new IllegalArgumentException("Invalid battle framing");
        }
        framingFieldOfView = fieldOfView;
        framingVerticalOffset = verticalOffset;
        framingBlend = blend;
    }

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
        draw(x, y, width, height, screenWidth, screenHeight, seconds, slide, false, 1);
    }

    /** 平台叠加到当前地图，不清除地图颜色缓冲。 */
    public void drawPlatforms(int x, int y, int width, int height, int screenWidth, int screenHeight,
                              float seconds, float slide) {
        draw(x, y, width, height, screenWidth, screenHeight, seconds, slide, true, 1);
    }

    public void drawPlatforms(int x, int y, int width, int height, int screenWidth, int screenHeight,
                              float seconds, float slide, float enemyScale) {
        draw(x, y, width, height, screenWidth, screenHeight, seconds, slide, true, enemyScale);
    }

    private void draw(int x, int y, int width, int height, int screenWidth, int screenHeight,
                      float seconds, float slide, boolean mapBackground, float enemyScale) {
        updateCamera(camera, width, height, seconds, framingFieldOfView, framingVerticalOffset, framingBlend);
        updateCamera(baseline, width, height, 0, 50, 0, 0);
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
            glClear((mapBackground ? 0 : GL_COLOR_BUFFER_BIT) | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST); glDepthFunc(GL_LEQUAL);
            glDisable(GL_LIGHTING); glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_ALPHA_TEST); glAlphaFunc(GL_GREATER, .01f);
            glColor4f(1, 1, 1, 1);
            for (boolean translucent : new boolean[]{false, true}) {
                glPushMatrix();
                glTranslatef(3, 2.5f, 3); glScalef(8, 8, 8);
                if (!mapBackground) scenery.draw(seconds*60 % animationFrames, translucent);
                glPopMatrix();
                drawPlatform(3-(1-slide)*4, 4, seconds, translucent, 1);
                drawPlatform(5.25f+(1-slide)*4, 1, seconds, translucent, enemyScale);
            }
        } finally {
            glPopMatrix(); glMatrixMode(GL_PROJECTION); glPopMatrix();
            glPopAttrib(); glMatrixMode(GL_MODELVIEW);
        }
    }

    private static void updateCamera(PerspectiveCamera camera, int width, int height, float seconds, float fieldOfView, float verticalOffset, float framingBlend) {
        // 入场完成后开始七段往返，时间直接采样以避免帧率和暂停造成累计漂移。
        float time = Math.max(0, seconds - 2.9f) % 35;
        int segment = (int) (time / 5);
        float fraction = (1 - (float) Math.cos(Math.PI * (time / 5 - segment))) / 2;
        float[] from = CAMERA_KEYS[segment], to = CAMERA_KEYS[segment + 1];
        float[] value = new float[6];
        for (int i = 0; i < value.length; i++) value[i] = from[i] + (to[i] - from[i]) * fraction;
        // 大体型取景保留七段镜头巡航。
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.near = .1f;
        camera.far = 100;
        camera.fieldOfView = 50 + (fieldOfView - 50) * framingBlend;
        float shift = verticalOffset * framingBlend;
        Vector3 target = new Vector3(value[0], value[1] + shift, value[2]);
        camera.position.set(value[3], value[4] + shift, value[5] + 3);
        camera.up.set(Vector3.Y);
        camera.lookAt(target);
        camera.rotateAround(target, Vector3.X, -10);
        camera.direction.nor();
        // 扩大视野时向近景推进，保持近景尺寸，让远景自然缩小。
        Vector3 ownCenter = new Vector3(3, 2.5f, 4);
        float ownDepth = ownCenter.sub(camera.position).dot(camera.direction);
        float focalRatio = (float) (Math.tan(Math.toRadians(25))
                / Math.tan(Math.toRadians(camera.fieldOfView / 2)));
        camera.position.mulAdd(camera.direction, ownDepth * (1 - focalRatio));
        camera.update();
    }

    /** 二维精灵以平台上的世界坐标计算位移和透视倍率，HUD 不参与镜头变换。 */
    public ActorPose actorPose(boolean own) {
        Vector3 initialPoint = new Vector3(own ? 3 : 5.25f, 2.5f, own ? 4 : 1);
        Vector3 point = new Vector3(initialPoint);
        float depth = new Vector3(point).sub(camera.position).dot(camera.direction);
        float baseDepth = new Vector3(initialPoint).sub(baseline.position).dot(baseline.direction);
        if (depth <= camera.near) return new ActorPose(10000, 10000, 1);
        float zoom = baseDepth / depth * (float) (Math.tan(Math.toRadians(baseline.fieldOfView / 2))
                / Math.tan(Math.toRadians(camera.fieldOfView / 2)));
        Vector3 current = camera.project(new Vector3(point), 0, 0, 760, 540);
        Vector3 initial = baseline.project(initialPoint, 0, 0, 760, 540);
        float offsetX = (own ? 377 : 740) - (134 + initial.x);
        float offsetY = (own ? 572 : 311) - (50 + 540 - initial.y);
        // 原布局偏移仅用于普通巡航；大体型取景直接落在圆盘中心投影。
        float retainedOffset = 1 - framingBlend;
        return new ActorPose(current.x - initial.x + offsetX * (zoom * retainedOffset - 1),
                initial.y - current.y + offsetY * (zoom * retainedOffset - 1), zoom);
    }

    private void drawPlatform(float x, float z, float seconds, boolean translucent, float scale) {
        glPushMatrix();
        glTranslatef(x, 2.5f, z); glScalef(16*scale, 16, 16*scale);
        platform.draw(seconds*30, translucent);
        glPopMatrix();
    }

    @Override public void close() { platform.close(); scenery.close(); }
}
