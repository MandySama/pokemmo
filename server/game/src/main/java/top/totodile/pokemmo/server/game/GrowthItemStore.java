package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import java.util.*;

/** Reference ItemUseService ADD_LEVEL plus item-triggered evolution and per-move PP Up state. */
final class GrowthItemStore {
    private final Database db;
    GrowthItemStore(Database db){this.db=db;}
    int use(String owner,GameService.GrowthItem item,String target,int amount,int move,
            Map<Integer,GameService.SpeciesTemplate> species,Map<Integer,Integer> movePp){
        if(amount<1||amount>9999||move< -1||move>3)return 6005;
        if(!item.multiple()&&amount!=1)return 6005;
        int[] result={6005};
        db.transaction(()->{
            var stacks=db.query("SELECT item_id,item_amount FROM owned_item WHERE owner_id=? AND inventory_id=1 AND item_index_id=? AND region=-1 AND item_amount>=? ORDER BY item_id LIMIT 1",owner,Integer.toString(item.item()),Integer.toString(amount));
            if(stacks.isEmpty()){result[0]=6003;return;}
            var p=new PokemonStore(db,species).load(owner).pokemons().stream().filter(v->v.id().equals(target)&&v.container()==PokemonData.PARTY&&!v.egg()).findFirst().orElse(null);if(p==null)return;
            if(item.ppUps()>0){
                if(move<0||p.moves().get(move)<=0)return;
                Integer base=movePp.get(p.moves().get(move));if(base==null||base<=1)return;
                int before=p.ppUps().get(move),after=item.ppUps()==3?3:before+amount;
                if(before>=3||after>3||item.ppUps()==3&&amount!=1)return;
                int oldMax=base*(5+before)/5,newMax=base*(5+after)/5;
                var pp=new ArrayList<>(p.pp());var ups=new ArrayList<>(p.ppUps());ups.set(move,after);
                if(pp.get(move)>=0)pp.set(move,Math.min(newMax,pp.get(move)+newMax-oldMax));
                db.execute("UPDATE pokemon SET moves_pp=?,pp_up_times=? WHERE trainer_id=? AND id=?",encode(pp),encode(ups),owner,target);
            }else{
                if(move!=-1)return;
                if(!db.query("SELECT pokemon_id FROM character_evolution WHERE character_id=?",owner).isEmpty()||MoveLearning.first(db,owner)!=null){result[0]=6002;return;}
                if(item.stone()>0){
                    if(amount!=1)return;
                    int next=PokemonEvolution.stoneTarget(p,item.stone(),species);if(next==0)return;
                    PokemonEvolution.stone(db,owner,p,next,item.item(),item.stone());
                }else if(item.levels()>0){
                    if(p.level()>=100||(long)item.levels()*amount>100-p.level())return;
                    var template=species.get(p.species());if(template==null)return;
                    int level=p.level()+item.levels()*amount;
                    int exp=PokemonExperience.atLevel(template.growthRate(),level);
                    BattleProgress.grow(db,owner,p,exp-Math.max(0,p.experience()),template,null);
                    var grown=new PokemonStore(db,species).load(owner).pokemons().stream().filter(v->v.id().equals(target)).findFirst().orElseThrow();
                    int hp=p.hp()==0?0:Math.min(grown.maxHp(),p.hp()+grown.maxHp()-p.maxHp());
                    db.execute("UPDATE pokemon SET current_hp=? WHERE trainer_id=? AND id=?",hp,owner,target);
                    PokemonEvolution.enqueue(db,owner,grown,species);
                }else return;
            }
            var stack=stacks.get(0);int remaining=Integer.parseInt(stack[1])-amount;
            if(remaining==0)db.execute("DELETE FROM owned_item WHERE owner_id=? AND item_id=? AND inventory_id=1",owner,stack[0]);
            else db.execute("UPDATE owned_item SET item_amount=? WHERE owner_id=? AND item_id=? AND inventory_id=1",remaining,owner,stack[0]);
            result[0]=0;
        });
        return result[0];
    }
    private static String encode(List<Integer> values){return String.join(",",values.stream().map(String::valueOf).toList());}
}
