package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 暗黑爆破（招式 ID：539）的攻击／事件阶段动画。
 *
 * <p>招式描述：放出黑暗的冲击波攻击对手. 有时会降低对手的命中率.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110539（名称）和 120539（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/NightDazeAnimation.java
 *
 * <p>源文件 SHA-256：8ae3869e9ab00ef7f1f3232e020ea7eaef78a8e84db217e2a6ec2f38d8c924ab
 */
final class ReferenceMoveProgram539 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.tintEnvironment(
                                                        4, 10570, 0.0f, 0.8125f, 0.1f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1455,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1427,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.546875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1427,
                                                        2,
                                                        14,
                                                        500.0f,
                                                        0.546875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(574))
                                        .append(owner.emitParticles(-1, 574, 0, 9, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 574, 1, 9, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 574, 2, 9, 8, 0.25f))
                                        .append(owner.restoreCamera(0.75f))
                                        .beginSequence()
                                        .waitSeconds(0.48f)
                                        .beginParallel(),
                                owner.emitParticles(-1, 574, 2, 9, 8, 0.25f),
                                1.68f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1450, 2, 16, 0.0f, 0.390625f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 560.0f, 400.0f));
        Color color = EncounterMoveAnimation.color(5125);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep9
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        2,
                                                        16,
                                                        1000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.375f, color))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 12, 0.016f, 0.016f, 0.19995117f, 0.0f))
                        .append(owner.tintEnvironment(4, 10570, 0.8125f, 0.0f, 0.1f));
        color = EncounterMoveAnimation.color(5125);
        result =
                animationStep10
                        .append(owner.tintCombatant(16, 0.75f, 0.375f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
