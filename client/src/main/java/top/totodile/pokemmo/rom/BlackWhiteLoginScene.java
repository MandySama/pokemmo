package top.totodile.pokemmo.rom;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;

/** 黑白标题场景：三组模型及镜头按每秒 30 帧播放，镜头结束后整体循环。 */
public final class BlackWhiteLoginScene implements AutoCloseable {
    static {
        com.badlogic.gdx.utils.GdxNativesLoader.load();
    }

    private final NitroModel[] models = new NitroModel[3];
    private final PerspectiveCamera camera = new PerspectiveCamera(80, 1024, 768);
    private final ByteBuffer track;
    private final int frames;
    private final int stride;
    private final String title;

    public BlackWhiteLoginScene(Path rom, boolean whiteScene) throws IOException {
        NdsArchive archive = new NdsArchive(rom, "/a/1/6/0");
        int offset = whiteScene ? 8 : 0;
        try {
            models[0] = new NitroModel(archive.file(227 + offset), archive.file(226 + offset), archive.file(228 + offset));
            models[1] = new NitroModel(archive.file(230 + offset), archive.file(229 + offset), null);
            models[2] = new NitroModel(archive.file(232 + offset), archive.file(231 + offset), null);
            track = archive.file(233 + offset);
            frames = track.getInt(0);
            stride = switch (track.getInt(4)) {
                case 65792 -> 24;
                case 65793 -> 36;
                default -> throw new IllegalArgumentException("Unsupported camera track");
            };
            if (frames < 1 || 8L + (long) frames * stride != track.limit()) {
                throw new IllegalArgumentException("Invalid camera track size");
            }
            try (var input = java.nio.file.Files.newInputStream(rom)) {
                byte[] header = input.readNBytes(12);
                title = new String(header, java.nio.charset.StandardCharsets.US_ASCII).replace("\0", "").trim();
            }
            camera.near = 0.01f;
            camera.far = 200;
        } catch (RuntimeException exception) {
            throw new IOException("Unable to decode Black/White login scene", exception);
        }
    }

    public String title() {
        return title;
    }

    public double durationSeconds() {
        return frames / 30.0;
    }

    public void draw(int width, int height, double seconds) {
        float frame = (float) (seconds * 30 % frames);
        int index = (int) frame;
        float fraction = frame - index;
        float[] sample = new float[6];
        float positionDelta = 0, rotationDelta = 0;
        for (int i = 0; i < 6; i++) {
            sample[i] = track.getFloat(8 + index * stride + i * 4);
            float delta = Math.abs(track.getFloat(8 + Math.min(index + 1, frames - 1) * stride + i * 4) - sample[i]);
            if (i < 3) rotationDelta += delta;
            else positionDelta += delta;
        }
        // 镜头切换处直接跳转，连续轨迹才进行帧间插值。
        if (positionDelta < 10 && rotationDelta < 10) {
            for (int i = 0; i < 6; i++) {
                float next = track.getFloat(8 + Math.min(index + 1, frames - 1) * stride + i * 4);
                sample[i] += (next - sample[i]) * fraction;
            }
        }
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.position.set(sample[3] / 64, sample[4] / 64, sample[5] / 64);
        camera.direction.set(0, 0, -1);
        camera.up.set(0, 1, 0);
        for (int i = 0; i < 3; i++) {
            Vector3 axis = i == 0 ? Vector3.X : i == 1 ? Vector3.Y : Vector3.Z;
            camera.direction.rotate(axis, sample[i]);
            camera.up.rotate(axis, sample[i]);
        }
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
            glClearColor(1, 1, 1, 1);
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
            for (NitroModel model : models) model.draw(frame, false);
            for (NitroModel model : models) model.draw(frame, true);
        } finally {
            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glPopAttrib();
            glMatrixMode(GL_MODELVIEW);
        }
    }

    @Override
    public void close() {
        for (NitroModel model : models) {
            if (model != null) model.close();
        }
    }
}
