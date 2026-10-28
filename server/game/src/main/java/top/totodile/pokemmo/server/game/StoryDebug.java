package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import top.totodile.pokemmo.db.Database;
import java.util.*;

/** Explicitly requested local debugging. Fields come from the implemented script/state adapters. */
public final class StoryDebug {
    private StoryDebug() {}
    public enum Mode { TEMPORARY, PERMANENT, RESTORE }
    public record Field(String key,String label,int value,int min,int max) {}
    public record State(boolean temporary,List<Field> fields) {
        public State { fields = List.copyOf(fields); }
    }
    public record Request(Mode mode,Map<String,Integer> values) {
        public Request { Objects.requireNonNull(mode); values = Map.copyOf(values); }
    }
    static State state(Database db,String id,boolean temporary) {
        var fields = new ArrayList<Field>();
        var opening = db.query("SELECT stage,starter FROM character_kanto_story WHERE character_id=?", id);
        add(fields,"enabled","启用关都剧情",opening.isEmpty()?0:1,0,1);
        add(fields,"stage","开场阶段（0～6）",opening.isEmpty()?0:Integer.parseInt(opening.get(0)[0]),0,6);
        add(fields,"starter","初始选择（0草／1火／2水／3未选）",opening.isEmpty()?3:Integer.parseInt(opening.get(0)[1]),0,3);
        var p=KantoMainline.progress(db,id);
        add(fields,"story","主线标记位集合（十进制）",p.story(),0,65535);
        add(fields,"extra","额外剧情标记位集合（十进制）",p.extra(),0,65535);
        add(fields,"badges","徽章位集合（0无／255八枚）",p.badges(),0,255);
        add(fields,"champion","冠军状态（0否／1是）",p.champion()?1:0,0,1);
        add(fields,"elite","四天王连续挑战阶段",p.elite(),0,4);
        var parcel=db.query("SELECT status FROM character_oak_parcel WHERE character_id=?",id);
        add(fields,"parcel","包裹状态（0未领／1已领／2已交／3图鉴）",parcel.isEmpty()?0:Integer.parseInt(parcel.get(0)[0]),0,3);
        add(fields,"sign","看牌女孩（0未读／1刚读／2再进镇）",PalletSignLady.inspect(db,id).scene(),0,2);
        var gender=db.query("SELECT gender FROM characters WHERE id=?",id);
        add(fields,"gender","性别分支（0男／1女）",Integer.parseInt(gender.get(0)[0]),0,1);
        var flags=new TreeSet<String>();
        for(var program:KantoMainline.PROGRAMS) collectFlags(program,flags);
        for(var row:db.query("SELECT flag FROM character_story_event WHERE character_id=?",id))flags.add(row[0]);
        for(String flag:flags)add(fields,"event:"+flag,"事件："+flag,KantoMainline.event(db,id,flag)?1:0,0,1);
        for(var row:db.query("SELECT action_key FROM character_story_action WHERE character_id=? ORDER BY action_key",id))
            add(fields,"action:"+row[0],"已执行记录（改0允许重做）："+row[0],1,0,1);
        return new State(temporary,fields);
    }
    private static void add(List<Field> fields,String key,String label,int value,int min,int max) {
        fields.add(new Field(key,label,value,min,max));
    }
    private static void collectFlags(JsonElement e,Set<String> flags) {
        if(e.isJsonObject()) {
            var o=e.getAsJsonObject();
            if(o.has("flag")&&o.get("flag").isJsonPrimitive())flags.add(o.get("flag").getAsString());
            for(var value:o.entrySet())collectFlags(value.getValue(),flags);
        } else if(e.isJsonArray()) for(var value:e.getAsJsonArray())collectFlags(value,flags);
    }
    static void apply(Database db,String id,Map<String,Integer> values) {
        var allowed=new HashMap<String,Field>();
        for(var field:state(db,id,false).fields())allowed.put(field.key(),field);
        for(var change:values.entrySet()) {
            var field=allowed.get(change.getKey());
            if(field==null||change.getValue()<field.min()||change.getValue()>field.max())
                throw new IllegalArgumentException("无效的剧情状态："+change.getKey());
        }
        db.transaction(()->{
            for(var change:values.entrySet()) {
                String key=change.getKey();int value=change.getValue();
                switch(key) {
                    case "enabled" -> {
                        if(value==0)db.execute("DELETE FROM character_kanto_story WHERE character_id=?",id);
                        else db.execute("INSERT OR IGNORE INTO character_kanto_story(character_id) VALUES (?)",id);
                    }
                    case "stage","starter" -> {
                        db.execute("INSERT OR IGNORE INTO character_kanto_story(character_id) VALUES (?)",id);
                        db.execute("UPDATE character_kanto_story SET "+key+"=? WHERE character_id=?",value,id);
                    }
                    case "story","extra","champion","elite" -> {
                        KantoMainline.ensure(db,id);
                        db.execute("UPDATE character_kanto_mainline SET "+key+"=? WHERE character_id=?",value,id);
                    }
                    case "badges" -> {
                        db.execute("INSERT OR IGNORE INTO character_trainer(character_id) VALUES (?)",id);
                        db.execute("UPDATE character_trainer SET badges=? WHERE character_id=?",value,id);
                    }
                    case "parcel" -> db.execute("INSERT INTO character_oak_parcel(character_id,status) VALUES (?,?) ON CONFLICT(character_id) DO UPDATE SET status=excluded.status",id,value);
                    case "sign" -> {
                        db.execute("INSERT INTO character_kanto_variables(character_id,variable,value) VALUES (?,16496,?) ON CONFLICT(character_id,variable) DO UPDATE SET value=excluded.value",id,value);
                        db.execute("DELETE FROM character_pallet_sign_lady WHERE character_id=?",id);
                    }
                    case "gender" -> db.execute("UPDATE characters SET gender=? WHERE id=?",value,id);
                    default -> {
                        if(key.startsWith("event:")) {
                            if(value==1)KantoMainline.eventSet(db,id,key.substring(6));
                            else db.execute("DELETE FROM character_story_event WHERE character_id=? AND flag=?",id,key.substring(6));
                        } else if(key.startsWith("action:")&&value==0)
                            db.execute("DELETE FROM character_story_action WHERE character_id=? AND action_key=?",id,key.substring(7));
                    }
                }
            }
            // Replay resolves the current map's conditions again rather than resuming stale actions.
            if(values.getOrDefault("enabled",1)==0)db.execute("DELETE FROM character_kanto_story WHERE character_id=?",id);
            db.execute("DELETE FROM character_story_resume WHERE character_id=?",id);
        });
    }
}
