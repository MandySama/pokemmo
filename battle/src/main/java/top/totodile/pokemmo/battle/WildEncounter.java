package top.totodile.pokemmo.battle;

/** 展示阶段的单次遭遇生命周期；不结算招式、经验或持久化奖励。 */
public final class WildEncounter {
    public enum Phase { LOADING, FLASH, SLIDE, THROW, RELEASE, READY, ENDING, CLOSED }
    public enum Action { FIGHT, BAG, POKEMON, RUN }
    private Phase phase = Phase.LOADING;
    private long started;
    private long ending;

    public void loaded(long milliseconds) {
        if (phase != Phase.LOADING) throw new IllegalStateException("Encounter already loaded");
        started = milliseconds;
        phase = Phase.FLASH;
    }

    public Phase update(long milliseconds) {
        if (phase == Phase.LOADING || phase == Phase.CLOSED) return phase;
        if (phase == Phase.ENDING) {
            if (milliseconds - ending >= 350) phase = Phase.CLOSED;
            return phase;
        }
        long elapsed = elapsed(milliseconds);
        phase = elapsed < 1300 ? Phase.FLASH : elapsed < 2000 ? Phase.SLIDE
                : elapsed < 2600 ? Phase.THROW : elapsed < 3200 ? Phase.RELEASE : Phase.READY;
        return phase;
    }

    public long elapsed(long milliseconds) { return Math.max(0, milliseconds - started); }

    public void choose(Action action, long milliseconds) {
        if (action == null || update(milliseconds) != Phase.READY) return;
        ending = milliseconds;
        phase = Phase.ENDING;
    }

    public float exitProgress(long milliseconds) {
        return phase == Phase.CLOSED ? 1 : phase == Phase.ENDING ? Math.min(1, Math.max(0, (milliseconds - ending) / 350f)) : 0;
    }

    /** 入场闪烁使用分段亮度曲线，随后在500毫秒内收暗地图。 */
    public static float flash(long elapsed) {
        if (elapsed < 0 || elapsed >= 600) return 0;
        return (elapsed < 200 ? elapsed : elapsed < 300 ? 400 - elapsed
                : elapsed < 400 ? elapsed - 200 : 600 - elapsed) / 255f;
    }
}
