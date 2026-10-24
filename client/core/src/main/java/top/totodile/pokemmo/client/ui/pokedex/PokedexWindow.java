package top.totodile.pokemmo.client.ui.pokedex;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.input.TextInputWrapper;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.ToggleButtonModel;
import de.matthiasmann.twl.renderer.Image;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import top.totodile.pokemmo.client.resources.*;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.rom.nds.bw.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.common.ClientWindow;
import top.totodile.pokemmo.client.ui.common.MessageDialog;

/** Catalogue, detail and horde windows follow the official desktop navigation; Android uses full-screen panels. */
public final class PokedexWindow extends ClientWindow implements AutoCloseable {
    private final ClientResources resources;
    private final ClientStrings strings;
    private final GdxTwlRenderer renderer;
    private final boolean mobile;
    private final Runnable onClose;
    private final Consumer<Image> mobileBackground;
    private final ResizableFrame frame,detailFrame,hordeFrame;
    private final Widget content=new Widget(){@Override protected void layout(){layoutCatalogue();}};
    private final Widget detailContent=new Widget(){@Override protected void layout(){layoutDetail();}};
    private final Widget hordeContent=new Widget(){@Override protected void layout(){layoutHordes();}};
    private final Widget listing=new Widget(){@Override protected void layout(){layoutList();}};
    private final Widget body=new Widget();
    private final DialogLayout counters=new DialogLayout();
    private PokedexChoice national,tier;
    private final EditField search;
    private final Button advancedButton,known,horde;
    private final PokedexScrollPane scroll=new PokedexScrollPane();
    private final Counter[] counts=new Counter[6];
    private final MessageDialog message;
    private final PokedexFilter filter=new PokedexFilter();
    private final List<Integer> regionChoices=new ArrayList<>();
    private BwPokedex dex;
    private byte[] tierData=new byte[0];
    private PokedexImages images;
    private PokedexGrid grid;
    private PokedexDetails details;
    private Set<Integer> ownedSpecies=Set.of();
    private PokedexAdvanced advancedPage;
    private PokedexHordes hordes;
    private boolean detail,advanced,nativeInput,disposed;
    private EditField nativeField;
    private PokedexScrollPane touchScroll;
    private int lastWidth,lastHeight;
    private boolean cataloguePlaced,detailPlaced,hordesPlaced;

    public PokedexWindow(Widget owner,ClientResources resources,GdxTwlRenderer renderer,boolean mobile,
                         Consumer<Image> mobileBackground,Runnable onClose) {
        super(owner);this.resources=resources;this.renderer=renderer;this.mobile=mobile;
        this.mobileBackground=mobileBackground;this.onClose=onClose;strings=resources.strings();
        frame=frame(strings.get(1),this::closeCatalogue,"/monsterdex-frame");
        detailFrame=frame("",this::closeDetails,"/monsterdex-frame");detailFrame.setVisible(false);
        hordeFrame=frame(strings.get(6481),()->{hordeFrameVisible(false);if(!frame.isVisible()&&!detailFrame.isVisible())closePopup();},"/monsterdex-frame");hordeFrame.setVisible(false);
        content.setTheme("");detailContent.setTheme("");hordeContent.setTheme("");listing.setTheme("");body.setTheme(mobile?"/monsterdex-box":"/monsterdex-frame.dialoglayout");
        scroll.setTheme("/monsterdex-frame.dialoglayout.scrollpane");
        search=field(false);search.setTheme("/editfield-search");search.setMaxTextLength(40);
        search.addCallback(key->{filter.query=search.getText();refresh();});
        advancedButton=new Button(strings.get(8010));advancedButton.setTheme("/monsterdex-frame.tabbedpane.container.dialoglayout.button");advancedButton.setCanAcceptKeyboardFocus(false);advancedButton.addCallback(this::showAdvanced);
        known=atlasButton("map_icon",24,24);
        known.setTheme("/monsterdex-frame.tabbedpane.container.dialoglayout.button");known.setModel(new ToggleButtonModel());known.setCanAcceptKeyboardFocus(false);known.setTooltipContent(strings.get(1700));
        known.addCallback(()->{filter.knownOnly=((ToggleButtonModel)known.getModel()).isSelected();refresh();});
        horde=atlasButton("horde_5x",24,24);
        horde.setTheme("/monsterdex-frame.tabbedpane.container.dialoglayout.button");horde.setCanAcceptKeyboardFocus(false);horde.setTooltipContent(strings.get(6481));horde.addCallback(this::showHordes);
        listing.add(body);listing.add(search);listing.add(advancedButton);listing.add(known);listing.add(horde);body.add(scroll);
        int[] labels={1701,1702,1728,1762,6472,6473};
        counters.setTheme("/pokedex-counters");counters.setAddDefaultGaps(false);
        for(int i=0;i<counts.length;i++)counts[i]=new Counter(strings.get(labels[i]),i);
        var columns=counters.createSequentialGroup();
        for(int i=0;i<4;i++){
            if(i>0)columns.addGap(DialogLayout.DEFAULT_GAP);
            var column=counters.createParallelGroup(counts[i]);if(i+4<counts.length)column.addWidget(counts[i+4]);columns.addGroup(column);
        }
        counters.setHorizontalGroup(columns);
        counters.setVerticalGroup(counters.createSequentialGroup().addGroup(counters.createParallelGroup(counts[0],counts[1],counts[2],counts[3])).addGap(DialogLayout.DEFAULT_GAP).addGroup(counters.createParallelGroup(counts[4],counts[5])));
        body.add(counters);
        content.add(listing);frame.add(content);detailFrame.add(detailContent);hordeFrame.add(hordeContent);
        add(frame);add(detailFrame);add(hordeFrame);
        message=new MessageDialog(owner,strings,mobile,mobileBackground,onClose);
    }
    private ResizableFrame frame(String title,Runnable close,String theme){
        var window=new ResizableFrame(){@Override protected void paintBackground(GUI gui){if(mobile)mobileBackground.accept(getBackground());else super.paintBackground(gui);}};
        window.setTheme(theme);window.setTitle(title);window.setResizableAxis(ResizableFrame.ResizableAxis.NONE);window.setDraggable(!mobile);window.addCloseCallback(close);return window;
    }
    private Button atlasButton(String name,int width,int height){
        return new Button(){@Override protected void paintWidget(GUI gui){
            super.paintWidget(gui);if(images==null)return;
            Image icon=images.atlas(name);if(icon==null)return;
            int scale=mobile?2:1,w=width*scale,h=height*scale;
            icon.draw(getAnimationState(),getX()+(getWidth()-w)/2,getY()+(getHeight()-h)/2,w,h);
        }};
    }
    public boolean open(){
        if(isOpen()){frame.setVisible(true);raiseWindow();return true;}
        if(dex==null){
            var rom=resources.roms().get(RomType.BLACK_WHITE);
            if(rom==null){message.show(strings.format(1186,strings.get(92)));return false;}
            try{
                dex=new BwPokedex(rom,strings);dex.loadDataPackage(resources.apply("data.pak"));dex.loadTiers(tierData);
                images=new PokedexImages(new BwPokemonSprites(dex),renderer,resources);
                grid=new PokedexGrid(dex,images,mobile,this::select);scroll.setContent(grid);
                details=new PokedexDetails(dex,images,strings,mobile,this::select,this::back);detailContent.add(details);
                details.setUnlocked(filter.state.unlocked());
                advancedPage=new PokedexAdvanced(dex,strings,mobile,filter,this::field,()->{
                    advanced=false;advancedPage.setVisible(false);listing.setVisible(true);search.setText(filter.query);hideKeyboard();refresh();invalidateLayout();
                });advancedPage.setVisible(false);content.add(advancedPage);
                hordes=new PokedexHordes(dex,images,strings,mobile,this::select);hordeContent.add(hordes);
                var options=new ArrayList<String>();options.add(dex.nationalTitle());regionChoices.add(-1);
                for(int region=0;region<5;region++){final int id=region;if(dex.all().stream().anyMatch(s->dex.number(s.id(),id)>0)){
                    regionChoices.add(region);options.add(dex.regionName(region)+" "+strings.get(1));
                }}
                national=new PokedexChoice(()->{filter.region=regionChoices.get(national.selected());refresh();},options.toArray(String[]::new));listing.add(national);
                // Reference N2.HP excludes the disabled category 4 via N2.qY.
                tier=new PokedexChoice(()->{filter.tier=tier.selected()==5?5:tier.selected()-1;refresh();},strings.get(5749),strings.get(5750),strings.get(5751),strings.get(5752),strings.get(5753),strings.get(5755));listing.add(tier);
            }catch(IOException|RuntimeException error){
                if(images!=null){images.close();images=null;}dex=null;
                java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.WARNING,"Cannot load Pokédex",error);message.show(strings.get(2103));return false;
            }
        }
        detail=false;advanced=false;frame.setHardVisible(true);detailFrame.setHardVisible(false);hordeFrame.setHardVisible(false);listing.setVisible(true);advancedPage.setVisible(false);
        refresh();if(!openPopup())return false;positionWindow();frame.setHardVisible(true);requestKeyboardFocus();return true;
    }
    public void setTierData(byte[] payload){tierData=payload.clone();}
    private void refresh(){
        if(grid==null)return;scroll.cancelTouch();grid.filter(filter);scroll.setScrollPositionY(0);
        long eligible=dex.all().stream().filter(s->filter.region<0||dex.number(s.id(),filter.region)>0).count();
        for(int i=0;i<counts.length;i++){final int type=i;long count=i>=4?0:dex.all().stream().filter(s->filter.region<0||dex.number(s.id(),filter.region)>0).filter(s->filter.state.has(s.id(),type)).count();counts[i].value(count,i>=4?0:eligible);}
    }
    /** Display the active character's persisted all-unlocked / empty local dex state. */
    public void setUnlocked(boolean value){
        setState(new top.totodile.pokemmo.server.game.GameService.PokedexState(value));
    }
    public void setState(top.totodile.pokemmo.server.game.GameService.PokedexState state){
        if(filter.state.equals(state))return;filter.state=state;
        if(details!=null)details.setUnlocked(state.has(details.speciesId(),0)||ownedSpecies.contains(details.speciesId()));
        if(detail)detailFrame.setTitle(String.format(Locale.ROOT,"%s %03d %s",strings.get(1705),details.speciesId(),state.has(details.speciesId(),0)||ownedSpecies.contains(details.speciesId())?dex.name(details.speciesId()):"???"));
        refresh();
    }
    public void setOwnedSpecies(Set<Integer> ids){ownedSpecies=Set.copyOf(ids);if(detail)select(details.speciesId());}
    public boolean editingSearch(){return isOpen()&&editing(this);}
    private boolean editing(Widget parent){if(!parent.isVisible()||parent!=this&&!parent.hasKeyboardFocus())return false;if(parent instanceof EditField)return true;for(int i=0;i<parent.getNumChildren();i++)if(editing(parent.getChild(i)))return true;return false;}
    public boolean showingMessage(){return message.isOpen();}
    public void back(){
        if(message.isOpen()){message.closePopup();return;}
        if(national!=null&&national.closeChoices()||tier!=null&&tier.closeChoices()||advancedPage!=null&&advancedPage.closeChoices())return;
        if(hordeFrame.isVisible()){hordeFrameVisible(false);return;}
        if(detail){closeDetails();return;}
        if(advanced){advanced=false;advancedPage.setVisible(false);listing.setVisible(true);hideKeyboard();invalidateLayout();return;}
        closePopup();
    }
    private void select(int id){
        detail=true;details.setUnlocked(filter.state.has(id,0)||ownedSpecies.contains(id));details.show(id);detailFrame.setTitle(String.format(Locale.ROOT,"%s %03d %s",strings.get(1705),id,filter.state.has(id,0)||ownedSpecies.contains(id)?dex.name(id):"???"));
        detailFrame.setVisible(true);removeChild(detailFrame);add(detailFrame);hideKeyboard();requestKeyboardFocus(null);requestKeyboardFocus();invalidateLayout();
    }
    public void showSpecies(int id){if(open())select(id);}
    private void closeDetails(){detail=false;detailFrame.setHardVisible(false);if(mobile)frame.setHardVisible(true);if(!frame.isVisible()&&!hordeFrame.isVisible())closePopup();else{requestKeyboardFocus(null);requestKeyboardFocus();}}
    private void closeCatalogue(){frame.setHardVisible(false);if(!detailFrame.isVisible()&&!hordeFrame.isVisible())closePopup();}
    private void showAdvanced(){advanced=true;advancedPage.query(search.getText());listing.setVisible(false);advancedPage.setVisible(true);hideKeyboard();invalidateLayout();}
    private void showHordes(){hordeFrameVisible(true);removeChild(hordeFrame);add(hordeFrame);invalidateLayout();}
    private void hordeFrameVisible(boolean value){hordeFrame.setHardVisible(value);invalidateLayout();}
    @Override public void closePopup(){
        boolean open=isOpen();cancelTouch();hideKeyboard();if(national!=null)national.closeChoices();if(tier!=null)tier.closeChoices();
        super.closePopup();if(open)onClose.run();
    }
    public void touchDown(int x,int y){
        if(!mobile||!isOpen())return;
        var pane=hordeFrame.isVisible()?hordes.scrollPane():detail?details.scrollPane():advanced?null:scroll;
        if(pane!=null&&pane.isInside(x,y)&&!pane.getVerticalScrollbar().isInside(x,y)){touchScroll=pane;pane.beginTouch(x,y);}
    }
    public boolean touchDragged(int x,int y){return touchScroll!=null&&touchScroll.drag(x,y);}
    public boolean touchUp(int x,int y){var pane=touchScroll;touchScroll=null;return pane!=null&&pane.endTouch(x,y);}
    public void cancelTouch(){if(touchScroll!=null)touchScroll.cancelTouch();touchScroll=null;}
    private EditField field(boolean number){
        return new EditField(){@Override public boolean handleEvent(Event event){boolean result=super.handleEvent(event);
            if(mobile&&event.getType()==Event.Type.MOUSE_BTNDOWN)showKeyboard(this,number);return result;}};
    }
    private void showKeyboard(EditField field,boolean number){
        if(!mobile||disposed||Gdx.app.getType()!=ApplicationType.Android||nativeField==field)return;
        if(nativeInput){Gdx.input.closeTextInputField(false,confirmed->{showKeyboard(field,number);return true;});return;}
        nativeInput=true;nativeField=field;String initial=field.getText();int cursor=field.getCursorPos();
        Gdx.input.openTextInputField(new NativeInputConfiguration().setType(number?Input.OnscreenKeyboardType.NumberPad:Input.OnscreenKeyboardType.Default)
            .setPreventCorrection(true).setMultiLine(false).setMaxLength(number?3:40)
            .setTextInputWrapper(new TextInputWrapper(){
                public String getText(){return initial;}public int getSelectionStart(){return cursor;}public int getSelectionEnd(){return cursor;}
                public void writeResults(String text,int start,int end){if(!disposed){field.setText(text);field.setSelection(Math.min(start,text.length()),Math.min(end,text.length()));}}
            }).setCloseCallback(confirm->{nativeInput=false;nativeField=null;return false;}));
    }
    private void hideKeyboard(){if(nativeInput){Gdx.input.closeTextInputField(false);nativeInput=false;nativeField=null;}}
    public void positionWindow(){
        message.positionWindow();if(!isOpen())return;var owner=getOwner();int w=owner.getInnerWidth(),h=owner.getInnerHeight();
        setPosition(owner.getInnerX(),owner.getInnerY());setSize(w,h);
        if(w!=lastWidth||h!=lastHeight){lastWidth=w;lastHeight=h;cataloguePlaced=false;detailPlaced=false;hordesPlaced=false;invalidateLayout();}
    }
    @Override protected void layout(){
        int w=mobile?getInnerWidth():Math.min(advanced?715:645,getInnerWidth()),h=mobile?getInnerHeight():Math.min(advanced?440:410,getInnerHeight());
        frame.setSize(w,h);if(!cataloguePlaced){frame.setPosition(getInnerX()+(getInnerWidth()-w)/2,getInnerY()+(getInnerHeight()-h)/2);cataloguePlaced=true;}
        int dw=mobile?getInnerWidth():Math.min(670,getInnerWidth()),dh=mobile?getInnerHeight():Math.min(Math.max(460,details==null?0:details.preferredInformationHeight()+40),getInnerHeight());
        detailFrame.setSize(dw,dh);if(!detailPlaced){detailFrame.setPosition(getInnerX()+(getInnerWidth()-dw)/2,getInnerY()+(getInnerHeight()-dh)/2);detailPlaced=true;}
        int hw=mobile?getInnerWidth():Math.min(640,getInnerWidth());
        hordeFrame.setSize(hw,mobile?getInnerHeight():Math.min(480,getInnerHeight()));if(!hordesPlaced){hordeFrame.setPosition(getInnerX()+(getInnerWidth()-hw)/2,getInnerY()+(getInnerHeight()-hordeFrame.getHeight())/2);hordesPlaced=true;}
        for(Widget child:List.of(frame,detailFrame,hordeFrame))child.setPosition(Math.max(getInnerX(),Math.min(child.getX(),getInnerRight()-child.getWidth())),Math.max(getInnerY(),Math.min(child.getY(),getInnerBottom()-child.getHeight())));
        if(mobile){frame.setHardVisible(!detail&&!hordeFrame.isVisible());detailFrame.setHardVisible(detail&&!hordeFrame.isVisible());}
    }
    private void layoutCatalogue(){
        int top=mobile?65:33,x=content.getInnerX()+(mobile?8:15),y=content.getInnerY()+top,w=content.getInnerWidth()-(mobile?16:25),h=content.getInnerHeight()-top-(mobile?8:4);
        listing.setPosition(x,y);listing.setSize(Math.max(0,w),Math.max(0,h));
        if(advancedPage!=null){advancedPage.setPosition(x,y);advancedPage.setSize(Math.max(0,w),Math.max(0,h));}
    }
    private void layoutDetail(){int top=mobile?60:28;details.setPosition(detailContent.getInnerX()+10,detailContent.getInnerY()+top);details.setSize(Math.max(1,detailContent.getInnerWidth()-20),Math.max(1,detailContent.getInnerHeight()-top-12));}
    private void layoutHordes(){if(hordes==null)return;int top=mobile?60:27;hordes.setPosition(hordeContent.getInnerX()+10,hordeContent.getInnerY()+top);hordes.setSize(Math.max(1,hordeContent.getInnerWidth()-20),Math.max(1,hordeContent.getInnerHeight()-top-12));}
    private void layoutList(){
        if(national==null)return;int x=listing.getInnerX(),y=listing.getInnerY(),w=listing.getInnerWidth(),h=listing.getInnerHeight();
        int bar=mobile?44:32,gap=mobile?4:7,searchGap=mobile?4:5,toolbarY=mobile?y:y-1;
        int nationalWidth=mobile?220:150,tierWidth=mobile?150:100,button=mobile?42:30,advancedWidth=mobile?120:69;
        national.setSize(nationalWidth,bar);national.setPosition(x,toolbarY);tier.setSize(tierWidth,bar);tier.setPosition(x+w-tierWidth,toolbarY);
        horde.setSize(button,bar);horde.setPosition(tier.getX()-button-gap,toolbarY);known.setSize(button,bar);known.setPosition(horde.getX()-button-gap,toolbarY);
        advancedButton.setSize(advancedWidth,bar);advancedButton.setPosition(known.getX()-advancedWidth-gap,toolbarY);
        search.setPosition(national.getRight()+searchGap,toolbarY);search.setSize(Math.max(1,advancedButton.getX()-search.getX()-searchGap),bar);
        int footer=counters.getPreferredHeight(),scrollY=y+bar+(mobile?15:10);
        // On desktop the inset panel encloses the toolbar, cards and both counter rows.
        body.setPosition(mobile?x:x-5,mobile?y+bar+5:y-6);
        body.setSize(mobile?w:w+10,Math.max(0,mobile?h-bar-5:h+6));
        int available=Math.max(0,body.getBottom()-footer-10-scrollY);
        if(!mobile)available=Math.max(0,(available+5)/51*51-5);
        scroll.setPosition(x,scrollY-(mobile?0:1));scroll.setSize(w,available);
        counters.setPosition(x+(mobile?0:2),scroll.getBottom()+(mobile?5:10));counters.setSize(Math.max(0,w-2),footer);
    }
    private final class Counter extends Label {
        private Image background,fill;
        private final String label;private float progress;
        Counter(String text,int index){super(text);this.label=text.replaceAll("[:：]$","");setTheme("/monsterdex-frame.dialoglayout.progressbar-"+new String[]{"white","blue","green","pink","gold","orange"}[index]);}
        void value(long current,long total){progress=total==0?0:(float)current/total;setText(label+": "+current+" / "+total);}
        @Override protected void applyTheme(ThemeInfo info){super.applyTheme(info);background=info.getImage("background");fill=info.getImage("progressImage");}
        @Override protected void paintBackground(GUI gui) { /* The progress track occupies the strip below the label. */ }
        @Override protected void paintWidget(GUI gui){super.paintWidget(gui);
            int y=getBottom()-8;if(background!=null)background.draw(getAnimationState(),getX(),y,getWidth(),8);
            if(fill!=null&&progress>0)fill.draw(getAnimationState(),getX(),y,Math.round(getWidth()*Math.min(1,progress)),8);
        }
    }
    @Override public void close(){disposed=true;closePopup();message.closePopup();if(images!=null)images.close();}
}
