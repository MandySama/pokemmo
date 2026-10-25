package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 龙之舞（招式 ID：349）的攻击／事件阶段动画。
 *
 * <p>招式描述：激烈地跳起神秘且强有力的舞蹈. 从而提高自己的攻击和速度.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110349（名称）和 120349（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/DragonDanceAnimation.java
 *
 * <p>源文件 SHA-256：22224e48e6f40d7df7d1ff8cc527ee2421eadc8a3fb9c5184c3d65219e90ae85
 */
final class ReferenceMoveProgram349 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1425,
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
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 2080.0f))
                        .append(owner.adjustSoundTrack(1, 1, 1600.0f, 480.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1445,
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
                                        2166.6667f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 2080.0f))
                        .append(owner.adjustSoundTrack(2, 1, 1600.0f, 480.0f))
                        .onStart(owner.preloadParticleEffect(522));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep5
                                .append(owner.emitParticles(-1, 522, 0, 9, 8, 0.5f))
                                .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color)),
                        owner.shakeCombatant(14, 2, 16, 0.032f, 0.016f, 0.30004883f, 0.0f),
                        1.6f);
        color = EncounterMoveAnimation.color(31);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep6, owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
