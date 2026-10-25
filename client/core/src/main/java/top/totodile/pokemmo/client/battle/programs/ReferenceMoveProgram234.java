package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 晨光（招式 ID：234）的攻击／事件阶段动画。
 *
 * <p>招式描述：回复自己的HP. 根据天气的不同, 回复量也会有所变化.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色。
 *
 * <p>编号对应参考动画注册表；语言条目为 110234（名称）和 120234（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/MorningSunAnimation.java
 *
 * <p>源文件 SHA-256：a684c78525eef70aca6cc685fe66fc7f562ae240e37bfba281c30a3f1d47e911
 */
final class ReferenceMoveProgram234 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.sequence()
                                .beginParallel()
                                .append(owner.focusCamera(0, 0.6f))
                                .endGroup()
                                .beginParallel()
                                .append(
                                        owner.tintEnvironment(
                                                4, Short.MAX_VALUE, 0.0f, 0.4375f, 0.075f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1820,
                                                2,
                                                14,
                                                333.33334f,
                                                0.625f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1820,
                                                1,
                                                14,
                                                666.6667f,
                                                0.234375f,
                                                owner.casterHandle))
                                .onStart(owner.preloadParticleEffect(401))
                                .append(owner.emitParticles(-1, 401, 0, 9, 8, 0.5f))
                                .append(owner.emitParticles(-1, 401, 1, 9, 8, 0.5f))
                                .append(owner.emitParticles(-1, 401, 2, 9, 8, 0.5f))
                                .endGroup()
                                .beginParallel()
                                .waitSeconds(0.6f),
                        owner.tintEnvironment(4, Short.MAX_VALUE, 0.4375f, 0.0f, 0.075f));
        return result;
    }
}
