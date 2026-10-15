package top.totodile.pokemmo.persistence;

import top.totodile.pokemmo.protocol.CharacterPackets;
import top.totodile.pokemmo.protocol.TrainerStats;
import top.totodile.pokemmo.protocol.PartyMember;
import top.totodile.pokemmo.protocol.PcPackets;
import top.totodile.pokemmo.protocol.CharacterPackets.*;
import java.sql.*;
import java.util.*;

/** 锁定账号行串行化同账号创建；角色名唯一性由数据库约束处理。 */
public final class CharacterStore {
    private final Database database;
    private final AvatarCatalog catalog;
    public CharacterStore(Database database, AvatarCatalog catalog) { this.database = database; this.catalog = catalog; }
    public boolean ownsAccount(int id, String username) throws SQLException {
        try (Connection connection = database.connect(); PreparedStatement query = connection.prepareStatement("SELECT 1 FROM accounts WHERE id=? AND username=?")) {
            query.setInt(1, id); query.setString(2, username);
            try (ResultSet row = query.executeQuery()) { return row.next(); }
        }
    }
    public List<CharacterInfo> list(int account) throws SQLException {
        try (Connection connection = database.connect()) { return list(connection, account); }
    }
    private List<CharacterInfo> list(Connection connection, int account) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("SELECT id,name,gender,skin,region,items,colors,map_group,map_id,position_x,position_y,elevation,facing,return_position,created_at,dex_unlocked,money,battle_points,online_seconds,CASE WHEN kanto_champion THEN 9 ELSE kanto_badges END,CASE WHEN hoenn_champion THEN 9 ELSE hoenn_badges END,party_species,party_flags,party_hp,party_max_hp,party_levels,CASE WHEN unova_champion THEN 9 ELSE unova_badges END,CASE WHEN sinnoh_champion THEN 9 ELSE sinnoh_badges END,cycling,debug_noclip,debug_speed_multiplier FROM characters WHERE account_id=? ORDER BY id")) {
            query.setInt(1, account);
            List<CharacterInfo> result = new ArrayList<>();
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) result.add(new CharacterInfo(rows.getLong(1), new Appearance(rows.getString(2),
                        rows.getInt(3), rows.getInt(4), rows.getInt(5), integers(rows.getArray(6)), integers(rows.getArray(7))), new Position(rows.getInt(8), rows.getInt(9), rows.getInt(10), rows.getInt(11), rows.getInt(12), rows.getInt(13), returnPoint(rows.getArray(14))), rows.getTimestamp(15).toInstant().getEpochSecond(), rows.getBoolean(16), new TrainerStats(rows.getBoolean(16) ? top.totodile.pokemmo.protocol.PokemonCatalog.MAX_SPECIES : 0, rows.getLong(17), rows.getLong(18), rows.getLong(19),
                                TrainerStats.obedience(rows.getInt(5), rows.getInt(5) >= 4 ? 0 : rows.getInt(rows.getInt(5) == 0 ? 20 : rows.getInt(5) == 1 ? 21 : rows.getInt(5) == 2 ? 27 : 28))), PokemonStore.load(connection,rows.getLong(1),party(rows.getArray(22), rows.getArray(23), rows.getArray(24), rows.getArray(25), rows.getArray(26))), rows.getBoolean(29), rows.getBoolean(30), rows.getInt(31)));
            }
            return List.copyOf(result);
        }
    }
    static List<PartyMember> party(Array speciesArray, Array flagsArray, Array hpArray, Array maxHpArray, Array levelsArray) throws SQLException {
        try {
            int[] species = integers(speciesArray), flags = integers(flagsArray), hp = integers(hpArray), maxHp = integers(maxHpArray), levels = integers(levelsArray);
            if (species.length != flags.length || species.length != hp.length || species.length != maxHp.length || species.length > 6 || species.length != levels.length) throw new SQLException("Invalid stored party");
            List<PartyMember> result = new ArrayList<>();
            for (int i = 0; i < species.length; i++) {
                if ((flags[i] & ~3) != 0) throw new SQLException("Invalid stored party flags");
                result.add(new PartyMember(species[i], (flags[i] & 1) != 0, (flags[i] & 2) != 0, hp[i], maxHp[i], levels[i]));
            }
            return List.copyOf(result);
        } finally {
            speciesArray.free();
            flagsArray.free();
            hpArray.free();
            maxHpArray.free();
            levelsArray.free();
        }
    }
    public List<PartyMember> moveParty(int account, long character, int from, int to) throws SQLException {
        if (from < 0 || from >= 6 || to < 0 || to >= 6 || from == to) return null;
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                List<PartyMember> members;
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT party_species,party_flags,party_hp,party_max_hp,party_levels,CASE WHEN unova_champion THEN 9 ELSE unova_badges END,CASE WHEN sinnoh_champion THEN 9 ELSE sinnoh_badges END FROM characters WHERE id=? AND account_id=? FOR UPDATE")) {
                    query.setLong(1, character);
                    query.setInt(2, account);
                    try (ResultSet row = query.executeQuery()) {
                        if (!row.next()) { connection.rollback(); return null; }
                        members = new ArrayList<>(PokemonStore.load(connection,character,party(row.getArray(1), row.getArray(2), row.getArray(3), row.getArray(4), row.getArray(5))));
                    }
                }
                if (from >= members.size() || to >= members.size()) { connection.rollback(); return null; }
                members.add(to, members.remove(from));
                savePartyArrays(connection, account, character, members);
                PokemonStore.save(connection,character,members);
                connection.commit();
                return List.copyOf(members);
            } catch (SQLException exception) { connection.rollback(); throw exception; }
        }
    }
    /** 队伍数组与个体记录必须一起写回，供排序和电脑箱子共用。 */
    private static void savePartyArrays(Connection connection, int account, long character, List<PartyMember> members) throws SQLException {
        Short[] species = new Short[members.size()], flags = new Short[members.size()], levels = new Short[members.size()];
        Integer[] hp = new Integer[members.size()], maxHp = new Integer[members.size()];
        for (int i = 0; i < members.size(); i++) {
            species[i] = (short) members.get(i).species();
            flags[i] = (short) members.get(i).flags();
            levels[i] = (short) members.get(i).level();
            hp[i] = members.get(i).hp(); maxHp[i] = members.get(i).maxHp();
        }
        Array speciesArray = connection.createArrayOf("smallint", species);
        Array flagsArray = connection.createArrayOf("smallint", flags);
        Array levelsArray = connection.createArrayOf("smallint", levels);
        Array hpArray = connection.createArrayOf("integer", hp), maxHpArray = connection.createArrayOf("integer", maxHp);
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE characters SET party_species=?,party_flags=?,party_hp=?,party_max_hp=?,party_levels=? WHERE id=? AND account_id=?")) {
            update.setArray(1, speciesArray); update.setArray(2, flagsArray);
            update.setArray(3, hpArray); update.setArray(4, maxHpArray);
            update.setArray(5, levelsArray);
            update.setLong(6, character); update.setInt(7, account); update.executeUpdate();
        } finally { speciesArray.free(); flagsArray.free(); hpArray.free(); maxHpArray.free(); levelsArray.free(); }
    }

    /** 电脑箱子：队伍与箱子共用角色行锁，一次搬移在一个事务内完成。 */
    public top.totodile.pokemmo.protocol.PcPackets.PartyState pcParty(int account, long character) throws SQLException {
        try (Connection connection = database.connect();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT pc_boxes,party_species,party_flags,party_hp,party_max_hp,party_levels,CASE WHEN unova_champion THEN 9 ELSE unova_badges END,CASE WHEN sinnoh_champion THEN 9 ELSE sinnoh_badges END FROM characters WHERE id=? AND account_id=?")) {
            query.setLong(1, character); query.setInt(2, account);
            try (ResultSet row = query.executeQuery()) {
                if (!row.next()) return new top.totodile.pokemmo.protocol.PcPackets.PartyState(1, List.of());
                return new top.totodile.pokemmo.protocol.PcPackets.PartyState(row.getInt(1),
                        PokemonStore.load(connection, character, party(row.getArray(2), row.getArray(3), row.getArray(4), row.getArray(5), row.getArray(6))));
            }
        }
    }

    /** 箱子页按占用格的编码大小截断，避免超过通道单帧上限。 */
    public top.totodile.pokemmo.protocol.PcPackets.BoxPage pcBox(int account, long character, int box, int start) throws SQLException {
        try (Connection connection = database.connect();
             PreparedStatement query = connection.prepareStatement("SELECT pc_boxes FROM characters WHERE id=? AND account_id=?")) {
            query.setLong(1, character); query.setInt(2, account);
            int boxes;
            try (ResultSet row = query.executeQuery()) {
                if (!row.next()) return new top.totodile.pokemmo.protocol.PcPackets.BoxPage(box, start, false, List.of());
                boxes = row.getInt(1);
            }
            if (box < 0 || box >= boxes) return new top.totodile.pokemmo.protocol.PcPackets.BoxPage(box, start, false, List.of());
            List<top.totodile.pokemmo.protocol.PcPackets.Entry> entries = new ArrayList<>();
            boolean more = false;
            int budget = 0;
            for (PokemonStore.Boxed stored : PokemonStore.loadBoxes(connection, character)) {
                if (stored.box() != box || stored.slot() < start) continue;
                int size = stored.member().individual().encode().length + 12;
                if (!entries.isEmpty() && budget + size > top.totodile.pokemmo.protocol.PcPackets.PAGE_BUDGET) { more = true; break; }
                budget += size;
                entries.add(new top.totodile.pokemmo.protocol.PcPackets.Entry(stored.slot(), stored.member()));
            }
            return new top.totodile.pokemmo.protocol.PcPackets.BoxPage(box, start, more, List.copyOf(entries));
        }
    }

    /** 队伍与箱子之间可交换或搬移；箱子目标为空时队伍缩容，队伍已满时拒绝从箱子取出。 */
    public top.totodile.pokemmo.protocol.PcPackets.Status pcMove(int account, long character, top.totodile.pokemmo.protocol.PcPackets.Move move) throws SQLException {
        boolean fromParty = move.fromKind() == top.totodile.pokemmo.protocol.PcPackets.PARTY;
        boolean toParty = move.toKind() == top.totodile.pokemmo.protocol.PcPackets.PARTY;
        if (move.fromKind() == move.toKind() && move.fromBox() == move.toBox() && move.fromSlot() == move.toSlot()) return PcPackets.Status.INVALID;
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                int boxes;
                List<PartyMember> members;
                List<PokemonStore.Boxed> stored;
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT pc_boxes,party_species,party_flags,party_hp,party_max_hp,party_levels,CASE WHEN unova_champion THEN 9 ELSE unova_badges END,CASE WHEN sinnoh_champion THEN 9 ELSE sinnoh_badges END FROM characters WHERE id=? AND account_id=? FOR UPDATE")) {
                    query.setLong(1, character); query.setInt(2, account);
                    try (ResultSet row = query.executeQuery()) {
                        if (!row.next()) { connection.rollback(); return PcPackets.Status.INVALID; }
                        boxes = row.getInt(1);
                        members = new ArrayList<>(PokemonStore.load(connection, character, party(row.getArray(2), row.getArray(3), row.getArray(4), row.getArray(5), row.getArray(6))));
                    }
                }
                stored = new ArrayList<>(PokemonStore.loadBoxes(connection, character));
                if (move.fromBox() >= boxes || move.toBox() >= boxes) { connection.rollback(); return PcPackets.Status.INVALID; }
                PokemonStore.Boxed source = fromParty ? null : boxed(stored, move.fromBox(), move.fromSlot());
                PartyMember sourceMember = fromParty ? (move.fromSlot() < members.size() ? members.get(move.fromSlot()) : null) : source == null ? null : source.member();
                if (sourceMember == null) { connection.rollback(); return PcPackets.Status.INVALID; }
                PokemonStore.Boxed target = toParty ? null : boxed(stored, move.toBox(), move.toSlot());
                PartyMember targetMember = toParty && move.toSlot() < members.size() ? members.get(move.toSlot()) : null;
                if (fromParty && toParty) {
                    if (move.toSlot() >= members.size()) { connection.rollback(); return PcPackets.Status.INVALID; }
                    members.add(move.toSlot(), members.remove(move.fromSlot()));
                } else if (fromParty) {
                    members.remove(move.fromSlot());
                    removeBoxed(stored, move.toBox(), move.toSlot());
                    stored.add(new PokemonStore.Boxed(move.toBox(), move.toSlot(), sourceMember));
                    if (target != null) members.add(move.fromSlot(), target.member());
                } else if (toParty) {
                    removeBoxed(stored, move.fromBox(), move.fromSlot());
                    if (targetMember != null) {
                        members.set(move.toSlot(), sourceMember);
                        stored.add(new PokemonStore.Boxed(move.fromBox(), move.fromSlot(), targetMember));
                    } else {
                        if (members.size() >= 6) { connection.rollback(); return PcPackets.Status.PARTY_FULL; }
                        members.add(sourceMember);
                    }
                } else {
                    removeBoxed(stored, move.fromBox(), move.fromSlot());
                    removeBoxed(stored, move.toBox(), move.toSlot());
                    stored.add(new PokemonStore.Boxed(move.toBox(), move.toSlot(), sourceMember));
                    if (target != null) stored.add(new PokemonStore.Boxed(move.fromBox(), move.fromSlot(), target.member()));
                }
                savePartyArrays(connection, account, character, members);
                PokemonStore.save(connection, character, members);
                PokemonStore.saveBoxes(connection, character, stored);
                connection.commit();
                return PcPackets.Status.SUCCESS;
            } catch (SQLException | RuntimeException exception) { connection.rollback(); throw exception; }
        }
    }

    public top.totodile.pokemmo.protocol.PcPackets.Status pcCreateBox(int account, long character) throws SQLException {
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                int boxes;
                try (PreparedStatement query = connection.prepareStatement("SELECT pc_boxes FROM characters WHERE id=? AND account_id=? FOR UPDATE")) {
                    query.setLong(1, character); query.setInt(2, account);
                    try (ResultSet row = query.executeQuery()) {
                        if (!row.next()) { connection.rollback(); return PcPackets.Status.INVALID; }
                        boxes = row.getInt(1);
                    }
                }
                if (boxes >= top.totodile.pokemmo.protocol.PcPackets.MAX_BOXES) { connection.rollback(); return PcPackets.Status.BOX_FULL; }
                try (PreparedStatement update = connection.prepareStatement("UPDATE characters SET pc_boxes=? WHERE id=? AND account_id=?")) {
                    update.setInt(1, boxes + 1); update.setLong(2, character); update.setInt(3, account); update.executeUpdate();
                }
                connection.commit();
                return PcPackets.Status.SUCCESS;
            } catch (SQLException | RuntimeException exception) { connection.rollback(); throw exception; }
        }
    }

    private static PokemonStore.Boxed boxed(List<PokemonStore.Boxed> stored, int box, int slot) {
        for (PokemonStore.Boxed entry : stored) if (entry.box() == box && entry.slot() == slot) return entry;
        return null;
    }

    private static void removeBoxed(List<PokemonStore.Boxed> stored, int box, int slot) {
        stored.removeIf(entry -> entry.box() == box && entry.slot() == slot);
    }
    public top.totodile.pokemmo.protocol.SearchPackets.Result search(int account, long character,
            top.totodile.pokemmo.protocol.SearchPackets.Request request, boolean debug) throws SQLException {
        var status = top.totodile.pokemmo.protocol.SearchPackets.Status.SUCCESS;
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                List<PartyMember> members;
                try (var query = connection.prepareStatement("SELECT party_species,party_flags,party_hp,party_max_hp,party_levels,CASE WHEN unova_champion THEN 9 ELSE unova_badges END,CASE WHEN sinnoh_champion THEN 9 ELSE sinnoh_badges END FROM characters WHERE id=? AND account_id=? FOR UPDATE")) {
                    query.setLong(1, character); query.setInt(2, account);
                    try (var row = query.executeQuery()) {
                        if (!row.next()) { connection.rollback(); return new top.totodile.pokemmo.protocol.SearchPackets.Result(
                                top.totodile.pokemmo.protocol.SearchPackets.Status.INVALID, Map.of(), List.of()); }
                        members = new ArrayList<>(PokemonStore.load(connection,character,party(row.getArray(1), row.getArray(2), row.getArray(3), row.getArray(4), row.getArray(5))));
                    }
                }
                Map<Integer, Integer> inventory = new TreeMap<>();
                try (var query = connection.prepareStatement("SELECT item_id,quantity FROM character_inventory WHERE character_id=?")) {
                    query.setLong(1, character);
                    try (var rows = query.executeQuery()) { while (rows.next()) inventory.put(rows.getInt(1), rows.getInt(2)); }
                }
                int action = request.action(), id = request.id(), amount = request.amount();
                Map<Integer,Integer> rewards=Map.of();
                var item = top.totodile.pokemmo.protocol.ItemCatalog.get(id);
                if ((action == 1 || action == 2) && !debug) status = top.totodile.pokemmo.protocol.SearchPackets.Status.DISABLED;
                else if (action == 0) {
                    if (id != 0 || amount != 0) status = top.totodile.pokemmo.protocol.SearchPackets.Status.INVALID;
                } else if (action == 1) {
                    if (item == null || !item.addable() || amount < 1 || amount > item.maxStack()) status = top.totodile.pokemmo.protocol.SearchPackets.Status.INVALID;
                    else if (inventory.getOrDefault(id, 0) > item.maxStack() - amount) status = top.totodile.pokemmo.protocol.SearchPackets.Status.STACK_FULL;
                    else {
                        int quantity = inventory.getOrDefault(id, 0) + amount;
                        try (var update = connection.prepareStatement("INSERT INTO character_inventory(character_id,item_id,quantity) VALUES (?,?,?) ON CONFLICT(character_id,item_id) DO UPDATE SET quantity=EXCLUDED.quantity")) {
                            update.setLong(1, character); update.setInt(2, id); update.setInt(3, quantity); update.executeUpdate();
                        }
                        inventory.put(id, quantity);
                    }
                } else if (action == 2) {
                    if (id < 1 || id > top.totodile.pokemmo.protocol.PokemonCatalog.MAX_SPECIES || amount < 1 || amount > 100) status = top.totodile.pokemmo.protocol.SearchPackets.Status.INVALID;
                    else if (members.size() == 6) status = top.totodile.pokemmo.protocol.SearchPackets.Status.PARTY_FULL;
                    else {
                        int hp = top.totodile.pokemmo.protocol.PokemonCatalog.hp(id, amount);
                        try (var update = connection.prepareStatement("UPDATE characters SET party_species=array_append(party_species,?::smallint),party_flags=array_append(party_flags,0::smallint),party_hp=array_append(party_hp,?),party_max_hp=array_append(party_max_hp,?),party_levels=array_append(party_levels,?::smallint) WHERE id=? AND account_id=?")) {
                            update.setInt(1, id); update.setInt(2, hp); update.setInt(3, hp); update.setInt(4, amount);
                            update.setLong(5, character); update.setInt(6, account); update.executeUpdate();
                        }
                        members.add(new PartyMember(id, false, false, hp, hp, amount));
                    }
                } else if (action == top.totodile.pokemmo.protocol.SearchPackets.USE_ITEM) {
                    int owned=inventory.getOrDefault(id,0);
                    if(amount!=1||owned<1||!top.totodile.pokemmo.protocol.ItemLoot.usable(id))
                        status=top.totodile.pokemmo.protocol.SearchPackets.Status.INVALID;
                    else {
                        var rolled=top.totodile.pokemmo.protocol.ItemLoot.roll(id,java.util.concurrent.ThreadLocalRandom.current());
                        boolean full=rolled.entrySet().stream().anyMatch(entry -> inventory.getOrDefault(entry.getKey(),0)
                                > top.totodile.pokemmo.protocol.ItemCatalog.get(entry.getKey()).maxStack()-entry.getValue());
                        if(full)status=top.totodile.pokemmo.protocol.SearchPackets.Status.STACK_FULL;
                        else {
                            try(var update=connection.prepareStatement(owned==1?
                                    "DELETE FROM character_inventory WHERE character_id=? AND item_id=?":
                                    "UPDATE character_inventory SET quantity=quantity-1 WHERE character_id=? AND item_id=?")) {
                                update.setLong(1,character);update.setInt(2,id);update.executeUpdate();
                            }
                            if(owned==1)inventory.remove(id);else inventory.put(id,owned-1);
                            for(var reward:rolled.entrySet()) {
                                int quantity=inventory.getOrDefault(reward.getKey(),0)+reward.getValue();
                                try(var update=connection.prepareStatement("INSERT INTO character_inventory(character_id,item_id,quantity) VALUES (?,?,?) ON CONFLICT(character_id,item_id) DO UPDATE SET quantity=EXCLUDED.quantity")) {
                                    update.setLong(1,character);update.setInt(2,reward.getKey());update.setInt(3,quantity);update.executeUpdate();
                                }
                                inventory.put(reward.getKey(),quantity);
                            }
                            rewards=rolled;
                        }
                    }
                } else if (action == top.totodile.pokemmo.protocol.SearchPackets.DESTROY_ITEM) {
                    int owned=inventory.getOrDefault(id,0);
                    if(item==null||item.pocket()==3||amount<1||amount>owned)status=top.totodile.pokemmo.protocol.SearchPackets.Status.INVALID;
                    else {
                        try(var update=connection.prepareStatement(amount==owned?
                                "DELETE FROM character_inventory WHERE character_id=? AND item_id=?":
                                "UPDATE character_inventory SET quantity=quantity-? WHERE character_id=? AND item_id=?")) {
                            int offset=amount==owned?0:1;
                            if(offset==1)update.setInt(1,amount);
                            update.setLong(offset+1,character);update.setInt(offset+2,id);update.executeUpdate();
                        }
                        if(amount==owned)inventory.remove(id);else inventory.put(id,owned-amount);
                    }
                } else status = top.totodile.pokemmo.protocol.SearchPackets.Status.INVALID;
                members=new ArrayList<>(PokemonStore.load(connection,character,members));
                connection.commit();
                return new top.totodile.pokemmo.protocol.SearchPackets.Result(status, Map.copyOf(inventory), List.copyOf(members), rewards);
            } catch (SQLException | RuntimeException exception) { connection.rollback(); throw exception; }
        }
    }

    public CreationResult create(int account, Appearance appearance) throws SQLException {
        if (!catalog.valid(appearance)) return new CreationResult(Status.INVALID, List.of());
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement lock = connection.prepareStatement("SELECT id FROM accounts WHERE id=? FOR UPDATE")) {
                    lock.setInt(1, account);
                    try (ResultSet row = lock.executeQuery()) {
                        if (!row.next()) { connection.rollback(); return new CreationResult(Status.SYSTEM_ERROR, List.of()); }
                    }
                }
                if (list(connection, account).size() >= CharacterPackets.LIMIT) {
                    connection.rollback(); return new CreationResult(Status.LIMIT_REACHED, List.of());
                }
                try (PreparedStatement insert = connection.prepareStatement("INSERT INTO characters(account_id,name,gender,skin,region,items,colors,map_group,map_id,position_x,position_y,elevation,facing) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                    insert.setInt(1, account); insert.setString(2, appearance.name()); insert.setInt(3, appearance.gender());
                    insert.setInt(4, appearance.skin()); insert.setInt(5, appearance.region());
                    Position spawn = Position.spawn(appearance.region(), appearance.gender());
                    insert.setInt(8, spawn.group()); insert.setInt(9, spawn.map());
                    insert.setInt(10, spawn.x()); insert.setInt(11, spawn.y());
                    insert.setInt(12, spawn.elevation()); insert.setInt(13, spawn.facing());
                    Array items = connection.createArrayOf("smallint", boxed(appearance.items()));
                    Array colors = connection.createArrayOf("smallint", boxed(appearance.colors()));
                    try { insert.setArray(6, items); insert.setArray(7, colors); insert.executeUpdate(); }
                    finally { items.free(); colors.free(); }
                }
                List<CharacterInfo> characters = list(connection, account);
                connection.commit();
                return new CreationResult(Status.SUCCESS, characters);
            } catch (SQLException exception) {
                connection.rollback();
                if ("23505".equals(exception.getSQLState())) return new CreationResult(Status.NAME_TAKEN, List.of());
                throw exception;
            }
        }
    }
    public CreationResult delete(int account, long id) throws SQLException {
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement lock = connection.prepareStatement("SELECT id FROM accounts WHERE id=? FOR UPDATE")) {
                    lock.setInt(1, account);
                    try (ResultSet row = lock.executeQuery()) {
                        if (!row.next()) { connection.rollback(); return new CreationResult(Status.INVALID, List.of()); }
                    }
                }
                try (PreparedStatement delete = connection.prepareStatement("DELETE FROM characters WHERE id=? AND account_id=?")) {
                    delete.setLong(1, id);
                    delete.setInt(2, account);
                    if (delete.executeUpdate() != 1) { connection.rollback(); return new CreationResult(Status.INVALID, List.of()); }
                }
                List<CharacterInfo> remaining = list(connection, account);
                connection.commit();
                return new CreationResult(Status.SUCCESS, remaining);
            } catch (SQLException exception) { connection.rollback(); throw exception; }
        }
    }
    /** 调试图鉴只更新已认证账号所属角色，不接收客户端指定的账号。 */
    public boolean setDexUnlocked(int account, long character, boolean unlocked) throws SQLException {
        try (var connection = database.connect(); var query = connection.prepareStatement(
                "UPDATE characters SET dex_unlocked=? WHERE id=? AND account_id=?")) {
            query.setBoolean(1, unlocked);
            query.setLong(2, character);
            query.setInt(3, account);
            return query.executeUpdate() == 1;
        }
    }

    public TrainerStats stats(int account, long character, long onlineSeconds) throws SQLException {
        try (var c = database.connect(); var q = c.prepareStatement(
                "SELECT region,dex_unlocked,money,battle_points,online_seconds,CASE WHEN kanto_champion THEN 9 ELSE kanto_badges END,CASE WHEN hoenn_champion THEN 9 ELSE hoenn_badges END,CASE WHEN unova_champion THEN 9 ELSE unova_badges END,CASE WHEN sinnoh_champion THEN 9 ELSE sinnoh_badges END FROM characters WHERE id=? AND account_id=?")) {
            q.setLong(1, character); q.setInt(2, account);
            try (var rows = q.executeQuery()) {
                if (!rows.next()) throw new SQLException("Character stats unavailable");
                int region = rows.getInt(1);
                return new TrainerStats(rows.getBoolean(2) ? top.totodile.pokemmo.protocol.PokemonCatalog.MAX_SPECIES : 0, rows.getLong(3), rows.getLong(4),
                        Math.max(onlineSeconds, rows.getLong(5)), TrainerStats.obedience(region, region >= 4 ? 0 : rows.getInt(region == 0 ? 6 : region == 1 ? 7 : region == 2 ? 8 : 9)));
            }
        }
    }

    public boolean savePosition(int account, long character, Position p) throws SQLException {
        return saveWorldState(account, character, p, -1);
    }

    public boolean saveDebugSettings(int account, long character, boolean noclip, int multiplier) throws SQLException {
        if (multiplier != 1 && multiplier != 2 && multiplier != 4 && multiplier != 8) return false;
        try (var c = database.connect(); var q = c.prepareStatement(
                "UPDATE characters SET debug_noclip=?,debug_speed_multiplier=? WHERE id=? AND account_id=?")) {
            q.setBoolean(1, noclip); q.setInt(2, multiplier); q.setLong(3, character); q.setInt(4, account);
            return q.executeUpdate() == 1;
        }
    }

    /** 地区和落点在同一条更新中提交，不改动角色外观、个体资料及各地区进度。 */
    public boolean transferRegion(int account, long character, int region, Position position,
                                  long online, boolean cycling) throws SQLException {
        if (!position.valid(region)) return false;
        try (var c = database.connect(); var q = c.prepareStatement(
                "UPDATE characters SET region=?,map_group=?,map_id=?,position_x=?,position_y=?,elevation=?,facing=?,return_position=ARRAY[]::smallint[],online_seconds=GREATEST(online_seconds,?),cycling=? WHERE id=? AND account_id=?")) {
            q.setInt(1, region); q.setInt(2, position.group()); q.setInt(3, position.map());
            q.setInt(4, position.x()); q.setInt(5, position.y()); q.setInt(6, position.elevation()); q.setInt(7, position.facing());
            q.setLong(8, online); q.setBoolean(9, cycling); q.setLong(10, character); q.setInt(11, account);
            return q.executeUpdate() == 1;
        }
    }

    public boolean saveWorldState(int account, long character, Position p, long onlineSeconds) throws SQLException {
        return saveWorldState(account, character, p, onlineSeconds, null);
    }

    public boolean saveWorldState(int account, long character, Position p, long onlineSeconds, Boolean cycling) throws SQLException {
        if (!p.valid()) return false;
        try (Connection c = database.connect(); PreparedStatement q = c.prepareStatement(
                "UPDATE characters SET map_group=?,map_id=?,position_x=?,position_y=?,elevation=?,facing=?,return_position=?,online_seconds=GREATEST(online_seconds,?),cycling=COALESCE(?,cycling) WHERE id=? AND account_id=? AND ((region=0 AND ?) OR (region=1 AND ?) OR (region=2 AND ?) OR (region=3 AND ?) OR (region=4 AND ?) OR (region=5 AND ?))")) {
            q.setInt(1,p.group()); q.setInt(2,p.map()); q.setInt(3,p.x()); q.setInt(4,p.y());
            q.setInt(5,p.elevation()); q.setInt(6,p.facing());
            var r = p.returnPoint();
            Array values = c.createArrayOf("smallint", r == null ? new Integer[0]
                    : new Integer[]{r.group(), r.map(), r.x(), r.y(), r.facing()});
            try {
                q.setArray(7, values); q.setLong(8, onlineSeconds); q.setObject(9, cycling, Types.BOOLEAN); q.setLong(10,character); q.setInt(11,account);
                q.setBoolean(12, p.valid(0)); q.setBoolean(13, p.valid(1)); q.setBoolean(14, p.valid(2)); q.setBoolean(15, p.valid(3)); q.setBoolean(16, p.valid(4)); q.setBoolean(17, p.valid(5));
                return q.executeUpdate() == 1;
            } finally { values.free(); }
        }
    }
    private static ReturnPoint returnPoint(Array array) throws SQLException {
        int[] values = integers(array);
        if (values.length == 0) return null;
        if (values.length != 5) throw new SQLException("Invalid saved return point");
        return new ReturnPoint(values[0], values[1], values[2], values[3], values[4]);
    }
    private static Integer[] boxed(int[] values) { return Arrays.stream(values).boxed().toArray(Integer[]::new); }
    private static int[] integers(Array value) throws SQLException {
        try {
            Object[] entries = (Object[]) value.getArray();
            int[] result = new int[entries.length];
            for (int i = 0; i < entries.length; i++) result[i] = ((Number) entries[i]).intValue();
            return result;
        } finally { value.free(); }
    }
}
