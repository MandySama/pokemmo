package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.protocol.ScriptDiagnosticPackets;

import java.util.ArrayList;
import java.util.List;

/** 手动查询运行状态；关闭面板后正常交互，再刷新对照执行结果。 */
final class ScriptDiagnosticWindow extends ResizableFrame {
    private final List<Label> rows = new ArrayList<>();
    private final Widget content = new Widget() {
        @Override public int getPreferredInnerHeight() { return rows.size() * 28; }
        @Override public int getPreferredInnerWidth() {
            return rows.stream().mapToInt(Label::getPreferredWidth).max().orElse(0);
        }
        @Override protected void layout() {
            int y = getInnerY();
            for (var row : rows) {
                row.setPosition(getInnerX(), y);
                row.setSize(Math.max(getInnerWidth(), row.getPreferredWidth()), 28);
                y += 28;
            }
        }
    };
    private final ScrollPane scroll = new ScrollPane(content);
    private final Button refresh = new Button(ClientStrings.get(252032));
    private final ComboBox<String> section = new ComboBox<>(new de.matthiasmann.twl.model.SimpleChangableListModel<>(
            ClientStrings.get(252070), ClientStrings.get(252071), ClientStrings.get(252072), ClientStrings.get(252073)));
    private ScriptDiagnosticPackets.Snapshot snapshot = new ScriptDiagnosticPackets.Snapshot(List.of());
    private final Label note = new Label(ClientStrings.get(252033));
    private int preferredWidth = 880, preferredHeight = 620;

    ScriptDiagnosticWindow(Runnable close, Runnable query) {
        setTheme("/script-diagnostic");
        setTitle(ClientStrings.get(252030));
        setResizableAxis(ResizableAxis.NONE);
        addCloseCallback(close);
        scroll.setTheme("entries"); content.setTheme("content");
        refresh.addCallback(query);
        section.setSelected(0); section.addCallback(this::renderSnapshot);
        add(note); add(refresh); add(section); add(scroll);
    }

    void busy(boolean busy) { refresh.setEnabled(!busy); }

    void message(int id) {
        show(new ScriptDiagnosticPackets.Snapshot(List.of(new ScriptDiagnosticPackets.Row(id, ""))));
    }

    void show(ScriptDiagnosticPackets.Snapshot snapshot) {
        this.snapshot = snapshot; renderSnapshot();
    }

    private void renderSnapshot() {
        content.removeAllChildren(); rows.clear();
        for (var row : snapshot.rows()) {
            int labelId = row.label(), selected = section.getSelected();
            if (selected == 1 && labelId >= 252048 && labelId <= 252063) continue;
            if (selected == 1 && labelId >= 252066) continue;
            if (selected == 2 && (labelId >= 252043 && labelId <= 252047 || labelId >= 252051 && labelId <= 252063 || labelId >= 252066)) continue;
            if (selected == 3 && labelId >= 252043 && labelId <= 252050) continue;
            String value = row.value();
            if (row.label() == 252063) {
                try { value += "  " + ClientStrings.get(Integer.parseInt(value)).replace('\n', ' '); }
                catch (NumberFormatException ignored) { }
            }
            var label = new Label(ClientStrings.get(row.label()) + "  " + value);
            label.setTheme("heading");
            rows.add(label); content.add(label);
        }
        content.invalidateLayout(); invalidateLayout();
    }

    @Override protected void applyTheme(ThemeInfo info) {
        super.applyTheme(info);
        preferredWidth = info.getParameter("panelWidth", 880);
        preferredHeight = info.getParameter("panelHeight", 620);
    }

    void place(int x, int y, int width, int height) {
        int w = Math.min(preferredWidth, width), h = Math.min(preferredHeight, height);
        setSize(w, h); setPosition(x + (width - w) / 2, y + (height - h) / 2);
    }

    @Override protected void layout() {
        super.layout();
        note.setPosition(getInnerX() + 10, getY() + 38);
        note.setSize(Math.max(0, getInnerWidth() - 130), 28);
        refresh.setPosition(getInnerRight() - 110, getY() + 38); refresh.setSize(100, 28);
        section.setPosition(getInnerX() + 10, getY() + 72); section.setSize(220, 28);
        scroll.setPosition(getInnerX() + 10, getY() + 108);
        scroll.setSize(Math.max(0, getInnerWidth() - 20), Math.max(0, getHeight() - 122));
    }
}
