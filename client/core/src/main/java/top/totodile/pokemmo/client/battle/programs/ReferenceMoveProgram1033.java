package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 春色恼人（自定义动画注册 ID：1033）的攻击／事件阶段动画。
 *
 * <p>招式描述：引起对手的注意, 将对手的攻击全部转移到自己身上.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 111033（名称）和 121033（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1033Animation.java
 *
 * <p>源文件 SHA-256：8e7bebf3dc7a2ab052af36dc60f7cbfea6a68e8fea6d7f10e5400aa57420e749
 */
final class ReferenceMoveProgram1033 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(0, 0.4f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1438,
                                                                                1,
                                                                                14,
                                                                                166.66667f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1711,
                                                                                2,
                                                                                14,
                                                                                833.3333f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1711,
                                                                                2,
                                                                                14,
                                                                                1333.3334f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        14, 1))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        16, 1))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                434))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                435))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 434, 0, 9, 8,
                                                                                0.75f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 434, 1, 9, 8,
                                                                                0.75f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 434, 2, 9, 8,
                                                                                0.75f)),
                                                        owner.emitParticles(
                                                                -1, 434, 3, 9, 8, 0.75f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 434, 0, 9, 8, 0.75f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 434, 1, 9, 8, 0.75f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 435, 1, 9, 8, 0.75f)),
                                        owner.emitParticles(-1, 435, 2, 9, 8, 0.75f))
                                .append(owner.focusCamera(1, 0.4f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1463,
                                                3,
                                                16,
                                                0.0f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1376,
                                                3,
                                                16,
                                                916.6667f,
                                                0.0f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(3, 0, 0.0f, 880.0f))
                                .append(owner.adjustSoundTrack(3, 1, 640.0f, 160.0f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1464,
                                                1,
                                                16,
                                                0.0f,
                                                0.625f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1376,
                                                1,
                                                16,
                                                916.6667f,
                                                0.0f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 880.0f))
                                .append(owner.adjustSoundTrack(1, 1, 640.0f, 160.0f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1712,
                                                2,
                                                16,
                                                333.33334f,
                                                0.8984375f,
                                                owner.casterHandle)),
                        owner.scheduleSound(
                                (byte) 2, 1712, 2, 16, 666.6667f, 0.8984375f, owner.casterHandle));
        return result;
    }
}
