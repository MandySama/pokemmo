package top.totodile.pokemmo.client.resources;

import java.io.IOException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipFile;

/** ParticleManager.eE: loose particle files take precedence over particles.pak entries. */
public final class ParticleResources {
    private ParticleResources() {}

    public static byte[] read(ClientResources resources, String path) throws IOException {
        var loose = resources.apply("sprites/particles/" + path);
        if (loose.exists()) return loose.readBytes();
        var archive = resources.apply("sprites/particles.pak");
        // ParticleManager.eE uses indexed ZipFile entries rather than inflating preceding entries.
        if (archive.file().isFile()) {
            try (var zip = new ZipFile(archive.file())) {
                var entry = zip.getEntry(path);
                if (entry != null) {
                    try (var input = zip.getInputStream(entry)) {
                        return input.readAllBytes();
                    }
                }
            }
            throw new IOException("Missing particle resource " + path);
        }
        try (var zip = new ZipInputStream(resources.apply("sprites/particles.pak").read())) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry())
                if (entry.getName().equals(path)) return zip.readAllBytes();
        }
        throw new IOException("Missing particle resource " + path);
    }
}
