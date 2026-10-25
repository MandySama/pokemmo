package top.totodile.pokemmo.server.game.battle;

import top.totodile.pokemmo.server.game.EncounterData;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;

import java.util.*;

/** Transient combat state; never silently writes simulator rules back into the character save. */
public record BattleState(
        String engine,
        int turn,
        boolean ended,
        String winner,
        boolean forceSwitch,
        List<Combatant> pokemon,
        List<String> logs,
        List<TurnMove> actions,
        List<Event> events) {
    /**
     * Ordered simulator results: each HP value is authoritative, never reconstructed from final
     * totals.
     */
    public record Event(
            String type,
            String casterId,
            String targetId,
            int move,
            int hp,
            int maxHp,
            String stat,
            int amount,
            String status,
            boolean direct,
            boolean animationSuppressed,
            List<String> targetIds) {
        public Event {
            targetIds = targetIds == null ? List.of() : List.copyOf(targetIds);
        }

        public Event(
                String type,
                String casterId,
                String targetId,
                int move,
                int hp,
                int maxHp,
                String stat,
                int amount,
                String status,
                boolean direct,
                boolean animationSuppressed) {
            this(
                    type,
                    casterId,
                    targetId,
                    move,
                    hp,
                    maxHp,
                    stat,
                    amount,
                    status,
                    direct,
                    animationSuppressed,
                    List.of());
        }
    }

    public record TurnMove(String casterId, String targetId, int move) {}

    public record Move(int id, int pp, int maxPp, boolean disabled, String disabledSource) {}

    public record Combatant(
            String id,
            int side,
            boolean active,
            int hp,
            int maxHp,
            String status,
            List<Integer> stats,
            List<Move> moves,
            Map<String, Integer> boosts) {
        public Combatant {
            boosts = boosts == null ? Map.of() : Map.copyOf(boosts);
        }
    }

    public BattleState {
        pokemon = List.copyOf(pokemon);
        logs = List.copyOf(logs);
        actions = List.copyOf(actions);
        events = List.copyOf(events);
    }

    public Combatant active(int side) {
        return pokemon.stream()
                .filter(p -> p.side() == side && p.active())
                .findFirst()
                .orElseThrow();
    }

    public Pokemon apply(Pokemon p) {
        var c =
                pokemon.stream()
                        .filter(value -> value.id().equals(p.id()))
                        .findFirst()
                        .orElse(null);
        if (c == null) return p;
        var pp = new ArrayList<Integer>();
        for (int i = 0; i < 4; i++) {
            int id = p.moves().get(i);
            pp.add(
                    c.moves().stream()
                            .filter(m -> m.id() == id)
                            .map(Move::pp)
                            .findFirst()
                            .orElse(0));
        }
        int status =
                switch (c.status()) {
                    case "slp" -> 7;
                    case "psn" -> 8;
                    case "brn" -> 16;
                    case "frz" -> 32;
                    case "par" -> 64;
                    case "tox" -> -128;
                    default -> 0;
                };
        return new Pokemon(
                p.id(),
                p.container(),
                p.position(),
                p.species(),
                p.form(),
                p.gender(),
                p.shiny(),
                p.egg(),
                p.name(),
                p.level(),
                c.hp(),
                status,
                p.item(),
                p.nature(),
                p.ability(),
                c.stats(),
                p.ivs(),
                p.evs(),
                p.moves(),
                p.friendship(),
                p.hiddenAbility(),
                p.alpha(),
                p.marks(),
                pp,
                p.ppUps(),
                p.origin(),
                p.honors(),
                p.ball(),
                p.secretShiny(),
                p.experience());
    }

    public EncounterData apply(EncounterData encounter) {
        var own = active(0);
        var party = encounter.party().stream().map(this::apply).toList();
        var player = party.stream().filter(p -> p.id().equals(own.id())).findFirst().orElseThrow();
        return new EncounterData(
                player, encounter.wilds().stream().map(this::apply).toList(), party);
    }
}
