package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 可怕面孔☆（自定义动画注册 ID：3184）的攻击／事件阶段动画。
 *
 * <p>招式描述：用恐怖的脸瞪着对手, 使其害怕, 从而大幅降低对手的速度.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>变体映射：基础招式 ID 为 184；按参考本地化规则沿用基础描述，并在名称末尾添加 ☆。
 *
 * <p>编号对应参考动画注册表；语言条目为 110184（名称）和 120184（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove3184Animation.java
 *
 * <p>源文件 SHA-256：b46bd486b22c796668c8c2a58bd5817aed49c434d3f535ae6221ae53a08ed1a8
 */
final class ReferenceMoveProgram3184 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(0, 0.4f))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        14, 1))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                3184)),
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.75f, 0.05f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1474,
                                                                1,
                                                                14,
                                                                250.0f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1509,
                                                                2,
                                                                14,
                                                                666.6667f,
                                                                0.78125f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                1, 0, 240.0f, 800.0f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 3184, 0, 9, 8, 0.5f)),
                                        owner.scaleCombatant(
                                                14, 2, 1, 0.016f, 0.16f, 0.0f, 0.30004883f),
                                        1.2f),
                                owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
