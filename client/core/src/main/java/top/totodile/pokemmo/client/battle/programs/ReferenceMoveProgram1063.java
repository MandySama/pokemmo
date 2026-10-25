package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 颠倒空间（自定义动画注册 ID：1063）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦场地位置；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111063（名称）和 121063（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1063Animation.java
 *
 * <p>源文件 SHA-256：545ce024429ee78f22d716f55a7b367f9437724e68a2e1c75ba5fca48bc1434e
 */
final class ReferenceMoveProgram1063 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline targetIndex = ReferenceMoveTimeline.sequence().beginParallel();
        targetIndex =
                targetIndex.append(
                        owner.scheduleSound((byte) 2, 1728, 1, 14, 0.0F, 1.0F, owner.casterHandle));
        Color targetIndex2 = new Color(0, 0, 1, .8f);
        targetIndex = targetIndex.onStart(owner.loadTintedBattleBackground(38, targetIndex2));
        targetIndex =
                targetIndex
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, true)))
                        .append(owner.tintEnvironment(4, 0, 0.0F, 1.0F, 0.05F))
                        .append(owner.focusCamera(4, 0.7F))
                        .endGroup()
                        .beginParallel();
        targetIndex =
                targetIndex.onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, false)));
        targetIndex =
                targetIndex
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, false)))
                        .append(owner.tintEnvironment(3, 0, 1.0F, 0.0F, 0.05F))
                        .append(owner.scrollBattleBackground(4, 0, -4, 1.4F))
                        .onStart(owner.setHealthHudHidden(14, true))
                        .onStart(owner.setHealthHudHidden(16, true))
                        .beginSequence()
                        .waitSeconds(0.64F);
        Color overlayColor = Color.valueOf("#71797e");
        targetIndex =
                targetIndex
                        .append(owner.tintCombatant(16, 0.15F, 0.0F, 0.8125F, overlayColor))
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.96F)
                        .append(owner.tintCombatant(16, 0.5F, 0.8125F, 0.0F, overlayColor))
                        .endGroup()
                        .endGroup()
                        .append(owner.tintEnvironment(3, 0, 0.0F, 0.9375F, 0.05F))
                        .beginParallel();
        targetIndex =
                targetIndex.append(
                        owner.scheduleSound((byte) 2, 1694, 1, 14, 0.0F, 1.0F, owner.casterHandle));
        targetIndex = targetIndex.onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)));
        result =
                targetIndex
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.tintEnvironment(2, 0, 0.9375F, 0.0F, 0.05F))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(14, false))
                        .onStart(owner.setHealthHudHidden(16, false))
                        .append(owner.restoreCamera(0.4F))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup();
        return result;
    }
}
