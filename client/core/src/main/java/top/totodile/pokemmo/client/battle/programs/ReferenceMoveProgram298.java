package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 摇晃舞（招式 ID：298）的攻击／事件阶段动画。
 *
 * <p>招式描述：摇摇晃晃地跳起舞蹈, 让自己周围的宝可梦陷入混乱状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110298（名称）和 120298（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/TeeterDanceAnimation.java
 *
 * <p>源文件 SHA-256：c5376a30baf6d8923c5b06de766644a83f684d759fb39e2a7210a60182f2965a
 */
final class ReferenceMoveProgram298 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1506,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1452,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1452,
                                                        1,
                                                        14,
                                                        500.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.shakeCombatant(
                                                        14, 2, 3, 0.016f, 0.096f, 1.0f, 0.0f))
                                        .onStart(owner.preloadParticleEffect(464)),
                                owner.emitParticles(-1, 464, 0, 9, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
