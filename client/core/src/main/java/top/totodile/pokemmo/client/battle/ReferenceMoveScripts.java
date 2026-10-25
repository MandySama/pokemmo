package top.totodile.pokemmo.client.battle;

import com.badlogic.gdx.graphics.Color;

/**
 * Ported from the identically named gen1 reference classes. Numeric parameters and nested
 * sequence/parallel groups are retained; only engine symbols and lifecycle calls are adapted.
 */
final class ReferenceMoveScripts {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner, int move) {
        return switch (move) {
            case 1 -> poundAnimation(owner);
            case 2 -> karateChopAnimation(owner);
            case 5 -> megaPunchAnimation(owner);
            case 7 -> firePunchAnimation(owner);
            case 8 -> icePunchAnimation(owner);
            case 9 -> thunderPunchAnimation(owner);
            case 10 -> scratchAnimation(owner);
            case 15 -> cutAnimation(owner);
            case 17 -> wingAttackAnimation(owner);
            case 22 -> vineWhipAnimation(owner);
            case 23 -> stompAnimation(owner);
            case 29 -> headbuttAnimation(owner);
            case 33 -> tackleAnimation(owner);
            case 34 -> bodySlamAnimation(owner);
            case 39 -> tailWhipAnimation(owner);
            case 44 -> biteAnimation(owner);
            case 52 -> emberAnimation(owner);
            case 64 -> peckAnimation(owner);
            case 84 -> thunderShockAnimation(owner);
            case 85 -> thunderboltAnimation(owner);
            case 88 -> rockThrowAnimation(owner);
            case 98 -> quickAttackAnimation(owner);
            case 112 -> barrierAnimation(owner);
            case 122 -> lickAnimation(owner);
            case 150 -> splashAnimation(owner);
            case 163 -> slashAnimation(owner);
            default -> throw new IllegalArgumentException("Unsupported move " + move);
        };
    }

    static java.util.Set<Integer> effects(int move) {
        return switch (move) {
            case 1 -> java.util.Set.of(161);
            case 2 -> java.util.Set.of(162);
            case 5 -> java.util.Set.of(165);
            case 7 -> java.util.Set.of(167);
            case 8 -> java.util.Set.of(168);
            case 9 -> java.util.Set.of(169);
            case 10 -> java.util.Set.of(170);
            case 15 -> java.util.Set.of(176);
            case 17 -> java.util.Set.of(178);
            case 22 -> java.util.Set.of(184);
            case 23 -> java.util.Set.of(185);
            case 29 -> java.util.Set.of(191);
            case 33 -> java.util.Set.of(195);
            case 34 -> java.util.Set.of(196);
            case 39 -> java.util.Set.of();
            case 44 -> java.util.Set.of(205);
            case 52 -> java.util.Set.of(213);
            case 64 -> java.util.Set.of(225);
            case 84 -> java.util.Set.of(248);
            case 85 -> java.util.Set.of(249, 250);
            case 88 -> java.util.Set.of(255);
            case 98 -> java.util.Set.of(266);
            case 112 -> java.util.Set.of(277);
            case 122 -> java.util.Set.of(287);
            case 150 -> java.util.Set.of(314);
            case 163 -> java.util.Set.of(329);
            default -> null;
        };
    }

    /**
     * 拍击（招式 ID：1）的攻击／事件阶段动画。
     *
     * <p>招式描述：使用长长的尾巴或手等拍打对手进行攻击.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110001（名称）和 120001（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/PoundAnimation.java
     *
     * <p>源文件 SHA-256：42f04258f710fe0e4e659e59b13fe4883d3fe4d37edd4091c3f675f8216cb340
     */
    private static ReferenceMoveTimeline poundAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(161))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 161, (short) 0, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 161, (short) 1, 11, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        133.33333f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.032f, 0.100097656f, -0.100097656f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 空手劈（招式 ID：2）的攻击／事件阶段动画。
     *
     * <p>招式描述：用锋利的手刀劈向对手进行攻击. 容易击中要害.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110002（名称）和 120002（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/KarateChopAnimation.java
     *
     * <p>源文件 SHA-256：159282ab646569f392234f7bd6c5c7d7e9de9b740e2799b51caaecbafe9d90ec
     */
    private static ReferenceMoveTimeline karateChopAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(162))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 162, (short) 0, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 162, (short) 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9765625f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        166.66667f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 百万吨重拳（招式 ID：5）的攻击／事件阶段动画。
     *
     * <p>招式描述：用充满力量的拳头攻击对手.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110005（名称）和 120005（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/MegaPunchAnimation.java
     *
     * <p>源文件 SHA-256：e1d46cb531983edb8b61666db01352cec9ddd50960bad4933ab866b72eb2e54f
     */
    private static ReferenceMoveTimeline megaPunchAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1421,
                                        2,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1424,
                                        1,
                                        16,
                                        166.66667f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.025f))
                        .onStart(owner.preloadParticleEffect(165))
                        .append(owner.emitParticles(-1, 165, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 165, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 165, 2, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 165, 3, 11, 8, 0.5f))
                        .beginSequence()
                        .waitSeconds(0.16f)
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.39990234f, -0.19995117f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 火焰拳（招式 ID：7）的攻击／事件阶段动画。
     *
     * <p>招式描述：用充满火焰的拳头攻击对手. 有时会让对手陷入灼伤状态.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110007（名称）和 120007（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/FirePunchAnimation.java
     *
     * <p>源文件 SHA-256：53eae19b7a163dbd75ce93673c256264d084c0bb64d00547d874666dcc902f12
     */
    private static ReferenceMoveTimeline firePunchAnimation(EncounterMoveAnimation owner) {
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.focusCamera(1, 0.4f)),
                                owner.tintEnvironment(4, 31, 0.0f, 0.75f, 0.075f))
                        .append(owner.tintCombatant(16, 0.25f, 0.0f, 0.625f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1421,
                                        2,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        2,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(167))
                        .append(owner.emitParticles(-1, 167, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 167, 1, 11, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(31);
        owner.timeline =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        animationStep6
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 167, 2, 11, 8, 0.5f))
                                                .append(owner.setSpriteAnimationPaused(16, 1)),
                                        owner.shakeCombatant(16, 2, 12, 0.0f, 0.032f, 0.5f, 0.0f))
                                .append(owner.restoreCamera(0.4f))
                                .endGroup()
                                .beginParallel()
                                .onStart(owner.setHealthHudHidden(18, false))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .append(owner.tintCombatant(16, 0.25f, 0.625f, 0.0f, color2)),
                        owner.tintEnvironment(4, 31, 0.75f, 0.0f, 0.075f));
        return owner.timeline;
    }

    /**
     * 冰冻拳（招式 ID：8）的攻击／事件阶段动画。
     *
     * <p>招式描述：用充满寒气的拳头攻击对手. 有时会让对手陷入冰冻状态.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110008（名称）和 120008（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/IcePunchAnimation.java
     *
     * <p>源文件 SHA-256：3710a384c7ce43f82dbcf0025d6b5934eb8dedba89b81dc5cb1369284b35b124
     */
    private static ReferenceMoveTimeline icePunchAnimation(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f)),
                                owner.tintEnvironment(4, 21140, 0.0f, 0.75f, 0.075f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1421,
                                        2,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1424,
                                        1,
                                        16,
                                        166.66667f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1700,
                                        2,
                                        16,
                                        333.33334f,
                                        0.9765625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1700,
                                        2,
                                        16,
                                        500.0f,
                                        0.9765625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1700,
                                        2,
                                        16,
                                        666.6667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1719,
                                        1,
                                        16,
                                        666.6667f,
                                        0.234375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1719,
                                        1,
                                        16,
                                        750.0f,
                                        0.234375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1719,
                                        1,
                                        16,
                                        833.3333f,
                                        0.234375f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .onStart(owner.preloadParticleEffect(168))
                        .append(owner.emitParticles(-1, 168, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 168, 1, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(25236);
        ReferenceMoveTimeline animationStep12 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep11
                                        .append(owner.emitParticles(-1, 168, 2, 11, 8, 0.5f))
                                        .append(owner.tintCombatant(16, 0.25f, 0.0f, 0.75f, color)),
                                owner.shakeCombatant(14, 2, 4, 0.0f, 0.032f, 0.5f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel();
        color = EncounterMoveAnimation.color(25236);
        owner.timeline =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep12.append(owner.tintCombatant(16, 0.25f, 0.75f, 0.0f, color)),
                        owner.tintEnvironment(4, 21140, 0.75f, 0.0f, 0.075f));
        return owner.timeline;
    }

    /**
     * 雷电拳（招式 ID：9）的攻击／事件阶段动画。
     *
     * <p>招式描述：用充满电流的拳头攻击对手. 有时会让对手陷入麻痹状态.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110009（名称）和 120009（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ThunderPunchAnimation.java
     *
     * <p>源文件 SHA-256：b1a50891d46f1ffd0c2f2a62a3aa11dedc311cb305e9f4caf261fa8513767cff
     */
    private static ReferenceMoveTimeline thunderPunchAnimation(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(1, 0.4f)),
                                        owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.075f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1424,
                                                1,
                                                16,
                                                0.0f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1522,
                                                2,
                                                16,
                                                83.333336f,
                                                0.9375f,
                                                owner.casterHandle))
                                .onStart(owner.preloadParticleEffect(169))
                                .append(owner.emitParticles(-1, 169, 0, 11, 8, 0.5f)),
                        owner.emitParticles(-1, 169, 1, 11, 8, 0.5f),
                        0.2f);
        Color color = EncounterMoveAnimation.color(660);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep5
                                        .append(owner.emitParticles(-1, 169, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.tintCombatant(16, 0.25f, 0.0f, 0.75f, color)),
                                owner.shakeCombatant(16, 2, 4, 0.0f, 0.032f, 0.5f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel();
        color = EncounterMoveAnimation.color(660);
        owner.timeline =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep6.append(owner.tintCombatant(16, 0.25f, 0.75f, 0.0f, color)),
                        owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.075f));
        return owner.timeline;
    }

    /**
     * 抓（招式 ID：10）的攻击／事件阶段动画。
     *
     * <p>招式描述：用坚硬且无比锋利的爪子抓对手进行攻击.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110010（名称）和 120010（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ScratchAnimation.java
     *
     * <p>源文件 SHA-256：11a4b6bae593ea7c5be0a439e6b80c7a920d1623e067a2a0c4e6220179fb8206
     */
    private static ReferenceMoveTimeline scratchAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(170))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 170, (short) 0, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 170, (short) 1, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 170, (short) 2, 11, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.032f,
                                                        0.100097656f,
                                                        0.30004883f)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1423,
                                        1,
                                        16,
                                        83.333336f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 居合劈（招式 ID：15）的攻击／事件阶段动画。
     *
     * <p>招式描述：用镰刀或爪子等切斩对手进行攻击.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110015（名称）和 120015（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/CutAnimation.java
     *
     * <p>源文件 SHA-256：e7df3c91df886a135055e7089226b48ec2d00f0473512510eba2a3516b84d8c8
     */
    private static ReferenceMoveTimeline cutAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(176))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 176, 0, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 176, 1, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 176, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1437,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1436,
                                                        2,
                                                        16,
                                                        100.0f,
                                                        0.0f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        100.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 翅膀攻击（招式 ID：17）的攻击／事件阶段动画。
     *
     * <p>招式描述：大大地展开美丽的翅膀, 将其撞向对手进行攻击.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110017（名称）和 120017（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/WingAttackAnimation.java
     *
     * <p>源文件 SHA-256：b1130837e6369a3b7c7a834710b35171d46c3ca9ce19d5357a3e6467c3120c52
     */
    private static ReferenceMoveTimeline wingAttackAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f)),
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1438,
                                                                        2,
                                                                        16,
                                                                        200.0f,
                                                                        0.9765625f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(178))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 178, (short) 0, 11, 8,
                                                                        0.5f)),
                                                owner.emitParticles(
                                                        -1, 178, (short) 1, 11, 8, 0.625f),
                                                0.08f)
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        0.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 藤鞭（招式 ID：22）的攻击／事件阶段动画。
     *
     * <p>招式描述：用如同鞭子般弯曲而细长的藤蔓摔打对手进行攻击.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110022（名称）和 120022（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/VineWhipAnimation.java
     *
     * <p>源文件 SHA-256：630a61ba4ca7cadaac7d11c88f0f13325d99e27eae8e68214de32c4b763a9f93
     */
    private static ReferenceMoveTimeline vineWhipAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence().beginParallel(),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        166.66667f,
                                                        0.8984375f,
                                                        owner.casterHandle),
                                                0.2f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.4f)
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(184))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 184, 0, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 184, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 184, 2, 11, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.032f,
                                                        0.064f,
                                                        0.19995117f,
                                                        -0.19995117f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1443,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1442,
                                        2,
                                        16,
                                        116.666664f,
                                        0.9375f,
                                        owner.casterHandle),
                                0.8f)
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 踩踏（招式 ID：23）的攻击／事件阶段动画。
     *
     * <p>招式描述：用大脚踩踏对手进行攻击. 有时会使对手畏缩.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110023（名称）和 120023（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/StompAnimation.java
     *
     * <p>源文件 SHA-256：90aab9a9b2119ec00308df29967500155459bd740d0e1fea61493a1c5c412735
     */
    private static ReferenceMoveTimeline stompAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(185))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 185, (short) 0, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 185, (short) 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 185, (short) 2, 11, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.032f,
                                                        0.064f,
                                                        0.39990234f,
                                                        -0.39990234f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1703,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 头锤（招式 ID：29）的攻击／事件阶段动画。
     *
     * <p>招式描述：将头伸出, 笔直地扑向对手进行攻击. 有时会使对手畏缩.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110029（名称）和 120029（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/HeadbuttAnimation.java
     *
     * <p>源文件 SHA-256：f317994306b8ec5a2208af60b1645bc622870c83efe3d2498acfa39a5e75d1c1
     */
    private static ReferenceMoveTimeline headbuttAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence().beginParallel(),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle),
                                                0.08f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.2f)
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(191))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 191, 0, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 191, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 320.0f, 160.0f)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1703,
                                        2,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 撞击（招式 ID：33）的攻击／事件阶段动画。
     *
     * <p>招式描述：用整个身体撞向对手进行攻击.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110033（名称）和 120033（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/TackleAnimation.java
     *
     * <p>源文件 SHA-256：f669f09b00d78169a6645b35d62b66872527c6e18c38484a1de22cb4ade87b76
     */
    private static ReferenceMoveTimeline tackleAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.shakeCombatant(
                                                        14, 2, 1, 0.016f, 0.064f, 1.0f, 0.0f))
                                        .onStart(owner.preloadParticleEffect(195))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 195, 0, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 195, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1437,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1424,
                                                        2,
                                                        16,
                                                        133.33333f,
                                                        0.8984375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1424,
                                        1,
                                        16,
                                        200.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 泰山压顶（招式 ID：34）的攻击／事件阶段动画。
     *
     * <p>招式描述：用整个身体压住对手进行攻击. 有时会让对手陷入麻痹状态.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110034（名称）和 120034（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BodySlamAnimation.java
     *
     * <p>源文件 SHA-256：05110ab18f67debe28641128b37a115f7037db1423590d3cb3ab1120dbbd8a6c
     */
    private static ReferenceMoveTimeline bodySlamAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        2,
                                                                        1,
                                                                        0.032f,
                                                                        0.064f,
                                                                        0.100097656f,
                                                                        0.30004883f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1426,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1463,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        14,
                                                                        1166.6666f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 1120.0f)),
                                                owner.adjustSoundTrack(1, 1, 800.0f, 320.0f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .beginSequence()
                                        .waitSeconds(0.2f)
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(196))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 196, 0, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 196, 1, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, (short) 196, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1705,
                                        2,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 摇尾巴（招式 ID：39）的攻击／事件阶段动画。
     *
     * <p>招式描述：可爱地左右摇晃尾巴, 诱使对手疏忽大意. 会降低对手的防御.
     *
     * <p>动画说明：控制精灵帧动画。
     *
     * <p>编号对应参考动画注册表；语言条目为 110039（名称）和 120039（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/TailWhipAnimation.java
     *
     * <p>源文件 SHA-256：681da8ed2256b053d810b131bc2834e961702f98694b56f819051a2ffeda6c8c
     */
    private static ReferenceMoveTimeline tailWhipAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1527,
                                        1,
                                        14,
                                        0.0f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1527,
                                        1,
                                        14,
                                        583.3333f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 咬住（招式 ID：44）的攻击／事件阶段动画。
     *
     * <p>招式描述：用尖锐的牙咬住对手进行攻击. 有时会使对手畏缩.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110044（名称）和 120044（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BiteAnimation.java
     *
     * <p>源文件 SHA-256：679ea56a7a46c9d2de7d5b6a7ea688ef85a36c8e124e988ca08b1fc368d5282e
     */
    private static ReferenceMoveTimeline biteAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(1, 0.25f))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1491,
                                                                2,
                                                                16,
                                                                333.33334f,
                                                                0.625f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1720,
                                                                1,
                                                                16,
                                                                333.33334f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(205))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 205, 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 205, 1, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 205, 2, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 205, 3, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 205, 4, 11, 8, 0.5f),
                                        0.2f),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 火花（招式 ID：52）的攻击／事件阶段动画。
     *
     * <p>招式描述：向对手发射小型火焰进行攻击. 有时会让对手陷入灼伤状态.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110052（名称）和 120052（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/EmberAnimation.java
     *
     * <p>源文件 SHA-256：bde92d79d019486561247e9e9651874e0ff0fcf1c67dab626775f7fa485668ab
     */
    private static ReferenceMoveTimeline emberAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(213))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 213, (short) 0, 11, 8, 0.5f))
                                        .append(
                                                owner.tintCombatant(
                                                        16,
                                                        0.75f,
                                                        0.0f,
                                                        0.75f,
                                                        EncounterMoveAnimation.color(30)))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.048f,
                                                        0.30004883f,
                                                        -0.19995117f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1426,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        2,
                                        16,
                                        333.33334f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(
                                owner.tintCombatant(
                                        16, 0.75f, 0.75f, 0.0f, EncounterMoveAnimation.color(30)))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 啄（招式 ID：64）的攻击／事件阶段动画。
     *
     * <p>招式描述：用尖锐的喙或角刺向对手进行攻击.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110064（名称）和 120064（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/PeckAnimation.java
     *
     * <p>源文件 SHA-256：f2dd605ddc74a935f9cb6b40b20392197c8d118932994a5ca92b0a14dee2836e
     */
    private static ReferenceMoveTimeline peckAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(225))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 225, (short) 1, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 225, (short) 0, 11, 8, 0.5f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 225, (short) 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1438,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        166.66667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 电击（招式 ID：84）的攻击／事件阶段动画。
     *
     * <p>招式描述：发出电流刺激对手进行攻击. 有时会让对手陷入麻痹状态.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110084（名称）和 120084（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ThunderShockAnimation.java
     *
     * <p>源文件 SHA-256：c9270b3af6527fe6fc645187900348372160281192ac0aae37a50700672137a6
     */
    private static ReferenceMoveTimeline thunderShockAnimation(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(14, 1)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f),
                                0.4f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1710,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep4 =
                animationStep2
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1458,
                                        2,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .onStart(owner.preloadParticleEffect(248))
                        .append(owner.emitParticles(-1, 248, 0, 11, 8, 0.0f));
        Color color2 = EncounterMoveAnimation.color(0);
        owner.timeline =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                animationStep4,
                                                owner.emitParticles(-1, 248, 1, 11, 8, 0.5f),
                                                0.6f)
                                        .append(owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color2)),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 十万伏特（招式 ID：85）的攻击／事件阶段动画。
     *
     * <p>招式描述：向对手发出强力电击进行攻击. 有时会让对手陷入麻痹状态.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110085（名称）和 120085（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ThunderboltAnimation.java
     *
     * <p>源文件 SHA-256：64203445ad24fcee10a60979537dc798992f15f03090dcaab6ef9377c4f44052
     */
    private static ReferenceMoveTimeline thunderboltAnimation(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(14, 1)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f),
                                0.4f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1710,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1710,
                                        2,
                                        16,
                                        250.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1445,
                                        1,
                                        16,
                                        333.33334f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        1500.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 320.0f, 1120.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1710,
                                        2,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep10 =
                animationStep6
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1458,
                                        2,
                                        16,
                                        1500.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color))
                        .onStart(owner.preloadParticleEffect(249))
                        .onStart(owner.preloadParticleEffect(250))
                        .append(owner.emitParticles(-1, 249, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 250, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 249, 1, 11, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(0);
        owner.timeline =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        animationStep10.append(
                                                owner.emitParticles(-1, 250, 1, 11, 8, 0.5f)),
                                        owner.shakeCombatant(16, 2, 8, 0.032f, 0.016f, 0.5f, 0.0f),
                                        1.4f),
                                owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color2))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 落石（招式 ID：88）的攻击／事件阶段动画。
     *
     * <p>招式描述：拿起小岩石, 投掷对手进行攻击.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110088（名称）和 120088（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/RockThrowAnimation.java
     *
     * <p>源文件 SHA-256：8aa205653c7afa5173a48ec8678ca4adfa65a6fa6ca647525932aa2d6e5f07cf
     */
    private static ReferenceMoveTimeline rockThrowAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .onStart(owner.preloadParticleEffect(255))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1,
                                                                        (short) 255,
                                                                        0,
                                                                        11,
                                                                        8,
                                                                        0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1,
                                                                        (short) 255,
                                                                        1,
                                                                        11,
                                                                        8,
                                                                        0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1,
                                                                        (short) 255,
                                                                        2,
                                                                        11,
                                                                        8,
                                                                        0.5f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1416,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9765625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        16,
                                                                        583.3333f,
                                                                        0.0f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1460,
                                                        2,
                                                        16,
                                                        583.3333f,
                                                        0.9375f,
                                                        owner.casterHandle),
                                                0.8f),
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                1,
                                                0.016f,
                                                0.048f,
                                                0.30004883f,
                                                -0.30004883f),
                                        1.0f),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.050048828f, -0.050048828f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 电光一闪（招式 ID：98）的攻击／事件阶段动画。
     *
     * <p>招式描述：以迅雷不及掩耳之势扑向对手. 必定能够先制攻击.
     *
     * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
     *
     * <p>编号对应参考动画注册表；语言条目为 110098（名称）和 120098（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/QuickAttackAnimation.java
     *
     * <p>源文件 SHA-256：70ac4bfd8bd8536335796a46c60aa739ff965e40a02fdb964696307a8f2d8c8e
     */
    private static ReferenceMoveTimeline quickAttackAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1437,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .endGroup()
                                        .beginParallel()
                                        .waitSeconds(0.6f)
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(266))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 266, (short) 0, 11, 8, 0.4f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 266, (short) 1, 11, 8, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, -0.19995117f, 0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 屏障（招式 ID：112）的攻击／事件阶段动画。
     *
     * <p>招式描述：制造坚固的壁障, 从而大幅提高自己的防御.
     *
     * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110112（名称）和 120112（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BarrierAnimation.java
     *
     * <p>源文件 SHA-256：e3f759f3f6af5df88c18a6bf6b951091fb33f91d2db6d886d73d36120aea95ad
     */
    private static ReferenceMoveTimeline barrierAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(0, 0.4f))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                277))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 277, (short) 3,
                                                                                9, 8, 0.5f)),
                                                        owner.emitParticles(
                                                                -1, 277, (short) 0, 9, 8, 0.5f),
                                                        0.2f),
                                                owner.emitParticles(-1, 277, (short) 1, 9, 8, 0.5f),
                                                0.6f)
                                        .append(owner.emitParticles(-1, 277, (short) 2, 9, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1472,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1473,
                                        2,
                                        14,
                                        333.33334f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 舌舔（招式 ID：122）的攻击／事件阶段动画。
     *
     * <p>招式描述：用长长的舌头, 舔遍对手进行攻击. 有时会让对手陷入麻痹状态.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110122（名称）和 120122（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/LickAnimation.java
     *
     * <p>源文件 SHA-256：5fae04221dc5dbd11a4ac440cfb1a9eb294b4ce986acc0f348f62e395580e95a
     */
    private static ReferenceMoveTimeline lickAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(1, 0.35f))
                                                .endGroup()
                                                .beginParallel()
                                                .onStart(owner.preloadParticleEffect(287))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 287, (short) 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 287, (short) 1, 11, 8, 0.5f))
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .append(
                                                        owner.scaleCombatant(
                                                                16,
                                                                2,
                                                                1,
                                                                0.016f,
                                                                0.144f,
                                                                -0.30004883f,
                                                                0.30004883f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1476,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle)),
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1477,
                                                2,
                                                16,
                                                0.0f,
                                                0.390625f,
                                                owner.casterHandle),
                                        0.16f),
                                owner.shakeCombatant(16, 2, 6, 0.016f, 0.032f, 0.100097656f, 0.0f))
                        .append(owner.restoreCamera(0.35f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 跃起（招式 ID：150）的攻击／事件阶段动画。
     *
     * <p>招式描述：也不攻击只是一蹦一蹦地跳, 什么都不会发生...
     *
     * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110150（名称）和 120150（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SplashAnimation.java
     *
     * <p>源文件 SHA-256：1fb06eca5cd39e25b7d34ab60b4c883576d555582356ce6c8b106bda4dd0825f
     */
    private static ReferenceMoveTimeline splashAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1502,
                                                                                        1,
                                                                                        14,
                                                                                        300.0f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1452,
                                                                                        2,
                                                                                        14,
                                                                                        300.0f,
                                                                                        0.703125f,
                                                                                        owner.casterHandle))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                314))
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                14,
                                                                                                1))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 314, 0,
                                                                                        9, 8, 0.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 314, 1,
                                                                                        9, 8,
                                                                                        0.0f)),
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        2,
                                                                        1,
                                                                        0.016f,
                                                                        0.096f,
                                                                        0.30004883f,
                                                                        -0.30004883f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1502,
                                                                        1,
                                                                        14,
                                                                        300.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1452,
                                                                        2,
                                                                        14,
                                                                        300.0f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 314, 0, 9, 8, 0.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 314, 1, 9, 8, 0.0f)),
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.096f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1502,
                                                        1,
                                                        14,
                                                        300.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        2,
                                                        14,
                                                        300.0f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 314, 0, 9, 8, 0.0f))
                                        .append(owner.emitParticles(-1, 314, 1, 9, 8, 0.0f)),
                                owner.scaleCombatant(
                                        14, 2, 1, 0.016f, 0.096f, 0.30004883f, -0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }

    /**
     * 劈开（招式 ID：163）的攻击／事件阶段动画。
     *
     * <p>招式描述：用爪子或镰刀等劈开对手进行攻击. 容易击中要害.
     *
     * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
     *
     * <p>编号对应参考动画注册表；语言条目为 110163（名称）和 120163（描述）。
     *
     * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SlashAnimation.java
     *
     * <p>源文件 SHA-256：4f83f61a5aabe782340adc29499460e0f3c5a31824959546ceaf60b784d18949
     */
    private static ReferenceMoveTimeline slashAnimation(EncounterMoveAnimation owner) {
        owner.timeline =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1725,
                                                        2,
                                                        16,
                                                        100.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(329))
                                        .append(owner.emitParticles(-1, 329, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 329, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 329, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 329, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return owner.timeline;
    }
}
