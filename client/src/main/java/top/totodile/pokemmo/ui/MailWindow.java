package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientStrings;

/** 邮箱展示窗口；未接通的写入操作保持禁用。 */
final class MailWindow extends ResizableFrame implements AutoCloseable {
    private final ToggleButton[] tabs = new ToggleButton[3];
    private final Widget content = new Widget();
    private final Label count = new Label();
    private final Label[] headers = new Label[4];
    private final Button[] pages = {new Button("<<"),new Button("1"),new Button(">>")};
    private final Label[] fieldLabels = new Label[6];
    private final EditField[] fields = new EditField[6];
    private final Button send = new Button();
    private final Button clear = new Button();
    private final CatalogIcons attachmentIcons = new CatalogIcons();
    private java.util.concurrent.CompletableFuture<java.awt.image.BufferedImage> pokemonPixels;
    private de.matthiasmann.twl.renderer.Texture pokemonTexture;
    private de.matthiasmann.twl.renderer.Image pokemonIcon;

    private de.matthiasmann.twl.renderer.Image pokemonImage(GUI gui) {
        if(pokemonIcon==null&&pokemonPixels!=null&&pokemonPixels.isDone()&&!pokemonPixels.isCompletedExceptionally()) {
            pokemonTexture=((GlfwTwlRenderer)gui.getRenderer()).uploadImage(pokemonPixels.join());
            pokemonIcon=pokemonTexture.getImage(0,0,32,32,Color.WHITE,false,de.matthiasmann.twl.renderer.Texture.Rotation.NONE);
        }
        return pokemonIcon;
    }
    private final Label money = new Label("$" + java.text.NumberFormat.getIntegerInstance(java.util.Locale.ROOT).format(30_000_000L));
    private final Button[] claim = new Button[4];
    private final Button itemAttachment = new Button("100x") {
        @Override protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            var image=attachmentIcons.image(gui,false,1192);
            if(image!=null)image.draw(getAnimationState(),getX()+(getWidth()-24)/2,getY()+2,24,24);
        }
    };
    private de.matthiasmann.twl.renderer.Image shinyMark, particleMark;
    private final Button pokemonAttachment = new Button("40") {
        @Override protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if(shinyMark!=null)shinyMark.draw(getAnimationState(),getX()+3,getY()+2,12,13);
            if(particleMark!=null)particleMark.draw(getAnimationState(),getX()+3,getY()+15,12,13);
            var image=pokemonImage(gui);
            if(image!=null)image.draw(getAnimationState(),getX()+(getWidth()-32)/2,getY()+2,32,32);
        }
    };
    @Override protected void applyTheme(ThemeInfo info) {
        super.applyTheme(info);
        shinyMark=info.getImage("shinyMark");particleMark=info.getImage("particleMark");
    }
    private int selected;
    void open() {
        attachmentIcons.open();
        if(pokemonPixels==null) pokemonPixels=java.util.concurrent.CompletableFuture.supplyAsync(()->{
            try {
                var rom=new top.totodile.pokemmo.RomFiles(top.totodile.pokemmo.ClientFiles.data("../roms")).selected(0);
                return top.totodile.pokemmo.rom.PartyIcons.loadStrip(rom,
                        java.util.List.of(new top.totodile.pokemmo.protocol.PartyMember(132,true,false,100,100,40)),
                        Color.WHITE.toARGB(),Color.WHITE.toARGB(),0,1).getSubimage(5,5,32,32);
            } catch(java.io.IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
        setVisible(true);
    }
    @Override public void close() {
        attachmentIcons.close();
        if(pokemonTexture!=null){pokemonTexture.destroy();pokemonTexture=null;}
        pokemonIcon=null;setVisible(false);
    }
    private int selectedMail = -1;
    private final ToggleButton[] rows = new ToggleButton[7];
    private final Label[][] cells = new Label[7][3];
    private final Button[] deletes = new Button[7];
    private final Label detailSender = new Label();
    private final Label detailSubject = new Label();
    private final Label detailBody = new Label();
    private String bodyText = "";
    private final Widget headerBackground = new Widget();
    private final Button reply = new Button();
    private static final int[] SENDERS = {9673,9660,9660,9660,9661,9662,9663};
    private static final int[] SUBJECTS = {9666,9664,9664,9665,9666,9667,9668};
    private static final int[] DATES = {1,2,2,17,17,18,467};

    int displayWidth() { return selected == 0 && selectedMail >= 0 ? 924 : 474; }

    MailWindow() {
        setTheme("/mail-window");setTitle(ClientStrings.get(5827));setResizableAxis(ResizableAxis.NONE);
        int[] names={5835,5834,5848};
        for(int i=0;i<tabs.length;i++){
            final int index=i;tabs[i]=new ToggleButton(ClientStrings.get(names[i]));tabs[i].setTheme("tab");
            tabs[i].addCallback(()->select(index));add(tabs[i]);
        }
        content.setTheme("content");add(content);
        count.setTheme("count");content.add(count);
        headerBackground.setTheme("header-background");content.add(headerBackground);
        for(int i=0;i<headers.length;i++){headers[i]=new Label(ClientStrings.get(9650+i));headers[i].setTheme("header");content.add(headers[i]);}
        for(Button page:pages){page.setTheme("page");page.setEnabled(false);content.add(page);}
        int[] labels={5828,5829,5830,5831,5832,5833};
        for(int i=0;i<labels.length;i++){
            fieldLabels[i]=new Label(ClientStrings.get(labels[i]));fieldLabels[i].setTheme("label");content.add(fieldLabels[i]);
            fields[i]=new EditField();fields[i].setTheme("editfield");fields[i].setEnabled(false);content.add(fields[i]);
        }
        send.setText(ClientStrings.get(5834));send.setTheme("button");send.setEnabled(false);content.add(send);
        clear.setText(ClientStrings.get(5841));clear.setTheme("button");clear.setEnabled(false);content.add(clear);
        for(int i=0;i<rows.length;i++) {
            final int index=i;
            rows[i]=new ToggleButton();rows[i].setTheme("row");rows[i].setCanAcceptKeyboardFocus(false);
            rows[i].addCallback(()->selectMail(index));content.add(rows[i]);
            String[] texts={ClientStrings.get(SENDERS[i]),ClientStrings.get(SUBJECTS[i]),i==0?ClientStrings.get(9678):ClientStrings.get(9669).replace("{00}",Integer.toString(DATES[i]))};
            for(int j=0;j<3;j++){cells[i][j]=new Label(texts[j]) { @Override protected boolean handleEvent(Event event) { return false; } };cells[i][j].setTheme("/mail-cell");rows[i].add(cells[i][j]);}
            deletes[i]=new Button();deletes[i].setTheme("delete");deletes[i].setTooltipContent(ClientStrings.get(9653));content.add(deletes[i]);
        }
        detailSender.setTheme("detail-value");detailSubject.setTheme("detail-value");
        detailBody.setTheme("detail-body");
        reply.setTheme("button");reply.setText(ClientStrings.get(9671));reply.addCallback(()->{select(1);fields[0].setText(ClientStrings.get(SENDERS[selectedMail]));fields[1].setText("Re: "+ClientStrings.get(SUBJECTS[selectedMail]));});
        content.add(detailSender);content.add(detailSubject);content.add(detailBody);content.add(reply);
        money.setTheme("detail-value");content.add(money);
        itemAttachment.setTheme("attachment-item");pokemonAttachment.setTheme("attachment-pokemon");
        itemAttachment.setTooltipContent(ClientStrings.get(4014)+" × 100");
        content.add(itemAttachment);content.add(pokemonAttachment);
        for(int i=0;i<claim.length;i++){claim[i]=new Button(ClientStrings.get(9674+i));claim[i].setTheme("button");content.add(claim[i]);}
        select(0);setVisible(false);
    }

    private void select(int tab) {
        selected=tab;
        for(int i=0;i<tabs.length;i++)tabs[i].setActive(i==tab);
        count.setText(ClientStrings.get(tab==2?9655:9654).replace("{00}",tab==0?"7":"0").replace("{01}","250"));
        count.setVisible(tab!=1);
        headerBackground.setVisible(tab!=1);
        for(Label header:headers)header.setVisible(tab!=1);
        for(Button page:pages)page.setVisible(tab!=1);
        for(Label label:fieldLabels)label.setVisible(tab==1);
        for(EditField field:fields)field.setVisible(tab==1);
        send.setVisible(tab==1);clear.setVisible(tab==1);
        for(int i=0;i<rows.length;i++){rows[i].setVisible(tab==0);deletes[i].setVisible(tab==0);}
        boolean detail=tab==0&&selectedMail>=0;
        boolean attached=detail&&(selectedMail==0||selectedMail==5);
        money.setVisible(attached);itemAttachment.setVisible(attached);pokemonAttachment.setVisible(attached);
        for(Button button:claim)button.setVisible(attached);
        detailSender.setVisible(detail);detailSubject.setVisible(detail);detailBody.setVisible(detail);reply.setVisible(detail);
        invalidateLayout();if(getParent()!=null)getParent().invalidateLayout();
    }

    private void selectMail(int index) {
        selectedMail=index;
        for(int i=0;i<rows.length;i++)rows[i].setActive(i==index);
        detailSender.setText(ClientStrings.get(SENDERS[index]));detailSubject.setText(ClientStrings.get(SUBJECTS[index]));
        bodyText=index==0?"1":index==5?ClientStrings.get(9670):ClientStrings.get(9666);
        select(0);
    }

    @Override protected void layout() {
        super.layout();
        int x=getInnerX(),y=getInnerY(),w=getInnerWidth(),h=getInnerHeight();
        int left=x+10;
        int tabIndex=0;
        for(ToggleButton tab:tabs){int width=new int[]{100,100,105}[tabIndex++];tab.setPosition(left,y);tab.setSize(width,26);left+=width;}
        content.setPosition(x,y+24);content.setSize(w,Math.max(0,h-24));
        int cx=x+16,cy=y+24;
        count.setPosition(x+2,cy+1);count.setSize(w-4,35);
        int listWidth=Math.min(434,w-32);
        int[] widths={listWidth*32/100,listWidth*38/100,listWidth*18/100,listWidth-listWidth*88/100};
        headerBackground.setPosition(cx,cy+51);headerBackground.setSize(listWidth,32);
        left=cx;
        for(int i=0;i<headers.length;i++){headers[i].setPosition(left,cy+51);headers[i].setSize(widths[i],32);left+=widths[i];}
        for(int i=0;i<rows.length;i++){
            int ry=cy+85+i*34;
            rows[i].setPosition(cx,ry);rows[i].setSize(listWidth,32);
            int column=cx;
            for(int j=0;j<3;j++){cells[i][j].setPosition(column,ry);cells[i][j].setSize(widths[j],32);column+=widths[j];}
            deletes[i].setPosition(column,ry);deletes[i].setSize(widths[3],32);
        }
        int dx=cx+listWidth+12,dw=Math.max(0,w-32-listWidth-12);
        detailSender.setPosition(dx,cy+47);detailSender.setSize(dw,29);
        detailSubject.setPosition(dx,cy+82);detailSubject.setSize(dw,29);
        detailBody.setPosition(dx,cy+117);detailBody.setSize(dw,149);
        StringBuilder wrapped=new StringBuilder();StringBuilder line=new StringBuilder();
        for(char character:bodyText.toCharArray()) {
            line.append(character);
            if(detailBody.getFont()!=null&&detailBody.getFont().computeTextWidth(line)>Math.max(1,dw-12)&&line.length()>1){
                line.setLength(line.length()-1);wrapped.append(line).append('\n');line.setLength(0);line.append(character);
            }
        }
        detailBody.setText(wrapped.append(line).toString());
        boolean attached=selectedMail==0||selectedMail==5;
        int actionWidth=158;
        money.setPosition(dx,cy+272);money.setSize(Math.max(0,dw-actionWidth-3),28);
        claim[0].setPosition(dx+dw-actionWidth,cy+272);claim[0].setSize(actionWidth,28);
        itemAttachment.setPosition(dx+Math.max(0,(dw-actionWidth-56)/2),cy+304);itemAttachment.setSize(56,46);
        pokemonAttachment.setPosition(itemAttachment.getX(),cy+356);pokemonAttachment.setSize(56,46);
        claim[1].setPosition(dx+dw-actionWidth,cy+312);claim[1].setSize(actionWidth,28);
        claim[2].setPosition(dx+dw-actionWidth,cy+352);claim[2].setSize(actionWidth,28);
        claim[3].setPosition(dx+dw-actionWidth,cy+382);claim[3].setSize(actionWidth,28);
        reply.setPosition(dx,cy+(attached?416:272));reply.setSize(dw,attached?28:61);
        left=x+(w-106)/2;
        for(Button page:pages){page.setPosition(left,getInnerBottom()-42);page.setSize(30,26);left+=38;}
        int row=cy+16;
        for(int i=0;i<fields.length;i++){
            int height=i==2?120:30;
            fieldLabels[i].setPosition(cx,row);fieldLabels[i].setSize(80,24);
            fields[i].setPosition(cx+88,row);fields[i].setSize(Math.max(0,w-120),height);row+=height+10;
        }
        send.setPosition(cx+88,row);send.setSize(120,30);clear.setPosition(cx+218,row);clear.setSize(120,30);
    }
}
