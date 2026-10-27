package top.totodile.pokemmo.client.rom.nds.hgss;

import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.gba.GbaNpcSprites.Frame;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;
import top.totodile.pokemmo.client.rom.nds.model.NitroTextureDictionary;
import java.io.IOException;
import java.util.*;

/** HeartGoldSoulSilverRom.Z80/sf0/v5: GBA region 0 -> HGSS /a/0/8/1 texture frames. */
public final class HgssNpcSprites {
    private static final Map<Integer,Integer> KANTO=Map.ofEntries(
            Map.entry(60,38),
            Map.entry(14,70),
            Map.entry(15,71),
            Map.entry(16,114),
            Map.entry(17,115),
            Map.entry(21,122),
            Map.entry(22,6),
            Map.entry(18,116),
            Map.entry(23,146),
            Map.entry(31,128),
            Map.entry(19,121),
            Map.entry(24,7),
            Map.entry(20,117),
            Map.entry(27,131),
            Map.entry(30,127),
            Map.entry(28,29),
            Map.entry(32,19),
            Map.entry(47,9),
            Map.entry(48,120),
            Map.entry(29,14),
            Map.entry(59,30),
            Map.entry(33,129),
            Map.entry(35,20),
            Map.entry(39,142),
            Map.entry(40,143),
            Map.entry(41,123),
            Map.entry(42,126),
            Map.entry(52,137),
            Map.entry(58,85),
            Map.entry(50,174),
            Map.entry(43,139),
            Map.entry(44,140),
            Map.entry(45,12),
            Map.entry(46,118),
            Map.entry(54,49),
            Map.entry(55,26),
            Map.entry(61,138),
            Map.entry(62,145),
            Map.entry(63,183),
            Map.entry(57,144),
            Map.entry(90,190),
            Map.entry(37,2),
            Map.entry(36,140),
            Map.entry(38,1),
            Map.entry(56,132),
            Map.entry(53,180),
            Map.entry(91,58),
            Map.entry(64,134),
            Map.entry(67,56),
            Map.entry(92,95),
            Map.entry(71,161),
            Map.entry(25,122),
            Map.entry(26,186),
            Map.entry(78,129),
            Map.entry(79,157),
            Map.entry(34,19),
            Map.entry(68,133),
            Map.entry(69,57),
            Map.entry(70,57),
            Map.entry(65,135),
            Map.entry(66,136),
            Map.entry(96,93),
            Map.entry(97,92),
            Map.entry(49,175),
            Map.entry(89,3),
            Map.entry(75,158),
            Map.entry(80,168),
            Map.entry(81,165),
            Map.entry(82,163),
            Map.entry(83,166),
            Map.entry(84,156),
            Map.entry(87,226),
            Map.entry(86,169),
            Map.entry(85,164),
            Map.entry(73,182),
            Map.entry(76,192),
            Map.entry(74,159),
            Map.entry(72,170),
            Map.entry(95,94),
            Map.entry(51,162),
            Map.entry(109,195),
            Map.entry(107,229),
            Map.entry(110,319),
            Map.entry(118,437),
            Map.entry(119,414),
            Map.entry(121,353),
            Map.entry(122,328),
            Map.entry(123,331),
            Map.entry(124,332),
            Map.entry(125,351),
            Map.entry(126,385),
            Map.entry(127,399),
            Map.entry(128,378),
            Map.entry(129,379),
            Map.entry(130,365),
            Map.entry(131,339),
            Map.entry(132,383),
            Map.entry(133,320),
            Map.entry(135,229),
            Map.entry(136,444),
            Map.entry(137,445),
            Map.entry(138,443),
            Map.entry(148,719),
            Map.entry(149,718),
            Map.entry(150,717),
            Map.entry(139,449),
            Map.entry(140,450),
            Map.entry(141,575),
            Map.entry(143,574),
            Map.entry(142,576),
            Map.entry(144,212),
            Map.entry(145,211),
            Map.entry(146,582),
            Map.entry(111,403),
            Map.entry(134,366),
            Map.entry(147,439),
            Map.entry(112,361),
            Map.entry(114,316),
            Map.entry(117,412),
            Map.entry(120,69),
            Map.entry(115,338),
            Map.entry(116,314),
            Map.entry(113,334),
            Map.entry(88,193));
    private static final int[] FRAMES_16={11,0,2,12,14,8,10,1,3,11};
    private static final int[] FRAMES_17={12,0,2,13,15,9,11,1,3,12};
    private static final int[] FRAMES_8={2,0,4,2,3,0,1,4,5};
    private final NdsArchive models;
    private final Map<Integer,List<Frame>> cache=new HashMap<>();
    public HgssNpcSprites(RomInfo rom) throws IOException {
        models=new NdsArchive(RomReader.readFile(rom,"a/0/8/1"));
    }
    public static Integer model(int graphics) {return KANTO.get(graphics);}
    public Frame frame(int graphics,int direction,int phase) throws IOException {
        Integer model=KANTO.get(graphics);
        if(model==null) return null;
        var frames=cache.get(model);
        if(frames==null) {
            // DualTableRegistry.eT converts the reference one-based model number to NARC index.
            int archiveIndex=model-1;
            if(archiveIndex<0 || archiveIndex>=models.size()) return null;
            byte[] bytes=models.read(archiveIndex);
            // Reference Z80 only accepts BTX0 texture bundles for replacement sprites.
            if(bytes.length<4 || bytes[0]!='B'||bytes[1]!='T'||bytes[2]!='X'||bytes[3]!='0')return null;
            var dictionary=new NitroTextureDictionary(bytes);
            if(dictionary.paletteNames().isEmpty())return null;
            String palette=dictionary.paletteNames().iterator().next();
            var decoded=new ArrayList<Frame>();
            for(String texture:dictionary.names()) {
                var pixels=dictionary.pixels(texture,palette,false,false);
                try {
                    int[] rgba=new int[pixels.getWidth()*pixels.getHeight()];
                    for(int y=0;y<pixels.getHeight();y++)for(int x=0;x<pixels.getWidth();x++)
                        rgba[y*pixels.getWidth()+x]=pixels.getPixel(x,y);
                    decoded.add(new Frame(pixels.getWidth(),pixels.getHeight(),rgba));
                } finally {pixels.dispose();}
            }
            frames=List.copyOf(decoded);cache.put(model,frames);
        }
        if(frames.isEmpty())return null;
        int index=direction==3?2:direction;
        if(phase==1)index=index*2+3;else if(phase==3)index=index*2+4;
        int[] mapping=frames.size()==16?FRAMES_16:frames.size()==17?FRAMES_17:frames.size()==8?FRAMES_8:null;
        if(mapping!=null) {if(index>=mapping.length)return null;index=mapping[index];}
        return frames.get(index<frames.size()?index:0);
    }
}
