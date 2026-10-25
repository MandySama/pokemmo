package top.totodile.pokemmo.client.ui.inventory;

import com.badlogic.gdx.Input;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.config.ClientSettings;
import top.totodile.pokemmo.server.game.PlayerData;

import java.util.*;
import java.util.function.Consumer;

/** HotkeyBarWindow / InventoryItemSlotWidget: nine persisted item slots. */
public final class HotkeyBar extends Widget {
    private final InventoryItems items;
    private final ClientSettings settings;
    private final boolean mobile;
    private final Slot[] slots = new Slot[9];
    private List<PlayerData.Item> inventory = List.of();
    private int region, drag = -1, startX, startY, originX, originY;
    private boolean dragging, positioned;
    private Consumer<PlayerData.Item> use = item -> {};

    public HotkeyBar(InventoryItems items, ClientSettings settings, boolean mobile) {
        this.items = items;
        this.settings = settings;
        this.mobile = mobile;
        setTheme(mobile ? "/mobile-hotkeybar" : "/hotkeybar");
        setCanAcceptKeyboardFocus(false);
        for (int i = 0; i < 9; i++) {
            slots[i] = new Slot(i);
            add(slots[i]);
        }
        update();
    }

    public void onUse(Consumer<PlayerData.Item> action) {
        use = Objects.requireNonNull(action);
    }

    public void inventory(List<PlayerData.Item> value, int region) {
        this.inventory = List.copyOf(value);
        this.region = region;
        update();
    }

    private int number(String key, int fallback) {
        try {
            return Integer.parseInt(settings.get(key, Integer.toString(fallback)));
        } catch (NumberFormatException invalid) {
            return fallback;
        }
    }

    public int itemId(int index) {
        return number("client.hotbar.item_id" + (index + 1), 0);
    }

    private String objectId(int index) {
        return settings.get("client.hotbar.item_object_id" + (index + 1), "0");
    }

    private int key(int index) {
        return number("client.controls.gdx.hotbar" + (index + 1), Input.Keys.NUM_1 + index);
    }

    private PlayerData.Item stack(int index) {
        int id = itemId(index);
        String object = objectId(index);
        return inventory.stream()
                .filter(i -> i.itemId() == id && (i.region() < 0 || i.region() == region))
                .filter(i -> object.equals("0") || object.isEmpty() || object.equals(i.objectId()))
                .findFirst()
                .orElseGet(
                        () ->
                                inventory.stream()
                                        .filter(
                                                i ->
                                                        i.itemId() == id
                                                                && (i.region() < 0
                                                                        || i.region() == region))
                                        .findFirst()
                                        .orElse(null));
    }

    public void update() {
        for (int i = 0; i < 9; i++) {
            int id = itemId(i), key = key(i);
            slots[i].setText(
                    id == 0 || mobile || key <= 0 || key > 255 ? "" : Input.Keys.toString(key));
            int count =
                    inventory.stream()
                            .filter(
                                    item ->
                                            item.itemId() == id
                                                    && (item.region() < 0
                                                            || item.region() == region))
                            .mapToInt(PlayerData.Item::amount)
                            .sum();
            slots[i].quantity.setText(
                    id == 0 || items.template(id).pocket() == 2 ? "" : Integer.toString(count));
        }
    }

    public boolean activate(int index) {
        if (index < 0 || index >= 9 || itemId(index) == 0) return false;
        var stack = stack(index);
        if (stack != null && stack.amount() > 0) use.accept(stack);
        return true;
    }

    public boolean keyDown(int key) {
        for (int i = 0; i < 9; i++) if (key > 0 && key == key(i)) return activate(i);
        return false;
    }

    public boolean drop(PlayerData.Item item, int x, int y) {
        int index = slotAt(x, y);
        if (index < 0 || items.attributes(item.itemId()).action == 0) return false;
        assign(index, item);
        return true;
    }

    public void assign(int index, PlayerData.Item item) {
        items.register(index + 1, item);
        update();
    }

    public void clear(int index) {
        settings.save(
                Map.of(
                        "client.hotbar.item_id" + (index + 1),
                        "0",
                        "client.hotbar.item_object_id" + (index + 1),
                        "0"));
        update();
    }

    public void swap(int a, int b) {
        if (a == b) return;
        settings.save(
                Map.of(
                        "client.hotbar.item_id" + (a + 1),
                        Integer.toString(itemId(b)),
                        "client.hotbar.item_object_id" + (a + 1),
                        objectId(b),
                        "client.hotbar.item_id" + (b + 1),
                        Integer.toString(itemId(a)),
                        "client.hotbar.item_object_id" + (b + 1),
                        objectId(a)));
        update();
    }

    private int slotAt(int x, int y) {
        for (int i = 0; i < 9; i++) if (slots[i].isInside(x, y)) return i;
        return -1;
    }

    public void position(int left, int top, int width, int height) {
        int cellW = mobile ? 90 : 42, cellH = mobile ? 60 : 42, gap = mobile ? 14 : 5;
        setSize(mobile ? 100 : 9 * cellW + 8 * gap + 20, mobile ? 9 * cellH + 8 * gap + 10 : 64);
        int x = mobile ? left : left + number("client.gui.hud.hotkeybar.x", 250),
                y =
                        mobile
                                ? top + height - getHeight()
                                : top + number("client.gui.hud.hotkeybar.y", 0);
        if (!dragging || drag != -2)
            setPosition(
                    Math.max(left, Math.min(x, left + width - getWidth())),
                    Math.max(top, Math.min(y, top + height - getHeight())));
        positioned = true;
    }

    @Override
    protected void layout() {
        int gap = mobile ? 14 : 5;
        for (int i = 0; i < 9; i++) {
            slots[i].setSize(mobile ? 90 : 42, mobile ? 60 : 42);
            slots[i].setPosition(
                    getX() + (mobile ? 5 : 10 + i * (42 + gap)),
                    getY() + (mobile ? 5 + i * (60 + gap) : 10));
        }
    }

    @Override
    protected boolean handleEvent(Event event) {
        if (!event.isMouseEvent()) return false;
        if (!mobile && event.getType() == Event.Type.MOUSE_BTNDOWN && event.getMouseButton() == 0) {
            drag = -2;
            dragging = false;
            startX = event.getMouseX();
            startY = event.getMouseY();
            originX = getX();
            originY = getY();
            return true;
        }
        if (drag == -2) return dragEvent(event);
        return true;
    }

    private boolean dragEvent(Event event) {
        int x = event.getMouseX(), y = event.getMouseY();
        if (event.getType() == Event.Type.MOUSE_DRAGGED) {
            if (Math.abs(x - startX) + Math.abs(y - startY) > 4) dragging = true;
            if (drag == -2 && dragging) {
                setPosition(originX + x - startX, originY + y - startY);
                invalidateLayout();
            }
            return true;
        }
        if (event.getType() == Event.Type.MOUSE_BTNUP && event.getMouseButton() == 0) {
            int source = drag;
            drag = -1;
            if (source >= 0) {
                if (dragging) {
                    int destination = slotAt(x, y);
                    if (destination < 0) clear(source);
                    else swap(source, destination);
                } else if (slots[source].isInside(x, y)) activate(source);
            } else if (dragging && positioned) {
                settings.save(
                        Map.of(
                                "client.gui.hud.hotkeybar.x",
                                Integer.toString(Math.max(0, getX() - getParent().getInnerX())),
                                "client.gui.hud.hotkeybar.y",
                                Integer.toString(Math.max(0, getY() - getParent().getInnerY()))));
            }
            dragging = false;
            return true;
        }
        return event.isMouseEvent();
    }

    private final class Slot extends Button {
        final int index;
        final Label quantity = new Label();

        Slot(int index) {
            this.index = index;
            setTheme("item-slot");
            setCanAcceptKeyboardFocus(false);
            setTooltipDelay(100);
            quantity.setTheme("quantity");
            quantity.setCanAcceptKeyboardFocus(false);
            add(quantity);
        }

        @Override
        public Object getTooltipContent() {
            return itemId(index) == 0 || mobile ? null : items.name(itemId(index));
        }

        @Override
        protected void layout() {
            quantity.adjustSize();
            quantity.setPosition(
                    getRight() - 5 - quantity.getWidth(), getBottom() - 3 - quantity.getHeight());
        }

        @Override
        protected void paintWidget(GUI gui) {
            int id = itemId(index);
            if (id != 0) {
                var image = items.icon(id);
                if (image != null)
                    image.draw(
                            getAnimationState(),
                            getX() + (mobile ? 21 : 10),
                            getY() + (mobile ? 6 : 10),
                            mobile ? 48 : 24,
                            mobile ? 48 : 24);
            }
            super.paintWidget(gui);
        }

        @Override
        protected boolean handleEvent(Event event) {
            if (event.getType() == Event.Type.MOUSE_BTNDOWN && event.getMouseButton() == 0) {
                drag = index;
                dragging = false;
                startX = event.getMouseX();
                startY = event.getMouseY();
                return true;
            }
            if (!mobile
                    && event.getType() == Event.Type.MOUSE_BTNUP
                    && event.getMouseButton() == 1) {
                clear(index);
                return true;
            }
            if (mobile && event.getType() == Event.Type.MOUSE_DRAGGED) return true;
            if (drag == index) return dragEvent(event);
            return super.handleEvent(event);
        }
    }
}
