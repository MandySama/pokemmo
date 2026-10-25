package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 蛛网（招式 ID：169）的攻击／事件阶段动画。
 *
 * <p>招式描述：将黏糊糊的细丝一层一层缠住对手, 使其不能从战斗中逃走.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110169（名称）和 120169（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SpiderWebAnimation.java
 *
 * <p>源文件 SHA-256：4fe43c62f59062e399251f19486ba7c599ef7eb0da91f923a6271a1fd479cd46
 */
final class ReferenceMoveProgram169 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1408,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        16,
                                                                        1000.0f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1445,
                                                                        2,
                                                                        16,
                                                                        416.66666f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        2,
                                                                        16,
                                                                        1000.0f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 0, 400.0f, 560.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1708,
                                                                        1,
                                                                        16,
                                                                        1000.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(336))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 336, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 336, 2, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 336, 0, 9, 11, 0.5f),
                                                0.2f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.4f)
                                        .beginParallel(),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
