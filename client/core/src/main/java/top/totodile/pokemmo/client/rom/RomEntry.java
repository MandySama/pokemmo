package top.totodile.pokemmo.client.rom;

/** NDS 文件在卡带中的字节范围。 */
public record RomEntry(long offset, int length) {}
