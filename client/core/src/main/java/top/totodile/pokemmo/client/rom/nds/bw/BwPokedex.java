package top.totodile.pokemmo.client.rom.nds.bw;

import java.io.IOException;
import java.util.*;
import com.badlogic.gdx.files.FileHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;

/** The same personal / learnset / evolution archives as BlackWhiteRom.loadSpecies, National 1–649. */
public final class BwPokedex {
    public static final int LAST_SPECIES=649;
    public record Evolution(int method,int parameter,int target) {}
    public record LearnedMove(int level,int move,int source) {
        public LearnedMove(int level,int move){this(level,move,-1);}
    }
    public record Location(String kind,int region,String name,int minimum,int maximum,String rarity,int season,int timeMask) {}
    public record Extra(int genderRatio,int catchRate,int growthRate,int baseExperience,int formCount,int formSprite,
                        List<Integer> tiers,List<Location> locations,Map<Integer,Integer> regional,boolean obtainable) {}
    public record Move(int id,int type,int category,int power,int accuracy,int pp) {}
    public record Species(int id,List<Integer> stats,int type1,int type2,int height,int weight,
                          List<Integer> abilities,List<Integer> eggGroups,List<Integer> evYield,
                          List<Integer> heldItems,List<LearnedMove> moves,List<Evolution> evolutions) {
        public int total(){return stats.stream().mapToInt(Integer::intValue).sum();}
    }
    private final RomInfo rom;
    final ClientStrings strings;
    final List<Species> species=new ArrayList<>();
    private final List<Move> moves=new ArrayList<>();
    private final Map<Integer,Integer> parents=new HashMap<>();
    final Map<Integer,Extra> extras=new HashMap<>();
    final Map<Integer,Move> moveOverrides=new HashMap<>();
    private final NdsArchive text;
    private final Map<Integer,String[]> textTables=new HashMap<>();

    public BwPokedex(RomInfo rom,ClientStrings strings) throws IOException {
        if(rom==null||rom.type()!=RomType.BLACK_WHITE)throw new IOException("Black/White ROM required");
        this.rom=rom;this.strings=strings;
        var personal=archive("a/0/1/6");var learnsets=archive("a/0/1/8");var evolutions=archive("a/0/1/9");
        text=archive("a/0/0/2");
        if(personal.size()<=LAST_SPECIES)throw new IOException("Incomplete BW personal archive");
        for(int id=1;id<=LAST_SPECIES;id++) {
            byte[] p=personal.readRaw(id),l=learnsets.readRaw(id),e=evolutions.readRaw(id);
            if(p.length<40||e.length<42)throw new IOException("Truncated species "+id);
            var learned=new ArrayList<LearnedMove>();
            for(int offset=0;offset+4<=l.length;offset+=4) {
                int move=u16(l,offset),level=u16(l,offset+2);
                if(move==65535||level==65535)break;
                learned.add(new LearnedMove(level,move));
            }
            var next=new ArrayList<Evolution>();
            for(int offset=0;offset<42;offset+=6) {
                int method=u16(e,offset),target=u16(e,offset+4);
                if(method>0&&target>0&&target<=LAST_SPECIES) {
                    next.add(new Evolution(method,u16(e,offset+2),target));parents.putIfAbsent(target,id);
                }
            }
            // ROM order is HP, Attack, Defense, Speed, Sp. Attack, Sp. Defense.
            List<Integer> stats=List.of(u8(p,0),u8(p,1),u8(p,2),u8(p,4),u8(p,5),u8(p,3));
            int ev=u16(p,10);
            List<Integer> yield=List.of(ev&3,ev>>2&3,ev>>4&3,ev>>8&3,ev>>10&3,ev>>6&3);
            species.add(new Species(id,stats,u8(p,6),u8(p,7),u16(p,36),u16(p,38),
                    List.of(u8(p,24),u8(p,25),u8(p,26)),List.of(u8(p,22),u8(p,23)),yield,
                    List.of(u16(p,12),u16(p,14),u16(p,16)),List.copyOf(learned),List.copyOf(next)));
            extras.put(id,new Extra(u8(p,18),u8(p,8),u8(p,21),u16(p,34),u8(p,32),u16(p,30),List.of(5),List.of(),Map.of(),false));
        }
        var moveData=archive("a/0/2/1");
        for(int id=0;id<moveData.size();id++) {
            byte[] p=moveData.readRaw(id);
            if(p.length<6)throw new IOException("Truncated move "+id);
            moves.add(new Move(id,u8(p,0),u8(p,2),u8(p,3),u8(p,4),u8(p,5)));
        }
    }
    /** ROM first, then data/data.pak, as in the reference DataPackageLoader.ha0/xu0. */
    public void loadDataPackage(FileHandle file) throws IOException {
        new BwPokedexDataPackage(this).load(file);
    }
    /** The reference ServerOpcode079Packet supplies tiers separately from the data package. */
    public void loadTiers(byte[] payload) throws IOException {
        if(payload.length==0)return;
        try {
            ByteBuffer input=ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN);
            var primary=new HashMap<Integer,Integer>();
            int count=Short.toUnsignedInt(input.getShort());
            for(int i=0;i<count;i++)primary.put(Short.toUnsignedInt(input.getShort()),Byte.toUnsignedInt(input.get()));
            var rules=new HashMap<Integer,Set<Integer>>();
            count=Byte.toUnsignedInt(input.get());
            for(int i=0;i<count;i++) {
                int tier=Byte.toUnsignedInt(input.get()),size=Short.toUnsignedInt(input.getShort());
                var ids=new HashSet<Integer>();
                for(int j=0;j<size;j++)ids.add(Short.toUnsignedInt(input.getShort()));
                rules.put(tier,ids);
            }
            if(input.hasRemaining())throw new IOException("Trailing Pokédex tier data");
            for(int id=1;id<=LAST_SPECIES;id++) {
                Extra old=extra(id);var tiers=new ArrayList<Integer>();tiers.add(primary.getOrDefault(id,5));
                for(var rule:rules.entrySet())if(rule.getValue().contains(id)&&!tiers.contains(rule.getKey()))tiers.add(rule.getKey());
                extras.put(id,new Extra(old.genderRatio,old.catchRate,old.growthRate,old.baseExperience,old.formCount,old.formSprite,
                        List.copyOf(tiers),old.locations,old.regional,old.obtainable));
            }
        }catch(RuntimeException error){throw new IOException("Invalid Pokédex tier packet",error);}
    }
    NdsArchive archive(String path) throws IOException {return new NdsArchive(RomReader.readFile(rom,path));}
    RomInfo rom(){return rom;}
    public List<Species> all(){return Collections.unmodifiableList(species);}
    public Species get(int id){if(id<1||id>LAST_SPECIES)throw new IllegalArgumentException("National ID "+id);return species.get(id-1);}
    Move moveOrNull(int id){Move override=moveOverrides.get(id);return override!=null?override:id>=0&&id<moves.size()?moves.get(id):null;}
    public Move move(int id){Move move=moveOrNull(id);if(move==null)throw new IllegalArgumentException("Move ID "+id);return move;}
    public Extra extra(int id){get(id);return extras.get(id);}
    public double effectiveness(int attack,int id){Species s=get(id);return BwTypeEffectiveness.against(attack,s.type1)*(s.type1==s.type2?1:BwTypeEffectiveness.against(attack,s.type2));}
    public List<LearnedMove> learnset(int id){
        var result=new ArrayList<>(get(id).moves);var seen=new HashSet<Integer>();
        for(LearnedMove move:result)if(move.source==0)seen.add(move.move);
        int parent=parent(id);var visited=new HashSet<Integer>();
        while(parent>0&&visited.add(parent)){
            for(LearnedMove move:get(parent).moves)if(move.source==0&&seen.add(move.move))result.add(move);
            parent=parent(parent);
        }
        return result;
    }
    public int number(int id,int region){return region<0?id:extra(id).regional.getOrDefault(region,0);}
    public String regionName(int region){return strings.getOrDefault(250000+region,"");}
    public String moveDescription(int id){return strings.getOrDefault(120000+id,"");}
    public String moveSource(LearnedMove learned){
        if(learned.source==4)return "TM";
        if(learned.source>=0)return strings.get(1750+learned.source);
        return learned.level<=1?strings.get(1732):strings.format(1731,Integer.toString(learned.level));
    }
    public long experienceAt100(int id){return switch(extra(id).growthRate){case 0->1_000_000;case 1->600_000;case 2->1_640_000;case 3->1_059_860;case 4->800_000;case 5->1_250_000;default->0;};}
    public String growthName(int id){return strings.get(switch(extra(id).growthRate){case 0->6483;case 1->6484;case 2->6485;case 3->6486;case 4->6487;default->6488;});}
    public String gender(int id){int ratio=extra(id).genderRatio;
        if(ratio==255)return strings.getOrDefault(181016,"—");
        double female=ratio==254?100:ratio==0?0:(ratio+1)*100.0/256;
        return strings.format(6491,String.format(Locale.ROOT,"%s%%",decimal(100-female)),String.format(Locale.ROOT,"%s%%",decimal(female)));
    }
    private static String decimal(double value){return value==Math.rint(value)?Integer.toString((int)value):String.format(Locale.ROOT,"%.1f",value);}
    public int parent(int id){return parents.getOrDefault(id,0);}
    public String name(int id){return strings.getOrDefault(150000+id,"#"+id);}
    public String moveName(int id){return strings.getOrDefault(110000+id,"#"+id);}
    public String ability(int id){return strings.getOrDefault(210000+id,"—");}
    public String abilityDescription(int id){return strings.getOrDefault(220000+id,"");}
    public String item(int id){return strings.getOrDefault(245000+id,"#"+id);}
    public String type(int id){return strings.getOrDefault(230000+(id>=9?id+1:id),"—");}
    public String eggGroup(int id){return strings.getOrDefault(181000+id,"—");}
    public String category(int id){return text(260,id);}
    public String description(int id){return text(rom.code().startsWith("IRA")?236:235,id).replace('\n',' ');}
    public String nationalTitle(){String title=text(276,1);return title.equalsIgnoreCase("National Pokédex")?"National Pokédex":title;}
    private String text(int table,int id) {
        String translated=strings.archive(2,0,table,0,id);
        if(translated!=null)return translated;
        String[] entries=textTables.computeIfAbsent(table,key->{
            try{return BwText.decode(text.readRaw(key));}
            catch(IOException error){throw new IllegalStateException("Cannot read BW text "+key,error);}
        });
        return id<entries.length?entries[id]:"—";
    }
    public List<Species> search(String query) {
        String term=query.strip().toLowerCase(Locale.ROOT).replaceFirst("^#","");
        return species.stream().filter(s->term.isEmpty()||name(s.id()).toLowerCase(Locale.ROOT).contains(term)
                ||Integer.toString(s.id()).equals(term)||String.format(Locale.ROOT,"%03d",s.id()).equals(term)).toList();
    }
    public String evolution(Evolution evolution) {
        int value=evolution.parameter();
        String parameter=switch(evolution.method()) {
            case 6,8,17,18,19,20 -> item(value);
            case 21 -> moveName(value);
            case 7 -> name(evolution.target()==617?588:616);
            case 22 -> value>=1&&value<=LAST_SPECIES?name(value):"—";
            default -> Integer.toString(value);
        };
        return strings.format(2550+evolution.method(),parameter);
    }
    static int u8(byte[] data,int offset){return data[offset]&255;}
    static int u16(byte[] data,int offset){return u8(data,offset)|(u8(data,offset+1)<<8);}
}
