package top.totodile.pokemmo.client.rom;

import java.util.Arrays;
import java.util.List;

/** 支持的游戏类型及稳定的配置编号。 */
public enum RomType {
    FIRE_RED(0, "gba", "BPR"),
    EMERALD(1, "gba", "BPE"),
    BLACK_WHITE(2, "nds", "IRB", "IRA"),
    PLATINUM(3, "nds", "CPU"),
    HEART_GOLD_SOUL_SILVER(4, "nds", "IPK", "IPG");
    public final int id;
    public final String extension;
    private final List<String> codes;

    RomType(int id, String extension, String... codes) {
        this.id = id;
        this.extension = extension;
        this.codes = List.of(codes);
    }

    public static RomType of(int id) {
        return Arrays.stream(values()).filter(t -> t.id == id).findFirst().orElseThrow();
    }

    boolean matches(String extension, String code) {
        return this.extension.equals(extension)
                && code.length() >= 3
                && codes.contains(code.substring(0, 3));
    }
}
