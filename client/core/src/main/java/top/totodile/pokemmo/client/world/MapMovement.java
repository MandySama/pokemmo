package top.totodile.pokemmo.client.world;

import top.totodile.pokemmo.client.rom.gba.FireRedMaps.*;
import top.totodile.pokemmo.server.game.GameService.Position;

/** Tile movement and ROM map transitions, independent of rendering/input. */
public final class MapMovement {
    private final WorldMaps maps;
    private MapData map;
    private int x,y,direction,elevation;
    private boolean noClip;
    private boolean continuousStep;
    private int stepFromX,stepFromY;
    private Runnable stateChanged=() -> {};
    private java.util.function.Consumer<MapData> prepareTransition=target -> {};
    public MapMovement(WorldMaps maps,Position position) {
        this.maps=maps; map=maps.map(new Id(position.bank(),position.map()));
        if(!map.contains(position.x(),position.y()))
            throw new IllegalArgumentException("Saved position is outside map");
        x=position.x(); y=position.y(); direction=position.direction(); elevation=map.block(x,y)>>>12;
    }
    public MapData map() { return map; }
    public int elevation() { return elevation; }
    public boolean noClip() { return noClip; }
    /** Last successful step's source footpoint, rebased into the destination map. */
    public int stepFromX() {return stepFromX;}
    public int stepFromY() {return stepFromY;}
    public boolean continuousStep() {return continuousStep;}
    public void onStateChanged(Runnable callback) { stateChanged=java.util.Objects.requireNonNull(callback); }
    public void beforeTransition(java.util.function.Consumer<MapData> callback) {prepareTransition=java.util.Objects.requireNonNull(callback);}
    public boolean canStand() {
        int block=map.block(x,y), behavior=maps.tiles(map).behavior(block&1023);
        return (block&0xc00)==0 && block>>>12!=15 && !maps.isWater(behavior);
    }
    public void setNoClip(boolean enabled) {
        if(noClip!=enabled) { noClip=enabled;stateChanged.run(); }
    }
    public Position position() { return new Position(map.id().bank(),map.id().map(),x,y,direction); }
    public boolean face(int dx,int dy) {
        if(Math.abs(dx)+Math.abs(dy)!=1)return false;
        int next=dy>0?0:dy<0?1:dx<0?2:3;
        if(next==direction)return false;
        direction=next;return true;
    }
    public boolean step(int dx,int dy) {
        if(Math.abs(dx)+Math.abs(dy)!=1) return false;
        face(dx,dy);
        MapData target=map;
        int nx=x+dx,ny=y+dy;
        Position crossing=maps.transition(map.id(),nx,ny,direction);
        if(crossing!=null) {
            target=maps.map(new Id(crossing.bank(),crossing.map()));nx=crossing.x();ny=crossing.y();
        } else if(!map.contains(nx,ny)) {
            int side=dy>0?1:dy<0?2:dx<0?3:4;
            boolean connected=false;
            for(Connection connection:map.connections()) if(connection.direction()==side) {
                MapData candidate=maps.map(connection.target());
                int cx=nx,cy=ny;
                if(side<=2) { cx-=connection.offset(); cy=side==1?0:candidate.height()-1; }
                else { cy-=connection.offset(); cx=side==4?0:candidate.width()-1; }
                if(candidate.contains(cx,cy)) { target=candidate; nx=cx; ny=cy; connected=true; break; }
            }
            if(!connected) return false;
        }
        int block=target.block(nx,ny), nextElevation=block>>>12;
        int behavior=maps.tiles(target).behavior(block&1023);
        Warp warp=target.warp(nx,ny);
        // GBA animated doors are impassable blocks; approaching their warp from below enters the building.
        boolean doorEntry=maps.isDoor(behavior) && warp!=null && dy<0;
        if(!noClip && ((block&0xc00)!=0 && !doorEntry || nextElevation==15 || elevation!=0 && nextElevation!=0 && elevation!=nextElevation)) return false;
        // Water requires Surf; directional ledges and scripted movement are handled separately later.
        if(!noClip && maps.isWater(behavior)) return false;
        int sourceBehavior=maps.tiles(map).behavior(map.block(x,y)&1023);
        if(!noClip && (maps.blocked(sourceBehavior,dx,dy) || maps.blocked(behavior,-dx,-dy))) return false;
        boolean warped=false;
        if(warp!=null && warp.targetWarp()!=255 && maps.ids().contains(warp.target())) {
            MapData destination=maps.map(warp.target());
            if(warp.targetWarp()>=destination.warps().size()) throw new IllegalArgumentException("Invalid destination warp");
            Warp arrival=destination.warps().get(warp.targetWarp());
            target=destination; nx=arrival.x(); ny=arrival.y();
            if(!target.contains(nx,ny)) throw new IllegalArgumentException("Warp outside destination map");
            // The original door sequence walks the actor one tile out of an exterior doorway.
            if(maps.isDoor(maps.tiles(target).behavior(target.block(nx,ny)&1023)) && target.contains(nx,ny+1)
                    && (target.block(nx,ny+1)&0xc00)==0) { ny++; direction=0; }
            nextElevation=target.block(nx,ny)>>>12;
            warped=true;
        }
        // Decode before committing a transition, so failed ROM resources leave the player in the old map.
        maps.tiles(target);
        if(!target.id().equals(map.id()))prepareTransition.accept(target);
        continuousStep=!warped;
        stepFromX=warped?nx:nx-dx;stepFromY=warped?ny:ny-dy;
        map=target; x=nx; y=ny;
        if(nextElevation!=0) elevation=nextElevation;
        return true;
    }
}
