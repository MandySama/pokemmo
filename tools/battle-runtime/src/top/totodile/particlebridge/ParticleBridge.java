package top.totodile.particlebridge;

import com.badlogic.gdx.graphics.g3d.particles.*;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatchExt;
import com.badlogic.gdx.graphics.g3d.particles.influencers.*;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRendererExt;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardControllerRenderData;
import cn.pokemmo.graphics.camera.PerspectiveCamera;
import f.*;
import java.util.*;

/** 隔离 CPU 控制器与宿主的 LibGDX 版本，只通过基本类型交换镜头和粒子通道。 */
public final class ParticleBridge {
    private final ParticleControllerExt controller;
    private final ParticleControllerExt trail;
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private final int source;
    private final int destination;
    private final boolean reverse;
    private boolean started;
    private final IdentityHashMap<ParticleControllerExt, ParticleSorter.Distance> sorters = new IdentityHashMap<>();

    public ParticleBridge(String json, int index, int source, int destination, float adjustment, boolean reverse) {
        this(json, index, source, destination, adjustment, reverse, -1, 0);
    }

    public ParticleBridge(String json, int index, int source, int destination, float adjustment, boolean reverse,
                          int position, float travelDuration) {
        this.source = source;
        this.destination = destination;
        this.reverse = reverse;
        context();
        char[] text = json.toCharArray();
        ResourceData data = (ResourceData) new gp_1().b20(ResourceData.class, null, new Y1().Gu0(text, text.length));
        ParticleEffect effect = (ParticleEffect) data.resource;
        controller = (ParticleControllerExt) ((ParticleController) effect.getControllers().get(index)).copy();
        int forceCount = 0;
        for (int i = 0; i < controller.influencers.KB; i++) {
            Influencer influence = (Influencer) controller.influencers.get(i);
            if (source >= 0 && influence instanceof ControllerSpawnInfluencer spawn) {
                if (position >= 0) {
                    spawn.setSpawnPosition(new C8(co_1.cOm6));
                    spawn.spawnType.setValue(5);
                } else spawn.spawnType.setValue(source == 3 ? 4 : source == 4 ? 3 : isEnemy(source) ? 1 : 0);
                spawn.spawnAdjustment.na(0, adjustment - .5f, 0);
            } else if (influence instanceof DynamicsInfluencerExt dynamics) {
                for (int j = 0; j < dynamics.velocities.KB; j++) {
                    var modifier = (DynamicsModifier) dynamics.velocities.get(j);
                    if (modifier instanceof DynamicsModifierExt.VectorPathModifier) travelDuration = 0;
                    if (modifierType(modifier, "DragForce") || modifierType(modifier, "CentripetalAccelerationExt")
                            || modifierType(modifier, "CircuralModifier")) forceCount++;
                }
            }
        }
        if (travelDuration > 0) {
            var path = new DynamicsModifierExt.VectorPathModifier();
            if (co_1.RC0 != ri_0.pN) {
                ((DynamicsModifierExt.VectorPathModifier.VectorEntry) path.vectorPath.get(0)).modifier = 2;
                ((DynamicsModifierExt.VectorPathModifier.VectorEntry) path.vectorPath.get(1)).modifier = 1;
            }
            path.strengthValue.setRelative(forceCount > 0);
            path.travelDuration.setActive(true);
            path.travelDuration.setValue(travelDuration);
            controller.influencers.Ue0(path);
        }
        configure(controller);
        if (controller.trailController >= 0) {
            trail = (ParticleControllerExt) ((ParticleController) effect.getControllers().get(controller.trailController)).copy();
            configure(trail);
            controller.updateTrailController(trail);
        } else trail = null;
    }

    private static boolean modifierType(DynamicsModifier modifier, String name) {
        for (Class<?> type = modifier.getClass(); type != null; type = type.getSuperclass())
            if (type.getName().equals("com.badlogic.gdx.graphics.g3d.particles.DynamicsModifierExt$" + name)) return true;
        return false;
    }

    private void configure(ParticleControllerExt value) {
        value.particleChannels = new ParticleChannels();
        BillboardParticleBatchExt batch = new BillboardParticleBatchExt(null, ParticleShaderExt.AlignMode.Screen, 0);
        batch.setCamera(camera);
        ((BillboardRendererExt) value.renderer).setBatch(batch);
        value.init();
    }

    private static boolean isEnemy(int target) { return target == 1 || target == 2 || target == 3 || target == 11; }

    private void context() {
        co_1.Kl0.x = reverse ? 5 : 3;
        co_1.Kl0.y = 3.08f;
        co_1.Kl0.z = reverse ? 1 : 4;
        co_1.cOm6.x = reverse ? 3 : 5;
        co_1.cOm6.y = 3.08f;
        co_1.cOm6.z = reverse ? 4 : 1;
        co_1.XZ.np(co_1.Kl0);
        co_1.OK.np(co_1.cOm6);
        co_1.Xh = isEnemy(source) ? ri_0.pN : ri_0.xQ;
        co_1.RC0 = destination == 8 ? ri_0.w8 : destination == 9 ? ri_0.xQ : ri_0.pN;
    }

    public void camera(float[] combined, float[] view, int width, int height) {
        System.arraycopy(combined, 0, camera.iJ.EW, 0, 16);
        System.arraycopy(view, 0, camera.bq.EW, 0, 16);
        if (lg_0.S4 == null) lg_0.S4 = new k3_0();
        lg_0.S4.width = width;
        lg_0.S4.height = height;
    }

    public void update(float delta) {
        context();
        if (!started) {
            controller.start();
            if (trail != null) trail.start();
            started = true;
        }
        controller.update(delta);
        if (trail != null) trail.update(delta);
    }

    /** 每层：位置3、纹理区域6、颜色4、XY缩放2、旋转2，共17个浮点值。 */
    public float[][] particles() {
        return trail == null ? new float[][]{channels(controller)} : new float[][]{channels(trail), channels(controller)};
    }

    private float[] channels(ParticleControllerExt value) {
        var particles = value.particles;
        if (particles.size == 0) return new float[0];
        var position = (ParallelArray.FloatChannel) particles.getChannel(ParticleChannels.Position);
        var region = (ParallelArray.FloatChannel) particles.getChannel(ParticleChannels.TextureRegion);
        var color = (ParallelArray.FloatChannel) particles.getChannel(ParticleChannels.Color);
        var scale = (ParallelArray.FloatChannel) particles.getChannel(ParticleChannelsExt.ScaleXY);
        var rotation = (ParallelArray.FloatChannel) particles.getChannel(ParticleChannels.Rotation2D);
        var sorter = sorters.computeIfAbsent(value, ignored -> new ParticleSorter.Distance());
        sorter.setCamera(camera);
        sorter.ensureCapacity(particles.size);
        var renderData = new BillboardControllerRenderData();
        renderData.controller = value;
        renderData.positionChannel = position;
        var batch = new es_1(new BillboardControllerRenderData[]{renderData});
        int[] order = sorter.sort(batch);
        float[] result = new float[particles.size * 17];
        for (int i = 0; i < particles.size; i++) {
            int offset = order[i] * 17;
            System.arraycopy(position.data, i * position.strideSize, result, offset, 3);
            System.arraycopy(region.data, i * region.strideSize, result, offset + 3, 6);
            System.arraycopy(color.data, i * color.strideSize, result, offset + 9, 4);
            System.arraycopy(scale.data, i * scale.strideSize, result, offset + 13, 2);
            System.arraycopy(rotation.data, i * rotation.strideSize, result, offset + 15, 2);
        }
        return result;
    }

    public void close() {
        controller.end();
        controller.dispose();
        if (trail != null) { trail.end(); trail.dispose(); }
        sorters.clear();
    }
}
