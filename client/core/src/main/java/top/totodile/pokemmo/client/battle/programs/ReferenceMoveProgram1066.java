package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 岩浆池（自定义动画注册 ID：1066）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦场地位置；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111066（名称）和 121066（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1066Animation.java
 *
 * <p>源文件 SHA-256：328d641070e6d036d2e118e2c400c7a98e95bb20abd184142db654bbafade370
 */
final class ReferenceMoveProgram1066 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .append(owner.focusCamera(4, 0.4f))
                        .beginParallel()
                        .onStart(owner.playCustomParticles("1066"))
                        .endGroup()
                        .waitSeconds(1.35f)
                        .append(owner.restoreCamera(0.4f));
        return result;
    }
}
