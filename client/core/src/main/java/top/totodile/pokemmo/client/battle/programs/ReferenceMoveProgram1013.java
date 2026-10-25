package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 瘴气（自定义动画注册 ID：1013）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111013（名称）和 121013（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1013Animation.java
 *
 * <p>源文件 SHA-256：c859a3a728f7cb94aa59159eaf1221e0849f66d513eb934f4d1f69820284b7cd
 */
final class ReferenceMoveProgram1013 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        4, 6150, 0.0f, 0.75f,
                                                                        0.05f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1533,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1505,
                                                                        2,
                                                                        14,
                                                                        1083.3334f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(1013))
                                                        .onStart(owner.preloadParticleEffect(642))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 642, 0, 11, 8, 0.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 1013, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 1013, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 1013, 2, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 1013, 3, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 1013, 4, 11, 8, 0.5f),
                                                0.4f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 13, 0.032f, 0.016f, 0.30004883f, 0.0f),
                                3.0f)
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup();
        return result;
    }
}
