package top.totodile.pokemmo.client.ui.pokedex;

import com.badlogic.gdx.input.GestureDetector;
import de.matthiasmann.twl.*;

/** The same mobile pan/fling and press cancellation used by the settings window. */
final class PokedexScrollPane extends ScrollPane {
    private boolean touching,panning;
    private double position,velocity,remaining;
    private long flingTime;
    private final GestureDetector gesture=new GestureDetector(20,.4f,1.1f,.15f,new GestureDetector.GestureAdapter(){
        @Override public boolean pan(float x,float y,float dx,float dy){
            if(!panning){panning=true;cancelPress(getContent());getGUI().handleMouse(-1,-1,Event.MOUSE_LBUTTON,false);cancelPress(getContent());}
            position=Math.max(0,Math.min(getVerticalScrollbar().getMaxValue(),position-dy));setScrollPositionY((int)Math.round(position));return true;
        }
        @Override public boolean fling(float vx,float vy,int button){
            if(Math.abs(vy)>150){velocity=-vy;remaining=.5;position=getScrollPositionY();flingTime=System.nanoTime();}return true;
        }
    });
    PokedexScrollPane(){setTheme("/monsterdex-frame.tabbedpane.container.scrollpane");setFixed(Fixed.HORIZONTAL);setExpandContentSize(true);}
    void beginTouch(int x,int y){touching=true;panning=false;remaining=0;position=getScrollPositionY();gesture.reset();gesture.touchDown(x,y,0,0);}
    boolean drag(int x,int y){if(!touching)return false;gesture.touchDragged(x,y,0);return panning;}
    boolean endTouch(int x,int y){if(!touching)return false;touching=false;gesture.touchUp(x,y,0,0);return panning;}
    void cancelTouch(){touching=false;panning=false;remaining=0;gesture.reset();}
    private static void cancelPress(Widget widget){
        if(widget instanceof Button button){button.getModel().setArmed(false);button.getModel().setPressed(false);button.getModel().setHover(false);}
        for(int i=0;i<widget.getNumChildren();i++)cancelPress(widget.getChild(i));
    }
    @Override protected void beforeRemoveFromGUI(GUI gui){cancelTouch();super.beforeRemoveFromGUI(gui);}
    @Override protected void paint(GUI gui){
        if(remaining>0){long now=System.nanoTime();double elapsed=Math.min(remaining,(now-flingTime)/1_000_000_000.0);
            position+=velocity*(remaining*elapsed-elapsed*elapsed/2)/.5;remaining-=elapsed;flingTime=now;
            position=Math.max(0,Math.min(getVerticalScrollbar().getMaxValue(),position));setScrollPositionY((int)Math.round(position));}
        super.paint(gui);
    }
}
