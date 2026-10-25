package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 电磁飘浮（招式 ID：393）的攻击／事件阶段动画。
 *
 * <p>招式描述：利用电气产生的磁力浮在空中. 在5回合内可以飘浮.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110393（名称）和 120393（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/MagnetRiseAnimation.java
 *
 * <p>源文件 SHA-256：ea4f0d84bc439125f683dcb31d40a5a8a32168eec1d9bacd1634067b6b56e046
 */
final class ReferenceMoveProgram393 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1625, 1, 14, 0.0f, 0.546875f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1024.0f))
                        .append(owner.adjustSoundTrack(1, 1, 640.0f, 480.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1728,
                                        2,
                                        14,
                                        500.0f,
                                        0.9765625f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 800.0f))
                        .onStart(owner.preloadParticleEffect(568))
                        .append(owner.emitParticles(-1, 568, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 568, 1, 9, 8, 0.5f))
                        .beginSequence()
                        .waitSeconds(0.2f)
                        .beginParallel()
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
