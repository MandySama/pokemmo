package top.totodile.pokemmo.client.ui.pokemon;

import static top.totodile.pokemmo.server.game.PokemonData.*;

import com.badlogic.gdx.Gdx;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.resources.*;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.rom.nds.bw.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.server.game.*;
import top.totodile.pokemmo.server.login.LoginService.Selection;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.*;

/** Character-bound party and PC state, matching ChangePokemonPosPacket's authoritative updates. */
public final class PokemonController implements AutoCloseable {
    final ClientResources resources;
    final ClientStrings strings;
    final boolean mobile;
    private final GdxTwlRenderer renderer;
    private java.util.concurrent.CompletableFuture<Map<Integer, com.badlogic.gdx.utils.JsonValue>>
            typeEffects;

    synchronized java.util.concurrent.CompletableFuture<
                    Map<Integer, com.badlogic.gdx.utils.JsonValue>>
            typeEffects() {
        if (typeEffects == null)
            typeEffects =
                    java.util.concurrent.CompletableFuture.supplyAsync(
                            () -> {
                                try {
                                    return SummaryParticles.readEffects(
                                            resources.apply("sprites/particles.pak"));
                                } catch (IOException error) {
                                    throw new java.io.UncheckedIOException(error);
                                }
                            });
        return typeEffects;
    }

    private IntFunction<de.matthiasmann.twl.renderer.Image> itemIcons = id -> null;
    private top.totodile.pokemmo.client.ui.inventory.InventoryItems items;
    private Runnable itemsChanged = () -> {};
    private PokemonHeldItemMenu heldItemTargets;
    private PokemonMedicineWindow medicine;
    private Runnable itemUsed=()->{};
    public void onItemUsed(Runnable callback){itemUsed=callback;}
    private PokemonMachineWindow machine;
    public boolean machineKey(int key){return machine!=null&&machine.key(key);}
    public void useMachine(Widget owner,PlayerData.Item item){
        if(busy||disposed||!ready()||items==null||items.machineMove(item.itemId())<=0)return;
        closeMenu();machine=new PokemonMachineWindow(owner,this,item);machine.open();
    }

    public void useItem(Widget owner,PlayerData.Item item){
        if(busy||disposed||!ready()||items==null||!items.medicine(item.itemId()).supported()&&!items.growthItem(item.itemId()).supported())return;
        closeMenu();medicine=new PokemonMedicineWindow(owner,this,item);medicine.open();
    }
    void useMedicine(PlayerData.Item item,Pokemon pokemon,int amount,int move){
        if(busy||disposed||game==null||character==null)return;
        long version=generation;busy=true;refreshSlots();
        game.useItem(selection,character,item.itemId(),pokemon.id(),amount,move).whenComplete((result,failure)->Gdx.app.postRunnable(()->{
            if(disposed||version!=generation)return;
            busy=false;
            if(failure!=null)error.accept(strings.get(2312));
            else{state=result.pokemon();if(!result.success())error.accept(strings.get(result.message()));else itemUsed.run();}
            itemsChanged.run();refreshSlots();changed.run();
        }));
    }

    public void giveItem(Widget anchor, PlayerData.Item item) {
        if (heldItemTargets == null) heldItemTargets = new PokemonHeldItemMenu(this);
        heldItemTargets.giveToParty(anchor, item);
    }

    public void items(top.totodile.pokemmo.client.ui.inventory.InventoryItems items, Runnable changed) {
        this.items = items;
        itemsChanged = changed;
    }

    top.totodile.pokemmo.client.ui.inventory.InventoryItems items() { return items; }

    void inventory(Consumer<List<PlayerData.Item>> completed) {
        if (game == null || character == null || busy || disposed) return;
        long version = generation;
        game.inventory(selection, character).whenComplete((value, failure) -> Gdx.app.postRunnable(() -> {
            if (disposed || version != generation) return;
            if (failure == null) completed.accept(value);
            else error.accept(strings.get(2312));
        }));
    }

    void heldItem(Pokemon pokemon, int item) {
        if (!busy && !disposed && game != null && character != null)
            request(game.updatePokemonItem(selection, character, pokemon.id(), pokemon.container(), item),
                    itemsChanged);
    }
    private IntFunction<de.matthiasmann.twl.renderer.Image> grayItemIcons = id -> null;

    public void grayItemIcons(IntFunction<de.matthiasmann.twl.renderer.Image> icons) {
        grayItemIcons = icons;
    }

    de.matthiasmann.twl.renderer.Image grayItemIcon(int id) {
        return grayItemIcons.apply(id);
    }

    public void itemIcons(IntFunction<de.matthiasmann.twl.renderer.Image> icons) {
        itemIcons = icons;
    }

    de.matthiasmann.twl.renderer.Image itemIcon(int id) {
        return itemIcons.apply(id);
    }

    private final GameService game;
    private final Consumer<String> error;
    private final IntConsumer codex;
    private final List<PokemonSlot> slots = new ArrayList<>();
    private Selection selection;
    private String character;
    private long generation;
    private boolean disposed, busy;
    private Containers state = Containers.empty();
    private GameService.Follower follower = GameService.Follower.none();
    private Consumer<GameService.Follower> followerChanged = value -> {};

    public void onFollower(Consumer<GameService.Follower> changed) {
        followerChanged = changed;
    }

    private Consumer<List<Pokemon>> releasePanel = value -> {};

    void onRelease(Consumer<List<Pokemon>> action) {
        releasePanel = action;
    }

    static boolean releasable(Pokemon p) {
        return p != null
                && p.container() == PC
                && !p.egg()
                && !(p.species() == 492 && p.form() == 1);
    }

    private void confirmRelease(PokemonSlot source) {
        var selected = storageSelection.get();
        if (!selected.contains(source)) selected = List.of(source);
        var values =
                selected.stream()
                        .map(PokemonSlot::pokemon)
                        .filter(PokemonController::releasable)
                        .toList();
        closeMenu();
        if (!values.isEmpty()) releasePanel.accept(values);
    }

    void release(List<Pokemon> values) {
        if (!busy && game != null && character != null)
            request(
                    game.releasePokemon(
                            selection, character, values.stream().map(Pokemon::id).toList()),
                    storageMoved);
    }

    private Consumer<Pokemon> summary = value -> {};

    public void onSummary(Consumer<Pokemon> action) {
        summary = action;
    }

    private BiPredicate<Integer, Integer> followerDrop = (x, y) -> false;

    public void onFollowerDrop(BiPredicate<Integer, Integer> target) {
        followerDrop = target;
    }

    public GameService.Follower follower() {
        return follower;
    }

    private BwPokedex dex;
    private PokedexImages images;
    private PokemonSlot dragged;
    private String draggedId;
    private int downX, downY;
    private int dragX, dragY;
    private boolean dragging;

    private record DragIcon(PokemonSlot slot, String id, int offsetX, int offsetY) {}

    private List<DragIcon> dragIcons = List.of();
    private PopupWindow menu, moveMenu;
    private Runnable changed = () -> {};
    private Runnable storageMoved = () -> {};

    void onStorageMoved(Runnable action) {
        storageMoved = action;
    }

    private record BoxDrop(Widget widget, int box) {}

    private final List<BoxDrop> boxDrops = new ArrayList<>();

    void boxDrop(Widget widget, int box) {
        boxDrops.add(new BoxDrop(widget, box));
    }

    public PokemonController(
            ClientResources resources,
            GdxTwlRenderer renderer,
            boolean mobile,
            GameService game,
            Consumer<String> error,
            IntConsumer codex) {
        this.resources = resources;
        strings = resources.strings();
        this.renderer = renderer;
        this.mobile = mobile;
        this.game = game;
        this.error = error;
        this.codex = codex;
    }

    public void onChanged(Runnable changed) {
        var previous = this.changed;
        this.changed =
                () -> {
                    previous.run();
                    changed.run();
                };
    }

    public void bind(Selection selection, String character) {
        if (heldItemTargets != null) heldItemTargets.close();
        generation++;
        cancelDrag();
        closeMenu();
        this.selection = selection;
        this.character = character;
        busy = false;
        state = Containers.empty();
        refreshSlots();
        changed.run();
        follower = GameService.Follower.none();
        followerChanged.accept(follower);
        if (character != null && selection != null && game != null) {
            reload();
            long version = generation;
            game.follower(selection, character)
                    .whenComplete(
                            (value, failure) ->
                                    Gdx.app.postRunnable(
                                            () -> {
                                                if (!disposed
                                                        && version == generation
                                                        && failure == null) {
                                                    follower = value;
                                                    followerChanged.accept(value);
                                                }
                                            }));
        }
    }

    public Containers state() {
        return state;
    }

    public boolean busy() {
        return busy;
    }

    boolean ready() {
        if (dex != null) return true;
        try {
            dex = new BwPokedex(resources.roms().get(RomType.BLACK_WHITE), strings);
            dex.loadDataPackage(resources.apply("data.pak"));
            if (game != null) dex.loadTiers(game.pokedexTiers());
            images = new PokedexImages(new BwPokemonSprites(dex), renderer, resources);
            return true;
        } catch (IOException | RuntimeException failure) {
            dex = null;
            Gdx.app.error("pokemon", "Cannot load Pokemon ROM resources", failure);
            return false;
        }
    }

    BwPokedex dex() {
        return ready() ? dex : null;
    }

    PokedexImages images() {
        return ready() ? images : null;
    }

    public de.matthiasmann.twl.renderer.Image characterPartyIcon(Pokemon pokemon) {
        return ready()
                ? images.partyIcon(
                        pokemon.species(),
                        pokemon.form(),
                        pokemon.gender(),
                        pokemon.egg(),
                        !mobile && !pokemon.egg() && pokemon.hp() == 0,
                        pokemon.shiny(),
                        pokemon.alpha())
                : null;
    }

    public String characterPartyName(Pokemon pokemon) {
        return ready() ? dex.name(pokemon.species()) : "#" + pokemon.species();
    }

    String name(Pokemon p) {
        String species = ready() ? dex.name(p.species()) : "#" + p.species();
        return p.egg() ? strings.format(1893, species) : p.name().isEmpty() ? species : p.name();
    }

    PokemonSlot slot(int container, int position, boolean storage) {
        var slot = new PokemonSlot(this, container, position, storage);
        slots.add(slot);
        return slot;
    }

    public PartyPanel party(boolean storage) {
        return new PartyPanel(this, storage);
    }

    public void removeParty(PartyPanel party) {
        slots.removeAll(Arrays.asList(party.slots));
    }

    void moveMove(String id, int from, int to) {
        if (!busy && game != null && character != null)
            request(game.movePokemonMove(selection, character, id, from, to));
    }

    void setSetting(String id, GameService.PokemonSetting setting, int index, int value) {
        if (!busy && game != null && character != null)
            request(game.setPokemonSetting(selection, character, id, setting, index, value));
    }

    void selectParticle(String pokemonId, int particle) {
        if (!busy && game != null && character != null)
            request(game.selectPokemonParticle(selection, character, pokemonId, particle));
    }

    void setMarks(String pokemonId, int marks) {
        if (!busy && game != null && character != null)
            request(game.setPokemonMarks(selection, character, pokemonId, marks));
    }

    void setFlag(String pokemonId, GameService.PokemonFlag flag, boolean enabled) {
        if (!busy && game != null && character != null)
            request(game.setPokemonFlag(selection, character, pokemonId, flag, enabled));
    }

    public void reload() {
        if (game != null && character != null) request(game.pokemon(selection, character));
    }

    private void request(CompletionStage<Containers> operation) {
        request(operation, () -> {});
    }

    private void request(CompletionStage<Containers> operation, Runnable success) {
        long version = generation;
        busy = true;
        refreshSlots();
        operation.whenComplete(
                (value, failure) ->
                        Gdx.app.postRunnable(
                                () -> {
                                    if (disposed || version != generation) return;
                                    busy = false;
                                    if (failure == null) {
                                        state = value;
                                        success.run();
                                    } else {
                                        Throwable cause = failure;
                                        while (cause.getCause() != null) cause = cause.getCause();
                                        Gdx.app.error(
                                                "pokemon",
                                                "Pokemon container request failed",
                                                failure);
                                        // Reference rejects failed moves and restores the original
                                        // containers.
                                        if (!(cause instanceof PokemonData.Rejected))
                                            error.accept(strings.get(2312));
                                    }
                                    refreshSlots();
                                    changed.run();
                                }));
    }

    private void refreshSlots() {
        for (var slot : slots) slot.refresh();
    }

    void move(PokemonSlot from, int container, int position) {
        if (busy
                || game == null
                || character == null
                || from.pokemon() == null
                || from.container == container && from.position == position) return;
        closeMenu();
        var selected = storageSelection.get();
        if (from.storage && selected.size() > 1 && selected.contains(from)) {
            var changes = new ArrayList<Move>();
            int index = 0;
            for (var slot : selected) {
                int target =
                        container == PC
                                ? position + slot.position - from.position
                                : position + index++;
                if (target < 0
                        || container == PC && target / BOX_SIZE != position / BOX_SIZE
                        || container == PARTY && target >= PARTY_SIZE) return;
                changes.add(new Move(slot.container, slot.position, container, target));
            }
            request(game.movePokemon(selection, character, changes), storageMoved);
        } else
            request(
                    game.movePokemon(
                            selection,
                            character,
                            List.of(new Move(from.container, from.position, container, position))),
                    storageMoved);
    }

    void sort(int box) {
        if (!busy && game != null && character != null)
            request(game.sortPokemon(selection, character, box));
    }

    void summary(PokemonSlot slot) {
        var p = slot.pokemon();
        if (p == null || busy) return;
        closeMenu();
        summary.accept(p);
    }

    void menu(PokemonSlot slot, boolean pc) {
        menu(slot, pc, slot.getRight(), slot.getY());
    }

    void menu(PokemonSlot slot, boolean pc, int mouseX, int mouseY) {
        var p = slot.pokemon();
        if (p == null || busy) return;
        closeMenu();
        var actions = new ArrayList<Button>();
        add(actions, 1801, mobile ? null : () -> summary.accept(p));
        if (pc && !mobile) {
            add(actions, 8043, () -> {});
            add(actions, 5827, () -> {});
            var destination =
                    new Button(strings.get(2301)) {
                        @Override
                        protected boolean handleEvent(Event event) {
                            if (event.getType() == Event.Type.MOUSE_ENTERED)
                                openMoveMenu(this, slot);
                            return super.handleEvent(event);
                        }
                    };
            destination.setTheme("/pokemon-menu-button");
            destination.setCanAcceptKeyboardFocus(false);
            destination.addCallback(() -> openMoveMenu(destination, slot));
            actions.add(destination);
            add(actions, 1412, () -> {});
            add(actions, 2302, releasable(p) ? () -> confirmRelease(slot) : null);
        } else {
            add(actions, 1729, () -> codex.accept(p.species()));
            if (pc) {
                if (slot.container == PC)
                    add(
                            actions,
                            2301,
                            () -> {
                                int target = firstEmpty(PARTY, 0, PARTY_SIZE);
                                if (target < 0) error.accept(strings.get(2305));
                                else move(slot, PARTY, target);
                            });
                else add(actions, 2301, () -> moveToBox(slot));
            } else {
                if (slot.position > 0)
                    add(actions, 2303, () -> move(slot, PARTY, slot.position - 1));
                if (slot.position + 1 < PARTY_SIZE && state.at(PARTY, slot.position + 1) != null)
                    add(actions, 2304, () -> move(slot, PARTY, slot.position + 1));
                if (slot.position > 0) add(actions, 2313, () -> move(slot, PARTY, 0));
            }
            if (!pc && !mobile) {
                boolean matching =
                        follower.species() == p.species()
                                && follower.form() == p.form()
                                && follower.gender() == p.gender()
                                && follower.shiny() == p.shiny()
                                && follower.alpha() == p.alpha();
                if (matching) add(actions, 2256, () -> selectFollower(2, null, 0));
                else {
                    if (!p.egg())
                        add(
                                actions,
                                strings.format(2262, name(p)),
                                () -> selectFollower(1, p.id(), 0));
                    add(
                            actions,
                            strings.format(2263, Integer.toString(slot.position + 1)) + " Slot",
                            () -> selectFollower(0, null, slot.position));
                }
            }
            add(actions, 53, () -> {});
        }
        var popup = new PopupWindow(slot);
        popup.setTheme("/pokemon-menu");
        popup.setRequestCloseCallback(this::closeMenu);
        popup.setCloseAndIgnoreMouseDownOutside(true);
        popup.setRouteToParentPopup(true);
        var content =
                new Widget() {
                    @Override
                    protected void layout() {
                        int y = getInnerY();
                        for (var b : actions) {
                            b.setPosition(getInnerX(), y);
                            b.setSize(getInnerWidth(), mobile ? 40 : 31);
                            y += b.getHeight();
                        }
                    }
                };
        content.setTheme("");
        for (var b : actions) content.add(b);
        popup.add(content);
        menu = popup;
        if (popup.openPopup()) {
            int
                    w =
                            mobile
                                    ? 240
                                    : Math.max(
                                            pc ? 100 : 160,
                                            actions.stream()
                                                            .mapToInt(Button::getPreferredWidth)
                                                            .max()
                                                            .orElse(0)
                                                    + popup.getBorderHorizontal()),
                    h = actions.size() * (mobile ? 40 : 31) + popup.getBorderVertical();
            popup.setSize(w, h);
            int x = pc && !mobile ? mouseX + 2 : slot.getX() - w;
            if (x + w > slot.getGUI().getWidth()) x = mouseX - w - 2;
            popup.setPosition(
                    Math.max(0, x),
                    Math.max(
                            0,
                            Math.min(
                                    pc && !mobile ? mouseY : slot.getY(),
                                    slot.getGUI().getHeight() - h)));
        }
    }

    public void hoverMenus(int x, int y) {
        if (moveMenu == null
                || !moveMenu.isOpen()
                || menu == null
                || !menu.isOpen()
                || moveMenu.isInside(x, y)
                || !menu.isInside(x, y)) return;
        Widget target = menu.getGUI().getWidgetAt(x, y);
        while (target != null && !(target instanceof Button)) target = target.getParent();
        if (target != null && target != moveMenu.getOwner()) {
            moveMenu.closePopup();
            moveMenu = null;
        }
    }

    private void openMoveMenu(Button owner, PokemonSlot slot) {
        if (moveMenu != null && moveMenu.isOpen() || menu == null || !menu.isOpen()) return;
        var actions = new ArrayList<Button>();
        if (firstEmpty(PARTY, 0, PARTY_SIZE) >= 0)
            add(actions, 1117, () -> moveToContainer(slot, PARTY, 0));
        for (int box = 0; box < 11; box++)
            if (slot.container != PC || slot.position / BOX_SIZE != box) {
                final int targetBox = box;
                add(
                        actions,
                        boxName(box),
                        () -> {
                            moveToContainer(slot, PC, targetBox);
                        });
            }
        var popup = new PopupWindow(owner);
        popup.setTheme("/pokemon-menu");
        popup.setRequestCloseCallback(this::closeMenu);
        popup.setCloseAndIgnoreMouseDownOutside(true);
        popup.setRouteToParentPopup(true);
        var content =
                new Widget() {
                    @Override
                    protected void layout() {
                        int y = getInnerY();
                        for (var b : actions) {
                            b.setPosition(getInnerX(), y);
                            b.setSize(getInnerWidth(), 31);
                            y += 31;
                        }
                    }
                };
        content.setTheme("");
        for (var b : actions) content.add(b);
        popup.add(content);
        moveMenu = popup;
        if (popup.openPopup()) {
            int width =
                    Math.max(
                            100,
                            actions.stream().mapToInt(Button::getPreferredWidth).max().orElse(0)
                                    + popup.getBorderHorizontal());
            int height = actions.size() * 31 + popup.getBorderVertical();
            popup.setSize(width, height);
            int x = menu.getRight();
            if (x + width > owner.getGUI().getWidth()) x = menu.getX() - width;
            popup.setPosition(
                    Math.max(0, x),
                    Math.max(0, Math.min(owner.getY(), owner.getGUI().getHeight() - height)));
        }
    }

    private void moveToContainer(PokemonSlot source, int container, int box) {
        if (busy || game == null || character == null || source.pokemon() == null) return;
        var selected = storageSelection.get();
        if (!selected.contains(source)) selected = List.of(source);
        var targets = new ArrayList<Integer>();
        int first = container == PC ? box * BOX_SIZE : 0,
                amount = container == PC ? BOX_SIZE : PARTY_SIZE;
        for (int i = first; i < first + amount && targets.size() < selected.size(); i++)
            if (state.at(container, i) == null) targets.add(i);
        if (targets.size() < selected.size())
            notification.accept(
                    container == PC ? strings.format(2306, boxName(box)) : strings.get(2305));
        if (targets.isEmpty()) return;
        var changes = new ArrayList<Move>();
        for (int i = 0; i < targets.size(); i++) {
            var slot = selected.get(i);
            changes.add(new Move(slot.container, slot.position, container, targets.get(i)));
        }
        closeMenu();
        request(game.movePokemon(selection, character, changes), storageMoved);
    }

    private void selectFollower(int type, String object, int position) {
        if (busy || character == null) return;
        long version = generation;
        busy = true;
        game.selectFollower(selection, character, type, object, position)
                .whenComplete(
                        (value, failure) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            if (disposed || version != generation) return;
                                            busy = false;
                                            if (failure != null) {
                                                Gdx.app.error(
                                                        "pokemon",
                                                        "Follower selection failed",
                                                        failure);
                                                error.accept(strings.get(2312));
                                                return;
                                            }
                                            follower = value;
                                            followerChanged.accept(value);
                                            changed.run();
                                        }));
    }

    private void add(List<Button> list, String text, Runnable callback) {
        var b = new Button(text);
        b.setTheme("/pokemon-menu-button");
        b.setCanAcceptKeyboardFocus(false);
        b.addCallback(
                () -> {
                    closeMenu();
                    callback.run();
                });
        list.add(b);
    }

    private void add(List<Button> list, int text, Runnable callback) {
        var b = new Button(strings.get(text));
        b.setTheme("/pokemon-menu-button");
        b.setCanAcceptKeyboardFocus(false);
        b.setEnabled(callback != null);
        if (callback != null)
            b.addCallback(
                    () -> {
                        closeMenu();
                        callback.run();
                    });
        list.add(b);
    }

    private void moveToBox(PokemonSlot slot) {
        int box = storageBox.getAsInt(), target = firstEmpty(PC, box * BOX_SIZE, BOX_SIZE);
        if (target < 0) notification.accept(strings.format(2306, boxName(box)));
        else move(slot, PC, target);
    }

    private Supplier<List<PokemonSlot>> storageSelection = List::of;
    private Consumer<String> notification = text -> {};

    public void onNotification(Consumer<String> notification) {
        this.notification = notification;
    }

    void storageSelection(Supplier<List<PokemonSlot>> selection) {
        storageSelection = selection;
    }

    private IntSupplier storageBox = () -> 0;

    void storageBox(IntSupplier box) {
        storageBox = box;
    }

    int firstEmpty(int container, int first, int amount) {
        for (int i = first; i < first + amount; i++) if (state.at(container, i) == null) return i;
        return -1;
    }

    String boxName(int box) {
        String custom = resources.preferences().get("client.pc." + character + ".box." + box, "");
        return custom.isEmpty() ? strings.format(1119, Integer.toString(box + 1)) : custom;
    }

    void rename(int box, String value) {
        if (character == null) return;
        if (!value.matches("[ |\\p{L}|\\p{N}|\\p{P}]{1,20}")) value = "";
        resources.preferences().save(Map.of("client.pc." + character + ".box." + box, value));
        changed.run();
    }

    public boolean closeMenu() {
        boolean medicineOpen=medicine!=null&&medicine.isOpen();if(medicineOpen)medicine.closePopup();
        if(machine!=null&&machine.isOpen()){machine.closePopup();medicineOpen=true;}
        if (moveMenu != null) {
            moveMenu.closePopup();
            moveMenu = null;
        }
        if (menu == null) return medicineOpen;
        boolean open = menu.isOpen();
        menu.closePopup();
        menu = null;
        return open;
    }

    public boolean hasMenu() {
        return machine!=null&&machine.isOpen()||medicine!=null&&medicine.isOpen()||menu != null && menu.isOpen();
    }

    public boolean touchDown(int x, int y, int button) {
        if (button != 0 || busy) return false;
        var slot = hit(x, y);
        if (slot == null || slot.pokemon() == null || !slot.canDragPokemon()) return false;
        dragged = slot;
        draggedId = slot.pokemon().id();
        downX = x;
        downY = y;
        dragging = false;
        return false;
    }

    public boolean touchDragged(int x, int y) {
        if (dragged == null) return false;
        dragX = x;
        dragY = y;
        if (!dragging && Math.abs(x - downX) + Math.abs(y - downY) > 6) {
            dragging = true;
            var selected = storageSelection.get();
            var group = dragged.storage && selected.contains(dragged) ? selected : List.of(dragged);
            dragIcons =
                    group.stream()
                            .filter(slot -> slot.pokemon() != null)
                            .map(
                                    slot ->
                                            new DragIcon(
                                                    slot,
                                                    slot.pokemon().id(),
                                                    slot.getX() - dragged.getX(),
                                                    slot.getY() - dragged.getY()))
                            .toList();
            dragged.cancelClick();
            dragged.getAnimationState().setAnimationState(Button.STATE_PRESSED, true);
        }
        return dragging;
    }

    public boolean hasDragCandidate() {
        return dragged != null;
    }

    public boolean touchUp(int x, int y) {
        if (dragged == null) return false;
        var from = dragged;
        boolean moved = dragging;
        var to = hit(x, y);
        if (moved
                && from.pokemon() != null
                && from.pokemon().id().equals(draggedId)
                && to != null
                && to != from) move(from, to.container, to.position);
        else if (moved && from.pokemon() != null && from.pokemon().id().equals(draggedId)) {
            boolean handled = false;
            for (var target : boxDrops)
                if (target.widget.getGUI() != null && target.widget.isInside(x, y)) {
                    handled = true;
                    int empty = firstEmpty(PC, target.box * BOX_SIZE, BOX_SIZE);
                    if (empty >= 0) move(from, PC, empty);
                    else notification.accept(strings.format(2306, boxName(target.box)));
                    break;
                }
            if (!handled
                    && to == null
                    && !mobile
                    && !from.storage
                    && from.container == PARTY
                    && !from.pokemon().egg()
                    && followerDrop.test(x, y)) selectFollower(1, from.pokemon().id(), 0);
        }
        cancelDrag();
        return moved;
    }

    public void cancelDrag() {
        if (dragged != null)
            dragged.getAnimationState().setAnimationState(Button.STATE_PRESSED, false);
        dragged = null;
        draggedId = null;
        dragging = false;
        dragIcons = List.of();
    }

    boolean draggingIcon(PokemonSlot slot) {
        return dragging
                && slot.pokemon() != null
                && dragIcons.stream()
                        .anyMatch(icon -> icon.slot == slot && icon.id.equals(slot.pokemon().id()));
    }

    public void paintDrag(de.matthiasmann.twl.renderer.AnimationState animation) {
        if (!dragging || dragged == null || dragged.pokemon() == null || images() == null) return;
        int size = mobile ? 72 : 36;
        for (var icon : dragIcons) {
            if (icon.slot.pokemon() == null || !icon.id.equals(icon.slot.pokemon().id())) continue;
            icon.slot
                    .pokemonIcon()
                    .draw(
                            animation,
                            dragX - size / 2 + icon.offsetX,
                            dragY - size / 2 + icon.offsetY,
                            size,
                            size);
        }
    }

    private PokemonSlot hit(int x, int y) {
        for (int i = slots.size() - 1; i >= 0; i--) {
            var slot = slots.get(i);
            if (slot.getGUI() != null && slot.isVisible() && slot.isInside(x, y)) {
                Widget top = slot.getGUI().getWidgetAt(x, y);
                while (top != null && !(top instanceof PokemonSlot)) top = top.getParent();
                if (top == slot) return slot;
            }
        }
        return null;
    }

    @Override
    public void close() {
        disposed = true;
        if (heldItemTargets != null) heldItemTargets.close();
        generation++;
        cancelDrag();
        closeMenu();
        if (images != null) images.close();
    }
}
