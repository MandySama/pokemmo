package top.totodile.pokemmo.rom;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 坐标事件与地图脚本保留 ROM 偏移；开场读取只执行控制流，不执行卡带原生函数。 */
public final class FireRedStoryData {
    public record Coordinate(int x, int y, int elevation, int variable, int value, int script) { }
    public record MapScript(int type, int variable, int value, int script) { }
    public record Events(List<Coordinate> coordinates, List<MapScript> scripts) {
        public Events { coordinates = List.copyOf(coordinates); scripts = List.copyOf(scripts); }
    }
    public record Opening(int stopText, int warningText, int actor, List<Integer> approach) {
        public Opening { approach = List.copyOf(approach); }
    }

    private final byte[] rom;
    private FireRedStoryData(byte[] rom) { this.rom = rom; }

    static Events read(byte[] rom, int header) throws IOException {
        var reader = new FireRedStoryData(rom);
        List<Coordinate> coordinates = new ArrayList<>();
        List<MapScript> scripts = new ArrayList<>();
        int events = reader.ptr(header + 4);
        if (events > 0 && reader.u8(events + 2) > 0) {
            int entries = reader.requiredPtr(events + 12);
            for (int i = 0; i < reader.u8(events + 2); i++) {
                int at = entries + i * 16;
                // 天气坐标事件没有脚本指针，不属于剧情触发器。
                if (reader.u16(at + 12) == 0 && reader.u16(at + 14) == 0) continue;
                coordinates.add(new Coordinate(reader.u16(at), reader.u16(at + 2), reader.u8(at + 4),
                        reader.u16(at + 6), reader.u16(at + 8), reader.requiredPtr(at + 12)));
            }
        }
        int at = reader.ptr(header + 8);
        for (int budget = 0; at > 0 && budget < 32; budget++, at += 5) {
            int type = reader.u8(at);
            if (type == 0) break;
            int target = reader.requiredPtr(at + 1);
            if (type == 2 || type == 4) {
                for (int i = 0; i < 256; i++, target += 8) {
                    int variable = reader.u16(target);
                    if (variable == 0) break;
                    scripts.add(new MapScript(type, variable, reader.u16(target + 2), reader.requiredPtr(target + 4)));
                    if (i == 255) throw new IOException("Map script conditions exceed budget");
                }
            } else scripts.add(new MapScript(type, 0, 0, target));
            if (budget == 31) throw new IOException("Map scripts exceed budget");
        }
        return new Events(coordinates, scripts);
    }

    /** 解码叫停至警告的首段；未知命令立即失败，不猜测后续指令边界。 */
    static Opening opening(byte[] rom, int script) throws IOException {
        var r = new FireRedStoryData(rom);
        int[] lengths;
        try (var input = FireRedStoryData.class.getResourceAsStream("/gba-script-lengths.csv")) {
            if (input == null) throw new IOException("Missing script schema");
            lengths = Arrays.stream(new String(input.readAllBytes(), StandardCharsets.UTF_8).trim().split(","))
                    .mapToInt(Integer::parseInt).toArray();
        }
        lengths[0x55] = 3; // addobject 的本地事件编号为 u16。
        Deque<Integer> calls = new ArrayDeque<>();
        Map<Integer, Integer> variables = new HashMap<>();
        int pc = script, comparison = 0, stop = 0, actor = 0;
        List<Integer> approach = new ArrayList<>();
        for (int budget = 0; budget < 512; budget++) {
            int op = r.u8(pc), length = lengths[op];
            if (length == 0) throw new IOException("Unsupported opening opcode " + op);
            r.u8(pc + length - 1);
            int next = pc + length;
            switch (op) {
                case 0x16 -> variables.put(r.u16(pc + 1), r.u16(pc + 3));
                case 0x21 -> comparison = Integer.compare(variables.getOrDefault(r.u16(pc + 1), 0), r.u16(pc + 3));
                case 0x04 -> { calls.push(next); next = r.requiredPtr(pc + 1); }
                case 0x05 -> next = r.requiredPtr(pc + 1);
                case 0x06, 0x07 -> {
                    int condition = r.u8(pc + 1);
                    boolean take = switch (condition) {
                        case 0 -> comparison < 0; case 1 -> comparison == 0; case 2 -> comparison > 0;
                        case 3 -> comparison <= 0; case 4 -> comparison >= 0; case 5 -> comparison != 0;
                        default -> throw new IOException("Invalid script condition");
                    };
                    if (take) { if (op == 7) calls.push(next); next = r.requiredPtr(pc + 2); }
                }
                case 0x03 -> { if (calls.isEmpty()) throw new IOException("Opening returned early"); next = calls.pop(); }
                case 0x67 -> stop = r.requiredPtr(pc + 1);
                case 0x0f -> {
                    if (r.u8(pc + 1) == 0 && stop > 0 && actor > 0 && !approach.isEmpty())
                        return new Opening(stop, r.requiredPtr(pc + 2), actor, approach);
                }
                case 0x4f -> {
                    int id = r.u16(pc + 1);
                    if (id != 255) {
                        actor = id;
                        int movement = r.requiredPtr(pc + 3);
                        for (int i = 0; i < 256; i++) {
                            int command = r.u8(movement + i);
                            if (command == 0xfe) break;
                            if (command < 0x10 || command > 0x13) throw new IOException("Unsupported approach movement");
                            approach.add(command - 0x10);
                            if (i == 255) throw new IOException("Unterminated approach movement");
                        }
                    }
                }
                case 0x69, 0x25, 0xc7, 0x28, 0x33, 0x66, 0x68, 0x51, 0x2f, 0x55 -> { }
                default -> throw new IOException("Unsupported opening command " + op);
            }
            pc = next;
            if (calls.size() > 32) throw new IOException("Opening call stack exceeded");
        }
        throw new IOException("Opening instruction budget exceeded");
    }

    private int u8(int at) throws IOException {
        if (at < 0 || at >= rom.length) throw new IOException("Story address outside ROM");
        return rom[at] & 255;
    }
    private int u16(int at) throws IOException { return u8(at) | u8(at + 1) << 8; }
    private int ptr(int at) throws IOException {
        long address = (long) u16(at) | (long) u16(at + 2) << 16;
        return address >= 0x08000000L && address < 0x08000000L + rom.length ? (int) (address - 0x08000000L) : 0;
    }
    private int requiredPtr(int at) throws IOException {
        int value = ptr(at);
        if (value == 0) throw new IOException("Invalid story pointer");
        return value;
    }
}
