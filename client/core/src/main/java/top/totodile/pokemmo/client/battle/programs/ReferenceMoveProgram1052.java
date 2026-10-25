package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 净化☆（自定义动画注册 ID：1052）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111052（名称）和 121052（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1048Animation.java
 *
 * <p>源文件 SHA-256：a4d957b5200e7ce590ae6b454cede50de9cabf5a8ea16a04cf30d8b70b0fa7ad
 */
final class ReferenceMoveProgram1052 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .append(owner.focusCamera(1, .4f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .beginParallel()
                        .append(owner.tintEnvironment(2, 13311, 0, .5f, .1f))
                        .append(
                                owner.tintCombatant(
                                        16, .75f, 0, .875f, EncounterMoveAnimation.color(13014)))
                        .endGroup()
                        .onStart(owner.preloadParticleEffect(1048))
                        .onStart(owner.playCustomParticles("1048_2"))
                        .beginParallel();
        ReferenceMoveTimeline.appendThenNextParallel(
                        result.beginSequence()
                                .beginParallel()
                                .append(owner.emitParticles(-1, 1048, 0, 11, 8, .5f))
                                .append(owner.emitParticles(-1, 1048, 1, 11, 8, .5f))
                                .append(owner.emitParticles(-1, 1048, 2, 11, 8, .5f))
                                .append(owner.emitParticles(-1, 1048, 3, 11, 8, .5f))
                                .append(owner.emitParticles(-1, 1048, 4, 11, 8, .5f)),
                        ReferenceMoveTimeline.sequence())
                .append(owner.tintEnvironment(2, 13311, .5f, 0, .1f))
                .append(
                        owner.tintCombatant(
                                16, .75f, .875f, 0, EncounterMoveAnimation.color(13014)))
                .endGroup()
                .endGroup();
        result.endGroup()
                .append(owner.setSpriteAnimationPaused(16, 0))
                .append(owner.restoreCamera(.4f));
        return result;
    }
}
