package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 剧毒牙（招式 ID：305）的攻击／事件阶段动画。
 *
 * <p>招式描述：用有毒的牙齿咬住对手进行攻击. 有时会使对手中剧毒.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110305（名称）和 120305（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/PoisonFangAnimation.java
 *
 * <p>源文件 SHA-256：ddb548089d89f2dbc1fc6ecf0909bc3565f4c42b06678035b92783e9a359b860
 */
final class ReferenceMoveProgram305 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(1, 0.4f))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1720,
                                                                1,
                                                                16,
                                                                333.33334f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1480,
                                                                2,
                                                                16,
                                                                500.0f,
                                                                0.625f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                2,
                                                                16,
                                                                1166.6666f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                2, 1, 800.0f, 320.0f))
                                                .onStart(owner.preloadParticleEffect(471))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 471, 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 471, 1, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 471, 2, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 471, 3, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 471, 4, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 471, 5, 11, 8, 0.5f),
                                        0.2f),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
