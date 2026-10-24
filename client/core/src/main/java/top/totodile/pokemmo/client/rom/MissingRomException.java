package top.totodile.pokemmo.client.rom;

/** The destination map requires a ROM that has not been loaded (reference message 6805). */
public final class MissingRomException extends IllegalStateException {
    public MissingRomException(RomType type) {
        super(type+" ROM is not loaded");
    }
}
