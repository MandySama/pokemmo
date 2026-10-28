package top.totodile.pokemmo.server.game.battle;

import top.totodile.pokemmo.server.game.EncounterData;

/** Engine strategies share reference MOVE / SWAP / FORFEIT action semantics. */
public interface BattleEngine {
    enum Kind {
        CUSTOM,
        SHOWDOWN
    }

    enum Type {
        MOVE,
        TEST_MOVE,
        SWAP,
        FORFEIT
    }

    record Action(Type type, int index, String pokemonId, String targetId) {
        public Action(Type type, int index, String pokemonId) {
            this(type, index, pokemonId, "");
        }

        public Action withTarget(String id) {
            return new Action(type, index, pokemonId, id);
        }

        public static Action move(int slot) {
            return new Action(Type.MOVE, slot, "");
        }

        /** Development-only one-turn move, independent of the Pokemon's learned moves. */
        public static Action testMove(int moveId) {
            return new Action(Type.TEST_MOVE, moveId, "");
        }

        public static Action swap(String id) {
            return new Action(Type.SWAP, 0, id);
        }

        public static Action forfeit() {
            return new Action(Type.FORFEIT, 0, "");
        }
    }

    interface Session extends AutoCloseable {
        BattleState state();

        BattleState choose(Action action);

        default void growth(java.util.List<top.totodile.pokemmo.server.game.PokemonData.Pokemon> pokemon) {}
        default String checkpoint(){return null;}

        @Override
        void close();
    }

    Session start(EncounterData encounter);

    static BattleEngine create(Kind kind) {
        return switch (kind) {
            case SHOWDOWN -> new ShowdownBattleEngine();
            case CUSTOM -> new CustomBattleEngine();
        };
    }
}
