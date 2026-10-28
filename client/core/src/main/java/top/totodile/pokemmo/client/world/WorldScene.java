package top.totodile.pokemmo.client.world;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;

import top.totodile.pokemmo.client.config.ClientSettings;
import top.totodile.pokemmo.client.rom.gba.FireRedMaps.*;
import top.totodile.pokemmo.client.ui.character.OverworldCharacter;
import top.totodile.pokemmo.server.game.GameService.Position;
import top.totodile.pokemmo.server.game.GameService.WorldInfo;

import java.util.*;
import java.util.function.Consumer;

/** Shared world input and motion, with GBA metatiles or the ROM's NDS field geometry. */
public final class WorldScene extends InputAdapter implements AutoCloseable {
    public int encounterBehavior() {
        var map=movement.map(); var p=movement.position();
        if(maps instanceof top.totodile.pokemmo.client.rom.nds.bw.BwMaps bw)return bw.nativeBehavior(map.id(),p.x(),p.y());
        return maps.tiles(map).behavior(map.block(p.x(),p.y()) & 1023);
    }
    public boolean encounterLand() {
        int behavior=encounterBehavior();
        if(maps instanceof top.totodile.pokemmo.client.rom.nds.bw.BwMaps)return behavior==4||behavior==5;
        return behavior==2 || maps instanceof top.totodile.pokemmo.client.rom.nds.gen4.Gen4Maps && behavior==3 || maps.battleTerrain(movement.map().id(),movement.position().x(),
                movement.position().y())==BattleTerrain.CAVE && !maps.isWater(behavior);
    }
    public boolean supportsWildEncounters() {
        return maps instanceof top.totodile.pokemmo.client.rom.nds.gen4.Gen4Maps || maps instanceof top.totodile.pokemmo.client.rom.nds.bw.BwMaps || maps instanceof top.totodile.pokemmo.client.rom.gba.FireRedMaps gba
                && (gba.rom().type()==top.totodile.pokemmo.client.rom.RomType.FIRE_RED
                    || gba.rom().type()==top.totodile.pokemmo.client.rom.RomType.EMERALD);
    }

    public void surfSprites(top.totodile.pokemmo.client.rom.RomInfo rom) {
        character.surfSprites(rom);
    }

    public void appearance(top.totodile.pokemmo.server.game.GameService.Appearance saved) {
        character.appearance(saved);
    }

    private final WorldAtmosphere atmosphere = new WorldAtmosphere();

    public WorldAtmosphere atmosphere() {
        return atmosphere;
    }

    public boolean supportsAtmosphere() {
        return !mobile && maps.supportsAtmosphere();
    }

    private final WorldMaps maps;
    private final NdsWorldRenderer nds;
    private final MapMovement movement;
    private final OverworldCharacter character;
    private final ClientSettings settings;
    private final boolean mobile;
    private final PlayerMotion motion;
    private final WorldPokedex pokedex;
    private WorldInfo worldInfo = WorldInfo.defaults();
    private final SpriteBatch batch = new SpriteBatch();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final LinkedHashMap<Tiles, Texture[]> atlases = new LinkedHashMap<>(8, .75f, true);
    private final Map<Tiles, int[]> animationFrames = new HashMap<>();
    private double animationTime;
    private boolean focused = true;
    private boolean inputBlocked;
    private WorldTransition transition;
    private java.util.concurrent.CompletableFuture<Void> warpCover;
    private boolean warpRevealing;
    private int warpDx, warpDy;
    private final WorldDoorAnimation doorAnimation = new WorldDoorAnimation();

    public java.util.concurrent.CompletableFuture<Void> rest(Runnable atBlack) {
        if (transition == null) {
            atBlack.run();
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }
        return transition.rest(atBlack);
    }

    public void transition(WorldTransition value) {
        transition = Objects.requireNonNull(value);
        movement.beforeWarp(
                (dx, dy) -> {
                    if (warpCover == null) {
                        if (maps instanceof top.totodile.pokemmo.client.rom.gba.FireRedMaps gba) {
                            var p=movement.position();
                            if (!doorAnimation.active() && dy<0)
                                doorAnimation.begin(gba,movement.map(),p.x()+dx,p.y()+dy,false);
                            if (!doorAnimation.ready()) { warpDx=dx; warpDy=dy; return false; }
                        }
                        warpDx = dx;
                        warpDy = dy;
                        warpCover = transition.cover();
                        return false;
                    }
                    return warpCover.isDone() && !warpCover.isCompletedExceptionally();
                });
    }

    private final Set<Integer> actionKeys = new HashSet<>();
    private final DirectionKeys directionKeys = new DirectionKeys();
    private MobileMapControls controls;
    private FollowerPokemon follower;
    private WorldMapObjects mapObjects;
    private HoennGateSprites gateSprites;
    private java.util.List<top.totodile.pokemmo.server.game.MapEntities.MapView> mapEntitySnapshot = java.util.List.of();
    private final Map<Id,Map<Integer,Integer>> storyTileOriginals=new HashMap<>();
    private void restoreStoryTiles(){
        for(var entry:storyTileOriginals.entrySet()){
            var map=maps.map(entry.getKey());
            for(var tile:entry.getValue().entrySet())map.blocks()[tile.getKey()]=tile.getValue();
        }
    }
    public void mapEntities(java.util.List<top.totodile.pokemmo.server.game.MapEntities.MapView> entities){
        if(mapEntitySnapshot==entities)return;
        restoreStoryTiles();
        for(var view:entities){
            if(view.tiles().isEmpty())continue;
            var map=maps.map(new Id(view.bank(),view.map()));
            var originals=storyTileOriginals.computeIfAbsent(map.id(),ignored->new HashMap<>());
            for(var tile:view.tiles()){
                if(!map.contains(tile.x(),tile.y()))throw new IllegalArgumentException("Story tile outside map");
                int at=tile.y()*map.width()+tile.x();originals.putIfAbsent(at,map.blocks()[at]);map.blocks()[at]=tile.block();
            }
        }
        mapEntitySnapshot = entities;
        if(mapObjects!=null)mapObjects.entities(entities);
    }

    public int facingBackground() {
        var p = movement.position();
        int elevation = movement.map().block(p.x(), p.y()) >>> 12;
        return mapEntitySnapshot.stream().filter(m -> m.bank() == p.bank() && m.map() == p.map())
                .flatMap(m -> m.backgrounds().stream()).filter(b -> b.facing(p, elevation))
                .mapToInt(top.totodile.pokemmo.server.game.MapEntities.Background::id).findFirst().orElse(-1);
    }
    public void dialogueNpc(int actor) {
        if (mapObjects == null) return;
        var p = movement.position();
        mapObjects.dialogue(movement.map().id(), actor, p.x(), p.y());
    }
    public void storyNpcFace(int actor,int direction){if(mapObjects!=null)mapObjects.face(movement.map().id(),actor,direction);}
    public void storyNpcReaction(int actor,boolean visible){if(mapObjects!=null)mapObjects.reaction(movement.map().id(),actor,visible);}

    public float[] dialogueAnchor(int actor, int width, int height) {
        if (mapObjects == null || actor < 0) return null;
        return mapObjects.actors(movement.map().id()).stream()
                .filter(n -> n.definition.id() == actor && n.visible()).findFirst().map(n -> {
                    var screen = camera.project(new com.badlogic.gdx.math.Vector3(
                            n.drawX() * 16 + 8, -n.drawY() * 16 + 16, 0), 0, 0, width, height);
                    // MessageBoxManager adds (36,24), then MessageBoxBubble subtracts 32 from X.
                    return new float[] {screen.x + 4, height - screen.y + 24};
                }).orElse(null);
    }

    public int facingStrengthBoulder(){
        int actor=facingNpc();if(actor<0||mapObjects==null)return -1;
        int graphics=maps instanceof top.totodile.pokemmo.client.rom.gba.FireRedMaps gba&&gba.rom().type()==top.totodile.pokemmo.client.rom.RomType.EMERALD?87:97;
        return mapObjects.actors(movement.map().id()).stream().anyMatch(n->n.definition.id()==actor&&n.definition.graphics()==graphics)?actor:-1;
    }
    public int facingNpc(){
        if(mapObjects==null)return -1;
        var p=movement.position();int[] dx={0,0,-1,1},dy={1,-1,0,0};
        var map = movement.map();
        int tx = p.x() + dx[p.direction()], ty = p.y() + dy[p.direction()];
        int npc = npcAt(tx, ty);
        if (npc >= 0) return npc;
        // GlfwDebugConsole skips the BW counter behavior WK (native id 212) for interaction.
        if (map.contains(tx, ty) && (maps.tiles(map).behavior(map.block(tx, ty) & 1023) == 0x80
                || maps instanceof top.totodile.pokemmo.client.rom.nds.bw.BwMaps bw && bw.nativeBehavior(map.id(),tx,ty)==212))
            return npcAt(tx + dx[p.direction()], ty + dy[p.direction()]);
        return -1;
    }
    private int npcAt(int x, int y) {
        int layer = WorldMapObjects.entityElevation(movement.elevation() << 12);
        return mapObjects.actors(movement.map().id()).stream()
                // Native invisible objects can be interaction proxies (e.g. Mr. Stone's desk).
                .filter(a -> (maps instanceof top.totodile.pokemmo.client.rom.nds.gen4.Gen4Maps || a.definition.graphics() != 0) && a.x == x && a.y == y
                        && (layer < 0 || a.definition.elevation() < 0
                            || layer / 3 == a.definition.elevation() / 3))
                .mapToInt(a -> a.definition.id()).findFirst().orElse(-1);
    }
    public boolean storyOccupied(int x,int y,int except){return mapObjects!=null&&mapObjects.actors(movement.map().id()).stream().anyMatch(a->a.definition.id()!=except&&a.occupies(x,y,0));}
    public boolean storyCanStep(int fromX,int fromY,int x,int y){
        var map=movement.map();if(!map.contains(x,y)||map.warp(x,y)!=null)return false;
        var tiles=maps.tiles(map);int block=map.block(x,y),to=tiles.behavior(block&1023),from=tiles.behavior(map.block(fromX,fromY)&1023);
        return (block&0xc00)==0&&block>>>12!=15&&!maps.isWater(to)&&!maps.blocked(from,x-fromX,y-fromY)&&!maps.blocked(to,fromX-x,fromY-y);
    }
    public Position storyNpcPosition(int actor){
        return mapObjects.actors(movement.map().id()).stream().filter(a->a.definition.id()==actor).findFirst()
                .map(a->new Position(movement.map().id().bank(),movement.map().id().map(),a.x,a.y,a.direction)).orElseThrow();
    }
    public void storyPosition(Position position){movement.storyPosition(position);motion.resetStep();}
    public void storyNpcPosition(int actor,int x,int y){
        if(mapObjects!=null)mapObjects.actors(movement.map().id()).stream().filter(a->a.definition.id()==actor).findFirst().ifPresent(a->a.storyPosition(x,y));
    }
    public boolean storyNpcStep(int actor,int x,int y){
        if(mapObjects==null)return true;
        return mapObjects.actors(movement.map().id()).stream().filter(a->a.definition.id()==actor).findFirst().map(a->a.storyStep(x,y)).orElse(true);
    }

    public void mapObjects(top.totodile.pokemmo.client.resources.ClientResources resources) {
        soundResources=resources;
        var rom=maps instanceof top.totodile.pokemmo.client.rom.gba.FireRedMaps gba ? gba.rom()
            : maps instanceof top.totodile.pokemmo.client.rom.nds.bw.BwMaps bw ? bw.rom() : maps instanceof top.totodile.pokemmo.client.rom.nds.gen4.Gen4Maps gen4 ? gen4.rom() : null;
        if(rom==null)return;
        try {
            var replacement = new WorldMapObjects(resources, rom);
            if (mapObjects != null) mapObjects.close();
            mapObjects = replacement;
            if(rom.type()==top.totodile.pokemmo.client.rom.RomType.EMERALD&&gateSprites==null)gateSprites=new HoennGateSprites(rom);
            movement.occupancy(mapObjects::occupied);
        } catch (java.io.IOException error) {
            throw new IllegalStateException("Cannot load reference Kanto map objects", error);
        }
    }

    public void follower(
            top.totodile.pokemmo.client.resources.ClientResources resources,
            top.totodile.pokemmo.server.game.GameService.Follower value,
            Consumer<String> error) {
        if (mobile) return;
        if (follower == null) {
            follower = new FollowerPokemon(resources, motion, maps);
            motion.onStep(follower::step);
        }
        follower.select(value, error);
    }

    private record Neighbor(MapData map, int x, int y) {}

    private final List<Neighbor> neighbors = new ArrayList<>();

    public WorldScene(
            WorldMaps maps,
            MapMovement movement,
            OverworldCharacter character,
            ClientSettings settings,
            Consumer<Position> save) {
        this(maps, movement, character, settings, false, save);
    }

    public WorldScene(
            WorldMaps maps,
            MapMovement movement,
            OverworldCharacter character,
            ClientSettings settings,
            boolean mobile,
            Consumer<Position> save) {
        this(
                maps,
                movement,
                character,
                settings,
                mobile,
                save,
                top.totodile.pokemmo.server.game.GameService.TravelState.defaults(),
                state -> {});
    }

    public WorldScene(
            WorldMaps maps,
            MapMovement movement,
            OverworldCharacter character,
            ClientSettings settings,
            boolean mobile,
            Consumer<Position> save,
            top.totodile.pokemmo.server.game.GameService.TravelState state,
            Consumer<top.totodile.pokemmo.server.game.GameService.TravelState> saveState) {
        this(
                maps,
                movement,
                character,
                settings,
                mobile,
                save,
                state,
                saveState,
                top.totodile.pokemmo.server.game.GameService.PokedexState.defaults(),
                pokedex -> {});
    }

    public WorldScene(
            WorldMaps maps,
            MapMovement movement,
            OverworldCharacter character,
            ClientSettings settings,
            boolean mobile,
            Consumer<Position> save,
            top.totodile.pokemmo.server.game.GameService.TravelState state,
            Consumer<top.totodile.pokemmo.server.game.GameService.TravelState> saveState,
            top.totodile.pokemmo.server.game.GameService.PokedexState pokedexState,
            Consumer<top.totodile.pokemmo.server.game.GameService.PokedexState> savePokedex) {
        this.maps = maps;
        this.movement = movement;
        this.character = character;
        this.settings = settings;
        this.mobile = mobile;
        motion =
                new PlayerMotion(
                        movement,
                        Boolean.parseBoolean(
                                settings.get("client.gameplay.autosprint.enabled", "true")),
                        save);
        // Reference GdxTileLayerRenderer.Wp checks shoes separately from autosprint.
        motion.runningShoes(false);
        motion.awaitTraversal();
        WorldState.bind(settings, motion, state, saveState);
        motion.addStepListener(step->{
            if(soundResources==null||soundResources.audio()==null)return;
            var map=movement.map();
            int behavior=maps.tiles(map).behavior(map.block(step.after().x(),step.after().y())&1023);
            int sound=maps.footstepSound(behavior);
            if(sound>0)soundResources.audio().sound(this,1,2,sound,0,1,0);
        });
        pokedex = new WorldPokedex(pokedexState, savePokedex);
        try {
            nds = maps.is3d() ? new NdsWorldRenderer(maps, movement.map().id()) : null;
            if (nds == null) atlas(maps.tiles(movement.map()));
            else movement.beforeTransition(nds::prepare);
        } catch (RuntimeException error) {
            batch.dispose();
            throw error;
        }
    }

    /** Freeze the encounter's scenery from the current physical map/tile and UTC season. */
    public top.totodile.pokemmo.client.battle.EncounterEnvironment battleEnvironment() {
        var position = movement.position();
        var id = movement.map().id();
        int season =
                (java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).getMonthValue() - 1) % 4;
        return top.totodile.pokemmo.client.battle.EncounterEnvironment.resolve(
                maps.battleTerrain(id, position.x(), position.y()),
                maps.battleBackground(id),
                season);
    }

    public MapMovement movement() {
        return movement;
    }

    public PlayerMotion motion() {
        return motion;
    }

    public WorldPokedex pokedex() {
        return pokedex;
    }

    public WorldInfo worldInfo() {
        return worldInfo;
    }

    public top.totodile.pokemmo.client.audio.RomAudio.Track music(long millis) {
        int hour=(int)Math.floorMod(Math.floorDiv(atmosphere().seconds(millis,worldInfo.timeOrigin()),3600),24);
        int season=(java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).getMonthValue()-1)%4;
        return maps.music(movement.map().id(),season,hour<4||hour>=21);
    }

    private top.totodile.pokemmo.client.resources.ClientResources soundResources;

    public void setWorldInfo(WorldInfo info) {
        worldInfo = Objects.requireNonNull(info);
    }

    public int locationStringId() {
        return maps.locationStringId(movement.map().id());
    }

    public String locationName() {
        return maps.locationName(movement.map().id());
    }

    public void setInputBlocked(boolean blocked) {
        setInputBlocked(blocked,false);
    }

    /** Dialogue pauses movement without forgetting physically held direction keys. */
    public void setInputBlocked(boolean blocked,boolean preserveDirections) {
        inputBlocked = blocked;
        if (blocked) {
            actionKeys.clear();
            if(!preserveDirections)directionKeys.clear();
            if (controls != null) controls.release();
        }
    }

    public void rememberStoryDirection(int key) {
        if(!focused)return;
        int direction=directionForKey(key);
        if(direction>=0)directionKeys.press(key,direction);
    }

    public void setControls(MobileMapControls controls) {
        this.controls = controls;
    }

    public void setFocused(boolean value) {
        focused = value;
        if (!value) {
            actionKeys.clear();
            directionKeys.clear();
            if (controls != null) controls.release();
        }
    }

    private int key(String name, int fallback) {
        try {
            int value =
                    Integer.parseInt(
                            settings.get(
                                    "client.controls.gdx.key_" + name, Integer.toString(fallback)));
            return value >= 0 && value <= 255 ? value : fallback;
        } catch (NumberFormatException error) {
            return fallback;
        }
    }

    public void draw(float delta, int width, int height) {
        if (controls != null) controls.enabled(focused && !inputBlocked);
        if (Float.isFinite(delta) && delta > 0) animationTime += delta;
        int dx = 0, dy = 0;
        if (focused && !inputBlocked) {
            dx = controls == null ? 0 : controls.dx();
            dy = controls == null ? 0 : controls.dy();
            switch (directionKeys.direction()) {
                case 0 -> {
                    dx = 0;
                    dy = 1;
                }
                case 1 -> {
                    dx = 0;
                    dy = -1;
                }
                case 2 -> {
                    dx = -1;
                    dy = 0;
                }
                case 3 -> {
                    dx = 1;
                    dy = 0;
                }
            }
        }
        try {
            if (warpCover != null) {
                if (warpCover.isCompletedExceptionally()) warpCover = null;
                else if (warpCover.isDone() && focused) {
                    motion.move(warpDx, warpDy);
                    doorAnimation.close();
                    if(maps instanceof top.totodile.pokemmo.client.rom.gba.FireRedMaps gba){
                        var p=movement.position();
                        if(p.direction()==0)doorAnimation.begin(gba,movement.map(),p.x(),p.y()-1,true);
                    }
                    transition.restore(warpCover);
                    warpCover = null;
                    warpRevealing = true;
                }
            } else if (warpRevealing) {
                warpRevealing = transition.active();
            } else {
                if(doorAnimation.entering()) {
                    if(doorAnimation.ready()&&focused)motion.move(warpDx,warpDy);
                } else if(!doorAnimation.active()) motion.update(focused ? delta : 0, dx, dy);
            }
        } catch (RuntimeException error) {
            if (warpCover != null) {
                transition.restore(warpCover);
                warpCover = null;
                warpRevealing = true;
            }
            doorAnimation.close();
            Gdx.app.error("world", "Cannot move to ROM map", error);
        }
        doorAnimation.update(focused ? delta : 0, warpRevealing);
        if (nds != null) {
            if (follower != null) follower.update(focused ? delta : 0);
            if(mapObjects!=null)mapObjects.update(focused?delta:0,maps,movement,motion);
            nds.draw(movement, motion, character, batch, width, height, mobile, follower, mapObjects);
            if (controls != null) controls.draw(batch, width, height);
            return;
        }
        if (follower != null) follower.update(focused ? delta : 0);
        if (mapObjects != null)
            mapObjects.update(focused ? delta : 0, maps, movement, motion);
        Position position = movement.position();
        float px = motion.x() * 16 + 8;
        float py = (motion.y()+doorAnimation.actorOffset()) * 16 + 16;
        int zoom;
        try {
            zoom = Integer.parseInt(settings.get("client.graphics.gba.zoom_level", "-1"));
        } catch (NumberFormatException error) {
            zoom = -1;
        }
        zoom = WorldScale.zoom(zoom, width, mobile);
        camera.setToOrtho(false, (float) width / zoom, (float) height / zoom);
        camera.position.set(Math.round(px), Math.round(-py + 8), 0);
        camera.update();
        Gdx.gl.glViewport(0, 0, width, height);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        prepareNeighbors();
        updateAnimations();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawMap(0, px, py);
        // GdxTileLayerRenderer.jL：低高程对象 -> 普通上层 -> 高高程对象 -> 高层遮挡。
        drawActors(px, py, position.direction(), false);
        drawMap(1, px, py);
        drawActors(px, py, position.direction(), true);
        drawMap(2, px, py);
        if(mapObjects!=null)mapObjects.drawReaction(batch);
        batch.end();
        drawAtmosphere(delta, width, height);
        if (controls != null) controls.draw(batch, width, height);
    }

    /** Live state shared with battle rendering; only Kanto PC participates. */
    public top.totodile.pokemmo.client.battle.EncounterAtmosphere.State battleAtmosphere() {
        if (!supportsAtmosphere()) return null;
        var id = movement.map().id();
        return new top.totodile.pokemmo.client.battle.EncounterAtmosphere.State(
                atmosphere.seconds(System.currentTimeMillis(), worldInfo.timeOrigin()),
                maps.outside(id),
                atmosphere.resolvedWeather(maps.weather(id)));
    }

    private void drawAtmosphere(float delta, int width, int height) {
        if (!supportsAtmosphere()) return;
        var id = movement.map().id();
        atmosphere.draw(
                batch,
                delta,
                width,
                height,
                atmosphere.seconds(System.currentTimeMillis(), worldInfo.timeOrigin()),
                maps.outside(id),
                maps.weather(id),
                motion.x(),
                motion.y());
    }

    private int directionForKey(int value) {
        if (value == Input.Keys.W || value == Input.Keys.UP || value == key("up", Input.Keys.W))
            return 1;
        if (value == Input.Keys.S || value == Input.Keys.DOWN || value == key("down", Input.Keys.S))
            return 0;
        if (value == Input.Keys.A || value == Input.Keys.LEFT || value == key("left", Input.Keys.A))
            return 2;
        if (value == Input.Keys.D
                || value == Input.Keys.RIGHT
                || value == key("right", Input.Keys.D)) return 3;
        return -1;
    }

    private void drawActors(float px, float py, int direction, boolean high) {
        if (mapObjects != null) {
            drawMapActors(px, py, direction, high);
            return;
        }
        boolean drawFollower =
                follower != null && follower.visible() && (follower.elevation() > 3) == high;
        boolean behind = drawFollower && follower.y() <= motion.y();
        if (behind) drawFollower();
        if ((movement.elevation() > 3) == high) drawCharacter(px, py, direction);
        if (drawFollower && !behind) drawFollower();
    }

    private record Actor(float y, float x, Runnable draw) {}

    private void drawMapActors(float px, float py, int direction, boolean high) {
        var actors = new ArrayList<Actor>();
        if(!high&&gateSprites!=null&&movement.rotatingGates()!=null)for(var gate:movement.rotatingGates().gates(movement.map().id()))actors.add(new Actor(gate.y(),gate.x(),()->gateSprites.draw(batch,gate)));
        appendMapActors(actors, movement.map(), 0, 0, high);
        for (var neighbor : neighbors)
            appendMapActors(actors, neighbor.map(), neighbor.x(), neighbor.y(), high);
        if ((movement.elevation() > 3) == high)
            actors.add(new Actor(motion.y(), motion.x(), () -> drawCharacter(px, py, direction)));
        if (follower != null && follower.visible() && (follower.elevation() > 3) == high)
            actors.add(new Actor(follower.y(), follower.x(), this::drawFollower));
        // GdxTileLayerRenderer queues actors with map rows, interleaved with the tile passes.
        actors.sort(Comparator.comparingDouble(Actor::y).thenComparingDouble(Actor::x));
        for (var actor : actors) actor.draw().run();
    }

    private void appendMapActors(List<Actor> actors, MapData map, int dx, int dy, boolean high) {
        for (var actor : mapObjects.actors(map.id())) {
            var object = actor.definition;
            if (!actor.visible()) continue;
            // Reference yD0: ground elevations <=2, raised elevations >=3.
            if ((object.elevation() >= 3) != high) continue;
            float x = actor.drawX() + dx, y = actor.drawY() + dy;
            if (Math.abs(x - motion.x()) * 16 > camera.viewportWidth / 2 + 128
                    || Math.abs(y - motion.y()) * 16 > camera.viewportHeight / 2 + 128) continue;
            var placed = new WorldMapObjects.Placed(actor, x, y);
            actors.add(new Actor(y, x, () -> mapObjects.draw(batch, placed)));
        }
    }

    private void drawFollower() {
        float x = Math.round(follower.x() * 16 + 8), y = Math.round(-(follower.y() * 16 + 16) + 4);
        if (follower.opacity() > .49f) character.drawShadow(batch, x, y);
        follower.draw(batch, x, y);
    }

    private void drawCharacter(float px, float py, int direction) {
        if(doorAnimation.hidesActor())return;
        if(motion.jumpHeight()>0){
            character.drawShadow(batch,px,-py+4);
            character.drawBody(batch,px,-py+4+motion.jumpHeight(),direction,motion.animationTime(),motion.moving(),motion.mode().animation);return;
        }
        character.draw(
                batch,
                px,
                -py + 4,
                direction,
                doorAnimation.active()?doorAnimation.actorTime():motion.animationTime(),
                doorAnimation.active()?doorAnimation.actorWalking():motion.moving(),
                doorAnimation.active()?0:motion.mode().animation);
    }

    private void prepareNeighbors() {
        MapData current = movement.map();
        neighbors.clear();
        atlas(maps.tiles(current));
        for (Connection connection : current.connections()) {
            if (connection.direction() < 1 || connection.direction() > 4) continue;
            MapData other = maps.map(connection.target());
            int x =
                    connection.direction() <= 2
                            ? connection.offset()
                            : connection.direction() == 3 ? -other.width() : current.width();
            int y =
                    connection.direction() >= 3
                            ? connection.offset()
                            : connection.direction() == 2 ? -other.height() : current.height();
            neighbors.add(new Neighbor(other, x, y));
            atlas(maps.tiles(other));
        }
    }

    public boolean move(int dx, int dy) {
        if(doorAnimation.active())return false;
        try {
            return motion.move(dx, dy);
        } catch (RuntimeException error) {
            Gdx.app.error("world", "Cannot move to ROM map", error);
            return false;
        }
    }

    private void drawMap(int pass, float px, float py) {
        MapData map = movement.map();
        MapData cachedSource = null;
        Tiles tiles = null;
        Texture[] textures = null;
        int left = (int) Math.floor((px - camera.viewportWidth / 2) / 16) - 1;
        int right = (int) Math.ceil((px + camera.viewportWidth / 2) / 16) + 1;
        int top = (int) Math.floor((py - camera.viewportHeight / 2) / 16) - 1;
        int bottom = (int) Math.ceil((py + camera.viewportHeight / 2) / 16) + 1;
        for (int y = top; y <= bottom; y++)
            for (int x = left; x <= right; x++) {
                MapData source = map;
                int sx = x, sy = y;
                if (!map.contains(x, y))
                    for (Neighbor neighbor : neighbors) {
                        if (neighbor.map.contains(x - neighbor.x, y - neighbor.y)) {
                            source = neighbor.map;
                            sx = x - neighbor.x;
                            sy = y - neighbor.y;
                            break;
                        }
                    }
                if(doorAnimation.replaces(source.id(),sx,sy)){
                    doorAnimation.draw(batch,pass,source.id(),sx,sy,x,y);
                    continue;
                }
                // A layer run uses one tileset/atlas, as in GdxTileLayerRenderer. Do not hash
                // the entire animated Tiles record for every cell of the live battle backdrop.
                if (source != cachedSource) {
                    cachedSource = source;
                    tiles = maps.tiles(source);
                    textures = atlases.get(tiles);
                }
                int block = source.block(sx, sy), id = block & 1023;
                if (id >= tiles.count()) continue;
                // 火红 C4=32：属性最高字节的 0x20 位，含 layer=1 和 layer=3。
                boolean covered = (tiles.attributes()[id] & 0x20000000) != 0;
                if (pass == 0) {
                    drawTile(textures[0], id, x, y);
                    if (covered) drawTile(textures[1], id, x, y);
                } else if (!covered && (pass == 1 ? block >>> 12 <= 3 : block >>> 12 > 3))
                    drawTile(textures[1], id, x, y);
            }
    }

    @Override
    public boolean keyDown(int key) {
        if (!focused || inputBlocked) return false;
        int direction = directionForKey(key);
        if (direction >= 0) {
            directionKeys.press(key, direction);
            return true;
        }
        if (key == Input.Keys.K) {
            if (!actionKeys.add(key)) return true;
            motion.toggleRunning();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int key) {
        return directionKeys.release(key) | actionKeys.remove(key);
    }

    private void drawTile(Texture texture, int id, int x, int y) {
        batch.draw(
                texture,
                x * 16,
                -y * 16 - 16,
                16,
                16,
                (id % 16) * 16,
                (id / 16) * 16,
                16,
                16,
                false,
                false);
    }

    private Texture[] atlas(Tiles tiles) {
        Texture[] existing = atlases.get(tiles);
        if (existing != null) return existing;
        Texture[] result = new Texture[2];
        try {
            for (int layer = 0; layer < 2; layer++) {
                Pixmap image =
                        new Pixmap(256, ((tiles.count() + 15) / 16) * 16, Pixmap.Format.RGBA8888);
                try {
                    image.setBlending(Pixmap.Blending.None);
                    for (int id = 0; id < tiles.count(); id++)
                        for (int y = 0; y < 16; y++)
                            for (int x = 0; x < 16; x++)
                                image.drawPixel(
                                        id % 16 * 16 + x,
                                        id / 16 * 16 + y,
                                        tiles.pixels()[layer][id * 256 + y * 16 + x]);
                    result[layer] = new Texture(image);
                    result[layer].setFilter(
                            Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                } finally {
                    image.dispose();
                }
            }
        } catch (RuntimeException error) {
            for (Texture texture : result) if (texture != null) texture.dispose();
            throw error;
        }
        atlases.put(tiles, result);
        animationFrames.put(tiles, new int[tiles.animations().size()]);
        if (atlases.size() > 6) {
            var oldest = atlases.entrySet().iterator();
            var entry = oldest.next();
            for (Texture texture : entry.getValue()) texture.dispose();
            oldest.remove();
            animationFrames.remove(entry.getKey());
        }
        return result;
    }

    private void updateAnimations() {
        Pixmap image = null;
        try {
            for (var atlas : atlases.entrySet()) {
                Tiles tiles = atlas.getKey();
                int[] previous = animationFrames.get(tiles);
                for (int index = 0; index < tiles.animations().size(); index++) {
                    TileAnimation animation = tiles.animations().get(index);
                    // 参考 SpriteAnimationFrameSet.gA0：300 毫秒一帧，与角色移动倍率无关。
                    int frame = (int) ((long) (animationTime / .3) % animation.frames().length);
                    if (previous[index] == frame) continue;
                    if (image == null) {
                        image = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
                        image.setBlending(Pixmap.Blending.None);
                    }
                    int[] pixels = animation.frames()[frame];
                    for (int y = 0; y < 16; y++)
                        for (int x = 0; x < 16; x++) image.drawPixel(x, y, pixels[y * 16 + x]);
                    atlas.getValue()[animation.layer()].draw(
                            image, animation.id() % 16 * 16, animation.id() / 16 * 16);
                    previous[index] = frame;
                }
            }
        } finally {
            if (image != null) image.dispose();
        }
    }

    @Override
    public void close() {
        restoreStoryTiles();
        storyTileOriginals.clear();
        if(soundResources!=null&&soundResources.audio()!=null)soundResources.audio().stop(this);
        atmosphere.close();
        doorAnimation.close();
        if (nds != null) nds.close();
        if (follower != null) follower.close();
        if (mapObjects != null) mapObjects.close();
        if(gateSprites!=null)gateSprites.close();
        for (Texture[] textures : atlases.values())
            for (Texture texture : textures) texture.dispose();
        atlases.clear();
        animationFrames.clear();
        character.close();
        if (controls != null) controls.close();
        batch.dispose();
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        return controls != null && controls.touchDown(x, y, pointer);
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        return controls != null && controls.touchDragged(x, y, pointer);
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        return controls != null && controls.touchUp(pointer);
    }

    @Override
    public boolean touchCancelled(int x, int y, int pointer, int button) {
        return controls != null && controls.touchUp(pointer);
    }
}
