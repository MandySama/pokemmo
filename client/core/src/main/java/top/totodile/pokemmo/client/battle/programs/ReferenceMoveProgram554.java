package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 极寒冷焰（招式 ID：554）的攻击／事件阶段动画。
 *
 * <p>招式描述：用能够冻结一切的强烈冷气, 在第2回合包裹住对手. 有时会让对手陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110554（名称）和 120554（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/IceBurnAnimation.java
 *
 * <p>源文件 SHA-256：43bdf61ab6501d8153b93900a39c038e033460377b0862bab7554e7ac907ef33
 */
final class ReferenceMoveProgram554 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .onStart(owner.loadBattleBackground(24))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.075f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1895,
                                        2,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1940,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.focusCamera(0, 0.4f))
                        .onStart(owner.preloadParticleEffect(725))
                        .append(owner.emitParticles(-1, 725, 0, 9, 8, 0.5f))
                        .onStart(owner.preloadParticleEffect(716))
                        .append(owner.emitParticles(-1, 716, 0, 9, 8, 0.5f))
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 1, 0.0f, 0.32f, 0.30004883f, -0.30004883f));
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep16 =
                animationStep5
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1440,
                                        1,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1449,
                                        2,
                                        16,
                                        416.66666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1449,
                                        2,
                                        16,
                                        833.3333f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1449,
                                        2,
                                        16,
                                        1250.0f,
                                        0.3125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1449,
                                        2,
                                        16,
                                        1666.6666f,
                                        0.15625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1488,
                                        1,
                                        16,
                                        666.6667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1488,
                                        1,
                                        16,
                                        1166.6666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1516,
                                        0,
                                        16,
                                        0.0f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(owner.focusCamera(1, 0.4f))
                        .append(owner.emitParticles(-1, 716, 5, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 716, 6, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 716, 7, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep17 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                animationStep16.onStart(
                                                                        ReferenceMoveTimeline
                                                                                .callback(
                                                                                        owner
                                                                                                .sceneFlag(
                                                                                                        1,
                                                                                                        false))),
                                                                owner.tintEnvironment(
                                                                        4, 0, 1.0f, 0.375f, 0.075f),
                                                                0.4f)
                                                        .append(
                                                                owner.tintCombatant(
                                                                        16, 0.75f, 0.0f, 0.5f,
                                                                        color)),
                                                owner.emitParticles(-1, 716, 2, 11, 8, 0.5f))
                                        .append(owner.tintEnvironment(4, 0, 0.375f, 1.0f, 0.075f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.032f, 0.100097656f, -0.100097656f))
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.075f));
        Runnable sceneFlagAction4 = owner.sceneFlag(0, true);
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                animationStep17
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.tintCombatant(16, 0.75f, 0.5f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
