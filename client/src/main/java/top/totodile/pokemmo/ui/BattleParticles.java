package top.totodile.pokemmo.ui;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.battle.MoveAnimations;
import top.totodile.pokemmo.rom.ParticleTextures;
import top.totodile.pokemmo.rom.BattleScene;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import static org.lwjgl.opengl.GL11.*;

/** 招式粒子按控制器的发射时间和寿命播放，离开战斗不保留 GPU 资源。 */
final class BattleParticles {
    record Definition(MoveAnimations.Effect effect, SummaryParticles.Definition particle,
                      float delay, float duration, int minimum, float[] colors, float[] colorTimes,
                      float velocity, float velocityPhi, float velocityTheta,
                      OriginalBattleParticles.Definition original) {
        Definition(MoveAnimations.Effect effect, SummaryParticles.Definition particle, float delay,
                   float duration, int minimum, float[] colors, float[] colorTimes,
                   float velocity, float velocityPhi, float velocityTheta) {
            this(effect, particle, delay, duration, minimum, colors, colorTimes, velocity, velocityPhi, velocityTheta, null);
        }
        Definition(MoveAnimations.Effect effect, OriginalBattleParticles.Definition original) {
            this(effect, null, 0, 0, 0, null, null, 0, 0, 0, original);
        }
        List<BufferedImage> textures() { return original == null ? List.of(particle.texture()) : original.textures(); }
    }
    static List<Definition> load(Path rom, int move) throws IOException {
        return OriginalBattleParticles.load(rom, move);
    }
    static List<Definition> load(Path rom, int move, boolean enemy) throws IOException {
        return OriginalBattleParticles.load(rom, MoveAnimations.get(move, enemy ? "enemy" : ""));
    }
    static List<Definition> loadShiny(Path rom) throws IOException {
        // 普通星星闪光按 152 双控制器、422 星光序列播放。
        return loadEffects(rom, List.of(
                new MoveAnimations.Effect(152, 0, 1, 0, 400, .4f),
                new MoveAnimations.Effect(152, 1, 1, 0, 400, .4f),
                new MoveAnimations.Effect(422, 2, 1, 400, 400, .4f),
                new MoveAnimations.Effect(152, 0, 1, 800, 400, .4f),
                new MoveAnimations.Effect(152, 1, 1, 800, 400, .4f),
                new MoveAnimations.Effect(422, 2, 1, 1200, 400, .4f)));
    }
    private static List<Definition> loadEffects(Path rom, List<MoveAnimations.Effect> requested) throws IOException {
        List<Definition> result = new ArrayList<>();
        Map<Integer, List<BufferedImage>> textures = new HashMap<>();
        Map<Integer, JsonValue> effects = new HashMap<>();
        try (var zip = new ZipFile(ClientFiles.data("battle/particles.zip").toFile())) {
            for (var effect : requested) {
                JsonValue resource = effects.get(effect.archive());
                if (resource == null) {
                    String path = "particle/"+MoveAnimations.resource(effect.archive())+".vfx";
                    var entry = zip.getEntry(path);
                    if (entry == null) throw new IOException("Missing battle effect " + path);
                    try (var input = zip.getInputStream(entry)) { resource = new JsonReader().parse(input).get("resource"); }
                    effects.put(effect.archive(),resource);
                }
                var controller = resource.get("controllers").get(effect.controller());
                int aps = controller.getInt("aps");
                boolean custom = controller.getString("type", "").equals("CUSTOM");
                int key = custom ? -aps-1 : aps;
                List<BufferedImage> pixels = textures.get(key);
                if (pixels == null) {
                    if (custom) {
                        var entry = zip.getEntry("sprites/"+aps+".png");
                        if (entry == null) throw new IOException("Missing custom battle texture " + aps);
                        try (var input=zip.getInputStream(entry)) { pixels=List.of(ImageIO.read(input)); }
                    } else pixels = ParticleTextures.load(rom,aps);
                    if (pixels.isEmpty()) throw new IOException("Empty battle texture " + aps);
                    textures.put(key,pixels);
                }
                var particle = SummaryParticles.definition(controller,pixels);
                var emitter = controller.get("emitter");
                float[] colors = {1,1,1}, times = {0};
                float velocity = 0, phi = 0, theta = 0;
                for (var influence : controller.get("influencers")) {
                    if (influence.getString("class", "").endsWith("ColorInfluencer$Single")) {
                        var color = influence.get("color");
                        if (color != null && color.get("colors") != null) {
                            colors = color.get("colors").asFloatArray(); times = color.get("timeline").asFloatArray();
                        }
                    }
                    var velocities = influence.get("velocities");
                    if (velocities != null) for (var v : velocities) if (v.getString("class", "").contains("PolarVelocity")) {
                        velocity = SummaryParticles.high(v.get("strengthValue"),0);
                        phi = SummaryParticles.high(v.get("phiValue"),0); theta=SummaryParticles.high(v.get("thetaValue"),0);
                    }
                }
                result.add(new Definition(effect,particle,SummaryParticles.number(emitter.get("delay"),"lowMax",0)/1000,
                        Math.max(.016f,SummaryParticles.number(emitter.get("duration"),"lowMax",16)/1000),emitter.getInt("minParticleCount",0),colors,times,velocity,phi,theta));
            }
        }
        return List.copyOf(result);
    }
    private record Particle(float born, float x, float y, float z, float sx, float sy, float spin, float phi, float theta) { }
    private final List<Definition> definitions;
    private final List<Image> images;
    private final List<List<Particle>> particles = new ArrayList<>();
    private final List<OriginalBattleParticles> originals = new ArrayList<>();
    BattleParticles(List<Definition> definitions, List<Image> images) {
        this.definitions = definitions; this.images = new ArrayList<>();
        int imageIndex = 0;
        for (int i=0;i<definitions.size();i++) {
            var definition = definitions.get(i);
            if (definition.original() != null) {
                int count = definition.textures().size();
                originals.add(new OriginalBattleParticles(definition.original(), images.subList(imageIndex, imageIndex + count)));
                imageIndex += count;
                this.images.add(null);
                particles.add(List.of());
                continue;
            }
            originals.add(null);
            this.images.add(images.get(imageIndex++));
            var d=definitions.get(i); var p=d.particle(); var random=new Random(5107L+i*137);
            int count=Math.min(600,Math.max(d.minimum(),Math.round(p.rate()*d.duration())));
            var layer=new ArrayList<Particle>();
            for(int n=0;n<count;n++) {
                float x=(random.nextFloat()-.5f)*p.width(),y=(random.nextFloat()-.5f)*p.height(),z=(random.nextFloat()-.5f)*p.depth();
                if(p.ellipse()){double angle=random.nextDouble()*Math.PI*2;x=(float)Math.cos(angle)*p.width()/2;y=(float)Math.sin(angle)*p.height()/2;}
                layer.add(new Particle(d.delay()+(n<d.minimum()?0:(n-d.minimum())/Math.max(1,p.rate())),x+p.x(),y+p.y(),z+p.z(),
                        between(random,p.minX(),p.maxX()),between(random,p.minY(),p.maxY()),between(random,p.spinMin(),p.spinMax()),between(random,p.phiMin(),p.phiMax()),between(random,p.thetaMin(),p.thetaMax())));
            }
            particles.add(List.copyOf(layer));
        }
    }
    private static float between(Random random,float a,float b){return a+random.nextFloat()*(b-a);}
    void draw(Widget target, long milliseconds, float ownX, float ownY, float enemyX, float enemyY, float scale) {
        for (int i=0;i<definitions.size();i++) {
            if (originals.get(i) != null) continue;
            var d=definitions.get(i);var p=d.particle();
            float elapsed=(milliseconds-d.effect().start())/1000f;
            boolean opponent=d.effect().target()==1||d.effect().target()==2||d.effect().target()==3||d.effect().target()==11;
            float cx=opponent?enemyX:ownX,cy=(opponent?enemyY:ownY)-(d.effect().adjustment()-.5f)*70*scale;
            for (var particle:particles.get(i)) {
                float age=elapsed-particle.born(); if(age<0||age>=p.life())continue;
                float progress=age/p.life(),alpha=Math.clamp(p.opacity()*p.alpha().value(progress),0,1);
                if(alpha<=0)continue;
                double phi=Math.toRadians(particle.phi()),theta=Math.toRadians(particle.theta());
                double vp=Math.toRadians(d.velocityPhi()),vt=Math.toRadians(d.velocityTheta());
                float dx=particle.x()+(float)(d.velocity()*Math.sin(vp)*Math.cos(vt)*age+p.force()*Math.sin(phi)*Math.cos(theta)*age*age/2);
                float dy=particle.y()+(float)(d.velocity()*Math.cos(vp)*age+p.force()*Math.cos(phi)*age*age/2);
                float perspective=70*scale/Math.max(.5f,1-particle.z()*.1f);
                int width=Math.max(1,Math.round(particle.sx()*p.sizeX().value(progress)*perspective)),height=Math.max(1,Math.round(particle.sy()*p.sizeY().value(progress)*perspective));
                width=Math.min(width,Math.round(760*scale));height=Math.min(height,Math.round(540*scale));
                float[] tint=color(d,progress);
                var renderer=target.getGUI().getRenderer();renderer.pushGlobalTintColor(tint[0],tint[1],tint[2],alpha);
                glPushMatrix();
                try {
                    glTranslatef(cx+dx*perspective,cy-dy*perspective,0);glRotatef(-particle.spin()*p.spin().value(progress)*age,0,0,1);
                    images.get(i).draw(target.getAnimationState(),-width/2,-height/2,width,height);
                } finally {glPopMatrix();renderer.popGlobalTintColor();}
            }
        }
    }
    void drawOriginal(Widget widget, long milliseconds, BattleScene scene, boolean reverse,
                      int x, int y, int width, int height) {
        for (var original : originals) if (original != null)
            original.draw(widget, milliseconds, scene, reverse, x, y, width, height);
    }

    int originalDrawnParticles() {
        return originals.stream().filter(Objects::nonNull).mapToInt(OriginalBattleParticles::drawnParticles).sum();
    }
    void close() {
        for (var original : originals) if (original != null) original.close();
    }
    private static float[] color(Definition d,float age) {
        int last=Math.min(d.colorTimes().length,d.colors().length/3)-1;
        if(last<0)return new float[]{1,1,1};
        int index=0;while(index<last&&age>d.colorTimes()[index+1])index++;
        int next=Math.min(last,index+1);float length=d.colorTimes()[next]-d.colorTimes()[index];
        float f=length<=0?0:Math.clamp((age-d.colorTimes()[index])/length,0,1);
        float[] color=new float[3];for(int c=0;c<3;c++)color[c]=d.colors()[index*3+c]+(d.colors()[next*3+c]-d.colors()[index*3+c])*f;
        return color;
    }
}
