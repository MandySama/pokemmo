package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 查封（招式 ID：373）的攻击／事件阶段动画。
 *
 * <p>招式描述：让对手在5回合内不能使用宝可梦携带的道具. 训练家也不能给那只宝可梦使用道具.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110373（名称）和 120373（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/EmbargoAnimation.java
 *
 * <p>源文件 SHA-256：ea5a880d471e846c51f68eb1aafc61d7fa7511d5265ab9c88861cdc9f0d36854
 */
final class ReferenceMoveProgram373 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .onStart(owner.preloadParticleEffect(548))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1480,
                                        1,
                                        16,
                                        0.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        1833.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 1280.0f, 480.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1480,
                                        2,
                                        16,
                                        500.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        16,
                                        1833.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 1280.0f, 480.0f))
                        .append(owner.emitParticles(-1, 548, 0, 11, 8, 0.25f));
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep7 =
                animationStep6
                        .append(owner.emitParticles(-1, 548, 1, 11, 8, 0.25f))
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.625f, color))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .beginSequence()
                        .waitSeconds(1.6f)
                        .beginParallel();
        color = EncounterMoveAnimation.color(0);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep7, owner.tintCombatant(16, 0.5f, 0.625f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
