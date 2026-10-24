package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokedex;

/** Reference TrainerBadgeTagLabel: 46px desktop / 96px mobile, icon above the National number. */
final class PokedexGrid extends Widget {
    private final BwPokedex dex;
    private final PokedexImages images;
    private final boolean mobile;
    private final IntConsumer selected;
    private List<BwPokedex.Species> entries=List.of();
    private int columns=12;
    private int region=-1;
    private boolean unlocked;
    PokedexGrid(BwPokedex dex,PokedexImages images,boolean mobile,IntConsumer selected) {
        this.dex=dex;this.images=images;this.mobile=mobile;this.selected=selected;setTheme("");
        filter("");
    }
    void filter(String query) {
        PokedexFilter filter=new PokedexFilter();filter.query=query;filter(filter);
    }
    void filter(PokedexFilter filter) {
        region=filter.region;
        unlocked=filter.unlocked;
        entries=dex.search(filter.query).stream().filter(s->filter.matches(dex,s))
                .sorted(java.util.Comparator.comparingInt(s->dex.number(s.id(),region))).toList();removeAllChildren();
        for(var species:entries) {
            int id=species.id();
            Button card=new Button(String.format(Locale.ROOT,"%03d",dex.number(id,region))) {
                @Override protected void paintWidget(GUI gui) {
                    int size=mobile?72:36;
                    // TrainerBadgeTagLabel offsets the 36px icon by (5, -6); the number stays bottom aligned.
                    images.icon(id,unlocked).draw(getAnimationState(),getX()+(getWidth()-size)/2,getY()+(mobile?5:-6),size,size);
                    super.paintWidget(gui);
                }
            };
            card.setTheme(unlocked?"/monsterdex-button-caught-ot":"/monsterdex-button");card.setCanAcceptKeyboardFocus(false);
            card.setTooltipContent(unlocked?dex.name(id):"???");card.setTooltipDelay(200); // TrainerBadgeTagLabel.Bb(200).
            card.addCallback(()->selected.accept(id));add(card);
        }
        invalidateLayout();
    }
    int count(){return entries.size();}
    private int cell(){return mobile?96:46;}
    @Override public int getPreferredInnerWidth(){return columns*(cell()+5)-5;}
    @Override public int getPreferredInnerHeight(){return Math.max(0,((entries.size()+columns-1)/columns)*(cell()+5)-5);}
    @Override protected void sizeChanged(){super.sizeChanged();int next=Math.max(1,Math.min(12,(getInnerWidth()+5)/(cell()+5)));if(next!=columns){columns=next;invalidateLayout();}}
    @Override protected void layout() {
        int left=getInnerX()+(mobile?Math.max(0,(getInnerWidth()-(columns*(cell()+5)-5))/2):0);
        for(int i=0;i<getNumChildren();i++) {
            var card=getChild(i);card.setSize(cell(),cell());
            card.setPosition(left+(i%columns)*(cell()+5),getInnerY()+(i/columns)*(cell()+5));
        }
    }
}
