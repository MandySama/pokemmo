package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** TrainerTeamManager / TrainerPokemonData.convertPokemonData. Configured teams and prizes. */
final class TrainerEncounters {
    static EncounterData create(List<PokemonData.Pokemon> party,Map<Integer,GameService.SpeciesTemplate> registry,Random random) {
        try(var input=TrainerEncounters.class.getResourceAsStream("/world/trainers.json")) {
            if(input==null) throw new IOException("Missing trainer teams");
            var gson=new Gson();
            var rows=JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonArray();
            var row=rows.get(random.nextInt(rows.size())).getAsJsonObject();
            return create(row,party,registry,random);
        } catch(IOException error) {throw new IllegalStateException(error);}
    }
    static EncounterData create(JsonObject row,List<PokemonData.Pokemon> party,Map<Integer,GameService.SpeciesTemplate> registry,Random random) {
        var gson=new Gson();
            var options=row.getAsJsonArray("battleTeams");
            var mons=options.get(random.nextInt(options.size())).getAsJsonObject().getAsJsonArray("pokemons");
            var team=new ArrayList<PokemonData.Pokemon>();
            int[] reorder={0,1,2,4,5,3},nature={1,2,5,3,4};
            for(var value:mons) {
                var mon=value.getAsJsonObject(); int species=mon.get("pokemonIndexId").getAsInt();
                var template=registry.get(species); int level=mon.get("level").getAsInt();
                if(template==null) throw new IllegalStateException("Missing trainer species "+species);
                var base=PokemonStore.createWildPokemon(template,level,9,team.size(),random);
                var object=gson.toJsonTree(base).getAsJsonObject();
                int personality=mon.get("personalityValue").getAsInt(),n=Math.floorMod(personality,25);
                var ivs=new ArrayList<Integer>();var evs=new ArrayList<Integer>();var stats=new ArrayList<Integer>();
                for(int i=0;i<6;i++) {ivs.add(mon.getAsJsonArray("pokemonIvs").get(reorder[i]).getAsInt());evs.add(mon.getAsJsonArray("pokemonEvs").get(reorder[i]).getAsInt());}
                for(int i=0;i<6;i++) {
                    int stat=(2*template.stats().get(i)+ivs.get(i)+evs.get(i)/4)*level/100+(i==0?level+10:5);
                    if(i!=0&&nature[n/5]!=nature[n%5]) stat=(int)(stat*(i==nature[n/5]?1.1:i==nature[n%5]?.9:1));
                    if(i==0&&species==292)stat=1;stats.add(stat);
                }
                object.addProperty("shiny",false);object.addProperty("secretShiny",false);object.addProperty("nature",n);
                object.addProperty("hp",stats.get(0));object.add("stats",gson.toJsonTree(stats));
                object.add("ivs",gson.toJsonTree(ivs));object.add("evs",gson.toJsonTree(evs));
                int ability=mon.get("ability").getAsInt();
                object.addProperty("ability",template.abilities().get(Math.min(ability,template.abilities().size()-1)));
                boolean hasMoves=false;for(var move:mon.getAsJsonArray("moves"))if(move.getAsInt()>0)hasMoves=true;
                if(hasMoves)object.add("moves",mon.get("moves"));
                object.addProperty("item",Math.max(0,mon.get("item").getAsInt()));
                object.addProperty("ball",mon.get("ballType").getAsInt());
                team.add(gson.fromJson(object,PokemonData.Pokemon.class));
            }
            var lead=party.stream().filter(p->!p.egg()&&p.hp()>0).findFirst().orElse(null);
            return lead==null?null:new EncounterData(lead,team,party,new EncounterData.Trainer(row.get("trainerTeamId").getAsInt(),
                    0,row.get("trainerModelIndexId").getAsInt(),row.get("money").getAsInt()));
    }

}
