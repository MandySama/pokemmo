package top.totodile.pokemmo.client.ui.inventory;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.ToggleButtonModel;
import de.matthiasmann.twl.renderer.Image;
import java.util.*;
import java.util.function.*;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.common.PlayerWindow;
import top.totodile.pokemmo.client.ui.common.PlayerTextInput;
import top.totodile.pokemmo.client.ui.common.TouchScrollPane;
import top.totodile.pokemmo.client.ui.common.WordWrapTextFormatter;
import top.totodile.pokemmo.server.game.PlayerData;

/** InventoryWindow / l5_0.Oy / ItemListContainerLayout: eight pockets, search and native item grid. */
public final class InventoryWindow extends PlayerWindow {
    private static final int[] POCKETS={1,7,8,2,3,4,5,6};
    private static final int[] TITLES={1401,1404,1405,1402,2,3,1406,1403};
    private static final int[] ICONS={5436,5017,5057,5476,5004,5252,5155,5459};
    private final ClientStrings strings;
    private final InventoryItems items;
    private final Consumer<Consumer<List<PlayerData.Item>>> load;
    private final IntFunction<Image> uiIcons;
    private final Button[] tabs=new Button[8];
    private final EditField search;
    private final PlayerTextInput textInput;
    private final ScrollPane scroll=new TouchScrollPane();
    private final Label wallet=new Label(),itemName=new Label(),description=new Label();
    private final Widget itemInfo=new Widget(){@Override protected void layout(){layoutInfo();}};
    private int selectedItem;
    private final Widget itemPicture=new Widget(){@Override protected void paintWidget(GUI gui){var icon=items.icon(selectedItem);if(selectedItem!=0&&icon!=null)icon.draw(getAnimationState(),getX(),getY(),96,96);}};
    private final Widget grid=new Widget(){
        @Override public int getPreferredInnerHeight(){return gridHeight();}
        @Override protected void layout(){layoutGrid();}
    };
    private final Widget content=new Widget(){@Override protected void layout(){layoutContent();}};
    private List<PlayerData.Item> inventory=List.of();
    public void reloadIfOpen(){if(isOpen())refresh();}
    private int selected,region;
    private final List<Widget[]> rows=new ArrayList<>();
    public InventoryWindow(Widget owner,ClientStrings strings,boolean mobile,InventoryItems items,IntFunction<Image> uiIcons,
                           Consumer<Consumer<List<PlayerData.Item>>> load,Runnable onClose){
        super(owner,mobile,mobile?"/mobile-inventory-tabbedframe":"/inventory-tabbedframe",530,386,onClose);this.strings=strings;this.items=items;this.uiIcons=uiIcons;this.load=load;
        textInput=new PlayerTextInput(mobile,this::isOpen);search=textInput.field(40,false);
        content.setTheme("");grid.setTheme("");frame.add(content);scroll.setTheme("/player-inventory-scroll");scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);scroll.setContent(grid);content.add(scroll);
        for(int i=0;i<tabs.length;i++){
            final int index=i;tabs[i]=new Button(mobile?strings.get(TITLES[i]):""){
                @Override protected void paintWidget(GUI gui){super.paintWidget(gui);Image icon=uiIcons.apply(ICONS[index]);if(icon!=null){int size=mobile?48:24;icon.draw(getAnimationState(),mobile?getX()+4:getX()+(getWidth()-size)/2,getY()+(getHeight()-size)/2,size,size);}}
            };
            tabs[i].setTheme("/inventory-tab-empty");tabs[i].setModel(new ToggleButtonModel());tabs[i].setTooltipContent(strings.get(TITLES[i]));tabs[i].setCanAcceptKeyboardFocus(false);
            tabs[i].addCallback(()->select(index));content.add(tabs[i]);
        }
        search.setTheme("/player-inventory-search");search.setMaxTextLength(40);search.addCallback(key->rebuild());content.add(search);select(0);
        if(mobile){wallet.setTheme("/mobile-inventory-wallet");content.add(wallet);itemInfo.setTheme("");itemName.setTheme("/mobile-inventory-name");description.setTheme("/mobile-inventory-description");itemInfo.add(itemName);itemInfo.add(itemPicture);itemInfo.add(description);itemInfo.setVisible(false);content.add(itemInfo);}
    }
    public void money(long money){wallet.setText(strings.format(1928,java.text.NumberFormat.getInstance().format(money)));}
    public void region(int region){this.region=region;inventory=List.of();search.setText("");select(0);}
    public boolean editing(){return search.hasKeyboardFocus();}
    @Override public void closePopup(){textInput.close();super.closePopup();}
    @Override protected void refresh(){load.accept(value->{if(!isOpen())return;inventory=List.copyOf(value);for(int i=0;i<tabs.length;i++){tabs[i].setTooltipContent(items.pocketName(TITLES[i]));if(mobile)tabs[i].setText(items.pocketName(TITLES[i]));}select(selected);});}
    private void select(int index){selected=index;for(int i=0;i<tabs.length;i++)((ToggleButtonModel)tabs[i].getModel()).setSelected(i==index);frame.setTitle(strings.get(1400)+" - "+items.pocketName(TITLES[index]).toUpperCase(Locale.ROOT));rebuild();}
    private void rebuild(){
        grid.removeAllChildren();rows.clear();selectedItem=0;itemInfo.setVisible(false);String filter=search.getText().strip().toLowerCase(Locale.ROOT);int group=-1;List<Widget> cells=new ArrayList<>();
        var entries=inventory.stream().filter(i->i.region()<0||i.region()==region).filter(i->{var t=items.template(i.itemId());return t!=null&&t.pocket()==POCKETS[selected]&&items.name(i.itemId()).toLowerCase(Locale.ROOT).contains(filter);}).sorted(Comparator.comparingInt(PlayerData.Item::itemId)).toList();
        for(var item:entries){
            int next=items.template(item.itemId()).group();
            if(next!=group){if(!cells.isEmpty()){rows.add(cells.toArray(Widget[]::new));cells.clear();}group=next;
                String title=strings.getOrDefault(10000+group,"");if(!title.isEmpty()){Label label=new Label(title);label.setTheme(mobile?"/mobile-inventory-name":"label");grid.add(label);rows.add(new Widget[]{label});}}
            Button cell=new Button(mobile?item.amount()+"x "+items.name(item.itemId()):"x"+item.amount()){
                @Override protected void paintWidget(GUI gui){super.paintWidget(gui);Image icon=items.icon(item.itemId());if(icon!=null){int size=mobile?48:24;icon.draw(getAnimationState(),mobile?getX():getX()+(getWidth()-size)/2,getY()+(mobile?3:2),size,size);}}
            };
            cell.setTheme(mobile?"/mobile-inventory-button":"/inventory-button");cell.setTooltipContent(items.name(item.itemId())+"\n"+items.description(item.itemId()));cell.setCanAcceptKeyboardFocus(false);
            if(mobile)cell.addCallback(()->{selectedItem=item.itemId();itemName.setText(item.amount()+"x "+items.name(selectedItem));itemInfo.setVisible(true);itemInfo.invalidateLayout();});
            grid.add(cell);cells.add(cell);if(cells.size()==(mobile?1:11)){rows.add(cells.toArray(Widget[]::new));cells.clear();}
        }
        if(!cells.isEmpty())rows.add(cells.toArray(Widget[]::new));scroll.setScrollPositionY(0);grid.invalidateLayout();content.invalidateLayout();
    }
    private int rowHeight(Widget[] row){return row[0] instanceof Label?Math.max(18,row[0].getPreferredHeight()):mobile?60:44;}
    private int gridHeight(){int h=0;for(var row:rows)h+=rowHeight(row)+3;return h;}
    private void layoutGrid(){int y=grid.getInnerY();for(var row:rows){int h=rowHeight(row);for(int i=0;i<row.length;i++){int cell=mobile?grid.getInnerWidth():44;row[i].setPosition(grid.getInnerX()+i*cell,y);row[i].setSize(row[i] instanceof Label?grid.getInnerWidth():cell,h);}y+=h+3;}}
    private void layoutContent(){
        if(mobile){
            int x=content.getX(),y=content.getY(),width=content.getWidth(),height=content.getHeight();
            int tabWidth=180;for(var tab:tabs)tabWidth=Math.max(tabWidth,tab.getPreferredWidth());
            int row=Math.max(50,tabs[0].getPreferredHeight());
            for(int i=0;i<tabs.length;i++){tabs[i].setPosition(x,y+61+i*row);tabs[i].setSize(tabWidth,row);}
            int listX=x+tabWidth+15,listWidth=Math.min(415,Math.max(0,width-tabWidth-15));
            scroll.setPosition(listX,y+61);scroll.setSize(listWidth,Math.max(0,height-61));
            search.setPosition(x+Math.max(320,width/2-180),y+8);search.setSize(300,44);
            wallet.setPosition(search.getRight()+15,y+10);wallet.setSize(Math.max(0,x+width-104-wallet.getX()),40);
            itemInfo.setPosition(listX+listWidth+15,y+61);itemInfo.setSize(Math.max(0,x+width-itemInfo.getX()-15),Math.max(0,height-61));return;
        }
        int tab=mobile?68:40;
        int header=mobile?61:0;
        for(int i=0;i<tabs.length;i++){int left=i*content.getWidth()/8,right=(i+1)*content.getWidth()/8;tabs[i].setPosition(content.getX()+left,content.getY()+header);tabs[i].setSize(right-left,tab);}
        search.setPosition(content.getRight()-(mobile?300:200),content.getY()+(mobile?15:tab+2));search.setSize(180,mobile?32:24);
        int top=header+tab+(mobile?10:40);scroll.setPosition(content.getX()+14,content.getY()+top);scroll.setSize(Math.max(0,content.getWidth()-28),Math.max(0,content.getHeight()-top-10));
    }
    private void layoutInfo(){
        int x=itemInfo.getInnerX(),y=itemInfo.getInnerY(),width=itemInfo.getInnerWidth();
        itemName.setPosition(x+10,y+10);itemName.setSize(Math.max(0,width-20),30);
        itemPicture.setPosition(x+Math.max(0,width-180),y+50);itemPicture.setSize(96,128);
        description.setText(WordWrapTextFormatter.w70(items.description(selectedItem),Math.max(1,(width-220)/18)));
        description.setPosition(x+10,y+50);description.setSize(Math.max(0,width-210),Math.max(0,itemInfo.getHeight()-60));
    }
}
