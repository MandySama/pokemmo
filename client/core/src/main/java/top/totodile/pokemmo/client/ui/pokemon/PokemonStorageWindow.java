package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.ToggleButtonModel;
import java.util.*;
import java.util.stream.Collectors;
import top.totodile.pokemmo.client.ui.common.*;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;
import static top.totodile.pokemmo.server.game.PokemonData.*;

/** Reference PokemonStorageWindow: preview, box strip, ten-column grid, party and search. */
public final class PokemonStorageWindow extends PlayerWindow {
    private final PokemonController controller;
    private final Widget content=new Widget(){@Override protected void layout(){layoutPc();}};
    private final Widget preview=new Widget();
    private final Widget portrait=new Widget(){@Override protected void paintWidget(GUI gui){
        var p=selected();if(p==null||p.egg()||controller.images()==null)return;
        var image=controller.images().animatedFront(p.species(),p.form(),p.shiny(),System.nanoTime()/1e9);
        int w=image.getWidth()*2,h=image.getHeight()*2;
        image.draw(getAnimationState(),getX()+(getWidth()-w)/2,getY()+(getHeight()-h)/2-10,w,h);
    }};
    private final Button title=new Button();
    private final Label name=new Label(),level=new Label(),slotNumber=new Label();
    private final Label[] headings=new Label[7],values=new Label[7],moves=new Label[4];
    private final Button[] boxes=new Button[BASE_BOXES+2];
    private final PokemonSlot[] cells=new PokemonSlot[BOX_SIZE];
    private final Widget grid=new Widget(){
        @Override public int getPreferredHeight(){return 6*((mobile?80:48)+3)-3;}
        @Override protected void layout(){layoutGrid();}
    };
    private final ScrollPane gridScroll;
    private final PartyPanel party;
    private final PlayerTextInput keyboard;
    private final EditField search;
    private final Button sort,reset;
    private final PopupWindow sortPopup;
    private final ToggleButton levelOrder=new ToggleButton();
    private final PopupWindow rename;
    private final EditField boxName;
    private final Button saveName,cancelName;
    private int box,selectedContainer=-1,selectedPosition=-1;
    public PokemonStorageWindow(Widget owner,PokemonController controller,boolean mobile,Runnable onClose){
        super(owner,mobile,"/pc-frame",1010,680,onClose);this.controller=controller;
        frame.setTitle(controller.strings.get(1118));keyboard=new PlayerTextInput(mobile,this::isOpen);
        content.setTheme("");preview.setTheme("/pc-preview");portrait.setTheme("/pc-portrait");portrait.setClip(true);grid.setTheme("");
        title.setTheme("/pc-box-name");title.setCanAcceptKeyboardFocus(false);title.addCallback(this::renameBox);
        name.setTheme("/pc-monster-name");level.setTheme("/pc-monster-name");slotNumber.setTheme("/label");slotNumber.setClip(true);
        preview.add(name);preview.add(portrait);preview.add(level);
        int[] headingIds={2318,1849,1800,1805,1824,1842,1869};
        for(int i=0;i<headingIds.length;i++){headings[i]=new Label(controller.strings.get(headingIds[i]));headings[i].setTheme("/pc-label-title");values[i]=new Label();values[i].setTheme("/pc-label-value");values[i].setClip(true);preview.add(headings[i]);preview.add(values[i]);}
        for(int i=0;i<moves.length;i++){moves[i]=new Label();moves[i].setTheme("/label");preview.add(moves[i]);}
        for(int i=0;i<boxes.length;i++){
            final int index=i;var b=new Button();b.setTheme("/pc-box-button");b.setModel(new ToggleButtonModel());b.setCanAcceptKeyboardFocus(false);
            if(i<BASE_BOXES){b.addCallback(()->selectBox(index));controller.boxDrop(b,i);}else b.setEnabled(false);
            boxes[i]=b;content.add(b);
        }
        for(int i=0;i<cells.length;i++){final int index=i;var s=controller.slot(PC,i,true);s.onSelect(()->select(s));cells[index]=s;grid.add(s);}
        party=controller.party(true);for(var s:party.slots)s.onSelect(()->select(s));
        gridScroll=new TouchScrollPane(grid);gridScroll.setTheme("/pc-grid-scroll");gridScroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        search=keyboard.field(100,false);search.setTheme("/editfield");search.addCallback(key->filter());
        sort=new Button(controller.strings.get(2315));sort.setTheme("/button");sort.setCanAcceptKeyboardFocus(false);sort.addCallback(this::sortMenu);
        sortPopup=new PopupWindow(sort);sortPopup.setTheme("/pokemon-menu");
        int[] criteria={2330,2331,2329,2334,8100,2332,2333,2328,2336};var checks=new ToggleButton[criteria.length];var labels=new Label[criteria.length];
        var sortApply=new Button(controller.strings.get(2335));sortApply.setTheme("/button");sortApply.setEnabled(false);sortApply.addCallback(()->{sortPopup.closePopup();if(levelOrder.isActive())controller.sort(box);});
        for(int i=0;i<criteria.length;i++){checks[i]=i==0?levelOrder:new ToggleButton();checks[i].setTheme("/togglebutton");checks[i].setEnabled(i==0);labels[i]=new Label(controller.strings.get(criteria[i]));labels[i].setTheme("/label");labels[i].setEnabled(i==0);}
        levelOrder.addCallback(()->sortApply.setEnabled(levelOrder.isActive()));
        var sortForm=new Widget(){@Override protected void layout(){int row=mobile?40:26;for(int i=0;i<checks.length;i++){checks[i].setPosition(getInnerX(),getInnerY()+i*row);checks[i].setSize(20,20);labels[i].setPosition(getInnerX()+27,getInnerY()+i*row);labels[i].setSize(getInnerWidth()-27,row);}sortApply.setPosition(getInnerX(),getInnerY()+checks.length*row);sortApply.setSize(getInnerWidth(),row);}};
        sortForm.setTheme("");for(int i=0;i<checks.length;i++){sortForm.add(checks[i]);sortForm.add(labels[i]);}sortForm.add(sortApply);sortPopup.add(sortForm);
        reset=new Button(){@Override protected void paintWidget(GUI gui){super.paintWidget(gui);var images=controller.images();if(images!=null){var icon=images.atlas("icon_cross");if(icon!=null)icon.draw(getAnimationState(),getX()+(getWidth()-16)/2,getY()+(getHeight()-16)/2,16,16);}}};reset.setTooltipContent(controller.strings.get(8036));reset.setTheme("/button");reset.setCanAcceptKeyboardFocus(false);reset.addCallback(()->search.setText(""));
        content.add(preview);content.add(title);content.add(gridScroll);content.add(party);content.add(sort);content.add(search);content.add(reset);content.add(slotNumber);frame.add(content);
        rename=new PopupWindow(title);rename.setTheme("/pokemon-menu");boxName=keyboard.field(20,false);boxName.setTheme("/editfield");saveName=new Button(controller.strings.get(52));cancelName=new Button(controller.strings.get(53));
        saveName.addCallback(()->{controller.rename(box,boxName.getText());rename.closePopup();keyboard.close();update();});cancelName.addCallback(()->{rename.closePopup();keyboard.close();});
        var form=new DialogLayout();form.setTheme("/dialoglayout");form.setHorizontalGroup(form.createParallelGroup(boxName).addGroup(form.createSequentialGroup(saveName,cancelName)));form.setVerticalGroup(form.createSequentialGroup(boxName).addGroup(form.createParallelGroup(saveName,cancelName)));rename.add(form);
        controller.storageBox(()->box);controller.onChanged(this::update);selectBox(0);
    }
    public void reset(){closePopup();box=0;selectedContainer=-1;selectedPosition=-1;search.setText("");selectBox(0);}
    @Override protected void refresh(){if(!controller.ready())return;update();controller.reload();}
    public boolean editing(){return search.hasKeyboardFocus()||rename.isOpen();}
    @Override public void closePopup(){super.closePopup();if(rename!=null)rename.closePopup();if(sortPopup!=null)sortPopup.closePopup();if(keyboard!=null)keyboard.close();controller.cancelDrag();controller.closeMenu();}
    private void select(PokemonSlot slot){selectedContainer=slot.container;selectedPosition=slot.position;updatePreview();}
    private Pokemon selected(){return controller.state().at(selectedContainer,selectedPosition);}
    private void selectBox(int index){box=index;for(int i=0;i<cells.length;i++)cells[i].position=index*BOX_SIZE+i;gridScroll.setScrollPositionY(0);update();}
    private void filter(){
        String query=search.getText().strip().toLowerCase(Locale.ROOT);
        for(var cell:cells){var p=cell.pokemon();boolean match=!query.isEmpty()&&p!=null&&(controller.name(p).toLowerCase(Locale.ROOT).contains(query)||Integer.toString(p.species()).equals(query));cell.getAnimationState().setAnimationState(de.matthiasmann.twl.AnimationState.StateKey.get("filtered"),match);}
    }
    private void update(){
        title.setText(controller.boxName(box));for(int i=0;i<boxes.length;i++){boxes[i].setText(i<BASE_BOXES?controller.boxName(i):controller.strings.get(i==BASE_BOXES?2364:2361));((ToggleButtonModel)boxes[i].getModel()).setSelected(i==box);}
        filter();updatePreview();sort.setEnabled(!controller.busy());content.invalidateLayout();
    }
    private static String join(List<Integer> values){return values.stream().map(String::valueOf).collect(Collectors.joining("/"));}
    private void updatePreview(){
        var p=selected();preview.setVisible(p!=null);slotNumber.setText(p==null?"":controller.strings.get(2319)+" "+p.position()+" ("+(p.container()==PC?controller.boxName(p.position()/BOX_SIZE):controller.strings.get(2301))+")");
        if(p==null)return;
        name.setText(controller.name(p));level.setText(controller.strings.format(1731,p.egg()?"???":Integer.toString(p.level()))+(p.gender()==0?" ♂":p.gender()==1?" ♀":""));
        var dex=controller.dex();String item=p.item()==0?controller.strings.get(61):p.item()>5000&&p.item()<5627&&dex!=null?dex.item(p.item()-5000):controller.strings.getOrDefault(100000+p.item(),controller.strings.get(1450));
        String[] text={join(p.stats()),join(p.ivs()),join(p.evs()),dex==null?"":dex.nature(p.nature()),dex==null?"":dex.ability(p.ability()),item,""};
        for(int i=0;i<text.length;i++)values[i].setText(p.egg()?"???":text[i]);
        for(int i=0;i<4;i++)moves[i].setText(p.egg()||p.moves().get(i)==0?"":dex==null?"":dex.moveName(p.moves().get(i)));
    }
    private void renameBox(){
        boxName.setText(controller.boxName(box));if(rename.openPopup()){rename.setSize(320,90);rename.setPosition(Math.max(0,Math.min(title.getX(),getWidth()-320)),title.getBottom());boxName.requestKeyboardFocus();boxName.setSelection(0,boxName.getText().length());}
    }
    private void sortMenu(){
        if(sortPopup.openPopup()){sortPopup.setSize(mobile?280:220,10*(mobile?40:26)+sortPopup.getBorderVertical());sortPopup.setPosition(sort.getX(),Math.max(0,sort.getY()-sortPopup.getHeight()));}
    }
    private void layoutPc(){
        if(mobile){layoutMobilePc();return;}
        int x=content.getInnerX(),y=content.getInnerY(),w=content.getInnerWidth(),h=content.getInnerHeight();
        int previewWidth=mobile?310:320;
        int partyWidth=party.getPreferredWidth(),centreX=x+previewWidth+8,centreWidth=w-previewWidth-partyWidth-26;
        int top=mobile?45:0,boxHeight=mobile?40:28,rows=2;
        title.setPosition(centreX,y+top);title.setSize(centreWidth,40);
        int bw=Math.min(mobile?80:60,Math.max(1,(centreWidth-10)/11));
        for(int i=0;i<boxes.length;i++){boxes[i].setPosition(centreX+(i%11)*(bw+1),y+top+42+(i/11)*boxHeight);boxes[i].setSize(bw,boxHeight);boxes[i].setTooltipContent(controller.boxName(i));}
        // Eleven base boxes occupy a single row; leave room for later expansion rows only when present.
        rows=(boxes.length+10)/11;int gridY=y+top+44+rows*boxHeight;
        gridScroll.setPosition(centreX,gridY);gridScroll.setSize(Math.max(1,centreWidth),Math.max(1,h-top-44-rows*boxHeight-(mobile?70:65)));
        party.setPosition(x+w-partyWidth-4,gridY);party.setSize(partyWidth,Math.min(party.getPreferredHeight(),gridScroll.getHeight()));
        preview.setPosition(x+3,y+top+8);preview.setSize(previewWidth,Math.max(1,h-top-28));
        name.setPosition(preview.getX()+5,preview.getY());name.setSize(previewWidth-10,28);
        portrait.setPosition(preview.getX(),preview.getY()+30);portrait.setSize(previewWidth,180);
        level.setPosition(preview.getX(),portrait.getBottom()+2);level.setSize(previewWidth,30);
        int row=mobile?31:26,py=level.getBottom()+3,labelWidth=mobile?112:94;
        for(int i=0;i<headings.length;i++){headings[i].setPosition(preview.getX(),py+i*row);headings[i].setSize(labelWidth,row-1);values[i].setPosition(preview.getX()+labelWidth+2,py+i*row);values[i].setSize(previewWidth-labelWidth-2,row-1);}
        for(int i=0;i<moves.length;i++){moves[i].setPosition(preview.getX()+5,py+headings.length*row+i*row);moves[i].setSize(previewWidth-10,row);}
        int bottom=y+h-(mobile?55:36);sort.setPosition(centreX,bottom);sort.setSize(mobile?150:100,mobile?42:28);
        reset.setSize(mobile?80:55,mobile?42:28);reset.setPosition(centreX+centreWidth-reset.getWidth(),bottom);
        search.setPosition(sort.getRight()+8,bottom);search.setSize(Math.max(1,reset.getX()-search.getX()-8),mobile?42:28);
        slotNumber.setPosition(x+3,y+h-20);slotNumber.setSize(Math.max(0,previewWidth),20);
    }
    /** PokemonStorageWindow's Android branch: box name above the 310-wide preview, search at the bottom. */
    private void layoutMobilePc(){
        int x=content.getInnerX(),y=content.getInnerY(),w=content.getInnerWidth(),h=content.getInnerHeight();
        int previewWidth=310,partyWidth=80,centreX=x+previewWidth+10,centreWidth=Math.max(1,w-previewWidth-partyWidth-25);
        title.setPosition(x,y);title.setSize(previewWidth,60);
        int boxHeight=60,boxWidth=w<=1300?80:84,columns=Math.max(1,Math.min(11,centreWidth/boxWidth)),rows=(boxes.length+columns-1)/columns;
        for(int i=0;i<boxes.length;i++){boxes[i].setPosition(centreX+i%columns*boxWidth,y+60+i/columns*boxHeight);boxes[i].setSize(boxWidth-2,boxHeight);}
        int gridY=y+60+rows*boxHeight+5,bottom=y+h-60;
        gridScroll.setPosition(centreX,gridY);gridScroll.setSize(centreWidth,Math.max(1,bottom-gridY-5));
        party.setPosition(x+w-partyWidth,gridY);party.setSize(partyWidth,Math.min(party.getPreferredHeight(),Math.max(0,bottom-gridY)));
        preview.setPosition(x,y+60);preview.setSize(previewWidth,Math.max(0,h-120));
        name.setPosition(x+5,preview.getY());name.setSize(previewWidth-10,26);
        portrait.setPosition(x,preview.getY()+26);portrait.setSize(previewWidth,150);
        level.setPosition(x,portrait.getBottom());level.setSize(previewWidth,26);
        int row=26,py=level.getBottom(),labelWidth=105;
        for(int i=0;i<headings.length;i++){headings[i].setPosition(x,py+i*row);headings[i].setSize(labelWidth,row);values[i].setPosition(x+labelWidth+2,py+i*row);values[i].setSize(previewWidth-labelWidth-2,row);}
        for(int i=0;i<moves.length;i++){moves[i].setPosition(x+5,py+headings.length*row+i*row);moves[i].setSize(previewWidth-10,row);}
        sort.setPosition(x,bottom);sort.setSize(150,60);reset.setSize(60,60);reset.setPosition(x+w-reset.getWidth()-105,bottom);
        search.setPosition(sort.getRight()+10,bottom);search.setSize(Math.max(1,reset.getX()-search.getX()-10),60);
        slotNumber.setVisible(false);
    }
    private void layoutGrid(){
        int gap=3,cw=Math.min(mobile?80:58,Math.max(32,(grid.getInnerWidth()-9*gap)/10)),ch=mobile?80:48;
        for(int i=0;i<cells.length;i++){cells[i].setSize(cw,ch);cells[i].setPosition(grid.getInnerX()+i%10*(cw+gap),grid.getInnerY()+i/10*(ch+gap));}
    }
}
