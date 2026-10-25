package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 特性查封（自定义动画注册 ID：1059）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111059（名称）和 121059（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/MistAnimation.java
 *
 * <p>源文件 SHA-256：37bf7d538d98347a430735f69bbd5c1ace000f89b36dda222a71c097f05514c0
 */
final class ReferenceMoveProgram1059 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .waitSeconds(0.4f),
                                                        owner.tintEnvironment(
                                                                4,
                                                                Short.MAX_VALUE,
                                                                0.0f,
                                                                0.75f,
                                                                0.125f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1455,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1440,
                                                                2,
                                                                14,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(215)),
                                        owner.emitParticles(-1, 215, 0, 9, 8, 0.5f))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintEnvironment(4, Short.MAX_VALUE, 0.75f, 0.0f, 0.125f));
        return result;
    }
}
