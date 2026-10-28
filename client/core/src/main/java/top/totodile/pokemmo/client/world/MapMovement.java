package top.totodile.pokemmo.client.world;

import top.totodile.pokemmo.client.rom.gba.FireRedMaps.*;
import top.totodile.pokemmo.server.game.GameService.Position;

/** Tile movement and ROM map transitions, independent of rendering/input. */
public final class MapMovement {
    private final WorldMaps maps;
    private MapData map;
    private int x, y, direction, elevation;
    private boolean noClip;
    private boolean surfAllowed=true;
    public void surfAllowed(boolean value){surfAllowed=value;}
    private HoennRotatingGates rotatingGates;
    HoennRotatingGates rotatingGates(){return rotatingGates;}
    private int spinningDirection=-1;
    public int forcedDirection(){
        int behavior=maps.tiles(map).behavior(map.block(x,y)&1023);
        if(behavior>=0x54&&behavior<=0x57)spinningDirection=maps.forcedDirection(behavior);
        if(behavior==0x58)spinningDirection=-1;
        int forced=maps.forcedDirection(behavior);return forced>=0?forced:spinningDirection;
    }
    private boolean continuousStep, jumping;
    public boolean jumping(){return jumping;}
    private int stepFromX, stepFromY;
    private Runnable stateChanged = () -> {};
    private java.util.function.Consumer<MapData> prepareTransition = target -> {};
    private java.util.function.BiPredicate<Integer, Integer> enterWarp = (dx, dy) -> true;
    @FunctionalInterface
    public interface Occupancy {
        boolean occupied(Id map, int x, int y, int elevation);
    }
    private Occupancy occupancy = (id, tx, ty, layer) -> false;

    public void occupancy(Occupancy value) {
        occupancy = java.util.Objects.requireNonNull(value);
    }

    public MapMovement(WorldMaps maps, Position position) {
        this.maps = maps;
        map = maps.map(new Id(position.bank(), position.map()));
        if (!map.contains(position.x(), position.y()))
            throw new IllegalArgumentException("Saved position is outside map");
        x = position.x();
        y = position.y();
        direction = position.direction();
        elevation = map.block(x, y) >>> 12;
        if(maps instanceof top.totodile.pokemmo.client.rom.gba.FireRedMaps gba&&gba.rom().type()==top.totodile.pokemmo.client.rom.RomType.EMERALD){rotatingGates=new HoennRotatingGates();rotatingGates.enter(map.id());}
    }

    public MapData map() {
        return map;
    }

    void storyPosition(Position position) {
        MapData destination=maps.map(new Id(position.bank(),position.map()));
        if(!destination.contains(position.x(),position.y()))throw new IllegalArgumentException("Story target outside map");
        jumping=false;spinningDirection=-1;prepareTransition.accept(destination);map=destination;x=position.x();y=position.y();direction=position.direction();
        elevation=map.block(x,y)>>>12;stepFromX=x;stepFromY=y;continuousStep=false;
    }

    public int elevation() {
        return elevation;
    }

    public boolean water() {
        return maps.isWater(maps.tiles(map).behavior(map.block(x, y) & 1023));
    }

    public boolean noClip() {
        return noClip;
    }

    /** Last successful step's source footpoint, rebased into the destination map. */
    public int stepFromX() {
        return stepFromX;
    }

    public int stepFromY() {
        return stepFromY;
    }

    public boolean continuousStep() {
        return continuousStep;
    }

    public void onStateChanged(Runnable callback) {
        stateChanged = java.util.Objects.requireNonNull(callback);
    }

    public void beforeTransition(java.util.function.Consumer<MapData> callback) {
        prepareTransition = java.util.Objects.requireNonNull(callback);
    }

    public void beforeWarp(java.util.function.BiPredicate<Integer, Integer> callback) {
        enterWarp = java.util.Objects.requireNonNull(callback);
    }

    public boolean canStand() {
        int block = map.block(x, y), behavior = maps.tiles(map).behavior(block & 1023);
        return (block & 0xc00) == 0 && block >>> 12 != 15 && !maps.isWater(behavior)
                && !occupancy.occupied(map.id(), x, y, elevation);
    }

    public void setNoClip(boolean enabled) {
        if (noClip != enabled) {
            noClip = enabled;
            stateChanged.run();
        }
    }

    public Position position() {
        return new Position(map.id().bank(), map.id().map(), x, y, direction);
    }

    public boolean face(int dx, int dy) {
        if (Math.abs(dx) + Math.abs(dy) != 1) return false;
        int next = dy > 0 ? 0 : dy < 0 ? 1 : dx < 0 ? 2 : 3;
        if (next == direction) return false;
        direction = next;
        return true;
    }

    public boolean step(int dx, int dy) {
        if (Math.abs(dx) + Math.abs(dy) != 1) return false;
        face(dx, dy);
        MapData target = map;
        int nx = x + dx, ny = y + dy;
        int sourceBehavior = maps.tiles(map).behavior(map.block(x, y) & 1023);
        Warp sourceWarp = map.warp(x, y);
        boolean standingWarp = sourceWarp != null
                && sourceWarp.targetWarp() != 255 && maps.ids().contains(sourceWarp.target())
                && maps.standingWarpDirection(sourceBehavior) == direction;
        // Stairs/arrow exits trigger from the tile underfoot, before testing the wall or
        // out-of-bounds tile beyond it (reference MovePacket.getStandingWarp).
        if (standingWarp) { nx = x; ny = y; }
        Position crossing = maps.transition(map.id(), nx, ny, direction);
        if (crossing != null) {
            target = maps.map(new Id(crossing.bank(), crossing.map()));
            nx = crossing.x();
            ny = crossing.y();
        } else if (!map.contains(nx, ny)) {
            int side = dy > 0 ? 1 : dy < 0 ? 2 : dx < 0 ? 3 : 4;
            boolean connected = false;
            for (Connection connection : map.connections())
                if (connection.direction() == side) {
                    MapData candidate = maps.map(connection.target());
                    int cx = nx, cy = ny;
                    if (side <= 2) {
                        cx -= connection.offset();
                        cy = side == 1 ? 0 : candidate.height() - 1;
                    } else {
                        cy -= connection.offset();
                        cx = side == 4 ? 0 : candidate.width() - 1;
                    }
                    if (candidate.contains(cx, cy)) {
                        target = candidate;
                        nx = cx;
                        ny = cy;
                        connected = true;
                        break;
                    }
                }
            if (!connected) return false;
        }
        int block = target.block(nx, ny), nextElevation = block >>> 12;
        int behavior = maps.tiles(target).behavior(block & 1023);
        int ledge=maps.ledgeDirection(behavior);
        if(!noClip&&ledge>=0){
            int lx=nx+dx,ly=ny+dy;
            if(ledge!=direction||target!=map||!map.contains(lx,ly)||map.warp(lx,ly)!=null||maps.blocked(sourceBehavior,dx,dy))return false;
            int landing=map.block(lx,ly),terrain=maps.tiles(map).behavior(landing&1023),height=landing>>>12;
            if((landing&0xc00)!=0||height==15||maps.isWater(terrain)||maps.ledgeDirection(terrain)>=0||maps.blocked(terrain,-dx,-dy)
                    ||occupancy.occupied(map.id(),nx,ny,elevation)||occupancy.occupied(map.id(),lx,ly,height))return false;
            stepFromX=x;stepFromY=y;x=lx;y=ly;if(height!=0)elevation=height;
            continuousStep=true;jumping=true;return true;
        }
        Warp warp = target.warp(nx, ny);
        if (!maps.allowsWarp(behavior) || !standingWarp && (maps.standingWarpDirection(behavior) >= 0
                || maps.isDoor(behavior) && dy >= 0)) warp = null;
        // GBA animated doors are impassable blocks; approaching their warp from below enters the
        // building.
        boolean doorEntry = maps.isDoor(behavior) && warp != null && dy < 0;
        boolean water = maps.isWater(behavior), shore = water || water();
        if(water&&!water()&&!surfAllowed)return false;
        // Automatic Surf is user-authorized; retain wall and elevation checks on land.
        if (!noClip
                && ((block & 0xc00) != 0 && !doorEntry && !water
                        || nextElevation == 15
                        || !shore
                                && (map.block(x, y) >>> 12) != 0
                                && elevation != 0
                                && nextElevation != 0
                                && elevation != nextElevation)) return false;
        if (!noClip && occupancy.occupied(target.id(), nx, ny,
                nextElevation == 0 ? elevation : nextElevation)) return false;
        if (!standingWarp && !noClip && (maps.blocked(sourceBehavior, dx, dy) || maps.blocked(behavior, -dx, -dy)))
            return false;
        if(!noClip&&rotatingGates!=null&&!rotatingGates.step(target,direction,nx,ny))return false;
        boolean warped = false;
        int arrivalDirection = direction;
        if (warp != null && warp.targetWarp() != 255 && maps.ids().contains(warp.target())) {
            MapData destination = maps.map(warp.target());
            if (warp.targetWarp() >= destination.warps().size())
                throw new IllegalArgumentException("Invalid destination warp");
            Warp arrival = destination.warps().get(warp.targetWarp());
            target = destination;
            nx = arrival.x();
            ny = arrival.y();
            if (!target.contains(nx, ny))
                throw new IllegalArgumentException("Warp outside destination map");
            // The original door sequence walks the actor one tile out of an exterior doorway.
            if (maps.isDoor(maps.tiles(target).behavior(target.block(nx, ny) & 1023))
                    && target.contains(nx, ny + 1)
                    && (target.block(nx, ny + 1) & 0xc00) == 0) {
                ny++;
                arrivalDirection = 0;
            }
            nextElevation = target.block(nx, ny) >>> 12;
            warped = true;
        }
        if (warped && !noClip && occupancy.occupied(target.id(), nx, ny, nextElevation)) return false;
        // Decode before committing a transition, so failed ROM resources leave the player in the
        // old map.
        maps.tiles(target);
        if(!surfAllowed&&!water()&&maps.isWater(maps.tiles(target).behavior(target.block(nx,ny)&1023)))return false;
        if (warped && !enterWarp.test(dx, dy)) return false;
        if (!target.id().equals(map.id())) prepareTransition.accept(target);
        if(warped)spinningDirection=-1;
        jumping=false;continuousStep = !warped;
        stepFromX = warped ? nx : nx - dx;
        stepFromY = warped ? ny : ny - dy;
        map = target;
        x = nx;
        y = ny;
        direction = arrivalDirection;
        if (nextElevation != 0) elevation = nextElevation;
        return true;
    }
}
