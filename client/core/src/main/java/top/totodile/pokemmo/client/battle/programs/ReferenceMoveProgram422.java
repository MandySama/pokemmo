package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 雷电牙（招式 ID：422）的攻击／事件阶段动画。
 *
 * <p>招式描述：用蓄满电流的牙齿咬住对手. 有时会使对手畏缩或陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110422（名称）和 120422（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/ThunderFangAnimation.java
 *
 * <p>源文件 SHA-256：35bd275b474515aba0302744f4081216fd0f9ae6c36f43047706f688e3f6f199
 */
final class ReferenceMoveProgram422 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1720,
                                        1,
                                        16,
                                        333.33334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1458,
                                        2,
                                        16,
                                        416.66666f,
                                        0.859375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(597))
                        .append(owner.emitParticles(-1, 597, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 597, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 597, 2, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 597, 3, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 597, 4, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 597, 5, 11, 8, 0.5f))
                        .beginSequence()
                        .waitSeconds(0.2f)
                        .beginParallel()
                        .append(owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
