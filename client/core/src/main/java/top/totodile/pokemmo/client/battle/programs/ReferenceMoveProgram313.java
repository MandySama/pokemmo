package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 假哭（招式 ID：313）的攻击／事件阶段动画。
 *
 * <p>招式描述：装哭流泪. 使对手不知所措, 从而大幅降低对手的特防.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110313（名称）和 120313（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/FakeTearsAnimation.java
 *
 * <p>源文件 SHA-256：9f6c36284810403d54a2167da401b1faf96d60e6c923547d53866f296d895612
 */
final class ReferenceMoveProgram313 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1508,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1508,
                                                        2,
                                                        14,
                                                        416.66666f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1508,
                                                        2,
                                                        14,
                                                        833.3333f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(484))
                                        .append(owner.emitParticles(-1, 484, 0, 9, 8, 0.25f)),
                                owner.emitParticles(-1, 484, 1, 9, 8, 0.25f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
