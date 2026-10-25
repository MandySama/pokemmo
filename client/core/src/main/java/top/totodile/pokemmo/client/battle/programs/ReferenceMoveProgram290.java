package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 秘密之力（招式 ID：290）的攻击／事件阶段动画。
 *
 * <p>招式描述：根据使用场所不同, 该招式的追加效果也会有所变化.
 *
 * <p>动画说明：按参考源码构建此事件的时间线。
 *
 * <p>编号对应参考动画注册表；语言条目为 110290（名称）和 120290（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SecretPowerAnimation.java
 *
 * <p>源文件 SHA-256：86b1a9173a4e7f590c11a297a62ed8c649ebabf70707041a66ccd96e1e25725d
 */
final class ReferenceMoveProgram290 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result = owner.buildMoveAnimation(owner.secretPowerMove());
        return result;
    }
}
