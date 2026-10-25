package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 闪焰冲锋（招式 ID：394）的攻击／事件阶段动画。
 *
 * <p>招式描述：让火焰覆盖全身猛撞向对手. 自己也会受到不小的伤害. 有时会让对手陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110394（名称）和 120394（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/FlareBlitzAnimation.java
 *
 * <p>源文件 SHA-256：848d5b387fb1b74159adfb4e78735f42bbb6e407119a5d481e5b281c55042753
 */
final class ReferenceMoveProgram394 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1425,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        14,
                                                        1000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 960.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 640.0f, 320.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        2,
                                                        14,
                                                        666.6667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        2,
                                                        14,
                                                        833.3333f,
                                                        0.3125f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(569)),
                                owner.emitParticles(-1, 569, 0, 9, 8, 0.5f),
                                0.6f)
                        .append(owner.shakeCombatant(14, 2, 1, 0.032f, 0.048f, 1.0f, 0.0f))
                        .onStart(owner.loadBattleBackground(24))
                        .append(owner.tintEnvironment(3, 0, 0.0f, 0.8125f, 0.025f));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep14 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep6.onStart(
                                                ReferenceMoveTimeline.callback(sceneFlagAction2)),
                                        owner.tintEnvironment(2, 0, 0.0f, 0.8125f, 0.05f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1425,
                                                0,
                                                14,
                                                0.0f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(0, 0, 0.0f, 960.0f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1376,
                                                0,
                                                16,
                                                1000.0f,
                                                0.0f,
                                                owner.casterHandle))
                                .append(owner.focusCamera(1, 0.4f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1505,
                                                2,
                                                16,
                                                833.3333f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1475,
                                                1,
                                                16,
                                                833.3333f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1475,
                                                1,
                                                16,
                                                1083.3334f,
                                                0.46875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1475,
                                                1,
                                                16,
                                                1333.3334f,
                                                0.15625f,
                                                owner.casterHandle))
                                .append(owner.emitParticles(-1, 569, 1, 11, 8, 0.5f)),
                        owner.emitParticles(-1, 569, 2, 11, 8, 0.5f),
                        0.6f);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Color color = EncounterMoveAnimation.color(0);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, true);
        ReferenceMoveTimeline animationStep15 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        animationStep14
                                                                .onStart(
                                                                        ReferenceMoveTimeline
                                                                                .callback(
                                                                                        sceneFlagAction3))
                                                                .onStart(
                                                                        ReferenceMoveTimeline
                                                                                .callback(
                                                                                        owner
                                                                                                .sceneFlag(
                                                                                                        1,
                                                                                                        false)))
                                                                .endGroup()
                                                                .endGroup()
                                                                .endGroup()
                                                                .beginParallel(),
                                                        owner.tintEnvironment(
                                                                3, 0, 0.8125f, 0.0f, 0.075f),
                                                        0.04f)
                                                .append(
                                                        owner.tintCombatant(
                                                                16, 0.25f, 0.0f, 0.8125f, color)),
                                        owner.shakeCombatant(
                                                16, 2, 2, 0.016f, 0.032f, 0.30004883f, 0.0f)),
                                owner.tintEnvironment(3, 0, 0.0f, 0.8125f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)));
        color = EncounterMoveAnimation.color(0);
        result =
                animationStep15
                        .append(owner.tintCombatant(16, 0.25f, 0.8125f, 0.0f, color))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.025f))
                        .append(owner.restoreCamera(0.3f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
