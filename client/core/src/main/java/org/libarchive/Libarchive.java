package org.libarchive;

/** Reference libarchive.so JNI entry points. Names and signatures are part of its ABI. */
public final class Libarchive {
    private Libarchive() {}

    public static void initialize() {
        init(IllegalArgumentException.class, RuntimeException.class);
    }

    private static native void init(Class<?> illegalArgumentException, Class<?> nativeException);

    public static native long archive_read_new_internal();

    public static native int archive_read_support_filter_all_internal(long reader);

    public static native int archive_read_support_format_all_internal(long reader);

    public static native int archive_read_support_format_raw_internal(long reader);

    public static native int archive_read_open_filename_internal(
            long reader, long filename, long blockSize);

    public static native int archive_read_next_header_internal(long reader, long entry);

    public static native int archive_read_has_encrypted_entries_internal(long reader);

    public static native int archive_read_data_block_internal(
            long reader, long data, long size, long offset);

    public static native int archive_read_free_internal(long reader);

    public static native long archive_entry_pathname_internal(long entry);

    public static native long archive_entry_pathname_utf8_internal(long entry);

    public static native long archive_entry_pathname_w_internal(long entry);

    public static native long archive_entry_size_internal(long entry);

    public static native int archive_entry_filetype_internal(long entry);

    public static native int archive_format_internal(long reader);

    public static native long archive_error_string_internal(long reader);
}
