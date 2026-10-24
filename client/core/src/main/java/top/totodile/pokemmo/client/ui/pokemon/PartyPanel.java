package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.Widget;
import static top.totodile.pokemmo.server.game.PokemonData.*;

/** PartyManagementWindow's six slots; the overworld strip is anchored at the right centre. */
public final class PartyPanel extends Widget {
    final PokemonSlot[] slots=new PokemonSlot[PARTY_SIZE];
    private final boolean storage,mobile;
    PartyPanel(PokemonController controller,boolean storage){
        this.storage=storage;mobile=controller.mobile;setTheme("");
        for(int i=0;i<PARTY_SIZE;i++){slots[i]=controller.slot(PARTY,i,storage);add(slots[i]);}
    }
    @Override public int getPreferredWidth(){return mobile?(storage?80:100):storage?54:46;}
    @Override public int getPreferredHeight(){return PARTY_SIZE*((mobile?(storage?80:100):storage?50:40)+(storage?3:5))-(storage?3:5);}
    @Override protected void layout(){
        int gap=storage?3:5,h=Math.max(0,Math.min(mobile?(storage?80:100):storage?50:40,(getInnerHeight()-gap*5)/6));
        for(int i=0;i<PARTY_SIZE;i++){var s=slots[i];s.setSize(getInnerWidth(),h);s.setPosition(getInnerX(),getInnerY()+i*(h+gap));}
    }
}
