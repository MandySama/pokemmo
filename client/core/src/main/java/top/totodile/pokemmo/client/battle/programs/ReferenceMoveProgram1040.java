package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 为友而战（自定义动画注册 ID：1040）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111040（名称）和 121040（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1040Animation.java
 *
 * <p>源文件 SHA-256：8cffb54370efeda1dd07acf339766c4b56aab89008d38de5bc07f40b256ad425
 */
final class ReferenceMoveProgram1040 {

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
                        ReferenceMoveTimeline animationStep7 =
                                ReferenceMoveTimeline.sequence()
                                        .onStart(owner.preloadParticleEffect(728))
                                        .onStart(owner.preloadParticleEffect(730))
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1589,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1976,
                                                        1,
                                                        14,
                                                        833.3333f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1995,
                                                        2,
                                                        14,
                                                        1333.3334f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1966,
                                                        3,
                                                        14,
                                                        2166.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1885,
                                                        1,
                                                        2,
                                                        2833.3333f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1885,
                                                        2,
                                                        16,
                                                        3000.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.focusCamera(0, 0.6f));
                        Color color = EncounterMoveAnimation.color(31);
                        ReferenceMoveTimeline animationStep16 =
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                animationStep7
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 730, 0, 9, 8, 0.0f))
                                                        .append(
                                                                owner.tintCombatant(
                                                                        14, 0.5f, 0.0f, 0.5f,
                                                                        color))
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.0f, 0.75f, 0.05f))
                                                        .endGroup()
                                                        .beginSequence()
                                                        .append(owner.focusCamera(1, 0.6f)),
                                                owner.emitParticles(-1, 730, 3, 0, 0, 3.25f))
                                        .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.075f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 728, 9, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 728, 10, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 728, 11, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 728, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 728, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 728, 7, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 728, 8, 11, 8, 0.5f));
                        Color color2 = EncounterMoveAnimation.color(18);
                        ReferenceMoveTimeline animationStep17 =
                                animationStep16
                                        .append(owner.emitParticles(-1, 728, 2, 11, 8, 0.5f))
                                        .append(owner.scrollBattleBackground(2, 0, 8, 0.032f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        6,
                                                        0.0f,
                                                        0.032f,
                                                        -0.19995117f,
                                                        0.19995117f))
                                        .append(
                                                owner.tintCombatant(
                                                        16, 0.75f, 0.0f, 0.625f, color2))
                                        .append(
                                                owner.shakeCombatant(
                                                        16, 2, 12, 0.0f, 0.032f, 1.0f, 0.0f))
                                        .onStart(
                                                owner.playCustomParticles(
                                                        "fight_together_fire_secondaries"))
                                        .endGroup()
                                        .beginParallel();
                        color2 = EncounterMoveAnimation.color(18);
                        ReferenceMoveTimeline animationStep18 =
                                animationStep17.append(
                                        owner.tintCombatant(14, 0.75f, 0.625f, 0.0f, color2));
                        color2 = EncounterMoveAnimation.color(18);
                        Runnable sceneFlagAction2 = owner.sceneFlag(0, true);
                        result =
                                animationStep18
                                        .append(
                                                owner.tintCombatant(
                                                        16, 0.75f, 0.625f, 0.0f, color2))
                                        .append(owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.075f))
                                        .endGroup()
                                        .beginSequence()
                                        .beginParallel()
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .onStart(
                                                ReferenceMoveTimeline.callback(
                                                        owner.sceneFlag(1, true)))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.08f)
                                        .beginParallel()
                                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.075f))
                                        .append(owner.setSpriteAnimationPaused(16, 0))
                                        .append(owner.restoreCamera(0.3f))
                                        .onStart(owner.setHealthHudHidden(18, false))
                                        .endGroup()
                                        .endGroup();
                        break block3;
                    }
                    if (owner.casterType() != 10) break block4;
                    Color color = Color.valueOf("#87CEEB");
                    result =
                            ReferenceMoveTimeline.appendThenNextParallel(
                                            ReferenceMoveTimeline.appendThenOuterParallel(
                                                    ReferenceMoveTimeline
                                                            .appendThenDelayedOuterParallel(
                                                                    ReferenceMoveTimeline.sequence()
                                                                            .onStart(
                                                                                    owner
                                                                                            .preloadParticleEffect(
                                                                                                    475))
                                                                            .onStart(
                                                                                    owner
                                                                                            .preloadParticleEffect(
                                                                                                    589))
                                                                            .beginParallel()
                                                                            .append(
                                                                                    owner
                                                                                            .focusCamera(
                                                                                                    0,
                                                                                                    0.4f))
                                                                            .append(
                                                                                    owner
                                                                                            .tintCombatant(
                                                                                                    14,
                                                                                                    0.75f,
                                                                                                    0.0f,
                                                                                                    0.625f,
                                                                                                    color))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1418,
                                                                                                    0,
                                                                                                    14,
                                                                                                    0.0f,
                                                                                                    0.9375f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1376,
                                                                                                    0,
                                                                                                    14,
                                                                                                    2166.6667f,
                                                                                                    0.0f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .adjustSoundTrack(
                                                                                                    3,
                                                                                                    0,
                                                                                                    0.0f,
                                                                                                    960.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .tintEnvironment(
                                                                                                    4,
                                                                                                    0,
                                                                                                    0.0f,
                                                                                                    0.4375f,
                                                                                                    0.075f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    475,
                                                                                                    3,
                                                                                                    9,
                                                                                                    8,
                                                                                                    0.5f))
                                                                            .endGroup()
                                                                            .waitSeconds(0.4f)
                                                                            .beginParallel()
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1450,
                                                                                                    1,
                                                                                                    14,
                                                                                                    0.0f,
                                                                                                    0.78125f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1376,
                                                                                                    1,
                                                                                                    14,
                                                                                                    1666.6666f,
                                                                                                    0.0f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1407,
                                                                                                    2,
                                                                                                    14,
                                                                                                    0.0f,
                                                                                                    0.9375f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1376,
                                                                                                    2,
                                                                                                    14,
                                                                                                    1666.6666f,
                                                                                                    0.0f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .adjustSoundTrack(
                                                                                                    2,
                                                                                                    0,
                                                                                                    0.0f,
                                                                                                    800.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .adjustSoundTrack(
                                                                                                    1,
                                                                                                    0,
                                                                                                    0.0f,
                                                                                                    800.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .adjustSoundTrack(
                                                                                                    1,
                                                                                                    1,
                                                                                                    1120.0f,
                                                                                                    480.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .adjustSoundTrack(
                                                                                                    2,
                                                                                                    1,
                                                                                                    1120.0f,
                                                                                                    480.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .launchParticles(
                                                                                                    475,
                                                                                                    0,
                                                                                                    9,
                                                                                                    11,
                                                                                                    0.25f,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .launchParticles(
                                                                                                    475,
                                                                                                    1,
                                                                                                    9,
                                                                                                    11,
                                                                                                    0.25f,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .launchParticles(
                                                                                                    475,
                                                                                                    2,
                                                                                                    9,
                                                                                                    11,
                                                                                                    0.25f,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1475,
                                                                                                    3,
                                                                                                    16,
                                                                                                    166.66667f,
                                                                                                    0.9375f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .scheduleSound(
                                                                                                    (byte)
                                                                                                            2,
                                                                                                    (short)
                                                                                                            1475,
                                                                                                    3,
                                                                                                    16,
                                                                                                    1166.6666f,
                                                                                                    0.9375f,
                                                                                                    owner.casterHandle))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    589,
                                                                                                    1,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    589,
                                                                                                    4,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    589,
                                                                                                    7,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    589,
                                                                                                    10,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    589,
                                                                                                    13,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .shakeCombatant(
                                                                                                    16,
                                                                                                    2,
                                                                                                    2,
                                                                                                    0.016f,
                                                                                                    0.032f,
                                                                                                    0.5f,
                                                                                                    0.0f))
                                                                            .onStart(
                                                                                    owner
                                                                                            .playCustomParticles(
                                                                                                    "fight_together_water_secondaries"))
                                                                            .beginSequence()
                                                                            .waitSeconds(1.5f)
                                                                            .beginParallel()
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    589,
                                                                                                    2,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    589,
                                                                                                    5,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    589,
                                                                                                    8,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    589,
                                                                                                    11,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .emitParticles(
                                                                                                    -1,
                                                                                                    589,
                                                                                                    14,
                                                                                                    11,
                                                                                                    8,
                                                                                                    0.0f))
                                                                            .append(
                                                                                    owner
                                                                                            .shakeCombatant(
                                                                                                    16,
                                                                                                    2,
                                                                                                    2,
                                                                                                    0.016f,
                                                                                                    0.032f,
                                                                                                    0.5f,
                                                                                                    0.0f)),
                                                                    owner.scaleCombatant(
                                                                            16,
                                                                            2,
                                                                            4,
                                                                            0.016f,
                                                                            0.032f,
                                                                            0.100097656f,
                                                                            -0.100097656f),
                                                                    0.4f)
                                                            .append(owner.restoreCamera(0.4f))
                                                            .append(
                                                                    owner.setSpriteAnimationPaused(
                                                                            16, 1))
                                                            .append(
                                                                    owner.shakeCombatant(
                                                                            16, 2, 8, 0.016f,
                                                                            0.032f, -0.5f, 0.0f)),
                                                    owner.tintCombatant(
                                                            14, 0.75f, 0.625f, 0.0f, color)),
                                            owner.tintEnvironment(4, 0, 0.4375f, 0.0f, 0.025f))
                                    .append(owner.setSpriteAnimationPaused(16, 0))
                                    .endGroup();
                    break block3;
                }
                if (owner.casterType() != 11) break block5;
                Color color = Color.valueOf("#DAA520");
                result =
                        ReferenceMoveTimeline.sequence()
                                .beginParallel()
                                .append(owner.tintCombatant(14, 0.75f, 0.0f, 0.625f, color))
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
                                .onStart(owner.playCustomParticles("fight_together_grass"))
                                .endGroup()
                                .waitSeconds(0.7f)
                                .beginParallel()
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
                                .append(owner.focusCamera(1, 0.8f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1475,
                                                5,
                                                16,
                                                166.66667f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1475,
                                                4,
                                                16,
                                                166.6666f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .endGroup()
                                .beginParallel()
                                .append(
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                4,
                                                0.016f,
                                                0.032f,
                                                0.100097656f,
                                                -0.100097656f))
                                .append(owner.tintCombatant(14, 0.75f, 0.625f, 0.0f, color))
                                .endGroup()
                                .waitSeconds(1.0f)
                                .beginParallel()
                                .append(owner.restoreCamera(0.4f))
                                .append(owner.tintEnvironment(4, 0, 0.4375f, 0.0f, 0.025f))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .endGroup();
            }
            return result;
        }
        return result;
    }
}
