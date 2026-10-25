package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 聚宝功（招式 ID：6）的攻击／事件阶段动画。
 *
 * <p>招式描述：将战斗后能回收的钱币投向对手. 钱币的数目随对手的等级而增长. 每场战斗仅生效一次.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110006（名称）和 120006（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/PayDayAnimation.java
 *
 * <p>源文件 SHA-256：5bbac096e008b3505ced8650a2074675403ff3ca14f4dcab524930c7816b1371
 */
final class ReferenceMoveProgram6 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .onStart(owner.preloadParticleEffect(166))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1432,
                                                                        1,
                                                                        16,
                                                                        166.66667f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1430,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle)),
                                                owner.launchParticles(166, 0, 9, 11, 0.5f, 0.0f),
                                                0.4f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(owner.emitParticles(-1, 166, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 166, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 166, 3, 11, 8, 0.0f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1431,
                                                        1,
                                                        16,
                                                        250.0f,
                                                        0.8984375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2, 1433, 2, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
