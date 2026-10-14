package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.protocol.ItemCatalog;
import top.totodile.pokemmo.protocol.SearchPackets;
import java.util.*;
import java.util.function.Consumer;

/** 综合目录采用可见行复用，避免为全部结果创建数千控件。 */
final class SearchWindow extends ResizableFrame implements AutoCloseable {
    private record Entry(boolean pokemon, int id, String name) { }
    private final EditField query = new EditField();
    private final Button search = new Button(ClientStrings.get(9501));
    private final ToggleButton items = new ToggleButton(ClientStrings.get(1401));
    private final ToggleButton pokemon = new ToggleButton(ClientStrings.get(0));
    private final Label itemLabel = new Label(ClientStrings.get(1401));
    private final Label pokemonLabel = new Label(ClientStrings.get(0));
    private final Label[] headers = new Label[4];
    private final Label count = new Label();
    private final Rows rows = new Rows();
    private final ScrollPane scroll = new ScrollPane(rows);
    private final CatalogIcons icons = new CatalogIcons();
    private List<Entry> entries = List.of();
    private Consumer<SearchPackets.Request> add = request -> { };
    private PopupWindow amountPopup;
    private boolean busy;
    private final boolean[] romAvailable = new boolean[5];
    private int rowHeight = 38, sidebar = 112;

    SearchWindow(Runnable close) {
        setTheme("/search-window");
        setTitle(ClientStrings.get(9500));
        setResizableAxis(ResizableAxis.NONE);
        addCloseCallback(close);
        query.setMaxTextLength(64);
        query.addCallback(key -> filter());
        search.addCallback(this::filter);
        items.setTheme("category"); pokemon.setTheme("category");
        items.setText(""); pokemon.setText("");
        itemLabel.setLabelFor(items); pokemonLabel.setLabelFor(pokemon);
        items.setActive(true);
        items.addCallback(this::filter); pokemon.addCallback(this::filter);
        add(query); add(search); add(items); add(pokemon);
        add(itemLabel); add(pokemonLabel);
        int[] titles = {9502,9503,9504,9505};
        for (int i = 0; i < headers.length; i++) { headers[i] = new Label(ClientStrings.get(titles[i])); headers[i].setTheme("header"); add(headers[i]); }
        scroll.setTheme("entries"); scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        add(scroll); add(count);
        setHardVisible(false);
    }
    void setAddAction(Consumer<SearchPackets.Request> callback) { add = callback; }
    void setBusy(boolean value) { busy = value; rows.update(); }
    boolean editingSearch() { return query.hasKeyboardFocus() || amountPopup != null && amountPopup.isOpen(); }
    void open() {
        try {
            var roms = new top.totodile.pokemmo.RomFiles(top.totodile.pokemmo.ClientFiles.data("../roms"));
            for (int i=0;i<romAvailable.length;i++) romAvailable[i] = roms.selected(i) != null;
        } catch (java.io.IOException exception) { java.util.Arrays.fill(romAvailable,false); }
        icons.open(); filter(); setHardVisible(true); query.requestKeyboardFocus();
    }
    private void filter() {
        String text = java.text.Normalizer.normalize(query.getText().strip(), java.text.Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        List<Entry> matches = new ArrayList<>();
        if (items.isActive()) for (var item : ItemCatalog.items()) {
            if (item.source() < 0 || romAvailable[item.source()]) append(matches, new Entry(false, item.id(), ClientStrings.get(item.nameId())), text);
        }
        if (pokemon.isActive()) for (int id = 1; id <= 649; id++) append(matches, new Entry(true, id, ClientStrings.get(150000 + id)), text);
        entries = List.copyOf(matches);
        count.setText(ClientStrings.get(9506).replace("{00}", Integer.toString(entries.size())));
        scroll.setScrollPositionY(0); rows.invalidateLayout(); invalidateLayout();
    }
    private static void append(List<Entry> result, Entry entry, String query) {
        if (query.isEmpty() || java.text.Normalizer.normalize(entry.name(), java.text.Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).contains(query)
                || Integer.toString(entry.id()).equals(query)) result.add(entry);
    }
    private void ask(Entry entry) {
        if (busy || !entry.pokemon() && !ItemCatalog.get(entry.id()).addable() || amountPopup != null && amountPopup.isOpen()) return;
        int maximum = entry.pokemon() ? 100 : ItemCatalog.get(entry.id()).maxStack();
        if (maximum == 1) { add.accept(new SearchPackets.Request(SearchPackets.ADD_ITEM, entry.id(), 1)); return; }
        var dialog = new DialogLayout(); dialog.setTheme("/search-window.amount-dialog");
        var message = new Label(ClientStrings.get(entry.pokemon() ? 9508 : 9507));
        var amount = new EditField(); amount.setMaxTextLength(4); amount.setText("100");
        var ok = new Button(ClientStrings.get(52)); var cancel = new Button(ClientStrings.get(53));
        var popup = new PopupWindow(this) {
            @Override protected void layout() {
                dialog.adjustSize(); dialog.setPosition(getX() + (getWidth() - dialog.getWidth()) / 2, getY() + (getHeight() - dialog.getHeight()) / 2);
            }
        };
        popup.setTheme("/rom-modal"); popup.setCloseOnClickedOutside(false);
        amountPopup = popup;
        Runnable validate = () -> {
            int value;
            try { value = Integer.parseInt(amount.getText()); } catch (NumberFormatException exception) { value = 0; }
            ok.setEnabled(value >= 1 && value <= maximum);
        };
        Runnable submit = () -> {
            validate.run(); if (!ok.isEnabled()) return;
            int value = Integer.parseInt(amount.getText()); popup.closePopup();
            add.accept(new SearchPackets.Request(entry.pokemon() ? SearchPackets.ADD_POKEMON : SearchPackets.ADD_ITEM, entry.id(), value));
        };
        amount.addCallback(key -> { validate.run(); if (key == Event.KEY_RETURN) submit.run(); });
        ok.addCallback(submit); cancel.addCallback(popup::closePopup);
        dialog.setHorizontalGroup(dialog.createParallelGroup(message,amount).addGroup(dialog.createSequentialGroup(ok,cancel)));
        dialog.setVerticalGroup(dialog.createSequentialGroup(message,amount).addGroup(dialog.createParallelGroup(ok,cancel)));
        popup.add(dialog); validate.run(); popup.openPopupCentered(); amount.requestKeyboardFocus(); amount.selectAll();
    }
    @Override protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        rowHeight = theme.getParameter("rowHeight", 38); sidebar = theme.getParameter("sidebarWidth",112);
    }
    @Override protected void layout() {
        super.layout();
        int left = getX() + sidebar + 8, width = Math.max(0, getWidth() - sidebar - 24);
        query.setPosition(left, getY() + 50); query.setSize(Math.max(0,width-155),30);
        search.setPosition(left+width-150,getY()+50);search.setSize(150,30);
        items.setPosition(getX()+10,getY()+94);items.setSize(26,26);
        pokemon.setPosition(getX()+10,getY()+124);pokemon.setSize(26,26);
        itemLabel.setPosition(getX()+42,getY()+94);itemLabel.setSize(sidebar-42,26);
        pokemonLabel.setPosition(getX()+42,getY()+124);pokemonLabel.setSize(sidebar-42,26);
        int[] x = {0,65,135,202}, w = {60,65,62,Math.max(0,width-202)};
        for(int i=0;i<4;i++){headers[i].setPosition(left+x[i],getY()+94);headers[i].setSize(w[i],28);}
        scroll.setPosition(left,getY()+128);scroll.setSize(width,Math.max(0,getHeight()-158));
        count.setPosition(left,getBottom()-27);count.setSize(width,22);
    }
    @Override public void close() {
        if(amountPopup!=null)amountPopup.closePopup(); amountPopup=null; icons.close();
    }
    private final class Rows extends Widget {
        private final List<Row> pool = new ArrayList<>();
        Rows(){setTheme("rows");}
        void update(){for(var row:pool)row.updateEnabled();}
        @Override public int getPreferredInnerHeight(){return entries.size()*rowHeight;}
        @Override public int getPreferredInnerWidth(){return 510;}
        @Override protected void paint(GUI gui){ invalidateLayout(); validateLayout(); super.paint(gui); }
        @Override protected void layout(){
            int first=Math.min(entries.size(),Math.max(0,scroll.getScrollPositionY()/rowHeight));
            int visible=Math.min(entries.size()-first, Math.max(1,scroll.getHeight()/rowHeight+2));
            while(pool.size()<visible){var row=new Row();pool.add(row);add(row);}
            for(int i=0;i<pool.size();i++){
                var row=pool.get(i);row.setVisible(i<visible);
                if(i<visible){row.assign(entries.get(first+i));row.setPosition(getX(),getY()+(first+i)*rowHeight);row.setSize(getWidth(),rowHeight);}
            }
        }
    }
    private final class Row extends Widget {
        private Entry entry;
        private final Label type=new Label(),region=new Label(),id=new Label(),name=new Label();
        private final Button add=new Button(ClientStrings.get(9509)){
            @Override protected void paintWidget(GUI gui){
                super.paintWidget(gui);
                if(entry!=null){var image=icons.image(gui,entry.pokemon(),entry.id());if(image!=null)image.draw(getAnimationState(),getX()+2,getY()+2,24,24);}
            }
        };
        Row(){setTheme("row");for(var label:new Label[]{type,region,id,name}){label.setTheme("cell");add(label);}add.setTheme("add");add.addCallback(()->ask(entry));add(add);}
        void updateEnabled() { add.setEnabled(!busy && entry != null && (entry.pokemon() || ItemCatalog.get(entry.id()).addable())); }
        void assign(Entry value){entry=value;type.setText(ClientStrings.get(value.pokemon()?0:1401));region.setText(ClientStrings.get(value.pokemon()||ItemCatalog.get(value.id()).region()==10?9510:250000+ItemCatalog.get(value.id()).region()));id.setText(Integer.toString(value.id()));name.setText(value.name());name.setTooltipContent(value.name());updateEnabled();}
        @Override protected void layout(){
            Widget[] fields={type,region,id,add,name};int[] x={0,65,135,202,287},w={60,65,62,80,Math.max(0,getWidth()-293)};
            for(int i=0;i<fields.length;i++){fields[i].setPosition(getX()+x[i],getY()+4);fields[i].setSize(w[i],28);}
        }
    }
}
