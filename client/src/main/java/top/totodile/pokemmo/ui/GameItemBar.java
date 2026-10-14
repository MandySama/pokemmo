package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import top.totodile.pokemmo.ClientStrings;

import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;

/**
 * 世界内上方快捷道具栏：九个栏位按 1-9 绑定道具。
 * 图标在后台解码，纹理只在 GUI 线程创建和释放；进入世界后需要显式 open，离开世界调用 close。
 */
final class GameItemBar extends Widget implements AutoCloseable {
    private static final int SLOTS = 9;
    private final Slot[] slots = new Slot[SLOTS];
    private final int[] itemIds = new int[SLOTS];
    private final CatalogIcons icons = new CatalogIcons();
    private IntUnaryOperator iconLevel = ignored -> 0;
    private IntConsumer activateListener;
    private int activeSlot = -1;
    private int slotWidth = 40, slotHeight = 40, gap = 10;
    private int topOffset = 20;

    int topOffset() { return topOffset; }
    private boolean active;
    private java.util.Map<Integer,Integer> inventory = java.util.Map.of();

    void setInventory(java.util.Map<Integer,Integer> values) {
        inventory = java.util.Map.copyOf(values);
        for (Slot slot : slots) slot.updateQuantity();
    }

    GameItemBar() {
        setTheme("/game-item-bar");
        setCanAcceptKeyboardFocus(false);
        for (int i = 0; i < SLOTS; i++) {
            Slot slot = new Slot(i);
            slots[i] = slot;
            slot.setTheme("slot");
            slot.setCanAcceptKeyboardFocus(false);
            slot.setText("");
            int index = i;
            slot.addCallback(() -> activate(index));
            add(slot);
        }
        setVisible(false);
    }

    /** 栏位图标来源由外部注入，避免控件直接依赖解码范围。 */
    void setIconLevel(IntUnaryOperator level) {
        iconLevel = level == null ? ignored -> 0 : level;
    }

    /** 激活栏位（按 1-9 或点击栏位）时回调，由外部决定自行车、城镇地图等动作。 */
    void setActivateListener(IntConsumer listener) {
        activateListener = listener;
    }

    void setGameVisible(boolean visible) {
        setVisible(visible);
        if (visible == active) return;
        active = visible;
        if (!visible) { close(); return; }
        icons.open();
    }

    /** 写入某一栏位绑定的道具编号，0 表示清空。 */
    void assign(int slot, int itemId) {
        if (slot < 0 || slot >= SLOTS) return;
        itemIds[slot] = itemId;
        slots[slot].setText(itemId == 0 ? "" : Integer.toString(slot + 1));
        slots[slot].setTooltipContent(itemId == 0 ? null : ClientStrings.get(top.totodile.pokemmo.protocol.ItemCatalog.get(itemId).nameId()));
        slots[slot].setHighlighted(activeSlot == slot && itemId != 0);
        slots[slot].updateQuantity();
        slots[slot].invalidateLayout();
    }

    void clear(int slot) {
        assign(slot, 0);
    }

    int itemId(int slot) {
        return slot >= 0 && slot < SLOTS ? itemIds[slot] : 0;
    }

    /** 返回该道具已注册的栏位（0 起），未注册为 -1。 */
    int slotOf(int itemId) {
        for (int i = 0; i < SLOTS; i++) if (itemIds[i] == itemId) return i;
        return -1;
    }

    /** 高亮并触发栏位：数字键与鼠标点击共用同一入口。 */
    void activate(int slot) {
        if (slot < 0 || slot >= SLOTS) return;
        activeSlot = slot;
        for (int i = 0; i < SLOTS; i++) slots[i].setHighlighted(i == slot && itemIds[i] != 0);
        if (activateListener != null) activateListener.accept(slot);
    }

    @Override
    protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        slotWidth = theme.getParameter("slotWidth", 40);
        slotHeight = theme.getParameter("slotHeight", 40);
        gap = theme.getParameter("slotGap", 10);
        topOffset = theme.getParameter("topOffset", 20);
    }

    @Override
    protected void layout() {
        int index = 0;
        for (Slot slot : slots) {
            slot.setPosition(getInnerX() + index * (slotWidth + gap), getInnerY());
            slot.setSize(slotWidth, slotHeight);
            index++;
        }
    }

    @Override
    public int getPreferredInnerWidth() {
        return SLOTS * slotWidth + (SLOTS - 1) * gap;
    }

    @Override
    public int getPreferredInnerHeight() {
        return slotHeight;
    }

    @Override
    public void close() {
        active = false;
        activeSlot = -1;
        icons.close();
        for (Slot slot : slots) slot.setHighlighted(false);
    }

    /** 单个栏位，绘制数字与道具图标；激活时先叠加高亮再绘制图标。 */
    private final class Slot extends Button {
        private final int index;
        private Image highlight;
        private Image sweetScentIcon;
        private boolean highlighted;
        private final Label quantity = new Label();
        private int quantityRight = 6, quantityBottom = 5;

        Slot(int index) {
            this.index = index;
            quantity.setTheme("quantity");
            quantity.setCanAcceptKeyboardFocus(false);
            quantity.setVisible(false);
            add(quantity);
        }

        void updateQuantity() {
            var item = top.totodile.pokemmo.protocol.ItemCatalog.get(itemIds[index]);
            boolean fixed = item != null && item.id() == top.totodile.pokemmo.HotbarSettings.SWEET_SCENT_ITEM;
            boolean consumable = item != null && (item.pocket() == 1 || item.pocket() == 4
                    || item.pocket() == 5 || item.pocket() == 6
                    || top.totodile.pokemmo.protocol.ItemLoot.usable(item.id()));
            quantity.setText(fixed ? "6" : consumable ? Integer.toString(inventory.getOrDefault(item.id(), 0)) : "");
            quantity.setVisible(fixed || consumable);
            invalidateLayout();
        }

        @Override
        protected void layout() {
            super.layout();
            quantity.adjustSize();
            quantity.setPosition(getRight() - quantity.getWidth() - quantityRight, getBottom() - quantity.getHeight() - quantityBottom);
        }

        void setHighlighted(boolean value) { highlighted = value; }

        @Override
        protected void applyTheme(ThemeInfo theme) {
            super.applyTheme(theme);
            highlight = theme.getImage("highlight");
            sweetScentIcon = theme.getImage("sweetScentIcon");
            quantityRight = theme.getParameter("quantityRight", 6);
            quantityBottom = theme.getParameter("quantityBottom", 5);
        }

        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if (highlighted && highlight != null) highlight.draw(getAnimationState(), getX(), getY(), getWidth(), getHeight());
            if (itemIds[index] == 0) return;
            Image icon = itemIds[index] == top.totodile.pokemmo.HotbarSettings.SWEET_SCENT_ITEM && sweetScentIcon != null
                    ? sweetScentIcon : icons.image(gui, false, itemIds[index]);
            if (icon != null) icon.draw(getAnimationState(), getX() + (getWidth() - 24) / 2, getY() + (getHeight() - 24) / 2, 24, 24);
        }
    }
}
