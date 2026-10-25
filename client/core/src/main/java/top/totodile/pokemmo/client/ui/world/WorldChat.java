package top.totodile.pokemmo.client.ui.world;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.config.ClientSettings;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.pokedex.PokedexChoice;
import top.totodile.pokemmo.server.chat.*;

import java.util.*;

/** ChatDetailWindow: independent display filters and outgoing channel, with retained history. */
public final class WorldChat extends ResizableFrame implements AutoCloseable {
    private final ClientStrings strings;
    private final ClientSettings settings;
    private final boolean mobile;
    private final EditField input =
            new EditField() {
                @Override
                protected void keyboardFocusGained() {
                    super.keyboardFocusGained();
                    editing = true;
                    focusChanged.run();
                }

                @Override
                protected void keyboardFocusLost() {
                    super.keyboardFocusLost();
                    editing = false;
                    focusChanged.run();
                }
            };
    private final Button send, hide = new Button();
    private final PokedexChoice channel;
    private final ChatChannel[] outgoing = {
        ChatChannel.NORMAL,
        ChatChannel.SHOUT,
        ChatChannel.TRADE,
        ChatChannel.GLOBAL,
        ChatChannel.CHANNEL
    };
    private final List<LocalChatService.Message> messages = new ArrayList<>();
    private final Map<LocalChatService.Message, List<String>> battleNames = new IdentityHashMap<>();
    private final List<String> sent = new ArrayList<>();
    private final List<ToggleButton> tabs = new ArrayList<>();
    private final List<Set<Integer>> hidden = new ArrayList<>();
    private final Widget lines =
            new Widget() {
                @Override
                public int getPreferredInnerHeight() {
                    return lineHeight(getInnerWidth(), false);
                }

                @Override
                protected void layout() {
                    lineHeight(getInnerWidth(), true);
                }

                @Override
                protected boolean handleEvent(Event event) {
                    return handleBattleContext(event) || super.handleEvent(event);
                }
            };
    private final ScrollPane scroll =
            new top.totodile.pokemmo.client.ui.common.TouchScrollPane(lines);
    private MenuManager battleMenu;
    private LocalChatService.Session session;
    private int sessionVersion;
    private Runnable focusChanged = () -> {};
    private boolean editing, scrollBottom;
    private int selected, historyIndex = -1;
    private int savedWidth, savedHeight, savedX, savedBottom;
    private int hudX, hudY, hudHeight;
    private boolean resizing;

    private int setting(String name, int fallback) {
        try {
            return Integer.parseInt(
                    settings.get("client.gui.hud.chatframe." + name, Integer.toString(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    /** ChatDetailWindow.C/ve: geometry uses a bottom-relative Y offset. */
    public void positionInHud(int x, int y, int width, int height, int battleBottom) {
        hudX = x;
        hudY = y;
        hudHeight = height;
        if (mobile) {
            setPosition(x, y);
            setSize(width, height);
            return;
        }
        int w = Math.min(width, Math.max(getMinWidth(), savedWidth));
        int maxHeight =
                battleBottom > 0
                        ? Math.max(
                                getMinHeight(),
                                height + Math.min(0, savedBottom) - (battleBottom - y) - 5)
                        : height;
        int h = Math.min(height, Math.min(maxHeight, Math.max(getMinHeight(), savedHeight)));
        int left = x + Math.max(0, Math.min(savedX, width - w));
        int top = y + Math.max(0, Math.min(height - h, height + savedBottom - h));
        if (battleBottom > 0) top = Math.max(top, battleBottom + 5);
        top = Math.min(top, y + height - h);
        setPosition(left, top);
        setSize(w, h);
    }

    private void rememberGeometry(boolean persist) {
        savedWidth = getWidth();
        savedHeight = getHeight();
        savedX = getX() - hudX;
        savedBottom = getBottom() - hudY - hudHeight;
        if (persist)
            settings.save(
                    Map.of(
                            "client.gui.hud.chatframe.width", Integer.toString(savedWidth),
                            "client.gui.hud.chatframe.height", Integer.toString(savedHeight),
                            "client.gui.hud.chatframe.x", Integer.toString(savedX),
                            "client.gui.hud.chatframe.y", Integer.toString(savedBottom)));
    }

    @Override
    public int getMinWidth() {
        return Math.max(super.getMinWidth(), tabs.size() * 52 + 26 + getBorderHorizontal());
    }

    @Override
    public int getMinHeight() {
        return Math.max(super.getMinHeight(), getBorderVertical() + 2 * 26 + 18);
    }

    @Override
    protected boolean handleEvent(Event event) {
        if (!mobile
                && event.getType() == Event.Type.MOUSE_BTNDOWN
                && event.getMouseButton() == Event.MOUSE_LBUTTON)
            resizing =
                    event.getMouseX() < getInnerX()
                            || event.getMouseX() >= getInnerRight()
                            || event.getMouseY() < getInnerY()
                            || event.getMouseY() >= getInnerBottom();
        boolean handled = super.handleEvent(event);
        if (resizing && event.getType() == Event.Type.MOUSE_DRAGGED) rememberGeometry(false);
        if (resizing && (event.isMouseDragEnd() || event.getType() == Event.Type.MOUSE_BTNUP)) {
            rememberGeometry(true);
            resizing = false;
        }
        return handled;
    }

    public WorldChat(ClientStrings strings, ClientSettings settings, boolean mobile) {
        this.strings = strings;
        this.settings = settings;
        this.mobile = mobile;
        setTheme("/world-chat");
        setCanAcceptKeyboardFocus(false);
        setDraggable(false);
        setResizableAxis(mobile ? ResizableAxis.NONE : ResizableAxis.BOTH);
        savedWidth = setting("width", 500);
        savedHeight = setting("height", 220);
        savedX = setting("x", 5);
        savedBottom = setting("y", -24);
        String[] names =
                settings.get("client.ui.chat.tab.names", "Local;Global;Trade;Whispers;Battle")
                        .split(";");
        String[] filters =
                settings.get(
                                "client.ui.chat.tab.hidden_chat_types",
                                "6,18,5;7,18,0,3,4,5,9,8,17;7,6,18,0,3,4,9,8,17;7,6,18,0,3,5,9,8;7,16,6,0,3,4,5,9,8,17")
                        .split(";", -1);
        for (int i = 0; i < names.length && i < filters.length; i++) {
            final int index = i;
            var tab = new ToggleButton(names[i]);
            tab.setTheme("tab");
            tab.setCanAcceptKeyboardFocus(false);
            tab.addCallback(() -> selectTab(index));
            tabs.add(tab);
            add(tab);
            var excluded = new HashSet<Integer>();
            for (String id : filters[i].split(","))
                try {
                    excluded.add(Integer.parseInt(id));
                } catch (NumberFormatException ignored) {
                }
            hidden.add(excluded);
        }
        if (tabs.isEmpty()) throw new IllegalArgumentException("Chat requires a display tab");
        String active = settings.get("client.ui.chat.tab.currently_active_tab", "Local");
        for (int i = 0; i < tabs.size(); i++)
            if (tabs.get(i).getText().equals(active)) selected = i;
        channel =
                new PokedexChoice(
                        this::focusInput,
                        Arrays.stream(outgoing)
                                .map(c -> strings.get(c.stringId))
                                .toArray(String[]::new));
        channel.setTheme("/combobox");
        add(channel);
        input.setTheme("editfield");
        input.setMaxTextLength(150);
        input.addCallback(
                key -> {
                    if (key == Event.KEY_RETURN) submit();
                });
        add(input);
        send = new Button(strings.get(1525));
        send.setTheme("button");
        send.setCanAcceptKeyboardFocus(false);
        send.addCallback(this::submit);
        add(send);
        hide.setTheme("hide");
        hide.setCanAcceptKeyboardFocus(false);
        hide.addCallback(
                () -> {
                    blur();
                    setVisible(false);
                });
        add(hide);
        lines.setTheme("/world-chat.lines");
        scroll.setTheme("scrollpane");
        scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        add(scroll);
        selectTab(selected);
    }

    public void connect(String server, String character, String name) {
        close();
        int version = sessionVersion;
        session =
                LocalChatService.connect(
                        server,
                        character,
                        name,
                        message ->
                                com.badlogic.gdx.Gdx.app.postRunnable(
                                        () -> {
                                            if (sessionVersion == version) receive(message);
                                        }));
    }

    public void onFocusChanged(Runnable value) {
        focusChanged = value;
    }

    public boolean editing() {
        return editing;
    }

    public void focusInput() {
        setVisible(true);
        editing = true;
        focusChanged.run();
        input.requestKeyboardFocus();
    }

    public boolean blur() {
        channel.closeChoices();
        if (!editing) return false;
        editing = false;
        input.giveupKeyboardFocus();
        focusChanged.run();
        return true;
    }

    public void submit() {
        String text = input.getText().strip();
        if (text.isEmpty()) {
            blur();
            return;
        }
        ChatChannel destination = outgoing[channel.selected()];
        if (text.startsWith("/")) {
            int space = text.indexOf(' ');
            String command = space < 0 ? text : text.substring(0, space);
            var requested = ChatChannel.command(command);
            if (requested != null) {
                if (!requested.broadcastInput()) {
                    receive(
                            new LocalChatService.Message(
                                    ChatChannel.SYSTEM,
                                    "",
                                    strings.get(requested == ChatChannel.WHISPER ? 1524 : 1514)));
                    return;
                }
                destination = requested;
                for (int i = 0; i < outgoing.length; i++)
                    if (outgoing[i] == requested) channel.select(i);
                text = space < 0 ? "" : text.substring(space + 1).strip();
                if (text.isEmpty()) {
                    input.setText("");
                    return;
                }
            }
        }
        if (session == null || !session.send(destination, text)) {
            receive(new LocalChatService.Message(ChatChannel.SYSTEM, "", strings.get(1514)));
            return;
        }
        if (sent.isEmpty() || !sent.get(sent.size() - 1).equals(input.getText()))
            sent.add(input.getText());
        if (sent.size() > 20) sent.remove(0);
        input.setText("");
        historyIndex = -1;
        blur();
    }

    public boolean history(boolean older) {
        if (sent.isEmpty()) return true;
        historyIndex = Math.max(0, Math.min(sent.size() - 1, historyIndex + (older ? 1 : -1)));
        input.setText(sent.get(sent.size() - 1 - historyIndex));
        return true;
    }

    public void battle(String text) {
        battle(text, List.of());
    }

    /** Requested battle-history action, using the existing PC popup-menu widget. */
    private boolean handleBattleContext(Event event) {
        if (mobile
                || event.getType() != Event.Type.MOUSE_BTNDOWN
                || event.getMouseButton() != Event.MOUSE_RBUTTON
                || hidden.get(selected).contains(ChatChannel.BATTLE.id)) return false;
        openBattleMenu(event.getMouseX(), event.getMouseY());
        return true;
    }

    private void openBattleMenu(int x, int y) {
        if (battleMenu != null) battleMenu.closePopup();
        var menu = new Menu();
        menu.add(new MenuAction(strings.get(8008), this::clearBattleHistory));
        battleMenu = menu.openPopupMenu(this, x, y);
    }

    private void clearBattleHistory() {
        messages.removeIf(message -> message.channel() == ChatChannel.BATTLE);
        battleNames.clear();
        rebuild();
        scrollBottom = true;
    }

    public void battle(String text, List<String> names) {
        var message = new LocalChatService.Message(ChatChannel.BATTLE, "", text);
        battleNames.put(message, List.copyOf(names));
        receive(message);
    }

    /** BattleStatStageModifier: a channel-prefixed empty row followed by the turn heading. */
    public void battleTurn(int turn) {
        battle("");
        battle(strings.format(5075, Integer.toString(turn)));
    }

    public void receive(LocalChatService.Message message) {
        if (message.text() == null
                || message.text().isBlank() && message.channel() != ChatChannel.BATTLE) return;
        boolean bottom = scroll.getScrollPositionY() >= scroll.getMaxScrollPosY();
        messages.add(message);
        rebuild();
        scrollBottom |= bottom;
    }

    private void selectTab(int index) {
        selected = index;
        for (int i = 0; i < tabs.size(); i++) tabs.get(i).setActive(i == index);
        settings.save(Map.of("client.ui.chat.tab.currently_active_tab", tabs.get(index).getText()));
        rebuild();
        scrollBottom = true;
    }

    private void rebuild() {
        lines.removeAllChildren();
        for (var message : messages)
            if (!hidden.get(selected).contains(message.channel().id)) {
                String text =
                        message.channel() == ChatChannel.BATTLE
                                ? message.text().replace('\n', ' ')
                                : message.text();
                if (!message.sender().isEmpty())
                    text =
                            "["
                                    + strings.get(message.channel().stringId)
                                    + "] "
                                    + message.sender()
                                    + ": "
                                    + text;
                String prefix =
                        message.channel() == ChatChannel.BATTLE
                                ? "[" + strings.get(ChatChannel.BATTLE.stringId) + "]"
                                : "";
                var label =
                        new MessageLine(
                                prefix + text,
                                prefix.length(),
                                battleNames.getOrDefault(message, List.of()));
                label.setTheme("message-" + message.channel().style);
                lines.add(label);
            }
        lines.invalidateLayout();
        scroll.invalidateLayout();
        invalidateLayout();
    }

    private int lineHeight(int width, boolean position) {
        int y = 0;
        for (int i = 0; i < lines.getNumChildren(); i++) {
            var label = (MessageLine) lines.getChild(i);
            String text = label.raw;
            var font = label.getFont();
            if (font == null) continue;
            var wrapped = new StringBuilder();
            int start = 0;
            for (int end = 0; end < text.length(); ) {
                int next = text.offsetByCodePoints(end, 1);
                if (text.charAt(end) == '\n') {
                    wrapped.append(text, start, next);
                    start = next;
                    end = next;
                    continue;
                }
                if (end > start
                        && font.computeTextWidth(text.substring(start, next))
                                > Math.max(1, width - 4)) {
                    wrapped.append(text, start, end).append('\n');
                    start = end;
                }
                end = next;
            }
            wrapped.append(text.substring(start));
            String value = wrapped.toString();
            int height =
                    (int) (value.chars().filter(c -> c == '\n').count() + 1) * font.getLineHeight()
                            + 3;
            if (position) {
                label.setText(value);
                label.setSize(width, height);
                label.setPosition(lines.getInnerX() + 2, lines.getInnerY() + y);
            }
            y += height;
        }
        return y;
    }

    private final class MessageLine extends Label {
        @Override
        protected boolean handleEvent(Event event) {
            return handleBattleContext(event) || super.handleEvent(event);
        }

        final String raw;
        final byte[] roles;
        final int prefixLength;
        private de.matthiasmann.twl.renderer.Font prefixFont, nameFont;

        MessageLine(String text, int prefixLength, List<String> names) {
            super(text);
            raw = text;
            this.prefixLength = prefixLength;
            roles = new byte[text.length()];
            if (prefixLength > 0) {
                for (String name :
                        names.stream()
                                .filter(n -> !n.isBlank())
                                .distinct()
                                .sorted(Comparator.comparingInt(String::length).reversed())
                                .toList()) {
                    for (int at = text.indexOf(name, prefixLength);
                            at >= 0;
                            at = text.indexOf(name, at + name.length()))
                        Arrays.fill(roles, at, at + name.length(), (byte) 2);
                }
                Arrays.fill(roles, 0, prefixLength, (byte) 1);
            }
            setAutoSize(false);
        }

        @Override
        protected void applyTheme(ThemeInfo theme) {
            super.applyTheme(theme);
            if (prefixLength > 0) {
                prefixFont = theme.getFont("prefixFont");
                nameFont = theme.getFont("nameFont");
            }
        }

        @Override
        protected void paintWidget(GUI gui) {
            if (prefixLength == 0) {
                super.paintWidget(gui);
                return;
            }
            String value = getText();
            int x = computeTextX(), startX = x, y = computeTextY(), rawIndex = 0;
            for (int at = 0; at < value.length(); ) {
                if (value.charAt(at) == '\n') {
                    x = startX;
                    y += getFont().getLineHeight();
                    at++;
                    continue;
                }
                byte role = roles[rawIndex];
                int end = at + 1, rawEnd = rawIndex + 1;
                while (end < value.length()
                        && value.charAt(end) != '\n'
                        && rawEnd < roles.length
                        && roles[rawEnd] == role) {
                    end++;
                    rawEnd++;
                }
                var font = role == 1 ? prefixFont : role == 2 ? nameFont : getFont();
                x += font.drawText(getAnimationState(), x, y, value, at, end);
                at = end;
                rawIndex = rawEnd;
            }
        }
    }

    @Override
    protected void paintWidget(GUI gui) {
        super.paintWidget(gui);
        if (scrollBottom) {
            scroll.setScrollPositionY(Integer.MAX_VALUE);
            scrollBottom = false;
        }
    }

    @Override
    protected void layout() {
        int row = mobile ? 60 : 26, x = getInnerX();
        int available = Math.max(0, getInnerWidth() - row);
        for (var tab : tabs) {
            int width =
                    Math.min(
                            available / tabs.size(),
                            Math.max(mobile ? 80 : 52, tab.getPreferredWidth()));
            tab.setSize(width, row);
            tab.setPosition(x, getInnerY());
            x += width;
        }
        hide.setSize(row, row);
        hide.setPosition(getInnerRight() - row, getInnerY());
        int bottom = getInnerBottom() - row;
        channel.setSize(Math.min(100, getInnerWidth() / 4), row);
        channel.setPosition(getInnerX(), bottom);
        send.setSize(mobile ? 64 : 50, row);
        send.setPosition(getInnerRight() - send.getWidth(), bottom);
        input.setSize(Math.max(0, send.getX() - channel.getRight() - 4), row);
        input.setPosition(channel.getRight() + 2, bottom);
        scroll.setSize(getInnerWidth(), Math.max(0, bottom - getInnerY() - row - 2));
        scroll.setPosition(getInnerX(), getInnerY() + row);
    }

    @Override
    public void close() {
        if (battleMenu != null) {
            battleMenu.closePopup();
            battleMenu = null;
        }
        sessionVersion++;
        if (session != null) {
            session.close();
            session = null;
        }
    }
}
