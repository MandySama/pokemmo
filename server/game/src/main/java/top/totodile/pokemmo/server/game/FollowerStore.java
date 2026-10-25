package top.totodile.pokemmo.server.game;

import static top.totodile.pokemmo.server.game.PokemonData.*;

import top.totodile.pokemmo.db.Database;

/** CharacterService.updateCharacterFollower / ChangeFllowPokemonPacket. */
final class FollowerStore {
    private final Database db;

    FollowerStore(Database db) {
        this.db = db;
    }

    static void initialize(Database db) {
        db.execute(
                "CREATE TABLE IF NOT EXISTS character_follower (character_id TEXT PRIMARY KEY"
                    + " REFERENCES characters(id) ON DELETE CASCADE,species INTEGER NOT NULL,form"
                    + " INTEGER NOT NULL,gender INTEGER NOT NULL,shiny INTEGER NOT NULL)");
        if (db.query("PRAGMA table_info(character_follower)").stream()
                .noneMatch(row -> "is_alpha".equals(row[1])))
            db.execute(
                    "ALTER TABLE character_follower ADD COLUMN is_alpha INTEGER NOT NULL DEFAULT"
                            + " 0");
    }

    GameService.Follower load(String id) {
        var rows =
                db.query(
                        "SELECT species,form,gender,shiny,is_alpha FROM character_follower WHERE"
                                + " character_id=?",
                        id);
        if (rows.isEmpty()) return GameService.Follower.none();
        var r = rows.get(0);
        return new GameService.Follower(
                Integer.parseInt(r[0]),
                Integer.parseInt(r[1]),
                Integer.parseInt(r[2]),
                Integer.parseInt(r[3]) != 0,
                Integer.parseInt(r[4]) != 0);
    }

    GameService.Follower select(String owner, int type, String object, int position) {
        GameService.Follower value;
        if (type == 2) {
            if (object != null || position != 0) throw new Rejected(Failure.INVALID_MOVE);
            value = GameService.Follower.none();
        } else {
            var party = new PokemonStore(db).load(owner);
            Pokemon pokemon =
                    type == 0 && position >= 0 && position < 6
                            ? party.at(PARTY, position)
                            : type == 1
                                    ? party.pokemons().stream()
                                            .filter(
                                                    p ->
                                                            p.container() == PARTY
                                                                    && p.id().equals(object))
                                            .findFirst()
                                            .orElse(null)
                                    : null;
            if (pokemon == null || pokemon.egg() || pokemon.form() > 31)
                throw new Rejected(Failure.INVALID_MOVE);
            value =
                    new GameService.Follower(
                            pokemon.species(),
                            pokemon.form(),
                            pokemon.gender(),
                            pokemon.shiny(),
                            pokemon.alpha());
        }
        db.execute(
                "INSERT OR REPLACE INTO"
                    + " character_follower(character_id,species,form,gender,shiny,is_alpha) VALUES"
                    + " (?,?,?,?,?,?)",
                owner,
                value.species(),
                value.form(),
                value.gender(),
                value.shiny() ? 1 : 0,
                value.alpha() ? 1 : 0);
        return value;
    }
}
