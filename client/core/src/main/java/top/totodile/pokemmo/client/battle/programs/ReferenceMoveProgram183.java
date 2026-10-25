package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 音速拳（招式 ID：183）的攻击／事件阶段动画。
 *
 * <p>招式描述：以迅雷不及掩耳之势出拳. 必定能够先制攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110183（名称）和 120183（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/MachPunchAnimation.java
 *
 * <p>源文件 SHA-256：df22413dd67f4bb232ed1c94bc7a5db52366ef19dfc20bf72a42527951d5e64e
 */
final class ReferenceMoveProgram183 {

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
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1425,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.859375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                1, 1, 800.0f, 160.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                1,
                                                                16,
                                                                1000.0f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 320.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1421,
                                                                2,
                                                                16,
                                                                333.33334f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                2,
                                                                16,
                                                                500.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(349)),
                                        owner.emitParticles(-1, 349, 3, 11, 8, 0.5f),
                                        0.04f),
                                owner.emitParticles(-1, 349, 1, 11, 8, 0.5f),
                                0.14f)
                        .append(owner.emitParticles(-1, 349, 2, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 349, 0, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.048f, -0.19995117f, 0.19995117f))
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
