package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 治愈之愿（招式 ID：361）的攻击／事件阶段动画。
 *
 * <p>招式描述：使用者陷入濒死状态. 接下来替换上场的替代使用者位置的宝可梦的HP和异常状态完全恢复. 如果宝可梦进入该侧边时HP全满, 该愿望将等待下一只替换上场的宝可梦.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110361（名称）和 120361（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/HealingWishAnimation.java
 *
 * <p>源文件 SHA-256：d3fc9353e2e465559892acbeba5b9ea69bf76fd2a7be3ed37278e72da584e649
 */
final class ReferenceMoveProgram361 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(0, false);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f)),
                                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.1f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1483,
                                                        1,
                                                        14,
                                                        166.66667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1358,
                                                        2,
                                                        14,
                                                        333.33334f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(535))
                                        .append(owner.emitParticles(-1, 535, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 535, 1, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 535, 2, 9, 8, 0.5f))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)))
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.1f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
