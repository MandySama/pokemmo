package top.totodile.pokemmo.client.ui.login;

import de.matthiasmann.twl.*;
import java.util.List;
import top.totodile.pokemmo.client.resources.ClientStrings;

/** 登录页公告；当前使用静态标题，不请求公告源。 */
public final class AnnouncementsPanel extends ResizableFrame {
    private final DialogLayout content = new DialogLayout();
    private static final List<String> TITLES = List.of(
            "PokeMMO B2/W2",
            "Changelog: Shiny Wars 2026",
            "Changelog: Lunar New Year 2026",
            "Changelog: Xmas 2025",
            "Changelog: 01/12/2025",
            "Changelog: Halloween 2025");

    public AnnouncementsPanel(ClientStrings strings, boolean mobile) {
        setTheme("/changelog-frame");
        setTitle(mobile ? "" : strings.get(1004));
        setDraggable(false);
        setResizableAxis(ResizableAxis.NONE);
        var columns = content.createParallelGroup();
        var rows = content.createSequentialGroup();
        for (String title : TITLES) {
            Button entry = new Button(title);
            // 静态公告只展示，不抢占登录和角色页面的默认键盘焦点。
            entry.setCanAcceptKeyboardFocus(false);
            columns.addWidget(entry);
            rows.addWidget(entry);
        }
        content.setHorizontalGroup(columns);
        content.setVerticalGroup(rows);
        add(content);
    }
}
