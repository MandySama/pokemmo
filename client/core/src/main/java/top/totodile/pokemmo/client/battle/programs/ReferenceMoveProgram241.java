package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 大晴天（招式 ID：241）的攻击／事件阶段动画。
 *
 * <p>招式描述：在5回合内阳光变得强烈, 从而提高火属性的招式威力. 水属性的招式威力则降低.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色。
 *
 * <p>编号对应参考动画注册表；语言条目为 110241（名称）和 120241（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SunnyDayAnimation.java
 *
 * <p>源文件 SHA-256：c9a2b907285a6b8d1977d193626beac5be5d09d1e0d32710da896cd3de9b9bd7
 */
final class ReferenceMoveProgram241 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.6f))
                                        .append(owner.tintEnvironment(4, 990, 0.0f, 0.375f, 0.1f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1495,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1358,
                                                        2,
                                                        14,
                                                        500.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1358,
                                                        1,
                                                        14,
                                                        1000.0f,
                                                        0.3125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1358,
                                                        1,
                                                        14,
                                                        1500.0f,
                                                        0.234375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1358,
                                                        1,
                                                        14,
                                                        2000.0f,
                                                        0.078125f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(408))
                                        .append(owner.emitParticles(-1, 408, 0, -4096, 0, 0.75f))
                                        .append(owner.emitParticles(-1, 408, 1, -4096, 0, 0.75f)),
                                owner.emitParticles(-1, 408, 2, -4096, 0, 0.75f))
                        .append(owner.tintEnvironment(4, 990, 0.375f, 0.0f, 0.1f))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
