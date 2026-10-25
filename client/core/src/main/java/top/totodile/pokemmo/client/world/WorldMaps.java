package top.totodile.pokemmo.client.world;

import top.totodile.pokemmo.client.rom.gba.FireRedMaps.*;

import java.util.List;
import java.util.Set;

/** ROM-backed map data shared by the GBA tile renderer and the NDS field renderer. */
public interface WorldMaps {
    Set<Id> ids();

    MapData map(Id id);

    Tiles tiles(MapData map);

    int locationStringId(Id id);

    /** Original ROM text, including deliberately empty names; null means unavailable. */
    default String locationName(Id id) {
        return null;
    }

    /**
     * Native moveto2 coordinates are absolute within an NDS matrix; saves use this map's origin.
     */
    default top.totodile.pokemmo.server.game.GameService.Position teleportPosition(
            Id id, int x, int y, int direction) {
        return new top.totodile.pokemmo.server.game.GameService.Position(
                id.bank(), id.map(), x, y, direction);
    }

    default top.totodile.pokemmo.server.game.GameService.Position start() {
        return top.totodile.pokemmo.server.game.GameService.Position.kantoStart();
    }

    /** Raw Nitro world coordinates: X right, Y up, Z south; one map tile is 16 units. */
    record ModelPlacement(
            byte[] model,
            byte[] textures,
            float x,
            float y,
            float z,
            float rotation,
            List<byte[]> animations) {
        public ModelPlacement(
                byte[] model, byte[] textures, float x, float y, float z, float rotation) {
            this(model, textures, x, y, z, rotation, List.of());
        }
    }

    default List<ModelPlacement> geometry(Id id) {
        return List.of();
    }

    default boolean includesNeighborGeometry() {
        return false;
    }

    /**
     * Matrix maps can have non-rectangular regions; resolve the physical tile before legacy edge
     * connections.
     */
    default top.totodile.pokemmo.server.game.GameService.Position transition(
            Id id, int x, int y, int direction) {
        return null;
    }

    default boolean is3d() {
        return false;
    }

    /** RomModelCacheBuilder 的两阶段遍历：地图地形，再遍历完整建筑资源库。 */
    default List<java.util.function.Supplier<List<ModelPlacement>>> modelCacheTasks() {
        return ids().stream()
                .<java.util.function.Supplier<List<ModelPlacement>>>map(id -> () -> geometry(id))
                .toList();
    }

    default top.totodile.pokemmo.client.rom.nds.model.RegionModelCache modelCache() {
        return null;
    }

    record CameraView(float fieldOfView, float distance, float pitch) {}

    /** Dppt/Hgss standard field camera: 15 degrees, 12.5 * 64 units, pitch 56 degrees. */
    default CameraView camera(Id id) {
        return new CameraView(15, 800, 56);
    }

    default CameraView camera(Id id, boolean mobile) {
        var view = camera(id);
        return mobile
                ? new CameraView(view.fieldOfView() * .7f, view.distance(), view.pitch())
                : view;
    }

    /** Fractional tile coordinates in; height in Nitro world units out. */
    default float height(Id id, float x, float y) {
        return 0;
    }

    default boolean isWater(int behavior) {
        return behavior >= 0x10 && behavior <= 0x15 || behavior == 0x1a || behavior == 0x1b;
    }

    default boolean isDoor(int behavior) {
        return behavior == 0x69;
    }

    default boolean blocked(int behavior, int dx, int dy) {
        return switch (behavior) {
            case 0x30 -> dx > 0;
            case 0x31 -> dx < 0;
            case 0x32 -> dy < 0;
            case 0x33 -> dy > 0;
            case 0x34 -> dx > 0 || dy < 0;
            case 0x35 -> dx < 0 || dy < 0;
            case 0x36 -> dx > 0 || dy > 0;
            case 0x37 -> dx < 0 || dy > 0;
            default -> false;
        };
    }
}
