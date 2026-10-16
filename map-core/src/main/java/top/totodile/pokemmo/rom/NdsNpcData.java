package top.totodile.pokemmo.rom;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/** 导出地图对象及普通对话入口；不执行剧情奖励或持久化条件。 */
public final class NdsNpcData {
    public record Npc(
            int header,
            int id,
            int graphics,
            int x,
            int y,
            int elevation,
            int facing,
            int movement,
            int rangeX,
            int rangeY,
            int hide,
            int text) {}

    private record Command(String name, int[] widths) {}

    private static final Map<Integer, Command> COMMANDS = commands();

    private NdsNpcData() {}

    public static List<Npc> read(Path rom, int region) throws IOException {
        NdsMaps maps =
                region == 2
                        ? new BlackWhiteMaps(rom)
                        : region == 3 ? new PlatinumMaps(rom) : new HeartGoldMaps(rom);
        NdsArchive events =
                new NdsArchive(
                        rom,
                        region == 2
                                ? "/a/1/2/5"
                                : region == 3
                                        ? "/fielddata/eventdata/zone_event.narc"
                                        : "/a/0/3/2");
        NdsArchive scripts =
                new NdsArchive(
                        rom,
                        region == 2
                                ? "/a/0/5/7"
                                : region == 3 ? "/fielddata/script/scr_seq.narc" : "/a/0/1/2");
        ByteBuffer headers;
        int start = 0, stride = region == 2 ? 48 : 24;
        if (region == 2) headers = new NdsArchive(rom, "/a/0/1/2").file(0);
        else {
            headers = NdsArchive.arm9(rom, region == 5);
            int first = region == 3 ? 1573448 : 33489410, second = region == 3 ? 1573449 : 11076050;
            start = -1;
            for (int i = 0; i <= headers.limit() - 8; i += 4)
                if (headers.getInt(i) == first && headers.getInt(i + 4) == second) {
                    start = i + (region == 3 ? 8 : 16);
                    break;
                }
            if (start < 0) throw new IOException("Missing NDS NPC headers");
        }
        List<Npc> result = new ArrayList<>();
        for (var header : maps.headers()) {
            int base = start + header.id() * stride;
            int scriptBank = NdsArchive.ushort(headers, base + (region == 3 ? 4 : 6));
            int textBank = NdsArchive.ushort(headers, base + (region == 3 ? 8 : 10));
            ByteBuffer data = events.file(header.events());
            int count, offset;
            if (region == 2) {
                if (data.getInt(0) < 1) continue;
                count = data.get(5) & 255;
                offset = 8 + (data.get(4) & 255) * 20;
            } else {
                offset = 4 + data.getInt(0) * 20;
                count = data.getInt(offset);
                offset += 4;
            }
            int size = region == 2 ? 36 : 32;
            if (count < 0 || count > 255 || (long) offset + count * size > data.limit())
                throw new IOException("Invalid NDS NPC events");
            for (int i = 0; i < count; i++, offset += size) {
                int id = NdsArchive.ushort(data, offset),
                        graphics = NdsArchive.ushort(data, offset + 2);
                if (id >= 255 || region == 2 && data.getInt(offset + 24) != 0) continue;
                int pos = offset + (region == 2 ? 28 : 24);
                int x = NdsArchive.ushort(data, pos), y = NdsArchive.ushort(data, pos + 2);
                int script = NdsArchive.ushort(data, offset + 10);
                int line = dialogue(scripts.file(scriptBank), region, script);
                int text = line < 0 || line > 4095 ? 0 : textId(region, textBank, line);
                result.add(
                        new Npc(
                                header.id(),
                                id,
                                graphics,
                                x,
                                y,
                                data.getInt(pos + 4),
                                facing(data.getShort(offset + 12)),
                                NdsArchive.ushort(data, offset + 4),
                                Math.max(0, data.getShort(offset + 20)),
                                Math.max(0, data.getShort(offset + 22)),
                                NdsArchive.ushort(data, offset + 8),
                                text));
            }
        }
        return List.copyOf(result);
    }

    public static int textId(int region, int bank, int line) {
        return 0x50000000 | region << 24 | bank << 12 | line;
    }

    private static int facing(int nativeDirection) {
        return switch (nativeDirection) {
            case 0 -> 1;
            case 1 -> 0;
            case 2 -> 2;
            case 3 -> 3;
            default -> 0;
        };
    }

    private static int dialogue(ByteBuffer code, int region, int script) {
        if (script <= 0 || script > 1024) return -1;
        int entry = (script - 1) * 4;
        if (entry + 4 > code.limit()) return -1;
        int pc = entry + 4 + code.getInt(entry), comparison = 0;
        Deque<Integer> calls = new ArrayDeque<>();
        Map<Integer, Integer> vars = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
        for (int budget = 0; budget < 256; budget++) {
            if (pc < 0 || pc + 2 > code.limit() || !visited.add(pc)) return -1;
            int op = NdsArchive.ushort(code, pc);
            pc += 2;
            Command command = COMMANDS.get(region << 16 | op);
            if (command == null) return -1;
            int[] args = new int[command.widths().length];
            for (int i = 0; i < args.length; i++) {
                int width = command.widths()[i];
                if (pc + width > code.limit()) return -1;
                args[i] =
                        width == 1
                                ? code.get(pc) & 255
                                : width == 2 ? NdsArchive.ushort(code, pc) : code.getInt(pc);
                pc += width;
            }
            String name = command.name().toLowerCase(Locale.ROOT);
            if (name.equals("message")
                    || name.equals("messageinstant")
                    || name.equals("messagenoskip")
                    || name.equals("npcmsg")
                    || name.equals("nonnpcmsg")) return region == 2 ? args[2] : args[0];
            if (region == 2 && name.equals("message2")) return args[2];
            if (name.contains("messagevar") || name.contains("msgvar")) return -1;
            if (name.equals("end") || name.equals("pauseend")) return -1;
            if (name.equals("return")) {
                if (calls.isEmpty()) return -1;
                pc = calls.pop();
            } else if (name.equals("goto") || name.equals("jump") || name.equals("call")) {
                if (name.equals("call")) calls.push(pc);
                pc += args[args.length - 1];
            } else if (name.equals("gotoif") || name.equals("callif")) {
                int condition = args[0];
                boolean take =
                        switch (condition) {
                            case 0 -> comparison < 0;
                            case 1 -> comparison == 0;
                            case 2 -> comparison > 0;
                            case 3 -> comparison <= 0;
                            case 4 -> comparison >= 0;
                            case 5 -> comparison != 0;
                            default -> false;
                        };
                if (take) {
                    if (name.equals("callif")) calls.push(pc);
                    pc += args[1];
                }
            } else if (name.equals("comparevartovalue"))
                comparison = Integer.compare(vars.getOrDefault(args[0], 0), args[1]);
            else if (name.equals("comparevartovar"))
                comparison =
                        Integer.compare(
                                vars.getOrDefault(args[0], 0), vars.getOrDefault(args[1], 0));
            else if (name.equals("setvar")) vars.put(args[0], args[1]);
            else if (name.contains("callstd")
                    || name.contains("commonscript")
                    || name.contains("trainerbattle")
                    || name.contains("yesno")) return -1;
        }
        return -1;
    }

    private static Map<Integer, Command> commands() {
        Map<Integer, Command> result = new HashMap<>();
        try (var input = NdsNpcData.class.getResourceAsStream("/nds/npc-script-commands.csv")) {
            if (input == null) throw new IOException("Missing NDS command widths");
            try (var reader =
                    new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    String[] f = line.split(",", -1);
                    int[] sizes =
                            f[3].isEmpty()
                                    ? new int[0]
                                    : Arrays.stream(f[3].split(":"))
                                            .mapToInt(Integer::parseInt)
                                            .toArray();
                    result.put(
                            Integer.parseInt(f[0]) << 16 | Integer.parseInt(f[1]),
                            new Command(f[2], sizes));
                }
            }
        } catch (IOException error) {
            throw new ExceptionInInitializerError(error);
        }
        return Map.copyOf(result);
    }
}
