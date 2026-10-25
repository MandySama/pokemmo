package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 幽灵视角（自定义动画注册 ID：1012）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111012（名称）和 121012（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1012Animation.java
 *
 * <p>源文件 SHA-256：e0245ac2a4ec1d66644756d7fb3bc9c3f998d09df45722cd8e19dadc540c1142
 */
final class ReferenceMoveProgram1012 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.8f, 0.05f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1533,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1505,
                                                        2,
                                                        14,
                                                        1083.3334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(1012))
                                        .append(owner.emitParticles(-1, 1012, 0, 0, 0, 0.5f))
                                        .append(owner.emitParticles(-1, 1012, 1, 0, 0, 0.5f))
                                        .append(owner.emitParticles(-1, 1012, 2, 0, 0, 0.5f)),
                                owner.emitParticles(-1, 1012, 3, 0, 0, 0.5f),
                                3.0f)
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.05f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup();
        return result;
    }
}
