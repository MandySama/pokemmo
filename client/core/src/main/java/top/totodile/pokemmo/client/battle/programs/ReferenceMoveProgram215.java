package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 治愈铃声（招式 ID：215）的攻击／事件阶段动画。
 *
 * <p>招式描述：让同伴听舒适的铃音, 从而治愈我方全员的异常状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110215（名称）和 120215（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/HealBellAnimation.java
 *
 * <p>源文件 SHA-256：e97045de433183aa1ccd69d1fcee225cd7349447ba6f6665363b9c308b0e0412
 */
final class ReferenceMoveProgram215 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .onStart(owner.preloadParticleEffect(381))
                        .append(owner.emitParticles(-1, 381, 0, 9, 8, 0.625f))
                        .append(owner.emitParticles(-1, 381, 1, 9, 8, 0.625f))
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.0f, 0.5f, 0.05f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1540,
                                        1,
                                        14,
                                        0.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        2166.6667f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 1440.0f, 640.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1541,
                                        2,
                                        14,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        750.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.emitParticles(-1, 381, 2, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep8.append(owner.emitParticles(-1, 381, 3, 9, 8, 0.5f)),
                        owner.tintCombatant(14, 0.25f, 0.0f, 0.5f, color),
                        0.32f);
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep9.append(
                                        owner.tintCombatant(14, 0.5f, 0.5f, 0.0f, color)),
                                owner.tintEnvironment(4, Short.MAX_VALUE, 0.5f, 0.0f, 0.05f),
                                0.64f)
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.0f, 0.5f, 0.05f))
                        .append(owner.emitParticles(-1, 381, 2, 9, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep10.append(owner.emitParticles(-1, 381, 3, 9, 8, 0.5f)),
                                owner.tintCombatant(14, 0.5f, 0.0f, 0.5f, color2),
                                0.96f)
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.5f, 0.0f, 0.05f));
        color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep12 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep11,
                                owner.tintCombatant(14, 0.5f, 0.5f, 0.0f, color2),
                                1.28f)
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.0f, 0.5f, 0.05f))
                        .append(owner.emitParticles(-1, 381, 2, 9, 8, 0.5f));
        Color color3 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep12.append(owner.emitParticles(-1, 381, 3, 9, 8, 0.5f)),
                                owner.tintCombatant(14, 0.5f, 0.0f, 0.5f, color3),
                                1.6f)
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.5f, 0.0f, 0.05f));
        color3 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep13, owner.tintCombatant(14, 0.5f, 0.5f, 0.0f, color3))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
