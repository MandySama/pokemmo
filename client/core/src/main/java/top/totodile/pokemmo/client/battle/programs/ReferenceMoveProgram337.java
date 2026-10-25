package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 龙爪（招式 ID：337）的攻击／事件阶段动画。
 *
 * <p>招式描述：用尖锐的巨爪劈开对手进行攻击.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110337（名称）和 120337（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/DragonClawAnimation.java
 *
 * <p>源文件 SHA-256：f0ab3c7249265a70541a1cd109c41722e900931d27a161dc4a74de4895b8ce50
 */
final class ReferenceMoveProgram337 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep4 =
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
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1120.0f))
                        .append(owner.adjustSoundTrack(1, 1, 960.0f, 320.0f))
                        .onStart(owner.preloadParticleEffect(507))
                        .append(owner.emitParticles(-1, 507, 0, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep4
                                .append(owner.emitParticles(-1, 507, 1, 9, 8, 0.5f))
                                .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color)),
                        owner.shakeCombatant(14, 2, 12, 0.016f, 0.016f, 0.0f, 0.19995117f),
                        0.8f);
        color = EncounterMoveAnimation.color(31);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenOuterParallel(
                                                                        animationStep5,
                                                                        owner.tintCombatant(
                                                                                14, 0.5f, 0.75f,
                                                                                0.0f, color))
                                                                .append(owner.focusCamera(1, 0.4f))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1423,
                                                                                1,
                                                                                16,
                                                                                0.0f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1423,
                                                                                2,
                                                                                16,
                                                                                116.666664f,
                                                                                0.390625f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1423,
                                                                                1,
                                                                                16,
                                                                                233.33333f,
                                                                                0.15625f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1423,
                                                                                2,
                                                                                16,
                                                                                500.0f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1423,
                                                                                1,
                                                                                16,
                                                                                616.6667f,
                                                                                0.390625f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1423,
                                                                                2,
                                                                                16,
                                                                                733.3333f,
                                                                                0.15625f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1423,
                                                                                1,
                                                                                16,
                                                                                850.0f,
                                                                                0.078125f,
                                                                                owner.casterHandle))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                508))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                509))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 508, 0, 11, 8,
                                                                                0.5f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 508, 1, 11, 8,
                                                                                0.5f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 509, 0, 11, 8,
                                                                                0.5f)),
                                                        owner.emitParticles(
                                                                -1, 509, 1, 11, 8, 0.5f),
                                                        0.2f)
                                                .append(owner.setSpriteAnimationPaused(16, 1)),
                                        owner.shakeCombatant(
                                                16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f),
                                        0.72f),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
