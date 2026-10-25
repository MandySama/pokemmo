package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 唱歌（招式 ID：47）的攻击／事件阶段动画。
 *
 * <p>招式描述：让对手听舒适、美妙的歌声, 从而陷入睡眠状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110047（名称）和 120047（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SingAnimation.java
 *
 * <p>源文件 SHA-256：3db500dbae5b53616bba86b95b0316b1f106eada0fe7ddb080954f740f049165
 */
final class ReferenceMoveProgram47 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f)),
                                owner.tintEnvironment(4, Short.MAX_VALUE, 0.0f, 0.625f, 0.075f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1523,
                                        1,
                                        14,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(208))
                        .append(owner.emitParticles(-1, 208, 0, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(15391);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        animationStep3.append(
                                                owner.emitParticles(-1, 208, 1, 9, 8, 0.5f)),
                                        owner.launchParticles(208, 2, 9, 11, 0.5f, 0.0f),
                                        1.2f)
                                .append(owner.restoreCamera(0.25f))
                                .append(owner.setSpriteAnimationPaused(16, 1)),
                        owner.tintCombatant(16, 0.75f, 0.0f, 0.375f, color),
                        1.8f);
        color = EncounterMoveAnimation.color(15391);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep4, owner.tintCombatant(16, 0.75f, 0.375f, 0.0f, color));
        color = EncounterMoveAnimation.color(15391);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep5, owner.tintCombatant(16, 0.75f, 0.0f, 0.375f, color));
        color = EncounterMoveAnimation.color(15391);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep6
                                .append(owner.tintCombatant(16, 0.75f, 0.375f, 0.0f, color))
                                .append(owner.setSpriteAnimationPaused(16, 0)),
                        owner.tintEnvironment(4, Short.MAX_VALUE, 0.625f, 0.0f, 0.075f));
        return result;
    }
}
