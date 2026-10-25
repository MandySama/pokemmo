package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 虫之抵抗（招式 ID：522）的攻击／事件阶段动画。
 *
 * <p>招式描述：抵抗并攻击对手. 降低对手的特攻.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110522（名称）和 120522（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/StruggleBugAnimation.java
 *
 * <p>源文件 SHA-256：a5947c5f7eb983bbd6a17d1fd295c1a8dfd1d3afd42cbd751584792dbd35d882
 */
final class ReferenceMoveProgram522 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1867,
                                                                                        3,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.234375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1489,
                                                                                        1,
                                                                                        14,
                                                                                        500.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1376,
                                                                                        1,
                                                                                        14,
                                                                                        833.3333f,
                                                                                        0.0f,
                                                                                        owner.casterHandle)),
                                                                owner.shakeCombatant(
                                                                        14,
                                                                        2,
                                                                        6,
                                                                        0.0f,
                                                                        0.032f,
                                                                        0.30004883f,
                                                                        0.0f),
                                                                0.2f)
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        2,
                                                                        1,
                                                                        0.016f,
                                                                        0.128f,
                                                                        0.19995117f,
                                                                        -0.19995117f))
                                                        .onStart(owner.preloadParticleEffect(688))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 688, 0, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 688, 1, 9, 8, 0.5f))
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(0.6f)
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .onStart(owner.preloadParticleEffect(421)),
                                                owner.emitParticles(-1, 421, 1, 11, 8, 0.5f),
                                                1.0f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        2,
                                                        16,
                                                        83.333336f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1568,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.shakeCombatant(16, 2, 6, 0.0f, 0.032f, 0.30004883f, 0.0f))
                        .append(owner.restoreCamera(0.25f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
