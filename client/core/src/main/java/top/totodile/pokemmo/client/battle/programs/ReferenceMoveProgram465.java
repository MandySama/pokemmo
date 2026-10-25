package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 种子闪光（招式 ID：465）的攻击／事件阶段动画。
 *
 * <p>招式描述：从身体里产生冲击波. 有时会大幅降低对手的特防.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110465（名称）和 120465（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/SeedFlareAnimation.java
 *
 * <p>源文件 SHA-256：552a023bb0717861df3f5f186d27267ce7125d8963442438381b7d49e5eb180c
 */
final class ReferenceMoveProgram465 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.sequence()
                                .beginParallel()
                                .append(
                                        owner.tintEnvironment(
                                                4, Short.MAX_VALUE, 0.0f, 0.75f, 0.025f))
                                .append(owner.focusCamera(0, 0.4f))
                                .endGroup()
                                .beginParallel()
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1434,
                                                1,
                                                14,
                                                0.0f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 480.0f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1521,
                                                4,
                                                14,
                                                833.3333f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1475,
                                                2,
                                                14,
                                                833.3333f,
                                                0.859375f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1418,
                                                3,
                                                16,
                                                1000.0f,
                                                0.78125f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1376,
                                                3,
                                                16,
                                                2166.6667f,
                                                0.0f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1475,
                                                1,
                                                16,
                                                1166.6666f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1475,
                                                2,
                                                16,
                                                1333.3334f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1475,
                                                1,
                                                16,
                                                1500.0f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1475,
                                                2,
                                                16,
                                                1666.6666f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1475,
                                                1,
                                                16,
                                                1833.3334f,
                                                0.46875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1475,
                                                2,
                                                16,
                                                2000.0f,
                                                0.15625f,
                                                owner.casterHandle))
                                .onStart(owner.preloadParticleEffect(641))
                                .append(owner.emitParticles(-1, 641, 0, 9, 8, 0.5f))
                                .append(owner.emitParticles(-1, 641, 1, 9, 8, 0.5f))
                                .append(owner.emitParticles(-1, 641, 2, 9, 8, 0.5f))
                                .append(owner.emitParticles(-1, 641, 3, 9, 8, 0.5f))
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .beginSequence()
                                .waitSeconds(1.2f)
                                .beginParallel()
                                .append(owner.shakeCombatant(16, 2, 12, 0.016f, 0.016f, 0.5f, 0.0f))
                                .append(owner.restoreCamera(0.4f))
                                .endGroup()
                                .endGroup()
                                .endGroup()
                                .beginParallel()
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .append(
                                        owner.tintEnvironment(
                                                4, Short.MAX_VALUE, 0.75f, 0.0f, 0.025f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1376,
                                                2,
                                                16,
                                                0.0f,
                                                0.0f,
                                                owner.casterHandle)),
                        owner.scheduleSound((byte) 2, 1376, 1, 16, 0.0f, 0.0f, owner.casterHandle));
        return result;
    }
}
