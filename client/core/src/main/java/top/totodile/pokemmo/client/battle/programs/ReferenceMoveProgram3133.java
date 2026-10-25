package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 瞬间失忆☆（自定义动画注册 ID：3133）的攻击／事件阶段动画。
 *
 * <p>招式描述：将头脑清空, 瞬间忘记某事, 从而大幅提高自己的特防.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>变体映射：基础招式 ID 为 133；按参考本地化规则沿用基础描述，并在名称末尾添加 ☆。
 *
 * <p>编号对应参考动画注册表；语言条目为 110133（名称）和 120133（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/AmnesiaAnimation.java
 *
 * <p>源文件 SHA-256：f8ead4fb599ecb91bf4a51e07dc63405132d4c9131b0690ab844110e6187154f
 */
final class ReferenceMoveProgram3133 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.25f)),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.075f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1452,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(298))
                                        .append(owner.emitParticles(-1, 298, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 298, 1, 9, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.25f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
