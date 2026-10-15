package top.totodile.pokemmo.protocol;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** 服务端与客户端共用的种族、成长曲线和升级招式规则，不含图像。 */
public final class PokemonCatalog {
    public record LearnedMove(int level,int id,int pp,int type,int category,int power,int accuracy) { }
    public record Species(List<Integer> stats,int type1,int type2,int gender,int ability1,int ability2,
                          int hiddenAbility,int growth,int friendship,List<LearnedMove> moves) { }
    public static final int MAX_SPECIES = 1025;
    private static final Species[] SPECIES=load();
    private static final Map<Integer,Integer> LOCATIONS=locations();
    private PokemonCatalog() { }
    public static Species get(int id) {if(id<1||id>MAX_SPECIES)throw new IllegalArgumentException("Invalid species");return SPECIES[id];}
    public static int hp(int id,int level) {return stat(id,level,0,0,0,0);}
    public static int stat(int id,int level,int nature,int index,int iv,int ev) {
        if(level<1||level>100||nature<0||nature>24||index<0||index>5||iv<0||iv>31||ev<0||ev>252)throw new IllegalArgumentException("Invalid stats");
        int value=(2*get(id).stats().get(index)+iv+ev/4)*level/100;
        if(index==0)return id==292?1:value+level+10;
        value+=5;int[] order={1,2,5,3,4};int up=order[nature/5],down=order[nature%5];
        return up==down?value:index==up?value*110/100:index==down?value*90/100:value;
    }
    public static long experience(int growth,int level) {
        if(level<1||level>100)throw new IllegalArgumentException("Invalid level");
        if(level==1)return 0;
        long n=level,c=n*n*n;
        return Math.max(0,switch(growth){
            case 0->c;
            case 1->n<=50?c*(100-n)/50:n<=68?c*(150-n)/100:n<=98?c*((1911-10*n)/3)/500:c*(160-n)/100;
            case 2->n<=15?c*((n+1)/3+24)/50:n<=36?c*(n+14)/50:c*(n/2+32)/50;
            case 3->6*c/5-15*n*n+100*n-140;
            case 4->4*c/5;case 5->5*c/4;default->throw new IllegalArgumentException("Invalid growth");
        });
    }
    public static List<PokemonIndividual.Move> moves(int id,int level) {
        List<LearnedMove> learned=new ArrayList<>();
        for(var move:get(id).moves())if(move.level()<=level){learned.removeIf(old->old.id()==move.id());learned.add(move);}
        learned.sort(Comparator.comparingInt(LearnedMove::level).reversed());
        return learned.subList(0,Math.min(4,learned.size())).stream().map(m->new PokemonIndividual.Move(m.id(),m.pp(),m.pp(),0)).toList();
    }
    public static LearnedMove move(int id,int move) {return get(id).moves().stream().filter(m->m.id()==move).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown move"));}
    public static int location(int region,int group,int map){return LOCATIONS.getOrDefault(region<<16|group<<8|map,region==0?9301:region==1?9424:250000+region);}
    private static Map<Integer,Integer> locations(){
        Map<Integer,Integer> result=new HashMap<>();
        try(var input=PokemonCatalog.class.getResourceAsStream("/catalog/pokemon-locations.csv")){
            if(input==null)throw new IOException("Missing Pokemon locations");
            try(var reader=new BufferedReader(new InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8))){for(String line;(line=reader.readLine())!=null;){if(line.startsWith("#")||line.isBlank())continue;String[] f=line.split(",");result.put(Integer.parseInt(f[0])<<16|Integer.parseInt(f[1])<<8|Integer.parseInt(f[2]),Integer.parseInt(f[3]));}}
        }catch(IOException e){throw new ExceptionInInitializerError(e);}
        for(int region : new int[]{2,3,5})try(var input=PokemonCatalog.class.getResourceAsStream(region==2?"/maps/unova.csv":region==5?"/maps/johto.csv":"/maps/sinnoh.csv")){
            if(input==null)throw new IOException("Missing Unova locations");
            try(var reader=new BufferedReader(new InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8))){for(String line;(line=reader.readLine())!=null;){String[] f=line.split(",");result.put(region<<16|Integer.parseInt(f[0])<<8|Integer.parseInt(f[1]),Integer.parseInt(f[4]));}}
        }catch(IOException e){throw new ExceptionInInitializerError(e);}return Map.copyOf(result);
    }
    private static Species[] load() {
        Species[] result=new Species[MAX_SPECIES + 1];
        try(var raw=PokemonCatalog.class.getResourceAsStream("/catalog/pokemon.dat.gz")) {
            if(raw==null)throw new IOException("Missing Pokemon catalog");
            try(var in=new DataInputStream(new GZIPInputStream(raw))){
                if(in.readInt()!=0x504b4431||in.readInt()!=649)throw new IOException("Invalid catalog");
                for(int i=1;i<=649;i++) {
                    if(in.readUnsignedShort()!=i)throw new IOException("Invalid species index");
                    List<Integer> stats=new ArrayList<>();for(int j=0;j<6;j++)stats.add(in.readUnsignedByte());
                    int t1=in.readUnsignedByte(),t2=in.readUnsignedByte(),g=in.readUnsignedByte(),a1=in.readUnsignedByte(),a2=in.readUnsignedByte(),ha=in.readUnsignedByte(),growth=in.readUnsignedByte(),friendship=in.readUnsignedByte();
                    int count=in.readUnsignedShort();if(count>100)throw new IOException("Invalid learnset");
                    List<LearnedMove> moves=new ArrayList<>();for(int j=0;j<count;j++)moves.add(new LearnedMove(in.readUnsignedByte(),in.readUnsignedShort(),in.readUnsignedByte(),in.readUnsignedByte(),in.readUnsignedByte(),in.readUnsignedByte(),in.readUnsignedByte()));
                    result[i]=new Species(List.copyOf(stats),t1,t2,g,a1,a2,ha,growth,friendship,List.copyOf(moves));
                }
                if(in.read()!=-1)throw new IOException("Trailing catalog data");
            }
        }catch(IOException e){throw new ExceptionInInitializerError(e);}
        try (var raw = PokemonCatalog.class.getResourceAsStream("/catalog/pokemon-gen9.dat.gz");
             var in = new DataInputStream(new GZIPInputStream(Objects.requireNonNull(raw)))) {
            if (in.readInt() != 0x504b4439 || in.readInt() != MAX_SPECIES - 649) throw new IOException("Invalid extended catalog");
            for (int id = 650; id <= MAX_SPECIES; id++) {
                if (in.readUnsignedShort() != id) throw new IOException("Invalid extended species index");
                List<Integer> stats = new ArrayList<>();
                for (int i = 0; i < 6; i++) stats.add(in.readUnsignedByte());
                int t1 = in.readUnsignedByte(), t2 = in.readUnsignedByte();
                int a1 = in.readUnsignedShort(), a2 = in.readUnsignedShort(), ha = in.readUnsignedShort();
                int gender = in.readUnsignedByte(), growth = in.readUnsignedByte(), friendship = in.readUnsignedByte();
                int count = in.readUnsignedShort();
                List<LearnedMove> moves = new ArrayList<>();
                for (int i = 0; i < count; i++) moves.add(new LearnedMove(in.readUnsignedByte(), in.readUnsignedShort(), in.readUnsignedByte(), in.readUnsignedByte(), in.readUnsignedByte(), in.readUnsignedShort(), in.readUnsignedByte()));
                result[id] = new Species(List.copyOf(stats), t1, t2, gender, a1, a2, ha, growth, friendship, List.copyOf(moves));
            }
            if (in.read() != -1) throw new IOException("Trailing extended catalog data");
        } catch (IOException e) { throw new ExceptionInInitializerError(e); }
        return result;
    }
}
