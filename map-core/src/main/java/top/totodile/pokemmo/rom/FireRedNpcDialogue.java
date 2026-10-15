package top.totodile.pokemmo.rom;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 有界读取交互脚本的文本分支；不执行战斗、奖励、商店或剧情写操作。 */
final class FireRedNpcDialogue {
    private static final int[] LENGTHS = lengths();
    private final byte[] rom;

    FireRedNpcDialogue(byte[] rom) {
        this.rom = rom;
    }

    List<Integer> textIds(int script, int gender) throws IOException {
        return dialogue(script, gender).stream().map(FireRedDialogue::textId).toList();
    }

    List<FireRedDialogue> dialogue(int script, int gender) throws IOException {
        List<FireRedDialogue> texts = new ArrayList<>();
        Map<Integer, FireRedDialogue.Buffer> buffers = new HashMap<>();
        Deque<Integer> calls = new ArrayDeque<>();
        Map<Integer, Integer> variables = new HashMap<>();
        int pc = script, comparison = 0, text = -1;
        Set<String> visited = new HashSet<>();
        for (int budget = 0; budget < 2048 && pc > 0 && pc < rom.length; budget++) {
            if (!visited.add(pc + ":" + calls.size() + ":" + comparison)) break;
            int op = u8(pc), length = switch (op) {
                case 0x7d, 0x7f, 0x80, 0x81, 0x82, 0x83, 0x84 -> 4;
                case 0x7e -> 2;
                case 0x85 -> 6;
                default -> LENGTHS[op];
            };
            if (op == 0x5c && pc + 10 <= rom.length) {
                int intro = pointer(pc + 6);
                if (intro > 0 && u8(pc + 1) != 3) texts.add(new FireRedDialogue(intro, buffers));
                return List.copyOf(texts);
            }
            if (length == 0 || pc + length > rom.length) break;
            int next = pc + length;
            switch (op) {
                case 0x02, 0x0d:
                    return List.copyOf(texts);
                case 0x03, 0x0c:
                    if (calls.isEmpty()) return List.copyOf(texts);
                    next = calls.pop();
                    break;
                case 0x04:
                    calls.push(next);
                    next = pointer(pc + 1);
                    break;
                case 0x05:
                    next = pointer(pc + 1);
                    break;
                case 0x06, 0x07:
                    if (condition(u8(pc + 1), comparison)) {
                        if (op == 7) calls.push(next);
                        next = pointer(pc + 2);
                    }
                    break;
                case 0x0f:
                    if (u8(pc + 1) == 0) text = pointer(pc + 2);
                    break;
                case 0x08, 0x09:
                    int standard = u8(pc + 1);
                    if (standard >= 2 && standard <= 6 && text > 0) {
                        texts.add(new FireRedDialogue(text, buffers));
                        // 确认框和有选择的脚本只展示当前文本，不猜测玩家的后续选择。
                        if (standard == 5) return List.copyOf(texts);
                    } else return List.copyOf(texts);
                    if (op == 8) return List.copyOf(texts);
                    break;
                case 0x16:
                    variables.put(u16(pc + 1), u16(pc + 3));
                    break;
                case 0x17, 0x18:
                    int key = u16(pc + 1);
                    variables.put(
                            key,
                            variables.getOrDefault(key, 0)
                                    + (op == 0x17 ? 1 : -1) * value(u16(pc + 3), variables));
                    break;
                case 0x19, 0x1a:
                    variables.put(u16(pc + 1), value(u16(pc + 3), variables));
                    break;
                case 0x21, 0x22:
                    comparison =
                            Integer.compare(
                                    value(u16(pc + 1), variables), value(u16(pc + 3), variables));
                    break;
                case 0x2b:
                    comparison = -1;
                    break;
                case 0x7d, 0x80, 0x82, 0x83, 0x84:
                    buffers.put(u8(pc + 1), new FireRedDialogue.Buffer(op, value(u16(pc + 2), variables)));
                    break;
                case 0x7e, 0x7f:
                    buffers.put(u8(pc + 1), new FireRedDialogue.Buffer(op, op == 0x7e ? 0 : value(u16(pc + 2), variables)));
                    break;
                case 0x85:
                    buffers.put(u8(pc + 1), new FireRedDialogue.Buffer(op, pointer(pc + 2)));
                    break;
                case 0x25, 0x26:
                    if (u16(pc + (op == 0x25 ? 1 : 3)) == 252) {
                        int[][] trades = {{63,122},{61,124},{32,29},{21,83},{33,30},{55,108},{26,101},{48,114},{77,86}};
                        int trade = variables.getOrDefault(0x8004, 0);
                        if (trade >= 0 && trade < trades.length) {
                            buffers.put(0, new FireRedDialogue.Buffer(0x7d, trades[trade][0]));
                            buffers.put(1, new FireRedDialogue.Buffer(0x7d, trades[trade][1]));
                            if (op == 0x26) variables.put(u16(pc + 1), trades[trade][0]);
                        }
                        break;
                    }
                    // 此轮只读文本，不调用卡带 special；默认结果避免执行剧情效果。
                    if (op == 0x26) variables.put(u16(pc + 1), 0);
                    break;
                case 0xa0:
                    variables.put(0x800d, gender);
                    break;
                case 0x67:
                    int address = pointer(pc + 1);
                    if (address > 0) texts.add(new FireRedDialogue(address, buffers));
                    break;
                // 仅跳过音效、等待、锁定、朝向、文本展示和文本颜色等展示指令。
                case 0x00,
                        0x01,
                        0x28,
                        0x30,
                        0x31,
                        0x32,
                        0x33,
                        0x34,
                        0x35,
                        0x36,
                        0x37,
                        0x38,
                        0x39,
                        0x3a,
                        0x3b,
                        0x3c,
                        0x3d,
                        0x3e,
                        0x3f,
                        0x40,
                        0x41,
                        0x50,
                        0x51,
                        0x5a,
                        0x66,
                        0x68,
                        0x69,
                        0x6a,
                        0x6b,
                        0x6c,
                        0x6d,
                        0x81,
                        0xa1,
                        0x9d,
                        0x9e,
                        0xc7:
                    break;
                default:
                    return List.copyOf(texts);
            }
            pc = next;
            if (texts.size() >= 16 || calls.size() > 32) break;
        }
        return List.copyOf(texts);
    }

    private int value(int operand, Map<Integer, Integer> variables) {
        return operand >= 0x4000 ? variables.getOrDefault(operand, 0) : operand;
    }

    private boolean condition(int type, int comparison) {
        return switch (type) {
            case 0 -> comparison < 0;
            case 1 -> comparison == 0;
            case 2 -> comparison > 0;
            case 3 -> comparison <= 0;
            case 4 -> comparison >= 0;
            case 5 -> comparison != 0;
            default -> false;
        };
    }

    private int u8(int offset) throws IOException {
        if (offset < 0 || offset >= rom.length) throw new IOException("NPC script outside ROM");
        return rom[offset] & 255;
    }

    private int u16(int offset) throws IOException {
        return u8(offset) | u8(offset + 1) << 8;
    }

    private int pointer(int offset) throws IOException {
        long address = (long) u16(offset) | (long) u16(offset + 2) << 16;
        return address >= 0x08000000L && address < 0x08000000L + rom.length
                ? (int) (address - 0x08000000L)
                : 0;
    }

    private static int[] lengths() {
        try (var stream = FireRedNpcDialogue.class.getResourceAsStream("/gba-script-lengths.csv")) {
            if (stream == null) throw new IOException("Missing GBA script schema");
            return Arrays.stream(
                            new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                                    .trim()
                                    .split(","))
                    .mapToInt(Integer::parseInt)
                    .toArray();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
