package top.totodile.pokemmo.battle;

/** 只插值显示值，结算 HP 由战斗服务持有。 */
public final class BattleHpAnimation {
    private double from, target;
    private long started, duration;

    public void reset(int hp) {
        from = target = Math.max(0, hp);
        duration = 0;
    }

    public long animate(int hp, int maxHp, long now, int minimum, int maximum) {
        from = value(now);
        target = Math.max(0, Math.min(hp, maxHp));
        started = now;
        duration = from == target ? 0 : Math.max(minimum,
                Math.min(maximum, Math.round(Math.abs(target - from) / Math.max(1, maxHp) * maximum)));
        return duration;
    }

    public double value(long now) {
        double progress = duration == 0 ? 1 : Math.max(0, Math.min(1, (double) (now - started) / duration));
        return from + (target - from) * progress;
    }

    public int displayed(long now) { return (int) Math.round(value(now)); }
}
