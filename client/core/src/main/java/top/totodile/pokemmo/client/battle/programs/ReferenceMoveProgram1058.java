package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 连攻（自定义动画注册 ID：1058）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111058（名称）和 121058（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/FocusEnergyAnimation.java
 *
 * <p>源文件 SHA-256：e4fd49899141c4ade20448f2e459da2ed9e758bad97a0e27a8a9689e2f114e54
 */
final class ReferenceMoveProgram1058 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(282))
                                        .append(owner.emitParticles(-1, 282, 0, 9, 8, 0.375f))
                                        .append(owner.emitParticles(-1, 282, 1, 9, 8, 0.375f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1474,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1418,
                                                        3,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 800.0f))
                                        .append(owner.adjustSoundTrack(3, 0, 0.0f, 800.0f)),
                                owner.scheduleSound(
                                        (byte) 2, 1376, 3, 14, 1000.0f, 0.0f, owner.casterHandle))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
