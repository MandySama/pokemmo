package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 吹飞（招式 ID：18）的攻击／事件阶段动画。
 *
 * <p>招式描述：吹飞对手, 强制拉后备宝可梦上场. 如果对手为野生宝可梦, 战斗将直接结束.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵显隐；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110018（名称）和 120018（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/WhirlwindAnimation.java
 *
 * <p>源文件 SHA-256：b57f35b9c8ef065e69dee577d8e6973beb14f46f20c2c4fd095ae461197d03f8
 */
final class ReferenceMoveProgram18 {

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
                                                                                                        .waitThenParallel(
                                                                                                                0.6f)
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                1439,
                                                                                                                                1,
                                                                                                                                16,
                                                                                                                                0.0f,
                                                                                                                                0.78125f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                1717,
                                                                                                                                2,
                                                                                                                                16,
                                                                                                                                0.0f,
                                                                                                                                0.78125f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                1376,
                                                                                                                                2,
                                                                                                                                16,
                                                                                                                                1666.6666f,
                                                                                                                                0.0f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .adjustSoundTrack(
                                                                                                                                2,
                                                                                                                                0,
                                                                                                                                0.0f,
                                                                                                                                320.0f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .adjustSoundTrack(
                                                                                                                                2,
                                                                                                                                0,
                                                                                                                                640.0f,
                                                                                                                                320.0f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .adjustSoundTrack(
                                                                                                                                2,
                                                                                                                                1,
                                                                                                                                0.0f,
                                                                                                                                480.0f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .adjustSoundTrack(
                                                                                                                                2,
                                                                                                                                1,
                                                                                                                                800.0f,
                                                                                                                                640.0f))
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .preloadParticleEffect(
                                                                                                                                179))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .emitParticles(
                                                                                                                                -1,
                                                                                                                                179,
                                                                                                                                0,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f)),
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                179,
                                                                                                                0,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f),
                                                                                                0.4f)
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                179,
                                                                                                                2,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f)),
                                                                                owner.emitParticles(
                                                                                        -1, 179, 2,
                                                                                        9, 11,
                                                                                        0.5f),
                                                                                0.6f)
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 179, 1,
                                                                                        9, 11,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 179, 1,
                                                                                        9, 11,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 179, 3,
                                                                                        9, 11,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 179, 3,
                                                                                        9, 11,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f)),
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1440,
                                                                        1,
                                                                        16,
                                                                        833.3333f,
                                                                        0.9765625f,
                                                                        owner.casterHandle),
                                                                1.1f)
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        1,
                                                                        0.016f,
                                                                        0.048f,
                                                                        0.30004883f,
                                                                        -0.30004883f)),
                                                owner.shakeCombatant(
                                                        16, 2, 6, 0.0f, 0.032f, 0.30004883f, 0.0f),
                                                1.5f)
                                        .onStart(owner.setHealthHudHidden(16, true)),
                                owner.shakeCombatant(16, 1, 1, 0.0f, 0.48f, -20.0f, 10.0f),
                                1.9f)
                        .append(owner.setCombatantVisibility(16, 3))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
