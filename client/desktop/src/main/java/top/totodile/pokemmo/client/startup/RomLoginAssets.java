package top.totodile.pokemmo.client.startup;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;

/** 转换参考登录场景的主模型、独立倒影与背景；转换结果按 ROM 资源散列缓存。 */
final class RomLoginAssets {
    record Prepared(Path[] models, byte[] camera, List<Map<String,Integer>> materials, byte[] pattern,
                    byte[][] modelData, byte[][] animationData) {}
    static Prepared prepare(RomInfo rom, Path cache, int variant) throws Exception {
        byte[] bytes=RomReader.readFile(rom,"a/1/6/0");
        Path directory=cache.resolve("models-v5").resolve(NativeTools.hash(bytes)).resolve(Integer.toString(variant));
        Files.createDirectories(directory);
        NdsArchive archive=new NdsArchive(bytes);
        Path[] models=new Path[3];
        byte[][] modelData=new byte[3][], animationData=new byte[3][];
        List<Map<String,Integer>> materials=new ArrayList<>();
        for (int i=0;i<models.length;i++) {
            int model=new int[]{227,230,232}[i]+variant;
            modelData[i]=archive.read(model); animationData[i]=archive.read(model-1);
            materials.add(RomMaterials.read(archive.read(model)));
            Path output=directory.resolve(Integer.toString(model));
            Path marker=output.resolve("model.path");
            if (Files.isRegularFile(marker)) {
                String filename=Files.readString(marker).trim();
                Path cached=output.resolve(filename).normalize();
                if (cached.getParent().equals(output) && Files.isRegularFile(cached)) {
                    removePolygonMetadata(cached); models[i]=cached; continue;
                }
            }
            // 每个任务使用独立目录，失败或中断不会留下可误用的缓存标记。
            Path work=Files.createTempDirectory(directory,"convert-");
            // 转换器需要动画文件来保留完整节点层级；输出的动画会全部丢弃，由 ROM 解码替换。
            Path input=work.resolve("model.nsbmd"), animation=work.resolve("animation.nsbca");
            Files.write(input,modelData[i]); Files.write(animation,animationData[i]);
            List<String> args=new ArrayList<>(List.of(NativeTools.extract(cache,"apicula.exe").toString(),
                    "convert",input.toString(),animation.toString(),"-f=gltf","-o",work.resolve("out").toString()));
            if (i==0) {
                Path pattern=work.resolve("pattern.nsbtp"); Files.write(pattern,archive.read(model+1));
                args.add(3,pattern.toString());
            }
            Process process=new ProcessBuilder(args).redirectErrorStream(true).redirectOutput(work.resolve("convert.log").toFile()).start();
            if (!process.waitFor(60,TimeUnit.SECONDS)) { process.destroyForcibly(); throw new IOException("Login model conversion timed out"); }
            if (process.exitValue()!=0) throw new IOException("Login model conversion failed: "+Files.readString(work.resolve("convert.log")));
            Path generated;
            try (var files=Files.list(work.resolve("out"))) {
                generated=files.filter(path -> path.toString().endsWith(".gltf")).findFirst().orElseThrow(() -> new IOException("No converted model"));
            }
            // 只有成功产物进入正式目录。保留转换日志用于定位资源问题。
            Files.createDirectories(output);
            try (var files=Files.list(work.resolve("out"))) {
                for (Path file:files.toList()) Files.move(file,output.resolve(file.getFileName()),StandardCopyOption.REPLACE_EXISTING);
            }
            models[i]=output.resolve(generated.getFileName());
            removePolygonMetadata(models[i]);
            Files.writeString(marker,models[i].getFileName().toString());
        }
        return new Prepared(models,archive.read(233+variant),materials,archive.read(228+variant),modelData,animationData);
    }
    /** apicula.encode_ngons 已输出三角形；仅去除建模工具恢复多边形用的可选标记。 */
    private static void removePolygonMetadata(Path file) throws IOException {
        String extension="FB_ngon_encoding";
        JsonValue root=new JsonReader().parse(Files.readString(file));
        JsonValue required=root.get("extensionsRequired");
        if(required!=null) for(JsonValue entry:required)
            if(extension.equals(entry.asString())) throw new IOException("Unexpected required polygon extension");
        boolean changed=false;
        JsonValue meshes=root.get("meshes");
        if(meshes!=null) for(JsonValue mesh:meshes) {
            JsonValue primitives=mesh.get("primitives");
            if(primitives==null) continue;
            for(JsonValue primitive:primitives) {
                JsonValue extensions=primitive.get("extensions");
                if(extensions==null || !extensions.has(extension)) continue;
                if(primitive.getInt("mode",4)!=4) throw new IOException("Expected triangulated login mesh");
                extensions.remove(extension); changed=true;
                if(extensions.size==0) primitive.remove("extensions");
            }
        }
        JsonValue used=root.get("extensionsUsed");
        if(used!=null) for(int i=used.size-1;i>=0;i--) if(extension.equals(used.getString(i))) {
            used.remove(i); changed=true;
        }
        if(!changed) return;
        Path temporary=Files.createTempFile(file.getParent(),"gltf-",".tmp");
        try {
            Files.writeString(temporary,root.toJson(JsonWriter.OutputType.json));
            Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temporary); }
    }
}
