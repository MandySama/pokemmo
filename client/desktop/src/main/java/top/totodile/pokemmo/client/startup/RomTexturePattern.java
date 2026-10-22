package top.totodile.pokemmo.client.startup;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import java.io.IOException;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import net.mgsx.gltf.scene3d.scene.Scene;

/** PAT0 逐帧换图（标题模型的眨眼）；glTF 本身不表达贴图切换。 */
final class RomTexturePattern implements AutoCloseable {
    private record Track(TextureAttribute target,int[] frames,Texture[] images) {}
    private final List<Track> tracks=new ArrayList<>();
    private final Map<String,Texture> textures=new HashMap<>();
    private int duration;
    RomTexturePattern(byte[] bytes,Path directory,Scene scene) throws IOException {
        try {
            ByteBuffer b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            int section=b.getInt(16), info=section+8, count=bytes[info+1]&255;
            if (count==0) return;
            int animation=section+b.getInt(info+16+count*4);
            duration=Short.toUnsignedInt(b.getShort(animation+4));
            int names=animation+Short.toUnsignedInt(b.getShort(animation+8));
            int textureCount=bytes[animation+6]&255;
            String[] filenames=new String[textureCount];
            for (int i=0;i<textureCount;i++) filenames[i]=name(bytes,names+i*16);
            info=animation+12; count=bytes[info+1]&255;
            int records=info+16+count*4;
            for (int i=0;i<count;i++) {
                String materialName=name(bytes,records+count*8+i*16);
                var material=scene.modelInstance.getMaterial(materialName);
                if (material==null) continue;
                TextureAttribute target=material.get(TextureAttribute.class,TextureAttribute.Diffuse);
                if (target==null) continue;
                int keys=b.getInt(records+i*8), offset=animation+Short.toUnsignedInt(b.getShort(records+i*8+6));
                if (keys<1 || keys>65535) throw new IOException("Invalid PAT0 track");
                int[] frames=new int[keys]; Texture[] images=new Texture[keys];
                for (int key=0;key<keys;key++) {
                    frames[key]=Short.toUnsignedInt(b.getShort(offset+key*4));
                    int texture=bytes[offset+key*4+2]&255;
                    if (texture>=textureCount) throw new IOException("Invalid PAT0 texture index");
                    String filename=filenames[texture].replaceAll("[^A-Za-z0-9]","_")+".png";
                    Path path=directory.resolve(filename).normalize();
                    if (!path.getParent().equals(directory)) throw new IOException("Invalid PAT0 texture name");
                    images[key]=textures.computeIfAbsent(filename,ignored -> new Texture(new FileHandle(path.toFile())));
                }
                tracks.add(new Track(target,frames,images));
            }
        } catch (IOException | RuntimeException error) { close(); throw new IOException("Invalid title texture animation",error); }
    }
    private static String name(byte[] bytes,int offset) {
        int size=0; while(size<16 && bytes[offset+size]!=0) size++;
        return new String(bytes,offset,size,StandardCharsets.US_ASCII);
    }
    void update(float time) {
        int frame=Math.min(Math.max(0,(int)(time*30)),Math.max(0,duration-1));
        for (Track track:tracks) {
            int key=0; while(key+1<track.frames.length && track.frames[key+1]<=frame) key++;
            track.target.textureDescription.texture=track.images[key];
        }
    }
    @Override public void close() { textures.values().forEach(Texture::dispose); textures.clear(); tracks.clear(); }
}
