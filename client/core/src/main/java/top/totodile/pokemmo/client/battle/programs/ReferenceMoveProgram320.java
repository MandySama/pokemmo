package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 草笛（招式 ID：320）的攻击／事件阶段动画。
 *
 * <p>招式描述：让对手听舒适的笛声, 从而陷入睡眠状态.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色。
 *
 * <p>编号对应参考动画注册表；语言条目为 110320（名称）和 120320（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/GrassWhistleAnimation.java
 *
 * <p>源文件 SHA-256：b866b4cea3aef0b0710390600d46d3c4d28b9812c3c246c815e358fd5efef697
 */
final class ReferenceMoveProgram320 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1512,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(490))
                                        .append(
                                                owner.tintEnvironment(
                                                        4, 960, 0.0f, 0.375f, 0.075f)),
                                owner.launchParticles(490, 0, 9, 11, 0.25f, 0.0f))
                        .append(owner.tintEnvironment(4, 960, 0.375f, 0.0f, 0.075f))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
