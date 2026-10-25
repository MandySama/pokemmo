package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 潮旋（招式 ID：250）的攻击／事件阶段动画。
 *
 * <p>招式描述：将对手困在激烈的水流旋涡中, 在4-5回合内进行攻击.
 *
 * <p>动画说明：播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色。
 *
 * <p>编号对应参考动画注册表；语言条目为 110250（名称）和 120250（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/WhirlpoolAnimation.java
 *
 * <p>源文件 SHA-256：3ed06ba759a63e692bf25f28daa83cae7aaf163c503db00e874154426af6136d
 */
final class ReferenceMoveProgram250 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.025f))
                        .onStart(owner.loadBattleBackground(28))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(3, 0, 0.75f, 0.0f, 0.1f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .endGroup()
                        .beginParallel()
                        .append(owner.scrollBattleBackground(1, 60, 0, 1.92f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1819,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1497,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        16,
                                        2000.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1600.0f))
                        .append(owner.adjustSoundTrack(2, 1, 1280.0f, 640.0f))
                        .onStart(owner.preloadParticleEffect(416))
                        .append(owner.emitParticles(-1, 416, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 416, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 416, 2, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(20810);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep7.append(owner.emitParticles(-1, 416, 3, 11, 8, 0.5f)),
                        owner.tintCombatant(16, 0.75f, 0.0f, 0.75f, color),
                        0.4f);
        color = EncounterMoveAnimation.color(20810);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                animationStep8
                        .append(owner.tintCombatant(16, 0.75f, 0.75f, 0.0f, color))
                        .append(owner.tintEnvironment(3, 0, 0.0f, 0.75f, 0.075f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.tintEnvironment(2, 0, 0.75f, 0.0f, 0.075f))
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
