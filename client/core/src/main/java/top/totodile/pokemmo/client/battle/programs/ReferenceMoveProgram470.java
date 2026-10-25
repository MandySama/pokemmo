package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 防守平分（招式 ID：470）的攻击／事件阶段动画。
 *
 * <p>招式描述：利用超能力将自己和对手的防御和特防相加, 再进行平分.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110470（名称）和 120470（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/GuardSplitAnimation.java
 *
 * <p>源文件 SHA-256：6b0f1df388541f229d69f9df733d15412d59f638aab6c38b639e7e2933860add
 */
final class ReferenceMoveProgram470 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .sequence()
                                                                                                        .beginParallel()
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .focusCamera(
                                                                                                                                0,
                                                                                                                                0.25f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .setSpriteAnimationPaused(
                                                                                                                                16,
                                                                                                                                1))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .tintEnvironment(
                                                                                                                                2,
                                                                                                                                0,
                                                                                                                                0.0f,
                                                                                                                                0.625f,
                                                                                                                                0.075f))
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .preloadParticleEffect(
                                                                                                                                560))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                560,
                                                                                                                                0,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f,
                                                                                                                                360.0f)),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                560,
                                                                                                                0,
                                                                                                                11,
                                                                                                                9,
                                                                                                                0.5f,
                                                                                                                360.0f),
                                                                                                0.2f)
                                                                                        .append(
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                560,
                                                                                                                0,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                360.0f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1884,
                                                                                                                1,
                                                                                                                14,
                                                                                                                0.0f,
                                                                                                                0.78125f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .adjustSoundTrack(
                                                                                                                1,
                                                                                                                1,
                                                                                                                1280.0f,
                                                                                                                320.0f)),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                560,
                                                                                                0,
                                                                                                11,
                                                                                                9,
                                                                                                0.5f,
                                                                                                360.0f),
                                                                                0.4f)
                                                                        .append(
                                                                                owner
                                                                                        .launchParticles(
                                                                                                560,
                                                                                                0,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                360.0f))
                                                                        .append(
                                                                                owner
                                                                                        .launchParticles(
                                                                                                560,
                                                                                                0,
                                                                                                11,
                                                                                                9,
                                                                                                0.5f,
                                                                                                360.0f))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.3f))
                                                                        .endGroup()
                                                                        .endGroup()
                                                                        .beginSequence()
                                                                        .waitSeconds(0.6f)
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner
                                                                                        .launchParticles(
                                                                                                560,
                                                                                                0,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                360.0f)),
                                                                owner.launchParticles(
                                                                        560, 0, 11, 9, 0.5f,
                                                                        360.0f),
                                                                0.8f)
                                                        .append(
                                                                owner.launchParticles(
                                                                        560, 0, 9, 11, 0.5f,
                                                                        360.0f)),
                                                owner.launchParticles(560, 0, 11, 9, 0.5f, 360.0f),
                                                1.0f)
                                        .append(owner.launchParticles(560, 0, 9, 11, 0.5f, 360.0f)),
                                owner.launchParticles(560, 0, 11, 9, 0.5f, 360.0f),
                                1.2f)
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.032f, 0.100097656f, -0.100097656f))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(2, 0, 0.625f, 0.0f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
