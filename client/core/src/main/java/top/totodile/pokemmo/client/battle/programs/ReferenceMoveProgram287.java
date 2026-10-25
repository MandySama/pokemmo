package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 焕然一新（招式 ID：287）的攻击／事件阶段动画。
 *
 * <p>招式描述：让身体休息, 治愈自己身上所中的毒、麻痹、灼伤的异常状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110287（名称）和 120287（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/RefreshAnimation.java
 *
 * <p>源文件 SHA-256：b6ad73bbb9b76f3eb4be9fa1021865360cc05fc958739e86f32ffa3a5583927d
 */
final class ReferenceMoveProgram287 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(
                                                        owner.tintEnvironment(
                                                                4,
                                                                Short.MAX_VALUE,
                                                                0.0f,
                                                                0.75f,
                                                                0.075f))
                                                .append(owner.focusCamera(0, 0.4f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1483,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 480.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1509,
                                                                2,
                                                                14,
                                                                750.0f,
                                                                0.78125f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(454))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 454, 0, 9, 8, 0.5f)),
                                        owner.emitParticles(-1, 454, 1, 9, 8, 0.5f))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintEnvironment(4, Short.MAX_VALUE, 0.75f, 0.0f, 0.075f));
        return result;
    }
}
