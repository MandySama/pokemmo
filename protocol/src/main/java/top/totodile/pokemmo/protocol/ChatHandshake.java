package top.totodile.pokemmo.protocol;

/** 仅用于确认聊天端点可用，不携带账号、角色或聊天消息。 */
public final class ChatHandshake {
    public static final int HELLO = 0x43485401;
    public static final int READY = 0x43485402;
    public static final int DEFAULT_PORT = 7778;

    private ChatHandshake() { }
}
