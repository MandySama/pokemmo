package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;
import java.util.*;

/** Compact dex tables: fixed column proportions, wrapped values and themed header cells. */
final class PokedexRows extends Widget {
    private final List<Widget[]> rows=new ArrayList<>();
    private final int rowHeight;
    private final int[] weights;
    private final List<Widget> backgrounds=new ArrayList<>();
    private final Set<Integer> centered=new HashSet<>();
    private final Map<Widget[],Integer> lastSpans=new IdentityHashMap<>();
    private boolean table;
    private String rowTheme="/pokedex-table-row";
    private int preferred,headerHeight;
    PokedexRows(int rowHeight,int...weights){this.rowHeight=rowHeight;this.headerHeight=rowHeight;this.weights=weights;setTheme("");}
    PokedexRows table(int...columns){table=true;for(int column:columns)centered.add(column);return this;}
    PokedexRows headerHeight(int height){headerHeight=height;return this;}
    PokedexRows rowTheme(String theme){rowTheme=theme;return this;}
    static Label value(String text){Label label=new PokedexDetails.WrappedLabel(text.isBlank()?"—":text);label.setTheme("/pokedex-value");return label;}
    static Label title(String text){Label label=new PokedexDetails.WrappedLabel(text);label.setTheme("/pokedex-title");return label;}
    void row(Widget...widgets){
        int index=rows.size();rows.add(widgets);
        if(table&&index>0){
            Button background=new Button();background.setModel(new de.matthiasmann.twl.model.ToggleButtonModel());background.setCanAcceptKeyboardFocus(false);
            Runnable select=()->{for(int r=0;r<backgrounds.size();r++){
                boolean selected=backgrounds.get(r)==background;
                ((de.matthiasmann.twl.model.ToggleButtonModel)((Button)backgrounds.get(r)).getModel()).setSelected(selected);
                for(Widget cell:rows.get(r+1))cell.getAnimationState().setAnimationState(Button.STATE_SELECTED,selected);
            }};
            background.addCallback(select);
            background.setTheme(rowTheme);backgrounds.add(background);add(background);
            for(int i=0;i<widgets.length;i++)if(widgets[i] instanceof Label label){label.setTheme(centered.contains(i)?"/pokedex-table-center":"/pokedex-table-value");label.addCallback(reason->select.run());}
            for(Widget widget:widgets)if(!(widget instanceof Label)&&"/pokedex-value".equals(widget.getTheme()))widget.setTheme("/pokedex-table-cell");
        }
        for(Widget widget:widgets)add(widget);invalidateLayout();
    }
    void values(String...values){row(Arrays.stream(values).map(PokedexRows::value).toArray(Widget[]::new));}
    void mergedLast(int span,Widget...widgets){row(widgets);lastSpans.put(widgets,span);}
    void titles(String...values){row(Arrays.stream(values).map(PokedexRows::title).toArray(Widget[]::new));}
    @Override public int getPreferredInnerHeight(){return Math.max(preferred,rows.isEmpty()?0:headerHeight+(rows.size()-1)*rowHeight+rows.size()*(table?0:3));}
    @Override protected void layout(){
        int gap=table?0:3,total=Math.max(1,Arrays.stream(weights).sum()),available=Math.max(0,getInnerWidth()-gap*(weights.length-1)),y=getInnerY(),index=0;
        for(Widget[] row:rows){
            int offset=0,height=index==0?headerHeight:rowHeight;
            for(int i=0;i<row.length;i++){
                int weight=weights[i],span=i==row.length-1?lastSpans.getOrDefault(row,1):1;
                for(int n=1;n<span;n++)weight+=weights[i+n];
                // Shared column boundaries avoid losing rounding pixels on each cell and on merged rows.
                int x=getInnerX()+(int)((long)available*offset/total)+gap*i;
                int right=getInnerX()+(int)((long)available*(offset+weight)/total)+gap*(i+span-1);
                int width=right-x;offset+=weight;
                if(row[i] instanceof PokedexDetails.WrappedLabel label){label.wrap(Math.max(1,width-label.getBorderHorizontal()));height=Math.max(height,label.getPreferredHeight());}
                else if(!(row[i] instanceof Label)&&!(row[i] instanceof Button)){
                    row[i].setSize(width,height);height=Math.max(height,row[i].getPreferredHeight());
                }
                row[i].setPosition(x,y);row[i].setSize(width,height);
            }
            for(Widget cell:row)cell.setSize(cell.getWidth(),height);
            if(table&&index>0){Widget background=backgrounds.get(index-1);background.setPosition(getInnerX(),y);background.setSize(getInnerWidth(),height);}
            y+=height+gap;index++;
        }
        int next=y-getInnerY();if(next!=preferred){preferred=next;invalidateLayout();}
    }
}
