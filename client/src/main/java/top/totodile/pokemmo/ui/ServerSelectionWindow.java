package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ResizableFrame;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.protocol.GameServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/** 三列服务器列表，名称和负载状态均来自登录服务。 */
final class ServerSelectionWindow extends ResizableFrame {
    private final DialogLayout content = new DialogLayout();
    private final IntConsumer onJoin;
    private final List<Button> joinButtons = new ArrayList<>();
    private final Label message = new Label();
    private List<GameServerInfo> servers = List.of();
    private boolean busy;
    private int selectedId;

    ServerSelectionWindow(IntConsumer onJoin, Runnable onClose) {
        this.onJoin = onJoin;
        setTheme("serverselect-window");
        setTitle(ClientStrings.get(1013));
        setResizableAxis(ResizableAxis.NONE);
        setDraggable(false);
        addCloseCallback(onClose);
        add(content);
        setServers(List.of());
    }

    void setServers(List<GameServerInfo> servers) {
        this.servers = List.copyOf(servers);
        joinButtons.clear();
        content.removeAllChildren();
        Label name = new Label(ClientStrings.get(1010));
        Label state = new Label(ClientStrings.get(1011));
        Label action = new Label(ClientStrings.get(1012));
        DialogLayout.Group names = content.createParallelGroup(name);
        DialogLayout.Group states = content.createParallelGroup(state);
        DialogLayout.Group actions = content.createParallelGroup(action);
        DialogLayout.Group rows = content.createSequentialGroup()
                .addGroup(content.createParallelGroup(name, state, action));
        for (GameServerInfo server : servers) {
            Label serverName = new Label(server.name());
            int status = !server.joinable() ? 1021 : server.load() >= server.capacity() ? 1023
                    : server.load() > server.capacity() / 2.0 ? 1022 : 1020;
            Label serverState = new Label(ClientStrings.get(status));
            Button join = new Button(ClientStrings.get(1000));
            join.addCallback(() -> onJoin.accept(server.id()));
            join.setEnabled(!busy && server.joinable() && server.load() < server.capacity());
            joinButtons.add(join);
            names.addWidget(serverName);
            states.addWidget(serverState);
            actions.addWidget(join);
            rows.addGroup(content.createParallelGroup(serverName, serverState, join));
        }
        content.setHorizontalGroup(content.createParallelGroup()
                .addGroup(content.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(names).addGroup(states).addGroup(actions).addGap(0, 0, Short.MAX_VALUE))
                .addWidget(message));
        content.setVerticalGroup(content.createSequentialGroup().addGroup(rows)
                .addGap(0, 0, Short.MAX_VALUE).addWidget(message));
        if (!busy) message.setText(servers.isEmpty() ? ClientStrings.get(50004) : "");
        invalidateLayout();
    }

    void setBusy(int serverId) {
        busy = true;
        selectedId = serverId;
        message.setText(ClientStrings.get(50002));
        for (int i = 0; i < joinButtons.size(); i++) {
            Button button = joinButtons.get(i);
            button.setEnabled(false);
            if (servers.get(i).id() == selectedId) button.setText(ClientStrings.get(1003));
        }
    }

    void showConnected(String serverName) {
        message.setText(ClientStrings.get(50001).replace("{00}", serverName));
        for (int i = 0; i < joinButtons.size(); i++) {
            if (servers.get(i).id() == selectedId) joinButtons.get(i).setText(ClientStrings.get(50005));
        }
    }

    void resetMessage() {
        busy = false;
        selectedId = 0;
        message.setText("");
        setServers(servers);
    }
}
