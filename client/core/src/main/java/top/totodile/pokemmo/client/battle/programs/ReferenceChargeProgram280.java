package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 劈瓦（招式 ID：280）的蓄力阶段动画。
 *
 * <p>招式描述：将手刀猛烈地挥下攻击对手. 还可以破坏光墙和反射壁等.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110280（名称）和 120280（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/BrickBreakAnimation.java
 *
 * <p>源文件 SHA-256：acfceac5df8246f166e4669fc8c3b2a2ec8e2acbd0e17d00a60314b0923b6866
 */
final class ReferenceChargeProgram280 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1476,
                                                                        2,
                                                                        16,
                                                                        250.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1421,
                                                                        1,
                                                                        16,
                                                                        1000.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(446))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 446, 1, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 446, 2, 11, 8, 0.5f),
                                                1.2f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1424,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2, 1420, 2, 16, 0.0f, 0.78125f, owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
