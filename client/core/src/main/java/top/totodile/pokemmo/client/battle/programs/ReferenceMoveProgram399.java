package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 恶之波动（招式 ID：399）的攻击／事件阶段动画。
 *
 * <p>招式描述：从体内发出充满恶意的恐怖气场. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110399（名称）和 120399（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/DarkPulseAnimation.java
 *
 * <p>源文件 SHA-256：05c1f86e15abb4f42f5fc35455a2565d570162fa3bc55dfb9fa73082b4fb6d00
 */
final class ReferenceMoveProgram399 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1519,
                                        1,
                                        14,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1418,
                                        2,
                                        14,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        1833.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 2048.0f))
                        .append(owner.tintEnvironment(4, 10570, 0.0f, 0.8125f, 0.1f))
                        .onStart(owner.preloadParticleEffect(574))
                        .append(owner.emitParticles(-1, 574, 0, 9, 8, 0.25f))
                        .append(owner.emitParticles(-1, 574, 1, 9, 8, 0.25f));
        Color color = EncounterMoveAnimation.color(5125);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep6
                                        .append(owner.emitParticles(-1, 574, 2, 9, 8, 0.25f))
                                        .append(owner.restoreCamera(0.75f))
                                        .beginSequence()
                                        .waitSeconds(1.2f)
                                        .beginParallel()
                                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.375f, color))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 12, 0.016f, 0.016f, 0.19995117f, 0.0f))
                        .append(owner.tintEnvironment(4, 10570, 0.8125f, 0.0f, 0.1f));
        color = EncounterMoveAnimation.color(5125);
        result =
                animationStep7
                        .append(owner.tintCombatant(16, 0.75f, 0.375f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
