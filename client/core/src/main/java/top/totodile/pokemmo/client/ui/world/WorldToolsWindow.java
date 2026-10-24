package top.totodile.pokemmo.client.ui.world;

import com.badlogic.gdx.Gdx;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.ToggleButtonModel;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import top.totodile.pokemmo.client.resources.*;
import top.totodile.pokemmo.client.ui.common.*;
import top.totodile.pokemmo.client.ui.inventory.InventoryItems;
import top.totodile.pokemmo.server.game.GameService;
import top.totodile.pokemmo.server.game.GameService.Position;

/** Adapts TeleportMenuWindow and AdminDatabaseSearchWindow to the three requested map buttons. */
public final class WorldToolsWindow extends PlayerWindow {
    public enum Tool { TELEPORT, ITEM, POKEMON }
    private record Row(Widget[] cells,boolean heading) {}
    private final Widget content=new Widget(){@Override protected void layout(){layoutContent();}};
    private final Widget results=new Widget(){
        @Override public int getPreferredInnerWidth(){return tool==Tool.TELEPORT?Arrays.stream(teleportColumns()).sum()+15:0;}
        @Override public int getPreferredInnerHeight(){return resultsHeight();}
        @Override protected void layout(){layoutResults();}
    };
    private final ScrollPane scroll=new TouchScrollPane();
    private final PlayerTextInput keyboard;
    private final EditField search;
    private final Button searchButton=new Button("Search");
    private static final int[] REGIONS={0,4,1,3,2};
    private final Button[] tabs=new Button[REGIONS.length];
    private final List<Row> rows=new ArrayList<>();
    private final ClientStrings strings;
    private final InventoryItems items;
    private final Supplier<List<GameService.ItemTemplate>> catalogue;
    private final GameService game;
    private final ReferenceTeleportEntries destinations;
    private final BiFunction<Tool,int[],CompletionStage<String>> execute;
    private final Consumer<String> completed,failed;
    private final AdminValueDialog amount;
    private Tool tool;
    private int selectedRegion,direction;
    private boolean busy,loading;
    private long version,loadVersion;
    public WorldToolsWindow(Widget owner,boolean mobile,ClientResources resources,InventoryItems items,GameService game,
                            BiFunction<Tool,int[],CompletionStage<String>> execute,Consumer<String> completed,Consumer<String> failed,Runnable onClose){
        super(owner,mobile,"/adminframe-nontab",850,500,onClose);this.strings=resources.strings();this.items=items;this.game=game;this.catalogue=game==null?List::of:game::itemTemplates;this.execute=execute;this.completed=completed;this.failed=failed;
        destinations=new ReferenceTeleportEntries(resources);keyboard=new PlayerTextInput(mobile,this::isOpen);search=keyboard.field(80,false);search.setTheme("/editfield");search.addCallback(key->rebuildSearch());
        content.setTheme("");results.setTheme("");scroll.setTheme("/world-tools-scroll");scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);scroll.setContent(results);frame.add(content);content.add(search);content.add(searchButton);content.add(scroll);searchButton.setTheme("/button");searchButton.addCallback(this::rebuildSearch);
        for(int i=0;i<tabs.length;i++){
            final int index=i;var button=new Button(strings.get(250000+REGIONS[i]));tabs[i]=button;button.setTheme("/teleport-tab");button.setModel(new ToggleButtonModel());button.setCanAcceptKeyboardFocus(false);button.addCallback(()->selectRegion(index));content.add(button);
        }
        amount=new AdminValueDialog(owner,strings,mobile,onClose);
    }
    public void show(Tool tool,int region,Position position){
        if(busy)return;this.tool=tool;direction=position.direction();version++;
        frame.setTitle(tool==Tool.TELEPORT?"传送菜单":"Search");frame.setTheme(tool==Tool.TELEPORT?"/tp-menu":"/adminframe-nontab");
        boolean teleport=tool==Tool.TELEPORT;scroll.setFixed(teleport?ScrollPane.Fixed.NONE:ScrollPane.Fixed.HORIZONTAL);search.setVisible(!teleport);searchButton.setVisible(!teleport);for(var tab:tabs)tab.setVisible(teleport);
        loading=true;clearRows();search.setText("");open();sizeWindow();
        if(teleport)selectRegion(selectedRegion);else{try{items.gameData(game);loading=false;rebuildSearch();search.requestKeyboardFocus();}catch(Exception e){loading=false;failed.accept(e.getMessage());}}
    }
    private void sizeWindow(){
        if(!isOpen())return;
        int height=mobile&&tool==Tool.TELEPORT?getHeight():Math.min(getHeight(),500);
        // 对齐参考 tp-menu 的 PC 尺寸；超出的四列内容通过横向滚动条查看。
        int preferredWidth=tool==Tool.TELEPORT?670:850;
        int width=mobile&&tool==Tool.TELEPORT?getWidth():Math.min(getWidth(),preferredWidth);
        frame.setSize(width,height);frame.setPosition(getX()+(getWidth()-width)/2,getY()+(getHeight()-height)/2);content.invalidateLayout();
    }
    @Override public void positionWindow(){int w=getWidth(),h=getHeight();super.positionWindow();if(w!=getWidth()||h!=getHeight())sizeWindow();amount.positionWindow();}
    @Override protected boolean fullScreen(){return mobile&&tool==Tool.TELEPORT;}
    @Override protected void refresh(){}
    public boolean editing(){return isOpen();}
    public void back(){if(amount.isOpen())amount.closePopup();else closePopup();}
    private void clearRows(){results.removeAllChildren();rows.clear();scroll.setScrollPositionX(0);scroll.setScrollPositionY(0);results.invalidateLayout();}
    private void selectRegion(int index){
        if(busy)return;selectedRegion=index;for(int i=0;i<tabs.length;i++)((ToggleButtonModel)tabs[i].getModel()).setSelected(i==index);clearRows();long request=++loadVersion;loading=true;
        CompletableFuture.supplyAsync(()->destinations.load(REGIONS[index])).whenComplete((entries,error)->Gdx.app.postRunnable(()->{
            if(!isOpen()||tool!=Tool.TELEPORT||request!=loadVersion)return;loading=false;
            if(error!=null){report(error);return;}
            var cells=new ArrayList<Widget>();
            for(var entry:entries){
                if(entry.heading()){
                    if(!cells.isEmpty()){addRow(false,cells.toArray(Widget[]::new));cells.clear();}var heading=new Label(entry.name());heading.setTheme("/teleport-heading");addRow(true,heading);
                }else{
                    var button=new Button(entry.name());button.setTheme("/teleport-destination");button.setClip(true);button.setCanAcceptKeyboardFocus(false);button.addCallback(()->teleport(entry));cells.add(button);
                    if(cells.size()==4){addRow(false,cells.toArray(Widget[]::new));cells.clear();}
                }
            }
            if(!cells.isEmpty())addRow(false,cells.toArray(Widget[]::new));results.invalidateLayout();scroll.invalidateLayout();sizeWindow();
        }));
    }
    private static String normalized(String value){return Normalizer.normalize(value,Normalizer.Form.NFKD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT);}
    private void rebuildSearch(){
        if(tool==null||tool==Tool.TELEPORT||busy||loading)return;clearRows();
        addRow(true,label("Type",true),label("Region",true),label("Id",true),label("",true),label("Name",true));
        String query=normalized(search.getText().strip());int numeric;try{numeric=Integer.parseInt(query);}catch(NumberFormatException ignored){numeric=0;}
        if(tool==Tool.ITEM){
            for(var item:catalogue.get()){
                String name=items.name(item.id());if(!normalized(name).contains(query)&&item.id()!=numeric)continue;
                var add=new Button("▶ ADD"){
                    @Override protected void paintWidget(GUI gui){super.paintWidget(gui);if(getBottom()<scroll.getInnerY()||getY()>scroll.getInnerBottom())return;var icon=items.icon(item.id());if(icon!=null){int size=mobile?32:24;icon.draw(getAnimationState(),getX()+3,getY()+(getHeight()-size)/2,size,size);}}
                };add.setTheme("/database-add");add.addCallback(()->{
                    if(item.maximumStack()>1&&items.template(item.id()).pocket()!=2)amount.show("Heh, how much do you want, cheater?",value->add(item.id(),value));
                    else add(item.id(),1);
                });addRow(false,label("Item",false),label(item.region()==-1?"--":item.region()==10?"Custom":strings.get(250000+item.region()),false),label(Integer.toString(item.id()),false),add,label(name,false));
            }
        }else{
            for(var template:items.speciesTemplates()){
                int id=template.id();String name=items.speciesName(id);if(!normalized(name).contains(query)&&id!=numeric)continue;final int species=id;
                var add=new Button("▶ ADD");add.setTheme("/database-add");add.addCallback(()->amount.show("Whats the level, please?",value->add(species,value)));
                addRow(false,label(strings.get(0),false),label("--",false),label(Integer.toString(id),false),add,label(name,false));
            }
        }
        results.invalidateLayout();
    }
    private Widget label(String text,boolean header){var label=new Label(text);label.setTheme(header?"/database-header":"/database-cell");label.setClip(true);if(text.length()>40&&!text.contains("\n")){label.setText(text.substring(0,40)+" ...");label.setTooltipContent(text.replace("|br|","\n"));}return label;}
    private void addRow(boolean heading,Widget... cells){for(var cell:cells)results.add(cell);rows.add(new Row(cells,heading));}
    private void add(int id,int value){if(!busy)perform(tool,new int[]{id,value});}
    private void teleport(ReferenceTeleportEntries.Entry entry){
        if(busy)return;long request=version;busy=true;
        CompletableFuture.supplyAsync(()->destinations.resolve(entry,direction)).whenComplete((position,error)->Gdx.app.postRunnable(()->{
            if(request!=version)return;busy=false;if(error!=null){report(error);return;}
            perform(Tool.TELEPORT,new int[]{entry.region(),position.bank(),position.map(),position.x(),position.y()});
        }));
    }
    private void perform(Tool tool,int[] values){
        long request=version;busy=true;if(tool!=Tool.TELEPORT)content.setEnabled(false);keyboard.close();CompletionStage<String> operation;
        try{operation=execute.apply(tool,values);}catch(RuntimeException failure){finish(request,null,failure);return;}
        operation.whenComplete((text,error)->Gdx.app.postRunnable(()->finish(request,text,error)));
    }
    private void finish(long request,String result,Throwable error){if(request!=version)return;busy=false;content.setEnabled(true);if(error!=null)report(error);else{if(tool==Tool.TELEPORT&&mobile)closePopup();else if(isOpen())raiseWindow();completed.accept(result);}}
    private void report(Throwable error){while(error.getCause()!=null)error=error.getCause();failed.accept(error.getMessage()==null?error.toString():error.getMessage());}
    private int rowHeight(Row row){int h=tool==Tool.TELEPORT?(mobile?48:30):(mobile?42:28);for(var cell:row.cells)h=Math.max(h,cell.getPreferredHeight());return h;}
    private int resultsHeight(){int h=0;for(var row:rows)h+=rowHeight(row)+5;return h;}
    private int[] teleportColumns(){int[] widths=new int[]{mobile?210:130,mobile?210:130,mobile?210:130,mobile?210:130};for(var row:rows)if(!row.heading)for(int i=0;i<row.cells.length;i++)widths[i]=Math.max(widths[i],row.cells[i].getPreferredWidth());return widths;}
    private void layoutResults(){
        int y=results.getInnerY(),width=results.getInnerWidth();
        int[] columns=tool==Tool.TELEPORT?teleportColumns():null;
        for(var row:rows){
            int h=rowHeight(row);
            if(tool==Tool.TELEPORT){int x=results.getInnerX();for(int i=0;i<row.cells.length;i++){row.cells[i].setPosition(x,y);row.cells[i].setSize(row.heading?width:columns[i],h);x+=columns[i]+5;}}
            else{int[] sizes=mobile?new int[]{95,85,70,120,Math.max(80,width-370)}:new int[]{65,65,55,100,Math.max(80,width-285)};int x=results.getInnerX();for(int i=0;i<row.cells.length;i++){row.cells[i].setPosition(x,y);row.cells[i].setSize(sizes[i],h);x+=sizes[i];}}
            y+=h+5;
        }
    }
    private void layoutContent(){
        int header=mobile?50:32;
        if(tool==Tool.TELEPORT){int width=mobile?180:90,y=content.getY()+(mobile?60:0);for(var tab:tabs){tab.setPosition(content.getX(),y);tab.setSize(width,Math.max(mobile?50:0,tab.getPreferredHeight()));y=tab.getBottom()+5;}scroll.setPosition(content.getX()+width+10,content.getY()+(mobile?60:0));scroll.setSize(Math.max(0,content.getWidth()-width-10),Math.max(0,content.getHeight()-(mobile?60:0)));}
        else{search.setPosition(content.getX(),content.getY());search.setSize(Math.max(0,content.getWidth()-155),header);searchButton.setPosition(search.getRight()+5,content.getY());searchButton.setSize(150,header);scroll.setPosition(content.getX(),search.getBottom()+5);scroll.setSize(content.getWidth(),Math.max(0,content.getHeight()-header-5));}
    }
    @Override public void closePopup(){if(busy)return;loadVersion++;super.closePopup();keyboard.close();amount.closePopup();}
    public void forceClose(){version++;busy=false;closePopup();clearRows();}
    public void closeForTeleport(){if(mobile){boolean pending=busy;busy=false;closePopup();busy=pending;}}
}
