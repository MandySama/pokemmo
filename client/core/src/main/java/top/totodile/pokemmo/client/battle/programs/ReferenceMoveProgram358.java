package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 唤醒巴掌（招式 ID：358）的攻击／事件阶段动画。
 *
 * <p>招式描述：给予睡眠状态下的对手较大的伤害. 但相反对手会从睡眠中醒过来.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110358（名称）和 120358（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/WakeUpSlapAnimation.java
 *
 * <p>源文件 SHA-256：13b21b8e7ba77bfd0c4a3a50ce5f349c3256736d8d2ecbdb07cb78cfac012169
 */
final class ReferenceMoveProgram358 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1422,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1471,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1422,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1471,
                                                        2,
                                                        16,
                                                        500.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1422,
                                                        1,
                                                        16,
                                                        1000.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1471,
                                                        2,
                                                        16,
                                                        1000.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1422,
                                                        1,
                                                        16,
                                                        1416.6666f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1471,
                                                        2,
                                                        16,
                                                        1416.6666f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(532))
                                        .append(owner.emitParticles(-1, 532, 0, 11, 8, 0.7999878f))
                                        .append(owner.emitParticles(-1, 532, 1, 11, 8, 0.7999878f))
                                        .append(owner.emitParticles(-1, 532, 2, 11, 8, 0.7999878f))
                                        .append(owner.emitParticles(-1, 532, 3, 11, 8, 0.7999878f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        4,
                                                        0.032f,
                                                        0.064f,
                                                        -0.30004883f,
                                                        0.30004883f)),
                                owner.shakeCombatant(16, 2, 4, 0.032f, 0.064f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
