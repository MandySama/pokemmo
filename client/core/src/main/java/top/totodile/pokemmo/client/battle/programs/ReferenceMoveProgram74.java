package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 生长（招式 ID：74）的攻击／事件阶段动画。
 *
 * <p>招式描述：让身体一下子长大, 从而提高攻击和特攻.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110074（名称）和 120074（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/GrowthAnimation.java
 *
 * <p>源文件 SHA-256：64ad1c579e5e751eeef59955b8115531be8f483dc42b5fed5c846fdeb508600d
 */
final class ReferenceMoveProgram74 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(13302);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color))
                        .append(
                                owner.scaleCombatant(
                                        14, 3, 1, 0.016f, 0.064f, -0.100097656f, -0.19995117f))
                        .onStart(owner.preloadParticleEffect(237))
                        .append(owner.emitParticles(-1, 237, 0, 9, 8, 0.25f))
                        .append(owner.emitParticles(-1, 237, 1, 9, 8, 0.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1463, 1, 14, 0.0f, 0.625f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        1,
                                        14,
                                        666.6667f,
                                        0.0f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(13302);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        animationStep5
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1456,
                                                                2,
                                                                14,
                                                                0.0f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 640.0f)),
                                        owner.adjustSoundTrack(1, 1, 480.0f, 160.0f),
                                        0.44f),
                                owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
