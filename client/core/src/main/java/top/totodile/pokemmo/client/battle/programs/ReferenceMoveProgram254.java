package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 蓄力（招式 ID：254）的攻击／事件阶段动画。
 *
 * <p>招式描述：积蓄力量, 提高自己的防御和特防. 最多积蓄3次.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110254（名称）和 120254（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/StockpileAnimation.java
 *
 * <p>源文件 SHA-256：e5d8aceeea030091d472acbd2493574b5344481466509a7a1342ae49f4cf3eb3
 */
final class ReferenceMoveProgram254 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .onStart(owner.setHealthHudHidden(18, true))
                        .append(owner.focusCamera(0, 0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintCombatant(14, 1.0f, 0.0f, 0.75f, color))
                        .onStart(owner.preloadParticleEffect(420))
                        .append(owner.emitParticles(-1, 420, 0, 9, 8, 0.5f))
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 2, 0.016f, 0.128f, 0.30004883f, -0.30004883f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1470, 1, 14, 0.0f, 0.234375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1779, 2, 14, 0.0f, 0.9375f, owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep4,
                                owner.scheduleSound(
                                        (byte) 2,
                                        1779,
                                        2,
                                        14,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.3f))
                        .append(owner.tintCombatant(14, 1.0f, 0.75f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
