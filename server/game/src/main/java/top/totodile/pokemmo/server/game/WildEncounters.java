package top.totodile.pokemmo.server.game;

import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** WildEncounterManager tables and WildEncounterService accepted-step rolls. */
final class WildEncounters {
    record Slot(int species, int min, int max, int weight) {}
    record Table(int rate, List<Slot> slots) {}
    record MapTable(int region, int bank, int map, Map<String, Table> tables) {}
    record Selection(int species, int level) {}
    private final Map<String, MapTable> maps = new HashMap<>();
    private int steps, buff, previousBehavior = -1;
    private final Random random;
    WildEncounters(Random random) {
        this.random = random;
        try (var input = WildEncounters.class.getResourceAsStream("/world/kanto-encounters.json")) {
            if (input == null) throw new IOException("Missing encounter tables");
            for (var row : new Gson().fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), MapTable[].class))
                maps.put(key(row.region, row.bank, row.map), row);
        } catch (IOException e) { throw new IllegalStateException(e); }
    }
    private static String key(int region, int bank, int map) { return region+":"+bank+":"+map; }
    void reset() { steps=0; buff=0; previousBehavior=-1; }
    Selection step(int region, GameService.Position before, GameService.Position after,
            int behavior, boolean land, boolean water, boolean bicycle) {
        if (before.bank()!=after.bank() || before.map()!=after.map()) { reset(); return null; }
        if (Math.abs(before.x()-after.x())+Math.abs(before.y()-after.y())!=1) return null;
        if (!land && !water) { previousBehavior=-1; return null; }
        var map=maps.get(key(region,after.bank(),after.map()));
        var table=map==null?null:map.tables.get(water?"water_mons":"land_mons");
        if(table==null || table.rate<=0 || table.slots.isEmpty()) return null;
        boolean changed=previousBehavior!=behavior; previousBehavior=behavior;
        if(changed && random.nextInt(100)>=60) return null;
        int minimum=table.rate>=80?0:table.rate<10?8:8-table.rate/10;
        if(steps<minimum) { steps++; if(random.nextInt(100)>=5) return null; }
        int rate=table.rate*16;
        if(bicycle && land) rate=rate*80/100;
        rate=Math.min(1600,rate+buff*16/200);
        if(random.nextInt(1600)>=rate) { buff+=table.rate; return null; }
        int total=table.slots.stream().mapToInt(Slot::weight).sum();
        int roll=random.nextInt(total);
        for(var slot:table.slots) if((roll-=slot.weight)<0) {
            reset(); return new Selection(slot.species,slot.min+random.nextInt(Math.max(1,slot.max-slot.min+1)));
        }
        return null;
    }
}
