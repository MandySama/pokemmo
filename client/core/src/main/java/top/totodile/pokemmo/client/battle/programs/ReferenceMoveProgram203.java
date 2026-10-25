package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 挺住（招式 ID：203）的攻击／事件阶段动画。
 *
 * <p>招式描述：即使受到攻击, 也至少会留下1HP. 连续使出则容易失败.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110203（名称）和 120203（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/EndureAnimation.java
 *
 * <p>源文件 SHA-256：4dbbf6b0ee8925a9536edb3abaceb4ed78a8864867d1ab78309557eaad59894c
 */
final class ReferenceMoveProgram203 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1418,
                                        1,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        166.66667f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1418,
                                        1,
                                        14,
                                        200.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        366.66666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1418,
                                        1,
                                        14,
                                        400.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        566.6667f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1418,
                                        1,
                                        14,
                                        600.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        766.6667f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1418,
                                        1,
                                        14,
                                        800.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        966.6667f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1418,
                                        1,
                                        14,
                                        1000.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1166.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 320.0f, 1600.0f))
                        .onStart(owner.preloadParticleEffect(369));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep14 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep13
                                .append(owner.emitParticles(-1, 369, 0, 9, 8, 0.5f))
                                .append(owner.setSpriteAnimationPaused(14, 1))
                                .beginSequence()
                                .waitSeconds(0.2f)
                                .beginParallel()
                                .append(owner.tintCombatant(14, 1.0f, 0.0f, 0.625f, color))
                                .append(
                                        owner.shakeCombatant(
                                                14, 2, 4, 0.016f, 0.016f, 0.19995117f, 0.0f)),
                        owner.scaleCombatant(14, 2, 1, 0.016f, 0.16f, 0.100097656f, -0.100097656f));
        color = EncounterMoveAnimation.color(31);
        result =
                animationStep14
                        .append(owner.tintCombatant(14, 1.0f, 0.625f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
