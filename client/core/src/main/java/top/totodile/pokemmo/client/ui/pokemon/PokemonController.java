package top.totodile.pokemmo.client.ui.pokemon;

import com.badlogic.gdx.Gdx;
import de.matthiasmann.twl.*;
import java.io.IOException;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.CompletionStage;
import top.totodile.pokemmo.client.resources.*;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.rom.nds.bw.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.server.game.*;
import top.totodile.pokemmo.server.login.LoginService.Selection;
import static top.totodile.pokemmo.server.game.PokemonData.*;

/** Character-bound party and PC state, matching ChangePokemonPosPacket's authoritative updates. */
public final class PokemonController implements AutoCloseable {
    final ClientResources resources;
    final ClientStrings strings;
    final boolean mobile;
    private final GdxTwlRenderer renderer;
    private final GameService game;
    private final Consumer<String> error;
    private final IntConsumer codex;
    private final List<PokemonSlot> slots=new ArrayList<>();
    private Selection selection;
    private String character;
    private long generation;
    private boolean disposed,busy;
    private Containers state=Containers.empty();
    private BwPokedex dex;
    private PokedexImages images;
    private PokemonSlot dragged;
    private String draggedId;
    private int downX,downY;
    private int dragX,dragY;
    private boolean dragging;
    private PopupWindow menu;
    private Runnable changed=()->{};
    private record BoxDrop(Widget widget,int box) {}
    private final List<BoxDrop> boxDrops=new ArrayList<>();
    void boxDrop(Widget widget,int box){boxDrops.add(new BoxDrop(widget,box));}
    public PokemonController(ClientResources resources,GdxTwlRenderer renderer,boolean mobile,GameService game,Consumer<String> error,IntConsumer codex){
        this.resources=resources;strings=resources.strings();this.renderer=renderer;this.mobile=mobile;this.game=game;this.error=error;this.codex=codex;
    }
    public void onChanged(Runnable changed){var previous=this.changed;this.changed=()->{previous.run();changed.run();};}
    public void bind(Selection selection,String character){
        generation++;cancelDrag();closeMenu();this.selection=selection;this.character=character;busy=false;state=Containers.empty();refreshSlots();changed.run();
        if(character!=null&&selection!=null&&game!=null)reload();
    }
    public Containers state(){return state;}
    public boolean busy(){return busy;}
    boolean ready(){
        if(dex!=null)return true;
        try {dex=new BwPokedex(resources.roms().get(RomType.BLACK_WHITE),strings);dex.loadDataPackage(resources.apply("data.pak"));images=new PokedexImages(new BwPokemonSprites(dex),renderer,resources);return true;}
        catch(IOException|RuntimeException failure){dex=null;Gdx.app.error("pokemon","Cannot load Pokemon ROM resources",failure);return false;}
    }
    BwPokedex dex(){return ready()?dex:null;}
    PokedexImages images(){return ready()?images:null;}
    String name(Pokemon p){String species=ready()?dex.name(p.species()):"#"+p.species();return p.egg()?strings.format(1893,species):p.name().isEmpty()?species:p.name();}
    PokemonSlot slot(int container,int position,boolean storage){var slot=new PokemonSlot(this,container,position,storage);slots.add(slot);return slot;}
    public PartyPanel party(boolean storage){return new PartyPanel(this,storage);}
    public void removeParty(PartyPanel party){slots.removeAll(Arrays.asList(party.slots));}
    public void reload(){if(game!=null&&character!=null)request(game.pokemon(selection,character));}
    private void request(CompletionStage<Containers> operation){
        long version=generation;busy=true;refreshSlots();
        operation.whenComplete((value,failure)->Gdx.app.postRunnable(()->{
            if(disposed||version!=generation)return;busy=false;
            if(failure==null)state=value;
            else {
                Throwable cause=failure;while(cause.getCause()!=null)cause=cause.getCause();
                Gdx.app.error("pokemon","Pokemon container request failed",failure);
                // Reference rejects failed moves and restores the original containers.
                if(!(cause instanceof PokemonData.Rejected))error.accept(strings.get(2312));
            }
            refreshSlots();changed.run();
        }));
    }
    private void refreshSlots(){for(var slot:slots)slot.refresh();}
    void move(PokemonSlot from,int container,int position){
        if(busy||game==null||character==null||from.pokemon()==null||from.container==container&&from.position==position)return;
        closeMenu();request(game.movePokemon(selection,character,List.of(new Move(from.container,from.position,container,position))));
    }
    void sort(int box){if(!busy&&game!=null&&character!=null)request(game.sortPokemon(selection,character,box));}
    void menu(PokemonSlot slot,boolean pc){
        var p=slot.pokemon();if(p==null||busy)return;closeMenu();
        var actions=new ArrayList<Button>();
        // Summary / field moves / following require systems not supplied by this container implementation.
        add(actions,1801,null);
        add(actions,1729,()->codex.accept(p.species()));
        if(pc){
            if(slot.container==PC)add(actions,2301,()->{
                int target=firstEmpty(PARTY,0,PARTY_SIZE);if(target<0)error.accept(strings.get(2305));else move(slot,PARTY,target);
            });
            else add(actions,2301,()->moveToBox(slot));
        }else{
            if(slot.position>0)add(actions,2303,()->move(slot,PARTY,slot.position-1));
            if(slot.position+1<PARTY_SIZE&&state.at(PARTY,slot.position+1)!=null)add(actions,2304,()->move(slot,PARTY,slot.position+1));
            if(slot.position>0)add(actions,2313,()->move(slot,PARTY,0));
        }
        add(actions,53,()->{});
        var popup=new PopupWindow(slot);popup.setTheme("/pokemon-menu");
        var content=new Widget(){@Override protected void layout(){int y=getInnerY();for(var b:actions){b.setPosition(getInnerX(),y);b.setSize(getInnerWidth(),mobile?40:26);y+=b.getHeight();}}};
        content.setTheme("");for(var b:actions)content.add(b);popup.add(content);menu=popup;
        if(popup.openPopup()){
            int w=mobile?240:190,h=actions.size()*(mobile?40:26)+popup.getBorderVertical();popup.setSize(w,h);
            popup.setPosition(Math.max(0,slot.getX()-w),Math.max(0,Math.min(slot.getY(),slot.getGUI().getHeight()-h)));
        }
    }
    private void add(List<Button> list,int text,Runnable callback){var b=new Button(strings.get(text));b.setTheme("/pokemon-menu-button");b.setCanAcceptKeyboardFocus(false);b.setEnabled(callback!=null);if(callback!=null)b.addCallback(()->{closeMenu();callback.run();});list.add(b);}
    private void moveToBox(PokemonSlot slot){
        int box=storageBox.getAsInt(),target=firstEmpty(PC,box*BOX_SIZE,BOX_SIZE);
        if(target<0)error.accept(strings.format(2306,boxName(box)));else move(slot,PC,target);
    }
    private IntSupplier storageBox=()->0;
    void storageBox(IntSupplier box){storageBox=box;}
    int firstEmpty(int container,int first,int amount){for(int i=first;i<first+amount;i++)if(state.at(container,i)==null)return i;return -1;}
    String boxName(int box){String custom=resources.preferences().get("client.pc."+character+".box."+box,"");return custom.isEmpty()?strings.format(1119,Integer.toString(box+1)):custom;}
    void rename(int box,String value){
        if(character==null)return;
        if(!value.matches("[ |\\p{L}|\\p{N}|\\p{P}]{1,20}"))value="";
        resources.preferences().save(Map.of("client.pc."+character+".box."+box,value));changed.run();
    }
    public boolean closeMenu(){if(menu==null)return false;boolean open=menu.isOpen();menu.closePopup();menu=null;return open;}
    public boolean hasMenu(){return menu!=null&&menu.isOpen();}
    public boolean touchDown(int x,int y,int button){
        if(button!=0||busy)return false;
        var slot=hit(x,y);if(slot==null||slot.pokemon()==null)return false;
        dragged=slot;draggedId=slot.pokemon().id();downX=x;downY=y;dragging=false;return false;
    }
    public boolean touchDragged(int x,int y){
        if(dragged==null)return false;
        dragX=x;dragY=y;
        if(Math.abs(x-downX)+Math.abs(y-downY)>6){dragging=true;dragged.getAnimationState().setAnimationState(Button.STATE_PRESSED,true);}
        return dragging;
    }
    public boolean hasDragCandidate(){return dragged!=null;}
    public boolean touchUp(int x,int y){
        if(dragged==null)return false;var from=dragged;boolean moved=dragging;var to=hit(x,y);
        if(moved&&from.pokemon()!=null&&from.pokemon().id().equals(draggedId)&&to!=null&&to!=from)move(from,to.container,to.position);
        else if(moved&&from.pokemon()!=null&&from.pokemon().id().equals(draggedId)){
            for(var target:boxDrops)if(target.widget.getGUI()!=null&&target.widget.isInside(x,y)){
                int empty=firstEmpty(PC,target.box*BOX_SIZE,BOX_SIZE);
                if(empty>=0)move(from,PC,empty);else error.accept(strings.format(2306,boxName(target.box)));break;
            }
        }
        cancelDrag();return moved;
    }
    public void cancelDrag(){if(dragged!=null)dragged.getAnimationState().setAnimationState(Button.STATE_PRESSED,false);dragged=null;draggedId=null;dragging=false;}
    public void paintDrag(de.matthiasmann.twl.renderer.AnimationState animation){
        if(!dragging||dragged==null||dragged.pokemon()==null||images()==null)return;
        var p=dragged.pokemon();int size=mobile?72:36;images.icon(p.species(),p.form(),p.gender(),p.egg()).draw(animation,dragX-size/2,dragY-size/2,size,size);
    }
    private PokemonSlot hit(int x,int y){
        for(int i=slots.size()-1;i>=0;i--){var slot=slots.get(i);if(slot.getGUI()!=null&&slot.isVisible()&&slot.isInside(x,y)){
            Widget top=slot.getGUI().getWidgetAt(x,y);while(top!=null&&!(top instanceof PokemonSlot))top=top.getParent();
            if(top==slot)return slot;
        }}return null;
    }
    @Override public void close(){disposed=true;generation++;cancelDrag();closeMenu();if(images!=null)images.close();}
}
