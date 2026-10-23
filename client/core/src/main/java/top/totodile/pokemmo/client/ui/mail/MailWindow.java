package top.totodile.pokemmo.client.ui.mail;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.ToggleButtonModel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.*;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.common.*;
import top.totodile.pokemmo.server.game.*;
import top.totodile.pokemmo.server.login.LoginService.Selection;

/** MailWindow / DateTimeFormatUtils: received, compose, sent, paging and read/reply/delete. */
public final class MailWindow extends PlayerWindow implements AutoCloseable {
    private final ClientStrings strings;
    private final GameService game;
    private Selection selection;
    private GameService.Character character;
    private long version;
    private final Button[] tabs=new Button[3];
    private final Widget content=new Widget(){@Override protected void layout(){layoutMail();}};
    private final Widget listing=new Widget(){@Override protected void layout(){layoutList();}};
    private final ScrollPane scroll=new TouchScrollPane(),composeScroll=new TouchScrollPane(),detailScroll=new TouchScrollPane();
    private final Widget pager=new Widget(){@Override protected void layout(){int size=65,gap=5,width=getNumChildren()*(size+gap)-gap,x=getInnerX()+(getInnerWidth()-width)/2;for(int i=0;i<getNumChildren();i++){var b=getChild(i);b.setPosition(x+i*(size+gap),getInnerY());b.setSize(size,60);}}};
    private final Widget entries=new Widget(){
        @Override public int getPreferredInnerHeight(){return getNumChildren()*(mobile?50:32);}
        @Override protected void layout(){for(int i=0;i<getNumChildren();i++){var row=getChild(i);row.setPosition(getInnerX(),getInnerY()+i*(mobile?50:32));row.setSize(getInnerWidth(),mobile?50:32);}}
    };
    private final Label counts=new Label(),pageLabel=new Label();
    private final Button previous=new Button("<"),next=new Button(">");
    private final EditField recipient,subject,body;
    private final PlayerTextInput textInput;
    private final Button send,clear;
    private final DialogLayout compose=new DialogLayout();
    private final MessageDialog message;
    private final ActionConfirmDialog confirm;
    private int tab,page,total;
    private PlayerData.Mail selected;
    private boolean mailboxAccess,sending;
    private final TextureAtlas atlas;
    private final de.matthiasmann.twl.renderer.Image cross;
    public MailWindow(Widget owner,ClientResources resources,GdxTwlRenderer renderer,boolean mobile,GameService game,Consumer<de.matthiasmann.twl.renderer.Image> background,Runnable onClose){
        super(owner,mobile,"/mail-window",510,550,onClose);this.strings=resources.strings();this.game=game;
        textInput=new PlayerTextInput(mobile,this::isOpen);recipient=textInput.field(16,false);subject=textInput.field(40,false);body=textInput.field(2000,true);
        atlas=new TextureAtlas(resources.apply("sprites/atlas/main.atlas"));cross=renderer.image(atlas.findRegion("icon_cross"));
        content.setTheme("");listing.setTheme("");entries.setTheme("");frame.setTitle(strings.get(5827));frame.add(content);
        message=new MessageDialog(owner,strings,mobile,background,()->{});confirm=new ActionConfirmDialog(owner,strings,mobile,background,()->{});
        int[] ids={5835,5834,5848};
        for(int i=0;i<3;i++){final int index=i;tabs[i]=new Button(strings.get(ids[i]));tabs[i].setModel(new ToggleButtonModel());tabs[i].setTheme("/player-mail-tab");tabs[i].setCanAcceptKeyboardFocus(false);tabs[i].addCallback(()->select(index));content.add(tabs[i]);}
        content.add(listing);scroll.setTheme("/mail-inner");scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);scroll.setContent(entries);listing.add(scroll);listing.add(counts);listing.add(previous);listing.add(pageLabel);listing.add(next);
        previous.setCanAcceptKeyboardFocus(false);next.setCanAcceptKeyboardFocus(false);previous.addCallback(()->{if(page>0){page--;refresh();}});next.addCallback(()->{if((page+1)*10<total){page++;refresh();}});
        if(mobile){pager.setTheme("");listing.add(pager);previous.setVisible(false);next.setVisible(false);pageLabel.setVisible(false);counts.setTheme("/player-mail-counts");}
        compose.setTheme("/player-mail-form");recipient.setMaxTextLength(16);subject.setMaxTextLength(40);body.setMaxTextLength(2000);body.setMultiLine(true);body.setTheme("/player-mail-body");
        composeScroll.setTheme("/mail-inner");composeScroll.setFixed(ScrollPane.Fixed.HORIZONTAL);composeScroll.setContent(compose);content.add(composeScroll);
        detailScroll.setTheme("/mail-inner");detailScroll.setFixed(ScrollPane.Fixed.HORIZONTAL);content.add(detailScroll);detailScroll.setVisible(false);
        send=new Button(strings.get(5834));clear=new Button(strings.get(5841));send.addCallback(this::send);clear.addCallback(()->confirm.show(strings.get(5816),()->{recipient.setText("");subject.setText("");body.setText("");}));
        var fields=new ArrayList<Widget[]>();fields.add(new Widget[]{label(5828),recipient});fields.add(new Widget[]{label(5829),subject});fields.add(new Widget[]{label(5830),body});
        // The existing local service has no attachment containers. Keep their reference fields visible
        // and disabled until item/monster transfer support is supplied, rather than simulate a transfer.
        var money=new EditField();money.setText("0");money.setEnabled(false);fields.add(new Widget[]{label(5831),money});
        fields.add(new Widget[]{label(5832),attachmentSlots()});fields.add(new Widget[]{label(5833),attachmentSlots()});
        var fee=new EditField();fee.setText("$100");fee.setEnabled(false);fields.add(new Widget[]{label(5840),fee});
        var h=compose.createParallelGroup();var v=compose.createSequentialGroup();
        for(var row:fields){h.addGroup(compose.createSequentialGroup(row));v.addGroup(compose.createParallelGroup(row));}
        h.addGroup(compose.createSequentialGroup().addWidget(clear).addGap().addWidget(send));v.addGroup(compose.createParallelGroup(clear,send));compose.setHorizontalGroup(h);compose.setVerticalGroup(v);
        select(0);setSending(false);
    }
    private Label label(int id){var label=new Label(strings.get(id));label.setTheme("/player-mail-label");return label;}
    private Widget attachmentSlots(){return new Widget(){
        {setTheme("");for(int i=0;i<5;i++){var slot=new Button();slot.setTheme("/inventory-button");slot.setEnabled(false);add(slot);}}
        @Override public int getPreferredInnerWidth(){return (mobile?64:40)*5+20;}
        @Override public int getPreferredInnerHeight(){return mobile?64:40;}
        @Override protected void layout(){int size=mobile?64:40;for(int i=0;i<5;i++){getChild(i).setPosition(getInnerX()+i*(size+5),getInnerY());getChild(i).setSize(size,size);}}
    };}
    public void bind(Selection selection,GameService.Character character){this.selection=selection;this.character=character;version++;recipient.setText("");subject.setText("");body.setText("");select(0);entries.removeAllChildren();}
    public void mailboxAccess(boolean access){mailboxAccess=access;setSending(sending);}
    private void setSending(boolean pending){sending=pending;send.setEnabled(mailboxAccess&&!pending);clear.setEnabled(!pending);for(var button:tabs)button.setEnabled(!pending);}
    public boolean editing(){return recipient.hasKeyboardFocus()||subject.hasKeyboardFocus()||body.hasKeyboardFocus();}
    public boolean hasDialog(){return message.isOpen()||confirm.isOpen();}
    public void back(){if(sending)return;if(confirm.isOpen())confirm.closePopup();else if(message.isOpen())message.closePopup();else if(selected!=null){selected=null;detailScroll.setVisible(false);resize(false);}else closePopup();}
    private <T> void request(CompletionStage<T> operation,Consumer<T> completed){
        long request=++version;String id=character.id();operation.whenComplete((result,error)->Gdx.app.postRunnable(()->{
            if(!isOpen()||request!=version||character==null||!character.id().equals(id))return;
            if(error!=null){Gdx.app.error("mail","Mail operation failed",error);message.show(strings.get(5820));setSending(false);}else completed.accept(result);
        }));
    }
    private void select(int index){tab=index;page=0;selected=null;detailScroll.setVisible(false);entries.removeAllChildren();counts.setText(tab==2?" ":strings.format(5839,"0","250"));for(int i=0;i<3;i++)((ToggleButtonModel)tabs[i].getModel()).setSelected(i==tab);listing.setVisible(tab!=1);composeScroll.setVisible(tab==1);resize(false);if(isOpen())refresh();}
    @Override protected void refresh(){
        if(character==null||selection==null||game==null)return;
        if(tab!=1)request(game.mail(selection,character.id(),tab==2,page),this::setPage);
    }
    private void setPage(PlayerData.MailPage data){
        total=data.total();counts.setText(tab==2?strings.get(5848):strings.format(5839,Integer.toString(total),"250"));pageLabel.setText((page+1)+" / "+Math.max(1,(total+9)/10));previous.setEnabled(page>0);next.setEnabled((page+1)*10<total);entries.removeAllChildren();
        entries.add(mailRow(null));
        for(var mail:data.entries()){
            entries.add(mailRow(mail));
        }if(mobile)updatePager();entries.invalidateLayout();content.invalidateLayout();
    }
    /** PaginationController.JK0: numbered pages and the neighbouring-page arrows. */
    private void updatePager(){
        pager.removeAllChildren();int pages=Math.max(1,(total+9)/10),first=Math.max(0,page-3),last=Math.min(pages,first+7);first=Math.max(0,last-7);
        addPage("<<",page-1,page>0);for(int i=first;i<last;i++)addPage(Integer.toString(i+1),i,i!=page);addPage(">>",page+1,page+1<pages);pager.invalidateLayout();
    }
    private void addPage(String text,int target,boolean enabled){var button=new Button(text);button.setTheme("/player-mail-page");button.setCanAcceptKeyboardFocus(false);button.setEnabled(enabled);if(target==page)button.getAnimationState().setAnimationState(Button.STATE_SELECTED,true);button.addCallback(()->{page=target;refresh();});pager.add(button);}
    private Widget mailRow(PlayerData.Mail mail){return new Widget(){
        final int[] weights=mobile?(tab==2?new int[]{300,400,125}:new int[]{300,400,125,60}):(tab==2?new int[]{100,200,125}:new int[]{100,200,125,60});
        {
            setTheme("");
            String[] texts=mail==null?new String[]{strings.get(tab==2?5828:5836),strings.get(5829),strings.get(5837),strings.get(5838)}:
                new String[]{tab==2?mail.recipient():mail.sender(),mail.subject(),new SimpleDateFormat("yyyy-MM-dd").format(new Date(mail.createdAt()*1000)),""};
            for(int i=0;i<weights.length;i++){
                if(mail==null){var cell=new Label(texts[i]);cell.setTheme("/player-mail-header");add(cell);}
                else{
                    final int column=i;var cell=new Button(texts[i]){
                        @Override protected void paintWidget(GUI gui){super.paintWidget(gui);if(column==3){int size=mobile?24:16;cross.draw(getAnimationState(),getX()+(getWidth()-size)/2,getY()+(getHeight()-size)/2,size,size);}}
                    };
                    cell.setTheme("/player-mail-row");cell.setCanAcceptKeyboardFocus(false);cell.setTooltipContent(column==3?strings.get(5838):texts[i]);
                    cell.addCallback(()->{if(column==3)request(game.deleteMail(selection,character.id(),mail.id(),false),ignored->{selected=null;detailScroll.setVisible(false);resize(false);refresh();});else request(game.readMail(selection,character.id(),mail.id()),MailWindow.this::read);});add(cell);
                }
            }
        }
        @Override protected void layout(){int total=Arrays.stream(weights).sum(),sum=0;for(int i=0;i<weights.length;i++){int left=getInnerWidth()*sum/total;sum+=weights[i];int right=getInnerWidth()*sum/total;getChild(i).setPosition(getInnerX()+left,getInnerY());getChild(i).setSize(right-left,getInnerHeight());}}
    };}
    private void read(PlayerData.Mail mail){
        selected=mail;var panel=new DialogLayout();panel.setTheme("/player-mail-form");var from=new EditField();from.setText(tab==2?mail.recipient():mail.sender());from.setReadOnly(true);var title=new EditField();title.setText(mail.subject());title.setReadOnly(true);
        var text=new EditField();text.setTheme("/player-mail-body");text.setMultiLine(true);text.setText(WordWrapTextFormatter.w70(mail.body(),48));text.setReadOnly(true);text.setMinSize(150,150);var reply=new Button(strings.get(5844));reply.setVisible(tab!=2);reply.addCallback(()->{select(1);recipient.setText(mail.sender());subject.setText(strings.get(5845)+" "+mail.subject());body.requestKeyboardFocus();});
        var date=new Label(new SimpleDateFormat("yyyy-MM-dd").format(new Date(mail.createdAt()*1000)));
        panel.setHorizontalGroup(panel.createParallelGroup(title,text,reply).addGroup(panel.createSequentialGroup(from,date)));panel.setVerticalGroup(panel.createSequentialGroup().addGroup(panel.createParallelGroup(from,date)).addWidget(title).addWidget(text).addWidget(reply));
        detailScroll.setContent(panel);detailScroll.setVisible(true);resize(true);content.invalidateLayout();
    }
    private void send(){
        if(!mailboxAccess||character==null)return;
        if(!recipient.getText().matches("[a-zA-Z]{2,16}")){message.show(strings.get(5804));return;}
        if(body.getText().isEmpty()){message.show(strings.get(5806));return;}
        if(subject.getText().isEmpty())subject.setText(strings.get(5905));setSending(true);
        request(game.sendMail(selection,character.id(),recipient.getText(),subject.getText(),body.getText()),result->{setSending(false);if(result==PlayerData.MailResult.SUCCESS){recipient.setText("");subject.setText("");body.setText("");select(2);}else message.show(strings.get(result==PlayerData.MailResult.INVALID_RECIPIENT?5804:result==PlayerData.MailResult.INVALID_SUBJECT?5805:result==PlayerData.MailResult.INVALID_BODY?5806:5801));});
    }
    private void resize(boolean detail){if(!isOpen()||mobile)return;int width=Math.min(getOwner().getInnerWidth(),detail?980:510);frame.setSize(width,frame.getHeight());frame.setPosition(getOwner().getInnerX()+(getOwner().getInnerWidth()-width)/2,frame.getY());}
    private void layoutMail(){
        int h=mobile?60:28,x=content.getInnerX(),y=content.getInnerY();
        // The current Android application keeps these tabs across the 60-pixel header.
        int start=mobile?350:0,tabWidth=mobile?Math.max(0,Math.min(660,content.getInnerWidth()-start-104)):content.getInnerWidth();
        for(int i=0;i<3;i++){int left=i*tabWidth/3,right=(i+1)*tabWidth/3;tabs[i].setPosition(x+start+left,y);tabs[i].setSize(right-left,h);}
        int top=y+h+5,height=Math.max(0,content.getInnerHeight()-h-5),width=selected==null?(mobile?Math.min(900,content.getInnerWidth()):content.getInnerWidth()):(content.getInnerWidth()-8)/2;
        listing.setPosition(x,top);listing.setSize(width,height);composeScroll.setPosition(x,top);composeScroll.setSize(mobile?Math.min(800,content.getInnerWidth()):content.getInnerWidth(),height);detailScroll.setPosition(x+width+8,top);detailScroll.setSize(Math.max(0,content.getInnerWidth()-width-8),height);
        body.setMinSize(200,mobile?200:150);
    }
    private void layoutList(){int x=listing.getInnerX(),y=listing.getInnerY(),width=listing.getInnerWidth(),height=listing.getInnerHeight();counts.setPosition(x+(mobile?10:0),y);counts.setSize(width,mobile?36:24);scroll.setPosition(x,y+(mobile?40:28));scroll.setSize(width,Math.max(0,height-(mobile?105:64)));previous.setPosition(x+width/2-85,listing.getInnerBottom()-30);previous.setSize(32,26);pageLabel.setPosition(x+width/2-48,previous.getY());pageLabel.setSize(96,26);next.setPosition(x+width/2+53,previous.getY());next.setSize(32,26);if(mobile){pager.setPosition(x,listing.getInnerBottom()-60);pager.setSize(width,60);}}
    @Override public void closePopup(){
        if(sending)return;
        boolean open=isOpen();version++;textInput.close();confirm.closePopup();message.closePopup();super.closePopup();
        if(open&&game!=null&&selection!=null&&character!=null)game.closeMailbox(selection,character.id());mailboxAccess=false;
    }
    public void forceClose(){setSending(false);closePopup();}
    @Override public void close(){forceClose();atlas.dispose();}
}
