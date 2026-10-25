package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 不给糖就捣乱（自定义动画注册 ID：1000）的攻击／事件阶段动画。
 *
 * <p>招式描述：不给糖就捣乱
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111000（名称）和 121000（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1000Animation.java
 *
 * <p>源文件 SHA-256：a006d6cc8db3fec7b9be96e5016dbf5c7a7a69a41426a7fd9d28f0d7ec6dd5ef
 */
final class ReferenceMoveProgram1000 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1507,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle)),
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.032f,
                                                        0.048f,
                                                        0.19995117f,
                                                        -0.19995117f))
                                        .append(owner.focusCamera(1, 0.3f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1508,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(1000))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 1000, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 1000, 1, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.032f, 0.048f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
