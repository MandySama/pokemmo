package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 炽热梦境（自定义动画注册 ID：1001）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111001（名称）和 121001（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1001Animation.java
 *
 * <p>源文件 SHA-256：2d57cc24605f1d3915dfe5ea3398087e81712d485da9337e35ae230e02691d92
 */
final class ReferenceMoveProgram1001 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .append(owner.focusCamera(0, 0.6f))
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1519, 1, 14, 0.0f, 0.78125f, owner.casterHandle))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.025f))
                        .onStart(owner.playCustomParticles("1001"))
                        .endGroup()
                        .waitSeconds(1.5f)
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.025f))
                        .append(owner.restoreCamera(0.6f));
        return result;
    }
}
