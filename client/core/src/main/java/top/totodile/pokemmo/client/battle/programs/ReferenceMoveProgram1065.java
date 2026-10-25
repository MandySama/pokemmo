package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 乱流发动（自定义动画注册 ID：1065）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111065（名称）和 121065（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/TwisterAnimation.java
 *
 * <p>源文件 SHA-256：ca9404177f3de585784165379cd6041eb2edd200564810c96564feb82e57a424
 */
final class ReferenceMoveProgram1065 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1717,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 160.0f, 1600.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 800.0f, 1600.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        16,
                                                        2500.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1497,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 160.0f, 1600.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 800.0f, 1600.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        2500.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(406))
                                        .append(owner.emitParticles(-1, 406, 0, 11, 8, 0.125f)),
                                owner.emitParticles(-1, 406, 1, 11, 8, 0.0f),
                                0.8f)
                        .append(owner.shakeCombatant(16, 2, 4, 0.016f, 0.048f, 1.0f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
