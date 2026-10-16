package top.totodile.particlebridge;

import com.badlogic.gdx.graphics.g3d.particles.*;
import f.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipFile;
import java.nio.file.*;

public final class RuntimeAudit {
    public static void main(String[] args) throws Exception {
        if (args.length > 2) {
            int factories = 0;
            for (String line : Files.readAllLines(Path.of(args[2]))) {
                String[] fields = line.split(",");
                float[] values = new float[fields.length - 2];
                for (int i = 2; i < fields.length; i++) values[i - 2] = Float.parseFloat(fields[i]);
                var pose = new PoseBridge(new String[]{fields[1]}, new long[]{0}, new float[][]{values});
                long duration = Math.round(pose.duration() * 1000);
                if (Math.abs(duration - Long.parseLong(fields[0])) > 1) throw new AssertionError("Factory duration mismatch " + line + " actual=" + duration);
                for (float[] state : pose.sample(pose.duration() / 2)) for (float value : state)
                    if (!Float.isFinite(value)) throw new AssertionError("Invalid actor pose " + line);
                pose.close();
                factories++;
            }
            System.out.println("Original pose factories passed=" + factories);
        }
        int passed = 0, failed = 0;
        float[] matrix = {1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
        try (var archive = new ZipFile(args[0])) {
            for (var entry : Collections.list(archive.entries())) {
                if (!entry.getName().endsWith(".vfx")) continue;
                if (args.length > 1) {
                    if (args[1].equals("moves")) {
                        if (!entry.getName().startsWith("particle/auto/") && !entry.getName().equals("particle/custom/precipice_blades.vfx")) continue;
                    } else if (!Arrays.asList(args[1].split(",")).contains(entry.getName())) continue;
                }
                String source;
                try (var input = archive.getInputStream(entry)) { source = new String(input.readAllBytes(), StandardCharsets.UTF_8); }
                char[] text = source.toCharArray();
                var data = (ResourceData) new gp_1().b20(ResourceData.class, null, new Y1().Gu0(text, text.length));
                int count = ((ParticleEffect) data.resource).getControllers().KB;
                for (int i = 0; i < count; i++) {
                    try {
                        var bridge = new ParticleBridge(source, i, 9, 11, .5f, false);
                        bridge.camera(matrix, matrix, 760, 540);
                        for (int t = 0; t < 120; t++) { bridge.update(1f / 60); bridge.particles(); }
                        bridge.close();
                        passed++;
                    } catch (Throwable error) {
                        failed++;
                        if (failed < 30) System.out.println("FAIL " + entry.getName() + " #" + i + " " + error);
                    }
                }
            }
        }
        System.out.println("CPU controllers passed=" + passed + " failed=" + failed);
        if (failed > 0) throw new AssertionError(failed + " controller failures");
    }
}
