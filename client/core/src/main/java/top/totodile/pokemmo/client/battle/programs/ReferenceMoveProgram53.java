package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 喷射火焰（招式 ID：53）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射烈焰进行攻击. 有时会让对手陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110053（名称）和 120053（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/FlamethrowerAnimation.java
 *
 * <p>源文件 SHA-256：82e087124b3a5cd9bcf6817d0e9f26011be4934090b997084b29f3ddebc302ec
 */
final class ReferenceMoveProgram53 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(owner.tintEnvironment(2, 30, 0.0f, 0.625f, 0.025f))
                                        .onStart(owner.preloadParticleEffect(214))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1425,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1445,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 960.0f))
                                        .append(owner.launchParticles(214, 0, 9, 11, 0.5f, 0.0f)),
                                owner.launchParticles(214, 1, 9, 11, 0.5f, 0.0f),
                                0.4f)
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.92f)
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1561,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1561,
                                        2,
                                        16,
                                        500.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        0,
                                        16,
                                        166.66667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.emitParticles(-1, 214, 3, 11, 8, 0.375f));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep8
                                .append(owner.emitParticles(-1, 214, 2, 11, 8, 0.375f))
                                .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.625f, color)),
                        owner.shakeCombatant(16, 2, 6, 0.016f, 0.032f, 0.5f, 0.0f));
        color = EncounterMoveAnimation.color(31);
        result =
                animationStep9
                        .append(owner.tintCombatant(16, 0.5f, 0.625f, 0.0f, color))
                        .append(owner.tintEnvironment(2, 30, 0.625f, 0.0f, 0.025f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
