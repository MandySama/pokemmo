package top.totodile.pokemmo.client.rom.nds.model;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.utils.Array;
import java.io.IOException;
import java.util.*;

/** 参考 NitroModelRenderer / NdsNitroModelParser.Xy：直接创建 Mesh、骨骼及原始纹理材质。 */
public final class NitroModelRenderer implements AutoCloseable {
    private final Model model;
    public final ModelInstance instance;
    private final NitroTextureDictionary dictionary;
    private final Map<String,Texture> textures=new HashMap<>();
    private final Map<String,NdsNitroModelParser.Material> materials=new HashMap<>();
    public NitroModelRenderer(NdsNitroModelParser.Parsed parsed) throws IOException {
        this(parsed,1f/64);
    }
    /** units converts original model units to the scene's world units. */
    public NitroModelRenderer(NdsNitroModelParser.Parsed parsed,float units) throws IOException {
        dictionary=parsed.textures();
        model=new Model(parsed.model()); instance=new ModelInstance(model);
        // 参考 GdxAnimatedModelInstance 的 Fg0 是乘法：在 1/positionScale 上再乘 positionScale/64。
        instance.nodes.first().scale.scl(parsed.positionScale()*units);
        instance.calculateTransforms();
        try {
            for(var source:parsed.materials()) {
                materials.put(source.name,source);
                Material target=instance.getMaterial(source.name);
                if(target==null)continue;
                int polygon=source.polygon;
                int faces=switch(polygon>>>6&3) { case 0->GL20.GL_FRONT_AND_BACK; case 1->GL20.GL_FRONT; case 2->GL20.GL_BACK; default->GL20.GL_NONE; };
                target.set(IntAttribute.createCullFace(faces));
                if(source.ambient!=null)target.set(ColorAttribute.createAmbientLight(source.ambient));
                target.set(new DepthTestAttribute((polygon&(1<<4))!=0?GL20.GL_EQUAL:GL20.GL_LESS,true));
                float alpha=(polygon>>>16&31)/31f;
                if(source.texture!=null) {
                    target.set(TextureAttribute.createDiffuse(texture(source,source.texture,source.palette)));
                    var info=dictionary.info(source.texture);
                    if(info!=null&&(info.format()==1 || info.format()==6 || info.transparent()) || alpha<1) {
                        target.set(new BlendingAttribute(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA,alpha));
                        // 登录场景使用 data/shaders/default.fragment.glsl，其实际裁剪阈值为 0.1。
                        target.set(FloatAttribute.createAlphaTest(.1f));
                    }
                } else if(alpha<1)target.set(new BlendingAttribute(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA,alpha));
            }
        } catch(IOException | RuntimeException error) { close(); throw error; }
    }
    private Texture texture(NdsNitroModelParser.Material source,String name,String palette) throws IOException {
        String key=name+"/"+palette+"/"+source.textureParams;
        Texture texture=textures.get(key);
        if(texture==null) {
            // GdxTextureStreamDecoderWrapper.De0: missing field materials use its solid-black fallback.
            Pixmap pixels;
            if(dictionary.has(name,palette))pixels=dictionary.pixels(name,palette,source.mirrorS(),source.mirrorT());
            else {pixels=new Pixmap(1,1,Pixmap.Format.RGBA8888);pixels.setColor(Color.BLACK);pixels.fill();}
            try { texture=new Texture(pixels); }
            finally { pixels.dispose(); }
            // 参考 NdsNitroModelParser：eb0_1.Y30 对应 Nearest，避免棋盘与模型纹理被平滑。
            texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            // GdxTextureStreamDecoderWrapper.Od0 统一采用 Repeat，镜像已由像素字典展开。
            texture.setWrap(Texture.TextureWrap.Repeat,Texture.TextureWrap.Repeat);
            textures.put(key,texture);
        }
        return texture;
    }
    public void setTexture(String material,String texture,String palette) throws IOException {
        var source=materials.get(material);
        Material target=instance.getMaterial(material);
        if(source!=null && target!=null)target.set(TextureAttribute.createDiffuse(texture(source,texture,palette)));
    }
    public int maxBones() {
        Array<Renderable> renderables=new Array<>();
        var pool=new com.badlogic.gdx.utils.Pool<Renderable>() {
            @Override protected Renderable newObject() { return new Renderable(); }
        };
        instance.getRenderables(renderables,pool);
        int count=0; for(var renderable:renderables)if(renderable.bones!=null)count=Math.max(count,renderable.bones.length);
        pool.freeAll(renderables); return count;
    }
    @Override public void close() { model.dispose(); textures.values().forEach(Texture::dispose); textures.clear(); }
}
