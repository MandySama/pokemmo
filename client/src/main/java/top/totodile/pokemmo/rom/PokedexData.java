package top.totodile.pokemmo.rom;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** 黑白 ROM 基础图鉴资料，不套用尚未接通的线上平衡或野生分布规则。 */
public final class PokedexData {
    public record Move(int level, int id, int type, int category, int power, int accuracy, int pp) { }
    public record Evolution(int from, int target, int method, int parameter) { }
    public record Species(int id, List<Integer> stats, int type1, int type2, int gender, int egg1, int egg2,
                          int ability1, int ability2, int hiddenAbility, int growth, int friendship, int height, int weight, List<Integer> effort, List<Integer> heldItems, List<Move> moves, List<Evolution> evolutions) { }
    private final Species[] species = new Species[top.totodile.pokemmo.protocol.PokemonCatalog.MAX_SPECIES + 1];
    private final NdsArchive battle;
    public PokedexData(Path rom) throws IOException {
        var personal = new NdsArchive(rom, "/a/0/1/6");
        var learnsets = new NdsArchive(rom, "/a/0/1/8");
        var evolutions = new NdsArchive(rom, "/a/0/1/9");
        var moves = new NdsArchive(rom, "/a/0/2/1");
        battle = new NdsArchive(rom, "/a/0/0/4");
        try {
            for (int id = 1; id <= 649; id++) {
                ByteBuffer p = personal.file(id), l = learnsets.file(id), e = evolutions.file(id);
                var stats = List.of(u(p,0),u(p,1),u(p,2),u(p,4),u(p,5),u(p,3));
                List<Move> learned = new ArrayList<>();
                for (int i = 0; i + 3 < l.limit(); i += 4) {
                    int move = s(l,i), level = s(l,i+2);
                    if (move == 65535 || level == 65535) break;
                    if (move < 1 || move > 559 || level > 100) throw new IOException("Invalid learned move");
                    ByteBuffer m = moves.file(move);
                    learned.add(new Move(level,move,u(m,0),u(m,2),u(m,3),u(m,4),u(m,5)));
                }
                List<Evolution> next = new ArrayList<>();
                for (int i = 0; i + 5 < e.limit(); i += 6) {
                    int method = s(e,i), parameter = s(e,i+2), target = s(e,i+4);
                    if (target > 0 && target <= 649) next.add(new Evolution(id,target,method,parameter));
                }
                species[id] = new Species(id,stats,u(p,6),u(p,7),u(p,18),u(p,22),u(p,23),u(p,24),u(p,25),u(p,26),u(p,21),u(p,20),s(p,36),s(p,38),List.of(s(p,10)&3,(s(p,10)>>2)&3,(s(p,10)>>4)&3,(s(p,10)>>8)&3,(s(p,10)>>10)&3,(s(p,10)>>6)&3),List.of(s(p,12),s(p,14),s(p,16)),List.copyOf(learned),List.copyOf(next));
            }
        } catch (IndexOutOfBoundsException exception) { throw new IOException("Invalid Pokedex ROM records", exception); }
        try (var in = new java.io.DataInputStream(new java.util.zip.GZIPInputStream(java.nio.file.Files.newInputStream(top.totodile.pokemmo.ClientFiles.data("catalog/gen9.dat.gz"))))) {
            if (in.readInt() != species.length - 650) throw new IOException("Invalid extended details");
            for (int id = 650; id < species.length; id++) {
                var rule = top.totodile.pokemmo.protocol.PokemonCatalog.get(id);
                int height = in.readUnsignedShort(), weight = in.readUnsignedShort(), egg1 = in.readUnsignedShort(), egg2 = in.readUnsignedShort();
                List<Integer> effort = new ArrayList<>(); for (int i=0;i<6;i++) effort.add(in.readUnsignedByte());
                List<Evolution> next = new ArrayList<>(); int n=in.readUnsignedShort();
                for (int i=0;i<n;i++) next.add(new Evolution(id,in.readUnsignedShort(),in.readUnsignedShort(),in.readUnsignedShort()));
                var learned = rule.moves().stream().map(m -> new Move(m.level(),m.id(),m.type(),m.category(),m.power(),m.accuracy(),m.pp())).toList();
                species[id] = new Species(id,rule.stats(),rule.type1(),rule.type2(),rule.gender(),egg1,egg2,rule.ability1(),rule.ability2(),rule.hiddenAbility(),rule.growth(),rule.friendship(),height,weight,List.copyOf(effort),List.of(0,0,0),learned,List.copyOf(next));
            }
            int cross = in.readUnsignedShort();
            for (int i=0;i<cross;i++) {
                int from=in.readUnsignedShort(), target=in.readUnsignedShort(), method=in.readUnsignedShort(), parameter=in.readUnsignedShort();
                var old=species[from]; var next=new ArrayList<>(old.evolutions());
                if (next.stream().noneMatch(e -> e.target()==target)) next.add(new Evolution(from,target,method,parameter));
                species[from]=new Species(old.id(),old.stats(),old.type1(),old.type2(),old.gender(),old.egg1(),old.egg2(),old.ability1(),old.ability2(),old.hiddenAbility(),old.growth(),old.friendship(),old.height(),old.weight(),old.effort(),old.heldItems(),old.moves(),List.copyOf(next));
            }
        }

    }
    public Species species(int id) { if (id < 1 || id >= species.length) throw new IllegalArgumentException("Invalid species"); return species[id]; }
    public List<Evolution> family(int id) {
        java.util.Set<Integer> family = new java.util.HashSet<>(); family.add(id);
        boolean changed;
        do { changed = false; for (int i=1;i<species.length;i++) for (Evolution evolution:species[i].evolutions())
            if (family.contains(evolution.from()) || family.contains(evolution.target())) {
                changed |= family.add(evolution.from()); changed |= family.add(evolution.target());
            }
        } while (changed);
        List<Evolution> result = new ArrayList<>();
        for (int i=1;i<species.length;i++) for (Evolution e:species[i].evolutions()) if(family.contains(e.from())) result.add(e);
        return List.copyOf(result);
    }
    public BufferedImage sprite(int id, boolean back) throws IOException { return sprite(id,back,false); }

    public BufferedImage sprite(int id,boolean back,boolean shiny) throws IOException {
        if (id > 649) {
            var image=PokemonAnimation.loadBattle(null,id,shiny,back).images().getFirst();
            var result=new BufferedImage(96,96,BufferedImage.TYPE_INT_ARGB);var g=result.createGraphics();
            double scale=Math.min(1,Math.min(92.0/image.getWidth(),92.0/image.getHeight()));
            int w=(int)(image.getWidth()*scale),h=(int)(image.getHeight()*scale);
            g.drawImage(image,(96-w)/2,(96-h)/2,w,h,null);g.dispose();return result;
        }
        ByteBuffer graphics = NdsCompression.decode(battle.file(id * 20 + (back ? 9 : 0)));
        ByteBuffer cells = NdsCompression.decode(battle.file(4));
        ByteBuffer palette = battle.file(id * 20 + (shiny?19:18));
        return renderCell(graphics,cells,palette,0);
    }

    static BufferedImage renderCell(ByteBuffer graphics,ByteBuffer cells,ByteBuffer palette,int cell) throws IOException {
        return renderCell(graphics, cells, palette, cell, 96);
    }

    static BufferedImage renderCell(ByteBuffer graphics,ByteBuffer cells,ByteBuffer palette,int cell,int canvas) throws IOException {
        try {
            if(graphics.getInt(0)!=0x4e434752 || cells.getInt(0)!=0x4e434552 || palette.getInt(0)!=0x4e434c52) throw new IOException("Invalid Pokemon sprite files");
            int cellCount=s(cells,24), stride=s(cells,26)==1?16:8, first=24+cells.getInt(28);
            int cellOffset=first+cell*stride;
            int count=s(cells,cellOffset), oam=first+cellCount*stride+cells.getInt(cellOffset+4);
            int shift=cells.getInt(32)&3, paletteStart=24+palette.getInt(36);
            int pixels=24+graphics.getInt(44), depth=graphics.getInt(28);
            if (depth != 3) throw new IOException("Unsupported Pokemon sprite depth");
            BufferedImage result = new BufferedImage(canvas,canvas,BufferedImage.TYPE_INT_ARGB);
            List<int[]> objects = new ArrayList<>();
            int[][] widths={{8,16,32,64},{16,32,32,64},{8,8,16,32}}, heights={{8,16,32,64},{8,8,16,32},{16,32,32,64}};
            for(int i=0;i<count;i++) {
                int off=oam+i*6,a=s(cells,off),b=s(cells,off+2),c=s(cells,off+4),shape=a>>14,size=b>>14;
                if(shape==3 || (a&0x300)==0x200)continue;
                int x=b&511;if(x>=256)x-=512;int y=(byte)a;
                boolean affine=(a&256)!=0;
                if(affine&&(a&512)!=0){x+=widths[shape][size]/2;y+=heights[shape][size]/2;}
                objects.add(new int[]{x,y,widths[shape][size],heights[shape][size],c&1023,(c>>12)&15,!affine&&(b&4096)!=0?1:0,!affine&&(b&8192)!=0?1:0,c>>10&3,i});
            }
            objects.sort((a,b)->a[8]==b[8]?Integer.compare(b[9],a[9]):Integer.compare(b[8],a[8]));
            for(int[] obj:objects)for(int y=0;y<obj[3];y++)for(int x=0;x<obj[2];x++) {
                int tx=obj[6]==0?x:obj[2]-1-x,ty=obj[7]==0?y:obj[3]-1-y;
                boolean linear=(graphics.getInt(36)&255)==1;
                int columns=s(graphics,26);
                int pixel=linear?((obj[4]/columns*8+ty)*columns*8+obj[4]%columns*8+tx):(ty/8*(obj[2]/8)+tx/8)*64+(ty%8)*8+tx%8;
                int packed=u(graphics,pixels+(linear?0:(obj[4]<<shift)*32)+pixel/2),index=packed>>((pixel&1)*4)&15;
                if(index==0)continue;
                int dx=canvas/2+obj[0]+x,dy=canvas/2+obj[1]+y;if(dx<0||dy<0||dx>=canvas||dy>=canvas)continue;
                int color=s(palette,paletteStart+obj[5]*32+index*2);
                result.setRGB(dx,dy,0xff000000|(color&31)*255/31<<16|(color>>5&31)*255/31<<8|(color>>10&31)*255/31);
            }
            return result;
        } catch(IndexOutOfBoundsException exception) { throw new IOException("Invalid Pokemon sprite",exception); }
    }
    private static int u(ByteBuffer data,int offset){return data.get(offset)&255;}
    private static int s(ByteBuffer data,int offset){return data.getShort(offset)&65535;}
}