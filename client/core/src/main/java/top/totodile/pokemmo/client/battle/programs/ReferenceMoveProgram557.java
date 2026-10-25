package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * V热焰（招式 ID：557）的攻击／事件阶段动画。
 *
 * <p>招式描述：从前额产生灼热的火焰, 舍身撞击对手. 防御、特防和速度会降低.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110557（名称）和 120557（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/VCreateAnimation.java
 *
 * <p>源文件 SHA-256：00a905a4b0a7e00dd67a1757c8b453475b230b7f3e5f8b85d8433c3f2ba25cf3
 */
final class ReferenceMoveProgram557 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .onStart(owner.loadBattleBackground(14))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.625f, 0.075f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1876,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1561,
                                        1,
                                        14,
                                        1083.3334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1425,
                                        2,
                                        14,
                                        1083.3334f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 2080.0f, 320.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1438,
                                        1,
                                        14,
                                        2333.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(728))
                        .append(owner.emitParticles(-1, 728, 3, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 728, 4, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 728, 5, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 728, 6, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 728, 12, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(18);
        ReferenceMoveTimeline animationStep13 =
                animationStep10
                        .append(owner.emitParticles(-1, 728, 13, 9, 8, 0.5f))
                        .append(owner.tintCombatant(14, 0.75f, 0.0f, 0.625f, color))
                        .beginSequence()
                        .waitSeconds(2.4f)
                        .beginParallel()
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.625f, 1.0f, 0.075f))
                        .append(owner.focusCamera(1, 0.25f))
                        .beginSequence()
                        .waitSeconds(0.24f)
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1885,
                                        1,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1991,
                                        2,
                                        16,
                                        250.0f,
                                        0.9921875f,
                                        owner.casterHandle));
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep20 =
                animationStep13
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.28f)
                        .beginParallel()
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
        ReferenceMoveTimeline animationStep21 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep20
                                .append(owner.emitParticles(-1, 728, 2, 11, 8, 0.5f))
                                .append(owner.scrollBattleBackground(2, 0, 8, 0.032f))
                                .append(
                                        owner.scaleCombatant(
                                                16, 2, 6, 0.0f, 0.032f, -0.19995117f, 0.19995117f))
                                .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.625f, color2)),
                        owner.shakeCombatant(16, 2, 12, 0.0f, 0.032f, 1.0f, 0.0f));
        color2 = EncounterMoveAnimation.color(18);
        ReferenceMoveTimeline animationStep22 =
                animationStep21.append(owner.tintCombatant(14, 0.75f, 0.625f, 0.0f, color2));
        color2 = EncounterMoveAnimation.color(18);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                animationStep22
                        .append(owner.tintCombatant(16, 0.75f, 0.625f, 0.0f, color2))
                        .append(owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.075f))
                        .endGroup()
                        .beginParallel()
                        .beginSequence()
                        .waitSeconds(0.04f)
                        .beginParallel()
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
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
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
