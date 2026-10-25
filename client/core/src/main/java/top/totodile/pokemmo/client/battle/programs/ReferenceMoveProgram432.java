package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 清除浓雾（招式 ID：432）的攻击／事件阶段动画。
 *
 * <p>招式描述：用强风吹开对手的反射壁或光墙等. 也会降低对手的闪避率.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110432（名称）和 120432（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/DefogAnimation.java
 *
 * <p>源文件 SHA-256：5ff2124ae16febcb20cdab6c398d1e942a19bf8b059d06bedb00700e1ebb1876
 */
final class ReferenceMoveProgram432 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .onStart(owner.setHealthHudHidden(18, true))
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.0f, 0.75f, 0.075f))
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1445, 1, 14, 0.0f, 0.859375f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1280.0f))
                        .append(owner.adjustSoundTrack(1, 1, 640.0f, 640.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1509,
                                        2,
                                        14,
                                        1000.0f,
                                        0.9375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep8 =
                animationStep3
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1509,
                                        1,
                                        14,
                                        1166.6666f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(owner.tintCombatant(18, 0.75f, 0.0f, 0.75f, color))
                        .onStart(owner.preloadParticleEffect(608))
                        .append(owner.emitParticles(-1, 608, 0, 11, 8, 0.75f))
                        .append(owner.emitParticles(-1, 608, 0, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 608, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 608, 0, 11, 8, 1.0f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep8, owner.emitParticles(-1, 608, 0, 11, 8, 1.25f))
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.75f, 0.0f, 0.075f))
                        .append(owner.tintCombatant(18, 0.75f, 0.75f, 0.0f, color2))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
