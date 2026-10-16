package top.totodile.pokemmo.ui;

import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.rom.FireRedDialogue;
import top.totodile.pokemmo.protocol.PartyMember;
import java.util.List;
import java.util.Map;

/** 语言文本保留占位符，展示时使用对应脚本的缓冲快照与角色上下文。 */
final class KantoDialogueText {
    private KantoDialogueText() { }

    static String format(String text, String player) {
        String rival = ClientStrings.get(9560);
        return text.replace("{PLAYER}", player).replace("{01}", player).replace("%CHARACTER_NAME%", player)
                .replace("{06}", rival).replace("{RIVAL}", rival).replace("%RIVAL_NAME%", rival);
    }

    static String format(String text, String player, Map<Integer, FireRedDialogue.Buffer> buffers, List<PartyMember> party) {
        String result = format(text, player);
        for (var entry : buffers.entrySet()) {
            int slot = entry.getKey();
            var buffer = entry.getValue();
            String value = buffer(buffer, party);
            if (value != null) {
                result = result.replace(String.format("{%02X}", slot + 2), value)
                        .replace("{STR_VAR_" + (slot + 1) + "}", value);
            }
        }
        return result;
    }

    static String format(String text, String player, Map<Integer, FireRedDialogue.Buffer> buffers,
                         List<PartyMember> party, int region, int gender) {
        if (region == 1) {
            String rival = ClientStrings.get(gender == 0 ? 195513 : 195512);
            text = text.replace("{06}", rival).replace("{RIVAL}", rival).replace("%RIVAL_NAME%", rival);
        }
        return format(text, player, buffers, party);
    }

    private static String buffer(FireRedDialogue.Buffer buffer, List<PartyMember> party) {
        int value = buffer.value();
        try {
            return switch (buffer.command()) {
                case 0x7d -> value > 0 ? ClientStrings.get(150000 + value) : null;
                case 0x7e, 0x7f -> {
                    if (value < 0 || value >= party.size()) yield ClientStrings.get(9561);
                    var member = party.get(value);
                    yield buffer.command() == 0x7f && member.individual() != null && !member.individual().nickname().isBlank()
                            ? member.individual().nickname() : ClientStrings.get(150000 + member.species());
                }
                case 0x80 -> ClientStrings.get(value == 182 ? 245216 : value == 261 ? 245471 : 240000 + value);
                case 0x82 -> ClientStrings.get(110000 + value);
                case 0x83 -> Integer.toString(value);
                case 0x84 -> value >= 15 && value <= 22 ? ClientStrings.get(9570 + value - 15) : null;
                case 0x85 -> ClientStrings.get(value);
                default -> null;
            };
        } catch (IllegalArgumentException missing) { return null; }
    }
}
