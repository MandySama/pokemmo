package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 喋喋不休（招式 ID：448）的攻击／事件阶段动画。
 *
 * <p>招式描述：用非常烦人的, 喋喋不休的音波攻击对手. 使对手混乱.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110448（名称）和 120448（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/ChatterAnimation.java
 *
 * <p>源文件 SHA-256：e8a7edde48aab1bac383741ca0adc1eca3d8d227f9a3404a2196a2c8078ed7b2
 */
final class ReferenceMoveProgram448 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.playPokemonCry(false, 0.0f))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1848,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.546875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1642,
                                                        2,
                                                        14,
                                                        666.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1642,
                                                        2,
                                                        14,
                                                        1166.6666f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(623))
                                        .append(owner.emitParticles(-1, 623, 0, 9, 8, 0.75f))
                                        .append(owner.emitParticles(-1, 623, 1, 9, 8, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(14, 2, 3, 0.016f, 0.096f, 1.0f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
