package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 金属残片（自定义动画注册 ID：1079）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦场地位置；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111079（名称）和 121079（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1079Animation.java
 *
 * <p>源文件 SHA-256：db80b85ac4ea1173c7f3620a0b9a43a5abd24b04a42f352572f3156c763bcc51
 */
final class ReferenceMoveProgram1079 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(18, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        1,
                                        14,
                                        166.66667f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        1,
                                        14,
                                        333.33334f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        1,
                                        14,
                                        500.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1436,
                                        2,
                                        16,
                                        500.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1436,
                                        2,
                                        16,
                                        666.6667f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1436,
                                        2,
                                        16,
                                        833.3333f,
                                        0.78125f,
                                        owner.casterHandle))
                        .onStart(owner.playCustomParticles(1079))
                        .append(owner.focusCamera(4, 0.8f))
                        .endGroup()
                        .waitSeconds(1.5f)
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(18, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup();
        return result;
    }
}
