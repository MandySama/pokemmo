package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 毒液冲击（招式 ID：474）的攻击／事件阶段动画。
 *
 * <p>招式描述：将特殊的毒液泼向对手. 对处于中毒状态的对手, 威力会变成2倍.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110474（名称）和 120474（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/VenoshockAnimation.java
 *
 * <p>源文件 SHA-256：7b5b6f1d2df0bef04b106e29c8d0d510c45c795867c36fd5930a84010a908e10
 */
final class ReferenceMoveProgram474 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f))
                                                                        .onStart(
                                                                                owner
                                                                                        .setHealthHudHidden(
                                                                                                18,
                                                                                                true))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                647))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1887,
                                                                                        1,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.625f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1410,
                                                                                        1,
                                                                                        16,
                                                                                        583.3333f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1887,
                                                                                        1,
                                                                                        16,
                                                                                        1166.6666f,
                                                                                        0.78125f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1886,
                                                                                        2,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.234375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        2,
                                                                                        16,
                                                                                        2000.0f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                0,
                                                                                                0.0f,
                                                                                                1920.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                1,
                                                                                                1600.0f,
                                                                                                320.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 647, 1,
                                                                                        11, 8,
                                                                                        0.5f)),
                                                                owner.emitParticles(
                                                                        -1, 647, 0, 11, 8, 0.25f),
                                                                0.4f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1,
                                                                        647,
                                                                        2,
                                                                        11,
                                                                        8,
                                                                        0.049987793f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 647, 3, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 647, 4, 11, 8, 0.25f),
                                                1.2f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 5, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
