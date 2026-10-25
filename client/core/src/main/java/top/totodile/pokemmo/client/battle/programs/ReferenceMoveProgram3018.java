package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 吹飞☆（自定义动画注册 ID：3018）的攻击／事件阶段动画。
 *
 * <p>招式描述：吹飞对手, 强制拉后备宝可梦上场. 如果对手为野生宝可梦, 战斗将直接结束.
 *
 * <p>动画说明：镜头聚焦场地位置；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵显隐；控制精灵帧动画；恢复默认镜头。
 *
 * <p>变体映射：基础招式 ID 为 18；按参考本地化规则沿用基础描述，并在名称末尾添加 ☆。
 *
 * <p>编号对应参考动画注册表；语言条目为 110018（名称）和 120018（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove3018Animation.java
 *
 * <p>源文件 SHA-256：6235ab27e8437b5d2fd16a1b971d159dd021af88d158227c67ded3442573031a
 */
final class ReferenceMoveProgram3018 {

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
                                                                                                                                3018))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .emitParticles(
                                                                                                                                -1,
                                                                                                                                3018,
                                                                                                                                0,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f)),
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                3018,
                                                                                                                0,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f),
                                                                                                0.4f)
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                3018,
                                                                                                                2,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f)),
                                                                                owner.emitParticles(
                                                                                        -1, 3018, 2,
                                                                                        9, 11,
                                                                                        0.5f),
                                                                                0.6f)
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 3018, 1,
                                                                                        9, 11,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 3018, 1,
                                                                                        9, 11,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 3018, 3,
                                                                                        9, 11,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 3018, 3,
                                                                                        9, 11,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        3, 0.4f)),
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
