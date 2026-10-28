package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import top.totodile.pokemmo.server.login.LoginService.Selection;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** 单机角色服务；所有数据库操作在同一后台线程执行。 */
public final class LocalGameService implements GameService {
    @Override public CompletionStage<List<GiftShopData.Item>> giftShop(Selection selection,String id) {
        return CompletableFuture.supplyAsync(()->{playerData(selection,id);return GiftShopData.items();},worker);
    }
    @Override public CompletionStage<StoryDebug.State> storyDebugState(Selection selection,String id) {
        return CompletableFuture.supplyAsync(()->{
            playerData(selection,id);
            if(currentRegion(id,0)!=0&&!((StoryTestDatabase)db()).active())throw new IllegalStateException("目前仅支持关都剧情调试");
            return StoryDebug.state(db(),id,((StoryTestDatabase)db()).active());
        },worker);
    }
    @Override public CompletionStage<Void> editStory(Selection selection,String id,StoryDebug.Request request) {
        return CompletableFuture.runAsync(()->{
            playerData(selection,id);
            if(request.mode()!=StoryDebug.Mode.RESTORE&&currentRegion(id,0)!=0)throw new IllegalStateException("目前仅支持关都剧情调试");
            if(request.mode()!=StoryDebug.Mode.RESTORE&&(battles.containsKey(id)||persistentBattles.contains(id)))throw new IllegalStateException("请先结束对战");
            var session=stories.get(id);
            if(request.mode()!=StoryDebug.Mode.RESTORE&&session!=null&&session.waitingAction()!=null)throw new IllegalStateException("请先完成当前对话或剧情");
            var database=(StoryTestDatabase)db();
            if(request.mode()==StoryDebug.Mode.RESTORE) {
                restoreStoryTest();
                return;
            }
            if(request.mode()==StoryDebug.Mode.PERMANENT&&database.active())
                throw new IllegalStateException("请先结束临时测试并恢复存档，再永久应用进度");
            boolean started=request.mode()==StoryDebug.Mode.TEMPORARY&&!database.active();
            if(started)database.begin(id);
            try { StoryDebug.apply(database,id,request.values()); }
            catch(RuntimeException|Error e) { if(started)database.restore();throw e; }
            stories.remove(id);
            wildRolls.remove(id);
        },worker);
    }
    private void restoreStoryTest() {
        if(database instanceof StoryTestDatabase test&&test.active()) {
            for(String id:new ArrayList<>(battles.keySet()))closeBattle(id);
            test.restore();
            stories.clear();storyBattles.clear();encounters.clear();persistentBattles.clear();
            progress.clear();wildRolls.clear();mailboxes.clear();playStarted.clear();
        }
    }
    private final Map<String,KantoStory> stories=new HashMap<>();
    private final Set<String> storyBattles=new HashSet<>();
    private final top.totodile.pokemmo.server.game.battle.ShowdownBattleEngine readyBattleEngine=new top.totodile.pokemmo.server.game.battle.ShowdownBattleEngine();
    @Override public CompletionStage<Void> prepareBattle(){return CompletableFuture.runAsync(readyBattleEngine::prepare);}

    @Override public CompletionStage<StoryInspection.Report> inspectStory(Selection selection,String id,Position position) {
        return CompletableFuture.supplyAsync(()->{
            playerData(selection,id);
            var p=position==null?savedPosition(id):position;
            if(p==null)p=Position.start(currentRegion(id,0));
            var report=StoryInspection.inspect(db(),id,currentRegion(id,0),p,stories.get(id));
            if(((StoryTestDatabase)db()).active())return new StoryInspection.Report(report.region(),report.bank(),report.map(),report.mapKey(),report.state()+"\n【临时测试中：结束测试或离开角色后恢复存档】",report.nextSteps(),report.npcs());
            return report;
        },worker);
    }

    @Override public CompletionStage<StoryData.Scene> story(Selection selection,String id,StoryData.Request request) {
        return CompletableFuture.supplyAsync(()->{
            playerData(selection,id);
            if (battles.containsKey(id)) throw new IllegalStateException("Battle already active");
            var p=request.position() == null ? savedPosition(id) : request.position();
            if(p==null){
                int initial=Integer.parseInt(db().query("SELECT region FROM characters WHERE id=?",id).get(0)[0]);
                p=Position.start(currentRegion(id,initial));
            }
            var story=stories.computeIfAbsent(id,key->new KantoStory(db(),id,speciesTemplates));
            story.itemTemplates(itemTemplates);
            // An encounter already selected by an earlier accepted step owns the interaction.
            // Later predicted coordinate checks must not start a second story while it opens.
            if(persistentBattles.contains(id))return new StoryData.Scene(story.state(),null,story.entities(),false,false);
            var scene = story.request(request,p,currentRegion(id,0));
            int region=currentRegion(id,0);
            boolean shoes=region>=2?story.regionalFlag("shoes"):region==1?scene.state().enabled()&&scene.state().stage()>=16:
                    KantoMainline.event(db(),id,"running_shoe_status");
            return new StoryData.Scene(scene.state(), scene.action(), story.entities(), story.takePartyHealed(), story.takeInventoryChanged(),shoes,
                    TraversalAccess.surf(db(),id,region),TraversalAccess.bicycle(db(),id,region));
        },worker);
    }

    @Override public CompletionStage<Traversal> traversal(Selection selection,String id){
        return CompletableFuture.supplyAsync(()->{
            playerData(selection,id);
            int region=currentRegion(id,0);
            return new Traversal(TraversalAccess.surf(db(),id,region),TraversalAccess.bicycle(db(),id,region));
        },worker);
    }

    @Override public CompletionStage<EncounterData> storyBattle(Selection selection,String id) {
        return CompletableFuture.supplyAsync(()->{
            playerData(selection,id);
            var story=stories.get(id);if(story==null)throw new IllegalStateException("No story session");
            if(battles.containsKey(id))throw new IllegalStateException("Battle already active");
            var encounter=story.battle();closeBattle(id);storyBattles.add(id);
            encounters.put(id,encounter);persistentBattles.add(id);return encounter;
        },worker);
    }
    private final Map<String, top.totodile.pokemmo.server.game.battle.BattleEngine.Session>
            battles = new HashMap<>();

    private final Map<String, WildEncounters> wildRolls = new HashMap<>();
    private final Set<String> persistentBattles = new HashSet<>();
    private final Map<String, BattleProgress> progress = new HashMap<>();
    private volatile Map<Integer, BattleYield> battleYields = Map.of();

    @Override public void battleYields(List<BattleYield> yields) {
        var values = new HashMap<Integer, BattleYield>();
        for (var yield : yields) values.put(yield.species(), yield);
        battleYields = Map.copyOf(values);
    }

    @Override public CompletionStage<EncounterData> trainerEncounter(Selection selection,String id) {
        return CompletableFuture.supplyAsync(() -> {
            playerData(selection,id);
            if(battles.containsKey(id)) throw new IllegalStateException("Battle already active");
            var party=new PokemonStore(db(),speciesTemplates).load(id).pokemons().stream()
                    .filter(p->p.container()==PokemonData.PARTY)
                    .sorted(Comparator.comparingInt(PokemonData.Pokemon::position)).toList();
            var encounter=TrainerEncounters.create(party,speciesTemplates,new Random());
            if(encounter!=null) {closeBattle(id);encounters.put(id,encounter);persistentBattles.add(id);}
            return encounter;
        },worker);
    }

    @Override public CompletionStage<EncounterData> wildEncounterStep(Selection selection, String id,
            int region, Position before, Position after, int behavior, boolean land,
            boolean water, boolean bicycle) {
        return CompletableFuture.supplyAsync(() -> {
            playerData(selection, id);
            if (battles.containsKey(id)||persistentBattles.contains(id)) return null;
            var activeStory=stories.get(id);
            if(activeStory!=null&&activeStory.waitingAction()!=null)return null;
            var rolls = wildRolls.computeIfAbsent(id, ignored -> new WildEncounters(new Random()));
            var selected = rolls.step(region, before, after, behavior, land, water, bicycle);
            if (selected == null) return null;
            var party = new PokemonStore(db(), speciesTemplates).load(id).pokemons().stream()
                    .filter(p -> p.container() == PokemonData.PARTY)
                    .sorted(Comparator.comparingInt(PokemonData.Pokemon::position)).toList();
            var lead = party.stream().filter(p -> !p.egg() && p.hp() > 0).findFirst().orElse(null);
            var template = speciesTemplates.get(selected.species());
            if (lead == null || template == null) return null;
            closeBattle(id);
            var wild = PokemonStore.createNaturalEncounterPokemon(template, selected.level(), new Random());
            var encounter = new EncounterData(lead, wild, party);
            encounters.put(id, encounter);
            persistentBattles.add(id);
            return encounter;
        }, worker);
    }

    private final Set<String> moveTestBattles = new HashSet<>();

    @Override
    public CompletionStage<top.totodile.pokemmo.server.game.battle.BattleState> startMoveTestBattle(
            Selection selection, String id) {
        return CompletableFuture.supplyAsync(
                () -> {
                    playerData(selection, id);
                    var encounter = encounters.get(id);
                    if (encounter == null)
                        throw new IllegalStateException("No generated encounter");
                    closeBattle(id);
                    var session =
                            new top.totodile.pokemmo.server.game.battle.ShowdownBattleEngine()
                                    .startMoveTest(encounter);
                    battles.put(id, session);
                    moveTestBattles.add(id);
                    encounters.put(id, session.state().apply(encounter));
                    return session.state();
                },
                worker);
    }

    private void closeBattle(String id) {
        closeBattle(id,true);
    }
    private void closeBattle(String id,boolean forget) {
        if(forget&&database!=null)database.execute("DELETE FROM character_battle_checkpoint WHERE character_id=?",id);
        storyBattles.remove(id);
        if (moveTestBattles.remove(id)) encounters.remove(id);
        persistentBattles.remove(id);
        progress.remove(id);
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
                    boolean persistent = persistentBattles.contains(id);
                    boolean storyBattle = storyBattles.contains(id);
                    closeBattle(id);
                    if (persistent) persistentBattles.add(id);
                    if (storyBattle) storyBattles.add(id);
                    var session =
                            (kind==top.totodile.pokemmo.server.game.battle.BattleEngine.Kind.SHOWDOWN?readyBattleEngine:top.totodile.pokemmo.server.game.battle.BattleEngine.create(kind))
                                    .start(encounter);
                    battles.put(id, session);
                    if (persistent) progress.put(id, new BattleProgress(encounter));
                    checkpointBattle(id);
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
                    if (persistentBattles.contains(id) && action.type() == top.totodile.pokemmo.server.game.battle.BattleEngine.Type.TEST_MOVE)
                        throw new IllegalStateException("Effect tests cannot modify an overworld battle");
                    var chosen=session.choose(action);
                    top.totodile.pokemmo.server.game.battle.BattleState[] result={chosen};
                    db().transaction(()->{
                        var state=chosen;
                        if(persistentBattles.contains(id)){
                            state=progress.get(id).settle(db(),id,speciesTemplates,battleYields,state);
                            if(storyBattles.contains(id)&&state.ended())stories.get(id).finishBattle("Player".equals(state.winner()));
                            session.growth(state.growth());
                            var actual=session.state();
                            state=new top.totodile.pokemmo.server.game.battle.BattleState(state.engine(),state.turn(),state.ended(),state.winner(),state.forceSwitch(),actual.pokemon(),state.logs(),state.actions(),state.events(),state.growth());
                        }
                        encounters.put(id,state.apply(encounters.get(id)));checkpointBattle(id);result[0]=state;
                    });
                    return result[0];
                },
                worker);
    }

    @Override
    public CompletionStage<Void> endBattle(Selection selection, String id) {
        return CompletableFuture.runAsync(
                () -> {
                    playerData(selection, id);
                    // BattleFinishSuccessPacket restores HP/status/PP after a real defeat.
                    // Do not heal simulated previews, escapes, victories, or an aborted battle.
                    var battle = battles.get(id);
                    if (persistentBattles.contains(id) && battle != null && battle.state().ended()
                            && !"Player".equals(battle.state().winner())
                            && battle.state().pokemon().stream().anyMatch(p -> p.side() == 0)
                            && battle.state().pokemon().stream().filter(p -> p.side() == 0).allMatch(p -> p.hp() == 0)) {
                        db().transaction(() -> {
                            for (var pokemon : new PokemonStore(db(), speciesTemplates).load(id).pokemons())
                                if (pokemon.container() == 1)
                                    db().execute("UPDATE pokemon SET current_hp=?,status=0,moves_pp='-1,-1,-1,-1' WHERE id=? AND trainer_id=?",
                                            pokemon.maxHp(), pokemon.id(), id);
                        });
                    }
                    closeBattle(id);
                    storyBattles.remove(id);
                    stories.computeIfPresent(id,(key,story)->story.afterBattle());
                },
                worker);
    }

    private final Supplier<Database> factory;
    private final java.util.function.LongSupplier battleClock;
    static final long BATTLE_RECONNECT_MILLIS=300_000;
    @Override public CompletionStage<Void> disconnectBattle(Selection selection,String id){
        return CompletableFuture.runAsync(()->{playerData(selection,id);boolean keep=persistentBattles.contains(id);if(keep)checkpointBattle(id);closeBattle(id,!keep);},worker);
    }
    @Override public CompletionStage<Void> touchBattle(Selection selection,String id){
        return CompletableFuture.runAsync(()->{playerData(selection,id);if(battles.containsKey(id)&&persistentBattles.contains(id))db().execute("UPDATE character_battle_checkpoint SET expires_at=? WHERE character_id=?",battleClock.getAsLong()+BATTLE_RECONNECT_MILLIS,id);},worker);
    }

    private void checkpointBattle(String id){
        var session=battles.get(id);if(session==null||!persistentBattles.contains(id))return;
        String engine=session.checkpoint();if(engine==null)return;
        var gson=new com.google.gson.Gson();var data=new com.google.gson.JsonObject();
        data.addProperty("engine",engine);data.add("encounter",gson.toJsonTree(encounters.get(id)));
        data.add("progress",gson.toJsonTree(progress.get(id)));
        if(storyBattles.contains(id))data.add("story",stories.get(id).battleCheckpoint());
        db().execute("INSERT INTO character_battle_checkpoint VALUES (?,?,?) ON CONFLICT(character_id) DO UPDATE SET expires_at=excluded.expires_at,payload=excluded.payload",
                id,battleClock.getAsLong()+BATTLE_RECONNECT_MILLIS,gson.toJson(data));
    }
    @Override public CompletionStage<BattleResume> reconnectBattle(Selection selection,String id){
        return CompletableFuture.supplyAsync(()->{
            playerData(selection,id);
            var rows=db().query("SELECT expires_at,payload FROM character_battle_checkpoint WHERE character_id=?",id);
            if(rows.isEmpty())return null;
            long expires=Long.parseLong(rows.get(0)[0]);var gson=new com.google.gson.Gson();
            var data=com.google.gson.JsonParser.parseString(rows.get(0)[1]).getAsJsonObject();
            var session=battles.get(id);
            if(session==null){
                session=top.totodile.pokemmo.server.game.battle.ShowdownBattleEngine.restore(data.get("engine").getAsString());
                battles.put(id,session);persistentBattles.add(id);
                encounters.put(id,gson.fromJson(data.get("encounter"),EncounterData.class));
                progress.put(id,gson.fromJson(data.get("progress"),BattleProgress.class));
                if(data.has("story")){
                    var story=stories.computeIfAbsent(id,key->new KantoStory(db(),id,speciesTemplates));story.itemTemplates(itemTemplates);
                    story.restoreBattle(data.getAsJsonObject("story"),session.state().ended());storyBattles.add(id);
                }
            }
            if(expires<=battleClock.getAsLong()){
                if(!session.state().ended()){
                    var state=session.choose(top.totodile.pokemmo.server.game.battle.BattleEngine.Action.forfeit());
                    db().transaction(()->{
                        progress.get(id).settle(db(),id,speciesTemplates,battleYields,state);
                        if(storyBattles.contains(id))stories.get(id).finishBattle(false);
                        db().execute("DELETE FROM character_battle_checkpoint WHERE character_id=?",id);
                    });
                }
                closeBattle(id);stories.computeIfPresent(id,(key,story)->story.afterBattle());return null;
            }
            var state=session.state();
            var view=new top.totodile.pokemmo.server.game.battle.BattleState(state.engine(),state.turn(),state.ended(),state.winner(),state.forceSwitch(),state.pokemon(),List.of(),List.of(),List.of(),List.of());
            checkpointBattle(id);return new BattleResume(encounters.get(id),view,battleClock.getAsLong()+BATTLE_RECONNECT_MILLIS);
        },worker);
    }
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
        this(factory,System::currentTimeMillis);
    }
    public LocalGameService(Supplier<Database> factory,java.util.function.LongSupplier battleClock) {
        this.factory = factory;this.battleClock=battleClock;
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
                opened.execute("CREATE TABLE IF NOT EXISTS character_recent_login (character_id TEXT"
                        + " PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE, last_login INTEGER NOT NULL)");
                PokemonStore.initialize(opened);
                PokedexStore.initialize(opened);
                FollowerStore.initialize(opened);
                KantoStory.initialize(opened);
                opened.execute("CREATE TABLE IF NOT EXISTS character_battle_checkpoint(character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE,expires_at INTEGER NOT NULL,payload TEXT NOT NULL)");
                database = new StoryTestDatabase(opened);
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
                                            "SELECT c.id,c.name,c.gender,c.skin,c.region,c.created_at FROM"
                                                + " characters c LEFT JOIN character_recent_login r ON r.character_id=c.id"
                                                + " WHERE c.account=? AND c.server=? ORDER BY"
                                                + " COALESCE(r.last_login,0) DESC,c.created_at,c.id",
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
                    if(((StoryTestDatabase)db()).active())throw new IllegalStateException("请先结束临时测试");
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
                                        KantoStory.create(db(),character.id(),appearance.region());
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
                    if(((StoryTestDatabase)db()).active())throw new IllegalStateException("请先结束临时测试");
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
                            if(((StoryTestDatabase)db()).active()&&!((StoryTestDatabase)db()).owner().equals(characterId))throw new IllegalStateException("请先结束临时测试");
                            // Entry snapshots also refresh the HUD after battle; preserve an in-flight story.
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
                            if (!playStarted.containsKey(characterId)) {
                                db().transaction(() -> {
                                    long previous = Long.parseLong(db().query(
                                            "SELECT COALESCE(MAX(last_login),0) FROM character_recent_login").get(0)[0]);
                                    long login = Math.max(System.currentTimeMillis(), previous + 1);
                                    db().execute("INSERT INTO character_recent_login(character_id,last_login) VALUES (?,?)"
                                            + " ON CONFLICT(character_id) DO UPDATE SET last_login=excluded.last_login",
                                            characterId, login);
                                });
                                playStarted.put(characterId, System.nanoTime());
                            }
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
                    if(db() instanceof StoryTestDatabase test&&test.active()&&!test.owner().equals(characterId))throw new IllegalStateException("请先结束临时测试");
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
                db().query("SELECT COALESCE(r.region,c.region) FROM characters c LEFT JOIN character_current_region r ON r.character_id=c.id WHERE c.id=?", id);
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
    private volatile Map<Integer,Integer> movePp=Map.of();
    private volatile Map<Integer,MachineTemplate> machines=Map.of();
    private volatile Map<Integer,GrowthItem> growthItems=Map.of();
    @Override public void growthItems(List<GrowthItem> items){growthItems=items.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(GrowthItem::item,java.util.function.Function.identity()));}
    private volatile Map<Integer,Set<Integer>> machineLearners=Map.of();
    @Override public void machines(List<MachineTemplate> items,Map<Integer,Set<Integer>> learners){
        machines=items.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(MachineTemplate::item,java.util.function.Function.identity()));
        var snapshot=new HashMap<Integer,Set<Integer>>();learners.forEach((id,moves)->snapshot.put(id,Set.copyOf(moves)));machineLearners=Map.copyOf(snapshot);
    }
    @Override public void movePp(Map<Integer,Integer> values){movePp=Map.copyOf(values);}

    @Override public CompletionStage<ItemUseResult> useItem(Selection selection,String character,int item,
            String pokemon,int amount,int move){
        return CompletableFuture.supplyAsync(()->{
            playerData(selection,character);
            var story=stories.get(character);
            int message;
            if(battles.containsKey(character)||story!=null&&story.waitingAction()!=null)message=6002;
            else if(machines.containsKey(item))message=new MachineStore(db()).use(character,machines.get(item),pokemon,amount,move,machineLearners,speciesTemplates);
            else if(growthItems.containsKey(item))message=new GrowthItemStore(db()).use(character,growthItems.get(item),pokemon,amount,move,speciesTemplates,movePp);
            else message=new MedicineStore(db()).use(character,item,pokemon,amount,move,itemTemplates,speciesTemplates,movePp);
            return new ItemUseResult(message==0,message,new PokemonStore(db(),speciesTemplates).load(character));
        },worker);
    }
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
                    var activeStory=stories.get(id);
                    if(storyBattles.contains(id)&&activeStory!=null&&activeStory.scriptedWild()&&!activeStory.scriptedWildCaptureAllowed())
                        throw new IllegalStateException("这只剧情幽灵无法捕捉");
                    boolean storyWild=storyBattles.contains(id)&&activeStory!=null&&activeStory.scriptedWildCaptureAllowed();
                    if (battles.containsKey(id)&&!storyWild)
                        throw new UnsupportedOperationException("This battle cannot use the simulated capture adapter");
                    if (moveTestBattles.contains(id))
                        throw new IllegalStateException(
                                "Effect-test encounters cannot be captured");
                    var encounter = encounters.get(id);
                    if (encounter == null || encounter.trainer()!=null
                            || encounter.wilds().stream().noneMatch(p -> p.id().equals(targetId)&&p.hp()>0))
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
                                        int code;
                                        if(encounter.captureTutorial()) {
                                            // Reference Gen5CaptureCalculator, tutorial's full-HP Weedle / ordinary ball.
                                            long rate=(3L*target.maxHp()-2L*target.hp())*255*4096/(3L*target.maxHp());
                                            int threshold=(int)Math.floor(65536d*Math.pow(rate/(255d*4096),.25d));
                                            int caughtCount=(int)new PokedexStore(db()).load(id).count(2);
                                            int numerator=caughtCount<=30?0:caughtCount<=150?1:caughtCount<=300?2:caughtCount<=450?3:caughtCount<=600?4:5;
                                            int criticalValue=(int)(rate*numerator/(12L*4096));
                                            boolean critical=criticalValue>0&&captureRandom.nextInt(256)<criticalValue;
                                            code=4;
                                            for(int shake=0;shake<(critical?1:3);shake++)if(captureRandom.nextInt(65536)>=threshold){code=critical?1:new int[]{0,1,3}[shake];break;}
                                            if(item.ballType()==0)code=4;
                                        } else code = new int[] {0, 1, 3, 4}[captureRandom.nextInt(4)];
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
                                            if(encounter.captureTutorial()||storyWild)stories.get(id).persistCapture();
                                            db().execute("DELETE FROM character_battle_checkpoint WHERE character_id=?",id);
                                        }
                                        result[0] =
                                                new CaptureResult(
                                                        code,
                                                        caught,
                                                        store.load(id),
                                                        new PlayerDataStore(db()).inventory(id),
                                                        state);
                                    });
                    if (result[0].captured() != null) {
                        if(encounter.captureTutorial()||storyWild)stories.get(id).finishBattle(true);
                        closeBattle(id);
                        encounters.remove(id);
                    }
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
                                                        state.bicycle() && TraversalAccess.bicycle(db(),characterId,currentRegion(characterId,0)) ? 1 : 0);
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
        if(db() instanceof StoryTestDatabase test&&test.active()&&!test.owner().equals(characterId))
            throw new IllegalStateException("请先结束其他角色的临时测试");
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
    public CompletionStage<PokemonData.Containers> updatePokemonItem(
            Selection selection, String character, String pokemon, int container, int item) {
        return CompletableFuture.supplyAsync(() -> {
            playerData(selection, character);
            new PokemonItemStore(db()).update(character, pokemon, container, item, itemTemplates);
            return new PokemonStore(db(), speciesTemplates).load(character);
        }, worker);
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
                            var trainer=playerData(selection, id).trainer(character);
                            if(currentRegion(id,character.appearance().region())==4)return new PlayerData.Trainer(trainer.character(),trainer.money(),trainer.battlePoints(),trainer.playedSeconds(),trainer.caught(),trainer.obedience(),JohtoStory.badges(db(),id));
                            if(currentRegion(id,character.appearance().region())==3)return new PlayerData.Trainer(trainer.character(),trainer.money(),trainer.battlePoints(),trainer.playedSeconds(),trainer.caught(),trainer.obedience(),SinnohStory.badges(db(),id));
                            if(currentRegion(id,character.appearance().region())==2)return new PlayerData.Trainer(trainer.character(),trainer.money(),trainer.battlePoints(),trainer.playedSeconds(),trainer.caught(),trainer.obedience(),UnovaStory.badges(db(),id));
                            return currentRegion(id,character.appearance().region())==1
                                ?new PlayerData.Trainer(trainer.character(),trainer.money(),trainer.battlePoints(),trainer.playedSeconds(),trainer.caught(),trainer.obedience(),HoennMainline.badges(db(),id)):trainer;
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
                    restoreStoryTest();
                },
                worker);
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        worker.execute(
                () -> {
                    for (String id : new ArrayList<>(battles.keySet())) {checkpointBattle(id);closeBattle(id,false);}
                    readyBattleEngine.close();
                    if (database != null) {
                        restoreStoryTest();
                        for (String id : new ArrayList<>(playStarted.keySet()))
                            checkpointPlaytime(id);
                        database.close();
                    }
                });
        worker.shutdown();
        try {worker.awaitTermination(5,TimeUnit.SECONDS);}catch(InterruptedException error){Thread.currentThread().interrupt();}
    }
}
