package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 抢先一步（招式 ID：382）的攻击／事件阶段动画。
 *
 * <p>招式描述：提高威力, 抢先使出对手想要使出的招式. 如果不先使出则会失败.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110382（名称）和 120382（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/MeFirstAnimation.java
 *
 * <p>源文件 SHA-256：9b77e8e07c754eaa580b997f5302b33d4a6fa79f7ffb60c1c6f092b808528c67
 */
final class ReferenceMoveProgram382 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(1, 0.4f))
                                                .endGroup()
                                                .beginParallel()
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1474,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.859375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1504,
                                                                2,
                                                                16,
                                                                0.0f,
                                                                0.859375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1509,
                                                                1,
                                                                16,
                                                                833.3333f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(557))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 557, 2, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 557, 0, 11, 8, 0.5f))
                                .append(owner.focusCamera(0, 0.4f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1510,
                                                2,
                                                14,
                                                0.0f,
                                                0.859375f,
                                                owner.casterHandle)),
                        owner.launchParticles(557, 1, 11, 9, 0.5f, 360.0f));
        Color color = EncounterMoveAnimation.color(15488);
        ReferenceMoveTimeline animationStep8 =
                animationStep7
                        .append(owner.emitParticles(-1, 557, 3, 9, 8, 0.5f))
                        .append(owner.tintCombatant(14, 0.25f, 0.0f, 0.375f, color));
        Color color2 = EncounterMoveAnimation.color(15680);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep8,
                                owner.scheduleSound(
                                        (byte) 2, 1452, 2, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(owner.tintCombatant(14, 0.25f, 0.375f, 0.0f, color2))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
