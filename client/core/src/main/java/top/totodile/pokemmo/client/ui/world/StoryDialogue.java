package top.totodile.pokemmo.client.ui.world;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.common.ClientDialog;

import java.util.function.IntConsumer;

/** MessageBoxBubble / ConfirmRejectDialogBubble using the existing native interaction theme. */
public final class StoryDialogue extends ClientDialog {
    private final Container panel = new Container() {
        @Override protected void layout() { layoutContents(); }
        @Override protected boolean handleEvent(Event event) {
            if (event.getType() == Event.Type.MOUSE_CLICKED) {
                advance();
                return true;
            }
            return event.isMouseEvent() || super.handleEvent(event);
        }
    };
    private final boolean mobile;
    private float anchorX, anchorY;
    private boolean anchored;
    private final Label text = new Label();
    private final Button next = new Button(), yes, no;
    private IntConsumer reply;
    private java.util.List<String> pages = java.util.List.of();
    private int page;
    private boolean choice, learning;
    private final java.util.List<Button> optionButtons=new java.util.ArrayList<>();
    private final Container optionBody=new Container(){
        @Override public int getPreferredInnerHeight(){return learning?4*optionButtons.stream().mapToInt(b->Math.max(30,b.getPreferredHeight())).max().orElse(30):optionButtons.stream().mapToInt(b->Math.max(30,b.getPreferredHeight())).sum();}
        @Override protected void layout(){if(learning){int h=optionButtons.stream().mapToInt(b->Math.max(30,b.getPreferredHeight())).max().orElse(30);for(int i=0;i<optionButtons.size();i++){var b=optionButtons.get(i);int row=i<4?i/2:i-2;b.setPosition(getInnerX()+(i<4?i%2*getInnerWidth()/2:0),getInnerY()+row*h);b.setSize(i<4?getInnerWidth()/2:getInnerWidth(),h);}return;}
            int y=getInnerY();for(var b:optionButtons){int h=Math.max(30,b.getPreferredHeight());b.setPosition(getInnerX(),y);b.setSize(getInnerWidth(),h);y+=h;}}
    };
    private final top.totodile.pokemmo.client.ui.common.TouchScrollPane optionScroll=new top.totodile.pokemmo.client.ui.common.TouchScrollPane(optionBody);
    public void showChoices(String message,java.util.List<String> options,IntConsumer callback){
        show(message,false,callback);optionBody.removeAllChildren();optionButtons.clear();
        for(int i=0;i<options.size();i++){final int index=i;var button=new Button(options.get(i));button.setTheme("/button");button.addCallback(()->answer(index));optionButtons.add(button);optionBody.add(button);}
        next.setVisible(false);optionScroll.setVisible(true);optionScroll.setScrollPositionY(0);optionBody.invalidateLayout();invalidateLayout();
        if(!optionButtons.isEmpty())optionButtons.get(0).requestKeyboardFocus();
    }

    public void showLearning(String message,java.util.List<String> moves,String candidate,IntConsumer callback){
        var labels=new java.util.ArrayList<>(moves);labels.add("取消");showChoices(message,labels,callback);learning=true;
        for(int i=0;i<4;i++)optionButtons.get(i).setTheme(mobile?"/button":"/encounter-move");
        var pending=new Button(candidate);pending.setTheme(mobile?"/button":"/encounter-move");pending.setEnabled(false);optionButtons.add(4,pending);optionBody.add(pending);optionBody.invalidateLayout();invalidateLayout();
    }

    public StoryDialogue(Widget owner, ClientStrings strings, boolean mobile) {
        super(owner, mobile);
        this.mobile = mobile;
        if (getParent() instanceof PopupWindow host) host.setTheme("");
        setTheme(mobile ? "/messagebox-mobile" : "/messagebox-bubble");
        panel.setTheme("/story-npc-popup");
        text.setTheme("labelanimation");
        next.setTheme("button-ok");
        yes = new Button(strings.get(50));
        no = new Button(strings.get(51));
        yes.setTheme("button");
        no.setTheme("button");
        next.addCallback(() -> answer(0));
        text.addCallback(reason -> advance());
        yes.addCallback(() -> answer(1));
        no.addCallback(() -> answer(0));
        text.setAlignment(Alignment.TOPLEFT);
        panel.add(text);
        panel.add(next);
        panel.add(yes);
        panel.add(no);
        optionBody.setTheme("");optionScroll.setTheme("/scrollpane");optionScroll.setFixed(ScrollPane.Fixed.HORIZONTAL);optionScroll.setVisible(false);panel.add(optionScroll);
        add(panel);
        setCloseOnEscape(false);
    }

    public void show(String message, boolean choice, IntConsumer callback) {
        learning=false;optionScroll.setVisible(false);optionButtons.clear();optionBody.removeAllChildren();
        pages = java.util.Arrays.asList(message.split("\\n\\n"));
        page = 0;
        this.choice = choice;
        showPage();
        reply = callback;
        openPopup();
        positionWindow();
        invalidateLayout();
        (choice ? yes : next).requestKeyboardFocus();
    }

    public void positionWindow() {
        if (!isOpen()) return;
        if (getParent() instanceof PopupWindow host) {
            host.setTheme("");
            host.setPosition(getOwner().getInnerX(), getOwner().getInnerY());
            host.setSize(getOwner().getInnerWidth(), getOwner().getInnerHeight());
        }
        setPosition(getOwner().getInnerX(), getOwner().getInnerY());
        setSize(getOwner().getInnerWidth(), getOwner().getInnerHeight());
    }

    public void advance() {
        if(learning){int selected=0;for(int i=0;i<optionButtons.size();i++)if(optionButtons.get(i).hasKeyboardFocus()){selected=i;break;}answer(selected==5?4:selected);}
        else if (next.isVisible()) answer(0);
    }

    public boolean learningKey(int key){
        if(!isOpen()||!learning)return false;
        if(key==Event.KEY_ESCAPE){answer(4);return true;}
        if(key==Event.KEY_RETURN){advance();return true;}
        int selected=0;for(int i=0;i<optionButtons.size();i++)if(optionButtons.get(i).hasKeyboardFocus()){selected=i;break;}
        int nextIndex=switch(key){
            case Event.KEY_LEFT -> selected<4?Math.max(0,selected-1):selected;
            case Event.KEY_RIGHT -> selected<4?Math.min(3,selected+1):selected;
            case Event.KEY_UP -> selected==5?3:Math.max(0,selected-2);
            case Event.KEY_DOWN -> selected>=2?5:selected+2;
            default -> -1;
        };
        if(nextIndex>=0){optionButtons.get(nextIndex).requestKeyboardFocus();return true;}
        return false;
    }
    @Override protected boolean handleEvent(Event event){
        if(event.isKeyPressedEvent()&&learningKey(event.getKeyCode()))return true;
        return super.handleEvent(event);
    }

    private void answer(int value) {
        var callback = reply;
        if (callback == null) return;
        if (page + 1 < pages.size()) {
            page++;
            showPage();
            invalidateLayout();
            return;
        }
        reply = null;
        closePopup();
        callback.accept(value);
    }

    private void showPage() {
        text.setText(pages.get(page));
        boolean last = page + 1 == pages.size();
        next.setVisible(!choice || !last);
        yes.setVisible(choice && last);
        no.setVisible(choice && last);
    }

    public void anchor(float[] point) {
        anchored = point != null;
        if (anchored) { anchorX = point[0]; anchorY = point[1]; }
    }

    @Override
    protected void paintWidget(GUI gui) {
        if (!mobile && anchored && gui.getRenderer() instanceof
                top.totodile.pokemmo.client.twl.GdxTwlRenderer renderer) {
            // MessageBoxBubble.tE / MessageBoxManager.YK: triangle behind the bubble.
            float center = getInnerX() + getInnerWidth() / 2f;
            renderer.drawTriangle(center + 100, panel.getY() + 15,
                    center + 140, panel.getY() + 15, anchorX, anchorY,
                    new de.matthiasmann.twl.Color(0xfff4f4f4));
        }
    }

    private String wrap(String source, int width) {
        var font = text.getFont();
        if (font == null || width <= 0) return source;
        var result = new StringBuilder();
        // Native short lines are soft wraps, not paragraphs. Reflow within a page.
        source = source.replace("\r", "").replaceAll(
                "(?<=[\\p{IsHan}，。！？、：；])\\n(?=[\\p{IsHan}，。！？、：；])", "");
        for (String paragraph : source.split("\n", -1)) {
            if (result.length() > 0) result.append('\n');
            String remaining = paragraph;
            while (font.computeTextWidth(remaining) > width) {
                int end = 0, space = -1;
                for (int i = 0; i < remaining.length();) {
                    int next = i + Character.charCount(remaining.codePointAt(i));
                    if (font.computeTextWidth(remaining.substring(0, next)) > width) break;
                    end = next;
                    if (Character.isWhitespace(remaining.charAt(i))) space = i;
                    i = next;
                }
                if (end == 0) end = Character.charCount(remaining.codePointAt(0));
                if (space > 0) end = space;
                result.append(remaining, 0, end).append('\n');
                remaining = remaining.substring(end).stripLeading();
            }
            result.append(remaining);
        }
        return result.toString();
    }

    @Override
    protected void layout() {
        if (pages.isEmpty()) return;
        int width = mobile ? Math.min(getInnerWidth(), text.getPreferredWidth() + panel.getBorderHorizontal())
                : Math.min(getInnerWidth(), Math.max(600, Math.min(getInnerWidth() / 2, 900)));
        width = Math.max(Math.min(optionScroll.isVisible()?320:200, getInnerWidth()), width);
        String wrapped = wrap(pages.get(page), width - panel.getBorderHorizontal());
        if (!text.getText().equals(wrapped)) text.setText(wrapped);
        int height = Math.max(mobile ? 100 : 150,
                text.getPreferredHeight() + panel.getBorderVertical() + (mobile ? 32 : 0));
        if(optionScroll.isVisible())height=Math.min(getInnerHeight()-80,text.getPreferredHeight()+panel.getBorderVertical()+optionBody.getPreferredHeight()+15);
        panel.setSize(width, height);
        panel.setPosition(getInnerX() + (getInnerWidth() - width) / 2,
                mobile ? getInnerY() + getInnerHeight() - height - 60
                       : getInnerY() + Math.max(0, (getInnerHeight() - 720) / 4));
        layoutContents();
    }

    private void layoutContents() {
        text.setPosition(panel.getInnerX(), panel.getInnerY());
        text.setSize(panel.getInnerWidth(), text.getPreferredHeight());
        next.setSize(32, 32);
        next.setPosition(panel.getRight() - 60, panel.getBottom() - 60);
        yes.adjustSize();
        no.adjustSize();
        if(optionScroll.isVisible()){
            optionScroll.setPosition(panel.getInnerX(),text.getBottom()+5);
            optionScroll.setSize(panel.getInnerWidth(),Math.max(30,panel.getInnerBottom()-text.getBottom()-5));
        }
        no.setPosition(panel.getInnerRight() - no.getWidth(), panel.getBottom() - 60);
        yes.setPosition(no.getX() - yes.getWidth() - 5, no.getY());
    }
}
