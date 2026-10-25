package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 泥巴射击（招式 ID：341）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手投掷泥块进行攻击. 同时降低对手的速度.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110341（名称）和 120341（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/MudShotAnimation.java
 *
 * <p>源文件 SHA-256：25c3d51926d3627ff1a9ccb1fc93c8fa2759e9b879889484aec9b760b5eec84b
 */
final class ReferenceMoveProgram341 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1455, 1, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1407, 2, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        2,
                                        14,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 320.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1452, 1, 16, 500.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1452,
                                        1,
                                        16,
                                        666.6667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1452,
                                        1,
                                        16,
                                        833.3333f,
                                        0.9375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedOuterParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .appendThenDelayedParallel(
                                                                                                                animationStep7
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .scheduleSound(
                                                                                                                                                (byte)
                                                                                                                                                        2,
                                                                                                                                                1452,
                                                                                                                                                1,
                                                                                                                                                16,
                                                                                                                                                1000.0f,
                                                                                                                                                0.9375f,
                                                                                                                                                owner.casterHandle))
                                                                                                                        .onStart(
                                                                                                                                owner
                                                                                                                                        .preloadParticleEffect(
                                                                                                                                                513))
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .launchParticles(
                                                                                                                                                513,
                                                                                                                                                0,
                                                                                                                                                9,
                                                                                                                                                11,
                                                                                                                                                0.5f,
                                                                                                                                                0.0f))
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .launchParticles(
                                                                                                                                                513,
                                                                                                                                                1,
                                                                                                                                                9,
                                                                                                                                                11,
                                                                                                                                                0.5f,
                                                                                                                                                0.0f)),
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                513,
                                                                                                                                2,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f,
                                                                                                                                240.0f),
                                                                                                                0.1f),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                513,
                                                                                                                2,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                240.0f),
                                                                                                0.2f)
                                                                                        .append(
                                                                                                owner
                                                                                                        .focusCamera(
                                                                                                                1,
                                                                                                                0.25f)),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                513,
                                                                                                2,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                240.0f),
                                                                                0.3f),
                                                                owner.launchParticles(
                                                                        513, 2, 9, 11, 0.5f,
                                                                        240.0f),
                                                                0.4f)
                                                        .append(
                                                                owner.launchParticles(
                                                                        513, 2, 9, 11, 0.5f,
                                                                        240.0f))
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        16,
                                                                        2,
                                                                        10,
                                                                        0.016f,
                                                                        0.016f,
                                                                        0.19995117f,
                                                                        0.0f)),
                                                owner.tintCombatant(
                                                        16, 0.25f, 0.0f, 0.8125f, color),
                                                0.5f),
                                        owner.launchParticles(513, 2, 9, 11, 0.5f, 240.0f),
                                        0.6f)
                                .append(owner.setSpriteAnimationPaused(16, 1)),
                        owner.launchParticles(513, 2, 9, 11, 0.5f, 240.0f),
                        0.8f);
        color = EncounterMoveAnimation.color(0);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep8.append(
                                        owner.tintCombatant(16, 0.25f, 0.8125f, 0.0f, color)),
                                owner.emitParticles(-1, 513, 2, 11, 8, 0.5f))
                        .append(owner.restoreCamera(0.25f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
