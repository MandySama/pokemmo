package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 幻象光线（招式 ID：60）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射神奇的光线进行攻击. 有时会使对手混乱.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110060（名称）和 120060（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/PsybeamAnimation.java
 *
 * <p>源文件 SHA-256：eb8bf7199b1b1dbbc058903c50c40134f3f4fa354841984e3acba10252c0cad8
 */
final class ReferenceMoveProgram60 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1455, 1, 14, 0.0f, 0.859375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1450,
                                        2,
                                        14,
                                        0.0f,
                                        0.546875f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(30735);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        animationStep3
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1376,
                                                                2,
                                                                14,
                                                                1666.6666f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(owner.adjustSoundTrack(2, 0, 0.0f, 1280.0f))
                                                .onStart(owner.preloadParticleEffect(221)),
                                        owner.launchParticles(221, 0, 9, 11, 0.5f, 0.0f),
                                        0.4f)
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .append(owner.tintCombatant(16, 1.0f, 0.0f, 0.625f, color))
                                .append(owner.restoreCamera(0.4f)),
                        owner.shakeCombatant(16, 2, 6, 0.016f, 0.032f, 0.19995117f, 0.0f));
        color = EncounterMoveAnimation.color(30735);
        result =
                animationStep4
                        .append(owner.tintCombatant(16, 1.0f, 0.625f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
