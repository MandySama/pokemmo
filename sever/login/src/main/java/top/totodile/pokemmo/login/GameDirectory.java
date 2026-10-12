package top.totodile.pokemmo.login;

import top.totodile.pokemmo.protocol.GameServerInfo;
import top.totodile.pokemmo.protocol.HandshakeKeys;
import top.totodile.pokemmo.protocol.PacketData;
import top.totodile.pokemmo.protocol.SecureChannel;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/** 配置提供节点地址，状态和在线数由游戏服务的加密探测实时返回。 */
public final class GameDirectory {
    public record Node(int id, String name, String host, int port) {
        public Node {
            new GameServerInfo(id, name, 0, 1, false);
            if (host.isBlank() || host.length() > 253 || port < 1 || port > 65535) {
                throw new IllegalArgumentException("Invalid game node address");
            }
        }
    }

    private final List<Node> nodes;

    public GameDirectory(List<Node> nodes) {
        if (nodes.size() > 16 || nodes.stream().map(Node::id).distinct().count() != nodes.size()) {
            throw new IllegalArgumentException("Invalid game directory");
        }
        this.nodes = List.copyOf(nodes);
    }

    public static GameDirectory configured() {
        return new GameDirectory(List.of(new Node(Integer.getInteger("pokemmo.game.id", 1),
                System.getProperty("pokemmo.game.name", "Live"),
                System.getProperty("pokemmo.game.host", "127.0.0.1"),
                Integer.getInteger("pokemmo.game.port", 7777))));
    }

    public Node node(int id) {
        return nodes.stream().filter(node -> node.id() == id).findFirst().orElse(null);
    }

    public List<GameServerInfo> list() {
        return nodes.stream().map(this::status).toList();
    }

    public GameServerInfo status(Node node) {
        try (Socket socket = new Socket(); SecureChannel channel = new SecureChannel(socket, true)) {
            socket.connect(new InetSocketAddress(node.host(), node.port()), 1500);
            socket.setSoTimeout(1500);
            channel.clientHandshake(HandshakeKeys.rootPublicKey());
            // 0x7E 为节点状态探测扩展，不携带账号数据。
            channel.send(new byte[]{0x7E});
            PacketData data = new PacketData(channel.receive());
            if (data.byteValue() != 0x7E || data.byteValue() != node.id()) throw new java.io.IOException("Node mismatch");
            int load = data.shortValue();
            int capacity = data.shortValue();
            data.end();
            return new GameServerInfo(node.id(), node.name(), load, capacity, true);
        } catch (Exception exception) {
            return new GameServerInfo(node.id(), node.name(), 0, 1, false);
        }
    }
}
