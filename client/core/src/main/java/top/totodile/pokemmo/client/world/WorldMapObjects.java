package top.totodile.pokemmo.client.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.RomInfo;
import top.totodile.pokemmo.client.rom.gba.FireRedMaps.Id;
import top.totodile.pokemmo.client.rom.gba.GbaNpcSprites;

import java.io.IOException;
import java.util.*;

/** Server-created map entities, ROM rendering and reference client idle movement. */
final class WorldMapObjects implements AutoCloseable {
    record ObjectSprite(int id, int graphics, int x, int y, int elevation, int direction,
                        String hideFlag, int movement, int rangeX, int rangeY) {}
    record Placed(WorldNpcMotion actor, float x, float y) {}

    private List<top.totodile.pokemmo.server.game.MapEntities.MapView> lastSnapshot;
    private final Map<Id, List<ObjectSprite>> maps = new HashMap<>();
    private final Map<Integer, Texture> textures = new HashMap<>();
    private final Set<Integer> absent = new HashSet<>();
    private final GbaNpcSprites sprites;
    private final top.totodile.pokemmo.client.rom.nds.bw.BwNpcSprites bw;
    private final top.totodile.pokemmo.client.rom.nds.gen4.Gen4NpcSprites gen4;
    private final ClientResources resources;
    private final boolean kanto;
    private top.totodile.pokemmo.client.rom.nds.hgss.HgssNpcSprites hgss;
    private boolean triedHgss;
    private Texture shadow, exclamation;
    private int reactionActor=-1;
    private Id reactionMap;
    private float reactionX,reactionY;
    private boolean reactionDrawn;
    void reaction(Id map,int actor,boolean visible){reactionMap=map;reactionActor=visible?actor:-1;reactionDrawn=false;}
    void face(Id map,int actor,int direction){for(var npc:actors(map))if(npc.definition.id()==actor){npc.direction=direction;npc.talking(false,direction);}}

    private final Set<Integer> missingHgss=new HashSet<>();

    private GbaNpcSprites.Frame replacement(int graphics,int direction,int phase) {
        if(!kanto)return null;
        if(!Boolean.parseBoolean(resources.preferences().get("client.graphics.nds.hgss_sprites_enabled_in_gba","true")))return null;
        if(missingHgss.contains(graphics))return null;
        try {
            if(!triedHgss) {
                triedHgss=true;
                var rom=resources.roms().get(top.totodile.pokemmo.client.rom.RomType.HEART_GOLD_SOUL_SILVER);
                if(rom!=null)hgss=new top.totodile.pokemmo.client.rom.nds.hgss.HgssNpcSprites(rom);
            }
            var frame=hgss==null?null:hgss.frame(graphics,direction,phase);
            if(frame==null)missingHgss.add(graphics);
            return frame;
        } catch(IOException | RuntimeException error) {
            missingHgss.add(graphics);
            com.badlogic.gdx.Gdx.app.error("world","Cannot load HGSS NPC replacement "+graphics,error);
            return null;
        }
    }
    private final Map<Id, List<WorldNpcMotion>> motions = new HashMap<>();

    WorldMapObjects(ClientResources resources, RomInfo rom) throws IOException {
        kanto=rom.type()==top.totodile.pokemmo.client.rom.RomType.FIRE_RED;
        bw = rom.type()==top.totodile.pokemmo.client.rom.RomType.BLACK_WHITE ? new top.totodile.pokemmo.client.rom.nds.bw.BwNpcSprites(rom) : null;
        gen4 = rom.type()==top.totodile.pokemmo.client.rom.RomType.PLATINUM || rom.type()==top.totodile.pokemmo.client.rom.RomType.HEART_GOLD_SOUL_SILVER ? new top.totodile.pokemmo.client.rom.nds.gen4.Gen4NpcSprites(rom) : null;
        sprites = bw==null && gen4==null ? new GbaNpcSprites(rom) : null;
        this.resources=resources;
    }

    List<ObjectSprite> objects(Id id) { return maps.getOrDefault(id, List.of()); }

    void dialogue(Id id, int actor, int playerX, int playerY) {
        for (var entry : motions.entrySet()) for (var npc : entry.getValue()) {
            boolean target = entry.getKey().equals(id) && npc.definition.id() == actor;
            int direction = playerX != npc.x ? (playerX < npc.x ? 2 : 3)
                    : playerY < npc.y ? 1 : 0;
            npc.talking(target, direction);
        }
    }

    /** Apply server entity additions/removals; retain ongoing client idle animations. */
    void entities(List<top.totodile.pokemmo.server.game.MapEntities.MapView> snapshot) {
        if (snapshot == lastSnapshot) return;
        lastSnapshot = snapshot;
        var active = new HashSet<Id>();
        for (var map : snapshot) {
            var id = new Id(map.bank(), map.map());
            active.add(id);
            var definitions = map.npcs().stream().map(n -> new ObjectSprite(n.id(), n.graphics(),
                    n.x(), n.y(), n.elevation(), n.direction(), "", n.movement(), n.rangeX(), n.rangeY())).toList();
            var old = motions.getOrDefault(id, List.of());
            var actors = new ArrayList<WorldNpcMotion>();
            for (var definition : definitions) {
                var actor = old.stream().filter(n -> n.definition.equals(definition)).findFirst().orElse(null);
                if (actor == null) actor = new WorldNpcMotion(definition,
                        java.util.concurrent.ThreadLocalRandom.current());
                actor.nativeZeroGraphics=gen4!=null;
                actors.add(actor);
            }
            maps.put(id, definitions);
            motions.put(id, List.copyOf(actors));
        }
        maps.keySet().retainAll(active);
        motions.keySet().retainAll(active);
    }

    List<WorldNpcMotion> actors(Id id) {
        return motions.computeIfAbsent(id, key -> objects(key).stream()
                .map(definition -> new WorldNpcMotion(definition,
                        java.util.concurrent.ThreadLocalRandom.current())).toList());
    }

    boolean occupied(Id id, int x, int y, int elevation) {
        // GdxParticleEmitterNode.qm uses occupancyDirection=-1 for the local player;
        // WorldEntity.a1 then treats a GBA NPC as a solid volume from every approach.
        return actors(id).stream().anyMatch(actor -> actor.occupies(x, y, 0));
    }

    /** TradeOfferItemEntry.Es: ROM block elevation and entity packet layer differ by one. */
    static int entityElevation(int block) {
        int result = ((byte)((short)block >> 10) >> 2) - 1;
        return result == -8 ? 8 : result;
    }

    void update(float delta, WorldMaps terrain, MapMovement player, PlayerMotion motion) {
        var ids = new LinkedHashSet<Id>();
        ids.add(player.map().id());
        for (var connection : player.map().connections()) ids.add(connection.target());
        for (var id : ids) {
            var map = terrain.map(id);
            var actors = actors(id);
            for (var actor : actors) actor.update(delta, (source, x, y) -> {
                if (!map.contains(x, y) || map.warp(x, y) != null) return false;
                int block = map.block(x, y), elevation = entityElevation(block);
                int behavior = terrain.tiles(map).behavior(block & 1023);
                if ((block & 0xc00) != 0 || block >>> 12 == 15 || terrain.isWater(behavior)
                        || source.definition.elevation() != 0 && elevation >= 0
                                && source.definition.elevation() != elevation) return false;
                int dx = x - source.x, dy = y - source.y;
                int previous = terrain.tiles(map).behavior(map.block(source.x, source.y) & 1023);
                if (terrain.blocked(previous, dx, dy) || terrain.blocked(behavior, -dx, -dy)) return false;
                int layer = elevation < 0 ? source.definition.elevation() : elevation;
                int playerLayer = entityElevation(player.elevation() << 12);
                if (actors.stream().anyMatch(other -> other != source && other.occupies(x, y, layer)))
                    return false;
                var position = player.position();
                if (id.equals(player.map().id())) {
                    if ((playerLayer < 0 || layer == 0 || playerLayer == layer)
                            && (position.x() == x && position.y() == y
                                    || motion.moving() && player.stepFromX() == x && player.stepFromY() == y))
                        return false;
                } else {
                    // Connected-map coordinates must reserve the player's tile across the seam too.
                    for (var connection : map.connections()) if (connection.target().equals(player.map().id())) {
                        int ox = connection.direction() <= 2 ? connection.offset()
                                : connection.direction() == 3 ? -player.map().width() : map.width();
                        int oy = connection.direction() >= 3 ? connection.offset()
                                : connection.direction() == 2 ? -player.map().height() : map.height();
                        if ((playerLayer < 0 || layer == 0 || playerLayer == layer)
                                && (x == position.x() + ox && y == position.y() + oy
                                        || motion.moving() && x == player.stepFromX() + ox
                                                && y == player.stepFromY() + oy)) return false;
                    }
                }
                return true;
            });
        }
    }

    void draw(SpriteBatch batch, Placed placed) {
        var actor = placed.actor();
        var object = actor.definition;
        if (!actor.visible()) return;
        var replacement=replacement(object.graphics(),actor.direction,actor.phase());
        boolean custom=replacement!=null;
        int key = object.graphics() * 16 + actor.direction * 4 + actor.phase() + (custom?1<<20:0);
        if (absent.contains(key)) return;
        Texture image = textures.get(key);
        if (image == null) {
            try {
                var pixels = custom?replacement:gen4!=null?gen4.frame(object.graphics(),actor.direction,actor.phase()):bw!=null?bw.frame(object.graphics(),actor.direction,actor.phase()):sprites.frame(object.graphics(), actor.direction, actor.phase());
                if (pixels == null) { absent.add(key); return; }
                var pixmap = new Pixmap(pixels.width(), pixels.height(), Pixmap.Format.RGBA8888);
                try {
                    pixmap.setBlending(Pixmap.Blending.None);
                    for (int y = 0; y < pixels.height(); y++)
                        for (int x = 0; x < pixels.width(); x++)
                            pixmap.drawPixel(x, y, pixels.rgba()[y * pixels.width() + x]);
                    image = new Texture(pixmap);
                    image.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                    textures.put(key, image);
                } finally { pixmap.dispose(); }
            } catch (IOException error) {
                absent.add(key);
                com.badlogic.gdx.Gdx.app.error("world", "Cannot load map object sprite " + object.graphics(), error);
                return;
            }
        }
        // GdxParticleController.tD0/Nq and GdxParticleFollowerController.N30:
        // map tile top minus 2 px, centered over 16 px; preserve source transparent padding.
        float width=image.getWidth()*(custom?.751f:1),height=image.getHeight()*(custom?.751f:1);
        if(bw!=null)width=height=bw.billboardSize(object.graphics());
        if(gen4!=null)width=height=22.528f;
        float x = (int)(placed.x() * 16) + (custom?8.5f:8) - width / 2;
        float y = -(int)(placed.y() * 16) - 14;
        if(custom) {
            if(shadow==null)shadow=top.totodile.pokemmo.client.ui.character.OverworldCharacter.loadShadow(resources.apply("sprites/game.pak"));
            batch.draw(shadow,(int)(placed.x()*16)-1.5f,-(int)(placed.y()*16)-16,18,18);
        }
        boolean mirror = bw==null && actor.direction == 3 && (gen4!=null || object.graphics() != 92);
        batch.draw(image, mirror ? x + width : x, y, mirror ? -width : width,height);
        if(object.id()==reactionActor && reactionMap!=null && actors(reactionMap).contains(actor)){
            reactionX=placed.x()*16;reactionY=y+height-2;reactionDrawn=true;
        }
    }

    void drawReaction(SpriteBatch batch){
        if(reactionActor<0||!reactionDrawn||sprites==null)return;
        if(exclamation==null)try{
                var frame=sprites.exclamation();var pixels=new Pixmap(16,16,Pixmap.Format.RGBA8888);
                try{for(int row=0;row<16;row++)for(int col=0;col<16;col++)pixels.drawPixel(col,row,frame.rgba()[row*16+col]);exclamation=new Texture(pixels);}
                finally{pixels.dispose();}
            }catch(IOException error){throw new IllegalStateException("Cannot load NPC reaction",error);}
        batch.draw(exclamation,reactionX,reactionY,16,16);
        reactionDrawn=false;
    }

    public void close() {
        textures.values().forEach(Texture::dispose);
        textures.clear();
        absent.clear();
        if(shadow!=null){shadow.dispose();shadow=null;}
        if(exclamation!=null){exclamation.dispose();exclamation=null;}
    }
}
