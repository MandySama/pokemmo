package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 神鸟猛击（招式 ID：143）的蓄力阶段动画。
 *
 * <p>招式描述：第2回合攻击对手. 偶尔使对手畏缩. 也容易击中要害.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110143（名称）和 120143（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SkyAttackAnimation.java
 *
 * <p>源文件 SHA-256：545c543a8af59c7e8d0efd8e01d2b2dc38c9227a81a058fa1ad75a8dbb5a3eef
 */
final class ReferenceChargeProgram143 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.025f))
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintCombatant(14, 0.25f, 0.0f, 0.8125f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1407,
                                        1,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1000.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 960.0f))
                        .append(owner.adjustSoundTrack(1, 1, 0.0f, 960.0f))
                        .onStart(owner.preloadParticleEffect(308));
        Color color2 = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep3, owner.emitParticles(-1, 308, 0, 9, 8, 0.5f)),
                        owner.tintCombatant(14, 0.25f, 0.8125f, 0.0f, color2));
        color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep5 =
                animationStep4
                        .append(owner.tintCombatant(14, 0.25f, 0.0f, 0.8125f, color2))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1358, 1, 14, 0.0f, 0.9375f, owner.casterHandle));
        Color color3 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep5,
                                owner.scheduleSound(
                                        (byte) 2,
                                        1358,
                                        2,
                                        14,
                                        500.0f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(owner.tintCombatant(14, 0.25f, 0.8125f, 0.0f, color3))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.025f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
