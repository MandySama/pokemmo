package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import top.totodile.pokemmo.server.login.LoginService.Selection;

import java.util.*;

/** SQLite adaptation of reference InventoryService / MailQueryService / MailDeleteService. */
final class PlayerDataStore {
    private final Database db;

    PlayerDataStore(Database db) {
        this.db = db;
    }

    static void initialize(Database db) {
        db.execute(
                "CREATE TABLE IF NOT EXISTS owned_item (item_id TEXT PRIMARY KEY, owner_id TEXT NOT"
                    + " NULL REFERENCES characters(id) ON DELETE CASCADE, item_index_id INTEGER NOT"
                    + " NULL, item_amount INTEGER NOT NULL CHECK(item_amount>0), inventory_id"
                    + " INTEGER NOT NULL DEFAULT 1, region INTEGER NOT NULL DEFAULT -1)");
        db.execute(
                "CREATE TABLE IF NOT EXISTS character_trainer (character_id TEXT PRIMARY KEY"
                    + " REFERENCES characters(id) ON DELETE CASCADE, battle_points INTEGER NOT NULL"
                    + " DEFAULT 0, played_seconds INTEGER NOT NULL DEFAULT 0, caught INTEGER NOT"
                    + " NULL DEFAULT 0, obedience INTEGER NOT NULL DEFAULT 10, badges INTEGER NOT"
                    + " NULL DEFAULT 0)");
        db.execute(
                "CREATE TABLE IF NOT EXISTS mail_message (mail_id TEXT PRIMARY KEY, sender_id TEXT"
                    + " NOT NULL REFERENCES characters(id) ON DELETE CASCADE, recipient_id TEXT NOT"
                    + " NULL REFERENCES characters(id) ON DELETE CASCADE, created_at INTEGER NOT"
                    + " NULL, title TEXT NOT NULL, body TEXT NOT NULL, is_read INTEGER NOT NULL"
                    + " DEFAULT 0, sender_deleted INTEGER NOT NULL DEFAULT 0, recipient_deleted"
                    + " INTEGER NOT NULL DEFAULT 0)");
    }

    void requireOwner(Selection selection, String id) {
        if (db.query(
                        "SELECT id FROM characters WHERE id=? AND account=? AND server=?",
                        id,
                        selection.session().username(),
                        selection.server().id())
                .isEmpty()) throw new GameService.Rejected(GameService.Failure.NOT_FOUND);
    }

    List<PlayerData.Item> inventory(String id) {
        var result = new ArrayList<PlayerData.Item>();
        for (var row :
                db.query(
                        "SELECT item_id,item_index_id,item_amount,region FROM owned_item WHERE"
                                + " owner_id=? AND inventory_id=1 ORDER BY item_index_id,item_id",
                        id))
            result.add(
                    new PlayerData.Item(
                            row[0],
                            Integer.parseInt(row[1]),
                            Integer.parseInt(row[2]),
                            Integer.parseInt(row[3])));
        return List.copyOf(result);
    }

    List<PlayerData.Item> destroyItem(
            String owner,
            String objectId,
            int amount,
            Map<Integer, GameService.ItemTemplate> templates) {
        if (amount < 1 || amount > Short.MAX_VALUE)
            throw new IllegalArgumentException("Invalid destroy amount");
        db.transaction(
                () -> {
                    var rows =
                            db.query(
                                    "SELECT item_index_id,item_amount FROM owned_item WHERE"
                                            + " item_id=? AND owner_id=? AND inventory_id=1",
                                    objectId,
                                    owner);
                    if (rows.size() != 1)
                        throw new GameService.Rejected(GameService.Failure.NOT_FOUND);
                    var row = rows.get(0);
                    var template = templates.get(Integer.parseInt(row[0]));
                    int available = Integer.parseInt(row[1]);
                    if (template == null || !template.destroyable() || amount > available)
                        throw new IllegalArgumentException("Item cannot be destroyed");
                    if (amount == available)
                        db.execute(
                                "DELETE FROM owned_item WHERE item_id=? AND owner_id=? AND"
                                        + " inventory_id=1",
                                objectId,
                                owner);
                    else
                        db.execute(
                                "UPDATE owned_item SET item_amount=item_amount-? WHERE item_id=?"
                                        + " AND owner_id=? AND inventory_id=1",
                                amount,
                                objectId,
                                owner);
                });
        return inventory(owner);
    }

    List<PlayerData.Item> replaceInventory(String owner, Map<Integer, Integer> items) {
        db.transaction(
                () -> {
                    db.execute("DELETE FROM owned_item WHERE owner_id=? AND inventory_id=1", owner);
                    for (var item : items.entrySet())
                        db.execute(
                                "INSERT INTO"
                                    + " owned_item(item_id,owner_id,item_index_id,item_amount,inventory_id,region)"
                                    + " VALUES (?,?,?,?,1,-1)",
                                UUID.randomUUID().toString(),
                                owner,
                                item.getKey(),
                                item.getValue());
                });
        return inventory(owner);
    }

    PlayerData.Item createItem(String owner, int itemId, int amount) {
        if (amount < 1 || amount > Short.MAX_VALUE)
            throw new IllegalArgumentException("Item amount must be an integer from 1 to 32767.");
        var result = new PlayerData.Item[1];
        db.transaction(
                () -> {
                    var rows =
                            db.query(
                                    "SELECT item_id,item_amount FROM owned_item WHERE owner_id=?"
                                        + " AND item_index_id=? AND inventory_id=1 AND region=-1",
                                    owner,
                                    Integer.toString(itemId));
                    if (!rows.isEmpty()) {
                        var row = rows.get(0);
                        int total = Integer.parseInt(row[1]) + amount;
                        if (total > Short.MAX_VALUE)
                            throw new IllegalArgumentException(
                                    "The item amount could not be saved. The stack may exceed the"
                                            + " database limit.");
                        db.execute(
                                "UPDATE owned_item SET item_amount=? WHERE item_id=? AND"
                                        + " owner_id=?",
                                total,
                                row[0],
                                owner);
                        result[0] = new PlayerData.Item(row[0], itemId, total, -1);
                    } else {
                        String id = UUID.randomUUID().toString();
                        db.execute(
                                "INSERT INTO"
                                    + " owned_item(item_id,owner_id,item_index_id,item_amount,inventory_id,region)"
                                    + " VALUES (?,?,?,?,1,-1)",
                                id,
                                owner,
                                itemId,
                                amount);
                        result[0] = new PlayerData.Item(id, itemId, amount, -1);
                    }
                });
        return result[0];
    }

    PlayerData.Trainer trainer(GameService.Character character) {
        db.execute(
                "INSERT OR IGNORE INTO character_trainer(character_id) VALUES (?)", character.id());
        var row =
                db.query(
                                "SELECT"
                                    + " c.money,t.battle_points,t.played_seconds,t.obedience,t.badges"
                                    + " FROM characters c JOIN character_trainer t ON"
                                    + " t.character_id=c.id WHERE c.id=?",
                                character.id())
                        .get(0);
        return new PlayerData.Trainer(
                character,
                Integer.parseInt(row[0]),
                Integer.parseInt(row[1]),
                Long.parseLong(row[2]),
                (int) new PokedexStore(db).load(character.id()).count(1),
                Integer.parseInt(row[3]),
                Integer.parseInt(row[4]));
    }

    void addPlayedSeconds(String id, long seconds) {
        if (seconds <= 0) return;
        db.execute("INSERT OR IGNORE INTO character_trainer(character_id) VALUES (?)", id);
        db.execute(
                "UPDATE character_trainer SET played_seconds=played_seconds+? WHERE character_id=?",
                seconds,
                id);
    }

    private static final String MAIL =
            "SELECT m.mail_id,s.name,r.name,m.created_at,m.title,m.body,m.is_read FROM mail_message"
                    + " m JOIN characters s ON s.id=m.sender_id JOIN characters r ON"
                    + " r.id=m.recipient_id ";

    private static PlayerData.Mail entry(String[] r) {
        return new PlayerData.Mail(
                r[0], r[1], r[2], Long.parseLong(r[3]), r[4], r[5], Integer.parseInt(r[6]) != 0);
    }

    PlayerData.MailPage mail(String id, boolean sent, int page) {
        if (page < 0 || page > 32767) throw new IllegalArgumentException("Invalid mail page");
        String condition =
                sent
                        ? "m.sender_id=? AND m.sender_deleted=0"
                        : "m.recipient_id=? AND m.recipient_deleted=0";
        int total =
                Integer.parseInt(
                        db.query("SELECT COUNT(*) FROM mail_message m WHERE " + condition, id)
                                .get(0)[0]);
        int unread =
                Integer.parseInt(
                        db.query(
                                        "SELECT COUNT(*) FROM mail_message WHERE recipient_id=? AND"
                                                + " recipient_deleted=0 AND is_read=0",
                                        id)
                                .get(0)[0]);
        var entries =
                db
                        .query(
                                MAIL
                                        + "WHERE "
                                        + condition
                                        + " ORDER BY m.created_at DESC,m.mail_id DESC LIMIT 10"
                                        + " OFFSET ?",
                                id,
                                Integer.toString(page * 10))
                        .stream()
                        .map(PlayerDataStore::entry)
                        .toList();
        return new PlayerData.MailPage(entries, total, unread, page, sent);
    }

    PlayerData.Mail read(String id, String mailId) {
        var result = new ArrayList<PlayerData.Mail>();
        db.transaction(
                () -> {
                    var rows =
                            db.query(
                                    MAIL
                                            + "WHERE m.mail_id=? AND ((m.sender_id=? AND"
                                            + " m.sender_deleted=0) OR (m.recipient_id=? AND"
                                            + " m.recipient_deleted=0))",
                                    mailId,
                                    id,
                                    id);
                    if (rows.isEmpty())
                        throw new GameService.Rejected(GameService.Failure.NOT_FOUND);
                    db.execute(
                            "UPDATE mail_message SET is_read=1 WHERE mail_id=? AND recipient_id=?",
                            mailId,
                            id);
                    result.add(entry(db.query(MAIL + "WHERE m.mail_id=?", mailId).get(0)));
                });
        return result.get(0);
    }

    void delete(String id, String mailId, boolean sent) {
        db.transaction(
                () -> {
                    String actor = sent ? "sender" : "recipient";
                    if (db.query(
                                    "SELECT mail_id FROM mail_message WHERE mail_id=? AND "
                                            + actor
                                            + "_id=?",
                                    mailId,
                                    id)
                            .isEmpty())
                        throw new GameService.Rejected(GameService.Failure.NOT_FOUND);
                    db.execute(
                            "UPDATE mail_message SET " + actor + "_deleted=1 WHERE mail_id=?",
                            mailId);
                });
    }

    PlayerData.MailResult send(String id, String recipient, String subject, String body) {
        if (recipient == null
                || recipient.isBlank()
                || recipient.length() > 16
                || recipient.indexOf(0) >= 0) return PlayerData.MailResult.INVALID_RECIPIENT;
        if (subject == null || subject.length() > 128 || subject.indexOf(0) >= 0)
            return PlayerData.MailResult.INVALID_SUBJECT;
        if (body == null || body.length() > 4000 || body.indexOf(0) >= 0)
            return PlayerData.MailResult.INVALID_BODY;
        var result = new PlayerData.MailResult[] {PlayerData.MailResult.NOT_FOUND};
        db.transaction(
                () -> {
                    var recipients =
                            db.query(
                                    "SELECT r.id FROM characters r JOIN characters s ON"
                                            + " s.server=r.server WHERE s.id=? AND r.name_key=?",
                                    id,
                                    recipient.toLowerCase(Locale.ROOT));
                    if (recipients.size() != 1) return;
                    // Reference MailSendService deducts attachments only; do not invent a different
                    // fee rule.
                    db.execute(
                            "INSERT INTO"
                                + " mail_message(mail_id,sender_id,recipient_id,created_at,title,body)"
                                + " VALUES (?,?,?,?,?,?)",
                            UUID.randomUUID().toString(),
                            id,
                            recipients.get(0)[0],
                            System.currentTimeMillis() / 1000,
                            subject,
                            body);
                    result[0] = PlayerData.MailResult.SUCCESS;
                });
        return result[0];
    }
}
