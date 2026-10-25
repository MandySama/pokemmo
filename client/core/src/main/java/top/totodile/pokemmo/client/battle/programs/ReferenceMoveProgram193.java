package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 识破（招式 ID：193）的攻击／事件阶段动画。
 *
 * <p>招式描述：对幽灵属性宝可梦没有效果的招式以及闪避率高的对手, 使用后变得能够打中.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110193（名称）和 120193（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/ForesightAnimation.java
 *
 * <p>源文件 SHA-256：ffc140b5ab776e29a48120b9de5132c1ca700f97ff019e998448f78c152bc649
 */
final class ReferenceMoveProgram193 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        ReferenceMoveTimeline.sequence()
                                .beginParallel()
                                .append(owner.focusCamera(1, 0.4f))
                                .append(owner.setSpriteAnimationPaused(14, 1))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1489,
                                                1,
                                                16,
                                                0.0f,
                                                0.859375f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1489,
                                                2,
                                                16,
                                                500.0f,
                                                0.859375f,
                                                owner.casterHandle))
                                .onStart(owner.preloadParticleEffect(359))
                                .append(owner.emitParticles(-1, 359, 0, 11, 8, 0.5f))
                                .append(owner.emitParticles(-1, 359, 1, 11, 8, 0.5f))
                                .append(owner.emitParticles(-1, 359, 2, 11, 8, 0.5f)),
                        owner.emitParticles(-1, 359, 3, 11, 8, 0.5f),
                        1.0f);
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep7.append(
                                owner.scheduleSound(
                                        (byte) 2, 1358, 1, 16, 0.0f, 0.9375f, owner.casterHandle)),
                        owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color),
                        1.6f);
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep8, owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
