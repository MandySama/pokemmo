package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 蛇之惧（自定义动画注册 ID：1037）的攻击／事件阶段动画。
 *
 * <p>招式描述：降低对手的攻击, 防御, 特攻, 特防以及速度.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111037（名称）和 121037（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/GlareAnimation.java
 *
 * <p>源文件 SHA-256：fb305ee0263756fa1b1195ed8106c3f9ab868d1e57da4e2daf60f6706cb77be7
 */
final class ReferenceMoveProgram1037 {

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
