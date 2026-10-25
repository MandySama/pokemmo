package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 破阵乐（自定义动画注册 ID：1035）的攻击／事件阶段动画。
 *
 * <p>招式描述：回复使用者和队友最大HP的25%. 战斗外使用时, 治愈全队成员.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111035（名称）和 121035（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1035Animation.java
 *
 * <p>源文件 SHA-256：cda8ec9db241eb8869dbd6c30176bd159ec8d88c1400ba760ba4887a01f09c6e
 */
final class ReferenceMoveProgram1035 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .append(owner.focusCamera(0, 0.4f))
                        .beginParallel()
                        .onStart(owner.playCustomParticles("1035"))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1578, 0, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 0, 16, 2000.0f, 0.0f, owner.casterHandle))
                        .endGroup()
                        .waitSeconds(1.35f)
                        .append(owner.restoreCamera(0.4f));
        return result;
    }
}
