package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 食梦（招式 ID：138）的攻击／事件阶段动画。
 *
 * <p>招式描述：吃掉正在睡觉的对手的梦进行攻击. 回复对手所受到伤害的一半HP.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色。
 *
 * <p>编号对应参考动画注册表；语言条目为 110138（名称）和 120138（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/DreamEaterAnimation.java
 *
 * <p>源文件 SHA-256：fbba296f7a27cbe92fe58d70e929e2b13f65f9f623fb447c3291ad3f4a98d9cb
 */
final class ReferenceMoveProgram138 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        ReferenceMoveTimeline.sequence()
                                .beginParallel()
                                .append(owner.focusCamera(0, 0.6f))
                                .endGroup()
                                .beginParallel()
                                .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.025f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1455,
                                                1,
                                                16,
                                                0.0f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 1600.0f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1455,
                                                2,
                                                16,
                                                166.66667f,
                                                0.703125f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(2, 0, 0.0f, 1600.0f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1471,
                                                1,
                                                14,
                                                2000.0f,
                                                0.9375f,
                                                owner.casterHandle))
                                .onStart(owner.preloadParticleEffect(303)),
                        owner.emitParticles(-1, 303, 0, 11, 9, 0.4f),
                        0.8f);
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep5.append(owner.emitParticles(-1, 303, 1, 9, 8, 0.4f)),
                        owner.tintCombatant(14, 0.25f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep6, owner.tintCombatant(14, 0.25f, 0.75f, 0.0f, color))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.025f))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
