package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 炽风☆（自定义动画注册 ID：1051）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111051（名称）和 121051（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1046Animation.java
 *
 * <p>源文件 SHA-256：7b84992bcc79d1ba63eeabf39a3da9810ee5a4f1d29183fe775db24a3acc5acf
 */
final class ReferenceMoveProgram1051 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        long animationStartedAtMillis = System.currentTimeMillis();
        ReferenceMoveTimeline speedUp = owner.animateSpriteSpeed(14, 1, 1.85f);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.8f)),
                                owner.tintEnvironment(4, 8470, 0.0f, 0.75f, 0.05f))
                        .append(speedUp)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1426, 1, 14, 150.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1426, 1, 14, 650.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1426,
                                        1,
                                        14,
                                        1150.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1426,
                                        1,
                                        14,
                                        1650.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1426,
                                        1,
                                        14,
                                        2150.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1426,
                                        1,
                                        14,
                                        2650.0f,
                                        0.9375f,
                                        owner.casterHandle));
        ReferenceMoveTimeline speedRestore = owner.animateSpriteSpeed(14, 1, 1);
        result =
                animationStep7
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1907,
                                        2,
                                        14,
                                        2650.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .endGroup()
                        .onStart(owner.playCustomParticles("1046"))
                        .waitSeconds(2.0f)
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .append(speedRestore)
                        .append(owner.tintEnvironment(4, 8470, 0.75f, 0.0f, 0.2f))
                        .endGroup()
                        .append(owner.restoreCamera(0.4f));
        return result;
    }
}
