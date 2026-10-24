package top.totodile.pokemmo.client.rom.nds.model;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import top.totodile.pokemmo.client.config.ClientSettings;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.world.WorldMapLoader;

/** PC 启动任务，对应 CameraController.yG0 和 RegionModelCacheManager.eT/Ro0。
 * 进度按实际完成的地形/建筑组统计，各地区等权；有效缓存直接复用。
 */
public final class RegionModelCaches implements AutoCloseable {
    private static final Logger LOG=Logger.getLogger(RegionModelCaches.class.getName());
    private final Path directory;
    private final ClientSettings settings;
    private final Map<RomType,RegionModelCache> caches=new ConcurrentHashMap<>();
    private final ExecutorService worker=Executors.newSingleThreadExecutor(r->{var t=new Thread(r,"rom-model-cache");t.setDaemon(true);return t;});
    private final Set<RomType> reused=ConcurrentHashMap.newKeySet();
    private volatile double progress;
    private volatile Throwable failure;
    private volatile boolean building,closed;
    private CompletableFuture<Void> task;
    public RegionModelCaches(Path directory,ClientSettings settings){this.directory=directory;this.settings=settings;}
    public synchronized void start(List<RomInfo> roms) {
        if(task!=null)return;
        if(!Boolean.parseBoolean(settings.get("client.cache.model_cache.enable","true"))) {
            progress=1;task=CompletableFuture.completedFuture(null);return;
        }
        var nds=roms.stream().filter(r->r.type().extension.equals("nds")).sorted(Comparator.comparingInt(r->r.type().id)).toList();
        task=CompletableFuture.runAsync(()->prepare(nds),worker).whenComplete((result,error)->{if(error!=null)failure=error;});
    }
    private void prepare(List<RomInfo> roms) {
        boolean regenerate=Boolean.parseBoolean(settings.get("client.cache.model_cache.regenerate","false"));
        for(int region=0;region<roms.size();region++) {
            if(closed||Thread.currentThread().isInterrupted())return;
            RomInfo rom=roms.get(region);
            Path file=directory.resolve("region-models-"+rom.type().id+".bin");
            try {
                byte[] fingerprint=RegionModelCache.fingerprint(rom.path());
                if(!regenerate)try {
                    var cache=RegionModelCache.open(file,rom.type().id,fingerprint);
                    if(!publish(rom.type(),cache))return;reused.add(rom.type());
                    LOG.info("Reused model cache: "+rom.type()+", "+cache.size()+" records");
                    progress=(region+1d)/roms.size();continue;
                }catch(IOException invalid){LOG.fine("Rebuilding model cache: "+invalid.getMessage());}
                building=true;
                var maps=WorldMapLoader.load(rom);
                var tasks=maps.modelCacheTasks();
                try(var builder=new RegionModelCache.Builder(file,rom.type().id,fingerprint)) {
                    for(int i=0;i<tasks.size();i++) {
                        if(closed||Thread.currentThread().isInterrupted())return;
                        for(var placement:tasks.get(i).get())builder.add(placement.model(),placement.textures(),placement.animations());
                        progress=(region+(i+1d)/tasks.size())/roms.size();
                    }
                    if(closed)return;
                    builder.commit();
                }
                var cache=RegionModelCache.open(file,rom.type().id,fingerprint);
                if(!publish(rom.type(),cache))return;
                LOG.info("Built model cache: "+rom.type()+", "+cache.size()+" records");
            }catch(Exception error) {
                if(closed||error instanceof InterruptedIOException)return;
                failure=error;LOG.log(Level.WARNING,"Unable to precache "+rom.type(),error);
            }
            progress=(region+1d)/roms.size();
        }
        if(regenerate&&failure==null&&!closed)settings.save(Map.of("client.cache.model_cache.regenerate","false"));
        progress=1;
    }
    public boolean done(){return task!=null&&task.isDone();}
    public boolean building(){return building;}
    public double progress(){return progress;}
    public Throwable failure(){return failure;}
    public RegionModelCache cache(RomType type){return caches.get(type);}
    public boolean reused(RomType type){return reused.contains(type);}
    private synchronized boolean publish(RomType type,RegionModelCache cache) {
        if(closed){cache.close();return false;}
        var previous=caches.put(type,cache);if(previous!=null)previous.close();return true;
    }
    @Override public synchronized void close(){closed=true;worker.shutdownNow();caches.values().forEach(RegionModelCache::close);caches.clear();}
}
