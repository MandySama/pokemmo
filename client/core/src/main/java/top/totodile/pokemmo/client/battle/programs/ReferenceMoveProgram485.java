package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 同步干扰（招式 ID：485）的攻击／事件阶段动画。
 *
 * <p>招式描述：用神奇电波对周围所有和自己属性相同的宝可梦给予伤害.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110485（名称）和 120485（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/SynchronoiseAnimation.java
 *
 * <p>源文件 SHA-256：91db31216e2e971f096b3bdd6f2f38f7bacc9e895e44b40bc34ba2130f7013b7
 */
final class ReferenceMoveProgram485 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenNextParallel(
                                                                        ReferenceMoveTimeline
                                                                                .sequence()
                                                                                .beginParallel()
                                                                                .append(
                                                                                        owner
                                                                                                .focusCamera(
                                                                                                        0,
                                                                                                        0.3f)),
                                                                        owner.tintEnvironment(
                                                                                4, 0, 0.0f, 0.75f,
                                                                                0.05f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1672,
                                                                                1,
                                                                                14,
                                                                                0.0f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1376,
                                                                                1,
                                                                                14,
                                                                                1250.0f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1467,
                                                                                2,
                                                                                14,
                                                                                333.33334f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.adjustSoundTrack(
                                                                                2, 1, 1120.0f,
                                                                                160.0f))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                655))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1,
                                                                                655,
                                                                                0,
                                                                                9,
                                                                                8,
                                                                                0.5250244f)),
                                                        owner.emitParticles(
                                                                -1, 655, 1, 9, 8, 0.5750122f),
                                                        0.12f)
                                                .append(owner.emitParticles(-1, 655, 2, 9, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 655, 0, 9, 8, 0.5250244f))
                                                .endGroup()
                                                .endGroup()
                                                .beginSequence()
                                                .waitSeconds(0.32f)
                                                .beginParallel()
                                                .waitSeconds(0.25f)
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1423,
                                                                1,
                                                                16,
                                                                1350.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1423,
                                                                2,
                                                                16,
                                                                1433.3334f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1423,
                                                                1,
                                                                16,
                                                                1516.6666f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1423,
                                                                2,
                                                                16,
                                                                1616.6666f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .endGroup()
                                                .endGroup()
                                                .beginSequence()
                                                .waitSeconds(1.32f)
                                                .beginParallel()
                                                .append(owner.focusCamera(1, 0.3f))
                                                .endGroup()
                                                .endGroup()
                                                .beginSequence()
                                                .waitSeconds(1.72f)
                                                .beginParallel()
                                                .append(
                                                        owner.shakeCombatant(
                                                                16,
                                                                2,
                                                                3,
                                                                0.0f,
                                                                0.032f,
                                                                0.30004883f,
                                                                0.0f)),
                                        owner.emitParticles(-1, 655, 3, 11, 8, 0.5f))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f));
        return result;
    }
}
