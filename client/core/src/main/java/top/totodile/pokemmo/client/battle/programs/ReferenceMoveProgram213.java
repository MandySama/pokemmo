package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 迷人（招式 ID：213）的攻击／事件阶段动画。
 *
 * <p>招式描述：♂诱惑♀或♀诱惑♂, 让对手着迷. 对手将很难使出招式.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110213（名称）和 120213（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/AttractAnimation.java
 *
 * <p>源文件 SHA-256：a154c61821b294a0f73b993b3f95072a42f61854a48af03c2f2a6ef569c4f61f
 */
final class ReferenceMoveProgram213 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.6f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1777,
                                        1,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 80.0f, 320.0f))
                        .append(owner.adjustSoundTrack(1, 1, 320.0f, 400.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        1216.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1517,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 80.0f, 640.0f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .endGroup()
                        .beginParallel()
                        .onStart(owner.preloadParticleEffect(379))
                        .append(owner.emitParticles(-1, 379, 0, 11, 8, 0.4f));
        Color color = EncounterMoveAnimation.color(16409);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep5.append(owner.emitParticles(-1, 379, 1, 11, 8, 0.4f)),
                                owner.tintCombatant(16, 0.75f, 0.0f, 0.625f, color))
                        .append(owner.setSpriteAnimationPaused(14, 0));
        color = EncounterMoveAnimation.color(16409);
        result =
                animationStep6
                        .append(owner.tintCombatant(16, 0.75f, 0.625f, 0.0f, color))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
