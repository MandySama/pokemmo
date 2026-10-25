package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 化为己用（招式 ID：289）的蓄力阶段动画。
 *
 * <p>招式描述：将对手打算使用的回复招式或能力变化招式夺为己用.
 *
 * <p>动画说明：按参考源码构建此事件的时间线。
 *
 * <p>编号对应参考动画注册表；语言条目为 110289（名称）和 120289（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SnatchAnimation.java
 *
 * <p>源文件 SHA-256：333ee1ee752cde6e6d911d8f05c9814f94e495bf3da06a71b7231bd42854a660
 */
final class ReferenceChargeProgram289 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        1,
                                        14,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
