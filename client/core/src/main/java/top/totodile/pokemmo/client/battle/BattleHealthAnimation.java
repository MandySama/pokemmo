package top.totodile.pokemmo.client.battle;

/** Reference LabeledProgressBarWidget.HP: immediate first step, then 5% maximum HP per 40 ms. */
final class BattleHealthAnimation {
    private int current, target, maximum;
    private long previous;
    private boolean started;

    BattleHealthAnimation(int hp, int maxHp) {
        current = target = hp;
        maximum = maxHp;
    }

    void target(int hp, int maxHp) {
        maximum = Math.max(1, maxHp);
        target = Math.max(0, Math.min(maximum, hp));
        started = false;
    }

    void update(long now) {
        if (current == target) return;
        int steps;
        if (!started) {
            steps = 1;
            previous = now;
            started = true;
        } else {
            steps = (int) ((now - previous) / 40);
            if (steps < 1) return;
            previous += steps * 40L;
        }
        int change = (int) (Math.max(1d, maximum * .05) * steps);
        current =
                current > target
                        ? Math.max(target, current - change)
                        : Math.min(target, current + change);
    }

    int hp() {
        return current;
    }

    int maxHp() {
        return maximum;
    }

    boolean complete() {
        return current == target;
    }
}
