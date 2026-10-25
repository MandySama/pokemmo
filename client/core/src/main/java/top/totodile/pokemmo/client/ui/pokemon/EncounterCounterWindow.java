package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import java.util.*;
import top.totodile.pokemmo.client.config.EncounterCounterStore;

/** EncounterHUDWidget layout, with the three species rows shown in the user's screenshot.
 * This preview holds generated data only; it does not listen to battle events. */
public final class EncounterCounterWindow extends ResizableFrame {
    private static final int WIDTH=200,ROW_HEIGHT=36;
    private final PokemonController pokemon;
    private final EncounterCounterStore store;
    private final Button toggle=new Button();
    private final Widget rows=new Widget(){@Override protected void layout(){
        for(int i=0;i<getNumChildren();i++){var row=getChild(i);row.setPosition(getInnerX(),getInnerY()+i*ROW_HEIGHT);row.setSize(getInnerWidth(),ROW_HEIGHT);}
    }};
    private final List<Entry> entries=new ArrayList<>();
    private int total,other;
    private boolean expanded=true,positioned,pointerCapture,namesReady;
    private int parentWidth=-1,parentHeight=-1;
    record Entry(int species,int count){}
    private final Set<Integer> pinned=new HashSet<>();
    private Runnable openRecorder=()->{},changed=()->{};
    public void onOpenRecorder(Runnable action){openRecorder=action;}
    void onChanged(Runnable action){changed=action;}
    int total(){return total;}
    List<Entry> records(){return List.copyOf(entries);}
    boolean pinned(int species){return pinned.contains(species);}
    void togglePinned(int species){if(!pinned.remove(species))pinned.add(species);sortRecords();refreshRows();persist();changed.run();}
    void hudVisible(boolean visible){setVisible(visible);if(visible)setHardVisible(true);persist();changed.run();}
    private void sortRecords(){entries.sort(Comparator.comparing((Entry e)->!pinned.contains(e.species)).thenComparing(Comparator.comparingInt(Entry::count).reversed()));}


    public EncounterCounterWindow(PokemonController pokemon){this(pokemon,null);}
    public EncounterCounterWindow(PokemonController pokemon,EncounterCounterStore store){
        this.store=store;
        this.pokemon=pokemon;setTheme("/encounter-counter");setTitle("");
        setResizableAxis(ResizableAxis.NONE);setDraggable(true);setCanAcceptKeyboardFocus(false);
        rows.setTheme("");add(rows);toggle.setTheme("/encounter-counter-minimize");toggle.setCanAcceptKeyboardFocus(false);
        toggle.addCallback(()->{expanded=!expanded;refreshRows();persist();});add(toggle);
        addCloseCallback(()->{pointerCapture=false;hudVisible(false);});
        if(store!=null){var saved=store.load();total=saved.total();expanded=saved.expanded();pinned.addAll(saved.pinned());for(var entry:saved.records())entries.add(new Entry(entry.species(),entry.count()));sortRecords();setVisible(saved.visible());}
        if(!entries.isEmpty())prepareNames();refreshRows();
    }
    public void randomize(){
        var dex=pokemon.dex();if(dex==null)return;
        if(!prepareNames())return;
        var choices=new ArrayList<>(dex.all());Collections.shuffle(choices);
        var random=java.util.concurrent.ThreadLocalRandom.current();entries.clear();pinned.clear();int shown=0;
        for(int i=0;i<Math.min(12,choices.size());i++){int count=random.nextInt(1,1000);entries.add(new Entry(choices.get(i).id(),count));shown+=count;}
        sortRecords();
        other=random.nextInt(1000,200001);total=shown+other;setVisible(true);setHardVisible(true);refreshRows();persist();changed.run();
    }
    private boolean prepareNames(){
        if(namesReady)return true;var dex=pokemon.dex();if(dex==null)return false;
        try{pokemon.strings.registerRomMessages(2,0,70,150000,0,top.totodile.pokemmo.client.rom.nds.bw.BwText.decode(dex.archive("a/0/0/2").readRaw(70)));namesReady=true;return true;}
        catch(java.io.IOException error){com.badlogic.gdx.Gdx.app.error("encounter-counter","Cannot load species names",error);return false;}
    }
    private void persist(){
        if(store!=null)store.save(new EncounterCounterStore.State(total,entries.stream().map(e->new EncounterCounterStore.SpeciesCount(e.species(),e.count())).toList(),pinned,expanded,isVisible()));
    }
    public boolean ownsPointer(int x,int y){return isVisible()&&(pointerCapture||isInside(x,y));}
    public void positionInHud(int x,int y,int width,int height){
        if(!positioned){setPosition(x+Math.max(0,width-WIDTH-12),y+12);positioned=true;}
        if(parentWidth!=width||parentHeight!=height){parentWidth=width;parentHeight=height;clampToHud();}
    }
    private void clampToHud(){
        var parent=getParent();if(parent==null)return;
        setPosition(Math.max(parent.getInnerX(),Math.min(getX(),parent.getInnerRight()-getWidth())),Math.max(parent.getInnerY(),Math.min(getY(),parent.getInnerBottom()-getHeight())));
    }
    private void refreshRows(){
        rows.removeAllChildren();rows.add(new CounterRow(-1,"总计",total));
        if(expanded&&!entries.isEmpty()){
            var dex=pokemon.dex();for(var entry:entries.stream().limit(3).toList())rows.add(new CounterRow(entry.species,dex.name(entry.species),entry.count));
            other=total-entries.stream().limit(3).mapToInt(Entry::count).sum();
            rows.add(new CounterRow(0,"其他",other));
        }
        toggle.setText(expanded?"":"+");toggle.setTheme(expanded?"/encounter-counter-minimize":"/encounter-counter-expand");
        setSize(WIDTH,20+rows.getNumChildren()*ROW_HEIGHT);invalidateLayout();clampToHud();
    }
    @Override protected void layout(){
        super.layout();rows.setPosition(getX()+4,getY()+20);rows.setSize(getWidth()-8,getHeight()-20);
        toggle.setPosition(getRight()-40,getY()+6);toggle.setSize(12,12);
    }
    @Override protected boolean handleEvent(Event event){
        if(event.getType()==Event.Type.MOUSE_BTNDOWN&&event.getMouseButton()==Event.MOUSE_LBUTTON)pointerCapture=true;
        boolean handled=super.handleEvent(event);
        if(event.isMouseDragEnd()||event.getType()==Event.Type.MOUSE_BTNUP){pointerCapture=false;clampToHud();}
        return handled;
    }
    private static Label displayLabel(String text){return new Label(text){@Override protected boolean handleEvent(Event event){return false;}};}
    private final class CounterRow extends Button {
        final int species;final Label name,count;
        CounterRow(int species,String text,int value){
            this.species=species;setTheme("");setCanAcceptKeyboardFocus(false);if(species<0)addCallback(()->openRecorder.run());name=displayLabel(text);name.setTheme("/encounter-counter-name");count=displayLabel(Integer.toString(value));count.setTheme("/encounter-counter-count");add(name);add(count);
        }
        @Override protected void layout(){
            name.setPosition(getX()+38,getY());name.setSize(getWidth()-100,getHeight());
            count.setPosition(getRight()-65,getY());count.setSize(62,getHeight());
        }
        @Override protected void paintWidget(GUI gui){
            var images=pokemon.images();if(images==null)return;
            Image icon=species<0?images.item(5004):images.icon(species);
            if(icon!=null){int size=species<0?24:species==0?36:32;icon.draw(getAnimationState(),getX()+2+(30-size)/2,getY()+(getHeight()-size)/2,size,size);}
        }
    }
}
