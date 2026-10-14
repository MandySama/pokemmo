package top.totodile.pokemmo.rom;
import java.awt.image.BufferedImage;import java.io.IOException;import java.nio.file.Path;import java.nio.ByteBuffer;import java.util.*;
/** 缎带使用32像素4bpp图块与公共色板；未获得项仅保留透明轮廓。 */
public final class RibbonSprites {
 public static List<BufferedImage> load(Path rom,List<Integer> owned)throws IOException{
  var archive=new NdsArchive(rom,"/a/0/7/8");var palette=archive.file(6);int pal=24+palette.getInt(36);List<BufferedImage> output=new ArrayList<>();
  int[][] fixed={{16,0,8},{22,0,38},{23,0,39},{20,2,11},{27,4,12},{28,0,13},{29,0,14},{30,0,15},{31,3,16},{32,0,17},{33,2,18},{24,1,19},{25,3,20},{26,0,21},{34,0,24}};
  for(int id=0;id<35;id++){
   int graphic=id<15?fixed[id][2]:(id-15)%5;
   int bank=id<15?fixed[id][1]:(id-15)/5;
   int ribbonId=id<15?fixed[id][0]:(id-15)%5;

   var pixels=archive.file(graphic+16);int start=24+pixels.getInt(44);var image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
   for(int y=0;y<32;y++)for(int x=0;x<32;x++){int p=((y/8)*4+x/8)*64+(y%8)*8+x%8,v=pixels.get(start+p/2)&255,index=v>>((p&1)*4)&15;if(index==0)continue;int c=palette.getShort(pal+bank*32+index*2)&65535;image.setRGB(x,y,owned.contains(ribbonId)?0xff000000|(c&31)*255/31<<16|(c>>5&31)*255/31<<8|(c>>10&31)*255/31:0xff202728);}
   output.add(image);
  }return List.copyOf(output);
 }
}
