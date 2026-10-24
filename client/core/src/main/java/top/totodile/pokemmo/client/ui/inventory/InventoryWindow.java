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
    public enum ItemAction { USE, GIVE, DISCARD }
    @FunctionalInterface public interface ItemDrop {boolean drop(PlayerData.Item item,int x,int y);}
    private ItemDrop itemDrop=(item,x,y)->false;
    public void onItemDrop(ItemDrop handler){itemDrop=Objects.requireNonNull(handler);}
    private static final int[] POCKETS={1,7,8,2,3,4,5,6};
    private static final int[] TITLES={1401,1404,1405,1402,2,3,1406,1403};
    private static final int[] ICONS={5436,5017,5057,5476,5004,5252,5155,5459};
    private final ClientStrings strings;
    private final InventoryItems items;
    private final Consumer<Consumer<List<PlayerData.Item>>> load;
    private final IntFunction<Image> uiIcons;
    private final Button[] tabs=new Button[8];
    private final BoxLayout tabBox=new BoxLayout(BoxLayout.Direction.HORIZONTAL);
    private final EditField search;
    private final PlayerTextInput textInput;
    private final InventoryContextMenu itemMenu;
    private BiConsumer<PlayerData.Item,ItemAction> itemAction=(item,action)->{};
    private final ScrollPane scroll=new TouchScrollPane();
    private final Label wallet=new Label(),itemName=new Label(),description=new Label();
    private final Widget itemInfo=new Widget(){@Override protected void layout(){layoutInfo();}};
    private int selectedItem;
    private final Widget itemPicture=new Widget(){@Override protected void paintWidget(GUI gui){var icon=items.icon(selectedItem);if(selectedItem!=0&&icon!=null)icon.draw(getAnimationState(),getX(),getY(),96,96);}};
    private final DialogLayout grid=new DialogLayout();
    private final Widget content=new Widget(){@Override protected void layout(){layoutContent();}};
    private List<PlayerData.Item> inventory=List.of();
    public void reloadIfOpen(){if(isOpen())refresh();}
    public int randomItemPageHeight(){return isOpen()?Math.max(1,scroll.getInnerHeight()):296;}
    private int selected,region;
    private final List<Widget[]> rows=new ArrayList<>();
    public InventoryWindow(Widget owner,ClientStrings strings,boolean mobile,InventoryItems items,IntFunction<Image> uiIcons,
                           Consumer<Consumer<List<PlayerData.Item>>> load,Runnable onClose){
        super(owner,mobile,mobile?"/mobile-inventory-tabbedframe":"/inventory-tabbedframe",530,386,onClose);this.strings=strings;this.items=items;this.uiIcons=uiIcons;this.load=load;
        itemMenu=new InventoryContextMenu(items,strings,(item,action)->itemAction.accept(item,action));
        textInput=new PlayerTextInput(mobile,this::isOpen);search=textInput.field(40,false);
        content.setTheme("");grid.setTheme("/player-inventory-grid");frame.add(content);scroll.setTheme("/player-inventory-scroll");scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);scroll.setContent(grid);content.add(scroll);
        if(!mobile){tabBox.setTheme("/player-inventory-tabbox");tabBox.setAlignment(Alignment.FILL);content.add(tabBox);}
        for(int i=0;i<tabs.length;i++){
            final int index=i;tabs[i]=new Button(mobile?strings.get(TITLES[i]):""){
                @Override public int getPreferredInnerWidth(){return mobile?super.getPreferredInnerWidth():24;}
                @Override public int getPreferredInnerHeight(){return mobile?super.getPreferredInnerHeight():26;}
                @Override protected void paintWidget(GUI gui){super.paintWidget(gui);Image icon=items.tabIcon(ICONS[index],((ToggleButtonModel)getModel()).isSelected());if(icon==null)icon=uiIcons.apply(ICONS[index]);if(icon!=null){int size=mobile?48:24;icon.draw(getAnimationState(),getX()+(mobile?6:(getWidth()-size)/2),getY()+(mobile?6:1),size,size);}}
            };
            tabs[i].setTheme("/inventory-tab-empty");tabs[i].setModel(new ToggleButtonModel());tabs[i].setTooltipContent(strings.get(TITLES[i]));tabs[i].setCanAcceptKeyboardFocus(false);
            tabs[i].addCallback(()->select(index));if(mobile)content.add(tabs[i]);else tabBox.add(tabs[i]);
        }
        search.setTheme("/player-inventory-search");search.setMaxTextLength(40);search.addCallback(key->rebuild());content.add(search);select(0);
        if(mobile){wallet.setTheme("/mobile-inventory-wallet");content.add(wallet);itemInfo.setTheme("");itemName.setTheme("/mobile-inventory-name");description.setTheme("/mobile-inventory-description");itemInfo.add(itemName);itemInfo.add(itemPicture);itemInfo.add(description);itemInfo.setVisible(false);content.add(itemInfo);}
    }
    public void money(long money){wallet.setText(strings.format(1928,java.text.NumberFormat.getInstance().format(money)));}
    public void region(int region){this.region=region;inventory=List.of();search.setText("");select(0);}
    public boolean editing(){return search.hasKeyboardFocus();}
    @Override public void open(){super.open();search.giveupKeyboardFocus();requestKeyboardFocus(null);requestKeyboardFocus();}
    public void onItemAction(BiConsumer<PlayerData.Item,ItemAction> action){itemAction=Objects.requireNonNull(action);}
    public void activateItem(PlayerData.Item item,ItemAction action){itemAction.accept(item,action);}
    public boolean hasMenu(){return itemMenu.isOpen();}
    public boolean closeMenu(){boolean open=hasMenu();itemMenu.close();return open;}
    @Override public void closePopup(){itemMenu.close();textInput.close();super.closePopup();}
    @Override protected void refresh(){load.accept(value->{if(!isOpen())return;inventory=List.copyOf(value);for(int i=0;i<tabs.length;i++){tabs[i].setTooltipContent(items.pocketName(TITLES[i]));if(mobile)tabs[i].setText(items.pocketName(TITLES[i]));}select(selected);});}
    private void select(int index){selected=index;for(int i=0;i<tabs.length;i++)((ToggleButtonModel)tabs[i].getModel()).setSelected(i==index);frame.setTitle(strings.get(1400)+" - "+items.pocketName(TITLES[index]).toUpperCase(Locale.ROOT));rebuild();}
    private void rebuild(){
        itemMenu.close();
        grid.setHorizontalGroup(null);grid.setVerticalGroup(null);grid.removeAllChildren();rows.clear();selectedItem=0;itemInfo.setVisible(false);String filter=search.getText().strip().toLowerCase(Locale.ROOT);int group=0;List<Widget> cells=new ArrayList<>();
        boolean machines=POCKETS[selected]==4;
        var entries=inventory.stream().filter(i->i.region()<0||i.region()==region).filter(i->{var t=items.template(i.itemId());return t!=null&&t.pocket()==POCKETS[selected]&&items.name(i.itemId()).toLowerCase(Locale.ROOT).contains(filter);}).sorted(Comparator.<PlayerData.Item>comparingInt(i->items.group(i.itemId())).thenComparing(i->items.name(i.itemId()).toLowerCase(Locale.ROOT)).thenComparingInt(PlayerData.Item::itemId)).toList();
        for(var item:entries){
            int next=items.group(item.itemId());
            if(next!=group){if(!cells.isEmpty()){rows.add(cells.toArray(Widget[]::new));cells.clear();}group=next;
                String title=items.groupTitle(item.itemId());if(!title.isEmpty()){Label label=new Label(title);label.setTheme(mobile?"/mobile-inventory-name":"label");grid.add(label);rows.add(new Widget[]{label});}}
            Button cell=new Button(mobile||machines?item.amount()+"x "+items.name(item.itemId()):"x"+item.amount()){
                private int downX,downY;private boolean dragging;
                @Override protected boolean handleEvent(Event event){
                    if(!mobile){
                        if(event.getType()==Event.Type.MOUSE_BTNDOWN&&event.getMouseButton()==0){downX=event.getMouseX();downY=event.getMouseY();dragging=false;}
                        if(event.getType()==Event.Type.MOUSE_DRAGGED&&Math.abs(event.getMouseX()-downX)+Math.abs(event.getMouseY()-downY)>4){dragging=true;getModel().setArmed(false);return true;}
                        if(event.getType()==Event.Type.MOUSE_BTNUP&&event.getMouseButton()==0&&dragging){getModel().setArmed(false);getModel().setPressed(false);dragging=false;itemDrop.drop(item,event.getMouseX(),event.getMouseY());return true;}
                    }
                    return super.handleEvent(event);
                }
                @Override public Object getTooltipContent(){return mobile?null:items.tooltipContent(item.itemId());}
                @Override public int getMinWidth(){return mobile?400:machines?196:40;}
                @Override public int getMaxWidth(){return getMinWidth();}
                @Override public int getPreferredWidth(){return getMinWidth();}
                @Override public int getMinHeight(){return mobile?60:machines?36:42;}
                @Override public int getMaxHeight(){return getMinHeight();}
                @Override public int getPreferredHeight(){return getMinHeight();}
                @Override protected void paintWidget(GUI gui){super.paintWidget(gui);Image icon=items.icon(item.itemId());if(icon!=null){int size=mobile?48:24;icon.draw(getAnimationState(),mobile?getX():machines?getX()+7:getX()+(getWidth()-size)/2,getY()+(mobile?3:machines?5:3),size,size);}}
            };
            cell.setTheme(mobile?"/mobile-inventory-button":machines?"/tm-button":"/inventory-button");cell.setCanAcceptKeyboardFocus(false);
            // ItemListContainerLayout: only desktop cells have a hover tooltip (GH0=40).
            if(!mobile){cell.setTooltipContent(items.tooltip(item.itemId(),false));cell.setTooltipDelay(40);}
            cell.addCallback(()->{if(mobile){selectedItem=item.itemId();itemName.setText(item.amount()+"x "+items.name(selectedItem));itemInfo.setVisible(true);itemInfo.invalidateLayout();}else itemMenu.open(item,cell);});
            grid.add(cell);cells.add(cell);if(cells.size()==(mobile?1:machines?2:11)){rows.add(cells.toArray(Widget[]::new));cells.clear();}
        }
        if(!cells.isEmpty())rows.add(cells.toArray(Widget[]::new));
        // ItemListContainerLayout.KX: each row is sequential horizontally and parallel vertically.
        grid.setAddDefaultGaps(false);var horizontal=grid.createParallelGroup();var vertical=grid.createSequentialGroup();
        boolean firstRow=true;for(var row:rows){var across=grid.createSequentialGroup();for(int i=0;i<row.length;i++){if(i>0)across.addGap(DialogLayout.DEFAULT_GAP);across.addWidget(row[i],Alignment.TOPLEFT);}horizontal.addGroup(across);if(!firstRow)vertical.addGap(DialogLayout.DEFAULT_GAP);vertical.addGroup(grid.createParallelGroup(row));firstRow=false;}
        grid.setHorizontalGroup(horizontal);grid.setVerticalGroup(vertical);
        scroll.setScrollPositionY(0);content.invalidateLayout();
    }
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
        tabBox.setPosition(content.getX()+10,content.getY());tabBox.setSize(Math.max(0,content.getWidth()-15),tabBox.getPreferredHeight());
        search.setPosition(frame.getX()+330,frame.getY()+40);search.setSize(Math.max(0,Math.min(180,frame.getWidth()-330-32)),28);
        int top=80;scroll.setPosition(frame.getX()+20,frame.getY()+top);scroll.setSize(Math.max(0,frame.getWidth()-30),Math.max(0,frame.getHeight()-top-10));
    }
    private void layoutInfo(){
        int x=itemInfo.getInnerX(),y=itemInfo.getInnerY(),width=itemInfo.getInnerWidth();
        itemName.setPosition(x+10,y+10);itemName.setSize(Math.max(0,width-20),30);
        itemPicture.setPosition(x+Math.max(0,width-180),y+50);itemPicture.setSize(96,128);
        description.setText(WordWrapTextFormatter.w70(items.description(selectedItem),Math.max(1,(width-220)/18)));
        description.setPosition(x+10,y+50);description.setSize(Math.max(0,width-210),Math.max(0,itemInfo.getHeight()-60));
    }
}
