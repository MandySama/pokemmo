package top.totodile.pokemmo.client.ui.character;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import top.totodile.pokemmo.server.game.GameService;

/** Shares the character editor's addon composition and saved outfit in the map scene. */
public final class OverworldCharacter implements AutoCloseable {
    private final CharacterAppearance appearance;
    private final CharacterPreview sprites;
    public OverworldCharacter(CharacterAppearance catalog,GameService.Appearance saved) {
        appearance=catalog.restore(saved);
        sprites=new CharacterPreview(appearance,null,false,saved::skin,saved::gender,()->0);
    }
    public void draw(SpriteBatch batch,float x,float feetY,int direction,float walkTime,boolean moving) {
        draw(batch,x,feetY,direction,walkTime,moving,0);
    }
    public void draw(SpriteBatch batch,float x,float feetY,int direction,float walkTime,boolean moving,int mode) {
        int base=direction==3?2:direction;
        int first=mode==2?18+base:mode==1&&moving?9+base*3:base;
        int left=mode==2?21+base*2:mode==1?first+1:3+base*2;
        int right=mode==2?left+1:mode==1?first+2:4+base*2;
        int[] frames={first,left,first,right};
        // KeyframeSplineInterpolator.pv0：步行、跑步、骑车三套帧，整数像素落点。
        int frame=moving?frames[(int)(walkTime/.16f)%4]:first;
        sprites.drawWorld(batch,Math.round(x),Math.round(feetY),frame,direction==3);
    }
    @Override public void close() { sprites.close(); appearance.dispose(); }
}
