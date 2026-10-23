package top.totodile.pokemmo.client.rom;

import com.sun.jna.*;
import com.sun.jna.ptr.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.function.DoubleConsumer;

/** 对应参考 ClientSettingsWindow.sv / GdxArchiveReader：LibArchive 解码到固定临时文件。 */
final class LibArchiveRomExtractor {
    private static final long LIMIT=512L*1024*1024;
    private static Api library;
    public interface Api extends Library {
        Pointer archive_read_new();
        int archive_read_support_filter_all(Pointer reader);
        int archive_read_support_format_all(Pointer reader);
        int archive_read_support_format_raw(Pointer reader);
        int archive_read_open_filename(Pointer reader,byte[] filename,SizeT blockSize);
        int archive_read_open_filename_w(Pointer reader,WString filename,SizeT blockSize);
        int archive_read_next_header(Pointer reader,PointerByReference entry);
        int archive_read_has_encrypted_entries(Pointer reader);
        int archive_read_data_block(Pointer reader,PointerByReference data,Pointer size,LongByReference offset);
        int archive_read_free(Pointer reader);
        Pointer archive_entry_pathname(Pointer entry);
        Pointer archive_entry_pathname_utf8(Pointer entry);
        Pointer archive_entry_pathname_w(Pointer entry);
        long archive_entry_size(Pointer entry);
        int archive_entry_filetype(Pointer entry);
        int archive_format(Pointer reader);
        Pointer archive_error_string(Pointer reader);
    }
    public static final class SizeT extends IntegerType {
        public SizeT(){this(0);}
        public SizeT(long value){super(Native.SIZE_T_SIZE,value,true);}
    }
    private static synchronized Api load(Path cache) throws IOException {
        if(library!=null)return library;
        try {
            String vm=System.getProperty("java.vm.name","");
            if(vm.equalsIgnoreCase("Dalvik")) {
                System.loadLibrary("archive");
                library=ReferenceAndroidArchive.create();
            }
            else {
                String platform=System.getProperty("os.name","").toLowerCase(Locale.ROOT);
                String architecture=System.getProperty("os.arch","");
                String resource=platform.startsWith("windows") && architecture.equals("amd64")?"windows-x86_64/archive64.dll":null;
                if(resource==null)throw new IOException("LibArchive is unavailable on this platform");
                byte[] bytes;
                try(InputStream input=LibArchiveRomExtractor.class.getResourceAsStream("/native-archive/"+resource)) {
                    if(input==null)throw new FileNotFoundException(resource);
                    bytes=input.readAllBytes();
                }
                StringBuilder digest=new StringBuilder();
                for(byte value:MessageDigest.getInstance("SHA-256").digest(bytes))digest.append(String.format(Locale.ROOT,"%02x",value&255));
                String hash=digest.toString();
                Path directory=cache.toAbsolutePath().resolve(hash.substring(0,16));Files.createDirectories(directory);
                Path target=directory.resolve("archive64.dll");
                if(!Files.isRegularFile(target) || !Arrays.equals(Files.readAllBytes(target),bytes)) {
                    Path temporary=Files.createTempFile(directory,"archive-",".tmp");
                    try { Files.write(temporary,bytes);Files.move(temporary,target,StandardCopyOption.REPLACE_EXISTING); }
                    finally { Files.deleteIfExists(temporary); }
                }
                library=Native.load(target.toString(),Api.class);
            }
            return library;
        } catch(LinkageError | RuntimeException | NoSuchAlgorithmException error){throw new IOException("Unable to load LibArchive",error);}
    }
    static String extract(Path archive,Path destination,String extension,boolean gzip,Path cache,DoubleConsumer progress) throws IOException {
        Api api=load(cache);
        Pointer reader=api.archive_read_new();
        if(reader==null)throw new IOException("Unable to allocate LibArchive reader");
        try {
            api.archive_read_support_filter_all(reader);api.archive_read_support_format_all(reader);
            if(gzip)api.archive_read_support_format_raw(reader);
            byte[] filename=(archive.toAbsolutePath()+"\0").getBytes(java.nio.charset.StandardCharsets.UTF_8);
            boolean windows=System.getProperty("os.name","").startsWith("Windows");
            check(api,reader,windows?api.archive_read_open_filename_w(reader,new WString(archive.toAbsolutePath().toString()),new SizeT(65536))
                    :api.archive_read_open_filename(reader,filename,new SizeT(65536)));
            PointerByReference entry=new PointerByReference(),data=new PointerByReference();
            LongByReference offset=new LongByReference();
            String selected=null;
            try(Memory size=new Memory(Native.SIZE_T_SIZE)) {
                while(true) {
                    int status=api.archive_read_next_header(reader,entry);
                    if(api.archive_read_has_encrypted_entries(reader)>0)throw new RomImportFailure(1093,"Password protected archive",null);
                    if(status==1)break;
                    check(api,reader,status);
                    Pointer item=entry.getValue();
                    if(item==null)throw new IOException("Missing archive entry");
                    Pointer namePointer=api.archive_entry_pathname_utf8(item);
                    Pointer wide=namePointer==null?api.archive_entry_pathname_w(item):null;
                    if(namePointer==null && wide==null)namePointer=api.archive_entry_pathname(item);
                    String name=wide!=null?wide.getWideString(0):namePointer==null?"":namePointer.getString(0,"UTF-8");
                    int type=api.archive_entry_filetype(item);
                    if(type!=0100000)continue;
                    // raw GZIP 提供的条目名可能只有 data；其内容仍须通过 ROM 校验。
                    if(!(gzip && api.archive_format(reader)==0x90000 && name.equals("data")) && !name.toLowerCase(Locale.ROOT).endsWith("."+extension))continue;
                    long total=api.archive_entry_size(item),written=0;
                    if(total>LIMIT)throw new RomImportFailure(1182,"ROM file too large",null);
                    progress.accept(0);
                    try(RandomAccessFile output=new RandomAccessFile(destination.toFile(),"rw")) {
                        output.setLength(0);
                        while(true) {
                            status=api.archive_read_data_block(reader,data,size,offset);
                            if(api.archive_read_has_encrypted_entries(reader)>0)throw new RomImportFailure(1093,"Password protected archive",null);
                            if(status==1)break;
                            check(api,reader,status);
                            long count=Native.SIZE_T_SIZE==8?size.getLong(0):Integer.toUnsignedLong(size.getInt(0));
                            long start=offset.getValue();
                            if(start<0 || count<0 || start>LIMIT || count>LIMIT-start)throw new RomImportFailure(1182,"ROM file too large",null);
                            output.seek(start);
                            for(long copied=0;copied<count;) {
                                int length=(int)Math.min(65536,count-copied);
                                output.write(data.getValue().getByteArray(copied,length));copied+=length;
                            }
                            written=Math.max(written,start+count);
                            progress.accept(total>0?Math.min(.9,written/(double)total*.9):-1);
                        }
                        if(total>0 && written!=total)throw new IOException("Truncated archive entry");
                    }
                    name=name.replace('\\','/');selected=name.substring(name.lastIndexOf('/')+1);
                    if(gzip && selected.equals("data"))selected="imported."+extension;
                }
            }
            if(selected==null)throw new RomImportFailure(1092,"No matching ROM extension",null);
            return selected;
        } catch(RuntimeException error) { throw new IOException("Unable to decode archive",error); }
        finally { api.archive_read_free(reader); }
    }
    private static void check(Api api,Pointer reader,int status) throws IOException {
        if(status>=0 || status==-20)return; // 与参考一致，允许 ARCHIVE_WARN。
        Pointer message=api.archive_error_string(reader);
        throw new IOException(message==null?"LibArchive error "+status:message.getString(0,"UTF-8"));
    }
}
