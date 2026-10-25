package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 辅助力量（招式 ID：500）的攻击／事件阶段动画。
 *
 * <p>招式描述：用蓄积起来的力量攻击对手. 自己的能力提高得越多, 威力就越大.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110500（名称）和 120500（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/StoredPowerAnimation.java
 *
 * <p>源文件 SHA-256：2324572135ba22f4e87c15c9ddfe4e8508cf6437c86cffa7156e35104a6c4cbd
 */
final class ReferenceMoveProgram500 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1654,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.546875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1593,
                                                                        1,
                                                                        14,
                                                                        700.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        2, 0, 0.0f, 0.625f,
                                                                        0.075f)),
                                                owner.tintCombatant(
                                                        14,
                                                        0.25f,
                                                        0.0f,
                                                        0.625f,
                                                        EncounterMoveAnimation.color(31)))
                                        .onStart(owner.preloadParticleEffect(667))
                                        .append(owner.emitParticles(-1, 667, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 667, 1, 9, 8, 0.5f)),
                                owner.tintCombatant(
                                        14, 0.25f, 0.625f, 0.0f, EncounterMoveAnimation.color(31)))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.tintEnvironment(2, 0, 0.625f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
