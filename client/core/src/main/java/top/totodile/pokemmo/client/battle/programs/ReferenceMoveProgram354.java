package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 精神突进（招式 ID：354）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出全部力量攻击对手. 使用之后会因为反作用力, 自己的特攻大幅降低.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110354（名称）和 120354（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/PsychoBoostAnimation.java
 *
 * <p>源文件 SHA-256：cd8f1823c58eb4d063011e4df8cc3258ee373885f6097bdf64c0a7c5bfc8ffb2
 */
final class ReferenceMoveProgram354 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1497,
                                        1,
                                        14,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1505,
                                        1,
                                        14,
                                        2166.6667f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 2000.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1509,
                                        2,
                                        14,
                                        2166.6667f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        2333.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        2583.3333f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        2833.3333f,
                                        0.234375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(527))
                        .append(owner.emitParticles(-1, 527, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 527, 3, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 527, 4, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                animationStep10,
                                                owner.emitParticles(-1, 527, 5, 9, 8, 0.5f),
                                                2.2f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(owner.tintCombatant(14, 1.25f, 0.0f, 0.75f, color))
                                        .onStart(owner.loadBattleBackground(1))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.875f, 0.025f),
                                2.32f)
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, false)))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(2.44f)
                        .beginParallel()
                        .append(owner.tintEnvironment(3, 0, 0.875f, 0.0f, 0.025f));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep11
                                .append(owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color))
                                .append(owner.scrollBattleBackground(2, 0, 4, 0.016f))
                                .append(owner.emitParticles(-1, 527, 1, 11, 8, 0.5f))
                                .append(owner.emitParticles(-1, 527, 2, 11, 8, 0.5f))
                                .append(owner.setSpriteAnimationPaused(16, 1)),
                        owner.shakeCombatant(16, 2, 6, 0.032f, 0.016f, 0.30004883f, 0.0f));
        Runnable sceneFlagAction4 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                animationStep13
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
