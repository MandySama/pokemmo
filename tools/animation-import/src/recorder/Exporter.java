package recorder;

import f.PF;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.ZipFile;

public final class Exporter {
    private static final Set<String> FACTORIES = new TreeSet<>();
    public static void main(String[] args) throws Exception {
        List<String> output = new ArrayList<>();
        List<String> variants = new ArrayList<>();
        int count = 0;
        try (var archive = new ZipFile(args[0])) {
            MU.archive = archive;
            for (String line : Files.readAllLines(Path.of(args[1]))) {
                String[] fields = line.split(",");
                int id = Integer.parseInt(fields[0]);
                Class<?> type = Class.forName(fields[1]);
                MU result = execute(type, "us");
                if (result.E8 == null) result = execute(type, "Kh");
                if (result.E8 == null) throw new IllegalStateException("No timeline " + id);
                emit(result, Integer.toString(id), output);
                for (String phase : new String[]{"Kh", "o"}) {
                    if (Arrays.stream(type.getDeclaredMethods()).anyMatch(method -> method.getName().equals(phase))) {
                        MU value = execute(type, phase);
                        if (value.E8 != null) emit(value, id + ":" + phase, variants);
                    }
                }
                if (id == 290) {
                    for (int terrain : new int[]{0,1,2,3,4,5,6,7,8,12}) {
                        MU.terrain = terrain;
                        emit(execute(type, "us"), id + ":terrain=" + terrain, variants);
                    }
                    MU.terrain = -1;
                }
                if (id == 546) {
                    for (int item : new int[]{5116,5117,5118,5119}) {
                        MU.item = item;
                        emit(execute(type, "us"), id + ":item=" + item, variants);
                    }
                    MU.item = 0;
                }
                if (id == 269) {
                    MU.localCaster = false;
                    emit(execute(type, "us"), id + ":enemy", variants);
                    MU.localCaster = true;
                }
                count++;
            }
            for (var entry : MU.names.entrySet()) output.add(0, "R," + entry.getKey() + "," + entry.getValue());
        }
        Files.write(Path.of(args[2]), output, StandardCharsets.UTF_8);
        Files.write(Path.of(args[3]), variants, StandardCharsets.UTF_8);
        Files.write(Path.of(args[4]), FACTORIES, StandardCharsets.UTF_8);
        System.out.println("Exported " + count + " move timelines; variant rows=" + variants.size());
    }

    private static MU execute(Class<?> type, String phase) throws Exception {
        MU recipe = (MU) type.getConstructor(PF.class).newInstance((Object) null);
        try { return (MU) type.getMethod(phase).invoke(recipe); }
        catch (InvocationTargetException error) { throw new IllegalStateException(type.getName() + " " + phase, error.getCause()); }
    }

    private static void emit(MU recipe, String key, List<String> output) {
        recipe.E8.C20();
        output.add("M," + key + "," + Math.round(recipe.E8.oX() * 1000));
        recipe.flatten(recipe.E8, 0, key, output);
        recipe.leaves.forEach((node, marks) -> {
            for (String mark : marks) if (mark.startsWith("C,EN,") || mark.startsWith("C,Xq0,") || mark.startsWith("C,WW,") || mark.startsWith("C,df0,"))
                FACTORIES.add(Math.round(node.sS * 1000) + "," + mark.substring(2).replace(",@", ""));
        });
        recipe.E8.bC0();
    }
}
