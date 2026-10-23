package top.totodile.pokemmo.client.ui.menu;

import de.matthiasmann.twl.*;
import java.util.function.LongSupplier;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.common.TouchScrollPane;

/** 参考 MF0：登录阶段的单列侧栏，以及右上角入口的串行动画。 */
public final class MobileSideMenu extends Widget {
    private static final int MIN_WIDTH = 600;
    private static final long STEP = 150;
    private final MobileStatusIcons status;
    private final LongSupplier clock;
    private final DialogLayout content = new DialogLayout();
    private final DialogLayout entries = new DialogLayout();
    private final ScrollPane scroll;
    private final Button settings = new Button();
    private final Button close = new Button();
    private Widget header;
    private long started;
    private boolean opening, animating;
    private float statusFrom, statusOffset, panelOffset = MIN_WIDTH;

    public MobileSideMenu(ClientStrings strings, MobileStatusIcons status, LongSupplier clock,
                   java.util.function.IntFunction<de.matthiasmann.twl.renderer.Image> icons) {
        this.status = status;
        this.clock = clock;
        setTheme("/hud-panel");
        setCanAcceptKeyboardFocus(true);
        setClip(true);
        settings.setTheme("/mobile-menu-settings");
        close.setTheme("/mobile-menu-close");
        settings.setCanAcceptKeyboardFocus(false);
        close.setCanAcceptKeyboardFocus(false);
        close.addCallback(this::close);
        // 侧栏使用 defaultGap；不让通用布局自动插入 smallGap。
        content.setAddDefaultGaps(false);
        entries.setAddDefaultGaps(false);
        var column = entries.createParallelGroup();
        var rows = entries.createSequentialGroup();
        int[] textIds = {1175, 1190, 1005, 1006, 1007, 1124, 1114};
        int[] iconIds = {5502, 5428, 5465, 5475, 5467, 5621, 5547};
        for (int index = 0; index < textIds.length; index++) {
            final int iconId = iconIds[index];
            var entry = new DraggableButton() {
                private de.matthiasmann.twl.renderer.Image icon;
                @Override protected void applyTheme(ThemeInfo theme) {
                    super.applyTheme(theme);
                    icon = icons.apply(iconId);
                    setMinSize(560, 64);
                    setMaxSize(560, 64);
                }
                @Override protected void paintWidget(GUI gui) {
                    super.paintWidget(gui);
                    if (icon != null) icon.draw(getAnimationState(), getX(), getY() + 8, 48, 48);
                }
            };
            entry.setText(strings.get(textIds[index]));
            entry.setTheme("hud-item-button");
            entry.setCanAcceptKeyboardFocus(false);
            entry.setMinSize(560, 64);
            entry.setMaxSize(560, 64);
            column.addWidget(entry);
            if (index > 0) rows.addGap(DialogLayout.DEFAULT_GAP);
            rows.addWidget(entry);
        }
        entries.setHorizontalGroup(column);
        entries.setVerticalGroup(rows);
        scroll = new TouchScrollPane(entries);
        scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        scroll.setCanAcceptKeyboardFocus(false);
        var drag = scroll.createDragListener();
        for (int index = 0; index < entries.getNumChildren(); index++)
            ((DraggableButton) entries.getChild(index)).setListener(drag);
        content.setHorizontalGroup(content.createParallelGroup()
                .addGroup(content.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
                        .addWidget(settings).addGap(DialogLayout.DEFAULT_GAP).addWidget(close)
                        .addGap("menu-header-right"))
                .addWidget(scroll));
        content.setVerticalGroup(content.createSequentialGroup()
                .addGroup(content.createParallelGroup().addWidget(settings).addWidget(close))
                .addGap(35).addWidget(scroll));
        add(content);
        setVisible(false);
    }

    /** MF0.hu: in-game entries use two 260x64 columns inside the same 600-wide panel. */
    public void worldEntries(ClientStrings strings,java.util.function.IntFunction<de.matthiasmann.twl.renderer.Image> icons,
                             Widget statusHeader,Runnable bag,Runnable trainer,Runnable dex,Runnable mail,Runnable logout,Runnable exit) {
        header=statusHeader;add(header);
        entries.setHorizontalGroup(null);entries.setVerticalGroup(null);entries.removeAllChildren();
        int[] texts={1100,2807,1101,1,8034,2353,1116,1126,1161,1121,7900,1124,1159,1114};
        int[] images={5436,5454,5432,5431,5471,5452,5459,5437,5001,5265,5277,5621,5481,5547};
        Runnable[] actions={bag,null,trainer,dex,null,mail,null,null,null,null,null,null,logout,exit};
        var left=entries.createParallelGroup();var right=entries.createParallelGroup();var rows=entries.createSequentialGroup();
        var buttons=new Button[texts.length];
        for(int i=0;i<buttons.length;i++){
            final int index=i;
            var button=new Button(strings.get(texts[i])){
                @Override protected void applyTheme(ThemeInfo theme){super.applyTheme(theme);setMinSize(260,64);setMaxSize(260,64);}
                @Override protected void paintWidget(GUI gui){super.paintWidget(gui);var icon=icons.apply(images[index]);if(icon!=null)icon.draw(getAnimationState(),getX()+4,getY()+8,48,48);}
            };
            button.setTheme("hud-item-button");button.setCanAcceptKeyboardFocus(false);button.setMinSize(260,64);button.setMaxSize(260,64);
            button.setEnabled(actions[i]!=null);if(actions[i]!=null)button.addCallback(()->{close();actions[index].run();});buttons[i]=button;
            (i%2==0?left:right).addWidget(button);
            if(i%2==1){if(i>1)rows.addGap(DialogLayout.DEFAULT_GAP);rows.addGroup(entries.createParallelGroup(buttons[i-1],button));}
        }
        entries.setHorizontalGroup(entries.createSequentialGroup().addGroup(left).addGap(DialogLayout.DEFAULT_GAP).addGroup(right));entries.setVerticalGroup(rows);
        content.invalidateLayout();invalidateLayout();
    }

    public boolean contains(int x,int y){return isVisible()&&isInside(x,y);}

    public void onClientManagement(Runnable action) {
        ((Button) entries.getChild(0)).addCallback(() -> { close(); action.run(); });
    }

    public void onMods(Runnable action) {
        ((Button) entries.getChild(1)).addCallback(() -> { close(); action.run(); });
    }

    public void onExit(Runnable action) {
        ((Button) entries.getChild(6)).addCallback(() -> { close(); action.run(); });
    }

    public void onSettings(Runnable action) {
        settings.addCallback(() -> { close(); action.run(); });
    }

    public void open() {
        if (isVisible()) return;
        update();
        opening = true;
        animating = true;
        statusFrom = statusOffset;
        panelOffset = panelWidth();
        started = clock.getAsLong();
        setVisible(true);
        status.setVisible(true);
        requestKeyboardFocus();
        position();
    }

    public void close() {
        if (!isVisible()) return;
        update();
        opening = false;
        animating = true;
        statusFrom = statusOffset;
        started = clock.getAsLong();
        panelOffset = panelWidth();
        setVisible(false);
        status.setVisible(true);
        position();
    }

    public void update() {
        if (animating) {
            long elapsed = Math.max(0, clock.getAsLong() - started);
            if (opening) {
                statusOffset = mix(statusFrom, status.getWidth() + 10, ease(elapsed));
                panelOffset = mix(panelWidth(), 0, ease(elapsed - STEP));
                if (elapsed >= STEP * 2) {
                    animating = false;
                    status.setVisible(false);
                }
            } else {
                statusOffset = mix(statusFrom, 0, ease(elapsed));
                if (elapsed >= STEP) animating = false;
            }
        }
        position();
    }

    public void position() {
        Widget host = getParent();
        if (host == null) return;
        int width = panelWidth();
        setSize(width, host.getInnerHeight());
        setPosition(host.getInnerRight() - width + Math.round(panelOffset), host.getInnerY());
        status.setPosition(host.getInnerRight() - status.getWidth() - 5 + Math.round(statusOffset),
                host.getInnerY() + 5);
    }

    private int panelWidth() {
        // 包含菜单项、滚动区域边框和外层留白，不能把固定宽菜单挤出可视区域。
        return Math.max(MIN_WIDTH, content.getPreferredWidth());
    }

    private static float ease(long elapsed) {
        float t = Math.max(0, Math.min(1, elapsed / (float) STEP));
        // 参考 AnimationTween.DX 默认 Quad.INOUT。
        return t < .5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    private static float mix(float from, float to, float t) { return from + (to - from) * t; }

    @Override protected void layout() {
        content.adjustSize();
        content.setSize(getInnerWidth(),getInnerHeight());
        content.setPosition(getInnerX(), getInnerY());
        if(header!=null){header.adjustSize();header.setPosition(getX()+8,getY()+8);}
    }

    @Override protected boolean handleEvent(Event event) {
        if (event.isKeyPressedEvent() && event.getKeyCode() == Event.KEY_ESCAPE) {
            close();
            return true;
        }
        return super.handleEvent(event) || event.isMouseEvent();
    }
}
