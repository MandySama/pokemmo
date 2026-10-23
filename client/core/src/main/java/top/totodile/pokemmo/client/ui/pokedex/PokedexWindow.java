package top.totodile.pokemmo.client.ui.pokedex;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.input.TextInputWrapper;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import java.io.IOException;
import java.util.Locale;
import java.util.function.Consumer;
import top.totodile.pokemmo.client.resources.*;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.rom.nds.bw.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.common.ClientWindow;
import top.totodile.pokemmo.client.ui.common.MessageDialog;

/** National Pokédex navigation. Data and textures are loaded on first use, on the render thread. */
public final class PokedexWindow extends ClientWindow implements AutoCloseable {
    private final ClientResources resources;
    private final ClientStrings strings;
    private final GdxTwlRenderer renderer;
    private final boolean mobile;
    private final Runnable onClose;
    private final Consumer<Image> mobileBackground;
    private final ResizableFrame frame;
    private final Widget content=new Widget(){@Override protected void layout(){layoutPages();}};
    private final Widget listing=new Widget(){@Override protected void layout(){layoutList();}};
    private final Label national=new Label();
    private final Label tier=new Label();
    private final EditField search;
    private final PokedexScrollPane scroll=new PokedexScrollPane();
    private final Label[] counts=new Label[4];
    private final MessageDialog message;
    private BwPokedex dex;
    private PokedexImages images;
    private PokedexGrid grid;
    private PokedexDetails details;
    private boolean detail;
    private boolean nativeInput,disposed;
    private PokedexScrollPane touchScroll;
    private int lastWidth,lastHeight;

    public PokedexWindow(Widget owner,ClientResources resources,GdxTwlRenderer renderer,boolean mobile,
                         Consumer<Image> mobileBackground,Runnable onClose) {
        super(owner);this.resources=resources;this.renderer=renderer;this.mobile=mobile;
        this.mobileBackground=mobileBackground;this.onClose=onClose;strings=resources.strings();
        frame=new ResizableFrame(){@Override protected void paintBackground(GUI gui){if(mobile)PokedexWindow.this.mobileBackground.accept(getBackground());else super.paintBackground(gui);}};
        frame.setTheme("/monsterdex-frame");frame.setTitle(strings.get(1));
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);frame.setDraggable(false);frame.addCloseCallback(this::closePopup);
        content.setTheme("/pokedex-content");listing.setTheme("");
        national.setTheme("/pokedex-filter");tier.setTheme("/pokedex-filter");tier.setText(strings.get(5749));
        search=new EditField(){
            @Override public boolean handleEvent(Event event) {
                boolean handled=super.handleEvent(event);
                if(mobile&&event.getType()==Event.Type.MOUSE_BTNDOWN)showKeyboard();
                return handled;
            }
        };
        search.setTheme("/pokedex-search");search.setMaxTextLength(40);
        search.addCallback(key->{if(grid!=null){scroll.cancelTouch();grid.filter(search.getText());scroll.setScrollPositionY(0);}});
        scroll.setTheme("/pokedex-scroll");scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        listing.add(national);listing.add(search);listing.add(tier);listing.add(scroll);
        int[] labels={1701,1702,1728,1762};
        for(int i=0;i<counts.length;i++){counts[i]=new Label(strings.get(labels[i])+" 0 / "+BwPokedex.LAST_SPECIES);counts[i].setTheme("/pokedex-counter");listing.add(counts[i]);}
        content.add(listing);frame.add(content);add(frame);
        message=new MessageDialog(owner,strings,mobile,mobileBackground,onClose);
    }
    public boolean open() {
        if(isOpen()){raiseWindow();return true;}
        if(dex==null) {
            var rom=resources.roms().get(RomType.BLACK_WHITE);
            if(rom==null){message.show(strings.format(1186,strings.get(92)));return false;}
            try {
                dex=new BwPokedex(rom,strings);images=new PokedexImages(new BwPokemonSprites(dex),renderer);
                grid=new PokedexGrid(dex,images,mobile,this::select);scroll.setContent(grid);
                details=new PokedexDetails(dex,images,strings,mobile,this::select,this::back);content.add(details);
                // The reference starts on the National catalogue. Region/tier metadata is server supplied.
                national.setText(dex.nationalTitle());
            }catch(IOException|RuntimeException error){
                if(images!=null){images.close();images=null;}dex=null;
                java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.WARNING,"Cannot load Pokédex",error);
                message.show(strings.get(2103));return false;
            }
        }
        detail=false;listing.setVisible(true);details.setVisible(false);frame.setTitle(strings.get(1));
        if(!openPopup())return false;
        positionWindow();frame.setHardVisible(true);requestKeyboardFocus();return true;
    }
    public boolean editingSearch(){return isOpen()&&!detail&&search.hasKeyboardFocus();}
    public boolean showingMessage(){return message.isOpen();}
    public void back() {
        if(message.isOpen()){message.closePopup();return;}
        if(detail){detail=false;listing.setVisible(true);details.setVisible(false);frame.setTitle(strings.get(1));requestKeyboardFocus();invalidateLayout();}
        else closePopup();
    }
    private void select(int id) {
        detail=true;listing.setVisible(false);details.show(id);details.setVisible(true);
        frame.setTitle(String.format(Locale.ROOT,"%s %03d %s",strings.get(1705),id,dex.name(id)));
        hideKeyboard();
        requestKeyboardFocus();invalidateLayout();
    }
    @Override public void closePopup(){boolean open=isOpen();cancelTouch();hideKeyboard();super.closePopup();if(open)onClose.run();}
    public void touchDown(int x,int y){
        if(!mobile||!isOpen())return;
        var pane=detail?details.scrollPane():scroll;
        if(pane.isInside(x,y)&&!pane.getVerticalScrollbar().isInside(x,y)){touchScroll=pane;pane.beginTouch(x,y);}
    }
    public boolean touchDragged(int x,int y){return touchScroll!=null&&touchScroll.drag(x,y);}
    public boolean touchUp(int x,int y){var pane=touchScroll;touchScroll=null;return pane!=null&&pane.endTouch(x,y);}
    public void cancelTouch(){if(touchScroll!=null)touchScroll.cancelTouch();touchScroll=null;}
    private void showKeyboard(){
        if(!mobile||nativeInput||disposed||Gdx.app.getType()!=ApplicationType.Android)return;
        nativeInput=true;String initial=search.getText();int cursor=search.getCursorPos();
        Gdx.input.openTextInputField(new NativeInputConfiguration().setType(Input.OnscreenKeyboardType.Default)
                .setPreventCorrection(true).setMultiLine(false).setMaxLength(40).setPlaceholder(strings.get(1716))
                .setTextInputWrapper(new TextInputWrapper(){
                    public String getText(){return initial;}
                    public int getSelectionStart(){return cursor;}
                    public int getSelectionEnd(){return cursor;}
                    public void writeResults(String text,int start,int end){if(!disposed){search.setText(text);search.setSelection(Math.min(start,text.length()),Math.min(end,text.length()));}}
                }).setCloseCallback(confirm->{nativeInput=false;return false;}));
    }
    private void hideKeyboard(){if(nativeInput){Gdx.input.closeTextInputField(false);nativeInput=false;}}
    public void positionWindow() {
        message.positionWindow();if(!isOpen())return;
        var owner=getOwner();int w=owner.getInnerWidth(),h=owner.getInnerHeight();
        setPosition(owner.getInnerX(),owner.getInnerY());setSize(w,h);
        if(w!=lastWidth||h!=lastHeight){lastWidth=w;lastHeight=h;invalidateLayout();}
    }
    @Override protected void layout(){
        int w=mobile?getInnerWidth():Math.min(670,getInnerWidth()),h=mobile?getInnerHeight():Math.min(510,getInnerHeight());
        frame.setSize(w,h);frame.setPosition(getInnerX()+(getInnerWidth()-w)/2,getInnerY()+(getInnerHeight()-h)/2);
    }
    private void layoutPages(){
        int top=mobile?65:38,bottom=mobile?25:8;
        int x=content.getInnerX()+10,y=content.getInnerY()+top,w=Math.max(0,content.getInnerWidth()-20),h=Math.max(0,content.getInnerHeight()-top-bottom);
        listing.setPosition(x,y);listing.setSize(w,h);
        if(details!=null){details.setPosition(x,y);details.setSize(w,h);}
    }
    private void layoutList(){
        int x=listing.getInnerX(),y=listing.getInnerY(),w=listing.getInnerWidth(),h=listing.getInnerHeight();
        int bar=mobile?44:28,left=Math.min(mobile?230:195,w/3),right=Math.min(mobile?200:140,w/4),footer=mobile?72:52;
        national.setPosition(x,y);national.setSize(left,bar);
        search.setPosition(x+left+5,y);search.setSize(Math.max(30,w-left-right-10),bar);
        tier.setPosition(x+w-right,y);tier.setSize(right,bar);
        scroll.setPosition(x,y+bar+7);scroll.setSize(w,Math.max(0,h-bar-footer-12));
        for(int i=0;i<4;i++){counts[i].setPosition(x+(i%2)*(w/2),y+h-footer+(i/2)*(footer/2));counts[i].setSize(w/2,footer/2);}
    }
    @Override public void close(){disposed=true;closePopup();message.closePopup();if(images!=null)images.close();}
}
