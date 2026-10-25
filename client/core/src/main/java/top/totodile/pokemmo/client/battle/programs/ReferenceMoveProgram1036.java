package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 龙呈祥（自定义动画注册 ID：1036）的攻击／事件阶段动画。
 *
 * <p>招式描述：提升使用者和所有队友的攻击和特攻.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111036（名称）和 121036（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1036Animation.java
 *
 * <p>源文件 SHA-256：96c931c341a1a2f87be59ea0ee1808d59bcedbe410916acdaef703279f13a901
 */
final class ReferenceMoveProgram1036 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .append(owner.focusCamera(0, 0.4f))
                        .beginParallel()
                        .onStart(owner.playCustomParticles("1036"))
                        .append(
                                owner.scheduleSound(
                                        (byte) 10,
                                        (short) 8,
                                        2,
                                        14,
                                        700.0f,
                                        0.8f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1474, 1, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1418, 3, 14, 0.0f, 0.859375f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 800.0f))
                        .append(owner.adjustSoundTrack(3, 0, 0.0f, 800.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 3, 14, 1000.0f, 0.0f, owner.casterHandle))
                        .endGroup()
                        .waitSeconds(1.35f)
                        .append(owner.restoreCamera(0.4f));
        return result;
    }
}
