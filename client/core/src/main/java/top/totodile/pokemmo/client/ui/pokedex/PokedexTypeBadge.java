package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;

/** Uses the reference atlas' language-specific type badges. */
final class PokedexTypeBadge extends Widget {
    private final PokedexImages images;
    private final int[] types;
    private final boolean mobile;
    PokedexTypeBadge(PokedexImages images,boolean mobile,int...types){this.images=images;this.mobile=mobile;this.types=types;setTheme("");}
    @Override public int getPreferredInnerWidth(){return types.length*(mobile?104:54);}
    @Override public int getPreferredInnerHeight(){return mobile?28:14;}
    @Override protected void paintWidget(GUI gui){
        int width=types.length*(mobile?104:54)-(mobile?6:5);
        int x=getInnerX()+Math.max(0,(getInnerWidth()-width)/2),y=getInnerY()+(getInnerHeight()-(mobile?28:14))/2;
        for(int type:types){Image image=images.type(type);if(image!=null)image.draw(getAnimationState(),x,y,mobile?98:49,mobile?28:14);x+=mobile?104:54;}
    }
}
