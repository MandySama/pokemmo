package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 玩水（招式 ID：346）的攻击／事件阶段动画。
 *
 * <p>招式描述：用水湿透周围. 在5回合内减弱火属性的招式.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110346（名称）和 120346（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/WaterSportAnimation.java
 *
 * <p>源文件 SHA-256：1e5f25aa7f56c8242961260384e5c5b39e6f0e49ae55558ff6d8ebc7fcbb321e
 */
final class ReferenceMoveProgram346 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1407,
                                                        2,
                                                        14,
                                                        166.66667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        14,
                                                        1500.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 1120.0f, 320.0f))
                                        .append(
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        3,
                                                        0.016f,
                                                        0.096f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .onStart(owner.preloadParticleEffect(519))
                                        .append(owner.emitParticles(-1, 519, 1, 9, 8, 0.4f)),
                                owner.emitParticles(-1, 519, 2, 9, 8, 0.4f))
                        .append(owner.focusCamera(0, 0.6f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1511,
                                        1,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1511,
                                        2,
                                        16,
                                        500.0f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(owner.emitParticles(-1, 519, 0, 11, 8, 0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .beginSequence()
                        .waitSeconds(0.2f)
                        .beginParallel()
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
