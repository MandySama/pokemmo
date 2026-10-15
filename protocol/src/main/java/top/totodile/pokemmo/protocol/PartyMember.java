package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 队伍显示所需的种族与特殊标记，槽位顺序由服务端角色数据决定。 */
public record PartyMember(int species, boolean shiny, boolean alpha, int hp, int maxHp, int level, PokemonIndividual individual) {
    public PartyMember(int species,boolean shiny,boolean alpha,int hp,int maxHp,int level) {this(species,shiny,alpha,hp,maxHp,level,null);}
    public PartyMember(int species, boolean shiny, boolean alpha, int hp, int maxHp) { this(species, shiny, alpha, hp, maxHp, 0); }
    public PartyMember(int species, boolean shiny, boolean alpha) { this(species, shiny, alpha, 100, 100); }
    public PartyMember {
        if (level < 0 || level > 100) throw new IllegalArgumentException("Invalid party level");
        if (hp < 0 || maxHp < 1 || maxHp > 65535 || hp > maxHp) throw new IllegalArgumentException("Invalid party HP");
        if (species < 1 || species > PokemonCatalog.MAX_SPECIES) throw new IllegalArgumentException("Invalid party species");
        if(individual!=null){
            var rule=PokemonCatalog.get(species);
            long min=PokemonCatalog.experience(rule.growth(),Math.max(1,level));
            long next=PokemonCatalog.experience(rule.growth(),Math.min(100,level+1));
            if(level<1||maxHp!=individual.stat(species,level,0)||individual.experience()<min||(level<100?individual.experience()>=next:individual.experience()!=min))throw new IllegalArgumentException("Inconsistent individual stats");
            if(individual.ability()!=rule.ability1()&&individual.ability()!=rule.ability2()&&individual.ability()!=rule.hiddenAbility())throw new IllegalArgumentException("Invalid species ability");
        }
    }

    public int flags() { return (shiny ? 1 : 0) | (alpha ? 2 : 0); }

    public static List<PartyMember> validate(List<PartyMember> members) {
        List<PartyMember> result = List.copyOf(members);
        if (result.size() > 6) throw new IllegalArgumentException("Too many party members");
        return result;
    }

    static void write(PacketData data, List<PartyMember> members) {
        data.putByte(members.size());
        for (PartyMember member : members) {data.putShort(member.species()).putByte(member.flags()).putShort(member.hp()).putShort(member.maxHp()).putByte(member.level()).putByte(member.individual()==null?0:1);if(member.individual()!=null)member.individual().write(data);}
    }

    static List<PartyMember> read(PacketData data) throws IOException {
        int count = data.byteValue();
        if (count > 6) throw new IOException("Too many party members");
        List<PartyMember> members = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int species = data.shortValue(), flags = data.byteValue();
            if (species < 1 || species > PokemonCatalog.MAX_SPECIES || (flags & ~3) != 0) throw new IOException("Invalid party member");
            int hp = data.shortValue(), maxHp = data.shortValue();
            if (maxHp == 0 || hp > maxHp) throw new IOException("Invalid party HP");
            int level = data.byteValue();
            if (level > 100) throw new IOException("Invalid party level");
            try {members.add(new PartyMember(species, (flags & 1) != 0, (flags & 2) != 0, hp, maxHp, level, data.booleanValue()?PokemonIndividual.read(data):null));}
            catch(IllegalArgumentException error){throw new IOException("Invalid party individual",error);}
        }
        return List.copyOf(members);
    }
}
