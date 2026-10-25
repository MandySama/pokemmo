package top.totodile.pokemmo.client.ui.world;

import top.totodile.pokemmo.client.resources.*;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.gba.FireRedMaps.Id;
import top.totodile.pokemmo.client.rom.nds.bw.BwMaps;
import top.totodile.pokemmo.client.world.*;
import top.totodile.pokemmo.server.game.GameService.Position;

import java.io.*;
import java.util.*;

/**
 * Ported literal destinations from reference TeleportMenuWindow.sL; no extracted or curated
 * catalogue.
 */
final class ReferenceTeleportEntries {
    record Entry(
            String name, int region, int bank, int map, int x, int y, int height, boolean noGrid) {
        boolean heading() {
            return region < 0;
        }
    }

    private final ClientResources resources;
    private final ClientStrings strings;
    private final Map<Integer, WorldMaps> cached =
            new LinkedHashMap<>(4, .75f, true) {
                protected boolean removeEldestEntry(Map.Entry<Integer, WorldMaps> e) {
                    return size() > 2;
                }
            };

    ReferenceTeleportEntries(ClientResources resources) {
        this.resources = resources;
        strings = resources.strings();
    }

    String regionName(int region) {
        return strings.get(250000 + region);
    }

    boolean available(int region) {
        return region != 10 && resources.roms().get(RomType.of(region)) != null;
    }

    private WorldMaps maps(int region) {
        return cached.computeIfAbsent(
                region,
                id -> {
                    try {
                        return WorldMapLoader.load(resources.roms().get(RomType.of(id)));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    synchronized List<Entry> load(int region) {
        return entries(region).stream().filter(Objects::nonNull).toList();
    }

    synchronized Position resolve(Entry entry, int direction) {
        // MoveToNdsCommand rejects the Gen5 no-grid targets; do not turn free coordinates into tile
        // (0,0).
        if (entry.noGrid())
            throw new IllegalArgumentException("moveto2 only supports Sinnoh region 3.");
        var id = new Id(entry.region() == 1 ? entry.bank() - 50 : entry.bank(), entry.map());
        if (entry.region() < 2)
            return new Position(id.bank(), id.map(), entry.x(), entry.y(), direction);
        return maps(entry.region()).teleportPosition(id, entry.x(), entry.y(), direction);
    }

    private Entry heading(String title) {
        return new Entry(title, -1, 0, 0, 0, 0, 0, false);
    }

    private Entry gba(int region, int bank, int map, int x, int y, int name) {
        return new Entry(strings.get(name), region, bank, map, x, y, 0, false);
    }

    private Entry nds(int region, int header, int x, int y, int height, boolean noGrid) {
        if (!available(region)) return null;
        var maps = maps(region);
        var id = new Id(header >>> 8, header & 255);
        if (!maps.ids().contains(id)) return null;
        String name = strings.getOrDefault(maps.locationStringId(id), maps.locationName(id));
        return new Entry(
                name == null ? Integer.toString(header) : name,
                region,
                id.bank(),
                id.map(),
                x,
                y,
                height,
                noGrid);
    }

    private Entry contest() {
        return new Entry("华丽大赛会场", 1, 63, 4, 14, 6, 0, false);
    }

    private Entry arena() {
        return new Entry("锦标赛竞技场", 10, 0, 1, 28, 64, 0, false);
    }

    private List<Entry> entries(int region) {
        if (!available(region)) {
            return Collections.emptyList();
        }
        if (region == 0) {
            List<Entry> entries = new ArrayList<>();
            String title = new StringBuilder().append(regionName(0)).append(" 第一部分").toString();
            Entry label;
            label = heading(title);
            entries.add(label);
            entries.add(gba((byte) 0, (byte) 3, (byte) 0, (short) 11, (short) 9, 140000));
            entries.add(gba((byte) 0, (byte) 3, (byte) 1, (short) 26, (short) 27, 140001));
            entries.add(gba((byte) 0, (byte) 3, (byte) 2, (short) 17, (short) 26, 140002));
            entries.add(gba((byte) 0, (byte) 3, (byte) 3, (short) 22, (short) 20, 140003));
            entries.add(gba((byte) 0, (byte) 3, (byte) 28, (short) 7, (short) 42, 140022));
            entries.add(gba((byte) 0, (byte) 3, (byte) 4, (short) 6, (short) 6, 140004));
            entries.add(gba((byte) 0, (byte) 3, (byte) 5, (short) 15, (short) 7, 140005));
            entries.add(gba((byte) 0, (byte) 3, (byte) 6, (short) 48, (short) 12, 140006));
            entries.add(gba((byte) 0, (byte) 3, (byte) 7, (short) 25, (short) 32, 140007));
            entries.add(gba((byte) 0, (byte) 3, (byte) 10, (short) 33, (short) 31, 140010));
            entries.add(gba((byte) 0, (byte) 3, (byte) 8, (short) 14, (short) 12, 140008));
            entries.add(gba((byte) 0, (byte) 3, (byte) 9, (short) 11, (short) 7, 140009));
            title = new StringBuilder().append(regionName(0)).append(" 第二部分").toString();

            label = heading(title);
            entries.add(label);
            entries.add(gba((byte) 0, (byte) 3, (byte) 12, (short) 14, (short) 6, 140055));
            entries.add(gba((byte) 0, (byte) 3, (byte) 13, (short) 21, (short) 8, 140056));
            entries.add(gba((byte) 0, (byte) 3, (byte) 14, (short) 14, (short) 28, 140057));
            entries.add(gba((byte) 0, (byte) 3, (byte) 15, (short) 18, (short) 21, 140058));
            entries.add(gba((byte) 0, (byte) 3, (byte) 16, (short) 18, (short) 7, 140059));
            entries.add(gba((byte) 0, (byte) 3, (byte) 18, (short) 11, (short) 12, 140061));
            entries.add(gba((byte) 0, (byte) 3, (byte) 17, (short) 12, (short) 4, 140060));
            entries.add(gba((byte) 0, (byte) 2, (byte) 0, (short) 9, (short) 9, 140086));
            entries.add(gba((byte) 0, (byte) 2, (byte) 56, (short) 15, (short) 13, 140099));
            entries.add(gba((byte) 0, (byte) 2, (byte) 10, (short) 9, (short) 11, 140091));
            return entries;
        }
        if (region == 1) {
            List<Entry> entries = new ArrayList<>();
            String title = regionName(1);
            Entry label;
            label = heading(title);
            entries.add(label);
            entries.add(gba((byte) 1, (byte) 50, (byte) 9, (short) 9, (short) 10, 141000));
            entries.add(gba((byte) 1, (byte) 50, (byte) 0, (short) 20, (short) 17, 141007));
            entries.add(gba((byte) 1, (byte) 50, (byte) 1, (short) 19, (short) 20, 141008));
            entries.add(gba((byte) 1, (byte) 50, (byte) 2, (short) 22, (short) 6, 141009));
            entries.add(gba((byte) 1, (byte) 50, (byte) 3, (short) 16, (short) 39, 141010));
            entries.add(gba((byte) 1, (byte) 50, (byte) 4, (short) 5, (short) 7, 141011));
            entries.add(gba((byte) 1, (byte) 50, (byte) 5, (short) 24, (short) 15, 141012));
            entries.add(gba((byte) 1, (byte) 50, (byte) 6, (short) 28, (short) 17, 141013));
            entries.add(gba((byte) 1, (byte) 50, (byte) 7, (short) 43, (short) 32, 141014));
            entries.add(gba((byte) 1, (byte) 50, (byte) 8, (short) 18, (short) 6, 141015));
            entries.add(gba((byte) 1, (byte) 50, (byte) 10, (short) 6, (short) 17, 141001));
            entries.add(gba((byte) 1, (byte) 50, (byte) 11, (short) 2, (short) 11, 141002));
            entries.add(gba((byte) 1, (byte) 50, (byte) 12, (short) 9, (short) 7, 141003));
            entries.add(gba((byte) 1, (byte) 50, (byte) 13, (short) 14, (short) 8, 141004));
            entries.add(gba((byte) 1, (byte) 50, (byte) 14, (short) 16, (short) 4, 141005));
            entries.add(gba((byte) 1, (byte) 50, (byte) 15, (short) 8, (short) 16, 141006));
            entries.add(gba((byte) 1, (byte) 74, (byte) 78, (short) 14, (short) 6, 141085));

            label = heading("副本");
            entries.add(label);
            entries.add(gba((byte) 1, (byte) 76, (byte) 4, (short) 26, (short) 43, 141058));
            entries.add(gba((byte) 1, (byte) 76, (byte) 60, (short) 9, (short) 13, 141212));
            entries.add(contest());

            label = heading("岛屿");
            entries.add(label);
            entries.add(gba((byte) 1, (byte) 76, (byte) 56, (short) 13, (short) 38, 141201));
            entries.add(gba((byte) 1, (byte) 76, (byte) 9, (short) 13, (short) 22, 141073));
            entries.add(gba((byte) 1, (byte) 76, (byte) 58, (short) 15, (short) 12, 141200));
            entries.add(gba((byte) 1, (byte) 76, (byte) 66, (short) 10, (short) 11, 141211));

            label = heading("树果");
            entries.add(label);
            entries.add(gba((byte) 1, (byte) 50, (byte) 19, (short) 8, (short) 22, 141019));
            entries.add(gba((byte) 1, (byte) 50, (byte) 38, (short) 14, (short) 7, 141038));
            return entries;
        }
        if (region == 2) {
            var entries = new ArrayList<Entry>();
            entries.add(heading(regionName(2)));
            boolean noGrid = false;
            for (var point : ((BwMaps) maps(2)).teleportPoints()) {
                if (point.noGrid() && !noGrid) {
                    entries.add(heading(regionName(2) + " 非网格地图"));
                    noGrid = true;
                }
                entries.add(
                        nds(
                                2,
                                point.header(),
                                point.x(),
                                point.y(),
                                point.header() == 28 ? 2 : 0,
                                point.noGrid()));
            }
            return entries;
        }
        if (region == 3) {
            List<Entry> entries = new ArrayList<>();
            String title = regionName(3);
            Entry label;
            label = heading(title);
            entries.add(label);
            entries.add(nds((byte) 3, (short) 411, (short) 116, (short) 886, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 418, (short) 177, (short) 843, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 426, (short) 176, (short) 667, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 433, (short) 566, (short) 657, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 442, (short) 472, (short) 539, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 3, (short) 180, (short) 777, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 33, (short) 58, (short) 723, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 45, (short) 303, (short) 757, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 65, (short) 305, (short) 531, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 86, (short) 465, (short) 698, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 120, (short) 600, (short) 816, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 132, (short) 717, (short) 612, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 150, (short) 860, (short) 785, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 165, (short) 379, (short) 234, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 244, (short) 15, (short) 78, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 172, (short) 847, (short) 560, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 573, (short) 55, (short) 40, (byte) 0, false));
            title = strings.get(143112);

            label = heading(title);
            entries.add(label);
            entries.add(nds((byte) 3, (short) 188, (short) 647, (short) 430, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 450, (short) 659, (short) 339, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 457, (short) 802, (short) 473, (byte) 0, false));
            entries.add(nds((byte) 3, (short) 392, (short) 306, (short) 910, (byte) 0, false));
            return entries;
        }
        if (region == 4) {
            List<Entry> entries = new ArrayList<>();
            String title = regionName(4);
            Entry label;
            label = heading(title);
            entries.add(label);
            entries.add(nds((byte) 4, (short) 60, (short) 695, (short) 397, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 67, (short) 564, (short) 392, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 73, (short) 497, (short) 272, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 74, (short) 410, (short) 461, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 75, (short) 187, (short) 370, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 76, (short) 352, (short) 369, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 77, (short) 272, (short) 258, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 78, (short) 397, (short) 184, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 87, (short) 534, (short) 184, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 88, (short) 536, (short) 90, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 89, (short) 674, (short) 177, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 90, (short) 820, (short) 266, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 174, (short) 82, (short) 303, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 411, (short) 8, (short) 15, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 280, (short) 42, (short) 23, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 30, (short) 909, (short) 297, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 36, (short) 468, (short) 419, (byte) 0, false));
            title = new StringBuilder().append(regionName(4)).append("2").toString();

            label = heading(title);
            entries.add(label);
            entries.add(nds((byte) 4, (short) 49, (short) 1033, (short) 364, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 50, (short) 1032, (short) 263, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 51, (short) 1048, (short) 107, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 52, (short) 1309, (short) 132, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 53, (short) 1418, (short) 235, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 54, (short) 1297, (short) 295, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 55, (short) 1231, (short) 238, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 56, (short) 1209, (short) 440, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 57, (short) 1039, (short) 503, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 58, (short) 912, (short) 201, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 59, (short) 1294, (short) 243, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 11, (short) 1167, (short) 107, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 18, (short) 1426, (short) 164, (byte) 0, false));

            label = heading("副本");
            entries.add(label);
            entries.add(nds((byte) 4, (short) 340, (short) 15, (short) 19, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 465, (short) 30, (short) 16, (byte) 0, false));
            entries.add(nds((byte) 4, (short) 102, (short) 25, (short) 3, (byte) 0, false));
            return entries;
        }
        if (region == 10) {
            List<Entry> entries = new ArrayList<>();
            if (available((byte) 0)) {
                String title = regionName(0);
                Entry label;
                label = heading(title);
                entries.add(label);
                entries.add(gba((byte) 0, (byte) 3, (byte) 23, (short) 24, (short) 2, 140017));
                entries.add(gba((byte) 0, (byte) 1, (byte) 63, (short) 36, (short) 17, 140048));
                entries.add(gba((byte) 0, (byte) 3, (byte) 45, (short) 14, (short) 112, 140062));
            }
            if (available((byte) 4)) {
                String title = regionName(4);
                Entry label;
                label = heading(title);
                entries.add(label);
                entries.add(nds((byte) 4, (short) 151, (short) 131, (short) 369, (byte) 0, false));
            }
            if (available((byte) 1)) {
                String title = regionName(1);
                Entry label;
                label = heading(title);
                entries.add(label);
                entries.add(gba((byte) 1, (byte) 74, (byte) 11, (short) 11, (short) 35, 141059));
                entries.add(gba((byte) 1, (byte) 74, (byte) 98, (short) 16, (short) 9, 141209));
                entries.add(gba((byte) 1, (byte) 76, (byte) 14, (short) 38, (short) 57, 141058));
            }
            if (available((byte) 3)) {
                String title = regionName(3);
                Entry label;
                label = heading(title);
                entries.add(label);
                entries.add(nds((byte) 3, (short) 356, (short) 563, (short) 688, (byte) 0, false));
            }
            String title = regionName(2);
            Entry label;
            label = heading(title);
            entries.add(label);
            entries.add(nds((byte) 2, (short) 240, (short) 773, (short) 302, (byte) 0, false));
            entries.add(nds((byte) 2, (short) 368, (short) 624, (short) 185, (byte) 0, false));
            entries.add(nds((byte) 2, (short) 206, (short) 22, (short) 44, (byte) 0, false));
            entries.add(nds((byte) 2, (short) 232, (short) 33, (short) 25, (byte) 0, false));
            entries.add(nds((byte) 2, (short) 96, (short) 202, (short) 412, (byte) 0, false));
            entries.add(nds((byte) 2, (short) 215, (short) 10, (short) 38, (byte) 0, false));
            entries.add(nds((byte) 2, (short) 182, (short) 16, (short) 12, (byte) 0, false));
            entries.add(nds((byte) 2, (short) 354, (short) 10, (short) 23, (byte) 0, false));

            label = heading("自定义地图");
            entries.add(label);
            entries.add(arena());
            return entries;
        }
        return Collections.emptyList();
    }
}
