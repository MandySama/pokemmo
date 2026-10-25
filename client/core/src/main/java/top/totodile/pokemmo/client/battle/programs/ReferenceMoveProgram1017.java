package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 蹦得再高点!（自定义动画注册 ID：1017）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵显隐；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111017（名称）和 121017（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1017Animation.java
 *
 * <p>源文件 SHA-256：652ca12b085e92e4c5a2edba4116bbb255e54acf51e6f694567741971d1d959e
 */
final class ReferenceMoveProgram1017 {

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
                                                        .onStart(owner.preloadParticleEffect(256)),
                                                owner.emitParticles(-1, 512, 0, 11, 8, 0.25f),
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
                                        .append(owner.emitParticles(-1, 512, 1, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 512, 2, 11, 8, 0.5f),
                                0.2f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        0.0f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        666.6667f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        833.3333f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        1166.6666f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        1333.3334f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1416,
                                        1,
                                        16,
                                        0.0f,
                                        0.9765625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1418,
                                        3,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        16,
                                        1833.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        3,
                                        16,
                                        1833.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(3, 1, 1280.0f, 320.0f))
                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 800.0f))
                        .append(owner.adjustSoundTrack(1, 1, 960.0f, 800.0f))
                        .append(owner.emitParticles(-1, 256, 0, 11, 8, 0.0f))
                        .append(owner.emitParticles(-1, 256, 1, 11, 8, 0.0f))
                        .append(owner.emitParticles(-1, 256, 1, 11, 8, 0.0f))
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
