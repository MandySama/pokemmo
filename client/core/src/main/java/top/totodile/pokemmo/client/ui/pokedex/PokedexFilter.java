package top.totodile.pokemmo.client.ui.pokedex;

import java.util.Arrays;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokedex;

/** Search state shared by the official basic and advanced catalogue views. */
final class PokedexFilter {
    int region=-1,tier=-1,type1=-1,type2=-1,eggGroup,ability,status,heldItem;
    boolean knownOnly;
    top.totodile.pokemmo.server.game.GameService.PokedexState state=top.totodile.pokemmo.server.game.GameService.PokedexState.defaults();
    String query="";
    final int[] minimum=new int[6],maximum={255,255,255,255,255,255};
    void reset(){type1=-1;type2=-1;eggGroup=0;ability=0;status=0;heldItem=0;Arrays.fill(minimum,0);Arrays.fill(maximum,255);}
    boolean matches(BwPokedex dex,BwPokedex.Species s){
        var extra=dex.extra(s.id());
        if(region>=0&&dex.number(s.id(),region)==0)return false;
        if(tier>=0&&!extra.tiers().contains(tier))return false;
        if(type1>=0&&s.type1()!=type1&&s.type2()!=type1)return false;
        if(type2>=0&&s.type1()!=type2&&s.type2()!=type2)return false;
        if(eggGroup>0&&!s.eggGroups().contains(eggGroup))return false;
        if(ability>0&&!s.abilities().contains(ability))return false;
        if(heldItem>0&&!s.heldItems().contains(heldItem))return false;
        boolean seen=state.has(s.id(),0),owned=state.has(s.id(),1);
        if(knownOnly&&!seen||status==1&&seen||status==2&&!seen||status==3&&!owned)return false;
        for(int i=0;i<6;i++)if(s.stats().get(i)<minimum[i]||s.stats().get(i)>maximum[i])return false;
        return true;
    }
}
