package top.totodile.pokemmo.client;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** 按需读取卡带，不把整个 ROM 放进堆内存。所有文件句柄在单次读取后关闭。 */
public final class RomResources {
    public enum Type {
        FIRE_RED(0, "gba", "BPR"), EMERALD(1, "gba", "BPE"),
        BLACK_WHITE(2, "nds", "IRB", "IRA"), PLATINUM(3, "nds", "CPU"),
        HEART_GOLD_SOUL_SILVER(4, "nds", "IPK", "IPG");
        public final int id;
        public final String extension;
        private final List<String> codes;
        Type(int id, String extension, String... codes) { this.id=id; this.extension=extension; this.codes=List.of(codes); }
        public static Type of(int id) { return Arrays.stream(values()).filter(t -> t.id==id).findFirst().orElseThrow(); }
    }
    public record Entry(long offset, int length) {}
    public static final class Rom {
        private final Path path;
        private final Type type;
        private final String title, code;
        private final int revision;
        private final long size;
        private final Map<String, Entry> files;
        private Rom(Path path, Type type, String title, String code, int revision, long size, Map<String, Entry> files) {
            this.path=path; this.type=type; this.title=title; this.code=code;
            this.revision=revision; this.size=size; this.files=Collections.unmodifiableMap(files);
        }
        public Path path() { return path; }
        public Type type() { return type; }
        public String title() { return title; }
        public String code() { return code; }
        public int revision() { return revision; }
        public long size() { return size; }
        public Map<String, Entry> files() { return files; }
        public byte[] read(long offset, int length) throws IOException {
            try (var input=new RandomAccessFile(path.toFile(), "r")) { return bytes(input, offset, length); }
        }
        public byte[] readFile(String name) throws IOException {
            Entry entry=files.get(name);
            if (entry==null) throw new FileNotFoundException(name);
            return read(entry.offset, entry.length);
        }
        public byte[] readGba(long address, int length) throws IOException {
            if (!type.extension.equals("gba")) throw new IOException("不是 GBA ROM");
            return read(address >= 0x08000000L ? address-0x08000000L : address, length);
        }
    }
    private final Path directory;
    private final ClientSettings settings;
    private Map<Type, Rom> loaded=Map.of();
    private List<String> errors=List.of();
    RomResources(Path directory, ClientSettings settings) {
        this.directory=directory.toAbsolutePath().normalize(); this.settings=settings; refresh();
    }
    public Path directory() { return directory; }
    public synchronized Rom get(Type type) { return loaded.get(type); }
    public synchronized List<Rom> loaded() { return List.copyOf(loaded.values()); }
    public synchronized List<String> errors() { return errors; }
    public synchronized void refresh() {
        Map<Type, Rom> next=new EnumMap<>(Type.class);
        List<String> failures=new ArrayList<>();
        Set<Path> candidates=new LinkedHashSet<>();
        for (Type type:Type.values()) {
            String value=settings.get("client.roms."+type.id, "");
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
            try { Rom rom=inspect(path); next.putIfAbsent(rom.type, rom); }
            catch (IOException | RuntimeException error) { failures.add(path.getFileName()+": "+error.getMessage()); }
        }
        loaded=next; errors=List.copyOf(failures);
    }
    public void importRom(Type expected, RomFilePicker.Source source) throws IOException {
        Files.createDirectories(directory);
        Path temporary=Files.createTempFile(directory, "rom-", ".tmp");
        try {
            try (InputStream input=source.open(); OutputStream output=Files.newOutputStream(temporary)) {
                byte[] buffer=new byte[65536]; long size=0;
                for (int count;(count=input.read(buffer))!=-1;) {
                    size+=count;
                    if (size>512L*1024*1024) throw new IOException("ROM 文件过大");
                    output.write(buffer,0,count);
                }
            }
            Rom rom=inspect(temporary);
            if (rom.type!=expected) throw new IOException("ROM 类型与所选行不符 ("+rom.code+")");
            // 使用独立文件名；配置写入失败时不会覆盖当前正在使用的 ROM。
            Path target=directory.resolve(rom.code+"-"+UUID.randomUUID()+"."+expected.extension);
            Files.move(temporary,target);
            try { settings.save(Map.of("client.roms."+expected.id, target.getFileName().toString())); }
            catch (RuntimeException error) { Files.deleteIfExists(target); throw new IOException("无法保存 ROM 配置", error); }
            refresh();
        } finally { Files.deleteIfExists(temporary); }
    }
    public static Rom inspect(Path path) throws IOException {
        try (var input=new RandomAccessFile(path.toFile(), "r")) {
            byte[] header=bytes(input,0,0x200);
            boolean gba=(header[0xb2]&255)==0x96;
            String code=ascii(header,gba?0xac:0x0c,4);
            Type type=Arrays.stream(Type.values()).filter(t -> t.extension.equals(gba?"gba":"nds")
                    && t.codes.contains(code.substring(0,3))).findFirst().orElseThrow(() -> new IOException("不支持的 ROM 游戏代码: "+code));
            Map<String,Entry> files=new LinkedHashMap<>();
            if (gba) {
                int checksum=0;
                for (int i=0xa0;i<=0xbc;i++) checksum-=header[i]&255;
                if (((checksum-0x19)&255)!=(header[0xbd]&255)) throw new IOException("GBA 文件头校验失败");
                if (input.length()<16L*1024*1024 || input.length()>32L*1024*1024) throw new IOException("GBA ROM 大小无效或文件被截断");
            } else {
                ByteBuffer data=ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
                int crc=0xffff;
                for (int i=0;i<0x15e;i++) {
                    crc^=header[i]&255;
                    for (int bit=0;bit<8;bit++) crc=(crc&1)!=0 ? (crc>>>1)^0xa001 : crc>>>1;
                }
                if (crc!=Short.toUnsignedInt(data.getShort(0x15e))) throw new IOException("NDS 文件头校验失败");
                long used=Integer.toUnsignedLong(data.getInt(0x80));
                if (used<0x200 || used>input.length()) throw new IOException("NDS ROM 大小无效或文件被截断");
                checkSection(input,data,0x20,0x2c); checkSection(input,data,0x30,0x3c);
                long fnt=Integer.toUnsignedLong(data.getInt(0x40)), fat=Integer.toUnsignedLong(data.getInt(0x48));
                int fntSize=data.getInt(0x44), fatSize=data.getInt(0x4c);
                if (fntSize<8 || fntSize>16*1024*1024 || fatSize<=0 || fatSize>8*65536 || fatSize%8!=0)
                    throw new IOException("NDS 文件表无效");
                byte[] names=bytes(input,fnt,fntSize), allocations=bytes(input,fat,fatSize);
                readDirectory(input,names,ByteBuffer.wrap(allocations).order(ByteOrder.LITTLE_ENDIAN),0xf000,"",files,new HashSet<>(),0);
                if (files.isEmpty()) throw new IOException("NDS ROM 没有可读取的资源");
            }
            return new Rom(path.toAbsolutePath().normalize(),type,ascii(header,gba?0xa0:0,12),code,
                    header[gba?0xbc:0x1e]&255,input.length(),files);
        } catch (IndexOutOfBoundsException | IllegalArgumentException error) { throw new IOException("ROM 文件表损坏",error); }
    }
    private static void checkSection(RandomAccessFile input, ByteBuffer header, int offset, int size) throws IOException {
        long start=Integer.toUnsignedLong(header.getInt(offset)), length=Integer.toUnsignedLong(header.getInt(size));
        if (start<0x200 || length==0 || start>input.length()-length) throw new IOException("NDS 程序区损坏或文件被截断");
    }
    private static void readDirectory(RandomAccessFile input, byte[] names, ByteBuffer fat, int id, String prefix,
                                      Map<String,Entry> files, Set<Integer> visited, int depth) throws IOException {
        ByteBuffer fnt=ByteBuffer.wrap(names).order(ByteOrder.LITTLE_ENDIAN);
        int directories=Short.toUnsignedInt(fnt.getShort(6)), index=id-0xf000;
        if (depth>64 || index<0 || index>=directories || !visited.add(id)) throw new IOException("NDS 目录无效");
        int cursor=fnt.getInt(index*8), fileId=Short.toUnsignedInt(fnt.getShort(index*8+4));
        while (true) {
            int descriptor=names[cursor++]&255;
            if (descriptor==0) break;
            int length=descriptor&127;
            if (length==0) throw new IOException("NDS 文件名无效");
            String name=ascii(names,cursor,length); cursor+=length;
            if (name.contains("/") || name.contains("\\") || name.equals(".") || name.equals("..")) throw new IOException("NDS 路径无效");
            if ((descriptor&128)!=0) {
                int child=Short.toUnsignedInt(fnt.getShort(cursor)); cursor+=2;
                readDirectory(input,names,fat,child,prefix+name+"/",files,visited,depth+1);
            } else {
                long start=Integer.toUnsignedLong(fat.getInt(fileId*8)), end=Integer.toUnsignedLong(fat.getInt(fileId*8+4)); fileId++;
                if (start>end || end>input.length() || end-start>Integer.MAX_VALUE) throw new IOException("NDS 资源超出文件范围");
                if (files.put(prefix+name,new Entry(start,(int)(end-start)))!=null) throw new IOException("NDS 资源路径重复");
            }
        }
    }
    private static String ascii(byte[] data,int start,int length) {
        return new String(data,start,length,StandardCharsets.US_ASCII).replace("\0", "").trim();
    }
    private static byte[] bytes(RandomAccessFile input,long offset,int length) throws IOException {
        if (length<0 || offset<0 || offset>input.length()-length) throw new IOException("ROM 文件被截断或读取越界");
        byte[] bytes=new byte[length]; input.seek(offset); input.readFully(bytes); return bytes;
    }
}
