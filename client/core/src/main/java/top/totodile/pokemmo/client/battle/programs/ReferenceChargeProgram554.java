package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 极寒冷焰（招式 ID：554）的蓄力阶段动画。
 *
 * <p>招式描述：用能够冻结一切的强烈冷气, 在第2回合包裹住对手. 有时会让对手陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110554（名称）和 120554（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/IceBurnAnimation.java
 *
 * <p>源文件 SHA-256：43bdf61ab6501d8153b93900a39c038e033460377b0862bab7554e7ac907ef33
 */
final class ReferenceChargeProgram554 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1440,
                                                                                1,
                                                                                14,
                                                                                0.0f,
                                                                                0.546875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 2036,
                                                                                2,
                                                                                14,
                                                                                0.0f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .onStart(
                                                                        owner.setHealthHudHidden(
                                                                                18, true))
                                                                .append(owner.focusCamera(0, 0.4f))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                724))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 724, 0, 9, 8,
                                                                                0.5f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 724, 1, 9, 8,
                                                                                0.5f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 724, 0, 9, 8,
                                                                                0.5f)),
                                                        owner.emitParticles(-1, 724, 1, 9, 8, 0.5f),
                                                        0.12f),
                                                owner.emitParticles(-1, 724, 2, 9, 8, 0.5f),
                                                0.2f)
                                        .append(owner.emitParticles(-1, 724, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 724, 0, 9, 8, 0.5f))
                                        .append(
                                                owner.shakeCombatant(
                                                        14,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.032f,
                                                        0.19995117f,
                                                        0.0f)),
                                owner.scaleCombatant(
                                        14, 2, 1, 0.0f, 0.32f, 0.19995117f, -0.19995117f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
