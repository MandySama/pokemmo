package top.totodile.pokemmo.ui;

import java.awt.image.BufferedImage;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** 每个窗口最多执行一个任务并保留一个最新请求，窗口取消不打断共享资源的首载。 */
final class LatestIconRequest {
    private static final ExecutorService WORKERS = Executors.newFixedThreadPool(2, action -> {
        var thread = new Thread(action, "pokemon-icons");
        thread.setDaemon(true);
        return thread;
    });
    private record Job(Supplier<BufferedImage> load, CompletableFuture<BufferedImage> result) { }
    private Job queued;
    private boolean running;

    synchronized CompletableFuture<BufferedImage> submit(Supplier<BufferedImage> load) {
        if (queued != null) queued.result().cancel(false);
        var result = new CompletableFuture<BufferedImage>();
        queued = new Job(load, result);
        if (!running) {
            running = true;
            WORKERS.execute(this::drain);
        }
        return result;
    }

    synchronized void cancel() {
        if (queued != null) queued.result().cancel(false);
        queued = null;
    }

    private void drain() {
        while (true) {
            Job job;
            synchronized (this) {
                job = queued;
                queued = null;
                if (job == null) { running = false; return; }
            }
            if (job.result().isCancelled()) continue;
            try { job.result().complete(job.load().get()); }
            catch (Throwable error) { job.result().completeExceptionally(error); }
        }
    }
}
