package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 逐步击破（招式 ID：498）的攻击／事件阶段动画。
 *
 * <p>招式描述：看准机会稳步攻击. 无视对手的能力变化, 直接给予伤害.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110498（名称）和 120498（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/ChipAwayAnimation.java
 *
 * <p>源文件 SHA-256：fdddd9c88d22bb2f4b35bd4b89b9e49a7e87c7961b052a30a2250412b72b93b8
 */
final class ReferenceMoveProgram498 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1415,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.tintCombatant(14, 0.25f, 0.0f, 0.75f, color))
                        .append(owner.focusCamera(1, 0.4f))
                        .beginSequence()
                        .waitSeconds(0.4f)
                        .beginParallel()
                        .onStart(owner.preloadParticleEffect(665))
                        .append(owner.emitParticles(-1, 665, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 665, 1, 11, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep4
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.096f, -0.30004883f, 0.30004883f))
                        .append(owner.tintCombatant(14, 0.75f, 0.75f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
