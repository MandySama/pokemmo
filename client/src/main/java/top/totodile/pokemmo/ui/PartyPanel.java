package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.RomFiles;
import top.totodile.pokemmo.protocol.PartyMember;
import top.totodile.pokemmo.rom.PartyIcons;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** 队伍内容由角色响应提供，图像后台解码，纹理在 GUI 线程上传和释放。 */
final class PartyPanel extends Widget implements AutoCloseable {
    private List<PartyMember> members = List.of();
    private CompletableFuture<BufferedImage> iconResult;
    private final LatestIconRequest iconRequests = new LatestIconRequest();
    private record IconStyle(List<PartyIcons.Appearance> appearances, int outline, int shinyOutline, float size, float intensity) { }
    private IconStyle iconStyle;
    private IconStyle uploadedIconStyle;
    private List<PartyIcons.Appearance> displayedIcons = List.of();
    private Texture texture;
    private Image healthBackground;
    private final Image[] healthFill = new Image[3];
    private int healthWidth = 40, healthHeight = 8, healthX = 2, healthY = 30;
    private final Image[] icons = new Image[6];
    private int shinyAlphaGlow = Color.WHITE.toARGB();
    private float glowSize = 0.0f;
    private float glowIntensity = 1.25f;
    private boolean draggedPosition;
    private int alphaOutline = Color.WHITE.toARGB();
    private final Widget[] slots = new Widget[6];
    private final Button orientation = new Button();
    private final Button move = new Button();
    private final de.matthiasmann.twl.DraggableButton handle = new de.matthiasmann.twl.DraggableButton();
    private SlotMenu menu;
    private java.util.function.IntFunction<CompletableFuture<PartyMember>> selectFollower;
    private java.util.function.Consumer<PartyMember> followerChanged = member -> { };
    private java.util.function.BiPredicate<Integer, Integer> mapDrop = (x, y) -> false;
    private CompletableFuture<PartyMember> followResult;
    private Runnable followFailed = () -> { };
    private PartyMember selectedFollower;
    private int boundFollowerSlot = -1, requestedFollowerSlot = -1;
    private int dragIndex = -1, dragX, dragY;
    private final Widget dragOverlay = new Widget() {
        @Override protected void paintWidget(GUI gui) {
            if (dragIndex >= 0 && icons[dragIndex] != null)
                icons[dragIndex].draw(getAnimationState(), dragX - PartyIcons.TILE_SIZE / 2, dragY - PartyIcons.TILE_SIZE / 2);
        }
    };
    private CompletableFuture<List<PartyMember>> moveResult;
    private java.util.function.IntConsumer openDex = species -> { };
    private java.util.function.BiFunction<Integer, Integer, CompletableFuture<List<PartyMember>>> reorder;
    private Runnable menuOpened = () -> { };
    private Runnable moveFailed = () -> { };
    private boolean horizontal;
    private boolean unlocked;
    private boolean positioned;
    private int offsetX;
    private int offsetY;
    private int slotWidth = 46;
    private int slotHeight = 40;
    private int gap = 5;

    PartyPanel() {
        setTheme("/party-panel");
        setCanAcceptKeyboardFocus(false);
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new Slot(i);
            slots[i].setTheme("empty");
            slots[i].setCanAcceptKeyboardFocus(false);
            add(slots[i]);
        }
        orientation.setTheme("orientation");
        orientation.setCanAcceptKeyboardFocus(false);
        orientation.addCallback(() -> {
            horizontal = !horizontal;
            adjustSize();
            place();
            invalidateLayout();
        });
        add(orientation);
        move.setTheme("move");
        move.setCanAcceptKeyboardFocus(false);
        move.addCallback(() -> {
            unlocked = !unlocked;
            adjustSize();
            place();
            invalidateLayout();
        });
        add(move);
        handle.setListener(new de.matthiasmann.twl.DraggableButton.DragListener() {
            public void dragStarted() { draggedPosition = true; offsetX = getX(); offsetY = getY(); }
            public void dragged(int dx, int dy) {
                setPosition(offsetX + dx, offsetY + dy);
                place();
            }
            public void dragStopped() { }
        });
        handle.setTheme("handle");
        handle.setCanAcceptKeyboardFocus(false);
        add(handle);
        dragOverlay.setTheme("");
        dragOverlay.setCanAcceptKeyboardFocus(false);
        dragOverlay.setVisible(false);
        setVisible(false);
    }

    @Override
    protected void applyTheme(ThemeInfo info) {
        super.applyTheme(info);
        slotWidth = info.getParameter("slotWidth", 46);
        slotHeight = info.getParameter("slotHeight", 40);
        gap = info.getParameter("gap", 5);
        shinyAlphaGlow = info.getParameter("shinyAlphaGlow", Color.WHITE).toARGB();
        glowSize = info.getParameter("glowSize", 0.0f);
        glowIntensity = info.getParameter("glowIntensity", 1.25f);
        alphaOutline = info.getParameter("alphaOutline", Color.WHITE).toARGB();
        healthBackground = info.getImage("healthBackground");
        healthFill[0] = info.getImage("healthGreen");
        healthFill[1] = info.getImage("healthOrange");
        healthFill[2] = info.getImage("healthRed");
        healthWidth = info.getParameter("healthWidth", 40);
        healthHeight = info.getParameter("healthHeight", 8);
        healthX = info.getParameter("healthX", 2);
        healthY = info.getParameter("healthY", 30);
    }

    void setGameVisible(boolean visible) {
        if (!visible) {
            close();
            positioned = false;
            draggedPosition = false;
            horizontal = false;
            unlocked = false;
        }
        setVisible(visible);
    }

    void place() {
        Widget parent = getParent();
        if (parent == null) return;
        if (!positioned || !draggedPosition) {
            setPosition(parent.getInnerRight() - getWidth(),
                    parent.getInnerY() + (parent.getInnerHeight() - getHeight()) / 2);
            positioned = true;
        }
        setPosition(Math.max(parent.getInnerX(), Math.min(getX(), parent.getInnerRight() - getWidth())),
                Math.max(parent.getInnerY(), Math.min(getY(), parent.getInnerBottom() - getHeight())));
    }

    @Override
    public int getPreferredInnerWidth() {
        return horizontal ? slots.length * slotWidth + (slots.length - 1) * gap : slotWidth;
    }

    @Override
    public int getPreferredInnerHeight() {
        return (horizontal ? slotHeight : slots.length * slotHeight + (slots.length - 1) * gap)
                + gap + 12 + (unlocked ? 20 : 0);
    }

    @Override
    protected void layout() {
        handle.setVisible(unlocked);
        handle.setPosition(getInnerX(), getInnerY());
        handle.setSize(getInnerWidth(), 16);
        int top = getInnerY() + (unlocked ? 20 : 0);
        for (int i = 0; i < slots.length; i++) {
            slots[i].setPosition(getInnerX() + (horizontal ? i * (slotWidth + gap) : 0),
                    top + (horizontal ? 0 : i * (slotHeight + gap)));
            slots[i].setSize(slotWidth, slotHeight);
        }
        orientation.setSize(12, 12);
        move.setSize(12, 12);
        orientation.setPosition(getInnerX(), getInnerBottom() - 12);
        move.setPosition(getInnerRight() - 12, getInnerBottom() - 12);
    }

    List<PartyMember> party() { return members; }

    void setParty(List<PartyMember> party) {
        PartyMember previousFollower = selectedFollower;
        int previousSlot = boundFollowerSlot;
        dragIndex = -1;
        dragOverlay.setVisible(false);
        if (menu != null) menu.closePopup();
        menu = null;
        if (moveResult != null) moveResult.cancel(false);
        moveResult = null;
        if (followResult != null) followResult.cancel(false);
        followResult = null;
        selectedFollower = previousFollower;
        boundFollowerSlot = previousSlot;
        members = PartyMember.validate(party);
        if (boundFollowerSlot >= members.size()) {
            // 跟随者已随 PC 存箱离开队伍：清除本地跟随状态，世界里的跟随精灵随之消失。
            selectedFollower = null;
            boundFollowerSlot = -1;
            followerChanged.accept(null);
        } else if (boundFollowerSlot >= 0 && !members.get(boundFollowerSlot).equals(selectedFollower)) {
            followSlot(boundFollowerSlot);
        }
        for (int i = 0; i < slots.length; i++) {
            if (i < members.size()) {
                PartyMember member = members.get(i);
                String text = ClientStrings.get(150000 + member.species());
                if (member.shiny()) text += " · " + ClientStrings.get(5614);
                if (member.alpha()) text += " · " + ClientStrings.get(8119);
                slots[i].setTooltipContent(text);
            } else slots[i].setTooltipContent(null);
        }
        var appearances = PartyIcons.appearances(members);
        var style = new IconStyle(appearances, alphaOutline, shinyAlphaGlow, glowSize, glowIntensity);
        if (style.equals(iconStyle)) return;
        Image[] previous = icons.clone();
        java.util.Arrays.fill(icons, null);
        for (int i = 0; i < appearances.size(); i++) for (int j = 0; j < displayedIcons.size(); j++) {
            if (appearances.get(i).equals(displayedIcons.get(j)) && previous[j] != null) { icons[i] = previous[j]; break; }
        }
        displayedIcons = appearances;
        iconStyle = style;
        boolean reuseTexture = texture != null && uploadedIconStyle != null
                && uploadedIconStyle.outline() == style.outline() && uploadedIconStyle.shinyOutline() == style.shinyOutline()
                && uploadedIconStyle.size() == style.size() && uploadedIconStyle.intensity() == style.intensity()
                && !appearances.isEmpty();
        for (int i = 0; i < appearances.size(); i++) reuseTexture &= icons[i] != null;
        if (iconResult != null) iconResult.cancel(false);
        if (reuseTexture) { iconRequests.cancel(); iconResult = null; return; }
        if (members.isEmpty()) {
            iconRequests.cancel(); iconResult = null;
            if (texture != null) texture.destroy();
            texture = null;
            return;
        }
        List<PartyMember> snapshot = members;
        int outline = alphaOutline;
        int shinyGlow = shinyAlphaGlow;
        float size = glowSize, intensity = glowIntensity;
        iconResult = iconRequests.submit(() -> {
            try {
                var rom = new RomFiles(ClientFiles.data("../roms")).selected(0);
                if (rom == null) return null;
                return PartyIcons.load(rom, snapshot, outline, shinyGlow, size, intensity);
            } catch (java.io.IOException exception) {
                System.getLogger(PartyPanel.class.getName()).log(System.Logger.Level.WARNING,
                        "Unable to load party icons", exception);
                return null;
            }
        });
    }

    @Override
    protected void paintWidget(GUI gui) {
        if (followResult != null && followResult.isDone()) {
            var result = followResult;
            followResult = null;
            try {
                selectedFollower = result.join();
                boundFollowerSlot = selectedFollower == null ? -1 : requestedFollowerSlot;
                followerChanged.accept(selectedFollower);
            } catch (java.util.concurrent.CompletionException error) { followFailed.run(); }
        }
        if (moveResult != null && moveResult.isDone()) {
            CompletableFuture<List<PartyMember>> result = moveResult;
            moveResult = null;
            try { setParty(result.join()); }
            catch (java.util.concurrent.CompletionException exception) { moveFailed.run(); }
        }
        super.paintWidget(gui);
        if (iconResult != null && iconResult.isDone()) {
            BufferedImage pixels;
            try { pixels = iconResult.getNow(null); }
            catch (java.util.concurrent.CompletionException | java.util.concurrent.CancellationException error) { pixels = null; }
            iconResult = null;
            if (pixels != null) {
                Texture oldTexture = texture;
                texture = ((GlfwTwlRenderer) gui.getRenderer()).uploadImage(pixels);
                for (int i = 0; i < members.size(); i++)
                    icons[i] = texture.getImage(i * PartyIcons.TILE_SIZE, 0, PartyIcons.TILE_SIZE, PartyIcons.TILE_SIZE, Color.WHITE, false, Texture.Rotation.NONE);
                uploadedIconStyle = iconStyle;
                if (oldTexture != null) oldTexture.destroy();
            } else iconStyle = null;
        }
    }

    @Override
    public void close() {
        if (followResult != null) followResult.cancel(false);
        followResult = null;
        selectedFollower = null;
        boundFollowerSlot = requestedFollowerSlot = -1;
        dragIndex = -1;
        dragOverlay.setVisible(false);
        if (menu != null) menu.closePopup();
        menu = null;
        if (moveResult != null) moveResult.cancel(false);
        moveResult = null;
        iconRequests.cancel();
        iconStyle = null;
        uploadedIconStyle = null;
        displayedIcons = List.of();
        if (iconResult != null) iconResult.cancel(false);
        iconResult = null;
        if (texture != null) texture.destroy();
        texture = null;
        java.util.Arrays.fill(icons, null);
        members = List.of();
        for (Widget slot : slots) slot.setTooltipContent(null);
    }

    void setSummaryAction(java.util.function.Consumer<PartyMember> action) { openSummary = action; }

    private java.util.function.Consumer<PartyMember> openSummary = member -> { };

    void setActions(java.util.function.IntConsumer dex,
                    java.util.function.BiFunction<Integer, Integer, CompletableFuture<List<PartyMember>>> reorder,
                    Runnable opened, Runnable failed) {
        this.openDex = dex;
        this.reorder = reorder;
        this.menuOpened = opened;
        this.moveFailed = failed;
    }

    Widget dragOverlay() { return dragOverlay; }

    void setFollowerActions(java.util.function.IntFunction<CompletableFuture<PartyMember>> select,
                            java.util.function.Consumer<PartyMember> changed,
                            java.util.function.BiPredicate<Integer, Integer> mapDrop, Runnable failed) {
        selectFollower = select; followerChanged = changed; this.mapDrop = mapDrop; followFailed = failed;
    }

    void clearFollowerSelection() { selectedFollower = null; boundFollowerSlot = -1; }

    private void follow(int index) {
        if (selectFollower == null || followResult != null || moveResult != null) return;
        if (index >= 0 && (index >= members.size() || members.get(index).species() > 493)) { followFailed.run(); return; }
        requestedFollowerSlot = -1;
        followResult = selectFollower.apply(index);
    }

    private void followSlot(int index) {
        follow(index);
        if (followResult != null) requestedFollowerSlot = index;
    }
    boolean menuOpen() { return menu != null && menu.isOpen(); }

    private void showMenu(int index) {
        if (index >= members.size() || moveResult != null || followResult != null) return;
        if (menu != null) menu.closePopup();
        menuOpened.run();
        menu = new SlotMenu(index);
        menu.show();
    }

    private void moveMember(int from, int to) {
        if (reorder == null || moveResult != null || followResult != null) return;
        moveResult = reorder.apply(from, to);
    }

    private final class SlotMenu extends de.matthiasmann.twl.PopupWindow {
        private final java.util.List<Button> buttons = new java.util.ArrayList<>();
        private int rowHeight = 31;
        private int minimumWidth = 156;
        SlotMenu(int index) {
            super(slots[index]);
            setTheme("/party-slot-menu");
            setCloseAndIgnoreMouseDownOutside(true);
            addItem(ClientStrings.get(1801), () -> openSummary.accept(members.get(index)));
            addItem(ClientStrings.get(1729), () -> openDex.accept(members.get(index).species()));
            if (index > 0) addItem(ClientStrings.get(2303), () -> moveMember(index, index - 1));
            if (index + 1 < members.size()) addItem(ClientStrings.get(2304), () -> moveMember(index, index + 1));
            if (index > 0) addItem(ClientStrings.get(2313), () -> moveMember(index, 0));
            if (selectedFollower != null && (selectedFollower.individual()!=null&&members.get(index).individual()!=null?selectedFollower.individual().id()==members.get(index).individual().id():selectedFollower.equals(members.get(index)))) {
                addItem(ClientStrings.get(2256), () -> follow(-1));
            } else {
                addItem(ClientStrings.get(2262).replace("{00}", ClientStrings.get(150000 + members.get(index).species())), () -> follow(index));
                addItem(ClientStrings.get(2263).replace("{00}", Integer.toString(index + 1)), () -> followSlot(index));
            }
            addItem(ClientStrings.get(53), () -> { });
        }
        private void addItem(String text, Runnable action) {
            Button button = new Button(text);
            button.setTheme("button");
            if (action == null) {
                button.setEnabled(false);
                button.setTooltipContent(ClientStrings.get(9350));
            } else button.addCallback(() -> { closePopup(); action.run(); });
            buttons.add(button);
            add(button);
        }
        @Override
        protected void applyTheme(ThemeInfo info) {
            super.applyTheme(info);
            rowHeight = info.getParameter("rowHeight", 31);
            minimumWidth = info.getParameter("menuWidth", 156);
        }
        void show() {
            if (openPopup()) {
                requestKeyboardFocus(null);
                int width = minimumWidth;
                for (Button button : buttons) width = Math.max(width, button.getPreferredWidth() + getBorderHorizontal());
                setSize(width, rowHeight * buttons.size() + getBorderVertical());
                positionMenu();
                getOwner().getAnimationState().setAnimationState(
                        de.matthiasmann.twl.renderer.AnimationState.StateKey.get("selected"), true);
            }
        }
        @Override
        public void closePopup() {
            getOwner().getAnimationState().setAnimationState(
                    de.matthiasmann.twl.renderer.AnimationState.StateKey.get("selected"), false);
            super.closePopup();
        }
        private void positionMenu() {
            if (getGUI() == null) return;
            int x = getOwner().getX() - getWidth() - 5;
            if (x < 0) x = getOwner().getRight() + 5;
            setPosition(Math.max(0, Math.min(x, getGUI().getWidth() - getWidth())),
                    Math.max(0, Math.min(getOwner().getY(), getGUI().getHeight() - getHeight())));
        }
        @Override
        protected void layout() {
            positionMenu();
            for (int i = 0; i < buttons.size(); i++) {
                buttons.get(i).setPosition(getInnerX(), getInnerY() + i * rowHeight);
                buttons.get(i).setSize(getInnerWidth(), rowHeight);
            }
        }
    }
    private final class Slot extends de.matthiasmann.twl.DraggableButton {
        private final int index;
        private Slot(int index) {
            this.index = index;
            addCallback(() -> showMenu(index));
            setListener(new DragListener() {
                public void dragStarted() {
                    if (index >= members.size() || moveResult != null || followResult != null) return;
                    if (menu != null) menu.closePopup();
                    dragIndex = index;
                    dragOverlay.setVisible(true);
                    menuOpened.run();
                }
                public void dragged(int dx, int dy) { }
                public void dragStopped() {
                    int source = dragIndex;
                    dragIndex = -1;
                    dragOverlay.setVisible(false);
                    if (source < 0) return;
                    for (int i = 0; i < members.size(); i++) if (slots[i].isInside(dragX, dragY)) {
                        if (i != source) moveMember(source, i);
                        return;
                    }
                    if (!PartyPanel.this.isInside(dragX, dragY) && mapDrop.test(dragX, dragY)) follow(source);
                }
            });
        }
        @Override
        public boolean handleEvent(de.matthiasmann.twl.Event event) {
            if (event.isMouseEvent()) { dragX = event.getMouseX(); dragY = event.getMouseY(); }
            if ((event.getType() == Event.Type.MOUSE_BTNDOWN || event.getType() == Event.Type.MOUSE_BTNUP)
                    && event.getMouseButton() == Event.MOUSE_RBUTTON) {
                if (event.getType() == Event.Type.MOUSE_BTNUP && index < members.size() && isInside(dragX, dragY)) {
                    if (menu != null) menu.closePopup();
                    menuOpened.run();
                    openSummary.accept(members.get(index));
                }
                return true;
            }
            return super.handleEvent(event);
        }
        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if (icons[index] != null)
                icons[index].draw(getAnimationState(), getX() + (getWidth() - PartyIcons.TILE_SIZE) / 2, getY() - 7);
            if (index < members.size() && healthBackground != null) {
                PartyMember member = members.get(index);
                float ratio = (float) member.hp() / member.maxHp();
                healthBackground.draw(getAnimationState(), getX() + healthX, getY() + healthY, healthWidth, healthHeight);
                if (member.hp() > 0) {
                    Image fill = healthFill[ratio > 0.5f ? 0 : ratio > 0.2f ? 1 : 2];
                    if (fill != null) fill.draw(getAnimationState(), getX() + healthX, getY() + healthY,
                            Math.max(5, Math.round(healthWidth * ratio)), healthHeight);
                }
            }
        }
    }
}
