package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 旋风刀（招式 ID：13）的蓄力阶段动画。
 *
 * <p>招式描述：制造风之刃, 于第2回合攻击对手. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110013（名称）和 120013（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/RazorWindAnimation.java
 *
 * <p>源文件 SHA-256：3a104a965fe651634f7e5f1048ede76209971ea9bffcb47c3b63f2076ba6cfed
 */
final class ReferenceChargeProgram13 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(0, 0.4f))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1497,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 1120.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1376,
                                                                1,
                                                                14,
                                                                1333.3334f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                1, 1, 800.0f, 480.0f))
                                                .onStart(owner.preloadParticleEffect(173))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 173, 0, 9, 8, 0.0f)),
                                        owner.emitParticles(-1, 173, 1, 9, 8, 0.0f),
                                        0.2f),
                                owner.shakeCombatant(14, 2, 3, 0.0f, 0.032f, 0.5f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
