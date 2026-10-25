package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 领导（自定义动画注册 ID：1076）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111076（名称）和 121076（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1076Animation.java
 *
 * <p>源文件 SHA-256：f6cd007228ce710560a0f8d231af6ef8d10adc5131f527a74ef8c2d306a827ca
 */
final class ReferenceMoveProgram1076 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .onStart(owner.playCustomParticles(1076))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1470,
                                        1,
                                        14,
                                        250.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1472,
                                        1,
                                        14,
                                        1200.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1471,
                                        2,
                                        14,
                                        1400.0f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1481,
                                        2,
                                        14,
                                        2400.0f,
                                        0.703125f,
                                        owner.casterHandle))
                        .endGroup()
                        .waitSeconds(2.8f)
                        .append(owner.restoreCamera(0.4f));
        return result;
    }
}
