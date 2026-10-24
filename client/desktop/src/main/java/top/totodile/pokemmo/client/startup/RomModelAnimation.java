package top.totodile.pokemmo.client.startup;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import java.io.IOException;
import top.totodile.pokemmo.client.rom.nds.model.NdsJointAnimation;

/** 登录模型与地区缓存共用参考 NSBCA 解析器。 */
final class RomModelAnimation {
    static Animation read(byte[] model,byte[] animation,ModelInstance instance) throws IOException {
        return NdsJointAnimation.read(model,animation,instance);
    }
    static Animation prepare(byte[] model,byte[] animation) throws IOException {
        return NdsJointAnimation.prepare(model,animation);
    }
}