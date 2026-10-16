package top.totodile.pokemmo.persistence;

import top.totodile.pokemmo.protocol.PokemonConfigPackets;
import top.totodile.pokemmo.protocol.StoryProgressPackets;

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
        try (PreparedStatement query = connection.prepareStatement("SELECT id,name,gender,skin,region,items,colors,map_group,map_id,position_x,position_y,elevation,facing,return_position,created_at,dex_unlocked,money,battle_points,online_seconds,CASE WHEN kanto_champion THEN 9 ELSE kanto_badges END,CASE WHEN hoenn_champion THEN 9 ELSE hoenn_badges END,party_species,party_flags,party_hp,party_max_hp,party_levels,CASE WHEN unova_champion THEN 9 ELSE unova_badges END,CASE WHEN sinnoh_champion THEN 9 ELSE sinnoh_badges END,cycling,debug_noclip,debug_speed_multiplier,wild_encounters_enabled FROM characters WHERE account_id=? ORDER BY id")) {
            query.setInt(1, account);
            List<CharacterInfo> result = new ArrayList<>();
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) result.add(new CharacterInfo(rows.getLong(1), new Appearance(rows.getString(2),
                        rows.getInt(3), rows.getInt(4), rows.getInt(5), integers(rows.getArray(6)), integers(rows.getArray(7))), new Position(rows.getInt(8), rows.getInt(9), rows.getInt(10), rows.getInt(11), rows.getInt(12), rows.getInt(13), returnPoint(rows.getArray(14))), rows.getTimestamp(15).toInstant().getEpochSecond(), rows.getBoolean(16), new TrainerStats(rows.getBoolean(16) ? top.totodile.pokemmo.protocol.PokemonCatalog.MAX_SPECIES : 0, rows.getLong(17), rows.getLong(18), rows.getLong(19),
                                TrainerStats.obedience(rows.getInt(5), rows.getInt(5) >= 4 ? 0 : rows.getInt(rows.getInt(5) == 0 ? 20 : rows.getInt(5) == 1 ? 21 : rows.getInt(5) == 2 ? 27 : 28))), PokemonStore.load(connection,rows.getLong(1),party(rows.getArray(22), rows.getArray(23), rows.getArray(24), rows.getArray(25), rows.getArray(26))), rows.getBoolean(29), rows.getBoolean(30), rows.getInt(31), rows.getBoolean(32)));
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

    /** 个体配置与箱子搬移共用角色行锁，避免修改时槽位变化导致写入另一只个体。 */
    public PokemonConfigPackets.Result configurePokemon(int account, long character,
            PokemonConfigPackets.Request request, boolean debug) throws SQLException {
        var invalid = new PokemonConfigPackets.Result(
                PokemonConfigPackets.Status.INVALID, null, List.of());
        if (!debug) return new PokemonConfigPackets.Result(
                PokemonConfigPackets.Status.DISABLED, null, List.of());
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                List<PartyMember> members;
                try (var query = connection.prepareStatement(
                        "SELECT party_species,party_flags,party_hp,party_max_hp,party_levels FROM characters WHERE id=? AND account_id=? FOR UPDATE")) {
                    query.setLong(1, character); query.setInt(2, account);
                    try (var row = query.executeQuery()) {
                        if (!row.next()) { connection.rollback(); return invalid; }
                        members = new ArrayList<>(PokemonStore.load(connection, character,
                                party(row.getArray(1), row.getArray(2), row.getArray(3), row.getArray(4), row.getArray(5))));
                    }
                }
                var boxes = new ArrayList<>(PokemonStore.loadBoxes(connection, character));
                PartyMember updated = null;
                for (int i = 0; i < members.size(); i++) {
                    var member = members.get(i);
                    if (member.individual().id() != request.individual()) continue;
                    updated = PokemonConfigPackets.apply(member, request);
                    members.set(i, updated); break;
                }
                if (updated == null) for (int i = 0; i < boxes.size(); i++) {
                    var box = boxes.get(i);
                    if (box.member().individual().id() != request.individual()) continue;
                    updated = PokemonConfigPackets.apply(box.member(), request);
                    boxes.set(i, new PokemonStore.Boxed(box.box(), box.slot(), updated)); break;
                }
                if (updated == null) { connection.rollback(); return invalid; }
                PokemonStore.save(connection, character, members);
                savePartyArrays(connection, account, character, members);
                PokemonStore.saveBoxes(connection, character, boxes);
                connection.commit();
                return new PokemonConfigPackets.Result(
                        PokemonConfigPackets.Status.SUCCESS, updated, members);
            } catch (IllegalArgumentException error) { connection.rollback(); return invalid; }
            catch (SQLException error) { connection.rollback(); throw error; }
        }
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

    /** 按个体身份核对战斗起始队伍，在同一事务中保存HP、PP和异常状态。 */
    public void saveBattleParty(int account, long character, List<PartyMember> expected, List<PartyMember> next) throws SQLException {
        saveBattleParty(account,character,expected,next,0);
    }
    public java.util.Set<Integer> defeatedTrainers(int account,long character) throws SQLException {
        var result=new java.util.HashSet<Integer>();
        try(var c=database.connect();var q=c.prepareStatement("SELECT t.trainer_id FROM character_trainers t JOIN characters c ON c.id=t.character_id WHERE c.account_id=? AND c.id=?")) {
            q.setInt(1,account);q.setLong(2,character);try(var row=q.executeQuery()){while(row.next())result.add(row.getInt(1));}
        }
        return result;
    }
    public void saveBattleParty(int account,long character,List<PartyMember> expected,List<PartyMember> next,int trainer) throws SQLException {
        try (var connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                List<PartyMember> current;
                try (var q = connection.prepareStatement("SELECT party_species,party_flags,party_hp,party_max_hp,party_levels FROM characters WHERE id=? AND account_id=? FOR UPDATE")) {
                    q.setLong(1,character);q.setInt(2,account);
                    try(var row=q.executeQuery()) {
                        if(!row.next())throw new SQLException("Battle character missing");
                        current=PokemonStore.load(connection,character,party(row.getArray(1),row.getArray(2),row.getArray(3),row.getArray(4),row.getArray(5)));
                    }
                }
                if(!current.equals(expected))throw new SQLException("Battle party changed");
                PokemonStore.save(connection,character,next);PokemonStore.sync(connection,character,next);
                if(trainer>0)try(var q=connection.prepareStatement("INSERT INTO character_trainers(character_id,trainer_id) VALUES (?,?) ON CONFLICT DO NOTHING")) {
                    q.setLong(1,character);q.setInt(2,trainer);q.executeUpdate();
                }
                connection.commit();
            } catch(SQLException|RuntimeException error){connection.rollback();throw error;}
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
    public CreationResult replaceOutfit(int account, long character, Appearance appearance) throws SQLException {
        if (!catalog.valid(appearance, true)) return new CreationResult(Status.INVALID, List.of());
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                // 身份字段只用来确认打开表单时的角色，服饰更新不改变身份与地区。
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE characters SET items=?,colors=? WHERE id=? AND account_id=? AND name=? AND gender=? AND skin=? AND region=?")) {
                    Array items = connection.createArrayOf("smallint", boxed(appearance.items()));
                    Array colors = connection.createArrayOf("smallint", boxed(appearance.colors()));
                    try {
                        update.setArray(1, items); update.setArray(2, colors);
                        update.setLong(3, character); update.setInt(4, account);
                        update.setString(5, appearance.name()); update.setInt(6, appearance.gender());
                        update.setInt(7, appearance.skin()); update.setInt(8, appearance.region());
                        if (update.executeUpdate() != 1) {
                            connection.rollback(); return new CreationResult(Status.INVALID, List.of());
                        }
                    } finally { items.free(); colors.free(); }
                }
                List<CharacterInfo> result = list(connection, account);
                connection.commit();
                return new CreationResult(Status.SUCCESS, result);
            } catch (SQLException error) { connection.rollback(); throw error; }
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
    /** 遭遇开关只保存当前已认证账号所属角色。 */
    public boolean setWildEncounters(int account, long character, boolean enabled) throws SQLException {
        try (var connection = database.connect(); var query = connection.prepareStatement(
                "UPDATE characters SET wild_encounters_enabled=? WHERE id=? AND account_id=?")) {
            query.setBoolean(1, enabled); query.setLong(2, character); query.setInt(3, account);
            return query.executeUpdate() == 1;
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

    public boolean kantoOpeningComplete(int account, long character) throws SQLException {
        try (var c = database.connect(); var q = c.prepareStatement(
                "SELECT kanto_opening_complete FROM characters WHERE id=? AND account_id=?")) {
            q.setLong(1, character); q.setInt(2, account);
            try (var rows = q.executeQuery()) {
                if (!rows.next()) throw new SQLException("Story character not owned");
                return rows.getBoolean(1);
            }
        }
    }

    public void saveKantoOpening(int account, long character, boolean complete) throws SQLException {
        try (var c = database.connect(); var q = c.prepareStatement(
                "UPDATE characters SET kanto_opening_complete=? WHERE id=? AND account_id=?")) {
            q.setBoolean(1, complete); q.setLong(2, character); q.setInt(3, account);
            if (q.executeUpdate() != 1) throw new SQLException("Story save rejected");
        }
    }

    public java.util.Set<String> storyCompleted(int account, long character) throws SQLException {
        try (var c = database.connect(); var q = c.prepareStatement(
                "SELECT story_completed,kanto_opening_complete,gba_story_state FROM characters WHERE id=? AND account_id=?")) {
            q.setLong(1, character); q.setInt(2, account);
            try (var rows = q.executeQuery()) {
                if (!rows.next()) throw new SQLException("Story character not owned");
                var array = rows.getArray(1);
                var result = new java.util.HashSet<String>();
                try { for (Object value : (Object[]) array.getArray()) result.add(value.toString()); }
                finally { array.free(); }
                boolean legacy=rows.getString(3).isBlank();
                boolean fullOpening = legacy && result.contains(StoryProgressPackets.OPENING) && rows.getBoolean(2);
                if (legacy&&rows.getBoolean(2)) result.add(StoryProgressPackets.OPENING);
                else if(legacy) result.remove(StoryProgressPackets.OPENING);
                // 旧自然开场仅完成叫停，不能把它当成已执行带路与研究所对白。
                if (fullOpening)
                    result.addAll(StoryProgressPackets.completionScenes(StoryProgressPackets.OPENING));
                result.retainAll(StoryProgressPackets.scenes());
                return java.util.Set.copyOf(result);
            }
        }
    }

    /** 场景标记与旧开场标记同条提交，奖励与角色物品不参与剧情重置。 */
    public boolean setStoryCompleted(int account, long character, String scene, boolean completed) throws SQLException {
        if (StoryProgressPackets.scene(scene) == null) throw new IllegalArgumentException("Unknown story scene");
        String sql = completed
                ? "UPDATE characters SET story_completed=ARRAY(SELECT DISTINCT v FROM unnest(story_completed || ?::text[]) v),kanto_opening_complete=CASE WHEN ? THEN TRUE ELSE kanto_opening_complete END WHERE id=? AND account_id=? AND NOT (?=ANY(story_completed)) AND NOT (? AND kanto_opening_complete)"
                : "UPDATE characters SET story_completed=ARRAY(SELECT v FROM unnest(story_completed) v WHERE NOT(v=ANY(?::text[]))),kanto_opening_complete=CASE WHEN ? THEN FALSE ELSE kanto_opening_complete END WHERE id=? AND account_id=?";
        try (var c = database.connect(); var q = c.prepareStatement(sql)) {
            var affected = completed ? StoryProgressPackets.completionScenes(scene) : StoryProgressPackets.resetScenes(scene);
            var array = c.createArrayOf("text", affected.toArray(String[]::new));
            try {
                boolean opening = affected.contains(StoryProgressPackets.OPENING);
                q.setArray(1, array); q.setBoolean(2, opening); q.setLong(3, character); q.setInt(4, account);
                if (completed) { q.setString(5, scene); q.setBoolean(6, opening); }
                return q.executeUpdate() == 1;
            } finally { array.free(); }
        }
    }

    public boolean resetStoryCheckpoint(int account, long character, String scene, Position target) throws SQLException {
        if (StoryProgressPackets.scene(scene) == null || !target.valid(0)) throw new IllegalArgumentException("Invalid story checkpoint");
        try (var c = database.connect(); var q = c.prepareStatement(
                "UPDATE characters SET story_completed=ARRAY(SELECT v FROM unnest(story_completed) v WHERE NOT(v=ANY(?::text[]))),kanto_opening_complete=CASE WHEN ? THEN FALSE ELSE kanto_opening_complete END,map_group=?,map_id=?,position_x=?,position_y=?,elevation=?,facing=?,return_position=ARRAY[]::smallint[] WHERE id=? AND account_id=? AND region=0")) {
            var affected = StoryProgressPackets.resetScenes(scene);
            var array = c.createArrayOf("text", affected.toArray(String[]::new));
            try {
                q.setArray(1, array); q.setBoolean(2, affected.contains(StoryProgressPackets.OPENING));
                q.setInt(3, target.group()); q.setInt(4, target.map()); q.setInt(5, target.x()); q.setInt(6, target.y());
                q.setInt(7, target.elevation()); q.setInt(8, target.facing()); q.setLong(9, character); q.setInt(10, account);
                return q.executeUpdate() == 1;
            } finally { array.free(); }
        }
    }

    public boolean saveDebugSettings(int account, long character, boolean noclip, int multiplier) throws SQLException {
        if (multiplier != 1 && multiplier != 2 && multiplier != 4 && multiplier != 8) return false;
        try (var c = database.connect(); var q = c.prepareStatement(
                "UPDATE characters SET debug_noclip=?,debug_speed_multiplier=? WHERE id=? AND account_id=?")) {
            q.setBoolean(1, noclip); q.setInt(2, multiplier); q.setLong(3, character); q.setInt(4, account);
            return q.executeUpdate() == 1;
        }
    }

    public String gbaStoryState(int account,long character)throws SQLException {
        try(var c=database.connect();var q=c.prepareStatement("SELECT gba_story_state FROM characters WHERE id=? AND account_id=?")){
            q.setLong(1,character);q.setInt(2,account);try(var r=q.executeQuery()){if(!r.next())throw new SQLException("Story character not owned");return r.getString(1);}
        }
    }

    public String gbaStoryState(int account,long character,int region)throws SQLException {
        if(region==0)return gbaStoryState(account,character);
        if(region!=1)throw new IllegalArgumentException("Unsupported script region");
        try(var c=database.connect();var q=c.prepareStatement("SELECT gba_hoenn_state FROM characters WHERE id=? AND account_id=?")){
            q.setLong(1,character);q.setInt(2,account);try(var r=q.executeQuery()){if(!r.next())throw new SQLException("Story character not owned");return r.getString(1);}
        }
    }

    public void saveGbaStory(int account,long character,int region,String state,Position p,int town,int lab)throws SQLException {
        try(var c=database.connect()){saveGbaRegion(c,account,character,region,state,p,town,lab);}
    }

    private static void saveGbaRegion(Connection c,int account,long character,int region,String state,Position p,int town,int lab)throws SQLException {
        if(region==0){saveGbaStory(c,account,character,state,p,town,lab);return;}
        if(region!=1)throw new IllegalArgumentException("Unsupported script region");
        var back=p.returnPoint();var array=c.createArrayOf("smallint",back==null?new Integer[0]:new Integer[]{back.group(),back.map(),back.x(),back.y(),back.facing()});
        try(var q=c.prepareStatement("UPDATE characters SET gba_hoenn_state=?,map_group=?,map_id=?,position_x=?,position_y=?,elevation=?,facing=?,return_position=? WHERE id=? AND account_id=? AND region=1")){
            q.setString(1,state);q.setInt(2,p.group());q.setInt(3,p.map());q.setInt(4,p.x());q.setInt(5,p.y());q.setInt(6,p.elevation());q.setInt(7,p.facing());q.setArray(8,array);q.setLong(9,character);q.setInt(10,account);if(q.executeUpdate()!=1)throw new SQLException("Hoenn story save rejected");
        }finally{array.free();}
    }

    public void saveGbaStory(int account,long character,String state,Position p,int townStage,int labStage)throws SQLException {
        try(var c=database.connect()){saveGbaStory(c,account,character,state,p,townStage,labStage);}
    }

    /** 整体替换关都剧情完成集合，与 ROM 状态和落点一起提交。 */
    public void saveKantoCheckpoint(int account, long character, String state, Position position,
            int townStage, int labStage) throws SQLException {
        try (var connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                try (var query = connection.prepareStatement("UPDATE characters SET story_completed=ARRAY(SELECT v FROM unnest(story_completed) v WHERE v NOT LIKE 'kanto.%') WHERE id=? AND account_id=? AND region=0")) {
                    query.setLong(1, character);
                    query.setInt(2, account);
                    if (query.executeUpdate() != 1) throw new SQLException("Story character not owned");
                }
                saveGbaStory(connection, account, character, state, position, townStage, labStage);
                connection.commit();
            } catch (SQLException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    private static void saveGbaStory(Connection c,int account,long character,String state,Position p,int townStage,int labStage)throws SQLException {
        var completed=new java.util.ArrayList<String>();
        if(townStage>=1){completed.add(StoryProgressPackets.OPENING);completed.add(StoryProgressPackets.ESCORT);}
        if(labStage>=2)completed.add(StoryProgressPackets.LAB_INTRODUCTION);
        var affected=StoryProgressPackets.resetScenes(StoryProgressPackets.OPENING);
        var remove=c.createArrayOf("text",affected.toArray(String[]::new));var add=c.createArrayOf("text",completed.toArray(String[]::new));
        try(var q=c.prepareStatement("UPDATE characters SET gba_story_state=?,kanto_opening_complete=?,story_completed=ARRAY(SELECT DISTINCT v FROM unnest(ARRAY(SELECT v FROM unnest(story_completed) v WHERE NOT(v=ANY(?::text[]))) || ?::text[]) v),map_group=?,map_id=?,position_x=?,position_y=?,elevation=?,facing=?,return_position=ARRAY[]::smallint[] WHERE id=? AND account_id=? AND region=0")){
            q.setString(1,state);q.setBoolean(2,townStage>=1);q.setArray(3,remove);q.setArray(4,add);q.setInt(5,p.group());q.setInt(6,p.map());q.setInt(7,p.x());q.setInt(8,p.y());q.setInt(9,p.elevation());q.setInt(10,p.facing());q.setLong(11,character);q.setInt(12,account);
            if(q.executeUpdate()!=1)throw new SQLException("GBA state save rejected");
        }finally{remove.free();add.free();}
    }

    /** ROM 奖励、个体和下一指令地址同事务提交，断线重试不能重复发放。 */
    public int gbaStoryEffect(int account,long character,String operation,int first,int second,String argument,String state,Position p,int townStage,int labStage)throws SQLException {
        return gbaStoryEffect(account,character,0,operation,first,second,argument,state,p,townStage,labStage);
    }

    public int gbaStoryEffect(int account,long character,int region,String operation,int first,int second,String argument,String state,Position p,int townStage,int labStage)throws SQLException {
        try(var c=database.connect()){
            c.setAutoCommit(false);
            try {
                String name;
                java.util.ArrayList<PartyMember> party;
                try(var q=c.prepareStatement("SELECT name,party_species,party_flags,party_hp,party_max_hp,party_levels FROM characters WHERE id=? AND account_id=? FOR UPDATE")){q.setLong(1,character);q.setInt(2,account);try(var r=q.executeQuery()){if(!r.next())throw new SQLException("Story character not owned");name=r.getString(1);party=new java.util.ArrayList<>(PokemonStore.load(c,character,party(r.getArray(2),r.getArray(3),r.getArray(4),r.getArray(5),r.getArray(6))));}}
                int result=1;
                if(operation.equals("pokemon")){
                    int instruction=Integer.parseInt(argument);boolean given=false;
                    try(var q=c.prepareStatement("SELECT species,individual_id FROM character_script_gifts WHERE character_id=? AND rom_hash=? AND instruction=?")){q.setLong(1,character);q.setString(2,top.totodile.pokemmo.protocol.GbaScriptProgram.forRegion(region).romHash());q.setInt(3,instruction);try(var r=q.executeQuery()){if(r.next()){if(r.getInt(1)!=first)throw new SQLException("Starter reward already granted with a different species");long giftId=r.getLong(2);result=party.stream().anyMatch(m->m.individual().id()==giftId)?0:1;given=true;}}}
                    if(!given){
                        var d=top.totodile.pokemmo.protocol.PokemonIndividual.create(first,second,false,character,name,region,p.group(),p.map(),java.time.Instant.now().getEpochSecond());
                        for(var t:top.totodile.pokemmo.protocol.TrainerCatalog.scriptTrainersForRegion(region)){for(var m:t.mons())if(m.species()==first&&m.level()==second)d=d.battleState(m.moves(),"");}
                        int hp=d.stat(first,second,0);var mon=new PartyMember(first,false,false,hp,hp,second,d);
                        if(party.size()<6){party.add(mon);result=0;}else{
                            var boxes=new java.util.ArrayList<>(PokemonStore.loadBoxes(c,character));int slot=-1;
                            for(int i=0;i<14*30;i++){int box=i/30,s=i%30;if(boxes.stream().noneMatch(b->b.box()==box&&b.slot()==s)){slot=i;break;}}
                            if(slot<0)throw new SQLException("No space for script Pokemon");boxes.add(new PokemonStore.Boxed(slot/30,slot%30,mon));PokemonStore.saveBoxes(c,character,boxes);result=1;
                        }
                        try(var q=c.prepareStatement("INSERT INTO character_script_gifts(character_id,rom_hash,instruction,individual_id,species) VALUES(?,?,?,?,?)")){q.setLong(1,character);q.setString(2,top.totodile.pokemmo.protocol.GbaScriptProgram.forRegion(region).romHash());q.setInt(3,instruction);q.setLong(4,d.id());q.setInt(5,first);q.executeUpdate();}
                    }
                }else if(operation.equals("heal")){
                    for(int i=0;i<party.size();i++){var m=party.get(i);var moves=m.individual().moves().stream().map(v->new top.totodile.pokemmo.protocol.PokemonIndividual.Move(v.id(),v.maxPp(),v.maxPp(),v.ppUps())).toList();party.set(i,new PartyMember(m.species(),m.shiny(),m.alpha(),m.maxHp(),m.maxHp(),m.level(),m.individual().battleState(moves,"")));}
                }else if(operation.equals("nickname")){
                    if(argument.length()>16)throw new SQLException("Invalid script nickname");
                    long gift=0;
                    // 当前开场目录只有一次 givemon，旧角色的测试队伍不能改变奖励个体的命名目标。
                    try(var q=c.prepareStatement("SELECT individual_id FROM character_script_gifts WHERE character_id=? AND rom_hash=?")){
                        q.setLong(1,character);q.setString(2,top.totodile.pokemmo.protocol.GbaScriptProgram.forRegion(region).romHash());
                        try(var r=q.executeQuery()){if(r.next())gift=r.getLong(1);if(r.next())throw new SQLException("Ambiguous script nickname target");}
                    }
                    int index=first;
                    if(gift!=0){index=-1;for(int i=0;i<party.size();i++)if(party.get(i).individual().id()==gift)index=i;}
                    if(index>=0&&index<party.size()){
                        var m=party.get(index);party.set(index,new PartyMember(m.species(),m.shiny(),m.alpha(),m.hp(),m.maxHp(),m.level(),m.individual().withNickname(argument)));
                    }else{
                        var boxes=new java.util.ArrayList<>(PokemonStore.loadBoxes(c,character));boolean found=false;
                        for(int i=0;i<boxes.size();i++){var b=boxes.get(i);var m=b.member();if(m.individual().id()==gift){boxes.set(i,new PokemonStore.Boxed(b.box(),b.slot(),new PartyMember(m.species(),m.shiny(),m.alpha(),m.hp(),m.maxHp(),m.level(),m.individual().withNickname(argument))));found=true;break;}}
                        if(!found)throw new SQLException("Script nickname target missing");PokemonStore.saveBoxes(c,character,boxes);
                    }
                }else if(operation.equals("starter-choice")){
                    result=-1;try(var q=c.prepareStatement("SELECT species FROM character_script_gifts WHERE character_id=? AND rom_hash=? AND instruction=?")){q.setLong(1,character);q.setString(2,top.totodile.pokemmo.protocol.GbaScriptProgram.forRegion(region).romHash());q.setInt(3,first);try(var r=q.executeQuery()){if(r.next())result=switch(r.getInt(1)){case 252->0;case 255->1;case 258->2;default->throw new SQLException("Invalid saved starter species");};}}
                }else if(operation.equals("party-count")){result=party.size();
                }else if(operation.equals("lead-species")||operation.equals("party-species")){int index=operation.equals("lead-species")?0:first;result=index>=0&&index<party.size()?party.get(index).species():0;
                }else if(operation.equals("clock-read")){try(var q=c.prepareStatement("SELECT (gba_hoenn_clock+FLOOR(EXTRACT(EPOCH FROM(NOW()-gba_hoenn_clock_set_at))/60)::integer)%1440 FROM characters WHERE id=? AND account_id=?")){q.setLong(1,character);q.setInt(2,account);try(var r=q.executeQuery()){r.next();result=r.getInt(1);}}
                }else if(operation.equals("clock")){if(first<0||first>1439)throw new SQLException("Invalid clock time");try(var q=c.prepareStatement("UPDATE characters SET gba_hoenn_clock=?,gba_hoenn_clock_set_at=NOW() WHERE id=? AND account_id=?")){q.setInt(1,first);q.setLong(2,character);q.setInt(3,account);q.executeUpdate();}
                }else if(operation.equals("dex-unlock")||operation.equals("dex-state")){try(var q=c.prepareStatement("UPDATE characters SET hoenn_pokedex_obtained=? WHERE id=? AND account_id=?")){q.setBoolean(1,first!=0);q.setLong(2,character);q.setInt(3,account);q.executeUpdate();}
                }else if(operation.startsWith("item-")){
                    boolean duplicate=false;
                    if(operation.equals("item-add")&&!argument.isBlank()){
                        try(var q=c.prepareStatement("INSERT INTO character_script_item_gifts(character_id,rom_hash,instruction,item_id,quantity) VALUES(?,?,?,?,?) ON CONFLICT DO NOTHING")){
                            q.setLong(1,character);q.setString(2,top.totodile.pokemmo.protocol.GbaScriptProgram.forRegion(region).romHash());q.setInt(3,Integer.parseInt(argument));q.setInt(4,first);q.setInt(5,second);duplicate=q.executeUpdate()==0;
                        }
                    }
                    int owned=0;try(var q=c.prepareStatement("SELECT quantity FROM character_inventory WHERE character_id=? AND item_id=?")){q.setLong(1,character);q.setInt(2,first);try(var r=q.executeQuery()){if(r.next())owned=r.getInt(1);}}
                    if(operation.equals("item-check"))result=owned>=second?1:0;
                    else if(!duplicate){int quantity=operation.equals("item-add")?owned+second:owned-second;if(quantity<0)result=0;else if(quantity==0){try(var q=c.prepareStatement("DELETE FROM character_inventory WHERE character_id=? AND item_id=?")){q.setLong(1,character);q.setInt(2,first);q.executeUpdate();}}else try(var q=c.prepareStatement("INSERT INTO character_inventory(character_id,item_id,quantity) VALUES(?,?,?) ON CONFLICT(character_id,item_id) DO UPDATE SET quantity=EXCLUDED.quantity")){q.setLong(1,character);q.setInt(2,first);q.setInt(3,quantity);q.executeUpdate();}}
                }else throw new SQLException("Unknown script host operation");
                savePartyArrays(c,account,character,party);PokemonStore.save(c,character,party);
                saveGbaRegion(c,account,character,region,state,p,townStage,labStage);c.commit();return result;
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
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
