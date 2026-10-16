package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.DraggableButton;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.RomFiles;
import top.totodile.pokemmo.protocol.PartyMember;
import top.totodile.pokemmo.protocol.PcPackets;
import top.totodile.pokemmo.protocol.PokemonCatalog;
import top.totodile.pokemmo.protocol.PokemonIndividual;
import top.totodile.pokemmo.rom.PartyIcons;
import top.totodile.pokemmo.rom.PokedexData;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 电脑箱子窗口。版式对齐参考客户端的 ui/pc.xml：左侧铺满内容区高度的资料底板，
 * 右侧依次是居中箱名、两行箱子选项卡（每行固定 11 个）、十列六行格子、队伍栏与底栏。
 * <p>
 * 格子上的宝可梦用拖拽搬移：按下并移动即拿起，图标跟随光标，松开时落在目标格才提交；
 * 没有移动的单击只切换左侧资料。底栏按钮按参考图摆放，功能尚未接通。
 */
final class PcBoxWindow extends ResizableFrame implements AutoCloseable {
    /** 详情底板上的资料行标题，顺序与 {@link #rows} 一一对应。 */
    private static final int[] DETAIL_STRING_IDS = {9400, 1849, 1800, 1805, 1824, 1842, 1869};
    private static final int DETAIL_ROWS = DETAIL_STRING_IDS.length;
    private static final int MOVE_ROWS = 4;
    private static final int MARK_COUNT = 5;
    /** 底栏图集格数：多选、排序箭头、垃圾桶、帮助、纸笔、清除筛选。 */
    private static final int BAR_ICONS = 6;
    private static final int BAR_ICON_SIZE = 24;
    /** 资料底板里宝可梦立绘的绘制边长，参考客户端同样是放大后的像素图。 */
    private static final int SPRITE_SIZE = 168;

    private record Pick(int kind, int box, int slot) { }

    private record Loaded(PokedexData.Species species, BufferedImage image) { }

    private final Map<Integer, Map<Integer, PartyMember>> cache = new LinkedHashMap<>();

    // ---- 左侧资料底板 ----
    private final Widget preview = new Widget();
    private final Widget shinyMark = new Widget();
    private final Widget picture = new Widget() {
        @Override protected void paintWidget(GUI gui) {
            if (sprite != null)
                sprite.draw(getAnimationState(), getX(), getY(), getWidth(), getHeight());
        }
    };
    private final Label name = new Label();
    private final List<Label[]> rows = new ArrayList<>();
    private final Widget[] marks = new Widget[MARK_COUNT];
    /** 立绘右上角的属性标牌，参考图里单属性靠右、双属性并排。 */
    private final Widget[] typeBadges = new Widget[2];
    private final List<Widget> moveTypes = new ArrayList<>();
    private final List<Label> moveNames = new ArrayList<>();

    // ---- 右侧箱子区 ----
    private final Label boxName = new Label();
    private final List<Button> boxTabs = new ArrayList<>();
    private final Button accountTab = new Button(ClientStrings.get(2364));
    private final Button battleTab = new Button(ClientStrings.get(2361));
    private final Button addBox = new Button("+");
    private final Slot[] boxSlots = new Slot[PcPackets.BOX_SLOTS];
    private final Slot[] partySlots = new Slot[6];
    private final Label[] slotLevels = new Label[PcPackets.BOX_SLOTS];
    private final Label[] partyLevels = new Label[6];
    private final Widget[] partyHealth = new Widget[6];

    // ---- 底栏（按钮功能未接通，只对齐参考图） ----
    private final Button sortButton = new Button(ClientStrings.get(2315));
    private final IconView[] barIcons = new IconView[BAR_ICONS];
    private final EditField searchField = new EditField();
    private final Button advancedSearch = new Button(ClientStrings.get(8010));

    private Supplier<CompletableFuture<PcPackets.PartyState>> partyCommand = () -> CompletableFuture.failedFuture(new IllegalStateException("No PC connection"));
    private BiFunction<Integer, Integer, CompletableFuture<PcPackets.BoxPage>> boxCommand = (box, start) -> CompletableFuture.failedFuture(new IllegalStateException("No PC connection"));
    private Function<PcPackets.Move, CompletableFuture<PcPackets.Status>> moveCommand = move -> CompletableFuture.failedFuture(new IllegalStateException("No PC connection"));
    private Supplier<CompletableFuture<PcPackets.Status>> createBoxCommand = () -> CompletableFuture.failedFuture(new IllegalStateException("No PC connection"));
    private java.util.function.Consumer<String> notify = message -> { };
    /** 每次拿到权威队伍状态（打开、搬移、新建箱子之后）回调宿主，让世界队伍面板同步。 */
    private java.util.function.Consumer<List<PartyMember>> partyListener = list -> { };

    private CompletableFuture<PcPackets.PartyState> partyRequest;
    private CompletableFuture<PcPackets.BoxPage> boxRequest;
    private CompletableFuture<PcPackets.Status> moveRequest;
    private CompletableFuture<PcPackets.Status> createRequest;
    private boolean busy;
    private int boxCount = 1, currentBox, fetchingBox = -1;
    private List<PartyMember> party = List.of();
    private final Map<Integer, PartyMember> entries = new TreeMap<>();
    private final Map<Integer, PartyMember> fetching = new TreeMap<>();
    private Pick picked;
    private PartyMember shown;
    private Texture iconTexture, spriteTexture, barIconTexture;
    private Image[] stripIcons = new Image[0];
    private BufferedImage iconPixels;
    private final LatestIconRequest iconRequests = new LatestIconRequest();
    private CompletableFuture<BufferedImage> iconRequest;
    private record IconStyle(List<PartyIcons.Appearance> appearances, int outline, int shinyOutline, float size, float intensity) { }
    private IconStyle iconStyle;
    private IconStyle uploadedIconStyle;
    private List<PartyIcons.Appearance> displayedIcons = List.of();
    private Image sprite;
    private CompletableFuture<Loaded> spriteRequest;
    private Image healthBackground;
    private final Image[] healthFill = new Image[3];
    private Image shiny;
    private BufferedImage barIconSheet;
    private int alphaOutline = Color.WHITE.toARGB(), shinyAlphaGlow = Color.WHITE.toARGB();
    private float glowSize, glowIntensity = 1.25f;

    // ---- 拖拽状态，覆盖层挂在 LoginScreen 顶层 ----
    private Image dragIcon;
    private Slot dragSource, dropTarget;
    private int dragX, dragY;
    private final Widget dragOverlay = new Widget() {
        @Override protected void paintWidget(GUI gui) {
            if (dragIcon != null)
                dragIcon.draw(getAnimationState(), dragX - PartyIcons.TILE_SIZE / 2,
                        dragY - PartyIcons.TILE_SIZE / 2, PartyIcons.TILE_SIZE, PartyIcons.TILE_SIZE);
        }
    };

    // ---- 版式参数，默认值取自参考截图 1101×700 的实测换算 ----
    private int panelWidth = 302, panelGap = 6;
    private int slotWidth = 56, slotHeight = 48, slotGap = 8, columnGap = 9;
    private int partyWidth = 54, partyGap = 15;
    private int gridTop = 198;
    private int tabWidth = 58, tabHeight = 40, tabGap = 2, tabColumns = 11;
    private int tabTop = 57;
    private int nameTop = 12, nameHeight = 30;
    private int barTop = 590, barHeight = 27;
    private int detailRowHeight = 33, detailTextHeight = 26, moveRowHeight = 31;

    PcBoxWindow(Runnable close) {
        setTheme("/pc-box");
        setTitle(ClientStrings.get(9433));
        setResizableAxis(ResizableAxis.NONE);
        addCloseCallback(close);
        setHardVisible(false);

        preview.setTheme("preview");
        add(preview);
        shinyMark.setTheme("shiny-mark");
        shinyMark.setVisible(false);
        add(shinyMark);
        picture.setTheme("picture");
        add(picture);
        name.setTheme("monster-name");
        add(name);
        for (int i = 0; i < MARK_COUNT; i++) {
            marks[i] = new Widget();
            marks[i].setTheme("mark-" + i);
            add(marks[i]);
        }
        for (int i = 0; i < typeBadges.length; i++) {
            typeBadges[i] = new PokemonTypeBadge();
            typeBadges[i].setTheme("move-type-none");
            typeBadges[i].setVisible(false);
            add(typeBadges[i]);
        }
        for (int id : DETAIL_STRING_IDS) line(id);
        for (int i = 0; i < MOVE_ROWS; i++) {
            Widget badge = new PokemonTypeBadge();
            badge.setTheme("move-type-none");
            badge.setVisible(false);
            add(badge);
            moveTypes.add(badge);
            Label move = new Label();
            move.setTheme("move-name");
            move.setVisible(false);
            add(move);
            moveNames.add(move);
        }

        boxName.setTheme("box-name");
        add(boxName);
        for (int i = 0; i < PcPackets.MAX_BOXES; i++) {
            Button tab = new Button(ClientStrings.get(9434).replace("{00}", Integer.toString(i + 1)));
            tab.setTheme("box-tab");
            final int index = i;
            tab.addCallback(() -> selectBox(index));
            tab.setVisible(false);
            add(tab);
            boxTabs.add(tab);
        }
        for (Button extra : new Button[]{accountTab, battleTab}) {
            extra.setTheme("box-tab");
            extra.setTooltipContent(ClientStrings.get(9350));
            add(extra);
        }
        addBox.setTheme("box-tab");
        addBox.setTooltipContent(ClientStrings.get(9435));
        addBox.addCallback(this::createBox);
        add(addBox);

        for (int i = 0; i < boxSlots.length; i++) {
            boxSlots[i] = new Slot(PcPackets.BOX, i);
            slotLevels[i] = new Label();
            slotLevels[i].setTheme("slot-level");
            boxSlots[i].add(slotLevels[i]);
            add(boxSlots[i]);
        }
        for (int i = 0; i < partySlots.length; i++) {
            partySlots[i] = new Slot(PcPackets.PARTY, i);
            partyLevels[i] = new Label();
            partyLevels[i].setTheme("slot-level");
            partySlots[i].add(partyLevels[i]);
            add(partySlots[i]);
            partyHealth[i] = new HealthBar(i);
            add(partyHealth[i]);
        }
        addBar();

        dragOverlay.setTheme("");
        dragOverlay.setCanAcceptKeyboardFocus(false);
        dragOverlay.setVisible(false);
        refreshTabs();
        refreshDetails();
    }

    /** 底栏控件按参考图排布，除“新建箱子”外都只做展示。 */
    private void addBar() {
        sortButton.setTheme("bar-button");
        sortButton.setTooltipContent(ClientStrings.get(9350));
        add(sortButton);
        for (int i = 0; i < barIcons.length; i++) {
            barIcons[i] = new IconView();
            barIcons[i].setTheme("bar-icon");
            barIcons[i].setTooltipContent(i == 0 ? ClientStrings.get(2314) : ClientStrings.get(9350));
            add(barIcons[i]);
        }
        searchField.setTheme("bar-search");
        searchField.setCanAcceptKeyboardFocus(false);
        add(searchField);
        advancedSearch.setTheme("bar-button");
        advancedSearch.setTooltipContent(ClientStrings.get(9350));
        add(advancedSearch);
    }

    /** 队伍血量条复用队伍面板的说明：箱子窗口只读显示，不截获点击。 */
    private final class HealthBar extends Widget {
        private final int index;

        private HealthBar(int index) {
            this.index = index;
            setTheme("healthbar");
            setCanAcceptKeyboardFocus(false);
        }

        @Override protected void paintWidget(GUI gui) {
            PartyMember member = partyMemberAt(index);
            if (member == null || healthBackground == null) return;
            healthBackground.draw(getAnimationState(), getX(), getY(), getWidth(), getHeight());
            float ratio = member.hp() / (float) member.maxHp();
            if (ratio <= 0f) return;
            Image fill = healthFill[ratio > 0.5f ? 0 : ratio > 0.2f ? 1 : 2];
            if (fill != null) fill.draw(getAnimationState(), getX(), getY(),
                    Math.max(5, Math.round(getWidth() * ratio)), getHeight());
        }
    }

    /** 底栏裸图标：没有按钮底，只把图集里的一格画在控件中央。 */
    private static final class IconView extends Widget {
        private Image image;

        private IconView() {
            setCanAcceptKeyboardFocus(false);
        }

        private void setImage(Image image) { this.image = image; }

        @Override protected void paintWidget(GUI gui) {
            if (image == null) return;
            image.draw(getAnimationState(), getX() + (getWidth() - BAR_ICON_SIZE) / 2,
                    getY() + (getHeight() - BAR_ICON_SIZE) / 2, BAR_ICON_SIZE, BAR_ICON_SIZE);
        }
    }

    void setCommands(Supplier<CompletableFuture<PcPackets.PartyState>> party,
                     BiFunction<Integer, Integer, CompletableFuture<PcPackets.BoxPage>> box,
                     Function<PcPackets.Move, CompletableFuture<PcPackets.Status>> move,
                     Supplier<CompletableFuture<PcPackets.Status>> createBox) {
        partyCommand = party;
        boxCommand = box;
        moveCommand = move;
        createBoxCommand = createBox;
    }

    /** 宿主用它接收 PC 搬移后的最新队伍，避免只能注销重登才看到变化。 */
    void setPartyListener(java.util.function.Consumer<List<PartyMember>> listener) {
        partyListener = listener == null ? list -> { } : listener;
    }

    void setNotify(java.util.function.Consumer<String> callback) { notify = callback; }

    /** 拖拽图标画在窗口之上，需要由宿主界面按整屏尺寸摆放。 */
    Widget dragOverlay() { return dragOverlay; }

    private boolean refreshPokemonPending;

    void refreshPokemon() { refreshPokemonPending = true; }

    void open() {
        refreshPokemonPending = false;
        clear();
        requestParty();
        requestBox(0, true);
        setHardVisible(true);
        requestKeyboardFocus();
    }

    /** Esc 先取消已经拿起的宝可梦，再交回上层关闭窗口。 */
    boolean cancelPick() {
        if (picked == null) return false;
        endDrag();
        picked = null;
        applyPicked();
        return true;
    }

    private void line(int titleId) {
        Label key = new Label(ClientStrings.get(titleId));
        Label body = new Label();
        key.setTheme("field-title");
        body.setTheme("field-value");
        add(key);
        add(body);
        rows.add(new Label[]{key, body});
    }

    private void requestParty() {
        if (partyRequest != null) return;
        partyRequest = partyCommand.get();
    }

    private void requestBox(int box, boolean force) {
        if (boxRequest != null) return;
        currentBox = box;
        if (!force && cache.containsKey(box)) { applyBox(box); return; }
        fetchingBox = box;
        fetching.clear();
        entries.clear();
        refreshTabs();
        rebuildIcons();
        invalidateLayout();
        boxRequest = boxCommand.apply(box, 0);
    }

    private void selectBox(int box) {
        if (box >= boxCount || busy) return;
        if (picked != null) cancelPick();
        requestBox(box, false);
    }

    private void createBox() {
        if (busy || boxCount >= PcPackets.MAX_BOXES) return;
        createRequest = createBoxCommand.get();
        busy = true;
    }

    private void move(Pick from, Pick to) {
        if (busy) return;
        PcPackets.Move move = new PcPackets.Move(from.kind(), from.kind() == PcPackets.PARTY ? 0 : from.box(), from.slot(),
                to.kind(), to.kind() == PcPackets.PARTY ? 0 : to.box(), to.slot());
        moveRequest = moveCommand.apply(move);
        busy = true;
    }

    private PartyMember memberAt(Pick pick) {
        if (pick == null) return null;
        if (pick.kind() == PcPackets.PARTY) return partyMemberAt(pick.slot());
        if (pick.box() == currentBox) return entries.get(pick.slot());
        Map<Integer, PartyMember> stored = cache.get(pick.box());
        return stored == null ? null : stored.get(pick.slot());
    }

    @Override
    protected void applyTheme(ThemeInfo info) {
        super.applyTheme(info);
        healthBackground = info.getImage("healthBackground");
        healthFill[0] = info.getImage("healthGreen");
        healthFill[1] = info.getImage("healthOrange");
        healthFill[2] = info.getImage("healthRed");
        shiny = info.getImage("shinyMark");
        alphaOutline = info.getParameter("alphaOutline", Color.WHITE).toARGB();
        shinyAlphaGlow = info.getParameter("shinyAlphaGlow", Color.WHITE).toARGB();
        glowSize = info.getParameter("glowSize", 0.0f);
        glowIntensity = info.getParameter("glowIntensity", 1.25f);
        panelWidth = info.getParameter("panelWidth", panelWidth);
        panelGap = info.getParameter("panelGap", panelGap);
        slotWidth = info.getParameter("slotWidth", slotWidth);
        slotHeight = info.getParameter("slotHeight", slotHeight);
        slotGap = info.getParameter("slotGap", slotGap);
        columnGap = info.getParameter("columnGap", slotGap);
        partyWidth = info.getParameter("partyWidth", partyWidth);
        partyGap = info.getParameter("partyGap", partyGap);
        gridTop = info.getParameter("gridTop", gridTop);
        tabWidth = info.getParameter("tabWidth", tabWidth);
        tabHeight = info.getParameter("tabHeight", tabHeight);
        tabGap = info.getParameter("tabGap", tabGap);
        tabColumns = Math.max(1, info.getParameter("tabColumns", tabColumns));
        tabTop = info.getParameter("tabTop", tabTop);
        nameTop = info.getParameter("nameTop", nameTop);
        nameHeight = info.getParameter("nameHeight", nameHeight);
        barTop = info.getParameter("barTop", barTop);
        barHeight = info.getParameter("barHeight", barHeight);
        detailRowHeight = info.getParameter("detailRowHeight", detailRowHeight);
        detailTextHeight = info.getParameter("detailTextHeight", detailTextHeight);
        moveRowHeight = info.getParameter("moveRowHeight", moveRowHeight);
    }

    @Override
    protected void layout() {
        super.layout();
        int left = getInnerX(), top = getInnerY();

        // 右侧固定宽度（格子 + 队伍栏）决定左侧底板还能占多宽。
        int columns = PcPackets.BOX_COLUMNS;
        int gridWidth = columns * slotWidth + (columns - 1) * columnGap;
        int rightWidth = gridWidth + partyGap + partyWidth;
        int gridX = getInnerRight() - rightWidth;
        int panel = Math.max(180, Math.min(panelWidth, gridX - panelGap - left));

        preview.setPosition(left, top);
        preview.setSize(panel, getInnerHeight());

        picture.setPosition(left + (panel - SPRITE_SIZE) / 2, top + 14);
        picture.setSize(SPRITE_SIZE, SPRITE_SIZE);
        shinyMark.setPosition(left + 2, top + 3);
        shinyMark.setSize(24, 24);

        name.setPosition(left + 8, top + 218);
        name.setSize(panel - 16, 26);

        int textX = left + 5, textWidth = 64;
        int valueX = textX + textWidth + 4, valueWidth = Math.max(40, panel - (valueX - left) - 5);
        int y = top + 261;
        for (Label[] row : rows) {
            row[0].setPosition(textX, y);
            row[0].setSize(textWidth, detailTextHeight);
            row[1].setPosition(valueX, y);
            row[1].setSize(valueWidth, detailTextHeight);
            y += detailRowHeight;
        }
        int markY = top + 261 + (DETAIL_ROWS - 1) * detailRowHeight + 7;
        for (int i = 0; i < marks.length; i++) {
            marks[i].setPosition(textX + 72 + i * 18, markY);
            marks[i].setSize(12, 12);
        }
        // 属性标牌贴住底板右上角：参考图里双属性是上下堆叠（行距 16），不是并排。
        for (int i = 0; i < typeBadges.length; i++) {
            typeBadges[i].setSize(49, 14);
            typeBadges[i].setPosition(left + panel - 54, top + 4 + i * 16);
        }
        y = top + 497;
        for (int i = 0; i < moveNames.size(); i++) {
            moveTypes.get(i).setPosition(textX, y + 6);
            moveTypes.get(i).setSize(49, 14);
            moveNames.get(i).setPosition(textX + 59, y);
            moveNames.get(i).setSize(Math.max(40, panel - 64), detailTextHeight);
            y += moveRowHeight;
        }

        boxName.setPosition(gridX - 4, top + nameTop);
        boxName.setSize(rightWidth + 8, nameHeight);

        // 选项卡每行固定 11 个，账号／战斗／+ 紧跟在最后一个箱子之后流式排布。
        int tabRowWidth = tabColumns * tabWidth + (tabColumns - 1) * tabGap;
        int tabX = gridX + Math.max(0, (rightWidth - tabRowWidth) / 2);
        for (int i = 0; i < boxTabs.size(); i++) {
            Button tab = boxTabs.get(i);
            tab.setSize(tabWidth, tabHeight);
            tab.setPosition(tabX + (i % tabColumns) * (tabWidth + tabGap),
                    top + tabTop + (i / tabColumns) * (tabHeight + tabGap));
        }
        Button[] trailing = {accountTab, battleTab, addBox};
        for (int i = 0; i < trailing.length; i++) {
            int index = boxCount + i;
            trailing[i].setSize(i == trailing.length - 1 ? 30 : tabWidth, tabHeight);
            trailing[i].setPosition(tabX + (index % tabColumns) * (tabWidth + tabGap),
                    top + tabTop + (index / tabColumns) * (tabHeight + tabGap));
        }

        int gridY = top + gridTop;
        for (int i = 0; i < boxSlots.length; i++) {
            int x = gridX + (i % columns) * (slotWidth + columnGap);
            int rowY = gridY + (i / columns) * (slotHeight + slotGap);
            boxSlots[i].setSize(slotWidth, slotHeight);
            boxSlots[i].setPosition(x, rowY);
            slotLevels[i].setPosition(x + 2, rowY + slotHeight - 18);
            slotLevels[i].setSize(slotWidth - 6, 16);
        }
        int partyX = gridX + gridWidth + partyGap;
        for (int i = 0; i < partySlots.length; i++) {
            int rowY = gridY + i * (slotHeight + slotGap);
            partySlots[i].setSize(partyWidth, slotHeight);
            partySlots[i].setPosition(partyX, rowY);
            partyLevels[i].setPosition(partyX + 2, rowY + slotHeight - 18);
            partyLevels[i].setSize(partyWidth - 6, 16);
            partyHealth[i].setPosition(partyX + 6, rowY + slotHeight - 6);
            partyHealth[i].setSize(partyWidth - 12, 4);
        }

        int barY = top + barTop;
        int barX = gridX + 19;
        sortButton.setPosition(barX, barY);
        sortButton.setSize(65, barHeight);
        for (int i = 0; i < BAR_ICONS - 1; i++) {
            barIcons[i].setPosition(barX + 69 + i * 30, barY);
            barIcons[i].setSize(30, barHeight);
        }
        // 清除筛选的圆形 ✕ 与高级搜索一起贴右边。
        barIcons[BAR_ICONS - 1].setPosition(getInnerRight() - 107, barY + 1);
        barIcons[BAR_ICONS - 1].setSize(BAR_ICON_SIZE, BAR_ICON_SIZE);
        advancedSearch.setPosition(getInnerRight() - 72, barY);
        advancedSearch.setSize(67, barHeight);
        searchField.setPosition(getInnerRight() - 268, barY);
        searchField.setSize(158, barHeight);
    }

    @Override
    protected void paintWidget(GUI gui) {
        super.paintWidget(gui);
        updateRequests(gui);
        if (refreshPokemonPending && isVisible() && !busy && partyRequest == null && boxRequest == null) {
            refreshPokemonPending = false;
            cancelPick();
            cache.clear();
            requestParty();
            requestBox(currentBox, true);
        }
        GlfwTwlRenderer renderer = (GlfwTwlRenderer) gui.getRenderer();
        if (barIconSheet != null) {
            BufferedImage sheet = barIconSheet;
            barIconSheet = null;
            if (barIconTexture != null) barIconTexture.destroy();
            barIconTexture = renderer.uploadImage(sheet);
            for (int i = 0; i < barIcons.length; i++)
                barIcons[i].setImage(barIconTexture.getImage(i * BAR_ICON_SIZE, 0, BAR_ICON_SIZE, BAR_ICON_SIZE,
                        Color.WHITE, false, Texture.Rotation.NONE));
        }
        if (iconRequest != null && iconRequest.isDone()) {
            try { iconPixels = iconRequest.getNow(null); }
            catch (java.util.concurrent.CompletionException | java.util.concurrent.CancellationException error) { iconPixels = null; }
            if (iconPixels == null) iconStyle = null;
            iconRequest = null;
        }
        BufferedImage pixels = iconPixels;
        if (pixels != null) {
            iconPixels = null;
            Texture oldTexture = iconTexture;
            iconTexture = renderer.uploadImage(pixels);
            int tiles = pixels.getWidth() / PartyIcons.TILE_SIZE;
            stripIcons = new Image[tiles];
            for (int i = 0; i < tiles; i++)
                stripIcons[i] = iconTexture.getImage(i * PartyIcons.TILE_SIZE, 0, PartyIcons.TILE_SIZE, PartyIcons.TILE_SIZE,
                        Color.WHITE, false, Texture.Rotation.NONE);
            uploadedIconStyle = iconStyle;
            if (oldTexture != null) oldTexture.destroy();
            if (dragSource != null) dragIcon = icon(dragSource.stripIndex());
        }
    }

    private void updateRequests(GUI gui) {
        if (partyRequest != null && partyRequest.isDone()) {
            CompletableFuture<PcPackets.PartyState> request = partyRequest;
            partyRequest = null;
            try {
                PcPackets.PartyState state = request.join();
                boxCount = Math.max(1, Math.min(PcPackets.MAX_BOXES, state.boxes()));
                party = state.party();
                partyListener.accept(party);
                if (currentBox >= boxCount) currentBox = boxCount - 1;
                refreshTabs();
                rebuildIcons();
                invalidateLayout();
            } catch (CompletionException error) { notify.accept(ClientStrings.get(9442)); }
        }
        if (boxRequest != null && boxRequest.isDone()) {
            CompletableFuture<PcPackets.BoxPage> request = boxRequest;
            boxRequest = null;
            try {
                PcPackets.BoxPage page = request.join();
                if (page.box() == fetchingBox) {
                    for (PcPackets.Entry entry : page.entries()) fetching.put(entry.slot(), entry.member());
                    if (page.more()) {
                        int next = page.entries().isEmpty() ? page.start() : page.entries().getLast().slot() + 1;
                        boxRequest = boxCommand.apply(fetchingBox, next);
                    } else {
                        cache.put(fetchingBox, new TreeMap<>(fetching));
                        fetchingBox = -1;
                        if (page.box() == currentBox) {
                            entries.clear();
                            entries.putAll(cache.get(currentBox));
                            rebuildIcons();
                            invalidateLayout();
                        }
                    }
                }
            } catch (CompletionException error) { notify.accept(ClientStrings.get(9442)); }
            if (boxRequest == null && fetchingBox == -1 && !cache.containsKey(currentBox)) requestBox(currentBox, true);
        }
        if (moveRequest != null && moveRequest.isDone()) {
            CompletableFuture<PcPackets.Status> request = moveRequest;
            moveRequest = null;
            busy = false;
            try {
                PcPackets.Status status = request.join();
                if (status == PcPackets.Status.SUCCESS) {
                    picked = null;
                    cache.clear();
                    invalidateLayout();
                    requestParty();
                    requestBox(currentBox, true);
                } else if (status == PcPackets.Status.PARTY_FULL) notify.accept(ClientStrings.get(9515));
                else notify.accept(ClientStrings.get(9437));
            } catch (CompletionException error) { notify.accept(ClientStrings.get(9437)); }
            applyPicked();
            refreshTabs();
        }
        if (createRequest != null && createRequest.isDone()) {
            CompletableFuture<PcPackets.Status> request = createRequest;
            createRequest = null;
            busy = false;
            try {
                PcPackets.Status status = request.join();
                if (status == PcPackets.Status.SUCCESS) requestParty();
                else if (status == PcPackets.Status.BOX_FULL) notify.accept(ClientStrings.get(9436));
                else notify.accept(ClientStrings.get(9437));
            } catch (CompletionException error) { notify.accept(ClientStrings.get(9437)); }
            refreshTabs();
        }
        if (spriteRequest != null && spriteRequest.isDone()) {
            CompletableFuture<Loaded> request = spriteRequest;
            spriteRequest = null;
            try {
                BufferedImage pixels = request.join().image();
                BufferedImage canvas = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
                canvas.getGraphics().drawImage(pixels, 0, 0, null);
                canvas.getGraphics().dispose();
                spriteTexture = ((GlfwTwlRenderer) gui.getRenderer()).uploadImage(canvas);
                sprite = spriteTexture.getImage(0, 0, 96, 96, Color.WHITE, false, Texture.Rotation.NONE);
            } catch (CompletionException | NullPointerException error) {
                notify.accept(ClientStrings.get(9443));
            }
        }
    }

    private void applyBox(int box) {
        entries.clear();
        Map<Integer, PartyMember> stored = cache.get(box);
        if (stored != null) entries.putAll(stored);
        refreshTabs();
        rebuildIcons();
        invalidateLayout();
    }

    private void refreshTabs() {
        boxName.setText(ClientStrings.get(9434).replace("{00}", Integer.toString(currentBox + 1)));
        for (int i = 0; i < boxTabs.size(); i++) {
            Button tab = boxTabs.get(i);
            tab.setVisible(i < boxCount);
            tab.setEnabled(i != currentBox && !busy);
            tab.getAnimationState().setAnimationState(AnimationState.StateKey.get("selected"), i == currentBox);
        }
        boolean room = boxCount < PcPackets.MAX_BOXES;
        addBox.setVisible(true);
        addBox.setEnabled(room && !busy);
        invalidateLayout();
    }

    private void rebuildIcons() {
        List<PartyMember> strip = new ArrayList<>(party);
        for (Map.Entry<Integer, PartyMember> entry : entries.entrySet()) strip.add(entry.getValue());
        var appearances = PartyIcons.appearances(strip);
        var style = new IconStyle(appearances, alphaOutline, shinyAlphaGlow, glowSize, glowIntensity);
        if (style.equals(iconStyle)) return;
        Image[] previous = stripIcons;
        stripIcons = new Image[strip.size()];
        for (int i = 0; i < appearances.size(); i++) for (int j = 0; j < displayedIcons.size(); j++) {
            if (appearances.get(i).equals(displayedIcons.get(j)) && previous[j] != null) { stripIcons[i] = previous[j]; break; }
        }
        displayedIcons = appearances;
        iconStyle = style;
        boolean reuseTexture = iconTexture != null && uploadedIconStyle != null
                && uploadedIconStyle.outline() == style.outline() && uploadedIconStyle.shinyOutline() == style.shinyOutline()
                && uploadedIconStyle.size() == style.size() && uploadedIconStyle.intensity() == style.intensity()
                && !appearances.isEmpty();
        for (int i = 0; i < appearances.size(); i++) reuseTexture &= stripIcons[i] != null;
        iconPixels = null;
        if (iconRequest != null) iconRequest.cancel(false);
        if (reuseTexture) { iconRequests.cancel(); iconRequest = null; return; }
        if (strip.isEmpty()) {
            iconRequests.cancel(); iconRequest = null;
            if (iconTexture != null) iconTexture.destroy();
            iconTexture = null;
            return;
        }
        List<PartyMember> snapshot = List.copyOf(strip);
        int outline = alphaOutline, shinyGlow = shinyAlphaGlow;
        float size = glowSize, intensity = glowIntensity;
        iconRequest = iconRequests.submit(() -> {
            try {
                var rom = new RomFiles(ClientFiles.data("../roms")).selected(0);
                if (rom == null) return null;
                return PartyIcons.loadStrip(rom, snapshot, outline, shinyGlow, size, intensity);
            } catch (java.io.IOException exception) {
                System.getLogger(PcBoxWindow.class.getName()).log(System.Logger.Level.WARNING,
                        "Unable to load PC box icons", exception);
                return null;
            }
        });
    }

    private Image icon(int index) {
        return index >= 0 && index < stripIcons.length ? stripIcons[index] : null;
    }

    private int boxIconIndex(int slot) {
        int index = party.size();
        for (Map.Entry<Integer, PartyMember> entry : entries.entrySet()) {
            if (entry.getKey() == slot) return index;
            index++;
        }
        return -1;
    }

    private PartyMember partyMemberAt(int index) {
        return index >= 0 && index < party.size() ? party.get(index) : null;
    }

    private void refreshDetails() {
        PartyMember member = shown;
        PokemonIndividual individual = member == null ? null : member.individual();
        boolean visible = member != null;
        picture.setVisible(visible);
        for (Label[] row : rows) { row[0].setVisible(visible); row[1].setVisible(visible); }
        for (Widget mark : marks) mark.setVisible(visible);
        name.setVisible(visible);
        shinyMark.setVisible(visible && member.shiny());
        if (!visible) {
            name.setText("");
            for (Label move : moveNames) move.setVisible(false);
            for (Widget badge : moveTypes) badge.setVisible(false);
            for (Widget badge : typeBadges) badge.setVisible(false);
            invalidateLayout();
            return;
        }
        String title = ClientStrings.get(150000 + member.species());
        boolean nicknamed = individual != null && !individual.nickname().isEmpty();
        name.setText(ClientStrings.get(9410) + " " + member.level() + " " + (nicknamed ? individual.nickname() : title));
        if (individual == null) {
            for (Label[] row : rows) row[1].setText(ClientStrings.get(9365));
            for (Label move : moveNames) move.setVisible(false);
            for (Widget badge : moveTypes) badge.setVisible(false);
            showTypeBadges(member.species());
            invalidateLayout();
            return;
        }
        int species = member.species(), levelValue = member.level();
        StringBuilder stats = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i > 0) stats.append('/');
            stats.append(i == 0 ? member.hp() : individual.stat(species, levelValue, i));
        }
        rows.get(0)[1].setText(stats.toString());
        rows.get(1)[1].setText(join(individual.ivs()));
        rows.get(2)[1].setText(join(individual.evs()));
        rows.get(3)[1].setText(ClientStrings.get(180000 + individual.nature()));
        rows.get(4)[1].setText(ClientStrings.get(210000 + individual.ability()));
        rows.get(5)[1].setText(individual.heldItem() == 0 ? ClientStrings.get(9365)
                : ClientStrings.get(245000 + individual.heldItem() - 5000));
        // 参考图里标记槽位始终可见（未勾选时是暗色图标），因此不再按位隐藏。
        List<PokemonIndividual.Move> moves = individual.moves();
        for (int i = 0; i < moveNames.size(); i++) {
            boolean has = i < moves.size();
            moveNames.get(i).setVisible(has);
            moveTypes.get(i).setVisible(has);
            if (!has) continue;
            int moveId = moves.get(i).id();
            moveNames.get(i).setText(ClientStrings.get(110000 + moveId));
            int type = moveType(species, moveId);
            moveTypes.get(i).setTheme(type < 0 ? "move-type-none" : "move-type-" + type);
            moveTypes.get(i).reapplyTheme();
        }
        showTypeBadges(species);
        requestSprite(species);
        invalidateLayout();
    }

    /** 底板右上角的属性标牌：单属性只画一枚并靠右，双属性并排。 */
    private void showTypeBadges(int species) {
        int first = -1, second = -1;
        try {
            PokemonCatalog.Species record = PokemonCatalog.get(species);
            first = record.type1();
            second = record.type1() == record.type2() ? -1 : record.type2();
        } catch (IllegalArgumentException error) {
            first = -1;
        }
        int[] types = {first, second};
        for (int i = 0; i < typeBadges.length; i++) {
            typeBadges[i].setVisible(types[i] >= 0);
            if (types[i] >= 0) {
                typeBadges[i].setTheme("move-type-" + types[i]);
                typeBadges[i].reapplyTheme();
            }
        }
    }

    private static String join(List<Integer> values) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) text.append('/');
            text.append(values.get(i));
        }
        return text.toString();
    }

    /** 属性来自该种族可习得的招式规则，查不到时不显示标牌。 */
    private static int moveType(int species, int moveId) {
        try {
            return PokemonCatalog.move(species, moveId).type();
        } catch (IllegalArgumentException error) {
            return -1;
        }
    }

    private void requestSprite(int species) {
        if (spriteRequest != null) spriteRequest.cancel(true);
        if (spriteTexture != null) spriteTexture.destroy();
        spriteTexture = null;
        sprite = null;
        spriteRequest = CompletableFuture.supplyAsync(() -> {
            try {
                var rom = new RomFiles(ClientFiles.data("../roms")).selected(0);
                if (rom == null) throw new java.io.IOException("Missing BW ROM");
                var data = new PokedexData(rom);
                return new Loaded(data.species(species), data.sprite(species, false));
            } catch (java.io.IOException error) {
                throw new CompletionException(error);
            }
        });
    }

    private void applyPicked() {
        for (Slot slot : boxSlots) slot.refreshSelected();
        for (Slot slot : partySlots) slot.refreshSelected();
        if (picked != null) shown = memberAt(picked);
        refreshDetails();
    }

    private void clear() {
        endDrag();
        cache.clear();
        entries.clear();
        fetching.clear();
        party = List.of();
        picked = null;
        shown = null;
        busy = false;
        fetchingBox = -1;
        dragIcon = null;
        dragOverlay.setVisible(false);
        if (partyRequest != null) partyRequest.cancel(true);
        if (boxRequest != null) boxRequest.cancel(true);
        if (moveRequest != null) moveRequest.cancel(true);
        if (createRequest != null) createRequest.cancel(true);
        partyRequest = null;
        boxRequest = null;
        moveRequest = null;
        createRequest = null;
        if (spriteRequest != null) spriteRequest.cancel(true);
        spriteRequest = null;
        if (spriteTexture != null) spriteTexture.destroy();
        spriteTexture = null;
        sprite = null;
        if (iconTexture != null) iconTexture.destroy();
        iconTexture = null;
        iconPixels = null;
        iconRequests.cancel();
        if (iconRequest != null) iconRequest.cancel(false);
        iconRequest = null;
        iconStyle = null;
        uploadedIconStyle = null;
        displayedIcons = List.of();
        stripIcons = new Image[0];
        if (barIconTexture != null) barIconTexture.destroy();
        barIconTexture = null;
        barIconSheet = barIconSheet();
        for (Label badge : slotLevels) badge.setText("");
        for (Label badge : partyLevels) badge.setText("");
        currentBox = 0;
        boxCount = 1;
        refreshTabs();
        refreshDetails();
    }

    @Override
    public void close() {
        clear();
        setHardVisible(false);
    }

    // ---- 拖拽搬移 ----

    private void beginDrag(Slot source) {
        if (busy || moveRequest != null || partyRequest != null || boxRequest != null) return;
        PartyMember member = source.member();
        if (member == null) return;
        picked = source.location();
        dragSource = source;
        shown = member;
        dragIcon = icon(source.stripIndex());
        dragOverlay.setVisible(true);
        applyPicked();
        updateDropTarget();
    }

    private void endDrag() {
        dragSource = null;
        dragIcon = null;
        dragOverlay.setVisible(false);
        if (dropTarget != null) dropTarget.getAnimationState().setAnimationState(AnimationState.StateKey.get("dropTarget"), false);
        dropTarget = null;
    }

    private Slot targetAt(int x, int y) {
        for (Slot slot : boxSlots) if (slot.isInside(x, y)) return slot;
        for (Slot slot : partySlots) if (slot.isInside(x, y)) return slot;
        // 将相邻格之间的间隙平分，落点提示与松开判定使用同一区域。
        for (Slot slot : boxSlots) {
            if (x >= slot.getX() - columnGap / 2 && x < slot.getRight() + (columnGap + 1) / 2
                    && y >= slot.getY() - slotGap / 2 && y < slot.getBottom() + (slotGap + 1) / 2) return slot;
        }
        for (Slot slot : partySlots) {
            if (x >= slot.getX() && x < slot.getRight()
                    && y >= slot.getY() - slotGap / 2 && y < slot.getBottom() + (slotGap + 1) / 2) return slot;
        }
        return null;
    }

    private void updateDropTarget() {
        Slot target = dragSource == null ? null : targetAt(dragX, dragY);
        if (target == dragSource) target = null;
        if (target == dropTarget) return;
        if (dropTarget != null) dropTarget.getAnimationState().setAnimationState(AnimationState.StateKey.get("dropTarget"), false);
        dropTarget = target;
        if (dropTarget != null) dropTarget.getAnimationState().setAnimationState(AnimationState.StateKey.get("dropTarget"), true);
    }

    private void finishDrag() {
        if (dragSource == null) return;
        Pick from = picked;
        Slot target = targetAt(dragX, dragY);
        endDrag();
        if (from == null) return;
        if (target != null) { drop(from, target); return; }
        // 落在空白处或窗口外：只收回拿起状态，左侧资料保持。
        cancelPick();
        applyPicked();
    }

    private void drop(Pick from, Slot target) {
        if (from.kind() == target.kind && from.slot() == target.slot
                && (target.kind == PcPackets.PARTY || from.box() == currentBox)) {
            cancelPick();
            applyPicked();
            return;
        }
        if (target.kind == PcPackets.PARTY && target.member() == null
                && target.slot >= party.size() && party.size() >= 6) {
            notify.accept(ClientStrings.get(9515));
            cancelPick();
            applyPicked();
            return;
        }
        move(from, target.location());
    }

    /** 用 AWT 画好底栏那六个裸图标，上传成纹理后按格取用。 */
    private static BufferedImage barIconSheet() {
        int size = BAR_ICON_SIZE;
        BufferedImage sheet = new BufferedImage(size * BAR_ICONS, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < BAR_ICONS; i++) {
            g.translate(i * size + 4, 4);
            if (i == BAR_ICONS - 1) {
                // 清除筛选：白底圆形加深色 ✕。
                g.setColor(new java.awt.Color(0xF2F2F2));
                g.fillOval(0, 0, 16, 16);
                g.setColor(new java.awt.Color(0x1E2226));
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(5, 5, 11, 11);
                g.drawLine(11, 5, 5, 11);
                g.translate(-(i * size + 4), -4);
                continue;
            }
            g.setColor(new java.awt.Color(0xE4E4E4));
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (i) {
                case 0 -> { // 多选：四个角括号
                    int n = 16, arm = 5;
                    g.drawLine(0, 0, arm, 0); g.drawLine(0, 0, 0, arm);
                    g.drawLine(n - arm, 0, n, 0); g.drawLine(n, 0, n, arm);
                    g.drawLine(0, n - arm, 0, n); g.drawLine(0, n, arm, n);
                    g.drawLine(n - arm, n, n, n); g.drawLine(n, n - arm, n, n);
                }
                case 1 -> { // 排序：整圈弧线加箭头
                    g.drawArc(1, 1, 14, 14, 45, 275);
                    g.drawLine(9, 0, 15, 3); g.drawLine(9, 0, 10, 6);
                }
                case 2 -> { // 垃圾桶
                    g.drawLine(1, 4, 15, 4);
                    g.drawLine(6, 1, 10, 1);
                    g.drawRect(3, 5, 10, 10);
                    g.drawLine(6, 7, 6, 13); g.drawLine(8, 7, 8, 13); g.drawLine(10, 7, 10, 13);
                }
                case 3 -> { // 问号
                    g.drawOval(0, 0, 16, 16);
                    g.drawArc(5, 3, 7, 6, 190, 200);
                    g.drawLine(8, 10, 8, 12);
                    g.drawLine(8, 14, 8, 15);
                }
                default -> { // 纸笔
                    g.drawRect(1, 2, 11, 13);
                    g.drawLine(4, 6, 9, 6); g.drawLine(4, 9, 9, 9); g.drawLine(4, 12, 7, 12);
                    g.drawLine(10, 9, 15, 4); g.drawLine(15, 4, 16, 5); g.drawLine(16, 5, 11, 10);
                    g.drawLine(10, 9, 11, 10);
                }
            }
            g.translate(-(i * size + 4), -4);
        }
        g.dispose();
        return sheet;
    }

    /** 格子：按下并移动就拿起，松开时命中另一个格子才提交搬移。 */
    private final class Slot extends DraggableButton {
        private final int kind, slot;
        private String tooltip;

        private Slot(int kind, int slot) {
            this.kind = kind;
            this.slot = slot;
            setTheme(kind == PcPackets.PARTY ? "party-slot" : "slot");
            addCallback(this::clicked);
            setListener(new DragListener() {
                public void dragStarted() { beginDrag(Slot.this); }
                public void dragged(int deltaX, int deltaY) { updateDropTarget(); }
                public void dragStopped() { finishDrag(); }
            });
        }

        private PartyMember member() {
            return kind == PcPackets.PARTY ? partyMemberAt(slot) : entries.get(slot);
        }

        private Pick location() {
            return new Pick(kind, kind == PcPackets.PARTY ? 0 : currentBox, slot);
        }

        /** 图标条里的下标：队伍在前，箱内按槽位升序跟在后面。 */
        private int stripIndex() {
            return kind == PcPackets.PARTY ? slot : boxIconIndex(slot);
        }

        /** 没有发生拖拽的单击只切换左侧资料，搬移交给拖拽。 */
        private void clicked() {
            if (busy) return;
            PartyMember member = member();
            if (member == null) return;
            picked = location();
            shown = member;
            applyPicked();
        }

        void refreshSelected() {
            boolean selected = picked != null && picked.kind() == kind && picked.slot() == slot
                    && (kind == PcPackets.PARTY || picked.box() == currentBox);
            getAnimationState().setAnimationState(AnimationState.StateKey.get("selected"), selected);
        }

        @Override
        public boolean handleEvent(Event event) {
            if (event.isMouseEvent() && event.getMouseButton() == Event.MOUSE_RBUTTON) {
                if (event.getType() == Event.Type.MOUSE_BTNDOWN) cancelPick();
                return true;
            }
            if (event.isMouseEvent()) { dragX = event.getMouseX(); dragY = event.getMouseY(); }
            return super.handleEvent(event);
        }

        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            PartyMember member = member();
            Label badge = kind == PcPackets.PARTY ? partyLevels[slot] : slotLevels[slot];
            badge.setVisible(member != null && this != dragSource);
            badge.setText(member == null ? "" : Integer.toString(member.level()));
            String text = member == null ? null : ClientStrings.get(150000 + member.species());
            if (!java.util.Objects.equals(text, tooltip)) {
                tooltip = text;
                setTooltipContent(text);
            }
            if (member == null) return;
            Image image = icon(stripIndex());
            // 图标跟着光标走的时候，源格只留底板。
            if (image != null && this != dragSource)
                image.draw(getAnimationState(), getX() + (getWidth() - PartyIcons.TILE_SIZE) / 2,
                        getY() + (getHeight() - PartyIcons.TILE_SIZE) / 2);
            if (shiny != null && member.shiny())
                shiny.draw(getAnimationState(), getX() + 2, getY() + 1, 14, 14);
        }
    }
}
