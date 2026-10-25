package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 精灵驱逐（自定义动画注册 ID：1010）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111010（名称）和 121010（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/RoarAnimation.java
 *
 * <p>源文件 SHA-256：98af0890bb13d33630fecde1109a8eb040bd12c5c3845327ceef719da62610d6
 */
final class ReferenceMoveProgram1010 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        14, 2, 6, 0.0f, 0.048f, 0.0f, 0.19995117f))
                                        .append(owner.playPokemonCry(false, 0.0f))
                                        .append(owner.playPokemonCry(false, 200.0f))
                                        .onStart(owner.preloadParticleEffect(207))
                                        .append(owner.emitParticles(-1, 207, 0, 9, 11, 0.5f))
                                        .append(owner.launchParticles(207, 1, 9, 11, 0.5f, 2160.0f))
                                        .append(
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        8,
                                                        0.016f,
                                                        0.016f,
                                                        0.30004883f,
                                                        0.0f))
                                        .append(owner.focusCamera(1, 0.65f))
                                        .beginSequence()
                                        .waitSeconds(0.52f)
                                        .beginParallel(),
                                owner.shakeCombatant(16, 1, 1, 0.016f, 0.256f, -30.0f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
