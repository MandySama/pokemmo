package top.totodile.pokemmo.world;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import top.totodile.pokemmo.protocol.EncounterCatalog;
import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.protocol.CharacterPackets.Appearance;
import top.totodile.pokemmo.protocol.CharacterPackets.ReturnPoint;
import top.totodile.pokemmo.protocol.CharacterPackets.Position;
import top.totodile.pokemmo.protocol.WorldPackets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import top.totodile.pokemmo.rom.FireRedMap;
import top.totodile.pokemmo.rom.FireRedAtlas;
import top.totodile.pokemmo.rom.GbaMapPreview;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/** GBA 地区的格子步行、传送与装扮渲染；纹理均由绘制线程管理。 */
public final class KantoWorld implements ClientWorld {
    private record Sprite(int texture, int width, int height, float scale) { }
    private static final int[][] WALK = {
            {0, 3, 0, 4}, {1, 5, 1, 6}, {2, 7, 2, 8},
            {9, 10, 9, 11}, {12, 13, 12, 14}, {15, 16, 15, 17},
            {18, 21, 18, 22}, {19, 23, 19, 24}, {20, 25, 20, 26}
    };
    private static final long STEP_NANOS = 275_000_000L;
    private static final long ENTRY_FADE_NANOS = 500_000_000L;
    private record Scene(FireRedMap map, GbaMapPreview renderer, List<FireRedAtlas.Npc> npcs) { }
    private FireRedMap map;
    private GbaMapPreview renderer;
    private FireRedAtlas atlas;
    private FireRedAtlas.Key current = new FireRedAtlas.Key(4, 1);
    private final Map<FireRedAtlas.Key, Scene> scenes = new LinkedHashMap<>(16, 0.75f, true);
    private MapTestWorld layout;
    private ReturnPoint returnPoint;
    private List<FireRedAtlas.Npc> npcs = List.of();
    private final AvatarAssets assets;
    private EncounterCatalog.Selection wildEncounter;

    @Override
    public EncounterCatalog.Selection takeWildEncounter() {
        if (stepping || entering) return null;
        var result = wildEncounter; wildEncounter = null;
        return result;
    }

    @Override
    public int encounterBehavior() {
        try { return map.walkable(x, y) ? map.behavior(x, y) : -1; }
        catch (IOException error) { return -1; }
    }

    private Appearance appearance;
    private final AvatarAssets.Item[] items = new AvatarAssets.Item[12];
    private final Map<Integer, Sprite> sprites = new HashMap<>();
    private final boolean[] held = new boolean[4];
    private final long started = System.nanoTime();
    private int x = 6, y = 6, fromX = 6, fromY = 6, facing, lastPressed;
    private long stepStarted;
    private long stepNanos = STEP_NANOS;
    private boolean running;
    private boolean cycling;
    private final boolean[] modeKeys = new boolean[2];
    private long animationStarted;
    private boolean animating;
    private boolean stepping;
    private boolean noclip;

    private int speedMultiplier = 1;

    public String locationName() {
        if (atlas == null) return top.totodile.pokemmo.ClientStrings.get(9282);
        try { return top.totodile.pokemmo.ClientStrings.get(atlas.locationName(current)); }
        catch (IOException | IllegalArgumentException error) { return top.totodile.pokemmo.ClientStrings.get(9282); }
    }

    @Override
    public void replaceOutfit(Appearance next) {
        if (next.region() != appearance.region() || next.gender() != appearance.gender() || next.skin() != appearance.skin())
            throw new IllegalArgumentException("Outfit identity mismatch");
        appearance = next;
        java.util.Arrays.fill(items, null);
        for (int category = 1; category < items.length; category++) {
            for (var item : assets.choices(category, next.gender(), true)) {
                if (item.id == next.items()[category]) { items[category] = item; break; }
            }
        }
        sprites.entrySet().removeIf(entry -> {
            if (entry.getKey() >= 0) return false;
            glDeleteTextures(entry.getValue().texture()); return true;
        });
    }

    public int region() { return appearance.region(); }

    /** 地图目录；独立地图测试构造的世界没有，返回 null。 */
    public FireRedAtlas atlas() { return atlas; }

    public int speedMultiplier() { return speedMultiplier; }

    public void setSpeedMultiplier(int multiplier) {
        if (multiplier != 1 && multiplier != 2 && multiplier != 4 && multiplier != 8)
            throw new IllegalArgumentException("Invalid movement speed multiplier");
        speedMultiplier = multiplier;
        syncCycling();
    }

    public boolean noclip() { return noclip; }

    /** 会话内的碰撞开关；地图边界与传送判定始终保留。 */
    public void setNoclip(boolean enabled) { noclip = enabled; syncCycling(); }
    private long entryFadeStarted;
    private boolean entering;
    private record Transfer(FireRedAtlas.Key key, Scene scene, int x, int y, int facing) { }
    private Transfer transfer;
    private Transfer pendingTransfer;
    private Function<byte[], CompletableFuture<WorldPackets.Result>> authority;
    private CompletableFuture<WorldPackets.Result> worldReply;
    private boolean replyMoves;
    private byte[] sentSettings;
    private long nextRequestAt;
    private boolean disposed;
    private top.totodile.pokemmo.protocol.KantoNpcs localNpcs;
    private Function<byte[], CompletableFuture<top.totodile.pokemmo.protocol.NpcPackets.Snapshot>> npcAuthority;
    private CompletableFuture<top.totodile.pokemmo.protocol.NpcPackets.Snapshot> npcReply;
    private final Map<FireRedAtlas.Key, Map<Integer, top.totodile.pokemmo.protocol.NpcPackets.State>> npcStates = new HashMap<>();
    private final Map<FireRedAtlas.Key, Long> npcClocks = new HashMap<>();
    private long nextNpcPoll, npcRenderTime;
    private boolean conversing;

    private Function<byte[], CompletableFuture<top.totodile.pokemmo.protocol.StoryPackets.Snapshot>> storyAuthority;
    private CompletableFuture<top.totodile.pokemmo.protocol.StoryPackets.Snapshot> storyReply;
    private top.totodile.pokemmo.protocol.StoryPackets.Snapshot story;
    private top.totodile.pokemmo.protocol.GbaScriptRuntime localOpening;
    private final Map<Integer,FireRedAtlas.Npc> scriptActors=new HashMap<>();
    private final Map<Integer,top.totodile.pokemmo.protocol.NpcPackets.State> scriptActorStates=new HashMap<>();
    private final java.util.Set<Integer> scriptManaged=new java.util.HashSet<>();
    private int lastScriptDoor;
    private top.totodile.pokemmo.protocol.StoryPackets.Request queuedScriptAnswer;
    public top.totodile.pokemmo.protocol.StoryPackets.Snapshot scriptSnapshot(){return story;}
    public void scriptAnswer(int action,int answer,String name){
        if(story==null||storyAuthority==null)return;
        if(storyReply!=null){queuedScriptAnswer=new top.totodile.pokemmo.protocol.StoryPackets.Request(action,story.phase(),answer,name);return;}
        storyReply=storyAuthority.apply(top.totodile.pokemmo.protocol.StoryPackets.request(action,story.phase(),answer,name));
    }
    private FireRedAtlas.Npc storyActor;
    private long nextStoryPoll, storyClock;
    private int presentedStoryPhase;
    private int pendingStoryAction = -1;
    private boolean replayStory;

    public void setStoryAuthority(Function<byte[], CompletableFuture<top.totodile.pokemmo.protocol.StoryPackets.Snapshot>> callback) {
        storyAuthority = callback;
    }

    public boolean storyActive() { return story != null && story.active(); }

    /** 对话只在首次收到该阶段时交给 UI，轮询不重置分页。 */
    public int takeStoryText() {
        if (story == null || story.text() == 0 || presentedStoryPhase == story.phase()) return 0;
        presentedStoryPhase = story.phase();
        return story.text();
    }

    public void advanceStory() {
        if (storyActive()) storyCommand(top.totodile.pokemmo.protocol.StoryPackets.ADVANCE);
    }

    public void debugStory(int action) {
        if (region() != 0 || stepping || worldReply != null || transfer != null || entering) return;
        clearKeys();
        replayStory = action == top.totodile.pokemmo.protocol.StoryPackets.START;
        storyCommand(replayStory ? top.totodile.pokemmo.protocol.StoryPackets.RESET : action);
    }

    private void storyCommand(int action) {
        int phase = story == null ? 0 : story.phase();
        if (storyReply != null) { if (action != 0) pendingStoryAction = action; return; }
        if (storyAuthority != null) storyReply = storyAuthority.apply(top.totodile.pokemmo.protocol.StoryPackets.request(action, phase));
        else if (authority == null && localOpening != null) {
            try {
                if(action==2)localOpening.resetScene(top.totodile.pokemmo.protocol.StoryProgressPackets.OPENING,npcRenderTime);
                else if(action==4)localOpening.startScene(top.totodile.pokemmo.protocol.StoryProgressPackets.OPENING,npcRenderTime);
                else localOpening.acknowledge(phase,action,0,"",npcRenderTime);
                storyReply=CompletableFuture.completedFuture(localOpening.snapshot(npcRenderTime));
            }catch(IOException error){throw new IllegalStateException(error);}
        }
    }

    private void updateStory(long now) {
        if (region() > 1 || atlas == null) return;
        if (storyReply != null && storyReply.isDone()) {
            story = storyReply.join(); storyReply = null; storyClock = now;
            var actor = story.actor();
            storyActor = actor == null ? null : new FireRedAtlas.Npc(actor.id(), story.graphics(), actor.x(), actor.y(),
                    story.elevation(), 0, 0);
            scriptActors.clear();scriptActorStates.clear();scriptManaged.clear();
            for(var view:story.actors()){
                scriptManaged.add(view.id());scriptActorStates.put(view.id(),view.state());
                if(view.visible()){var state=view.state();scriptActors.put(view.id(),new FireRedAtlas.Npc(view.id(),view.graphics(),state.x(),state.y(),view.elevation(),0,0));}
            }
            if (story.phase() == 0) presentedStoryPhase = 0;
            if (storyActive()) clearKeys();
            if (story.active() && story.player() != null) applyStoryMovement(story, now);
            if(story.player()!=null){var source=scenes.get(new FireRedAtlas.Key(story.player().group(),story.player().map()));if(source!=null){var cells=new java.util.HashMap<Integer,Integer>();story.buffers().forEach((id,value)->{if(id>=20000)cells.put(id-20000,value);});source.renderer().setScriptCells(cells);}}
            if(story.doorState()!=lastScriptDoor&&story.doorX()>=0&&story.doorY()>=0&&story.doorX()<map.width()/16&&story.doorY()<map.height()/16){
                lastScriptDoor=story.doorState();if(lastScriptDoor!=0)startDoor(story.doorX(),story.doorY(),now,lastScriptDoor==2);
            }
            if (replayStory && story.phase() == 0 && pendingStoryAction < 0) {
                replayStory = false;
                try { teleport(3, 0, 12, 1, now); }
                catch (IOException error) { throw new IllegalStateException(error); }
            }
        }
        if (storyReply == null && pendingStoryAction >= 0) {
            int action = pendingStoryAction; pendingStoryAction = -1; storyCommand(action);
        }
        if(storyReply==null&&queuedScriptAnswer!=null){var answer=queuedScriptAnswer;queuedScriptAnswer=null;storyReply=storyAuthority.apply(top.totodile.pokemmo.protocol.StoryPackets.request(answer.action(),answer.phase(),answer.answer(),answer.name()));}
        if (storyReply != null || stepping || entering || transfer != null || pendingTransfer != null || worldReply != null) return;
        if (storyAuthority == null && authority == null) {
            try {
                if (localOpening == null) {
                    localOpening = new top.totodile.pokemmo.protocol.GbaScriptRuntime(region(),appearance.gender(),"",position(),(op,a,b,text)->{throw new IOException("Offline script host unavailable: "+op);});
                }
                if (story != null && story.warp() && !entering && transfer == null) localOpening.arrived(now);
                localOpening.onPosition(position(),now);
            }
            catch (IOException error) { throw new IllegalStateException(error); }
        }
        if (now >= nextStoryPoll) {
            storyCommand(top.totodile.pokemmo.protocol.StoryPackets.POLL);
            nextStoryPoll = now + 50_000_000L;
        }
    }

    private boolean replayMode;

    private void applyStoryMovement(top.totodile.pokemmo.protocol.StoryPackets.Snapshot snapshot, long now) {
        var p = snapshot.player();
        var key = new FireRedAtlas.Key(p.group(), p.map());
        try {
            if (!key.equals(current)) {
                if (transfer == null && pendingTransfer == null) {
                    transfer = new Transfer(key, scene(key), p.x(), p.y(), p.facing());
                    returnPoint = null; fadeOutStarted = now; fromX = x; fromY = y;
                    stepping = animating = false; clearKeys();
                }
            } else if (p.x() != x || p.y() != y) {
                fromX = snapshot.fromX(); fromY = snapshot.fromY(); x = p.x(); y = p.y(); facing = p.facing();
                stepStarted = animationStarted = now;
                stepNanos = java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(Math.max(1, snapshot.remaining()));
                stepping = animating = snapshot.remaining() > 0;
            }
        } catch (IOException error) { throw new IllegalStateException("Opening movement failed", error); }
    }

    private boolean storyActorOn(FireRedAtlas.Key key) {
        if (storyActor == null || story == null) return false;
        var p = story.player();
        return key.equals(p == null ? new FireRedAtlas.Key(3, 0) : new FireRedAtlas.Key(p.group(), p.map()));
    }
    private boolean scriptActorsOn(FireRedAtlas.Key key){return story!=null&&story.player()!=null&&key.group()==story.player().group()&&key.map()==story.player().map();}
    private java.util.List<FireRedAtlas.Npc> interactiveNpcs(){
        var result=new java.util.ArrayList<>(npcs);
        if(scriptActorsOn(current)){result.removeIf(n->scriptManaged.contains(n.id()));result.addAll(scriptActors.values());}
        return result;
    }
    private final Map<Integer, FireRedAtlas.Npc> replayActors = new HashMap<>();
    private final Map<Integer, top.totodile.pokemmo.protocol.NpcPackets.State> replayStates = new HashMap<>();
    private final Map<Integer, Long> replayClocks = new HashMap<>();

    public Appearance appearance() { return appearance; }
    public boolean canStartReplay() {
        return !disposed && !stepping && !entering && transfer == null && pendingTransfer == null && worldReply == null;
    }
    public void setReplayMode() { replayMode = true; clearKeys(); }
    public void clearReplayActors() { replayActors.clear(); replayStates.clear(); replayClocks.clear(); }
    public boolean replayMoving() { return stepping || entering; }

    public void replayPlayerStep(int tx, int ty, long now) {
        if (!replayMode || Math.abs(tx - x) + Math.abs(ty - y) != 1) throw new IllegalArgumentException("Invalid scripted player step");
        fromX = x; fromY = y;
        facing = tx < x ? 2 : tx > x ? 3 : ty < y ? 1 : 0;
        x = tx; y = ty;
        stepStarted = animationStarted = now; stepNanos = STEP_NANOS;
        stepping = animating = true;
    }

    public void replayActor(int id, int graphics, int elevation, int tx, int ty, int fx, int fy, int direction, long now) {
        if (!replayMode) throw new IllegalStateException("No replay world");
        replayActors.put(id, new FireRedAtlas.Npc(id, graphics, tx, ty, elevation, 0, 0));
        int remaining = tx == fx && ty == fy ? 0 : 275;
        replayStates.put(id, new top.totodile.pokemmo.protocol.NpcPackets.State(id, tx, ty, fx, fy, direction, remaining));
        replayClocks.put(id, now);
    }

    public void setNpcAuthority(Function<byte[], CompletableFuture<top.totodile.pokemmo.protocol.NpcPackets.Snapshot>> authority) {
        npcAuthority = authority;
    }

    private void applyNpcs(top.totodile.pokemmo.protocol.NpcPackets.Snapshot snapshot, long now) {
        if (disposed) return;
        var key = new FireRedAtlas.Key(snapshot.group(), snapshot.map());
        Map<Integer, top.totodile.pokemmo.protocol.NpcPackets.State> states = new HashMap<>();
        for (var state : snapshot.states()) states.put(state.id(), state);
        npcStates.put(key, states); npcClocks.put(key, now);
    }

    private void updateNpcs(long now) {
        npcRenderTime = now;
        if (appearance.region() > 1 || atlas == null) return;
        if (npcReply != null && npcReply.isDone()) {
            if (!npcReply.isCompletedExceptionally()) applyNpcs(npcReply.join(), now);
            npcReply = null;
        }
        if (npcAuthority != null) {
            if (npcReply == null && now >= nextNpcPoll && transfer == null && pendingTransfer == null) {
                npcReply = npcAuthority.apply(top.totodile.pokemmo.protocol.NpcPackets.request(0, 0));
                nextNpcPoll = now + 100_000_000L;
            }
        } else if (authority == null) {
            try {
                if (localNpcs == null) localNpcs = new top.totodile.pokemmo.protocol.KantoNpcs(new top.totodile.pokemmo.protocol.FireRedWorldRules(appearance.region()));
                if (localOpening != null) localOpening.resumeNpcs(localNpcs, now);
                localNpcs.update(position(), stepping ? new Position(current.group(), current.map(), fromX, fromY, position().elevation(), facing) : null, now); applyNpcs(localNpcs.snapshot(position(), now), now);
            } catch (IOException error) { throw new IllegalStateException(error); }
        }
    }

    public boolean canInteractPc() {
        if (disposed || stepping || entering || transfer != null || pendingTransfer != null || worldReply != null
                || conversing || facing != 1 || !top.totodile.pokemmo.rom.PokemonCenters.contains(region(), current.group(), current.map())) return false;
        int targetY = y - 1;
        if (x < 0 || x >= map.width() / 16 || targetY < 0 || targetY >= map.height() / 16) return false;
        try { return map.behavior(x, targetY) == 0x83; }
        catch (IOException error) { throw new IllegalStateException(error); }
    }

    public boolean hasNpcInFront() {
        int tx = x + (facing == 2 ? -1 : facing == 3 ? 1 : 0);
        int ty = y + (facing == 1 ? -1 : facing == 0 ? 1 : 0);
        for (var npc : interactiveNpcs()) {
            var state = npcState(npc, current);
            if ((state == null ? npc.x() : state.x()) == tx && (state == null ? npc.y() : state.y()) == ty) return true;
        }
        if(region()<=1){try{return atlas.storyEvents(current).backgrounds().stream().anyMatch(b->b.x()==tx&&b.y()==ty&&(region()==1||top.totodile.pokemmo.protocol.KantoStoryScenes.backgroundScript(b.script())));}catch(IOException e){throw new IllegalStateException(e);}}
        return false;
    }

    /** 面前一格交互；移动中及过图时不锁定目标。 */
    public CompletableFuture<List<Integer>> interactNpc() {
        if (appearance.region() > 1 || stepping || entering || transfer != null || pendingTransfer != null
                || worldReply != null || conversing) return CompletableFuture.completedFuture(List.of());
        int targetX = x + (facing == 2 ? -1 : facing == 3 ? 1 : 0);
        int targetY = y + (facing == 1 ? -1 : facing == 0 ? 1 : 0);
        FireRedAtlas.Npc target = null;
        for (var npc : interactiveNpcs()) {
            var state = npcState(npc, current);
            if ((state == null ? npc.x() : state.x()) == targetX && (state == null ? npc.y() : state.y()) == targetY) { target = npc; break; }
        }
        if(target==null&&region()<=1){try{for(var b:atlas.storyEvents(current).backgrounds())if(b.x()==targetX&&b.y()==targetY&&(region()==1||top.totodile.pokemmo.protocol.KantoStoryScenes.backgroundScript(b.script()))){target=new FireRedAtlas.Npc(255,0,b.x(),b.y(),b.elevation(),0,0,0,0,b.script());break;}}catch(IOException e){throw new IllegalStateException(e);}}
        if (target == null) return CompletableFuture.completedFuture(List.of());
        final FireRedAtlas.Npc selected = target;
        conversing = true; clearKeys();
        CompletableFuture<top.totodile.pokemmo.protocol.NpcPackets.Snapshot> response;
        if (npcAuthority != null) response = npcAuthority.apply(top.totodile.pokemmo.protocol.NpcPackets.request(1, target.id()));
        else if (localNpcs != null) {
            localNpcs.talk(position(), target.id(), npcRenderTime);
            response = CompletableFuture.completedFuture(localNpcs.snapshot(position(), npcRenderTime));
        } else { conversing = false; return CompletableFuture.completedFuture(List.of()); }
        return response.thenApply(snapshot -> {
            // 网络线程只返回文本 ID，场景快照仍由 GUI 线程轮询更新。
            if (snapshot.talking() != selected.id()) return List.<Integer>of();
            try {
                dialogueDetails = snapshot.dialogueText()>0
                        ? List.of(new top.totodile.pokemmo.rom.FireRedDialogue(snapshot.dialogueText(),Map.of()))
                        : atlas.npcDialogueDetails(selected, appearance.gender());
                return dialogueDetails.stream().map(top.totodile.pokemmo.rom.FireRedDialogue::textId).toList();
            }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    private volatile java.util.List<top.totodile.pokemmo.rom.FireRedDialogue> dialogueDetails = java.util.List.of();

    public java.util.Map<Integer, top.totodile.pokemmo.rom.FireRedDialogue.Buffer> dialogueBuffers(int textId) {
        return dialogueDetails.stream().filter(text -> text.textId() == textId).findFirst()
                .map(top.totodile.pokemmo.rom.FireRedDialogue::buffers).orElse(java.util.Map.of());
    }

    public void endNpcConversation() {
        conversing = false; clearKeys();
        if (npcAuthority != null) npcAuthority.apply(top.totodile.pokemmo.protocol.NpcPackets.request(2, 255));
        else if (localNpcs != null) localNpcs.talk(position(), 255, npcRenderTime);
    }

    private top.totodile.pokemmo.protocol.NpcPackets.State npcState(FireRedAtlas.Npc npc, FireRedAtlas.Key key) {
        if (replayMode && key.equals(current) && replayStates.containsKey(npc.id())) return replayStates.get(npc.id());
        if(scriptActorsOn(key)&&scriptActorStates.containsKey(npc.id()))return scriptActorStates.get(npc.id());
        if (storyActorOn(key) && npc.id() == storyActor.id()) return story.actor();
        return npcStates.getOrDefault(key, Map.of()).get(npc.id());
    }

    private long npcClock(FireRedAtlas.Npc npc, FireRedAtlas.Key key) {
        if (replayMode && key.equals(current) && replayClocks.containsKey(npc.id())) return replayClocks.get(npc.id());
        if(scriptActorsOn(key)&&scriptActorStates.containsKey(npc.id()))return storyClock;
        return storyActorOn(key) && npc.id() == storyActor.id() ? storyClock
                : npcClocks.getOrDefault(key, npcRenderTime);
    }

    private float npcCoordinate(FireRedAtlas.Npc npc, FireRedAtlas.Key key, boolean horizontal) {
        var state = npcState(npc, key);
        if (state == null) return horizontal ? npc.x() : npc.y();
        float progress = state.remaining() == 0 ? 1 : Math.min(1, Math.max(0,
                (275 - state.remaining() + (npcRenderTime - npcClock(npc, key)) / 1_000_000f) / 275f));
        return horizontal ? state.fromX() + (state.x() - state.fromX()) * progress
                : state.fromY() + (state.y() - state.fromY()) * progress;
    }

    private top.totodile.pokemmo.protocol.PartyMember follower;
    private CompletableFuture<BufferedImage[]> followerImages;
    private final FollowerTrail followerTrail = new FollowerTrail();
    private boolean resetFollower = true, followerFailure;
    private FireRedAtlas.Key followerMap;
    private long followerStep = Long.MIN_VALUE;
    private int lastFollowerPlayerX, lastFollowerPlayerY;

    public void setFollower(top.totodile.pokemmo.protocol.PartyMember member) {
        clearFollower();
        followerFailure = false;
        follower = member;
        if (member == null) return;
        followerImages = CompletableFuture.supplyAsync(() -> {
            try {
                var roms = new top.totodile.pokemmo.RomFiles(top.totodile.pokemmo.ClientFiles.data("../roms"));
                return top.totodile.pokemmo.rom.FollowerSprites.load(roms.selected(4), roms.selected(0), member.species());
            } catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    public boolean takeFollowerFailure() {
        boolean result = followerFailure;
        followerFailure = false;
        return result;
    }

    private void clearFollower() {
        if (followerImages != null) followerImages.cancel(true);
        followerImages = null;
        for (int i = 0; i < 8; i++) {
            Sprite old = sprites.remove(-10000 - i);
            if (old != null) glDeleteTextures(old.texture());
        }
        follower = null;
        resetFollower = true;
        followerStep = Long.MIN_VALUE;
    }

    private void updateFollower(long now) {
        if (followerImages != null && followerImages.isDone()) {
            var result = followerImages;
            followerImages = null;
            try {
                BufferedImage[] images = result.join();
                for (int i = 0; i < images.length; i++) {
                    int size = images[i].getWidth() / (follower.alpha() ? 3 : 4) * 3;
                    upload(-10000 - i, images[i], size / (float) images[i].getWidth());
                }
            } catch (java.util.concurrent.CompletionException error) {
                clearFollower();
                followerFailure = true;
                System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Unable to load follower", error.getCause());
            }
        }
        if (follower == null) return;
        if (resetFollower) {
            int bx = x - (facing == 2 ? -1 : facing == 3 ? 1 : 0);
            int by = y - (facing == 1 ? -1 : facing == 0 ? 1 : 0);
            if (!map.walkable(bx, by) || occupied(bx, by)) {
                bx = x; by = y;
                for (int direction : new int[]{0, 2, 3, 1}) {
                    int nx = x + (direction == 2 ? -1 : direction == 3 ? 1 : 0);
                    int ny = y + (direction == 1 ? -1 : direction == 0 ? 1 : 0);
                    if (map.walkable(nx, ny) && !occupied(nx, ny)) { bx = nx; by = ny; break; }
                }
            }
            int direction = bx < x ? 3 : bx > x ? 2 : by < y ? 0 : by > y ? 1 : facing;
            followerTrail.reset(bx, by, direction);
            followerMap = current;
            resetFollower = false;
        } else if (!current.equals(followerMap)) {
            followerTrail.translate(fromX - lastFollowerPlayerX, fromY - lastFollowerPlayerY);
            followerMap = current;
        }
        if (stepping && followerStep != stepStarted) {
            followerTrail.follow(fromX, fromY, stepStarted, stepNanos, now);
            followerStep = stepStarted;
        }
        lastFollowerPlayerX = x;
        lastFollowerPlayerY = y;
    }

    private void drawFollower(int left, int top, int scale, long now) {
        int frame = followerTrail.facing() * 2 + (followerTrail.moving(now)
                ? (int) ((now / (160_000_000L / speedMultiplier)) % 2) : 0);
        Sprite sprite = sprites.get(-10000 - frame);
        if (sprite == null) return;
        int px = (int) (followerTrail.x(now) * 16), py = (int) (followerTrail.y(now) * 16);
        Sprite shadow = sprites.get(64);
        draw(shadow, left + (px - 4.5f + shadow.width * 0.125f) * scale,
                top + (py - 5 + shadow.height * 0.125f) * scale,
                shadow.width * 0.75f * scale, shadow.height * 0.75f * scale, false);
        float width = sprite.width * sprite.scale, height = sprite.height * sprite.scale;
        draw(sprite, left + (px + 0.5f - (width - 16) / 2f) * scale,
                top + (py - (height - 16)) * scale, width * scale, height * scale, false);
    }

    private record Actor(int y, int layer, Runnable draw) { }

    public void setAuthority(Function<byte[], CompletableFuture<WorldPackets.Result>> authority) {
        this.authority = authority;
        acknowledgeLoaded();
    }

    private void acknowledgeLoaded() {
        if (authority != null) {
            replyMoves = false;
            worldReply = authority.apply(new byte[]{WorldPackets.LOADED});
        }
    }

    private void requestMove(int direction) {
        var packets = WorldPackets.settings(cycling, speedMultiplier, noclip);
        byte[] move = WorldPackets.move(x, y, direction, running);
        replyMoves = true;
        if (java.util.Arrays.equals(packets, sentSettings)) worldReply = authority.apply(move);
        else {
            sentSettings = packets;
            worldReply = authority.apply(packets).thenCompose(ignored -> authority.apply(move));
        }
    }

    private void applyReply(long now) {
        if (worldReply.isCompletedExceptionally()) {
            worldReply = null;
            clearKeys();
            return;
        }
        var reply = worldReply.join();
        if (reply.encounter() != null) { wildEncounter = reply.encounter(); clearKeys(); }
        worldReply = null;
        if (!replyMoves || disposed) return;
        if (!reply.moved()) nextRequestAt = now + 100_000_000L;
        var p = reply.position();
        facing = p.facing();
        if (reply.warp()) {
            try {
                var key = new FireRedAtlas.Key(p.group(), p.map());
                transfer = new Transfer(key, scene(key), p.x(), p.y(), p.facing());
                returnPoint = p.returnPoint();
                fadeOutStarted = now;
                fromX = x;
                fromY = y;
                stepping = animating = false;
                clearKeys();
                if (reply.approach() != null) {
                    pendingTransfer = transfer;
                    transfer = null;
                    var approach = reply.approach();
                    var approachKey = new FireRedAtlas.Key(approach.group(), approach.map());
                    Scene on = scene(approachKey);
                    current = approachKey;
                    map = on.map();
                    renderer = on.renderer();
                    npcs = on.npcs();
                    layout.select(current);
                    facing = approach.facing();
                    fromX = approach.x() - (facing == 2 ? -1 : facing == 3 ? 1 : 0);
                    fromY = approach.y() - (facing == 1 ? -1 : facing == 0 ? 1 : 0);
                    x = approach.x();
                    y = approach.y();
                    stepping = animating = true;
                    animationStarted = stepStarted = now;
                    stepNanos = java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(reply.millis());
                    if (facing == 1 && startDoor(x, y, now, false)) {
                        stepStarted += 120_000_000L;
                        animationStarted = stepStarted;
                    }
                }
            } catch (IOException error) { throw new IllegalStateException(error); }
        } else if (reply.moved()) {
            try {
                var key = new FireRedAtlas.Key(p.group(), p.map());
                Scene destination = scene(key);
                fromX = key.equals(current) ? x : p.x() - (facing == 2 ? -1 : facing == 3 ? 1 : 0);
                fromY = key.equals(current) ? y : p.y() - (facing == 1 ? -1 : facing == 0 ? 1 : 0);
                current = key;
                map = destination.map();
                renderer = destination.renderer();
                npcs = destination.npcs();
                layout.select(key);
                returnPoint = p.returnPoint();
                x = p.x();
                y = p.y();
                if (!animating || now - stepStarted > 320_000_000L) animationStarted = now;
                stepping = animating = true;
                stepStarted = now;
                stepNanos = java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(reply.millis());
            } catch (IOException error) { throw new IllegalStateException(error); }
        } else if (p.group() != current.group() || p.map() != current.map() || p.x() != x || p.y() != y) {
            try { restore(p); } catch (IOException error) { throw new IllegalStateException(error); }
        }
    }
    private long fadeOutStarted;
    private FireRedMap.DoorFrames door;
    private int doorX, doorY;
    private long doorStarted;
    private boolean doorClosing;
    private final java.util.List<Sprite> doorSprites = new java.util.ArrayList<>();

    private void clearDoor() {
        renderer.setDoorArea(-1, -1, 1, 1);
        for (Sprite sprite : doorSprites) glDeleteTextures(sprite.texture());
        doorSprites.clear();
        door = null;
    }

    private boolean startDoor(int dx, int dy, long now, boolean closing) {
        clearDoor();
        try {
            door = map.doorFrames(dx, dy, 1);
            if (door == null) return false;
            doorX = dx; doorY = dy; doorStarted = now; doorClosing = closing;
            for (var image : door.frames()) {
                upload(Integer.MIN_VALUE, image);
                doorSprites.add(sprites.remove(Integer.MIN_VALUE));
            }
            renderer.setDoorArea(dx, dy, (door.flags() & 512) != 0 ? 2 : 1,
                    (door.flags() & (256 | 512)) != 0 ? 2 : 1);
            return true;
        } catch (IOException error) { clearDoor(); throw new IllegalStateException(error); }
    }

    private void drawDoor(int layer, int left, int top, int scale, long now) {
        if (door == null || layer > 1) return;
        long ms = Math.max(0, now - doorStarted) / 1_000_000;
        int frame = doorClosing ? (ms < 380 ? 2 : ms < 440 ? 1 : 0)
                : (ms < 60 ? 0 : ms < 120 ? 1 : 2);
        int height = (door.flags() & (256 | 512)) != 0 ? 2 : 1;
        int width = (door.flags() & 512) != 0 ? 2 : 1;
        if (height == 1 && layer == 1) return;
        int row = layer == 0 ? height - 1 : 0;
        for (int column = 0; column < width; column++) {
            int index = frame * width * height + column * height + row;
            draw(doorSprites.get(index), left + (doorX + column) * 16 * scale,
                    top + (doorY - (height - 1 - row)) * 16 * scale, 16 * scale, 16 * scale, false);
        }
    }

    private java.util.function.Consumer<Position> onPosition = p -> { };
    private Position lastPublished;

    public void restore(Position p) throws IOException {
        var key = new FireRedAtlas.Key(p.group(), p.map());
        Scene scene = atlas == null ? scenes.get(key) : scene(key);
        if (!p.valid(appearance.region()) || scene == null || p.x() >= scene.map().width() / 16 || p.y() >= scene.map().height() / 16)
            throw new IOException("Invalid saved position");
        current = key; map = scene.map(); renderer = scene.renderer(); npcs = scene.npcs();
        if (layout != null) layout.select(key);
        returnPoint = p.returnPoint();
        x = fromX = p.x(); y = fromY = p.y(); facing = p.facing();
        resetFollower = true;
        lastPublished = position();
    }

    /** 服务端已提交剧情重置后采用权威落点，不再发送一次传送请求。 */
    public void restoreStoryPosition(Position position, boolean opening, long now) throws IOException {
        clearKeys(); stepping = animating = false;
        transfer = null; pendingTransfer = null; worldReply = null;
        restore(position);
        if (opening) {
            story = null; storyReply = null; storyActor = null; presentedStoryPhase = 0;
            pendingStoryAction = -1; replayStory = false;
            if (localOpening != null) localOpening.resetScene(top.totodile.pokemmo.protocol.StoryProgressPackets.OPENING,now);
            scriptActors.clear();scriptActorStates.clear();scriptManaged.clear();lastScriptDoor=0;
            queuedScriptAnswer=null;clearDoor();
        }
        beginEntry(now);
        acknowledgeLoaded();
    }

    public void setOnPosition(java.util.function.Consumer<Position> listener) {
        onPosition = listener;
    }

    /** 当前已走完或已落地的格子位置，供城镇地图等界面定位。 */
    public Position position() {
        return new Position(current.group(), current.map(), x, y, map.elevation(x, y) + 1, facing, returnPoint);
    }

    /** 只发布已走完的格子和传送落点，插值中的坐标不进入存档。 */
    private void publishPosition() {
        if (authority != null) return;
        var p = position();
        if (!p.equals(lastPublished)) { onPosition.accept(p); lastPublished = p; }
    }

    /** 地图纹理准备完成后开始淡入，过渡期间保持出生位置。 */
    public void beginEntry(long now) {
        clearKeys();
        entryFadeStarted = now;
        entering = true;
    }

    public KantoWorld(FireRedAtlas atlas, AvatarAssets assets, Appearance appearance) throws IOException {
        this(atlas.load(new FireRedAtlas.Key(Position.spawn(appearance.region(), appearance.gender()).group(),
                Position.spawn(appearance.region(), appearance.gender()).map()), true), assets, appearance);
        Position spawn = Position.spawn(appearance.region(), appearance.gender());
        current = new FireRedAtlas.Key(spawn.group(), spawn.map());
        x = fromX = spawn.x(); y = fromY = spawn.y();
        this.atlas = atlas;
        scenes.put(current, new Scene(map, renderer, npcs));
        try {
            layout = new MapTestWorld(atlas);
            layout.select(current);
        } catch (IOException | RuntimeException exception) {
            close();
            throw exception;
        }
    }

    private Scene scene(FireRedAtlas.Key key) throws IOException {
        Scene cached = scenes.get(key);
        if (cached != null) return cached;
        loadScene(key);
        return scenes.get(key);
    }

    private void loadScene(FireRedAtlas.Key key) throws IOException {
        var floor = atlas.load(key, true);
        var objects = atlas.npcs(key).stream().filter(npc -> npc.flag() == 0 && npc.hasStaticGraphics())
                .sorted(java.util.Comparator.comparingInt(FireRedAtlas.Npc::y)).toList();
        for (var npc : objects) {
            int spriteKey = npcSpriteKey(npc);
            if (!sprites.containsKey(spriteKey)) {
                var sprite = atlas.npcSprite(npc, true, 1);
                upload(spriteKey, sprite.image(), sprite.scale());
            }
        }
        scenes.put(key, new Scene(floor, new GbaMapPreview(floor), objects));
    }

    private static int npcSpriteKey(FireRedAtlas.Npc npc) {
        return 128 + npc.graphics() * 16 + Math.min(npc.direction(), 2) * 3;
    }

    public KantoWorld(FireRedMap map, AvatarAssets assets, Appearance appearance) throws IOException {
        this.map = map;
        this.assets = assets;
        this.appearance = appearance;
        int[] ids = appearance.items();
        for (int category = 1; category < items.length; category++) {
            for (var item : assets.choices(category, appearance.gender(), true)) {
                if (item.id == ids[category]) { items[category] = item; break; }
            }
        }
        if (items[11] == null) {
            var bicycles = assets.choices(11, appearance.gender());
            if (!bicycles.isEmpty()) items[11] = bicycles.getFirst();
        }
        renderer = new GbaMapPreview(map);
        try {
            upload(64, GameSprites.shadow());
            for (int direction = 0; direction < 4; direction++) {
                for (int frame : WALK[Math.min(direction, 2)]) sprite(direction, frame);
            }
        } catch (IOException | RuntimeException exception) {
            close();
            throw exception;
        }
    }

    public void key(int key, boolean pressed) {
        int modeKey = key == GLFW.GLFW_KEY_K ? 0 : key == GLFW.GLFW_KEY_2 ? 1 : -1;
        if (modeKey >= 0) {
            if (pressed && !modeKeys[modeKey] && transfer == null && !entering) {
                if (modeKey == 0) running = !running;
                else toggleBicycle();
            }
            modeKeys[modeKey] = pressed;
            return;
        }
        int direction = switch (key) {
            case GLFW.GLFW_KEY_S -> 0;
            case GLFW.GLFW_KEY_W -> 1;
            case GLFW.GLFW_KEY_A -> 2;
            case GLFW.GLFW_KEY_D -> 3;
            default -> -1;
        };
        if (direction >= 0) {
            held[direction] = pressed;
            if (pressed) lastPressed = direction;
        }
    }

    /** 准备目标场景后切换；清除旧室内返回点，避免跨区域出口回到旧地图。 */
    /** 骑上或下车，与按 2 键一致；快捷栏等外部入口使用同一判断。 */
    public void toggleBicycle() {
        if (transfer == null && !entering) {
            cycling = !cycling;
            syncCycling();
        }
    }

    @Override
    public void restoreCycling(boolean cycling) {
        this.cycling = cycling;
    }

    private void syncCycling() {
        if (authority != null) {
            sentSettings = WorldPackets.settings(cycling, speedMultiplier, noclip);
            authority.apply(sentSettings);
        }
    }

    public void teleport(int group, int id, int targetX, int targetY, long now) throws IOException {
        // 目录只会列出当前地区的地点；仍按地区校验，避免把异地区坐标发给服务端。
        if (!top.totodile.pokemmo.protocol.FireRedMaps.contains(appearance.region(), group, id, targetX, targetY))
            throw new IOException("Invalid teleport destination");
        if (authority != null) {
            if (worldReply != null || transfer != null || pendingTransfer != null || entering) return;
            clearKeys(); stepping = animating = false;
            replyMoves = true;
            worldReply = authority.apply(new top.totodile.pokemmo.protocol.PacketData(7)
                    .putByte(WorldPackets.TELEPORT).putByte(group).putByte(id)
                    .putShort(targetX).putShort(targetY).written());
            return;
        }
        if (atlas == null) throw new IOException("Invalid teleport destination");
        var key = new FireRedAtlas.Key(group, id);
        Scene destination = scene(key);
        if (targetX >= destination.map().width() / 16 || targetY >= destination.map().height() / 16)
            throw new IOException("Teleport destination outside map");
        clearKeys();
        stepping = false;
        animating = false;
        entering = false;
        returnPoint = null;
        transfer = new Transfer(key, destination, targetX, targetY, 0);
        fadeOutStarted = now;
        fromX = x;
        fromY = y;
    }

    public void clearKeys() { java.util.Arrays.fill(held, false); java.util.Arrays.fill(modeKeys, false); }

    /** 一次只走一个格子；长帧不积攒补走，松键后完成当前步。 */
    public void update(long now) {
        if (replayMode) {
            npcRenderTime = now;
            if (stepping && now - stepStarted >= stepNanos) stepping = animating = false;
            if (entering && now - entryFadeStarted >= ENTRY_FADE_NANOS) entering = false;
            return;
        }
        updateNpcs(now);
        updateStory(now);
        if (worldReply != null) {
            if (!worldReply.isDone()) return;
            applyReply(now);
        }
        if (transfer != null) {
            if (now - fadeOutStarted < ENTRY_FADE_NANOS) return;
            clearDoor();
            current = transfer.key();
            try { layout.select(current); } catch (IOException exception) { throw new IllegalStateException(exception); }
            map = transfer.scene().map();
            if (cycling && map.indoor()) {
                cycling = false;
                syncCycling();
            }
            renderer = transfer.scene().renderer();
            npcs = transfer.scene().npcs();
            x = fromX = transfer.x();
            y = fromY = transfer.y();
            facing = transfer.facing();
            resetFollower = true;
            transfer = null;
            publishPosition();
            beginEntry(now);
            if (facing == 0 && startDoor(x, y - 1, now, true)) {
                fromY = y - 1;
                stepping = animating = true;
                stepStarted = animationStarted = now;
                stepNanos = STEP_NANOS;
            }
            acknowledgeLoaded();
            return;
        }
        if (conversing) return;
        if (entering) {
            if (now - entryFadeStarted < ENTRY_FADE_NANOS) return;
            entering = false;
            clearDoor();
        }
        if (stepping && now - stepStarted >= stepNanos) {
            stepping = false;
            if (pendingTransfer != null) {
                transfer = pendingTransfer; pendingTransfer = null;
                fadeOutStarted = now; animating = false; clearKeys(); return;
            }
            if (authority == null && !storyActive() && warp(facing, now, true)) return;
            publishPosition();
        }
        if (stepping || wildEncounter != null || storyActive()) return;
        int direction = held[lastPressed] ? lastPressed : -1;
        if (direction < 0) for (int i = 0; i < held.length; i++) if (held[i]) { direction = i; break; }
        if (direction < 0) return;
        if (authority != null) { if (now >= nextRequestAt) requestMove(direction); return; }
        facing = direction;
        if (warp(direction, now, false)) return;
        int nextX = x + (direction == 2 ? -1 : direction == 3 ? 1 : 0);
        int nextY = y + (direction == 1 ? -1 : direction == 0 ? 1 : 0);
        if (nextX < 0 || nextY < 0 || nextX >= map.width() / 16 || nextY >= map.height() / 16) {
            if (crossConnection(nextX, nextY, direction, now)) return;
            publishPosition();
            return;
        }
        boolean jump;
        try { jump = !noclip && top.totodile.pokemmo.protocol.FireRedWorldRules.ledgeJump(map.behavior(nextX, nextY), direction); }
        catch (IOException error) { throw new IllegalStateException(error); }
        if (jump) {
            nextX += direction == 2 ? -1 : direction == 3 ? 1 : 0;
            nextY += direction == 1 ? -1 : direction == 0 ? 1 : 0;
            if (nextX < 0 || nextY < 0 || nextX >= map.width() / 16 || nextY >= map.height() / 16
                    || !map.walkable(nextX, nextY) || occupied(nextX, nextY)) { publishPosition(); return; }
        }
        if (!noclip && !jump && ((!map.canMove(x, y, nextX, nextY) && !canEnterDoor(nextX, nextY, direction))
                || occupied(nextX, nextY))) { publishPosition(); return; }
        fromX = x;
        fromY = y;
        x = nextX;
        y = nextY;
        if (!animating || now - stepStarted > 320_000_000L) animationStarted = now;
        animating = true;
        stepStarted = now;
        stepNanos = jump ? 550_000_000L / speedMultiplier : movementNanos();
        stepping = true;
        if (direction == 1 && canEnterDoor(x, y, direction) && startDoor(x, y, now, false)) {
            stepStarted += 120_000_000L;
            animationStarted = stepStarted;
        }
    }

    /** 在界面绘制后覆盖全屏，使地图和 HUD 共用同一过渡。 */
    public void renderEntryFade(int width, int height, long now) {
        if (!entering && transfer == null) return;
        float progress = Math.max(0, Math.min(1, (now - (transfer == null ? entryFadeStarted : fadeOutStarted)) / (float) ENTRY_FADE_NANOS));
        float alpha = transfer == null ? 1 - progress : progress;
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT | GL_CURRENT_BIT);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_SCISSOR_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(0, 0, 0, alpha);
        glBegin(GL_QUADS);
        glVertex2i(0, 0);
        glVertex2i(width, 0);
        glVertex2i(width, height);
        glVertex2i(0, height);
        glEnd();
        glPopAttrib();
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    private boolean occupied(int x, int y) {
        for (var npc : npcs) {
            var state = npcState(npc, current);
            if (state == null ? npc.x() == x && npc.y() == y
                    : state.x() == x && state.y() == y || state.remaining() > 0 && state.fromX() == x && state.fromY() == y) return true;
        }
        return false;
    }

    /** 门格自身带阻挡标记，由可用的向上入门事件接管通行。 */
    private boolean canEnterDoor(int x, int y, int direction) {
        if (atlas == null || direction != 1) return false;
        try {
            if (map.behavior(x, y) != 0x69) return false;
            return atlas.area(current).warps().stream()
                    .anyMatch(warp -> warp.x() == x && warp.y() == y && available(warp));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /** 楼梯和出口按方向触发，门按走入触发；完全淡出后才提交目标地图和落点。 */
    private boolean warp(int direction, long now, boolean entered) {
        if (atlas == null) return false;
        try {
            int stair = map.standingWarpDirection(x, y);
            if (entered ? stair >= 0 || map.behavior(x, y) == 0x69 && direction != 1 : stair != direction) return false;
            for (var warp : atlas.area(current).warps()) {
                if (warp.x() != x || warp.y() != y) continue;
                if (!available(warp)) continue;
                FireRedAtlas.Key destinationKey;
                int targetX, targetY, targetFacing = facing;
                if (dynamic(warp)) {
                    destinationKey = new FireRedAtlas.Key(returnPoint.group(), returnPoint.map());
                    targetX = returnPoint.x(); targetY = returnPoint.y(); targetFacing = returnPoint.facing();
                } else {
                    destinationKey = warp.target();
                    var arrival = atlas.area(destinationKey).warps().get(warp.targetWarp());
                    targetX = arrival.x(); targetY = arrival.y();
                }
                Scene destination = scene(destinationKey);
                if (!dynamic(warp) && atlas.area(destinationKey).warps().stream().anyMatch(KantoWorld::dynamic)) {
                    returnPoint = new ReturnPoint(current.group(), current.map(), x, y, facing);
                }
                // 室外门格落点向下移一格，人物朝下走出门口。
                if (destination.map().behavior(targetX, targetY) == 0x69
                        && destination.map().walkable(targetX, targetY + 1)) {
                    targetY++;
                    targetFacing = 0;
                }
                transfer = new Transfer(destinationKey, destination, targetX, targetY, targetFacing);
                fadeOutStarted = now;
                fromX = x;
                fromY = y;
                animating = false;
                clearKeys();
                return true;
            }
        } catch (IOException exception) {
            // 事件目标读取失败时终止当前地图操作。
            throw new IllegalStateException(exception);
        }
        return false;
    }

    private static boolean dynamic(FireRedAtlas.Warp warp) {
        return warp.target().group() == 127 || warp.target().map() == 127
                || warp.target().group() == 255 || warp.target().map() == 255 || warp.targetWarp() == 255;
    }

    private boolean available(FireRedAtlas.Warp warp) {
        try {
            if (dynamic(warp)) return returnPoint != null && returnPoint.valid(appearance.region());
            return warp.targetWarp() < atlas.area(warp.target()).warps().size();
        } catch (IOException exception) { return false; }
    }

    /** 连接偏移以目标地图格子为单位，过界时同步转换插值起点和镜头坐标。 */
    private boolean crossConnection(int nextX, int nextY, int direction, long now) {
        if (atlas == null) return false;
        int connectionDirection = switch (direction) { case 0 -> 1; case 1 -> 2; case 2 -> 3; default -> 4; };
        try {
            var from = atlas.area(current);
            for (var connection : from.connections()) {
                if (connection.direction() != connectionDirection) continue;
                var to = atlas.area(connection.target());
                int originX = switch (connectionDirection) { case 3 -> -to.columns(); case 4 -> from.columns(); default -> connection.offset(); };
                int originY = switch (connectionDirection) { case 1 -> from.rows(); case 2 -> -to.rows(); default -> connection.offset(); };
                int targetX = nextX - originX, targetY = nextY - originY;
                if (targetX < 0 || targetY < 0 || targetX >= to.columns() || targetY >= to.rows()) continue;
                Scene target = scene(to.key());
                if (!noclip && (!target.map().walkable(targetX, targetY)
                        || target.npcs().stream().anyMatch(n -> n.x() == targetX && n.y() == targetY))) return false;
                fromX = x - originX; fromY = y - originY; x = targetX; y = targetY;
                current = to.key(); map = target.map(); renderer = target.renderer(); npcs = target.npcs();
                layout.select(current);
                if (!animating || now - stepStarted > 320_000_000L) animationStarted = now;
                animating = stepping = true; stepStarted = now; stepNanos = movementNanos();
                return true;
            }
        } catch (IOException exception) { throw new IllegalStateException(exception); }
        return false;
    }

    public void render(int width, int height, long now) throws IOException {
        update(now);
        updateFollower(now);
        renderer.setBorderVisible(true);
        // 同帧创建后可能收到创建前采样的时间，首帧从动画零点开始。
        long elapsedMillis = Math.max(0L, now - started) / 1_000_000;
        double progress = stepping ? Math.max(0, Math.min(1, (now - stepStarted) / (double) stepNanos)) : 1;
        // 地图坐标先截断到 ROM 像素，再进入正交投影，人物和镜头使用相同位置。
        int px = (int) ((fromX + (x - fromX) * progress) * 16);
        int py = (int) ((fromY + (y - fromY) * progress) * 16);
        int jumpLift = stepping && Math.abs(x - fromX) + Math.abs(y - fromY) == 2
                ? (int) Math.round(Math.sin(progress * Math.PI) * 12) : 0;
        int scale = WorldCamera.scale(width);
        int focusX=px,focusY=py;
        var camera=scriptActorsOn(current)&&storyActive()?scriptActorStates.get(127):null;
        if(camera!=null){
            float cameraProgress=camera.remaining()==0?1:Math.min(1,Math.max(0,(275-camera.remaining()+(now-storyClock)/1_000_000f)/275f));
            focusX=(int)((camera.fromX()+(camera.x()-camera.fromX())*cameraProgress)*16);
            focusY=(int)((camera.fromY()+(camera.y()-camera.fromY())*cameraProgress)*16);
        }
        int left = WorldCamera.origin(width, focusX + 8, scale);
        int top = WorldCamera.origin(height, focusY + 12, scale);
        if (now - stepStarted > 320_000_000L) animating = false;
        int animation = animating ? (int) (Math.max(0, now - animationStarted) / ((running && !cycling ? 140_000_000L : 160_000_000L) / speedMultiplier) % 4) : 0;
        int mode = cycling ? 2 : running && animating ? 1 : 0;
        int frame = WALK[mode * 3 + Math.min(facing, 2)][animation];
        sprite(facing, frame);
        Sprite sprite = sprites.get(avatarSpriteKey(facing, frame));
        java.util.List<Actor> actors = new java.util.ArrayList<>();
        var visibleNpcs = new java.util.ArrayList<>(interactiveNpcs());
        if (replayMode) {
            visibleNpcs.removeIf(npc -> replayActors.containsKey(npc.id()));
            visibleNpcs.addAll(replayActors.values());
        }
        if (storyActorOn(current)) {
            visibleNpcs.removeIf(npc -> npc.id() == storyActor.id());
            visibleNpcs.add(storyActor);
        }
        for (var npc : visibleNpcs) actors.add(new Actor((int) (npcCoordinate(npc, current, false) * 16), npcLayer(npc), () -> drawNpc(npc, current, left, top, scale)));
        int playerLayer = map.elevation(x, y) >= 3 ? 2 : 1;
        if (follower != null) {
            int fx = (int) Math.round(followerTrail.x(now)), fy = (int) Math.round(followerTrail.y(now));
            int followerLayer = map.elevation(fx, fy) >= 3 ? 2 : 1;
            actors.add(new Actor((int) (followerTrail.y(now) * 16), followerLayer, () -> drawFollower(left, top, scale, now)));
        }
        actors.add(new Actor(py, playerLayer, () -> {
            Sprite shadow = sprites.get(64);
            draw(shadow, left + (px - 4.5f + shadow.width * 0.125f) * scale,
                    top + (py - 5 + shadow.height * 0.125f) * scale,
                    shadow.width * 0.75f * scale, shadow.height * 0.75f * scale, false);
            draw(sprite, left + (px - 13.5f) * scale, top + (py - 20 - jumpLift) * scale,
                    sprite.width * 0.75f * scale, sprite.height * 0.75f * scale, facing == 3);
        }));
        actors.sort(java.util.Comparator.comparingInt(Actor::y));
        renderer.renderWithActorRows(width, height, 1, 12, 0, elapsedMillis, left, top, scale, (layer,row) -> {
            glColor4f(1, 1, 1, 1);
            for (Actor actor : actors) if (actor.layer() == layer && (layer==2||Math.floorDiv(actor.y(),16)==row)) actor.draw().run();
        }, layer -> drawDoor(layer, left, top, scale, now));
        if (layout != null) {
            java.util.Set<FireRedAtlas.Key> visible = new java.util.HashSet<>();
            visible.add(current);
            for (var entry : layout.positions().entrySet()) {
                if (entry.getKey().equals(current)) continue;
                var area = atlas.area(entry.getKey());
                int neighborLeft = left + entry.getValue().x() * 16 * scale;
                int neighborTop = top + entry.getValue().y() * 16 * scale;
                if (neighborLeft >= width || neighborTop >= height || neighborLeft + area.columns() * 16 * scale <= 0
                        || neighborTop + area.rows() * 16 * scale <= 0) continue;
                visible.add(entry.getKey());
                Scene neighbor = scene(entry.getKey());
                neighbor.renderer().setBorderVisible(false);
                neighbor.renderer().renderWithActorRows(width, height, 1, 12, 0, elapsedMillis,
                        neighborLeft, neighborTop, scale, (layer,row) -> {
                            glColor4f(1, 1, 1, 1);
                            for (var npc : neighbor.npcs()) {
                                if (npcLayer(npc) == layer && (layer==2||(int)npcCoordinate(npc, entry.getKey(), false)==row)) drawNpc(npc, entry.getKey(), neighborLeft, neighborTop, scale);
                            }
                        }, null);
            }
            trimScenes(visible);
        }
    }

    private void trimScenes(java.util.Set<FireRedAtlas.Key> visible) {
        var iterator = scenes.entrySet().iterator();
        while (scenes.size() > 16 && iterator.hasNext()) {
            var entry = iterator.next();
            if (visible.contains(entry.getKey()) || transfer != null && entry.getValue() == transfer.scene()
                    || pendingTransfer != null && entry.getValue() == pendingTransfer.scene()) continue;
            entry.getValue().renderer().close();
            iterator.remove();
        }
        npcStates.keySet().retainAll(scenes.keySet());
        npcClocks.keySet().retainAll(scenes.keySet());
        java.util.Set<Integer> used = new java.util.HashSet<>();
        for (Scene scene : scenes.values()) for (var npc : scene.npcs()) for (int i = 0; i < 9; i++) used.add(128 + npc.graphics() * 16 + i);
        for (var npc : replayActors.values()) for (int i = 0; i < 9; i++) used.add(128 + npc.graphics() * 16 + i);
        for(var npc:scriptActors.values())for(int i=0;i<9;i++)used.add(128+npc.graphics()*16+i);
        if (storyActor != null) for (int i = 0; i < 9; i++) used.add(128 + storyActor.graphics() * 16 + i);
        sprites.entrySet().removeIf(entry -> {
            if (entry.getKey() < 128 || used.contains(entry.getKey())) return false;
            glDeleteTextures(entry.getValue().texture()); return true;
        });
    }

    private static int npcLayer(FireRedAtlas.Npc npc) {
        return npc.elevation() < 3 ? 1 : 2;
    }

    private void drawNpc(FireRedAtlas.Npc npc, FireRedAtlas.Key key, int left, int top, int scale) {
        var state = npcState(npc, key);
        int facing = state == null ? npc.direction() : state.facing();
        boolean walking = state != null && state.remaining() > 0
                && npcRenderTime - npcClock(npc, key) < state.remaining() * 1_000_000L;
        int walkFrame = walking ? (int)((npcRenderTime / 140_000_000L) % 4) : 0;
        walkFrame = walkFrame == 1 ? 1 : walkFrame == 3 ? 2 : 0;
        int spriteKey = 128 + npc.graphics() * 16 + Math.min(facing, 2) * 3 + walkFrame;
        Sprite sprite = sprites.get(spriteKey);
        if (sprite == null) {
            try { var image = atlas.npcSprite(npc, true, 1, facing, walkFrame); upload(spriteKey, image.image(), image.scale()); }
            catch (IOException error) { throw new IllegalStateException(error); }
            sprite = sprites.get(spriteKey);
        }
        int px = (int)(npcCoordinate(npc, key, true) * 16), py = (int)(npcCoordinate(npc, key, false) * 16);
        Sprite shadow = sprites.get(64);
        draw(shadow, left + (px - 4.5f + shadow.width * 0.125f) * scale,
                top + (py - 5 + shadow.height * 0.125f) * scale,
                shadow.width * 0.75f * scale, shadow.height * 0.75f * scale, false);
        float width = sprite.width * sprite.scale, height = sprite.height * sprite.scale;
        draw(sprite, left + (px + (sprite.scale == 0.75f ? 0.5f : 0) - (width - 16) / 2f) * scale,
                top + (py - (height - 16)) * scale,
                width * scale, height * scale, facing == 3);
    }

    private long movementNanos() {
        return (cycling ? 80_000_000L : running ? 135_000_000L : STEP_NANOS) / speedMultiplier;
    }

    private static int avatarSpriteKey(int direction, int frame) {
        return -1 - direction * 32 - frame;
    }

    private void sprite(int direction, int frame) throws IOException {
        int key = avatarSpriteKey(direction, frame);
        if (sprites.containsKey(key)) return;
        upload(key, assets.renderOverworld(appearance.skin(), items, appearance.colors(), frame, direction));
    }

    private void upload(int key, BufferedImage image) {
        upload(key, image, 1);
    }

    private void upload(int key, BufferedImage image, float scale) {
        ByteBuffer pixels = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
            int argb = image.getRGB(x, y);
            pixels.put((byte) (argb >>> 16)).put((byte) (argb >>> 8)).put((byte) argb).put((byte) (argb >>> 24));
        }
        pixels.flip();
        int texture = glGenTextures();
        sprites.put(key, new Sprite(texture, image.getWidth(), image.getHeight(), scale));
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private static void draw(Sprite sprite, float x, float y, float width, float height, boolean flip) {
        glBindTexture(GL_TEXTURE_2D, sprite.texture);
        float u0 = flip ? 1 : 0, u1 = 1 - u0;
        glBegin(GL_QUADS);
        glTexCoord2f(u0, 0); glVertex2f(x, y);
        glTexCoord2f(u1, 0); glVertex2f(x + width, y);
        glTexCoord2f(u1, 1); glVertex2f(x + width, y + height);
        glTexCoord2f(u0, 1); glVertex2f(x, y + height);
        glEnd();
    }

    @Override public void close() {
        disposed = true;
        clearReplayActors();
        storyAuthority = null; storyReply = null; story = null; storyActor = null; localOpening = null;
        clearFollower();
        clearDoor();
        clearKeys();
        if (scenes.isEmpty()) renderer.close();
        else for (Scene scene : scenes.values()) scene.renderer().close();
        scenes.clear();
        for (Sprite sprite : sprites.values()) glDeleteTextures(sprite.texture);
        sprites.clear();
        npcStates.clear(); npcClocks.clear(); npcReply = null; npcAuthority = null; localNpcs = null;
    }
}
