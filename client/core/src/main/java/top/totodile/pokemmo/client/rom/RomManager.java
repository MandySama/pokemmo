package top.totodile.pokemmo.client.rom;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** ROM 目录扫描、加载状态与导入；不依赖 UI 或客户端配置实现。 */
public final class RomManager {
    /** 由客户端组装层适配配置存储；未配置时 selectedFile 返回空字符串。 */
    public interface Configuration {
        String selectedFile(RomType type);
        void selectFile(RomType type, String filename);
    }

    private final Path directory;
    private final Configuration configuration;
    private final Object importLock=new Object();
    private Map<RomType, RomInfo> loaded=Map.of();
    private List<String> errors=List.of();
    public RomManager(Path directory, Configuration configuration) {
        this.directory=directory.toAbsolutePath().normalize(); this.configuration=Objects.requireNonNull(configuration); refresh();
    }
    public Path directory() { return directory; }
    public synchronized RomInfo get(RomType type) { return loaded.get(type); }
    public synchronized List<RomInfo> loaded() { return List.copyOf(loaded.values()); }
    public synchronized List<String> errors() { return errors; }
    public synchronized void refresh() {
        Map<RomType, RomInfo> next=new EnumMap<>(RomType.class);
        List<String> failures=new ArrayList<>();
        Set<Path> candidates=new LinkedHashSet<>();
        for (RomType type:RomType.values()) {
            String value=configuration.selectedFile(type);
            if (!value.isBlank()) {
                try { candidates.add(directory.resolve(value).normalize()); }
                catch (InvalidPathException error) { failures.add(value+": 路径无效"); }
            }
        }
        if (Files.isDirectory(directory)) {
            try (var paths=Files.list(directory)) {
                paths.filter(Files::isRegularFile).filter(path -> {
                    String name=path.toString().toLowerCase(Locale.ROOT);
                    return name.endsWith(".gba") || name.endsWith(".nds");
                }).sorted().forEach(candidates::add);
            } catch (IOException error) { failures.add("无法读取 ROM 文件夹: "+error.getMessage()); }
        }
        for (Path path:candidates) {
            try { RomInfo rom=RomReader.inspect(path); next.putIfAbsent(rom.type(), rom); }
            catch (IOException | RuntimeException error) { failures.add(path.getFileName()+": "+error.getMessage()); }
        }
        loaded=next; errors=List.copyOf(failures);
    }
    public enum ImportResult { IMPORTED, ALREADY_LOADED }
    public ImportResult importRom(RomType expected, RomFilePicker.Source source) throws IOException {
        return importRom(expected,source,value -> {});
    }
    public ImportResult importRom(RomType expected, RomFilePicker.Source source,
                                  java.util.function.DoubleConsumer progress) throws IOException {
        // 复制不能持有 loaded 的监视器，否则渲染线程查询 ROM 时会被阻塞，进度框无法更新。
        synchronized(importLock) { return importSelected(expected,source,progress); }
    }
    private ImportResult importSelected(RomType expected, RomFilePicker.Source source,
                                       java.util.function.DoubleConsumer progress) throws IOException {
        if (source.path()!=null && directory.equals(source.path().toAbsolutePath().normalize().getParent())) {
            RomInfo rom=validate(expected,source.path());
            if(alreadyLoaded(expected,rom.path()))return ImportResult.ALREADY_LOADED;
            try { configuration.selectFile(expected, rom.path().getFileName().toString()); }
            catch (RuntimeException error) { throw new IOException("无法保存 ROM 配置",error); }
            refresh();
            return ImportResult.IMPORTED;
        }
        Files.createDirectories(directory);
        Path temporary=Files.createTempFile(directory, "rom-", ".tmp");
        try {
            try (InputStream input=source.open(); OutputStream output=Files.newOutputStream(temporary)) {
                byte[] prefix=new byte[8];
                prefix=Arrays.copyOf(prefix,readBlock(input,prefix));
                String archive=archiveType(prefix);
                if(archive!=null)throw new RomImportFailure(1181,archive,null);
                output.write(prefix);
                byte[] buffer=new byte[65536]; long size=prefix.length;
                long total=source.path()==null?0:Files.size(source.path());
                for (int count;(count=input.read(buffer))!=-1;) {
                    size+=count;
                    if (size>512L*1024*1024) throw new RomImportFailure(1182,"ROM 文件过大",null);
                    output.write(buffer,0,count);
                    progress.accept(total>0?Math.min(0.9,size/(double)total*0.9):-1);
                }
            }
            RomInfo rom=validate(expected,temporary);
            progress.accept(0.95);
            // 内容比较，改名后重复选择也不会再复制一份；旧的外部引用仍需复制到受管目录。
            if(alreadyLoaded(expected,temporary))return ImportResult.ALREADY_LOADED;
            // 使用独立文件名；配置写入失败时不会覆盖当前正在使用的 ROM。
            Path target;
            if(source.path()!=null) {
                String name=source.path().getFileName().toString();
                target=directory.resolve(name);
                int dot=name.lastIndexOf('.');
                String stem=dot>0?name.substring(0,dot):name;
                String suffix=dot>0?name.substring(dot):"."+expected.extension;
                for(int index=2;Files.exists(target);index++)target=directory.resolve(stem+"-"+index+suffix);
            } else target=directory.resolve(rom.code()+"-"+UUID.randomUUID()+"."+expected.extension);
            Files.move(temporary,target);
            try { configuration.selectFile(expected, target.getFileName().toString()); }
            catch (RuntimeException error) { Files.deleteIfExists(target); throw new IOException("无法保存 ROM 配置", error); }
            refresh();
            progress.accept(1);
            return ImportResult.IMPORTED;
        } finally { Files.deleteIfExists(temporary); }
    }

    private boolean alreadyLoaded(RomType type,Path candidate) throws IOException {
        RomInfo existing=get(type);
        return existing!=null && directory.equals(existing.path().getParent()) &&
                Files.isRegularFile(existing.path()) && sameContent(existing.path(),candidate);
    }
    private static boolean sameContent(Path left,Path right) throws IOException {
        if(Files.size(left)!=Files.size(right))return false;
        // 避免 Files.mismatch / InputStream.readNBytes 对较旧 Android API 的依赖。
        try(InputStream a=Files.newInputStream(left);InputStream b=Files.newInputStream(right)) {
            byte[] first=new byte[65536],second=new byte[65536];
            for(int count;(count=readBlock(a,first))>0;) {
                if(readBlock(b,second)!=count)return false;
                for(int i=0;i<count;i++)if(first[i]!=second[i])return false;
            }
            return b.read()==-1;
        }
    }
    private static int readBlock(InputStream input,byte[] buffer) throws IOException {
        int size=0;
        while(size<buffer.length) {
            int count=input.read(buffer,size,buffer.length-size);
            if(count<0)break;
            size+=count;
        }
        return size;
    }
    private static RomInfo validate(RomType expected,Path path) throws IOException {
        RomInfo rom;
        try { rom=RomReader.inspect(path); }
        catch(RomReader.UnsupportedGame error) { throw new RomImportFailure(1183,error.getMessage(),error); }
        catch(IOException | RuntimeException error) { throw new RomImportFailure(1182,error.getMessage(),error); }
        if(rom.type()!=expected)throw new RomImportFailure(1183,rom.code(),null);
        return rom;
    }
    private static String archiveType(byte[] prefix) {
        if(prefix.length>=4 && prefix[0]=='P' && prefix[1]=='K' &&
                ((prefix[2]==3 && prefix[3]==4)||(prefix[2]==5 && prefix[3]==6)||(prefix[2]==7 && prefix[3]==8)))return "ZIP";
        if(prefix.length>=6 && prefix[0]=='R' && prefix[1]=='a' && prefix[2]=='r' && prefix[3]=='!' && prefix[4]==0x1a && prefix[5]==7)return "RAR";
        if(prefix.length>=6 && prefix[0]=='7' && prefix[1]=='z' && prefix[2]==(byte)0xbc && prefix[3]==(byte)0xaf && prefix[4]==0x27 && prefix[5]==0x1c)return "7z";
        if(prefix.length>=2 && prefix[0]==0x1f && prefix[1]==(byte)0x8b)return "GZIP";
        return null;
    }
}
