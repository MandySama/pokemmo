package top.totodile.pokemmo.server.game;

import java.util.*;
import java.util.concurrent.CompletionStage;
import top.totodile.pokemmo.server.login.LoginService.Selection;

public interface GameService extends AutoCloseable {
    default CompletionStage<PokemonData.Containers> selectPokemonParticle(Selection selection,String character,String pokemonId,int particle){return java.util.concurrent.CompletableFuture.failedFuture(new Rejected(Failure.NOT_FOUND));}
    default CompletionStage<PokemonData.Containers> setPokemonMarks(Selection selection,String character,String pokemonId,int marks){return java.util.concurrent.CompletableFuture.failedFuture(new Rejected(Failure.NOT_FOUND));}
    default CompletionStage<PokemonData.Containers> movePokemonMove(Selection selection,String character,String pokemonId,int from,int to){return java.util.concurrent.CompletableFuture.failedFuture(new Rejected(Failure.NOT_FOUND));}
    enum PokemonSetting { EV, NATURE, GENDER, ABILITY, IV, FRIENDSHIP, PARTICLE, RIBBON, BALL }
    default CompletionStage<PokemonData.Containers> setPokemonSetting(Selection selection,String character,String pokemonId,PokemonSetting setting,int index,int value){return java.util.concurrent.CompletableFuture.failedFuture(new Rejected(Failure.NOT_FOUND));}
    enum PokemonFlag { SHINY, HIDDEN_ABILITY, ALPHA }
    default CompletionStage<PokemonData.Containers> setPokemonFlag(Selection selection,String character,String pokemonId,PokemonFlag flag,boolean enabled){return java.util.concurrent.CompletableFuture.failedFuture(new Rejected(Failure.NOT_FOUND));}
    /** SendSetFollowPokemonPacket appearance; zero species recalls the follower. */
    record Follower(int species,int form,int gender,boolean shiny) {
        public Follower {if(species<0||species>649||form<0||form>31||gender< -1||gender>1)throw new IllegalArgumentException("Invalid follower");}
        public static Follower none(){return new Follower(0,0,-1,false);}
    }
    default CompletionStage<Follower> follower(Selection selection,String character){return java.util.concurrent.CompletableFuture.completedFuture(Follower.none());}
    /** ChangeFllowPokemonPacket: 0=party position, 1=owned party object, 2=recall. */
    default CompletionStage<Follower> selectFollower(Selection selection,String character,int type,String pokemonId,int position){return java.util.concurrent.CompletableFuture.failedFuture(new Rejected(Failure.NOT_FOUND));}
    record Part(int style, int color) {}
    record Appearance(int gender, int skin, int region, Map<Integer, Part> parts) {
        public Appearance { parts = Map.copyOf(parts); }
    }
    record Character(String id, String name, Appearance appearance, long createdAt) {}
    default CompletionStage<Character> saveAppearance(Selection selection,String characterId,Appearance appearance){return java.util.concurrent.CompletableFuture.failedFuture(new UnsupportedOperationException());}
    record ItemTemplate(int id,int region,int maximumStack) {}
    record LearnedMove(int id,int level) {}
    record SpeciesTemplate(int id,int genderRatio,List<Integer> stats,int ability,List<LearnedMove> moves,List<Integer> abilities) {
        public SpeciesTemplate(int id,int genderRatio,List<Integer> stats,int ability,List<LearnedMove> moves){this(id,genderRatio,stats,ability,moves,List.of(ability));}
        public SpeciesTemplate {stats=List.copyOf(stats);moves=List.copyOf(moves);abilities=List.copyOf(abilities);}
    }
    record Position(int bank, int map, int x, int y, int direction) {
        public static Position kantoStart() { return new Position(4,1,6,6,0); }
        /** Starting map IDs use ROM headers; existing saved positions always take precedence. */
        public static Position start(int region) {
            return switch(region) {
                case 0 -> kantoStart();
                case 1 -> new Position(0,9,5,9,0);
                case 2 -> new Position(1,133,14,14,0);
                case 3 -> new Position(1,155,9,12,0);
                case 4 -> new Position(0,60,12,10,0);
                default -> throw new IllegalArgumentException("Invalid starting region");
            };
        }
        public Position {
            if(bank<0 || bank>255 || map<0 || map>255 || x<0 || x>1023 || y<0 || y>1023 || direction<0 || direction>3)
                throw new IllegalArgumentException("Invalid character position");
        }
    }
    record TravelState(boolean noClip,float speed,boolean bicycle) {
        public static TravelState defaults() { return new TravelState(false,1,false); }
        public TravelState {
            if(!Float.isFinite(speed) || speed<=0 || speed>16)throw new IllegalArgumentException("Invalid character speed");
        }
    }
    /** Reference SendLoadDexPacket: seen, owned, caught and caught-alpha flags per species. */
    record PokedexState(Map<Integer,Integer> entries) {
        public static final int LAST_SPECIES=649;
        public PokedexState {entries=Map.copyOf(entries);for(var entry:entries.entrySet())if(entry.getKey()<1||entry.getKey()>LAST_SPECIES||entry.getValue()<1||entry.getValue()>15)throw new IllegalArgumentException("Invalid Pokédex record");}
        public PokedexState(boolean unlocked){this(all(unlocked));}
        private static Map<Integer,Integer> all(boolean unlocked){var result=new HashMap<Integer,Integer>();if(unlocked)for(int id=1;id<=LAST_SPECIES;id++)result.put(id,15);return result;}
        public boolean has(int species,int type){return (entries.getOrDefault(species,0)&(1<<type))!=0;}
        public long count(int type){return entries.values().stream().filter(flags->(flags&(1<<type))!=0).count();}
        public boolean unlocked(){return entries.size()==LAST_SPECIES&&entries.values().stream().allMatch(flags->flags==15);}
        public static PokedexState defaults() { return new PokedexState(false); }
    }
    /** CharacterData money/channel and SendJoinGameWorldResponsePacket's game clock origin. */
    record WorldInfo(int money,int channel,long timeOrigin) {
        public static WorldInfo defaults(){return new WorldInfo(0,0,1640995200L);}
    }
    record Entry(Character character, Position position,TravelState travelState,PokedexState pokedexState,WorldInfo worldInfo,int region) {
        public Entry(Character character,Position position,TravelState travelState,PokedexState pokedexState,WorldInfo worldInfo){this(character,position,travelState,pokedexState,worldInfo,character.appearance().region());}
        public Entry(Character character,Position position) {this(character,position,TravelState.defaults());}
        public Entry(Character character,Position position,TravelState travelState) {this(character,position,travelState,PokedexState.defaults());}
        public Entry(Character character,Position position,TravelState travelState,PokedexState pokedexState) {
            this(character,position,travelState,pokedexState,WorldInfo.defaults());
        }
    }
    enum Failure { INVALID_NAME, NAME_TAKEN, ACCOUNT_FULL, NOT_FOUND }
    final class Rejected extends RuntimeException {
        public final Failure reason;
        public Rejected(Failure reason) { super(reason.name()); this.reason = reason; }
    }
    CompletionStage<List<Character>> characters(Selection selection);
    CompletionStage<Character> create(Selection selection, String name, Appearance appearance);
    CompletionStage<Void> delete(Selection selection, String characterId);
    CompletionStage<Entry> enter(Selection selection, String characterId);
    CompletionStage<Void> savePosition(Selection selection, String characterId, Position position);
    default CompletionStage<Void> teleport(Selection selection,String characterId,int region,Position position){return java.util.concurrent.CompletableFuture.failedFuture(new UnsupportedOperationException());}
    default CompletionStage<PlayerData.Item> createItem(Selection selection,String characterId,int itemId,int amount){return java.util.concurrent.CompletableFuture.failedFuture(new UnsupportedOperationException());}
    default CompletionStage<List<PlayerData.Item>> replaceInventory(Selection selection,String characterId,java.util.Map<Integer,Integer> items){return java.util.concurrent.CompletableFuture.failedFuture(new UnsupportedOperationException());}
    default CompletionStage<PokemonData.Pokemon> addPokemon(Selection selection,String characterId,int species,int level){return java.util.concurrent.CompletableFuture.failedFuture(new UnsupportedOperationException());}
    default List<ItemTemplate> itemTemplates(){return List.of();}
    /** Local service consumes the same loaded ROM/data.pak registry as the client. */
    default void gameData(List<ItemTemplate> items,List<SpeciesTemplate> species){}
    CompletionStage<Void> saveTravelState(Selection selection,String characterId,TravelState state);
    CompletionStage<Void> savePokedexState(Selection selection,String characterId,PokedexState state);
    default CompletionStage<Void> leave(Selection selection,String characterId){return java.util.concurrent.CompletableFuture.completedFuture(null);}
    /** Payload of the reference SendPokemonPvpLevelInfoPacket (client opcode 79). */
    default byte[] pokedexTiers(){return new byte[0];}
    default CompletionStage<List<PlayerData.Item>> inventory(Selection selection,String characterId){return java.util.concurrent.CompletableFuture.completedFuture(List.of());}
    default CompletionStage<PokemonData.Containers> pokemon(Selection selection,String characterId){return java.util.concurrent.CompletableFuture.completedFuture(PokemonData.Containers.empty());}
    default CompletionStage<PokemonData.Containers> movePokemon(Selection selection,String characterId,List<PokemonData.Move> moves){return java.util.concurrent.CompletableFuture.failedFuture(new PokemonData.Rejected(PokemonData.Failure.INVALID_MOVE));}
    default CompletionStage<PokemonData.Containers> sortPokemon(Selection selection,String characterId,int box){return java.util.concurrent.CompletableFuture.failedFuture(new PokemonData.Rejected(PokemonData.Failure.INVALID_MOVE));}
    default CompletionStage<PlayerData.Trainer> trainer(Selection selection,String characterId){return java.util.concurrent.CompletableFuture.failedFuture(new Rejected(Failure.NOT_FOUND));}
    default CompletionStage<PlayerData.MailPage> mail(Selection selection,String characterId,boolean sent,int page){return java.util.concurrent.CompletableFuture.completedFuture(new PlayerData.MailPage(List.of(),0,0,page,sent));}
    default CompletionStage<PlayerData.Mail> readMail(Selection selection,String characterId,String mailId){return java.util.concurrent.CompletableFuture.failedFuture(new Rejected(Failure.NOT_FOUND));}
    default CompletionStage<Void> deleteMail(Selection selection,String characterId,String mailId,boolean sent){return java.util.concurrent.CompletableFuture.failedFuture(new Rejected(Failure.NOT_FOUND));}
    default CompletionStage<Boolean> openMailbox(Selection selection,String characterId){return java.util.concurrent.CompletableFuture.completedFuture(false);}
    default CompletionStage<Void> closeMailbox(Selection selection,String characterId){return java.util.concurrent.CompletableFuture.completedFuture(null);}
    default CompletionStage<PlayerData.MailResult> sendMail(Selection selection,String characterId,String recipient,String subject,String body){return java.util.concurrent.CompletableFuture.completedFuture(PlayerData.MailResult.ACCESS_DENIED);}
    @Override void close();
}
