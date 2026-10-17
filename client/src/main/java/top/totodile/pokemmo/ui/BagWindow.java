package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.RomFiles;
import top.totodile.pokemmo.rom.BlackWhiteItemIcons;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/** 背包内容取自服务端快照，分类与搜索只筛选已拥有的物品。 */
final class BagWindow extends ResizableFrame implements AutoCloseable {
    private static final int[] NAMES = {1401, 1404, 1405, 1402, 2, 3, 1406, 1403};
    private static final int[] ICONS = {5436, 5017, 5057, 5476, 5004, 5252, 5155, 5459};
    private final Pocket[] pockets = new Pocket[NAMES.length];
    private final EditField search = new EditField();
    private CompletableFuture<BufferedImage[]> iconResult;
    private int selected;
    private java.util.Map<Integer, Integer> inventory = java.util.Map.of();
    private final BagItemIcons itemIcons = new BagItemIcons();
    private java.util.function.BiConsumer<Integer,Integer> destroy = (id,amount) -> { };
    private PopupWindow destroyPopup;
    private boolean busy;
    private java.util.function.IntConsumer use = id -> { };
    void setUseAction(java.util.function.IntConsumer action){use=action;}
    void setDestroyAction(java.util.function.BiConsumer<Integer,Integer> action){destroy=action;}
    void setBusy(boolean busy){this.busy=busy;}
    private final Label groupTitle = new Label();
    private int slotWidth = 40, slotHeight = 42, gap = 8;
    private HotbarBinding hotbar;

    /** 快捷道具栏的注册入口，由外部接入，背包不直接依赖栏位控件。 */
    interface HotbarBinding {
        int slotOf(int itemId);

        void register(int itemId, int slot);

        void unregister(int itemId);
    }
    private final Widget contents = new Widget() {
        private int columns() { return Math.max(1, (getWidth() + gap) / (slotWidth + gap)); }
        @Override public int getPreferredInnerHeight() {
            int count=Math.max(0,getNumChildren()-1);
            return count==0?0:26+((count+columns()-1)/columns())*(slotHeight+gap);
        }
        @Override public int getPreferredInnerWidth() { return 460; }
        @Override protected void layout() {
            groupTitle.setPosition(getInnerX(),getInnerY());groupTitle.setSize(getInnerWidth(),22);
            for (int i = 1; i < getNumChildren(); i++) {
                int slot=i-1;
                getChild(i).setPosition(getInnerX()+slot%columns()*(slotWidth+gap),getInnerY()+26+slot/columns()*(slotHeight+gap));
                getChild(i).setSize(slotWidth,slotHeight);
            }
        }
    };
    private final ScrollPane entries = new ScrollPane(contents);

    BagWindow(Runnable close) {
        setTheme("/bag-window");
        setResizableAxis(ResizableAxis.NONE);
        addCloseCallback(close);
        search.setTheme("search");
        search.setMaxTextLength(64);
        search.setTooltipContent(ClientStrings.get(9290));
        add(search);
        search.addCallback(key -> rebuild());
        contents.setTheme("contents");
        entries.setTheme("entries"); entries.setFixed(ScrollPane.Fixed.HORIZONTAL); add(entries);
        for (int i = 0; i < pockets.length; i++) {
            int index = i;
            pockets[i] = new Pocket();
            pockets[i].setTheme("pocket");
            pockets[i].setTooltipContent(ClientStrings.get(NAMES[i]));
            pockets[i].addCallback(() -> select(index));
            add(pockets[i]);
        }
        select(0);
    }

    private boolean battleView;
    void setBattleView(boolean enabled) { battleView = enabled; }

    void open() {
        itemIcons.preload(inventory.keySet());
        search.setText("");
        setHardVisible(true);
        requestKeyboardFocus();
        if (iconResult == null) iconResult = CompletableFuture.supplyAsync(() -> {
            try {
                var rom = new RomFiles(ClientFiles.data("../roms")).selected(0);
                if (rom == null) return null;
                var source = new BlackWhiteItemIcons(rom);
                BufferedImage[] images = new BufferedImage[ICONS.length];
                for (int i = 0; i < images.length; i++) {
                    BufferedImage pixels = source.image(ICONS[i]);
                    BufferedImage strip = new BufferedImage(48, 24, BufferedImage.TYPE_INT_ARGB);
                    for (int y = 0; y < 24; y++) for (int x = 0; x < 24; x++) {
                        int color = pixels.getRGB(x, y);
                        int gray = ((color >> 16 & 255) + (color >> 8 & 255) + (color & 255)) / 3;
                        strip.setRGB(x, y, color);
                        strip.setRGB(x + 24, y, color & 0xff000000 | gray << 16 | gray << 8 | gray);
                    }
                    images[i] = strip;
                }
                return images;
            } catch (IOException exception) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Unable to load bag pocket icons", exception);
                return null;
            }
        });
    }

    void select(int index) {
        selected = Math.floorMod(index, pockets.length);
        for (int i = 0; i < pockets.length; i++) pockets[i].setActive(i == selected);
        setTitle(ClientStrings.get(1400) + " - " + ClientStrings.get(NAMES[selected]));
        rebuild();
    }

    void setInventory(java.util.Map<Integer, Integer> value) { inventory = java.util.Map.copyOf(value); itemIcons.preload(inventory.keySet()); rebuild(); }

    void setHotbarBinding(HotbarBinding binding) { hotbar = binding; }

    /** 右键道具弹出的注册菜单：注册子菜单 1-9，已注册时提供取消注册。 */
    private void openHotbarMenu(int itemId, int x, int y) {
        if (battleView) return;
        if (hotbar == null || getGUI() == null) return;
        Menu menu = new Menu();
        Menu register = new Menu(ClientStrings.get(1412));
        for (int i = 0; i < 9; i++) {
            int slot = i;
            register.add(ClientStrings.get(1413).replace("{00}", Integer.toString(i + 1)),
                    () -> hotbar.register(itemId, slot));
        }
        menu.add(register);
        if (hotbar.slotOf(itemId) >= 0) menu.add(ClientStrings.get(1414), () -> hotbar.unregister(itemId));
        menu.add(ClientStrings.get(53), () -> { });
        menu.openPopupMenu(this, x, y);
    }
    private void openItemMenu(int id,int x,int y) {
        if (battleView) return;
        if(busy)return;
        var menu=new Menu();
        menu.add(new MenuAction(ClientStrings.get(1410),()->{if(!busy)use.accept(id);}).setEnabled(top.totodile.pokemmo.protocol.ItemLoot.usable(id) || top.totodile.pokemmo.protocol.GbaBackgroundEvents.isItemfinder(id)));
        if(hotbar!=null){
            var register=new Menu(ClientStrings.get(1412));
            for(int i=0;i<9;i++){int slot=i;register.add(ClientStrings.get(1413).replace("{00}",Integer.toString(i+1)),()->hotbar.register(id,slot));}
            menu.add(register);
            if(hotbar.slotOf(id)>=0)menu.add(ClientStrings.get(1414),()->hotbar.unregister(id));
        }
        menu.add(new MenuAction(ClientStrings.get(1411),()->{}).setEnabled(false));
        menu.add(new MenuAction(ClientStrings.get(1424),()->askDestroy(id)).setEnabled(top.totodile.pokemmo.protocol.ItemCatalog.get(id).pocket()!=3));
        menu.add(ClientStrings.get(53),()->{});menu.openPopupMenu(this,x,y);
    }
    private void askDestroy(int id) {
        int maximum=inventory.getOrDefault(id,0);
        if(busy||maximum<1)return;
        if(destroyPopup!=null)destroyPopup.closePopup();
        var dialog=new DialogLayout(){
            private int minimumWidth;
            @Override protected void applyTheme(ThemeInfo info){super.applyTheme(info);minimumWidth=info.getParameter("minWidth",320);}
            @Override public int getMinWidth(){return Math.max(super.getMinWidth(),minimumWidth);}
            @Override public void adjustSize(){super.adjustSize();setSize(Math.max(minimumWidth,getWidth()),getHeight());}
        };dialog.setTheme("/bag-destroy-dialog");
        var message=new Label(ClientStrings.get(9433).replace("{00}",ClientStrings.get(top.totodile.pokemmo.protocol.ItemCatalog.get(id).nameId())));
        var amount=new ValueAdjusterInt(new de.matthiasmann.twl.model.SimpleIntegerModel(1,maximum,1));
        ((Button)amount.getChild(2)).setText("-");((Button)amount.getChild(3)).setText("+");
        var all=new Button(ClientStrings.get(49));var ok=new Button(ClientStrings.get(60));var cancel=new Button(ClientStrings.get(53));
        var popup=new PopupWindow(this){@Override protected void layout(){dialog.adjustSize();dialog.setPosition(getX()+(getWidth()-dialog.getWidth())/2,getY()+(getHeight()-dialog.getHeight())/2);}};
        popup.setTheme("/rom-modal");popup.setCloseOnClickedOutside(false);destroyPopup=popup;
        all.addCallback(()->amount.setValue(maximum));cancel.addCallback(popup::closePopup);
        ok.addCallback(()->{if(busy)return;int count=amount.getValue();if(count<1||count>inventory.getOrDefault(id,0))return;popup.closePopup();destroy.accept(id,count);});
        dialog.setHorizontalGroup(dialog.createParallelGroup(message,ok,cancel).addGroup(dialog.createSequentialGroup(amount,all)));
        dialog.setVerticalGroup(dialog.createSequentialGroup(message).addGroup(dialog.createParallelGroup(amount,all)).addWidget(ok).addWidget(cancel));
        popup.add(dialog);popup.openPopupCentered(getGUI().getWidth(),getGUI().getHeight());ok.requestKeyboardFocus();
    }

    private void rebuild() {
        contents.removeAllChildren();
        groupTitle.setTheme("group-title");
        groupTitle.setText(selected==0?ClientStrings.get(10129):ClientStrings.get(NAMES[selected])+":");
        contents.add(groupTitle);
        String query = search.getText().strip().toLowerCase(java.util.Locale.ROOT);
        for (var entry : new java.util.TreeMap<>(inventory).entrySet()) {
            var item = top.totodile.pokemmo.protocol.ItemCatalog.get(entry.getKey());
            if (item == null || item.pocket() != selected) continue;
            String name = ClientStrings.get(item.nameId());
            if (!query.isEmpty() && !name.toLowerCase(java.util.Locale.ROOT).contains(query) && !Integer.toString(item.id()).equals(query)) continue;
            int itemId = item.id();
            class ItemButton extends Button implements GUI.TooltipPositionAdjuster {
                ItemButton(){super("x"+entry.getValue());}
                @Override public void adjustTooltipPosition(TooltipPositionData position){
                    position.x=getX()+getWidth()/2;position.y=getY();position.alignment=Alignment.BOTTOM;
                }
                @Override protected void applyTheme(ThemeInfo info) {
                    super.applyTheme(info);
                    setTooltipDelay(info.getParameter("tooltipDelay",100));
                }
                @Override protected boolean handleEvent(Event event) {
                    // 右键打开注册菜单，左键仍交给背包原有交互。
                    if (event.getType() == Event.Type.MOUSE_BTNDOWN && event.getMouseButton() == 1) {
                        openHotbarMenu(itemId, event.getMouseX(), event.getMouseY());
                        return true;
                    }
                    return super.handleEvent(event);
                }
                @Override protected void paintWidget(GUI gui) {
                    super.paintWidget(gui);
                    Image image = itemIcons.image(gui, false, item.id());
                    if (image != null) image.draw(getAnimationState(),getX()+(getWidth()-24)/2,getY()+2,24,24);
                }
            }
            var slot=new ItemButton();
            slot.addCallback(()->openItemMenu(itemId,slot.getX()+slot.getWidth()/2,slot.getY()+slot.getHeight()/2));
            slot.setTheme("item");
            slot.setCanAcceptKeyboardFocus(false);
            slot.setTooltipContent(tooltip(item.id(), name));
            contents.add(slot);
        }
        groupTitle.setVisible(contents.getNumChildren()>1);
        entries.setScrollPositionY(0);
        contents.invalidateLayout(); invalidateLayout();
    }

    private static String tooltip(int id,String name) {
        String description;
        try { description=ClientStrings.get(top.totodile.pokemmo.protocol.ItemCatalog.get(id).descriptionId()).replace("\\n","\n"); }
        catch(IllegalArgumentException error) { description=""; }
        StringBuilder result=new StringBuilder(name);
        if(!description.isBlank()) {
            result.append('\n');int column=0;
            for(char character:description.toCharArray()) {
                if(character=='\n'){result.append(character);column=0;continue;}
                if(column>=26){result.append('\n');column=0;}
                result.append(character);column++;
            }
        }
        return result.toString();
    }

    @Override protected void applyTheme(ThemeInfo info) {
        super.applyTheme(info);
        slotWidth=info.getParameter("slotWidth",40);slotHeight=info.getParameter("slotHeight",42);gap=info.getParameter("slotGap",8);
    }

    void nextPocket(boolean backwards) { select(selected + (backwards ? -1 : 1)); }
    boolean editingSearch() { return search.hasKeyboardFocus(); }

    @Override
    protected void layout() {
        super.layout();
        entries.setPosition(getX()+15,getY()+78); entries.setSize(Math.max(0,getWidth()-30),Math.max(0,getHeight()-95));
        search.setPosition(getRight() - 210, getY() + 40);
        search.setSize(180, 28);
        for (int i = 0; i < pockets.length; i++) {
            pockets[i].setPosition(getX() + 10 + i * 53, getY() + 10);
            pockets[i].setSize(53, 26);
        }
    }

    @Override
    public void close() {
        if(destroyPopup!=null)destroyPopup.closePopup();destroyPopup=null;
        itemIcons.close();
        if (iconResult != null) iconResult.cancel(true);
        iconResult = null;
        for (Pocket pocket : pockets) pocket.release();
    }

    private final class Pocket extends ToggleButton {
        private Texture texture;
        private Image image, inactive;
        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if (image == null && iconResult != null && iconResult.isDone() && !iconResult.isCancelled()) {
                BufferedImage[] images = iconResult.getNow(null);
                if (images != null) {
                    int index = 0;
                    while (pockets[index] != this) index++;
                    BufferedImage pixels = images[index];
                    texture = ((GlfwTwlRenderer) gui.getRenderer()).uploadImage(pixels);
                    image = texture.getImage(0, 0, 24, 24, Color.WHITE, false, Texture.Rotation.NONE);
                    inactive = texture.getImage(24, 0, 24, 24, Color.WHITE, false, Texture.Rotation.NONE);
                }
            }
            if (image != null) (isActive() ? image : inactive).draw(getAnimationState(), getX() + 15, getY());
        }
        private void release() {
            if (texture != null) texture.destroy();
            texture = null;
            image = inactive = null;
        }
    }
}
