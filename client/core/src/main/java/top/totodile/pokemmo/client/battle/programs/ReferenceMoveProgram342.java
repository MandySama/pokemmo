package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 毒尾（招式 ID：342）的攻击／事件阶段动画。
 *
 * <p>招式描述：用尾巴拍打. 有时会让对手陷入中毒状态, 也容易击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110342（名称）和 120342（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/PoisonTailAnimation.java
 *
 * <p>源文件 SHA-256：9b9abfd93b57944a3f95f9676859746f92d2653b0e34e78e664736a05f6111f4
 */
final class ReferenceMoveProgram342 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1421,
                                        1,
                                        16,
                                        250.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1438,
                                        2,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1455,
                                        2,
                                        16,
                                        333.33334f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        16,
                                        1500.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 640.0f, 640.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1442,
                                        1,
                                        16,
                                        333.33334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(514))
                        .append(owner.emitParticles(-1, 514, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 514, 1, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 514, 2, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(30740);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep9
                                        .append(owner.emitParticles(-1, 514, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.032f,
                                                        0.032f,
                                                        0.0f,
                                                        -0.19995117f)),
                                owner.tintCombatant(16, 0.25f, 0.0f, 0.8125f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f));
        color = EncounterMoveAnimation.color(30740);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep10, owner.tintCombatant(16, 0.25f, 0.8125f, 0.0f, color));
        return result;
    }
}
