package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 暗影爪（招式 ID：421）的攻击／事件阶段动画。
 *
 * <p>招式描述：以影子做成的锐爪, 劈开对手. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110421（名称）和 120421（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/ShadowClawAnimation.java
 *
 * <p>源文件 SHA-256：13abc53265fb97baab40a55630c3c7554095560a6e2439c527cdb204094b55cd
 */
final class ReferenceMoveProgram421 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1438,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1423,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(596))
                                        .append(owner.emitParticles(-1, 596, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 596, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 596, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 6, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
