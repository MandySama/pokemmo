package top.totodile.pokemmo.server.chat;

/** IDs and command aliases from the reference ChatChannel / ChatType. */
public enum ChatChannel {
    NORMAL(0, 1500, "normal", "/normal", "/all", "/region", "/talk", "/general", "/n", "/a"),
    SHOUT(3, 1501, "shout", "/shout", "/s"),
    WHISPER(4, 1502, "whisper", "/whisper", "/w"),
    TRADE(5, 1503, "trade", "/trade", "/tr"),
    GLOBAL(6, 1504, "global", "/global", "/gl"),
    CHANNEL(7, 1505, "channel", "/channel", "/ch"),
    TEAM(8, 1507, "team", "/team", "/t"),
    LINK(9, 1510, "link", "/link", "/party", "/l", "/p"),
    ANNOUNCEMENT(16, 1506, "system"),
    SYSTEM(17, 1509, "system"),
    BATTLE(18, 1508, "battle");
    public final int id, stringId;
    public final String style;
    public final String[] aliases;

    ChatChannel(int id, int stringId, String style, String... aliases) {
        this.id = id;
        this.stringId = stringId;
        this.style = style;
        this.aliases = aliases;
    }

    public boolean broadcastInput() {
        return this == NORMAL
                || this == SHOUT
                || this == TRADE
                || this == GLOBAL
                || this == CHANNEL;
    }

    public static ChatChannel command(String command) {
        for (var channel : values())
            for (var alias : channel.aliases) if (alias.equalsIgnoreCase(command)) return channel;
        return null;
    }
}
