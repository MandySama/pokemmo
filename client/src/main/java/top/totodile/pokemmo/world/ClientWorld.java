package top.totodile.pokemmo.world;

import top.totodile.pokemmo.protocol.EncounterCatalog;
import top.totodile.pokemmo.protocol.CharacterPackets.Position;
import top.totodile.pokemmo.protocol.PartyMember;
import top.totodile.pokemmo.protocol.WorldPackets;
import top.totodile.pokemmo.rom.FireRedAtlas;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/** 游戏窗口与各地区世界的交互边界。 */
public interface ClientWorld extends AutoCloseable {
    default void setNpcAuthority(Function<byte[], CompletableFuture<top.totodile.pokemmo.protocol.NpcPackets.Snapshot>> authority) { }
    default boolean hasNpcInFront() { return false; }
    default CompletableFuture<java.util.List<Integer>> interactNpc() { return CompletableFuture.completedFuture(java.util.List.of()); }
    default void endNpcConversation() { }
    void replaceOutfit(top.totodile.pokemmo.protocol.CharacterPackets.Appearance appearance);
    default EncounterCatalog.Selection takeWildEncounter() { return null; }
    default int encounterBehavior() { return -1; }
    String locationName();
    int region();
    FireRedAtlas atlas();
    Position position();
    void restore(Position position) throws IOException;
    int speedMultiplier();
    void setSpeedMultiplier(int multiplier);
    boolean noclip();
    void setNoclip(boolean enabled);
    void setFollower(PartyMember member);
    boolean takeFollowerFailure();
    void setAuthority(Function<byte[], CompletableFuture<WorldPackets.Result>> authority);
    void beginEntry(long now);
    void key(int key, boolean pressed);
    void toggleBicycle();
    void restoreCycling(boolean cycling);
    void teleport(int group, int map, int x, int y, long now) throws IOException;
    void clearKeys();
    void render(int width, int height, long now) throws IOException;
    void renderEntryFade(int width, int height, long now);
    void close();
}
