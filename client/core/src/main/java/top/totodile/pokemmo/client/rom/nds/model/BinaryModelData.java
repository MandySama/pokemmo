package top.totodile.pokemmo.client.rom.nds.model;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.model.data.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** wh0_2 / Ou0 的字段式二进制读写，适配当前 LibGDX ModelData，不创建 GL 资源。 */
final class BinaryModelData {
    private BinaryModelData() {}
    static int count(DataInput in)throws IOException {
        int n=in.readInt();if(n<0||n>1_000_000)throw new IOException("Invalid model array length: "+n);return n;
    }
    static void text(DataOutput out,String s)throws IOException {
        if(s==null){out.writeInt(-1);return;}byte[] bytes=s.getBytes(StandardCharsets.UTF_8);out.writeInt(bytes.length);out.write(bytes);
    }
    static String text(DataInput in)throws IOException {
        int n=in.readInt();if(n==-1)return null;if(n<0||n>1_000_000)throw new IOException("Invalid model string");
        byte[] bytes=new byte[n];in.readFully(bytes);return new String(bytes,StandardCharsets.UTF_8);
    }
    static void floats(DataOutput out,float[] a)throws IOException {out.writeInt(a==null?-1:a.length);if(a!=null)for(float v:a)out.writeFloat(v);}
    static float[] floats(DataInput in)throws IOException {int n=in.readInt();if(n==-1)return null;if(n<0||n>8_000_000)throw new IOException("Invalid vertex count");float[] a=new float[n];for(int i=0;i<n;i++)a[i]=in.readFloat();return a;}
    static void vector(DataOutput out,Vector3 v)throws IOException {out.writeBoolean(v!=null);if(v!=null){out.writeFloat(v.x);out.writeFloat(v.y);out.writeFloat(v.z);}}
    static Vector3 vector(DataInput in)throws IOException {return in.readBoolean()?new Vector3(in.readFloat(),in.readFloat(),in.readFloat()):null;}
    static void quat(DataOutput out,Quaternion q)throws IOException {out.writeBoolean(q!=null);if(q!=null){out.writeFloat(q.x);out.writeFloat(q.y);out.writeFloat(q.z);out.writeFloat(q.w);}}
    static Quaternion quat(DataInput in)throws IOException {return in.readBoolean()?new Quaternion(in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat()):null;}
    static void color(DataOutput out,Color c)throws IOException {out.writeBoolean(c!=null);if(c!=null){out.writeFloat(c.r);out.writeFloat(c.g);out.writeFloat(c.b);out.writeFloat(c.a);}}
    static Color color(DataInput in)throws IOException {return in.readBoolean()?new Color(in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat()):null;}
    static void write(DataOutput out,ModelData model)throws IOException {
        text(out,model.id);for(short v:model.version)out.writeShort(v);
        out.writeInt(model.meshes.size);
        for(var mesh:model.meshes) {
            text(out,mesh.id);out.writeInt(mesh.attributes.length);
            for(var a:mesh.attributes){out.writeInt(a.usage);out.writeInt(a.numComponents);out.writeInt(a.type);out.writeBoolean(a.normalized);text(out,a.alias);out.writeInt(a.unit);}
            floats(out,mesh.vertices);out.writeInt(mesh.parts.length);
            for(var part:mesh.parts){text(out,part.id);out.writeInt(part.primitiveType);out.writeInt(part.indices.length);for(short v:part.indices)out.writeShort(v);}
        }
        out.writeInt(model.materials.size);
        for(var m:model.materials) {
            text(out,m.id);text(out,m.type==null?null:m.type.name());color(out,m.ambient);color(out,m.diffuse);color(out,m.specular);color(out,m.emissive);color(out,m.reflection);
            out.writeFloat(m.shininess);out.writeFloat(m.opacity);out.writeInt(m.textures==null?-1:m.textures.size);
            if(m.textures!=null)for(var t:m.textures){text(out,t.id);text(out,t.fileName);vector2(out,t.uvTranslation);vector2(out,t.uvScaling);out.writeInt(t.usage);}
        }
        out.writeInt(model.nodes.size);for(var node:model.nodes)node(out,node);
        out.writeInt(model.animations.size);
        for(var a:model.animations) {
            text(out,a.id);out.writeInt(a.nodeAnimations.size);
            for(var channel:a.nodeAnimations){text(out,channel.nodeId);vectors(out,channel.translation);rotations(out,channel.rotation);vectors(out,channel.scaling);}
        }
    }
    static ModelData read(DataInput in)throws IOException {
        var model=new ModelData();model.id=text(in);for(int i=0;i<model.version.length;i++)model.version[i]=in.readShort();
        for(int n=count(in);n>0;n--) {
            var mesh=new ModelMesh();mesh.id=text(in);mesh.attributes=new VertexAttribute[count(in)];
            for(int i=0;i<mesh.attributes.length;i++)mesh.attributes[i]=new VertexAttribute(in.readInt(),in.readInt(),in.readInt(),in.readBoolean(),text(in),in.readInt());
            mesh.vertices=floats(in);mesh.parts=new ModelMeshPart[count(in)];
            for(int i=0;i<mesh.parts.length;i++){var p=new ModelMeshPart();p.id=text(in);p.primitiveType=in.readInt();p.indices=new short[count(in)];for(int j=0;j<p.indices.length;j++)p.indices[j]=in.readShort();mesh.parts[i]=p;}
            model.meshes.add(mesh);
        }
        for(int n=count(in);n>0;n--) {
            var m=new ModelMaterial();m.id=text(in);String type=text(in);m.type=type==null?null:ModelMaterial.MaterialType.valueOf(type);
            m.ambient=color(in);m.diffuse=color(in);m.specular=color(in);m.emissive=color(in);m.reflection=color(in);m.shininess=in.readFloat();m.opacity=in.readFloat();
            int textures=in.readInt();if(textures < -1 || textures>1_000_000)throw new IOException("Invalid model textures");
            if(textures>=0){m.textures=new Array<>(textures);for(int i=0;i<textures;i++){var t=new ModelTexture();t.id=text(in);t.fileName=text(in);t.uvTranslation=vector2(in);t.uvScaling=vector2(in);t.usage=in.readInt();m.textures.add(t);}}
            model.materials.add(m);
        }
        for(int n=count(in);n>0;n--)model.nodes.add(node(in,0));
        for(int n=count(in);n>0;n--) {
            var a=new ModelAnimation();a.id=text(in);a.nodeAnimations=new Array<>();
            for(int c=count(in);c>0;c--){var channel=new ModelNodeAnimation();channel.nodeId=text(in);channel.translation=vectors(in);channel.rotation=rotations(in);channel.scaling=vectors(in);a.nodeAnimations.add(channel);}
            model.animations.add(a);
        }
        return model;
    }
    private static void vector2(DataOutput out,Vector2 v)throws IOException {out.writeBoolean(v!=null);if(v!=null){out.writeFloat(v.x);out.writeFloat(v.y);}}
    private static Vector2 vector2(DataInput in)throws IOException {return in.readBoolean()?new Vector2(in.readFloat(),in.readFloat()):null;}
    private static void node(DataOutput out,ModelNode n)throws IOException {
        text(out,n.id);vector(out,n.translation);quat(out,n.rotation);vector(out,n.scale);text(out,n.meshId);
        out.writeInt(n.parts==null?-1:n.parts.length);
        if(n.parts!=null)for(var p:n.parts){text(out,p.materialId);text(out,p.meshPartId);out.writeInt(p.bones==null?-1:p.bones.size);
            if(p.bones!=null)for(var b:p.bones){text(out,b.key);for(float v:b.value.val)out.writeFloat(v);}
            out.writeInt(p.uvMapping==null?-1:p.uvMapping.length);if(p.uvMapping!=null)for(var row:p.uvMapping){out.writeInt(row==null?-1:row.length);if(row!=null)for(int v:row)out.writeInt(v);}}
        out.writeInt(n.children==null?-1:n.children.length);if(n.children!=null)for(var child:n.children)node(out,child);
    }
    private static int nullableCount(DataInput in)throws IOException {int n=in.readInt();if(n < -1 || n>1_000_000)throw new IOException("Invalid model array");return n;}
    private static ModelNode node(DataInput in,int depth)throws IOException {
        if(depth>128)throw new IOException("Model node nesting too deep");
        var n=new ModelNode();n.id=text(in);n.translation=vector(in);n.rotation=quat(in);n.scale=vector(in);n.meshId=text(in);
        int count=nullableCount(in);if(count>=0){n.parts=new ModelNodePart[count];for(int i=0;i<count;i++) {
            var p=new ModelNodePart();p.materialId=text(in);p.meshPartId=text(in);int bones=nullableCount(in);
            if(bones>=0){p.bones=new ArrayMap<>(true,bones,String.class,Matrix4.class);for(int j=0;j<bones;j++){String key=text(in);float[] v=new float[16];for(int k=0;k<16;k++)v[k]=in.readFloat();p.bones.put(key,new Matrix4(v));}}
            int rows=nullableCount(in);if(rows>=0){p.uvMapping=new int[rows][];for(int j=0;j<rows;j++){int cols=nullableCount(in);if(cols>=0){p.uvMapping[j]=new int[cols];for(int k=0;k<cols;k++)p.uvMapping[j][k]=in.readInt();}}}n.parts[i]=p;
        }}
        int children=nullableCount(in);if(children>=0){n.children=new ModelNode[children];for(int i=0;i<children;i++)n.children[i]=node(in,depth+1);}return n;
    }
    private static void vectors(DataOutput out,Array<ModelNodeKeyframe<Vector3>> keys)throws IOException {out.writeInt(keys==null?-1:keys.size);if(keys!=null)for(var k:keys){out.writeFloat(k.keytime);vector(out,k.value);}}
    private static Array<ModelNodeKeyframe<Vector3>> vectors(DataInput in)throws IOException {int n=nullableCount(in);if(n<0)return null;var keys=new Array<ModelNodeKeyframe<Vector3>>(n);for(int i=0;i<n;i++){var k=new ModelNodeKeyframe<Vector3>();k.keytime=in.readFloat();k.value=vector(in);keys.add(k);}return keys;}
    private static void rotations(DataOutput out,Array<ModelNodeKeyframe<Quaternion>> keys)throws IOException {out.writeInt(keys==null?-1:keys.size);if(keys!=null)for(var k:keys){out.writeFloat(k.keytime);quat(out,k.value);}}
    private static Array<ModelNodeKeyframe<Quaternion>> rotations(DataInput in)throws IOException {int n=nullableCount(in);if(n<0)return null;var keys=new Array<ModelNodeKeyframe<Quaternion>>(n);for(int i=0;i<n;i++){var k=new ModelNodeKeyframe<Quaternion>();k.keytime=in.readFloat();k.value=quat(in);keys.add(k);}return keys;}
}
