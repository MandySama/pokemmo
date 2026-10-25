package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 睡觉（招式 ID：156）的攻击／事件阶段动画。
 *
 * <p>招式描述：连续睡上2回合. 回复自己的全部HP以及治愈所有异常状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110156（名称）和 120156（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/RestAnimation.java
 *
 * <p>源文件 SHA-256：a5f0fbe4e3c14311b74b05d8bede7eea21895ed69246bc8127d0efcabe0d52a9
 */
final class ReferenceMoveProgram156 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(0, 0.4f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 2033,
                                                                                1,
                                                                                14,
                                                                                333.33334f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 2033,
                                                                                1,
                                                                                14,
                                                                                1166.6666f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 2033,
                                                                                1,
                                                                                14,
                                                                                1666.6666f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1462,
                                                                                2,
                                                                                14,
                                                                                333.33334f,
                                                                                0.859375f,
                                                                                owner.casterHandle)),
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1462,
                                                                2,
                                                                14,
                                                                1416.6666f,
                                                                0.859375f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(320))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 320, 1, 9, 8, 0.5f)),
                                        owner.emitParticles(-1, 320, 0, 9, 8, 0.5f),
                                        0.6f),
                                owner.emitParticles(-1, 320, 0, 9, 8, 0.5f),
                                1.2f)
                        .append(owner.emitParticles(-1, 320, 1, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 320, 0, 9, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
