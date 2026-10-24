package top.totodile.pokemmo.client.world;

/** GdxFollowerParticleRenderer.Rd / Modern_Util_yz_2: four frames, 332ms each, including idle. */
final class FollowerAnimation {
    static int frame(int[] sequence,long milliseconds){
        int phase=(int)Math.floorMod(Math.floorDiv(milliseconds,332),4);
        if(phase>=sequence.length)phase-=2;
        return sequence[phase];
    }
    static boolean bounce(long milliseconds){return Math.floorMod(Math.floorDiv(milliseconds,166),2)==0;}
    static int frame(int[] sequence,long milliseconds,int count){
        int index=frame(sequence,milliseconds);
        // GdxFollowerParticleRenderer.Me: six-frame species reuse the side pair.
        return index>=count?index-2:index;
    }
    static boolean mirror(int direction,int count){return count<7&&direction==3;}
    private FollowerAnimation(){}
}
