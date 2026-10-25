package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 暗袭要害（招式 ID：400）的攻击／事件阶段动画。
 *
 * <p>招式描述：抓住瞬间的空隙切斩对手. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110400（名称）和 120400（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/NightSlashAnimation.java
 *
 * <p>源文件 SHA-256：9f6495561713dcbebeb9e4d56d009d51a2bb3ba9d2e5dbea9011e25ac274e499
 */
final class ReferenceMoveProgram400 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .sequence()
                                                                                                        .beginParallel()
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .focusCamera(
                                                                                                                                1,
                                                                                                                                0.4f))
                                                                                                        .endGroup()
                                                                                                        .beginParallel()
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1435,
                                                                                                                                1,
                                                                                                                                16,
                                                                                                                                0.0f,
                                                                                                                                0.8984375f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1514,
                                                                                                                                2,
                                                                                                                                16,
                                                                                                                                500.0f,
                                                                                                                                0.9375f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1514,
                                                                                                                                2,
                                                                                                                                16,
                                                                                                                                1083.3334f,
                                                                                                                                0.9375f,
                                                                                                                                owner.casterHandle))
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .preloadParticleEffect(
                                                                                                                                575)),
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                575,
                                                                                                                2,
                                                                                                                11,
                                                                                                                8,
                                                                                                                0.5f),
                                                                                                0.2f)
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                575,
                                                                                                                3,
                                                                                                                11,
                                                                                                                8,
                                                                                                                0.5f)),
                                                                                owner.emitParticles(
                                                                                        -1, 575, 4,
                                                                                        11, 8,
                                                                                        0.0f),
                                                                                0.3f)
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 575, 1,
                                                                                        11, 8,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner
                                                                                        .shakeCombatant(
                                                                                                16,
                                                                                                2,
                                                                                                12,
                                                                                                0.016f,
                                                                                                0.016f,
                                                                                                0.30004883f,
                                                                                                0.0f)),
                                                                owner.emitParticles(
                                                                        -1, 575, 0, 11, 8, 0.5f),
                                                                0.7f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 575, 3, 11, 8, 0.5f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1)),
                                                owner.emitParticles(-1, 575, 4, 11, 8, 0.5f),
                                                0.8f)
                                        .append(owner.emitParticles(-1, 575, 1, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 575, 0, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
