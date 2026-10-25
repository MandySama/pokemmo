package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 定身法（招式 ID：50）的攻击／事件阶段动画。
 *
 * <p>招式描述：阻碍对手行动, 之前使出的招式将在4回合内无法使用.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110050（名称）和 120050（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/DisableAnimation.java
 *
 * <p>源文件 SHA-256：e0c413dfc6e2ca9b0c4ef0aebe2acc5e0bcae22d6f7466c82da78b551249b75b
 */
final class ReferenceMoveProgram50 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenNextParallel(
                                                                        ReferenceMoveTimeline
                                                                                .sequence()
                                                                                .beginParallel()
                                                                                .append(
                                                                                        owner
                                                                                                .focusCamera(
                                                                                                        0,
                                                                                                        0.4f)),
                                                                        owner.tintEnvironment(
                                                                                4, 21140, 0.0f,
                                                                                0.75f, 0.125f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1509,
                                                                                1,
                                                                                14,
                                                                                0.0f,
                                                                                0.859375f,
                                                                                owner.casterHandle))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                211))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 211, 0, 9, 8,
                                                                                0.5f)),
                                                        owner.emitParticles(-1, 211, 1, 9, 8, 0.5f),
                                                        0.2f)
                                                .append(owner.restoreCamera(0.4f))
                                                .endGroup()
                                                .endGroup()
                                                .endGroup()
                                                .beginParallel()
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1524,
                                                                2,
                                                                16,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.tintCombatant(
                                                                16,
                                                                1.0f,
                                                                0.0f,
                                                                0.75f,
                                                                EncounterMoveAnimation.color(0))),
                                        owner.scaleCombatant(
                                                16, 2, 1, 0.016f, 0.16f, 0.30004883f, 0.30004883f))
                                .append(
                                        owner.tintCombatant(
                                                16,
                                                1.0f,
                                                0.75f,
                                                0.0f,
                                                EncounterMoveAnimation.color(0))),
                        owner.tintEnvironment(4, 21140, 0.75f, 0.0f, 0.125f));
        return result;
    }
}
