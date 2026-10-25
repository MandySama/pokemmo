package top.totodile.pokemmo.client.battle;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleListModel;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokedex;
import top.totodile.pokemmo.client.ui.common.PlayerWindow;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.function.*;

/** Development-only effect review panel submitting the existing TEST_MOVE turn action. */
public final class MoveAnimationTestWindow extends PlayerWindow {
    private final ClientResources resources;
    private final BwPokedex dex;
    private final Supplier<EncounterWindow> battle;
    private final Runnable newBattle;
    private final List<Integer> catalogue = EncounterMoveAnimation.registeredMoves();
    private List<Integer> visible = List.of();
    private final EditField search = new EditField(), notes = new EditField();
    private final ListBox<String> list = new ListBox<>();
    private final Label description = new Label();
    private final ScrollPane descriptionScroll =
            new top.totodile.pokemmo.client.ui.common.TouchScrollPane(description);
    private final Label status = new Label(),
            detail = new Label(),
            help = new Label("单击查看描述，双击使用；敌方初始999999 HP，暂不出招。");
    private final Button previous = new Button("上一招"),
            next = new Button("下一招"),
            useOnce = new Button("使用1次"),
            useTen = new Button("使用10次"),
            stop = new Button("停止"),
            side = new Button("新随机怪"),
            target = new Button("目标：按招式"),
            passed = new Button("通过"),
            failed = new Button("异常");
    private final Widget content =
            new Widget() {
                @Override
                protected void layout() {
                    layoutContent();
                }
            };
    private int selected, targetSlot = -1;
    private boolean playbackRequested, skipBatchAnimations;
    private int remainingUses, completedUses, requestedUses;
    private String result = "", lastPlaybackStatus = "";

    public MoveAnimationTestWindow(
            Widget owner,
            ClientResources resources,
            Supplier<EncounterWindow> battle,
            Runnable newBattle)
            throws IOException {
        super(owner, false, "/adminframe-nontab", 680, 460, () -> {});
        this.resources = resources;
        this.battle = battle;
        this.newBattle = newBattle;
        dex = new BwPokedex(resources.roms().get(RomType.BLACK_WHITE), resources.strings());
        frame.setTitle("招式效果测试（开发）");
        content.setTheme("");
        frame.add(content);
        search.setTheme("/editfield");
        search.setTooltipContent("招式 ID / 名称");
        notes.setTheme("/editfield");
        notes.setTooltipContent("异常现象或测试备注，随通过／异常记录保存");
        search.addCallback(
                key -> {
                    filter();
                });
        description.setTheme("/label");
        descriptionScroll.setTheme("/scrollpane");
        descriptionScroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        list.setTheme("/comboboxPopup.listbox");
        list.addCallback(
                reason -> {
                    if (list.getSelected() >= 0 && list.getSelected() < visible.size()) {
                        int choice = visible.get(list.getSelected());
                        if (selected != choice && playbackRequested) stop();
                        selected = choice;
                        loadReview();
                    }
                    if (reason == ListBox.CallbackReason.MOUSE_DOUBLE_CLICK) startUses(1);
                });
        for (var label : new Label[] {status, detail, help}) label.setTheme("/label");
        for (var button :
                new Button[] {
                    previous, next, useOnce, useTen, stop, side, target, passed, failed
                }) {
            button.setTheme("/button");
            button.setCanAcceptKeyboardFocus(false);
            content.add(button);
        }
        for (var widget :
                new Widget[] {search, notes, list, status, detail, help, descriptionScroll})
            content.add(widget);
        previous.addCallback(() -> step(-1));
        next.addCallback(() -> step(1));
        useOnce.addCallback(() -> startUses(1));
        useTen.addCallback(() -> startUses(10));
        useTen.setTooltipContent("同一招式连续结算10次，不播放动画；回合日志照常记录");
        stop.addCallback(this::stop);
        side.addCallback(
                () -> {
                    stop();
                    newBattle.run();
                });
        target.addCallback(
                () -> {
                    stop();
                    var b = battle.get();
                    int count = b == null ? 0 : b.animationPreviewTargets();
                    targetSlot++;
                    if (targetSlot >= count) targetSlot = -1;
                    target.setText(targetSlot < 0 ? "目标：按招式" : "目标：第" + (targetSlot + 1) + "只");
                    loadReview();
                });
        passed.addCallback(() -> mark("PASS"));
        failed.addCallback(() -> mark("FAIL"));
        selected =
                Integer.parseInt(resources.preferences().get("client.debug.animation.last", "1"));
        if (!catalogue.contains(selected)) selected = catalogue.get(0);
        filter();
    }

    @Override
    protected void refresh() {
        filter();
        update();
    }

    private void filter() {
        if (playbackRequested) stop();
        int keep = selected;
        String query = search.getText().strip().toLowerCase(Locale.ROOT);
        boolean numeric = query.matches("[0-9]+");
        visible =
                catalogue.stream()
                        .filter(
                                id ->
                                        query.isEmpty()
                                                || Integer.toString(id).equals(query)
                                                || !numeric
                                                        && dex.moveName(id)
                                                                .toLowerCase(Locale.ROOT)
                                                                .contains(query))
                        .toList();
        list.setModel(
                new SimpleListModel<String>() {
                    @Override
                    public int getNumEntries() {
                        return visible.size();
                    }

                    @Override
                    public String getEntry(int index) {
                        int id = visible.get(index);
                        return id + "  " + dex.moveName(id);
                    }
                });
        int index = visible.indexOf(keep);
        if (!visible.isEmpty()) selected = visible.get(Math.max(0, index));
        list.setSelected(visible.isEmpty() ? -1 : Math.max(0, index));
        loadReview();
        detail.setText(visible.size() + " / " + catalogue.size() + " 个注册动画入口（含自定义事件）");
    }

    private void step(int delta) {
        if (visible.isEmpty()) return;
        int index = Math.max(0, Math.min(visible.size() - 1, visible.indexOf(selected) + delta));
        list.setSelected(index);
        selected = visible.get(index);
        loadReview();
    }

    private void startUses(int count) {
        var current = battle.get();
        if (current != null && current.effectTestPlaying()) {
            result = "请等待当前回合结束";
            return;
        }
        stop();
        requestedUses = count;
        remainingUses = count;
        completedUses = 0;
        skipBatchAnimations = count > 1;
        play();
    }

    private void play() {
        if (visible.isEmpty()) return;
        var b = battle.get();
        if (b == null) {
            result = "请点击新随机怪启动测试";
            remainingUses = 0;
            return;
        }
        if (b.effectTestPlaying()) {
            result = "请等待当前回合结束";
            return;
        }
        result = b.testMoveEffect(selected, targetSlot, skipBatchAnimations);
        playbackRequested = b.effectTestPlaying();
        lastPlaybackStatus = "";
        if (!playbackRequested) {
            remainingUses = 0;
            if (result.contains("失败")) mark("ERROR", result);
        }
        resources
                .preferences()
                .save(Map.of("client.debug.animation.last", Integer.toString(selected)));
    }

    private void stop() {
        remainingUses = 0;
        playbackRequested = false;
        var b = battle.get();
        if (b != null) b.stopAnimationPreview();
    }

    private String reviewKey() {
        var b = battle.get();
        return "client.debug.effects.review."
                + selected
                + "."
                + "player"
                + "."
                + (b == null ? 0 : b.animationPreviewTargets())
                + "."
                + targetSlot;
    }

    private void loadReview() {
        int accuracy = 0;
        try {
            accuracy = dex.move(selected).accuracy();
        } catch (IllegalArgumentException ignored) {
        }
        // PokemonStatTextFormatter: zero and 101 are displayed as no accuracy check.
        String accuracyText = accuracy == 0 || accuracy == 101 ? "---" : Integer.toString(accuracy);
        description.setText(
                dex.moveName(selected)
                        + "\n"
                        + resources.strings().get(1851)
                        + " "
                        + accuracyText
                        + "\n\n"
                        + top.totodile.pokemmo.client.ui.common.WordWrapTextFormatter.LPt2(
                                22, dex.moveDescription(selected)));
        descriptionScroll.setScrollPositionY(0);
        String review = resources.preferences().get(reviewKey(), "");
        result = review.isEmpty() ? "未标记" : review;
        int separator = review.lastIndexOf(" | ");
        notes.setText(separator < 0 ? "" : review.substring(separator + 3));
    }

    private void mark(String verdict) {
        mark(verdict, "");
    }

    private void mark(String verdict, String diagnostic) {
        var b = battle.get();
        String value =
                verdict
                        + " | "
                        + Instant.now()
                        + " | "
                        + (b == null ? "无战斗" : b.animationPreviewContext())
                        + " | "
                        + diagnostic
                        + " | "
                        + notes.getText();
        resources.preferences().save(Map.of(reviewKey(), value));
        result = value;
    }

    public void update() {
        if (!isOpen()) return;
        var current = battle.get();
        if (playbackRequested && current != null) {
            if (current.effectTestPlaying() && current.effectTestElapsed() > 60000) {
                mark("TIMEOUT", "超过60秒，等待人工核对");
                remainingUses = 0;
                playbackRequested = false;
            } else if (!current.effectTestPlaying()) {
                playbackRequested = false;
                lastPlaybackStatus = current.effectTestStatus();
                if (lastPlaybackStatus.contains("失败")) {
                    mark("ERROR", lastPlaybackStatus);
                    remainingUses = 0;
                } else {
                    completedUses++;
                    remainingUses = Math.max(0, remainingUses - 1);
                    result = "已使用 " + completedUses + " / " + requestedUses + " 次，等待标记测试结果";
                    if (remainingUses > 0) play();
                }
            }
        }
        String progress =
                requestedUses > 0 ? "  " + completedUses + " / " + requestedUses + " 次" : "";
        String phase =
                current != null && current.effectTestPlaying()
                        ? current.effectTestStatus()
                        : lastPlaybackStatus;
        status.setText(
                selected + " " + dex.moveName(selected) + "  " + phase + progress + "\n" + result);
        status.setTooltipContent(status.getText());
    }

    @Override
    public void closePopup() {
        stop();
        super.closePopup();
    }

    private void layoutContent() {
        int x = content.getInnerX(),
                y = content.getInnerY(),
                w = content.getInnerWidth(),
                h = content.getInnerHeight();
        place(help, x + 8, y + 4, w - 16, 24);
        place(search, x + 8, y + 32, w - 16, 28);
        int column = (w - 24) / 2;
        place(list, x + 8, y + 65, column, Math.max(80, h - 232));
        place(descriptionScroll, x + 16 + column, y + 65, column, Math.max(80, h - 232));
        int controls = y + h - 162;
        Button[] buttons = {previous, next, useOnce, useTen, stop};
        int bw = (w - 24) / 5;
        for (int i = 0; i < buttons.length; i++)
            place(buttons[i], x + 8 + i * (bw + 2), controls, bw, 28);
        place(side, x + 8, controls + 32, 130, 28);
        place(target, x + 142, controls + 32, 150, 28);
        place(passed, x + w - 170, controls + 32, 78, 28);
        place(failed, x + w - 88, controls + 32, 78, 28);
        place(notes, x + 8, controls + 64, w - 16, 28);
        place(detail, x + 8, controls + 95, w - 16, 20);
        place(status, x + 8, controls + 116, w - 16, 44);
    }

    private static void place(Widget widget, int x, int y, int w, int h) {
        widget.setPosition(x, y);
        widget.setSize(w, h);
    }

    @Override
    protected void layout() {
        super.layout();
        content.setPosition(frame.getInnerX(), frame.getInnerY());
        content.setSize(frame.getInnerWidth(), frame.getInnerHeight());
    }
}
