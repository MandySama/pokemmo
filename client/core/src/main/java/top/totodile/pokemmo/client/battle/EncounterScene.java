package top.totodile.pokemmo.client.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;

import de.matthiasmann.twl.renderer.Image;

import top.totodile.pokemmo.client.rom.nds.bw.BwPokedex;
import top.totodile.pokemmo.client.rom.nds.model.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

import java.io.IOException;

/** Gdx3DModelBatch.Wi0: BW battle NARC, I4.ri scenery and the two single-battle platforms. */
final class EncounterScene implements AutoCloseable {
    private final GdxTwlRenderer renderer;
    private NitroModelRenderer scenery, playerBase, enemyBase;
    private ModelBatch batch;
    private com.badlogic.gdx.graphics.g3d.utils.AnimationController animation;
    private FrameBuffer buffer;
    private Image image;
    private final PerspectiveCamera camera = new PerspectiveCamera(50, 760, 400);
    private EncounterMoveAnimation renderingMove;

    EncounterScene(BwPokedex dex, GdxTwlRenderer renderer) throws IOException {
        this.renderer = renderer;
        try {
            var archive = dex.archive("a/0/1/1");
            byte[] model = archive.read(34);
            var parsed =
                    NdsNitroModelParser.readAll(model, model, java.util.List.of(archive.read(35)))
                            .get(0);
            scenery = new NitroModelRenderer(parsed);
            animation =
                    new com.badlogic.gdx.graphics.g3d.utils.AnimationController(scenery.instance);
            animation.setAnimation(scenery.instance.animations.first().id, -1, 2, null);
            playerBase = new NitroModelRenderer(NdsNitroModelParser.read(archive.read(0)));
            enemyBase = new NitroModelRenderer(NdsNitroModelParser.read(archive.read(0)));
            scenery.instance.transform.setToTranslation(3, 2.5f, 3).scale(8, 8, 8);
            playerBase.instance.transform.setToTranslation(3, 2.5f, 4).scale(16, 16, 16);
            enemyBase.instance.transform.setToTranslation(5.25f, 2.5f, 1).scale(16, 16, 16);
            var config = new com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config();
            config.numBones =
                    Math.max(
                            scenery.maxBones(),
                            Math.max(playerBase.maxBones(), enemyBase.maxBones()));
            var alphaMaterials = new java.util.HashSet<com.badlogic.gdx.graphics.g3d.Material>();
            for (var material : parsed.materials()) {
                var info = parsed.textures().info(material.texture);
                if (info != null && (info.format() == 1 || info.format() == 6))
                    alphaMaterials.add(scenery.instance.getMaterial(material.name));
            }
            // Gdx3DModelBatch uses FG (OpaqueTransparentRenderableSorter): alpha-texture
            // materials follow indexed cutouts, before applying the distance ordering.
            var sorter =
                    new com.badlogic.gdx.graphics.g3d.utils.DefaultRenderableSorter() {
                        @Override
                        public int compare(
                                com.badlogic.gdx.graphics.g3d.Renderable a,
                                com.badlogic.gdx.graphics.g3d.Renderable b) {
                            var aa =
                                    (com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute)
                                            a.material.get(
                                                    com.badlogic.gdx.graphics.g3d.attributes
                                                            .BlendingAttribute.Type);
                            var ba =
                                    (com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute)
                                            b.material.get(
                                                    com.badlogic.gdx.graphics.g3d.attributes
                                                            .BlendingAttribute.Type);
                            boolean ab = aa != null && aa.blended, bb = ba != null && ba.blended;
                            if (ab != bb) return ab ? 1 : -1;
                            if (ab && bb) {
                                if (aa.opacity == 1 && ba.opacity < 1) return -1;
                                if (ba.opacity == 1 && aa.opacity < 1) return 1;
                            }
                            boolean ak = alphaMaterials.contains(a.material),
                                    bk = alphaMaterials.contains(b.material);
                            if (ak != bk) return ak ? 1 : -1;
                            return super.compare(a, b);
                        }
                    };
            var groups =
                    new java.util.IdentityHashMap<
                            com.badlogic.gdx.graphics.g3d.Material, Integer>();
            for (var material : scenery.instance.materials) groups.put(material, 0);
            for (var material : playerBase.instance.materials) groups.put(material, 1);
            for (var material : enemyBase.instance.materials) groups.put(material, 2);
            String fragment =
                    com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.getDefaultFragmentShader()
                                    .replaceFirst(
                                            "void\\s+main\\s*\\(\\s*\\)", "void reference_main()")
                            + "\n"
                            + "uniform vec4 u_referenceTint;\n"
                            + "void main(){reference_main();gl_FragColor.rgb=mix(gl_FragColor.rgb,u_referenceTint.rgb,u_referenceTint.a);}\n";
            var provider =
                    new com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider(config) {
                        @Override
                        protected com.badlogic.gdx.graphics.g3d.Shader createShader(
                                com.badlogic.gdx.graphics.g3d.Renderable renderable) {
                            var shader =
                                    new com.badlogic.gdx.graphics.g3d.shaders.DefaultShader(
                                            renderable,
                                            config,
                                            com.badlogic.gdx.graphics.g3d.shaders.DefaultShader
                                                    .createPrefix(renderable, config),
                                            com.badlogic.gdx.graphics.g3d.shaders.DefaultShader
                                                    .getDefaultVertexShader(),
                                            fragment);
                            shader.register(
                                    new com.badlogic.gdx.graphics.g3d.shaders.BaseShader.Uniform(
                                            "u_referenceTint"),
                                    new com.badlogic.gdx.graphics.g3d.shaders.BaseShader
                                            .LocalSetter() {
                                        public void set(
                                                com.badlogic.gdx.graphics.g3d.shaders.BaseShader
                                                        target,
                                                int input,
                                                com.badlogic.gdx.graphics.g3d.Renderable value,
                                                com.badlogic.gdx.graphics.g3d.Attributes
                                                        attributes) {
                                            target.set(
                                                    input,
                                                    renderingMove == null
                                                            ? Color.CLEAR
                                                            : renderingMove
                                                                    .modelTints[
                                                                    groups.getOrDefault(
                                                                            value.material, 0)]);
                                        }
                                    });
                            return shader;
                        }
                    };
            batch = new ModelBatch(provider, sorter);
        } catch (IOException | RuntimeException error) {
            close();
            throw error;
        }
    }

    Image image(
            int width,
            int height,
            long entranceTime,
            EncounterMoveAnimation move,
            Runnable drawCombatants) {
        renderer.renderOffscreen(
                () -> {
                    if (buffer == null
                            || buffer.getWidth() != width
                            || buffer.getHeight() != height) {
                        if (buffer != null) buffer.dispose();
                        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, true);
                        var region = new TextureRegion(buffer.getColorBufferTexture());
                        region.flip(false, true);
                        image = renderer.image(region);
                    }
                    // Gdx3DModelBatch's settled camera (Lt / SG0), OverworldCamera.JP and PB.
                    float t = Math.max(0, Math.min(1, (entranceTime - 1000) / 1000f));
                    t = t < .5f ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);
                    var target = new Vector3(3.5f, 3, -.5f).lerp(new Vector3(3.5f, 3.25f, 4), t);
                    camera.viewportWidth = width;
                    camera.viewportHeight = height;
                    camera.near = .1f;
                    camera.far = 50;
                    camera.up.set(Vector3.Y);
                    camera.position.set(4, 3, 4).lerp(new Vector3(3.5f, 3.4f, 5.5f), t);
                    camera.lookAt(target);
                    // w9 case 6 frames the trainer for 1s; case 7 returns the camera during the
                    // throw.
                    if (entranceTime >= 1500 && entranceTime < 3500) {
                        float pan =
                                entranceTime < 2500
                                        ? (entranceTime - 1500) / 1000f
                                        : 1 - (entranceTime - 2500) / 1000f;
                        pan = pan < .5f ? 2 * pan * pan : 1 - 2 * (1 - pan) * (1 - pan);
                        target.set(3.5f, 3.25f, 4).lerp(new Vector3(2.75f, 3, 3), pan);
                        camera.position
                                .set(3.5f, 3.4f, 5.5f)
                                .lerp(new Vector3(3.75f, 2.75f, 7), pan);
                        camera.lookAt(target);
                    }
                    float turn = Math.max(0, Math.min(1, entranceTime / 1000f));
                    turn = turn < .5f ? 2 * turn * turn : 1 - 2 * (1 - turn) * (1 - turn);
                    camera.rotateAround(target, Vector3.Y, 15 * (1 - turn));
                    camera.rotateAround(target, camera.direction.cpy().crs(camera.up).nor(), -10);
                    camera.update();
                    if (move != null) move.camera(camera);
                    buffer.begin();
                    try {
                        // Gdx3DModelBatch.bL0 clears ordinary battle viewports to opaque black.
                        // This viewport is composed through a texture here, so translucent sky
                        // materials must not punch holes in its alpha channel while blending.
                        boolean transparent = move != null && move.useBackground;
                        Gdx.gl.glColorMask(true, true, true, true);
                        Gdx.gl.glDepthMask(true);
                        Gdx.gl.glClearColor(0, 0, 0, transparent ? 0 : 1);
                        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
                        Gdx.gl.glColorMask(true, true, true, transparent);
                        animation.update(Math.min(.1f, Gdx.graphics.getDeltaTime()));
                        renderingMove = move;
                        batch.begin(camera);
                        if (move == null || !move.useBackground) batch.render(scenery.instance);
                        if (move == null || !move.hidePlatforms) {
                            batch.render(playerBase.instance);
                            batch.render(enemyBase.instance);
                        }
                        batch.end();
                        renderingMove = null;
                        drawCombatants.run();
                    } finally {
                        Gdx.gl.glColorMask(true, true, true, true);
                        buffer.end();
                        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
                        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
                        Gdx.gl.glDepthMask(true);
                    }
                });
        return image;
    }

    Vector3 foot(boolean own) {
        return camera.project(
                own ? new Vector3(3, 2.5f, 4) : new Vector3(5.25f, 2.5f, 1),
                0,
                0,
                camera.viewportWidth,
                camera.viewportHeight);
    }

    Vector3 project(Vector3 point) {
        return camera.project(point.cpy(), 0, 0, camera.viewportWidth, camera.viewportHeight);
    }

    PerspectiveCamera camera() {
        return camera;
    }

    @Override
    public void close() {
        if (buffer != null) buffer.dispose();
        if (batch != null) batch.dispose();
        if (scenery != null) scenery.close();
        if (playerBase != null) playerBase.close();
        if (enemyBase != null) enemyBase.close();
    }
}
