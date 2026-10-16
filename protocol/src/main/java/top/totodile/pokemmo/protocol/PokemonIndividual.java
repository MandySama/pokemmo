package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** 角色所属宝可梦的持久化个体资料；槽位变化不改变身份或训练记录。 */
public record PokemonIndividual(long id,String nickname,long originalTrainerId,String originalTrainer,int nature,int ability,int gender,
        int friendship,long experience,int heldItem,int ball,long metAt,int metRegion,int metGroup,int metMap,int metLevel,
        List<Integer> ivs,List<Integer> evs,List<Move> moves,int marks,List<Integer> ribbons,List<Integer> contest,List<Integer> particles,int form,String status) {
    public PokemonIndividual(long id,String nickname,long originalTrainerId,String originalTrainer,int nature,int ability,int gender,
            int friendship,long experience,int heldItem,int ball,long metAt,int metRegion,int metGroup,int metMap,int metLevel,
            List<Integer> ivs,List<Integer> evs,List<Move> moves,int marks,List<Integer> ribbons,List<Integer> contest,List<Integer> particles,int form) {
        this(id,nickname,originalTrainerId,originalTrainer,nature,ability,gender,friendship,experience,heldItem,ball,metAt,metRegion,metGroup,metMap,metLevel,ivs,evs,moves,marks,ribbons,contest,particles,form, "");
    }
    public PokemonIndividual(long id,String nickname,long originalTrainerId,String originalTrainer,int nature,int ability,int gender,
            int friendship,long experience,int heldItem,int ball,long metAt,int metRegion,int metGroup,int metMap,int metLevel,
            List<Integer> ivs,List<Integer> evs,List<Move> moves,int marks,List<Integer> ribbons,List<Integer> contest,List<Integer> particles) {
        this(id,nickname,originalTrainerId,originalTrainer,nature,ability,gender,friendship,experience,heldItem,ball,metAt,
                metRegion,metGroup,metMap,metLevel,ivs,evs,moves,marks,ribbons,contest,particles,0);
    }
    public record Move(int id,int pp,int maxPp,int ppUps) {
        public Move {if(id<1||id>1000||pp<0||maxPp<1||pp>maxPp||maxPp>160||ppUps<0||ppUps>3)throw new IllegalArgumentException("Invalid move");}
    }
    public PokemonIndividual {
        if (!List.of("", "brn", "par", "slp", "frz", "psn", "tox").contains(status)) throw new IllegalArgumentException("Invalid status");
        if (form < 0 || form > 3) throw new IllegalArgumentException("Invalid Pokemon form");
        ivs=List.copyOf(ivs);evs=List.copyOf(evs);moves=List.copyOf(moves);ribbons=List.copyOf(ribbons);contest=List.copyOf(contest);particles=List.copyOf(particles);
        if(id<=0||originalTrainerId<=0||nickname==null||nickname.length()>16||originalTrainer==null||originalTrainer.isBlank()||originalTrainer.length()>16||nature<0||nature>24||ability<1||ability>310||gender<0||gender>2||friendship<0||friendship>255||experience<0||experience>1640000||heldItem<0||heldItem>65535||ball<1||ball>65535||metAt<0||metRegion<0||metRegion>5||metGroup<0||metGroup>255||metMap<0||metMap>255||metLevel<1||metLevel>100||marks<0||marks>31)throw new IllegalArgumentException("Invalid Pokemon individual");
        if(ivs.size()!=6||evs.size()!=6||contest.size()!=6||moves.size()>4||ribbons.size()>64||particles.size()>64)throw new IllegalArgumentException("Invalid individual arrays");
        for(int v:ivs)if(v<0||v>31)throw new IllegalArgumentException("Invalid IV");
        for(int v:evs)if(v<0||v>252)throw new IllegalArgumentException("Invalid EV");
        if(evs.stream().mapToInt(Integer::intValue).sum()>510)throw new IllegalArgumentException("Invalid EV total");
        for(int v:contest)if(v<0||v>255)throw new IllegalArgumentException("Invalid contest stat");
        for(int v:ribbons)if(v<1||v>65535)throw new IllegalArgumentException("Invalid ribbon");
        for(int v:particles)if(v<1||v>65535)throw new IllegalArgumentException("Invalid particle");
        if(moves.stream().map(Move::id).distinct().count()!=moves.size())throw new IllegalArgumentException("Duplicate move");
    }
    public static PokemonIndividual create(int species,int level,boolean alpha,long trainerId,String trainer,int region,int group,int map,long now) {
        var random=ThreadLocalRandom.current();var rule=PokemonCatalog.get(species);
        List<Integer> ivs=new ArrayList<>();for(int i=0;i<6;i++)ivs.add(random.nextInt(32));
        if(alpha){List<Integer> indices=new ArrayList<>(List.of(0,1,2,3,4,5));Collections.shuffle(indices);for(int i=0;i<3;i++)ivs.set(indices.get(i),31);}
        int gender=rule.gender()==255?2:rule.gender()==254?1:rule.gender()==0?0:random.nextInt(256)<rule.gender()?1:0;
        int ability=rule.ability2()>0&&random.nextBoolean()?rule.ability2():rule.ability1();
        return new PokemonIndividual(random.nextLong(1,Long.MAX_VALUE),"",trainerId,trainer,random.nextInt(25),ability,gender,rule.friendship(),PokemonCatalog.experience(rule.growth(),level),0,4,now,region,group,map,level,ivs,List.of(0,0,0,0,0,0),PokemonCatalog.moves(species,level),0,List.of(),List.of(0,0,0,0,0,0),List.of());
    }
    public PokemonIndividual battleState(List<Move> nextMoves, String nextStatus) {
        return new PokemonIndividual(id,nickname,originalTrainerId,originalTrainer,nature,ability,gender,friendship,
                experience,heldItem,ball,metAt,metRegion,metGroup,metMap,metLevel,ivs,evs,nextMoves,marks,ribbons,contest,particles,form,nextStatus);
    }

    public PokemonIndividual withNickname(String name) {
        return new PokemonIndividual(id,name,originalTrainerId,originalTrainer,nature,ability,gender,friendship,experience,heldItem,ball,metAt,metRegion,metGroup,metMap,metLevel,ivs,evs,moves,marks,ribbons,contest,particles,form,status);
    }
    public int stat(int species,int level,int index){return PokemonCatalog.stat(species,level,nature,index,ivs.get(index),evs.get(index));}
    public byte[] encode(){PacketData data=new PacketData(1024);write(data);return data.written();}
    public static PokemonIndividual decode(byte[] bytes)throws IOException {var data=new PacketData(bytes);var result=read(data);data.end();return result;}
    void write(PacketData d){
        d.putByte(4).putLong(id).putString(nickname).putLong(originalTrainerId).putString(originalTrainer).putByte(nature).putShort(ability).putByte(gender).putByte(friendship).putLong(experience).putShort(heldItem).putShort(ball).putLong(metAt).putByte(metRegion).putByte(metGroup).putByte(metMap).putByte(metLevel);
        for(int v:ivs)d.putByte(v);for(int v:evs)d.putByte(v);d.putByte(moves.size());for(var m:moves)d.putShort(m.id()).putByte(m.pp()).putByte(m.maxPp()).putByte(m.ppUps());
        d.putByte(marks).putByte(ribbons.size());for(int v:ribbons)d.putShort(v);for(int v:contest)d.putByte(v);d.putByte(particles.size());for(int v:particles)d.putShort(v);d.putByte(form).putString(status);
    }
    static PokemonIndividual read(PacketData d)throws IOException {
        try {
            int version = d.byteValue(); if(version != 1 && version != 2 && version != 3 && version != 4)throw new IOException("Unsupported individual format");
            long id=d.longValue();String nick=d.string(16);long trainerId=d.longValue();String trainer=d.string(16);int nature=d.byteValue(),ability=version == 1 ? d.byteValue() : d.shortValue(),gender=d.byteValue(),friendship=d.byteValue();long exp=d.longValue();int item=d.shortValue(),ball=d.shortValue();long met=d.longValue();int region=d.byteValue(),group=d.byteValue(),map=d.byteValue(),level=d.byteValue();
            List<Integer> iv=new ArrayList<>(),ev=new ArrayList<>();for(int i=0;i<6;i++)iv.add(d.byteValue());for(int i=0;i<6;i++)ev.add(d.byteValue());
            int n=d.byteValue();if(n>4)throw new IOException("Too many moves");List<Move> moves=new ArrayList<>();for(int i=0;i<n;i++)moves.add(new Move(d.shortValue(),d.byteValue(),d.byteValue(),d.byteValue()));
            int marks=d.byteValue();n=d.byteValue();if(n>64)throw new IOException("Too many ribbons");List<Integer> ribbons=new ArrayList<>();for(int i=0;i<n;i++)ribbons.add(d.shortValue());List<Integer> contest=new ArrayList<>();for(int i=0;i<6;i++)contest.add(d.byteValue());
            n=d.byteValue();if(n>64)throw new IOException("Too many particles");List<Integer> particles=new ArrayList<>();for(int i=0;i<n;i++)particles.add(d.shortValue());
            int form = version >= 3 ? d.byteValue() : 0;
            String status = version >= 4 ? d.string(3) : "";
            return new PokemonIndividual(id,nick,trainerId,trainer,nature,ability,gender,friendship,exp,item,ball,met,region,group,map,level,iv,ev,moves,marks,ribbons,contest,particles,form,status);
        }catch(IllegalArgumentException e){throw new IOException("Invalid individual",e);}
    }
}
