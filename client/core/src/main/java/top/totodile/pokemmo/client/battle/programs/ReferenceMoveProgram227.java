package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 再来一次（招式 ID：227）的攻击／事件阶段动画。
 *
 * <p>招式描述：让对手接受再来一次, 连续3次使出最后使用的招式.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110227（名称）和 120227（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/EncoreAnimation.java
 *
 * <p>源文件 SHA-256：e5e442e80f433b4233381c3b32fa3d7bbf29d0268b360b9fb3d90007aa5160e1
 */
final class ReferenceMoveProgram227 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.tintEnvironment(2, 0, 0.0f, 0.625f, 0.075f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1778,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1578,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 1600.0f, 320.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        2000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(393))
                                        .append(owner.emitParticles(-1, 393, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 393, 1, 11, 8, 0.2000122f))
                                        .append(owner.emitParticles(-1, 393, 2, 11, 8, 0.2000122f)),
                                owner.scaleCombatant(
                                        16, 2, 3, 0.0f, 0.16f, -0.30004883f, 0.30004883f))
                        .append(owner.tintEnvironment(2, 0, 0.625f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
