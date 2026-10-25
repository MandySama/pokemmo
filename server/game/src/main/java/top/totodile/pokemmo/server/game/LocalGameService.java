package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import top.totodile.pokemmo.server.login.LoginService.Selection;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** 单机角色服务；所有数据库操作在同一后台线程执行。 */
public final class LocalGameService implements GameService {
    private final Map<String, top.totodile.pokemmo.server.game.battle.BattleEngine.Session>
            battles = new HashMap<>();

    private void closeBattle(String id) {
        var battle = battles.remove(id);
        if (battle != null) battle.close();
    }

    @Override
    public CompletionStage<top.totodile.pokemmo.server.game.battle.BattleState> startBattle(
            Selection selection,
            String id,
            top.totodile.pokemmo.server.game.battle.BattleEngine.Kind kind) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    var encounter = encounters.get(id);
                    if (encounter == null)
                        throw new IllegalStateException("No generated encounter");
                    closeBattle(id);
                    var session =
                            top.totodile.pokemmo.server.game.battle.BattleEngine.create(kind)
                                    .start(encounter);
                    battles.put(id, session);
                    return session.state();
                },
                worker);
    }

    @Override
    public CompletionStage<top.totodile.pokemmo.server.game.battle.BattleState> chooseBattle(
            Selection selection,
            String id,
            top.totodile.pokemmo.server.game.battle.BattleEngine.Action action) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    var session = battles.get(id);
                    if (session == null) throw new IllegalStateException("No active battle engine");
                    var state = session.choose(action);
                    encounters.put(id, state.apply(encounters.get(id)));
                    return state;
                },
                worker);
    }

    @Override
    public CompletionStage<Void> endBattle(Selection selection, String id) {
        return CompletableFuture.runAsync(
                () -> {
                    playerData(selection, id);
                    closeBattle(id);
                },
                worker);
    }

    private final Supplier<Database> factory;
    private final ExecutorService worker =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread t = new Thread(r, "local-game");
                        t.setDaemon(true);
                        return t;
                    });
    private Database database;
    private final Map<String, EncounterData> encounters = new HashMap<>();
    private final Random captureRandom = new Random();
    private final Map<String, Position> mailboxes = new HashMap<>();
    private final Map<String, Long> playStarted = new HashMap<>();
    private final java.util.concurrent.atomic.AtomicBoolean closed =
            new java.util.concurrent.atomic.AtomicBoolean();

    public LocalGameService(Supplier<Database> factory) {
        this.factory = factory;
    }

    @Override
    public byte[] pokedexTiers() {
        return PokedexTierData.payload();
    }

    private Database db() {
        if (database == null) {
            Database opened = Objects.requireNonNull(factory.get());
            try {
                opened.execute("PRAGMA foreign_keys=ON");
                opened.execute(
                        "CREATE TABLE IF NOT EXISTS characters (id TEXT PRIMARY KEY, account TEXT"
                            + " NOT NULL, server TEXT NOT NULL, name TEXT NOT NULL, name_key TEXT"
                            + " NOT NULL, gender INTEGER NOT NULL, skin INTEGER NOT NULL, region"
                            + " INTEGER NOT NULL, created_at INTEGER NOT NULL,"
                            + " UNIQUE(server,name_key))");
                // Reference character.money defaults to zero; migrate existing local characters in
                // place.
                if (opened.query("PRAGMA table_info(characters)").stream()
                        .noneMatch(row -> "money".equals(row[1])))
                    opened.execute(
                            "ALTER TABLE characters ADD COLUMN money INTEGER NOT NULL DEFAULT 0");
                opened.execute(
                        "CREATE TABLE IF NOT EXISTS character_parts (character_id TEXT NOT NULL"
                            + " REFERENCES characters(id) ON DELETE CASCADE, slot INTEGER NOT NULL,"
                            + " style INTEGER NOT NULL, color INTEGER NOT NULL, PRIMARY"
                            + " KEY(character_id,slot))");
                opened.execute(
                        "CREATE TABLE IF NOT EXISTS character_positions (character_id TEXT PRIMARY"
                            + " KEY REFERENCES characters(id) ON DELETE CASCADE, bank INTEGER NOT"
                            + " NULL, map INTEGER NOT NULL, x INTEGER NOT NULL, y INTEGER NOT NULL,"
                            + " direction INTEGER NOT NULL)");
                opened.execute(
                        "CREATE TABLE IF NOT EXISTS character_current_region (character_id TEXT"
                                + " PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE,region"
                                + " INTEGER NOT NULL)");
                opened.execute(
                        "CREATE TABLE IF NOT EXISTS character_travel_state (character_id TEXT"
                            + " PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE, no_clip"
                            + " INTEGER NOT NULL, speed REAL NOT NULL, bicycle INTEGER NOT NULL)");
                opened.execute(
                        "CREATE TABLE IF NOT EXISTS character_pokedex_state (character_id TEXT"
                            + " PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE, unlocked"
                            + " INTEGER NOT NULL)");
                PlayerDataStore.initialize(opened);
                PokemonStore.initialize(opened);
                PokedexStore.initialize(opened);
                FollowerStore.initialize(opened);
                database = opened;
            } catch (RuntimeException | Error error) {
                try {
                    opened.close();
                } catch (RuntimeException | Error closeError) {
                    error.addSuppressed(closeError);
                }
                throw error;
            }
        }
        return database;
    }

    private static void identity(Selection selection) {
        Objects.requireNonNull(selection);
        if (selection.session().username().isBlank())
            throw new IllegalArgumentException("Invalid local identity");
    }

    @Override
    public CompletionStage<List<Character>> characters(Selection selection) {
        return CompletableFuture.supplyAsync(
                () -> {
                    identity(selection);
                    List<Character> result = new ArrayList<>();
                    for (String[] row :
                            db().query(
                                            "SELECT id,name,gender,skin,region,created_at FROM"
                                                + " characters WHERE account=? AND server=? ORDER"
                                                + " BY created_at,id",
                                            selection.session().username(),
                                            selection.server().id())) {
                        Map<Integer, Part> parts = new HashMap<>();
                        for (String[] part :
                                db().query(
                                                "SELECT slot,style,color FROM character_parts WHERE"
                                                        + " character_id=?",
                                                row[0]))
                            parts.put(
                                    Integer.parseInt(part[0]),
                                    new Part(Integer.parseInt(part[1]), Integer.parseInt(part[2])));
                        result.add(
                                new Character(
                                        row[0],
                                        row[1],
                                        new Appearance(
                                                Integer.parseInt(row[2]),
                                                Integer.parseInt(row[3]),
                                                Integer.parseInt(row[4]),
                                                parts),
                                        Long.parseLong(row[5])));
                    }
                    return List.copyOf(result);
                },
                worker);
    }

    @Override
    public CompletionStage<Character> create(
            Selection selection, String name, Appearance appearance) {
        return CompletableFuture.supplyAsync(
                () -> {
                    identity(selection);
                    if (name == null
                            || name.codePointCount(0, name.length()) < 2
                            || name.codePointCount(0, name.length()) > 16
                            || !name.codePoints().allMatch(java.lang.Character::isLetter))
                        throw new Rejected(Failure.INVALID_NAME);
                    if (appearance.gender() < 0
                            || appearance.gender() > 1
                            || appearance.skin() < 0
                            || appearance.skin() > 4
                            || appearance.region() < 0
                            || appearance.region() > 4
                            || !appearance
                                    .parts()
                                    .keySet()
                                    .equals(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11)))
                        throw new IllegalArgumentException("Invalid character appearance");
                    String key = name.toLowerCase(Locale.ROOT), server = selection.server().id();
                    Character character =
                            new Character(
                                    UUID.randomUUID().toString(),
                                    name,
                                    appearance,
                                    System.currentTimeMillis());
                    db().transaction(
                                    () -> {
                                        if (Integer.parseInt(
                                                        db().query(
                                                                        "SELECT COUNT(*) FROM"
                                                                            + " characters WHERE"
                                                                            + " account=?",
                                                                        selection
                                                                                .session()
                                                                                .username())
                                                                .get(0)[0])
                                                >= 3) throw new Rejected(Failure.ACCOUNT_FULL);
                                        if (!db().query(
                                                        "SELECT id FROM characters WHERE server=?"
                                                                + " AND name_key=?",
                                                        server,
                                                        key)
                                                .isEmpty()) throw new Rejected(Failure.NAME_TAKEN);
                                        db().execute(
                                                        "INSERT INTO characters"
                                                            + " (id,account,server,name,name_key,gender,skin,region,created_at)"
                                                            + " VALUES (?,?,?,?,?,?,?,?,?)",
                                                        character.id(),
                                                        selection.session().username(),
                                                        server,
                                                        name,
                                                        key,
                                                        appearance.gender(),
                                                        appearance.skin(),
                                                        appearance.region(),
                                                        character.createdAt());
                                        appearance
                                                .parts()
                                                .forEach(
                                                        (slot, part) ->
                                                                db().execute(
                                                                                "INSERT INTO"
                                                                                    + " character_parts"
                                                                                    + " VALUES"
                                                                                    + " (?,?,?,?)",
                                                                                character.id(),
                                                                                slot,
                                                                                part.style(),
                                                                                part.color()));
                                    });
                    return character;
                },
                worker);
    }

    @Override
    public CompletionStage<Void> delete(Selection selection, String characterId) {
        return CompletableFuture.runAsync(
                () -> {
                    identity(selection);
                    db().transaction(
                                    () -> {
                                        String[] values = {
                                            characterId,
                                            selection.session().username(),
                                            selection.server().id()
                                        };
                                        if (db().query(
                                                        "SELECT id FROM characters WHERE id=? AND"
                                                                + " account=? AND server=?",
                                                        values)
                                                .isEmpty()) throw new Rejected(Failure.NOT_FOUND);
                                        db().execute(
                                                        "DELETE FROM characters WHERE id=? AND"
                                                                + " account=? AND server=?",
                                                        (Object[]) values);
                                        playStarted.remove(characterId);
                                        mailboxes.remove(characterId);
                                    });
                },
                worker);
    }

    @Override
    public CompletionStage<Character> saveAppearance(
            Selection selection, String id, Appearance appearance) {
        return characters(selection)
                .thenApplyAsync(
                        values -> {
                            var old =
                                    values.stream()
                                            .filter(c -> c.id().equals(id))
                                            .findFirst()
                                            .orElseThrow(() -> new Rejected(Failure.NOT_FOUND));
                            // TrainerCustomizationOutboundPacket: ten style bits and six colour
                            // bits.
                            if (appearance.gender() < 0
                                    || appearance.gender() > 1
                                    || appearance.skin() < 0
                                    || appearance.skin() > 4
                                    || appearance.region() != old.appearance().region()
                                    || !appearance
                                            .parts()
                                            .keySet()
                                            .containsAll(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11))
                                    || appearance.parts().entrySet().stream()
                                            .anyMatch(
                                                    e ->
                                                            e.getKey() < 0
                                                                    || e.getKey() > 11
                                                                    || e.getValue().style() < -1
                                                                    || e.getValue().style() > 1023
                                                                    || e.getValue().color() < -1
                                                                    || e.getValue().color() > 63))
                                throw new IllegalArgumentException(
                                        "Invalid character customization");
                            db().transaction(
                                            () -> {
                                                db().execute(
                                                                "UPDATE characters SET"
                                                                    + " gender=?,skin=? WHERE id=?",
                                                                appearance.gender(),
                                                                appearance.skin(),
                                                                id);
                                                db().execute(
                                                                "DELETE FROM character_parts WHERE"
                                                                        + " character_id=?",
                                                                id);
                                                appearance
                                                        .parts()
                                                        .forEach(
                                                                (slot, part) ->
                                                                        db().execute(
                                                                                        "INSERT"
                                                                                            + " INTO"
                                                                                            + " character_parts"
                                                                                            + " VALUES"
                                                                                            + " (?,?,?,?)",
                                                                                        id,
                                                                                        slot,
                                                                                        part
                                                                                                .style(),
                                                                                        part
                                                                                                .color()));
                                            });
                            return new Character(old.id(), old.name(), appearance, old.createdAt());
                        },
                        worker);
    }

    @Override
    public CompletionStage<Entry> enter(Selection selection, String characterId) {
        return characters(selection)
                .thenApplyAsync(
                        values -> {
                            Character character =
                                    values.stream()
                                            .filter(c -> c.id().equals(characterId))
                                            .findFirst()
                                            .orElseThrow(() -> new Rejected(Failure.NOT_FOUND));
                            var rows =
                                    db().query(
                                                    "SELECT bank,map,x,y,direction FROM"
                                                            + " character_positions WHERE"
                                                            + " character_id=?",
                                                    characterId);
                            int region =
                                    currentRegion(characterId, character.appearance().region());
                            Position position = Position.start(region);
                            if (!rows.isEmpty()) {
                                String[] row = rows.get(0);
                                position =
                                        new Position(
                                                Integer.parseInt(row[0]),
                                                Integer.parseInt(row[1]),
                                                Integer.parseInt(row[2]),
                                                Integer.parseInt(row[3]),
                                                Integer.parseInt(row[4]));
                            }
                            var states =
                                    db().query(
                                                    "SELECT no_clip,speed,bicycle FROM"
                                                            + " character_travel_state WHERE"
                                                            + " character_id=?",
                                                    characterId);
                            TravelState state = TravelState.defaults();
                            if (!states.isEmpty()) {
                                String[] row = states.get(0);
                                state =
                                        new TravelState(
                                                Integer.parseInt(row[0]) != 0,
                                                Float.parseFloat(row[1]),
                                                Integer.parseInt(row[2]) != 0);
                            }
                            var pokedex = new PokedexStore(db()).load(characterId);
                            int money =
                                    Integer.parseInt(
                                            db().query(
                                                            "SELECT money FROM characters WHERE"
                                                                    + " id=?",
                                                            characterId)
                                                    .get(0)[0]);
                            WorldInfo defaults = WorldInfo.defaults();
                            playStarted.putIfAbsent(characterId, System.nanoTime());
                            return new Entry(
                                    character,
                                    position,
                                    state,
                                    pokedex,
                                    new WorldInfo(money, defaults.channel(), defaults.timeOrigin()),
                                    region);
                        },
                        worker);
    }

    @Override
    public CompletionStage<Void> savePosition(
            Selection selection, String characterId, Position position) {
        Objects.requireNonNull(position);
        return CompletableFuture.runAsync(
                () -> {
                    identity(selection);
                    db().transaction(
                                    () -> {
                                        if (db().query(
                                                        "SELECT id FROM characters WHERE id=? AND"
                                                                + " account=? AND server=?",
                                                        characterId,
                                                        selection.session().username(),
                                                        selection.server().id())
                                                .isEmpty()) throw new Rejected(Failure.NOT_FOUND);
                                        db().execute(
                                                        "INSERT OR REPLACE INTO character_positions"
                                                                + " VALUES (?,?,?,?,?,?)",
                                                        characterId,
                                                        position.bank(),
                                                        position.map(),
                                                        position.x(),
                                                        position.y(),
                                                        position.direction());
                                        if (!position.equals(mailboxes.get(characterId)))
                                            mailboxes.remove(characterId);
                                        checkpointPlaytime(characterId);
                                    });
                },
                worker);
    }

    private int currentRegion(String id, int fallback) {
        var rows =
                db().query("SELECT region FROM character_current_region WHERE character_id=?", id);
        return rows.isEmpty() ? fallback : Integer.parseInt(rows.get(0)[0]);
    }

    @Override
    public CompletionStage<Void> teleport(
            Selection selection, String id, int region, Position position) {
        Objects.requireNonNull(position);
        return CompletableFuture.runAsync(
                () -> {
                    playerData(selection, id);
                    if (region < 0 || region > 4)
                        throw new IllegalArgumentException("Unknown region: " + region);
                    db().transaction(
                                    () -> {
                                        db().execute(
                                                        "INSERT OR REPLACE INTO"
                                                                + " character_current_region VALUES"
                                                                + " (?,?)",
                                                        id,
                                                        region);
                                        db().execute(
                                                        "INSERT OR REPLACE INTO character_positions"
                                                                + " VALUES (?,?,?,?,?,?)",
                                                        id,
                                                        position.bank(),
                                                        position.map(),
                                                        position.x(),
                                                        position.y(),
                                                        position.direction());
                                        mailboxes.remove(id);
                                        checkpointPlaytime(id);
                                    });
                },
                worker);
    }

    private volatile Map<Integer, ItemTemplate> itemTemplates = Map.of();
    private volatile Map<Integer, SpeciesTemplate> speciesTemplates = Map.of();

    @Override
    public void gameData(List<ItemTemplate> items, List<SpeciesTemplate> species) {
        itemTemplates =
                items.stream()
                        .collect(
                                java.util.stream.Collectors.toUnmodifiableMap(
                                        ItemTemplate::id, java.util.function.Function.identity()));
        speciesTemplates =
                species.stream()
                        .collect(
                                java.util.stream.Collectors.toUnmodifiableMap(
                                        SpeciesTemplate::id,
                                        java.util.function.Function.identity()));
    }

    @Override
    public CompletionStage<PlayerData.Item> createItem(
            Selection selection, String id, int itemId, int amount) {
        return CompletableFuture.supplyAsync(
                () -> {
                    var store = playerData(selection, id);
                    if (itemId < 1
                            || itemId > Short.MAX_VALUE
                            || !itemTemplates.containsKey(itemId))
                        throw new IllegalArgumentException("Unknown item id: " + itemId);
                    return store.createItem(id, itemId, amount);
                },
                worker);
    }

    @Override
    public List<ItemTemplate> itemTemplates() {
        return itemTemplates.values().stream()
                .sorted(Comparator.comparingInt(ItemTemplate::id))
                .toList();
    }

    @Override
    public CompletionStage<List<PlayerData.Item>> destroyItem(
            Selection selection, String id, String objectId, int amount) {
        return CompletableFuture.supplyAsync(
                () -> playerData(selection, id).destroyItem(id, objectId, amount, itemTemplates),
                worker);
    }

    @Override
    public CompletionStage<List<PlayerData.Item>> replaceInventory(
            Selection selection, String id, Map<Integer, Integer> items) {
        var replacement = Map.copyOf(items);
        return CompletableFuture.supplyAsync(
                () -> {
                    var store = playerData(selection, id);
                    for (var entry : replacement.entrySet()) {
                        var template = itemTemplates.get(entry.getKey());
                        if (template == null
                                || entry.getKey() < 1
                                || entry.getKey() > Short.MAX_VALUE)
                            throw new IllegalArgumentException(
                                    "Unknown item id: " + entry.getKey());
                        if (entry.getValue() < 1
                                || entry.getValue()
                                        > Math.min(Short.MAX_VALUE, template.maximumStack()))
                            throw new IllegalArgumentException(
                                    "Invalid item amount: " + entry.getValue());
                    }
                    return store.replaceInventory(id, replacement);
                },
                worker);
    }

    @Override
    public CompletionStage<PokemonData.Containers> randomPokemonBox(
            Selection selection, String id) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    return new PokemonStore(db(), speciesTemplates)
                            .randomBox(id, speciesTemplates.values(), new Random());
                },
                worker);
    }

    @Override
    public CompletionStage<EncounterData> randomEncounter(Selection selection, String id) {
        return randomEncounter(selection, id, 1);
    }

    @Override
    public CompletionStage<EncounterData> randomEncounter(
            Selection selection, String id, int count) {
        return randomEncounter(selection, id, count, false);
    }

    @Override
    public CompletionStage<EncounterData> randomEncounter(
            Selection selection, String id, int count, boolean pure) {
        if (count != 1 && count != 3 && count != 5)
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Encounter size must be 1, 3 or 5"));
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    var party =
                            new PokemonStore(db(), speciesTemplates)
                                    .load(id).pokemons().stream()
                                            .filter(p -> p.container() == PokemonData.PARTY)
                                            .sorted(
                                                    Comparator.comparingInt(
                                                            PokemonData.Pokemon::position))
                                            .toList();
                    // BattleGenerator.findTopAlivePokemons /
                    // WildEncounterService.hasAlivePartyPokemon.
                    var lead =
                            party.stream()
                                    .filter(p -> !p.egg() && p.hp() > 0)
                                    .findFirst()
                                    .orElse(null);
                    if (lead == null) return null;
                    var candidates =
                            speciesTemplates.values().stream()
                                    .filter(s -> s.id() > 0 && s.id() <= 649)
                                    .sorted(Comparator.comparingInt(SpeciesTemplate::id))
                                    .toList();
                    if (candidates.isEmpty())
                        throw new IllegalStateException("No loaded Pokemon templates");
                    var random = new Random();
                    // Explicit random-button simulation; reuse the reference-derived wild
                    // generator.
                    // PokemonContainerType.EVENT=9: never insert the encounter into PARTY/PC or the
                    // database.
                    var wilds = new ArrayList<PokemonData.Pokemon>();
                    var pureSpecies = candidates.get(random.nextInt(candidates.size()));
                    var mixedSpecies = new ArrayList<>(candidates);
                    java.util.Collections.shuffle(mixedSpecies, random);
                    for (int index = 0; index < count; index++) {
                        // HORDE_BATTLE has five debut slots. Three-member hordes occupy 1..3.
                        int slot = count == 3 ? index + 1 : index;
                        wilds.add(
                                PokemonStore.createEncounterPokemon(
                                        pure
                                                ? pureSpecies
                                                : mixedSpecies.get(index % mixedSpecies.size()),
                                        random.nextInt(100) + 1,
                                        9,
                                        slot,
                                        random));
                    }
                    closeBattle(id);
                    var encounter = new EncounterData(lead, wilds, party);
                    encounters.put(id, encounter);
                    return encounter;
                },
                worker);
    }

    @Override
    public CompletionStage<PokemonData.Pokemon> addPokemon(
            Selection selection, String id, int species, int level) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    return new PokemonStore(db(), speciesTemplates)
                            .add(id, speciesTemplates.get(species), level, new Random());
                },
                worker);
    }

    @Override
    public CompletionStage<CaptureResult> capturePokemon(
            Selection selection, String id, String targetId, String ballObjectId) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    if (battles.containsKey(id))
                        throw new UnsupportedOperationException(
                                "Capture is not yet available with the Showdown engine");
                    var encounter = encounters.get(id);
                    if (encounter == null
                            || encounter.wilds().stream().noneMatch(p -> p.id().equals(targetId)))
                        throw new IllegalArgumentException("No eligible wild capture target");
                    var target =
                            encounter.wilds().stream()
                                    .filter(p -> p.id().equals(targetId))
                                    .findFirst()
                                    .orElseThrow();
                    CaptureResult[] result = new CaptureResult[1];
                    db().transaction(
                                    () -> {
                                        var rows =
                                                db().query(
                                                                "SELECT"
                                                                    + " item_index_id,item_amount,region"
                                                                    + " FROM owned_item WHERE"
                                                                    + " owner_id=? AND item_id=?"
                                                                    + " AND inventory_id=1",
                                                                id,
                                                                ballObjectId);
                                        if (rows.size() != 1
                                                || Integer.parseInt(rows.get(0)[1]) < 1)
                                            throw new IllegalArgumentException(
                                                    "No owned capture ball");
                                        int region =
                                                currentRegion(
                                                        id,
                                                        Integer.parseInt(
                                                                db().query(
                                                                                "SELECT region FROM"
                                                                                    + " characters"
                                                                                    + " WHERE id=?",
                                                                                id)
                                                                        .get(0)[0]));
                                        int ballRegion = Integer.parseInt(rows.get(0)[2]);
                                        if (ballRegion >= 0 && ballRegion != region)
                                            throw new IllegalArgumentException(
                                                    "Ball belongs to another region");
                                        var item =
                                                itemTemplates.get(Integer.parseInt(rows.get(0)[0]));
                                        if (item == null || item.ballType() < 0)
                                            throw new IllegalArgumentException(
                                                    "Item is not a capture ball");
                                        // Explicit user-requested simulation, not a replacement
                                        // catch-rate formula.
                                        int code = new int[] {0, 1, 3, 4}[captureRandom.nextInt(4)];
                                        var store = new PokemonStore(db(), speciesTemplates);
                                        PokemonData.Pokemon caught =
                                                code == 4
                                                        ? store.capture(id, target, item.ballType())
                                                        : null;
                                        int quantity = Integer.parseInt(rows.get(0)[1]);
                                        if (quantity == 1)
                                            db().execute(
                                                            "DELETE FROM owned_item WHERE"
                                                                    + " owner_id=? AND item_id=?",
                                                            id,
                                                            ballObjectId);
                                        else
                                            db().execute(
                                                            "UPDATE owned_item SET"
                                                                + " item_amount=item_amount-1 WHERE"
                                                                + " owner_id=? AND item_id=?",
                                                            id,
                                                            ballObjectId);
                                        var dex = new PokedexStore(db());
                                        var state = dex.load(id);
                                        if (caught != null) {
                                            var entries = new HashMap<>(state.entries());
                                            entries.merge(
                                                    caught.species(),
                                                    7 | (caught.alpha() ? 8 : 0),
                                                    (a, c) -> a | c);
                                            // Stay in the same transaction as the ball decrement
                                            // and Pokemon insertion.
                                            db().execute(
                                                            "INSERT INTO character_pokedex_entry"
                                                                + " VALUES (?,?,?) ON"
                                                                + " CONFLICT(character_id,species)"
                                                                + " DO UPDATE SET"
                                                                + " flags=excluded.flags",
                                                            id,
                                                            caught.species(),
                                                            entries.get(caught.species()));
                                            state = new PokedexState(entries);
                                        }
                                        result[0] =
                                                new CaptureResult(
                                                        code,
                                                        caught,
                                                        store.load(id),
                                                        new PlayerDataStore(db()).inventory(id),
                                                        state);
                                    });
                    if (result[0].captured() != null) encounters.remove(id);
                    return result[0];
                },
                worker);
    }

    @Override
    public CompletionStage<Void> saveTravelState(
            Selection selection, String characterId, TravelState state) {
        Objects.requireNonNull(state);
        return CompletableFuture.runAsync(
                () -> {
                    identity(selection);
                    db().transaction(
                                    () -> {
                                        if (db().query(
                                                        "SELECT id FROM characters WHERE id=? AND"
                                                                + " account=? AND server=?",
                                                        characterId,
                                                        selection.session().username(),
                                                        selection.server().id())
                                                .isEmpty()) throw new Rejected(Failure.NOT_FOUND);
                                        db().execute(
                                                        "INSERT OR REPLACE INTO"
                                                                + " character_travel_state VALUES"
                                                                + " (?,?,?,?)",
                                                        characterId,
                                                        state.noClip() ? 1 : 0,
                                                        state.speed(),
                                                        state.bicycle() ? 1 : 0);
                                    });
                },
                worker);
    }

    @Override
    public CompletionStage<Void> savePokedexState(
            Selection selection, String characterId, PokedexState state) {
        Objects.requireNonNull(state);
        return CompletableFuture.runAsync(
                () -> {
                    playerData(selection, characterId);
                    new PokedexStore(db()).save(characterId, state);
                },
                worker);
    }

    private PlayerDataStore playerData(Selection selection, String characterId) {
        identity(selection);
        var store = new PlayerDataStore(db());
        store.requireOwner(selection, characterId);
        return store;
    }

    @Override
    public CompletionStage<List<PlayerData.Item>> inventory(Selection selection, String id) {
        return CompletableFuture.supplyAsync(() -> playerData(selection, id).inventory(id), worker);
    }

    @Override
    public CompletionStage<Follower> follower(Selection selection, String id) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    return new FollowerStore(db()).load(id);
                },
                worker);
    }

    @Override
    public CompletionStage<Follower> selectFollower(
            Selection selection, String id, int type, String pokemonId, int position) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    return new FollowerStore(db()).select(id, type, pokemonId, position);
                },
                worker);
    }

    @Override
    public CompletionStage<PokemonData.Containers> movePokemonMove(
            Selection selection, String id, String pokemonId, int from, int to) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    return new PokemonStore(db(), speciesTemplates)
                            .moveMove(id, pokemonId, from, to);
                },
                worker);
    }

    @Override
    public CompletionStage<PokemonData.Containers> setPokemonSetting(
            Selection selection,
            String id,
            String pokemonId,
            PokemonSetting setting,
            int index,
            int value) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    return new PokemonStore(db(), speciesTemplates)
                            .setSetting(id, pokemonId, setting, index, value, speciesTemplates);
                },
                worker);
    }

    @Override
    public CompletionStage<PokemonData.Containers> selectPokemonParticle(
            Selection selection, String character, String pokemonId, int particle) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, character);
                    return new PokemonStore(db(), speciesTemplates)
                            .selectParticle(character, pokemonId, particle);
                },
                worker);
    }

    @Override
    public CompletionStage<PokemonData.Containers> setPokemonMarks(
            Selection selection, String id, String pokemonId, int marks) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    return new PokemonStore(db(), speciesTemplates).setMarks(id, pokemonId, marks);
                },
                worker);
    }

    @Override
    public CompletionStage<PokemonData.Containers> setPokemonFlag(
            Selection selection, String id, String pokemonId, PokemonFlag flag, boolean enabled) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    return new PokemonStore(db(), speciesTemplates)
                            .setFlag(id, pokemonId, flag, enabled);
                },
                worker);
    }

    @Override
    public CompletionStage<PokemonData.Containers> pokemon(Selection selection, String id) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    return new PokemonStore(db(), speciesTemplates).load(id);
                },
                worker);
    }

    @Override
    public CompletionStage<PokemonData.Containers> releasePokemon(
            Selection selection, String id, List<String> ids) {
        var snapshot = List.copyOf(ids);
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    return new PokemonStore(db(), speciesTemplates).release(id, snapshot);
                },
                worker);
    }

    @Override
    public CompletionStage<PokemonData.Containers> movePokemon(
            Selection selection, String id, List<PokemonData.Move> moves) {
        var snapshot = moves == null ? null : List.copyOf(moves);
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    return new PokemonStore(db(), speciesTemplates).move(id, snapshot);
                },
                worker);
    }

    @Override
    public CompletionStage<PokemonData.Containers> sortPokemon(
            Selection selection, String id, int box) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    return new PokemonStore(db(), speciesTemplates).sort(id, box);
                },
                worker);
    }

    @Override
    public CompletionStage<PlayerData.Trainer> trainer(Selection selection, String id) {
        return characters(selection)
                .thenApplyAsync(
                        values -> {
                            var character =
                                    values.stream()
                                            .filter(c -> c.id().equals(id))
                                            .findFirst()
                                            .orElseThrow(() -> new Rejected(Failure.NOT_FOUND));
                            checkpointPlaytime(id);
                            return playerData(selection, id).trainer(character);
                        },
                        worker);
    }

    @Override
    public CompletionStage<PlayerData.MailPage> mail(
            Selection selection, String id, boolean sent, int page) {
        return CompletableFuture.supplyAsync(
                () -> playerData(selection, id).mail(id, sent, page), worker);
    }

    @Override
    public CompletionStage<PlayerData.Mail> readMail(
            Selection selection, String id, String mailId) {
        return CompletableFuture.supplyAsync(
                () -> playerData(selection, id).read(id, mailId), worker);
    }

    @Override
    public CompletionStage<Void> deleteMail(
            Selection selection, String id, String mailId, boolean sent) {
        return CompletableFuture.runAsync(
                () -> playerData(selection, id).delete(id, mailId, sent), worker);
    }

    private Position savedPosition(String id) {
        var rows =
                db().query(
                                "SELECT bank,map,x,y,direction FROM character_positions WHERE"
                                        + " character_id=?",
                                id);
        if (rows.isEmpty()) return null;
        var r = rows.get(0);
        return new Position(
                Integer.parseInt(r[0]),
                Integer.parseInt(r[1]),
                Integer.parseInt(r[2]),
                Integer.parseInt(r[3]),
                Integer.parseInt(r[4]));
    }

    public static boolean mailboxTarget(Position p, int region) {
        // PcInteractionService.isPcTarget: the confirmed Kanto centre PC.
        return PlayerData.pcTarget(p, region);
    }

    @Override
    public CompletionStage<Boolean> openMailbox(Selection selection, String id) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    mailboxes.remove(id);
                    var p = savedPosition(id);
                    int region =
                            currentRegion(
                                    id,
                                    Integer.parseInt(
                                            db().query(
                                                            "SELECT region FROM characters WHERE"
                                                                    + " id=?",
                                                            id)
                                                    .get(0)[0]));
                    if (!mailboxTarget(p, region)) return false;
                    mailboxes.put(id, p);
                    return true;
                },
                worker);
    }

    @Override
    public CompletionStage<Void> closeMailbox(Selection selection, String id) {
        return CompletableFuture.runAsync(
                () -> {
                    playerData(selection, id);
                    mailboxes.remove(id);
                },
                worker);
    }

    @Override
    public CompletionStage<PlayerData.MailResult> sendMail(
            Selection selection, String id, String recipient, String subject, String body) {
        return CompletableFuture.supplyAsync(
                () -> {
                    var store = playerData(selection, id);
                    var p = mailboxes.get(id);
                    if (p == null || !p.equals(savedPosition(id)))
                        return PlayerData.MailResult.ACCESS_DENIED;
                    return store.send(id, recipient, subject, body);
                },
                worker);
    }

    private void checkpointPlaytime(String id) {
        Long start = playStarted.get(id);
        if (start == null) return;
        long seconds = (System.nanoTime() - start) / 1_000_000_000L;
        if (seconds > 0) {
            new PlayerDataStore(db()).addPlayedSeconds(id, seconds);
            playStarted.put(id, start + seconds * 1_000_000_000L);
        }
    }

    @Override
    public CompletionStage<Void> leave(Selection selection, String id) {
        return CompletableFuture.runAsync(
                () -> {
                    playerData(selection, id);
                    checkpointPlaytime(id);
                    closeBattle(id);
                    encounters.remove(id);
                    playStarted.remove(id);
                    mailboxes.remove(id);
                },
                worker);
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        worker.execute(
                () -> {
                    for (String id : new ArrayList<>(battles.keySet())) closeBattle(id);
                    if (database != null) {
                        for (String id : new ArrayList<>(playStarted.keySet()))
                            checkpointPlaytime(id);
                        database.close();
                    }
                });
        worker.shutdown();
    }
}
