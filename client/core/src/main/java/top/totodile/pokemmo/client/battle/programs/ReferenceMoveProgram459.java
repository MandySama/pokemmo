package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 时光咆哮（招式 ID：459）的攻击／事件阶段动画。
 *
 * <p>招式描述：释放出扭曲时间般的强大力量攻击对手. 下一回合自己将无法动弹.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110459（名称）和 120459（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/RoarOfTimeAnimation.java
 *
 * <p>源文件 SHA-256：0479090c559af7261f82046bac003b8b4df07de3a759de9a1b590bbd1ed96562
 */
final class ReferenceMoveProgram459 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(0, 0 != 0);
        ReferenceMoveTimeline animationStep17 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner
                                                                                        .tintEnvironment(
                                                                                                4,
                                                                                                0,
                                                                                                0.0f,
                                                                                                0.8125f,
                                                                                                0.075f))
                                                                        .onStart(
                                                                                ReferenceMoveTimeline
                                                                                        .callback(
                                                                                                sceneFlagAction2))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1487,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.78125f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                0,
                                                                                                0.0f,
                                                                                                480.0f))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                634))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 634, 3,
                                                                                        9, 8,
                                                                                        0.5f)),
                                                                owner.emitParticles(
                                                                        -1, 634, 4, 9, 8, 0.5f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1473,
                                                                        2,
                                                                        14,
                                                                        333.33334f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1941,
                                                                        1,
                                                                        14,
                                                                        1000.0f,
                                                                        0.9765625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1907,
                                                                        2,
                                                                        16,
                                                                        2000.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 634, 0, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 634, 5, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 634, 6, 9, 8, 0.5f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .beginSequence()
                                        .waitSeconds(1.2f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        1,
                                                        16,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        1,
                                                        16,
                                                        666.6667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 634, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 634, 2, 11, 8, 0.5f)),
                                owner.shakeCombatant(16, 2, 6, 0.0f, 0.032f, 0.30004883f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(16, 0));
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 1 != 0);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep17.onStart(ReferenceMoveTimeline.callback(sceneFlagAction3)),
                        owner.scheduleSound((byte) 2, 1376, 2, 16, 0.0f, 0.0f, owner.casterHandle));
        return result;
    }
}
