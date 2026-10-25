package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 怪力（招式 ID：70）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出浑身力气殴打对手进行攻击.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110070（名称）和 120070（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/StrengthAnimation.java
 *
 * <p>源文件 SHA-256：e6aca14b6dcceabe6b551c884f651b0f0af82870594c4ff10ea706e27c18a68a
 */
final class ReferenceMoveProgram70 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(28);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenDelayedParallel(
                                                                        ReferenceMoveTimeline
                                                                                .sequence()
                                                                                .beginParallel()
                                                                                .append(
                                                                                        owner
                                                                                                .focusCamera(
                                                                                                        0,
                                                                                                        0.4f))
                                                                                .append(
                                                                                        owner
                                                                                                .setSpriteAnimationPaused(
                                                                                                        14,
                                                                                                        1))
                                                                                .append(
                                                                                        owner
                                                                                                .tintCombatant(
                                                                                                        14,
                                                                                                        1.25f,
                                                                                                        0.75f,
                                                                                                        0.0f,
                                                                                                        color))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1474,
                                                                                                        1,
                                                                                                        14,
                                                                                                        0.0f,
                                                                                                        0.9375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .adjustSoundTrack(
                                                                                                        1,
                                                                                                        0,
                                                                                                        0.0f,
                                                                                                        320.0f))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1475,
                                                                                                        1,
                                                                                                        16,
                                                                                                        1000.0f,
                                                                                                        0.9375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1475,
                                                                                                        2,
                                                                                                        16,
                                                                                                        1166.6666f,
                                                                                                        0.9375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1475,
                                                                                                        1,
                                                                                                        16,
                                                                                                        1333.3334f,
                                                                                                        0.9375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1475,
                                                                                                        2,
                                                                                                        16,
                                                                                                        1500.0f,
                                                                                                        0.9375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .shakeCombatant(
                                                                                                        14,
                                                                                                        2,
                                                                                                        4,
                                                                                                        0.032f,
                                                                                                        0.016f,
                                                                                                        0.0f,
                                                                                                        0.19995117f)),
                                                                        owner.scaleCombatant(
                                                                                14,
                                                                                1,
                                                                                1,
                                                                                0.016f,
                                                                                0.192f,
                                                                                0.8000488f,
                                                                                0.8000488f),
                                                                        0.88f)
                                                                .append(
                                                                        owner.scaleCombatant(
                                                                                14, 1, 1, 0.016f,
                                                                                0.032f, 1.0f,
                                                                                1.0f)),
                                                        owner.shakeCombatant(
                                                                14,
                                                                2,
                                                                2,
                                                                0.032f,
                                                                0.016f,
                                                                0.0f,
                                                                0.19995117f),
                                                        0.96f)
                                                .append(owner.focusCamera(1, 0.4f))
                                                .onStart(owner.preloadParticleEffect(231))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 231, 0, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 231, 1, 11, 8, 0.5f),
                                        1.16f),
                                owner.shakeCombatant(16, 3, 3, 0.032f, 0.016f, 1.0f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
