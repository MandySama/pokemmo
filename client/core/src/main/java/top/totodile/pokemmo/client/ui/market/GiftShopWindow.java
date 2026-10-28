package top.totodile.pokemmo.client.ui.market;

import com.badlogic.gdx.Gdx;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleListModel;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.common.*;
import top.totodile.pokemmo.client.ui.inventory.InventoryItems;
import top.totodile.pokemmo.server.game.GiftShopData;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/** Reference GiftShopWindow / TaskCallbackOt00: nonempty categories and two-column cards. */
public final class GiftShopWindow extends PlayerWindow {
    private final ClientStrings strings;
    private final InventoryItems items;
    private final Supplier<CompletionStage<List<GiftShopData.Item>>> load;
    private final Label balance=new Label(),sortLabel=new Label(),message=new Label();
    private final Button points;
    private final top.totodile.pokemmo.client.ui.pokedex.PokedexChoice sort;
    private final Map<Integer,ToggleButton> tabs=new LinkedHashMap<>();
    private List<GiftShopData.Item> catalog=List.of(),displayed=List.of();
    private int category=-1;private long version;
    private final Widget content=new Widget(){@Override protected void layout(){arrange();}};
    private final Widget grid=new Widget(){
        @Override public int getPreferredInnerHeight(){return ((getNumChildren()+1)/2)*(mobile?270:245);}
        @Override protected void layout(){int gap=10,w=Math.max(0,(getInnerWidth()-gap)/2),h=mobile?260:235;for(int i=0;i<getNumChildren();i++){var c=getChild(i);c.setPosition(getInnerX()+(i%2)*(w+gap),getInnerY()+(i/2)*(h+gap));c.setSize(w,h);}}
    };
    private final ScrollPane scroll=new TouchScrollPane();
    public GiftShopWindow(Widget owner,ClientStrings strings,InventoryItems items,boolean mobile,
                          Supplier<CompletionStage<List<GiftShopData.Item>>> load,Runnable close){
        super(owner,mobile,mobile?"/mobile-giftshop":"/giftshop",880,595,close);
        this.strings=strings;this.items=items;this.load=load;frame.setTitle(strings.get(3000));
        content.setTheme("");grid.setTheme("");frame.add(content);
        balance.setTheme("/giftshop-balance");balance.setText(strings.get(3001)+": — "+strings.get(3002));
        balance.setTooltipContent("当前参考服务端未接入奖励点余额。");
        sortLabel.setTheme("/label");sortLabel.setText(strings.get(3016));message.setTheme("/label");
        points=new Button(strings.get(3003));points.setTheme("/button");points.setEnabled(false);
        sort=new top.totodile.pokemmo.client.ui.pokedex.PokedexChoice(this::showCategory,
                strings.get(3024),strings.get(3020),strings.get(3021),strings.get(3022),strings.get(3023));
        sort.setTheme("/combobox");
        scroll.setTheme("/giftshop-scroll");scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);scroll.setContent(grid);
        for(var w:new Widget[]{balance,sortLabel,sort,points,scroll,message})content.add(w);
    }
    @Override protected void refresh(){
        long token=++version;message.setText("…");sort.setEnabled(false);
        try{items.load();}catch(java.io.IOException error){message.setText(strings.get(3017));return;}
        load.get().whenComplete((result,error)->Gdx.app.postRunnable(()->{
            if(token!=version||!isOpen())return;
            if(error!=null){message.setText(strings.get(3017));return;}
            catalog=List.copyOf(result);for(var tab:tabs.values())content.removeChild(tab);tabs.clear();
            for(int c=0;c<=8;c++){
                final int selected=c;if(catalog.stream().noneMatch(i->selected==0?i.featuredOrder()>=0:i.category()==selected))continue;
                var tab=new ToggleButton(strings.get(3050+c));tab.setTheme("/giftshop-tab");tab.setCanAcceptKeyboardFocus(false);
                tab.addCallback(()->{category=selected;showCategory();});tabs.put(c,tab);content.add(tab);
            }
            if(!tabs.containsKey(category))category=tabs.isEmpty()?-1:tabs.keySet().iterator().next();
            sort.setEnabled(true);showCategory();content.invalidateLayout();
        }));
    }
    private void showCategory(){
        if(items==null)return;
        Comparator<GiftShopData.Item> fallback=Comparator.comparingInt(GiftShopData.Item::param1).reversed()
                .thenComparing(Comparator.comparingInt(GiftShopData.Item::releaseTime).reversed())
                .thenComparing(Comparator.comparingInt(GiftShopData.Item::id).reversed());
        Comparator<GiftShopData.Item> by= switch(sort.selected()){
            case 1 -> Comparator.comparingInt(GiftShopData.Item::releaseTime).reversed().thenComparing(Comparator.comparingInt(GiftShopData.Item::id).reversed());
            case 2 -> Comparator.comparing((GiftShopData.Item i)->items.name(i.itemIndexId()).split("\\(")[0]).thenComparingInt(GiftShopData.Item::id);
            case 3 -> Comparator.comparingInt(GiftShopData.Item::price).thenComparing(fallback);
            case 4 -> Comparator.comparingInt(GiftShopData.Item::price).reversed().thenComparing(fallback);
            default -> Comparator.comparingInt(category==0?GiftShopData.Item::featuredOrder:GiftShopData.Item::param1).reversed().thenComparing(Comparator.comparingInt(GiftShopData.Item::releaseTime).reversed()).thenComparing(Comparator.comparingInt(GiftShopData.Item::id).reversed());
        };
        displayed=catalog.stream().filter(i->category==0?i.featuredOrder()>=0:i.category()==category).sorted(by).toList();
        tabs.forEach((c,b)->b.setActive(c==category));grid.removeAllChildren();
        for(var item:displayed)grid.add(new Card(item));
        message.setText(displayed.isEmpty()?strings.get(3004):"");scroll.setScrollPositionY(0);content.invalidateLayout();
    }
    private final class Card extends Widget {
        final Label name,price,description;final Button buy;final GiftShopData.Item item;
        Card(GiftShopData.Item item){
            this.item=item;setTheme("/giftshop-card");
            name=new Label((item.quantity()>1?"x"+item.quantity()+" ":"")+items.name(item.itemIndexId()));name.setTheme("/giftshop-name");
            name.setTooltipContent(items.description(item.itemIndexId()));
            name.setClip(true);
            description=new Label(items.description(item.itemIndexId()));description.setTheme("/label");description.setClip(true);
            description.setTooltipContent(items.description(item.itemIndexId()));
            String text=strings.format(2996,Integer.toString(item.price()));
            if(item.originalPrice()>item.price())text+=" ("+strings.format(2995,Integer.toString((item.originalPrice()-item.price())*100/item.originalPrice()))+")";
            price=new Label(text);price.setTheme("/label");
            buy=new Button(strings.get(56));buy.setTheme("/button");buy.setEnabled(false);buy.setTooltipContent("参考服务端尚未接入商城购买结算。");
            add(name);add(price);add(description);add(buy);setTooltipContent(items.description(item.itemIndexId()));
        }
        @Override protected void paintWidget(GUI gui){super.paintWidget(gui);var icon=items.icon(item.itemIndexId());if(icon!=null){int size=mobile?48:24;icon.draw(getAnimationState(),getInnerX()+22,getInnerY()+12,size,size);}}
        private String wrap(String source,int width){
            if(description.getFont()==null)return source;
            var out=new StringBuilder();var line=new StringBuilder();
            for(int at=0;at<source.length();){int cp=source.codePointAt(at);at+=Character.charCount(cp);
                if(cp=='\n'){out.append(line).append('\n');line.setLength(0);continue;}
                String next=line.toString()+new String(Character.toChars(cp));
                if(!line.isEmpty()&&description.getFont().computeTextWidth(next)>width){out.append(line).append('\n');line.setLength(0);}
                line.appendCodePoint(cp);
            }
            return out.append(line).toString();
        }
        @Override protected void layout(){
            int x=getInnerX(),y=getInnerY(),w=getInnerWidth(),h=getInnerHeight(),left=mobile?92:80,tw=Math.max(0,w-left-4);
            name.setPosition(x+left,y);name.setSize(tw,mobile?36:24);
            description.setText(wrap(items.description(item.itemIndexId()),tw));
            description.setPosition(x+left,y+(mobile?38:26));description.setSize(tw,Math.max(0,h-(mobile?116:82)));
            price.setPosition(x+left,y+h-(mobile?76:52));price.setSize(tw,mobile?32:24);
            buy.setPosition(x+left,y+h-(mobile?44:28));buy.setSize(tw,mobile?44:28);
        }
    }
    private void arrange(){
        content.setPosition(frame.getInnerX(),frame.getInnerY());content.setSize(frame.getInnerWidth(),frame.getInnerHeight());
        int x=content.getInnerX()+4,y=content.getInnerY(),w=content.getInnerWidth()-8,h=content.getInnerHeight(),row=mobile?56:30;
        if(mobile){
            balance.setPosition(x,y);balance.setSize(Math.min(w-240,300),row);sortLabel.setPosition(x+w-230,y);sortLabel.setSize(70,row);sort.setPosition(x+w-150,y);sort.setSize(150,row);
            y+=row+8;h-=row+8;int tw=150,j=0;for(var tab:tabs.values()){tab.setPosition(x+w-tw,y+j*row);tab.setSize(tw,row);j++;}
            scroll.setPosition(x,y);scroll.setSize(Math.max(0,w-tw-8),Math.max(0,h-row-8));
        }else{
            int tx=x;for(var tab:tabs.values()){int tw=Math.min(150,Math.max(64,tab.getPreferredInnerWidth()+28));tab.setPosition(tx,y);tab.setSize(tw,26);tx+=tw+2;}
            y+=28;balance.setPosition(x,y);balance.setSize(Math.min(250,balance.getPreferredWidth()),30);
            sortLabel.setPosition(x+w-180,y);sortLabel.setSize(55,30);sort.setPosition(x+w-120,y);sort.setSize(120,30);
            scroll.setPosition(x+8,y+42);scroll.setSize(w-16,Math.max(0,h-28-42-40));
        }
        points.setSize(mobile?260:170,row);points.setPosition(x+w-points.getWidth(),content.getInnerBottom()-row-4);
        message.setPosition(scroll.getX()+10,scroll.getY()+10);message.setSize(Math.max(0,scroll.getWidth()-20),row);
    }
    @Override protected void layout(){super.layout();arrange();}
    @Override public void closePopup(){version++;sort.closeChoices();super.closePopup();}
}
