package top.totodile.pokemmo.client.startup;

import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;
import top.totodile.pokemmo.client.rom.nds.model.NdsNitroModelParser;

/** 参考 Abstract3DCutsceneScene 17/18：直接解析 ROM 中的三层模型及其动画。 */
final class RomLoginAssets {
    record Prepared(NdsNitroModelParser.Parsed[] models,byte[] camera,byte[] pattern,
            com.badlogic.gdx.graphics.g3d.model.Animation[] animations) {}
    static Prepared prepare(RomInfo rom,int variant) throws Exception {
        var archive=new NdsArchive(RomReader.readFile(rom,"a/1/6/0"));
        var models=new NdsNitroModelParser.Parsed[3];
        var animations=new com.badlogic.gdx.graphics.g3d.model.Animation[3];
        for(int i=0;i<3;i++) {
            int index=new int[]{227,230,232}[i]+variant;
            byte[] modelData=archive.read(index);
            models[i]=NdsNitroModelParser.read(modelData);
            animations[i]=RomModelAnimation.prepare(modelData,archive.read(index-1));
        }
        return new Prepared(models,archive.read(233+variant),archive.read(228+variant),animations);
    }
}
