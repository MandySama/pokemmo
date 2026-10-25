package top.totodile.pokemmo.client.resources;

import java.io.IOException;
import java.util.zip.ZipInputStream;

/** ParticleManager.eE: loose particle files take precedence over particles.pak entries. */
public final class ParticleResources {
    private ParticleResources() {}

    public static byte[] read(ClientResources resources, String path) throws IOException {
        var loose = resources.apply("sprites/particles/" + path);
        if (loose.exists()) return loose.readBytes();
        try (var zip = new ZipInputStream(resources.apply("sprites/particles.pak").read())) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry())
                if (entry.getName().equals(path)) return zip.readAllBytes();
        }
        throw new IOException("Missing particle resource " + path);
    }
}
