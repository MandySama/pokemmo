package top.totodile.pokemmo.ui;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.rom.ParticleTextures;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import static org.lwjgl.opengl.GL11.*;

/** 属性背景使用独立寿命粒子，并按窗口相机参数投影。 */
final class SummaryParticles {
    record Curve(float[] values,float[] times) {
        float value(float age) {
            if(values.length==0)return 1;
            int last=Math.min(values.length,times.length)-1;
            if(last<1)return values[0];
            for(int i=1;i<=last;i++)if(age<=times[i]){
                float width=times[i]-times[i-1],f=width==0?1:(age-times[i-1])/width;
                return values[i-1]+(values[i]-values[i-1])*Math.max(0,Math.min(1,f));
            }
            return values[last];
        }
    }
    record Definition(BufferedImage texture,float life,float rate,int maximum,float width,float height,float depth,
                      float x,float y,float z,float minX,float maxX,float minY,float maxY,Curve sizeX,Curve sizeY,
                      Curve alpha,float opacity,float spinMin,float spinMax,Curve spin,float force,float phiMin,float phiMax,
                      float thetaMin,float thetaMax,float damping,boolean ellipse) { }
    private static final Curve ONE=new Curve(new float[]{1},new float[]{0});
    private static float number(JsonValue value,String key,float fallback){return value==null?fallback:value.getFloat(key,fallback);}
    private static float high(JsonValue value,float fallback){return number(value,"highMin",fallback);}
    private static Curve curve(JsonValue node){return node==null?ONE:new Curve(node.get("scaling")==null?new float[]{1}:node.get("scaling").asFloatArray(),node.get("timeline")==null?new float[]{0}:node.get("timeline").asFloatArray());}

    static List<Definition> load(Path rom,int type) throws IOException {
        var root=new JsonReader().parse(Files.readString(ClientFiles.data("particles/summary/type_"+type+".vfx")));
        List<Definition> definitions=new ArrayList<>();
        for(var controller:root.get("resource").get("controllers")){
            int id=controller.getInt("aps",-1);if(id<0)continue;
            List<BufferedImage> textures;
            try {textures=ParticleTextures.load(rom,id);}catch(IOException|RuntimeException error){continue;}
            if(textures.isEmpty())continue;
            JsonValue shape=null,sx=null,sy=null,alpha=null,spin=null,force=null;float damping=0;
            for(var influence:controller.get("influencers")){
                String name=influence.getString("class","");
                if(name.endsWith("SpawnInfluencerExt"))shape=influence.get("spawnShape");
                if(name.endsWith("ScaleXYInfluencer")){sx=influence.get("valuex");sy=influence.get("valuey");}
                if(name.endsWith("ColorInfluencer$Single"))alpha=influence.get("alpha");
                if(name.endsWith("DynamicsInfluencerExt")){
                    damping=number(influence.get("damp"),"value",0);
                    for(var velocity:influence.get("velocities")){
                        String kind=velocity.getString("class","");
                        if(kind.endsWith("Rotational2D"))spin=velocity;
                        if(kind.endsWith("PolarAcceleration"))force=velocity;
                    }
                }
            }
            var emitter=controller.get("emitter");var range=controller.get("aps_texture_range");
            int texture=Math.max(0,Math.min(textures.size()-1,(int)number(range,"lowMin",0)));
            JsonValue rotation=spin==null?null:spin.get("strengthValue"),acceleration=force==null?null:force.get("strengthValue");
            float life=Math.max(.05f,Math.min(20,high(emitter.get("life"),1500)/1000));
            definitions.add(new Definition(textures.get(texture),life,Math.max(0,Math.min(150,high(emitter.get("emission"),20))),
                    Math.min(300,emitter.getInt("maxParticleCount",60)),
                    shape==null?2:high(shape.get("spawnWidthValue"),2),shape==null?2:high(shape.get("spawnHeightValue"),2),shape==null?0:high(shape.get("spawnDepthValue"),0),
                    shape==null?0:number(shape.get("xOffsetValue"),"lowMin",0),shape==null?0:number(shape.get("yOffsetValue"),"lowMin",0),shape==null?0:number(shape.get("zOffsetValue"),"lowMin",0),
                    high(sx,.3f),number(sx,"highMax",.3f),high(sy,.3f),number(sy,"highMax",.3f),curve(sx),curve(sy),curve(alpha),high(alpha,1),
                    high(rotation,0),number(rotation,"highMax",0),curve(rotation),high(acceleration,0),
                    force==null?0:high(force.get("phiValue"),0),force==null?0:number(force.get("phiValue"),"highMax",0),
                    force==null?0:high(force.get("thetaValue"),0),force==null?0:number(force.get("thetaValue"),"highMax",0),damping,
                    shape!=null&&shape.getString("class","").contains("Ellipse")));
        }
        return List.copyOf(definitions);
    }

    private static final class Particle {
        float x,y,z,vx,vy,vz,age,sizeX,sizeY,spin,angle,ax,ay,az;
    }
    private static final class Layer {
        final Definition definition;final Image[] alphaImages=new Image[33];final List<Particle> particles=new ArrayList<>();
        final Random random;float emitted;
        Layer(Definition d,Image image,int seed){definition=d;random=new Random(seed);for(int i=0;i<33;i++)alphaImages[i]=image.createTintedVersion(new Color((byte)255,(byte)255,(byte)255,(byte)(i*255/32)));}
        float between(float a,float b){return a+random.nextFloat()*(b-a);}
        void spawn(){
            var d=definition;var p=new Particle();
            p.x=between(-d.width()/2,d.width()/2)+d.x();p.y=between(-d.height()/2,d.height()/2)+d.y();p.z=between(-d.depth()/2,d.depth()/2)+d.z();
            if(d.ellipse()){double angle=between(0,(float)Math.PI*2);p.x=d.x()+(float)Math.cos(angle)*d.width()/2;p.y=d.y()+(float)Math.sin(angle)*d.height()/2;}
            p.sizeX=between(d.minX(),d.maxX());p.sizeY=between(d.minY(),d.maxY());p.spin=between(d.spinMin(),d.spinMax());
            double phi=Math.toRadians(between(d.phiMin(),d.phiMax())),theta=Math.toRadians(between(d.thetaMin(),d.thetaMax()));
            p.ax=(float)(d.force()*Math.cos(theta)*Math.sin(phi));p.ay=(float)(d.force()*Math.cos(phi));p.az=(float)(d.force()*Math.sin(theta)*Math.sin(phi));particles.add(p);
        }
        void step(float dt){
            var d=definition;particles.removeIf(p->p.age+dt>=d.life());
            for(var p:particles){p.age+=dt;float damping=Math.max(0,1-d.damping()*dt);p.vx=(p.vx+p.ax*dt)*damping;p.vy=(p.vy+p.ay*dt)*damping;p.vz=(p.vz+p.az*dt)*damping;p.x+=p.vx*dt;p.y+=p.vy*dt;p.z+=p.vz*dt;p.angle+=p.spin*d.spin().value(p.age/d.life())*dt;}
            emitted+=d.rate()*dt;while(emitted>=1){if(particles.size()<d.maximum())spawn();emitted-=1;}
        }
    }
    private final List<Layer> layers=new ArrayList<>();private long last;private double accumulator;
    SummaryParticles(List<Definition> definitions,List<Image> images){
        for(int i=0;i<definitions.size();i++)layers.add(new Layer(definitions.get(i),images.get(i),6257+i*137));
        for(int i=0;i<120;i++)for(var layer:layers)layer.step(1f/60);
        last=System.nanoTime();
    }
    void draw(Widget target,long now){
        accumulator+=Math.min(.1,Math.max(0,(now-last)/1_000_000_000.0));last=now;
        while(accumulator>=1.0/60){for(var layer:layers)layer.step(1f/60);accumulator-=1.0/60;}
        float focal=(float)(target.getHeight()/(2*Math.tan(Math.toRadians(67)/2)));
        for(var layer:layers)for(var p:layer.particles){
            var d=layer.definition;float depth=2-p.z;if(depth<.1f)continue;float scale=focal/depth,age=p.age/d.life();
            int opacity=Math.max(0,Math.min(32,Math.round(d.opacity()*d.alpha().value(age)*32)));
            if(opacity==0)continue;
            int width=Math.max(1,Math.round(p.sizeX*d.sizeX().value(age)*scale)),height=Math.max(1,Math.round(p.sizeY*d.sizeY().value(age)*scale));
            float x=target.getX()+target.getWidth()/2f+p.x*scale,y=target.getY()+target.getHeight()/2f-p.y*scale;
            glPushMatrix();glTranslatef(x,y,0);glRotatef(-p.angle,0,0,1);layer.alphaImages[opacity].draw(target.getAnimationState(),-width/2,-height/2,width,height);glPopMatrix();
        }
    }
}
