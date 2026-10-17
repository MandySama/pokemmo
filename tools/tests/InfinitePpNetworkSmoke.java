import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.game.GameServer;
import top.totodile.pokemmo.net.GameConnection;
import top.totodile.pokemmo.persistence.*;
import top.totodile.pokemmo.protocol.*;
import top.totodile.pokemmo.protocol.CharacterPackets.Appearance;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public final class InfinitePpNetworkSmoke {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static <T> T await(CompletableFuture<T> result) throws Exception { return result.get(20, TimeUnit.SECONDS); }
    public static void main(String[] args) throws Exception {
        var db = new Database(); db.initialize();
        String suffix = Long.toHexString(System.nanoTime()), user = "pp_" + suffix;
        var accounts = new AccountStore(db); int account = 0;
        Path publicKey = Path.of("target/infinite-pp-test.public");
        try {
            accounts.create(user, "isolated-pp-test"); account = accounts.find(user).orElseThrow().id();
            var assets = new AvatarAssets(Path.of("data/sprites/addons.pak"));
            int[] items = new int[12]; Arrays.fill(items, -1); items[10] = 0;
            for (int c : new int[]{2,3,6,8,9,11}) items[c] = assets.choices(c, 0).getFirst().id;
            var store = new CharacterStore(db, new AvatarCatalog(Path.of("data/sprites/addons.pak")));
            long first = store.create(account, new Appearance("P" + suffix, 0, 2, 0, items, new int[12])).characters().getFirst().id();
            store.create(account, new Appearance("Q" + suffix, 0, 2, 0, items, new int[12]));
            long second = store.list(account).stream().filter(c -> c.id() != first).findFirst().orElseThrow().id();
            check(!store.infinitePp(account, first), "default off");
            check(!store.setInfinitePp(account + 1, first, true), "foreign ownership rejected");
            var keys = HandshakeKeys.generate(); Files.write(publicKey, keys.getPublic().getEncoded());
            System.setProperty("pokemmo.login.publicKey", publicKey.toString());
            try (var server = new GameServer(new InetSocketAddress("127.0.0.1", 0), 1, 100, keys.getPrivate(), keys.getPublic(), store)) {
                Thread.ofPlatform().daemon().start(() -> { try { server.serve(); } catch (Exception ignored) { } });
                for (int i = 0; i < 3; i++) {
                    try (var net = new GameConnection()) {
                        check(await(net.connect(new GameEndpoint(account, 1, "127.0.0.1", server.port(), GameTicket.issue(account, user, 1, keys.getPrivate())))) == GameConnection.Result.CONNECTED, "encrypted admission");
                        await(net.enterCharacter(i == 1 ? second : first));
                        var initial = await(net.infinitePpCommand(InfinitePpPackets.QUERY, false));
                        check(initial.accepted() && initial.enabled() == (i == 2), "character isolation and reconnect persistence");
                        if (i == 0) check(await(net.infinitePpCommand(InfinitePpPackets.SET, true)).enabled(), "TCP enable");
                        if (i == 2) {
                            System.setProperty("pokemmo.game.debug", "false");
                            check(!await(net.infinitePpCommand(InfinitePpPackets.SET, true)).accepted(), "debug-disabled enable rejected");
                            System.setProperty("pokemmo.game.debug", "true");
                            var disabled = await(net.infinitePpCommand(InfinitePpPackets.SET, false));
                            check(disabled.accepted() && !disabled.enabled(), "TCP disable");
                        }
                    }
                    long end = System.nanoTime() + 3_000_000_000L;
                    while (server.onlineCount() != 0 && System.nanoTime() < end) Thread.sleep(10);
                }
            }
            check(!store.infinitePp(account, first), "disable stored");
            System.out.println("PASS real PostgreSQL/encrypted TCP: default, ownership, enable/disable, character isolation, reconnect, debug gate");
        } finally {
            try (var connection = db.connect()) {
                try (var q = connection.prepareStatement("DELETE FROM characters WHERE account_id=?")) { q.setInt(1, account); q.executeUpdate(); }
                try (var q = connection.prepareStatement("DELETE FROM accounts WHERE username=?")) { q.setString(1, user); q.executeUpdate(); }
            }
            Files.deleteIfExists(publicKey); db.close();
        }
    }
}
