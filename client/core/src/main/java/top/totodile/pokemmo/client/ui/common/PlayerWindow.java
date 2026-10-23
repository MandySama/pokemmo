package top.totodile.pokemmo.client.ui.common;

import de.matthiasmann.twl.*;

/** Shared host for the reference's regular draggable PC / full-screen Android frames. */
public abstract class PlayerWindow extends ClientWindow {
    protected final ResizableFrame frame=new ResizableFrame(){
        @Override protected void paintBackground(GUI gui){
            if(mobile&&fullScreen()&&gui.getRenderer() instanceof top.totodile.pokemmo.client.twl.GdxTwlRenderer renderer)renderer.drawScreenBackground(getBackground(),getAnimationState());
            else super.paintBackground(gui);
        }
    };
    protected final boolean mobile;
    private final int preferredWidth,preferredHeight;
    private final Runnable onClose;
    private int width=-1,height=-1;
    protected PlayerWindow(Widget owner,boolean mobile,String theme,int preferredWidth,int preferredHeight,Runnable onClose){
        super(owner);this.mobile=mobile;this.preferredWidth=preferredWidth;this.preferredHeight=preferredHeight;this.onClose=onClose;
        frame.setTheme(theme);frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);frame.setDraggable(!mobile);frame.addCloseCallback(this::closePopup);add(frame);
    }
    public void open(){
        if(isOpen()){raiseWindow();return;}
        if(openPopup()){width=-1;positionWindow();frame.setHardVisible(true);refresh();}
    }
    protected abstract void refresh();
    protected boolean fullScreen(){return mobile;}
    public void positionWindow(){
        if(!isOpen())return;
        var owner=getOwner();setPosition(owner.getInnerX(),owner.getInnerY());setSize(owner.getInnerWidth(),owner.getInnerHeight());
        if(width!=getWidth()||height!=getHeight()){
            width=getWidth();height=getHeight();
            int w=fullScreen()?width:Math.min(width,preferredWidth),h=fullScreen()?height:Math.min(height,preferredHeight);
            frame.setSize(w,h);frame.setPosition(getX()+(width-w)/2,getY()+(height-h)/2);
        }
    }
    @Override protected void layout(){positionWindow();}
    @Override public void closePopup(){boolean open=isOpen();super.closePopup();if(open)onClose.run();}
}
