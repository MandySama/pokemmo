package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 爆炸头突击（招式 ID：543）的攻击／事件阶段动画。
 *
 * <p>招式描述：用厉害的爆炸头猛撞向对手进行攻击. 自己也会受到少许伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110543（名称）和 120543（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/HeadChargeAnimation.java
 *
 * <p>源文件 SHA-256：095dc3da788118c697a0af623eed7eb57c86caf3f57db8750c19777c24d6a341
 */
final class ReferenceMoveProgram543 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1416,
                                        1,
                                        14,
                                        166.66667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        683.3333f,
                                        0.0f,
                                        owner.casterHandle))
                        .onStart(owner.loadBattleBackground(3))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, (1 != 0))))
                        .endGroup()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1753,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1842,
                                        0,
                                        16,
                                        83.333336f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.scrollBattleBackground(2, 0, 8, 0.048f))
                        .onStart(owner.preloadParticleEffect(706))
                        .append(owner.emitParticles(-1, 706, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 706, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 706, 2, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.096f, 0.39990234f, -0.39990234f))
                        .append(owner.shakeCombatant(16, 2, 10, 0.0f, 0.048f, 1.0f, 0.0f))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, (0 != 0))))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, (0 != 0))))
                        .endGroup()
                        .beginParallel()
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, (1 != 0))))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, (1 != 0))))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
