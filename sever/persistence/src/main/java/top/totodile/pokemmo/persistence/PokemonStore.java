package top.totodile.pokemmo.persistence;

import top.totodile.pokemmo.protocol.*;
import java.sql.*;
import java.util.*;

/** 个体记录通过角色外键绑定所属关系，旧队伍首次读取时在角色锁内完成迁移。 */
final class PokemonStore {
    private record Stored(int species,PokemonIndividual data) { }
    /** 电脑箱子里的个体：箱位、显示标记与当前等级／血量独立于队伍数组。 */
    record Boxed(int box,int slot,PartyMember member) { }
    private PokemonStore() { }
    static List<PartyMember> load(Connection c,long character,List<PartyMember> base)throws SQLException {
        boolean own=c.getAutoCommit();if(own)c.setAutoCommit(false);
        try {
            String trainer;int region,group,map;long created;
            try(var q=c.prepareStatement("SELECT name,region,map_group,map_id,created_at,party_species,party_flags,party_hp,party_max_hp,party_levels FROM characters WHERE id=? FOR UPDATE")) {
                q.setLong(1,character);try(var r=q.executeQuery()){if(!r.next())throw new SQLException("Character missing");trainer=r.getString(1);region=r.getInt(2);group=r.getInt(3);map=r.getInt(4);created=java.time.Instant.now().getEpochSecond();base=CharacterStore.party(r.getArray(6),r.getArray(7),r.getArray(8),r.getArray(9),r.getArray(10));}
            }
            Map<Integer,Stored> stored=new HashMap<>();
            try(var q=c.prepareStatement("SELECT slot,species,individual_id,individual_data FROM character_pokemon WHERE character_id=?")) {
                q.setLong(1,character);try(var r=q.executeQuery()){while(r.next()) {
                    var d=PokemonIndividual.decode(r.getBytes(4));if(d.id()!=r.getLong(3))throw new SQLException("Individual identity mismatch");
                    stored.put(r.getInt(1),new Stored(r.getInt(2),d));
                }}
            }catch(java.io.IOException e){throw new SQLException("Invalid stored Pokemon",e);}
            List<PartyMember> result=new ArrayList<>();boolean changed=stored.size()!=base.size();
            for(int i=0;i<base.size();i++) {
                var b=base.get(i);var old=stored.get(i);boolean fresh=old==null||old.species()!=b.species();
                int level=b.level()==0?b.species()==150?70:b.species()==151?30:5:b.level();
                var d=fresh?PokemonIndividual.create(b.species(),level,b.alpha(),character,trainer,region,group,map,created):old.data();
                int max=d.stat(b.species(),level,0);
                int hp=fresh?(int)((long)b.hp()*max/b.maxHp()):Math.min(b.hp(),max);
                result.add(new PartyMember(b.species(),b.shiny(),b.alpha(),hp,max,level,d));
                changed|=fresh||b.maxHp()!=max||b.hp()!=hp||b.level()!=level;
            }
            if(changed){save(c,character,result);sync(c,character,result);}
            if(own)c.commit();return List.copyOf(result);
        }catch(SQLException|RuntimeException e){if(own)c.rollback();throw e;}
        finally {if(own)c.setAutoCommit(true);}
    }
    static void save(Connection c,long character,List<PartyMember> members)throws SQLException {
        try(var q=c.prepareStatement("DELETE FROM character_pokemon WHERE character_id=?")){q.setLong(1,character);q.executeUpdate();}
        try(var q=c.prepareStatement("INSERT INTO character_pokemon(individual_id,character_id,slot,species,individual_data) VALUES (?,?,?,?,?)")) {
            for(int i=0;i<members.size();i++){var m=members.get(i);if(m.individual()==null)throw new SQLException("Missing individual");q.setLong(1,m.individual().id());q.setLong(2,character);q.setInt(3,i);q.setInt(4,m.species());q.setBytes(5,m.individual().encode());q.addBatch();}q.executeBatch();
        }
    }
    /** 箱子记录单独存表，读取时按箱位排序并重建与个体一致的显示数据。 */
    static List<Boxed> loadBoxes(Connection c,long character)throws SQLException {
        List<Boxed> result=new ArrayList<>();
        try(var q=c.prepareStatement("SELECT box,slot,species,flags,level,hp,individual_id,individual_data FROM character_box_pokemon WHERE character_id=? ORDER BY box,slot")) {
            q.setLong(1,character);
            try(var r=q.executeQuery()){while(r.next()) {
                try {
                    var d=PokemonIndividual.decode(r.getBytes(8));
                    if(d.id()!=r.getLong(7))throw new SQLException("Individual identity mismatch");
                    int species=r.getInt(3),flags=r.getInt(4),level=r.getInt(5);
                    if((flags&~3)!=0)throw new SQLException("Invalid stored box flags");
                    int max=d.stat(species,level,0);
                    result.add(new Boxed(r.getInt(1),r.getInt(2),new PartyMember(species,(flags&1)!=0,(flags&2)!=0,Math.min(r.getInt(6),max),max,level,d)));
                } catch(java.io.IOException e){throw new SQLException("Invalid stored box Pokemon",e);}
                catch(IllegalArgumentException e){throw new SQLException("Inconsistent stored box Pokemon",e);}
            }}
        }
        return List.copyOf(result);
    }
    static void saveBoxes(Connection c,long character,List<Boxed> boxed)throws SQLException {
        try(var q=c.prepareStatement("DELETE FROM character_box_pokemon WHERE character_id=?")){q.setLong(1,character);q.executeUpdate();}
        try(var q=c.prepareStatement("INSERT INTO character_box_pokemon(individual_id,character_id,box,slot,species,flags,level,hp,individual_data) VALUES (?,?,?,?,?,?,?,?,?)")) {
            for(var b:boxed){var m=b.member();if(m.individual()==null)throw new SQLException("Missing individual");
                q.setLong(1,m.individual().id());q.setLong(2,character);q.setInt(3,b.box());q.setInt(4,b.slot());
                q.setInt(5,m.species());q.setInt(6,m.flags());q.setInt(7,m.level());q.setInt(8,m.hp());
                q.setBytes(9,m.individual().encode());q.addBatch();}
            q.executeBatch();
        }
    }
    /** 队伍与箱子在同一事务内落库时复用同一套数组同步逻辑。 */
    static void sync(Connection c,long character,List<PartyMember> members)throws SQLException {
        var hp=c.createArrayOf("integer",members.stream().map(PartyMember::hp).toArray());
        var max=c.createArrayOf("integer",members.stream().map(PartyMember::maxHp).toArray());
        var levels=c.createArrayOf("smallint",members.stream().map(m->(short)m.level()).toArray());
        try(var q=c.prepareStatement("UPDATE characters SET party_hp=?,party_max_hp=?,party_levels=? WHERE id=?")){q.setArray(1,hp);q.setArray(2,max);q.setArray(3,levels);q.setLong(4,character);q.executeUpdate();}
        finally{hp.free();max.free();levels.free();}
    }
}
