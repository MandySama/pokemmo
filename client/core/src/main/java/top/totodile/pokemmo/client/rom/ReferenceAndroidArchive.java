package top.totodile.pokemmo.client.rom;

import com.sun.jna.*;
import com.sun.jna.ptr.*;
import org.libarchive.Libarchive;

/** 使用参考 Android 库现有 JNI 导出；JNA 仅分配 C 指针/输出参数所需内存。 */
final class ReferenceAndroidArchive implements LibArchiveRomExtractor.Api {
    static ReferenceAndroidArchive create() {
        Libarchive.initialize();
        return new ReferenceAndroidArchive();
    }
    private static long address(Pointer pointer) { return Pointer.nativeValue(pointer); }
    private static Pointer pointer(long address) { return address==0?null:new Pointer(address); }
    public Pointer archive_read_new(){return pointer(Libarchive.archive_read_new_internal());}
    public int archive_read_support_filter_all(Pointer r){return Libarchive.archive_read_support_filter_all_internal(address(r));}
    public int archive_read_support_format_all(Pointer r){return Libarchive.archive_read_support_format_all_internal(address(r));}
    public int archive_read_support_format_raw(Pointer r){return Libarchive.archive_read_support_format_raw_internal(address(r));}
    public int archive_read_open_filename(Pointer r,byte[] filename,LibArchiveRomExtractor.SizeT blockSize) {
        try(Memory memory=new Memory(filename.length)) {
            memory.write(0,filename,0,filename.length);
            return Libarchive.archive_read_open_filename_internal(address(r),address(memory),blockSize.longValue());
        }
    }
    public int archive_read_open_filename_w(Pointer r,WString filename,LibArchiveRomExtractor.SizeT blockSize) {
        throw new UnsupportedOperationException("Android uses UTF-8 paths");
    }
    public int archive_read_next_header(Pointer r,PointerByReference entry){return Libarchive.archive_read_next_header_internal(address(r),address(entry.getPointer()));}
    public int archive_read_has_encrypted_entries(Pointer r){return Libarchive.archive_read_has_encrypted_entries_internal(address(r));}
    public int archive_read_data_block(Pointer r,PointerByReference data,Pointer size,LongByReference offset){return Libarchive.archive_read_data_block_internal(address(r),address(data.getPointer()),address(size),address(offset.getPointer()));}
    public int archive_read_free(Pointer r){return Libarchive.archive_read_free_internal(address(r));}
    public Pointer archive_entry_pathname(Pointer e){return pointer(Libarchive.archive_entry_pathname_internal(address(e)));}
    public Pointer archive_entry_pathname_utf8(Pointer e){return pointer(Libarchive.archive_entry_pathname_utf8_internal(address(e)));}
    public Pointer archive_entry_pathname_w(Pointer e){return pointer(Libarchive.archive_entry_pathname_w_internal(address(e)));}
    public long archive_entry_size(Pointer e){return Libarchive.archive_entry_size_internal(address(e));}
    public int archive_entry_filetype(Pointer e){return Libarchive.archive_entry_filetype_internal(address(e));}
    public int archive_format(Pointer r){return Libarchive.archive_format_internal(address(r));}
    public Pointer archive_error_string(Pointer r){return pointer(Libarchive.archive_error_string_internal(address(r)));}
}
