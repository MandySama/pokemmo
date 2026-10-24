package top.totodile.pokemmo.client.ui.inventory;

import java.util.*;

/** User-requested local debug sampling of the loaded reference item registry. */
public final class RandomInventoryItems {
    public record Grant(int itemId,int amount) {}
    private RandomInventoryItems(){}
    public static List<Grant> sample(InventoryItems items,int pageHeight,Random random){
        var pockets=items.randomCandidates();
        return sample(items,pockets,pageHeight,random);
    }
    static Map<Integer,Map<Integer,List<Integer>>> candidates(InventoryItems items){
        var pockets=new TreeMap<Integer,Map<Integer,List<Integer>>>();
        for(var entry:items.catalogue()){
            var template=items.template(entry.id());String name=items.name(entry.id());
            if(template.pocket()<1||template.pocket()>8||name.isBlank()||name.contains("STRING_")||name.contains("?")||name.contains("？")||!items.randomEligible(entry.id()))continue;
            pockets.computeIfAbsent(template.pocket(),key->new TreeMap<>()).computeIfAbsent(items.group(entry.id()),key->new ArrayList<>()).add(entry.id());
        }
        return pockets;
    }
    private static List<Grant> sample(InventoryItems items,Map<Integer,Map<Integer,List<Integer>>> pockets,int pageHeight,Random random){
        var result=new ArrayList<Grant>();
        for(var pocket:pockets.entrySet()){
            var selected=new LinkedHashMap<Integer,List<Integer>>();var remaining=new LinkedHashMap<Integer,List<Integer>>();
            for(var group:pocket.getValue().entrySet()){
                var pool=new ArrayList<>(group.getValue());Collections.shuffle(pool,random);
                int count=1+random.nextInt(Math.min(pocket.getKey()==4?3:6,pool.size()));
                selected.put(group.getKey(),new ArrayList<>(pool.subList(0,count)));
                remaining.put(group.getKey(),new ArrayList<>(pool.subList(count,pool.size())));
            }
            while(height(items,selected,pocket.getKey())<pageHeight){
                var available=remaining.entrySet().stream().filter(e->!e.getValue().isEmpty()).toList();
                if(available.isEmpty())break;
                var group=available.get(random.nextInt(available.size()));
                selected.get(group.getKey()).add(group.getValue().remove(group.getValue().size()-1));
            }
            for(var group:selected.values())for(int id:group){
                int maximum=pocket.getKey()==2?1:Math.min(30,items.template(id).maximumStack());
                result.add(new Grant(id,1+random.nextInt(Math.max(1,maximum))));
            }
        }
        return List.copyOf(result);
    }
    private static int height(InventoryItems items,Map<Integer,List<Integer>> groups,int pocket){
        int height=0,columns=pocket==4?2:11,row=pocket==4?36:42;
        for(var group:groups.values()){
            if(!items.groupTitle(group.get(0)).isEmpty())height+=23;
            height+=(group.size()+columns-1)/columns*(row+5);
        }
        return height;
    }
}
