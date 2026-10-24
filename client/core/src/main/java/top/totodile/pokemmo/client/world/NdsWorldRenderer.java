package top.totodile.pokemmo.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import java.io.IOException;
import java.util.*;
import top.totodile.pokemmo.client.rom.gba.FireRedMaps.*;
import top.totodile.pokemmo.client.rom.nds.model.*;
import top.totodile.pokemmo.client.ui.character.OverworldCharacter;

/** Nitro field models share the camera and depth buffer with the character billboard. */
final class NdsWorldRenderer implements AutoCloseable {
    private record Piece(NitroModelRenderer model,Matrix4 local,BoundingBox bounds) {}
    private record Asset(NitroModelRenderer model,BoundingBox bounds) {}
    private record AssetKey(byte[] model,byte[] textures,List<byte[]> animations,int hash) {
        AssetKey(byte[] model,byte[] textures,List<byte[]> animations) {this(model,textures,animations,31*(31*Arrays.hashCode(model)+Arrays.hashCode(textures))+Arrays.deepHashCode(animations.toArray()));}
        @Override public int hashCode(){return hash;}
        @Override public boolean equals(Object other) {
            return other instanceof AssetKey key&&hash==key.hash&&Arrays.equals(model,key.model)&&Arrays.equals(textures,key.textures)&&Arrays.deepEquals(animations.toArray(),key.animations.toArray());
        }
    }
    private final WorldMaps maps;
    private final ModelBatch models=new ModelBatch();
    private final PerspectiveCamera camera=new PerspectiveCamera();
    private final LinkedHashMap<Id,List<Piece>> loaded=new LinkedHashMap<>(8,.75f,true);
    private final Map<AssetKey,List<Asset>> assets=new HashMap<>();
    private final Matrix4 transform=new Matrix4();
    private final BoundingBox visible=new BoundingBox();
    NdsWorldRenderer(WorldMaps maps,Id initial) {
        this.maps=maps;
        try {load(initial);}catch(RuntimeException error){close();throw error;}
    }
    void prepare(MapData target) {load(target.id());}
    private List<Piece> load(Id id) {
        var existing=loaded.get(id);if(existing!=null)return existing;
        List<Piece> pieces=new ArrayList<>();
        try {
            for(var placement:maps.geometry(id))for(var asset:assets(placement)) {
                Matrix4 local=new Matrix4().setToTranslation(placement.x(),placement.y(),placement.z()).rotate(Vector3.Y,placement.rotation());
                pieces.add(new Piece(asset.model,local,asset.bounds));
            }
            loaded.put(id,pieces);
            return pieces;
        } catch(IOException|RuntimeException failure) {
            releaseUnusedAssets();
            throw new IllegalStateException("Cannot render NDS field "+id,failure);
        }
    }
    private List<Asset> assets(WorldMaps.ModelPlacement placement) throws IOException {
        var key=new AssetKey(placement.model(),placement.textures(),placement.animations());var existing=assets.get(key);
        if(existing!=null)return existing;
        List<Asset> created=new ArrayList<>();
        try {
            var parsedModels=maps.modelCache()==null?NdsNitroModelParser.readAll(placement.model(),placement.textures(),placement.animations())
                    :maps.modelCache().read(placement.model(),placement.textures(),placement.animations());
            for(var parsed:parsedModels) {
                var model=new NitroModelRenderer(parsed,1);
                created.add(new Asset(model,model.instance.calculateBoundingBox(new BoundingBox())));
            }
            assets.put(key,created);return created;
        }catch(IOException|RuntimeException failure){created.forEach(a -> a.model.close());throw failure;}
    }
    private void releaseUnusedAssets() {
        Set<NitroModelRenderer> used=Collections.newSetFromMap(new IdentityHashMap<>());
        loaded.values().forEach(pieces -> pieces.forEach(piece -> used.add(piece.model)));
        var entries=assets.values().iterator();
        while(entries.hasNext()) {
            var entry=entries.next();
            if(entry.stream().noneMatch(asset -> used.contains(asset.model))) {
                entry.forEach(asset -> asset.model.close());entries.remove();
            }
        }
    }
    void draw(MapMovement movement,PlayerMotion motion,OverworldCharacter character,SpriteBatch sprites,int width,int height,boolean mobile,FollowerPokemon follower) {
        Id id=movement.map().id();var view=maps.camera(id,mobile);
        float x=(motion.x()+.5f)*16,z=(motion.y()+.5f)*16,y=maps.height(id,motion.x(),motion.y());
        float angle=view.pitch()*MathUtils.degreesToRadians;
        camera.fieldOfView=view.fieldOfView();
        camera.viewportWidth=width;camera.viewportHeight=height;camera.near=.5f;camera.far=6400;
        camera.up.set(Vector3.Y);
        camera.position.set(x,y+view.distance()*MathUtils.sin(angle),z+view.distance()*MathUtils.cos(angle));
        camera.lookAt(x,y,z);camera.update();
        Gdx.gl.glViewport(0,0,width,height);Gdx.gl.glDepthMask(true);Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        models.begin(camera);
        try {
            drawMap(id,0,0);
            MapData current=movement.map();
            if(!maps.includesNeighborGeometry())for(Connection connection:current.connections()) {
                if(connection.direction()<1||connection.direction()>4)continue;
                MapData other=maps.map(connection.target());
                int ox=connection.direction()<=2?connection.offset():connection.direction()==3?-other.width():current.width();
                int oz=connection.direction()>=3?connection.offset():connection.direction()==2?-other.height():current.height();
                // Bounds reject distant adjoining routes before allocating their GPU models.
                visible.set(new Vector3(ox*16,-256,oz*16),new Vector3((ox+other.width())*16,512,(oz+other.height())*16));
                if(camera.frustum.boundsInFrustum(visible))drawMap(other.id(),ox*16,oz*16);
            }
        } finally {models.end();}
        sprites.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        // Ground shadow and upright sprite have independent planes, both depth-tested against field meshes.
        transform.setToTranslation(x,y+.2f,z).rotate(Vector3.X,-90);
        sprites.setTransformMatrix(transform);sprites.begin();character.drawShadow(sprites,0,0);sprites.end();
        if(follower!=null&&follower.visible()&&follower.y()<=motion.y())drawFollower(follower,id,view.pitch(),sprites,character);
        transform.setToTranslation(x,y+.3f,z).rotate(Vector3.X,-view.pitch());
        sprites.setTransformMatrix(transform);sprites.begin();
        character.drawBody(sprites,0,0,movement.position().direction(),motion.animationTime(),motion.moving(),motion.mode().animation);
        sprites.end();sprites.setTransformMatrix(transform.idt());
        if(follower!=null&&follower.visible()&&follower.y()>motion.y())drawFollower(follower,id,view.pitch(),sprites,character);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);Gdx.gl.glDepthMask(true);
        boolean evicted=false;
        while(loaded.size()>6) {var entries=loaded.entrySet().iterator();entries.next();entries.remove();evicted=true;}
        if(evicted)releaseUnusedAssets();
    }
    private void drawFollower(FollowerPokemon follower,Id id,float pitch,SpriteBatch sprites,OverworldCharacter character){
        float x=(follower.x()+.5f)*16,z=(follower.y()+.5f)*16,y=maps.height(id,follower.x(),follower.y());
        if(follower.opacity()>.49f){transform.setToTranslation(x,y+.2f,z).rotate(Vector3.X,-90);sprites.setTransformMatrix(transform);sprites.begin();character.drawShadow(sprites,0,0);sprites.end();}
        transform.setToTranslation(x,y+.3f,z).rotate(Vector3.X,-pitch);
        sprites.setTransformMatrix(transform);sprites.begin();follower.draw(sprites,0,0);sprites.end();sprites.setTransformMatrix(transform.idt());
    }
    private void drawMap(Id id,float x,float z) {
        for(Piece piece:load(id)) {
            piece.model.instance.transform.setToTranslation(x,0,z).mul(piece.local);
            visible.set(piece.bounds).mul(piece.model.instance.transform);
            if(camera.frustum.boundsInFrustum(visible))models.render(piece.model.instance);
        }
    }
    @Override public void close() {
        loaded.clear();releaseUnusedAssets();models.dispose();
    }
}
