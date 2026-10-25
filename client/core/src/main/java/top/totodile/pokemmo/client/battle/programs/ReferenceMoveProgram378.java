package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 绞紧（招式 ID：378）的攻击／事件阶段动画。
 *
 * <p>招式描述：用力勒紧对手进行攻击. 对手的HP越多, 威力越大.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110378（名称）和 120378（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/WringOutAnimation.java
 *
 * <p>源文件 SHA-256：b3ba415dde03ad789b78a16f55977eacd5a23e01b95c55244955f248a95598f3
 */
final class ReferenceMoveProgram378 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .onStart(owner.preloadParticleEffect(553))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1445,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        16,
                                                        1666.6666f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 1280.0f, 320.0f))
                                        .append(owner.emitParticles(-1, 553, 0, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 553, 1, 11, 8, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 16, 0.032f, 0.016f, 0.19995117f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
