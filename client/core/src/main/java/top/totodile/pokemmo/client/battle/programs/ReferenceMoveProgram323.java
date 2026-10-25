package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 喷水（招式 ID：323）的攻击／事件阶段动画。
 *
 * <p>招式描述：掀起潮水进行攻击. 自己的HP越少, 招式的威力越小.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110323（名称）和 120323（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/WaterSpoutAnimation.java
 *
 * <p>源文件 SHA-256：5b080bea689936e97b88ff5b1290101d85907d6bb068614ba1eed89ffade53a7
 */
final class ReferenceMoveProgram323 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(0, 0.4f))
                                                                .beginSequence()
                                                                .waitSeconds(0.32f)
                                                                .beginParallel()
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1452,
                                                                                1,
                                                                                14,
                                                                                0.0f,
                                                                                0.859375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1407,
                                                                                2,
                                                                                14,
                                                                                166.66667f,
                                                                                0.8984375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1376,
                                                                                2,
                                                                                14,
                                                                                1500.0f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.adjustSoundTrack(
                                                                                2, 1, 1120.0f,
                                                                                320.0f))
                                                                .append(
                                                                        owner.scaleCombatant(
                                                                                14,
                                                                                2,
                                                                                1,
                                                                                0.016f,
                                                                                0.096f,
                                                                                0.30004883f,
                                                                                -0.30004883f))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                493))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 493, 3, 9, 8,
                                                                                0.5f)),
                                                        owner.emitParticles(-1, 493, 4, 9, 8, 0.5f),
                                                        1.52f)
                                                .append(owner.focusCamera(1, 0.4f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1511,
                                                                1,
                                                                16,
                                                                333.33334f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 493, 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 493, 1, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 493, 2, 11, 8, 0.75f),
                                        1.72f),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
