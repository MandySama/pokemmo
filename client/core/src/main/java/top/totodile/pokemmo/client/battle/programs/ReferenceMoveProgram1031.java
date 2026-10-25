package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 春回大地（自定义动画注册 ID：1031）的攻击／事件阶段动画。
 *
 * <p>招式描述：恢复队伍和队友的全部异常状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111031（名称）和 121031（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/AromatherapyAnimation.java
 *
 * <p>源文件 SHA-256：453873e740b96125a10e75c4a0b44f924447a79942e87aa84495e424237d99ce
 */
final class ReferenceMoveProgram1031 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .onStart(owner.preloadParticleEffect(483))
                        .append(owner.emitParticles(-1, 483, 2, 0, 0, 0.0f))
                        .append(owner.emitParticles(-1, 483, 0, 0, 0, 0.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1869, 1, 16, 0.0f, 0.625f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 1040.0f, 288.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        1,
                                        16,
                                        1383.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1869,
                                        2,
                                        14,
                                        83.333336f,
                                        0.3125f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 1040.0f, 288.0f));
        Color color = EncounterMoveAnimation.color(23254);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep6.append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1376,
                                                2,
                                                14,
                                                1383.3334f,
                                                0.0f,
                                                owner.casterHandle)),
                                owner.tintEnvironment(4, 8896, 0.0f, 0.75f, 0.05f),
                                0.2f)
                        .onStart(owner.setHealthHudHidden(18, true))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.6f)
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.6f))
                        .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color))
                        .append(owner.emitParticles(-1, 483, 3, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 483, 1, 9, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(23254);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                animationStep9,
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1786,
                                                        2,
                                                        14,
                                                        83.333336f,
                                                        0.9921875f,
                                                        owner.casterHandle),
                                                2.4f)
                                        .append(owner.tintEnvironment(4, 8896, 0.75f, 0.0f, 0.05f)),
                                owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color2))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
