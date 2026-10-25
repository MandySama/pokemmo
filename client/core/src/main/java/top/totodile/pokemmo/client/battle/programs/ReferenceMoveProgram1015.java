package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 噬灭（自定义动画注册 ID：1015）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111015（名称）和 121015（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1015Animation.java
 *
 * <p>源文件 SHA-256：c3ed1f52bf3a105c0e0ce67e3144fdaff742142b4c30fd0cd3d70a515934c2b3
 */
final class ReferenceMoveProgram1015 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1720,
                                        1,
                                        16,
                                        416.66666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1488,
                                        2,
                                        16,
                                        416.66666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1720,
                                        0,
                                        16,
                                        416.66666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(409))
                        .append(owner.emitParticles(-1, 409, 0, 9, 9, 0.5f))
                        .append(owner.emitParticles(-1, 409, 1, 9, 9, 0.5f))
                        .append(owner.emitParticles(-1, 409, 2, 9, 9, 0.5f))
                        .append(owner.emitParticles(-1, 409, 3, 9, 9, 0.5f))
                        .append(owner.emitParticles(-1, 409, 4, 9, 9, 0.5f))
                        .beginSequence()
                        .waitSeconds(0.24f)
                        .beginParallel()
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
