package top.totodile.pokemmo.client.ui.pokemon;

import com.badlogic.gdx.graphics.Texture;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.nds.bw.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.common.*;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.client.ui.character.*;
import top.totodile.pokemmo.server.game.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/** GdxSoundManager (ys_2): 1s intro, 4s/member, .5s transition, 10s party finale.
 * Uses its native BW sprite fallback and the same hof/bg textures, with no extra controls. */
public final class HallOfFameWindow extends ClientDialog implements AutoCloseable {
    private final ClientResources resources;
    private final BwPokedex dex;
    private final List<PokemonData.Pokemon> party;
    private final GameService.Character trainer;
    private final boolean mobile;
    private final ActionConfirmDialog confirmation;
    private final Label name=new Label(),species=new Label(),level=new Label(),ot=new Label(),history=new Label();
    private final Widget scene=new Widget(){
        @Override protected void paintWidget(GUI gui){paintScene((GdxTwlRenderer)gui.getRenderer());}
    };
    private final List<Texture> textures=new ArrayList<>();
    private final List<Image> panorama=new ArrayList<>();
    private PokedexImages images;
    private Image hof,white;
    private CharacterAppearance appearance;
    private CharacterPreview portrait;
    private AttributeParticleScene particles,trail;
    private int particleStage=-3;
    private final com.badlogic.gdx.graphics.PerspectiveCamera camera=new com.badlogic.gdx.graphics.PerspectiveCamera(67,1,1);
    private long started,pausedAt,pausedNanos;
    private Runnable completed;
    private int showing=-2,cried=-1;
    private String historyText="";
    private boolean dumped;
    private boolean finished;

    public HallOfFameWindow(Widget owner,boolean mobile,ClientResources resources,BwPokedex dex,
            GameService.Character trainer,List<PokemonData.Pokemon> party){
        super(owner,mobile);this.resources=resources;this.dex=dex;this.trainer=trainer;this.mobile=mobile;this.party=List.copyOf(party);
        if(party.isEmpty())throw new IllegalArgumentException("Hall of Fame requires a party");
        setTheme("/hall-of-fame");setCloseOnEscape(false);setCloseOnClickedOutside(false);
        scene.setTheme("");add(scene);
        for(var label:List.of(name,species,level,ot,history)){label.setTheme("label");add(label);}
        confirmation=new ActionConfirmDialog(owner,resources.strings(),mobile,image->{},()->{
            if(pausedAt!=0){pausedNanos+=System.nanoTime()-pausedAt;pausedAt=0;}
        });
    }

    public void open(Runnable completed){
        this.completed=completed;started=System.nanoTime();finished=false;openPopup();position();
        if(resources.audio()!=null)resources.audio().music(new top.totodile.pokemmo.client.audio.RomAudio.Track(2,1016));
    }
    private double seconds(){return ((pausedAt==0?System.nanoTime():pausedAt)-started-pausedNanos)/1e9;}
    private double finaleAt(){return 1+4*party.size()+.5;}
    public void position(){setPosition(getOwner().getInnerX(),getOwner().getInnerY());setSize(getOwner().getInnerWidth(),getOwner().getInnerHeight());invalidateLayout();}
    public void update(){
        if(!isOpen()||finished)return;position();
        if(confirmation.isOpen()){confirmation.positionWindow();return;}
        double time=seconds();
        int index=time<1?-2:time<finaleAt()?Math.min(party.size()-1,(int)((time-1)/4)):-1;
        if(index!=showing){showing=index;setLabels(index);}
        if(index>=0&&index!=cried){cried=index;var p=party.get(index);if(resources.audio()!=null)resources.audio().cry(this,p.species(),p.form(),0,0);}
        if(time>=finaleAt()+10)finish();
    }
    private void setLabels(int index){
        for(var label:List.of(name,species,level,ot,history))label.setVisible(index>=0);
        if(portrait!=null)portrait.setVisible(index==-1);
        if(index==-1){name.setVisible(true);name.setText(trainer.name());return;}
        if(index<0)return;
        var p=party.get(index);var strings=resources.strings();var origin=p.origin();
        name.setText(p.name().isEmpty()?dex.name(p.species()):p.name());species.setText(dex.name(p.species()));
        level.setText(strings.get(59)+" "+p.level());
        ot.setText(strings.get(1807)+" "+(origin.trainer().isEmpty()?trainer.name():origin.trainer()));
        String action=strings.get(origin.level()==-1?1873:origin.address()==-1?1874:1872);
        String metLevel=origin.level()>0?strings.format(1875,Integer.toString(origin.level())):"";
        int code=origin.region()==0?139912+(origin.address()&255):140000+origin.region()*1000+(origin.address()&255);
        String place=origin.address()==-1?strings.get(1876):strings.format(1877,strings.getOrDefault(code,dex.regionName(origin.region())));
        String date=origin.time()>0?strings.format(1878,DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a").format(Instant.ofEpochSecond(origin.time()).atZone(ZoneId.systemDefault()))):"";
        historyText=strings.get(1870).contains("{05}")?strings.format(1870,"",action,metLevel,place,"",date,""):strings.format(1870,"",action,metLevel,place,date);
        history.setText(historyText);
    }
    private void skip(){if(!finished&&!confirmation.isOpen()){pausedAt=System.nanoTime();confirmation.show(resources.strings().get(6150),this::finish);}}
    public boolean key(int key){
        if(!isOpen())return false;if(confirmation.isOpen())return false;
        if(key==Event.KEY_RETURN||key==Event.KEY_Z||key==Event.KEY_X||key==Event.KEY_ESCAPE)skip();
        return true;
    }
    @Override protected boolean handleEventPopup(Event event){
        if(confirmation.isOpen())return super.handleEventPopup(event);
        if(event.isMouseEvent()&&event.getType()==Event.Type.MOUSE_BTNUP&&event.getMouseButton()==0){skip();return true;}
        if(event.isKeyPressedEvent())return key(event.getKeyCode());
        return true;
    }
    private void finish(){
        if(finished)return;finished=true;confirmation.closePopup();closePopup();
        if(resources.audio()!=null)resources.audio().stop(this);
        var callback=completed;completed=null;if(callback!=null)callback.run();
    }
    @Override protected void layout(){
        scene.setPosition(getInnerX(),getInnerY());scene.setSize(getInnerWidth(),getInnerHeight());
        int x=getInnerX()+Math.max(16,getInnerWidth()/16),y=getInnerY()+getInnerHeight()/4;
        int width=getInnerWidth()*5/12;
        if(history.getFont()!=null)history.setText(wrap(historyText,width));
        for(var label:List.of(name,species,level,ot,history)){label.setPosition(x,y);label.setSize(width,label==history?140:40);y+=label==ot?56:40;}
        if(showing==-1){name.setPosition(getInnerX(),getInnerY()+getInnerHeight()/8);name.setSize(getInnerWidth(),40);name.setAlignment(Alignment.CENTER);}
        else name.setAlignment(Alignment.LEFT);
        if(portrait!=null){int size=(mobile?4:6)*80;portrait.setPosition(getInnerX()+(getInnerWidth()-size)/2,getInnerY()+getInnerHeight()/4-40);portrait.setSize(size,size);}
    }
    private String wrap(String text,int width){
        var out=new StringBuilder();var line=new StringBuilder();
        for(int i=0;i<text.length();){int code=text.codePointAt(i);i+=Character.charCount(code);
            if(code=='\n'){out.append(line).append('\n');line.setLength(0);continue;}
            String next=line.toString()+new String(Character.toChars(code));
            if(!line.isEmpty()&&history.getFont().computeTextWidth(next)>width){out.append(line).append('\n');line.setLength(0);}
            line.appendCodePoint(code);
        }
        return out.append(line).toString();
    }
    private void load(GdxTwlRenderer renderer){
        if(images!=null)return;
        try{images=new PokedexImages(new BwPokemonSprites(dex),renderer,resources);}catch(java.io.IOException error){throw new java.io.UncheckedIOException(error);}images.reserveAnimations(6);
        Texture texture=new Texture(resources.apply("sprites/textures/hof.png"));textures.add(texture);hof=renderer.image(texture);
        for(int i=0;i<100;i++){
            var file=resources.apply(String.format(java.util.Locale.ROOT,"sprites/textures/bg_%02d.png",i));if(!file.exists())break;
            texture=new Texture(file);textures.add(texture);panorama.add(renderer.image(texture));
        }
        if(panorama.isEmpty())throw new IllegalStateException("Missing reference Hall of Fame panorama");
        var pix=new com.badlogic.gdx.graphics.Pixmap(1,1,com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        try{pix.setColor(1,1,1,1);pix.fill();texture=new Texture(pix);}finally{pix.dispose();}
        textures.add(texture);white=renderer.image(texture);
        var catalog=resources.newAppearance();try{appearance=catalog.restore(trainer.appearance());}finally{catalog.dispose();}
        portrait=new CharacterPreview(appearance,renderer,false,()->trainer.appearance().skin(),()->trainer.appearance().gender(),System::currentTimeMillis);
        portrait.portrait(mobile?4:6);add(portrait);portrait.setVisible(showing==-1);invalidateLayout();
    }
    private void paintScene(GdxTwlRenderer renderer){
        load(renderer);double time=seconds();int x=getInnerX(),y=getInnerY(),w=getInnerWidth(),h=getInnerHeight();
        white.createTintedVersion(Color.BLACK).draw(getAnimationState(),x,y,w,h);
        if(time<finaleAt()){
            Color color=showing>=0?PokemonSummaryWindow.typeColor(dex.backgroundType(party.get(showing).species())):new Color(0xff808080);
            float alpha=(float)Math.min(1,time);if(time>finaleAt()-.5)alpha=(float)((finaleAt()-time)*2);
            renderer.pushGlobalTintColor(color.getRedFloat(),color.getGreenFloat(),color.getBlueFloat(),alpha);
            hof.draw(getAnimationState(),x,y,w,h);renderer.popGlobalTintColor();
            if(!dumped&&time>2){dumped=true;renderer.renderOffscreen(()->{
                var pixels=com.badlogic.gdx.utils.ScreenUtils.getFrameBufferPixmap(0,0,w,h);
                try{com.badlogic.gdx.graphics.PixmapIO.writePNG(com.badlogic.gdx.Gdx.files.local("build/story-diagnostics/hall-background-debug.png"),pixels,-1,true);}finally{pixels.dispose();}
            });}
            if(showing>=0){drawParticles(renderer,x+w/2,y,w/2,h);double local=(time-1)%4;drawMember(renderer,party.get(showing),x+w*3/4,y+h/2,Math.min(w/3,h/2),time,(float)Math.min(1,local*2));}
        }else{
            int total=0;for(var image:panorama)total+=Math.max(1,(int)((float)h/image.getHeight()*image.getWidth()));
            int offset=-(int)((time-finaleAt())/0.022)%total;
            for(int at=offset;at<w;){for(var image:panorama){int width=Math.max(1,(int)((float)h/image.getHeight()*image.getWidth()));image.draw(getAnimationState(),x+at,y,width,h);at+=width;}}
            drawParticles(renderer,x,y,w,h);
            int size=Math.min(w/(party.size()+1),h/3);
            for(int i=0;i<party.size();i++){
                float arrival=(float)Math.min(1,Math.max(0,(time-finaleAt()-i*.1)/.1));
                drawMember(renderer,party.get(i),x+(i+1)*w/(party.size()+1),y+h*4/5-(int)((1-arrival)*h),size,time,arrival);
            }
            float fade=(float)Math.max(0,(time-finaleAt()-9.5)*2);
            if(fade>0){renderer.pushGlobalTintColor(1,1,1,fade);white.draw(getAnimationState(),x,y,w,h);renderer.popGlobalTintColor();}
        }
    }
    private AttributeParticleScene effect(String name){
        try{return new AttributeParticleScene(images,new com.badlogic.gdx.utils.JsonReader().parse(new String(
                top.totodile.pokemmo.client.resources.ParticleResources.read(resources,"particle/special/"+name+".vfx"),java.nio.charset.StandardCharsets.UTF_8)),new com.badlogic.gdx.math.Vector3());}
        catch(java.io.IOException error){throw new java.io.UncheckedIOException(error);}
    }
    private void drawParticles(GdxTwlRenderer renderer,int x,int y,int w,int h){
        if(particleStage!=showing){
            if(particles!=null)particles.close();if(trail!=null){trail.close();trail=null;}
            particleStage=showing;
            if(showing==-1)particles=effect("hof");
            else{int type=dex.backgroundType(party.get(showing).species());particles=effect("type_"+(type>=9?type+1:type));trail=effect("hof_party_trail");}
        }
        camera.viewportWidth=w;camera.viewportHeight=h;camera.position.set(0,-1,-3.25f);camera.up.set(0,1,0);camera.lookAt(0,.5f,1);camera.near=.1f;camera.far=100;camera.update();
        float dt=confirmation.isOpen()?0:Math.min(.05f,com.badlogic.gdx.Gdx.graphics.getDeltaTime());
        particles.draw(renderer,dt,x,y,w,h,camera);if(trail!=null)trail.draw(renderer,dt,x,y,w,h,camera);
    }
    private void drawMember(GdxTwlRenderer renderer,PokemonData.Pokemon p,int cx,int cy,int size,double time,float alpha){
        Image image=images.animatedFront(p.species(),p.form(),p.shiny(),time);
        // The reference sprite fallback uses the 96px monster cell, not the transparent 192px FBO.
        float scale=(float)size/96;int w=Math.round(image.getWidth()*scale),h=Math.round(image.getHeight()*scale);
        renderer.pushGlobalTintColor(1,1,1,alpha);image.draw(getAnimationState(),cx-w/2,cy-h/2,w,h);renderer.popGlobalTintColor();
    }
    @Override public void close(){completed=null;finished=true;confirmation.closePopup();closePopup();if(resources.audio()!=null)resources.audio().stop(this);if(particles!=null)particles.close();if(trail!=null)trail.close();if(images!=null)images.close();if(portrait!=null)portrait.close();if(appearance!=null)appearance.dispose();textures.forEach(Texture::dispose);textures.clear();}
}
