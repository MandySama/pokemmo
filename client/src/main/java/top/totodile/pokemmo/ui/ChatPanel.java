package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.net.ChatConnection;
import top.totodile.pokemmo.protocol.ChatHandshake;

/** 左下角聊天框只展示端点状态，输入栏在消息协议接通前禁用。 */
final class ChatPanel extends Widget implements AutoCloseable {
    private final Widget top = new Widget();
    private final Widget bottom = new Widget();
    private final ToggleButton[] tabs = new ToggleButton[5];
    private final Label messages = new Label();
    private final ScrollPane scroll = new ScrollPane(messages);
    private final Button channel = new Button();
    private final EditField input = new EditField();
    private final Button send = new Button();
    private final Button expand = new Button();
    private final Button language = new Button();
    private final Button settings = new Button();
    private final ToggleButton sound = new ToggleButton();
    private final ToggleButton lock = new ToggleButton();
    private final Button collapse = new Button();
    private final Button link = new Button();
    private final Widget resizeTop = resizeHandle(false, true);
    private final Widget resizeRight = resizeHandle(true, false);

    private Widget resizeHandle(boolean horizontal, boolean vertical) {
        return new Widget() {
            @Override protected boolean handleEvent(Event event) {
                if(lock.isActive())return false;
                if(event.getType()==Event.Type.MOUSE_BTNDOWN&&event.getMouseButton()==Event.MOUSE_LBUTTON) {
                    resizing=true;resizeWidth=horizontal;resizeHeight=vertical||event.getMouseY()<ChatPanel.this.getY()+5;
                    dragX=event.getMouseX();dragY=event.getMouseY();dragWidth=getWidthOfPanel();dragHeight=ChatPanel.this.getHeight();return true;
                }
                if(resizing)return ChatPanel.this.handleEvent(event);
                return event.isMouseEvent();
            }
        };
    }
    private int getWidthOfPanel(){return getWidth();}
    private final java.util.List<String[]> records = new java.util.ArrayList<>();
    private boolean expanded = true;
    private int preferredWidth = 408, preferredHeight = 196;
    private boolean resizing;
    private int dragX, dragY, dragWidth, dragHeight;
    private boolean resizeWidth, resizeHeight;
    private ChatConnection connection;
    private ChatConnection.State lastState;
    private boolean active;
    private boolean connectedOnce;
    private int selected;
    private String history = "";

    ChatPanel() {
        setTheme("/game-chat");
        setCanAcceptKeyboardFocus(false);
        top.setTheme("top"); add(top);
        int[] names = {9620, 9621, 9622, 9623, 9624};
        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            tabs[i] = new ToggleButton(ClientStrings.get(names[i]));
            tabs[i].setTheme("tab"); tabs[i].setCanAcceptKeyboardFocus(false);
            tabs[i].addCallback(() -> select(index)); top.add(tabs[i]);
        }
        messages.setTheme("messages"); messages.setCanAcceptKeyboardFocus(false);
        scroll.setTheme("scroll"); scroll.setFixed(ScrollPane.Fixed.HORIZONTAL); add(scroll);
        bottom.setTheme("bottom"); add(bottom);
        channel.setText(ClientStrings.get(9600)); channel.setTheme("channel"); channel.setEnabled(false); bottom.add(channel);
        input.setTheme("input"); input.setEnabled(false); bottom.add(input);
        send.setText(ClientStrings.get(9602)); send.setTheme("send"); send.setEnabled(false); bottom.add(send);
        expand.setTheme("expand"); expand.setTooltipContent(ClientStrings.get(9625)); expand.addCallback(() -> setExpanded(true)); add(expand);
        setup(language, "language", 9626, () -> languageMenu());
        setup(settings, "settings", 9627, () -> settingsMenu());
        setup(sound, "sound", 9628, null);
        setup(lock, "lock", 9629, null);
        setup(collapse, "collapse", 9630, () -> setExpanded(false));
        link.setText(ClientStrings.get(9631)); link.setTheme("link"); link.addCallback(() -> {
            var menu = new Menu(); menu.add(new MenuAction(ClientStrings.get(9632), () -> {}).setEnabled(false));
            menu.openPopupMenu(link, link.getX(), link.getY());
        }); bottom.add(link);
        channel.setEnabled(true); channel.addCallback(() -> {
            var menu = new Menu();
            for (int i=0;i<tabs.length;i++) { final int index=i; menu.add(tabs[i].getText(), () -> { select(index); channel.setText(tabs[index].getText()); }); }
            menu.openPopupMenu(channel, channel.getX(), channel.getY());
        });
        resizeTop.setTheme("handle");resizeRight.setTheme("handle");add(resizeTop);add(resizeRight);
        select(0); setExpanded(true); setVisible(false);
    }

    private void setup(Button button, String theme, int tooltip, Runnable action) {
        button.setTheme(theme); button.setCanAcceptKeyboardFocus(false);
        
        button.setTooltipContent(ClientStrings.get(tooltip));
        if(action!=null)button.addCallback(action);
        top.add(button);
    }

    private void languageMenu() {
        var menu = new Menu();
        for(int id:new int[]{9633,9634,9635}) {
            var choice=new MenuCheckbox(ClientStrings.get(id),new de.matthiasmann.twl.model.SimpleBooleanModel(id==9633));  menu.add(choice);
        }
        menu.openPopupMenu(language,language.getX(),language.getBottom());
    }

    private void settingsMenu() {
        var menu=new Menu();
        for(int id:new int[]{9636,9637,9638}) { var choice=new MenuCheckbox(ClientStrings.get(id),new de.matthiasmann.twl.model.SimpleBooleanModel(id==9633));  menu.add(choice); }
        menu.openPopupMenu(settings,settings.getX(),settings.getBottom());
    }

    private void select(int index) {
        selected = index;
        for (int i = 0; i < tabs.length; i++) tabs[i].setActive(i == index);
        refreshMessages();
    }

    private void refreshMessages() {
        // 系统通知沿用本地频道；其余频道只展示所属记录。
        history=records.stream().filter(record -> record[0].equals(Integer.toString(selected)))
                .map(record -> record[1]).collect(java.util.stream.Collectors.joining("\n"));
        messages.setText(history);
        scroll.setScrollPositionY(Integer.MAX_VALUE);
    }

    void addSystemMessage(String message) { addMessage(0, message); }

    void addMessage(int channelIndex, String message) {
        if(channelIndex<0||channelIndex>=tabs.length)throw new IllegalArgumentException("Invalid chat channel");
        records.add(new String[]{Integer.toString(channelIndex),message});
        while(records.size()>100)records.removeFirst();
        refreshMessages();
    }

    void setExpanded(boolean value) {
        expanded=value;
        resizeTop.setVisible(value);resizeRight.setVisible(value);
        top.setVisible(value);scroll.setVisible(value);bottom.setVisible(value);expand.setVisible(!value);
        invalidateLayout();
        if(getParent()!=null)getParent().invalidateLayout();
    }

    void place(int left, int bottom, int availableWidth, int availableHeight) {
        setSize(expanded?Math.min(preferredWidth,Math.max(24,availableWidth)):24,
                expanded?Math.min(preferredHeight,Math.max(24,availableHeight)):24);
        setPosition(left,bottom-getHeight());
    }

    @Override
    protected boolean handleEvent(Event event) {
        if(resizing) {
            if(event.getType()==Event.Type.MOUSE_DRAGGED||event.getType()==Event.Type.MOUSE_BTNUP) {
                if(resizeWidth)preferredWidth=Math.max(408,dragWidth+event.getMouseX()-dragX);
                if(resizeHeight)preferredHeight=Math.max(140,dragHeight+dragY-event.getMouseY());
                if(getParent()!=null)getParent().invalidateLayout();
                if(event.getType()==Event.Type.MOUSE_BTNUP)resizing=false;
                return true;
            }
        }
        if(expanded&&!lock.isActive()&&event.getType()==Event.Type.MOUSE_BTNDOWN&&event.getMouseButton()==Event.MOUSE_LBUTTON) {
            resizeWidth=event.getMouseX()>=getRight()-5;
            resizeHeight=event.getMouseY()<getY()+5;
            if(resizeWidth||resizeHeight){resizing=true;dragX=event.getMouseX();dragY=event.getMouseY();dragWidth=getWidth();dragHeight=getHeight();return true;}
        }
        return super.handleEvent(event);
    }

    void update(boolean visible, String host) {
        setVisible(visible);
        if (visible && !active) {
            records.clear(); refreshMessages(); connectedOnce = false; lastState = null;
            connection = new ChatConnection(System.getProperty("pokemmo.chat.host", host),
                    Integer.getInteger("pokemmo.chat.port", ChatHandshake.DEFAULT_PORT));
        } else if (!visible && active) close();
        active = visible;
        if (connection == null) return;
        var state = connection.state();
        if (state != lastState) {
            int id = switch (state) {
                case CONNECTING -> 9603;
                case CONNECTED -> 9604;
                case DISCONNECTED -> connectedOnce ? 9606 : 9605;
            };
            if (state == ChatConnection.State.CONNECTED) connectedOnce = true;
            addSystemMessage(ClientStrings.get(id));
            lastState = state;
            scroll.setScrollPositionY(Integer.MAX_VALUE);
        }
    }

    @Override
    protected void layout() {
        int x=getInnerX(),y=getInnerY(),w=getInnerWidth();
        expand.setPosition(x,y);expand.setSize(24,24);
        if(!expanded)return;
        resizeTop.setPosition(x,y);resizeTop.setSize(w,5);
        resizeRight.setPosition(x+w-5,y);resizeRight.setSize(5,getInnerHeight());
        top.setPosition(x,y+5);top.setSize(w,27);
        int left=x+4;
        for(ToggleButton tab:tabs){int width=tab.getPreferredWidth();tab.setPosition(left,y+5);tab.setSize(width,25);left+=width+6;}
        Button[] tools={language,settings,sound,lock,collapse};
        for(Button tool:tools){tool.setPosition(left,y+8);tool.setSize(20,20);left+=22;}
        scroll.setPosition(x,y+34);scroll.setSize(w,Math.max(0,getInnerHeight()-78));
        bottom.setPosition(x,getInnerBottom()-40);bottom.setSize(w,40);
        channel.setPosition(x+8,bottom.getY()+6);channel.setSize(44,29);
        input.setPosition(x+60,bottom.getY()+6);input.setSize(Math.max(0,w-166),29);
        link.setPosition(x+w-98,bottom.getY()+6);link.setSize(43,29);
        send.setPosition(x+w-47,bottom.getY()+6);send.setSize(43,29);
    }

    @Override
    public void close() {
        if (connection != null) { connection.close(); connection = null; }
        active = false;
    }
}
