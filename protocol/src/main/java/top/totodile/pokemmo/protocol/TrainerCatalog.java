package top.totodile.pokemmo.protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 由卡带导出的单人NPC挑战目录；编号、队伍和文本不接受客户端覆盖。 */
public final class TrainerCatalog {
    public record Mon(int species,int level,int iv,int item,List<PokemonIndividual.Move> moves,int rawItem) { }
    public record Trainer(int group,int map,int npc,int id,String name,int intro,int defeat,int after,List<Mon> mons) {
        public Trainer { mons=List.copyOf(mons); }
        public List<PartyMember> party() {
            return mons.stream().map(m->{
                var d=PokemonIndividual.create(m.species(),m.level(),false,id,name,0,group,map,0);
                d=new PokemonIndividual(d.id(),"",id,name,d.nature(),PokemonCatalog.get(m.species()).ability1(),d.gender(),d.friendship(),d.experience(),m.item(),4,0,0,group,map,m.level(),
                        Collections.nCopies(6,m.iv()),Collections.nCopies(6,0),m.moves(),0,List.of(),Collections.nCopies(6,0),List.of());
                int hp=d.stat(m.species(),m.level(),0);return new PartyMember(m.species(),false,false,hp,hp,m.level(),d);
            }).toList();
        }
    }
    private static final Map<Integer,Trainer> ENTRIES=load();
    private TrainerCatalog() { }
    public static Trainer get(int group,int map,int npc){return ENTRIES.get(group<<16|map<<8|npc);}
    public static Collection<Trainer> all(){return ENTRIES.values();}
    private static final Map<Integer,Trainer> SCRIPT_TRAINERS=scriptTrainers();
    public static Trainer scriptTrainer(int id){return SCRIPT_TRAINERS.get(id);}
    private static Map<Integer,Trainer> scriptTrainers(){
        var result=new HashMap<Integer,Trainer>();
        try(var input=TrainerCatalog.class.getResourceAsStream("/maps/gba-opening-trainers.csv")){
            if(input==null)throw new IOException("Missing ROM script trainers");
            for(String line:new String(input.readAllBytes(),StandardCharsets.UTF_8).split("\\R")){
                if(line.isBlank())continue;String[] parts=line.split(";"),f=parts[0].split(",");var mons=new ArrayList<Mon>();
                for(int i=1;i<parts.length;i++){var a=parts[i].split(":",-1);var moves=new ArrayList<PokemonIndividual.Move>();for(String m:a[4].split("\\+")){var v=m.split("/");int pp=Integer.parseInt(v[1]);moves.add(new PokemonIndividual.Move(Integer.parseInt(v[0]),pp,pp,0));}mons.add(new Mon(Integer.parseInt(a[0]),Integer.parseInt(a[1]),Integer.parseInt(a[2]),Integer.parseInt(a[3]),moves,Integer.parseInt(a[5])));}
                int id=Integer.parseInt(f[3]);result.put(id,new Trainer(4,3,8,id,f[4],0,0,0,mons));
            }
        }catch(IOException e){throw new ExceptionInInitializerError(e);}return Map.copyOf(result);
    }
    private static Map<Integer,Trainer> load() {
        var result=new LinkedHashMap<Integer,Trainer>();
        try(var input=TrainerCatalog.class.getResourceAsStream("/maps/kanto-trainers.csv")) {
            if(input==null)throw new IOException("Missing Kanto trainer catalog");
            for(String line:new String(input.readAllBytes(),StandardCharsets.UTF_8).split("\\R")) {
                if(line.isBlank()||line.startsWith("#"))continue;
                String[] parts=line.split(";"),f=parts[0].split(",");var mons=new ArrayList<Mon>();
                for(int i=1;i<parts.length;i++) {
                    var fields=parts[i].split(":",-1);var moves=new ArrayList<PokemonIndividual.Move>();
                    if(!fields[4].isBlank())for(String move:fields[4].split("\\+")){var data=move.split("/");int pp=Integer.parseInt(data[1]);moves.add(new PokemonIndividual.Move(Integer.parseInt(data[0]),pp,pp,0));}
                    mons.add(new Mon(Integer.parseInt(fields[0]),Integer.parseInt(fields[1]),Integer.parseInt(fields[2]),Integer.parseInt(fields[3]),List.copyOf(moves),Integer.parseInt(fields[5])));
                }
                var trainer=new Trainer(Integer.parseInt(f[0]),Integer.parseInt(f[1]),Integer.parseInt(f[2]),Integer.parseInt(f[3]),f[4],Integer.parseInt(f[5]),Integer.parseInt(f[6]),Integer.parseInt(f[7]),mons);
                if(trainer.id()<1||trainer.id()>742||mons.isEmpty()||mons.size()>6||result.put(trainer.group()<<16|trainer.map()<<8|trainer.npc(),trainer)!=null)throw new IOException("Invalid trainer catalog");
            }
        }catch(IOException|RuntimeException error){throw new ExceptionInInitializerError(error);}return Collections.unmodifiableMap(result);
    }
}
