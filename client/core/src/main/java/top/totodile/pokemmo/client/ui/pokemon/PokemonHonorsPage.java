package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.ToggleButtonModel;
import de.matthiasmann.twl.renderer.Image;
import java.util.*;
import java.util.function.Supplier;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;

/** PC contest/ribbons/particle pages from PokemonSummaryWindow. */
final class PokemonHonorsPage extends Widget {
    static final int[][] RIBBONS={{16, 0, 8, 0, 80},{22, 0, 38, 42, 122},{23, 0, 39, 43, 123},{20, 2, 11, 51, 131},{27, 4, 12, 52, 132},{28, 0, 13, 53, 133},{29, 1, 14, 54, 134},{30, 0, 15, 55, 135},{31, 3, 16, 56, 136},{32, 0, 17, 57, 137},{33, 2, 18, 58, 138},{24, 1, 19, 59, 139},{25, 3, 20, 60, 140},{26, 0, 21, 61, 141},{34, 0, 24, 64, 144}};
    private final PokemonController controller;
    private final Supplier<Pokemon> selected;
    private final List<Widget> entries=new ArrayList<>();
    private final List<int[]> positions=new ArrayList<>();
    private final List<Button> particleButtons=new ArrayList<>();
    private int page,gridHeight;
    private final Widget particleGrid=new Widget(){
        @Override public int getPreferredInnerWidth(){return 235;}
        @Override public int getPreferredInnerHeight(){return gridHeight;}
        @Override protected void layout(){layoutEntries(true);}
    };
    private final ScrollPane scroll=new ScrollPane(particleGrid);

    PokemonHonorsPage(PokemonController controller,Supplier<Pokemon> selected){this.controller=controller;this.selected=selected;setTheme("");particleGrid.setTheme("");scroll.setTheme("/scrollpane");scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);}
    void refresh(int page){
        this.page=page;removeAllChildren();particleGrid.removeAllChildren();gridHeight=0;entries.clear();positions.clear();particleButtons.clear();var p=selected.get();if(p==null)return;
        if(page==6){
            int total=0;for(int i=0;i<6;i++){
                int value=i==5?total:p.honors().condition().get(i);if(i<5)total+=value;
                var title=new Label(i==5?controller.strings.get(1726):controller.strings.get(310418+i)+":");title.setTheme("/label-monster-area.label-monster-title");addEntry(title,0,i*42,230,25);
                var bar=new Bar(value/(i==5?640f:255f));bar.setTheme("/summary-contest-"+i);addEntry(bar,4,i*42+27,230,8);
            }
        }else if(page==7){
            int count=0,row=0;
            if(p.honors().ribbons().contains(21)){addRibbon(0,14,true,0,0,controller.strings.format(1844,controller.dex().regionName(p.origin().region())));count++;}
            for(int[] r:RIBBONS){addRibbon(r[1],r[2],p.honors().ribbons().contains(r[0]),count%6,row,controller.dex().ribbonText(r[3])+"\n"+controller.dex().ribbonText(r[4]));if(++count%6==0)row++;}
            if(count%6!=0)row++;
            for(int rank=0;rank<4;rank++,row++)for(int category=0;category<5;category++)addRibbon(category,rank,p.honors().contestRibbons().get(category)>=rank+1,category,row,controller.dex().ribbonText(category*4+2+rank)+"\n"+controller.dex().ribbonText(category*4+82+rank));
        }else{
            var title=new Label(controller.strings.get(1890));title.setTheme("/label-monster-area.label-monster-title");addEntry(title,0,0,235,25);
            add(scroll);scroll.setScrollPositionY(0);
            var owned=p.honors().particles().stream().distinct().sorted().toList();
            if(owned.isEmpty()){var empty=new Label(controller.strings.get(1891));empty.setTheme("/label");addEntry(empty,0,32,235,50);}
            int index=0;for(int particle:owned){addParticle(particle,index%6,index/6);index++;}
            if(owned.size()>1)addParticle(-3,index%6,index/6);
        }particleGrid.invalidateLayout();invalidateLayout();
    }
    private void addEntry(Widget widget,int x,int y,int width,int height){entries.add(widget);positions.add(new int[]{x,page==8&&y>=32?y-32:y,width,height});if(page==8&&y>=32){particleGrid.add(widget);gridHeight=Math.max(gridHeight,y-32+height);}else add(widget);}
    private void addRibbon(int palette,int sprite,boolean owned,int column,int row,String tooltip){
        var image=controller.images().ribbon(palette,sprite);if(!owned)image=image.createTintedVersion(new Color(0x7f000000));final Image drawn=image;
        var widget=new Widget(){@Override protected void paintWidget(GUI gui){drawn.draw(getAnimationState(),getX(),getY(),32,32);}@Override protected boolean handleEvent(Event event){return event.isMouseEvent()||super.handleEvent(event);}};
        widget.setTheme("");widget.setTooltipContent(tooltip);widget.setTooltipDelay(0);addEntry(widget,column*38,row*38,32,32);
    }
    private void addParticle(int particle,int column,int row){
        var image=particle<0?controller.images().atlas("icon_dice"):particle==0&&selected.get().secretShiny()?controller.images().atlas("secret_shiny_particle"):controller.images().atlas("particle_id",particle);
        var button=new Button(){@Override protected void paintWidget(GUI gui){if(image!=null)image.draw(getAnimationState(),getX(),getY(),32,32);}};
        var model=new ToggleButtonModel();button.setModel(model);model.setSelected(selected.get().honors().selectedParticle()==particle);button.setTheme("/summary-particle-button");button.setCanAcceptKeyboardFocus(false);
        button.addCallback(()->{var p=selected.get();if(p!=null&&!controller.busy())controller.selectParticle(p.id(),p.honors().selectedParticle()==particle?-1:particle);});
        button.setTooltipContent(particleTooltip(controller,particle,selected.get().secretShiny()));button.setTooltipDelay(0);
        particleButtons.add(button);addEntry(button,column*40,32+row*40,32,32);
    }
    static String particleTooltip(PokemonController controller,int particle,boolean secret){
        if(particle<0)return controller.strings.get(10997)+"\n\n"+controller.strings.get(10998);
        String title=controller.strings.getOrDefault(particle==0&&secret?11199:11000+particle,controller.strings.get(10800+particle));
        String detail=controller.strings.getOrDefault(particle==0&&secret?11399:11200+particle,"");
        String event=controller.strings.getOrDefault(920000+particle,"");
        return title+(event.isEmpty()?"":"\n\n"+event)+(detail.isEmpty()?"":"\n\n"+detail);
    }
    private void layoutEntries(boolean grid){for(int i=0;i<entries.size();i++){var widget=entries.get(i);if((widget.getParent()==particleGrid)!=grid)continue;var r=positions.get(i);widget.setPosition((grid?particleGrid.getX():getX())+r[0],(grid?particleGrid.getY():getY())+r[1]);widget.setSize(r[2],r[3]);}}
    @Override protected void layout(){layoutEntries(false);if(page==8){scroll.setPosition(getX(),getY()+32);scroll.setSize(244,234);}}
    private static final class Bar extends Widget {
        private final float value;private Image fill;
        Bar(float value){this.value=Math.min(1,value);}
        @Override protected void applyTheme(ThemeInfo theme){super.applyTheme(theme);fill=theme.getImage("progressImage");}
        @Override protected void paintWidget(GUI gui){if(fill!=null)fill.draw(getAnimationState(),getX(),getY(),Math.round(getWidth()*value),getHeight());}
    }
}
