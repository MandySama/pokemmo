package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 模仿（招式 ID：102）的攻击／事件阶段动画。
 *
 * <p>招式描述：可以将对手最后使用的招式, 在战斗内变成自己的招式.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110102（名称）和 120102（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/MimicAnimation.java
 *
 * <p>源文件 SHA-256：1c165865969c62f8c6ad53a9da259f8a6c2c20eb3765fff858680aa30ef7e35b
 */
final class ReferenceMoveProgram102 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .onStart(owner.preloadParticleEffect(269))
                                                        .append(
                                                                owner.launchParticles(
                                                                        269, 1, 11, 9, 0.5f,
                                                                        480.0f)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1466,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle),
                                                0.32f)
                                        .append(owner.focusCamera(0, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 269, 0, 9, 8, 0.5f)),
                                owner.scheduleSound(
                                        (byte) 2, 1465, 2, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
