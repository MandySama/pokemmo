package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 吸取（招式 ID：71）的攻击／事件阶段动画。
 *
 * <p>招式描述：吸取对手的养分进行攻击. 可以回复给予对手伤害的一半HP.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110071（名称）和 120071（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/AbsorbAnimation.java
 *
 * <p>源文件 SHA-256：92de9b27a2336ee12fa6219d9e4248786452ccdbae9463e42dcca4216aa5648b
 */
final class ReferenceMoveProgram71 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        ReferenceMoveTimeline.sequence()
                                .beginParallel()
                                .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.025f))
                                .append(owner.focusCamera(0, 0.6f))
                                .endGroup()
                                .beginParallel()
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1480,
                                                1,
                                                16,
                                                0.0f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1376,
                                                1,
                                                16,
                                                1166.6666f,
                                                0.0f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 1, 480.0f, 480.0f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1358,
                                                2,
                                                14,
                                                916.6667f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1358,
                                                2,
                                                14,
                                                1166.6666f,
                                                0.390625f,
                                                owner.casterHandle))
                                .onStart(owner.preloadParticleEffect(232)),
                        owner.emitParticles(-1, 232, 0, 11, 9, 0.4f),
                        0.8f);
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep6.append(owner.emitParticles(-1, 232, 1, 9, 8, 0.4f)),
                        owner.tintCombatant(14, 0.25f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                animationStep7
                        .append(owner.tintCombatant(14, 0.25f, 0.75f, 0.0f, color))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.025f))
                        .append(owner.restoreCamera(0.6f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
