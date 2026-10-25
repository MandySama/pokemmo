package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 自我暗示（招式 ID：244）的攻击／事件阶段动画。
 *
 * <p>招式描述：向自己施以自我暗示, 将能力变化的状态变得和对手一样.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110244（名称）和 120244（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/PsychUpAnimation.java
 *
 * <p>源文件 SHA-256：9d62bbef562257e232d00e761b2435b1865820246b65d994f7fda09dfd4dba22
 */
final class ReferenceMoveProgram244 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(0, 0.4f)),
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.75f, 0.075f))
                                                .onStart(owner.preloadParticleEffect(411))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1455,
                                                                2,
                                                                14,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle)),
                                        owner.emitParticles(-1, 411, 0, 9, 8, 0.5f))
                                .append(owner.restoreCamera(0.4f))
                                .endGroup()
                                .beginParallel(),
                        owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.075f));
        return result;
    }
}
