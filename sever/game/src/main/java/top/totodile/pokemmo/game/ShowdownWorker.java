package top.totodile.pokemmo.game;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
import java.util.concurrent.*;

/** 常驻私有工作进程；所有战斗共享有序通道，超时销毁进程以避免响应串场。 */
final class ShowdownWorker {
    private static Process process;
    private static BufferedWriter input;
    private static BufferedReader output;
    private static final ExecutorService reader = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r,"showdown-response");t.setDaemon(true);return t;
    });
    private static long generation;
    static { Runtime.getRuntime().addShutdownHook(new Thread(ShowdownWorker::shutdown)); }
    private ShowdownWorker() { }
    static synchronized long generation() { return generation; }
    static synchronized byte[] call(String json, long expected) throws IOException {
        if (expected != 0 && (process == null || !process.isAlive() || generation != expected))
            throw new IOException("Battle engine session lost");
        if (process == null || !process.isAlive()) {
            Path script = Path.of(System.getProperty("pokemmo.battle.engine", "battle-engine/worker.cjs")).toAbsolutePath();
            if (!Files.isRegularFile(script)) throw new IOException("Missing battle-engine/worker.cjs");
            process = new ProcessBuilder(System.getProperty("pokemmo.battle.node","node"),script.toString())
                    .redirectError(ProcessBuilder.Redirect.INHERIT).start();
            input = process.outputWriter(StandardCharsets.UTF_8);output = process.inputReader(StandardCharsets.UTF_8);generation++;
        }
        try {
            input.write(json);input.newLine();input.flush();
            BufferedReader source=output;
            var pending=reader.submit(source::readLine);
            String result;
            try { result=pending.get(15,TimeUnit.SECONDS); }
            catch(InterruptedException error){Thread.currentThread().interrupt();throw new IOException("Engine interrupted",error);}
            catch(ExecutionException|TimeoutException error){pending.cancel(true);throw new IOException("Engine response failed",error);}
            if(result==null||result.length()>65536||!result.startsWith("OK\t"))throw new IOException("Battle engine rejected request");
            return Base64.getDecoder().decode(result.substring(3));
        } catch(IOException|IllegalArgumentException error) {shutdown();throw new IOException("Battle engine unavailable",error);}
    }
    static synchronized void shutdown() {
        if(process!=null)process.destroyForcibly();process=null;
    }
}
