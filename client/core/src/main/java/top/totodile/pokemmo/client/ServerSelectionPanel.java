package top.totodile.pokemmo.client;

import de.matthiasmann.twl.*;
import java.util.ArrayList;
import java.util.List;
import top.totodile.pokemmo.server.login.LoginService.*;

/** 参考客户端的三列表格：服务器名称、服务器状态、逐行登录按钮。 */
final class ServerSelectionPanel extends ResizableFrame {
    private final DialogLayout content = new DialogLayout();
    private final List<Button> serverButtons = new ArrayList<>();
    private final ClientStrings strings;
    private final LoginController controller;
    private Session displayedSession;

    ServerSelectionPanel(ClientStrings strings, LoginController controller) {
        this.strings = strings;
        this.controller = controller;
        content.setHorizontalGroup(content.createParallelGroup());
        content.setVerticalGroup(content.createSequentialGroup());
        setTheme("serverselect-window");
        setTitle(strings.get(1013));
        setDraggable(false);
        setResizableAxis(ResizableAxis.NONE);
        add(content);
        setVisible(false);
    }

    void refresh() {
        Session session = controller.session();
        if (session == null) return;
        if (displayedSession != session) {
            displayedSession = session;
            rebuild(session);
            if (!serverButtons.isEmpty()) serverButtons.get(0).requestKeyboardFocus();
        }
        invalidateLayout();
    }

    private void rebuild(Session session) {
        content.setHorizontalGroup(null);
        content.setVerticalGroup(null);
        content.removeAllChildren();
        serverButtons.clear();
        Label nameHeader = new Label(strings.get(1010));
        Label stateHeader = new Label(strings.get(1011));
        Label loginHeader = new Label(strings.get(1012));
        var names = content.createParallelGroup(nameHeader);
        var states = content.createParallelGroup(stateHeader);
        var actions = content.createParallelGroup(loginHeader);
        var rows = content.createSequentialGroup().addGroup(content.createParallelGroup(nameHeader, stateHeader, loginHeader));
        for (Server server : session.servers()) {
            Label name = new Label(server.name());
            Label state = new Label(strings.get(1020));
            Button login = new Button(strings.get(1000));
            login.addCallback(() -> controller.select(server.id()));
            serverButtons.add(login);
            names.addWidget(name); states.addWidget(state); actions.addWidget(login);
            rows.addGroup(content.createParallelGroup(name, state, login));
        }
        content.setHorizontalGroup(content.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
                .addGroup(names).addGroup(states).addGroup(actions).addGap(0, 0, Short.MAX_VALUE));
        content.setVerticalGroup(rows);
    }
}
