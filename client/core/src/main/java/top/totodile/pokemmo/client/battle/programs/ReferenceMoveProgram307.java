package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 爆炸烈焰（招式 ID：307）的攻击／事件阶段动画。
 *
 * <p>招式描述：用爆炸的火焰烧尽对手进行攻击. 下一回合自己将无法动弹.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110307（名称）和 120307（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/BlastBurnAnimation.java
 *
 * <p>源文件 SHA-256：ff762f7a09475d0ea2bbb7ab5bc2d5706971a6ea9e1f5a27802949864a71bc49
 */
final class ReferenceMoveProgram307 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep24 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.tintEnvironment(
                                                                                4, 20, 0.0f, 0.75f,
                                                                                0.1f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1478,
                                                                                1,
                                                                                14,
                                                                                0.0f,
                                                                                0.546875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1407,
                                                                                0,
                                                                                14,
                                                                                0.0f,
                                                                                0.625f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.adjustSoundTrack(
                                                                                0, 0, 0.0f,
                                                                                3200.0f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1376,
                                                                                0,
                                                                                16,
                                                                                3333.3333f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(owner.focusCamera(0, 0.4f))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                474))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 474, 6, 9, 8,
                                                                                0.25f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 474, 0, 9, 8,
                                                                                0.25f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 474, 3, 9, 8,
                                                                                0.25f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1428,
                                                                                1,
                                                                                14,
                                                                                0.0f,
                                                                                0.78125f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1428,
                                                                                1,
                                                                                14,
                                                                                416.66666f,
                                                                                0.78125f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1428,
                                                                                1,
                                                                                14,
                                                                                833.3333f,
                                                                                0.78125f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1706,
                                                                                2,
                                                                                16,
                                                                                1166.6666f,
                                                                                0.3125f,
                                                                                owner.casterHandle))
                                                                .onStart(
                                                                        owner.setHealthHudHidden(
                                                                                18, true))
                                                                .beginSequence()
                                                                .waitSeconds(0.4f)
                                                                .beginParallel()
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 474, 7, 9, 8,
                                                                                0.25f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 474, 1, 9, 8,
                                                                                0.25f)),
                                                        owner.emitParticles(
                                                                -1, 474, 4, 9, 8, 0.25f),
                                                        0.8f)
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 474, 8, 9, 8, 0.25f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 474, 2, 9, 8, 0.25f)),
                                        owner.emitParticles(-1, 474, 5, 9, 8, 0.25f))
                                .append(owner.focusCamera(1, 0.25f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1706,
                                                1,
                                                16,
                                                0.0f,
                                                0.3125f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1885,
                                                1,
                                                16,
                                                333.33334f,
                                                0.78125f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1800,
                                                2,
                                                16,
                                                416.66666f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1800,
                                                2,
                                                16,
                                                500.0f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1407,
                                                2,
                                                16,
                                                833.3333f,
                                                0.703125f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 0, 1920.0f, 640.0f))
                                .append(owner.adjustSoundTrack(2, 1, 1920.0f, 640.0f))
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .endGroup()
                                .beginParallel()
                                .append(owner.emitParticles(-1, 474, 0, 11, 8, 0.25f)),
                        owner.emitParticles(-1, 474, 6, 11, 8, 0.25f),
                        0.2f);
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep26 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep24,
                                owner.emitParticles(-1, 474, 9, 11, 8, 0.5f),
                                0.24f)
                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.625f, color))
                        .append(owner.emitParticles(-1, 474, 10, 11, 8, 0.2000122f));
        Color color2 = EncounterMoveAnimation.color(20);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                animationStep26,
                                                owner.emitParticles(-1, 474, 11, 11, 8, 0.2000122f),
                                                0.44f),
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                6,
                                                0.016f,
                                                0.032f,
                                                0.19995117f,
                                                -0.19995117f))
                                .append(owner.tintCombatant(16, 0.75f, 0.625f, 0.0f, color2))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .onStart(owner.setHealthHudHidden(18, false))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintEnvironment(4, 30, 0.75f, 0.0f, 0.1f));
        return result;
    }
}
