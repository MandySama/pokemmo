package top.totodile.pokemmo.battle;

/** 展示阶段的单次遭遇生命周期；不结算招式、经验或持久化奖励。 */
public final class WildEncounter {
    public enum Phase { LOADING, FLASH, SLIDE, ANNOUNCE, THROW, RELEASE, READY, SELECT_MOVE, MOVE, BAG, SELECT_PARTY, SWITCH_LOADING, SWITCHING, ENDING, CLOSED }
    public enum Action { FIGHT, BAG, POKEMON, RUN }
    public record Roll(int species, int level) { }

    /** 展示遭遇暂使用全图鉴均匀抽取，地图分布由后续遇敌规则提供。 */
    public static Roll roll(java.util.random.RandomGenerator random) {
        return new Roll(random.nextInt(1, 650), random.nextInt(3, 31));
    }

    private Phase phase = Phase.LOADING;
    private long started;
    private long ending;
    private long moveStarted, switchStarted;
    private int moveId = MovePresentation.TACKLE;
    private int[] partyHp = new int[0];
    private int activeSlot = -1, requestedSlot = -1;
    private int introductionDelay;

    public void trainerIntroduction() {
        if (phase != Phase.LOADING) throw new IllegalStateException("Encounter already loaded");
        introductionDelay = 1800;
    }

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
        if (phase == Phase.SELECT_MOVE || phase == Phase.SELECT_PARTY || phase == Phase.BAG
                || phase == Phase.SWITCH_LOADING) return phase;
        if (phase == Phase.SWITCHING) {
            if (switchElapsed(milliseconds) >= 600) phase = Phase.READY;
            return phase;
        }
        if (phase == Phase.MOVE) {
            if (moveElapsed(milliseconds) >= MoveAnimations.playback(moveId).duration()) phase = Phase.READY;
            return phase;
        }
        long elapsed = elapsed(milliseconds);
        if (elapsed >= 2000) elapsed = Math.max(1300, elapsed - introductionDelay);
        phase = elapsed < 1300 ? Phase.FLASH : elapsed < 2000 ? Phase.SLIDE
                : elapsed < 3000 ? Phase.ANNOUNCE : elapsed < 3600 ? Phase.THROW : elapsed < 4200 ? Phase.RELEASE : Phase.READY;
        return phase;
    }

    public long elapsed(long milliseconds) { return Math.max(0, milliseconds - started); }

    public void choose(Action action, long milliseconds) {
        if (action == null || update(milliseconds) != Phase.READY) return;
        if (action == Action.FIGHT) {
            phase = Phase.SELECT_MOVE;
            return;
        }
        if (action == Action.BAG) { phase = Phase.BAG; return; }
        if (action == Action.POKEMON) { phase = Phase.SELECT_PARTY; return; }
        ending = milliseconds;
        phase = Phase.ENDING;
    }

    public void chooseMove(int move, long milliseconds) {
        if (!MoveAnimations.supports(move) || update(milliseconds) != Phase.SELECT_MOVE) return;
        moveId = move;
        moveStarted = milliseconds;
        phase = Phase.MOVE;
    }

    public void cancelMove(long milliseconds) {
        if (update(milliseconds) == Phase.SELECT_MOVE) phase = Phase.READY;
    }

    public void playServerMove(int move, long milliseconds) {
        moveId = move;
        moveStarted = milliseconds;
        phase = Phase.MOVE;
    }
    public void serverParty(int[] hp, boolean forced) {
        partyHp = hp.clone();
        phase = forced ? Phase.SELECT_PARTY : Phase.READY;
    }
    public void finish(long milliseconds) { ending = milliseconds; phase = Phase.ENDING; }

    public int moveId() { return moveId; }

    public long moveElapsed(long milliseconds) { return Math.max(0, milliseconds - moveStarted); }

    public void configureParty(int[] hp, int active) {
        if (phase != Phase.LOADING || hp == null || hp.length < 1 || hp.length > 6
                || active < 0 || active >= hp.length || hp[active] <= 0)
            throw new IllegalArgumentException("Invalid battle party");
        for (int value : hp) if (value < 0) throw new IllegalArgumentException("Invalid party HP");
        partyHp = hp.clone();
        activeSlot = active;
    }

    public int activeSlot() { return activeSlot; }

    public void restoreParty(int[] hp, int active) {
        if (phase != Phase.LOADING || hp == null || hp.length < 1 || hp.length > 6 || active < 0 || active >= hp.length)
            throw new IllegalArgumentException("Invalid recovered battle party");
        for (int value : hp) if (value < 0) throw new IllegalArgumentException("Invalid party HP");
        partyHp = hp.clone(); activeSlot = active;
    }

    public boolean selectParty(int slot, long milliseconds) {
        if (update(milliseconds) != Phase.SELECT_PARTY || slot < 0 || slot >= partyHp.length
                || slot == activeSlot || partyHp[slot] <= 0) return false;
        requestedSlot = slot;
        phase = Phase.SWITCH_LOADING;
        return true;
    }

    public void switchLoaded(long milliseconds) {
        if (phase != Phase.SWITCH_LOADING) throw new IllegalStateException("No pending switch");
        activeSlot = requestedSlot;
        requestedSlot = -1;
        switchStarted = milliseconds;
        phase = Phase.SWITCHING;
    }

    public void switchFailed() {
        if (phase == Phase.SWITCH_LOADING) { requestedSlot = -1; phase = Phase.SELECT_PARTY; }
    }

    public long switchElapsed(long milliseconds) { return Math.max(0, milliseconds - switchStarted); }

    public void cancelParty(long milliseconds) {
        if (update(milliseconds) == Phase.SELECT_PARTY) phase = Phase.READY;
    }

    public void closeBag(long milliseconds) {
        if (update(milliseconds) == Phase.BAG) phase = Phase.READY;
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
