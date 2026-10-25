package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 巨声（招式 ID：304）的攻击／事件阶段动画。
 *
 * <p>招式描述：给予对手又吵又响的巨大震动进行攻击.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110304（名称）和 120304（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/HyperVoiceAnimation.java
 *
 * <p>源文件 SHA-256：89bd9db0f67bc918581090d2e94d0106f8bda17ba643a41268bc487c39661cbb
 */
final class ReferenceMoveProgram304 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(0, 0.3f))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.playPokemonCry(
                                                                                true, 0.0f))
                                                                .append(
                                                                        owner.playPokemonCry(
                                                                                false, 266.66666f))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                470))
                                                                .append(
                                                                        owner.tintEnvironment(
                                                                                4, 990, 0.0f,
                                                                                0.375f, 0.1f)),
                                                        owner.launchParticles(
                                                                470, 1, 9, 11, 0.5f, 0.0f),
                                                        0.6f)
                                                .append(owner.focusCamera(1, 0.25f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1865,
                                                                2,
                                                                16,
                                                                0.0f,
                                                                0.625f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.shakeCombatant(
                                                                16, 2, 4, 0.032f, 0.032f, 0.5f,
                                                                0.0f)),
                                        owner.emitParticles(-1, 470, 0, 11, 8, 0.5f)),
                                owner.tintEnvironment(4, 990, 0.375f, 0.0f, 0.1f))
                        .append(owner.restoreCamera(0.3f))
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
