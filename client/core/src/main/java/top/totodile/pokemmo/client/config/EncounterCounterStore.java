package top.totodile.pokemmo.client.config;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** Per-character local counter snapshots, committed by ClientSettings' atomic file replacement. */
public final class EncounterCounterStore {
    public record SpeciesCount(int species,int count){}
    public record State(int total,List<SpeciesCount> records,Set<Integer> pinned,boolean expanded,boolean visible){
        public State {
            records=List.copyOf(records);pinned=Set.copyOf(pinned);
            var ids=new HashSet<Integer>();long sum=0;
            for(var entry:records){
                if(entry.species()<1||entry.species()>649||entry.count()<0||!ids.add(entry.species()))throw new IllegalArgumentException("Invalid encounter species record");
                sum+=entry.count();
            }
            if(total<0||sum>total||!ids.containsAll(pinned))throw new IllegalArgumentException("Invalid encounter totals or pins");
        }
        public static State empty(){return new State(0,List.of(),Set.of(),true,true);}
    }
    private final ClientSettings settings;
    private final String key;
    public EncounterCounterStore(ClientSettings settings,String server,String username,String character){
        this.settings=Objects.requireNonNull(settings);
        String identity=Objects.requireNonNull(server)+"\0"+Objects.requireNonNull(username).toLowerCase(Locale.ROOT)+"\0"+Objects.requireNonNull(character);
        try{key="client.encounters."+HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8)));}
        catch(NoSuchAlgorithmException impossible){throw new AssertionError(impossible);}
    }
    public State load(){
        String saved=settings.get(key,"");if(saved.isEmpty())return State.empty();
        try{
            var json=new JsonReader().parse(saved);if(json.getInt("version")!=1)throw new IllegalArgumentException("Unknown encounter snapshot version");
            var records=new ArrayList<SpeciesCount>();var pinned=new HashSet<Integer>();
            for(var record:json.get("records")){int species=record.getInt("species");records.add(new SpeciesCount(species,record.getInt("count")));if(record.getBoolean("pinned",false))pinned.add(species);}
            return new State(json.getInt("total"),records,pinned,json.getBoolean("expanded",true),json.getBoolean("visible",true));
        }catch(RuntimeException error){throw new IllegalStateException("Cannot read saved encounter counter",error);}
    }
    public void save(State state){
        var json=new JsonValue(JsonValue.ValueType.object);json.addChild("version",new JsonValue(1L));json.addChild("total",new JsonValue((long)state.total()));json.addChild("expanded",new JsonValue(state.expanded()));json.addChild("visible",new JsonValue(state.visible()));
        var records=new JsonValue(JsonValue.ValueType.array);
        for(var entry:state.records()){
            var record=new JsonValue(JsonValue.ValueType.object);record.addChild("species",new JsonValue((long)entry.species()));record.addChild("count",new JsonValue((long)entry.count()));record.addChild("pinned",new JsonValue(state.pinned().contains(entry.species())));records.addChild(record);
        }
        json.addChild("records",records);settings.save(Map.of(key,json.toJson(JsonWriter.OutputType.json)));
    }
}
