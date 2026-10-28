package top.totodile.pokemmo.client.ui.market;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.common.PlayerWindow;

/** Reference GlobalTradeMarketWindow / ChatFilterMenuWidget. Trading transport is not connected yet. */
public final class GlobalTradeMarketWindow extends PlayerWindow {
    private final ClientStrings strings;
    private final ToggleButton[] tabs = new ToggleButton[6];
    private static final int[] PAGES={0,-1,1,2,3,4};
    private final Widget pager=new Widget(){
        @Override protected void layout(){int size=mobile?45:29,gap=8,total=getNumChildren()*(size+gap)-gap,x=getInnerX()+(getInnerWidth()-total)/2;for(int i=0;i<getNumChildren();i++){var b=getChild(i);b.setPosition(x+i*(size+gap),getInnerY());b.setSize(size,mobile?42:27);}}
    };
    private final Widget content = new Widget() {
        @Override protected void layout() { layoutMarket(); }
    };
    private final Widget page = new Widget();
    private int selected;
    private boolean atPc;

    public GlobalTradeMarketWindow(Widget owner, ClientStrings strings, boolean mobile, Runnable close) {
        super(owner, mobile, mobile ? "/mobile-market-window" : "/market-window", 900, 605, close);
        this.strings = strings;
        frame.setTitle(strings.get(8000));
        content.setTheme("");
        page.setTheme("/market-page");
        frame.add(content);
        content.add(page);pager.setTheme("");content.add(pager);
        for(String text:new String[]{"<<","1",">>"})pager.add(unavailable(text));
        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            tabs[i] = new ToggleButton(i==1?"道具市场":strings.get(PAGES[i]==4?8070:8001+PAGES[i]));
            tabs[i].setEnabled(i!=1); // Current reference source predates the item-market service.
            tabs[i].setTheme("/market-tab");
            tabs[i].setCanAcceptKeyboardFocus(false);
            tabs[i].addCallback(() -> select(PAGES[index]));
            content.add(tabs[i]);
        }
        select(0);
    }

    public void open(boolean atPc) {
        this.atPc = atPc;
        if (isOpen()) { refresh(); raiseWindow(); }
        else open();
    }

    @Override protected void refresh() { select(selected); }

    private Button unavailable(String text) {
        Button button = new Button(text);
        button.setTheme(text.equals("☒") || text.equals("↻") ? "/market-symbol" : "/button");
        button.setEnabled(false);
        return button;
    }

    private Label label(String text, boolean header) {
        Label label = new Label(text);
        label.setTheme(header ? "/market-header" : "/label");
        return label;
    }

    private void select(int index) {
        selected = index;
        for (int i = 0; i < tabs.length; i++) tabs[i].setActive(PAGES[i] == selected);
        pager.setVisible(selected<3);
        page.removeAllChildren();
        if (selected < 2) {
            page.add(unavailable(strings.get(8084)));
            EditField search = new EditField();
            search.setTheme("/editfield");
            search.setEnabled(false);
            page.add(search);
            page.add(unavailable(strings.get(8010)));
            page.add(unavailable("☒"));
            page.add(unavailable("↻"));
            page.add(unavailable(strings.get(8050)));
        } else if (selected == 2) {
            page.add(unavailable(strings.get(8042)));
            page.add(unavailable("↻"));
        } else if (selected == 3) {
            // Reference remote entry deliberately substitutes this PC-only panel.
            if (!atPc) page.add(label(strings.get(8029), false));
            else {
                for (int id : new int[]{8005, 0, 8006, 8007, 8047}) {
                    page.add(label(strings.get(id), true));
                    if (id == 8005 || id == 0) page.add(unavailable(""));
                    else {
                        EditField value = new EditField();
                        value.setTheme("/editfield");
                        value.setText(id == 8006 ? "$0" : "");
                        value.setEnabled(false);
                        page.add(value);
                    }
                }
                page.add(unavailable(strings.get(8009)));
                page.add(unavailable(strings.get(8008)));
            }
        }
        if (selected != 3) {
            String[] headers = switch (selected) {
                case 0 -> new String[]{strings.get(0), strings.get(8106), strings.get(1849).replace(":", "").replace("：", ""), strings.get(8012), strings.get(8037), strings.get(8013), strings.get(8014)};
                case 1 -> new String[]{strings.get(8015), strings.get(8016), strings.get(8012), strings.get(8013), strings.get(8014)};
                case 2 -> new String[]{strings.get(8015), strings.get(8017), strings.get(8012), strings.get(8018), strings.get(8019), strings.get(8020), strings.get(8021)};
                default -> new String[]{strings.get(8071), strings.get(8072), strings.get(8073), strings.get(5837)};
            };
            for (String header : headers) page.add(label(header, true));
        }
        content.invalidateLayout();
    }

    private void layoutMarket() {
        int x = content.getInnerX(), y = content.getInnerY(), w = content.getInnerWidth(), h = content.getInnerHeight();
        int tabWidth = mobile ? 140 : 96, tabHeight = mobile ? 76 : 26,tabX=x;
        for (int i = 0; i < tabs.length; i++) {
            int tw=mobile?tabWidth:Math.max(86,tabs[i].getPreferredInnerWidth()+28);
            tabs[i].setPosition(mobile ? x : tabX, mobile ? y + i * tabHeight : y);
            tabs[i].setSize(tw, tabHeight);tabX+=tw+2;
        }
        page.setPosition(mobile ? x + tabWidth : x, mobile ? y : y + tabHeight);
        page.setSize(Math.max(0, w - (mobile ? tabWidth : 0)), Math.max(0, h - (mobile ? 0 : tabHeight)));
        pager.setPosition(page.getInnerX(),page.getInnerBottom()-(mobile?50:32));pager.setSize(page.getInnerWidth(),mobile?45:28);
        int px = page.getInnerX(), py = page.getInnerY(), pw = page.getInnerWidth(), row = mobile ? 56 : 32;
        if (selected == 3) {
            if (atPc) {
                int labelWidth = Math.min(180, pw / 3), valueWidth = Math.min(300, pw - labelWidth - 10);
                for (int i = 0; i < 10; i++) {
                    Widget child = page.getChild(i);
                    child.setPosition(px + (i % 2 == 0 ? 0 : labelWidth + 10), py + i / 2 * (row + 10));
                    child.setSize(i % 2 == 0 ? labelWidth : valueWidth, row);
                }
                for (int i = 10; i < 12; i++) {
                    page.getChild(i).setPosition(px + (i - 10) * 145, py + 5 * (row + 10));
                    page.getChild(i).setSize(140, row);
                }
                return;
            }
            for (int i = 0; i < page.getNumChildren(); i++) {
                Widget child = page.getChild(i);
                child.setPosition(px, py + i * (row + 8));
                child.setSize(pw, row);
            }
            return;
        }
        int controls = selected < 2 ? 6 : selected == 2 ? 2 : 0;
        if (controls == 6) {
            int[] widths = {Math.min(260, pw*30/100), Math.max(60, pw - 552), 68, 26, 26, 148};
            // Keep the search toolbar inside narrower Android content areas.
            int total = java.util.Arrays.stream(widths).sum() + 5 * 4;
            float scale = Math.min(1f, pw / (float) total);
            int cx = px;
            for (int i = 0; i < controls; i++) {
                int cw = Math.max(1, (int)(widths[i] * scale));
                page.getChild(i).setPosition(cx, py);
                page.getChild(i).setSize(cw, row);
                cx += cw + 4;
            }
        } else for (int i = 0; i < controls; i++) {
            page.getChild(i).setPosition(px + i * 145, py);
            page.getChild(i).setSize(140, row);
        }
        int columns = page.getNumChildren() - controls;
        int hy = py + (controls == 0 ? 0 : row + 10);
        int[] weights=selected==0?new int[]{21,12,22,12,11,11,11}:null;
        int used=0;
        for (int i = 0; i < columns; i++) {
            Widget child = page.getChild(controls + i);
            int cw=weights==null?pw*(i+1)/columns-pw*i/columns:pw*weights[i]/100;
            child.setPosition(px+used,hy);child.setSize(Math.max(0,cw-4),row);used+=cw;
        }
    }

    @Override protected void layout() {
        super.layout();
        content.setPosition(frame.getInnerX(), frame.getInnerY());
        content.setSize(frame.getInnerWidth(), frame.getInnerHeight());
    }
}
