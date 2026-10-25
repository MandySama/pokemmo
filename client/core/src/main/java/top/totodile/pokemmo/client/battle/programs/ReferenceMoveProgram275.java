package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 扎根（招式 ID：275）的攻击／事件阶段动画。
 *
 * <p>招式描述：在大地上扎根, 每回合回复自己的HP. 因为扎根了, 所以不能替换宝可梦.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110275（名称）和 120275（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/IngrainAnimation.java
 *
 * <p>源文件 SHA-256：b22386708794884269ca862033e13728f5138591a3483448e90eb2394fa07651
 */
final class ReferenceMoveProgram275 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1441,
                                                        1,
                                                        14,
                                                        166.66667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1441,
                                                        2,
                                                        14,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1441,
                                                        1,
                                                        14,
                                                        500.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1828,
                                                        1,
                                                        14,
                                                        1000.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1828,
                                                        2,
                                                        14,
                                                        1333.3334f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1828,
                                                        1,
                                                        14,
                                                        1700.0f,
                                                        0.0625f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(440))
                                        .onStart(owner.preloadParticleEffect(441))
                                        .append(owner.emitParticles(-1, 440, 0, 9, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 440, 1, 9, 8, 0.0f))
                                        .append(owner.emitParticles(-1, 440, 2, 9, 8, 0.0f))
                                        .append(owner.emitParticles(-1, 441, 0, 9, 8, 0.0f)),
                                owner.emitParticles(-1, 441, 1, 9, 8, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
