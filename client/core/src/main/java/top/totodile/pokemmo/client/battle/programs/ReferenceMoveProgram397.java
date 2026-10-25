package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 岩石打磨（招式 ID：397）的攻击／事件阶段动画。
 *
 * <p>招式描述：打磨自己的身体, 减少空气阻力. 可以大幅提高自己的速度.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110397（名称）和 120397（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/RockPolishAnimation.java
 *
 * <p>源文件 SHA-256：cab3deb6681ac890ded3ff69aad8a328d025515412c4297a47021ed77f5a0ad8
 */
final class ReferenceMoveProgram397 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(0, 0.4f))
                                                .append(
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.75f, 0.025f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1411,
                                                                2,
                                                                14,
                                                                250.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1358,
                                                                2,
                                                                14,
                                                                916.6667f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1449,
                                                                1,
                                                                14,
                                                                916.6667f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(572))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 572, 2, 9, 8, 0.5f)),
                                        owner.emitParticles(-1, 572, 1, 9, 8, 0.5f))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.025f));
        return result;
    }
}
