package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 乱流（自定义动画注册 ID：1061）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111061（名称）和 121061（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1061Animation.java
 *
 * <p>源文件 SHA-256：f847e6f5c796d22c5cd836c57fec312ed53ab6117c79e1060ba2545cd93e9585
 */
final class ReferenceMoveProgram1061 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.6f))
                        .onStart(owner.playCustomParticles("1061"))
                        .endGroup()
                        .waitSeconds(1.6f)
                        .append(owner.restoreCamera(0.4f));
        return result;
    }
}
