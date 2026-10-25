package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 蹦得真高啊!（自定义动画注册 ID：1018）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵显隐；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111018（名称）和 121018（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1018Animation.java
 *
 * <p>源文件 SHA-256：098be0e43201c9aa1ba7ec736c85db5d54e6b45406840d29c058d70772e39803
 */
final class ReferenceMoveProgram1018 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1407,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1705,
                                                                        1,
                                                                        16,
                                                                        500.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 0, 0.0f, 480.0f))
                                                        .onStart(owner.preloadParticleEffect(512))
                                                        .onStart(owner.preloadParticleEffect(285)),
                                                owner.emitParticles(-1, 512, 0, 11, 11, 0.25f),
                                                0.4f)
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        2,
                                                        0.0f,
                                                        0.064f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(owner.emitParticles(-1, 512, 1, 11, 11, 0.5f)),
                                owner.emitParticles(-1, 512, 2, 11, 11, 0.5f),
                                0.2f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        166.66667f,
                                        0.78125f,
                                        owner.targetHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        333.33334f,
                                        0.703125f,
                                        owner.targetHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        500.0f,
                                        0.7421875f,
                                        owner.targetHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        666.6667f,
                                        0.78125f,
                                        owner.targetHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        833.3333f,
                                        0.703125f,
                                        owner.targetHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        1000.0f,
                                        0.7421875f,
                                        owner.targetHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        1166.6666f,
                                        0.9921875f,
                                        owner.targetHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        1166.6666f,
                                        0.9921875f,
                                        owner.targetHandle))
                        .append(owner.emitParticles(-1, 285, 1, 11, 11, 0.5f))
                        .append(owner.emitParticles(-1, 285, 2, 11, 11, 0.5f))
                        .append(owner.emitParticles(-1, 285, 3, 11, 11, 0.5f))
                        .endGroup()
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        owner.chargeCount = 0;
        return result;
    }
}
