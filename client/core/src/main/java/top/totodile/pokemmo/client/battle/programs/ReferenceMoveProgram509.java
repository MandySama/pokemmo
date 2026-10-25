package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 巴投（招式 ID：509）的攻击／事件阶段动画。
 *
 * <p>招式描述：扔飞对手, 强制拉后备宝可梦上场. 如果对手为野生宝可梦, 战斗将直接结束.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110509（名称）和 120509（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/CircleThrowAnimation.java
 *
 * <p>源文件 SHA-256：1bc57989ab9a0a550886ffcf7a83d956485f9b0c6c9d03ad5801040fcf0aeb0d
 */
final class ReferenceMoveProgram509 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.waitThenParallel(
                                                                0.6f),
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1444,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle),
                                                        0.04f)
                                                .append(owner.focusCamera(1, 0.4f))
                                                .endGroup()
                                                .endGroup()
                                                .beginSequence()
                                                .waitSeconds(0.08f)
                                                .beginParallel()
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1420,
                                                                2,
                                                                16,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1434,
                                                                2,
                                                                16,
                                                                333.33334f,
                                                                0.78125f,
                                                                owner.casterHandle))
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .onStart(owner.preloadParticleEffect(674))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 674, 0, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 674, 1, 11, 8, 0.5f),
                                        0.24f),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.0f, 0.048f, 0.39990234f, -0.39990234f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
