package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 羽栖（招式 ID：355）的攻击／事件阶段动画。
 *
 * <p>招式描述：降到地面, 使身体休息. 回复自己最大HP的一半.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110355（名称）和 120355（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/RoostAnimation.java
 *
 * <p>源文件 SHA-256：1eb8fa7aa59102479d50adb4df278a6700a465b7f0d36340e667cfc72cac28e5
 */
final class ReferenceMoveProgram355 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.sequence()
                                .beginParallel()
                                .append(owner.focusCamera(0, 0.4f))
                                .endGroup()
                                .beginParallel()
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1438,
                                                1,
                                                14,
                                                0.0f,
                                                0.859375f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1438,
                                                2,
                                                14,
                                                83.333336f,
                                                0.546875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1438,
                                                1,
                                                14,
                                                166.66667f,
                                                0.3125f,
                                                owner.casterHandle))
                                .onStart(owner.preloadParticleEffect(528)),
                        owner.emitParticles(-1, 528, 0, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep5.append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1483,
                                        2,
                                        14,
                                        0.0f,
                                        0.703125f,
                                        owner.casterHandle)),
                        owner.tintCombatant(14, 0.75f, 0.0f, 0.8125f, color),
                        0.6f);
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep6
                                        .append(
                                                owner.tintCombatant(
                                                        14, 0.75f, 0.8125f, 0.0f, color))
                                        .append(owner.emitParticles(-1, 528, 1, 9, 8, 0.5f)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1358,
                                        2,
                                        14,
                                        1000.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
