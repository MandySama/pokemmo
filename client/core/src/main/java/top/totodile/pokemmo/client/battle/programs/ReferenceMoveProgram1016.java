package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 召暗（自定义动画注册 ID：1016）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；调整场景着色。
 *
 * <p>编号对应参考动画注册表；语言条目为 111016（名称）和 121016（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1016Animation.java
 *
 * <p>源文件 SHA-256：24edc1e95843e08986059422d5f84b3025df7f6b32c9ed086002be8c7a433876
 */
final class ReferenceMoveProgram1016 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.6f))
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.1f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1680,
                                                        2,
                                                        14,
                                                        166.66667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        2,
                                                        14,
                                                        2000.0f,
                                                        0.0f,
                                                        owner.casterHandle)),
                                owner.adjustSoundTrack(2, 1, 1280.0f, 640.0f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.075f))
                        .waitSeconds(0.6f)
                        .endGroup();
        return result;
    }
}
