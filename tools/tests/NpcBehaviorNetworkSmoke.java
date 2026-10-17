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

public final class NpcBehaviorNetworkSmoke {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static <T> T await(CompletableFuture<T> result) throws Exception { return result.get(20, TimeUnit.SECONDS); }
    public static void main(String[] args) throws Exception {
        var db = new Database(); db.initialize();
        String suffix = Long.toHexString(System.nanoTime()), user = "npc_" + suffix;
        var accounts = new AccountStore(db); int account = 0;
        Path publicKey = Path.of("target/npc-behavior-test.public");
        try {
            accounts.create(user, "isolated-pp-test"); account = accounts.find(user).orElseThrow().id();
            var assets = new AvatarAssets(Path.of("data/sprites/addons.pak"));
            int[] items = new int[12]; Arrays.fill(items, -1); items[10] = 0;
            for (int c : new int[]{2,3,6,8,9,11}) items[c] = assets.choices(c, 0).getFirst().id;
            var store = new CharacterStore(db, new AvatarCatalog(Path.of("data/sprites/addons.pak")));
            long first = store.create(account, new Appearance("P" + suffix, 0, 2, 0, items, new int[12])).characters().getFirst().id();
            store.create(account, new Appearance("Q" + suffix, 0, 2, 0, items, new int[12]));
            long second = store.list(account).stream().filter(c -> c.id() != first).findFirst().orElseThrow().id();
            check(store.savePosition(account, first, new top.totodile.pokemmo.protocol.CharacterPackets.Position(3,0,5,16,3,1)), "test position");
            var keys = HandshakeKeys.generate(); Files.write(publicKey, keys.getPublic().getEncoded());
            System.setProperty("pokemmo.login.publicKey", publicKey.toString());
            try (var server = new GameServer(new InetSocketAddress("127.0.0.1", 0), 1, 100, keys.getPrivate(), keys.getPublic(), store)) {
                Thread.ofPlatform().daemon().start(() -> { try { server.serve(); } catch (Exception ignored) { } });
                for (int i = 0; i < 3; i++) {
                    try (var net = new GameConnection()) {
                        check(await(net.connect(new GameEndpoint(account, 1, "127.0.0.1", server.port(), GameTicket.issue(account, user, 1, keys.getPrivate())))) == GameConnection.Result.CONNECTED, "encrypted admission");
                        await(net.enterCharacter(first));
                        await(net.worldCommand(new byte[]{WorldPackets.LOADED})); Thread.sleep(520);
                        await(net.storyCommand(StoryPackets.request(StoryPackets.POLL,0)));
                        var girl = await(net.npcCommand(NpcPackets.request(0,255))).states().stream().filter(n -> n.id()==1).findFirst().orElseThrow();
                        check(girl.x()==(i==1?4:5)&&girl.y()==15, "initialization/reconnect/rollback standing position");
                        if(i==0){
                            await(net.npcCommand(NpcPackets.request(1,1)));
                            long deadline=System.nanoTime()+12_000_000_000L;
                            while(true){
                                var story=await(net.storyCommand(StoryPackets.request(StoryPackets.POLL,0)));
                                if(!story.active())break;
                                if(story.mode()==GbaScriptRuntime.BUTTON)await(net.storyCommand(StoryPackets.request(StoryPackets.ADVANCE,story.phase())));
                                check(System.nanoTime()<deadline,"dialogue budget");Thread.sleep(30);
                            }
                            girl=await(net.npcCommand(NpcPackets.request(0,255))).states().stream().filter(n->n.id()==1).findFirst().orElseThrow();
                            check(girl.x()==4&&girl.y()==15,"native sidestep handoff");
                            Thread.sleep(1100);
                            girl=await(net.npcCommand(NpcPackets.request(0,255))).states().stream().filter(n->n.id()==1).findFirst().orElseThrow();
                            check(girl.x()==4&&girl.y()==15,"standing after handoff");
                        }
                        if(i==1){
                            var reset=await(net.storyProgress(StoryProgressPackets.RESET,StoryProgressPackets.OPENING,0));
                            check(reset.accepted(),"rollback accepted");
                            girl=await(net.npcCommand(NpcPackets.request(0,255))).states().stream().filter(n->n.id()==1).findFirst().orElseThrow();
                            check(girl.x()==5&&girl.y()==15,"rollback removes previous daily cache");
                        }

                    }
                    long end = System.nanoTime() + 3_000_000_000L;
                    while (server.onlineCount() != 0 && System.nanoTime() < end) Thread.sleep(10);
                }
            }
            System.out.println("PASS real PostgreSQL/encrypted TCP: native girl placement, sidestep, standing, saved behavior reconnect, rollback cache reset and second reconnect");
        } finally {
            try (var connection = db.connect()) {
                try (var q = connection.prepareStatement("DELETE FROM characters WHERE account_id=?")) { q.setInt(1, account); q.executeUpdate(); }
                try (var q = connection.prepareStatement("DELETE FROM accounts WHERE username=?")) { q.setString(1, user); q.executeUpdate(); }
            }
            Files.deleteIfExists(publicKey); db.close();
        }
    }
}
