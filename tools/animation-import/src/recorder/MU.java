package recorder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.particles.*;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import f.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipFile;

/** 招式工厂记录叶节点，串行、并行、循环和节点延时由时间轴引擎编排。 */
public class MU {
    public PF Vz0;
    public PF Xp;
    public es_1 CoM9 = new es_1();
    public byte ei0;
    public boolean nJ0;
    public pw_1 E8;
    public final View Vs = new View();
    public static final class View { public ii0_1 wP = new ii0_1(); public void jH(pw_1 timeline) { } }
    public static int terrain = -1;
    public static int item;
    public static boolean localCaster = true;
    public static ZipFile archive;
    public static final Map<Integer, String> names = new LinkedHashMap<>();
    private static final Map<String, ParticleEffect> resources = new HashMap<>();
    public final IdentityHashMap<D2, List<String>> leaves = new IdentityHashMap<>();

    public MU(PF pokemon) { }
    public MU us() { return this; }
    public MU Kh() { return this; }
    public MU o() { return this; }
    public void Vc() { }
    public void ZS() { }
    public void R10() { }
    public boolean bL() { return true; }
    public void kA0(PF[] targets) { }
    public MU vv(PF target) { return this; }
    public boolean nn0() { return localCaster; }

    private static String args(float... values) {
        StringBuilder text = new StringBuilder();
        for (float value : values) text.append(',').append(Float.toString(value));
        return text.toString();
    }
    private pw_1 command(String operation, float duration, float... values) {
        pw_1 timeline = pw_1.xC().p1(duration);
        leaves.put(timeline, List.of("C," + operation + ",@" + args(values)));
        return timeline;
    }
    private ao_1 instant(String operation, float... values) {
        ao_1 node = ao_1.pc((event, tween) -> { });
        leaves.put(node, List.of("C," + operation + ",@" + args(values)));
        return node;
    }
    public pw_1 Wt(int target, float duration) { return command("Wt", duration, target, duration); }
    public pw_1 tP(float duration) { return command("tP", duration, duration); }
    public pw_1 WW(int target, float duration, float from, float to, Color color) {
        int rgb = Math.round(color.r * 31) | Math.round(color.g * 31) << 5 | Math.round(color.b * 31) << 10;
        return command("WW", target == 14 || target == 16 ? duration : 0, target, duration, from, to, rgb);
    }
    public pw_1 Ue0(int target, int color, float from, float to, float duration) { return command("Ue0", duration * 10, target, color, from, to, duration); }
    public pw_1 EN(int target, int type, int count, float first, float second, float x, float y) {
        float duration = switch (type) {
            case 0, 1, 4 -> first + second;
            case 2 -> (first + second) * (count + 1);
            case 3 -> 2 * (first + second) * (count + 1);
            case 14, 15 -> (first + second) / 2;
            default -> 0;
        };
        return command("EN", target == 14 || target == 16 ? duration : 0, target, type, count, first, second, x, y);
    }
    public pw_1 Xq0(int target, int type, int count, float first, float second, float x, float y) {
        float duration = switch (type) {
            case 0 -> first + second;
            case 1 -> (first + second) * (count > 2 ? count : 1);
            case 2 -> 2 * (first + second) * (count > 2 ? count : 1);
            case 3 -> 3 * (first + second);
            default -> 0;
        };
        return command("Xq0", target == 14 || target == 16 ? duration : 0, target, type, count, first, second, x, y);
    }
    public pw_1 i6(byte type, short sound, int channel, int target, float delay, float volume, PF pokemon) { return command("i6", 0, type, sound, channel, target, delay, volume); }
    public pw_1 Sv0(int first, int second, float delay, float duration) { return command("Sv0", 0, first, second, delay, duration); }
    public pw_1 mf0(int first, int second, int third, float duration) { return command("mf0", duration, first, second, third, duration); }
    public pw_1 df0(int target, int type) { return command("df0", 0, target, type); }
    public pw_1 nM(int target, int type) { return command("nM", 0, target, type); }
    public pw_1 Uv(boolean disabled, float delay) { return command("Uv", disabled ? 0 : delay / 1000, disabled ? 1 : 0, delay); }
    public ao_1 E2(int target, boolean enabled) { return instant("E2", target, enabled ? 1 : 0); }
    public ao_1 QO(int target) { return instant("QO", target); }
    public ao_1 Qh0(int resource) { effect(resource); return instant("Qh0", resource); }

    private static ParticleEffect effect(int resource) { return effect(names.getOrDefault(resource, (resource >= 1000 ? "custom/" : "auto/") + resource)); }
    private static ParticleEffect effect(String name) {
        return resources.computeIfAbsent(name, key -> {
            var entry = archive.getEntry("particle/" + key + ".vfx");
            if (entry == null) throw new IllegalStateException("Missing effect " + key);
            try (var input = archive.getInputStream(entry)) {
                char[] text = new String(input.readAllBytes(), StandardCharsets.UTF_8).toCharArray();
                var data = (ResourceData) new gp_1().b20(ResourceData.class, null, new Y1().Gu0(text, text.length));
                return (ParticleEffect) data.resource;
            } catch (IOException error) { throw new UncheckedIOException(error); }
        });
    }
    private static float duration(ParticleController controller) {
        var emitter = (RegularEmitter) controller.emitter;
        return emitter.delayValue.getLowMax() + emitter.durationValue.getLowMax() + emitter.lifeValue.getHighMax();
    }
    private static String particle(int archive, int index, int target, int destination, float adjustment, int position, float travel, float duration) {
        return "E," + archive + "," + index + "," + target + ",@," + Math.round(duration) + "," + adjustment + "," + destination + "," + position + "," + travel;
    }
    private pw_1 particle(int archive, int index, int target, int destination, float adjustment, int position, float travel) {
        var controllers = effect(archive).getControllers();
        if (index >= controllers.KB) return pw_1.gb0();
        float duration = duration((ParticleController) controllers.get(index));
        pw_1 timeline = pw_1.xC().p1(duration / 1000);
        leaves.put(timeline, List.of(particle(archive, index, target, destination, adjustment, position, travel, duration)));
        return timeline;
    }
    public pw_1 fE0(int position, int archive, int index, int source, int destination, float adjustment) { return particle(archive, index, source, destination, adjustment, position, 0); }
    public pw_1 dA0(int archive, int index, int source, int destination, float adjustment, float travel) { return particle(archive, index, source, destination, adjustment, -1, travel); }
    public ao_1 wn0(String name) {
        int id = names.entrySet().stream().filter(entry -> entry.getValue().equals("custom/" + name)).map(Map.Entry::getKey).findFirst().orElseGet(() -> { int key = 20000 + names.size(); names.put(key, "custom/" + name); return key; });
        var controllers = effect(id).getControllers();
        var attached = new HashSet<Integer>();
        for (int i = 0; i < controllers.KB; i++) attached.add(((ParticleControllerExt) controllers.get(i)).trailController);
        var marks = new ArrayList<String>();
        for (int i = 0; i < controllers.KB; i++) if (!attached.contains(i)) marks.add(particle(id, i, -1, 8, .5f, -1, 0, duration((ParticleController) controllers.get(i))));
        ao_1 node = ao_1.pc((event, tween) -> { });
        leaves.put(node, marks);
        return node;
    }

    public void flatten(D2 node, float parent, String prefix, List<String> output) {
        for (int repetition = 0; repetition <= node.m70; repetition++) {
            float start = parent + node.Sq0 + repetition * (node.sS + node.o80);
            String time = Long.toString(Math.round(start * 1000));
            if (leaves.containsKey(node)) for (String mark : leaves.get(node)) output.add(prefix + "," + mark.replace("@", time));
            if (node.xF0 instanceof WorldCallback callback) output.add(prefix + ",C,WorldVisibility," + time + args(callback.target, callback.value ? 1 : 0));
            if (node instanceof pw_1 timeline && !leaves.containsKey(node)) for (Object child : timeline.pk0) flatten((D2) child, start, prefix, output);
        }
    }
}
