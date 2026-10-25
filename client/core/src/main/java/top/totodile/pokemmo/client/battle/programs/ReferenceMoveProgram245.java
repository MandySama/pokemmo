package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 神速（招式 ID：245）的攻击／事件阶段动画。
 *
 * <p>招式描述：以迅雷不及掩耳之势猛撞向对手进行攻击. 必定能够先制攻击.
 *
 * <p>动画说明：播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵显隐；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110245（名称）和 120245（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/ExtremeSpeedAnimation.java
 *
 * <p>源文件 SHA-256：4a2c5e40c984dc501425bd6f940b209f1f28bd28aa450b1cbec74abf9922e82a
 */
final class ReferenceMoveProgram245 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep17 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenNextParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .sequence()
                                                                                                        .beginParallel()
                                                                                                        .waitSeconds(
                                                                                                                0.6f)
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .setHealthHudHidden(
                                                                                                                                18,
                                                                                                                                true))
                                                                                                        .endGroup()
                                                                                                        .beginParallel()
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .loadBattleBackground(
                                                                                                                                29))
                                                                                                        .onStart(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .callback(
                                                                                                                                sceneFlagAction2)),
                                                                                                owner
                                                                                                        .tintEnvironment(
                                                                                                                4,
                                                                                                                0,
                                                                                                                0.0f,
                                                                                                                0.8125f,
                                                                                                                0.025f))
                                                                                        .onStart(
                                                                                                ReferenceMoveTimeline
                                                                                                        .callback(
                                                                                                                sceneFlagAction3))
                                                                                        .onStart(
                                                                                                ReferenceMoveTimeline
                                                                                                        .callback(
                                                                                                                sceneFlagAction4))
                                                                                        .append(
                                                                                                owner
                                                                                                        .tintEnvironment(
                                                                                                                3,
                                                                                                                0,
                                                                                                                0.8125f,
                                                                                                                0.0f,
                                                                                                                0.025f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scrollBattleBackground(
                                                                                                                4,
                                                                                                                36,
                                                                                                                0,
                                                                                                                3.2f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1818,
                                                                                                                1,
                                                                                                                14,
                                                                                                                0.0f,
                                                                                                                0.703125f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1376,
                                                                                                                1,
                                                                                                                14,
                                                                                                                2333.3333f,
                                                                                                                0.0f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .adjustSoundTrack(
                                                                                                                1,
                                                                                                                0,
                                                                                                                0.0f,
                                                                                                                2240.0f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .adjustSoundTrack(
                                                                                                                1,
                                                                                                                1,
                                                                                                                1600.0f,
                                                                                                                640.0f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1817,
                                                                                                                3,
                                                                                                                14,
                                                                                                                250.0f,
                                                                                                                0.9921875f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1420,
                                                                                                                2,
                                                                                                                16,
                                                                                                                583.3333f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1420,
                                                                                                                2,
                                                                                                                16,
                                                                                                                1000.0f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1420,
                                                                                                                2,
                                                                                                                16,
                                                                                                                1416.6666f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle))
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                412)),
                                                                                owner
                                                                                        .scaleCombatant(
                                                                                                14,
                                                                                                1,
                                                                                                0,
                                                                                                0.0f,
                                                                                                0.096f,
                                                                                                0.100097656f,
                                                                                                1.0f),
                                                                                0.06f),
                                                                owner.shakeCombatant(
                                                                        14,
                                                                        2,
                                                                        2,
                                                                        0.0f,
                                                                        0.032f,
                                                                        0.39990234f,
                                                                        0.0f),
                                                                0.22f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 412, 3, 0, 0, 0.0f))
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        14, 1, 0, 0.0f, 0.32f, 0.0f,
                                                                        10.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 412, 2, 9, 8, 0.4f))
                                                        .waitSeconds(0.6f)
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(0.42f)
                                                        .beginParallel()
                                                        .append(owner.setCombatantVisibility(14, 3))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        14, 1, 0, 0.0f, 0.096f,
                                                                        1.0f, 1.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 412, 0, 11, 8, 0.4f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 412, 1, 11, 8, 0.4f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        1,
                                                                        0.0f,
                                                                        0.048f,
                                                                        -0.30004883f,
                                                                        0.30004883f)),
                                                owner.shakeCombatant(
                                                        16, 1, 0, 0.0f, 0.032f, 0.5f, 0.0f),
                                                0.74f)
                                        .append(owner.emitParticles(-1, 412, 3, 0, 0, 0.0f))
                                        .append(owner.emitParticles(-1, 412, 0, 11, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 412, 1, 11, 8, 0.4f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.048f,
                                                        -0.30004883f,
                                                        0.30004883f)),
                                owner.shakeCombatant(16, 1, 0, 0.0f, 0.032f, 0.5f, 0.0f),
                                1.06f)
                        .append(owner.emitParticles(-1, 412, 3, 0, 0, 0.0f))
                        .append(owner.emitParticles(-1, 412, 0, 11, 8, 0.4f))
                        .append(owner.emitParticles(-1, 412, 1, 11, 8, 0.4f))
                        .append(owner.shakeCombatant(16, 1, 0, 0.0f, 0.032f, 0.5f, 0.0f))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.048f, -0.30004883f, 0.30004883f))
                        .append(owner.shakeCombatant(14, 5, 0, 0.0f, 0.32f, 0.0f, 0.0f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.38f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.44f)
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.shakeCombatant(16, 1, 0, 0.0f, 0.096f, -1.5f, 0.0f))
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.5f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.56f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.62f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.68f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.74f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.8f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.86f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.92f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.98f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(2.04f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(2.1f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(2.16f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f))
                        .beginSequence()
                        .waitSeconds(0.32f)
                        .beginParallel()
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(1, true);
        result =
                animationStep17
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .waitSeconds(0.6f)
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
