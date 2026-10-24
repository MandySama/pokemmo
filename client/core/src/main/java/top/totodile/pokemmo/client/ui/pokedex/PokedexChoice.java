package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleListModel;

/** Same themed combobox/list popup as the character and settings choices. */
final class PokedexChoice extends Button {
    private final String[] options;
    private final Label display=new Label(getAnimationState()) {
        @Override protected boolean handleEvent(Event event){return false;}
    };
    private final Widget arrow=new Widget(getAnimationState());
    private final PopupWindow popup=new PopupWindow(this);
    private final Runnable changed;
    private int selected;
    PokedexChoice(Runnable changed,String... options) {
        this.options=options;this.changed=changed;setTheme("/monsterdex-frame.dialoglayout.combobox");setCanAcceptKeyboardFocus(false);
        display.setTheme("display");display.setClip(true);arrow.setTheme("button");add(display);add(arrow);
        popup.setTheme("/comboboxPopup");select(0);addCallback(this::openChoices);
    }
    int selected(){return selected;}
    void select(int index){selected=Math.max(0,Math.min(options.length-1,index));display.setText(options[selected]);}
    boolean closeChoices(){if(!popup.isOpen())return false;popup.closePopup();return true;}
    @Override protected void paintWidget(GUI gui){}
    @Override protected void layout(){
        int w=arrow.getPreferredWidth();arrow.setSize(w,getInnerHeight());arrow.setPosition(getInnerRight()-w,getInnerY());
        display.setSize(Math.max(0,getInnerWidth()-w),getInnerHeight());display.setPosition(getInnerX(),getInnerY());
    }
    private void openChoices(){
        if(closeChoices())return;
        ListBox<String> list=new ListBox<>(new SimpleListModel<String>(){
            public int getNumEntries(){return options.length;}
            public String getEntry(int i){return options[i];}
        }){
            @Override protected ListBoxDisplay createDisplay(){return new ListBoxLabel(){
                @Override protected boolean handleListBoxEvent(Event event){
                    if(event.getType()==Event.Type.MOUSE_BTNUP&&event.getMouseButton()==Event.MOUSE_LBUTTON){doListBoxCallback(CallbackReason.MOUSE_DOUBLE_CLICK);return true;}
                    return super.handleListBoxEvent(event);
                }
            };}
        };
        list.setTheme("listbox");list.setSelected(selected);list.addCallback(reason->{
            if(reason.actionRequested()&&list.getSelected()>=0){select(list.getSelected());popup.closePopup();changed.run();}
        });
        popup.removeAllChildren();popup.add(list);
        if(popup.openPopup()){
            int height=Math.min(list.getMaxHeight(),options.length*list.getCellHeight()+list.getBorderVertical());
            int below=getGUI().getHeight()-getBottom();boolean down=below>=height||below>=getY();
            popup.setSize(getWidth(),Math.max(1,Math.min(height,down?below:getY())));
            popup.setPosition(Math.max(0,Math.min(getX(),getGUI().getWidth()-popup.getWidth())),down?getBottom():Math.max(0,getY()-popup.getHeight()));list.requestKeyboardFocus();
        }
    }
}
