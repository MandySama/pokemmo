package top.totodile.pokemmo.client.rom.nds.bw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.random.RandomGenerator;

/** BlackWhiteRom.Ep/jx, BwTrainerDataEntry.A5 and CharacterAvatarAccessoryProvider.KN. */
public record BwNpcTrainer(int id, int portrait, String type, String name,
                           BwPokemonSprites.Pixels pixels) {
    public static BwNpcTrainer specified(BwPokedex dex,top.totodile.pokemmo.client.resources.ClientResources resources,
            top.totodile.pokemmo.server.game.EncounterData.Trainer identity)throws IOException{
        if(identity.region()==0||identity.region()==1){
            var rom=resources.roms().get(identity.region()==0?top.totodile.pokemmo.client.rom.RomType.FIRE_RED:top.totodile.pokemmo.client.rom.RomType.EMERALD);
            if(rom==null)throw new IOException("Missing trainer's regional ROM");
            var trainer=top.totodile.pokemmo.client.rom.gba.GbaTrainer.load(rom,identity.teamId(),identity.model());
            int typeId=190000+identity.region()*120+trainer.trainerClass();
            String type=resources.strings().get(typeId);if(type.equals("STRING_"+typeId))type=trainer.type();
            int nameId=195000+identity.region()*1000+identity.teamId();
            String name=identity.teamId()<0?trainer.name():resources.strings().get(nameId);
            if(name.equals("STRING_"+nameId))name=trainer.name();
            if(identity.region()==0&&(trainer.trainerClass()==81||trainer.trainerClass()==89||trainer.trainerClass()==90)){
                name=resources.strings().get(300095);if(name.equals("STRING_300095"))name="GREEN";
            }
            return new BwNpcTrainer(identity.teamId(),trainer.portrait(),type,name,trainer.pixels());
        }
        if(identity.region()==3||identity.region()==4){
            var rom=resources.roms().get(identity.region()==3?top.totodile.pokemmo.client.rom.RomType.PLATINUM:top.totodile.pokemmo.client.rom.RomType.HEART_GOLD_SOUL_SILVER);
            if(rom==null)throw new IOException("Missing trainer's regional ROM");
            var trainer=top.totodile.pokemmo.client.rom.nds.gen4.Gen4Trainer.load(rom,identity.teamId(),identity.model());
            int typeId=190000+identity.region()*120+trainer.trainerClass();
            String type=resources.strings().get(typeId);if(type.equals("STRING_"+typeId))type=trainer.type();
            int nameId=195000+identity.region()*1000+identity.teamId();
            String name=resources.strings().get(nameId);if(name.equals("STRING_"+nameId))name=trainer.name();
            if(identity.region()==4)try(var input=BwNpcTrainer.class.getResourceAsStream("/story/johto/trainer-names.json")) {
                if(input!=null){
                    var overrides=new com.badlogic.gdx.utils.JsonReader().parse(new java.io.InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8));
                    type=overrides.getString("class:"+trainer.trainerClass(),type);
                    name=overrides.getString("trainer:"+identity.teamId(),name);
                }
            }
            return new BwNpcTrainer(identity.teamId(),trainer.portrait(),type,name,trainer.pixels());
        }
        if(identity.region()==2&&identity.teamId()>=0)return load(dex,identity.teamId());
        throw new IOException("Trainer portrait loader unavailable for region "+identity.region());
    }
    public static BwNpcTrainer random(BwPokedex dex, RandomGenerator random) throws IOException {
        var data = dex.archive("a/0/9/2");
        var candidates = new ArrayList<Integer>();
        for (int i = 0; i < data.size(); i++) {
            byte[] entry = data.readRaw(i);
            if (entry.length < 20 || (entry[3] & 255) == 0) continue;
            int portrait = portrait(entry[1] & 255);
            // These three use a separate animated bitmap path in the reference loader.
            if (portrait != 37 && portrait != 38 && portrait != 40) candidates.add(i);
        }
        if (candidates.isEmpty()) throw new IOException("No BW NPC trainers available");
        return load(dex,candidates.get(random.nextInt(candidates.size())));
    }
    public static BwNpcTrainer load(BwPokedex dex,int id)throws IOException{
        var data=dex.archive("a/0/9/2");
        if(id<0||id>=data.size())throw new IOException("Invalid BW trainer identity");
        int type = data.readRaw(id)[1] & 255, portrait = portrait(type);
        var graphics = dex.archive("a/0/7/2");
        var pixels = BwPokemonSprites.compose(graphics.read(portrait * 8),
                graphics.read(portrait * 8 + 7), graphics.read(portrait * 8 + 2), 0, 80, 0);
        return new BwNpcTrainer(id, portrait, dex.trainerClass(type), dex.trainerName(id), pixels);
    }

    private static int portrait(int type) {
        if (type < 47) return type;
        if (type == 47) return 40;
        if (type < 92) return type - 1;
        return switch (type) {
            case 92 -> 71; case 93 -> 4; case 94 -> 70; case 95 -> 74;
            case 96 -> 75; case 97 -> 69; case 98 -> 42; case 99 -> 41;
            case 100 -> 91; case 101 -> 40; case 102 -> 92; case 103 -> 93;
            case 104 -> 94; default -> 0;
        };
    }
}
