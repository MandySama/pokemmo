package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 幻影打击（自定义动画注册 ID：1003）的攻击／事件阶段动画。
 *
 * <p>招式描述：幻影打击的简单模式变种
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵显隐；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 111003（名称）和 121003（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1003Animation.java
 *
 * <p>源文件 SHA-256：60dccc537d4f8b0729c5285f1408bc69d4dc09792234a1ee5e5c3b4bbb6aaea7
 */
final class ReferenceMoveProgram1003 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.6f)),
                                                owner.tintEnvironment(4, 16912, 0.0f, 0.75f, 0.1f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1492,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        1000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 480.0f, 480.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1783,
                                                        1,
                                                        14,
                                                        500.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 480.0f, 640.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 800.0f, 320.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        14,
                                                        1166.6666f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(643))
                                        .append(owner.emitParticles(-1, 643, 0, 11, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 643, 1, 11, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 643, 2, 11, 8, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 9, 0.016f, 0.032f, 0.19995117f, -0.19995117f),
                                0.04f)
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.08f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.12f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.16f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.2f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.24f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.28f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.32f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.36f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.4f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.44f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.tintEnvironment(4, 16912, 0.75f, 0.0f, 0.1f))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
