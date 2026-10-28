package top.totodile.pokemmo.client.rom.nds.gen4;
import java.io.IOException;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokemonSprites;
/** PlatinumBattleSceneLoader.Dt and native trfgra/HGSS a/0/5/8 five-member portraits. */
public record Gen4Trainer(int id,int portrait,int trainerClass,String type,String name,BwPokemonSprites.Pixels pixels) {
 public static Gen4Trainer load(RomInfo rom,int id,int model)throws IOException {
  boolean hg=rom.type()==RomType.HEART_GOLD_SOUL_SILVER;
  if(!hg&&rom.type()!=RomType.PLATINUM)throw new IOException("Not a Gen4 trainer ROM");
  var data=new NdsArchive(RomReader.readFile(rom,hg?"a/0/5/5":"poketool/trainer/trdata.narc"));
  if(id<0||id>=data.size())throw new IOException("Invalid Gen4 trainer "+id);
  byte[] trainer=data.readRaw(id);if(trainer.length<20)throw new IOException("Truncated Gen4 trainer");
  int type=trainer[1]&255,portrait=type,base=portrait*5;
  var gfx=new NdsArchive(RomReader.readFile(rom,hg?"a/0/5/8":"poketool/trgra/trfgra.narc"));
  if(base+2>=gfx.size())throw new IOException("Missing Gen4 portrait "+portrait);
  byte[] tiles=gfx.read(base); // NitroScreenResource uses only Vb & 255 for layout; upper flags are not bitmap mode.
  tiles[37]=tiles[38]=tiles[39]=0;
  var pixels=BwPokemonSprites.compose(tiles,gfx.read(base+1),gfx.read(base+2),0,80,-1);
  var messages=new NdsArchive(RomReader.readFile(rom,hg?"a/0/2/7":"msgdata/pl_msg.narc"));
  int shift=rom.code().endsWith("J")?(hg?10:6):rom.code().endsWith("K")?5:0;
  String[] names=Gen4Text.decode(messages.readRaw((hg?729:618)-shift),rom),types=Gen4Text.decode(messages.readRaw((hg?730:619)-shift),rom);
  String name=id<names.length?names[id]:"",label=type<types.length?types[type]:"";
  if(!hg&&type==63)name="Barry";
  return new Gen4Trainer(id,portrait,type,label,name,pixels);
 }
}
