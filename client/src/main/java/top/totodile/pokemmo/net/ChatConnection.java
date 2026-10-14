package top.totodile.pokemmo.net;

import top.totodile.pokemmo.protocol.ChatHandshake;
import java.io.*;
import java.net.*;

/** 后台握手状态由 GUI 线程轮询，关闭会中断连接与阻塞读取。 */
public final class ChatConnection implements AutoCloseable {
    public enum State { CONNECTING, CONNECTED, DISCONNECTED }
    private final Socket socket = new Socket();
    private volatile State state = State.CONNECTING;
    private volatile boolean closed;

    public ChatConnection(String host, int port) {
        Thread.startVirtualThread(() -> {
            try {
                socket.connect(new InetSocketAddress(host, port), 5000);
                socket.setSoTimeout(5000);
                var output = new DataOutputStream(socket.getOutputStream());
                output.writeInt(ChatHandshake.HELLO);
                output.flush();
                var input = new DataInputStream(socket.getInputStream());
                if (input.readInt() != ChatHandshake.READY) throw new IOException("Invalid chat handshake");
                if (closed) return;
                state = State.CONNECTED;
                socket.setSoTimeout(0);
                input.read();
            } catch (IOException ignored) {
            } finally {
                state = State.DISCONNECTED;
                try { socket.close(); } catch (IOException ignored) { }
            }
        });
    }

    public State state() { return state; }

    @Override
    public void close() {
        closed = true;
        state = State.DISCONNECTED;
        try { socket.close(); } catch (IOException ignored) { }
    }
}
