package top.totodile.pokemmo.client.startup;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.math.Matrix3;

/** SRT0 needs rotation as well as the stock shader's UV offset and scale. */
final class RomTitleShaderProvider extends DefaultShaderProvider {
    static final class Transform extends Attribute {
        static final long TYPE=register("romTitleUV");
        final Matrix3 matrix=new Matrix3();
        Transform() { super(TYPE); }
        @Override public Attribute copy() { Transform t=new Transform(); t.matrix.set(matrix); return t; }
        @Override public int compareTo(Attribute other) { return Long.compare(type,other.type); }
    }
    RomTitleShaderProvider(DefaultShader.Config config) { super(config); }
    @Override protected Shader createShader(Renderable renderable) {
        String vertex=DefaultShader.getDefaultVertexShader()
                .replace("uniform vec4 u_diffuseUVTransform;","uniform vec4 u_diffuseUVTransform;\nuniform mat3 u_romTitleUV;")
                .replace("v_diffuseUV = u_diffuseUVTransform.xy + a_texCoord0 * u_diffuseUVTransform.zw;",
                        "v_diffuseUV = (u_romTitleUV * vec3(u_diffuseUVTransform.xy + a_texCoord0 * u_diffuseUVTransform.zw, 1.0)).xy;");
        return new DefaultShader(renderable,config,DefaultShader.createPrefix(renderable,config),vertex,DefaultShader.getDefaultFragmentShader()) {
            final Matrix3 identity=new Matrix3();
            final int transform=register(new Uniform("u_romTitleUV"));
            @Override public void render(Renderable value,com.badlogic.gdx.graphics.g3d.Attributes attributes) {
                Transform uv=(Transform)value.material.get(Transform.TYPE);
                set(transform,uv==null?identity:uv.matrix);
                super.render(value,attributes);
            }
        };
    }
}

