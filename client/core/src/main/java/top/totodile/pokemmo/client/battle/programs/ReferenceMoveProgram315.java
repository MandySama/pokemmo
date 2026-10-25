package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 过热（招式 ID：315）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出全部力量攻击对手. 使用之后会因为反作用力, 自己的特攻大幅降低.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；切换战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110315（名称）和 120315（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/OverheatAnimation.java
 *
 * <p>源文件 SHA-256：40af09c6e2d0bcf2d1215a9d84aa2033cdeb8afeb5121d7a259f7ffc1de6cfe4
 */
final class ReferenceMoveProgram315 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1418, 3, 14, 0.0f, 0.9375f, owner.casterHandle));
        Color color = EncounterMoveAnimation.color(31);
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                animationStep2
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1376,
                                                                        3,
                                                                        14,
                                                                        1166.6666f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        3, 0, 0.0f, 800.0f))
                                                        .append(
                                                                owner.tintCombatant(
                                                                        14, 0.75f, 0.0f, 0.75f,
                                                                        color))
                                                        .onStart(owner.loadBattleBackground(24))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction2)),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(
                                                ReferenceMoveTimeline.callback(
                                                        owner.sceneFlag(1, false)))
                                        .append(owner.tintEnvironment(3, 0, 0.9375f, 0.0f, 0.05f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1424,
                                                        2,
                                                        14,
                                                        166.66667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(
                                                owner.shakeCombatant(
                                                        14,
                                                        2,
                                                        6,
                                                        0.016f,
                                                        0.032f,
                                                        0.19995117f,
                                                        0.0f))
                                        .onStart(owner.preloadParticleEffect(486))
                                        .append(owner.emitParticles(-1, 486, 0, 9, 8, 0.5f))
                                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.05f)),
                                owner.emitParticles(-1, 486, 1, 9, 8, 0.5f)),
                        owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f));
        Runnable sceneFlagAction4 = owner.sceneFlag(0, true);
        Color color2 = EncounterMoveAnimation.color(31);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep7
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .onStart(
                                                ReferenceMoveTimeline.callback(
                                                        owner.sceneFlag(1, true)))
                                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.05f))
                                        .append(owner.tintCombatant(14, 0.75f, 0.75f, 0.0f, color2))
                                        .append(owner.setSpriteAnimationPaused(14, 0))
                                        .append(owner.restoreCamera(0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1440,
                                                        1,
                                                        14,
                                                        166.66667f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1440,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 486, 2, 9, 8, 0.4f)),
                                owner.emitParticles(-1, 486, 2, 11, 8, 0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
