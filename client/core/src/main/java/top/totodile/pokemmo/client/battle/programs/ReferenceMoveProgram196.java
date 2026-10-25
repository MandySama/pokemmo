package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冰冻之风（招式 ID：196）的攻击／事件阶段动画。
 *
 * <p>招式描述：将结冰的冷气吹向对手进行攻击. 会降低对手的速度.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110196（名称）和 120196（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/IcyWindAnimation.java
 *
 * <p>源文件 SHA-256：339f29b7934a5950fcb8dd990c6e561831a34b1949054ca22b4230154e55c739
 */
final class ReferenceMoveProgram196 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.075f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1439,
                                        1,
                                        16,
                                        166.66667f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1717,
                                        2,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        16,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 320.0f))
                        .append(owner.adjustSoundTrack(2, 0, 640.0f, 480.0f))
                        .append(owner.adjustSoundTrack(2, 1, 0.0f, 480.0f))
                        .append(owner.adjustSoundTrack(2, 1, 640.0f, 640.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1488,
                                        1,
                                        16,
                                        1333.3334f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1700,
                                        2,
                                        16,
                                        1333.3334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1488,
                                        1,
                                        16,
                                        1666.6666f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1700,
                                        2,
                                        16,
                                        1666.6666f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(362))
                        .append(owner.launchParticles(362, 1, 9, 11, 0.5f, 0.0f))
                        .append(owner.launchParticles(362, 2, 9, 11, 0.5f, 0.0f))
                        .beginSequence()
                        .waitSeconds(0.6f)
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.2f)
                        .beginParallel()
                        .append(owner.shakeCombatant(16, 2, 4, 0.032f, 0.032f, 0.5f, 0.0f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep8
                                .append(owner.emitParticles(-1, 362, 0, 11, 8, 0.5f))
                                .append(owner.setSpriteAnimationPaused(16, 1)),
                        owner.tintCombatant(16, 1.0f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                animationStep9
                        .append(owner.tintCombatant(16, 1.0f, 0.75f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
