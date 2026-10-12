package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** 服务器选择列表 0x22，只包含节点名称、负载和可加入标记。 */
public record GameServerInfo(int id, String name, int load, int capacity, boolean joinable) {
    public GameServerInfo {
        if (id < 1 || id > 127 || name.isBlank() || name.length() > 64 || name.indexOf('\0') >= 0
                || load < 0 || capacity < 1 || load > 65535 || capacity > 65535) {
            throw new IllegalArgumentException("Invalid game server info");
        }
    }

    public static byte[] encodeList(List<GameServerInfo> servers) {
        if (servers.size() > 16) throw new IllegalArgumentException("Too many game servers");
        PacketData data = new PacketData(2300).putByte(0x22).putByte(servers.size())
                .putByte(servers.isEmpty() ? 0 : servers.getFirst().id());
        if (servers.isEmpty()) data.putByte(0);
        for (GameServerInfo server : servers) {
            data.putByte(server.id()).putString(server.name()).putShort(server.load())
                    .putShort(server.capacity()).putByte(server.joinable() ? 1 : 0);
        }
        return data.written();
    }

    public static List<GameServerInfo> decodeList(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 0x22) throw new IOException("Unexpected server list opcode");
        int count = data.byteValue();
        if (count > 16) throw new IOException("Too many game servers");
        int preferred = data.byteValue();
        if (count == 0 && (preferred != 0 || data.byteValue() != 0)) throw new IOException("Invalid empty list");
        List<GameServerInfo> result = new ArrayList<>();
        var ids = new HashSet<Integer>();
        try {
            for (int i = 0; i < count; i++) {
                GameServerInfo info = new GameServerInfo(data.byteValue(), data.string(64), data.shortValue(),
                        data.shortValue(), data.booleanValue());
                if (!ids.add(info.id())) throw new IOException("Duplicate game server ID");
                result.add(info);
            }
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid server list", exception);
        }
        data.end();
        return List.copyOf(result);
    }
}
