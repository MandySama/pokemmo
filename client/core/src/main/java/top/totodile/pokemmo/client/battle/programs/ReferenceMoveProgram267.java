package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 自然之力（招式 ID：267）的攻击／事件阶段动画。
 *
 * <p>招式描述：用自然之力进行攻击. 根据所使用场所的不同, 使出的招式也会有所变化.
 *
 * <p>动画说明：按参考源码构建此事件的时间线。
 *
 * <p>编号对应参考动画注册表；语言条目为 110267（名称）和 120267（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/NaturePowerAnimation.java
 *
 * <p>源文件 SHA-256：ea1e29c124c27507b3cec24b1257efc99b9404c22dd586420e83ddb5514cc19d
 */
final class ReferenceMoveProgram267 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result = ReferenceMoveTimeline.sequence().beginParallel().endGroup();
        return result;
    }
}
