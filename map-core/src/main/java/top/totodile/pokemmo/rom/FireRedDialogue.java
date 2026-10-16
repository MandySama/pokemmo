package top.totodile.pokemmo.rom;
import java.util.Map;
/** 每条文本持有展示时的字符串缓冲快照。 */
public record FireRedDialogue(int textId, Map<Integer, Buffer> buffers, TrainerBattle battle) {
    public FireRedDialogue(int textId, Map<Integer,Buffer> buffers) { this(textId,buffers,null); }
    public record TrainerBattle(int id, int type, int defeatText, int afterText) { }
    public FireRedDialogue { buffers = Map.copyOf(buffers); }
    public record Buffer(int command, int value) { }
}
