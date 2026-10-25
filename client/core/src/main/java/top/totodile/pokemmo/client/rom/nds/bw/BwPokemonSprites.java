package top.totodile.pokemmo.client.rom.nds.bw;

import top.totodile.pokemmo.client.rom.RomReader;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;

import java.io.IOException;
import java.nio.*;
import java.util.*;

/**
 * BW icon and battle sprite NCGR/NCLR/NCER composition, following loadSprites / NitroOamData.oQ.
 */
public final class BwPokemonSprites {
    public record Pixels(int width, int height, int[] rgba) {}

    /**
     * PokemonSpriteManager.y60: one-pixel eight-neighbour outline, without changing the source
     * icon.
     */
    public static Pixels outlined(Pixels source, int color) {
        int width = source.width(), height = source.height();
        int[] original = source.rgba(), pixels = original.clone();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                if ((original[y * width + x] & 255) != 0) continue;
                boolean adjacent = false;
                for (int dy = -1; dy <= 1 && !adjacent; dy++)
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0
                                && nx < width
                                && ny >= 0
                                && ny < height
                                && (original[ny * width + nx] & 255) != 0) {
                            adjacent = true;
                            break;
                        }
                    }
                if (adjacent) pixels[y * width + x] = color;
            }
        return new Pixels(width, height, pixels);
    }

    private final BwPokedex dex;
    private final NdsArchive icons;
    private BwShinyIcons shinyIcons;

    public void loadShinyIcons(byte[] bytes) throws IOException {
        shinyIcons = new BwShinyIcons(bytes);
    }

    private NdsArchive battle;
    private final byte[] iconCells, iconColors, iconPaletteIds;
    private byte[] battleCells;
    private NdsArchive items, ribbons;
    private byte[] ribbonPalette;
    private NdsArchive balls;
    private byte[] itemCells;
    private final int[] itemTiles = new int[627], itemPalettes = new int[627];

    public BwPokemonSprites(BwPokedex dex) throws IOException {
        this.dex = dex;
        icons = dex.archive("a/0/0/7");
        iconCells = icons.read(2);
        iconColors = icons.read(0);
        byte[] arm = arm9(dex.rom());
        byte[] signature = {0, 17, 17, 17, 0, 0, 0, 0, 34, 34, 17, 17, 0, 17, 34, 34};
        int table = -1;
        for (int i = 0; i <= arm.length - 712; i++) {
            boolean match = true;
            for (int j = 0; j < signature.length; j++)
                if (arm[i + j] != signature[j]) {
                    match = false;
                    break;
                }
            if (match) {
                table = i;
                break;
            }
        }
        if (table < 0) throw new IOException("Missing BW icon palette table");
        iconPaletteIds = Arrays.copyOfRange(arm, table, table + 712);
        byte[] itemSignature =
                ByteBuffer.allocate(16)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putInt(19071103)
                        .putInt(16318992)
                        .putInt(555)
                        .putInt(65668073)
                        .array();
        for (int p = 0; p <= arm.length - 16 - 626 * 4; p++) {
            if (!Arrays.equals(arm, p, p + 16, itemSignature, 0, 16)) continue;
            for (int id = 1; id < 627; id++) {
                itemTiles[id] = u16(arm, p + 16 + (id - 1) * 4);
                itemPalettes[id] = u16(arm, p + 18 + (id - 1) * 4);
            }
            break;
        }
    }

    public static byte[] arm9(top.totodile.pokemmo.client.rom.RomInfo rom) throws IOException {
        byte[] h = RomReader.read(rom, 0, 0x40);
        return decompressArm(RomReader.read(rom, i32(h, 0x20), i32(h, 0x2c)));
    }

    public Pixels icon(int id) throws IOException {
        dex.get(id);
        return compose(
                icons.read(id * 2 + 7), iconColors, iconCells, 0, 36, iconPaletteIds[id] & 15);
    }

    /** BlackWhiteRom.loadSprites / PokemonSpriteManager.Ed, including the native egg icon 650. */
    public Pixels icon(int species, int form, int gender, boolean egg) throws IOException {
        return icon(species, form, gender, egg, false);
    }

    public Pixels icon(int species, int form, int gender, boolean egg, boolean shiny)
            throws IOException {
        dex.get(species);
        int index =
                egg
                        ? 650
                        : form == 0
                                ? species
                                : switch (species) {
                                    case 201 -> form + 651;
                                    case 351 -> form + 678;
                                    case 386 -> form + 681;
                                    case 412 -> form + 684;
                                    case 413 -> form + 686;
                                    case 421 -> 689;
                                    case 422 -> 690;
                                    case 423 -> 691;
                                    case 479 -> form + 691;
                                    case 487 -> 697;
                                    case 492 -> 698;
                                    case 550 -> 699;
                                    case 555 -> 700;
                                    case 585 -> form + 700;
                                    case 586 -> form + 703;
                                    case 648 -> 707;
                                    case 649 -> form + 707;
                                    default -> species;
                                };
        int female = gender == 1 ? 1 : 0;
        byte[] tiles = icons.read(index * 2 + 7 + female);
        if (tiles.length == 0 && female == 1) tiles = icons.read(index * 2 + 7);
        int palette = (iconPaletteIds[index] >> (female * 4)) & 15;
        if (!shiny || egg || shinyIcons == null)
            return compose(tiles, iconColors, iconCells, 0, 36, palette);
        int start = 24 + i32(iconColors, 36);
        int[] original = new int[16];
        for (int i = 0; i < 16; i++) {
            int color = u16(iconColors, start + (palette * 16 + i) * 2);
            original[i] =
                    ((color & 31) * 255 / 31) << 24
                            | ((color >> 5 & 31) * 255 / 31) << 16
                            | ((color >> 10 & 31) * 255 / 31) << 8
                            | 255;
        }
        original[0] = 0;
        var corrected = shinyIcons.palette(index, female, original);
        var result =
                compose(tiles, iconColors, iconCells, 0, 36, 36, 18, 18, palette, false, corrected);
        return shinyIcons.pixels(index, female, 0, result, original, corrected);
    }

    public Pixels front(int id) throws IOException {
        return front(id, 0, false);
    }

    /** BwBadgeModelLoader.es0: top-left 8x8 of archive a/0/8/3 NCGR 65, NCLR 66. */
    public Pixels heldItem() throws IOException {
        var archive = dex.archive("a/0/8/3");
        byte[] tiles = archive.read(65), palette = archive.read(66);
        if (i32(tiles, 0) != 0x4e434752 || i32(palette, 0) != 0x4e434c52)
            throw new IOException("Invalid held-item marker");
        int tileStart = 24 + i32(tiles, 44), paletteStart = 24 + i32(palette, 36);
        int[] pixels = new int[64];
        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 8; x++) {
                int index = (tiles[tileStart + y * 4 + x / 2] >> ((x & 1) * 4)) & 15;
                if (index == 0) continue;
                int c = u16(palette, paletteStart + index * 2);
                pixels[y * 8 + x] =
                        ((c & 31) * 255 / 31) << 24
                                | ((c >> 5 & 31) * 255 / 31) << 16
                                | ((c >> 10 & 31) * 255 / 31) << 8
                                | 255;
            }
        return new Pixels(8, 8, pixels);
    }

    public Pixels item(int id) throws IOException {
        return item(id, false);
    }

    public boolean hasItemIcon(int id) {
        int index = id - 5000;
        return index > 0 && index < 627 && itemTiles[index] != 0;
    }

    /** BwTrainerModelLoader.CR / MapWeatherSandstormParticleProvider: normal and gray variants. */
    public Pixels item(int id, boolean gray) throws IOException {
        int index = id - 5000;
        if (index < 1 || index >= 627 || itemTiles[index] == 0)
            throw new IOException("Missing BW item " + id);
        if (items == null) {
            items = dex.archive("a/0/2/5");
            itemCells = items.read(1);
        }
        Pixels full =
                compose(
                        items.read(itemTiles[index]),
                        items.read(itemPalettes[index]),
                        itemCells,
                        0,
                        32,
                        0);
        int[] cropped = new int[24 * 24];
        for (int y = 0; y < 24; y++) System.arraycopy(full.rgba(), y * 32, cropped, y * 24, 24);
        if (gray)
            for (int i = 0; i < cropped.length; i++) {
                int rgba = cropped[i],
                        average = ((rgba >>> 24) + (rgba >>> 16 & 255) + (rgba >>> 8 & 255)) / 3;
                cropped[i] = average << 24 | average << 16 | average << 8 | rgba & 255;
            }
        return new Pixels(24, 24, cropped);
    }

    public Pixels front(int id, int form, boolean shiny) throws IOException {
        dex.get(id);
        var extra = dex.extra(id);
        int sprite = form > 0 && id != 493 ? 652 + extra.formSprite() + form - 1 : id;
        if (form < 0 || form >= Math.max(1, extra.formCount()))
            throw new IOException("Invalid Pokémon form");
        if (battle == null) {
            battle = dex.archive("a/0/0/4");
            battleCells = battle.read(4);
        }
        return compose(
                battle.read(sprite * 20),
                battle.read(sprite * 20 + 18 + (shiny ? 1 : 0)),
                battleCells,
                0,
                96,
                -1);
    }

    /** BlackWhiteRom sets PokemonSpriteManager's egg battle sprite to index 650. */
    public Pixels egg() throws IOException {
        if (battle == null) {
            battle = dex.archive("a/0/0/4");
            battleCells = battle.read(4);
        }
        return compose(battle.read(650 * 20), battle.read(650 * 20 + 18), battleCells, 0, 96, -1);
    }

    /** BwBadgeModelLoader / H10: 32x32 ribbon tile at column+16 with palette row. */
    public Pixels ribbon(int row, int column) throws IOException {
        if (ribbons == null) {
            ribbons = dex.archive("a/0/7/8");
            ribbonPalette = ribbons.read(6);
        }
        byte[] tiles = ribbons.read(column + 16), palette = ribbonPalette;
        int offset = 24 + i32(tiles, 44), paletteOffset = 24 + i32(palette, 36) + row * 32;
        int[] pixels = new int[1024];
        for (int y = 0; y < 32; y++)
            for (int x = 0; x < 32; x++) {
                int pixel = ((y / 8) * 4 + x / 8) * 64 + (y % 8) * 8 + x % 8;
                int index = (tiles[offset + pixel / 2] >>> ((pixel & 1) * 4)) & 15;
                if (index == 0) continue;
                int c = u16(palette, paletteOffset + index * 2),
                        rgba =
                                ((c & 31) * 255 / 31) << 24
                                        | ((c >> 5 & 31) * 255 / 31) << 16
                                        | ((c >> 10 & 31) * 255 / 31) << 8
                                        | 255;
                pixels[y * 32 + x] = rgba == -2004313857 ? -1061097472 : rgba;
            }
        return new Pixels(32, 32, pixels);
    }

    /** BwBadgeModelLoader.W6 / E4: matching NCLR 87+n and NCGR 112+n. */
    public Pixels ball(int type) throws IOException {
        if (type < 0 || type > 24) throw new IOException("Invalid ball type " + type);
        if (balls == null) balls = dex.archive("a/0/8/3");
        byte[] palette = balls.read(type + 87), tiles = balls.read(type + 112);
        int start = 24 + i32(tiles, 44), colors = 24 + i32(palette, 36);
        int[] pixels = new int[256];
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 16; x++) {
                int i = ((y / 8) * 2 + x / 8) * 64 + (y % 8) * 8 + x % 8;
                int index = (tiles[start + i / 2] >>> ((i & 1) * 4)) & 15;
                if (index == 0) continue;
                int c = u16(palette, colors + index * 2);
                pixels[y * 16 + x] =
                        ((c & 31) * 255 / 31) << 24
                                | ((c >> 5 & 31) * 255 / 31) << 16
                                | ((c >> 10 & 31) * 255 / 31) << 8
                                | 255;
            }
        return new Pixels(16, 16, pixels);
    }

    /** NitroCharacterSpriteTable.vE0/RB0: APS texture used by the official type effects. */
    public Pixels particle(int id) throws IOException {
        return particle(id, 0);
    }

    /**
     * NdsCellAnimationSpriteLoader.LP(4, ball): throwing sequence sampled every three ROM ticks.
     */
    public java.util.List<Pixels> thrownBall(int type) throws IOException {
        var archive = dex.archive("a/0/9/5");
        var b = java.nio.ByteBuffer.wrap(archive.read(130)).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        int sequence = 48 + 4 * 16,
                count = b.getInt(sequence),
                frames = b.getInt(32) + 24 + b.getInt(sequence + 12),
                data = b.getInt(36) + 24,
                total = 0;
        for (int i = 0; i < count; i++)
            total += Short.toUnsignedInt(b.getShort(frames + i * 8 + 4));
        var result = new java.util.ArrayList<Pixels>();
        var decoded = new java.util.HashMap<Integer, Pixels>();
        for (int tick = 0; tick < total; tick += 3) {
            int remaining = tick, index = 0;
            while (index < count - 1
                    && remaining >= Short.toUnsignedInt(b.getShort(frames + index * 8 + 4))) {
                remaining -= Short.toUnsignedInt(b.getShort(frames + index * 8 + 4));
                index++;
            }
            int cell = Short.toUnsignedInt(b.getShort(data + b.getInt(frames + index * 8)));
            Pixels pixels = decoded.get(cell);
            if (pixels == null) {
                if (type < 16 || type == 24) {
                    int group = type == 24 ? 16 : type;
                    pixels =
                            compose(
                                    archive.read(131 + group * 2),
                                    archive.read(132 + group * 2),
                                    archive.read(129),
                                    cell,
                                    32,
                                    -1,
                                    true);
                } else if (cell < 8) pixels = particle(type * 4 + 48, cell);
                else if (cell < 11) pixels = new Pixels(32, 32, new int[32 * 32]);
                else {
                    var base = particle(type * 4 + 48, 0);
                    int[] rgba = new int[32 * 32];
                    float angle =
                            switch (cell) {
                                case 12 -> -33;
                                case 13 -> -40;
                                case 14 -> 20;
                                case 15 -> 33;
                                case 16 -> 44;
                                default -> -10;
                            };
                    int shift =
                            switch (cell) {
                                case 12 -> -1;
                                case 13 -> -2;
                                case 14 -> 1;
                                case 15, 16 -> 2;
                                default -> 0;
                            };
                    double cos = Math.cos(Math.toRadians(angle)),
                            sin = Math.sin(Math.toRadians(angle));
                    for (int y = 0; y < 32; y++)
                        for (int x = 0; x < 32; x++) {
                            int sx = (int) Math.round((x - 16) * cos + (y - 16) * sin + 16),
                                    sy = (int) Math.round(-(x - 16) * sin + (y - 16) * cos + 16);
                            if (sx >= 0
                                    && sx < base.width()
                                    && sy >= 0
                                    && sy < base.height()
                                    && x + shift >= 0
                                    && x + shift < 32)
                                rgba[y * 32 + x + shift] = base.rgba()[sy * base.width() + sx];
                        }
                    pixels = new Pixels(32, 32, rgba);
                }
                decoded.put(cell, pixels);
            }
            result.add(pixels);
        }
        if (result.isEmpty()) throw new IOException("Missing throwing ball animation");
        return java.util.List.copyOf(result);
    }

    public Pixels particle(int id, int frame) throws IOException {
        var data =
                java.nio.ByteBuffer.wrap(dex.archive("a/0/0/6").read(id))
                        .order(java.nio.ByteOrder.LITTLE_ENDIAN);
        try {
            if (data.getInt(0) != 1397768480 || Short.toUnsignedInt(data.getShort(10)) < 1)
                throw new IOException("Missing APS texture " + id);
            int count = Short.toUnsignedInt(data.getShort(10));
            if (frame < 0 || frame >= count)
                throw new IOException("Missing APS frame " + id + ":" + frame);
            data.position(data.getInt(24));
            for (int f = 0; f < frame; f++) {
                int at = data.position();
                int pixels = data.getInt(at + 8),
                        palette = Short.toUnsignedInt(data.getShort(at + 16));
                data.position(at + 32 + pixels + palette);
            }
            data.getInt();
            int flags = Short.toUnsignedInt(data.getShort());
            data.getShort();
            int size = data.getInt();
            data.getInt();
            int paletteSize = Short.toUnsignedInt(data.getShort());
            data.position(data.position() + 14);
            byte[] pixels = new byte[size], palette = new byte[paletteSize];
            data.get(pixels);
            data.get(palette);
            int width = 8 << ((flags >>> 4) & 7),
                    height = 8 << ((flags >>> 8) & 7),
                    format = flags & 7;
            int[] rgba = new int[width * height];
            for (int i = 0; i < rgba.length; i++) {
                int value = pixels[format == 3 ? i / 2 : i] & 255, index, alpha;
                if (format == 3) {
                    index = (value >>> ((i & 1) * 4)) & 15;
                    alpha = index == 0 ? 0 : 255;
                } else if (format == 1) {
                    index = value & 31;
                    int a = ((byte) value) >> 5;
                    alpha = (a / 2 + a * 4) * 8;
                    alpha = (alpha | (alpha >> 5)) & 255;
                } else if (format == 6) {
                    index = value & 7;
                    alpha = (value >>> 3) * 8;
                } else throw new IOException("Unsupported APS format " + format);
                int color = (palette[index * 2] & 255) | ((palette[index * 2 + 1] & 255) << 8);
                rgba[i] =
                        ((color & 31) * 8 << 24)
                                | ((color >>> 5 & 31) * 8 << 16)
                                | ((color >>> 10 & 31) * 8 << 8)
                                | alpha;
            }
            return new Pixels(width, height, rgba);
        } catch (RuntimeException failure) {
            throw new IOException("Invalid APS texture " + id, failure);
        }
    }

    /** Gdx3DModelBatch.rj0 / NitroTilePaletteResource.dB: battle background triples in a/0/9/5. */
    public Pixels battleBackground(int id) throws IOException {
        var archive = dex.archive("a/0/9/5");
        // Gdx3DModelBatch.rj0: NSCR layout, NCGR tiles, then NCLR palette.
        // Its decompiled local names are misleading; Gt0 decodes NCGR and Tt0 NCLR.
        byte[] screen = archive.read(id * 3),
                tiles = archive.read(id * 3 + 1),
                palette = archive.read(id * 3 + 2);
        if (screen.length < 36 || i32(screen, 0) != 0x4e534352)
            throw new IOException("Battle background " + id + " requires an NSCR layout");
        if (tiles.length < 48 || i32(tiles, 0) != 0x4e434752)
            throw new IOException("Battle background " + id + " requires NCGR tiles");
        if (palette.length < 40 || i32(palette, 0) != 0x4e434c52)
            throw new IOException("Battle background " + id + " requires an NCLR palette");
        int width = 256,
                height =
                        (id == 8 || id == 9 || id == 11 || id == 34 || id >= 29 && id <= 32)
                                ? 192
                                : (id == 23 || id == 25) ? 256 : 200;
        int depth = i32(tiles, 28),
                data = 24 + i32(tiles, 44),
                paletteStart = 24 + i32(palette, 36);
        int paletteSize = i32(palette, 32), paletteSectionSize = i32(palette, 20);
        if (paletteSize == 0 || paletteSize > paletteSectionSize)
            paletteSize = paletteSectionSize - 24;
        int colorsPerBank = u16(palette, 24) == 3 ? 16 : 256;
        int storedBankSize = Math.min(colorsPerBank, paletteSize / 2);
        int paletteEnd = paletteStart + paletteSize;
        int firstPaletteBank = 0;
        // NitroPaletteResource.Of expands a PCMP palette prefix, filling lower
        // bank numbers with the first stored bank before copying the stored banks.
        if (u16(palette, 14) != 1 && paletteEnd < palette.length) {
            if (paletteEnd + 18 > palette.length || i32(palette, paletteEnd) != 0x50434d50)
                throw new IOException("Invalid battle background " + id + " PCMP section");
            firstPaletteBank = u16(palette, paletteEnd + 16);
        }
        int[] pixels = new int[width * height];
        if (depth != 3 && depth != 4)
            throw new IOException("Unsupported battle background NCGR depth " + depth);
        int count = i32(screen, 32) / 2;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int index = (y / 8) * (width / 8) + x / 8;
                if (index >= count) continue;
                int tile = u16(screen, 36 + index * 2), tx = x & 7, ty = y & 7;
                if ((tile & 1024) != 0) tx = 7 - tx;
                if ((tile & 2048) != 0) ty = 7 - ty;
                int offset = (tile & 1023) * 64 + ty * 8 + tx, value;
                if (depth == 3) {
                    int packed = tiles[data + offset / 2] & 255;
                    value = packed >>> ((offset & 1) * 4) & 15;
                } else value = tiles[data + offset] & 255;
                int paletteBank = (tile >>> 12) + (colorsPerBank == 16 ? value >>> 4 : 0);
                int colorIndex = colorsPerBank == 16 ? value & 15 : value;
                int storedBank = Math.max(0, paletteBank - firstPaletteBank);
                int colorOffset = paletteStart + (storedBank * storedBankSize + colorIndex) * 2;
                if (colorIndex >= storedBankSize || colorOffset + 2 > paletteEnd)
                    throw new IOException(
                            "Battle background "
                                    + id
                                    + " palette bank "
                                    + paletteBank
                                    + " color "
                                    + colorIndex
                                    + " is missing");
                int rgb = u16(palette, colorOffset);
                // NitroPaletteResource uses 5-bit channels * 8 and transparent
                // entry zero independently in every palette bank.
                pixels[y * width + x] =
                        ((rgb & 31) * 8) << 24
                                | ((rgb >> 5 & 31) * 8) << 16
                                | (Math.min(31, rgb >>> 10) * 8) << 8
                                | (colorIndex == 0 ? 0 : 255);
            }
        return new Pixels(width, height, pixels);
    }

    public BwPokemonAnimation frontAnimation(int id, int form, boolean shiny) throws IOException {
        return animation(id, form, shiny, false);
    }

    /** BlackWhiteRom.loadSprites: rear sprites use the second nine-entry battle group. */
    public BwPokemonAnimation animation(int id, int form, boolean shiny, boolean back)
            throws IOException {
        dex.get(id);
        var extra = dex.extra(id);
        if (form < 0 || form >= Math.max(1, extra.formCount()))
            throw new IOException("Invalid Pokémon form");
        int sprite = form > 0 && id != 493 ? 652 + extra.formSprite() + form - 1 : id;
        if (battle == null) {
            battle = dex.archive("a/0/0/4");
            battleCells = battle.read(4);
        }
        int base = sprite * 20 + (back ? 9 : 0);
        return new BwPokemonAnimation(
                battle.read(base + 2),
                battle.read(sprite * 20 + 18 + (shiny ? 1 : 0)),
                battle.read(base + 4),
                battle.read(base + 5),
                battle.read(base + 6),
                battle.read(base + 7));
    }

    static Pixels compose(
            byte[] tiles, byte[] palette, byte[] cells, int frame, int size, int paletteOverride)
            throws IOException {
        return compose(tiles, palette, cells, frame, size, paletteOverride, false);
    }

    static Pixels compose(
            byte[] tiles,
            byte[] palette,
            byte[] cells,
            int frame,
            int size,
            int paletteOverride,
            boolean animated)
            throws IOException {
        return compose(
                tiles,
                palette,
                cells,
                frame,
                size,
                size,
                size / 2,
                size / 2,
                paletteOverride,
                animated);
    }

    static Pixels composeCell(
            byte[] tiles,
            byte[] palette,
            byte[] cells,
            int frame,
            int width,
            int height,
            int originX,
            int originY)
            throws IOException {
        return compose(tiles, palette, cells, frame, width, height, originX, originY, -1, true);
    }

    private static Pixels compose(
            byte[] tiles,
            byte[] palette,
            byte[] cells,
            int frame,
            int width,
            int height,
            int originX,
            int originY,
            int paletteOverride,
            boolean animated)
            throws IOException {
        return compose(
                tiles,
                palette,
                cells,
                frame,
                width,
                height,
                originX,
                originY,
                paletteOverride,
                animated,
                null);
    }

    private static Pixels compose(
            byte[] tiles,
            byte[] palette,
            byte[] cells,
            int frame,
            int width,
            int height,
            int originX,
            int originY,
            int paletteOverride,
            boolean animated,
            int[] customColors)
            throws IOException {
        try {
            if (i32(tiles, 0) != 0x4e434752
                    || i32(palette, 0) != 0x4e434c52
                    || i32(cells, 0) != 0x4e434552)
                throw new IOException("Invalid Nitro Pokémon sprite");
            int depth = i32(tiles, 28),
                    dataOffset = 24 + i32(tiles, 44),
                    tileLength = i32(tiles, 40);
            boolean bitmap = i32(tiles, 36) != 0;
            int bitmapWidth = u16(tiles, 26) * 8;
            if (depth != 3 && depth != 4) throw new IOException("Unsupported NCGR depth " + depth);
            int paletteStart = 24 + i32(palette, 36), paletteSize = i32(palette, 32) / 2;
            int[] colors = new int[paletteSize];
            for (int i = 0; i < colors.length; i++) {
                int c = u16(palette, paletteStart + i * 2);
                colors[i] =
                        ((c & 31) * 255 / 31) << 24
                                | ((c >> 5 & 31) * 255 / 31) << 16
                                | ((c >> 10 & 31) * 255 / 31) << 8
                                | 255;
            }
            if (customColors != null)
                System.arraycopy(customColors, 0, colors, paletteOverride * 16, 16);
            int count = u16(cells, 24), cellSize = u16(cells, 26) == 1 ? 16 : 8;
            if (frame >= count) throw new IOException("Missing NCER frame");
            int cellStart = 24 + i32(cells, 28), cell = cellStart + frame * cellSize;
            int objects = u16(cells, cell),
                    oam = cellStart + count * cellSize + i32(cells, cell + 4),
                    mapping = i32(cells, 32) & 3;
            var order = new ArrayList<Integer>();
            for (int i = 0; i < objects; i++) order.add(oam + i * 6);
            order.sort(Comparator.comparingInt((Integer p) -> -(u16(cells, p + 4) >> 10 & 3)));
            if (animated) Collections.reverse(order);
            int[] output = new int[width * height];
            int[][] widths = {{8, 16, 32, 64}, {16, 32, 32, 64}, {8, 8, 16, 32}};
            int[][] heights = {{8, 16, 32, 64}, {8, 8, 16, 32}, {16, 32, 32, 64}};
            for (int p : order) {
                int a = u16(cells, p),
                        b = u16(cells, p + 2),
                        c = u16(cells, p + 4),
                        shape = a >> 14,
                        dimension = b >> 14;
                if (shape >= 3) continue;
                int w = widths[shape][dimension], h = heights[shape][dimension];
                int x = b & 511;
                if (x >= 256) x -= 512;
                int y = (byte) a, base = ((c & 1023) << mapping) * 32;
                // NitroOamData.else$ places double-size affine objects at the centre of their OAM
                // bounds.
                if (animated && (a & 0x300) == 0x300) {
                    x += w / 2;
                    y += h / 2;
                }
                boolean flipX = (a & 256) == 0 && (b & 4096) != 0,
                        flipY = (a & 256) == 0 && (b & 8192) != 0;
                int bank = paletteOverride >= 0 ? paletteOverride : c >> 12;
                if (bank * (depth == 3 ? 16 : 256) >= colors.length) bank = 0;
                for (int py = 0; py < h; py++)
                    for (int px = 0; px < w; px++) {
                        int sx = flipX ? w - 1 - px : px, sy = flipY ? h - 1 - py : py;
                        int pixel =
                                bitmap
                                        ? (((c & 1023) / (bitmapWidth / 8) * 8 + sy) * bitmapWidth
                                                + (c & 1023) % (bitmapWidth / 8) * 8
                                                + sx)
                                        : ((sy / 8) * (w / 8) + sx / 8) * 64
                                                + (sy % 8) * 8
                                                + sx % 8;
                        int index = (bitmap ? 0 : base) + (depth == 3 ? pixel / 2 : pixel);
                        if (index >= tileLength) continue;
                        int value = tiles[dataOffset + index] & 255;
                        if (depth == 3) value = (value >> ((pixel & 1) * 4)) & 15;
                        if (value == 0) continue;
                        int dx = originX + x + px, dy = originY + y + py;
                        if (dx >= 0 && dy >= 0 && dx < width && dy < height)
                            output[dy * width + dx] =
                                    colors[bank * (depth == 3 ? 16 : 256) + value];
                    }
            }
            return new Pixels(width, height, output);
        } catch (IndexOutOfBoundsException error) {
            throw new IOException("Truncated Pokémon sprite", error);
        }
    }

    /** Same backwards LZ as reference BlzDecompressor (the palette indices live in ARM9). */
    private static byte[] decompressArm(byte[] input) throws IOException {
        try {
            int n = input.length, extra = i32(input, n - 4);
            if (extra == 0) return input;
            int header = input[n - 5] & 255, packed = i32(input, n - 8) & 0xffffff;
            if (header < 8
                    || header > 11
                    || packed < header
                    || packed > n
                    || extra < 0
                    || extra > 16 * 1024 * 1024 - n)
                throw new IOException("Invalid ARM9 compression footer");
            int start = n - packed, source = n - header, destination = n + extra;
            byte[] out = Arrays.copyOf(input, destination);
            while (destination > start) {
                int flags = out[--source] & 255;
                for (int bit = 7; bit >= 0 && destination > start; bit--) {
                    if ((flags & (1 << bit)) == 0) out[--destination] = out[--source];
                    else {
                        int token = (out[--source] & 255) << 8;
                        token |= out[--source] & 255;
                        int length = (token >>> 12) + 3, distance = (token & 4095) + 3;
                        while (length-- > 0 && destination > start) {
                            destination--;
                            out[destination] = out[destination + distance];
                        }
                    }
                }
            }
            return out;
        } catch (IndexOutOfBoundsException error) {
            throw new IOException("Truncated ARM9", error);
        }
    }

    private static int u16(byte[] b, int p) {
        return BwPokedex.u16(b, p);
    }

    private static int i32(byte[] b, int p) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt(p);
    }
}
