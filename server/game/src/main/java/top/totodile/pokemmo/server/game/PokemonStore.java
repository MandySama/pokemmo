package top.totodile.pokemmo.server.game;

import static top.totodile.pokemmo.server.game.PokemonData.*;

import top.totodile.pokemmo.db.Database;

import java.util.*;

/** Local DB adapter for reference PokemonContainerService: slots, batch swaps and level sort. */
final class PokemonStore {
    private final Database db;
    private final Map<Integer, GameService.SpeciesTemplate> species;

    PokemonStore(Database db) {
        this(db, Map.of());
    }

    PokemonStore(Database db, Map<Integer, GameService.SpeciesTemplate> species) {
        this.db = db;
        this.species = species;
    }

    static void initialize(Database db) {
        db.execute(
                "CREATE TABLE IF NOT EXISTS pokemon (id TEXT PRIMARY KEY,trainer_id TEXT NOT NULL"
                    + " REFERENCES characters(id) ON DELETE CASCADE,container_id INTEGER NOT"
                    + " NULL,container_position INTEGER NOT NULL,dex_id INTEGER NOT NULL,form_type"
                    + " INTEGER NOT NULL DEFAULT 0,gender INTEGER NOT NULL DEFAULT -1,is_shiny"
                    + " INTEGER NOT NULL DEFAULT 0,is_egg INTEGER NOT NULL DEFAULT 0,name TEXT NOT"
                    + " NULL DEFAULT '',level_value INTEGER NOT NULL,current_hp INTEGER NOT"
                    + " NULL,status INTEGER NOT NULL DEFAULT 0,item INTEGER NOT NULL DEFAULT"
                    + " 0,nature INTEGER NOT NULL DEFAULT 0,ability INTEGER NOT NULL DEFAULT"
                    + " 0,stats TEXT NOT NULL,iv_values TEXT NOT NULL,ev_values TEXT NOT NULL,moves"
                    + " TEXT NOT NULL)");
        if (db.query("PRAGMA table_info(pokemon)").stream()
                .noneMatch(row -> "friend_value".equals(row[1])))
            db.execute("ALTER TABLE pokemon ADD COLUMN friend_value INTEGER NOT NULL DEFAULT 0");
        for (String column : List.of("has_hidden_ability", "is_alpha", "mark_status"))
            if (db.query("PRAGMA table_info(pokemon)").stream()
                    .noneMatch(row -> column.equals(row[1])))
                db.execute(
                        "ALTER TABLE pokemon ADD COLUMN " + column + " INTEGER NOT NULL DEFAULT 0");
        for (String column : List.of("moves_pp", "pp_up_times"))
            if (db.query("PRAGMA table_info(pokemon)").stream()
                    .noneMatch(row -> column.equals(row[1])))
                db.execute(
                        "ALTER TABLE pokemon ADD COLUMN " + column + " TEXT NOT NULL DEFAULT ''");
        for (String column :
                List.of(
                        "catch_address",
                        "catch_level",
                        "catch_region",
                        "catch_time",
                        "personality_value",
                        "origin_flags"))
            if (db.query("PRAGMA table_info(pokemon)").stream()
                    .noneMatch(row -> column.equals(row[1])))
                db.execute(
                        "ALTER TABLE pokemon ADD COLUMN "
                                + column
                                + " INTEGER NOT NULL DEFAULT "
                                + (column.equals("catch_address")
                                        ? Integer.MIN_VALUE
                                        : column.equals("catch_region") ? -1 : 0));
        if (db.query("PRAGMA table_info(pokemon)").stream()
                .noneMatch(row -> "ot_name".equals(row[1])))
            db.execute("ALTER TABLE pokemon ADD COLUMN ot_name TEXT NOT NULL DEFAULT ''");
        if (db.query("PRAGMA table_info(pokemon)").stream()
                .noneMatch(row -> "can_remember_moves".equals(row[1])))
            db.execute(
                    "ALTER TABLE pokemon ADD COLUMN can_remember_moves TEXT NOT NULL DEFAULT ''");
        for (String column :
                List.of(
                        "contests_category_values",
                        "normal_ribbon",
                        "contest_ribbon",
                        "particle_effects"))
            if (db.query("PRAGMA table_info(pokemon)").stream()
                    .noneMatch(row -> column.equals(row[1])))
                db.execute(
                        "ALTER TABLE pokemon ADD COLUMN " + column + " TEXT NOT NULL DEFAULT ''");
        if (db.query("PRAGMA table_info(pokemon)").stream()
                .noneMatch(row -> "selected_particle".equals(row[1])))
            db.execute(
                    "ALTER TABLE pokemon ADD COLUMN selected_particle INTEGER NOT NULL DEFAULT -1");
        if (db.query("PRAGMA table_info(pokemon)").stream()
                .noneMatch(row -> "ball_type".equals(row[1])))
            db.execute("ALTER TABLE pokemon ADD COLUMN ball_type INTEGER NOT NULL DEFAULT 3");
        if (db.query("PRAGMA table_info(pokemon)").stream()
                .noneMatch(row -> "is_secret".equals(row[1])))
            db.execute("ALTER TABLE pokemon ADD COLUMN is_secret INTEGER NOT NULL DEFAULT 0");
        if (db.query("PRAGMA table_info(pokemon)").stream().noneMatch(row -> "exp".equals(row[1])))
            db.execute("ALTER TABLE pokemon ADD COLUMN exp INTEGER NOT NULL DEFAULT -1");
        db.execute(
                "CREATE INDEX IF NOT EXISTS pokemon_owner_container ON"
                        + " pokemon(trainer_id,container_id,container_position)");
    }

    Containers load(String id) {
        // Legacy local snapshots omitted EXP. Repair once from the same creation threshold.
        for (var row :
                db.query(
                        "SELECT id,dex_id,level_value FROM pokemon WHERE trainer_id=? AND exp<0",
                        id)) {
            var template = species.get(n(row[1]));
            if (template != null)
                db.execute(
                        "UPDATE pokemon SET exp=? WHERE id=?",
                        PokemonExperience.atLevel(template.growthRate(), n(row[2])),
                        row[0]);
        }
        var pokemons =
                db
                        .query(
                                "SELECT"
                                    + " id,container_id,container_position,dex_id,form_type,gender,is_shiny,is_egg,name,level_value,current_hp,status,item,nature,ability,stats,iv_values,ev_values,moves,friend_value,has_hidden_ability,is_alpha,mark_status,moves_pp,pp_up_times,catch_address,catch_level,catch_region,catch_time,ot_name,personality_value,origin_flags,can_remember_moves,contests_category_values,normal_ribbon,contest_ribbon,particle_effects,selected_particle,ball_type,is_secret,exp"
                                    + " FROM pokemon WHERE trainer_id=? AND container_id IN (0,1)"
                                    + " ORDER BY container_id,container_position",
                                id)
                        .stream()
                        .map(
                                r ->
                                        new Pokemon(
                                                r[0],
                                                n(r[1]),
                                                n(r[2]),
                                                n(r[3]),
                                                n(r[4]),
                                                n(r[5]),
                                                n(r[6]) != 0,
                                                n(r[7]) != 0,
                                                r[8],
                                                n(r[9]),
                                                n(r[10]),
                                                n(r[11]),
                                                n(r[12]),
                                                n(r[13]),
                                                n(r[14]),
                                                values(r[15]),
                                                values(r[16]),
                                                values(r[17]),
                                                values(r[18]),
                                                n(r[19]),
                                                n(r[20]) != 0,
                                                n(r[21]) != 0,
                                                n(r[22]),
                                                r[23].isEmpty()
                                                        ? List.of(-1, -1, -1, -1)
                                                        : values(r[23]),
                                                r[24].isEmpty()
                                                        ? List.of(0, 0, 0, 0)
                                                        : values(r[24]),
                                                new Origin(
                                                        n(r[25]),
                                                        n(r[26]),
                                                        n(r[27]),
                                                        Long.parseLong(r[28]),
                                                        r[29],
                                                        n(r[30]),
                                                        (n(r[31]) & 16) != 0,
                                                        (n(r[31]) & 64) != 0,
                                                        r[32].isEmpty()
                                                                ? List.of()
                                                                : values(r[32])),
                                                new Honors(
                                                        r[33].isEmpty()
                                                                ? List.of(0, 0, 0, 0, 0)
                                                                : values(r[33]),
                                                        r[34].isEmpty() ? List.of() : values(r[34]),
                                                        r[35].isEmpty()
                                                                ? List.of(0, 0, 0, 0, 0)
                                                                : values(r[35]),
                                                        r[36].isEmpty() ? List.of() : values(r[36]),
                                                        n(r[37])),
                                                n(r[38]),
                                                n(r[39]) != 0,
                                                n(r[40])))
                        .toList();
        return new Containers(pokemons, BASE_BOXES);
    }

    Containers release(String owner, List<String> ids) {
        if (ids.isEmpty() || ids.size() > BOX_SIZE || new HashSet<>(ids).size() != ids.size())
            reject();
        db.transaction(
                () -> {
                    var current = load(owner);
                    for (String id : ids) {
                        var p =
                                current.pokemons().stream()
                                        .filter(v -> v.id().equals(id))
                                        .findFirst()
                                        .orElse(null);
                        if (p == null
                                || p.container() != PC
                                || p.egg()
                                || p.species() == 492 && p.form() == 1) reject();
                    }
                    for (String id : ids)
                        db.execute(
                                "UPDATE pokemon SET container_id=8,container_position=0 WHERE"
                                        + " trainer_id=? AND id=? AND container_id=0",
                                owner,
                                id);
                });
        return load(owner);
    }

    Containers selectParticle(String owner, String id, int particle) {
        db.transaction(
                () -> {
                    var p =
                            load(owner).pokemons().stream()
                                    .filter(v -> v.id().equals(id))
                                    .findFirst()
                                    .orElseThrow(
                                            () ->
                                                    new GameService.Rejected(
                                                            GameService.Failure.NOT_FOUND));
                    if (p.egg()
                            || particle == -3 && p.availableParticles().isEmpty()
                            || particle != -1
                                    && particle != -3
                                    && !p.availableParticles().contains(particle)) reject();
                    db.execute(
                            "UPDATE pokemon SET selected_particle=? WHERE trainer_id=? AND id=?",
                            particle,
                            owner,
                            id);
                });
        return load(owner);
    }

    /** ChangePokemonMovePosPacket swaps the entire move slot, including PP and PP Ups. */
    Containers moveMove(String owner, String id, int from, int to) {
        if (from < 0 || from > 3 || to < 0 || to > 3 || from == to) reject();
        db.transaction(
                () -> {
                    var p =
                            load(owner).pokemons().stream()
                                    .filter(v -> v.id().equals(id))
                                    .findFirst()
                                    .orElseThrow(
                                            () ->
                                                    new GameService.Rejected(
                                                            GameService.Failure.NOT_FOUND));
                    var moves = new ArrayList<>(p.moves());
                    var pp = new ArrayList<>(p.pp());
                    var ups = new ArrayList<>(p.ppUps());
                    if (p.egg() || moves.get(from) == 0 || moves.get(to) == 0) reject();
                    Collections.swap(moves, from, to);
                    Collections.swap(pp, from, to);
                    Collections.swap(ups, from, to);
                    db.execute(
                            "UPDATE pokemon SET moves=?,moves_pp=?,pp_up_times=? WHERE id=? AND"
                                    + " trainer_id=?",
                            encoded(moves),
                            encoded(pp),
                            encoded(ups),
                            id,
                            owner);
                });
        return load(owner);
    }

    Containers setSetting(
            String owner,
            String id,
            GameService.PokemonSetting setting,
            int index,
            int value,
            Map<Integer, GameService.SpeciesTemplate> species) {
        db.transaction(
                () -> {
                    var p =
                            load(owner).pokemons().stream()
                                    .filter(v -> v.id().equals(id))
                                    .findFirst()
                                    .orElseThrow(
                                            () ->
                                                    new GameService.Rejected(
                                                            GameService.Failure.NOT_FOUND));
                    var template = species.get(p.species());
                    if (template == null || setting == null) reject();
                    var evs = new ArrayList<>(p.evs());
                    var ivs = new ArrayList<>(p.ivs());
                    int nature = p.nature(),
                            gender = p.gender(),
                            ability = p.ability(),
                            level = p.level(),
                            speciesId = p.species(),
                            form = p.form();
                    boolean hidden = p.hiddenAbility();
                    switch (setting) {
                        case STATUS -> {
                            // Reference PokemonStatusType stores the persistent status byte;
                            // fainting is HP=0.
                            if (p.egg()
                                    || index != 0
                                    || !List.of(-1, 0, 7, 8, 16, 32, 64, -128).contains(value))
                                reject();
                            int hp = value == -1 ? 0 : p.hp() > 0 ? p.hp() : p.maxHp();
                            db.execute(
                                    "UPDATE pokemon SET status=?,current_hp=? WHERE id=? AND"
                                            + " trainer_id=?",
                                    value == -1 ? 0 : value,
                                    hp,
                                    id,
                                    owner);
                            return;
                        }
                        case LEVEL -> {
                            if (p.egg() || index != 0 || value < 1 || value > 100) reject();
                            level = value;
                        }
                        case EVOLUTION -> {
                            var links =
                                    species.values().stream()
                                            .collect(
                                                    java.util.stream.Collectors.toMap(
                                                            GameService.SpeciesTemplate::id,
                                                            GameService.SpeciesTemplate
                                                                    ::evolutions));
                            if (p.egg()
                                    || index != 0
                                    || value == p.species()
                                    || !PokemonEvolutionChain.family(p.species(), links)
                                            .contains(value)
                                    || !species.containsKey(value)) reject();
                            var target = species.get(value);
                            speciesId = value;
                            form = 0;
                            int abilitySlot =
                                    p.hiddenAbility()
                                            ? 2
                                            : template.abilities().indexOf(p.ability());
                            if (abilitySlot < 0
                                    || abilitySlot >= target.abilities().size()
                                    || target.abilities().get(abilitySlot) <= 0) abilitySlot = 0;
                            ability = target.abilities().get(abilitySlot);
                            hidden = abilitySlot == 2;
                            int ratio = target.genderRatio();
                            gender =
                                    ratio == 255
                                            ? -1
                                            : ratio == 254
                                                    ? 1
                                                    : ratio == 0
                                                            ? 0
                                                            : gender < 0
                                                                    ? ((p.origin().personality()
                                                                                            & 255)
                                                                                    >= ratio
                                                                            ? 0
                                                                            : 1)
                                                                    : gender;
                            template = target;
                        }
                        case EV -> {
                            if (index < -1 || index > 5 || value < 0 || value > 252) reject();
                            if (index < 0) Collections.fill(evs, value);
                            else evs.set(index, value);
                        }
                        case IV -> {
                            if (index != -1 || value < -1 || value > 31) reject();
                            var random = new Random();
                            for (int i = 0; i < 6; i++)
                                ivs.set(i, value == -1 ? random.nextInt(31) : value);
                        }
                        case OT -> {
                            if (index != 0 || value < 0 || value > 3) reject();
                            String trainer =
                                    switch (value) {
                                        case 0 -> "{STRING_5685}";
                                        case 1 -> "{STRING_5684}";
                                        case 2 -> "unknown";
                                        default ->
                                                db.query(
                                                                "SELECT name FROM characters WHERE"
                                                                        + " id=?",
                                                                owner)
                                                        .get(0)[0];
                                    };
                            if (trainer.isBlank() || trainer.length() > 32) reject();
                            db.execute(
                                    "UPDATE pokemon SET ot_name=? WHERE id=? AND trainer_id=?",
                                    trainer,
                                    id,
                                    owner);
                            return;
                        }
                        case BALL -> {
                            if (index != 0 || value < 0 || value > 24) reject();
                            db.execute(
                                    "UPDATE pokemon SET ball_type=? WHERE id=? AND trainer_id=?",
                                    value,
                                    id,
                                    owner);
                            return;
                        }
                        case FRIENDSHIP -> {
                            if (index != 0 || value < 0 || value > 255) reject();
                            db.execute(
                                    "UPDATE pokemon SET friend_value=? WHERE id=? AND trainer_id=?",
                                    value,
                                    id,
                                    owner);
                            return;
                        }
                        case PARTICLE -> {
                            if (index != -1 && index != 0 || value < 0 || value > 38) reject();
                            var particles = new TreeSet<>(p.honors().particles());
                            if (index == -1) for (int i = 0; i <= 38; i++) particles.add(i);
                            else particles.add(value);
                            db.execute(
                                    "UPDATE pokemon SET particle_effects=? WHERE id=? AND"
                                            + " trainer_id=?",
                                    encoded(new ArrayList<>(particles)),
                                    id,
                                    owner);
                            return;
                        }
                        case RIBBON -> {
                            var ribbons = new TreeSet<>(p.honors().ribbons());
                            var contest = new ArrayList<>(p.honors().contestRibbons());
                            if (index == -2 && value == 0) {
                                ribbons.addAll(
                                        List.of(
                                                16, 22, 23, 20, 27, 28, 29, 30, 31, 32, 33, 24, 25,
                                                26, 34));
                                Collections.fill(contest, 4);
                            } else if (index == -1 && value == 0) {
                                ribbons.clear();
                                Collections.fill(contest, 0);
                            } else if (index == 0 && value >= 0 && value <= 34) ribbons.add(value);
                            else if (index >= 1 && index <= 5 && value >= 1 && value <= 4)
                                contest.set(index - 1, Math.max(contest.get(index - 1), value));
                            else reject();
                            db.execute(
                                    "UPDATE pokemon SET normal_ribbon=?,contest_ribbon=? WHERE id=?"
                                            + " AND trainer_id=?",
                                    encoded(new ArrayList<>(ribbons)),
                                    encoded(contest),
                                    id,
                                    owner);
                            return;
                        }
                        case NATURE -> {
                            if (value < 0 || value > 24) reject();
                            nature = value;
                        }
                        case GENDER -> {
                            int ratio = template.genderRatio();
                            if (value < 0
                                    || value > 1
                                    || ratio == 255
                                    || ratio == 0 && value != 0
                                    || ratio == 254 && value != 1) reject();
                            gender = value;
                        }
                        case ABILITY -> {
                            if (value <= 0
                                    || index < 0
                                    || index >= template.abilities().size()
                                    || template.abilities().get(index) != value) reject();
                            ability = value;
                            hidden = index == 2;
                        }
                    }
                    var stats = new ArrayList<>(p.stats());
                    if (setting == GameService.PokemonSetting.EV
                            || setting == GameService.PokemonSetting.NATURE
                            || setting == GameService.PokemonSetting.IV
                            || setting == GameService.PokemonSetting.LEVEL
                            || setting == GameService.PokemonSetting.EVOLUTION)
                        for (int i = 0; i < 6; i++) {
                            int stat =
                                    ((2 * template.stats().get(i) + ivs.get(i) + evs.get(i) / 4)
                                                            * level)
                                                    / 100
                                            + (i == 0 ? level + 10 : 5);
                            int up = NATURE_STATS[nature / 5], down = NATURE_STATS[nature % 5];
                            if (i != 0 && up != down)
                                stat = (int) (stat * (i == up ? 1.1 : i == down ? .9 : 1));
                            if (i == 0 && speciesId == 292) stat = 1;
                            stats.set(i, stat);
                        }
                    if (setting == GameService.PokemonSetting.LEVEL
                            || setting == GameService.PokemonSetting.EVOLUTION) {
                        // Reference ItemUseService keeps fainted Pokemon fainted and adjusts
                        // positive HP on growth.
                        int hp =
                                p.hp() == 0
                                        ? 0
                                        : Math.min(
                                                stats.get(0),
                                                p.hp() + Math.max(0, stats.get(0) - p.maxHp()));
                        int exp =
                                setting == GameService.PokemonSetting.EVOLUTION
                                                && template.growthRate()
                                                        == species.get(p.species()).growthRate()
                                                && p.experience() >= 0
                                        ? p.experience()
                                        : PokemonExperience.atLevel(template.growthRate(), level);
                        db.execute(
                                "UPDATE pokemon SET"
                                    + " dex_id=?,form_type=?,level_value=?,exp=?,gender=?,ability=?,has_hidden_ability=?,stats=?,current_hp=?"
                                    + " WHERE id=? AND trainer_id=?",
                                speciesId,
                                form,
                                level,
                                exp,
                                gender,
                                ability,
                                hidden ? 1 : 0,
                                encoded(stats),
                                hp,
                                id,
                                owner);
                        return;
                    }
                    db.execute(
                            "UPDATE pokemon SET"
                                + " ev_values=?,iv_values=?,nature=?,gender=?,ability=?,has_hidden_ability=?,stats=?,current_hp=?"
                                + " WHERE id=? AND trainer_id=?",
                            encoded(evs),
                            encoded(ivs),
                            nature,
                            gender,
                            ability,
                            hidden ? 1 : 0,
                            encoded(stats),
                            Math.min(p.hp(), stats.get(0)),
                            id,
                            owner);
                });
        return load(owner);
    }

    /** ClientOpcode029RequestPacket: replace the five mark bits on an owned Pokemon. */
    Containers setMarks(String owner, String pokemonId, int marks) {
        if (marks < 0
                || marks > 31
                || load(owner).pokemons().stream().noneMatch(p -> p.id().equals(pokemonId)))
            throw new GameService.Rejected(GameService.Failure.NOT_FOUND);
        db.execute(
                "UPDATE pokemon SET mark_status=? WHERE id=? AND trainer_id=?",
                marks,
                pokemonId,
                owner);
        return load(owner);
    }

    Containers setFlag(
            String owner, String pokemonId, GameService.PokemonFlag flag, boolean enabled) {
        if (flag == null
                || load(owner).pokemons().stream().noneMatch(p -> p.id().equals(pokemonId)))
            throw new GameService.Rejected(GameService.Failure.NOT_FOUND);
        String column =
                switch (flag) {
                    case SHINY -> "is_shiny";
                    case SECRET_SHINY -> "is_secret";
                    case HIDDEN_ABILITY -> "has_hidden_ability";
                    case ALPHA -> "is_alpha";
                };
        db.execute(
                "UPDATE pokemon SET " + column + "=? WHERE id=? AND trainer_id=?",
                enabled ? 1 : 0,
                pokemonId,
                owner);
        return load(owner);
    }

    private static int n(String v) {
        return Integer.parseInt(v);
    }

    private static List<Integer> values(String value) {
        return Arrays.stream(value.split(",", -1)).map(Integer::valueOf).toList();
    }

    private record Slot(int container, int position) {}

    private static Slot source(Move m) {
        return new Slot(m.oldContainer(), m.oldPosition());
    }

    private static Slot target(Move m) {
        return new Slot(m.newContainer(), m.newPosition());
    }

    private static boolean valid(Slot s, int boxes) {
        return s.position >= 0
                && (s.container == PARTY
                        ? s.position < PARTY_SIZE
                        : s.container == PC && s.position < boxes * BOX_SIZE);
    }

    private static void reject() {
        throw new Rejected(Failure.INVALID_MOVE);
    }

    Containers move(String id, List<Move> changes) {
        if (changes == null || changes.isEmpty() || changes.size() > 127) reject();
        db.transaction(
                () -> {
                    var current = load(id);
                    var occupied = new HashMap<Slot, Pokemon>();
                    var original = new HashMap<String, Slot>();
                    for (var p : current.pokemons()) {
                        var s = new Slot(p.container(), p.position());
                        if (occupied.put(s, p) != null || !valid(s, current.boxes())) reject();
                        original.put(p.id(), s);
                    }
                    var finalSlots = new HashMap<>(original);
                    var sources = new HashSet<Slot>();
                    var targets = new HashSet<Slot>();
                    var moved = new HashSet<String>();
                    for (var m : changes) {
                        if (m == null) reject();
                        var from = source(m);
                        var to = target(m);
                        var p = occupied.get(from);
                        if (!valid(from, current.boxes())
                                || !valid(to, current.boxes())
                                || from.equals(to)
                                || p == null
                                || !sources.add(from)
                                || !targets.add(to)
                                || !moved.add(p.id())) reject();
                        finalSlots.put(p.id(), to);
                    }
                    // Occupied targets outside the same batch swap back to the corresponding
                    // source.
                    for (var m : changes) {
                        var p = occupied.get(target(m));
                        if (p != null && !moved.contains(p.id())) finalSlots.put(p.id(), source(m));
                    }
                    if (new HashSet<>(finalSlots.values()).size() != finalSlots.size()) reject();
                    if (original.values().stream().anyMatch(s -> s.container == PARTY)
                            && finalSlots.values().stream().noneMatch(s -> s.container == PARTY))
                        throw new Rejected(Failure.LAST_PARTY_POKEMON);
                    // After a deposit removes party members, retain their relative order and close
                    // the gaps.
                    long beforeParty =
                            original.values().stream().filter(s -> s.container == PARTY).count();
                    var remaining =
                            finalSlots.entrySet().stream()
                                    .filter(e -> e.getValue().container == PARTY)
                                    .sorted(Comparator.comparingInt(e -> e.getValue().position))
                                    .map(Map.Entry::getKey)
                                    .toList();
                    if (remaining.size() < beforeParty)
                        for (int i = 0; i < remaining.size(); i++)
                            finalSlots.put(remaining.get(i), new Slot(PARTY, i));
                    for (var e : finalSlots.entrySet())
                        if (!e.getValue().equals(original.get(e.getKey())))
                            db.execute(
                                    "UPDATE pokemon SET container_id=?,container_position=? WHERE"
                                            + " id=? AND trainer_id=?",
                                    e.getValue().container,
                                    e.getValue().position,
                                    e.getKey(),
                                    id);
                });
        return load(id);
    }

    Containers sort(String id, int box) {
        db.transaction(
                () -> {
                    var current = load(id);
                    if (box < 0 || box >= current.boxes()) reject();
                    var values =
                            current.pokemons().stream()
                                    .filter(
                                            p ->
                                                    p.container() == PC
                                                            && p.position() / BOX_SIZE == box)
                                    .sorted(
                                            Comparator.comparingInt(Pokemon::level)
                                                    .thenComparingInt(Pokemon::position)
                                                    .thenComparing(Pokemon::id))
                                    .toList();
                    for (int i = 0; i < values.size(); i++)
                        db.execute(
                                "UPDATE pokemon SET container_position=? WHERE id=? AND"
                                        + " trainer_id=?",
                                box * BOX_SIZE + i,
                                values.get(i).id(),
                                id);
                });
        return load(id);
    }

    Pokemon add(String id, GameService.SpeciesTemplate species, int level, Random random) {
        if (species == null || species.id() < 1 || species.id() > 649)
            throw new IllegalArgumentException("Unknown Pokemon index id.");
        if (level < 1 || level > 100)
            throw new IllegalArgumentException("Pokemon level must be an integer from 1 to 100.");
        var added = new Pokemon[1];
        db.transaction(
                () -> {
                    var current = load(id);
                    int container = PARTY, position = 0;
                    while (position < PARTY_SIZE && current.at(PARTY, position) != null) position++;
                    if (position == PARTY_SIZE) {
                        container = PC;
                        position = 0;
                        while (position < current.boxes() * BOX_SIZE
                                && current.at(PC, position) != null) position++;
                    }
                    if (container == PC && position >= current.boxes() * BOX_SIZE)
                        throw new IllegalStateException("The target has no free party or PC slot.");
                    var p = createWildPokemon(species, level, container, position, random);
                    insert(id, p);
                    added[0] =
                            load(id).pokemons().stream()
                                    .filter(v -> v.id().equals(p.id()))
                                    .findFirst()
                                    .orElseThrow();
                });
        return added[0];
    }

    private void insert(String id, Pokemon p) {
        int level = p.level();
        db.execute(
                "INSERT INTO"
                    + " pokemon(id,trainer_id,container_id,container_position,dex_id,form_type,gender,is_shiny,is_egg,name,level_value,current_hp,status,item,nature,ability,stats,iv_values,ev_values,moves)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                p.id(),
                id,
                p.container(),
                p.position(),
                p.species(),
                p.form(),
                p.gender(),
                p.shiny() ? 1 : 0,
                p.egg() ? 1 : 0,
                p.name(),
                p.level(),
                p.hp(),
                p.status(),
                p.item(),
                p.nature(),
                p.ability(),
                encoded(p.stats()),
                encoded(p.ivs()),
                encoded(p.evs()),
                encoded(p.moves()));
        var owner = db.query("SELECT name,region FROM characters WHERE id=?", id);
        var regions =
                db.query("SELECT region FROM character_current_region WHERE character_id=?", id);
        int region =
                regions.isEmpty()
                        ? (owner.isEmpty() ? -1 : Integer.parseInt(owner.get(0)[1]))
                        : Integer.parseInt(regions.get(0)[0]);
        db.execute(
                "UPDATE pokemon SET"
                        + " catch_level=?,catch_region=?,catch_time=?,ot_name=?,personality_value=?"
                        + " WHERE id=?",
                level,
                region,
                System.currentTimeMillis() / 1000,
                owner.isEmpty() ? "" : owner.get(0)[0],
                p.origin().personality(),
                p.id());
        db.execute("UPDATE pokemon SET exp=? WHERE id=?", p.experience(), p.id());
    }

    /**
     * User-requested local tool; creation and rarity fields follow
     * PokemonManager/PokemonAttributeService.
     */
    Containers randomBox(
            String id, Collection<GameService.SpeciesTemplate> registry, Random random) {
        var candidates =
                registry.stream()
                        .filter(s -> s.id() > 0 && s.id() <= 649)
                        .sorted(Comparator.comparingInt(GameService.SpeciesTemplate::id))
                        .toList();
        if (candidates.isEmpty()) throw new IllegalStateException("No loaded Pokemon templates");
        db.transaction(
                () -> {
                    var current = load(id);
                    var available = new ArrayList<Integer>();
                    // PokemonContainerService uses absolute PC positions across all boxes.
                    for (int position = 0;
                            position < current.boxes() * BOX_SIZE && available.size() < BOX_SIZE;
                            position++)
                        if (current.at(PC, position) == null) available.add(position);
                    if (available.isEmpty())
                        throw new IllegalStateException("No free Pokemon PC slot");
                    for (int position : available) {
                        var species = candidates.get(random.nextInt(candidates.size()));
                        var p =
                                createWildPokemon(
                                        species, random.nextInt(100) + 1, PC, position, random);
                        insert(id, p);
                        // A secret shiny retains the shiny bit; alpha is independent. No custom
                        // shiny particle ID.
                        db.execute(
                                "UPDATE pokemon SET"
                                    + " is_shiny=1,is_secret=?,is_alpha=?,selected_particle=0 WHERE"
                                    + " id=? AND trainer_id=?",
                                random.nextBoolean() ? 1 : 0,
                                random.nextBoolean() ? 1 : 0,
                                p.id(),
                                id);
                    }
                });
        return load(id);
    }

    private static String encoded(List<Integer> values) {
        return values.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
    }

    /**
     * PokemonManager.createWildPokemon / PokemonDexData.getPokemonAbilityValue, using the loaded
     * registry.
     */
    // PokemonStatType's internal order is HP, Atk, Def, Speed, SpA, SpD; snapshots use display
    // order.
    private static final int[] NATURE_STATS = {1, 2, 5, 3, 4};

    static PokemonData.Pokemon createWildPokemon(
            GameService.SpeciesTemplate species,
            int level,
            int container,
            int position,
            Random random) {
        int[] rawIvs = new int[6];
        for (int i = 0; i < 6; i++) rawIvs[i] = random.nextInt(31);
        int personality = random.nextInt(Integer.MAX_VALUE),
                nature = Math.floorMod(personality, 25);
        var ivs = List.of(rawIvs[0], rawIvs[1], rawIvs[2], rawIvs[4], rawIvs[5], rawIvs[3]);
        var stats = new ArrayList<Integer>();
        for (int i = 0; i < 6; i++) {
            int value =
                    ((species.stats().get(i) * 2 + ivs.get(i) + (i == 0 ? 100 : 0)) * level) / 100
                            + (i == 0 ? 10 : 5);
            int up = NATURE_STATS[nature / 5], down = NATURE_STATS[nature % 5];
            double multiplier = up == down ? 1 : i == up ? 1.1 : i == down ? .9 : 1;
            stats.add((int) (value * multiplier));
        }
        var moves =
                new ArrayList<>(
                        species.moves().stream()
                                .filter(m -> m.level() <= level)
                                .sorted(
                                        Comparator.comparingInt(GameService.LearnedMove::level)
                                                .reversed())
                                .limit(4)
                                .map(GameService.LearnedMove::id)
                                .toList());
        while (moves.size() < 4) moves.add(0);
        int ratio = species.genderRatio();
        // ActivePokemon.Dg0 handles the male/female-only and genderless species sent by the server.
        int gender =
                ratio == 255
                        ? -1
                        : ratio == 254 ? 1 : ratio == 0 ? 0 : (personality & 255) >= ratio ? 0 : 1;
        // The supplied reference server is explicitly in its wild-rarity test mode (denominator 1).
        boolean shiny = random.nextInt(1) == 0;
        if (shiny) random.nextInt(2);
        return new PokemonData.Pokemon(
                UUID.randomUUID().toString(),
                container,
                position,
                species.id(),
                0,
                gender,
                shiny,
                false,
                "",
                level,
                stats.get(0),
                0,
                0,
                nature,
                species.ability(),
                stats,
                ivs,
                List.of(0, 0, 0, 0, 0, 0),
                moves,
                0,
                false,
                false,
                0,
                List.of(-1, -1, -1, -1),
                List.of(0, 0, 0, 0),
                new Origin(Integer.MIN_VALUE, 0, -1, 0, "", personality, false, false),
                Honors.empty(),
                3,
                false,
                PokemonExperience.atLevel(species.growthRate(), level));
    }
}
