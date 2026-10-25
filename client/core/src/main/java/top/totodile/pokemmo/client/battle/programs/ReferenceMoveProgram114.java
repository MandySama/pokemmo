package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 黑雾（招式 ID：114）的攻击／事件阶段动画。
 *
 * <p>招式描述：升起黑雾, 将正在场上战斗的全体宝可梦的能力变回原点.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110114（名称）和 120114（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/HazeAnimation.java
 *
 * <p>源文件 SHA-256：1812185af49afc1be0b37f4a64d413538eeb31194d3dde585eaa7238a64d7f1c
 */
final class ReferenceMoveProgram114 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.1875f, 0.05f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1417,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        14,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 800.0f, 480.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1418,
                                                        3,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        3,
                                                        14,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(3, 1, 800.0f, 480.0f))
                                        .onStart(owner.preloadParticleEffect(280))
                                        .append(owner.emitParticles(-1, 280, 0, 0, 8, 0.4f)),
                                owner.emitParticles(-1, 280, 1, 0, 8, 0.4f),
                                0.88f)
                        .append(owner.restoreCamera(0.8f))
                        .append(owner.tintEnvironment(4, 0, 0.1875f, 0.0f, 0.05f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
