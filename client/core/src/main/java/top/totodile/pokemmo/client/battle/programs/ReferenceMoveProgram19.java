package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 飞翔（招式 ID：19）的攻击／事件阶段动画。
 *
 * <p>招式描述：第1回合飞上天空, 第2回合攻击对手.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵显隐；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110019（名称）和 120019（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/FlyAnimation.java
 *
 * <p>源文件 SHA-256：f6098a352c8182db5f53e0e23bb5619801e2307e63248cbcf8068195d7e536cc
 */
final class ReferenceMoveProgram19 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .beginSequence()
                                                .waitSeconds(0.32f)
                                                .beginParallel()
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1497,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.859375f,
                                                                owner.casterHandle))
                                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 480.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                1,
                                                                16,
                                                                666.6667f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(181))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 181, 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 181, 1, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 181, 2, 11, 8, 0.5f),
                                        0.58f)
                                .append(owner.focusCamera(1, 0.3f))
                                .endGroup()
                                .endGroup()
                                .beginSequence()
                                .waitSeconds(0.84f)
                                .beginParallel()
                                .append(
                                        owner.scaleCombatant(
                                                16, 2, 1, 0.0f, 0.064f, 0.60009766f, -0.60009766f))
                                .append(owner.shakeCombatant(14, 1, 0, 0.0f, 0.016f, 0.0f, 2.0f))
                                .onStart(owner.setHealthHudHidden(14, true))
                                .endGroup()
                                .endGroup()
                                .endGroup()
                                .beginParallel()
                                .append(owner.restoreCamera(0.4f))
                                .append(owner.setCombatantVisibility(14, 4))
                                .onStart(owner.setHealthHudHidden(14, false)),
                        owner.shakeCombatant(14, 5, 0, 0.0f, 0.192f, 0.0f, 0.0f));
        return result;
    }
}
