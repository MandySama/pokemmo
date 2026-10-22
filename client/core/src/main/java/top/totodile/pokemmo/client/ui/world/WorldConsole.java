package top.totodile.pokemmo.client.ui.world;

import de.matthiasmann.twl.*;
import java.util.*;
import top.totodile.pokemmo.client.world.WorldScene;

/** 沿用参考控制台的半屏布局、输入框、回车执行和上下历史记录。 */
public final class WorldConsole extends Widget {
    private final WorldScene world;
    private final Label output=new Label();
    private final ScrollPane scroll=new ScrollPane(output);
    private final EditField input=new EditField();
    private final List<String> history=new ArrayList<>();
    private final Deque<String> lines=new ArrayDeque<>();
    private int historyIndex;
    public WorldConsole(WorldScene world) {
        this.world=world;
        setTheme("/console");
        output.setTheme("/label"); scroll.setTheme("scrollpane"); input.setTheme("editfield");
        scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        input.setMaxTextLength(256);
        input.addCallback(key -> {
            if(key==Event.KEY_RETURN) submit();
            else if(key==Event.KEY_UP && historyIndex>0) recall(--historyIndex);
            else if(key==Event.KEY_DOWN) {
                if(historyIndex<history.size()-1) recall(++historyIndex);
                else { historyIndex=history.size(); input.setText(""); }
            }
        });
        add(scroll); add(input); setVisible(false);
    }
    public void toggle() { show(!isVisible()); }
    public void show(boolean visible) {
        setVisible(visible); world.setInputBlocked(visible);
        if(visible) input.requestKeyboardFocus();
    }
    private void recall(int index) { input.setText(history.get(index)); input.setCursorPos(input.getText().length()); }
    private void submit() {
        String line=input.getText().trim(); if(line.isEmpty())return;
        if(history.isEmpty() || !history.get(history.size()-1).equals(line)) history.add(line);
        historyIndex=history.size(); input.setText("");
        lines.add("> "+line); lines.add(world.commands().execute(line));
        while(lines.size()>100) lines.remove();
        output.setText(String.join("\n",lines));
        output.adjustSize(); scroll.validateLayout(); scroll.setScrollPositionY(Integer.MAX_VALUE);
    }
    public void position(int x,int y,int width,int height) { setPosition(x,y); setSize(width,height/2); }
    @Override protected void layout() {
        int height=Math.max(24,input.getPreferredHeight());
        input.setPosition(getInnerX(),getInnerBottom()-height); input.setSize(getInnerWidth(),height);
        scroll.setPosition(getInnerX(),getInnerY()); scroll.setSize(getInnerWidth(),Math.max(0,getInnerHeight()-height));
    }
}
