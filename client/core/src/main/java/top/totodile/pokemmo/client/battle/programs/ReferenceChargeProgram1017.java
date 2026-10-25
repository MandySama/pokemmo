package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 蹦得再高点!（自定义动画注册 ID：1017）的蓄力阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵显隐；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111017（名称）和 121017（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1017Animation.java
 *
 * <p>源文件 SHA-256：652ca12b085e92e4c5a2edba4116bbb255e54acf51e6f694567741971d1d959e
 */
final class ReferenceChargeProgram1017 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        int chargeCount = owner.chargeCount;
        if (chargeCount > 0) {
            owner.chargeCount = (byte) (chargeCount + 1);
            return null;
        }
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(0, 0.4f))
                                                .endGroup()
                                                .beginParallel()
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1407,
                                                                2,
                                                                14,
                                                                500.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                2,
                                                                14,
                                                                1333.3334f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(owner.adjustSoundTrack(2, 0, 0.0f, 1120.0f))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                2, 1, 800.0f, 480.0f))
                                                .append(owner.setCombatantVisibility(14, 3))
                                                .onStart(owner.preloadParticleEffect(511)),
                                        owner.emitParticles(-1, 511, (short) 0, 9, 8, 0.5f),
                                        0.2f),
                                owner.emitParticles(-1, 511, (short) 1, 9, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        owner.chargeCount = 1;
        return result;
    }
}
