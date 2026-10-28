package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import java.util.*;

/** Local teaching transaction for the reference TEACH_MOVE placeholder and client sn0 request. */
final class MachineStore {
    private final Database db;
    MachineStore(Database db){this.db=db;}
    int use(String owner,GameService.MachineTemplate machine,String target,int amount,int slot,
            Map<Integer,Set<Integer>> learners,Map<Integer,GameService.SpeciesTemplate> species){
        if(amount!=1||slot<0||slot>3)return 6005;
        int[] result={6005};
        db.transaction(()->{
            var rows=db.query("SELECT item_id,item_amount FROM owned_item WHERE owner_id=? AND inventory_id=1 AND item_index_id=? AND region=-1 AND item_amount>0 ORDER BY item_id LIMIT 1",owner,Integer.toString(machine.item()));
            if(rows.isEmpty()){result[0]=6003;return;}
            var p=new PokemonStore(db,species).load(owner).pokemons().stream().filter(v->v.id().equals(target)&&v.container()==PokemonData.PARTY&&!v.egg()).findFirst().orElse(null);
            if(p==null||p.moves().contains(machine.move())||!learners.getOrDefault(p.species(),Set.of()).contains(machine.move()))return;
            var moves=new ArrayList<>(p.moves());var pp=new ArrayList<>(p.pp());var ups=new ArrayList<>(p.ppUps());
            int empty=-1;for(int i=0;i<4;i++)if(moves.get(i)<=0){empty=i;break;}
            int selected=empty>=0?empty:slot;
            moves.set(selected,machine.move());pp.set(selected,-1);ups.set(selected,0);
            db.execute("UPDATE pokemon SET moves=?,moves_pp=?,pp_up_times=? WHERE trainer_id=? AND id=? AND container_id=1",encode(moves),encode(pp),encode(ups),owner,target);
            if(!machine.reusable()){
                var row=rows.get(0);if(Integer.parseInt(row[1])==1)db.execute("DELETE FROM owned_item WHERE owner_id=? AND item_id=? AND inventory_id=1",owner,row[0]);
                else db.execute("UPDATE owned_item SET item_amount=item_amount-1 WHERE owner_id=? AND item_id=? AND inventory_id=1",owner,row[0]);
            }
            result[0]=0;
        });
        return result[0];
    }
    private static String encode(List<Integer> values){return String.join(",",values.stream().map(String::valueOf).toList());}
}
