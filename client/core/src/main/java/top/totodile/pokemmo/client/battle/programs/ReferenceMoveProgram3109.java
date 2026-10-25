package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 奇异之光☆（自定义动画注册 ID：3109）的攻击／事件阶段动画。
 *
 * <p>招式描述：显示奇怪的光, 扰乱对手. 使对手混乱.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>变体映射：基础招式 ID 为 109；按参考本地化规则沿用基础描述，并在名称末尾添加 ☆。
 *
 * <p>编号对应参考动画注册表；语言条目为 110109（名称）和 120109（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove3109Animation.java
 *
 * <p>源文件 SHA-256：49a212667f2ca07cb9efd66003953f672513cc18845765a9889a99dc1778111d
 */
final class ReferenceMoveProgram3109 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .waitSeconds(0.6f),
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.75f, 0.075f))
                                                .onStart(owner.preloadParticleEffect(3109))
                                                .append(
                                                        owner.launchParticles(
                                                                3109, 1, 9, 11, 0.5f, 1440.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1843,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.78125f,
                                                                owner.casterHandle)),
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1848,
                                                2,
                                                14,
                                                250.0f,
                                                0.46875f,
                                                owner.casterHandle),
                                        0.6f)
                                .append(owner.focusCamera(1, 0.8f))
                                .endGroup()
                                .endGroup()
                                .beginSequence()
                                .waitSeconds(1.2f)
                                .beginParallel()
                                .append(owner.emitParticles(-1, 3109, 0, 11, 8, 0.5f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1713,
                                                1,
                                                16,
                                                666.6667f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1350,
                                                1,
                                                16,
                                                2166.6667f,
                                                0.0f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 1, 1280.0f, 800.0f))
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .endGroup()
                                .endGroup()
                                .endGroup()
                                .beginParallel()
                                .append(owner.restoreCamera(0.4f))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.075f)),
                        owner.scaleCombatant(
                                16, 2, 1, 0.032f, 0.032f, 0.100097656f, -0.100097656f));
        return result;
    }
}
