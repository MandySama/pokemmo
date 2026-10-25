package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 自我激励（招式 ID：526）的攻击／事件阶段动画。
 *
 * <p>招式描述：激励自己, 从而提高攻击和特攻.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110526（名称）和 120526（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/WorkUpAnimation.java
 *
 * <p>源文件 SHA-256：7fdaf5234460f359a07a4473b4b33b42864095882a8d499280fd532282e77bd0
 */
final class ReferenceMoveProgram526 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1450, 1, 14, 0.0f, 0.78125f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 640.0f, 160.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        1,
                                        14,
                                        833.3333f,
                                        0.0f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(511);
        ReferenceMoveTimeline animationStep4 =
                animationStep3
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1718,
                                        2,
                                        14,
                                        833.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.tintCombatant(14, 1.25f, 0.0f, 0.5f, color))
                        .onStart(owner.preloadParticleEffect(691));
        Color color2 = EncounterMoveAnimation.color(511);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep4.append(owner.emitParticles(-1, 691, 0, 9, 8, 0.5f)),
                                owner.scaleCombatant(
                                        14, 2, 6, 0.0f, 0.032f, 0.100097656f, -0.100097656f))
                        .append(owner.tintCombatant(14, 1.25f, 0.5f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
