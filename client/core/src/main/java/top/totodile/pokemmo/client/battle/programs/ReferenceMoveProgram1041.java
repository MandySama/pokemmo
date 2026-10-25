package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 并肩作战（自定义动画注册 ID：1041）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111041（名称）和 121041（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1041Animation.java
 *
 * <p>源文件 SHA-256：a501bce02b0ff1c4b0baa7c5da5f9f0da1e4f4be7925475e3d8744cb0fe24337
 */
final class ReferenceMoveProgram1041 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result = null;
        block5:
        {
            block3:
            {
                block4:
                {
                    block2:
                    {
                        if (owner.casterType() != 9) break block2;
                        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
                        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
                        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
                        ReferenceMoveTimeline animationStep7 =
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .onStart(owner.loadBattleBackground(1))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction2)),
                                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.05f),
                                                0.2f)
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1487,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 720.0f, 144.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1536,
                                                        1,
                                                        14,
                                                        916.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(708))
                                        .append(owner.emitParticles(-1, 708, 5, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 708, 6, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 708, 2, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 708, 3, 9, 8, 0.5f));
                        Color color = EncounterMoveAnimation.color(31);
                        ReferenceMoveTimeline animationStep8 =
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        animationStep7.append(
                                                owner.emitParticles(-1, 708, 0, 9, 8, 0.5f)),
                                        owner.tintCombatant(14, 1.0f, 0.0f, 0.625f, color),
                                        1.8f);
                        color = EncounterMoveAnimation.color(31);
                        ReferenceMoveTimeline animationStep15 =
                                animationStep8
                                        .append(owner.tintCombatant(14, 1.0f, 0.625f, 0.0f, color))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(2.2f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1460,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1536,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1460,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1536,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1460,
                                                        2,
                                                        16,
                                                        666.6667f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1536,
                                                        2,
                                                        16,
                                                        833.3333f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1895,
                                                        1,
                                                        16,
                                                        833.3333f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.128f,
                                                        0.39990234f,
                                                        -0.39990234f))
                                        .append(owner.setSpriteAnimationPaused(16, 1));
                        Color color2 = EncounterMoveAnimation.color(31);
                        ReferenceMoveTimeline animationStep16 =
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                animationStep15,
                                                owner.emitParticles(-1, 708, 1, 11, 8, 0.375f),
                                                2.4f)
                                        .append(owner.tintCombatant(16, 1.0f, 0.0f, 0.625f, color2))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        9,
                                                        0.0f,
                                                        0.064f,
                                                        0.30004883f,
                                                        -0.30004883f));
                        Color color3 = EncounterMoveAnimation.color(31);
                        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
                        result =
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                                animationStep16
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 708, 4,
                                                                                        11, 8,
                                                                                        0.25f))
                                                                        .append(
                                                                                owner
                                                                                        .tintEnvironment(
                                                                                                3,
                                                                                                0,
                                                                                                1.0f,
                                                                                                0.0f,
                                                                                                0.075f)),
                                                                owner.scrollBattleBackground(
                                                                        2, 6, 0, 0.032f))
                                                        .append(
                                                                owner.tintCombatant(
                                                                        16, 1.0f, 0.625f, 0.0f,
                                                                        color3)),
                                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.075f))
                                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.075f))
                                        .append(owner.restoreCamera(0.4f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                                        .append(owner.setSpriteAnimationPaused(16, 0))
                                        .onStart(
                                                ReferenceMoveTimeline.callback(
                                                        owner.sceneFlag(1, true)))
                                        .endGroup()
                                        .beginParallel()
                                        .endGroup();
                        break block3;
                    }
                    if (owner.casterType() != 11) break block4;
                    Runnable sceneFlagAction6 = owner.sceneFlag(2, true);
                    Runnable sceneFlagAction7 = owner.sceneFlag(0, false);
                    Runnable sceneFlagAction8 = owner.sceneFlag(1, false);
                    ReferenceMoveTimeline animationStep35 =
                            ReferenceMoveTimeline.appendThenOuterParallel(
                                            ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                    ReferenceMoveTimeline
                                                            .appendThenDelayedOuterParallel(
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
                                                                                                                                    sceneFlagAction6)),
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
                                                                                                                    sceneFlagAction7))
                                                                                            .onStart(
                                                                                                    ReferenceMoveTimeline
                                                                                                            .callback(
                                                                                                                    sceneFlagAction8))
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
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    510,
                                                                                                    8,
                                                                                                    9,
                                                                                                    8,
                                                                                                    0.0f),
                                                                                    0.64f)
                                                                            .append(
                                                                                    owner
                                                                                            .focusCamera(
                                                                                                    1,
                                                                                                    0.4f))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1416,
                                                                                                    1,
                                                                                                    16,
                                                                                                    0.0f,
                                                                                                    0.8984375f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1376,
                                                                                                    1,
                                                                                                    16,
                                                                                                    1666.6666f,
                                                                                                    0.0f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .adjustSoundTrack(
                                                                                                    1,
                                                                                                    1,
                                                                                                    1120.0f,
                                                                                                    480.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1420,
                                                                                                    2,
                                                                                                    16,
                                                                                                    1416.6666f,
                                                                                                    0.8984375f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1420,
                                                                                                    2,
                                                                                                    16,
                                                                                                    1583.3334f,
                                                                                                    0.8984375f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1420,
                                                                                                    2,
                                                                                                    16,
                                                                                                    1833.3334f,
                                                                                                    0.8984375f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1420,
                                                                                                    2,
                                                                                                    16,
                                                                                                    2050.0f,
                                                                                                    0.8984375f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .setSpriteAnimationPaused(
                                                                                                    16,
                                                                                                    1))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    510,
                                                                                                    4,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    510,
                                                                                                    7,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    510,
                                                                                                    2,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    510,
                                                                                                    5,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    510,
                                                                                                    3,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f)),
                                                                    owner.emitParticles(
                                                                            -1, 510, 6, 11, 8,
                                                                            0.0f),
                                                                    2.24f)
                                                            .append(
                                                                    owner.emitParticles(
                                                                            -1, 510, 0, 11, 8,
                                                                            0.25f)),
                                                    owner.shakeCombatant(
                                                            16,
                                                            2,
                                                            6,
                                                            0.032f,
                                                            0.016f,
                                                            0.30004883f,
                                                            0.0f),
                                                    3.04f),
                                            owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f))
                                    .append(owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.05f));
                    Runnable sceneFlagAction9 = owner.sceneFlag(1, true);
                    result =
                            animationStep35
                                    .onStart(ReferenceMoveTimeline.callback(sceneFlagAction9))
                                    .onStart(
                                            ReferenceMoveTimeline.callback(
                                                    owner.sceneFlag(0, true)))
                                    .append(owner.restoreCamera(0.4f))
                                    .append(owner.setSpriteAnimationPaused(14, 0))
                                    .append(owner.setSpriteAnimationPaused(16, 0))
                                    .endGroup()
                                    .beginParallel()
                                    .endGroup();
                    break block3;
                }
                if (owner.casterType() != 10) break block5;
                ReferenceMoveTimeline animationStep42 =
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(0, 0.4f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1418,
                                                                3,
                                                                14,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                3,
                                                                14,
                                                                2166.6667f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(owner.adjustSoundTrack(3, 0, 0.0f, 960.0f))
                                                .append(
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.9375f, 0.075f))
                                                .onStart(owner.preloadParticleEffect(475)),
                                        owner.emitParticles(-1, 475, 3, 9, 8, 0.5f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1450,
                                                1,
                                                14,
                                                0.0f,
                                                0.78125f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1376,
                                                1,
                                                14,
                                                1666.6666f,
                                                0.0f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1407,
                                                2,
                                                14,
                                                0.0f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1376,
                                                2,
                                                14,
                                                1666.6666f,
                                                0.0f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(2, 0, 0.0f, 800.0f))
                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 800.0f))
                                .append(owner.adjustSoundTrack(1, 1, 1120.0f, 480.0f))
                                .append(owner.adjustSoundTrack(2, 1, 1120.0f, 480.0f))
                                .append(owner.launchParticles(475, 0, 9, 11, 0.25f, 0.0f))
                                .append(owner.launchParticles(475, 1, 9, 11, 0.25f, 0.0f))
                                .append(owner.launchParticles(475, 2, 9, 11, 0.25f, 0.0f))
                                .onStart(owner.loadBattleBackground(30));
                Runnable sceneFlagAction10 = owner.sceneFlag(2, true);
                Runnable sceneFlagAction11 = owner.sceneFlag(0, false);
                Runnable sceneFlagAction12 = owner.sceneFlag(1, false);
                Runnable sceneFlagAction13 = owner.sceneFlag(0, true);
                result =
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.appendThenOuterParallel(
                                                animationStep42
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction10))
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.9375f, 0.0f,
                                                                        0.025f))
                                                        .append(
                                                                owner.scrollBattleBackground(
                                                                        4, -32, 0, 1.6f))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction11))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction12))
                                                        .beginSequence()
                                                        .waitSeconds(0.4f)
                                                        .beginParallel()
                                                        .append(owner.restoreCamera(0.4f))
                                                        .onStart(owner.setHealthHudHidden(14, true))
                                                        .onStart(owner.setHealthHudHidden(16, true))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1)),
                                                owner.shakeCombatant(
                                                        16, 2, 8, 0.016f, 0.032f, -0.5f, 0.0f)),
                                        owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f))
                                .onStart(owner.setHealthHudHidden(14, false))
                                .onStart(owner.setHealthHudHidden(16, false))
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction13))
                                .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                                .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .endGroup()
                                .beginParallel()
                                .endGroup();
            }
            return result;
        }
        return result;
    }
}
