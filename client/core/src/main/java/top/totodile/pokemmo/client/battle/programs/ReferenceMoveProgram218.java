package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 迁怒（招式 ID：218）的攻击／事件阶段动画。
 *
 * <p>招式描述：为了发泄不满而全力攻击对手. 亲密度越低, 威力越大.
 *
 * <p>动画说明：播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放。
 *
 * <p>编号对应参考动画注册表；语言条目为 110218（名称）和 120218（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/FrustrationAnimation.java
 *
 * <p>源文件 SHA-256：f1921f4751f296201fd5173b54f442a374cc38a33f710967e74b4ac26078b035
 */
final class ReferenceMoveProgram218 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(owner.tintCombatant(14, 0.75f, 0.0f, 0.75f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1463,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        1,
                                        14,
                                        166.66667f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 160.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1463,
                                        1,
                                        14,
                                        666.6667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 1, 14, 833.3333f, 0.0f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 640.0f, 160.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1440,
                                        0,
                                        14,
                                        0.0f,
                                        0.859375f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(31);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep6,
                                owner.scheduleSound(
                                        (byte) 2,
                                        1440,
                                        0,
                                        14,
                                        666.6667f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(owner.tintCombatant(14, 0.75f, 0.75f, 0.0f, color2))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .append(owner.shakeCombatant(14, 2, 4, 0.016f, 0.032f, 1.0f, 0.5f))
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 2, 0.032f, 0.064f, 0.19995117f, -0.19995117f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1489,
                                        1,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1489,
                                        2,
                                        16,
                                        166.66667f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1482,
                                        1,
                                        16,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(385))
                        .append(owner.emitParticles(-1, 385, 0, 11, 8, 0.4f))
                        .append(owner.emitParticles(-1, 385, 1, 11, 8, 0.4f))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.032f, 0.064f, -0.19995117f, 0.19995117f))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
