package top.totodile.pokemmo.protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 单人训练家战胜利奖励；金额和基础经验来自已导出的卡带目录。 */
public final class BattleRewards {
    private static final Map<String, Integer> VALUES = load();

    private BattleRewards() { }

    public static int money(int region, TrainerCatalog.Trainer trainer) {
        Integer rate = VALUES.get("money," + region + "," + trainer.id());
        if (rate == null) throw new IllegalArgumentException("Missing trainer prize: " + region + "/" + trainer.id());
        return rate * trainer.mons().getLast().level();
    }

    public static int experience(int region, PartyMember opponent, int participants) {
        Integer yield = VALUES.get("exp," + region + "," + opponent.species());
        if (yield == null) throw new IllegalArgumentException("Missing experience yield: " + opponent.species());
        return Math.max(1, yield * opponent.level() * 3 / (14 * participants));
    }

    public static PartyMember grow(PartyMember member, long gained) {
        var d = member.individual();
        int growth = PokemonCatalog.get(member.species()).growth();
        long experience = Math.min(PokemonCatalog.experience(growth, 100), d.experience() + gained);
        int level = member.level();
        while (level < 100 && experience >= PokemonCatalog.experience(growth, level + 1)) level++;
        var updated = new PokemonIndividual(d.id(), d.nickname(), d.originalTrainerId(), d.originalTrainer(),
                d.nature(), d.ability(), d.gender(), d.friendship(), experience, d.heldItem(), d.ball(), d.metAt(),
                d.metRegion(), d.metGroup(), d.metMap(), d.metLevel(), d.ivs(), d.evs(), d.moves(), d.marks(),
                d.ribbons(), d.contest(), d.particles(), d.form(), d.status());
        int maxHp = updated.stat(member.species(), level, 0);
        return new PartyMember(member.species(), member.shiny(), member.alpha(),
                member.hp() == 0 ? 0 : member.hp() + maxHp - member.maxHp(), maxHp, level, updated);
    }

    private static Map<String, Integer> load() {
        var result = new HashMap<String, Integer>();
        try (var input = BattleRewards.class.getResourceAsStream("/catalog/battle-rewards.csv")) {
            if (input == null) throw new IOException("Missing battle rewards");
            for (String line : new String(input.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                if (line.isBlank() || line.startsWith("#")) continue;
                int split = line.lastIndexOf(',');
                result.put(line.substring(0, split), Integer.parseInt(line.substring(split + 1)));
            }
        } catch (IOException error) { throw new ExceptionInInitializerError(error); }
        return Map.copyOf(result);
    }
}
