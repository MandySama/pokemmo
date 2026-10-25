package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 光合作用（招式 ID：235）的攻击／事件阶段动画。
 *
 * <p>招式描述：回复自己的HP. 根据天气的不同, 回复量也会有所变化.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110235（名称）和 120235（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SynthesisAnimation.java
 *
 * <p>源文件 SHA-256：06f4f5ce05bd0e15e6205e9c47002c7e58d3e8f64ccb173f0c40eb7b861933e5
 */
final class ReferenceMoveProgram235 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1495,
                                        1,
                                        14,
                                        0.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1450,
                                        2,
                                        14,
                                        0.0f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 480.0f))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1440.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        1500.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1495,
                                        1,
                                        14,
                                        666.6667f,
                                        0.390625f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(402))
                        .append(owner.emitParticles(-1, 402, 0, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(660);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep6.append(owner.emitParticles(-1, 402, 1, 9, 8, 0.5f)),
                        owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(660);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep7,
                                owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color),
                                0.14f)
                        .append(owner.emitParticles(-1, 402, 0, 9, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep8, owner.emitParticles(-1, 402, 1, 9, 8, 0.5f)),
                        owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color2));
        color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep9, owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color2))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
