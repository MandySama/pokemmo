package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 大蛇瞪眼（招式 ID：137）的攻击／事件阶段动画。
 *
 * <p>招式描述：用腹部的花纹使对手害怕, 从而让其陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110137（名称）和 120137（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/GlareAnimation.java
 *
 * <p>源文件 SHA-256：fb305ee0263756fa1b1195ed8106c3f9ab868d1e57da4e2daf60f6706cb77be7
 */
final class ReferenceMoveProgram137 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(0, 0.4f))
                                                .append(
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.75f, 0.05f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1474,
                                                                1,
                                                                14,
                                                                250.0f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1509,
                                                                2,
                                                                14,
                                                                666.6667f,
                                                                0.78125f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                1, 0, 240.0f, 800.0f))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .onStart(owner.preloadParticleEffect(302)),
                                        owner.emitParticles(-1, 302, 0, 9, 8, 0.5f),
                                        0.24f),
                                owner.scaleCombatant(14, 2, 1, 0.016f, 0.224f, 0.0f, 0.30004883f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
