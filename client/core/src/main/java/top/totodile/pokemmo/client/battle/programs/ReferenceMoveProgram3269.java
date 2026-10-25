package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 挑衅☆（自定义动画注册 ID：3269）的攻击／事件阶段动画。
 *
 * <p>招式描述：使对手愤怒. 在3回合内让对手只能使出给予伤害的招式.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦场地位置；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>变体映射：基础招式 ID 为 269；按参考本地化规则沿用基础描述，并在名称末尾添加 ☆。
 *
 * <p>编号对应参考动画注册表；语言条目为 110269（名称）和 120269（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove3269Animation.java
 *
 * <p>源文件 SHA-256：e0ef20646bda2ee73cad78a571d692699a7af13306cfc4405a2ba559f7b38d9e
 */
final class ReferenceMoveProgram3269 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2;
        int direction;
        short effectId;
        int controllerIndex;
        int spawnType;
        float heightOffset;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1438,
                                        1,
                                        14,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1711,
                                        2,
                                        14,
                                        833.3333f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1711,
                                        2,
                                        14,
                                        1333.3334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .onStart(owner.preloadParticleEffect(434))
                        .onStart(owner.preloadParticleEffect(435));
        if (!owner.isPlayerSide()) {
            animationStep2 =
                    result.beginParallel()
                            .append(owner.emitParticles(-1, (short) 434, 0, 9, 8, 0.75f))
                            .append(owner.emitParticles(-1, (short) 434, 1, 9, 8, 0.75f))
                            .append(owner.emitParticles(-1, (short) 434, 2, 9, 8, 0.75f));
            effectId = 434;
            controllerIndex = 3;
            spawnType = 9;
            direction = 8;
            heightOffset = 0.75f;
        } else {
            animationStep2 =
                    result.beginParallel()
                            .append(owner.emitParticles(-1, (short) 434, 0, 9, 8, 0.75f))
                            .append(owner.emitParticles(-1, (short) 434, 1, 9, 8, 0.75f))
                            .append(owner.emitParticles(-1, (short) 435, 1, 9, 8, 0.75f));
            effectId = 435;
            controllerIndex = 2;
            spawnType = 9;
            direction = 8;
            heightOffset = 0.75f;
        }
        animationStep2
                .append(
                        owner.emitParticles(
                                -1, effectId, controllerIndex, spawnType, direction, heightOffset))
                .endGroup();
        ReferenceMoveTimeline animationStep13 =
                result.endGroup()
                        .beginParallel()
                        .append(owner.focusCamera(3, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1463,
                                        3,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        3,
                                        16,
                                        916.6667f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(3, 0, 0.0f, 880.0f))
                        .append(owner.adjustSoundTrack(3, 1, 640.0f, 160.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1464,
                                        1,
                                        16,
                                        0.0f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        916.6667f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 880.0f))
                        .append(owner.adjustSoundTrack(1, 1, 640.0f, 160.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1712,
                                        2,
                                        16,
                                        333.33334f,
                                        0.8984375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep14 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep13.append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1712,
                                        2,
                                        16,
                                        666.6667f,
                                        0.8984375f,
                                        owner.casterHandle)),
                        owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color),
                        0.4f);
        ReferenceMoveTimeline animationStep15 = ReferenceMoveTimeline.parallel();
        int targetIndex = 0;
        while (true) {
            if (targetIndex >= 1) {
                Color color2 = EncounterMoveAnimation.color(31);
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        animationStep14.append(animationStep15),
                                        owner.shakeCombatant(
                                                16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f),
                                        0.8f),
                                owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
                return result;
            }
            animationStep15.append(owner.emitParticles(targetIndex, 435, 0, 11, 8, 0.5f));
            ++targetIndex;
        }
    }
}
