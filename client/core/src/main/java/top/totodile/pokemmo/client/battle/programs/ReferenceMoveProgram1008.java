package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 狂暴冲击（自定义动画注册 ID：1008）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111008（名称）和 121008（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1008Animation.java
 *
 * <p>源文件 SHA-256：88965ab1399adf42039a67745c201c36c0ef2592303e2600b8862470c28116d1
 */
final class ReferenceMoveProgram1008 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.5f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1534,
                                        2,
                                        14,
                                        0.0f,
                                        0.625f,
                                        owner.targetHandle))
                        .onStart(owner.preloadParticleEffect(1008))
                        .append(
                                owner.tintCombatant(
                                        16, 0.25f, 0.0f, 0.75f, EncounterMoveAnimation.color(31)))
                        .waitSeconds(0.6f)
                        .append(owner.emitParticles(-1, 1008, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 1008, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 1008, 2, 11, 8, 0.5f))
                        .append(owner.shakeCombatant(16, 2, 6, 0.016f, 0.032f, 0.19995117f, 0.0f))
                        .append(owner.adjustSoundTrack(1, 1, 320.0f, 480.0f))
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.tintCombatant(
                                        16, 0.25f, 0.625f, 0.0f, EncounterMoveAnimation.color(31)))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup();
        return result;
    }
}
