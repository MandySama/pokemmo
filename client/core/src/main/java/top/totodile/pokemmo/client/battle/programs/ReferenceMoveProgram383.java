package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 仿效（招式 ID：383）的攻击／事件阶段动画。
 *
 * <p>招式描述：模仿对手刚才使出的招式, 并使出相同招式. 如果对手还没出招则会失败.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110383（名称）和 120383（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/CopycatAnimation.java
 *
 * <p>源文件 SHA-256：5310f54139d4c31dec9f9f39a504dea36066f2195ead1c3851371af84b5868b9
 */
final class ReferenceMoveProgram383 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1504,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.3125f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(558))
                                        .append(owner.emitParticles(-1, 558, 2, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 558, 0, 9, 8, 0.5f)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1541,
                                        1,
                                        14,
                                        0.0f,
                                        0.859375f,
                                        owner.casterHandle),
                                0.6f),
                        owner.emitParticles(-1, 558, 1, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(15488);
        ReferenceMoveTimeline animationStep7 =
                animationStep6
                        .append(owner.emitParticles(-1, 558, 3, 9, 8, 0.5f))
                        .append(owner.tintCombatant(14, 0.25f, 0.0f, 0.375f, color));
        Color color2 = EncounterMoveAnimation.color(15680);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep7,
                                owner.scheduleSound(
                                        (byte) 2,
                                        1452,
                                        2,
                                        14,
                                        0.0f,
                                        0.5859375f,
                                        owner.casterHandle))
                        .append(owner.tintCombatant(14, 0.25f, 0.375f, 0.0f, color2))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
