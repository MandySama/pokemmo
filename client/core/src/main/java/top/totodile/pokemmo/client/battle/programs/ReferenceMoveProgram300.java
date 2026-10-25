package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 玩泥巴（招式 ID：300）的攻击／事件阶段动画。
 *
 * <p>招式描述：一旦使用此招式, 周围就会弄得到处是泥. 在5回合内减弱电属性的招式.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110300（名称）和 120300（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/MudSportAnimation.java
 *
 * <p>源文件 SHA-256：60d798750be4289e7ad2d24d0d21444d6d399af9d5bb934a3bd501d595ffc4ec
 */
final class ReferenceMoveProgram300 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1871,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1479,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.234375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1871,
                                                                        1,
                                                                        14,
                                                                        500.0f,
                                                                        0.625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1479,
                                                                        2,
                                                                        14,
                                                                        500.0f,
                                                                        0.234375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1479,
                                                                        1,
                                                                        14,
                                                                        833.3333f,
                                                                        0.546875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        2,
                                                                        3,
                                                                        0.032f,
                                                                        0.064f,
                                                                        -0.30004883f,
                                                                        0.30004883f))
                                                        .onStart(owner.preloadParticleEffect(466))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 466, 1, 9, 8, 0.4f)),
                                                owner.emitParticles(-1, 466, 2, 9, 8, 0.4f))
                                        .append(owner.focusCamera(0, 0.6f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1821,
                                                        2,
                                                        16,
                                                        83.333336f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1479,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1821,
                                                        2,
                                                        16,
                                                        250.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 400.0f, 160.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 400.0f, 160.0f))
                                        .append(owner.emitParticles(-1, 466, 0, 11, 8, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 1, 1, 0.0f, 0.032f, 1.0500488f, 0.89990234f))
                        .append(owner.scaleCombatant(16, 1, 1, 0.0f, 0.032f, 1.0f, 1.0f))
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
