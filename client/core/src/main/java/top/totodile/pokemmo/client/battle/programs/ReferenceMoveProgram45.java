package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 叫声（招式 ID：45）的攻击／事件阶段动画。
 *
 * <p>招式描述：让对手听可爱的叫声, 引开注意力使其疏忽, 从而降低对手的攻击.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110045（名称）和 120045（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/GrowlAnimation.java
 *
 * <p>源文件 SHA-256：e9d33ea757c79d9826033a89ad6d390bc761a67d4f29406868e34933626a7ec5
 */
final class ReferenceMoveProgram45 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.playPokemonCry(true, 0.0f))
                                                        .append(
                                                                owner.playPokemonCry(
                                                                        false, 233.33333f))
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .onStart(owner.preloadParticleEffect(206))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 206, 0, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 206, 1, 9, 8, 0.5f))
                                        .append(owner.restoreCamera(0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 4, 0.016f, 0.032f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
