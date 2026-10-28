package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import java.util.*;

/** Owner-scoped SQLite adaptation of reference ItemUseService.applyEffects/consumeItem. */
final class MedicineStore {
    private final Database db;
    MedicineStore(Database db){this.db=db;}

    int use(String owner,int item,String target,int amount,int move,
            Map<Integer,GameService.ItemTemplate> items,Map<Integer,GameService.SpeciesTemplate> species,
            Map<Integer,Integer> movePp){
        if(amount<1||amount>9999||move< -1||move>3)throw new IllegalArgumentException("Invalid medicine request");
        var template=items.get(item);
        if(template==null||!template.medicine().supported())return 6005;
        var effect=template.medicine();
        if(effect.action()!=2&&effect.action()!=3&&effect.action()!=5&&effect.action()!=7)return 6005;
        if(!effect.multiple()&&amount!=1)return 6005;
        // SKILL restores the selected move; MONSTER restores all moves (Elixir).
        if(effect.pp()>0&&(effect.action()==3?move<0:move!=-1))return 6005;
        if(effect.pp()==0&&move!=-1)return 6005;
        int[] result={6005};
        db.transaction(()->{
            var owned=db.query("SELECT item_id,item_amount FROM owned_item WHERE owner_id=?"
                    +" AND inventory_id=1 AND item_index_id=? AND region=-1 AND item_amount>=? ORDER BY item_id LIMIT 1",owner,Integer.toString(item),Integer.toString(amount));
            if(owned.isEmpty()){result[0]=6003;return;}
            var p=new PokemonStore(db,species).load(owner).pokemons().stream()
                    .filter(v->v.id().equals(target)&&v.container()==PokemonData.PARTY&&!v.egg()).findFirst().orElse(null);
            if(p==null||p.hp()==0&&!effect.revive()||p.hp()>0&&effect.revive())return;
            int hp=p.hp(),status=p.status(),max=p.stats().get(0);
            var pp=new ArrayList<>(p.pp());
            boolean changed=false;
            if(effect.hp()>0){
                int recovery=effect.percent()?(int)Math.ceil(max*(double)effect.hp()*amount/100.0):effect.hp()*amount;
                int next=effect.revive()?Math.min(max,Math.max(1,recovery)):Math.min(max,hp+recovery);
                changed=next!=hp;hp=next;
                if(effect.revive())status=0;
            }
            int mask=effect.statusMask(),current=status&255;
            if(mask!=0&&current!=0&&(mask==255||(current&mask)!=0)){
                status=0;changed=true;
            }
            if(effect.pp()>0){
                for(int i=0;i<p.moves().size();i++){
                    int id=p.moves().get(i);if(id<=0||move>=0&&move!=i)continue;
                    Integer base=movePp.get(id);if(base==null||base<1)throw new IllegalStateException("Missing move PP data "+id);
                    int maximum=base*(5+Math.min(3,Math.max(0,p.ppUps().get(i))))/5;
                    int before=pp.get(i)<0?maximum:pp.get(i);
                    int after=Math.min(maximum,before+effect.pp()*amount);
                    if(after!=before){changed=true;pp.set(i,after);}
                }
            }
            if(!changed)return;
            int friendship=Math.max(0,Math.min(255,p.friendship()+effect.friendship()*amount));
            db.execute("UPDATE pokemon SET current_hp=?,status=?,moves_pp=?,friend_value=? WHERE trainer_id=? AND id=? AND container_id=1",
                    hp,status,String.join(",",pp.stream().map(String::valueOf).toList()),friendship,owner,target);
            var stack=owned.get(0);int remaining=Integer.parseInt(stack[1])-amount;
            if(remaining==0)db.execute("DELETE FROM owned_item WHERE owner_id=? AND item_id=? AND inventory_id=1",owner,stack[0]);
            else db.execute("UPDATE owned_item SET item_amount=? WHERE owner_id=? AND item_id=? AND inventory_id=1",remaining,owner,stack[0]);
            result[0]=0;
        });
        return result[0];
    }
}
