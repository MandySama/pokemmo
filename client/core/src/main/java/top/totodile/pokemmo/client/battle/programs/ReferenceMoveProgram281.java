package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 哈欠（招式 ID：281）的攻击／事件阶段动画。
 *
 * <p>招式描述：打个大哈欠引起睡意. 在下一回合让对手陷入睡眠状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110281（名称）和 120281（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/YawnAnimation.java
 *
 * <p>源文件 SHA-256：ac9d2111bcfc0c1a12a41775063755f4db93baf3c846fa8e5427a11e1def3779
 */
final class ReferenceMoveProgram281 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1964,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.128f,
                                                        -0.19995117f,
                                                        0.19995117f))
                                        .onStart(owner.preloadParticleEffect(448))
                                        .append(owner.launchParticles(448, 0, 9, 11, 0.5f, 0.0f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(owner.emitParticles(-1, 448, 1, 11, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.048f,
                                                        0.19995117f,
                                                        -0.19995117f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1461,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1461,
                                        1,
                                        16,
                                        133.33333f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
