package top.totodile.pokemmo.client.rom.nds.model;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.model.data.*;
import com.badlogic.gdx.utils.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.*;

/** NitroModelRenderer.Iq/RG 的当前架构适配：持久化 CPU ModelData，不创建 GL 对象。
 * 使用独立格式版本，不能把参考客户端的 Ou0 二进制文件误读为 ModelData。
 */
final class ModelCacheCodec {
    static final class Data {
        public ModelData[] models;
        public NdsNitroModelParser.Material[][] materials;
        public int[] bones;
        public float[] scales;
        public byte[][][] resourceAnimations;
    }
    private static Json json() {
        Json json=new Json();
        json.setUsePrototypes(false);
        json.setElementType(ModelData.class,"meshes",ModelMesh.class);
        json.setElementType(ModelData.class,"materials",ModelMaterial.class);
        json.setElementType(ModelData.class,"nodes",ModelNode.class);
        json.setElementType(ModelData.class,"animations",ModelAnimation.class);
        json.setElementType(ModelAnimation.class,"nodeAnimations",ModelNodeAnimation.class);
        json.setElementType(ModelNodeAnimation.class,"translation",ModelNodeKeyframe.class);
        json.setElementType(ModelNodeAnimation.class,"rotation",ModelNodeKeyframe.class);
        json.setElementType(ModelNodeAnimation.class,"scaling",ModelNodeKeyframe.class);
        json.setSerializer(VertexAttribute.class,new Json.Serializer<VertexAttribute>() {
            public void write(Json j,VertexAttribute a,Class type) {
                j.writeObjectStart();j.writeValue("usage",a.usage);j.writeValue("components",a.numComponents);
                j.writeValue("type",a.type);j.writeValue("normalized",a.normalized);j.writeValue("alias",a.alias);
                j.writeValue("unit",a.unit);j.writeObjectEnd();
            }
            public VertexAttribute read(Json j,JsonValue v,Class type) {
                return new VertexAttribute(v.getInt("usage"),v.getInt("components"),v.getInt("type"),
                        v.getBoolean("normalized"),v.getString("alias"),v.getInt("unit"));
            }
        });
        json.setSerializer(ModelNodePart.class,new Json.Serializer<ModelNodePart>() {
            public void write(Json j,ModelNodePart p,Class type) {
                j.writeObjectStart();j.writeValue("materialId",p.materialId);j.writeValue("meshPartId",p.meshPartId);
                j.writeValue("uvMapping",p.uvMapping,int[][].class);
                j.writeArrayStart("bones");
                if(p.bones!=null)for(var bone:p.bones) {
                    j.writeObjectStart();j.writeValue("node",bone.key);j.writeValue("matrix",bone.value.val,float[].class);j.writeObjectEnd();
                }
                j.writeArrayEnd();j.writeObjectEnd();
            }
            public ModelNodePart read(Json j,JsonValue v,Class type) {
                var p=new ModelNodePart();p.materialId=v.getString("materialId");p.meshPartId=v.getString("meshPartId");
                p.uvMapping=j.readValue("uvMapping",int[][].class,v);
                JsonValue bones=v.get("bones");
                if(bones.size>0) {
                    p.bones=new ArrayMap<>();
                    for(JsonValue b:bones)p.bones.put(b.getString("node"),new com.badlogic.gdx.math.Matrix4(b.get("matrix").asFloatArray()));
                }
                return p;
            }
        });
        return json;
    }
    static List<NdsNitroModelParser.Parsed> parse(byte[] model,byte[] textures,List<byte[]> animations) throws IOException {
        var parsed=NdsNitroModelParser.readAll(model,textures);
        if(animations.isEmpty())return parsed;
        var result=new java.util.ArrayList<NdsNitroModelParser.Parsed>();
        for(var p:parsed) {
            for(byte[] source:animations) {
                // NSBCA 骨骼动画转成当前 ModelData；其余 Nitro 材质动画原样保留。
                if(source.length<4||source[0]!='B'||source[1]!='C'||source[2]!='A'||source[3]!='0')continue;
                var decoded=NdsJointAnimation.prepare(model,source);
                var animation=new ModelAnimation();animation.id="field-"+p.model().animations.size;animation.nodeAnimations=new Array<>();
                for(var channel:decoded.nodeAnimations) {
                    var node=new ModelNodeAnimation();node.nodeId=channel.node.id;
                    node.translation=frames(channel.translation);node.rotation=frames(channel.rotation);node.scaling=frames(channel.scaling);
                    animation.nodeAnimations.add(node);
                }
                p.model().animations.add(animation);
            }
            result.add(new NdsNitroModelParser.Parsed(p.model(),p.materials(),p.textures(),p.bones(),p.positionScale(),animations));
        }
        return List.copyOf(result);
    }
    private static <T> Array<ModelNodeKeyframe<T>> frames(Array<com.badlogic.gdx.graphics.g3d.model.NodeKeyframe<T>> source) {
        if(source==null)return null;
        var result=new Array<ModelNodeKeyframe<T>>(source.size);
        for(var key:source){var target=new ModelNodeKeyframe<T>();target.keytime=key.keytime;target.value=key.value;result.add(target);}
        return result;
    }
    static byte[] encode(List<NdsNitroModelParser.Parsed> parsed) throws IOException {
        Data data=new Data();int count=parsed.size();
        data.models=new ModelData[count];data.materials=new NdsNitroModelParser.Material[count][];
        data.bones=new int[count];data.scales=new float[count];
        data.resourceAnimations=new byte[count][][];
        for(int i=0;i<count;i++) {
            var p=parsed.get(i);data.models[i]=p.model();data.materials[i]=p.materials().toArray(NdsNitroModelParser.Material[]::new);
            data.bones[i]=p.bones();data.scales[i]=p.positionScale();
            data.resourceAnimations[i]=p.resourceAnimations().toArray(byte[][]::new);
        }
        var bytes=new ByteArrayOutputStream();
        try(var output=new GZIPOutputStream(bytes)) { output.write(json().toJson(data,Data.class).getBytes(StandardCharsets.UTF_8)); }
        return bytes.toByteArray();
    }
    static List<NdsNitroModelParser.Parsed> decode(byte[] bytes,byte[] textures) throws IOException {
        try(var input=new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            var output=new ByteArrayOutputStream();byte[] buffer=new byte[65536];
            for(int n;(n=input.read(buffer))!=-1;) {
                if(output.size()>64*1024*1024-n)throw new IOException("Model cache record too large");
                output.write(buffer,0,n);
            }
            Data d=json().fromJson(Data.class,output.toString(StandardCharsets.UTF_8));
            if(d.models.length!=d.materials.length||d.models.length!=d.bones.length||d.models.length!=d.scales.length)
                throw new IOException("Invalid cached model arrays");
            var result=new java.util.ArrayList<NdsNitroModelParser.Parsed>();
            for(int i=0;i<d.models.length;i++) {
                var materials=List.of(d.materials[i]);
                var dictionary=materials.stream().anyMatch(m->m.texture!=null)?new NitroTextureDictionary(textures):null;
                result.add(new NdsNitroModelParser.Parsed(d.models[i],materials,dictionary,d.bones[i],d.scales[i],List.of(d.resourceAnimations[i])));
            }
            return List.copyOf(result);
        }catch(RuntimeException error){throw new IOException("Invalid model cache record",error);}
    }
}
