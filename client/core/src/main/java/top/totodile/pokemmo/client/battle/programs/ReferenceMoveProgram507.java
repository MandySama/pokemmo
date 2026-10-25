package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 自由落体（招式 ID：507）的攻击／事件阶段动画。
 *
 * <p>招式描述：第1回合将对手带到空中, 第2回合将其摔下进行攻击. 被带到空中的对手不能动弹.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵显隐；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110507（名称）和 120507（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/SkyDropAnimation.java
 *
 * <p>源文件 SHA-256：cc754bd375ea02b26261ff1c0e393004aa41a1926a08fd54bd24b8b476681941
 */
final class ReferenceMoveProgram507 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1459,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        533.3333f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(181))
                                        .append(owner.emitParticles(-1, 181, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 181, 1, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 181, 2, 11, 8, 0.5f),
                                0.4f)
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 4, 0.0f, 0.064f, 0.30004883f, -0.30004883f))
                        .append(owner.setCombatantVisibility(16, 4))
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
