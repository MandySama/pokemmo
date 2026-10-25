package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 忍耐（招式 ID：117）的蓄力阶段动画。
 *
 * <p>招式描述：在2回合内忍受攻击, 受到的伤害会2倍返还给对手.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110117（名称）和 120117（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BideAnimation.java
 *
 * <p>源文件 SHA-256：58550ce4dc4e752e3a4f3d8c9132eccc2eb442c190b7412cbff8b9e0afb2da71
 */
final class ReferenceChargeProgram117 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintCombatant(14, 1.25f, 0.0f, 0.625f, color))
                        .onStart(owner.preloadParticleEffect(283))
                        .append(owner.emitParticles(-1, 283, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 283, 1, 9, 8, 0.5f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1418,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        1,
                                        14,
                                        166.66667f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1418,
                                        1,
                                        14,
                                        200.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        1,
                                        14,
                                        366.66666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1418,
                                        1,
                                        14,
                                        400.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 1, 14, 566.6667f, 0.0f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1418,
                                        1,
                                        14,
                                        600.0f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(31);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep10.append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1376,
                                                1,
                                                14,
                                                766.6667f,
                                                0.0f,
                                                owner.casterHandle)),
                                owner.adjustSoundTrack(1, 0, 320.0f, 1600.0f))
                        .append(owner.tintCombatant(14, 1.25f, 0.625f, 0.0f, color2))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
