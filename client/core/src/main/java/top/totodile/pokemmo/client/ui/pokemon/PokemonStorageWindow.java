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
    private final Widget portrait=new Widget(){
        private de.matthiasmann.twl.renderer.Image background,tinted;private int type=-1;
        @Override protected void applyTheme(ThemeInfo theme){super.applyTheme(theme);background=theme.getImage("background");type=-1;}
        @Override protected void paintBackground(GUI gui){
            if(mobile){super.paintBackground(gui);return;}
            var p=selected();if(p==null||background==null)return;int next=controller.dex().get(p.species()).type1();
            if(type!=next){type=next;tinted=background.createTintedVersion(PokemonSummaryWindow.typeColor(type));}
            tinted.draw(getAnimationState(),getX(),getY(),getWidth(),getHeight());
        }
        @Override protected void paintWidget(GUI gui){
        var p=selected();if(p==null||p.egg()||controller.images()==null)return;
        var image=controller.images().animatedFront(p.species(),p.form(),p.shiny(),System.nanoTime()/1e9);
        int scale=2,w=image.getWidth()*scale,h=image.getHeight()*scale;

        image.draw(getAnimationState(),getX()+(getWidth()-w)/2,mobile?getY()+(getHeight()-h)/2-10:getBottom()+20-h,w,h);
        if(!mobile){var species=controller.dex().get(p.species());var type=controller.images().type(species.type1());if(type!=null)type.draw(getAnimationState(),getRight()-54,getY()+4,49,14);}

    }};
    private final SummaryParticles previewParticles;
    private final Button title=new Button();
    private final Label name=new Label(),level=new Label(),slotNumber=new Label();
    private final Widget previewGender=new Widget(){@Override protected void paintWidget(GUI gui){var p=selected();if(p!=null&&!p.egg()&&p.gender()>=0){var icon=controller.images().atlas(p.gender()==1?"icon_gender_female":"icon_gender_male");if(icon!=null)icon.draw(getAnimationState(),getX(),getY(),16,16);}}};
    private final Widget marks=new Widget(){@Override protected void paintWidget(GUI gui){var p=selected();if(p==null)return;for(int i=0;i<5;i++){var icon=controller.images().atlas("mark",i);if(icon!=null){if((p.marks()&(1<<i))==0)icon=icon.createTintedVersion(new Color(0xff404040));icon.draw(getAnimationState(),getX()+i*18,getY()+5,14,14);}}}};
    private final Widget[] moveTypes=new Widget[4];
    private final Label[] headings=new Label[7],values=new Label[7],moves=new Label[4];
    private final Button[] boxes=new Button[BASE_BOXES+2];
    private final PokemonSlot[] cells=new PokemonSlot[BOX_SIZE];
    private final Widget grid=new Widget(){
        @Override public int getPreferredHeight(){return 6*((mobile?80:50)+3)-3;}
        @Override protected void layout(){layoutGrid();}
    };
    private final ScrollPane gridScroll;
    private final PartyPanel party;
    private final PlayerTextInput keyboard;
    private final EditField search;
    private final Button sort,reset,renameTool,multiselect,itemMode,lowValue,help,advanced;
    private final Set<String> selectedIds=new LinkedHashSet<>();
    private boolean selecting,itemsMode,advancedOpen,forcingClose;
    private PokemonStorageSearch advancedPage;
    private final PopupWindow helpPopup;

    private final PopupWindow sortPopup;
    private final ToggleButton levelOrder=new ToggleButton();
    private final PopupWindow rename;
    private final EditField boxName;
    private final Button saveName,cancelName;
    private int box,selectedContainer=-1,selectedPosition=-1;
    public PokemonStorageWindow(Widget owner,PokemonController controller,boolean mobile,Runnable onClose){
        super(owner,mobile,"/pc-frame",1030,654,onClose);this.controller=controller;
        previewParticles=new SummaryParticles(controller);if(!mobile)portrait.add(previewParticles);
        frame.setTitle(controller.strings.get(1118));keyboard=new PlayerTextInput(mobile,this::isOpen);
        content.setTheme("");preview.setTheme("preview-frame");portrait.setTheme("border-container");portrait.setClip(true);grid.setTheme("dialoglayout");
        title.setTheme("box-name");title.setCanAcceptKeyboardFocus(false);title.addCallback(this::renameBox);
        name.setTheme("monster-name-label");level.setTheme("monster-name-label");slotNumber.setTheme("/label");slotNumber.setClip(true);
        preview.add(name);preview.add(portrait);preview.add(level);previewGender.setTheme("");marks.setTheme("");preview.add(previewGender);preview.add(marks);
        int[] headingIds={2318,1849,1800,1805,1824,1842,1869};
        for(int i=0;i<headingIds.length;i++){headings[i]=new Label(controller.strings.get(headingIds[i]));headings[i].setTheme("label-title");values[i]=new Label();values[i].setTheme("label");values[i].setClip(true);preview.add(headings[i]);preview.add(values[i]);}
        for(int i=0;i<moves.length;i++){final int index=i;moves[i]=new Label();moves[i].setTheme("label");preview.add(moves[i]);moveTypes[i]=new Widget(){@Override protected void paintWidget(GUI gui){var p=selected();if(p==null||p.egg()||p.moves().get(index)==0)return;var icon=controller.images().type(controller.dex().move(p.moves().get(index)).type());if(icon!=null)icon.draw(getAnimationState(),getX(),getY()+6,49,14);}};moveTypes[i].setTheme("");preview.add(moveTypes[i]);}
        for(int i=0;i<boxes.length;i++){
            final int index=i;var b=new Button();b.setTheme("pc-box-button");b.setModel(new ToggleButtonModel());b.setCanAcceptKeyboardFocus(false);
            if(i<BASE_BOXES){b.addCallback(()->selectBox(index));controller.boxDrop(b,i);}else b.setEnabled(false);
            boxes[i]=b;content.add(b);
        }
        for(int i=0;i<cells.length;i++){final int index=i;var s=controller.slot(PC,i,true);s.onSelect(()->select(s));s.onContext(()->contextSelect(s));cells[index]=s;grid.add(s);}
        party=controller.party(true);party.setTheme("/pc-frame.container.dialoglayout");for(var s:party.slots){s.onSelect(()->select(s));s.onContext(()->contextSelect(s));}
        gridScroll=new TouchScrollPane(grid);gridScroll.setTheme("/pc-frame.container.scrollpane");gridScroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        search=keyboard.field(100,false);search.setTheme("/pc-frame.editfield");search.addCallback(key->filter());
        sort=new Button(){@Override protected void paintWidget(GUI gui){if(mobile){super.paintWidget(gui);return;}var icon=controller.images().atlas("icon_sort");if(icon!=null)icon.draw(getAnimationState(),getX()+1,getY()+1,20,20);}};sort.setText(mobile?controller.strings.get(2315):"");sort.setTooltipContent(controller.strings.get(2315));sort.setTheme(mobile?"/pc-frame.label-button-small2-centered":"/pc-tool-button");sort.setCanAcceptKeyboardFocus(false);sort.addCallback(this::sortMenu);
        sortPopup=new PopupWindow(sort);sortPopup.setTheme("/pokemon-menu");
        int[] criteria={2330,2331,2329,2334,8100,2332,2333,2328,2336};var checks=new ToggleButton[criteria.length];var labels=new Label[criteria.length];
        var sortApply=new Button(controller.strings.get(2335));sortApply.setTheme("/button");sortApply.setEnabled(false);sortApply.addCallback(()->{sortPopup.closePopup();if(levelOrder.isActive())controller.sort(box);});
        for(int i=0;i<criteria.length;i++){checks[i]=i==0?levelOrder:new ToggleButton();checks[i].setTheme("/togglebutton");checks[i].setEnabled(i==0);labels[i]=new Label(controller.strings.get(criteria[i]));labels[i].setTheme("/label");labels[i].setEnabled(i==0);}
        levelOrder.addCallback(()->sortApply.setEnabled(levelOrder.isActive()));
        var sortForm=new Widget(){@Override protected void layout(){int row=mobile?40:26;for(int i=0;i<checks.length;i++){checks[i].setPosition(getInnerX(),getInnerY()+i*row);checks[i].setSize(20,20);labels[i].setPosition(getInnerX()+27,getInnerY()+i*row);labels[i].setSize(getInnerWidth()-27,row);}sortApply.setPosition(getInnerX(),getInnerY()+checks.length*row);sortApply.setSize(getInnerWidth(),row);}};
        sortForm.setTheme("");for(int i=0;i<checks.length;i++){sortForm.add(checks[i]);sortForm.add(labels[i]);}sortForm.add(sortApply);sortPopup.add(sortForm);
        reset=new Button(){@Override protected void paintWidget(GUI gui){super.paintWidget(gui);var images=controller.images();if(images!=null){var icon=images.atlas("icon_cross");if(icon!=null)icon.draw(getAnimationState(),getX()+(getWidth()-16)/2,getY()+(getHeight()-16)/2,16,16);}}};reset.setTooltipContent(controller.strings.get(8036));reset.setTheme(mobile?"/button":"/pc-tool-button");reset.setCanAcceptKeyboardFocus(false);reset.addCallback(()->{search.setText("");if(advancedPage!=null)advancedPage.reset();filter();});
        renameTool=new Button(){@Override protected void paintWidget(GUI gui){var icon=controller.images().atlas("edit_box_name");if(icon!=null)icon.draw(getAnimationState(),getX()+1,getY()+1,20,20);}};renameTool.setTheme("/pc-tool-button");renameTool.setCanAcceptKeyboardFocus(false);renameTool.addCallback(this::renameBox);renameTool.setVisible(!mobile);content.add(renameTool);
        multiselect=tool("pc_multiselect-default",()->{selecting=!selecting;selectedIds.clear();selectionChanged();});
        itemMode=tool("",()->{itemsMode=!itemsMode;for(var cell:cells)cell.itemMode(itemsMode);for(var slot:party.slots)slot.itemMode(itemsMode);});
        lowValue=tool("icon_trash",this::selectLowValue);
        help=tool("tooltip",()->showHelp());
        helpPopup=new PopupWindow(help);helpPopup.setTheme("/pokemon-menu");
        advanced=new Button(controller.strings.get(8010));advanced.setTheme("/button");advanced.setCanAcceptKeyboardFocus(false);advanced.addCallback(this::showAdvanced);advanced.setVisible(!mobile);content.add(advanced);
        renameTool.setTooltipContent(controller.strings.get(2362));lowValue.setTooltipContent(controller.strings.get(2383));help.setTooltipContent(controller.strings.get(2383));
        controller.storageSelection(()->Arrays.stream(cells).filter(c->c.pokemon()!=null&&selectedIds.contains(c.pokemon().id())).toList());
        reset.setTooltipDelay(100); // PokemonStorageWindow: search reset (8036), qj_2.GH0=100.
        content.add(preview);content.add(title);content.add(gridScroll);content.add(party);content.add(sort);content.add(search);content.add(reset);content.add(slotNumber);frame.add(content);
        rename=new PopupWindow(title);rename.setTheme("/pokemon-menu");boxName=keyboard.field(20,false);boxName.setTheme("/editfield");saveName=new Button(controller.strings.get(52));cancelName=new Button(controller.strings.get(53));
        saveName.addCallback(()->{controller.rename(box,boxName.getText());rename.closePopup();keyboard.close();update();});cancelName.addCallback(()->{rename.closePopup();keyboard.close();});
        var form=new DialogLayout();form.setTheme("/dialoglayout");form.setHorizontalGroup(form.createParallelGroup(boxName).addGroup(form.createSequentialGroup(saveName,cancelName)));form.setVerticalGroup(form.createSequentialGroup(boxName).addGroup(form.createParallelGroup(saveName,cancelName)));rename.add(form);
        controller.storageBox(()->box);controller.onChanged(this::update);selectBox(0);
    }
    public void forceClose(){forcingClose=true;try{closePopup();}finally{forcingClose=false;}}
    public void reset(){forceClose();if(advancedPage!=null)advancedPage.reset();selecting=false;itemsMode=false;selectedIds.clear();box=0;selectedContainer=-1;selectedPosition=-1;search.setText("");selectBox(0);}
    @Override protected void refresh(){if(!controller.ready())return;update();controller.reload();}
    public boolean editing(){return search.hasKeyboardFocus()||rename.isOpen();}
    @Override public void closePopup(){
        if(!forcingClose){if(advancedOpen){if(!advancedPage.closeChoices())hideAdvanced();return;}if(rename!=null&&rename.isOpen()){rename.closePopup();keyboard.close();return;}if(sortPopup!=null&&sortPopup.isOpen()){sortPopup.closePopup();return;}if(helpPopup!=null&&helpPopup.isOpen()){helpPopup.closePopup();return;}}
        if(advancedOpen)hideAdvanced();super.closePopup();if(helpPopup!=null)helpPopup.closePopup();if(advancedPage!=null)advancedPage.closeChoices();if(rename!=null)rename.closePopup();if(sortPopup!=null)sortPopup.closePopup();if(keyboard!=null)keyboard.close();controller.cancelDrag();controller.closeMenu();}
    private void select(PokemonSlot slot){
        if(slot.pokemon()==null){
            // PcStorageSlotWidget clears the container selection when an empty slot is clicked.
            selectedIds.clear();selectedContainer=-1;selectedPosition=-1;controller.closeMenu();
        }else{
            selectedContainer=slot.container;selectedPosition=slot.position;
            if(selecting&&slot.container==PC){if(!selectedIds.add(slot.pokemon().id()))selectedIds.remove(slot.pokemon().id());}
            else{selectedIds.clear();selectedIds.add(slot.pokemon().id());}
        }
        selectionChanged();updatePreview();
    }
    private void contextSelect(PokemonSlot slot){
        var p=slot.pokemon();if(p==null)return;
        selectedContainer=slot.container;selectedPosition=slot.position;
        if(!selectedIds.contains(p.id())){selectedIds.clear();selectedIds.add(p.id());}
        selectionChanged();updatePreview();
    }
    private Pokemon selected(){return controller.state().at(selectedContainer,selectedPosition);}
    private void selectBox(int index){selectedIds.clear();selectedContainer=-1;selectedPosition=-1;box=index;controller.cancelDrag();for(int i=0;i<cells.length;i++)cells[i].bindPosition(index*BOX_SIZE+i);gridScroll.setScrollPositionY(0);update();}
    private void filter(){
        String query=search.getText().strip().toLowerCase(Locale.ROOT);
        for(var cell:cells){var p=cell.pokemon();boolean match=p!=null&&(!query.isEmpty()||advancedPage!=null&&advancedPage.active())&&(query.isEmpty()||Arrays.stream(query.split("\\s+")).allMatch(word->controller.name(p).toLowerCase(Locale.ROOT).contains(word)||Integer.toString(p.species()).equals(word)))&&(advancedPage==null||advancedPage.matches(p));cell.getAnimationState().setAnimationState(de.matthiasmann.twl.AnimationState.StateKey.get("filtered"),match);}
    }
    private void update(){
        title.setText(controller.boxName(box));for(int i=0;i<boxes.length;i++){boxes[i].setText(i<BASE_BOXES?controller.boxName(i):controller.strings.get(i==BASE_BOXES?2364:2361));((ToggleButtonModel)boxes[i].getModel()).setSelected(i==box);}
        selectionChanged();filter();updatePreview();sort.setEnabled(!controller.busy());content.invalidateLayout();
    }
    private static String join(List<Integer> values){return values.stream().map(String::valueOf).collect(Collectors.joining("/"));}
    private void updatePreview(){
        var p=selected();preview.setVisible(!mobile||p!=null);for(var widget:new Widget[]{portrait,name,level,previewGender,marks})widget.setVisible(p!=null);for(int i=0;i<headings.length;i++){headings[i].setVisible(p!=null);values[i].setVisible(p!=null);}for(int i=0;i<4;i++){moves[i].setVisible(p!=null);moveTypes[i].setVisible(p!=null&&!mobile);}slotNumber.setText(p==null?"":controller.strings.get(2319)+" "+p.position()+" ("+(p.container()==PC?controller.boxName(p.position()/BOX_SIZE):controller.strings.get(2301))+")");
        if(p==null)return;
        if(!mobile){previewParticles.type(controller.dex().get(p.species()).type1());previewParticles.setVisible(!p.egg());}
        name.setText(mobile?controller.name(p):"");level.setText(controller.strings.format(1731,p.egg()?"???":Integer.toString(p.level()))+(mobile?(p.gender()==0?" ♂":p.gender()==1?" ♀":""):" "+controller.name(p)));
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
        if(advancedOpen){advancedPage.setPosition(x+8,y+8);advancedPage.setSize(w-16,h-16);return;}
        int previewWidth=Math.min(320,w/3),partyWidth=54,centreX=x+previewWidth+5,centreWidth=w-previewWidth-10;
        title.setPosition(centreX,y+2);title.setSize(centreWidth,48);
        int boxHeight=42,boxGap=4,bw=Math.max(1,(centreWidth-20-10*boxGap)/11),boxX=centreX+15;
        for(int i=0;i<boxes.length;i++){boxes[i].setPosition(boxX+i%11*(bw+boxGap),y+56+i/11*(boxHeight+3));boxes[i].setSize(bw,boxHeight);boxes[i].setTooltipContent(controller.boxName(i));}
        int rows=(boxes.length+10)/11,gridY=y+56+rows*(boxHeight+3)+28,bottom=y+h-46;
        int gridWidth=Math.max(1,centreWidth-partyWidth-12),gridHeight=6*50+5*3;
        gridScroll.setPosition(centreX,gridY);gridScroll.setSize(gridWidth,Math.max(1,Math.min(gridHeight,bottom-gridY-12)));
        party.setPosition(centreX+gridWidth+7,gridY);party.setSize(partyWidth,Math.min(gridHeight,gridScroll.getHeight()));
        preview.setPosition(x,y);preview.setSize(previewWidth,h);
        name.setPosition(x,y);name.setSize(previewWidth,20);
        portrait.setPosition(x+2,y+2);portrait.setSize(previewWidth-4,222);previewParticles.setPosition(portrait.getX(),portrait.getY());previewParticles.setSize(portrait.getWidth(),portrait.getHeight());
        level.setPosition(x+4,y+224);level.setSize(previewWidth-8,30);
        int genderX=level.getX()+(level.getWidth()+level.getFont().computeTextWidth(level.getText()))/2+3;
        previewGender.setPosition(genderX,y+231);previewGender.setSize(16,16);
        int row=33,py=y+260,labelWidth=70;
        for(int i=0;i<headings.length;i++){headings[i].setPosition(x+4,py+i*row);headings[i].setSize(labelWidth,row-4);values[i].setPosition(x+labelWidth+7,py+i*row);values[i].setSize(previewWidth-labelWidth-11,row-4);}
        marks.setPosition(values[6].getX(),values[6].getY());marks.setSize(100,28);
        for(int i=0;i<moves.length;i++){int my=py+headings.length*row+i*31;moveTypes[i].setPosition(x+5,my);moveTypes[i].setSize(49,28);moves[i].setPosition(x+62,my);moves[i].setSize(previewWidth-66,28);}
        itemMode.setPosition(centreX+3,bottom);itemMode.setSize(32,40);
        multiselect.setPosition(centreX+45,bottom);multiselect.setSize(22,20);lowValue.setPosition(centreX+93,bottom);lowValue.setSize(22,20);
        help.setPosition(centreX+45,bottom+22);help.setSize(22,20);renameTool.setPosition(centreX+69,bottom+22);renameTool.setSize(22,20);sort.setPosition(centreX+93,bottom+22);sort.setSize(22,20);
        reset.setSize(24,28);advanced.setSize(70,28);advanced.setPosition(centreX+centreWidth-72,bottom+8);reset.setPosition(advanced.getX()-28,bottom+8);
        int searchWidth=Math.min(190,Math.max(70,centreWidth-80));search.setPosition(reset.getX()-searchWidth-8,bottom+8);search.setSize(searchWidth,28);
        slotNumber.setVisible(false);
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
    private Button tool(String image,Runnable action){var button=new Button(){@Override protected void paintWidget(GUI gui){String name=this==multiselect?(selecting?"pc_multiselect-enabled":"pc_multiselect-default"):image;var icon=this==itemMode?controller.itemIcon(5152):controller.images().atlas(name);if(icon!=null){if(this==itemMode&&!itemsMode)icon=icon.createTintedVersion(new Color(0xff808080));int size=this==itemMode?30:20;icon.draw(getAnimationState(),getX()+(getWidth()-size)/2,getY()+(getHeight()-size)/2,size,size);}}};button.setTheme("/pc-tool-button");button.setCanAcceptKeyboardFocus(false);button.addCallback(action);button.setVisible(!mobile);content.add(button);return button;}
    private void selectionChanged(){
        var slots=new ArrayList<PokemonSlot>(Arrays.asList(cells));slots.addAll(Arrays.asList(party.slots));
        for(var slot:slots){var p=slot.pokemon();boolean selected=p!=null&&selectedIds.contains(p.id());slot.getModel().setSelected(selecting&&selected);slot.getAnimationState().setAnimationState(Widget.STATE_KEYBOARD_FOCUS,selected&&(!selecting||slot.container==selectedContainer&&slot.position==selectedPosition));slot.getAnimationState().setAnimationState(de.matthiasmann.twl.AnimationState.StateKey.get("multiselectActive"),selecting&&selected);}
    }
    private void selectLowValue(){selecting=true;selectedIds.clear();for(var cell:cells){var p=cell.pokemon();if(p!=null&&!valuable(p))selectedIds.add(p.id());}selectionChanged();}
    private boolean valuable(Pokemon p){return p.egg()||controller.dex().legendary(p.species())||p.shiny()||p.alpha()||p.hiddenAbility()||p.origin().fateful()||p.origin().raid()||!p.honors().particles().isEmpty()||p.ivs().contains(31)||p.ivs().stream().mapToInt(Integer::intValue).sum()>=120||p.ivs().stream().filter(n->n==0).count()>=5||p.level()>=75||p.origin().time()>0&&java.time.Instant.ofEpochSecond(p.origin().time()).atZone(java.time.ZoneOffset.UTC).getYear()<=java.time.Year.now().getValue()-10;}
    private void showHelp(){var text=new StringBuilder();for(int id:new int[]{2371,2388,2378,2375,2376,2389,2377})text.append("• ").append(controller.strings.get(id)).append("\n");var label=new Label(controller.strings.get(2383)+"\n\n"+controller.strings.format(2384,text.toString()));label.setTheme("/label");helpPopup.removeAllChildren();helpPopup.add(label);if(helpPopup.openPopup()){helpPopup.setSize(Math.min(600,getWidth()-20),Math.min(300,getHeight()-20));helpPopup.setPosition(Math.max(5,Math.min(help.getX(),getWidth()-helpPopup.getWidth()-5)),Math.max(5,help.getY()-helpPopup.getHeight()));}}
    private void showAdvanced(){if(advancedPage==null){advancedPage=new PokemonStorageSearch(controller,()->{hideAdvanced();search.setText("");filter();},()->{hideAdvanced();filter();});content.add(advancedPage);}advancedOpen=true;for(int i=0;i<content.getNumChildren();i++)content.getChild(i).setVisible(content.getChild(i)==advancedPage);content.invalidateLayout();}
    private void hideAdvanced(){if(!advancedOpen)return;advancedPage.closeChoices();advancedOpen=false;for(int i=0;i<content.getNumChildren();i++)content.getChild(i).setVisible(content.getChild(i)!=advancedPage);for(var cell:cells)cell.itemMode(itemsMode);update();content.invalidateLayout();}
    private void layoutGrid(){
        int gap=3,cw=Math.min(mobile?80:60,Math.max(32,(grid.getInnerWidth()-9*gap)/10)),ch=mobile?80:50;
        for(int i=0;i<cells.length;i++){cells[i].setSize(cw,ch);cells[i].setPosition(grid.getInnerX()+i%10*(cw+gap),grid.getInnerY()+i/10*(ch+gap));}
    }
}
