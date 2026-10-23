package top.totodile.pokemmo.client.rom.nds.model;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.model.data.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import java.io.IOException;
import java.nio.*;
import java.util.*;
import static top.totodile.pokemmo.client.rom.nds.model.NitroDictionary.u16;

/**
 * 参考 NdsNitroModelParser.a40/decodePolygon/Un0/Xy 的直接解析路径。
 * 对应 AreaHeaderRomChunk、MoveEffectRomChunk、MonsterStatsRomChunk 的字段，
 * 使用当前 LibGDX 的 ModelData 类型替代参考工程的同类包装；不经过转换器或磁盘缓存。
 */
public final class NdsNitroModelParser {
    public static final class Material {
        public String name,texture,palette;
        public int polygon,textureParams,width,height,flags;
        public float scaleU=1,scaleV=1;
        public Color ambient;
        public boolean mirrorS() { return (textureParams&(1<<18))!=0; }
        public boolean mirrorT() { return (textureParams&(1<<19))!=0; }
    }
    public record Parsed(ModelData model,List<Material> materials,NitroTextureDictionary textures,int bones,float positionScale) {}
    private static final class Joint {
        String name; int parent=-1;
        final Vector3 translation=new Vector3(),scale=new Vector3(1,1,1);
        final Quaternion rotation=new Quaternion();
        final Matrix4 bind=new Matrix4();
        ModelNode node;
    }
    private static final class Stack {
        final Matrix4 current=new Matrix4(); final Matrix4[] saved=new Matrix4[32];
        final int[] bones=new int[32]; int bone;
        Stack() { for(int i=0;i<32;i++)saved[i]=new Matrix4(); }
        void restore(int index) { current.set(saved[index]); bone=bones[index]; }
        void store(int index) { saved[index].set(current); bones[index]=bone; }
    }
    private final ByteBuffer data;
    private final Joint[] joints;
    private final List<Material> materials=new ArrayList<>();
    private final ModelData model=new ModelData();
    private final Map<Integer,List<ModelNode>> pieces=new HashMap<>();
    private final int base,materialBase,shapeBase,envelope;
    private final float positionScale,inverseScale;
    private final NitroDictionary shapes;
    private final Stack stack=new Stack();
    private int meshIndex;

    public static Parsed read(byte[] bytes) throws IOException {
        try {
            NdsNitroModelParser parser=new NdsNitroModelParser(bytes);
            parser.decode();
            return new Parsed(parser.model,List.copyOf(parser.materials),new NitroTextureDictionary(bytes),parser.joints.length,parser.positionScale);
        } catch(IOException error) { throw error; }
        catch(RuntimeException error) { throw new IOException("Invalid Nitro model",error); }
    }
    private NdsNitroModelParser(byte[] bytes) throws IOException {
        data=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int section=data.getInt(16);
        if(data.getInt(section)!=0x304c444d)throw new IOException("Missing MDL0");
        var models=new NitroDictionary(data,section+8,4);
        if(models.count!=1)throw new IOException("Expected a single Nitro login model");
        base=section+data.getInt(models.record(0)); model.id=models.name(0);
        materialBase=base+data.getInt(base+8); shapeBase=base+data.getInt(base+12); envelope=base+data.getInt(base+16);
        positionScale=data.getInt(base+28)/4096f; inverseScale=data.getInt(base+32)/4096f;
        if(positionScale<=0)throw new IOException("Invalid Nitro position scale");
        var nodes=new NitroDictionary(data,base+64,4); joints=new Joint[nodes.count];
        for(int i=0;i<joints.length;i++)joints[i]=joint(nodes.name(i),base+64+data.getInt(nodes.record(i)));
        var mats=new NitroDictionary(data,materialBase+4,4);
        for(int i=0;i<mats.count;i++)materials.add(material(mats.name(i),materialBase+data.getInt(mats.record(i))));
        bindTextures(materialBase+u16(data,materialBase),true);
        bindTextures(materialBase+u16(data,materialBase+2),false);
        shapes=new NitroDictionary(data,shapeBase,4);
    }
    private Joint joint(String name,int offset) {
        Joint joint=new Joint(); joint.name=name; data.position(offset);
        int flags=data.getShort()&65535; float first=data.getShort()/4096f;
        if((flags&1)==0)joint.translation.set(data.getInt()/4096f,data.getInt()/4096f,data.getInt()/4096f);
        if((flags&2)==0) {
            Matrix4 rotation=new Matrix4();
            if((flags&8)!=0) {
                float a=data.getShort()/4096f,b=data.getShort()/4096f;
                pivot(rotation,flags>>>4&15,flags>>>8&7,a,b);
            } else {
                rotation.val[0]=first;
                for(int i:new int[]{1,2,4,5,6,8,9,10})rotation.val[i]=data.getShort()/4096f;
            }
            joint.rotation.setFromMatrix(false,rotation);
        }
        if((flags&4)==0)joint.scale.set(data.getInt()/4096f,data.getInt()/4096f,data.getInt()/4096f);
        return joint;
    }
    private static void pivot(Matrix4 matrix,int axis,int signs,float a,float b) {
        int[][] slots={{0,5,6,9,10},{1,4,6,8,10},{2,4,5,8,9},{4,1,2,9,10},{5,0,2,8,10},
                {6,0,1,8,9},{8,1,2,5,6},{9,0,2,4,6},{10,0,1,4,5}};
        if(axis>8)throw new IllegalArgumentException("Invalid Nitro pivot");
        Arrays.fill(matrix.val,0); matrix.val[15]=1;
        int[] p=slots[axis]; matrix.val[p[0]]=(signs&1)==0?1:-1;
        matrix.val[p[1]]=a; matrix.val[p[2]]=b;
        matrix.val[p[3]]=(signs&2)==0?b:-b; matrix.val[p[4]]=(signs&4)==0?a:-a;
    }
    private Material material(String name,int offset) {
        Material mat=new Material(); mat.name=name; mat.polygon=data.getInt(offset+12);
        mat.textureParams=data.getInt(offset+20); mat.flags=u16(data,offset+30);
        mat.width=u16(data,offset+32); mat.height=u16(data,offset+34);
        if((mat.flags&0x400)!=0) {
            int rgb=data.getInt(offset+8)>>>16;
            Color ambient=new Color((rgb&31)/31f,(rgb>>>5&31)/31f,(rgb>>>10&31)/31f,1);
            // NdsNitroModelParser.Xy 只采用三个通道均大于 0.25 的材质环境色。
            if(ambient.r>.25f && ambient.g>.25f && ambient.b>.25f)mat.ambient=ambient;
        }
        if((mat.flags&2)==0) { mat.scaleU=data.getInt(offset+44)/4096f; mat.scaleV=data.getInt(offset+48)/4096f; }
        return mat;
    }
    private void bindTextures(int offset,boolean texture) {
        var dict=new NitroDictionary(data,offset,4);
        for(int i=0;i<dict.count;i++) {
            int record=dict.record(i),indices=materialBase+u16(data,record),count=data.get(record+2)&255;
            for(int j=0;j<count;j++) {
                Material mat=materials.get(data.get(indices+j)&255);
                if(texture)mat.texture=dict.name(i); else mat.palette=dict.name(i);
            }
        }
    }
    private void decode() throws IOException {
        int cursor=base+data.getInt(base+4),material=0;
        while(cursor<materialBase) {
            int command=data.get(cursor++)&255;
            switch(command&15) {
                case 0 -> {}
                case 1 -> cursor=materialBase;
                case 2,12,13 -> cursor+=2;
                case 3 -> stack.restore(data.get(cursor++)&31);
                case 4 -> material=data.get(cursor++)&255;
                case 5 -> polygon(data.get(cursor++)&255,material);
                case 6 -> {
                    int node=data.get(cursor++)&255,parent=data.get(cursor++)&255; cursor++;
                    int save=(command&32)!=0?data.get(cursor++)&31:-1;
                    int restore=(command&64)!=0?data.get(cursor++)&31:-1;
                    if(restore>=0)stack.restore(restore);
                    stack.bone=node; joints[node].bind.set(stack.current);
                    if(node!=parent)joints[node].parent=parent;
                    if(save>=0)stack.store(save);
                }
                case 7,8 -> { cursor++; if((command&32)!=0)cursor++; if((command&64)!=0)cursor++; }
                case 9 -> {
                    int target=data.get(cursor++)&31,count=data.get(cursor++)&255;
                    Matrix4 combined=new Matrix4(); Arrays.fill(combined.val,0);
                    for(int i=0;i<count;i++) {
                        int source=data.get(cursor++)&31,inverse=data.get(cursor++)&255;
                        float weight=(data.get(cursor++)&255)/256f;
                        Matrix4 binding=new Matrix4(); int at=envelope+inverse*84;
                        for(int c=0;c<4;c++)for(int r=0;r<3;r++)binding.val[c*4+r]=data.getInt(at+(c*3+r)*4)/4096f;
                        Matrix4 weighted=stack.saved[source].cpy().mul(binding);
                        for(int j=0;j<16;j++)combined.val[j]+=weighted.val[j]*weight;
                    }
                    stack.current.set(combined); stack.store(target);
                }
                case 11 -> stack.current.scale((command&32)==0?positionScale:inverseScale,
                        (command&32)==0?positionScale:inverseScale,(command&32)==0?positionScale:inverseScale);
                default -> throw new IOException("Unsupported reference SBC opcode: "+command);
            }
        }
        for(Joint joint:joints) {
            joint.node=new ModelNode(); joint.node.id=joint.name;
            joint.node.translation=joint.translation; joint.node.rotation=joint.rotation; joint.node.scale=joint.scale;
        }
        for(int i=0;i<joints.length;i++) {
            List<ModelNode> children=new ArrayList<>(pieces.getOrDefault(i,List.of()));
            for(Joint joint:joints)if(joint.parent==i)children.add(joint.node);
            joints[i].node.children=children.toArray(ModelNode[]::new);
        }
        ModelNode root=new ModelNode(); root.id="Aramature"; root.scale=new Vector3(1,1,1).scl(1/positionScale);
        root.children=Arrays.stream(joints).filter(joint->joint.parent<0).map(joint->joint.node).toArray(ModelNode[]::new);
        model.nodes.add(root);
        for(Material mat:materials) {
            ModelMaterial target=new ModelMaterial(); target.id=mat.name; target.diffuse=new Color(Color.WHITE); model.materials.add(target);
        }
    }
    private void polygon(int shape,int materialIndex) throws IOException {
        Material material=materials.get(materialIndex);
        int entry=shapeBase+data.getInt(shapes.record(shape));
        int cursor=entry+data.getInt(entry+8),end=cursor+data.getInt(entry+12);
        FloatArray vertices=new FloatArray(); ShortArray indices=new ShortArray();
        LinkedHashMap<Integer,Integer> bones=new LinkedHashMap<>();
        Vector3 position=new Vector3(),normal=new Vector3(0,1,0);
        float u=0,v=1; Color color=new Color(Color.WHITE);
        int primitive=0,start=0;
        while(cursor<end) {
            int packedCommands=data.getInt(cursor); cursor+=4;
            for(int c=0;c<4;c++) {
                int opcode=packedCommands>>>(c*8)&255;
                switch(opcode) {
                    case 0,0x11 -> {}
                    case 0x10,0x12,0x13,0x29,0x2a,0x2b,0x30,0x31,0x32,0x33,0x50,0x60,0x72 -> cursor+=4;
                    case 0x34 -> cursor+=128;
                    case 0x70 -> cursor+=12;
                    case 0x71 -> cursor+=8;
                    case 0x14 -> { stack.restore(data.getInt(cursor)&31); cursor+=4; }
                    case 0x15 -> stack.current.idt();
                    case 0x16,0x17,0x18,0x19,0x1a -> {
                        int columns=opcode==0x1a?3:4,rows=opcode==0x16 || opcode==0x18?4:3;
                        Matrix4 matrix=new Matrix4();
                        for(int col=0;col<columns;col++)for(int row=0;row<rows;row++) {
                            matrix.val[col*4+row]=data.getInt(cursor)/4096f; cursor+=4;
                        }
                        if(opcode<=0x17)stack.current.set(matrix); else stack.current.mul(matrix);
                    }
                    case 0x1b,0x1c -> {
                        float x=data.getInt(cursor)/4096f,y=data.getInt(cursor+4)/4096f,z=data.getInt(cursor+8)/4096f; cursor+=12;
                        if(opcode==0x1b)stack.current.scale(x,y,z); else stack.current.translate(x,y,z);
                    }
                    case 0x20 -> {
                        int rgb=data.getInt(cursor); cursor+=4;
                        color.set((rgb&31)/31f,(rgb>>>5&31)/31f,(rgb>>>10&31)/31f,1);
                    }
                    case 0x21 -> { int value=data.getInt(cursor); cursor+=4; normal.set(signed10(value)/512f,signed10(value>>>10)/512f,signed10(value>>>20)/512f); }
                    case 0x22 -> {
                        float s=data.getShort(cursor)/16f,t=data.getShort(cursor+2)/16f; cursor+=4;
                        float scaleS=material.scaleU>0?material.scaleU:1,scaleT=material.scaleV>0?material.scaleV:1;
                        u=s*scaleS/material.width/(material.mirrorS()?2:1); v=1+t*scaleT/material.height/(material.mirrorT()?2:1);
                    }
                    case 0x23,0x24,0x25,0x26,0x27,0x28 -> {
                        int value=data.getInt(cursor); cursor+=4;
                        switch(opcode) {
                            case 0x23 -> { position.set((short)value/4096f,(short)(value>>>16)/4096f,data.getShort(cursor)/4096f); cursor+=4; }
                            case 0x24 -> position.set(signed10(value)/64f,signed10(value>>>10)/64f,signed10(value>>>20)/64f);
                            case 0x25 -> { position.x=(short)value/4096f; position.y=(short)(value>>>16)/4096f; }
                            case 0x26 -> { position.x=(short)value/4096f; position.z=(short)(value>>>16)/4096f; }
                            case 0x27 -> { position.y=(short)value/4096f; position.z=(short)(value>>>16)/4096f; }
                            case 0x28 -> position.add(signed10(value)/4096f,signed10(value>>>10)/4096f,signed10(value>>>20)/4096f);
                        }
                        Vector3 transformed=position.cpy().mul(stack.current);
                        int bone=bones.computeIfAbsent(stack.bone,ignored->bones.size());
                        vertices.addAll(transformed.x,transformed.y,transformed.z,normal.x,normal.y,normal.z,color.r,color.g,color.b,color.a,u,v,bone,1);
                    }
                    case 0x40 -> { primitive=data.getInt(cursor); cursor+=4; start=vertices.size/14; }
                    case 0x41 -> triangulate(indices,primitive,start,vertices.size/14-start);
                    default -> throw new IOException("Unsupported reference GX opcode: 0x"+Integer.toHexString(opcode));
                }
            }
        }
        if(indices.size==0)return;
        if(vertices.size/14>65535)throw new IOException("Nitro polygon exceeds index limit");
        String id="polygon"+meshIndex+++"_"+material.name;
        ModelMesh mesh=new ModelMesh(); mesh.id=id;
        mesh.attributes=new VertexAttribute[]{VertexAttribute.Position(),VertexAttribute.Normal(),VertexAttribute.ColorUnpacked(),VertexAttribute.TexCoords(0),VertexAttribute.BoneWeight(0)};
        mesh.vertices=vertices.toArray();
        ModelMeshPart part=new ModelMeshPart(); part.id=id; part.primitiveType=GL20.GL_TRIANGLES; part.indices=indices.toArray(); mesh.parts=new ModelMeshPart[]{part}; model.meshes.add(mesh);
        ModelNodePart nodePart=new ModelNodePart(); nodePart.meshPartId=id; nodePart.materialId=material.name;
        nodePart.bones=new ArrayMap<>(true,bones.size(),String.class,Matrix4.class);
        for(int bone:bones.keySet())nodePart.bones.put(joints[bone].name,joints[bone].bind.cpy());
        ModelNode node=new ModelNode(); node.id=id; node.parts=new ModelNodePart[]{nodePart};
        pieces.computeIfAbsent(stack.bone,ignored->new ArrayList<>()).add(node);
    }
    private static int signed10(int value) { return (value<<22)>>22; }
    private static void triangle(ShortArray out,int a,int b,int c) { out.add((short)a,(short)b,(short)c); }
    private static void triangulate(ShortArray out,int type,int start,int count) throws IOException {
        switch(type) {
            case 0 -> { for(int i=0;i+2<count;i+=3)triangle(out,start+i,start+i+1,start+i+2); }
            case 1 -> { for(int i=0;i+3<count;i+=4) { triangle(out,start+i,start+i+1,start+i+2); triangle(out,start+i,start+i+2,start+i+3); } }
            case 2,3 -> { for(int i=0;i+2<count;i+=2) { triangle(out,start+i,start+i+1,start+i+2); if(i+3<count)triangle(out,start+i+1,start+i+3,start+i+2); } }
            default -> throw new IOException("Invalid Nitro primitive: "+type);
        }
    }
}
