package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 污泥炸弹（招式 ID：188）的攻击／事件阶段动画。
 *
 * <p>招式描述：用污泥投掷对手进行攻击. 有时会让对手陷入中毒状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110188（名称）和 120188（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SludgeBombAnimation.java
 *
 * <p>源文件 SHA-256：1daa05309cd883de2baae30e3e5db7694b07c448bb196cca109809fe56c6329a
 */
final class ReferenceMoveProgram188 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .waitSeconds(0.6f)
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1727,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(354)),
                                owner.launchParticles(354, 2, 9, 11, 0.5f, 360.0f),
                                0.2f)
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1452,
                                        2,
                                        16,
                                        0.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1480,
                                        1,
                                        16,
                                        0.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        833.3333f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 320.0f, 480.0f))
                        .append(owner.emitParticles(-1, 354, 0, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(31775);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep6
                                        .append(owner.emitParticles(-1, 354, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.064f,
                                                        0.19995117f,
                                                        -0.19995117f)),
                                owner.tintCombatant(16, 0.75f, 0.0f, 0.625f, color))
                        .append(owner.restoreCamera(0.4f));
        color = EncounterMoveAnimation.color(31775);
        result =
                animationStep7
                        .append(owner.tintCombatant(16, 0.75f, 0.625f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
