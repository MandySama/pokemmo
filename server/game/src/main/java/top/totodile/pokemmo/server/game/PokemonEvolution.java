package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import java.util.*;

/** BW EvolutionMethod.LEVEL / BwEvolutionScene: a deferred, cancellable evolution, not a stat-only swap. */
final class PokemonEvolution {
    record Pending(String pokemon, int source, int target, int level) {}
    private static final int EVERSTONE = 5229; // Reference resource/item/Item.jsonc.

    static void initialize(Database db) {
        db.execute("CREATE TABLE IF NOT EXISTS character_evolution(character_id TEXT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,pokemon_id TEXT NOT NULL REFERENCES pokemon(id) ON DELETE CASCADE,source INTEGER NOT NULL,target INTEGER NOT NULL,level INTEGER NOT NULL,PRIMARY KEY(character_id,pokemon_id))");
        db.execute("CREATE TABLE IF NOT EXISTS character_evolution_migration(character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE)");
    }

    private static int target(PokemonData.Pokemon p, Map<Integer,GameService.SpeciesTemplate> templates) {
        var source = templates.get(p.species());
        if (source == null || p.egg() || p.form() != 0 || p.item() == EVERSTONE) return 0;
        for (var rule : source.evolutionRules()) {
            boolean matches = switch (rule.method()) {
                case 4 -> p.level() >= rule.parameter();
                case 23 -> p.gender() == 0 && p.level() >= rule.parameter();
                case 24 -> p.gender() == 1 && p.level() >= rule.parameter();
                default -> false; // Stone, trade and environmental methods require their own trigger.
            };
            if (matches) return rule.target();
        }
        return 0;
    }

    static void enqueue(Database db, String owner, PokemonData.Pokemon p,
            Map<Integer,GameService.SpeciesTemplate> templates) {
        int next = target(p, templates);
        if (next == 0 || p.container() != PokemonData.PARTY) return;
        db.execute("INSERT OR IGNORE INTO character_evolution VALUES (?,?,?,?,?)",
                owner, p.id(), p.species(), next, p.level());
    }

    /** One compatibility pass for pre-feature saves. No Pokemon changes until the scene completes. */
    static void legacy(Database db, String owner, Map<Integer,GameService.SpeciesTemplate> templates) {
        if (templates.values().stream().noneMatch(t -> !t.evolutionRules().isEmpty())
                || !db.query("SELECT character_id FROM character_evolution_migration WHERE character_id=?",owner).isEmpty()) return;
        db.transaction(() -> {
            for (var p : new PokemonStore(db,templates).load(owner).pokemons()) enqueue(db,owner,p,templates);
            db.execute("INSERT INTO character_evolution_migration VALUES (?)",owner);
        });
    }

    static Pending first(Database db, String owner, Map<Integer,GameService.SpeciesTemplate> templates) {
        var party = new PokemonStore(db,templates).load(owner).pokemons();
        for (var row : db.query("SELECT pokemon_id,source,target,level FROM character_evolution WHERE character_id=? ORDER BY rowid",owner)) {
            var p = party.stream().filter(mon -> mon.id().equals(row[0])).findFirst().orElse(null);
            if (p == null || p.species() != Integer.parseInt(row[1])) {
                db.execute("DELETE FROM character_evolution WHERE character_id=? AND pokemon_id=?",owner,row[0]);
                continue;
            }
            // An old all-fainted save can still use the nurse; queued evolution resumes afterwards.
            if (p.container() == PokemonData.PARTY && p.hp() > 0 && templates.containsKey(Integer.parseInt(row[2]))
                    && target(p,templates) == Integer.parseInt(row[2]))
                return new Pending(row[0],Integer.parseInt(row[1]),Integer.parseInt(row[2]),Integer.parseInt(row[3]));
        }
        return null;
    }

    static void choose(Database db, String owner, Pending pending, int answer,
            Map<Integer,GameService.SpeciesTemplate> templates) {
        if (answer != 0 && answer != 1) throw new IllegalArgumentException("Invalid evolution result");
        db.transaction(() -> {
            if (!Objects.equals(first(db,owner,templates),pending)) throw new IllegalStateException("Stale evolution result");
            if (answer == 1) {
                var p = new PokemonStore(db,templates).load(owner).pokemons().stream()
                        .filter(mon -> mon.id().equals(pending.pokemon())).findFirst().orElseThrow();
                var before = templates.get(p.species()); var after = templates.get(pending.target());
                var stats = BattleProgress.stats(p,after,p.level(),p.evs());
                int abilitySlot = p.hiddenAbility() ? 2 : Math.max(0,before.abilities().indexOf(p.ability()));
                int ability = after.abilities().get(Math.min(abilitySlot,after.abilities().size()-1));
                int hp = p.hp() == 0 ? 0 : Math.min(stats.get(0),p.hp()+stats.get(0)-p.maxHp());
                db.execute("UPDATE pokemon SET dex_id=?,ability=?,stats=?,current_hp=? WHERE trainer_id=? AND id=?",
                        after.id(),ability,encode(stats),hp,owner,p.id());
                // Evolution preserves nickname, identity, origin, IV/EV, EXP, held item and old move PP.
                BattleProgress.learn(db,owner,p,after.moves().stream()
                        .filter(m -> m.level() == 0 || m.level() == p.level()).toList());
                db.execute("INSERT INTO character_pokedex_entry VALUES (?,?,?) ON CONFLICT(character_id,species) DO UPDATE SET flags=flags|excluded.flags",
                        owner,after.id(),7|(p.alpha()?8:0));
            }
            db.execute("DELETE FROM character_evolution WHERE character_id=? AND pokemon_id=?",owner,pending.pokemon());
        });
        // No immediate second stage at the same level. Cancelled evolutions retry on the next level-up.
    }

    private static String encode(List<Integer> values) {
        return String.join(",",values.stream().map(String::valueOf).toList());
    }
}
