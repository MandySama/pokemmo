package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 咒术（招式 ID：174）的攻击／事件阶段动画。
 *
 * <p>招式描述：根据使用者的属性不同会有不同的效果. 幽灵系使用时, 目标在每回合结束时损失其最大HP的1/4, 但使用者将立即减少其最大HP的1/2. 其他系使用时, 令使用者的速度降低1级,
 * 攻击和防御各提升1级.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110174（名称）和 120174（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/CurseAnimation.java
 *
 * <p>源文件 SHA-256：489d80db08aae20b219d345da6dcbe40bd591ecb5d9e0aa9cec9abc2c363e651
 */
final class ReferenceMoveProgram174 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        if (owner.casterHandle != null) {
            if (owner.hasCasterType(7)) {
                {
                    ReferenceMoveTimeline animationStep2 =
                            ReferenceMoveTimeline.sequence()
                                    .beginParallel()
                                    .append(owner.focusCamera(0, 0.4f))
                                    .endGroup()
                                    .beginParallel()
                                    .append(
                                            owner.scheduleSound(
                                                    (byte) 2,
                                                    (short) 1436,
                                                    2,
                                                    14,
                                                    0.0f,
                                                    0.859375f,
                                                    owner.casterHandle));
                    ReferenceMoveTimeline animationStep3 =
                            animationStep2
                                    .append(
                                            owner.scheduleSound(
                                                    (byte) 2,
                                                    (short) 1441,
                                                    1,
                                                    14,
                                                    83.333336f,
                                                    0.9375f,
                                                    owner.casterHandle))
                                    .append(owner.adjustSoundTrack(1, 1, 0.0f, 320.0f));
                    ReferenceMoveTimeline animationStep4 =
                            animationStep3.append(
                                    owner.scheduleSound(
                                            (byte) 2,
                                            (short) 1436,
                                            2,
                                            14,
                                            416.66666f,
                                            0.859375f,
                                            owner.casterHandle));
                    ReferenceMoveTimeline animationStep5 =
                            animationStep4
                                    .append(
                                            owner.scheduleSound(
                                                    (byte) 2,
                                                    (short) 1441,
                                                    1,
                                                    14,
                                                    500.0f,
                                                    0.9375f,
                                                    owner.casterHandle))
                                    .append(owner.adjustSoundTrack(1, 1, 0.0f, 320.0f));
                    ReferenceMoveTimeline animationStep6 =
                            animationStep5.append(
                                    owner.scheduleSound(
                                            (byte) 2,
                                            (short) 1436,
                                            2,
                                            14,
                                            833.3333f,
                                            0.859375f,
                                            owner.casterHandle));
                    ReferenceMoveTimeline animationStep7 =
                            animationStep6
                                    .append(
                                            owner.scheduleSound(
                                                    (byte) 2,
                                                    (short) 1441,
                                                    1,
                                                    14,
                                                    916.6667f,
                                                    0.9375f,
                                                    owner.casterHandle))
                                    .append(owner.adjustSoundTrack(1, 1, 0.0f, 320.0f))
                                    .onStart(owner.preloadParticleEffect(341));
                    ReferenceMoveTimeline animationStep8 =
                            animationStep7.append(owner.emitParticles(-1, 341, 0, 9, 8, 0.5f));
                    ReferenceMoveTimeline animationStep9 =
                            animationStep8.append(owner.emitParticles(-1, 341, 1, 9, 8, 0.5f));
                    ReferenceMoveTimeline animationStep10 =
                            animationStep9.append(owner.emitParticles(-1, 341, 2, 9, 8, 0.5f));
                    result =
                            ReferenceMoveTimeline.appendThenNextParallel(
                                            animationStep10,
                                            owner.emitParticles(-1, 341, 3, 9, 8, 0.5f))
                                    .append(owner.restoreCamera(0.4f))
                                    .endGroup()
                                    .beginParallel()
                                    .endGroup();
                }
            } else {
                ReferenceMoveTimeline animationStep2 =
                        ReferenceMoveTimeline.sequence()
                                .beginParallel()
                                .append(owner.focusCamera(0, 0.4f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1445,
                                                1,
                                                14,
                                                0.0f,
                                                0.8984375f,
                                                owner.casterHandle));
                ReferenceMoveTimeline animationStep3 =
                        animationStep2.append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle));
                ReferenceMoveTimeline animationStep4 =
                        animationStep3.append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1535,
                                        2,
                                        14,
                                        0.0f,
                                        0.859375f,
                                        owner.casterHandle));
                ReferenceMoveTimeline animationStep5 =
                        animationStep4.append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1535,
                                        2,
                                        14,
                                        333.33334f,
                                        0.859375f,
                                        owner.casterHandle));
                ReferenceMoveTimeline animationStep6 =
                        animationStep5.append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1535,
                                        2,
                                        14,
                                        666.6667f,
                                        0.859375f,
                                        owner.casterHandle));
                ReferenceMoveTimeline animationStep7 =
                        animationStep6.append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1535,
                                        2,
                                        14,
                                        1000.0f,
                                        0.859375f,
                                        owner.casterHandle));
                ReferenceMoveTimeline animationStep8 =
                        animationStep7
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1535,
                                                2,
                                                14,
                                                1333.3334f,
                                                0.859375f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 1440.0f))
                                .onStart(owner.preloadParticleEffect(340));
                Color color = EncounterMoveAnimation.color(31);
                ReferenceMoveTimeline animationStep9 =
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                animationStep8,
                                                owner.emitParticles(-1, 340, 0, 9, 8, 0.5f),
                                                0.2f)
                                        .append(
                                                owner.tintCombatant(
                                                        14, 0.75f, 0.0f, 0.625f, color)),
                                owner.shakeCombatant(14, 2, 2, 0.016f, 0.16f, 1.5f, 0.0f));
                color = EncounterMoveAnimation.color(31);
                result =
                        animationStep9
                                .append(owner.tintCombatant(14, 0.75f, 0.625f, 0.0f, color))
                                .append(owner.restoreCamera(0.4f))
                                .endGroup()
                                .beginParallel()
                                .endGroup();
            }
        } else {
            {
                ReferenceMoveTimeline animationStep2 =
                        ReferenceMoveTimeline.sequence()
                                .beginParallel()
                                .append(owner.focusCamera(0, 0.4f))
                                .endGroup()
                                .beginParallel()
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1436,
                                                2,
                                                14,
                                                0.0f,
                                                0.859375f,
                                                owner.casterHandle));
                ReferenceMoveTimeline animationStep3 =
                        animationStep2
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1441,
                                                1,
                                                14,
                                                83.333336f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 1, 0.0f, 320.0f));
                ReferenceMoveTimeline animationStep4 =
                        animationStep3.append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1436,
                                        2,
                                        14,
                                        416.66666f,
                                        0.859375f,
                                        owner.casterHandle));
                ReferenceMoveTimeline animationStep5 =
                        animationStep4
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1441,
                                                1,
                                                14,
                                                500.0f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 1, 0.0f, 320.0f));
                ReferenceMoveTimeline animationStep6 =
                        animationStep5.append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1436,
                                        2,
                                        14,
                                        833.3333f,
                                        0.859375f,
                                        owner.casterHandle));
                ReferenceMoveTimeline animationStep7 =
                        animationStep6
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1441,
                                                1,
                                                14,
                                                916.6667f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 1, 0.0f, 320.0f))
                                .onStart(owner.preloadParticleEffect(341));
                ReferenceMoveTimeline animationStep8 =
                        animationStep7.append(owner.emitParticles(-1, 341, 0, 9, 8, 0.5f));
                ReferenceMoveTimeline animationStep9 =
                        animationStep8.append(owner.emitParticles(-1, 341, 1, 9, 8, 0.5f));
                ReferenceMoveTimeline animationStep10 =
                        animationStep9.append(owner.emitParticles(-1, 341, 2, 9, 8, 0.5f));
                result =
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        animationStep10,
                                        owner.emitParticles(-1, 341, 3, 9, 8, 0.5f))
                                .append(owner.restoreCamera(0.4f))
                                .endGroup()
                                .beginParallel()
                                .endGroup();
            }
        }
        return result;
    }
}
