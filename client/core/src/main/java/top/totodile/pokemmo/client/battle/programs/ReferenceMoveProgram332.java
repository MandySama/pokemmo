package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 燕返（招式 ID：332）的攻击／事件阶段动画。
 *
 * <p>招式描述：以敏捷的动作戏弄对手后进行切斩. 攻击必定会命中.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110332（名称）和 120332（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/AerialAceAnimation.java
 *
 * <p>源文件 SHA-256：5d7bcc0dda99096886165f16182375ba75afe8d3d46129b51c51ae9ce66a19b5
 */
final class ReferenceMoveProgram332 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.6f))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.025f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        2,
                                        16,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1423,
                                        2,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1423,
                                        2,
                                        16,
                                        666.6667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        1,
                                        16,
                                        666.6667f,
                                        0.625f,
                                        owner.casterHandle));
        Runnable sceneFlagAction2 = owner.sceneFlag(0, false);
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep5
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, false)))
                                .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.75f, color))
                                .onStart(owner.preloadParticleEffect(502))
                                .append(owner.emitParticles(-1, 502, 0, 11, 8, 0.4f))
                                .append(owner.emitParticles(-1, 502, 1, 11, 8, 0.4f))
                                .append(owner.emitParticles(-1, 502, 2, 11, 8, 0.4f))
                                .append(owner.emitParticles(-1, 502, 3, 11, 8, 0.4f))
                                .append(owner.emitParticles(-1, 502, 4, 11, 8, 0.4f))
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .beginSequence()
                                .waitSeconds(0.2f)
                                .beginParallel(),
                        owner.shakeCombatant(16, 2, 1, 0.016f, 0.032f, -1.0f, 0.0f));
        Runnable sceneFlagAction3 = owner.sceneFlag(0, true);
        Color color2 = EncounterMoveAnimation.color(0);
        result =
                animationStep10
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.tintCombatant(16, 0.75f, 0.75f, 0.0f, color2))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.025f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
