package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 疯狂植物（招式 ID：338）的攻击／事件阶段动画。
 *
 * <p>招式描述：用大树摔打对手进行攻击. 下一回合自己将无法动弹.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110338（名称）和 120338（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/FrenzyPlantAnimation.java
 *
 * <p>源文件 SHA-256：44e2c760bd4c7b2217f7f1ba8ec98e515406117e21ee975229217e7e56c336b3
 */
final class ReferenceMoveProgram338 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep20 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenDelayedParallel(
                                                                        ReferenceMoveTimeline
                                                                                .appendThenNextParallel(
                                                                                        ReferenceMoveTimeline
                                                                                                .sequence()
                                                                                                .beginParallel()
                                                                                                .append(
                                                                                                        owner
                                                                                                                .focusCamera(
                                                                                                                        0,
                                                                                                                        0.4f))
                                                                                                .onStart(
                                                                                                        owner
                                                                                                                .loadBattleBackground(
                                                                                                                        27))
                                                                                                .onStart(
                                                                                                        ReferenceMoveTimeline
                                                                                                                .callback(
                                                                                                                        sceneFlagAction2)),
                                                                                        owner
                                                                                                .tintEnvironment(
                                                                                                        4,
                                                                                                        0,
                                                                                                        0.0f,
                                                                                                        0.9375f,
                                                                                                        0.05f))
                                                                                .append(
                                                                                        owner
                                                                                                .scrollBattleBackground(
                                                                                                        4,
                                                                                                        0,
                                                                                                        12,
                                                                                                        3.2f))
                                                                                .onStart(
                                                                                        ReferenceMoveTimeline
                                                                                                .callback(
                                                                                                        sceneFlagAction3))
                                                                                .onStart(
                                                                                        ReferenceMoveTimeline
                                                                                                .callback(
                                                                                                        sceneFlagAction4))
                                                                                .append(
                                                                                        owner
                                                                                                .tintEnvironment(
                                                                                                        4,
                                                                                                        0,
                                                                                                        0.9375f,
                                                                                                        0.0f,
                                                                                                        0.05f))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1708,
                                                                                                        1,
                                                                                                        14,
                                                                                                        0.0f,
                                                                                                        0.8984375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1475,
                                                                                                        2,
                                                                                                        14,
                                                                                                        283.33334f,
                                                                                                        0.9375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1418,
                                                                                                        3,
                                                                                                        14,
                                                                                                        333.33334f,
                                                                                                        0.8984375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1376,
                                                                                                        3,
                                                                                                        14,
                                                                                                        2500.0f,
                                                                                                        0.0f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .adjustSoundTrack(
                                                                                                        3,
                                                                                                        1,
                                                                                                        1920.0f,
                                                                                                        480.0f))
                                                                                .append(
                                                                                        owner
                                                                                                .setSpriteAnimationPaused(
                                                                                                        14,
                                                                                                        1))
                                                                                .append(
                                                                                        owner
                                                                                                .shakeCombatant(
                                                                                                        14,
                                                                                                        2,
                                                                                                        1,
                                                                                                        0.016f,
                                                                                                        0.032f,
                                                                                                        0.0f,
                                                                                                        0.5f))
                                                                                .append(
                                                                                        owner
                                                                                                .scaleCombatant(
                                                                                                        14,
                                                                                                        3,
                                                                                                        1,
                                                                                                        0.016f,
                                                                                                        0.032f,
                                                                                                        -0.30004883f,
                                                                                                        0.30004883f))
                                                                                .onStart(
                                                                                        owner
                                                                                                .preloadParticleEffect(
                                                                                                        510))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        510,
                                                                                                        1,
                                                                                                        9,
                                                                                                        8,
                                                                                                        0.0f)),
                                                                        owner.emitParticles(
                                                                                -1, 510, 8, 9, 8,
                                                                                0.0f),
                                                                        0.64f)
                                                                .append(owner.focusCamera(1, 0.4f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1416,
                                                                                1,
                                                                                16,
                                                                                0.0f,
                                                                                0.8984375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1376,
                                                                                1,
                                                                                16,
                                                                                1666.6666f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.adjustSoundTrack(
                                                                                1, 1, 1120.0f,
                                                                                480.0f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1420,
                                                                                2,
                                                                                16,
                                                                                1416.6666f,
                                                                                0.8984375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1420,
                                                                                2,
                                                                                16,
                                                                                1583.3334f,
                                                                                0.8984375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1420,
                                                                                2,
                                                                                16,
                                                                                1833.3334f,
                                                                                0.8984375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1420,
                                                                                2,
                                                                                16,
                                                                                2050.0f,
                                                                                0.8984375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        16, 1))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 510, 4, 11, 8,
                                                                                0.0f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 510, 7, 11, 8,
                                                                                0.0f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 510, 2, 11, 8,
                                                                                0.0f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 510, 5, 11, 8,
                                                                                0.0f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 510, 3, 11, 8,
                                                                                0.0f)),
                                                        owner.emitParticles(
                                                                -1, 510, 6, 11, 8, 0.0f),
                                                        2.24f)
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 510, 0, 11, 8, 0.25f)),
                                        owner.shakeCombatant(
                                                16, 2, 6, 0.032f, 0.016f, 0.30004883f, 0.0f),
                                        3.04f),
                                owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f))
                        .append(owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.05f));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        result =
                animationStep20
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
