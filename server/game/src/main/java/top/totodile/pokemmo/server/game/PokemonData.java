package top.totodile.pokemmo.server.game;

import java.util.List;

/** PokemonContainerType / PokemonData snapshots supplied by the character service. */
public final class PokemonData {
    private PokemonData() {}
    public static final int PC=0, PARTY=1, PARTY_SIZE=6, BOX_SIZE=60, BASE_BOXES=11;
    public record Origin(int address,int level,int region,long time,String trainer,int personality,boolean fateful,boolean raid,List<Integer> inheritedMoves) {
        public Origin(int address,int level,int region,long time,String trainer,int personality,boolean fateful,boolean raid){this(address,level,region,time,trainer,personality,fateful,raid,List.of());}
        public Origin { inheritedMoves=List.copyOf(inheritedMoves); }
        public static Origin unknown(){return new Origin(Integer.MIN_VALUE,0,-1,0,"",0,false,false);}
    }
    public record Honors(List<Integer> condition,List<Integer> ribbons,List<Integer> contestRibbons,List<Integer> particles,int selectedParticle) {
        public Honors {condition=List.copyOf(condition);ribbons=List.copyOf(ribbons);contestRibbons=List.copyOf(contestRibbons);particles=List.copyOf(particles);
            if(condition.size()!=5||contestRibbons.size()!=5||condition.stream().anyMatch(v->v<0||v>255)||contestRibbons.stream().anyMatch(v->v<0||v>4)||ribbons.stream().anyMatch(v->v<0||v>34)||particles.stream().anyMatch(v->v<0||v>38)||selectedParticle< -3||selectedParticle>38)throw new IllegalArgumentException("Invalid Pokemon honors");
        }
        public static Honors empty(){return new Honors(List.of(0,0,0,0,0),List.of(),List.of(0,0,0,0,0),List.of(),-1);}
    }
    public record Pokemon(String id,int container,int position,int species,int form,int gender,
                          boolean shiny,boolean egg,String name,int level,int hp,int status,int item,
                          int nature,int ability,List<Integer> stats,List<Integer> ivs,List<Integer> evs,List<Integer> moves,int friendship,boolean hiddenAbility,boolean alpha,int marks,List<Integer> pp,List<Integer> ppUps,Origin origin,Honors honors,int ball) {
        public Pokemon(String id,int container,int position,int species,int form,int gender,boolean shiny,boolean egg,String name,int level,int hp,int status,int item,int nature,int ability,List<Integer> stats,List<Integer> ivs,List<Integer> evs,List<Integer> moves){
            this(id,container,position,species,form,gender,shiny,egg,name,level,hp,status,item,nature,ability,stats,ivs,evs,moves,0,false,false,0);
        }
        public Pokemon(String id,int container,int position,int species,int form,int gender,boolean shiny,boolean egg,String name,int level,int hp,int status,int item,int nature,int ability,List<Integer> stats,List<Integer> ivs,List<Integer> evs,List<Integer> moves,int friendship){
            this(id,container,position,species,form,gender,shiny,egg,name,level,hp,status,item,nature,ability,stats,ivs,evs,moves,friendship,false,false,0);
        }
        public Pokemon(String id,int container,int position,int species,int form,int gender,boolean shiny,boolean egg,String name,int level,int hp,int status,int item,int nature,int ability,List<Integer> stats,List<Integer> ivs,List<Integer> evs,List<Integer> moves,int friendship,boolean hiddenAbility,boolean alpha,int marks){
            this(id,container,position,species,form,gender,shiny,egg,name,level,hp,status,item,nature,ability,stats,ivs,evs,moves,friendship,hiddenAbility,alpha,marks,List.of(-1,-1,-1,-1),List.of(0,0,0,0));
        }
        public Pokemon(String id,int container,int position,int species,int form,int gender,boolean shiny,boolean egg,String name,int level,int hp,int status,int item,int nature,int ability,List<Integer> stats,List<Integer> ivs,List<Integer> evs,List<Integer> moves,int friendship,boolean hiddenAbility,boolean alpha,int marks,List<Integer> pp,List<Integer> ppUps){
            this(id,container,position,species,form,gender,shiny,egg,name,level,hp,status,item,nature,ability,stats,ivs,evs,moves,friendship,hiddenAbility,alpha,marks,pp,ppUps,Origin.unknown());
        }
        public Pokemon(String id,int container,int position,int species,int form,int gender,boolean shiny,boolean egg,String name,int level,int hp,int status,int item,int nature,int ability,List<Integer> stats,List<Integer> ivs,List<Integer> evs,List<Integer> moves,int friendship,boolean hiddenAbility,boolean alpha,int marks,List<Integer> pp,List<Integer> ppUps,Origin origin){
            this(id,container,position,species,form,gender,shiny,egg,name,level,hp,status,item,nature,ability,stats,ivs,evs,moves,friendship,hiddenAbility,alpha,marks,pp,ppUps,origin,Honors.empty());
        }
        public Pokemon(String id,int container,int position,int species,int form,int gender,boolean shiny,boolean egg,String name,int level,int hp,int status,int item,int nature,int ability,List<Integer> stats,List<Integer> ivs,List<Integer> evs,List<Integer> moves,int friendship,boolean hiddenAbility,boolean alpha,int marks,List<Integer> pp,List<Integer> ppUps,Origin origin,Honors honors){
            this(id,container,position,species,form,gender,shiny,egg,name,level,hp,status,item,nature,ability,stats,ivs,evs,moves,friendship,hiddenAbility,alpha,marks,pp,ppUps,origin,honors,3);
        }
        public Pokemon {
            stats=List.copyOf(stats);ivs=List.copyOf(ivs);evs=List.copyOf(evs);moves=List.copyOf(moves);pp=List.copyOf(pp);ppUps=List.copyOf(ppUps);
            if(ball<0||ball>24||honors==null||origin==null||id==null||id.isBlank()||name==null||species<1||species>649||form<0||gender< -1||gender>1
                    ||pp.size()!=4||ppUps.size()!=4||pp.stream().anyMatch(v->v< -1||v>64)||ppUps.stream().anyMatch(v->v<0||v>3)||marks<0||marks>31||friendship<0||friendship>255||level<1||level>100||stats.size()!=6||ivs.size()!=6||evs.size()!=6||moves.size()!=4
                    ||stats.stream().anyMatch(v->v<1)||ivs.stream().anyMatch(v->v<0||v>31)
                    ||evs.stream().anyMatch(v->v<0||v>252)||hp<0||hp>stats.get(0))throw new IllegalArgumentException("Invalid Pokemon snapshot");
        }
        public int maxHp(){return stats.get(0);}
    }
    public record Containers(List<Pokemon> pokemons,int boxes) {
        public Containers {pokemons=List.copyOf(pokemons);}
        public static Containers empty(){return new Containers(List.of(),BASE_BOXES);}
        public Pokemon at(int container,int position){return pokemons.stream().filter(p->p.container()==container&&p.position()==position).findFirst().orElse(null);}
    }
    /** Same old/new slot contract as ChangePokemonPosPacket. */
    public record Move(int oldContainer,int oldPosition,int newContainer,int newPosition) {}
    public enum Failure { INVALID_MOVE, LAST_PARTY_POKEMON }
    public static final class Rejected extends RuntimeException {
        public final Failure reason;
        public Rejected(Failure reason){super(reason.name());this.reason=reason;}
    }
}
