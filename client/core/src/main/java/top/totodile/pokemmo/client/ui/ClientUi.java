package top.totodile.pokemmo.client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.theme.ThemeManager;

import top.totodile.pokemmo.client.config.SavedAccounts;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.rom.RomInfo;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.character.CharacterCreationWindow;
import top.totodile.pokemmo.client.ui.character.CharacterDeleteWindow;
import top.totodile.pokemmo.client.ui.character.CharacterSelectionPanel;
import top.totodile.pokemmo.client.ui.common.ExitConfirmation;
import top.totodile.pokemmo.client.ui.common.FrameRateCounter;
import top.totodile.pokemmo.client.ui.common.PlayerWindow;
import top.totodile.pokemmo.client.ui.inventory.InventoryItems;
import top.totodile.pokemmo.client.ui.inventory.InventoryWindow;
import top.totodile.pokemmo.client.ui.login.AnnouncementsPanel;
import top.totodile.pokemmo.client.ui.login.LoginController;
import top.totodile.pokemmo.client.ui.login.LoginPanel;
import top.totodile.pokemmo.client.ui.login.ServerSelectionPanel;
import top.totodile.pokemmo.client.ui.mail.MailWindow;
import top.totodile.pokemmo.client.ui.mail.PcMenuWindow;
import top.totodile.pokemmo.client.ui.management.ClientManagementWindow;
import top.totodile.pokemmo.client.ui.management.ModManagementWindow;
import top.totodile.pokemmo.client.ui.menu.DesktopMenu;
import top.totodile.pokemmo.client.ui.menu.MobileSideMenu;
import top.totodile.pokemmo.client.ui.menu.MobileStatusIcons;
import top.totodile.pokemmo.client.ui.pokedex.PokedexWindow;
import top.totodile.pokemmo.client.ui.pokemon.*;
import top.totodile.pokemmo.client.ui.settings.SettingsWindow;
import top.totodile.pokemmo.client.ui.trainer.TrainerCardWindow;
import top.totodile.pokemmo.client.ui.world.WorldToolsWindow;
import top.totodile.pokemmo.server.game.GameService;
import top.totodile.pokemmo.server.game.PlayerData;

import java.io.IOException;
import java.net.*;
import java.util.function.Function;
import java.util.function.LongSupplier;

/** 客户端公共界面、页面导航及 TWL 渲染和输入生命周期。 */
public final class ClientUi implements Disposable {
    private final GdxTwlRenderer renderer;
    private final GUI gui;
    private final ThemeManager theme;
    private final ClientResources resources;
    private final boolean ownsResources;
    private final Widget logo = new Widget();
    private final java.util.Map<Integer, de.matthiasmann.twl.renderer.Image> itemIcons =
            new java.util.HashMap<>();
    private final LoginPanel loginPanel;
    private final Widget root;
    private final LoginController controller;
    private final ServerSelectionPanel serverFrame;
    private final CharacterSelectionPanel characterPage;
    private final Label romInfo;
    private final AnnouncementsPanel announcements;
    private final DesktopMenu desktopMenu;
    private final MobileStatusIcons mobileStatus;
    private final java.util.function.IntSupplier batteryPercent;
    private top.totodile.pokemmo.client.ui.common.TouchScrollPane playerTouchScroll;
    private final MobileSideMenu mobileMenu;
    private final ClientManagementWindow clientManagement;
    private final ModManagementWindow mods;
    private final SettingsWindow settings;
    private final PokedexWindow pokedex;
    private final InventoryItems inventoryItems;
    private final InventoryWindow inventory;
    private top.totodile.pokemmo.client.ui.inventory.HotkeyBar hotkeyBar;
    private final TrainerCardWindow trainerCard;
    private final MailWindow mail;
    private final PcMenuWindow pcMenu;
    private final top.totodile.pokemmo.client.ui.market.GlobalTradeMarketWindow market;
    private final top.totodile.pokemmo.client.ui.market.GiftShopWindow giftShop;
    private final top.totodile.pokemmo.client.ui.pokemon.IncubatorWindow incubator;
    private final PokemonController pokemon;
    private final PokemonStorageWindow storage;
    private final java.util.List<PokemonSummaryWindow> pokemonSummaries =
            new java.util.ArrayList<>();
    private final WorldToolsWindow worldTools;
    private java.util.function.Function<
                    GameService.Entry, java.util.concurrent.CompletionStage<Void>>
            teleportWorld;

    public void onTeleport(
            java.util.function.Function<
                            GameService.Entry, java.util.concurrent.CompletionStage<Void>>
                    action) {
        teleportWorld = action;
    }

    private PartyPanel party;
    private GameService.Entry playerEntry;
    private final java.util.Set<Integer> playerKeys = new java.util.HashSet<>();
    private int repeatingPlayerKey;
    private long playerRepeatAt;
    private boolean generatingRandomItems;
    private GameService.PokedexState displayedPokedexState;
    private char suppressedPlayerCharacter;
    private final CharacterCreationWindow characterCreation;
    private final CharacterCreationWindow characterCustomization;
    private final CharacterDeleteWindow characterDelete;
    private final ExitConfirmation exitConfirmation;
    private final Label fps = new Label("0.00");
    private final FrameRateCounter frameRate = new FrameRateCounter(System::nanoTime);
    private final InputAdapter input;
    private final ClientStrings strings;
    private final boolean mobile;
    private final LongSupplier notificationClock;
    private int offsetX, offsetY;
    private final top.totodile.pokemmo.server.game.GameService gameService;
    private top.totodile.pokemmo.server.login.LoginService.Selection characterSelection;
    private boolean creatingCharacter;
    private long characterLoadVersion;
    private boolean disposed;
    private boolean enteringWorld;
    private top.totodile.pokemmo.client.ui.world.WorldActions worldActions;
    private top.totodile.pokemmo.client.ui.world.WorldHud worldHud;
    private top.totodile.pokemmo.client.world.WorldScene worldScene;
    private top.totodile.pokemmo.client.ui.world.KantoStoryController story;
    private Runnable worldExit;
    private final top.totodile.pokemmo.client.ui.common.ActionConfirmDialog worldLogout;
    private final java.util.Map<Widget, Boolean> worldHidden = new java.util.IdentityHashMap<>();
    private Runnable leaveWorld = () -> {};
    private top.totodile.pokemmo.client.battle.EncounterWindow encounterWindow;
    private top.totodile.pokemmo.client.ui.pokemon.EncounterRecorderWindow encounterRecorder;
    private top.totodile.pokemmo.client.battle.MoveAnimationTestWindow animationTests;

    private top.totodile.pokemmo.client.ui.world.StoryInspectionWindow storyInspection;
    private top.totodile.pokemmo.client.ui.world.StoryDebugWindow storyDebug;
    private void openStoryInspection(){
        if(worldScene==null||playerEntry==null||controller.selection()==null||gameService==null)return;
        if(storyInspection==null){storyInspection=new top.totodile.pokemmo.client.ui.world.StoryInspectionWindow(
                root,mobile,()->gameService.inspectStory(controller.selection(),playerEntry.character().id(),worldScene.movement().position()),
                (id,species)->story==null?resources.strings().get(id):story.previewText(id,species),this::restoreWorldInput);
            storyInspection.onEdit(this::openStoryDebug);
            storyInspection.mapName(()->worldScene==null?"":resources.strings().getOrDefault(worldScene.locationStringId(),worldScene.locationName()));
            storyInspection.itemNames(inventoryItems::name);
        }
        storyInspection.open();restoreWorldInput();
    }

    private void openStoryDebug(){
        if(worldScene==null||playerEntry==null||gameService==null)return;
        if(storyDebug==null)storyDebug=new top.totodile.pokemmo.client.ui.world.StoryDebugWindow(root,mobile,
                ()->gameService.storyDebugState(controller.selection(),playerEntry.character().id()),
                this::editStoryDebug,this::restoreWorldInput);
        storyDebug.open();restoreWorldInput();
    }
    private java.util.concurrent.CompletionStage<Void> editStoryDebug(top.totodile.pokemmo.server.game.StoryDebug.Request request){
        var result=new java.util.concurrent.CompletableFuture<Void>();
        if(enteringWorld||worldScene==null||playerEntry==null||worldEntry==null||encounterActive()
                ||story!=null&&story.busy()){
            result.completeExceptionally(new IllegalStateException("请先结束当前对话、剧情或对战"));return result;
        }
        var selection=controller.selection();String id=playerEntry.character().id();
        enteringWorld=true;worldScene.setInputBlocked(true);if(story!=null)story.suspend();
        gameService.savePosition(selection,id,worldScene.movement().position())
                .thenCompose(v->gameService.editStory(selection,id,request))
                .thenCompose(v->gameService.enter(selection,id))
                .whenComplete((entry,error)->Gdx.app.postRunnable(()->{
                    if(error!=null){enteringWorld=false;if(story!=null)story.resume();restoreWorldInput();result.completeExceptionally(error);return;}
                    if(disposed||selection!=controller.selection()){enteringWorld=false;result.completeExceptionally(new IllegalStateException("角色会话已切换"));return;}
                    if(story!=null)story.close();playerEntry=entry;
                    try{worldEntry.apply(entry).whenComplete((v,failure)->Gdx.app.postRunnable(()->{
                        enteringWorld=false;restoreWorldInput();
                        if(failure!=null)result.completeExceptionally(failure);else result.complete(null);
                    }));}catch(RuntimeException failure){enteringWorld=false;result.completeExceptionally(failure);}
                }));
        return result;
    }

    private void openAnimationTests() {
        if (mobile) return;
        if (resources.roms().get(RomType.BLACK_WHITE) == null) {
            entryError.show("请先导入黑／白 ROM，再打开招式动画测试");
            return;
        }
        if (encounterPending) return;
        if (animationTests != null) animationTests.closePopup();
        dismissEncounter();
        generateRandomEncounter(1, false, true);
    }

    private void showAnimationTests() {
        try {
            if (animationTests == null)
                animationTests =
                        new top.totodile.pokemmo.client.battle.MoveAnimationTestWindow(
                                root, resources, () -> encounterWindow, this::openAnimationTests);
            animationTests.open();
        } catch (IOException error) {
            Gdx.app.error("Animation test", "Cannot open animation catalogue", error);
            entryError.show("无法加载招式测试数据：" + error.getMessage());
        }
    }

    private boolean encounterPending;
    private long encounterVersion;
    private long battleHeartbeatAt;
    private top.totodile.pokemmo.client.world.WorldScene preparedBattleWorld;
    private Object preparedBattleMap;
    private void prepareBattleEnvironment(top.totodile.pokemmo.client.world.WorldScene world){
        if(gameService==null||resources.roms().get(top.totodile.pokemmo.client.rom.RomType.BLACK_WHITE)==null)return;
        if(preparedBattleWorld==world&&java.util.Objects.equals(preparedBattleMap,world.movement().map().id()))return;
        preparedBattleWorld=world;preparedBattleMap=world.movement().map().id();
        gameService.prepareBattle().exceptionally(error->{Gdx.app.error("battle","Cannot prepare battle engine",error);return null;});
        try{
            inventoryItems.gameData(gameService);
            if(encounterWindow==null)encounterWindow=new top.totodile.pokemmo.client.battle.EncounterWindow(root,resources,renderer,notificationClock,this::restoreWorldInput);
            encounterWindow.inventory(inventoryItems,this::loadBattleInventory);
            encounterWindow.prepareEnvironment(world.battleEnvironment());
        }catch(IOException|RuntimeException error){Gdx.app.error("battle","Cannot prepare map battle assets",error);}
    }

    private boolean encounterActive() {
        return encounterPending || encounterWindow != null && encounterWindow.isOpen();
    }

    private void dismissEncounter() {
        encounterVersion++;
        encounterPending = false;
        if (encounterWindow != null) {
            if(encounterWindow.isOpen()&&gameService!=null&&playerEntry!=null&&controller.selection()!=null)
                gameService.disconnectBattle(controller.selection(),playerEntry.character().id());
            encounterWindow.disconnect();
        }
    }

    public void onLeaveWorld(Runnable action) {
        leaveWorld = java.util.Objects.requireNonNull(action);
    }

    public void showCharacters() {
        if (gameService != null) gameService.prepareBattle().exceptionally(error -> {
            Gdx.app.error("battle", "Cannot prepare battle engine", error);
            return null;
        });
        if(story!=null){story.close();story=null;}
        if(resources.audio()!=null)resources.audio().music(null);
        if (worldActions == null) return;
        if (encounterRecorder != null) {
            encounterRecorder.closePopup();
            encounterRecorder = null;
        }
        dismissEncounter();
        playerKeys.clear();
        repeatingPlayerKey = 0;
        suppressedPlayerCharacter = 0;
        characterCustomization.closePopup();
        inventory.closePopup();
        trainerCard.closePopup();
        mail.forceClose();
        pcMenu.closePopup();
        market.closePopup();
        giftShop.closePopup();
        incubator.closePopup();
        storage.forceClose();
        closePokemonSummaries();
        if(storyInspection!=null)storyInspection.closePopup();
        if(storyDebug!=null)storyDebug.closePopup();
        worldTools.forceClose();
        pokemon.bind(null, null);
        if (playerEntry != null && gameService != null && controller.selection() != null)
            gameService.leave(controller.selection(), playerEntry.character().id());
        playerEntry = null;
        pokedex.closePopup();
        pokemon.removeParty(party);
        party = null;
        worldHud.chat().close();
        root.removeChild(worldHud.chat());
        root.removeChild(worldHud);
        worldHud = null;
        worldScene = null;
        hotkeyBar = null;
        root.removeChild(worldActions);
        worldActions = null;
        worldHidden.forEach(Widget::setVisible);
        worldHidden.clear();
        enteringWorld = false;
        refreshLoginState();
        if (controller.selection() != null) loadCharacters(controller.selection());
        Gdx.input.setInputProcessor(input);
        root.invalidateLayout();
    }

    /** Reference ROM loadItems runs before the world HUD becomes visible. */
    public void prepareWorldData() throws java.io.IOException {
        if (!mobile
                && resources.roms().get(top.totodile.pokemmo.client.rom.RomType.BLACK_WHITE)
                        != null) inventoryItems.prepare();
    }

    public void prepareWorldIcons() throws java.io.IOException {
        if (mobile
                || resources.roms().get(top.totodile.pokemmo.client.rom.RomType.BLACK_WHITE)
                        == null) return;
        inventoryItems.load();
        for (int id : new int[] {5436, 5432, 5437, 5001, 5431, 5471, 5452, 5459, 5626}) {
            inventoryItems.tabIcon(id, false);
            inventoryItems.tabIcon(id, true);
        }
    }

    public com.badlogic.gdx.InputProcessor showWorld(
            top.totodile.pokemmo.client.world.WorldScene world) {
        return showWorld(world, playerEntry);
    }

    public com.badlogic.gdx.InputProcessor showWorld(
            top.totodile.pokemmo.client.world.WorldScene world, GameService.Entry entry) {
        if(entry!=null)world.motion().runningShoes(false);
        if (encounterRecorder != null) {
            encounterRecorder.closePopup();
            encounterRecorder = null;
        }
        dismissEncounter();
        playerKeys.clear();
        repeatingPlayerKey = 0;
        suppressedPlayerCharacter = 0;
        boolean replacing = worldScene != null;
        if (replacing) {
            characterCustomization.closePopup();
            inventory.closePopup();
            trainerCard.closePopup();
            mail.forceClose();
            pcMenu.closePopup();
            market.closePopup();
        giftShop.closePopup();
        incubator.closePopup();
            storage.forceClose();
            closePokemonSummaries();
            if(storyInspection!=null)storyInspection.closePopup();
        if(storyDebug!=null)storyDebug.closePopup();
            worldTools.closeForTeleport();
            pokedex.closePopup();
            pokemon.removeParty(party);
            worldHud.chat().close();
            root.removeChild(worldHud.chat());
            root.removeChild(worldHud);
            root.removeChild(worldActions);
        }
        playerEntry = entry;
        if (entry != null) world.setWorldInfo(entry.worldInfo());
        if(story!=null){story.close();story=null;}
        enteringWorld = false;
        world.surfSprites(resources.roms().get(top.totodile.pokemmo.client.rom.RomType.FIRE_RED));
        world.mapObjects(resources);
        world.motion().addStepListener(step -> {
            if(story!=null || mobile || worldScene != world || encounterActive() || !world.supportsWildEncounters()
                    || world.movement().noClip()) return;
            generateWildEncounter(step);
        });
        inventoryItems.previewAppearance(entry == null ? null : entry.character().appearance());
        displayedPokedexState = world.pokedex().pokedexState();
        pokedex.setState(displayedPokedexState);
        worldActions = new top.totodile.pokemmo.client.ui.world.WorldActions(world, mobile);
        worldActions.onAnimationTests(this::openAnimationTests);
        worldActions.onStoryInspection(this::openStoryInspection);
        worldActions.onTestMode(
                enabled -> {
                    if (encounterWindow != null) encounterWindow.testMode(enabled);
                });
        worldActions.onTeleport(() -> openWorldTool(WorldToolsWindow.Tool.TELEPORT));
        worldActions.onItem(() -> openWorldTool(WorldToolsWindow.Tool.ITEM));
        worldActions.onRandomItems(this::generateRandomItems);
        worldActions.onRandomOutfit(this::generateRandomOutfit);
        worldActions.onRandomPokemon(this::generateRandomPokemon);
        worldActions.onPortablePc(this::openPortablePc);
        if (!mobile) {
            worldActions.onRandomEncounter(this::generateRandomEncounter);
            worldActions.onNpcEncounter(() -> generateRandomEncounter(5, false, false, true));
            worldActions.onHordeEncounter(this::generateRandomEncounter);
        }
        worldActions.onPokemon(() -> openWorldTool(WorldToolsWindow.Tool.POKEMON));
        worldScene = world;
        if (!mobile) world.atmosphere().configure(resources, renderer);
        worldHud =
                new top.totodile.pokemmo.client.ui.world.WorldHud(
                        world,
                        strings,
                        mobile,
                        resources.preferences(),
                        this::itemIcon,
                        () -> {
                            world.setInputBlocked(true);
                            settings.open();
                        },
                        () -> {
                            world.setInputBlocked(true);
                            worldLogout.show(
                                    strings.get(1160),
                                    () -> {
                                        leaveWorld.run();
                                        controller.back();
                                    });
                        },
                        worldExit,
                        batteryPercent);
        worldHud.onTrade(() -> market.open(false));
        if (!mobile) {
            var counter =
                    new top.totodile.pokemmo.client.ui.pokemon.EncounterCounterWindow(
                            pokemon,
                            entry == null || controller.selection() == null
                                    ? null
                                    : new top.totodile.pokemmo.client.config.EncounterCounterStore(
                                            resources.preferences(),
                                            controller.selection().server().id(),
                                            controller.selection().session().username(),
                                            entry.character().id()));
            worldHud.encounterCounter(counter);
            counter.onOpenRecorder(
                    () -> {
                        if (encounterRecorder == null)
                            encounterRecorder =
                                    new top.totodile.pokemmo.client.ui.pokemon
                                            .EncounterRecorderWindow(
                                            root, pokemon, counter, this::restoreWorldInput);
                        encounterRecorder.open();
                        restoreWorldInput();
                    });
            worldActions.onRandomCounter(worldHud::randomizeEncounterCounter);
        }
        hotkeyBar =
                new top.totodile.pokemmo.client.ui.inventory.HotkeyBar(
                        inventoryItems, resources.preferences(), mobile);
        hotkeyBar.onUse(item -> inventory.activateItem(item, InventoryWindow.ItemAction.USE));
        inventory.onItemDrop((item, x, y) -> hotkeyBar != null && hotkeyBar.drop(item, x, y));
        worldHud.hotkeyBar(hotkeyBar);
        reloadHotkeyInventory();
        worldHud.onBag(() -> toggle(inventory));
        worldHud.onTrainer(() -> toggle(trainerCard));
        worldHud.onMail(() -> toggle(mail));
        worldHud.onGiftShop(() -> toggle(giftShop));
        pokemon.onFollower(
                value -> {
                    if (worldScene == world)
                        world.follower(resources, value, this::showNotification);
                });
        pokemon.onFollowerDrop(
                (x, y) ->
                        worldScene == world
                                && !enteringWorld
                                && (!mobile || !entryError.isOpen())
                                && (!mobile || !pokedex.isOpen() && !playerWindowsOpen())
                                && !playerWindowsOwnPointer(x, y)
                                && !gui.hasOpenPopups()
                                && !pokemon.hasMenu()
                                && (!mobile || !pokedex.showingMessage())
                                && !settings.blocksInput()
                                && (!mobile || !worldLogout.isOpen())
                                && (exitConfirmation == null || !exitConfirmation.isOpen())
                                && !worldActions.isInside(x, y)
                                && !worldHud.ownsPointer(x, y));
        try {
            inventoryItems.gameData(gameService);
        } catch (IOException failure) {
            Gdx.app.error("pokemon", "Cannot load Pokemon growth data", failure);
        }
        storage.reset();
        pokemon.bind(
                controller.selection(), playerEntry == null ? null : playerEntry.character().id());
        party = pokemon.party(false);
        worldHud.party(party);
        if (playerEntry != null) {
            inventory.region(playerEntry.region());
            inventory.money(world.worldInfo().money());
            mail.bind(controller.selection(), playerEntry.character());
        }
        worldHud.onCustomization(
                () -> {
                    if (playerEntry != null) {
                        while (worldHud.closeOverlay()) {}
                        characterCustomization.openCustomization(
                                playerEntry.character().appearance());
                        restoreWorldInput();
                    }
                });
        worldHud.onPokedex(
                () -> {
                    while (worldHud.closeOverlay()) {}
                    if (mobile) world.setInputBlocked(true);
                    pokedex.open();
                    restoreWorldInput();
                });
        if (!replacing)
            for (int i = 0; i < root.getNumChildren(); i++) {
                Widget child = root.getChild(i);
                worldHidden.put(child, child.isVisible());
                child.setVisible(false);
            }
        root.add(worldHud);
        if (playerEntry != null && controller.selection() != null)
            worldHud.chat()
                    .connect(
                            controller.selection().server().id(),
                            playerEntry.character().id().toString(),
                            playerEntry.character().name());
        root.add(worldActions);
        root.add(worldHud.chat());
        if(entry!=null&&(entry.region()>=0&&entry.region()<=4)&&controller.selection()!=null){
            story=new top.totodile.pokemmo.client.ui.world.KantoStoryController(
                    root,mobile,resources,gameService,controller.selection(),entry,world,
                    request->generateEncounter(1,false,false,true,request),()->{
                        pokemon.reload();
                        inventory.reloadIfOpen();
                        reloadHotkeyInventory();
                    },
                    failure->{Gdx.app.error("story","Cannot continue Kanto story",failure);entryError.show(strings.get(2103));});
            story.idleStep(step->{
                if((!mobile||entry.region()>=2)&&worldScene==world&&!encounterActive()&&world.supportsWildEncounters()&&!world.movement().noClip())generateWildEncounter(step);
            });
            story.notifications(this::showNotification);
            prepareBattleEnvironment(world);
            story.suspend();encounterPending=true;
            var connectingStory=story;
            gameService.reconnectBattle(controller.selection(),entry.character().id()).whenComplete((saved,error)->Gdx.app.postRunnable(()->{
                if(disposed||story!=connectingStory||playerEntry!=entry)return;
                encounterPending=false;
                if(error!=null){Gdx.app.error("battle","Cannot reconnect battle",error);entryError.show(strings.get(2103));return;}
                if(saved==null)connectingStory.start();
                else generateEncounter(1,false,false,true,java.util.concurrent.CompletableFuture.completedFuture(saved.encounter()),saved);
                restoreWorldInput();
            }));
        }
        fps.setVisible(true);
        root.invalidateLayout();
        return new com.badlogic.gdx.InputMultiplexer(input, world);
    }

    private java.util.function.Function<
                    top.totodile.pokemmo.server.game.GameService.Entry,
                    java.util.concurrent.CompletionStage<Void>>
            worldEntry;
    private final top.totodile.pokemmo.client.ui.common.MessageDialog entryError;

    public void onRomRestart(Runnable restart) {
        clientManagement.onRestart(restart);
        mods.onRestart(restart);
    }

    public void showNotification(String text) {
        loginPanel.showNotification(text);
    }

    public void onEnterWorld(
            java.util.function.Function<
                            top.totodile.pokemmo.server.game.GameService.Entry,
                            java.util.concurrent.CompletionStage<Void>>
                    action) {
        worldEntry = java.util.Objects.requireNonNull(action);
    }

    ClientUi(
            Viewport viewport,
            Function<String, FileHandle> assets,
            boolean mobile,
            SavedAccounts savedAccounts,
            LoginController controller) {
        this(
                viewport,
                assets,
                mobile,
                savedAccounts,
                () -> System.nanoTime() / 1_000_000L,
                controller);
    }

    ClientUi(
            Viewport viewport,
            Function<String, FileHandle> assets,
            boolean mobile,
            SavedAccounts savedAccounts,
            LongSupplier notificationClock,
            LoginController controller) {
        this(viewport, assets, mobile, savedAccounts, notificationClock, controller, () -> -1);
    }

    ClientUi(
            Viewport viewport,
            Function<String, FileHandle> assets,
            boolean mobile,
            SavedAccounts savedAccounts,
            LongSupplier notificationClock,
            LoginController controller,
            java.util.function.IntSupplier batteryPercent) {
        this(
                viewport,
                assets,
                mobile,
                savedAccounts,
                notificationClock,
                controller,
                batteryPercent,
                () -> Gdx.app.exit());
    }

    ClientUi(
            Viewport viewport,
            Function<String, FileHandle> assets,
            boolean mobile,
            SavedAccounts savedAccounts,
            LongSupplier notificationClock,
            LoginController controller,
            java.util.function.IntSupplier batteryPercent,
            Runnable exit) {
        this(
                viewport,
                assets,
                mobile,
                savedAccounts,
                notificationClock,
                controller,
                batteryPercent,
                exit,
                null);
    }

    public ClientUi(
            Viewport viewport,
            Function<String, FileHandle> assets,
            boolean mobile,
            SavedAccounts savedAccounts,
            LongSupplier notificationClock,
            LoginController controller,
            java.util.function.IntSupplier batteryPercent,
            Runnable exit,
            top.totodile.pokemmo.server.game.GameService gameService) {
        this.gameService = gameService;
        this.batteryPercent = batteryPercent;
        this.controller = controller;
        this.mobile = mobile;
        this.notificationClock = notificationClock;
        ownsResources = !(assets instanceof ClientResources);
        resources =
                assets instanceof ClientResources
                        ? (ClientResources) assets
                        : new ClientResources(assets, savedAccounts.directory(), mobile);
        strings = resources.strings();
        renderer = new GdxTwlRenderer(viewport, strings.typeface(), resources::apply);
        serverFrame = new ServerSelectionPanel(strings, controller);
        characterPage = new CharacterSelectionPanel(strings, controller);
        romInfo = new Label(romSummary());
        announcements = new AnnouncementsPanel(strings, mobile);
        desktopMenu = mobile ? null : new DesktopMenu(strings);
        mobileStatus = mobile ? new MobileStatusIcons(batteryPercent) : null;
        mobileMenu =
                mobile
                        ? new MobileSideMenu(
                                strings, mobileStatus, notificationClock, this::itemIcon)
                        : null;
        fps.setTheme("/fpscounter");
        logo.setTheme("logo");
        root =
                new Widget() {
                    @Override
                    protected void paintOverlay(GUI gui) {
                        if (pokemon != null) pokemon.paintDrag(getAnimationState());
                    }

                    @Override
                    protected void layout() {
                        if (worldActions != null) {
                            loginPanel.layoutNotifications();
                            worldHud.position(
                                    getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight());
                            worldActions.position(
                                    getInnerX() + (mobile ? 100 : 0),
                                    getInnerY(),
                                    getInnerWidth() - (mobile ? 100 : 0),
                                    getInnerHeight());
                            fps.adjustSize();
                            fps.setPosition(
                                    mobile ? getInnerX() : getInnerRight() - fps.getWidth(),
                                    mobile ? getInnerBottom() - fps.getHeight() : getInnerY());
                            return;
                        }
                        logo.setSize(484, 143);
                        logo.setPosition(
                                getInnerX() + (getInnerWidth() - 484) / 2,
                                getInnerY() + (getInnerHeight() - 143) / 2 - 200);
                        romInfo.adjustSize();
                        romInfo.setPosition(getInnerX() + 3, getInnerY() + 3);
                        fps.adjustSize();
                        fps.setPosition(
                                mobile ? getInnerX() : getInnerRight() - fps.getWidth(),
                                mobile ? getInnerBottom() - fps.getHeight() : getInnerY());
                        announcements.adjustSize();
                        announcements.setPosition(
                                getInnerX(),
                                getInnerBottom()
                                        - announcements.getHeight()
                                        - (mobile ? fps.getHeight() : 0));
                        if (desktopMenu != null) {
                            desktopMenu.adjustSize();
                            desktopMenu.setPosition(
                                    getInnerRight() - desktopMenu.getWidth() - 4,
                                    getInnerBottom() - desktopMenu.getHeight() - 4);
                        }
                        if (mobileStatus != null) {
                            mobileStatus.adjustSize();
                            mobileStatus.setPosition(
                                    getInnerRight() - mobileStatus.getWidth() - 5, getInnerY() + 5);
                        }
                        if (mobileMenu != null) mobileMenu.position();
                        if (loginPanel.isVisible()) {
                            loginPanel.adjustSize();
                            loginPanel.setPosition(
                                    (getInnerWidth() - loginPanel.getWidth()) / 2
                                            + getInnerX()
                                            + offsetX,
                                    (getInnerHeight() - loginPanel.getHeight()) / 2
                                            + getInnerY()
                                            - offsetY);
                        }
                        if (serverFrame.isVisible()) {
                            serverFrame.adjustSize();
                            serverFrame.setPosition(
                                    getInnerX() + (getInnerWidth() - serverFrame.getWidth()) / 2,
                                    getInnerY() + (getInnerHeight() - serverFrame.getHeight()) / 2);
                        }
                        characterPage.setPosition(getInnerX(), getInnerY());
                        characterPage.setSize(getInnerWidth(), getInnerHeight());
                        loginPanel.layoutNotifications();
                    }
                };
        loginPanel =
                new LoginPanel(root, strings, mobile, savedAccounts, notificationClock, controller);
        if (mobileMenu != null) loginPanel.onInputRequested(mobileMenu::close);
        if (mobileStatus != null)
            mobileStatus.onMenu(
                    () -> {
                        hideEditors();
                        mobileMenu.open();
                    });
        root.setTheme("logingui");
        root.add(logo);
        root.add(loginPanel);
        root.add(serverFrame);
        root.add(characterPage);
        root.add(romInfo);
        root.add(announcements);
        if (desktopMenu != null) root.add(desktopMenu);
        root.add(fps);
        if (mobileStatus != null) root.add(mobileStatus);
        if (mobileMenu != null) root.add(mobileMenu);
        gui = new GUI(root, renderer, null);
        gui.setTooltipDelay(500); // TwlGui: u10.Bb(500); child widgets retain their own GH0.
        Runnable restoreFocus = this::restoreWindowFocus;
        java.util.function.Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground =
                image -> renderer.drawScreenBackground(image, root.getAnimationState());
        entryError =
                new top.totodile.pokemmo.client.ui.common.MessageDialog(
                        root, strings, mobile, mobileBackground, this::restoreWindowFocus);
        worldLogout =
                new top.totodile.pokemmo.client.ui.common.ActionConfirmDialog(
                        root, strings, mobile, mobileBackground, this::restoreWindowFocus);
        characterPage.onEnter(this::enterWorld);
        clientManagement =
                new ClientManagementWindow(
                        root,
                        strings,
                        mobile,
                        restoreFocus,
                        mobileBackground,
                        resources.roms(),
                        resources.romFilePicker(),
                        () -> romInfo.setText(romSummary()),
                        loginPanel::showNotification);
        mods =
                new ModManagementWindow(
                        root,
                        strings,
                        mobile,
                        restoreFocus,
                        mobileBackground,
                        resources.modImporter(),
                        resources.romFilePicker(),
                        renderer,
                        resources.preferences(),
                        loginPanel::showNotification);
        settings =
                new SettingsWindow(
                        root,
                        strings,
                        mobile,
                        restoreFocus,
                        mobileBackground,
                        resources.preferences(),
                        loginPanel::showNotification);
        pokedex =
                new PokedexWindow(
                        root,
                        resources,
                        renderer,
                        mobile,
                        mobileBackground,
                        () -> {
                            restoreWorldInput();
                        });
        if (gameService != null) pokedex.setTierData(gameService.pokedexTiers());
        characterCreation =
                new CharacterCreationWindow(
                        root,
                        strings,
                        mobile,
                        () -> {
                            characterPage.setVisible(
                                    controller.state() == LoginController.State.SELECTED);
                            restoreWindowFocus();
                        },
                        mobileBackground,
                        resources.newAppearance(),
                        renderer,
                        notificationClock,
                        loginPanel::showNotification);
        characterCustomization =
                new CharacterCreationWindow(
                        root,
                        strings,
                        mobile,
                        this::restoreWorldInput,
                        mobileBackground,
                        resources.newAppearance(),
                        renderer,
                        notificationClock,
                        loginPanel::showNotification,
                        true);
        characterCustomization.onCreate(this::saveCustomization);
        inventoryItems = new InventoryItems(resources, renderer);
        inventory =
                new InventoryWindow(
                        root,
                        strings,
                        mobile,
                        inventoryItems,
                        id -> {
                            if (resources.apply("sprites/itemicons/" + id + ".png").exists())
                                return itemIcon(id);
                            return inventoryItems.icon(id);
                        },
                        completed -> {
                            try {
                                inventoryItems.load();
                            } catch (IOException failure) {
                                Gdx.app.error("inventory", "Cannot load item ROM", failure);
                                entryError.show(strings.format(1186, strings.get(92)));
                                return;
                            }
                            playerRequest(
                                    (selection, id) -> gameService.inventory(selection, id),
                                    value -> {
                                        if (hotkeyBar != null && playerEntry != null)
                                            hotkeyBar.inventory(value, playerEntry.region());
                                        completed.accept(value);
                                    });
                        },
                        this::restoreWorldInput);
        inventory.onItemAction(this::useInventoryItem);
        inventory.onDestroy(this::destroyInventoryItem);
        trainerCard =
                new TrainerCardWindow(
                        root,
                        resources,
                        renderer,
                        mobile,
                        characterCreation::savedAppearance,
                        inventoryItems::icon,
                        completed ->
                                playerRequest(
                                        (selection, id) -> gameService.trainer(selection, id),
                                        completed),
                        this::restoreWorldInput);
        mail =
                new MailWindow(
                        root,
                        resources,
                        renderer,
                        mobile,
                        gameService,
                        mobileBackground,
                        this::restoreWorldInput);
        pokemon =
                new PokemonController(
                        resources,
                        renderer,
                        mobile,
                        gameService,
                        text -> entryError.show(text),
                        id -> {
                            pokedex.showSpecies(id);
                            restoreWorldInput();
                        });
        pokemon.onNotification(this::showNotification);
        pokemon.itemIcons(inventoryItems::icon);
        pokemon.onItemUsed(()->{if(story!=null)story.resume();});
        pokemon.items(inventoryItems, () -> {
            inventory.reloadIfOpen();
            reloadHotkeyInventory();
        });
        pokemon.grayItemIcons(inventoryItems::grayIcon);
        storage = new PokemonStorageWindow(root, pokemon, mobile, this::restoreWorldInput);
        incubator=new top.totodile.pokemmo.client.ui.pokemon.IncubatorWindow(root,strings,mobile,this::restoreWorldInput);
        storage.onIncubator(incubator::open);
        if (!mobile)
            pokemon.onSummary(
                    value -> {
                        try {
                            inventoryItems.gameData(gameService);
                        } catch (IOException failure) {
                            Gdx.app.error("pokemon", "Cannot register species data", failure);
                            return;
                        }
                        showPokemonSummary(value);
                        restoreWorldInput();
                    });
        worldTools =
                new WorldToolsWindow(
                        root,
                        mobile,
                        resources,
                        inventoryItems,
                        gameService,
                        this::executeWorldTool,
                        text -> {
                            if (worldActions != null) {
                                pokemon.reload();
                                inventory.reloadIfOpen();
                                reloadHotkeyInventory();
                            }
                        },
                        text -> Gdx.app.error("world-tools", text),
                        this::restoreWorldInput);
        pokemon.onChanged(
                () -> {
                        reloadTraversalAccess();
                        pokedex.setOwnedSpecies(
                                pokemon.state().pokemons().stream()
                                        .filter(p -> !p.egg())
                                        .map(
                                                top.totodile.pokemmo.server.game.PokemonData.Pokemon
                                                        ::species)
                                        .collect(java.util.stream.Collectors.toSet()));
                });
        pcMenu =
                new PcMenuWindow(
                        root,
                        strings,
                        mobile,
                        this::openPortablePc,
                        this::openPcMail,
                        this::restoreWorldInput);
        market = new top.totodile.pokemmo.client.ui.market.GlobalTradeMarketWindow(
                root, strings, mobile, this::restoreWorldInput);
        pcMenu.onTrade(() -> market.open(true));
        giftShop=new top.totodile.pokemmo.client.ui.market.GiftShopWindow(root,strings,inventoryItems,mobile,
                ()->gameService.giftShop(controller.selection(),playerEntry.character().id()),this::restoreWorldInput);
        characterPage.onNewCharacter(
                () -> {
                    characterCreation.availableRegions(
                            resources.roms().loaded().stream()
                                    .map(rom -> rom.type().id)
                                    .sorted()
                                    .toList());
                    characterCreation.open();
                    if (characterCreation.isOpen()) characterPage.setVisible(false);
                });
        characterCreation.onCreate(this::createCharacter);
        characterDelete =
                new CharacterDeleteWindow(
                        root,
                        strings,
                        mobile,
                        restoreFocus,
                        mobileBackground,
                        this::deleteCharacter);
        characterPage.onDeleteCharacter(() -> characterDelete.open(characterPage.characters()));
        exitConfirmation =
                mobile
                        ? new ExitConfirmation(root, strings, exit, restoreFocus, mobileBackground)
                        : null;
        Runnable requestExit =
                () -> {
                    loginPanel.hideKeyboard();
                    characterCreation.hideKeyboard();
                    if (mobile) exitConfirmation.open();
                    else exit.run();
                };
        worldExit = requestExit;
        if (desktopMenu != null) desktopMenu.onExit(requestExit);
        if (mobileMenu != null) mobileMenu.onExit(requestExit);
        Runnable openManagement =
                () -> {
                    loginPanel.hideKeyboard();
                    characterCreation.hideKeyboard();
                    clientManagement.open();
                };
        if (desktopMenu != null) desktopMenu.onClientManagement(openManagement);
        if (mobileMenu != null) mobileMenu.onClientManagement(openManagement);
        Runnable openMods =
                () -> {
                    hideEditors();
                    mods.open();
                };
        if (desktopMenu != null) desktopMenu.onMods(openMods);
        if (mobileMenu != null) mobileMenu.onMods(openMods);
        Runnable openSettings =
                () -> {
                    loginPanel.hideKeyboard();
                    characterCreation.hideKeyboard();
                    settings.open();
                };
        if (desktopMenu != null) desktopMenu.onSettings(openSettings);
        if (mobileMenu != null) mobileMenu.onSettings(openSettings);
        theme = loadTheme(resources.selectedTheme());
        gui.applyTheme(theme);
        if (!mobile)
            renderer.setFullscreenCursors(
                    theme.getImage("fullscreen.cursor.arrow"),
                    theme.getImage("fullscreen.cursor.text"));
        input =
                new InputAdapter() {
                    private boolean windowPointerCapture;
                    private int pointerX, pointerY;

                    private boolean pointer(int x, int y, int button, boolean pressed) {
                        if (enteringWorld) return true;

                        Vector2 point = viewport.unproject(new Vector2(x, y));
                        int px = Math.round(point.x),
                                py = renderer.getHeight() - Math.round(point.y);
                        pointerX = px;
                        pointerY = py;
                        if (pressed
                                && button >= 0
                                && worldHud != null
                                && worldHud.editingChat()
                                && !worldHud.chat().isInside(px, py)
                                && !gui.hasOpenPopups()) {
                            worldHud.chat().blur();
                            restoreWorldInput();
                        }
                        if (pressed
                                && button >= 0
                                && !mobile
                                && !gui.hasOpenPopups()
                                && !settings.blocksInput()) raiseWindowAt(px, py);
                        boolean captured = windowPointerCapture;
                        if (pressed && button >= 0) {
                            windowPointerCapture =
                                    playerWindowsOwnPointer(px, py)
                                            || dialogOwnsPointer(px, py)
                                            || encounterWindow != null
                                                    && encounterWindow.isOpen()
                                                    && encounterWindow.isInside(px, py)
                                            || worldHud != null
                                                    && worldHud.chat().isVisible()
                                                    && worldHud.chat().isInside(px, py)
                                            || gui.hasOpenPopups()
                                            || hotkeyBar != null && hotkeyBar.isInside(px, py);
                            captured = windowPointerCapture;
                            if (worldScene != null
                                    && !mobile
                                    && !windowPointerCapture
                                    && !worldHud.ownsPointer(px, py)) releaseWindowFocus();
                        }
                        if (!pressed && button >= 0) windowPointerCapture = false;
                        if (!pressed && button < 0) {
                            pokemon.hoverMenus(px, py);
                            pokemonSummaries.forEach(w -> w.hoverMenus(px, py));
                        }
                        if (worldScene != null
                                && pressed
                                && button == 0
                                && (!mobile || !entryError.isOpen())
                                && !settings.blocksInput()
                                && (!mobile
                                        || !pokedex.isOpen()
                                                && !mail.isOpen()
                                                && !inventory.isOpen()
                                                && !trainerCard.isOpen())
                                && (!mobile || !characterCustomization.isOpen())
                                && !dialogOwnsPointer(px, py)
                                && !(encounterWindow != null
                                        && encounterWindow.isOpen()
                                        && encounterWindow.isInside(px, py))
                                && (mobile
                                        || (!playerWindowsOwnPointer(px, py)
                                                        || storage.isOpen()
                                                                && storage.ownsPointer(px, py)
                                                                && topPlayerWindow() == storage)
                                                && !gui.hasOpenPopups()))
                            pokemon.touchDown(
                                    Math.round(point.x),
                                    renderer.getHeight() - Math.round(point.y),
                                    button);
                        if (worldScene != null
                                && !mobile
                                && pressed
                                && button == 0
                                && topPlayerWindow() == storage
                                && !gui.hasOpenPopups()
                                && !settings.blocksInput()) storage.selectionDown(px, py);
                        if (pressed && button == 0 && pokedex.isOpen())
                            pokedex.touchDown(
                                    Math.round(point.x),
                                    renderer.getHeight() - Math.round(point.y));
                        if (worldActions != null
                                && !captured
                                && (!mobile || !pokedex.isOpen() && !playerWindowsOpen())
                                && !playerWindowsOwnPointer(px, py)
                                && !gui.hasOpenPopups()
                                && !pokemon.hasMenu()
                                && (!mobile || !pokedex.showingMessage())
                                && !settings.blocksInput()
                                && (!mobile || !worldLogout.isOpen())
                                && (exitConfirmation == null || !exitConfirmation.isOpen())
                                && !worldActions.isInside(
                                        Math.round(point.x),
                                        renderer.getHeight() - Math.round(point.y))
                                && !(encounterWindow != null
                                        && encounterWindow.isOpen()
                                        && encounterWindow.isInside(px, py))
                                && !dialogOwnsPointer(px, py)
                                && !worldHud.ownsPointer(
                                        Math.round(point.x),
                                        renderer.getHeight() - Math.round(point.y))) {
                            gui.handleMouse(-1, -1, button, false);
                            return false;
                        }
                        boolean handled =
                                gui.handleMouse(
                                        Math.round(point.x),
                                        renderer.getHeight() - Math.round(point.y),
                                        button,
                                        pressed);
                        if (pressed) restoreWorldInput();
                        return handled;
                    }

                    @Override
                    public boolean touchDown(int x, int y, int pointer, int button) {
                        if (pointer != 0) return false;
                        if (button == 0 && mods.isOpen()) {
                            Vector2 p = viewport.unproject(new Vector2(x, y));
                            mods.pointerDown(
                                    Math.round(p.x), renderer.getHeight() - Math.round(p.y));
                        }
                        if (button == 0) {
                            Vector2 point = viewport.unproject(new Vector2(x, y));
                            int px = Math.round(point.x),
                                    py = renderer.getHeight() - Math.round(point.y);
                            Widget target = gui.getWidgetAt(px, py);
                            while (target != null
                                    && !(target
                                            instanceof
                                            top.totodile.pokemmo.client.ui.common
                                                    .TouchScrollPane)) {
                                if (target instanceof Scrollbar) {
                                    target = null;
                                    break;
                                }
                                target = target.getParent();
                            }
                            playerTouchScroll =
                                    (top.totodile.pokemmo.client.ui.common.TouchScrollPane) target;
                            if (playerTouchScroll != null)
                                playerTouchScroll.beginTouch(px, py, mobile);
                        }
                        if (worldHud != null
                                && (!mobile || !pokedex.isOpen() && !playerWindowsOpen())
                                && (!mobile || !pokedex.showingMessage())
                                && !settings.blocksInput()
                                && (!mobile || !worldLogout.isOpen())) {
                            Vector2 point = viewport.unproject(new Vector2(x, y));
                            if (worldHud.dismissMenuOutside(
                                    Math.round(point.x),
                                    renderer.getHeight() - Math.round(point.y))) return true;
                        }
                        if (worldActions != null && !settings.blocksInput())
                            return pointer(x, y, button, true);
                        Vector2 point = viewport.unproject(new Vector2(x, y));
                        if ((exitConfirmation == null || !exitConfirmation.isOpen())
                                && !characterCreation.isAbove(settings))
                            settings.touchDown(
                                    Math.round(point.x),
                                    renderer.getHeight() - Math.round(point.y),
                                    x,
                                    y,
                                    button);
                        return pointer(x, y, button, true);
                    }

                    @Override
                    public boolean touchUp(int x, int y, int pointer, int button) {
                        if (pointer != 0) return false;
                        if (button == 0 && mods.pointerUp()) return true;
                        if (playerTouchScroll != null) {
                            Vector2 p = viewport.unproject(new Vector2(x, y));
                            boolean pan =
                                    playerTouchScroll.endTouch(
                                            Math.round(p.x),
                                            renderer.getHeight() - Math.round(p.y));
                            playerTouchScroll = null;
                            if (pan) return true;
                        }
                        if (button == 0 && !mobile && storage.selectionUp()) {
                            windowPointerCapture = false;
                            gui.handleMouse(-1, -1, button, false);
                            return true;
                        }
                        if (button == 0 && worldScene != null) {
                            Vector2 p = viewport.unproject(new Vector2(x, y));
                            if (pokemon.touchUp(
                                    Math.round(p.x), renderer.getHeight() - Math.round(p.y))) {
                                gui.handleMouse(-1, -1, button, false);
                                return true;
                            }
                        }
                        if (pokedex.isOpen()) {
                            Vector2 p = viewport.unproject(new Vector2(x, y));
                            if (pokedex.touchUp(
                                    Math.round(p.x), renderer.getHeight() - Math.round(p.y)))
                                return true;
                        }
                        if (worldActions != null && !settings.blocksInput())
                            return pointer(x, y, button, false);
                        if (settings.touchUp(x, y, button)) return true;
                        return pointer(x, y, button, false);
                    }

                    @Override
                    public boolean touchDragged(int x, int y, int pointer) {
                        if (pointer != 0) return false;
                        if (mods.isOpen()) {
                            Vector2 p = viewport.unproject(new Vector2(x, y));
                            if (mods.pointerDrag(
                                    Math.round(p.x), renderer.getHeight() - Math.round(p.y)))
                                return true;
                        }
                        if (!mobile && storage.isOpen()) {
                            Vector2 p = viewport.unproject(new Vector2(x, y));
                            if (storage.selectionDragged(
                                    Math.round(p.x), renderer.getHeight() - Math.round(p.y)))
                                return true;
                        }
                        if (playerTouchScroll != null && !pokemon.hasDragCandidate()) {
                            Vector2 p = viewport.unproject(new Vector2(x, y));
                            if (playerTouchScroll.drag(
                                    Math.round(p.x), renderer.getHeight() - Math.round(p.y)))
                                return true;
                        }
                        if (worldScene != null) {
                            Vector2 p = viewport.unproject(new Vector2(x, y));
                            if (pokemon.touchDragged(
                                    Math.round(p.x), renderer.getHeight() - Math.round(p.y)))
                                return true;
                        }
                        if (pokedex.isOpen()) {
                            Vector2 p = viewport.unproject(new Vector2(x, y));
                            if (pokedex.touchDragged(
                                    Math.round(p.x), renderer.getHeight() - Math.round(p.y)))
                                return true;
                        }
                        if (worldActions != null && !settings.blocksInput())
                            return pointer(x, y, -1, false);
                        if (settings.touchDragged(x, y)) return true;
                        return pointer(x, y, -1, false);
                    }

                    @Override
                    public boolean touchCancelled(int x, int y, int pointer, int button) {
                        if (pointer == 0) mods.pointerUp();
                        if (pointer == 0 && playerTouchScroll != null) {
                            playerTouchScroll.cancelTouch();
                            playerTouchScroll = null;
                            gui.handleMouse(-1, -1, button, false);
                        }
                        if (pointer == 0) {
                            pokemon.cancelDrag();
                            storage.selectionUp();
                        }
                        if (pointer == 0 && pokedex.isOpen()) {
                            pokedex.cancelTouch();
                            return gui.handleMouse(-1, -1, button, false);
                        }
                        if (pointer != 0 || !settings.blocksInput()) return false;
                        settings.cancelTouch();
                        return gui.handleMouse(-1, -1, button, false);
                    }

                    @Override
                    public boolean mouseMoved(int x, int y) {
                        return pointer(x, y, -1, false);
                    }

                    @Override
                    public boolean keyDown(int key) {
                        if(worldScene!=null&&story!=null&&story.busy()&&!enteringWorld
                                &&!encounterActive()&&!focusedTextInput())worldScene.rememberStoryDirection(key);
                        if (enteringWorld) return true;
                        if(pokemon!=null&&pokemon.machineKey(key))return true;
                        if(story!=null&&(story.cinematicKey(key)||story.learningKey(keyCode(key))))return true;
                        if (encounterWindow != null && encounterWindow.specifiedMovePickerOpen()) {
                            int code = key == Input.Keys.BACK ? Event.KEY_ESCAPE : keyCode(key);
                            if (code != 0) gui.handleKey(code, '\0', true);
                            return true;
                        }
                        if (!mobile && worldActions != null && !gui.hasOpenPopups()) {
                            if (settings.isOpen()
                                    && settings.hasKeyboardFocus()
                                    && settings.captureKey(key)) return true;
                            if (handlePlayerShortcut(key)) return true;
                            var rootPane = gui.getRootPane();
                            for (int i = 0; i < rootPane.getNumChildren(); i++) {
                                var child = rootPane.getChild(i);
                                if (child
                                                instanceof
                                                top.totodile.pokemmo.client.ui.common.ClientDialog
                                        && child.hasKeyboardFocus()
                                        && (focusedTextInput()
                                                || key == Input.Keys.ESCAPE
                                                || key == Input.Keys.ENTER)) {
                                    int code = keyCode(key);
                                    return code != 0 && gui.handleKey(code, '\0', true);
                                }
                            }
                        }
                        if (mobile && settings.showingMessage()) {
                            int code = keyCode(key);
                            if (code != 0) gui.handleKey(code, '\0', true);
                            return true;
                        }
                        if (worldActions != null) {
                            if (characterCustomization.isOpen()
                                    && (mobile || characterCustomization.hasKeyboardFocus())) {
                                if (key == Input.Keys.ESCAPE || key == Input.Keys.BACK) {
                                    characterCustomization.back();
                                    return true;
                                }
                                int code = keyCode(key);
                                return code != 0 && gui.handleKey(code, '\0', true);
                            }
                            if (entryError.isOpen() && (mobile || entryError.hasKeyboardFocus())) {
                                if (key == Input.Keys.ESCAPE
                                        || key == Input.Keys.BACK
                                        || key == Input.Keys.ENTER) entryError.closePopup();
                                return true;
                            }
                            if (worldLogout.isOpen()
                                    && (mobile || worldLogout.hasKeyboardFocus())) {
                                int code = keyCode(key);
                                if (code != 0) gui.handleKey(code, '\0', true);
                                return true;
                            }
                            if (exitConfirmation != null && exitConfirmation.isOpen()) {
                                if (key == Input.Keys.ESCAPE || key == Input.Keys.BACK)
                                    exitConfirmation.closePopup();
                                return true;
                            }
                            if (settings.blocksInput()
                                    || settings.isOpen() && settings.hasKeyboardFocus()) {
                                if (settings.captureKey(key)) return true;
                                if (key == Input.Keys.ESCAPE || key == Input.Keys.BACK) {
                                    settings.back();
                                    return true;
                                }
                                int code = keyCode(key);
                                return code != 0 && gui.handleKey(code, '\0', true);
                            }
                            if ((key == Input.Keys.ESCAPE || key == Input.Keys.BACK)
                                    && pokemon.closeMenu()) return true;
                            if (pokemon.hasMenu()) {
                                int code = keyCode(key);
                                if (code != 0) gui.handleKey(code, '\0', true);
                                return true;
                            }
                            if (mobile && inventory.hasDialog()) {
                                int code = keyCode(key);
                                if (code != 0) gui.handleKey(code, '\0', true);
                                return true;
                            }
                            if (inventory.hasMenu()) {
                                if (key == Input.Keys.ESCAPE || key == Input.Keys.BACK)
                                    inventory.closeMenu();
                                else {
                                    int code = keyCode(key);
                                    if (code != 0) gui.handleKey(code, '\0', true);
                                }
                                return true;
                            }
                            if (worldHud.editingChat()) {
                                if (key == Input.Keys.UP || key == Input.Keys.DOWN)
                                    return worldHud.chat().history(key == Input.Keys.UP);
                                if (key == Input.Keys.ESCAPE || key == Input.Keys.BACK) {
                                    worldHud.closeOverlay();
                                    restoreWorldInput();
                                    return true;
                                }
                                if (key == Input.Keys.ENTER) {
                                    worldHud.submitChat();
                                    restoreWorldInput();
                                    return true;
                                }
                                int code = keyCode(key);
                                if (code != 0) gui.handleKey(code, '\0', true);
                                return true;
                            }
                            if (!worldInputBlocked() && hotkeyBar != null) {
                                if (playerKeys.contains(key)) return true;
                                if (hotkeyBar.keyDown(key)) {
                                    playerKeys.add(key);
                                    return true;
                                }
                            }
                            if (handlePlayerShortcut(key)) return true;
                            if (!mobile && !focusedTextInput() && !gui.hasOpenPopups()) {
                                if (key == Input.Keys.ENTER) {
                                    worldHud.openChat();
                                    return true;
                                }
                                int action =
                                        Integer.parseInt(
                                                resources
                                                        .preferences()
                                                        .get(
                                                                "client.controls.gdx.key_a",
                                                                Integer.toString(Input.Keys.J)));
                                if(key==action&&story!=null&&story.interact())return true;
                                if (key == action
                                        && playerEntry != null
                                        && PlayerData.pcTarget(
                                                worldScene.movement().position(),
                                                playerEntry.region())) {
                                    pcMenu.playerName(strings, playerEntry.character().name());
                                    pcMenu.open();
                                    restoreWorldInput();
                                    return true;
                                }
                            }
                            if (!mobile
                                    && encounterWindow != null
                                    && encounterWindow.isOpen()
                                    && encounterWindow.hasKeyboardFocus()) {
                                int code = keyCode(key);
                                if (code != 0 && gui.handleKey(code, '\0', true)) return true;
                            }
                            var top = topPlayerWindow();
                            if (!mobile && top != null && !top.hasKeyboardFocus()) top = null;
                            if (top != null) {
                                if (key == Input.Keys.ESCAPE || key == Input.Keys.BACK) {
                                    if (top == mail) mail.back();
                                    else if (top == worldTools) worldTools.back();
                                    else if (top == encounterRecorder) encounterRecorder.back();
                                    else top.closePopup();
                                    return true;
                                }
                                int code = keyCode(key);
                                return code != 0 && gui.handleKey(code, '\0', true);
                            }
                            if (mobile && pokedex.showingMessage()) {
                                if (key == Input.Keys.ESCAPE
                                        || key == Input.Keys.BACK
                                        || key == Input.Keys.ENTER) pokedex.back();
                                return true;
                            }
                            if (pokedex.isOpen() && (mobile || pokedex.hasKeyboardFocus())) {
                                if (key == Input.Keys.ESCAPE || key == Input.Keys.BACK) {
                                    pokedex.back();
                                    return true;
                                }
                                int code = keyCode(key);
                                return code != 0 && gui.handleKey(code, '\0', true);
                            }
                            if (key == Input.Keys.ESCAPE || key == Input.Keys.BACK) {
                                if (!worldHud.closeOverlay() && key == Input.Keys.BACK)
                                    leaveWorld.run();
                                return true;
                            }
                            if (key == Input.Keys.ENTER) {
                                worldHud.openChat();
                                return true;
                            }
                            if (key
                                    == Integer.parseInt(
                                            resources
                                                    .preferences()
                                                    .get(
                                                            "client.controls.gdx.key_a",
                                                            Integer.toString(Input.Keys.J)))) {
                                if(story!=null&&story.interact())return true;
                                if (playerEntry != null
                                        && PlayerData.pcTarget(
                                                worldScene.movement().position(),
                                                playerEntry.region())) {
                                    pcMenu.playerName(strings, playerEntry.character().name());
                                    pcMenu.open();
                                    restoreWorldInput();
                                    return true;
                                }
                            }
                            return false;
                        }
                        if (enteringWorld) return true;
                        if(pokemon!=null&&pokemon.machineKey(key))return true;
                        if(story!=null&&(story.cinematicKey(key)||story.learningKey(keyCode(key))))return true;
                        if (entryError.isOpen() && (mobile || entryError.hasKeyboardFocus())) {
                            if (key == Input.Keys.ESCAPE
                                    || key == Input.Keys.BACK
                                    || key == Input.Keys.ENTER) entryError.closePopup();
                            return true;
                        }
                        if (mobile && (mods.isImporting() || clientManagement.isImporting()))
                            return true;
                        if (settings.captureKey(key)) return true;
                        if (key == Input.Keys.ESCAPE || key == Input.Keys.BACK) {
                            if (clientManagement.closeMessage()) return true;
                            if (mods.closeMessage()) return true;
                            if (exitConfirmation != null && exitConfirmation.isOpen())
                                exitConfirmation.closePopup();
                            else if (characterDelete.isOpen()) characterDelete.closePopup();
                            else if (settings.isAbove(clientManagement)
                                    && settings.isAbove(characterCreation)
                                    && settings.isAbove(mods)) {
                                settings.back();
                            } else if (mods.isAbove(clientManagement)
                                    && mods.isAbove(characterCreation)) mods.closePopup();
                            else if (clientManagement.isAbove(characterCreation))
                                clientManagement.closePopup();
                            else if (mobileMenu != null && mobileMenu.isVisible())
                                mobileMenu.close();
                            else if (characterCreation.isOpen()) characterCreation.back();
                            else if (loginPanel.hasNativeEditor()) loginPanel.hideKeyboard();
                            else if (controller.state() != LoginController.State.LOGIN)
                                controller.back();
                            else if (!gui.handleKey(Event.KEY_ESCAPE, '\0', true))
                                loginPanel.hideKeyboard();
                            return true;
                        }
                        int code = keyCode(key);
                        return code != 0 && gui.handleKey(code, '\0', true);
                    }

                    @Override
                    public boolean keyUp(int key) {
                        if (worldScene != null) worldScene.keyUp(key);
                        if (playerKeys.remove(key)) {
                            if (repeatingPlayerKey == key) repeatingPlayerKey = 0;
                            suppressedPlayerCharacter = 0;
                            return true;
                        }
                        int code = keyCode(key);
                        return code != 0 && gui.handleKey(code, '\0', false);
                    }

                    @Override
                    public boolean keyTyped(char value) {
                        for (int held : playerKeys)
                            if (held >= Input.Keys.A
                                    && held <= Input.Keys.Z
                                    && java.lang.Character.toLowerCase(value)
                                            == 'a' + held - Input.Keys.A) return true;
                        if (suppressedPlayerCharacter != 0) {
                            char suppressed = suppressedPlayerCharacter;
                            suppressedPlayerCharacter = 0;
                            if (java.lang.Character.toLowerCase(value) == suppressed) return true;
                        }
                        if (worldActions != null
                                && !worldHud.editingChat()
                                && (!mobile || !pokedex.isOpen() && !playerWindowsOpen())
                                && !focusedTextInput()
                                && !gui.hasOpenPopups()
                                && !settings.blocksInput()) return false;
                        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) return false;
                        if (value < 32 || value == 127) return false;
                        boolean handled = gui.handleKey(Event.KEY_NONE, value, true);
                        // 字符输入是一次完整事件，必须释放以免 TWL 持续自动重复末尾字符。
                        gui.handleKey(Event.KEY_NONE, value, false);
                        return handled;
                    }

                    @Override
                    public boolean scrolled(float x, float y) {
                        return gui.handleMouseWheel(-(int) y);
                    }
                };
        Gdx.input.setInputProcessor(input);
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        controller.listen(this::refreshLoginState);
        loginPanel.focusDefault();
    }

    private PlayerWindow[] playerWindows() {
        var windows =
                new java.util.ArrayList<PlayerWindow>(
                        java.util.List.of(
                                inventory, trainerCard, mail, pcMenu, storage, worldTools, market, giftShop, incubator));
        if (storyInspection != null) windows.add(storyInspection);
        if (storyDebug != null) windows.add(storyDebug);
        if (encounterRecorder != null) windows.add(encounterRecorder);
        if (animationTests != null) windows.add(animationTests);
        windows.addAll(pokemonSummaries);
        return windows.toArray(PlayerWindow[]::new);
    }

    private top.totodile.pokemmo.client.ui.common.ClientWindow[] pointerWindows() {
        var windows =
                new java.util.ArrayList<top.totodile.pokemmo.client.ui.common.ClientWindow>(
                        java.util.Arrays.asList(playerWindows()));
        windows.add(pokedex);
        windows.add(characterCustomization);
        windows.add(settings);
        windows.add(mods);
        windows.add(clientManagement);
        return windows.toArray(top.totodile.pokemmo.client.ui.common.ClientWindow[]::new);
    }

    private void closePokemonSummaries() {
        pokemonSummaries.forEach(PokemonSummaryWindow::closePopup);
    }

    private void raiseWindowAt(int x, int y) {
        for (int i = root.getNumChildren() - 1; i >= 0; i--) {
            var child = root.getChild(i);
            if (worldHud != null
                    && child == worldHud.chat()
                    && child.isVisible()
                    && child.isInside(x, y)) {
                if (i != root.getNumChildren() - 1) {
                    root.removeChild(child);
                    root.add(child);
                }
                return;
            }

            if (child.isVisible()
                    && child instanceof top.totodile.pokemmo.client.ui.common.ClientDialog dialog
                    && child.isInside(x, y)) {
                dialog.raiseWindow();
                return;
            }
            if (child.isVisible()
                    && child instanceof top.totodile.pokemmo.client.battle.EncounterWindow battle
                    && child.isInside(x, y)) {
                battle.raiseWindow();
                return;
            }
            if (child instanceof top.totodile.pokemmo.client.ui.common.ClientWindow window
                    && window.isVisible()
                    && window.ownsPointer(x, y)) {
                window.raiseWindow();
                return;
            }
        }
    }

    private void showPokemonSummary(top.totodile.pokemmo.server.game.PokemonData.Pokemon value) {
        for (var window : pokemonSummaries)
            if (window.shows(value.id())) {
                window.raiseWindow();
                return;
            }
        var previous =
                pokemonSummaries.stream()
                        .filter(PokemonSummaryWindow::isOpen)
                        .reduce((a, b) -> a.isAbove(b) ? a : b)
                        .orElse(null);
        var window = pokemonSummaries.stream().filter(w -> !w.isOpen()).findFirst().orElse(null);
        if (window == null) {
            window =
                    new PokemonSummaryWindow(
                            root, pokemon, inventoryItems::icon, this::restoreWorldInput);
            pokemonSummaries.add(window);
        }
        window.show(value, playerEntry == null ? null : playerEntry.character().name());
        if (previous != null) window.cascadeFrom(previous);
    }

    private boolean playerWindowsOpen() {
        return inventory != null
                && (characterCustomization.isOpen()
                        || inventory.isOpen()
                        || trainerCard.isOpen()
                        || mail.isOpen()
                        || pcMenu.isOpen()
                        || market.isOpen()
                        || giftShop.isOpen()
                        || incubator.isOpen()
                        || storage.isOpen()
                        || worldTools.isOpen()
                        || storyInspection!=null&&storyInspection.isOpen()
                        || storyDebug!=null&&storyDebug.isOpen()
                        || encounterRecorder != null && encounterRecorder.isOpen()
                        || pokemonSummaries.stream().anyMatch(PokemonSummaryWindow::isOpen));
    }

    private PlayerWindow topPlayerWindow() {
        PlayerWindow top = null;
        for (var window : playerWindows())
            if (window.isOpen() && window.isAbove(pokedex) && (top == null || window.isAbove(top)))
                top = window;
        return top;
    }

    private boolean dialogOwnsPointer(int x, int y) {
        var root = gui.getRootPane();
        for (int i = 0; i < root.getNumChildren(); i++) {
            var child = root.getChild(i);
            if (child instanceof top.totodile.pokemmo.client.ui.common.ClientDialog
                    && child.isVisible()
                    && child.isInside(x, y)) return true;
        }
        return false;
    }

    private boolean playerWindowsOwnPointer(int x, int y) {
        for (var window : pointerWindows())
            if (window != null && window.isOpen() && window.ownsPointer(x, y)) return true;
        return false;
    }

    private void releaseWindowFocus() {
        for (var window : pointerWindows()) if (window != null) window.giveupKeyboardFocus();
        if (encounterWindow != null) encounterWindow.giveupKeyboardFocus();
        var rootPane = gui.getRootPane();
        for (int i = 0; i < rootPane.getNumChildren(); i++)
            if (rootPane.getChild(i)
                    instanceof top.totodile.pokemmo.client.ui.common.ClientDialog dialog)
                dialog.giveupKeyboardFocus();
        restoreWorldInput();
    }

    private boolean focusedTextInput() {
        return focusedTextInput(gui);
    }

    private boolean focusedTextInput(Widget parent) {
        if (parent instanceof EditField) return true;
        for (int i = 0; i < parent.getNumChildren(); i++) {
            var child = parent.getChild(i);
            if (child.isVisible() && child.hasKeyboardFocus() && focusedTextInput(child))
                return true;
        }
        return false;
    }

    private boolean worldInputBlocked() {
        return enteringWorld
                || encounterActive()
                || storyInspection!=null&&storyInspection.isOpen()
                || storyDebug!=null&&storyDebug.isOpen()
                || story!=null&&story.busy()
                || mobile && (playerWindowsOpen() || pokedex.isOpen())
                || mobile && characterCustomization.isOpen()
                || gui.hasOpenPopups()
                || focusedTextInput()
                || pokemon.hasMenu()
                || mobile && entryError.isOpen()
                || mobile && pokedex.showingMessage()
                || worldHud.menuOpen()
                || worldHud.blockingChat()
                || settings.blocksInput()
                || mobile && worldLogout.isOpen()
                || exitConfirmation != null && exitConfirmation.isOpen();
    }

    private boolean resumeStoryDirections;
    private void restoreWorldInput() {
        if(worldScene==null){resumeStoryDirections=false;return;}
        boolean blocked=worldInputBlocked();
        if(story!=null&&story.busy()&&!encounterActive())resumeStoryDirections=true;
        if(encounterActive()||enteringWorld)resumeStoryDirections=false;
        worldScene.setInputBlocked(blocked,resumeStoryDirections);
        if(!blocked)resumeStoryDirections=false;
    }

    private void toggle(PlayerWindow window) {
        if (window.isOpen()) window.closePopup();
        else {
            while (worldHud.closeOverlay()) {}
            window.open();
        }
        restoreWorldInput();
    }

    private boolean handlePlayerShortcut(int key) {
        return handlePlayerShortcut(key, false);
    }

    private boolean handlePlayerShortcut(int key, boolean repeated) {
        if (mobile && entryError.isOpen()
                || mobile && pokedex.showingMessage()
                || worldHud.blockingChat()
                || focusedTextInput()
                || mobile && mail.isOpen() && mail.hasDialog()
                || mobile && worldTools.isOpen()) return false;
        int bag =
                Integer.parseInt(
                        resources
                                .preferences()
                                .get("client.controls.gdx.bag", Integer.toString(Input.Keys.B)));
        int trainer =
                Integer.parseInt(
                        resources
                                .preferences()
                                .get(
                                        "client.controls.gdx.trainer",
                                        Integer.toString(Input.Keys.C)));
        int dex =
                Integer.parseInt(
                        resources
                                .preferences()
                                .get("client.controls.gdx.codex", Integer.toString(Input.Keys.N)));
        int menu =
                Integer.parseInt(resources.preferences().get("client.controls.gdx.gamemenu", "0"));
        int trade = Integer.parseInt(resources.preferences().get("client.controls.gdx.tradelink", "0"));
        if (key <= 0 || key != bag && key != trainer && key != dex && key != menu && key != trade) return false;
        if (repeated || playerKeys.add(key)) {
            if (!repeated) {
                repeatingPlayerKey = key;
                playerRepeatAt = gui.getCurrentTime() + 250;
            }
            suppressedPlayerCharacter =
                    key >= Input.Keys.A && key <= Input.Keys.Z
                            ? (char) ('a' + key - Input.Keys.A)
                            : 0;
            if (key == bag || key == trainer) toggle(key == bag ? inventory : trainerCard);
            else if (key == dex) {
                if (pokedex.isOpen()) pokedex.closePopup();
                else {
                    while (worldHud.closeOverlay()) {}
                    pokedex.open();
                }
                restoreWorldInput();
            } else if (key == trade) {
                if (market.isOpen()) market.closePopup();
                else market.open(false);
                restoreWorldInput();
            } else {
                worldHud.toggleMenu();
                restoreWorldInput();
            }
        }
        return true;
    }

    private void repeatPlayerShortcut() {
        if (repeatingPlayerKey == 0 || gui.getCurrentTime() < playerRepeatAt) return;
        // TWL GUI key repeat: 250 ms initial delay, then 30 repeats per second.
        playerRepeatAt = gui.getCurrentTime() + 1000 / 30;
        if (!enteringWorld
                && !settings.blocksInput()
                && (!mobile || !worldLogout.isOpen())
                && !pokemon.hasMenu()
                && !inventory.hasMenu()
                && (exitConfirmation == null || !exitConfirmation.isOpen()))
            handlePlayerShortcut(repeatingPlayerKey, true);
    }

    private <T> void playerRequest(
            java.util.function.BiFunction<
                            top.totodile.pokemmo.server.login.LoginService.Selection,
                            String,
                            java.util.concurrent.CompletionStage<T>>
                    operation,
            java.util.function.Consumer<T> completed) {
        var entry = playerEntry;
        var selection = controller.selection();
        if (entry == null || selection == null || gameService == null) return;
        operation
                .apply(selection, entry.character().id())
                .whenComplete(
                        (result, error) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            if (disposed
                                                    || playerEntry != entry
                                                    || controller.selection() != selection) return;
                                            if (error != null) {
                                                Gdx.app.error(
                                                        "player", "Cannot load player data", error);
                                                entryError.show(strings.get(2103));
                                            } else completed.accept(result);
                                        }));
    }

    private boolean destroyingItem;

    private void destroyInventoryItem(PlayerData.Item item, int amount) {
        if (destroyingItem
                || playerEntry == null
                || gameService == null
                || controller.selection() == null) return;
        try {
            inventoryItems.gameData(gameService);
        } catch (IOException error) {
            Gdx.app.error("inventory", "Cannot load item rules", error);
            return;
        }
        var entry = playerEntry;
        var selection = controller.selection();
        destroyingItem = true;
        gameService
                .destroyItem(selection, entry.character().id(), item.objectId(), amount)
                .whenComplete(
                        (value, error) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            destroyingItem = false;
                                            if (disposed
                                                    || playerEntry != entry
                                                    || controller.selection() != selection) return;
                                            if (error != null) {
                                                Gdx.app.error(
                                                        "inventory", "Cannot destroy item", error);
                                                entryError.show(strings.get(2103));
                                                return;
                                            }
                                            inventory.reloadIfOpen();
                                            if (hotkeyBar != null)
                                                hotkeyBar.inventory(value, entry.region());
                                        }));
    }

    private void useInventoryItem(PlayerData.Item item, InventoryWindow.ItemAction action) {
        if (action == InventoryWindow.ItemAction.GIVE && playerEntry != null && !enteringWorld
                && (encounterWindow == null || !encounterWindow.isOpen()) && item.amount() > 0) {
            pokemon.giveItem(inventory, item);
            return;
        }
        if (action != InventoryWindow.ItemAction.USE
                || worldScene == null
                || enteringWorld
                || item.amount() <= 0) return;
        // ItemUseService.useBike toggles transportation without consuming the item.
        // WorldState already persists this character's travel state.
        if (inventoryItems.bicycle(item.itemId())) worldScene.motion().toggleBicycle();
        else if(inventoryItems.machineMove(item.itemId())>0){
            if(encounterActive()||story!=null&&story.busy()){entryError.show(strings.get(6002));return;}
            pokemon.useMachine(root,item);restoreWorldInput();
        }
        else if(inventoryItems.medicine(item.itemId()).supported()||inventoryItems.growthItem(item.itemId()).supported()){
            if(encounterActive()||story!=null&&story.busy()){entryError.show(strings.get(6002));return;}
            pokemon.useItem(root,item);restoreWorldInput();
        }
    }

    private void loadBattleInventory(
            java.util.function.Consumer<java.util.List<PlayerData.Item>> callback) {
        if (playerEntry == null) return;
        int region = playerEntry.region();
        playerRequest(
                (selection, id) -> gameService.inventory(selection, id),
                inventory ->
                        callback.accept(
                                inventory.stream()
                                        .filter(
                                                item ->
                                                        item.region() < 0
                                                                || item.region() == region)
                                        .toList()));
    }

    private void reloadTraversalAccess() {
        if(playerEntry==null||worldScene==null)return;
        var traversalWorld=worldScene;
        playerRequest((selection,id)->gameService.traversal(selection,id),access->{
            if(traversalWorld!=null&&traversalWorld==worldScene)
                traversalWorld.motion().traversal(access.surf(),access.bicycle());
        });
    }

    private void reloadHotkeyInventory() {
        if (hotkeyBar == null || playerEntry == null) return;
        reloadTraversalAccess();
        try {
            inventoryItems.load();
        } catch (IOException error) {
            Gdx.app.error("hotbar", "Cannot load item resources", error);
            return;
        }
        var target = hotkeyBar;
        int region = playerEntry.region();
        playerRequest(
                (selection, id) -> gameService.inventory(selection, id),
                value -> {
                    if (target == hotkeyBar) target.inventory(value, region);
                });
    }

    private void openPcMail() {
        if (playerEntry == null || gameService == null) return;
        var entry = playerEntry;
        var selection = controller.selection();
        var position = worldScene.movement().position();
        if (!PlayerData.pcTarget(position, entry.region())) return;
        gameService
                .savePosition(selection, entry.character().id(), position)
                .thenCompose(ignored -> gameService.openMailbox(selection, entry.character().id()))
                .whenComplete(
                        (allowed, error) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            if (disposed
                                                    || playerEntry != entry
                                                    || worldScene == null
                                                    || !position.equals(
                                                            worldScene.movement().position())) {
                                                gameService.closeMailbox(
                                                        selection, entry.character().id());
                                                return;
                                            }
                                            if (error != null || !Boolean.TRUE.equals(allowed)) {
                                                entryError.show(strings.get(5801));
                                                return;
                                            }
                                            mail.bind(selection, entry.character());
                                            mail.mailboxAccess(true);
                                            mail.open();
                                            restoreWorldInput();
                                        }));
    }

    private void openPortablePc() {
        if (playerEntry == null || gameService == null) return;
        pokemon.closeMenu();
        pokemon.cancelDrag();
        while (worldHud.closeOverlay()) {}
        storage.open();
        restoreWorldInput();
    }

    private boolean generatingRandomPokemon;

    private record BattleLaunch(
            top.totodile.pokemmo.server.game.EncounterData encounter,
            top.totodile.pokemmo.server.game.battle.BattleState state) {}

    private void generateRandomEncounter() {
        generateRandomEncounter(1);
    }

    private void generateRandomEncounter(int count) {
        generateRandomEncounter(count, false);
    }

    private void generateRandomEncounter(int count, boolean pure) {
        generateRandomEncounter(count, pure, false);
    }

    private void generateRandomEncounter(int count, boolean pure, boolean moveTest) {
        generateRandomEncounter(count, pure, moveTest, false);
    }

    private boolean wildStepPending;
    private record WildStep(top.totodile.pokemmo.client.world.PlayerMotion.Step step,
            top.totodile.pokemmo.client.world.WorldScene world,int behavior,boolean land,boolean water,boolean bicycle) {}
    private final java.util.Deque<WildStep> wildSteps=new java.util.ArrayDeque<>();

    private void generateWildEncounter(top.totodile.pokemmo.client.world.PlayerMotion.Step step) {
        if(playerEntry==null||worldScene==null||gameService==null||controller.selection()==null)return;
        wildSteps.addLast(new WildStep(step,worldScene,worldScene.encounterBehavior(),worldScene.encounterLand(),worldScene.motion().surfing(),worldScene.motion().bicycle()));
        nextWildStep();
    }
    private void nextWildStep(){
        if(wildStepPending||wildSteps.isEmpty())return;
        var check=wildSteps.removeFirst();var step=check.step();
        if(disposed||worldScene!=check.world()||playerEntry==null||controller.selection()==null){wildSteps.clear();return;}
        try { inventoryItems.gameData(gameService); }
        catch(IOException error) { Gdx.app.error("battle","Cannot load encounter tables",error); return; }
        var world=worldScene;
        var entry=playerEntry;
        var selection=controller.selection();
        wildStepPending=true;
        gameService.wildEncounterStep(selection,entry.character().id(),entry.region(),
                step.before(),step.after(),check.behavior(),check.land(),check.water(),check.bicycle())
                .whenComplete((encounter,error) -> Gdx.app.postRunnable(() -> {
                    wildStepPending=false;
                    if(error!=null) {Gdx.app.error("battle","Cannot roll wild encounter",error);wildSteps.clear();return;}
                    if(encounter==null) {nextWildStep();return;}
                    wildSteps.clear();
                    if(disposed || worldScene!=world || playerEntry!=entry || encounterActive()) {
                        gameService.endBattle(selection,entry.character().id()); return;
                    }
                    // Predict ordinary movement while the roll is pending; only a real encounter
                    // corrects the player to its authoritative trigger tile and stops movement.
                    world.storyPosition(step.after());
                    if(story!=null)story.suspend();
                    generateEncounter(1,false,false,false,
                            gameService.savePosition(selection,entry.character().id(),step.after()).thenApply(ignored->encounter));
                }));
    }

    private void generateRandomEncounter(int count, boolean pure, boolean moveTest, boolean npc) {
        generateEncounter(count,pure,moveTest,npc,null);
    }

    private void generateEncounter(int count, boolean pure, boolean moveTest, boolean npc,
            java.util.concurrent.CompletionStage<top.totodile.pokemmo.server.game.EncounterData> natural) {
        generateEncounter(count,pure,moveTest,npc,natural,null);
    }
    private void generateEncounter(int count, boolean pure, boolean moveTest, boolean npc,
            java.util.concurrent.CompletionStage<top.totodile.pokemmo.server.game.EncounterData> natural,
            top.totodile.pokemmo.server.game.GameService.BattleResume resumed) {
        if ((mobile && natural==null)
                || encounterActive()
                || playerEntry == null
                || gameService == null
                || controller.selection() == null) return;
        try {
            inventoryItems.gameData(gameService);
        } catch (IOException error) {
            Gdx.app.error("battle", "Cannot load encounter data", error);
            entryError.show(strings.get(2103));
            return;
        }
        var entry = playerEntry;
        var selection = controller.selection();
        var battleEnvironment = worldScene.battleEnvironment();
        long version = ++encounterVersion;
        encounterPending = true;
        if((natural!=null||npc)&&story!=null)story.suspend();
        restoreWorldInput();
        pokemon.closeMenu();
        pokemon.cancelDrag();
        while (worldHud.closeOverlay()) {}
        (natural!=null ? natural : npc ? gameService.trainerEncounter(selection,entry.character().id()) : gameService
                .randomEncounter(selection, entry.character().id(), count, pure))
                .thenCompose(
                        value -> {
                            if(resumed!=null)return java.util.concurrent.CompletableFuture.completedFuture(new BattleLaunch(value,resumed.state()));
                            if (value==null) return java.util.concurrent.CompletableFuture.completedFuture(new BattleLaunch(null,null));

                            Gdx.app.postRunnable(() -> {
                                if (disposed || playerEntry != entry || encounterVersion != version) return;
                                if (encounterWindow == null) encounterWindow =
                                        new top.totodile.pokemmo.client.battle.EncounterWindow(
                                                root, resources, renderer, notificationClock,
                                                this::restoreWorldInput);
                                encounterWindow.beginMapEntry();
                            });

                            if(value.captureTutorial())return java.util.concurrent.CompletableFuture.completedFuture(new BattleLaunch(value,null));
                            if (moveTest)
                                return gameService
                                        .startMoveTestBattle(selection, entry.character().id())
                                        .thenApply(
                                                state ->
                                                        new BattleLaunch(state.apply(value), state));
                            String engine =
                                    resources
                                            .preferences()
                                            .get(
                                                    "server.battle.engine",
                                                    mobile ? "custom" : "showdown");
                            if (!engine.equalsIgnoreCase("custom")
                                    && !engine.equalsIgnoreCase("showdown"))
                                return java.util.concurrent.CompletableFuture
                                        .<BattleLaunch>failedFuture(
                                                new IllegalArgumentException(
                                                        "Unknown battle engine: " + engine));
                            if (value != null && (natural!=null || npc || engine.equalsIgnoreCase("showdown")))
                                return gameService
                                        .startBattle(
                                                selection,
                                                entry.character().id(),
                                                top.totodile.pokemmo.server.game.battle.BattleEngine
                                                        .Kind.SHOWDOWN)
                                        .thenApply(
                                                state ->
                                                        new BattleLaunch(
                                                                state.apply(value), state));
                            return java.util.concurrent.CompletableFuture.completedFuture(
                                    new BattleLaunch(value, null));
                        })
                .whenComplete(
                        (launch, error) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            if (disposed
                                                    || version != encounterVersion
                                                    || playerEntry != entry
                                                    || controller.selection() != selection) return;
                                            encounterPending = false;
                                            if (error != null && encounterWindow != null)
                                                encounterWindow.cancelMapEntry();
                                            if(error!=null&&story!=null)story.resume();
                                            try {
                                                var value =
                                                        launch == null ? null : launch.encounter();
                                                if (error != null)
                                                    throw new java.util.concurrent
                                                            .CompletionException(error);
                                                if (value != null) {
                                                    if (encounterWindow == null)
                                                        encounterWindow =
                                                                new top.totodile.pokemmo.client
                                                                        .battle.EncounterWindow(
                                                                        root,
                                                                        resources,
                                                                        renderer,
                                                                        notificationClock,
                                                                        this::restoreWorldInput);
                                                    encounterWindow.testMode(
                                                            natural==null && !npc && worldActions != null
                                                                    && worldActions.testMode());
                                                    inventoryItems.load();
                                                    encounterWindow.inventory(
                                                            inventoryItems,
                                                            this::loadBattleInventory);
                                                    encounterWindow.pokedex(
                                                            worldScene.pokedex().pokedexState());
                                                    encounterWindow.environmentControls(
                                                            (x, y) ->
                                                                    worldActions
                                                                                    .environmentButtonAt(
                                                                                            x, y)
                                                                            || worldHud.chat()
                                                                                            .isVisible()
                                                                                    && worldHud.chat()
                                                                                            .isInside(
                                                                                                    x,
                                                                                                    y));
                                                    encounterWindow.onBattleMessage(
                                                            text ->
                                                                    worldHud.chat()
                                                                            .battle(
                                                                                    text,
                                                                                    encounterWindow
                                                                                            .battleNames()));
                                                    encounterWindow.onBattleTurn(
                                                            worldHud.chat()::battleTurn);
                                                    encounterWindow.capture(
                                                            choice ->
                                                                    gameService.capturePokemon(
                                                                            selection,
                                                                            entry.character().id(),
                                                                            choice.pokemonId()
                                                                                            .isEmpty()
                                                                                    ? value.wild()
                                                                                            .id()
                                                                                    : choice
                                                                                            .pokemonId(),
                                                                            choice.itemObjectId()),
                                                            result -> {
                                                                if (playerEntry != entry
                                                                        || disposed) return;
                                                                pokemon.reload();
                                                                worldScene
                                                                        .pokedex()
                                                                        .restore(result.pokedex());
                                                                encounterWindow.pokedex(
                                                                        result.pokedex());
                                                            });
                                                    encounterWindow.atmosphere(
                                                            worldScene::battleAtmosphere);
                                                    if (value.trainer()!=null) encounterWindow.openNpc(
                                                            value,
                                                            characterCreation.savedAppearance(
                                                                    entry.character().appearance()),
                                                            entry.character().appearance(),
                                                            entry.character().name(), battleEnvironment);
                                                    else encounterWindow.open(
                                                            value,
                                                            characterCreation.savedAppearance(
                                                                    entry.character().appearance()),
                                                            entry.character().appearance(),
                                                            entry.character().name(),
                                                            battleEnvironment);
                                                    if (launch.state() != null)
                                                        encounterWindow.engine(
                                                                launch.state(),
                                                                action ->
                                                                        gameService.chooseBattle(
                                                                                selection,
                                                                                entry.character()
                                                                                        .id(),
                                                                                action).thenApply(state -> {
                                                                                    if(state.events().stream().anyMatch(e -> e.type().equals("MONEY") || e.type().equals("PAYDAY")))
                                                                                        gameService.enter(selection,entry.character().id()).whenComplete((updated,loadError) -> Gdx.app.postRunnable(() -> {
                                                                                            if(loadError==null && playerEntry==entry && worldScene!=null)worldScene.setWorldInfo(updated.worldInfo());
                                                                                        }));
                                                                                    return state;
                                                                                }),
                                                                () -> {
                                                                    gameService.endBattle(selection,entry.character().id())
                                                                        .whenComplete((ignored,failure) -> Gdx.app.postRunnable(() -> {
                                                                            if(playerEntry!=entry || disposed) return;
                                                                            pokemon.reload();
                                                                            if(story!=null)story.resume();
                                                                            gameService.prepareBattle();
                                                                            gameService.enter(selection,entry.character().id()).whenComplete((updated,loadError) ->
                                                                                Gdx.app.postRunnable(() -> {
                                                                                    if(loadError==null && playerEntry==entry && worldScene!=null)
                                                                                        worldScene.setWorldInfo(updated.worldInfo());
                                                                                }));
                                                                        }));
                                                                },
                                                                failure -> {
                                                                    Gdx.app.error(
                                                                            "battle",
                                                                            "Showdown action"
                                                                                    + " rejected",
                                                                            failure);
                                                                    entryError.show(
                                                                            strings.get(2103));
                                                                });
                                                    if(value.captureTutorial()) {
                                                        var window=encounterWindow;
                                                        gameService.inventory(selection,entry.character().id()).whenComplete((items,err)->Gdx.app.postRunnable(()->{
                                                            if(err!=null||playerEntry!=entry||!window.isOpen())return;
                                                            var ball=items.stream().filter(i->i.itemId()==5004&&i.amount()>0).findFirst();
                                                            ball.ifPresent(i->window.captureTutorial(new top.totodile.pokemmo.client.battle.EncounterWindow.Selection(top.totodile.pokemmo.client.battle.EncounterWindow.SelectionKind.ITEM,"",i.objectId(),5004,-1),()->{
                                                                gameService.endBattle(selection,entry.character().id()).whenComplete((v,e)->Gdx.app.postRunnable(()->{if(story!=null)story.resume();}));
                                                            }));
                                                        }));
                                                    }
                                                    if(resumed!=null)encounterWindow.reconnect();
                                                    encounterWindow.effectTestingSession(moveTest);
                                                    if (moveTest) {
                                                        encounterWindow.testMode(true);
                                                        showAnimationTests();
                                                    }
                                                    root.removeChild(worldHud.chat());
                                                    root.add(worldHud.chat());
                                                }
                                            } catch (IOException | RuntimeException failure) {
                                                if (encounterWindow != null)
                                                    encounterWindow.cancelMapEntry();
                                                Gdx.app.error(
                                                        "battle",
                                                        "Cannot start simulated encounter",
                                                        failure);
                                                entryError.show(strings.get(2103));
                                            }
                                            restoreWorldInput();
                                        }));
    }

    private void generateRandomPokemon() {
        if (generatingRandomPokemon
                || playerEntry == null
                || gameService == null
                || controller.selection() == null) return;
        try {
            inventoryItems.gameData(gameService);
        } catch (IOException error) {
            Gdx.app.error("pokemon", "Cannot load Pokemon templates", error);
            entryError.show(strings.get(2103));
            return;
        }
        var entry = playerEntry;
        var selection = controller.selection();
        var actions = worldActions;
        generatingRandomPokemon = true;
        actions.randomPokemonEnabled(false);
        gameService
                .randomPokemonBox(selection, entry.character().id())
                .whenComplete(
                        (value, error) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            generatingRandomPokemon = false;
                                            if (actions == worldActions)
                                                actions.randomPokemonEnabled(true);
                                            if (disposed
                                                    || playerEntry != entry
                                                    || controller.selection() != selection) return;
                                            if (error != null) {
                                                Gdx.app.error(
                                                        "pokemon",
                                                        "Cannot generate random Pokemon box",
                                                        error);
                                                entryError.show(strings.get(2103));
                                                return;
                                            }
                                            pokemon.reload();
                                        }));
    }

    private void generateRandomOutfit() {
        if (playerEntry == null || gameService == null) return;
        var entry = playerEntry;
        worldActions.randomOutfitEnabled(false);
        var appearance = characterCustomization.randomAppearance(entry.character().appearance());
        gameService
                .saveAppearance(controller.selection(), entry.character().id(), appearance)
                .whenComplete(
                        (character, error) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            if (worldActions != null)
                                                worldActions.randomOutfitEnabled(true);
                                            if (disposed || playerEntry != entry) return;
                                            if (error != null) {
                                                Gdx.app.error(
                                                        "customization",
                                                        "Cannot randomize outfit",
                                                        error);
                                                entryError.show(strings.get(2103));
                                                return;
                                            }
                                            playerEntry =
                                                    new GameService.Entry(
                                                            character,
                                                            entry.position(),
                                                            entry.travelState(),
                                                            entry.pokedexState(),
                                                            worldScene == null ? entry.worldInfo() : worldScene.worldInfo(),
                                                            entry.region());
                                            if (worldScene != null)
                                                worldScene.appearance(character.appearance());
                                            trainerCard.reloadIfOpen();
                                        }));
    }

    private void saveCustomization() {
        if (playerEntry == null || gameService == null || !characterCustomization.isOpen()) return;
        var entry = playerEntry;
        long version = characterCustomization.version();
        characterCustomization.saving(true);
        gameService
                .saveAppearance(
                        controller.selection(),
                        entry.character().id(),
                        characterCustomization.selectedAppearance())
                .whenComplete(
                        (character, error) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            characterCustomization.saving(false);
                                            if (disposed || playerEntry != entry) return;
                                            if (error != null) {
                                                Gdx.app.error(
                                                        "customization",
                                                        "Cannot save character outfit",
                                                        error);
                                                entryError.show(strings.get(2103));
                                                return;
                                            }
                                            playerEntry =
                                                    new GameService.Entry(
                                                            character,
                                                            entry.position(),
                                                            entry.travelState(),
                                                            entry.pokedexState(),
                                                            worldScene == null ? entry.worldInfo() : worldScene.worldInfo(),
                                                            entry.region());
                                            if (worldScene != null)
                                                worldScene.appearance(character.appearance());
                                            trainerCard.reloadIfOpen();
                                            if (version == characterCustomization.version())
                                                characterCustomization.closePopup();
                                            restoreWorldInput();
                                        }));
    }

    private void generateRandomItems() {
        if (generatingRandomItems || playerEntry == null || gameService == null) return;
        final java.util.List<top.totodile.pokemmo.client.ui.inventory.RandomInventoryItems.Grant>
                grants;
        try {
            inventoryItems.gameData(gameService);
            grants =
                    top.totodile.pokemmo.client.ui.inventory.RandomInventoryItems.sample(
                            inventoryItems,
                            inventory.randomItemPageHeight(),
                            new java.util.Random());
            if (grants.isEmpty())
                throw new IllegalStateException("No available item templates for random inventory");
        } catch (Exception error) {
            Gdx.app.error("inventory", "Cannot prepare random items", error);
            entryError.show(strings.get(2103));
            return;
        }
        var entry = playerEntry;
        var selection = controller.selection();
        generatingRandomItems = true;
        var replacement = new java.util.LinkedHashMap<Integer, Integer>();
        for (var grant : grants) replacement.put(grant.itemId(), grant.amount());
        gameService
                .replaceInventory(selection, entry.character().id(), replacement)
                .whenComplete(
                        (ignored, error) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            generatingRandomItems = false;
                                            if (disposed || playerEntry != entry) return;
                                            if (error != null) {
                                                Gdx.app.error(
                                                        "inventory",
                                                        "Cannot generate random items",
                                                        error);
                                                entryError.show(strings.get(2103));
                                            } else {
                                                inventory.reloadIfOpen();
                                                reloadHotkeyInventory();
                                            }
                                        }));
    }

    private void openWorldTool(WorldToolsWindow.Tool tool) {
        if (playerEntry == null || gameService == null) return;
        pokemon.closeMenu();
        while (worldHud.closeOverlay()) {}
        worldTools.show(tool, playerEntry.region(), worldScene.movement().position());
        restoreWorldInput();
    }

    private java.util.concurrent.CompletionStage<String> executeWorldTool(
            WorldToolsWindow.Tool tool, int[] values) {
        var entry = playerEntry;
        var selection = controller.selection();
        if (entry == null || selection == null || gameService == null)
            return java.util.concurrent.CompletableFuture.failedFuture(
                    new IllegalStateException("The character is not ready."));
        if (tool == WorldToolsWindow.Tool.ITEM)
            return gameService
                    .createItem(selection, entry.character().id(), values[0], values[1])
                    .thenApply(item -> "");
        if (tool == WorldToolsWindow.Tool.POKEMON)
            return gameService
                    .addPokemon(selection, entry.character().id(), values[0], values[1])
                    .thenApply(p -> "");
        if (teleportWorld == null)
            return java.util.concurrent.CompletableFuture.failedFuture(
                    new IllegalStateException("The character is not ready for movement."));
        if (values[0] < 0 || values[0] > 4)
            throw new IllegalArgumentException("Unknown region: " + values[0]);
        var old = worldScene;
        var position =
                new GameService.Position(
                        values[1],
                        values[2],
                        values[3],
                        values[4],
                        old.movement().position().direction());
        var state =
                new GameService.TravelState(
                        old.movement().noClip(), old.motion().speed(), old.motion().bicycle());
        var next =
                new GameService.Entry(
                        entry.character(),
                        position,
                        state,
                        old.pokedex().pokedexState(),
                        worldScene == null ? old.worldInfo() : worldScene.worldInfo(),
                        values[0]);
        enteringWorld = true;
        old.setInputBlocked(true);
        java.util.concurrent.CompletionStage<Void> operation;
        try {
            operation = teleportWorld.apply(next);
        } catch (RuntimeException failure) {
            enteringWorld = false;
            throw failure;
        }
        return operation
                .whenComplete(
                        (ignored, error) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            enteringWorld = false;
                                            restoreWorldInput();
                                        }))
                .thenApply(ignored -> "");
    }

    private String romSummary() {
        // 参考 ChatHistoryViewportComponent.Nq：主卡带缺失时保留 --，其余按固定顺序追加。
        var roms = resources.roms();
        RomInfo blackWhite = roms.get(RomType.BLACK_WHITE);
        StringBuilder text =
                new StringBuilder(strings.get(1170))
                        .append("\nNDS: ")
                        .append(blackWhite == null ? "--" : blackWhite.title());
        for (var type : java.util.List.of(RomType.PLATINUM, RomType.HEART_GOLD_SOUL_SILVER)) {
            RomInfo rom = roms.get(type);
            if (rom != null) text.append(", ").append(rom.title().replaceAll("\\P{Print}", ""));
        }
        RomInfo fireRed = roms.get(RomType.FIRE_RED);
        text.append("\nGBA: ")
                .append(fireRed == null ? "--" : fireRed.code() + " v1." + fireRed.revision());
        RomInfo emerald = roms.get(RomType.EMERALD);
        if (emerald != null)
            text.append(", ").append(emerald.code()).append(" v1.").append(emerald.revision());
        return text.toString();
    }

    private void hideEditors() {
        loginPanel.hideKeyboard();
        characterCreation.hideKeyboard();
    }

    private void restoreWindowFocus() {
        if (settings.isAbove(clientManagement)
                && settings.isAbove(characterCreation)
                && settings.isAbove(mods)) settings.raiseWindow();
        else if (mods.isAbove(clientManagement) && mods.isAbove(characterCreation))
            mods.raiseWindow();
        else if (clientManagement.isAbove(characterCreation)) clientManagement.raiseWindow();
        else if (characterCreation.isOpen()) characterCreation.raiseWindow();
        else if (controller.state() == LoginController.State.LOGIN) loginPanel.focusDefault();
        else if (controller.state() == LoginController.State.SELECTED) characterPage.focusDefault();
        else if (controller.state() == LoginController.State.SERVERS) serverFrame.focusDefault();
    }

    private ThemeManager loadTheme(ClientResources.Theme selected) {
        try {
            return ThemeManager.createThemeManager(
                    resources.themeUrl(selected),
                    renderer,
                    renderer.createNewCacheContext(),
                    new java.util.HashMap<String, Object>(
                            resources.imageConstants(selected, strings.language())));
        } catch (IOException | RuntimeException error) {
            if (!selected.name().equals("default") && !selected.name().equals("android")) {
                java.util.logging.Logger.getLogger(ClientUi.class.getName())
                        .log(
                                java.util.logging.Level.WARNING,
                                "Unable to load theme " + selected.name() + "; using default theme",
                                error);
                return loadTheme(resources.defaultTheme());
            }
            throw new IllegalStateException("加载登录主题失败", error);
        }
    }

    private de.matthiasmann.twl.renderer.Image itemIcon(int id) {
        return itemIcons.computeIfAbsent(
                id,
                key ->
                        new de.matthiasmann.twl.renderer.Image() {
                            public int getWidth() {
                                return 24;
                            }

                            public int getHeight() {
                                return 24;
                            }

                            public void draw(
                                    de.matthiasmann.twl.renderer.AnimationState state,
                                    int x,
                                    int y) {
                                draw(state, x, y, 24, 24);
                            }

                            public void draw(
                                    de.matthiasmann.twl.renderer.AnimationState state,
                                    int x,
                                    int y,
                                    int w,
                                    int h) {
                                if (inventoryItems == null) return;
                                var image =
                                        inventoryItems.tabIcon(
                                                key,
                                                state.getAnimationState(Button.STATE_HOVER)
                                                        || state.getAnimationState(
                                                                Widget.STATE_KEYBOARD_FOCUS));
                                if (image != null) image.draw(state, x, y, w, h);
                            }

                            public de.matthiasmann.twl.renderer.Image createTintedVersion(
                                    Color tint) {
                                var image = inventoryItems.icon(key);
                                return image == null ? this : image.createTintedVersion(tint);
                            }
                        });
    }

    private static int keyCode(int key) {
        return switch (key) {
            case Input.Keys.ESCAPE, Input.Keys.BACK -> Event.KEY_ESCAPE;
            case Input.Keys.ENTER -> Event.KEY_RETURN;
            case Input.Keys.TAB -> Event.KEY_TAB;
            case Input.Keys.DEL -> Event.KEY_BACK;
            case Input.Keys.FORWARD_DEL -> Event.KEY_DELETE;
            case Input.Keys.LEFT -> Event.KEY_LEFT;
            case Input.Keys.RIGHT -> Event.KEY_RIGHT;
            case Input.Keys.UP -> Event.KEY_UP;
            case Input.Keys.DOWN -> Event.KEY_DOWN;
            case Input.Keys.HOME -> Event.KEY_HOME;
            case Input.Keys.END -> Event.KEY_END;
            case Input.Keys.SHIFT_LEFT -> Event.KEY_LSHIFT;
            case Input.Keys.SHIFT_RIGHT -> Event.KEY_RSHIFT;
            case Input.Keys.CONTROL_LEFT -> Event.KEY_LCONTROL;
            case Input.Keys.CONTROL_RIGHT -> Event.KEY_RCONTROL;
            case Input.Keys.A -> Event.KEY_A;
            case Input.Keys.B -> Event.KEY_B;
            case Input.Keys.C -> Event.KEY_C;
            case Input.Keys.V -> Event.KEY_V;
            case Input.Keys.X -> Event.KEY_X;
            default -> 0;
        };
    }

    private void refreshLoginState() {
        loginPanel.refreshLoginState();
        boolean loginPage =
                controller.state() == LoginController.State.LOGIN
                        || controller.state() == LoginController.State.LOGGING_IN;
        boolean characters = controller.state() == LoginController.State.SELECTED;
        if (!characters && characterDelete.isOpen()) characterDelete.closePopup();
        if (characterSelection != controller.selection()) {
            characterSelection = controller.selection();
            characterPage.clearCards();
            if (characters) loadCharacters(characterSelection);
        }
        if (!characters && characterCreation.isOpen()) characterCreation.closePopup();
        logo.setVisible(!characters);
        boolean enteringCharacters = characters && !characterPage.isVisible();
        serverFrame.setHardVisible(!loginPage && !characters);
        characterPage.setVisible(characters && !characterCreation.isOpen());
        romInfo.setVisible(!characters);
        announcements.setHardVisible(!characters);
        if (desktopMenu != null) desktopMenu.setVisible(!characters);
        serverFrame.refresh();
        if (enteringCharacters) characterPage.focusDefault();
        root.invalidateLayout();
    }

    private void loadCharacters(
            top.totodile.pokemmo.server.login.LoginService.Selection selection) {
        if (gameService == null) return;
        long request = ++characterLoadVersion;
        characterPage.beginLoading();
        gameService
                .characters(selection)
                .thenCompose(
                        characters -> {
                            var parties =
                                    characters.stream()
                                            .map(
                                                    character ->
                                                            gameService
                                                                    .pokemon(
                                                                            selection,
                                                                            character.id())
                                                                    .handle(
                                                                            (party, error) -> {
                                                                                if (error != null) {
                                                                                    Gdx.app.error(
                                                                                            "game",
                                                                                            "Cannot"
                                                                                                + " load"
                                                                                                + " character"
                                                                                                + " party",
                                                                                            error);
                                                                                    return top
                                                                                            .totodile
                                                                                            .pokemmo
                                                                                            .server
                                                                                            .game
                                                                                            .PokemonData
                                                                                            .Containers
                                                                                            .empty();
                                                                                }
                                                                                return party;
                                                                            })
                                                                    .toCompletableFuture())
                                            .toList();
                            return java.util.concurrent.CompletableFuture.allOf(
                                            parties.toArray(
                                                    java.util.concurrent.CompletableFuture[]::new))
                                    .thenApply(
                                            ignored ->
                                                    java.util.stream.IntStream.range(
                                                                    0, characters.size())
                                                            .mapToObj(
                                                                    i ->
                                                                            new CharacterRoster(
                                                                                    characters.get(
                                                                                            i),
                                                                                    parties.get(i)
                                                                                            .join()))
                                                            .toList());
                        })
                .whenComplete(
                        (rosters, error) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            if (disposed
                                                    || selection != controller.selection()
                                                    || request != characterLoadVersion) return;
                                            if (error != null) {
                                                characterPage.loadFailed();
                                                Gdx.app.error(
                                                        "game",
                                                        "Cannot load local characters",
                                                        error);
                                            } else {
                                                characterPage.characters(
                                                        rosters.stream()
                                                                .map(CharacterRoster::character)
                                                                .toList(),
                                                        strings,
                                                        mobile,
                                                        renderer,
                                                        characterCreation::savedAppearance);
                                                for (var roster : rosters)
                                                    characterPage.party(
                                                            roster.character().id(),
                                                            roster.party(),
                                                            pokemon::characterPartyIcon,
                                                            pokemon::characterPartyName);
                                            }
                                            if (!characterCreation.isOpen()) {
                                                // 不把上一个页面遗留的鼠标/触点位置解释为新按钮的悬停。
                                                gui.handleMouse(-1, -1, -1, false);
                                                restoreWindowFocus();
                                            }
                                        }));
    }

    private record CharacterRoster(
            top.totodile.pokemmo.server.game.GameService.Character character,
            top.totodile.pokemmo.server.game.PokemonData.Containers party) {}

    private void enterWorld(top.totodile.pokemmo.server.game.GameService.Character character) {
        var selection = controller.selection();
        if (disposed
                || enteringWorld
                || worldEntry == null
                || gameService == null
                || selection == null) return;
        enteringWorld = true;
        characterPage.beginLoading();
        java.util.concurrent.CompletionStage<top.totodile.pokemmo.server.game.GameService.Entry>
                operation;
        try {
            operation = gameService.enter(selection, character.id());
        } catch (RuntimeException failure) {
            worldEntryFailed(selection, failure);
            return;
        }
        operation.whenComplete(
                (entry, error) ->
                        Gdx.app.postRunnable(
                                () -> {
                                    if (disposed || selection != controller.selection()) {
                                        enteringWorld = false;
                                        return;
                                    }
                                    if (error != null) {
                                        worldEntryFailed(selection, error);
                                        return;
                                    }
                                    try {
                                        playerEntry = entry;
                                        worldEntry
                                                .apply(entry)
                                                .whenComplete(
                                                        (ignored, failure) ->
                                                                Gdx.app.postRunnable(
                                                                        () -> {
                                                                            if (disposed) return;
                                                                            enteringWorld = false;
                                                                            if (failure != null
                                                                                    && selection
                                                                                            == controller
                                                                                                    .selection())
                                                                                worldEntryFailed(
                                                                                        selection,
                                                                                        failure);
                                                                        }));
                                    } catch (RuntimeException failure) {
                                        worldEntryFailed(selection, failure);
                                    }
                                }));
    }

    private void worldEntryFailed(
            top.totodile.pokemmo.server.login.LoginService.Selection selection, Throwable failure) {
        enteringWorld = false;
        Throwable cause = failure;
        while ((cause instanceof java.util.concurrent.CompletionException
                        || cause instanceof java.util.concurrent.ExecutionException)
                && cause.getCause() != null) cause = cause.getCause();
        boolean missingRom = cause instanceof top.totodile.pokemmo.client.rom.MissingRomException;
        if (!missingRom) Gdx.app.error("world", "Cannot enter local character", failure);
        loadCharacters(selection);
        entryError.show(strings.get(missingRom ? 6805 : 2103));
    }

    private void createCharacter() {
        var selection = controller.selection();
        if (gameService == null
                || creatingCharacter
                || selection == null
                || !characterCreation.isOpen()) return;
        creatingCharacter = true;
        long version = characterCreation.version();
        gameService
                .create(
                        selection,
                        characterCreation.characterName(),
                        characterCreation.selectedAppearance())
                .whenComplete(
                        (character, error) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            if (disposed) return;
                                            creatingCharacter = false;
                                            if (selection != controller.selection()) return;
                                            if (error == null) {
                                                loadCharacters(selection);
                                                if (version == characterCreation.version())
                                                    characterCreation.closePopup();
                                            } else if (version == characterCreation.version()) {
                                                Throwable cause = error;
                                                while (cause
                                                                instanceof
                                                                java.util.concurrent
                                                                        .CompletionException
                                                        && cause.getCause() != null)
                                                    cause = cause.getCause();
                                                int id =
                                                        cause
                                                                        instanceof
                                                                        top.totodile.pokemmo.server
                                                                                                .game
                                                                                                .GameService
                                                                                                .Rejected
                                                                                        rejected
                                                                ? switch (rejected.reason) {
                                                                    case INVALID_NAME -> 2101;
                                                                    case NAME_TAKEN -> 2102;
                                                                    case ACCOUNT_FULL -> 2100;
                                                                    default -> 2103;
                                                                }
                                                                : 2103;
                                                characterCreation.creationError(id);
                                                if (id == 2103)
                                                    Gdx.app.error(
                                                            "game",
                                                            "Cannot create local character",
                                                            cause);
                                            }
                                        }));
    }

    private void deleteCharacter(top.totodile.pokemmo.server.game.GameService.Character character) {
        var selection = controller.selection();
        if (gameService == null || selection == null) return;
        long version = characterDelete.version();
        gameService
                .delete(selection, character.id())
                .whenComplete(
                        (ignored, error) ->
                                Gdx.app.postRunnable(
                                        () -> {
                                            if (disposed || selection != controller.selection())
                                                return;
                                            if (error == null) {
                                                if (version == characterDelete.version())
                                                    characterDelete.closePopup();
                                                loadCharacters(selection);
                                            } else if (version == characterDelete.version()) {
                                                characterDelete.failure(2103);
                                                Gdx.app.error(
                                                        "game",
                                                        "Cannot delete local character",
                                                        error);
                                            }
                                        }));
    }

    public void draw(float width, float height, float x, float y) {
        if(worldScene!=null&&!encounterActive()) {
            prepareBattleEnvironment(worldScene);
            if (encounterWindow != null) {
                var lead = pokemon.state().pokemons().stream()
                        .filter(p -> p.container() == top.totodile.pokemmo.server.game.PokemonData.PARTY
                                && !p.egg() && p.hp() > 0)
                        .min(java.util.Comparator.comparingInt(
                                top.totodile.pokemmo.server.game.PokemonData.Pokemon::position))
                        .orElse(null);
                try { encounterWindow.preparePartyEffect(lead); }
                catch (IOException | RuntimeException error) {
                    Gdx.app.error("battle", "Cannot prepare party entrance effect", error);
                }
            }
        }
        if(encounterWindow!=null&&encounterWindow.isOpen()&&playerEntry!=null&&controller.selection()!=null
                &&System.nanoTime()-battleHeartbeatAt>15_000_000_000L){
            battleHeartbeatAt=System.nanoTime();gameService.touchBattle(controller.selection(),playerEntry.character().id());
        }
        loginPanel.updateNotifications();
        if (worldActions != null) {
            if(story!=null&&!encounterActive())story.update();
            if(resources.audio()!=null&&(story==null||!story.busy())){
                var mapMusic=worldScene.music(System.currentTimeMillis());
                resources.audio().music(encounterWindow!=null&&encounterWindow.isOpen()&&mapMusic!=null
                    ?top.totodile.pokemmo.client.audio.RomAudio.battle(mapMusic.region(),encounterWindow.trainerBattle()):mapMusic);
            }
            if (encounterWindow != null) {
                encounterWindow.update();
                encounterWindow.drawMapTransition(Math.round(width), Math.round(height));
            }
            worldActions.randomEncounterEnabled(
                    !encounterActive()
                            && resources.roms().get(RomType.BLACK_WHITE) != null
                            && pokemon.state().pokemons().stream()
                                    .anyMatch(
                                            p ->
                                                    p.container()
                                                                    == top.totodile.pokemmo.server
                                                                            .game.PokemonData.PARTY
                                                            && !p.egg()
                                                            && p.hp() > 0));
            if (animationTests != null) animationTests.update();
            repeatPlayerShortcut();
            var dexState = worldScene.pokedex().pokedexState();
            if (!dexState.equals(displayedPokedexState)) {
                displayedPokedexState = dexState;
                pokedex.setState(dexState);
                trainerCard.reloadIfOpen();
            }
            worldActions.update();
            worldHud.battleBottom(
                    encounterWindow != null && encounterWindow.isOpen()
                            ? encounterWindow.commandBottom()
                            : 0);
            worldHud.update();
            restoreWorldInput();
            fps.setVisible(
                    Boolean.parseBoolean(
                            resources.preferences().get("client.graphics.show_fps", "true")));
            fps.setText(frameRate.frame());
            if (renderer.updateFontScale()) invalidateFontLayout(gui);
            pokedex.positionWindow();
            inventory.positionWindow();
            trainerCard.positionWindow();
            mail.positionWindow();
            pcMenu.positionWindow();
            market.positionWindow();
            giftShop.positionWindow();
            incubator.positionWindow();
            storage.positionWindow();
            pokemonSummaries.forEach(PokemonSummaryWindow::positionWindow);
            worldTools.positionWindow();
            if(storyInspection!=null)storyInspection.positionWindow();
            if(storyDebug!=null)storyDebug.positionWindow();
            if (encounterRecorder != null) encounterRecorder.positionWindow();
            gui.update();
            settings.positionWindow();
            worldLogout.positionWindow();
            if (exitConfirmation != null) exitConfirmation.positionWindow();
            return;
        }
        fps.setVisible(
                Boolean.parseBoolean(
                        resources.preferences().get("client.graphics.show_fps", "true")));
        if (mobileStatus != null) mobileStatus.update(System.nanoTime());
        if (mobileMenu != null) mobileMenu.update();
        fps.setText(frameRate.frame());
        if (renderer.updateFontScale()) invalidateFontLayout(gui);
        int nextX = Math.round(x), nextY = Math.round(y);
        if (nextX != offsetX || nextY != offsetY) {
            offsetX = nextX;
            offsetY = nextY;
            root.invalidateLayout();
        }
        gui.update();
        clientManagement.positionWindow();
        mods.positionWindow();
        settings.positionWindow();
        characterCreation.positionWindow();
        characterCustomization.positionWindow();
        characterDelete.positionWindow();
        entryError.positionWindow();
        if (exitConfirmation != null) exitConfirmation.positionWindow();
    }

    private static void invalidateFontLayout(Widget widget) {
        if (widget instanceof TextWidget text) text.setFont(text.getFont());
        for (int index = 0; index < widget.getNumChildren(); index++)
            invalidateFontLayout(widget.getChild(index));
        widget.invalidateLayout();
    }

    @Override
    public void dispose() {
        if(story!=null){story.close();story=null;}
        if (animationTests != null) animationTests.closePopup();
        if (worldHud != null) worldHud.chat().close();
        dismissEncounter();
        if (encounterWindow != null) encounterWindow.close();
        clientManagement.dispose();
        mods.dispose();
        inventory.closePopup();
        trainerCard.close();
        mail.close();
        pcMenu.closePopup();
        market.closePopup();
        giftShop.closePopup();
        incubator.closePopup();
        storage.forceClose();
        closePokemonSummaries();
        if(storyInspection!=null)storyInspection.closePopup();
        if(storyDebug!=null)storyDebug.closePopup();
        worldTools.forceClose();
        pokemon.close();
        inventoryItems.close();
        pokedex.close();
        disposed = true;
        characterDelete.closePopup();
        characterPage.clearCards();
        loginPanel.dispose();
        controller.close();
        characterCreation.dispose();
        characterCustomization.dispose();
        if (Gdx.input.getInputProcessor() == input) Gdx.input.setInputProcessor(null);
        gui.destroy();
        theme.destroy();
        renderer.close();
        if (ownsResources) resources.close();
    }
}
