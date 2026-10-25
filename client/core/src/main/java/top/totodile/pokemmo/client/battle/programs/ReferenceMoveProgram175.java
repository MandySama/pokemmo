package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 抓狂（招式 ID：175）的攻击／事件阶段动画。
 *
 * <p>招式描述：抓狂般乱打进行攻击. 自己的HP越少, 招式的威力越大.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110175（名称）和 120175（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/FlailAnimation.java
 *
 * <p>源文件 SHA-256：ba3efad51425d011d1d7dac6af7c7b355f91dfb50b4c6b8268442fab622617b4
 */
final class ReferenceMoveProgram175 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .endGroup()
                                                        .beginParallel()
                                                        .waitSeconds(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1726,
                                                                        3,
                                                                        14,
                                                                        0.0f,
                                                                        0.546875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1726,
                                                                        3,
                                                                        14,
                                                                        666.6667f,
                                                                        0.546875f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(342))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 342, 0, 9, 8, 0.5f)),
                                                owner.shakeCombatant(
                                                        14, 2, 4, 0.016f, 0.064f, 2.0f, 0.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        266.66666f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .beginSequence()
                                        .waitSeconds(0.32f)
                                        .beginParallel()
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.19995117f,
                                                        -0.19995117f)),
                                owner.emitParticles(-1, 342, 1, 11, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
