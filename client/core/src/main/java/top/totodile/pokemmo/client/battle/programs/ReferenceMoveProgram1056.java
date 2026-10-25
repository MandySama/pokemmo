package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * BOSS爆发（自定义动画注册 ID：1056）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111056（名称）和 121056（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/BattleKm0Animation.java
 *
 * <p>源文件 SHA-256：15dd75fe7486f322db7857ce1c50c249418b0b48fff66913eb3542f4d0e52536
 */
final class ReferenceMoveProgram1056 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .onStart(owner.preloadParticleEffect(729))
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.6f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .endGroup()
                        .beginParallel()
                        .append(owner.emitParticles(-1, 729, 0, 9, 8, 0.0f))
                        .append(
                                owner.tintCombatant(
                                        14, 0.5f, 0.0f, 0.5f, EncounterMoveAnimation.color(31)))
                        .append(owner.scaleCombatant(14, 1, 2, 0.016f, 0.228f, 1.2f, 1.2f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1412,
                                        0,
                                        14,
                                        100.0f,
                                        1.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1440,
                                        0,
                                        14,
                                        950.0f,
                                        1.0f,
                                        owner.casterHandle));
        result.endGroup()
                .beginParallel()
                .append(owner.setSpriteAnimationPaused(14, 0))
                .append(owner.tintCombatant(14, 0.5f, 0.5f, 0.0f, EncounterMoveAnimation.color(31)))
                .append(owner.scaleCombatant(14, 1, 2, 0.016f, 0.228f, 1.0f, 1.0f))
                .endGroup()
                .append(owner.restoreCamera(0.4f));
        return result;
    }
}
