package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 贪婪吞咽（自定义动画注册 ID：1011）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111011（名称）和 121011（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BiteAnimation.java
 *
 * <p>源文件 SHA-256：679ea56a7a46c9d2de7d5b6a7ea688ef85a36c8e124e988ca08b1fc368d5282e
 */
final class ReferenceMoveProgram1011 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(1, 0.25f))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1491,
                                                                2,
                                                                16,
                                                                333.33334f,
                                                                0.625f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1720,
                                                                1,
                                                                16,
                                                                333.33334f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(205))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 205, 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 205, 1, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 205, 2, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 205, 3, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 205, 4, 11, 8, 0.5f),
                                        0.2f),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
