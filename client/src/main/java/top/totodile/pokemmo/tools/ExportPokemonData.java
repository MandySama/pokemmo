package top.totodile.pokemmo.tools;
import top.totodile.pokemmo.*;
import top.totodile.pokemmo.rom.PokedexData;
import java.io.*;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;
/** 导出不含像素的种族、成长与升级招式规则，供游戏服务创建个体。 */
public final class ExportPokemonData {
 public static void main(String[] args)throws Exception {
  var rom=new RomFiles(ClientFiles.data("../roms")).selected(0);
  var data=new PokedexData(rom);
  try(var out=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(Path.of("protocol/src/main/resources/catalog/pokemon.dat.gz"))))) {
   out.writeInt(0x504b4431);out.writeInt(649);
   for(int id=1;id<=649;id++) {var s=data.species(id);
    out.writeShort(id);for(int stat:s.stats())out.writeByte(stat);
    for(int value:new int[]{s.type1(),s.type2(),s.gender(),s.ability1(),s.ability2(),s.hiddenAbility(),s.growth(),s.friendship()})out.writeByte(value);
    out.writeShort(s.moves().size());for(var m:s.moves()){out.writeByte(m.level());out.writeShort(m.id());out.writeByte(m.pp());out.writeByte(m.type());out.writeByte(m.category());out.writeByte(m.power());out.writeByte(m.accuracy());}
   }
  }
  StringBuilder locations=new StringBuilder("# region,group,map,string_id\n");
  var roms=new RomFiles(ClientFiles.data("../roms"));
  for(int region=0;region<2;region++){
   var atlas=new top.totodile.pokemmo.rom.FireRedAtlas(roms.selected(region==0?1:2));
   for(var area:atlas.areas())locations.append(region).append(',').append(area.key().group()).append(',').append(area.key().map()).append(',').append(atlas.locationName(area.key())).append('\n');
  }
  Files.writeString(Path.of("protocol/src/main/resources/catalog/pokemon-locations.csv"),locations);

 }
}
