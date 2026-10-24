package top.totodile.pokemmo.client.ui.pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import java.util.*;
import java.util.zip.ZipInputStream;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

/** PC projection of ParticleManager's special/type_N: official APS sprites and emitter curves. */
final class SummaryParticles extends Widget {
    private final PokemonController controller;
    private final List<Emitter> emitters=new ArrayList<>();
    private final Random random=new Random();
    private record Curve(float[] values,float[] times){}
    private final Map<JsonValue,Curve> curves=new IdentityHashMap<>();
    private int type=-1;
    SummaryParticles(PokemonController controller){this.controller=controller;setTheme("");setClip(true);}
    void type(int romType){
        int next=romType>=9?romType+1:romType;if(next==type)return;emitters.clear();curves.clear();
        try{
            var effect=readEffect(controller.resources.apply("sprites/particles.pak"),next);
            for(var config:effect.get("resource").get("controllers"))emitters.add(new Emitter(config));
            type=next;
        }catch(Exception failure){type=-1;emitters.clear();Gdx.app.error("pokemon-summary","Cannot load official type effect "+next,failure);}
    }
    static JsonValue readEffect(com.badlogic.gdx.files.FileHandle resource,int type)throws java.io.IOException{
        // ClientResources handles may represent bundled/overlaid assets, not a working-directory file.
        try(var zip=new ZipInputStream(resource.read())){
            String path="particle/special/type_"+type+".vfx";
            for(var entry=zip.getNextEntry();entry!=null;entry=zip.getNextEntry()){
                if(entry.getName().equals(path))return new JsonReader().parse(new String(zip.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));
            }
            throw new java.io.IOException("Missing official effect "+path);
        }
    }

    void reset(){for(var emitter:emitters){emitter.particles.clear();emitter.pending=0;}}
    @Override protected void paintWidget(GUI gui){
        if(!(gui.getRenderer() instanceof GdxTwlRenderer renderer))return;
        float delta=Math.min(.1f,Gdx.graphics.getDeltaTime());
        for(var emitter:emitters)emitter.draw(renderer,delta);
    }
    @Override protected boolean handleEvent(Event event){return false;}
    private float range(JsonValue value,float fallback){if(value==null)return fallback;float min=value.getFloat("highMin",fallback),max=value.getFloat("highMax",min);return min+random.nextFloat()*(max-min);}
    private float curve(JsonValue value,float t,float fallback){
        if(value==null||value.get("scaling")==null||value.get("timeline")==null)return fallback;
        var curve=curves.computeIfAbsent(value,v->new Curve(v.get("scaling").asFloatArray(),v.get("timeline").asFloatArray()));var values=curve.values();var times=curve.times();
        int length=Math.min(values.length,times.length);if(length==0)return fallback;
        for(int i=1;i<length;i++)if(t<=times[i])return values[i-1]+(values[i]-values[i-1])*(t-times[i-1])/Math.max(.00001f,times[i]-times[i-1]);
        return values[length-1];
    }
    private final class Emitter {
        final Image image;
        final JsonValue rate,life,scale,alpha,rotation;
        final float spawnWidth,spawnHeight,acceleration;
        final int maximum;
        final List<Particle> particles=new ArrayList<>();
        float pending;
        Emitter(JsonValue config){
            image=controller.images().particle(config.getInt("aps",0));var emitter=config.get("emitter");
            maximum=emitter.getInt("maxParticleCount",60);rate=emitter.get("emission");life=emitter.get("life");
            JsonValue size=null,opacity=null,spin=null,spawn=null;float force=0;
            for(var influencer:config.get("influencers")){
                String kind=influencer.getString("class","");
                if(kind.contains("SpawnInfluencer"))spawn=influencer.get("spawnShape");
                if(kind.contains("ScaleXYInfluencer"))size=influencer.get("valuex");
                if(kind.contains("ColorInfluencer"))opacity=influencer.get("alpha");
                var velocities=influencer.get("velocities");if(velocities!=null)for(var velocity:velocities){
                    String modifier=velocity.getString("class","");if(modifier.endsWith("Rotational2D"))spin=velocity.get("strengthValue");
                    if(modifier.endsWith("PolarAcceleration"))force=range(velocity.get("strengthValue"),0);
                }
            }
            scale=size;alpha=opacity;rotation=spin;acceleration=force;
            spawnWidth=spawn==null?2:range(spawn.get("spawnWidthValue"),2);spawnHeight=spawn==null?2:range(spawn.get("spawnHeightValue"),2);
        }
        void draw(GdxTwlRenderer renderer,float delta){
            pending+=delta*range(rate,0);
            while(pending>=1){pending--;if(particles.size()<maximum)particles.add(new Particle(this));}
            for(var iterator=particles.iterator();iterator.hasNext();){
                var p=iterator.next();p.age+=delta;if(p.age>=p.lifetime){iterator.remove();continue;}
                float t=p.age/p.lifetime;p.angle+=p.spin*curve(rotation,t,1)*delta;
                float x=getX()+getWidth()/2f+p.x*getWidth()/2f;
                float y=getY()+getHeight()/2f-(p.y+.5f*acceleration*p.age*p.age*p.sine)*getHeight()/2f;
                renderer.drawParticle(image,x,y,p.size*getWidth()/2f*curve(scale,t,1),p.angle,range(alpha,1)*curve(alpha,t,1));
            }
        }
    }
    private final class Particle {
        float age,angle;
        final float lifetime,x,y,size,spin,sine;
        Particle(Emitter emitter){lifetime=Math.max(.001f,range(emitter.life,1500)/1000);x=(random.nextFloat()-.5f)*emitter.spawnWidth;y=(random.nextFloat()-.5f)*emitter.spawnHeight;size=range(emitter.scale,.3f);spin=range(emitter.rotation,0);sine=(float)Math.sin(random.nextFloat()*Math.PI);}
    }
}
