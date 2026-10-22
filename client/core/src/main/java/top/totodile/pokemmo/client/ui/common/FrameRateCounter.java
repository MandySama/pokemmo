package top.totodile.pokemmo.client.ui.common;

import java.util.Locale;
import java.util.function.LongSupplier;

/** 参考帧率计数器：累计 100 帧后按纳秒耗时计算，保留两位小数。 */
public final class FrameRateCounter {
    private final LongSupplier clock;
    private long startedAt;
    private int frames;
    private String text = "0.00";

    public FrameRateCounter(LongSupplier clock) {
        this.clock = clock;
        startedAt = clock.getAsLong();
    }

    public String frame() {
        if (++frames == 100) {
            long now = clock.getAsLong();
            long elapsed = now - startedAt;
            if (elapsed > 0) {
                long hundredths = Math.min(99999, Math.round(100 * 100_000_000_000.0 / elapsed));
                text = String.format(Locale.ROOT, "%d.%02d", hundredths / 100, hundredths % 100);
            }
            frames = 0;
            startedAt = now;
        }
        return text;
    }
}
