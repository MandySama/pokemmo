package top.totodile.pokemmo.server.game;

import static top.totodile.pokemmo.server.game.StoryData.*;

import com.google.gson.*;

import top.totodile.pokemmo.db.Database;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Adapts the reference Pallet story program and transactional checkpoints to local SQLite. */
final class KantoStory {
    private static final JsonObject PROGRAM = read("opening.json").getAsJsonObject();
    private static final JsonObject NURSE = read("nurse.json").getAsJsonObject();
    private static final JsonObject PARCEL = read("parcel.json").getAsJsonObject();
    static final JsonObject DAISY = read("daisy.json").getAsJsonObject();
    private final PalletDaisy daisy;
    private static final JsonObject SIGN_LADY = read("sign-lady.json").getAsJsonObject();
    private JsonObject program = PROGRAM, question, battleAction;
    private String trainerEvent;
    private boolean battleSettled, resumeActive;
    private boolean coordinateInteraction;
    private final Set<String> temporaryHidden=new HashSet<>();
    private final HoennStory hoenn;
    private final Map<Integer,RegionalStory> regionalStories=new HashMap<>();
    private RegionalStory chapter(){return regionalStories.get(activeRegion);}
    boolean regionalFlag(String flag){return chapter()!=null&&chapter().has(flag);}
    private int activeRegion;
    private final PalletSignLady signLady;
    private final KantoMechanisms mechanisms;
    private MoveLearning.Pending pendingLearn;
    private PokemonEvolution.Pending pendingEvolution;
    private static final JsonArray MAPS = read("maps.json").getAsJsonArray();

    private static JsonElement read(String file) {
        try (var input = KantoStory.class.getResourceAsStream("/story/kanto/" + file)) {
            if (input == null) throw new FileNotFoundException(file);
            return JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static JsonObject openingProgram(){return PROGRAM;}
    static JsonObject nurseProgram(){return NURSE;}
    static JsonObject parcelProgram(){return PARCEL;}
    static JsonObject signLadyProgram(){return SIGN_LADY;}
    String daisyNode(){return daisy.node(state(),templates);}
    PalletSignLady.View signLadyView(){return signLady.view();}
    Action waitingAction(){return chapter()!=null&&waiting==null?chapter().waiting():waiting;}

    private final Database db;
    private final Map<Integer, GameService.SpeciesTemplate> templates;
    private final String owner;
    private Map<Integer, GameService.ItemTemplate> itemTemplates = Map.of();
    void itemTemplates(Map<Integer, GameService.ItemTemplate> value) { itemTemplates=value; }
    private final Random random = new Random();
    private JsonArray actions;
    private int index, interactionNpc = -1;
    private static final java.util.concurrent.atomic.AtomicLong SEQUENCES =
            new java.util.concurrent.atomic.AtomicLong();
    private Action waiting;
    private String starterNext;
    private boolean partyHealed;
    private boolean inventoryChanged;
    boolean takeInventoryChanged() { boolean regionalChanged=chapter()!=null&&chapter().takeChanged(); boolean value=inventoryChanged||regionalChanged; inventoryChanged=false; return value; }
    boolean takePartyHealed() { if(chapter()!=null)return chapter().takeHealed(); boolean value = partyHealed; partyHealed = false; return value; }
    private GameService.Position position;
    private final Map<String, MapEntities.Npc> actorPoses = new HashMap<>();

    private State entityState;
    private List<MapEntities.MapView> entitySnapshot;

    List<MapEntities.MapView> entities() {
        if(chapter()!=null)return chapter().entities();
        if(activeRegion!=0&&activeRegion!=1)return List.of();
        var current = state();
        if (entitySnapshot != null && current.equals(entityState)) return entitySnapshot;
        entityState = current;
        if(activeRegion==1)return entitySnapshot=hoenn.entities().stream().map(m->new MapEntities.MapView(m.bank(),m.map(),m.npcs().stream()
                .filter(n->!temporaryHidden.contains(m.bank()+":"+m.map()+":"+n.id()))
                .map(n->actorPoses.getOrDefault(m.bank()+":"+m.map()+":"+n.id(),n)).toList(),m.backgrounds(),m.tiles())).toList();
        return entitySnapshot = mechanisms.project(List.copyOf(MapEntities.projectStory(db,owner,OakParcel.phase(db,owner,current).equals("DELIVER") ? new State(true,5,current.starter()) : current).stream().map(m -> new MapEntities.MapView(m.bank(), m.map(),
                m.npcs().stream().filter(n->!temporaryHidden.contains(m.bank()+":"+m.map()+":"+n.id())).map(n -> m.bank()==3 && m.map()==0 && n.id()==0 ? signLady.project(n) : actorPoses.getOrDefault(
                        m.bank() + ":" + m.map() + ":" + n.id(), n)).toList(), m.backgrounds(), m.tiles())).toList()));
    }

    private void completeActorMove(Action action) {
        if (!action.type().equals("MOVE_NPC") && !action.type().equals("GUIDE") && !action.type().equals("FACE_NPC")) return;
        var p = action.target();
        for (var m : entities()) if (m.bank() == p.bank() && m.map() == p.map()) {
            for (var n : m.npcs()) if (n.id() == action.actor()) {
                int direction = action.type().equals("FACE_NPC") ? p.direction() : n.x() != p.x() ? (p.x() > n.x() ? 3 : 2)
                        : n.y() != p.y() ? (p.y() > n.y() ? 0 : 1) : n.direction();
                entitySnapshot = null;
                if (m.bank()==3 && m.map()==0 && n.id()==0 && program==SIGN_LADY)
                    signLady.pose(new GameService.Position(3,0,p.x(),p.y(),direction));
                actorPoses.put(m.bank() + ":" + m.map() + ":" + n.id(),
                        new MapEntities.Npc(n.id(), n.graphics(), p.x(), p.y(), n.elevation(),
                                direction, n.movement(), n.rangeX(), n.rangeY()));
            }
        }
    }

    KantoStory(Database db, String owner, Map<Integer, GameService.SpeciesTemplate> templates) {
        this(db, owner, templates, null);
    }

    private KantoStory(Database db, String owner, Map<Integer, GameService.SpeciesTemplate> templates,
            KantoMechanisms fieldMechanisms) {
        this.db = db;
        this.owner = owner;
        this.templates = templates;
        hoenn = new HoennStory(db,owner);
        regionalStories.put(2,new UnovaStory(db,owner,templates));
        regionalStories.put(3,new SinnohStory(db,owner,templates));
        regionalStories.put(4,new JohtoStory(db,owner,templates));
        var location=db.query("SELECT COALESCE(r.region,c.region) FROM characters c LEFT JOIN character_current_region r ON r.character_id=c.id WHERE c.id=?",owner);
        activeRegion=location.isEmpty()?0:Integer.parseInt(location.get(0)[0]);
        // Existing characters from before these chapters gain only the missing story row.
        // Keep their position, party, inventory and any existing chapter checkpoint intact.
        if (!location.isEmpty() && chapter()!=null && !chapter().state().enabled()) create(db,owner,activeRegion);
        signLady = new PalletSignLady(db,owner);
        daisy = new PalletDaisy(db,owner);
        mechanisms=fieldMechanisms==null?new KantoMechanisms(db,owner,random):fieldMechanisms;
    }

    KantoStory afterBattle() {
        // BattleFinishSuccessPacket clears combat, not the current field's object positions.
        // Renew dialogue state while retaining map-local pushes/Strength until a real map change.
        var next=new KantoStory(db,owner,templates,mechanisms);
        next.position=position;next.activeRegion=activeRegion;
        next.itemTemplates=itemTemplates;
        return next;
    }

    JsonObject battleCheckpoint(){
        if(chapter()!=null){var data=chapter().checkpoint();data.addProperty("region",activeRegion);return data;}
        var data=new JsonObject();var gson=new Gson();
        data.addProperty("program",program==null||!program.has("id")?"":program.get("id").getAsString());
        data.add("battleAction",gson.toJsonTree(battleAction));data.add("position",gson.toJsonTree(position));
        data.addProperty("trainerEvent",trainerEvent);return data;
    }
    void restoreBattle(JsonObject data,boolean ended){
        if(data.has("region")&&regionalStories.containsKey(data.get("region").getAsInt())){activeRegion=data.get("region").getAsInt();return;}
        if(data.has("unova")){activeRegion=2;return;}
        var gson=new Gson();var saved=KantoMainline.program(data.get("program").getAsString());program=data.get("program").getAsString().startsWith("hoenn.")?HoennStory.program(data.get("program").getAsString()):saved==null?PROGRAM:saved;
        battleAction=!data.has("battleAction")||data.get("battleAction").isJsonNull()?null:data.getAsJsonObject("battleAction");
        position=gson.fromJson(data.get("position"),GameService.Position.class);
        trainerEvent=!data.has("trainerEvent")||data.get("trainerEvent").isJsonNull()?null:data.get("trainerEvent").getAsString();
        battleSettled=ended;actions=null;
        waiting=ended?null:new Action(SEQUENCES.incrementAndGet(),"START_BATTLE",0,-1,0,null,null,0,"");
    }

    static void initialize(Database db) {
        db.execute("CREATE TABLE IF NOT EXISTS character_hall_of_fame(character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE,team TEXT NOT NULL,completed INTEGER NOT NULL DEFAULT 0)");
        PalletSignLady.initialize(db);
        HoennStory.initialize(db);
        UnovaStory.initialize(db);
        SinnohStory.initialize(db);
        JohtoStory.initialize(db);
        OakParcel.initialize(db);
        KantoMainline.initialize(db);
        db.execute(
                "CREATE TABLE IF NOT EXISTS character_kanto_story (character_id TEXT PRIMARY KEY"
                    + " REFERENCES characters(id) ON DELETE CASCADE,stage INTEGER NOT NULL DEFAULT"
                    + " 0,starter INTEGER NOT NULL DEFAULT 3)");
        db.execute("CREATE TABLE IF NOT EXISTS character_kanto_variables (character_id TEXT NOT NULL"
                + " REFERENCES characters(id) ON DELETE CASCADE, variable INTEGER NOT NULL,"
                + " value INTEGER NOT NULL, PRIMARY KEY(character_id, variable))");
        db.execute("CREATE TABLE IF NOT EXISTS character_kanto_fame_checker (character_id TEXT NOT NULL"
                + " REFERENCES characters(id) ON DELETE CASCADE, person INTEGER NOT NULL,"
                + " flavor_flags INTEGER NOT NULL, pick_state INTEGER NOT NULL, PRIMARY KEY(character_id, person))");
    }

    static void create(Database db, String owner, int region) {
        if (region == 1) HoennStory.create(db,owner);
        if (region == 2) UnovaStory.create(db,owner);
        if (region == 3) SinnohStory.create(db,owner);
        if (region == 4) JohtoStory.create(db,owner);
        if (region == 0)
            db.execute("INSERT INTO character_kanto_story(character_id) VALUES (?)", owner);
    }

    State state() {
        if(chapter()!=null)return chapter().state();
        if(activeRegion==1)return hoenn.state();
        var rows =
                db.query(
                        "SELECT stage,starter FROM character_kanto_story WHERE character_id=?",
                        owner);
        return rows.isEmpty()
                ? new State(false, 0, 3)
                : new State(
                        true, Integer.parseInt(rows.get(0)[0]), Integer.parseInt(rows.get(0)[1]));
    }

    Scene request(Request request, GameService.Position p, int region) {
        var previous=position;
        position = p;
        if(activeRegion!=region){entitySnapshot=null;actorPoses.clear();temporaryHidden.clear();}
        activeRegion=region;
        if(chapter()!=null){
            if(waiting!=null && (waiting.type().equals("SELECT_LEARN_MOVE")||waiting.type().equals("EVOLUTION"))) {
                if(!request.event().equals("REPLY")||waiting.sequence()!=request.sequence())return new Scene(state(),waiting);
                if(waiting.type().equals("SELECT_LEARN_MOVE")){MoveLearning.choose(db,owner,pendingLearn,request.answer(),templates);pendingLearn=null;}
                else {PokemonEvolution.choose(db,owner,pendingEvolution,request.answer(),templates);pendingEvolution=null;}
                waiting=null;inventoryChanged=true;return new Scene(state(),null);
            }
            if(chapter().waiting()==null&&!request.event().equals("REPLY")&&state().starter()<3){var growth=pendingGrowth();if(growth!=null)return growth;}
            return chapter().request(request,p,itemTemplates);
        }
        if(region==1&&previous!=null&&(previous.bank()!=p.bank()||previous.map()!=p.map())){actorPoses.clear();entitySnapshot=null;}
        if(region==0&&state().enabled()&&state().stage()>=6)mechanisms.arrive(previous,p,request.event());
        if(previous!=null&&(previous.bank()!=p.bank()||previous.map()!=p.map())&&!temporaryHidden.isEmpty()){temporaryHidden.clear();entitySnapshot=null;}
        if(region==0&&state().enabled()&&state().stage()>=6&&request.event().equals("MAP_READY")&&!mapKey(p).startsWith("SafariZone_")&&!mapKey(p).equals("FuchsiaCity_SafariZone_Entrance"))
            db.execute("UPDATE character_kanto_mainline SET safari_steps=0,safari_balls=0 WHERE character_id=?",owner);
        if(region==0&&request.event().equals("COORDINATE")&&previous!=null&&!p.equals(previous)&&mapKey(p).startsWith("SafariZone_")&&!mapKey(p).contains("_RestHouse")&&!mapKey(p).contains("_SecretHouse")) {
            var rows=db.query("SELECT safari_steps FROM character_kanto_mainline WHERE character_id=?",owner);
            if(!rows.isEmpty()&&Integer.parseInt(rows.get(0)[0])>0) {
                db.execute("UPDATE character_kanto_mainline SET safari_steps=safari_steps-1 WHERE character_id=?",owner);
                if(rows.get(0)[0].equals("1")) {
                    actions=new JsonArray();var leave=new JsonObject();leave.addProperty("type","CHANGE_MAP");leave.addProperty("map","FuchsiaCity_SafariZone_Entrance");leave.addProperty("x",4);leave.addProperty("y",7);leave.addProperty("toward",1);actions.add(leave);index=0;return advance();
                }
            }
        }
        if (region != 0 && region != 1) return new Scene(state(), null);
        if(region==0){signLady.mapReady(request,p,state());
        if (daisy.arrive(request,p)) actorPoses.remove("4:2:0");}
        if(request.event().equals("MAP_READY"))entitySnapshot=null;
        if (request.event().equals("REPLY")) {
            if (waiting == null || waiting.sequence() != request.sequence())
                return new Scene(state(), waiting);
            if (waiting.type().equals("MOVE_PLAYER")
                    || waiting.type().equals("CHANGE_MAP")
                    || waiting.type().equals("GUIDE")) {
                var expected =
                        waiting.playerTarget() == null ? waiting.target() : waiting.playerTarget();
                if (p.bank() != expected.bank()
                        || p.map() != expected.map()
                        || p.x() != expected.x()
                        || p.y() != expected.y())
                    throw new IllegalStateException("Story movement has not completed");
            }
            if (waiting.type().equals("START_BATTLE"))
                throw new IllegalStateException("Battle must be resolved by the engine");
            if (waiting.type().equals("HALL_OF_FAME")) {
                db.transaction(()->{
                    if(activeRegion==1){db.execute("UPDATE character_hoenn_progress SET completed=1 WHERE character_id=?",owner);hoenn.resume(program,"hall_exit",position);}
                    else {db.execute("UPDATE character_hall_of_fame SET completed=1 WHERE character_id=?",owner);KantoMainline.resume(db,owner,program,"hall_exit",position);}
                });
                resumeActive=true;
            }
            if(waiting.type().equals("SELECT_LEARN_MOVE")){
                MoveLearning.choose(db,owner,pendingLearn,request.answer(),templates);pendingLearn=null;waiting=null;actions=null;inventoryChanged=true;return new Scene(state(),null);
            }
            if(waiting.type().equals("EVOLUTION")){
                PokemonEvolution.choose(db,owner,pendingEvolution,request.answer(),templates);
                pendingEvolution=null;waiting=null;actions=null;entitySnapshot=null;inventoryChanged=true;
                return new Scene(state(),null);
            }
            if (waiting.type().equals("YES_NO")) {
                if (request.answer() != 0 && request.answer() != 1)
                    throw new IllegalArgumentException("Invalid dialogue choice");
                node(question.get(request.answer() == 1 ? "yes" : "no").getAsString());
                question = null;
            }
            if(waiting.type().equals("SELECT_OPTION")){
                if(question!=null&&question.has("nodes")){
                    var choices=question.getAsJsonArray("nodes");
                    if(request.answer()<0||request.answer()>=choices.size())throw new IllegalArgumentException("Invalid story menu choice");
                    String target=choices.get(request.answer()).getAsString();question=null;waiting=null;node(target);return advance();
                }
                var plan=mechanisms.choose(request.answer());actions=plan.actions();index=0;interactionNpc=plan.actor();waiting=null;return advance();
            }
            completeActorMove(waiting);
            if(activeRegion==0)mechanisms.acknowledge(waiting);entitySnapshot=null;
            if (waiting.type().equals("SELECT_STARTER")) {
                if (request.answer() != 0 && request.answer() != 1)
                    throw new IllegalArgumentException("Invalid story choice");
                if (request.answer() == 0) {
                    actions = null;
                    waiting = null;
                    return new Scene(state(), null);
                }
                int choice =
                        switch (interactionNpc) {
                            case 4 -> 0;
                            case 5 -> 2;
                            case 6 -> 1;
                            default -> throw new IllegalStateException("Invalid starter ball");
                        };
                claim(choice);
                node(starterNext);
            }
            waiting = null;
            return advance();
        }
        if (waiting != null) return new Scene(state(), waiting);
        if(region==1)return requestHoenn(request,p);
        if (request.event().equals("NPC") && p.bank()==4 && p.map()==2
                && request.npc()==0 && nearFixedNpc(p,0)) {
            program=DAISY; interactionNpc=0; node(daisy.node(state(),templates)); return advance();
        }
        String girlNode=signLady.node(request,p);
        if(p.bank()==3&&p.map()==0&&signLady.view().scene()==2
                &&request.event().equals("NPC")&&request.npc()==0&&nearFixedNpc(p,0))girlNode="done";
        if(girlNode!=null){
            program=SIGN_LADY;interactionNpc=request.event().equals("BACKGROUND")?-1:0;
            node(girlNode);return advance();
        }
        var resume=db.query("SELECT program,node,bank,map FROM character_story_resume WHERE character_id=?",owner);
        if(!resume.isEmpty() && p.bank()==Integer.parseInt(resume.get(0)[2]) && p.map()==Integer.parseInt(resume.get(0)[3])) {
            var saved=resume.get(0)[0].equals(PROGRAM.get("id").getAsString())?PROGRAM:KantoMainline.program(resume.get(0)[0]);
            if(saved!=null){resumeActive=true;program=saved;node(resume.get(0)[1]);return advance();}
        }
        if(state().enabled()&&state().stage()>=6){
            KantoPostgame.arrive(db,owner,request,p);
            var plan=mechanisms.request(request,p,entities());
            if(plan!=null){program=KantoMechanisms.program();actions=plan.actions();index=0;interactionNpc=plan.actor();entitySnapshot=null;return advance();}
        }
        if(state().enabled()&&state().stage()>=6){var growth=pendingGrowth();if(growth!=null)return growth;}
        if(state().enabled()&&state().stage()>=6)for(var chapter:KantoMainline.PROGRAMS)if(trigger(chapter,request,p))return advance();
        if(state().enabled()&&state().stage()>=6&&request.event().equals("NPC")&&nearFixedNpc(p,request.npc())) {
            var pickup=KantoMainline.pickup(mapKey(p),request.npc());
            if(pickup!=null) {
                String flag=KantoMainline.pickupFlag(pickup);
                boolean collected=!KantoMainline.event(db,owner,flag);
                if(collected)db.transaction(()->{
                    KantoMainline.grant(db,owner,pickup.get("itemId").getAsInt(),pickup.get("amount").getAsInt(),itemTemplates);
                    KantoMainline.eventSet(db,owner,flag);
                });
                inventoryChanged=collected;entitySnapshot=null;actions=null;
                if(collected){waiting=new Action(SEQUENCES.incrementAndGet(),"NOTIFY",0,-1,0,null,null,0,"获得道具 #"+pickup.get("itemId").getAsInt()+" × "+pickup.get("amount").getAsInt());return new Scene(state(),waiting);}
                return new Scene(state(),null);
            }
            var binding=KantoMainline.binding(mapKey(p),request.npc());
            if(hasBattlePokemon()&&binding!=null&&binding.get("sightRange").getAsInt()>0&&!KantoMainline.event(db,owner,KantoMainline.trainerFlag(mapKey(p),binding))) {
                trainerEvent=KantoMainline.trainerFlag(mapKey(p),binding);
                battleAction=new JsonObject();battleAction.addProperty("type","START_TRAINER_BATTLE");
                battleAction.addProperty("trainerTeamId",binding.get("trainerTeamId").getAsInt());
                battleAction.addProperty("battleId",trainerEvent);
                program=PROGRAM;interactionNpc=request.npc();
                actions=new JsonArray();actions.add(battleAction);index=0;return advance();
            }
        }
        if(state().enabled()&&state().stage()>=6&&hasBattlePokemon()&&request.event().equals("COORDINATE")) {
            var candidates=entities().stream().filter(m->m.bank()==p.bank()&&m.map()==p.map()).flatMap(m->m.npcs().stream())
                .sorted(Comparator.comparingInt(n->Math.abs(n.x()-p.x())+Math.abs(n.y()-p.y()))).toList();
            for(var npc:candidates) {
                var binding=KantoMainline.binding(mapKey(p),npc.id());
                if(binding==null||!MapEntities.inSight(p,npc,binding.get("sightRange").getAsInt())||KantoMainline.event(db,owner,KantoMainline.trainerFlag(mapKey(p),binding)))continue;
                String quest=KantoPostgame.trainerNode(db,owner,mapKey(p),npc.id());
                if(quest!=null){
                    if(quest.isEmpty())continue;
                    program=KantoMainline.program("kanto.postgame");interactionNpc=npc.id();trainerEvent=null;node(quest);return advance();
                }
                trainerEvent=KantoMainline.trainerFlag(mapKey(p),binding);
                battleAction=new JsonObject();battleAction.addProperty("type","START_TRAINER_BATTLE");battleAction.addProperty("trainerTeamId",binding.get("trainerTeamId").getAsInt());battleAction.addProperty("battleId",trainerEvent);
                program=PROGRAM;interactionNpc=npc.id();actions=new JsonArray();actions.add(battleAction);index=0;return advance();
            }
        }
        // Quest interactions must precede ordinary NPC/shop dialogue.
        if (state().enabled() && trigger(PARCEL, request, p)) return advance();
        if (request.event().equals("BACKGROUND")) {
            var background = MapEntities.background(p, request.npc());
            var fixed = background == null ? null : BackgroundDialogues.find(p.bank(), p.map(), background.id());
            if (fixed == null) return new Scene(state(), null);
            program = fixed;
            interactionNpc = -1; // A map event is not an NPC; do not turn or anchor to an actor.
            node("talk");
            return advance();
        }
        if (request.event().equals("NPC") && nearFixedNpc(p, request.npc())) {
            var fixed = FixedNpcDialogues.find(p.bank(), p.map(), request.npc());
            if (fixed != null) {
                program = fixed;
                interactionNpc = request.npc();
                node("talk");
                return advance();
            }
        }
        if (request.event().equals("NPC") && nearNpc(p, request.npc())
                && MapEntities.nurse(p.bank(), p.map(), request.npc())) {
            program = NURSE;
            interactionNpc = request.npc();
            node("nurse");
            return advance();
        }
        if (!state().enabled()) {
            if (!request.event().equals("NPC") || !nearNpc(p, request.npc()))
                return new Scene(state(), null);
            program = PROGRAM;
            interactionNpc = request.npc();
            if (p.bank() == 4 && p.map() == 0 && interactionNpc == 0) {
                if (new PokemonStore(db, templates).load(owner).pokemons().stream()
                        .anyMatch(n -> n.container() == 1 && !n.egg())) node("mother_heal");
                else node(condition(JsonParser.parseString("{\"type\":\"SEX_EQUALS\",\"value\":0}").getAsJsonObject())
                        ? "mother_boy" : "mother_girl");
            } else if (p.bank() == 4 && p.map() == 3) {
                if (interactionNpc <= 2 || interactionNpc == 7) node("aide");
                else if (interactionNpc == 3) node("choose_again");
                else if (interactionNpc <= 6 || interactionNpc == 9) node("ball");
                else if (interactionNpc == 8) node("dex");
                else return new Scene(state(), null);
            } else return new Scene(state(), null);
            return advance();
        }
        program = PROGRAM;
        String map = mapKey(p);
        if (map == null) return new Scene(state(), null);
        // PalletStoryLifecycle resumes the escort checkpoint after a reconnect.
        if (request.event().equals("MAP_READY")
                && state().stage() == 1
                && map.equals("PalletTown")) {
            node("escort");
            return advance();
        }
        for (var value : PROGRAM.getAsJsonArray("triggers")) {
            var trigger = value.getAsJsonObject();
            if (!trigger.get("type").getAsString().equals(request.event())
                    || !trigger.get("map").getAsString().equals(map)) continue;
            if (request.event().equals("NPC")
                    && trigger.get("entityIdx").getAsInt() != request.npc()) continue;
            if (request.event().equals("NPC") && !nearNpc(p, request.npc())) continue;
            if (request.event().equals("COORDINATE")
                    && (trigger.get("x").getAsInt() != p.x()
                            || trigger.get("y").getAsInt() != p.y())) continue;
            if (trigger.has("conditions") && !conditions(trigger.getAsJsonArray("conditions")))
                continue;
            interactionNpc = request.npc();
            node(trigger.get("startNode").getAsString());
            return advance();
        }
        if (request.event().equals("NPC") && nearNpc(p, request.npc())
                && map.equals("PalletTown_ProfessorOaksLab")) {
            interactionNpc = request.npc();
            if (interactionNpc == 3 && state().stage() >= 4) {
                program = PARCEL;
                node("go_to_city");
            } else if (interactionNpc == 3) node("choose_again");
            else if (interactionNpc == 7 && state().stage() == 3) node("challenge");
            else if (interactionNpc == 2 || interactionNpc == 7) node("aide");
            else if (interactionNpc >= 4 && interactionNpc <= 6) node("ball");
            else return new Scene(state(), null);
            return advance();
        }
        return new Scene(state(), null);
    }

    private Scene pendingGrowth(){
        {
            pendingLearn=MoveLearning.first(db,owner);
            if(pendingLearn!=null){
                var pokemon=new PokemonStore(db,templates).load(owner).pokemons().stream().filter(mon->mon.id().equals(pendingLearn.pokemon())).findFirst().orElseThrow();
                waiting=new Action(SEQUENCES.incrementAndGet(),"SELECT_LEARN_MOVE",0,-1,pendingLearn.move(),null,null,0,"",0,pokemon.moves().stream().map(String::valueOf).toList());
                return new Scene(state(),waiting);
            }
        }
        {
            PokemonEvolution.legacy(db,owner,templates);
            pendingEvolution=PokemonEvolution.first(db,owner,templates);
            if(pendingEvolution!=null){
                var pokemon=new PokemonStore(db,templates).load(owner).pokemons().stream().filter(mon->mon.id().equals(pendingEvolution.pokemon())).findFirst().orElseThrow();
                waiting=new Action(SEQUENCES.incrementAndGet(),"EVOLUTION",0,-1,pendingEvolution.source(),null,null,0,pokemon.name(),0,
                        List.of(Integer.toString(pendingEvolution.target()),Integer.toString(pokemon.form()),Boolean.toString(pokemon.shiny()),Boolean.toString(pendingEvolution.item()==0)));
                return new Scene(state(),waiting);
            }
        }
        return null;
    }

    private Scene requestHoenn(Request request,GameService.Position p) {
        hoenn.arrive(p);
        var fall=hoenn.ice(request,p);if(fall!=null){waiting=new Action(SEQUENCES.incrementAndGet(),"CHANGE_MAP",0,-1,0,fall,null,0,"");actions=new JsonArray();index=0;return new Scene(state(),waiting);}if(HoennWorld.key(p).equals("SootopolisCity_Gym_1F"))entitySnapshot=null;
        program=HoennStory.PROGRAM;
        if(!state().enabled())return new Scene(state(),null);
        var resume=hoenn.resume();
        if(resume!=null){
            program=resume.has("program")?HoennStory.program(resume.get("program").getAsString()):HoennStory.PROGRAM;
            String name=resume.get("node").getAsString();
            var saved=new Gson().fromJson(resume.get("position"),GameService.Position.class);
            if(saved.bank()!=p.bank()||saved.map()!=p.map()){
                actions=new JsonArray();var warp=new JsonObject();warp.addProperty("type","CHANGE_MAP");warp.addProperty("map",HoennWorld.key(saved));warp.addProperty("x",saved.x());warp.addProperty("y",saved.y());warp.addProperty("toward",saved.direction());actions.add(warp);
                var go=new JsonObject();go.addProperty("type","GOTO");go.addProperty("node",name);actions.add(go);index=0;
            }else node(name);
            return advance();
        }
        if(state().stage()>=10){var growth=pendingGrowth();if(growth!=null)return growth;}
        if(state().stage()>=16){
            String field=hoenn.fieldNode(request,p,entities());
            if(field!=null){program=HoennMainline.PROGRAM;interactionNpc=request.npc();node(field);return advance();}
            var push=hoenn.push(request,p,entities());if(push!=null){program=HoennMainline.PROGRAM;interactionNpc=request.npc();actions=push;index=0;return advance();}
        }
        for(var chapter:HoennStory.PROGRAMS){
            String name=hoenn.trigger(chapter,request,p,entities());
            if(name==null)continue;program=chapter;interactionNpc=request.event().equals("NPC")?request.npc():-1;node(name);return advance();
        }
        return new Scene(state(),null);
    }

    private boolean hasBattlePokemon(){return !db.query("SELECT id FROM pokemon WHERE trainer_id=? AND container_id=1 AND is_egg=0 AND current_hp>0 LIMIT 1",owner).isEmpty();}
    private boolean trigger(JsonObject candidate, Request request, GameService.Position p) {
        for(var value:candidate.getAsJsonArray("triggers")) {
            var t=value.getAsJsonObject();
            if(!t.get("type").getAsString().equals(request.event()) || !t.get("map").getAsString().equals(mapKey(p)))continue;
            if(request.event().equals("COORDINATE")) {
                if(t.has("x")&&(t.get("x").getAsInt()!=p.x()||t.get("y").getAsInt()!=p.y()))continue;
                if(t.has("script")&&!MapEntities.coordinate(p,t.get("script").getAsString()))continue;
            }
            if(request.event().equals("BACKGROUND") && (!t.has("backgroundId")||t.get("backgroundId").getAsInt()!=request.npc()||MapEntities.background(p,request.npc())==null))continue;
            if(request.event().equals("NPC") && (t.get("entityIdx").getAsInt()!=request.npc() || !nearNpc(p,request.npc())))continue;
            if(t.has("conditions")&&!conditions(t.getAsJsonArray("conditions")))continue;
            var start=candidate.getAsJsonObject("nodes").getAsJsonObject(t.get("startNode").getAsString());
            if(start.getAsJsonArray("actions").asList().stream().map(JsonElement::getAsJsonObject).anyMatch(a->a.get("type").getAsString().equals("START_TRAINER_BATTLE")||a.get("type").getAsString().equals("START_BATTLE"))&&!hasBattlePokemon())continue;
            coordinateInteraction=request.event().equals("COORDINATE");
            program=candidate; interactionNpc=request.event().equals("BACKGROUND")?-1:request.npc(); node(t.get("startNode").getAsString()); return true;
        }
        return false;
    }

    private boolean conditions(JsonArray conditions) {
        for (var c : conditions) if (!condition(c.getAsJsonObject())) return false;
        return true;
    }

    private boolean nearNpc(GameService.Position p, int npc) {
        return entities().stream().filter(m -> m.bank() == p.bank() && m.map() == p.map())
                .flatMap(m -> m.npcs().stream()).anyMatch(n -> n.id() == npc
                        && Math.abs(p.x() - n.x()) + Math.abs(p.y() - n.y()) <= 2);
    }

    private boolean nearFixedNpc(GameService.Position p, int npc) {
        // Reference idle walking is client-side. A fixed NPC may have left its birth tile,
        // but must remain within the map resource's movement leash. The client resolves
        // the actual adjacent actor; story actors continue to use their server poses.
        return entities().stream().filter(m -> m.bank() == p.bank() && m.map() == p.map())
                .flatMap(m -> m.npcs().stream()).anyMatch(n -> {
                    if (n.id() != npc || n.graphics() == 0 || n.movement() == 76) return false;
                    boolean walks = n.movement() >= 2 && n.movement() <= 6
                            || n.movement() >= 25 && n.movement() <= 52;
                    int rx = walks && n.movement() != 3 && n.movement() != 4 ? Math.max(1, n.rangeX()) : 0;
                    int ry = walks && n.movement() != 5 && n.movement() != 6 ? Math.max(1, n.rangeY()) : 0;
                    return Math.max(0, Math.abs(p.x()-n.x())-rx)
                            + Math.max(0, Math.abs(p.y()-n.y())-ry) <= 2;
                });
    }

    private boolean condition(JsonObject c) {
        var s = state();
        String type = c.get("type").getAsString();
        if(activeRegion==1&&type.equals("PLAYER_FACING_EQUALS"))return position.direction()==c.get("value").getAsInt();
        if(activeRegion==1)return hoenn.condition(c);
        if (type.equals("PARCEL_NEEDS_PICKUP")) return OakParcel.needsPickup(db,owner,s);
        if (type.equals("PARCEL_PHASE_EQUALS")) return OakParcel.phase(db,owner,s).equals(c.get("value").getAsString());
        int value = c.has("value") && c.get("value").getAsJsonPrimitive().isNumber() ? c.get("value").getAsInt() : 0;
        return switch (c.get("type").getAsString()) {
            case "STORY_STAGE_EQUALS" -> s.stage() == value;
            case "STORY_STAGE_AT_LEAST" -> s.stage() >= value;
            case "STARTER_EQUALS" -> s.starter() == value;
            case "SEX_EQUALS" ->
                    Integer.parseInt(
                                    db.query("SELECT gender FROM characters WHERE id=?", owner)
                                            .get(0)[0])
                            == value;
            default -> KantoMainline.condition(db,owner,c);
        };
    }

    private void node(String name) {
        if(activeRegion==1&&HoennStory.PROGRAMS.contains(program))hoenn.resume(program,name,position);
        actions = program.getAsJsonObject("nodes").getAsJsonObject(name).getAsJsonArray("actions");
        index = 0;
        if(KantoMainline.PROGRAMS.contains(program)) {
            KantoMainline.commitNode(db,owner,program.get("id").getAsString()+"#"+name,actions,itemTemplates,templates,random);
            if(actions.asList().stream().anyMatch(a->KantoMainline.DURABLE.contains(a.getAsJsonObject().get("type").getAsString()))) {inventoryChanged=true;entitySnapshot=null;}
        }
    }

    private Scene advance() {
        for (int guard = 0; actions != null && index < actions.size() && guard < 100; guard++) {
            var a = actions.get(index++).getAsJsonObject();
            String type = a.get("type").getAsString();
            if(KantoMainline.PROGRAMS.contains(program)&&KantoMainline.DURABLE.contains(type))continue;
            switch (type) {
                // Native SetFlavorTextFlagFromSpecialVars: unlock this fact and at least the silhouette.
                // Reading it again must not downgrade an already colored portrait (pick_state=2).
                case "HOENN_OPERATION" -> {
                    hoenn.operation(a,itemTemplates,choice->{
                        var party=new PokemonStore(db,templates).load(owner);int slot=0;while(slot<6&&party.at(1,slot)!=null)slot++;
                        if(slot==6)throw new IllegalStateException("Party is full");return makeStarter(choice,true,slot);
                    });inventoryChanged=true;entitySnapshot=null;
                }
                case "SET_FAME_CHECKER_FLAG" -> db.execute(
                        "INSERT INTO character_kanto_fame_checker(character_id,person,flavor_flags,pick_state)"
                                + " VALUES (?,?,?,1) ON CONFLICT(character_id,person) DO UPDATE SET"
                                + " flavor_flags=flavor_flags | excluded.flavor_flags, pick_state=MAX(pick_state,1)",
                        owner, a.get("person").getAsInt(), 1 << a.get("entry").getAsInt());
                case "SET_NATIVE_VAR" -> db.execute(
                        "INSERT INTO character_kanto_variables(character_id,variable,value) VALUES (?,?,?)"
                                + " ON CONFLICT(character_id,variable) DO UPDATE SET value=excluded.value",
                        owner, a.get("variable").getAsInt(), a.get("value").getAsInt());
                case "SET_OPENING_STAGE" -> {
                    int expected = a.get("expected").getAsInt(),
                            next = a.get("nextStage").getAsInt();
                    var s = state();
                    if (s.stage() < next) {
                        boolean battleExit=expected==3&&next==4&&resumeActive&&program==PROGRAM&&s.starter()!=3;
                        if (s.stage() != expected || s.starter() != 3&&!battleExit)
                            throw new IllegalStateException("Conflicting story checkpoint");
                        db.execute(
                                "UPDATE character_kanto_story SET stage=? WHERE character_id=? AND"
                                        + " stage=?",
                                next,
                                owner,
                                expected);
                    }
                }
                case "BRANCH" ->
                        node(
                                a.get(condition(a.getAsJsonObject("condition")) ? "yes" : "no")
                                        .getAsString());
                case "GOTO" -> node(a.get("node").getAsString());
                case "DAISY_TOWN_MAP" -> {
                    if (position.bank()!=4 || position.map()!=2) throw new IllegalStateException("Daisy map mismatch");
                    daisy.giveMap(itemTemplates); inventoryChanged=true; entitySnapshot=null;
                }
                case "PARCEL_OPERATION" -> {
                    String operation=a.get("operation").getAsString();
                    String required=operation.equals("PICKUP")?"ViridianCity_Mart":"PalletTown_ProfessorOaksLab";
                    if(!required.equals(mapKey(position)))throw new IllegalStateException("角色已离开剧情所在地图");
                    OakParcel.commit(db,owner,state(),operation,itemTemplates);
                    inventoryChanged=true;
                    entitySnapshot=null;
                }
                case "HEAL_PARTY" -> {
                    db.transaction(this::heal);
                    partyHealed = true;
                }
                case "HIDE_TEMP_NPC" -> {
                    temporaryHidden.add(position.bank()+":"+position.map()+":"+(a.has("entityIdx")?a.get("entityIdx").getAsInt():interactionNpc));entitySnapshot=null;
                    if(activeRegion==1)hoenn.removed(a.has("entityIdx")?a.get("entityIdx").getAsInt():interactionNpc,position);
                }
                case "HOENN_DIVE" -> {
                    var target=hoenn.diveTarget(position);if(target==null)throw new IllegalStateException("Not a native Dive location");
                    waiting=new Action(SEQUENCES.incrementAndGet(),"CHANGE_MAP",0,-1,0,target,null,0,"");return new Scene(state(),waiting);
                }
                case "HOENN_ROTATE" -> {
                    int start=HoennMainline.PROGRAM.get("rotatingStart").getAsInt(),color=a.get("color").getAsInt();var replacement=new JsonArray();
                    for(var m:entities())if(m.bank()==position.bank()&&m.map()==position.map())for(var n:m.npcs()){
                        int tile=(HoennWorld.block(position,n.x(),n.y())&1023)-start;if(tile<0||tile/8!=color||tile%8>=4)continue;
                        int d=tile%8,dx=d==0?1:d==2?-1:0,dy=d==1?1:d==3?-1:0;var move=new JsonObject();move.addProperty("type","MOVE_NPC");move.addProperty("message","ROTATING_TILE_SHIFT");move.addProperty("actorIndex",n.id());var target=new JsonObject();target.addProperty("x",n.x()+dx);target.addProperty("y",n.y()+dy);move.add("target",target);replacement.add(move);
                        var face=new JsonObject();face.addProperty("type","FACE_NPC");face.addProperty("actorIndex",n.id());face.addProperty("toward",switch(n.direction()){case 3->1;case 0->3;case 2->0;default->2;});replacement.add(face);
                    }
                    for(int i=index;i<actions.size();i++)replacement.add(actions.get(i));actions=replacement;index=0;
                }
                case "REJECT_COORDINATE_MOVE" -> {
                    if(coordinateInteraction){
                        int[] dx={0,0,1,-1},dy={-1,1,0,0};
                        waiting=new Action(SEQUENCES.incrementAndGet(),"MOVE_PLAYER",0,-1,0,at(position.x()+dx[position.direction()],position.y()+dy[position.direction()]),null,0,"");return new Scene(state(),waiting);
                    }
                }
                case "MECHANISM_OPERATION" -> {mechanisms.operate(a.get("operation").getAsString());entitySnapshot=null;}
                case "SET_ELITE_STAGE" -> {
                    KantoMainline.ensure(db,owner);
                    db.execute("UPDATE character_kanto_mainline SET elite=? WHERE character_id=?",a.get("stage").getAsInt(),owner);
                    entitySnapshot=null;
                }
                case "FINISH_LEAGUE_REMATCH" -> {
                    db.transaction(()->{
                        KantoPostgame.active(db,owner,false);
                        db.execute("UPDATE character_kanto_mainline SET elite=0 WHERE character_id=?",owner);
                        db.execute("DELETE FROM character_hall_of_fame WHERE character_id=?",owner);
                    });entitySnapshot=null;
                }
                case "START_SAFARI" -> {
                    boolean[] paid={false};
                    db.transaction(()->{
                        int money=Integer.parseInt(db.query("SELECT money FROM characters WHERE id=?",owner).get(0)[0]);
                        int fee=a.get("fee").getAsInt();
                        if(money<fee)return;
                        KantoMainline.ensure(db,owner);
                        db.execute("UPDATE characters SET money=money-? WHERE id=?",fee,owner);
                        db.execute("UPDATE character_kanto_mainline SET safari_steps=?,safari_balls=? WHERE character_id=?",a.get("steps").getAsInt(),a.get("balls").getAsInt(),owner);paid[0]=true;
                    });
                    if(!paid[0]){actions=null;return new Scene(state(),null);}
                    inventoryChanged=true;
                }
                case "RUN_MAP_READY" -> { actions=null;clearResume();return request(Request.ready(),position,0); }
                case "SIGN_LADY_ASIDE" -> signLady.movedAside();
                case "SIGN_LADY_READ" -> {
                    signLady.read();
                    db.execute("INSERT INTO character_kanto_variables(character_id,variable,value) VALUES (?,16496,1)"
                            + " ON CONFLICT(character_id,variable) DO UPDATE SET value=excluded.value", owner);
                }
                case "CLOSE_DIALOG" -> {}
                case "HALL_OF_FAME" -> {
                    if(activeRegion==1){waiting=new Action(SEQUENCES.incrementAndGet(),type,0,-1,0,null,null,0,"",0,HoennMainline.hall(db,owner,templates));return new Scene(state(),waiting);}
                    if(!KantoMainline.progress(db,owner).champion())throw new IllegalStateException("Hall of Fame requires champion");
                    var saved=db.query("SELECT team,completed FROM character_hall_of_fame WHERE character_id=?",owner);
                    if(!saved.isEmpty()&&saved.get(0)[1].equals("1"))continue;
                    String team;
                    if(saved.isEmpty()){
                        var members=new PokemonStore(db,templates).load(owner).pokemons().stream()
                                .filter(p->p.container()==PokemonData.PARTY).sorted(Comparator.comparingInt(PokemonData.Pokemon::position)).toList();
                        if(members.isEmpty())throw new IllegalStateException("Cannot record an empty Hall of Fame team");
                        team=new Gson().toJson(members);
                        db.execute("INSERT INTO character_hall_of_fame(character_id,team,completed) VALUES (?,?,0)",owner,team);
                    }else team=saved.get(0)[0];
                    waiting=new Action(SEQUENCES.incrementAndGet(),type,0,-1,0,null,null,1016,"",0,
                            JsonParser.parseString(team).getAsJsonArray().asList().stream().map(JsonElement::toString).toList());
                    return new Scene(state(),waiting);
                }
                case "NOTIFY" -> {
                    if(KantoMainline.PROGRAMS.contains(program)&&a.has("message")&&!a.get("message").getAsString().contains("\uFFFD")) {
                        waiting=new Action(SEQUENCES.incrementAndGet(),"NOTIFY",0,-1,0,null,null,0,a.get("message").getAsString());return new Scene(state(),waiting);
                    }
                }
                case "CLOSE_SCENE" -> {
                    clearResume();
                    actions = null;
                    waiting = null;
                    return new Scene(state(), null);
                }
                default -> {
                    int text = 0, species = a.has("starterSpecies")&&activeRegion==1?hoenn.species():0, actor = interactionNpc, music = 0;
                    GameService.Position target = null, playerTarget = null;
                    if(a.has("actorIndex"))actor=a.get("actorIndex").getAsInt();
                    if (a.has("actor"))
                        actor =
                                program.getAsJsonObject("actors")
                                        .getAsJsonObject(a.get("actor").getAsString())
                                        .get("entityIdx")
                                        .getAsInt();
                    if ((type.equals("DIALOGUE") || type.equals("YES_NO")) && a.has("text"))
                        text =
                                program.getAsJsonObject("text")
                                        .get(a.get("text").getAsString())
                                        .getAsInt();
                    else if (type.equals("SELECT_STARTER")) {
                        int choice =
                                switch (interactionNpc) {
                                    case 4 -> 0;
                                    case 5 -> 2;
                                    case 6 -> 1;
                                    default ->
                                            throw new IllegalStateException("Invalid starter ball");
                                };
                        var starter = starter(choice);
                        species = starter.get("species").getAsInt();
                        text = starter.get("prompt").getAsInt();
                        starterNext = a.get("next").getAsString();
                        actor = interactionNpc;
                    } else if (type.equals("PLAY_MUSIC")) music = a.get("musicId").getAsInt();
                    else if (type.equals("CHANGE_MAP"))
                        target =
                                mapPosition(
                                        a.get("map").getAsString(),
                                        a.get("x").getAsInt(),
                                        a.get("y").getAsInt(),
                                        a.get("toward").getAsInt());
                    else if (type.equals("MOVE_PLAYER"))
                        target = at(coordinate(a, "x"), coordinate(a, "y"));
                    else if (type.equals("MOVE_NPC")) {
                        var t = a.getAsJsonObject("target");
                        target = at(coordinate(t, "x"), coordinate(t, "y"));
                    } else if (type.equals("GUIDE")) {
                        target = at(a.get("npcX").getAsInt(), a.get("npcY").getAsInt());
                        playerTarget = at(a.get("playerX").getAsInt(), a.get("playerY").getAsInt());
                    } else if (type.equals("FACE_NPC")) {
                        var npc=entities().stream().filter(m->m.bank()==position.bank()&&m.map()==position.map())
                                .flatMap(m->m.npcs().stream()).filter(n->n.id()==actorId(a)).findFirst().orElseThrow();
                        int facing = a.get("toward").getAsString().equals("PLAYER")
                                ? position.x()!=npc.x() ? position.x()<npc.x()?2:3
                                        : position.y()<npc.y()?1:0
                                : a.get("toward").getAsInt();
                        target=new GameService.Position(position.bank(),position.map(),npc.x(),npc.y(),facing);
                    } else if (type.equals("FACE_PLAYER")) target=new GameService.Position(position.bank(),position.map(),position.x(),position.y(),a.get("toward").getAsInt());
                    else if (type.equals("REJECT_MOVE")) {
                        int[] dx={0,0,1,-1},dy={-1,1,0,0};target=at(position.x()+dx[position.direction()],position.y()+dy[position.direction()]);type="MOVE_PLAYER";
                    }
                    else if (type.equals("START_TRAINER_BATTLE")||type.equals("START_BATTLE")) {if(a.has("finishNode"))trainerEvent=null;battleAction=a;type="START_BATTLE";battleSettled=false;}
                    else if (!type.equals("START_BATTLE") && !type.equals("NPC_REACTION") && !type.equals("WAIT") && !type.equals("DIALOGUE") && !type.equals("YES_NO") && !type.equals("SELECT_OPTION"))
                        throw new IllegalStateException("Unsupported story action " + type);
                    if (type.equals("YES_NO")||type.equals("SELECT_OPTION")&&a.has("nodes")) question = a;
                    waiting =
                            new Action(
                                    SEQUENCES.incrementAndGet(),
                                    type,
                                    text,
                                    a.has("signpost") && a.get("signpost").getAsBoolean() ? -1 : actor,
                                    species,
                                    target,
                                    playerTarget,
                                    music,
                                    a.has("message") ? a.get("message").getAsString() : a.has("battleType") ? a.get("battleType").getAsString() : a.has("facePlayer") && a.get("facePlayer").getAsBoolean() ? "FACE_PLAYER" : "",
                                    a.has("duration") ? a.get("duration").getAsInt() : 0,
                                    a.has("options")?a.getAsJsonArray("options").asList().stream().map(JsonElement::getAsString).toList():List.of());
                    return new Scene(state(), waiting);
                }
            }
        }
        actions = null;
        clearResume();
        return new Scene(state(), null);
    }

    private void clearResume() {
        if(activeRegion==1){hoenn.clearResume();return;}
        if(resumeActive){db.execute("DELETE FROM character_story_resume WHERE character_id=?",owner);resumeActive=false;}
    }

    private int actorId(JsonObject action) {
        if(action.has("actorIndex"))return action.get("actorIndex").getAsInt();
        return program.getAsJsonObject("actors").getAsJsonObject(action.get("actor").getAsString()).get("entityIdx").getAsInt();
    }

    private int coordinate(JsonObject a, String name) {
        String value=a.get(name).getAsString();
        int base=value.equals("PLAYER_X")?position.x():value.equals("PLAYER_Y")?position.y():a.get(name).getAsInt();
        return base+(a.has(name+"Offset")?a.get(name+"Offset").getAsInt():0);
    }

    private GameService.Position at(int x, int y) {
        return new GameService.Position(
                position.bank(), position.map(), x, y, position.direction());
    }

    String mapKey(GameService.Position p) {
        if(activeRegion==1)return HoennWorld.key(p);
        for (var m : MAPS) {
            var o = m.getAsJsonObject();
            if (o.get("bank").getAsInt() == p.bank() && o.get("map").getAsInt() == p.map())
                return o.get("key").getAsString();
        }
        return MapEntities.mapKey(p.bank(),p.map());
    }

    private GameService.Position mapPosition(String key, int x, int y, int direction) {
        if(activeRegion==1)return HoennWorld.position(key,x,y,direction);
        for (var m : MAPS) {
            var o = m.getAsJsonObject();
            if (o.get("key").getAsString().equals(key))
                return new GameService.Position(
                        o.get("bank").getAsInt(), o.get("map").getAsInt(), x, y, direction);
        }
        return MapEntities.position(key,x,y,direction);
    }

    private static JsonObject starter(int choice) {
        for (var s : PROGRAM.getAsJsonObject("resources").getAsJsonArray("starters"))
            if (s.getAsJsonObject().get("choice").getAsInt() == choice) return s.getAsJsonObject();
        throw new IllegalArgumentException("Invalid starter");
    }

    private void claim(int choice) {
        db.transaction(
                () -> {
                    var s = state();
                    if (s.stage() >= 3) return;
                    if (s.stage() != 2 || s.starter() != 3)
                        throw new IllegalStateException("Cannot claim a starter here");
                    var store = new PokemonStore(db, templates);
                    var party = store.load(owner);
                    int slot = 0;
                    while (slot < 6 && party.at(1, slot) != null) slot++;
                    if (slot == 6) throw new IllegalStateException("Party is full");
                    var pokemon = makeStarter(choice, true, slot);
                    store.insert(owner, pokemon);
                    db.execute(
                            "UPDATE pokemon SET"
                                + " friend_value=70,normal_ribbon='2',moves_pp='-1,-1,-1,-1' WHERE"
                                + " id=?",
                            pokemon.id());
                    db.execute(
                            "INSERT INTO character_pokedex_entry(character_id,species,flags) VALUES"
                                    + " (?,?,3) ON CONFLICT(character_id,species) DO UPDATE SET"
                                    + " flags=flags|3",
                            owner,
                            pokemon.species());
                    db.execute(
                            "UPDATE character_kanto_story SET stage=3,starter=? WHERE"
                                    + " character_id=? AND stage=2",
                            choice,
                            owner);
                });
    }

    PokemonData.Pokemon makeStarter(int choice, boolean gift, int slot) {
        var entry = activeRegion==1?hoenn.starter(choice):starter(choice);
        int species = entry.get("species").getAsInt();
        var template = templates.get(species);
        if (template == null) throw new IllegalStateException("Missing story species data");
        int personality = random.nextInt(Integer.MAX_VALUE),
                nature = personality % 25,
                iv = gift ? 15 : activeRegion==1 ? 0 : 10;
        var ivs = Collections.nCopies(6, iv);
        var stats = new ArrayList<Integer>();
        int[] natureStats = {1, 2, 5, 3, 4};
        for (int i = 0; i < 6; i++) {
            int n =
                    ((template.stats().get(i) * 2 + iv + (i == 0 ? 100 : 0)) * 5) / 100
                            + (i == 0 ? 10 : 5);
            int up = natureStats[nature / 5], down = natureStats[nature % 5];
            stats.add((int) (n * (up == down ? 1 : i == up ? 1.1 : i == down ? .9 : 1)));
        }
        var moves = new ArrayList<Integer>();
        for (var move : entry.getAsJsonArray("moves")) moves.add(move.getAsInt());
        int gender =
                template.genderRatio() == 255
                        ? -1
                        : (personality & 255) >= template.genderRatio() ? 0 : 1;
        return new PokemonData.Pokemon(
                UUID.randomUUID().toString(),
                gift ? 1 : 9,
                slot,
                species,
                0,
                gender,
                false,
                false,
                "",
                5,
                stats.get(0),
                0,
                0,
                nature,
                template.ability(),
                stats,
                ivs,
                Collections.nCopies(6, 0),
                moves,
                70);
    }

    boolean captureTutorial(){return battleAction!=null&&battleAction.has("battleType")&&battleAction.get("battleType").getAsString().equals("CAPTURE_TUTORIAL");}
    boolean scriptedWild(){return battleAction!=null&&battleAction.has("wildSpecies");}
    boolean scriptedWildCaptureAllowed(){return battleAction!=null&&battleAction.has("wildSpecies")&&(!battleAction.has("noCapture")||!battleAction.get("noCapture").getAsBoolean());}
    EncounterData battle() {
        if(chapter()!=null)return chapter().battle();
        if(activeRegion==1){
            if(waiting==null||!waiting.type().equals("START_BATTLE")||battleAction==null)throw new IllegalStateException("No Hoenn battle pending");
            var party=new PokemonStore(db,templates).load(owner).pokemons().stream().filter(p->p.container()==1).toList();
            var lead=party.stream().filter(p->!p.egg()&&p.hp()>0).findFirst().orElseThrow();
            if(battleAction.has("wildSpecies"))return new EncounterData(lead,PokemonStore.createWildPokemon(templates.get(battleAction.get("wildSpecies").getAsInt()),battleAction.get("wildLevel").getAsInt(),9,0,random),party);
            if(battleAction.has("nativeTrainer")||battleAction.has("nativeTrainers"))return HoennMainline.battle(battleAction,party,templates,random,state().starter());
            var rival=makeStarter((state().starter()+1)%3,false,0);
            return new EncounterData(lead,List.of(rival),party,new EncounterData.Trainer(-1,1,hoenn.gender()==0?72:71,300));
        }
        if(waiting!=null&&waiting.type().equals("START_BATTLE")&&battleAction!=null&&state().stage()>=6) {
            var party=new PokemonStore(db,templates).load(owner).pokemons().stream().filter(p->p.container()==1).toList();
            var lead=party.stream().filter(p->!p.egg()&&p.hp()>0).findFirst().orElseThrow(()->new IllegalStateException("队伍没有可战斗的宝可梦"));
            if(captureTutorial())return new EncounterData(lead,List.of(PokemonStore.createWildPokemon(templates.get(13),5,9,0,random)),party,null,true);
            if(battleAction.has("wildSpecies"))return new EncounterData(lead,PokemonStore.createWildPokemon(templates.get(battleAction.get("wildSpecies").getAsInt()),battleAction.get("wildLevel").getAsInt(),9,0,random),party);
            if(!battleAction.has("trainerTeamId")&&!battleAction.has("trainerTeamIds"))return KantoMainline.inline(battleAction,party,templates,random);
            int team=battleAction.has("trainerTeamIds")?battleAction.getAsJsonArray("trainerTeamIds").get(state().starter()).getAsInt():battleAction.get("trainerTeamId").getAsInt();
            return TrainerEncounters.create(KantoMainline.team(team),party,templates,random);
        }
        if (waiting == null || !waiting.type().equals("START_BATTLE") || state().stage() != 3)
            throw new IllegalStateException("No story battle pending");
        var party =
                new PokemonStore(db, templates)
                        .load(owner).pokemons().stream().filter(p -> p.container() == 1).toList();
        int rival =
                switch (state().starter()) {
                    case 0 -> 1;
                    case 1 -> 2;
                    case 2 -> 0;
                    default -> throw new IllegalStateException();
                };
        return new EncounterData(
                party.stream().filter(p -> p.hp() > 0 && !p.egg()).findFirst().orElseThrow(),
                List.of(makeStarter(rival, false, 0)),
                party,
                new EncounterData.Trainer(rival==0?327:rival==1?328:326, 0, 328, 0));
    }

    void persistCapture() {
        if(!captureTutorial()&&!scriptedWildCaptureAllowed())throw new IllegalStateException("This story target cannot be captured");
        if(captureTutorial())KantoMainline.flag(db,owner,"SET_STORY_BIT",5);
        KantoMainline.resume(db,owner,program,battleAction.get("finishNode").getAsString(),position);
    }
    void finishBattle(boolean won) {
        if(chapter()!=null){chapter().finishBattle(won);return;}
        if(activeRegion==1){if(battleSettled)return;db.transaction(()->hoenn.finishBattle(won,program,battleAction,position));battleSettled=true;waiting=null;actions=null;entitySnapshot=null;return;}
        if(battleSettled)return;
        if(battleAction!=null&&state().stage()>=6) {
            db.transaction(()->{
                if(won) {
                    if(captureTutorial())KantoMainline.flag(db,owner,"SET_STORY_BIT",5);
                    if(trainerEvent!=null)KantoMainline.eventSet(db,owner,trainerEvent);
                    if(battleAction.has("finishNode"))KantoMainline.resume(db,owner,program,battleAction.get("finishNode").getAsString(),position);
                } else {
                    KantoMainline.ensure(db,owner);db.execute("UPDATE character_kanto_mainline SET elite=0 WHERE character_id=?",owner);
                    KantoPostgame.active(db,owner,false);
                    if(battleAction.has("defeatNode"))KantoMainline.resume(db,owner,program,battleAction.get("defeatNode").getAsString(),position);
                }
            });
            battleSettled=true;waiting=null;actions=null;entitySnapshot=null;return;
        }
        db.transaction(
                () -> {
                    if (state().stage() != 3) return;
                    if(!db.query("SELECT node FROM character_story_resume WHERE character_id=? AND program=?",owner,PROGRAM.get("id").getAsString()).isEmpty())return;
                    if (won) db.execute("UPDATE characters SET money=money+80 WHERE id=?", owner);
                    heal();
                    KantoMainline.resume(db,owner,PROGRAM,"battle_complete",position);
                });
        waiting = null;
        actions = null;
        battleSettled=true;entitySnapshot=null;
    }

    private void heal() {
        for (var p : new PokemonStore(db, templates).load(owner).pokemons())
            if (p.container() == 1)
                db.execute(
                        "UPDATE pokemon SET current_hp=?,status=0,moves_pp='-1,-1,-1,-1' WHERE id=?"
                                + " AND trainer_id=?",
                        p.maxHp(),
                        p.id(),
                        owner);
    }
}
