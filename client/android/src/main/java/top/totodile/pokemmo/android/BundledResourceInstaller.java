package top.totodile.pokemmo.android;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Installs bundled data only; account settings and saves are outside this list. */
public final class BundledResourceInstaller {
    public interface Source { InputStream open(String name) throws IOException; }
    public interface Progress { void update(int percent); }
    private static final String MARKER = ".bundled-resources-version";

    private BundledResourceInstaller() {}

    // Multiple Activity instances may overlap during rotation; never extract concurrently.
    public static synchronized void install(Source source, File root, Progress progress) throws Exception {
        JSONObject manifest = readManifest(source);
        if (!root.isDirectory() && !root.mkdirs()) throw new IOException("无法创建资源目录");
        JSONArray entries = manifest.getJSONArray("files");
        Map<String, JSONObject> expected = new HashMap<>();
        long total = 0;
        boolean existing = true;
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            String path = entry.getString("path");
            File target = checkedTarget(root, path);
            if (expected.put(path, entry) != null) throw new IOException("资源清单路径重复");
            long size = entry.getLong("size");
            if (size < 0) throw new IOException("资源大小无效");
            total += size;
            existing &= target.isFile() && target.length() == size;
        }
        if (entries.length() == 0) throw new IOException("资源清单为空");
        String revision = manifest.getString("revision");
        File marker = new File(root, MARKER);
        if (existing && revision.equals(readMarker(marker))) {
            progress.update(100);
            return;
        }
        // Atomic per-file replacement allows retry after interrupted extraction.
        // Extra device files (including config/) are never enumerated or deleted.
        long written = 0;
        Set<String> seen = new HashSet<>();
        byte[] buffer = new byte[128 * 1024];
        try (ZipInputStream zip = new ZipInputStream(source.open("resources.zip"))) {
            ZipEntry zipped;
            while ((zipped = zip.getNextEntry()) != null) {
                String path = zipped.getName();
                if (zipped.isDirectory()) {
                    checkedTarget(root, path);
                    zip.closeEntry();
                    continue;
                }
                JSONObject entry = expected.get(path);
                if (entry == null || !seen.add(path))
                    throw new IOException("安装包含有未知或重复资源: " + path);
                File target = checkedTarget(root, path);
                File parent = target.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) throw new IOException("无法创建资源子目录");
                long size = entry.getLong("size");
                File temporary = new File(parent, target.getName() + ".unpacking");
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                long count = 0;
                try {
                    try (FileOutputStream out = new FileOutputStream(temporary)) {
                        int read;
                        while ((read = zip.read(buffer)) != -1) {
                            count += read;
                            if (count > size) throw new IOException("资源超过预期大小: " + path);
                            digest.update(buffer, 0, read);
                            out.write(buffer, 0, read);
                            written += read;
                            progress.update((int) (written * 100 / Math.max(1, total)));
                        }
                        out.getFD().sync();
                    }
                    if (count != size || !hex(digest.digest()).equals(entry.getString("sha256")))
                        throw new IOException("资源校验失败: " + path);
                    java.nio.file.Files.move(temporary.toPath(), target.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                            java.nio.file.StandardCopyOption.ATOMIC_MOVE);
                } finally {
                    if (temporary.exists() && !temporary.delete()) temporary.deleteOnExit();
                }
                zip.closeEntry();
            }
        }
        if (seen.size() != expected.size()) throw new IOException("安装包缺少资源文件");
        File temporaryMarker = new File(root, MARKER + ".unpacking");
        try (FileOutputStream out = new FileOutputStream(temporaryMarker)) {
            out.write(revision.getBytes(StandardCharsets.UTF_8));
            out.getFD().sync();
        }
        java.nio.file.Files.move(temporaryMarker.toPath(), marker.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        progress.update(100);
    }

    private static JSONObject readManifest(Source source) throws Exception {
        try (InputStream in = source.open("manifest.json")) {
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                if (bytes.size() + read > 1024 * 1024) throw new IOException("资源清单过大");
                bytes.write(buffer, 0, read);
            }
            return new JSONObject(bytes.toString("UTF-8"));
        }
    }

    static boolean isInstalled(Source source, File root) throws Exception {
        JSONObject manifest = readManifest(source);
        JSONArray entries = manifest.getJSONArray("files");
        if (entries.length() == 0) return false;
        boolean existing = true;
        Set<String> paths = new HashSet<>();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            String path = entry.getString("path");
            File target = checkedTarget(root, path);
            long size = entry.getLong("size");
            if (size < 0 || !paths.add(path)) throw new IOException("Invalid resource manifest");
            existing &= target.isFile() && target.length() == size;
        }
        return existing && manifest.getString("revision").equals(readMarker(new File(root, MARKER)));
    }

    private static File checkedTarget(File root, String path) throws IOException {
        if (!path.startsWith("data/") || path.contains("\\"))
            throw new IOException("非法资源路径");
        for (String segment : path.split("/"))
            if (segment.equals(".") || segment.equals("..")) throw new IOException("资源路径越界");
        File target = new File(root, path);
        if (!target.getCanonicalPath().startsWith(root.getCanonicalPath() + File.separator))
            throw new IOException("资源路径越界");
        return target;
    }

    private static String readMarker(File marker) throws IOException {
        if (!marker.isFile() || marker.length() > 256) return "";
        try (FileInputStream in = new FileInputStream(marker)) {
            byte[] bytes = new byte[(int) marker.length()];
            int count = 0, read;
            while (count < bytes.length && (read = in.read(bytes, count, bytes.length - count)) != -1) count += read;
            return new String(bytes, 0, count, StandardCharsets.UTF_8);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte value : bytes) result.append(String.format(java.util.Locale.ROOT, "%02x", value & 255));
        return result.toString();
    }
}
