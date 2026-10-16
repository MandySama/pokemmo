package top.totodile.pokemmo.protocol;

import java.util.random.RandomGenerator;

/** 按接受的移动步数计算遭遇；地形切换、冷却和累积概率独立于帧率。 */
public final class WildEncounterState {
    private int steps, buff, previous = -1;
    public void reset() { steps = buff = 0; previous = -1; }
    public EncounterCatalog.Selection step(EncounterCatalog.Table table, int behavior, boolean cycling, RandomGenerator random) {
        if (table == null) { previous = -1; return null; }
        boolean changed = previous != behavior; previous = behavior;
        if (changed && random.nextInt(100) >= 60) return null;
        int rate = table.rate();
        int minimumSteps = rate >= 80 ? 0 : rate < 10 ? 8 : 8 - rate / 10;
        if (steps < minimumSteps) {
            steps++;
            if (random.nextInt(100) >= 5) return null;
        }
        int effective = rate * 16;
        if (cycling) effective = effective * 80 / 100;
        effective = Math.min(1600, effective + buff * 16 / 200);
        if (random.nextInt(1600) >= effective) { buff = Math.min(20000, buff + rate); return null; }
        reset();
        return table.roll(random);
    }
}
