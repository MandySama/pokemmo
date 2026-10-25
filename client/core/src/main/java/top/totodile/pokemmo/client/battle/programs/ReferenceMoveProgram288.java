package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 怨念（招式 ID：288）的攻击／事件阶段动画。
 *
 * <p>招式描述：因对手的招式而陷入濒死时给对手施加怨念, 让该招式的PP变成0.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110288（名称）和 120288（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/GrudgeAnimation.java
 *
 * <p>源文件 SHA-256：edd1c7a2784f8a4eb12b75b6b7584fadbcf79a304c156f9df14d46812ee301c0
 */
final class ReferenceMoveProgram288 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1850,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1868,
                                        2,
                                        14,
                                        0.0f,
                                        0.546875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 960.0f, 1280.0f))
                        .append(owner.adjustSoundTrack(2, 1, 800.0f, 1120.0f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .onStart(owner.preloadParticleEffect(455))
                        .append(owner.emitParticles(-1, 455, 0, 9, 8, 0.0f));
        Color color = EncounterMoveAnimation.color(22550);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep4
                                        .append(owner.emitParticles(-1, 455, 1, 9, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        14, 2, 1, 0.016f, 0.256f, 0.0f, 0.5f)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f),
                                0.4f)
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color))
                        .append(owner.restoreCamera(0.8f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.8f)
                        .beginParallel();
        color = EncounterMoveAnimation.color(22550);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep5, owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color), 1.2f);
        color = EncounterMoveAnimation.color(22550);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep6, owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color), 1.6f);
        color = EncounterMoveAnimation.color(22550);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep7.append(
                                        owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color)),
                                owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
