package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 轮唱（招式 ID：496）的攻击／事件阶段动画。
 *
 * <p>招式描述：用歌声攻击对手. 同伴还可以接着使出轮唱招式, 威力也会提高.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110496（名称）和 120496（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/RoundAnimation.java
 *
 * <p>源文件 SHA-256：0bd5082104492f21a8668ee0940d392a7a27ffd08efeb952f051165100d7c083
 */
final class ReferenceMoveProgram496 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(owner.tintEnvironment(2, Short.MAX_VALUE, 0.0f, 0.625f, 0.075f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1495,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1495,
                                        1,
                                        14,
                                        916.6667f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(10590);
        ReferenceMoveTimeline animationStep5 =
                animationStep3
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1523,
                                        2,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 2400.0f, 480.0f))
                        .onStart(owner.preloadParticleEffect(663))
                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.75f, color))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.emitParticles(-1, 663, 5, 9, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(10590);
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedOuterParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                ReferenceMoveTimeline
                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                                                                        .appendThenDelayedParallel(
                                                                                                                                                                                                                                animationStep5
                                                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                                        .emitParticles(
                                                                                                                                                                                                                                                                -1,
                                                                                                                                                                                                                                                                663,
                                                                                                                                                                                                                                                                6,
                                                                                                                                                                                                                                                                9,
                                                                                                                                                                                                                                                                8,
                                                                                                                                                                                                                                                                0.5f)),
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                                                663,
                                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                                                9,
                                                                                                                                                                                                                                                11,
                                                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                                                1800.0f),
                                                                                                                                                                                                                                0.12f),
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                                663,
                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                                9,
                                                                                                                                                                                                                                11,
                                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                                1800.0f),
                                                                                                                                                                                                                0.24f),
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                663,
                                                                                                                                                                                                                2,
                                                                                                                                                                                                                9,
                                                                                                                                                                                                                11,
                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                1800.0f),
                                                                                                                                                                                                0.36f),
                                                                                                                                                                                owner
                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                663,
                                                                                                                                                                                                2,
                                                                                                                                                                                                9,
                                                                                                                                                                                                11,
                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                1800.0f),
                                                                                                                                                                                0.48f),
                                                                                                                                                                owner
                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                663,
                                                                                                                                                                                2,
                                                                                                                                                                                9,
                                                                                                                                                                                11,
                                                                                                                                                                                0.5f,
                                                                                                                                                                                1800.0f),
                                                                                                                                                                0.6f),
                                                                                                                                                owner
                                                                                                                                                        .launchParticles(
                                                                                                                                                                663,
                                                                                                                                                                2,
                                                                                                                                                                9,
                                                                                                                                                                11,
                                                                                                                                                                0.5f,
                                                                                                                                                                1800.0f),
                                                                                                                                                0.62f),
                                                                                                                                owner
                                                                                                                                        .launchParticles(
                                                                                                                                                663,
                                                                                                                                                3,
                                                                                                                                                9,
                                                                                                                                                11,
                                                                                                                                                0.5f,
                                                                                                                                                1560.0f),
                                                                                                                                0.64f),
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                663,
                                                                                                                                3,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f,
                                                                                                                                1560.0f),
                                                                                                                0.66f),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                663,
                                                                                                                3,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                1560.0f),
                                                                                                0.68f),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                663,
                                                                                                3,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                1560.0f),
                                                                                0.7f),
                                                                owner.launchParticles(
                                                                        663, 3, 9, 11, 0.5f,
                                                                        1560.0f),
                                                                0.72f),
                                                owner.launchParticles(663, 3, 9, 11, 0.5f, 1560.0f),
                                                0.74f)
                                        .append(owner.launchParticles(663, 3, 9, 11, 0.5f, 1680.0f))
                                        .append(owner.focusCamera(1, 0.8f))
                                        .endGroup()
                                        .endGroup()
                                        .endGroup()
                                        .beginParallel(),
                                owner.tintCombatant(16, 0.75f, 0.75f, 0.0f, color2),
                                0.1f)
                        .append(owner.tintEnvironment(2, Short.MAX_VALUE, 0.625f, 0.0f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
