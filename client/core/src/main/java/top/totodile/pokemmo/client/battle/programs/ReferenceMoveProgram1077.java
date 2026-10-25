package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 打断（自定义动画注册 ID：1077）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦场地位置；播放参考粒子特效；调整场景着色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111077（名称）和 121077（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1077Animation.java
 *
 * <p>源文件 SHA-256：067f5db424ec3b90cc68c6c84d95f51d0c5dc0c13d6dbb73a759dcde9dec1aae
 */
final class ReferenceMoveProgram1077 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(3, 0.4f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1427,
                                                                                3,
                                                                                16,
                                                                                0.0f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1427,
                                                                                3,
                                                                                16,
                                                                                250.0f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1427,
                                                                                3,
                                                                                16,
                                                                                500.0f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.adjustSoundTrack(
                                                                                3, 0, 0.0f,
                                                                                1600.0f))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        18, 1))
                                                                .onStart(
                                                                        owner.playCustomParticles(
                                                                                1077)),
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.75f, 0.05f),
                                                        1.74f)
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1509,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle)),
                                        owner.tintEnvironment(4, 0, 0.75f, 0.75f, 0.05f),
                                        2.08f),
                                owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(18, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup();
        return result;
    }
}
