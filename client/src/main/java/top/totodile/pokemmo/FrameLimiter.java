package top.totodile.pokemmo;

import java.util.concurrent.locks.LockSupport;

/** 将绘制和交换缓冲的耗时计入帧预算；较慢的帧不额外等待。 */
public final class FrameLimiter {
    private FrameLimiter() {
    }

    public static void await(long startedAt, int fps) {
        long deadline = startedAt + 1_000_000_000L / Math.max(10, Math.min(1000, fps));
        long remaining;
        while ((remaining = deadline - System.nanoTime()) > 0 && !Thread.currentThread().isInterrupted()) {
            LockSupport.parkNanos(remaining);
        }
    }
}
