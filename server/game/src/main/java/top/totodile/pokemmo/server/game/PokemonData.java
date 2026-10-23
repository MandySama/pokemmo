package top.totodile.pokemmo.server.game;

import java.util.List;

/** PokemonContainerType / PokemonData snapshots supplied by the character service. */
public final class PokemonData {
    private PokemonData() {}
    public static final int PC=0, PARTY=1, PARTY_SIZE=6, BOX_SIZE=60, BASE_BOXES=11;
    public record Pokemon(String id,int container,int position,int species,int form,int gender,
                          boolean shiny,boolean egg,String name,int level,int hp,int status,int item,
                          int nature,int ability,List<Integer> stats,List<Integer> ivs,List<Integer> evs,List<Integer> moves) {
        public Pokemon {
            stats=List.copyOf(stats);ivs=List.copyOf(ivs);evs=List.copyOf(evs);moves=List.copyOf(moves);
            if(id==null||id.isBlank()||name==null||species<1||species>649||form<0||gender< -1||gender>1
                    ||level<1||level>100||stats.size()!=6||ivs.size()!=6||evs.size()!=6||moves.size()!=4
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
