package top.totodile.pokemmo.server.game;

import java.util.*;
import top.totodile.pokemmo.db.Database;
import java.util.concurrent.*;
import java.util.function.Supplier;
import top.totodile.pokemmo.server.login.LoginService.Selection;

/** 单机角色服务；所有数据库操作在同一后台线程执行。 */
public final class LocalGameService implements GameService {
    private final Supplier<Database> factory;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "local-game"); t.setDaemon(true); return t;
    });
    private Database database;
    private final Map<String,Position> mailboxes=new HashMap<>();
    private final Map<String,Long> playStarted=new HashMap<>();
    private final java.util.concurrent.atomic.AtomicBoolean closed = new java.util.concurrent.atomic.AtomicBoolean();
    public LocalGameService(Supplier<Database> factory) { this.factory = factory; }
    @Override public byte[] pokedexTiers(){return PokedexTierData.payload();}
    private Database db() {
        if (database == null) {
            Database opened = Objects.requireNonNull(factory.get());
            try {
                opened.execute("PRAGMA foreign_keys=ON");
                opened.execute("CREATE TABLE IF NOT EXISTS characters (id TEXT PRIMARY KEY, account TEXT NOT NULL, server TEXT NOT NULL, name TEXT NOT NULL, name_key TEXT NOT NULL, gender INTEGER NOT NULL, skin INTEGER NOT NULL, region INTEGER NOT NULL, created_at INTEGER NOT NULL, UNIQUE(server,name_key))");
                // Reference character.money defaults to zero; migrate existing local characters in place.
                if(opened.query("PRAGMA table_info(characters)").stream().noneMatch(row -> "money".equals(row[1])))
                    opened.execute("ALTER TABLE characters ADD COLUMN money INTEGER NOT NULL DEFAULT 0");
                opened.execute("CREATE TABLE IF NOT EXISTS character_parts (character_id TEXT NOT NULL REFERENCES characters(id) ON DELETE CASCADE, slot INTEGER NOT NULL, style INTEGER NOT NULL, color INTEGER NOT NULL, PRIMARY KEY(character_id,slot))");
                opened.execute("CREATE TABLE IF NOT EXISTS character_positions (character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE, bank INTEGER NOT NULL, map INTEGER NOT NULL, x INTEGER NOT NULL, y INTEGER NOT NULL, direction INTEGER NOT NULL)");
                opened.execute("CREATE TABLE IF NOT EXISTS character_current_region (character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE,region INTEGER NOT NULL)");
                opened.execute("CREATE TABLE IF NOT EXISTS character_travel_state (character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE, no_clip INTEGER NOT NULL, speed REAL NOT NULL, bicycle INTEGER NOT NULL)");
                opened.execute("CREATE TABLE IF NOT EXISTS character_pokedex_state (character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE, unlocked INTEGER NOT NULL)");
                PlayerDataStore.initialize(opened);
                PokemonStore.initialize(opened);
                database = opened;
            } catch (RuntimeException | Error error) {
                try { opened.close(); }
                catch (RuntimeException | Error closeError) { error.addSuppressed(closeError); }
                throw error;
            }
        }
        return database;
    }
    private static void identity(Selection selection) {
        Objects.requireNonNull(selection);
        if (selection.session().username().isBlank()) throw new IllegalArgumentException("Invalid local identity");
    }
    @Override public CompletionStage<List<Character>> characters(Selection selection) {
        return CompletableFuture.supplyAsync(() -> {
            identity(selection);
            List<Character> result = new ArrayList<>();
            for (String[] row : db().query("SELECT id,name,gender,skin,region,created_at FROM characters WHERE account=? AND server=? ORDER BY created_at,id",
                    selection.session().username(), selection.server().id())) {
                Map<Integer,Part> parts = new HashMap<>();
                for (String[] part : db().query("SELECT slot,style,color FROM character_parts WHERE character_id=?", row[0]))
                    parts.put(Integer.parseInt(part[0]), new Part(Integer.parseInt(part[1]), Integer.parseInt(part[2])));
                result.add(new Character(row[0], row[1], new Appearance(Integer.parseInt(row[2]), Integer.parseInt(row[3]), Integer.parseInt(row[4]), parts), Long.parseLong(row[5])));
            }
            return List.copyOf(result);
        }, worker);
    }
    @Override public CompletionStage<Character> create(Selection selection, String name, Appearance appearance) {
        return CompletableFuture.supplyAsync(() -> {
            identity(selection);
            if (name == null || name.codePointCount(0, name.length()) < 2 || name.codePointCount(0, name.length()) > 16
                    || !name.codePoints().allMatch(java.lang.Character::isLetter)) throw new Rejected(Failure.INVALID_NAME);
            if (appearance.gender() < 0 || appearance.gender() > 1 || appearance.skin() < 0 || appearance.skin() > 4
                    || appearance.region() < 0 || appearance.region() > 4 || !appearance.parts().keySet().equals(Set.of(1,2,3,4,5,6,7,8,9,11)))
                throw new IllegalArgumentException("Invalid character appearance");
            String key = name.toLowerCase(Locale.ROOT), server = selection.server().id();
            Character character = new Character(UUID.randomUUID().toString(), name, appearance, System.currentTimeMillis());
            db().transaction(() -> {
                if (Integer.parseInt(db().query("SELECT COUNT(*) FROM characters WHERE account=?", selection.session().username()).get(0)[0]) >= 3)
                    throw new Rejected(Failure.ACCOUNT_FULL);
                if (!db().query("SELECT id FROM characters WHERE server=? AND name_key=?", server, key).isEmpty())
                    throw new Rejected(Failure.NAME_TAKEN);
                db().execute("INSERT INTO characters (id,account,server,name,name_key,gender,skin,region,created_at) VALUES (?,?,?,?,?,?,?,?,?)", character.id(), selection.session().username(), server, name, key,
                        appearance.gender(), appearance.skin(), appearance.region(), character.createdAt());
                appearance.parts().forEach((slot, part) -> db().execute("INSERT INTO character_parts VALUES (?,?,?,?)", character.id(), slot, part.style(), part.color()));
            });
            return character;
        }, worker);
    }
    @Override public CompletionStage<Void> delete(Selection selection, String characterId) {
        return CompletableFuture.runAsync(() -> {
            identity(selection);
            db().transaction(() -> {
                String[] values = {characterId, selection.session().username(), selection.server().id()};
                if (db().query("SELECT id FROM characters WHERE id=? AND account=? AND server=?", values).isEmpty())
                    throw new Rejected(Failure.NOT_FOUND);
                db().execute("DELETE FROM characters WHERE id=? AND account=? AND server=?", (Object[])values);
                playStarted.remove(characterId);mailboxes.remove(characterId);
            });
        }, worker);
    }
    @Override public CompletionStage<Entry> enter(Selection selection, String characterId) {
        return characters(selection).thenApplyAsync(values -> {
            Character character=values.stream().filter(c -> c.id().equals(characterId)).findFirst()
                    .orElseThrow(() -> new Rejected(Failure.NOT_FOUND));
            var rows=db().query("SELECT bank,map,x,y,direction FROM character_positions WHERE character_id=?",characterId);
            int region=currentRegion(characterId,character.appearance().region());
            Position position=Position.start(region);
            if(!rows.isEmpty()) {
                String[] row=rows.get(0);
                position=new Position(Integer.parseInt(row[0]),Integer.parseInt(row[1]),Integer.parseInt(row[2]),
                        Integer.parseInt(row[3]),Integer.parseInt(row[4]));
            }
            var states=db().query("SELECT no_clip,speed,bicycle FROM character_travel_state WHERE character_id=?",characterId);
            TravelState state=TravelState.defaults();
            if(!states.isEmpty()) {
                String[] row=states.get(0);
                state=new TravelState(Integer.parseInt(row[0])!=0,Float.parseFloat(row[1]),Integer.parseInt(row[2])!=0);
            }
            var pokedex=db().query("SELECT unlocked FROM character_pokedex_state WHERE character_id=?",characterId);
            int money=Integer.parseInt(db().query("SELECT money FROM characters WHERE id=?",characterId).get(0)[0]);
            WorldInfo defaults=WorldInfo.defaults();
            playStarted.putIfAbsent(characterId,System.nanoTime());
            return new Entry(character,position,state,pokedex.isEmpty()?PokedexState.defaults():new PokedexState(Integer.parseInt(pokedex.get(0)[0])!=0),
                    new WorldInfo(money,defaults.channel(),defaults.timeOrigin()),region);
        },worker);
    }
    @Override public CompletionStage<Void> savePosition(Selection selection,String characterId,Position position) {
        Objects.requireNonNull(position);
        return CompletableFuture.runAsync(() -> {
            identity(selection);
            db().transaction(() -> {
                if(db().query("SELECT id FROM characters WHERE id=? AND account=? AND server=?",characterId,
                        selection.session().username(),selection.server().id()).isEmpty()) throw new Rejected(Failure.NOT_FOUND);
                db().execute("INSERT OR REPLACE INTO character_positions VALUES (?,?,?,?,?,?)",characterId,
                        position.bank(),position.map(),position.x(),position.y(),position.direction());
                if(!position.equals(mailboxes.get(characterId)))mailboxes.remove(characterId);
                checkpointPlaytime(characterId);
            });
        },worker);
    }
    private int currentRegion(String id,int fallback){var rows=db().query("SELECT region FROM character_current_region WHERE character_id=?",id);return rows.isEmpty()?fallback:Integer.parseInt(rows.get(0)[0]);}
    @Override public CompletionStage<Void> teleport(Selection selection,String id,int region,Position position){
        Objects.requireNonNull(position);
        return CompletableFuture.runAsync(()->{
            playerData(selection,id);if(region<0||region>4)throw new IllegalArgumentException("Unknown region: "+region);
            db().transaction(()->{db().execute("INSERT OR REPLACE INTO character_current_region VALUES (?,?)",id,region);db().execute("INSERT OR REPLACE INTO character_positions VALUES (?,?,?,?,?,?)",id,position.bank(),position.map(),position.x(),position.y(),position.direction());mailboxes.remove(id);checkpointPlaytime(id);});
        },worker);
    }
    private volatile Map<Integer,ItemTemplate> itemTemplates=Map.of();
    private volatile Map<Integer,SpeciesTemplate> speciesTemplates=Map.of();
    @Override public void gameData(List<ItemTemplate> items,List<SpeciesTemplate> species){
        itemTemplates=items.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(ItemTemplate::id,java.util.function.Function.identity()));
        speciesTemplates=species.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(SpeciesTemplate::id,java.util.function.Function.identity()));
    }
    @Override public CompletionStage<PlayerData.Item> createItem(Selection selection,String id,int itemId,int amount){
        return CompletableFuture.supplyAsync(()->{
            var store=playerData(selection,id);if(itemId<1||itemId>Short.MAX_VALUE||!itemTemplates.containsKey(itemId))throw new IllegalArgumentException("Unknown item id: "+itemId);
            return store.createItem(id,itemId,amount);
        },worker);
    }
    @Override public List<ItemTemplate> itemTemplates(){return itemTemplates.values().stream().sorted(Comparator.comparingInt(ItemTemplate::id)).toList();}
    @Override public CompletionStage<PokemonData.Pokemon> addPokemon(Selection selection,String id,int species,int level){
        return CompletableFuture.supplyAsync(()->{playerData(selection,id);return new PokemonStore(db()).add(id,speciesTemplates.get(species),level,new Random());},worker);
    }
    @Override public CompletionStage<Void> saveTravelState(Selection selection,String characterId,TravelState state) {
        Objects.requireNonNull(state);
        return CompletableFuture.runAsync(() -> {
            identity(selection);
            db().transaction(() -> {
                if(db().query("SELECT id FROM characters WHERE id=? AND account=? AND server=?",characterId,
                        selection.session().username(),selection.server().id()).isEmpty())throw new Rejected(Failure.NOT_FOUND);
                db().execute("INSERT OR REPLACE INTO character_travel_state VALUES (?,?,?,?)",characterId,
                        state.noClip()?1:0,state.speed(),state.bicycle()?1:0);
            });
        },worker);
    }
    @Override public CompletionStage<Void> savePokedexState(Selection selection,String characterId,PokedexState state) {
        Objects.requireNonNull(state);
        return CompletableFuture.runAsync(() -> {
            identity(selection);
            db().transaction(() -> {
                if(db().query("SELECT id FROM characters WHERE id=? AND account=? AND server=?",characterId,
                        selection.session().username(),selection.server().id()).isEmpty())throw new Rejected(Failure.NOT_FOUND);
                db().execute("INSERT OR REPLACE INTO character_pokedex_state VALUES (?,?)",characterId,state.unlocked()?1:0);
            });
        },worker);
    }
    private PlayerDataStore playerData(Selection selection,String characterId) {
        identity(selection);
        var store=new PlayerDataStore(db());store.requireOwner(selection,characterId);return store;
    }
    @Override public CompletionStage<List<PlayerData.Item>> inventory(Selection selection,String id){
        return CompletableFuture.supplyAsync(()->playerData(selection,id).inventory(id),worker);
    }
    @Override public CompletionStage<PokemonData.Containers> pokemon(Selection selection,String id){
        return CompletableFuture.supplyAsync(()->{playerData(selection,id);return new PokemonStore(db()).load(id);},worker);
    }
    @Override public CompletionStage<PokemonData.Containers> movePokemon(Selection selection,String id,List<PokemonData.Move> moves){
        var snapshot=moves==null?null:List.copyOf(moves);
        return CompletableFuture.supplyAsync(()->{playerData(selection,id);return new PokemonStore(db()).move(id,snapshot);},worker);
    }
    @Override public CompletionStage<PokemonData.Containers> sortPokemon(Selection selection,String id,int box){
        return CompletableFuture.supplyAsync(()->{playerData(selection,id);return new PokemonStore(db()).sort(id,box);},worker);
    }
    @Override public CompletionStage<PlayerData.Trainer> trainer(Selection selection,String id){
        return characters(selection).thenApplyAsync(values->{
            var character=values.stream().filter(c->c.id().equals(id)).findFirst().orElseThrow(()->new Rejected(Failure.NOT_FOUND));
            checkpointPlaytime(id);return playerData(selection,id).trainer(character);
        },worker);
    }
    @Override public CompletionStage<PlayerData.MailPage> mail(Selection selection,String id,boolean sent,int page){
        return CompletableFuture.supplyAsync(()->playerData(selection,id).mail(id,sent,page),worker);
    }
    @Override public CompletionStage<PlayerData.Mail> readMail(Selection selection,String id,String mailId){
        return CompletableFuture.supplyAsync(()->playerData(selection,id).read(id,mailId),worker);
    }
    @Override public CompletionStage<Void> deleteMail(Selection selection,String id,String mailId,boolean sent){
        return CompletableFuture.runAsync(()->playerData(selection,id).delete(id,mailId,sent),worker);
    }
    private Position savedPosition(String id){
        var rows=db().query("SELECT bank,map,x,y,direction FROM character_positions WHERE character_id=?",id);
        if(rows.isEmpty())return null;var r=rows.get(0);
        return new Position(Integer.parseInt(r[0]),Integer.parseInt(r[1]),Integer.parseInt(r[2]),Integer.parseInt(r[3]),Integer.parseInt(r[4]));
    }
    public static boolean mailboxTarget(Position p,int region){
        // PcInteractionService.isPcTarget: the confirmed Kanto centre PC.
        return PlayerData.pcTarget(p,region);
    }
    @Override public CompletionStage<Boolean> openMailbox(Selection selection,String id){
        return CompletableFuture.supplyAsync(()->{
            playerData(selection,id);mailboxes.remove(id);var p=savedPosition(id);
            int region=currentRegion(id,Integer.parseInt(db().query("SELECT region FROM characters WHERE id=?",id).get(0)[0]));
            if(!mailboxTarget(p,region))return false;mailboxes.put(id,p);return true;
        },worker);
    }
    @Override public CompletionStage<Void> closeMailbox(Selection selection,String id){
        return CompletableFuture.runAsync(()->{playerData(selection,id);mailboxes.remove(id);},worker);
    }
    @Override public CompletionStage<PlayerData.MailResult> sendMail(Selection selection,String id,String recipient,String subject,String body){
        return CompletableFuture.supplyAsync(()->{
            var store=playerData(selection,id);var p=mailboxes.get(id);
            if(p==null||!p.equals(savedPosition(id)))return PlayerData.MailResult.ACCESS_DENIED;
            return store.send(id,recipient,subject,body);
        },worker);
    }
    private void checkpointPlaytime(String id){
        Long start=playStarted.get(id);if(start==null)return;long seconds=(System.nanoTime()-start)/1_000_000_000L;
        if(seconds>0){new PlayerDataStore(db()).addPlayedSeconds(id,seconds);playStarted.put(id,start+seconds*1_000_000_000L);}
    }
    @Override public CompletionStage<Void> leave(Selection selection,String id){
        return CompletableFuture.runAsync(()->{playerData(selection,id);checkpointPlaytime(id);playStarted.remove(id);mailboxes.remove(id);},worker);
    }
    @Override public void close() {
        if (!closed.compareAndSet(false, true)) return;
        worker.execute(() -> { if (database != null) {for(String id:new ArrayList<>(playStarted.keySet()))checkpointPlaytime(id);database.close();} });
        worker.shutdown();
    }
}
