package top.totodile.pokemmo.game;

import top.totodile.pokemmo.persistence.CharacterStore;
import top.totodile.pokemmo.protocol.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static top.totodile.pokemmo.protocol.CharacterPackets.*;

/** 会话持有权威位置；网络操作更新内存，定时提交与退出提交串行化。 */
final class WorldSession implements AutoCloseable {
    private WorldRules rules;
    private int region;
    private final int gender;
    private boolean wildEnabled = true;
    private EncounterCatalog.Selection activeEncounter;
    private ShowdownBattle battle;
    private TrainerCatalog.Trainer activeTrainer;
    private java.util.List<PartyMember> trainerParty;
    private final java.util.Set<Integer> defeatedTrainers;
    private java.util.List<PartyMember> savedBattleParty;
    private final WildEncounterState encounters = new WildEncounterState();

    private KantoNpcs npcs;
    private NdsNpcs ndsNpcs;
    private GbaScriptRuntime opening;
    private long savedScriptVersion=-1;
    private boolean scriptBattle;
    private GbaScriptRuntime createScriptRuntime(Position position)throws SQLException,IOException {
        savedScriptVersion=-1;
        return new GbaScriptRuntime(region,gender,store.gbaStoryState(account,character,region),position,(operation,first,second,argument)->{
            try{return store.gbaStoryEffect(account,character,region,operation,first,second,argument,opening.save(),opening.position(),opening.variable(0x4050),opening.variable(0x4055));}
            catch(SQLException e){throw new IOException("ROM script host operation failed: "+operation,e);}
        });
    }
    private void persistScript()throws SQLException,IOException {
        if(opening==null||opening.version()==savedScriptVersion)return;
        var p=opening.position();
        if(opening.active())position=rules.at(p.group(),p.map(),p.x(),p.y(),p.facing(),p.returnPoint());
        store.saveGbaStory(account,character,region,opening.save(),position,opening.variable(0x4050),opening.variable(0x4055));
        saved=position;savedScriptVersion=opening.version();
    }
    private final CharacterStore store;
    private final int account;
    private final long character;
    private final ScheduledFuture<?> saving;
    private Position position, saved, movementFrom;
    private final long onlineBase;
    private final long onlineStarted = System.nanoTime();
    private long onlineStopped;
    private long savedOnline;
    private boolean loading = true, cycling, savedCycling, noclip, closed;
    private int multiplier = 1;
    private long readyAt;
    private String playingStory;
    private long storyToken;
    private long loadDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
    private final Object saveLock = new Object();
    private volatile SQLException saveFailure;

    WorldSession(WorldRules rules, CharacterStore store, int account, CharacterInfo info,
                 ScheduledExecutorService scheduler) throws SQLException, IOException {
        this.rules = rules;
        region = info.appearance().region();gender=info.appearance().gender();
        if (rules instanceof BlackWhiteWorldRules ndsRules) ndsNpcs = new NdsNpcs(region, ndsRules);
        if (info.appearance().region() == 0 || info.appearance().region() == 1) npcs = new KantoNpcs((FireRedWorldRules) rules);
        this.store = store;
        this.account = account;
        character = info.id();
        defeatedTrainers=store.defeatedTrainers(account,character);
        onlineBase = savedOnline = info.stats().onlineSeconds();
        position = saved = info.position();
        if(region<=1&&npcs!=null){opening=createScriptRuntime(position);position=opening.position();persistScript();}
        cycling = savedCycling = info.cycling();
        noclip = debugEnabled() && info.noclip();
        multiplier = debugEnabled() ? info.speedMultiplier() : 1;
        wildEnabled = !debugEnabled() || info.wildEncounters();
        saving = scheduler.scheduleWithFixedDelay(() -> {
            try {
                flush();
            } catch (SQLException error) {
                saveFailure = error;
            }
        }, 200, 200, TimeUnit.MILLISECONDS);
    }

    byte[] handle(byte[] packet) throws IOException, SQLException {
        // 重置落点与定时位置存档共用锁序，防止旧快照覆盖刚提交的检查点。
        if (packet.length > 1 && ((packet[0] & 255) == StoryProgressPackets.OPCODE
                || (packet[0] & 255) == StoryPackets.OPCODE || (packet[0] & 255) == NpcPackets.OPCODE
                || (packet[0] & 255) == EncounterPackets.OPCODE)) {
            synchronized (saveLock) { synchronized (this) { return handleLocked(packet); } }
        }
        synchronized (this) { return handleLocked(packet); }
    }

    private byte[] handleLocked(byte[] packet) throws IOException, SQLException {
        checkHealthy();
        int opcode = packet[0] & 255;
        int duration = (cycling ? 80 : 275) / multiplier;
        long npcNow = System.nanoTime();
        if (opcode == StoryProgressPackets.OPCODE) {
            var request = StoryProgressPackets.readRequest(packet);
            if (region > 1 || !debugEnabled()) throw new IOException("Story debug disabled");
            if(request.action()!=StoryProgressPackets.QUERY&&StoryProgressPackets.scene(request.scene()).region()!=region)throw new IOException("Story scene region mismatch");
            var completed = store.storyCompleted(account, character);
            boolean accepted = true;
            Position destination = null;
            boolean nativeScript=false;
            switch (request.action()) {
                case StoryProgressPackets.QUERY -> { }
                case StoryProgressPackets.RESET -> {
                    if (loading || npcNow < readyAt || activeEncounter != null || playingStory != null
                            || opening != null && opening.active() && !request.scene().equals(StoryProgressPackets.OPENING)&&!request.scene().equals(StoryProgressPackets.HOENN_OPENING)) accepted = false;
                    else {
                        accepted=opening!=null&&opening.canReset(request.scene());
                        if (accepted) {
                            var target=opening.resetScene(request.scene(),npcNow);
                            position = saved = rules.at(target.group(),target.map(),target.x(),target.y(),target.facing(),null); destination = position; movementFrom = null;
                            if (region == 0) {
                                store.saveKantoCheckpoint(account, character, opening.save(), position,
                                        opening.variable(0x4050), opening.variable(0x4055));
                                savedScriptVersion = opening.version();
                            } else persistScript();
                            nativeScript=true;
                            if (npcs != null) npcs.talk(position, 255, npcNow);
                            encounters.reset(); loading = true;
                            loadDeadline = npcNow + TimeUnit.SECONDS.toNanos(30);
                        }
                    }
                }
                case StoryProgressPackets.BEGIN -> {
                    accepted = !loading && npcNow >= readyAt && activeEncounter == null && opening!=null&&opening.canStart(request.scene());
                    if (accepted) {
                        var target=opening.startScene(request.scene(),npcNow);position=saved=rules.at(target.group(),target.map(),target.x(),target.y(),target.facing(),null);
                        destination=position;nativeScript=true;persistScript();loading=true;loadDeadline=npcNow+TimeUnit.SECONDS.toNanos(30);
                    }
                }
                case StoryProgressPackets.COMPLETE, StoryProgressPackets.CANCEL -> {
                    accepted = request.scene().equals(playingStory) && request.token() == storyToken;
                    if (accepted) {
                        if (request.action() == StoryProgressPackets.COMPLETE) {
                            accepted = store.setStoryCompleted(account, character, playingStory, true);
                        }
                        playingStory = null;
                    }
                }
                default -> throw new IOException("Invalid story progress action");
            }
            var available=new java.util.HashSet<String>();if(opening!=null)for(String scene:StoryProgressPackets.scenes())if(opening.canStart(scene))available.add(scene);
            return StoryProgressPackets.response(new StoryProgressPackets.Result(accepted,storyToken,region==1?opening.completedScenes():store.storyCompleted(account,character),destination,nativeScript,java.util.Set.copyOf(available)));
        }
        if (playingStory != null && (opcode == WorldPackets.MOVE || opcode == WorldPackets.TELEPORT))
            return response(false, false, duration);
        if (npcs != null && !loading && (opening == null || !opening.active())) npcs.update(position, npcNow < readyAt ? movementFrom : null, npcNow,opening==null?java.util.Set.of():opening.actorViews().stream().map(GbaScriptRuntime.ActorView::id).collect(java.util.stream.Collectors.toSet()));
        if (ndsNpcs != null && !loading) ndsNpcs.update(position, npcNow < readyAt ? movementFrom : null, npcNow);
        if(opcode==TrainerPackets.OPCODE) {
            if(packet.length!=1)throw new IOException("Invalid trainer request");
            var snapshot=npcs==null?null:npcs.snapshot(position,npcNow);
            var trainer=region!=0||snapshot==null?null:TrainerCatalog.get(position.group(),position.map(),snapshot.talking());
            if(trainer==null)return TrainerPackets.response(new TrainerPackets.Ready(0,0,"",0,0,java.util.List.of()));
            if(defeatedTrainers.contains(trainer.id()))return TrainerPackets.response(new TrainerPackets.Ready(2,trainer.id(),trainer.name(),trainer.defeat(),trainer.after(),java.util.List.of()));
            if(activeEncounter!=null||loading||playingStory!=null||!hasAliveParty())return TrainerPackets.response(new TrainerPackets.Ready(3,trainer.id(),trainer.name(),0,0,java.util.List.of()));
            activeTrainer=trainer;var party=trainer.party();trainerParty=party;var first=party.getFirst();
            activeEncounter=new EncounterCatalog.Selection(first.species(),first.level(),0,false);
            return TrainerPackets.response(new TrainerPackets.Ready(1,trainer.id(),trainer.name(),trainer.defeat(),trainer.after(),party));
        }
        if (opcode == BattlePackets.OPCODE) {
            var request=BattlePackets.readRequest(packet);
            try {
                if(battle==null) {
                    if(request.action()!=BattlePackets.START||activeEncounter==null||activeEncounter.selections().size()!=1)
                        throw new IOException("No single encounter");
                    savedBattleParty=store.pcParty(account,character).party();
                    battle=activeTrainer==null&&!scriptBattle?new ShowdownBattle(savedBattleParty,activeEncounter):new ShowdownBattle(savedBattleParty,trainerParty,true);
                }
                var result=battle.handle(request);
                if(result.accepted()&&(!result.party().equals(savedBattleParty)||activeTrainer!=null&&result.outcome()==1)) {
                    store.saveBattleParty(account,character,savedBattleParty,result.party(),activeTrainer!=null&&result.outcome()==1?activeTrainer.id():0);savedBattleParty=result.party();
                    if(activeTrainer!=null&&result.outcome()==1)defeatedTrainers.add(activeTrainer.id());
                }
                return BattlePackets.response(result);
            } catch(IOException error) {
                System.getLogger(WorldSession.class.getName()).log(System.Logger.Level.ERROR,"Battle engine failure",error);
                if(battle!=null){battle.close();battle=null;}
                var party=savedBattleParty==null?store.pcParty(account,character).party():savedBattleParty;
                int active=0;for(int i=0;i<party.size();i++)if(party.get(i).hp()>0){active=i;break;}
                if(party.isEmpty())throw error;
                return BattlePackets.response(new BattlePackets.State(false,1,0,5,active,false,1,1,party,java.util.List.of(),java.util.List.of()));
            }
        }
        if (opcode == EncounterPackets.OPCODE) {
            PacketData request = new PacketData(packet); request.byteValue();
            int action = request.byteValue(); boolean enabled = request.booleanValue(); request.end();
            boolean accepted = true;
            if (action == EncounterPackets.SET) {
                if (!debugEnabled() && !enabled) accepted = false;
                else if (!store.setWildEncounters(account, character, enabled)) accepted = false;
                else { wildEnabled = enabled; encounters.reset(); }
            } else if (action == EncounterPackets.FINISH) {
                if(scriptBattle&&battle!=null){
                    int outcome=battle.state().outcome();
                    if(outcome!=1&&outcome!=2)return EncounterPackets.response(new EncounterPackets.Snapshot(wildEnabled,false,activeEncounter));
                    opening.battleFinished(outcome==1,npcNow);if(opening.mode()==GbaScriptRuntime.WARP){loading=true;loadDeadline=npcNow+TimeUnit.SECONDS.toNanos(30);}persistScript();scriptBattle=false;
                }
                if (battle != null) { battle.close(); battle = null; savedBattleParty = null; }
                activeTrainer=null; trainerParty=null; activeEncounter = null; encounters.reset();
            } else if (action == EncounterPackets.SCENT) {
                if (activeEncounter != null || loading || npcNow < readyAt || opening != null && opening.active()
                        || npcs != null && npcs.talking() || ndsNpcs != null && ndsNpcs.talking()) accepted = false;
                else {
                    var table = currentEncounterTable();
                    if (table == null || !hasAliveParty()) accepted = false;
                    else { activeEncounter = table.rollHorde(java.util.concurrent.ThreadLocalRandom.current()); encounters.reset(); }
                }
            } else if (action != EncounterPackets.QUERY) throw new IOException("Invalid encounter operation");
            return EncounterPackets.response(new EncounterPackets.Snapshot(wildEnabled, accepted, activeEncounter));
        }
        if (opcode == StoryPackets.OPCODE) {
            var request=StoryPackets.readRequest(packet);
            if(opening==null)return StoryPackets.response(new StoryPackets.Snapshot(0,0,0,0,null));
            if(request.action()==StoryPackets.RESET||request.action()==StoryPackets.START||request.action()==StoryPackets.SKIP){
                if(!debugEnabled())throw new IOException("Story debug disabled");
                if(request.action()==StoryPackets.SKIP)throw new IOException("ROM scripts cannot be skipped by changing a completion flag");
                if(request.action()==StoryPackets.RESET)position=opening.resetScene(StoryProgressPackets.OPENING,npcNow);
                else opening.startScene(StoryProgressPackets.OPENING,npcNow);
            }else if(request.action()!=StoryPackets.POLL)opening.acknowledge(request.phase(),request.action(),request.answer(),request.name(),npcNow);
            if(!loading&&npcNow>=readyAt&&activeEncounter==null)opening.onPosition(position,npcNow);
            var snapshot=opening.snapshot(npcNow);
            if(opening.mode()==GbaScriptRuntime.BATTLE&&activeEncounter==null){
                activeTrainer=opening.trainer()==0?null:TrainerCatalog.scriptTrainer(region,opening.trainer());
                if(opening.trainer()!=0&&activeTrainer==null)throw new IOException("Missing ROM trainer party");
                if(activeTrainer==null){int species=snapshot.buffers().get(514),level=snapshot.buffers().get(515);var d=PokemonIndividual.create(species,level,false,1,"Wild",region,position.group(),position.map(),0);int hp=d.stat(species,level,0);trainerParty=java.util.List.of(new PartyMember(species,false,false,hp,hp,level,d));}else trainerParty=activeTrainer.party();var first=trainerParty.getFirst();activeEncounter=new EncounterCatalog.Selection(first.species(),first.level(),0,false);scriptBattle=true;
            }
            if(snapshot.player()!=null){
                var p=snapshot.player();boolean warp=p.group()!=position.group()||p.map()!=position.map();
                position=rules.at(p.group(),p.map(),p.x(),p.y(),p.facing(),p.returnPoint());
                if(warp){loading=true;loadDeadline=npcNow+TimeUnit.SECONDS.toNanos(30);encounters.reset();}
            }
            if(!snapshot.active()&&npcs!=null&&npcs.talking())npcs.talk(position,255,npcNow);persistScript();return StoryPackets.response(snapshot);
        }
        if (opcode == NpcPackets.OPCODE) {
            if (packet.length != 3 || (packet[1] & 255) > 2) throw new IOException("Invalid NPC operation");
            if (ndsNpcs != null) {
                int action = packet[1] & 255;
                if (action == 2) ndsNpcs.end(npcNow);
                else if (action == 1 && !loading && !closed && activeEncounter == null && npcNow >= readyAt)
                    ndsNpcs.talk(position, packet[2] & 255, npcNow);
                return NpcPackets.response(ndsNpcs.snapshot(position, npcNow));
            }
            if (npcs == null) return NpcPackets.response(new NpcPackets.Snapshot(position.group(), position.map(), -1, java.util.List.of()));
            int action = packet[1] & 255;
            if(action==1&&opening!=null&&!loading&&npcNow>=readyAt){opening.syncNpcs(npcs.snapshot(position,npcNow),npcNow);if(opening.interact(position,packet[2]&255,npcNow)){npcs.talk(position,packet[2]&255,npcNow);return NpcPackets.response(new NpcPackets.Snapshot(position.group(),position.map(),-1,java.util.List.of()));}}
            if (action == 2) npcs.talk(position, 255, npcNow);
            else if (action == 1 && (opening == null || !opening.active()) && !loading && !closed && npcNow >= readyAt) npcs.talk(position, packet[2] & 255, npcNow);
            var snapshot=npcs.snapshot(position,npcNow);
            var trainer=region==0?TrainerCatalog.get(position.group(),position.map(),snapshot.talking()):null;
            if(trainer!=null&&defeatedTrainers.contains(trainer.id()))
                snapshot=new NpcPackets.Snapshot(snapshot.group(),snapshot.map(),snapshot.talking(),snapshot.states(),trainer.after()>0?trainer.after():trainer.defeat());
            return NpcPackets.response(snapshot);
        }
        if (opcode == WorldPackets.MOVE) {
            var request = WorldPackets.decodeMove(packet);
            duration = Math.max(1, (cycling ? 80 : request.running() ? 135 : 275) / multiplier);
            long now = System.nanoTime();
            if (loading || closed || activeEncounter != null || opening != null && opening.active() || npcs != null && npcs.talking() || ndsNpcs != null && ndsNpcs.talking() || now < readyAt || request.x() != position.x() || request.y() != position.y())
                return response(false, false, duration);
            var result = npcs == null ? rules.move(position, request.direction(), noclip)
                    : ((FireRedWorldRules) rules).move(position, request.direction(), noclip,
                    p -> npcs.occupiedExcept(p.group(),p.map(),p.x(),p.y(),now,opening==null?java.util.Set.of():opening.actorViews().stream().map(GbaScriptRuntime.ActorView::id).collect(java.util.stream.Collectors.toSet()))||opening!=null&&opening.occupied(p),opening==null?null:opening::collisionOverride);
            if (ndsNpcs != null && !noclip && result.moved() && ndsNpcs.occupied(result.position(), now))
                result = new FireRedWorldRules.Decision(new Position(position.group(), position.map(), position.x(), position.y(), position.elevation(), request.direction(), position.returnPoint()), false, false);
            movementFrom = position;
            position = result.position();
            if (opening != null && result.moved() && !result.warp()) opening.onPosition(position,now);
            if (result.moved()) {
                readyAt = now + TimeUnit.MILLISECONDS.toNanos(duration);
                loading = result.warp();
                if (loading) loadDeadline = now + TimeUnit.SECONDS.toNanos(30);
            }
            EncounterCatalog.Selection encounter = null;
            if (result.moved()) {
                if (result.warp() || movementFrom.group() != position.group() || movementFrom.map() != position.map()) encounters.reset();
                else if (wildEnabled && (opening == null || !opening.active())) {
                    encounter = encounters.step(currentEncounterTable(), rules.encounterBehavior(position), cycling,
                            java.util.concurrent.ThreadLocalRandom.current());
                    if (encounter != null && hasAliveParty()) activeEncounter = encounter;
                    else encounter = null;
                }
            }
            return WorldPackets.result(new WorldPackets.Result(position, result.moved(), result.warp(),
                    duration, result.approach(), encounter));
        }
        if (opcode == WorldPackets.LOADED) {
            if (packet.length != 1) throw new IOException("Invalid loaded acknowledgement");
            if (loading) {
                loading = false;
                readyAt = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(500);
                if (opening != null) opening.arrived(readyAt);
            }
            return response(false, false, duration);
        }
        if (opcode == WorldPackets.SETTINGS) {
            PacketData data = new PacketData(packet);
            data.byteValue();
            boolean bike = data.booleanValue();
            int speed = data.byteValue();
            boolean clip = data.booleanValue();
            data.end();
            if (speed != 1 && speed != 2 && speed != 4 && speed != 8)
                throw new IOException("Invalid movement settings");
            if (!debugEnabled() && (speed != 1 || clip)) throw new IOException("Debug movement disabled");
            if (!store.saveDebugSettings(account, character, clip, speed)) throw new SQLException("Debug settings save rejected");
            cycling = bike;
            multiplier = speed;
            noclip = clip;
            return response(false, false, duration);
        }
        if (opcode == WorldPackets.TELEPORT) {
            if (!debugEnabled()) throw new IOException("Debug teleport disabled");
            if (activeEncounter != null || opening != null && opening.active()) return response(false, false, duration);
            PacketData data = new PacketData(packet);
            data.byteValue();
            int group = data.byteValue(), map = data.byteValue(), x = data.shortValue(), y = data.shortValue();
            data.end();
            try {
                position = rules.at(group, map, x, y, 0, null);
            } catch (IllegalArgumentException error) {
                throw new IOException("Invalid teleport", error);
            }
            if (npcs != null) npcs.talk(position, 255, System.nanoTime());
            if (ndsNpcs != null) ndsNpcs.end(System.nanoTime());
            loading = true;
            loadDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            return response(true, true, duration);
        }
        throw new IOException("Unknown world operation");
    }

    CharacterInfo transferRegion(int region, int group, int map, int x, int y, WorldRules targetRules) throws IOException, SQLException {
        if (!debugEnabled()) throw new IOException("Debug teleport disabled");
        if (activeEncounter != null || opening != null && opening.active()) throw new IOException("World interaction in progress");
        Position target;
        try { target = targetRules.at(group, map, x, y, 0, null); }
        catch (IllegalArgumentException error) { throw new IOException("Invalid region teleport", error); }
        synchronized (saveLock) {
            synchronized (this) {
                checkHealthy();
                if (closed) throw new IOException("World is closed");
                long online = onlineSeconds();
                if (!store.transferRegion(account, character, region, target, online, cycling)) throw new SQLException("Region transfer rejected");
                rules = targetRules;
                this.region = region;
                encounters.reset();
                npcs = region == 0 || region == 1 ? new KantoNpcs((FireRedWorldRules) rules) : null;
                ndsNpcs = rules instanceof BlackWhiteWorldRules ndsRules ? new NdsNpcs(region, ndsRules) : null;
                opening = region <= 1 ? createScriptRuntime(target) : null;
                position = saved = target;
                savedOnline = online; savedCycling = cycling;
                loading = true; readyAt = 0;
                loadDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            }
            return store.list(account).stream().filter(info -> info.id() == character).findFirst()
                    .orElseThrow(() -> new SQLException("Transferred character missing"));
        }
    }

    private boolean hasAliveParty() throws SQLException {
        return store.pcParty(account, character).party().stream().anyMatch(member -> member.hp() > 0);
    }

    private EncounterCatalog.Table currentEncounterTable() {
        int season = (java.time.LocalDate.now(java.time.ZoneOffset.UTC).getMonthValue() - 1) % 4;
        int hour = (int) Math.floorMod(java.time.Instant.now().getEpochSecond() * 4 / 3600, 24);
        int time = hour >= 4 && hour < 10 ? 0 : hour >= 10 && hour < 20 ? 1 : 2;
        return EncounterCatalog.walking(region, position, rules.encounterBehavior(position), season, time);
    }

    private static boolean debugEnabled() {
        return Boolean.parseBoolean(System.getProperty("pokemmo.game.debug", "true"));
    }

    synchronized void checkHealthy() throws IOException, SQLException {
        if (saveFailure != null) throw saveFailure;
        if (loading && System.nanoTime() >= loadDeadline) throw new IOException("Map load acknowledgement timed out");
    }

    private byte[] response(boolean moved, boolean warp, int duration) {
        return WorldPackets.result(new WorldPackets.Result(position, moved, warp, Math.max(1, duration)));
    }

    private synchronized long onlineSeconds() {
        return onlineBase + TimeUnit.NANOSECONDS.toSeconds(Math.max(0, (closed ? onlineStopped : System.nanoTime()) - onlineStarted));
    }

    TrainerStats stats() throws SQLException {
        return store.stats(account, character, onlineSeconds());
    }

    void flush() throws SQLException {
        // 不在 JDBC 等待期间持有会话监视器，数据库阻塞不妨碍网络裁决。
        synchronized (saveLock) {
            Position snapshot;
            long online;
            boolean bike;
            synchronized (this) {
                snapshot = position;
                online = onlineSeconds();
                bike = cycling;
                if (snapshot.equals(saved) && online == savedOnline && bike == savedCycling) return;
            }
            if (!store.saveWorldState(account, character, snapshot, online, bike)) throw new SQLException("World save rejected");
            synchronized (this) {
                saved = snapshot;
                savedOnline = online;
                savedCycling = bike;
            }
        }
    }

    synchronized boolean battling() { return battle != null && battle.state().outcome() == 0; }

    @Override
    public void close() throws SQLException {
        synchronized (this) {
            if (!closed) {
                if (battle != null) { battle.close(); battle = null; }
                onlineStopped = System.nanoTime();
                closed = true;
            }
        }
        saving.cancel(false);
        flush();
    }
}
