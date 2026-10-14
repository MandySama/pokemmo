package top.totodile.pokemmo.chat;

import top.totodile.pokemmo.protocol.ChatHandshake;
import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** 独立聊天端点的就绪握手；当前不接收玩家身份及消息。 */
public final class ChatServer implements AutoCloseable {
    private final ServerSocket server;
    private final Set<Socket> clients = ConcurrentHashMap.newKeySet();
    private volatile boolean closed;

    public ChatServer(String host, int port) throws IOException {
        server = new ServerSocket();
        server.bind(new InetSocketAddress(host, port));
    }

    public int port() { return server.getLocalPort(); }

    public void serve() throws IOException {
        while (!closed) {
            try {
                Socket socket = server.accept();
                clients.add(socket);
                if (closed) socket.close();
                else Thread.startVirtualThread(() -> handshake(socket));
            } catch (SocketException exception) {
                if (!closed) throw exception;
            }
        }
    }

    private void handshake(Socket socket) {
        try (socket) {
            socket.setSoTimeout(5000);
            var input = new DataInputStream(socket.getInputStream());
            if (input.readInt() != ChatHandshake.HELLO) return;
            var output = new DataOutputStream(socket.getOutputStream());
            output.writeInt(ChatHandshake.READY);
            output.flush();
            socket.setSoTimeout(0);
            // 保留连接直到客户端退出；本阶段拒绝后续载荷。
            input.read();
        } catch (IOException ignored) {
        } finally {
            clients.remove(socket);
        }
    }

    @Override
    public void close() throws IOException {
        closed = true;
        server.close();
        for (Socket socket : clients) {
            try { socket.close(); } catch (IOException ignored) { }
        }
        clients.clear();
    }
}
