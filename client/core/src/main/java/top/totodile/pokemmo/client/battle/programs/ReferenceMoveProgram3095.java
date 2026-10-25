package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 催眠术☆（自定义动画注册 ID：3095）的攻击／事件阶段动画。
 *
 * <p>招式描述：施以诱导睡意的暗示, 让对手陷入睡眠状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>变体映射：基础招式 ID 为 95；按参考本地化规则沿用基础描述，并在名称末尾添加 ☆。
 *
 * <p>编号对应参考动画注册表；语言条目为 110095（名称）和 120095（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove3095Animation.java
 *
 * <p>源文件 SHA-256：e060d00b0032975135f5cbf4d2dff4eb6c9d4151e2cadd68dc37e651a20ff301
 */
final class ReferenceMoveProgram3095 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .onStart(owner.playCustomParticles("3095"))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1455, 1, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .endGroup()
                        .waitSeconds(1.4f)
                        .append(owner.restoreCamera(0.4f));
        return result;
    }
}
