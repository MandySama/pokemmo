package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 变身（招式 ID：144）的攻击／事件阶段动画。
 *
 * <p>招式描述：变身成对手宝可梦的样子, 能够使用和对手完全相同的招式.
 *
 * <p>动画说明：按参考源码构建此事件的时间线。
 *
 * <p>编号对应参考动画注册表；语言条目为 110144（名称）和 120144（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/TransformAnimation.java
 *
 * <p>源文件 SHA-256：5e4cdcc7332078b6ec89a6745e4c75dddef64fe08bc3994759c66edc0a67dd97
 */
final class ReferenceMoveProgram144 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result = ReferenceMoveTimeline.sequence().beginParallel().endGroup();
        return result;
    }
}
