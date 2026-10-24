package top.totodile.pokemmo.client.startup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.ScreenUtils;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import top.totodile.pokemmo.client.rom.nds.model.NitroModelRenderer;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.ui.startup.LoginScene;

/** 参考 RenderTargetResource.mT 与 Abstract3DCutsceneScene 17/18：随机播放黑白标题场景。 */
public final class DesktopLoginScene implements LoginScene {
    private final ClientResources resources;
    private final Path cache;
    private final int variant;
    private final RomInfo startupRom;
    private final List<NitroModelRenderer> scenes=new ArrayList<>();
    private final List<AnimationController> animations=new ArrayList<>();
    private final PerspectiveCamera camera=new PerspectiveCamera(80,1024,768);
    private RomInfo rom;
    private CompletableFuture<RomLoginAssets.Prepared> loading;
    private RomCameraTrack track;
    private ModelBatch renderer;
    // UiThemeSkinResource.IZ：白色环境光，材质中的环境色可覆盖此值。
    private final Environment environment=new Environment();
    private RomLoginMusic music;
    private RomTexturePattern pattern;
    private float time;
    private boolean warming, starting;
    private boolean focused=true, closed;

    public DesktopLoginScene(ClientResources resources,Path cache) {
        this(resources,cache,ThreadLocalRandom.current().nextBoolean()?0:8);
    }
    DesktopLoginScene(ClientResources resources,Path cache,int variant) {
        this.resources=resources; this.cache=cache; this.variant=variant;
        // 本次启动的场景固定使用启动时的 ROM，新导入内容重启后才启用。
        startupRom=resources.roms().get(RomType.BLACK_WHITE);
        camera.near=0.01f; camera.far=200;
        environment.set(ColorAttribute.createAmbientLight(1,1,1,1));
    }
    @Override public boolean draw(float delta,int width,int height) {
        if (closed) return false;
        RomInfo selected=startupRom;
        if (selected!=rom) {
            release(); rom=selected;
            if (rom!=null) {
                RomInfo source=rom;
                loading=CompletableFuture.supplyAsync(() -> {
                    try { return RomLoginAssets.prepare(source,variant); }
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
        // 参考场景按实际帧间隔推进；截断为 0.25 秒会在卡顿后丢失镜头和动画时间。
        // 加载与首次 GL 绘制不属于播放时间；正常播放仍保留完整 delta。
        float step=warming || starting?0:Math.max(0,delta);
        starting=false;
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
        for(var animation:animations)animation.update(step);
        if(pattern!=null)try { pattern.update(time); }
        catch(java.io.IOException error) { pattern.close(); pattern=null; Gdx.app.error("Login scene","Unable to update ROM texture animation",error); }
        renderer.begin(camera);
        for(var scene:scenes)renderer.render(scene.instance,environment);
        renderer.end();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        if(warming) {
            // 先完成着色器编译和首次上传，下一帧才把场景交给前景 UI。
            warming=false; starting=true;
            ScreenUtils.clear(0,0,0,1,true);
            return false;
        }
        return true;
    }
    private void install(RomLoginAssets.Prepared prepared) throws Exception {
        track=new RomCameraTrack(prepared.camera());
        for (int i=0;i<prepared.models().length;i++) {
            NitroModelRenderer scene=new NitroModelRenderer(prepared.models()[i]); scenes.add(scene);
            var animation=prepared.animations()[i];
            for(int channel=animation.nodeAnimations.size-1;channel>=0;channel--) {
                var entry=animation.nodeAnimations.get(channel);
                entry.node=scene.instance.getNode(entry.node.id,true);
                if(entry.node==null)animation.nodeAnimations.removeIndex(channel);
            }
            scene.instance.animations.add(animation);
            animations.add(new AnimationController(scene.instance));
        }
        var config=new DefaultShader.Config(); config.numBones=Math.max(1,scenes.stream().mapToInt(NitroModelRenderer::maxBones).max().orElse(0));
        renderer=new ModelBatch(new DefaultShaderProvider(config));
        pattern=new RomTexturePattern(prepared.pattern(),scenes.get(0));
        resetAnimation(); time=0; warming=true; starting=false;
    }
    private void resetAnimation() {
        for(int i=0;i<scenes.size();i++)
            animations.get(i).setAnimation(scenes.get(i).instance.animations.first().id,1).time=0;
    }
    @Override public void setFocused(boolean focused) {
        this.focused=focused;
        if (music!=null) music.setFocused(focused);
    }
    private void releaseModels() {
        if(pattern!=null) { pattern.close(); pattern=null; }
        if (renderer!=null) { renderer.dispose(); renderer=null; }
        scenes.forEach(NitroModelRenderer::close);
        scenes.clear(); animations.clear(); track=null; warming=false; starting=false;
    }
    private void release() {
        if (music!=null) { music.close(); music=null; }
        loading=null; releaseModels();
    }
    @Override public void close() { closed=true; release(); }
}
