package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 烦恼种子（招式 ID：388）的攻击／事件阶段动画。
 *
 * <p>招式描述：种植心神不宁的种子. 使对手不能入眠, 并将特性变成不眠.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110388（名称）和 120388（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/WorrySeedAnimation.java
 *
 * <p>源文件 SHA-256：17e23f70ee73de3b0cafdaa52273d2ecf5ace43fc33afb0b2e705bfbc51cdde5
 */
final class ReferenceMoveProgram388 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1459,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1475,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1484,
                                                                        1,
                                                                        16,
                                                                        666.6667f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 656.0f, 640.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 640.0f, 800.0f))
                                                        .onStart(owner.preloadParticleEffect(563)),
                                                owner.launchParticles(563, 1, 9, 11, 0.5f, 360.0f),
                                                0.6f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(owner.emitParticles(-1, 563, 0, 11, 8, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.76f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
