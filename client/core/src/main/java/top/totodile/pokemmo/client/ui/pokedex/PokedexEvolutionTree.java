package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;
import java.util.*;
import java.util.function.IntConsumer;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokedex;

/** Reference EvolutionTreeWidget: leaf rows, parents centred on children, condition badges between stages. */
final class PokedexEvolutionTree extends Widget {
    private final BwPokedex dex;
    private final boolean mobile;
    private final Map<Integer,Widget> cards=new LinkedHashMap<>();
    private final Map<Integer,PokedexDetails.WrappedLabel> conditions=new LinkedHashMap<>();
    private final Map<Integer,Integer> depths=new LinkedHashMap<>(),rows=new HashMap<>();
    private int leaves,maxDepth;

    PokedexEvolutionTree(BwPokedex dex,PokedexImages images,boolean mobile,int selected,IntConsumer select) {
        this.dex=dex;this.mobile=mobile;setTheme("");
        int root=selected;var visited=new HashSet<Integer>();
        while(dex.parent(root)>0&&visited.add(root))root=dex.parent(root);
        collect(root,0,new HashSet<>());
        for(int id:depths.keySet()) {
            var card=new Widget(){
                final Button icon=new Button(){@Override protected void paintWidget(GUI gui){int size=mobile?72:36;images.icon(id).draw(getAnimationState(),getX()+(getWidth()-size)/2,getY()+(getHeight()-size)/2,size,size);}};
                final Label name=new Label(dex.name(id));
                {setTheme("");icon.setTheme("/monsterdex-button");icon.setCanAcceptKeyboardFocus(false);icon.setTooltipContent(dex.name(id));icon.addCallback(()->select.accept(id));
                    name.setTheme("/pokedex-evolution-name");add(icon);add(name);}
                @Override protected void layout(){int size=mobile?92:46;icon.setSize(size,size);icon.setPosition(getX()+(getWidth()-size)/2,getY());
                    name.setSize(getWidth(),mobile?28:22);name.setPosition(getX(),icon.getBottom()+2);}
            };
            cards.put(id,card);add(card);
            int parent=dex.parent(id);
            if(depths.containsKey(parent)) {
                var evolution=dex.get(parent).evolutions().stream().filter(e->e.target()==id).findFirst().orElseThrow();
                String condition=dex.evolution(evolution);
                var badge=new PokedexDetails.WrappedLabel("─ "+condition+" ─") {
                    final Label arrow=new Label("➤");
                    {arrow.setTheme("/pokedex-evolution-arrow");add(arrow);}
                    @Override public int getPreferredInnerHeight(){return (getFont()==null?14:getFont().getLineHeight())*getText().split("\n",-1).length;}
                    @Override protected void layout(){arrow.setSize(16,getInnerHeight());arrow.setPosition(getInnerRight()-16,getInnerY());}
                };badge.setTheme("/pokedex-evolution-condition");badge.setTooltipContent(condition);
                conditions.put(id,badge);add(badge);
            }
        }
    }
    private int collect(int id,int depth,Set<Integer> visited) {
        if(!visited.add(id))return rows.getOrDefault(id,0);
        depths.put(id,depth);maxDepth=Math.max(maxDepth,depth);
        var children=dex.get(id).evolutions().stream().map(BwPokedex.Evolution::target).distinct().toList();
        int row;
        if(children.isEmpty())row=leaves++*2;
        else {int first=collect(children.get(0),depth+1,visited),last=first;for(int i=1;i<children.size();i++)last=collect(children.get(i),depth+1,visited);row=(first+last)/2;}
        rows.put(id,row);return row;
    }
    private int rowHeight(){return mobile?142:90;}
    @Override public int getPreferredInnerHeight(){return Math.max(mobile?360:323,leaves*rowHeight()+32);}
    @Override protected void layout() {
        int cardW=mobile?120:80,cardH=mobile?122:70;
        int step=maxDepth==0?0:Math.max(cardW+70,(getInnerWidth()-cardW-36)/maxDepth);
        int left=getInnerX()+(getInnerWidth()-(maxDepth*step+cardW))/2;
        int top=getInnerY()+Math.max(16,(getInnerHeight()-leaves*rowHeight())/2);
        for(var entry:cards.entrySet()) {
            int id=entry.getKey();Widget card=entry.getValue();
            card.setSize(cardW,cardH);card.setPosition(left+depths.get(id)*step,top+rows.get(id)*rowHeight()/2);
        }
        for(var entry:conditions.entrySet()) {
            Widget from=cards.get(dex.parent(entry.getKey())),to=cards.get(entry.getKey());
            var badge=entry.getValue();int width=Math.max(20,to.getX()-from.getRight()-8);
            badge.wrap(width-26);badge.setSize(width,badge.getPreferredHeight());
            badge.setPosition(from.getRight()+4,to.getY()+((mobile?92:46)-badge.getHeight())/2);
        }
    }
}
