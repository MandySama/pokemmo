package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 烈焰溅射（招式 ID：481）的攻击／事件阶段动画。
 *
 * <p>招式描述：如果击中, 爆裂的火焰会攻击到对手. 爆裂出的火焰还会飞溅到旁边的对手.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110481（名称）和 120481（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/FlameBurstAnimation.java
 *
 * <p>源文件 SHA-256：940eeda69448e81e491d9ff40391c3e463f7e7ef4a5d9f193e27cc9593bd597a
 */
final class ReferenceMoveProgram481 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .onStart(owner.preloadParticleEffect(652))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1535,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.emitParticles(-1, 652, 3, 9, 11, 0.5f),
                                0.24f)
                        .append(owner.focusCamera(1, 0.4f))
                        .append(owner.emitParticles(-1, 652, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 652, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 652, 2, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.32f)
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1507,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep8 =
                animationStep7
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1746, 1, 16, 0.0f, 0.546875f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 480.0f, 320.0f))
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.5f, color))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.096f, -0.100097656f, 0.30004883f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.72f)
                        .beginParallel();
        color = EncounterMoveAnimation.color(31);
        result =
                animationStep8
                        .append(owner.tintCombatant(16, 0.5f, 0.5f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
