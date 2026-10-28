package top.totodile.pokemmo.client.ui.world;

import com.badlogic.gdx.Gdx;

import de.matthiasmann.twl.Widget;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.world.WorldScene;
import top.totodile.pokemmo.server.game.*;
import top.totodile.pokemmo.server.login.LoginService.Selection;

import java.util.*;
import java.util.function.Consumer;

/** Reference StoryActionExecutor scenes, advanced only after dialogue/movement/map completion. */
public final class KantoStoryController implements AutoCloseable {
    private final GameService game;
    private final Selection selection;
    private final GameService.Entry entry;
    private final WorldScene world;
    private final ClientResources resources;
    private final StoryDialogue dialogue;
    private final boolean mobile;
    private top.totodile.pokemmo.client.ui.pokemon.PokemonEvolutionWindow evolution;
    private top.totodile.pokemmo.client.ui.pokemon.HallOfFameWindow hall;
    private final Consumer<java.util.concurrent.CompletionStage<EncounterData>> battle;
    private final Runnable refresh;
    private final Consumer<Throwable> error;
    private Consumer<String> notifications=text->{};
    public void notifications(Consumer<String> consumer){notifications=consumer;}
    private StoryData.Action action;
    private StoryData.State state = new StoryData.State(false, 0, 3);
    private boolean pending, started, blockingRequest;
    private volatile boolean closed, suspended;
    public void suspend(){suspended=true;steps.clear();queuedInteraction=false;}
    private long lastPushNanos;
    private boolean healing;
    private record Check(StoryData.Request request, GameService.Position position,top.totodile.pokemmo.client.world.PlayerMotion.Step accepted){
        Check(StoryData.Request request,GameService.Position position){this(request,position,null);}
    }
    private Consumer<top.totodile.pokemmo.client.world.PlayerMotion.Step> idleStep=step->{};
    public void idleStep(Consumer<top.totodile.pokemmo.client.world.PlayerMotion.Step> callback){idleStep=callback;}
    private final Deque<Check> steps = new ArrayDeque<>();
    private boolean queuedInteraction;
    private boolean initialized;
    private int routeIndex;
    private List<int[]> path = List.of();
    private List<int[]> npcPath = List.of();
    private int npcRouteIndex;
    private long actionStarted;
    private top.totodile.pokemmo.client.rom.nds.bw.BwPokedex learningDex;
    private final Map<RomType, top.totodile.pokemmo.client.rom.gba.GbaStoryText> texts = new EnumMap<>(RomType.class);
    private top.totodile.pokemmo.client.rom.nds.NdsArchive unovaText;
    private long lastSequence = -1;
    private GameService.Position lastPosition;

    public KantoStoryController(
            Widget root,
            boolean mobile,
            ClientResources resources,
            GameService game,
            Selection selection,
            GameService.Entry entry,
            WorldScene world,
            Consumer<java.util.concurrent.CompletionStage<EncounterData>> battle,
            Runnable refresh,
            Consumer<Throwable> error) {
        this.resources = resources;
        this.mobile = mobile;
        this.game = game;
        this.selection = selection;
        this.entry = entry;
        this.world = world;
        this.battle = battle;
        this.refresh = refresh;
        this.error = error;
        dialogue = new StoryDialogue(root, resources.strings(), mobile);
        world.motion().addStepListener(step -> {
            if (closed || action != null || blockingRequest) return;
            boolean changed = step.before().bank() != step.after().bank()
                    || step.before().map() != step.after().map();
            steps.addLast(new Check(changed ? StoryData.Request.ready() : StoryData.Request.step(), step.after(),step));
            lastPosition = step.after();
        });
        world.motion().addBlockedListener(position->{
            if(closed||action!=null||blockingRequest)return;
            int actor=world.facingStrengthBoulder();if(actor<0||System.nanoTime()-lastPushNanos<200_000_000L)return;lastPushNanos=System.nanoTime();
            steps.addLast(new Check(new StoryData.Request("PUSH",actor,0,0),position));blockingRequest=true;
        });
    }

    public boolean busy() {
        return healing || blockingRequest || action != null;
    }

    public void resume() {
        suspended=false;
        if(action!=null&&action.type().equals("START_BATTLE")){action=null;started=false;steps.clear();}
        lastPosition=null;
    }

    public void start() {
        suspended=false;
        send(StoryData.Request.ready());
    }

    public boolean learningKey(int key){return hall!=null&&hall.key(key)||evolution!=null&&evolution.key(key)||dialogue.learningKey(key);}

    public boolean cinematicKey(int key){
        if(hall==null||!hall.isOpen())return false;
        int confirm=Integer.parseInt(resources.preferences().get("client.controls.gdx.key_a",Integer.toString(com.badlogic.gdx.Input.Keys.J)));
        int cancel=Integer.parseInt(resources.preferences().get("client.controls.gdx.key_b",Integer.toString(com.badlogic.gdx.Input.Keys.X)));
        if(key==confirm||key==cancel||key==com.badlogic.gdx.Input.Keys.ESCAPE||key==com.badlogic.gdx.Input.Keys.BACK||key==com.badlogic.gdx.Input.Keys.ENTER)
            return hall.key(de.matthiasmann.twl.Event.KEY_RETURN);
        return hall.key(0);
    }

    public boolean interact() {
        if (dialogue.isOpen()) {
            dialogue.advance();
            return true;
        }
        if (busy()) return true;
        if (pending) {
            queuedInteraction = true;
            blockingRequest = true;
            return true;
        }
        int npc = world.facingNpc();
        if (npc >= 0) send(StoryData.Request.npc(npc));
        else {
            int background = world.facingBackground();
            if (background < 0) {if(entry.region()==1){send(new StoryData.Request("FIELD",-1,0,0));return true;}return false;}
            send(StoryData.Request.background(background));
        }
        return true;
    }

    private void send(StoryData.Request request) {
        send(request, world.movement().position());
    }

    private void send(StoryData.Request request, GameService.Position position) {send(request,position,null);}
    private void send(StoryData.Request request, GameService.Position position,top.totodile.pokemmo.client.world.PlayerMotion.Step accepted) {
        if (closed || suspended || (entry.region()<0||entry.region()>4) || pending) return;
        pending = true;
        blockingRequest = !initialized || request.event().equals("NPC")
                || request.event().equals("BACKGROUND") || request.event().equals("REPLY") || request.event().equals("PUSH");
        // Accepted movement already persists through PlayerMotion.save; do not write it twice.
        java.util.concurrent.CompletionStage<Void> saved = request.event().equals("COORDINATE")
                ? java.util.concurrent.CompletableFuture.completedFuture(null)
                : game.savePosition(selection, entry.character().id(), position);
        saved.thenCompose(v -> closed || suspended
                        ? java.util.concurrent.CompletableFuture.completedFuture(new StoryData.Scene(state,null))
                        : game.story(selection, entry.character().id(), request.at(position)))
                .whenComplete(
                        (scene, failure) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            if (closed) return;
                                            pending = false;
                                            blockingRequest = false;
                                            if(suspended)return;
                                            initialized = true;
                                            if (failure != null) {
                                                action = null;
                                                world.dialogueNpc(-1);
                                                error.accept(failure);
                                                return;
                                            }
                                            if (scene.partyHealed()) {
                                                healing = true;
                                                world.rest(() -> { if (!closed) refresh.run(); })
                                                        .whenComplete((ignored, animationFailure) -> {
                                                            healing = false;
                                                            lastPosition = null;
                                                            if (!closed && animationFailure != null)
                                                                error.accept(animationFailure);
                                                        });
                                            }
                                            if (scene.inventoryChanged()) refresh.run();
                                            state = scene.state();
                                            world.motion().runningShoes(scene.runningShoes());
                                            world.motion().traversal(scene.surfAllowed(),scene.bicycleAllowed());
                                            world.mapEntities(scene.entities());
                                            action = scene.action();
                                            if (action != null) {
                                                if (request.event().equals("COORDINATE") || request.event().equals("MAP_READY")) {
                                                    // Match the reference server's event lock and position correction.
                                                    world.setInputBlocked(true,true);
                                                    world.storyPosition(position);
                                                }
                                                steps.clear();
                                                queuedInteraction = false;
                                            }
                                            started = false;
                                            path = List.of();
                                            routeIndex = 0;
                                            if (action == null) {
                                                if(accepted!=null&&!queuedInteraction)idleStep.accept(accepted);
                                                world.dialogueNpc(-1);
                                                if (request.event().equals("REPLY")) refresh.run();
                                                if (queuedInteraction) {
                                                    queuedInteraction = false;
                                                    interact();
                                                }
                                                return;
                                            }
                                            if (action.sequence() == lastSequence) return;
                                            lastSequence = action.sequence();
                                        }));
    }

    private void done() {
        world.motion().storyWalking(false);
        long sequence = action.sequence();
        action = null;
        send(StoryData.Request.reply(sequence, 0));
    }

    public void update() {
        if(suspended)return;
        if(hall!=null&&hall.isOpen()){hall.update();return;}
        if(evolution!=null&&evolution.isOpen()){evolution.update();return;}
        dialogue.anchor(action == null ? null : world.dialogueAnchor(action.actor(),
                dialogue.getOwner().getInnerWidth(), dialogue.getOwner().getInnerHeight()));
        dialogue.positionWindow();
        if (closed || pending || healing || (entry.region()<0||entry.region()>4)) return;
        if (action == null) {
            if (!steps.isEmpty()) {
                var step = steps.removeFirst();
                send(step.request(), step.position(),step.accepted());
                return;
            }
            var p = world.movement().position();
            if (!world.motion().moving() && !p.equals(lastPosition)) {
                boolean mapChanged =
                        lastPosition == null
                                || p.bank() != lastPosition.bank()
                                || p.map() != lastPosition.map();
                lastPosition = p;
                send(mapChanged ? StoryData.Request.ready() : StoryData.Request.step());
            }
            return;
        }
        try {
            switch (action.type()) {
                case "HALL_OF_FAME" -> {
                    if(!started){started=true;
                        if(learningDex==null)learningDex=new top.totodile.pokemmo.client.rom.nds.bw.BwPokedex(resources.roms().get(RomType.BLACK_WHITE),resources.strings());
                        if(hall!=null)hall.close();
                        hall=new top.totodile.pokemmo.client.ui.pokemon.HallOfFameWindow(dialogue.getOwner(),mobile,resources,learningDex,entry.character(),
                                StoryData.hallParty(action));
                        hall.open(()->{done();if(resources.audio()!=null)resources.audio().music(world.music(System.currentTimeMillis()));});
                    }
                }
                case "EVOLUTION" -> {
                    if(!started){started=true;var current=action;
                        if(learningDex==null)learningDex=new top.totodile.pokemmo.client.rom.nds.bw.BwPokedex(resources.roms().get(RomType.BLACK_WHITE),resources.strings());
                        if(evolution==null)evolution=new top.totodile.pokemmo.client.ui.pokemon.PokemonEvolutionWindow(dialogue.getOwner(),mobile,resources,learningDex);
                        evolution.open(current.species(),Integer.parseInt(current.options().get(0)),Integer.parseInt(current.options().get(1)),Boolean.parseBoolean(current.options().get(2)),current.message(),current.options().size()<4||Boolean.parseBoolean(current.options().get(3)),answer->{action=null;lastPosition=null;send(StoryData.Request.reply(current.sequence(),answer));if(resources.audio()!=null)resources.audio().music(world.music(System.currentTimeMillis()));});
                    }
                }
                case "SELECT_LEARN_MOVE" -> {
                    if(!started){started=true;var current=action;
                        if(learningDex==null)learningDex=new top.totodile.pokemmo.client.rom.nds.bw.BwPokedex(resources.roms().get(RomType.BLACK_WHITE),resources.strings());
                        dialogue.showLearning(resources.strings().get(8096),current.options().stream().map(id->learningDex.moveName(Integer.parseInt(id))).toList(),learningDex.moveName(current.species()),answer->{action=null;lastPosition=null;send(StoryData.Request.reply(current.sequence(),answer));});
                    }
                }
                case "SELECT_OPTION" -> {
                    if(!started){started=true;var current=action;dialogue.showChoices(current.text()==0?current.message():text(current.text(),current.species()),current.options(),answer->{action=null;send(StoryData.Request.reply(current.sequence(),answer));});}
                }
                case "DIALOGUE", "SELECT_STARTER", "YES_NO" -> {
                    if (!started) {
                        started = true;
                        var current = action;
                        if (current.message().equals("FACE_PLAYER") || current.text()==0&&current.actor()>=0) world.dialogueNpc(current.actor());
                        java.util.function.IntConsumer reply=answer -> {
                                    action = null;
                                    send(StoryData.Request.reply(current.sequence(), answer));
                                };
                        String message=current.text()==0 ? current.message() : text(current.text(),current.species());
                        if(current.type().equals("SELECT_STARTER"))dialogue.showStarter(message,current.species(),resources,reply);
                        else dialogue.show(message,current.type().equals("YES_NO"),reply);
                    }
                }
                case "WAIT", "NPC_REACTION" -> {
                    if(!started){started=true;actionStarted=System.nanoTime();
                        if(action.type().equals("NPC_REACTION")) {
                            world.storyNpcReaction(action.actor(),true);
                            // Native SignLady bytecode playse 0x0015 (SE_PIN).
                            if(resources.audio()!=null)resources.audio().sound(this,1,0,21,0,1,0);
                        }
                    }
                    if(System.nanoTime()-actionStarted>=action.duration()*1_000_000L){
                        world.storyNpcReaction(action.actor(),false);done();
                    }
                }
                case "FACE_NPC" -> {world.storyNpcFace(action.actor(),action.target().direction());done();}
                case "FACE_PLAYER" -> {world.storyPosition(action.target());done();}
                case "PLAY_MUSIC" -> {
                    if (resources.audio() != null)
                        resources
                                .audio()
                                .music(
                                        new top.totodile.pokemmo.client.audio.RomAudio.Track(
                                                0, action.music()));
                    done();
                }
                case "MOVE_NPC" -> {
                    if (!started) {
                        started = true;
                        if (Set.of("STRENGTH_PUSH","ROTATING_TILE_SHIFT").contains(action.message())) {
                            var from = world.storyNpcPosition(action.actor());
                            if (Math.abs(from.x()-action.target().x())+Math.abs(from.y()-action.target().y())!=1)
                                throw new IllegalStateException("Non-adjacent forced object step");
                            npcPath = List.of(new int[]{action.target().x(),action.target().y()});
                        } else npcPath = route(world.storyNpcPosition(action.actor()), action.target());
                        npcRouteIndex = 0;
                    }
                    if (moveNpc()) done();
                }
                case "GUIDE", "MOVE_PLAYER" -> {
                    world.motion().storyWalking(true);
                    var target =
                            action.type().equals("GUIDE") ? action.playerTarget() : action.target();
                    if (!started) {
                        started = true;
                        path = route(target);
                        routeIndex = 0;
                        if (action.type().equals("GUIDE")) {
                            npcPath =
                                    route(world.storyNpcPosition(action.actor()), action.target());
                            npcRouteIndex = 0;
                        }
                    }
                    boolean npcReady = !action.type().equals("GUIDE") || moveNpc();
                    if (world.motion().moving()) return;
                    if (routeIndex < path.size()) {
                        int[] tile = path.get(routeIndex);
                        var p = world.movement().position();
                        if (p.x() == tile[0] && p.y() == tile[1]) {
                            routeIndex++;
                            if(routeIndex>=path.size()){if(npcReady)done();return;}
                            tile=path.get(routeIndex);
                        }
                        if (!world.motion()
                                .move(
                                        Integer.compare(tile[0], p.x()),
                                        Integer.compare(tile[1], p.y()))) return;
                    } else if (npcReady) {
                        done();
                    }
                }
                case "CHANGE_MAP" -> {
                    world.storyPosition(action.target());
                    lastPosition = null;
                    done();
                }
                case "NOTIFY" -> {notifications.accept(action.message());done();}
                case "START_BATTLE" -> {
                    if (!started) {
                        started = true;
                        action = null;
                        battle.accept(game.storyBattle(selection, entry.character().id()));
                    }
                }
                default -> throw new IllegalStateException("Unknown story action " + action.type());
            }
        } catch (Throwable failure) {
            world.motion().storyWalking(false);
            action = null;
            world.dialogueNpc(-1);
            world.storyNpcReaction(-1,false);
            dialogue.closePopup();
            error.accept(failure);
        }
    }

    private List<int[]> route(GameService.Position destination) {
        return route(world.movement().position(), destination);
    }

    private boolean moveNpc() {
        while(npcRouteIndex<npcPath.size()){
            int[] tile=npcPath.get(npcRouteIndex);
            if(!world.storyNpcStep(action.actor(),tile[0],tile[1]))break;
            npcRouteIndex++;
        }
        return npcRouteIndex >= npcPath.size();
    }

    private List<int[]> route(GameService.Position from, GameService.Position destination) {
        var map = world.movement().map();
        int width = map.width(),
                start = from.y() * width + from.x(),
                end = destination.y() * width + destination.x();
        var previous = new HashMap<Integer, Integer>();
        var queue = new ArrayDeque<Integer>();
        queue.add(start);
        previous.put(start, -1);
        int[] dx = {0, -1, 1, 0}, dy = {1, 0, 0, -1};
        while (!queue.isEmpty() && !previous.containsKey(end)) {
            int at = queue.remove();
            for (int i = 0; i < 4; i++) {
                int x = at % width + dx[i], y = at / width + dy[i];
                if (!map.contains(x, y)) continue;
                int next = y * width + x, block = map.block(x, y);
                // GUIDE paths play concurrently. The player's route cannot pass through
                // the guide's final standing tile, which will remain occupied on arrival.
                if(action.type().equals("GUIDE")&&destination.equals(action.playerTarget())
                        &&x==action.target().x()&&y==action.target().y())continue;
                if (previous.containsKey(next)
                        || !(world.storyCanStep(at % width, at / width, x, y)
                            || action.type().equals("MOVE_NPC") && next==end && map.warp(x,y)!=null)
                        || world.storyOccupied(x, y, action.actor())) continue;
                previous.put(next, at);
                queue.add(next);
            }
        }
        if (!previous.containsKey(end))
            throw new IllegalStateException("No reference-map story route: "+from+" -> "+destination+" ("+action.type()+")");
        var result = new ArrayList<int[]>();
        for (int at = end; at != start; at = previous.get(at))
            result.add(new int[] {at % width, at / width});
        Collections.reverse(result);
        return result;
    }

    public String previewText(int id,int species) {
        try{return text(id,species);}catch(Exception error){throw new IllegalStateException(error.getMessage(),error);}
    }

    private final java.util.Map<Integer, top.totodile.pokemmo.client.rom.nds.NdsArchive> gen4Text = new java.util.HashMap<>();
    private final java.util.Map<String, com.badlogic.gdx.utils.JsonValue> chapterTextResources = new java.util.HashMap<>();
    private com.badlogic.gdx.utils.JsonValue chapterTextResource(String path) throws java.io.IOException {
        if (!chapterTextResources.containsKey(path)) {
            try (var input = KantoStoryController.class.getResourceAsStream("/story/" + path + ".json")) {
                chapterTextResources.put(path, input == null ? null : new com.badlogic.gdx.utils.JsonReader().parse(new java.io.InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8)));
            }
        }
        return chapterTextResources.get(path);
    }
    private String gen4Text(int id, int species) throws Exception {
        int region = entry.region(), bank = (id >>> 12) & 0xFFFF, index = id & 4095;
        String chapter = region == 3 ? "sinnoh" : "johto", key = bank + ":" + index;
        var overrides = chapterTextResource(chapter + "/texts");
        String value = overrides != null && overrides.has(key) ? overrides.getString(key) : null;
        if (value == null) {
            var rom = resources.roms().get(region == 3 ? RomType.PLATINUM : RomType.HEART_GOLD_SOUL_SILVER);
            var archive = gen4Text.get(region);
            if (archive == null) {
                archive = new top.totodile.pokemmo.client.rom.nds.NdsArchive(RomReader.readFile(rom, region == 3 ? "msgdata/pl_msg.narc" : "a/0/2/7"));
                gen4Text.put(region, archive);
            }
            value = top.totodile.pokemmo.client.rom.nds.gen4.Gen4Text.decodeFormatted(archive.readRaw(bank), rom)[index];
        }
        var bindings = chapterTextResource(chapter + "/text-bindings");
        var slots = bindings == null ? null : bindings.get(key);
        if (slots != null) for (var slot = slots.child; slot != null; slot = slot.next) {
            String binding = slot.asString();
            String replacement = switch (binding) {
                case "PLAYER" -> entry.character().name();
                case "RIVAL" -> region == 3 ? "Barry" : "Silver";
                case "RIVAL_STARTER" -> switch(species) {case 387 -> "Chimchar";case 390 -> "Piplup";case 393 -> "Turtwig";case 152 -> "火球鼠";case 155 -> "小鋸鱷";case 158 -> "菊草葉";default -> "Pokémon";};
                case "STARTER" -> switch(species) {case 387 -> "Turtwig";case 390 -> "Chimchar";case 393 -> "Piplup";case 152 -> "菊草葉";case 155 -> "火球鼠";case 158 -> "小鋸鱷";default -> resources.strings().get(100000 + species);};
                default -> binding;
            };
            String slotId = slot.name.length() == 1 ? "0" + slot.name : slot.name;
            value = value.replace("{" + slotId + "}", replacement);
        }
        return value;
    }

    private String text(int id, int species) throws Exception {
        if (entry.region() == 3 && (id & 0x40000000) != 0 || entry.region() == 4 && (id & 0x10000000) != 0) return gen4Text(id, species);
        if ((id & 0x20000000) != 0) {
            if (unovaText == null) unovaText = new top.totodile.pokemmo.client.rom.nds.NdsArchive(RomReader.readFile(resources.roms().get(RomType.BLACK_WHITE), "a/0/0/3"));
            var lines = top.totodile.pokemmo.client.rom.nds.bw.BwText.decodeFormatted(unovaText.readRaw((id >>> 12) & 0xFFFF));
            String name = resources.strings().get(100000 + species);
            if (name.startsWith("STRING_")) name = species == 495 ? "Snivy" : species == 498 ? "Tepig" : species == 501 ? "Oshawott" : "Pokémon";
            if (((id >>> 12) & 0xFFFF) == 12 && (id & 4095) == 24) return lines[24].replace("{00}","TM83");
            if (((id >>> 12) & 0xFFFF) == 160 && (id & 4095) == 7) return lines[7].replace("{00}",entry.character().name()).replace("{01}",species==495?"Panpour":species==498?"Pansage":"Pansear");
            if (((id >>> 12) & 0xFFFF) == 160 && (id & 4095) <= 1) {
                String monkey = species == 495 ? "Panpour" : species == 498 ? "Pansage" : "Pansear";
                String type = species == 495 ? "Fire" : species == 498 ? "Water" : "Grass";
                return lines[id & 4095].replace("{00}",name).replace("{01}",monkey).replace("{02}",type);
            }
            return lines[id & 4095].replace("{00}", entry.character().name()).replace("{01}", name).replaceAll("\n{3,}", "\n\n");
        }
        String value = resources.strings().get(id, variable -> switch (variable) {
            case 0, 1 -> entry.character().name();
            case 6 -> entry.region()==1?(entry.character().appearance().gender()==0?"MAY":"BRENDAN"):"GREEN";
            case 2 -> species == 0 ? entry.character().name()
                    : species == 252 ? "TREECKO" : species == 255 ? "TORCHIC" : species == 258 ? "MUDKIP" : species == 1 ? "BULBASAUR" : species == 4 ? "CHARMANDER" : "SQUIRTLE";
            default -> null;
        });
        if (value.equals("STRING_" + id)) {
            var type = (id & 0x10000000) != 0 ? RomType.EMERALD : RomType.FIRE_RED;
            var rom = texts.get(type);
            if (rom == null) {
                var source = resources.roms().get(type);
                if (source == null) throw new IllegalStateException(type + " ROM required for NPC dialogue");
                rom = new top.totodile.pokemmo.client.rom.gba.GbaStoryText(resources, source);
                texts.put(type, rom);
            }
            value = rom.get(id & 0xFFFFFF);
        }
        String player = entry.character().name();
        return value.replace("{KEY_GAMEMENU}", keyName("gamemenu", 0))
                .replace("{KEY_B}", keyName("key_b", com.badlogic.gdx.Input.Keys.X))
                .replace("{01}", player)
                .replace("{06}", entry.region()==1?(entry.character().appearance().gender()==0?"MAY":"BRENDAN"):"GREEN")
                .replace("{05}", "")
                .replace("{00}", player)
                .replace(
                        "{02}",
                        species == 0
                                ? player
                                : species == 1
                                        ? "BULBASAUR"
                                        : species == 4 ? "CHARMANDER" : "SQUIRTLE");
    }

    /** Reference GameTextTemplateResolver.dG uses current bindings and string 61 for an unbound key. */
    private String keyName(String setting, int fallback) {
        int code;
        try {
            code = Integer.parseInt(resources.preferences().get("client.controls.gdx." + setting,
                    Integer.toString(fallback)));
        } catch (NumberFormatException invalid) { code = 0; }
        String name = code > 0 && code <= 255 ? com.badlogic.gdx.Input.Keys.toString(code) : null;
        return name == null ? resources.strings().get(61) : name;
    }

    @Override
    public void close() {
        dialogue.disposePreview();
        world.motion().storyWalking(false);
        closed = true;
        if(resources.audio()!=null)resources.audio().stop(this);
        world.dialogueNpc(-1);
        world.storyNpcReaction(-1,false);
        dialogue.closePopup();
        if(evolution!=null)evolution.close();
        if(hall!=null)hall.close();
    }
}
