package top.totodile.pokemmo.game;

import top.totodile.pokemmo.protocol.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/** 游戏服务入口连接：校验短期入服凭证，维护在线会话及加密保活。 */
public final class GameServer implements AutoCloseable {
    private static final System.Logger LOGGER = System.getLogger(GameServer.class.getName());
    private final WorldRules[] worldRules = {new FireRedWorldRules(0), new FireRedWorldRules(1), new BlackWhiteWorldRules(), new BlackWhiteWorldRules(3), new FireRedWorldRules(4), new BlackWhiteWorldRules(5)};
    private final java.util.concurrent.ScheduledExecutorService saves = Executors.newScheduledThreadPool(2);
    private final ServerSocket listener = new ServerSocket();
    private final java.util.concurrent.ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor();
    private final Set<Socket> connections = ConcurrentHashMap.newKeySet();
    private final Set<Integer> accounts = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Long> consumed = new ConcurrentHashMap<>();
    private final Semaphore connectionLimit = new Semaphore(128);
    private final int serverId;
    private final int capacity;
    private final PrivateKey handshakeKey;
    private final PublicKey ticketKey;
    private final top.totodile.pokemmo.persistence.CharacterStore characters;

    public GameServer(InetSocketAddress address, int serverId, int capacity, PrivateKey handshakeKey,
                      PublicKey ticketKey) throws IOException {
        this(address, serverId, capacity, handshakeKey, ticketKey, null);
    }

    public GameServer(InetSocketAddress address, int serverId, int capacity, PrivateKey handshakeKey,
                      PublicKey ticketKey, top.totodile.pokemmo.persistence.CharacterStore characters) throws IOException {
        this.characters = characters;
        if (serverId < 1 || serverId > 127 || capacity < 1 || capacity > 100) {
            throw new IllegalArgumentException("Invalid game server configuration");
        }
        this.serverId = serverId;
        this.capacity = capacity;
        this.handshakeKey = handshakeKey;
        this.ticketKey = ticketKey;
        listener.bind(address);
    }

    public int port() {
        return listener.getLocalPort();
    }

    public int onlineCount() {
        return accounts.size();
    }

    public void serve() throws IOException {
        while (!listener.isClosed()) {
            Socket socket;
            try {
                socket = listener.accept();
            } catch (SocketException exception) {
                if (listener.isClosed()) return;
                throw exception;
            }
            if (!connectionLimit.tryAcquire()) {
                socket.close();
                continue;
            }
            connections.add(socket);
            try {
                workers.submit(() -> handle(socket));
            } catch (RuntimeException exception) {
                connections.remove(socket);
                connectionLimit.release();
                socket.close();
                if (!listener.isClosed()) throw exception;
            }
        }
    }

    private synchronized boolean admit(GameTicket.Claims claims) {
        long now = System.currentTimeMillis();
        consumed.entrySet().removeIf(entry -> entry.getValue() <= now);
        // 一分钟内最多保留 4096 次消费记录；容量不足时拒绝而不丢弃防重放记录。
        if (consumed.size() >= 4096 || accounts.size() >= capacity || accounts.contains(claims.accountId())
                || consumed.putIfAbsent(claims.nonce(), claims.expiresAt()) != null) return false;
        accounts.add(claims.accountId());
        return true;
    }

    private void handle(Socket socket) {
        Integer accountId = null;
        long activeCharacter = 0;
        WorldSession world = null;
        try (SecureChannel channel = new SecureChannel(socket, true)) {
            socket.setSoTimeout(10_000);
            socket.setTcpNoDelay(true);
            channel.serverHandshake(handshakeKey);
            byte[] packet = channel.receive();
            if (packet.length == 1 && packet[0] == 0x7E) {
                channel.send(new PacketData(6).putByte(0x7E).putByte(serverId).putShort(accounts.size())
                        .putShort(capacity).written());
                return;
            }
            boolean accepted = false;
            try {
                GameAdmission.Request request = GameAdmission.decodeRequest(packet);
                GameTicket.Claims claims = GameTicket.verify(request.ticket(), ticketKey, serverId,
                        System.currentTimeMillis());
                if (request.accountId() == claims.accountId()
                        && (characters == null || characters.ownsAccount(claims.accountId(), claims.username())) && admit(claims)) {
                    accountId = claims.accountId();
                    accepted = true;
                }
            } catch (IOException | GeneralSecurityException exception) {
                LOGGER.log(System.Logger.Level.DEBUG, "Game admission rejected: {0}", exception.getClass().getSimpleName());
            }
            channel.send(GameAdmission.response(accepted));
            if (!accepted) return;
            socket.setSoTimeout(45_000);
            while (!socket.isClosed()) {
                byte[] command = channel.receive();
                if (command.length == 0) throw new IOException("Empty game command");
                if (world != null) world.checkHealthy();
                if (command[0] == 0x20) channel.send(GameAdmission.heartbeatResponse(command));
                else if (command[0] == 2) {
                    if (command.length != 1) throw new IOException("Malformed character list request");
                    channel.send(CharacterPackets.listResponse(characters == null ? java.util.List.of() : characters.list(accountId)));
                } else if (command[0] == 3) {
                    CharacterPackets.CreationResult result;
                    try {
                        var appearance = CharacterPackets.decodeCreate(command);
                        result = characters == null ? new CharacterPackets.CreationResult(CharacterPackets.Status.SYSTEM_ERROR, java.util.List.of())
                                : characters.create(accountId, appearance);
                    } catch (IOException exception) {
                        result = new CharacterPackets.CreationResult(CharacterPackets.Status.INVALID, java.util.List.of());
                    } catch (java.sql.SQLException exception) {
                        LOGGER.log(System.Logger.Level.ERROR, "Unable to create character", exception);
                        result = new CharacterPackets.CreationResult(CharacterPackets.Status.SYSTEM_ERROR, java.util.List.of());
                    }
                    channel.send(CharacterPackets.createResponse(result));
                } else if (command[0] == 0x71) {
                    CharacterPackets.CreationResult result;
                    try {
                        long id = CharacterPackets.decodeDelete(command);
                        if (id == activeCharacter && world != null) {
                            world.close(); world = null; activeCharacter = 0;
                        }
                        result = characters == null ? new CharacterPackets.CreationResult(CharacterPackets.Status.SYSTEM_ERROR, java.util.List.of())
                                : characters.delete(accountId, id);
                    } catch (IOException exception) {
                        result = new CharacterPackets.CreationResult(CharacterPackets.Status.INVALID, java.util.List.of());
                    } catch (java.sql.SQLException exception) {
                        LOGGER.log(System.Logger.Level.ERROR, "Unable to delete character", exception);
                        result = new CharacterPackets.CreationResult(CharacterPackets.Status.SYSTEM_ERROR, java.util.List.of());
                    }
                    channel.send(CharacterPackets.deleteResponse(result));
                } else if (command[0] == 4) {
                    long id = CharacterPackets.decodeEnter(command);
                    if (world != null) {
                        world.close();
                        world = null;
                    }
                    var owned = characters == null ? java.util.List.<CharacterPackets.CharacterInfo>of()
                            : characters.list(accountId).stream().filter(character -> character.id() == id).toList();
                    activeCharacter = owned.isEmpty() ? 0 : id;
                    if (!owned.isEmpty()) world = new WorldSession(worldRules[owned.getFirst().appearance().region()], characters, accountId, owned.getFirst(), saves);
                    channel.send(new byte[]{4, (byte) (owned.isEmpty() ? 0 : 1)});
                } else if (command[0] == 5) {
                    if (command.length != 1 || world == null) throw new IOException("Invalid player load request");
                    long selected = activeCharacter;
                    channel.send(CharacterPackets.enterResponse(characters.list(accountId).stream().filter(c -> c.id() == selected).toList()));
                } else if ((command[0] & 255) == WorldPackets.REGION_TELEPORT) {
                    if (world == null || activeCharacter == 0) throw new IOException("No active world");
                    PacketData request = new PacketData(command);
                    request.byteValue();
                    int region = request.byteValue(), group = request.byteValue(), map = request.byteValue();
                    int x = request.shortValue(), y = request.shortValue(); request.end();
                    try {
                        if (region >= worldRules.length) throw new IOException("Invalid target region");
                        var info = world.transferRegion(region, group, map, x, y, worldRules[region]);
                        channel.send(CharacterPackets.enterResponse(java.util.List.of(info)));
                    } catch (IOException error) {
                        channel.send(CharacterPackets.enterResponse(java.util.List.of()));
                    }
                } else if (command[0] == top.totodile.pokemmo.protocol.SearchPackets.OPCODE) {
                    if (world == null || activeCharacter == 0 || characters == null) throw new IOException("No active search character");
                    var request = top.totodile.pokemmo.protocol.SearchPackets.decodeRequest(command);
                    top.totodile.pokemmo.protocol.SearchPackets.Result result;
                    try {
                        result = characters.search(accountId, activeCharacter, request,
                                Boolean.parseBoolean(System.getProperty("pokemmo.game.debug", "true")));
                    } catch (java.sql.SQLException exception) {
                        LOGGER.log(System.Logger.Level.ERROR, "Unable to update search inventory", exception);
                        result = new top.totodile.pokemmo.protocol.SearchPackets.Result(
                                top.totodile.pokemmo.protocol.SearchPackets.Status.SYSTEM_ERROR, java.util.Map.of(), java.util.List.of());
                    }
                    channel.send(top.totodile.pokemmo.protocol.SearchPackets.response(result));
                } else if (command[0] == top.totodile.pokemmo.protocol.PartyPackets.OPCODE) {
                    if (world == null || activeCharacter == 0) throw new IOException("No active party");
                    int[] move = top.totodile.pokemmo.protocol.PartyPackets.decodeMove(command);
                    var party = characters.moveParty(accountId, activeCharacter, move[0], move[1]);
                    channel.send(top.totodile.pokemmo.protocol.PartyPackets.response(party != null,
                            party == null ? java.util.List.of() : party));
                } else if (command[0] == top.totodile.pokemmo.protocol.FollowerPackets.OPCODE) {
                    if (world == null || activeCharacter == 0) throw new IOException("No active world for follower");
                    int index = top.totodile.pokemmo.protocol.FollowerPackets.decodeRequest(command);
                    long followerCharacter = activeCharacter;
                    var party = characters.list(accountId).stream().filter(c -> c.id() == followerCharacter).findFirst().orElseThrow().party();
                    var member = index >= 0 && index < party.size() ? party.get(index) : null;
                    boolean followerAccepted = index == -1 || member != null && member.species() <= 493 && !member.shiny();
                    channel.send(top.totodile.pokemmo.protocol.FollowerPackets.response(followerAccepted, followerAccepted ? member : null));
                } else if (command[0] == 0x78) {
                    if (command.length != 1 || world == null) throw new IOException("No active world to logout");
                    world.close();
                    world = null;
                    activeCharacter = 0;
                    channel.send(CharacterPackets.leaveResponse(characters.list(accountId)));
                } else if (command[0] == TrainerStats.OPCODE) {
                    if (command.length != 1 || world == null) throw new IOException("No active trainer card");
                    channel.send(TrainerStats.response(world.stats()));
                } else if (command[0] == 0x76) {
                    boolean success = false;
                    boolean unlocked = command.length == 2 && command[1] == 1;
                    if (command.length == 2 && (command[1] == 0 || command[1] == 1)
                            && world != null && activeCharacter != 0 && characters != null
                            && Boolean.parseBoolean(System.getProperty("pokemmo.game.debug", "true"))) {
                        try { success = characters.setDexUnlocked(accountId, activeCharacter, unlocked); }
                        catch (java.sql.SQLException exception) { LOGGER.log(System.Logger.Level.ERROR, "Unable to update Pokedex", exception); }
                    }
                    channel.send(new byte[]{0x76, (byte) (success ? 1 : 0), (byte) (unlocked ? 1 : 0)});
                } else if ((command[0] & 255) == PokemonConfigPackets.OPCODE) {
                    var request = PokemonConfigPackets.decodeRequest(command);
                    var result = new PokemonConfigPackets.Result(
                            PokemonConfigPackets.Status.INVALID, null, java.util.List.of());
                    if (world != null && activeCharacter != 0 && characters != null) {
                        try {
                            result = characters.configurePokemon(accountId, activeCharacter, request,
                                    Boolean.parseBoolean(System.getProperty("pokemmo.game.debug", "true")));
                        } catch (java.sql.SQLException error) {
                            LOGGER.log(System.Logger.Level.ERROR, "Unable to configure Pokemon", error);
                            result = new PokemonConfigPackets.Result(
                                    PokemonConfigPackets.Status.SYSTEM_ERROR, null, java.util.List.of());
                        }
                    }
                    channel.send(PokemonConfigPackets.response(result));
                } else if ((command[0] & 255) == CharacterPackets.OUTFIT) {
                    var appearance = CharacterPackets.decodeOutfit(command);
                    var result = new CharacterPackets.CreationResult(CharacterPackets.Status.INVALID, java.util.List.of());
                    if (world != null && activeCharacter != 0 && characters != null
                            && Boolean.parseBoolean(System.getProperty("pokemmo.game.debug", "true"))) {
                        try { result = characters.replaceOutfit(accountId, activeCharacter, appearance); }
                        catch (java.sql.SQLException error) {
                            LOGGER.log(System.Logger.Level.ERROR, "Unable to replace outfit", error);
                            result = new CharacterPackets.CreationResult(CharacterPackets.Status.SYSTEM_ERROR, java.util.List.of());
                        }
                    }
                    channel.send(CharacterPackets.outfitResponse(result));
                } else if (command[0] == top.totodile.pokemmo.protocol.PcPackets.OPCODE) {
                    if (world == null || activeCharacter == 0 || characters == null) throw new IOException("No active PC character");
                    int action = top.totodile.pokemmo.protocol.PcPackets.decodeAction(command);
                    if (action == top.totodile.pokemmo.protocol.PcPackets.FETCH_PARTY) {
                        try {
                            var state = characters.pcParty(accountId, activeCharacter);
                            channel.send(top.totodile.pokemmo.protocol.PcPackets.partyResponse(
                                    top.totodile.pokemmo.protocol.PcPackets.Status.SUCCESS, state.boxes(), state.party()));
                        } catch (java.sql.SQLException exception) {
                            LOGGER.log(System.Logger.Level.ERROR, "Unable to load PC party", exception);
                            channel.send(top.totodile.pokemmo.protocol.PcPackets.partyResponse(
                                    top.totodile.pokemmo.protocol.PcPackets.Status.SYSTEM_ERROR, 1, java.util.List.of()));
                        }
                    } else if (action == top.totodile.pokemmo.protocol.PcPackets.FETCH_BOX) {
                        int[] request = top.totodile.pokemmo.protocol.PcPackets.decodeBoxRequest(command);
                        var status = top.totodile.pokemmo.protocol.PcPackets.Status.SUCCESS;
                        top.totodile.pokemmo.protocol.PcPackets.BoxPage page = null;
                        try {
                            page = characters.pcBox(accountId, activeCharacter, request[0], request[1]);
                        } catch (java.sql.SQLException exception) {
                            LOGGER.log(System.Logger.Level.ERROR, "Unable to load PC box", exception);
                            status = top.totodile.pokemmo.protocol.PcPackets.Status.SYSTEM_ERROR;
                        }
                        channel.send(page == null
                                ? top.totodile.pokemmo.protocol.PcPackets.boxResponse(status, request[0], request[1], false, java.util.List.of())
                                : top.totodile.pokemmo.protocol.PcPackets.boxResponse(status, page.box(), page.start(), page.more(), page.entries()));
                    } else if (action == top.totodile.pokemmo.protocol.PcPackets.MOVE) {
                        var move = top.totodile.pokemmo.protocol.PcPackets.decodeMove(command);
                        var status = top.totodile.pokemmo.protocol.PcPackets.Status.SYSTEM_ERROR;
                        try {
                            status = characters.pcMove(accountId, activeCharacter, move);
                        } catch (java.sql.SQLException exception) {
                            LOGGER.log(System.Logger.Level.ERROR, "Unable to move PC Pokemon", exception);
                        }
                        channel.send(top.totodile.pokemmo.protocol.PcPackets.statusResponse(
                                top.totodile.pokemmo.protocol.PcPackets.MOVE, status));
                    } else {
                        var status = top.totodile.pokemmo.protocol.PcPackets.Status.SYSTEM_ERROR;
                        try {
                            status = characters.pcCreateBox(accountId, activeCharacter);
                        } catch (java.sql.SQLException exception) {
                            LOGGER.log(System.Logger.Level.ERROR, "Unable to create PC box", exception);
                        }
                        channel.send(top.totodile.pokemmo.protocol.PcPackets.statusResponse(
                                top.totodile.pokemmo.protocol.PcPackets.CREATE_BOX, status));
                    }
                } else if (command[0] == WorldPackets.MOVE || command[0] == WorldPackets.LOADED
                        || command[0] == WorldPackets.SETTINGS || command[0] == WorldPackets.TELEPORT || (command[0] & 255) == EncounterPackets.OPCODE || (command[0] & 255) == NpcPackets.OPCODE || (command[0] & 255) == StoryPackets.OPCODE) {
                    if (world == null) throw new IOException("No active world");
                    channel.send(world.handle(command));
                } else if (command[0] == WorldPackets.SAVE) {
                    if (command.length != 1 || world == null)
                        throw new IOException("Position save rejected");
                    world.flush();
                    channel.send(new byte[]{WorldPackets.SAVE});
                } else throw new IOException("Unexpected game command");
            }
        } catch (IOException | GeneralSecurityException | java.sql.SQLException exception) {
            LOGGER.log(System.Logger.Level.DEBUG, "Game connection closed: {0}", exception.getClass().getSimpleName());
        } finally {
            if (world != null) try { world.close(); } catch (java.sql.SQLException e) {
                LOGGER.log(System.Logger.Level.ERROR, "Unable to save world on disconnect", e);
            }
            if (accountId != null) accounts.remove(accountId);
            connections.remove(socket);
            connectionLimit.release();
        }
    }

    @Override
    public void close() throws IOException {
        listener.close();
        for (Socket socket : connections) {
            try {
                socket.close();
            } catch (IOException exception) {
                LOGGER.log(System.Logger.Level.DEBUG, "Unable to close game connection", exception);
            }
        }
        workers.shutdown();
        try {
            if (!workers.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) workers.shutdownNow();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt(); workers.shutdownNow();
        }
        saves.shutdown();
    }
}
