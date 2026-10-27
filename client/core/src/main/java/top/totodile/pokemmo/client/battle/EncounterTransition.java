package top.totodile.pokemmo.client.battle;

/** PC timings ported from LinearTimelineSequence.nr, TwlGui and Gdx3DModelBatch.aQ. */
final class EncounterTransition {
    static final long INTRO = 1310, EXPAND = 500, MESSAGE = 1250, REVEAL = 100;
    // Gdx3DModelBatch: finish the 1s camera + 1.5s wild entrance, then wait for the text task.
    static final long WILD_MESSAGE_AT = 2500;
    private long started, escaped;
    private long encounterMessageDuration = MESSAGE;
    private long opponentDuration;

    void opponentDuration(long duration) { opponentDuration = duration; }
    private boolean flash, exiting;
    private int dim;

    void enter(long now, boolean flash, int dim) {
        started = now;
        this.flash = flash;
        this.dim = dim;
        exiting = false;
    }

    void encounterMessageDuration(long duration) {
        encounterMessageDuration = duration;
    }

    long encounterMessageTime(long now) {
        return now - started - INTRO - EXPAND - WILD_MESSAGE_AT;
    }

    boolean encounterMessageVisible(long now) {
        long elapsed = encounterMessageTime(now);
        return !exiting && elapsed >= 0 && elapsed < encounterMessageDuration;
    }

    void escape(long now) {
        escaped = now;
        exiting = true;
    }

    long entranceTime(long now) {
        long elapsed = Math.max(0, now - started - INTRO - EXPAND);
        return elapsed - Math.max(0, Math.min(encounterMessageDuration, elapsed - WILD_MESSAGE_AT));
    }

    boolean ready(long now) {
        return !exiting
                && now - started
                        >= INTRO + EXPAND + encounterMessageDuration + EncounterEntrance.DURATION
                                + opponentDuration;
    }

    boolean fieldVisible(long now) {
        return now - started >= INTRO && (!exiting || now - escaped < MESSAGE);
    }

    boolean complete(long now) {
        return exiting && now - escaped >= MESSAGE + REVEAL;
    }

    float expansion(long now) {
        float t = fraction(now - started - INTRO, EXPAND);
        return t * t * t * t * t;
    }

    int black(long now) {
        if (exiting) return (int) (dim * (1 - fraction(now - escaped - MESSAGE, REVEAL)));
        return (int) (dim * fraction(now - started - (flash ? 800 : 0), 500));
    }

    int white(long now) {
        if (!flash || exiting) return 0;
        long t = now - started;
        if (t < 200) return (int) Math.max(0, t);
        if (t < 300) return (int) (400 - t);
        if (t < 400) return (int) (t - 200);
        if (t < 600) return (int) (600 - t);
        return 0;
    }

    private static float fraction(long time, long duration) {
        return Math.max(0, Math.min(1, time / (float) duration));
    }
}
