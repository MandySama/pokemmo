package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 十字毒刃（招式 ID：440）的攻击／事件阶段动画。
 *
 * <p>招式描述：用毒刃劈开对手. 有时会让对手陷入中毒状态, 也容易击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110440（名称）和 120440（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/CrossPoisonAnimation.java
 *
 * <p>源文件 SHA-256：9660e46763ced91e6e2208a9ccd2aa95595cf236a5f70e947dda3cd9a70336eb
 */
final class ReferenceMoveProgram440 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1455,
                                                                        2,
                                                                        16,
                                                                        166.66667f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(615))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 615, 2, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 615, 3, 11, 8, 0.5f),
                                                0.2f)
                                        .append(owner.emitParticles(-1, 615, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 615, 1, 11, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        4,
                                                        0.016f,
                                                        0.032f,
                                                        0.19995117f,
                                                        -0.19995117f)),
                                owner.tintCombatant(
                                        16,
                                        0.75f,
                                        0.0f,
                                        0.8125f,
                                        EncounterMoveAnimation.color(31764)))
                        .append(
                                owner.tintCombatant(
                                        16,
                                        0.75f,
                                        0.8125f,
                                        0.0f,
                                        EncounterMoveAnimation.color(31764)))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
