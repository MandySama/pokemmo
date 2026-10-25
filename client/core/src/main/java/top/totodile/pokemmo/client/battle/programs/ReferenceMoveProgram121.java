package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 炸蛋（招式 ID：121）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手用力投掷大大的蛋进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110121（名称）和 120121（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/EggBombAnimation.java
 *
 * <p>源文件 SHA-256：7e7569bc2c5cc7757ed435af8ba902ac50b1be22cafb7a9ecd110cbfa94b0875
 */
final class ReferenceMoveProgram121 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1459,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(286)),
                                                owner.launchParticles(286, 2, 9, 11, 0.5f, 480.0f),
                                                0.4f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.8f)
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 286, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 286, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 286, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.048f,
                                                        0.30004883f,
                                                        -0.30004883f)),
                                owner.scheduleSound(
                                        (byte) 2, 1475, 2, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
