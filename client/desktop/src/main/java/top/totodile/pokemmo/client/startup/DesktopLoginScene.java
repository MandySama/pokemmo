package top.totodile.pokemmo.client.startup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.ScreenUtils;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.ui.startup.LoginScene;

/** 参考 RenderTargetResource.mT 与 Abstract3DCutsceneScene 17/18：随机播放黑白标题场景。 */
public final class DesktopLoginScene implements LoginScene {
    private final ClientResources resources;
    private final Path cache;
    private final int variant;
    private final List<SceneAsset> assets=new ArrayList<>();
    private final List<Scene> scenes=new ArrayList<>();
    private final PerspectiveCamera camera=new PerspectiveCamera(80,1024,768);
    private RomInfo rom;
    private CompletableFuture<RomLoginAssets.Prepared> loading;
    private RomCameraTrack track;
    private SceneManager renderer;
    private RomLoginMusic music;
    private RomTexturePattern pattern;
    private float time;
    private boolean focused=true, closed;

    public DesktopLoginScene(ClientResources resources,Path cache) {
        this(resources,cache,ThreadLocalRandom.current().nextBoolean()?0:8);
    }
    DesktopLoginScene(ClientResources resources,Path cache,int variant) {
        this.resources=resources; this.cache=cache; this.variant=variant;
        camera.near=0.64f; camera.far=12800;
    }
    @Override public boolean draw(float delta,int width,int height) {
        if (closed) return false;
        RomInfo selected=resources.roms().get(RomType.BLACK_WHITE);
        if (selected!=rom) {
            release(); rom=selected;
            if (rom!=null) {
                RomInfo source=rom;
                loading=CompletableFuture.supplyAsync(() -> {
                    try { return RomLoginAssets.prepare(source,cache,variant); }
                    catch (Exception error) { throw new CompletionException(error); }
                });
                music=new RomLoginMusic(rom,cache,resources.preferences()); music.setFocused(focused);
            }
        }
        if (loading!=null && loading.isDone()) {
            var pending=loading; loading=null;
            try { install(pending.join()); }
            catch (Exception error) { releaseModels(); Gdx.app.error("Login scene","Unable to load ROM login scene",error); }
        }
        if (renderer==null) return false;
        float step=Math.max(0,Math.min(delta,0.25f));
        time+=step;
        if (time*30>=track.frames) {
            time=0; resetAnimation();
            // 镜头和模型一起回到第 0 帧，不能再次把本帧 delta 加到模型上。
            step=0;
        }
        camera.viewportWidth=width; camera.viewportHeight=height;
        track.apply(camera,time);
        Gdx.gl.glDepthMask(true);
        ScreenUtils.clear(1,1,1,1,true);
        // 镜头、ROM 骨骼和纹理都使用参考项目的 30 FPS 时间轴。
        renderer.update(step);
        if(pattern!=null) pattern.update(time);
        renderer.render();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        return true;
    }
    private void install(RomLoginAssets.Prepared prepared) throws Exception {
        track=new RomCameraTrack(prepared.camera());
        for (int i=0;i<prepared.models().length;i++) {
            Path model=prepared.models()[i];
            SceneAsset asset=new GLTFLoader().load(new FileHandle(model.toFile())); assets.add(asset);
            Scene scene=new Scene(asset.scene);
            scene.animationController=new com.badlogic.gdx.graphics.g3d.utils.AnimationController(scene.modelInstance);
            scene.modelInstance.animations.clear();
            scene.modelInstance.animations.add(RomModelAnimation.read(prepared.modelData()[i],prepared.animationData()[i],scene.modelInstance));
            for(var material:scene.modelInstance.materials) {
                int polygon=prepared.materials().get(i).getOrDefault(material.id,0x1f0080);
                int faces=switch(polygon>>>6&3) { case 0 -> GL20.GL_FRONT_AND_BACK; case 1 -> GL20.GL_FRONT; case 2 -> GL20.GL_BACK; default -> GL20.GL_NONE; };
                material.set(IntAttribute.createCullFace(faces));
                boolean blend=material.has(BlendingAttribute.Type);
                // MonsterStatsRomChunk.vD / NdsNitroModelParser.Xy：位 4 控制
                // 深度比较，参考模型材质（包含倒影及透明光效）均写入深度。
                material.set(new DepthTestAttribute((polygon&(1<<4))!=0?GL20.GL_EQUAL:GL20.GL_LESS,true));
                if(blend) material.set(FloatAttribute.createAlphaTest(0.01f));
            }
            scenes.add(scene);
        }
        int bones=assets.stream().mapToInt(asset -> asset.maxBones).max().orElse(0);
        renderer=new SceneManager(Math.max(1,bones));
        renderer.setCamera(camera);
        for (Scene scene:scenes) renderer.addScene(scene);
        pattern=new RomTexturePattern(prepared.pattern(),prepared.models()[0].getParent(),scenes.get(0));
        resetAnimation(); time=0;
    }
    private void resetAnimation() {
        for (Scene scene:scenes) if (scene.modelInstance.animations.size>0)
            scene.animationController.setAnimation(scene.modelInstance.animations.first().id,1).time=0;
    }
    @Override public void setFocused(boolean focused) {
        this.focused=focused;
        if (music!=null) music.setFocused(focused);
    }
    private void releaseModels() {
        if(pattern!=null) { pattern.close(); pattern=null; }
        if (renderer!=null) { renderer.dispose(); renderer=null; }
        for (SceneAsset asset:assets) asset.dispose();
        assets.clear(); scenes.clear(); track=null;
    }
    private void release() {
        if (music!=null) { music.close(); music=null; }
        loading=null; releaseModels();
    }
    @Override public void close() { closed=true; release(); }
}
