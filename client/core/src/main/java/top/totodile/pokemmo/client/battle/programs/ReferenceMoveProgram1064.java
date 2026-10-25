package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 技能开关（自定义动画注册 ID：1064）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦场地位置；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111064（名称）和 121064（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1064Animation.java
 *
 * <p>源文件 SHA-256：11abe03872673466cf2513f097a7a60c9634a4c1550d020e346a2dfdec26abb9
 */
final class ReferenceMoveProgram1064 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animation = ReferenceMoveTimeline.sequence().beginParallel();
        animation =
                animation.append(
                        owner.scheduleSound(
                                (byte) 2, (short) 1675, 1, 14, 0.0f, 1.0f, owner.casterHandle));
        animation.onStart(owner.loadTintedBattleBackground(34, Color.valueOf("#f168be")));
        animation.onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, true)));
        animation = animation.append(owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.05f));
        animation = animation.append(owner.focusCamera(4, 0.7f));
        animation = animation.endGroup().beginParallel();
        animation.onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, false)));
        animation.onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, false)));
        animation = animation.append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.05f));
        animation = animation.append(owner.scrollBattleBackground(4, 0, 2, 1.4f));
        animation.onStart(owner.setHealthHudHidden(14, true));
        animation.onStart(owner.setHealthHudHidden(16, true));
        animation = animation.beginSequence().waitSeconds(0.64f);
        animation =
                animation.append(
                        owner.tintCombatant(16, 1.2f, 0.0f, 0.8125f, Color.valueOf("#ffbf00")));
        animation = animation.endGroup().beginSequence().waitSeconds(0.96f);
        animation =
                animation.append(
                        owner.tintCombatant(16, 0.5f, 0.8125f, 0.0f, Color.valueOf("#ffbf00")));
        animation = animation.endGroup().endGroup();
        animation =
                animation.append(owner.tintEnvironment(3, 0, 0.0f, 0.9375f, 0.05f)).beginParallel();
        animation =
                animation.append(
                        owner.scheduleSound(
                                (byte) 2, (short) 1694, 1, 14, 0.0f, 1.0f, owner.casterHandle));
        animation.onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)));
        animation.onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)));
        animation = animation.append(owner.tintEnvironment(2, 0, 0.9375f, 0.0f, 0.05f));
        animation = animation.append(owner.setSpriteAnimationPaused(16, 0));
        animation.onStart(owner.setHealthHudHidden(14, false));
        animation.onStart(owner.setHealthHudHidden(16, false));
        animation = animation.append(owner.restoreCamera(0.4f));
        animation.onStart(owner.setHealthHudHidden(18, false));
        animation = animation.endGroup();
        result = animation;
        return result;
    }
}
