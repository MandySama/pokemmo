package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import java.util.Map;

/** SQLite adaptation of reference PokemonItemService.updatePokemonItem (C2S 0x0F). */
final class PokemonItemStore {
    private final Database db;

    PokemonItemStore(Database db) { this.db = db; }

    void update(String character, String pokemon, int container, int requested,
                Map<Integer, GameService.ItemTemplate> templates) {
        if ((container != PokemonData.PARTY && container != PokemonData.PC)
                || requested < -1 || requested > Short.MAX_VALUE)
            throw new IllegalArgumentException("Invalid held item request");
        int next = Math.max(0, requested);
        var template = templates.get(next);
        if (next > 0 && (template == null || !template.holdable()))
            throw new IllegalArgumentException("Item cannot be held");
        db.transaction(() -> {
            var rows = db.query("SELECT item FROM pokemon WHERE id=? AND trainer_id=? AND container_id=?",
                    pokemon, character, Integer.toString(container));
            if (rows.size() != 1) throw new GameService.Rejected(GameService.Failure.NOT_FOUND);
            int previous = Integer.parseInt(rows.get(0)[0]);
            if (previous == next) return;
            String[] selected = null;
            if (next > 0) {
                var owned = db.query("SELECT item_id,item_amount FROM owned_item WHERE owner_id=?"
                        + " AND inventory_id=1 AND item_index_id=? AND region=-1 AND item_amount>0"
                        + " ORDER BY item_id LIMIT 1", character, Integer.toString(next));
                if (owned.isEmpty()) throw new GameService.Rejected(GameService.Failure.NOT_FOUND);
                selected = owned.get(0);
            }
            // Return before consuming, matching the reference; transaction rolls both back on failure.
            if (previous > 0) returnItem(character, previous);
            if (selected != null) {
                if (Integer.parseInt(selected[1]) == 1)
                    db.execute("DELETE FROM owned_item WHERE item_id=? AND owner_id=? AND inventory_id=1",
                            selected[0], character);
                else db.execute("UPDATE owned_item SET item_amount=item_amount-1 WHERE item_id=?"
                        + " AND owner_id=? AND inventory_id=1", selected[0], character);
            }
            db.execute("UPDATE pokemon SET item=? WHERE id=? AND trainer_id=? AND container_id=?",
                    next, pokemon, character, container);
        });
    }

    private void returnItem(String character, int item) {
        var rows = db.query("SELECT item_id,item_amount FROM owned_item WHERE owner_id=?"
                + " AND inventory_id=1 AND item_index_id=? AND region=-1 ORDER BY item_id LIMIT 1",
                character, Integer.toString(item));
        if (rows.isEmpty()) {
            db.execute("INSERT INTO owned_item(item_id,owner_id,item_index_id,item_amount,inventory_id,region)"
                    + " VALUES (?,?,?,1,1,-1)", java.util.UUID.randomUUID().toString(), character, item);
        } else {
            var row = rows.get(0);
            if (Integer.parseInt(row[1]) >= Short.MAX_VALUE)
                throw new IllegalArgumentException("Returned held item stack is full");
            db.execute("UPDATE owned_item SET item_amount=item_amount+1 WHERE item_id=? AND owner_id=?",
                    row[0], character);
        }
    }
}
