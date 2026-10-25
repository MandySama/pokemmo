package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冥想（招式 ID：347）的攻击／事件阶段动画。
 *
 * <p>招式描述：静心凝神, 从而提高自己的特攻和特防.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110347（名称）和 120347（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/CalmMindAnimation.java
 *
 * <p>源文件 SHA-256：cf4307800272132d39fd0620aa33e608e56d2afc56bbb4308de7a9b959addb8a
 */
final class ReferenceMoveProgram347 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(0, false);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.8125f, 0.025f))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction2))
                                                .append(owner.focusCamera(0, 0.4f))
                                                .endGroup()
                                                .beginParallel()
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1509,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.546875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1434,
                                                                2,
                                                                14,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(520))
                                                .append(owner.emitParticles(-1, 520, 0, 9, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 520, 1, 9, 8, 0.5f)),
                                        owner.emitParticles(-1, 520, 2, 9, 8, 0.5f))
                                .append(owner.restoreCamera(0.4f))
                                .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true))),
                        owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.025f));
        return result;
    }
}
