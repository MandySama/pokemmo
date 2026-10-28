package top.totodile.pokemmo.client.world;

import com.badlogic.gdx.utils.*;
import java.io.*;
import java.util.*;
import top.totodile.pokemmo.client.rom.gba.FireRedMaps.*;

/** Native rotating_gate.c collision tables and temporary orientations. */
public final class HoennRotatingGates {
    static final JsonValue DATA;
    static {try(var in=HoennRotatingGates.class.getResourceAsStream("/story/hoenn/mechanisms.json")){
        if(in==null)throw new FileNotFoundException("Hoenn gate definitions");DATA=new JsonReader().parse(in);
    }catch(IOException e){throw new UncheckedIOException(e);}}
    public record Gate(int x,int y,int shape,int orientation,float angle){}
    private Id current;
    private JsonValue definitions=new JsonValue(JsonValue.ValueType.array);
    private int[] orientations=new int[0],from=new int[0],turn=new int[0];
    private long[] started=new long[0];
    public void enter(Id id){
        if(id.equals(current))return;current=id;definitions=new JsonValue(JsonValue.ValueType.array);
        for(var pv:DATA.get("puzzles")){var p=pv;if(p.get("bank").asInt()==id.bank()&&p.get("id").asInt()==id.map())definitions=p.get("gates");}
        int n=definitions.size;orientations=new int[n];from=new int[n];turn=new int[n];started=new long[n];
        for(int i=0;i<n;i++)orientations[i]=definitions.get(i).get("orientation").asInt();
    }
    public String state(){return Arrays.toString(orientations);}
    public HoennRotatingGates copy(){var result=new HoennRotatingGates();result.current=current;result.definitions=definitions;result.orientations=orientations.clone();result.from=from.clone();result.turn=turn.clone();result.started=started.clone();return result;}
    public List<Gate> gates(Id id){enter(id);var gates=new ArrayList<Gate>();
        for(int i=0;i<definitions.size;i++){var g=definitions.get(i);float progress=Math.min(1,(System.nanoTime()-started[i])/266_666_667f);float angle=started[i]==0?orientations[i]*90:from[i]*90+turn[i]*90*progress;gates.add(new Gate(g.get("x").asInt(),g.get("y").asInt(),g.get("shape").asInt(),orientations[i],angle));}return gates;
    }
    public boolean step(MapData map,int direction,int x,int y){
        enter(map.id());
        for(int i=0;i<definitions.size;i++){
            var g=definitions.get(i);int gx=g.get("x").asInt(),gy=g.get("y").asInt();
            if(x<gx-2||x>gx+1||y<gy-2||y>gy+1)continue;
            int info=DATA.get("rotation").get(Integer.toString(direction)).get((y-gy+2)*4+x-gx+2).asInt();if(info<0)continue;
            int rotation=info>>4,arm=info&15,relative=((arm/2-orientations[i]+4)%4)*2+arm%2;
            var layout=DATA.get("arms").get(g.get("shape").asInt());if(layout.get(relative).asInt()==0)continue;
            var sweep=DATA.get("sweep").get(Integer.toString(rotation));
            for(int a=0;a<4;a++)for(int length=0;length<2;length++)if(layout.get(a*2+length).asInt()!=0){
                var point=sweep.get(((orientations[i]+a)%4)*2+length);int tx=gx+point.get(0).asInt(),ty=gy+point.get(1).asInt();
                if(!map.contains(tx,ty)||(map.block(tx,ty)>>10&3)==1)return false;
            }
            from[i]=orientations[i];turn[i]=rotation==1?-1:1;started[i]=System.nanoTime();orientations[i]=Math.floorMod(orientations[i]+turn[i],4);return true;
        }
        return true;
    }
}
