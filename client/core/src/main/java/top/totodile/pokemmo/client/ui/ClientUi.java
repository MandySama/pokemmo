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
import java.io.IOException;
import java.net.*;
import java.util.function.Function;
import java.util.function.LongSupplier;
import top.totodile.pokemmo.client.config.SavedAccounts;
import top.totodile.pokemmo.client.resources.AssetUrlStreamHandler;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.rom.RomInfo;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.character.CharacterAppearance;
import top.totodile.pokemmo.client.ui.character.CharacterCreationWindow;
import top.totodile.pokemmo.client.ui.character.CharacterDeleteWindow;
import top.totodile.pokemmo.client.ui.character.CharacterSelectionPanel;
import top.totodile.pokemmo.client.ui.common.ExitConfirmation;
import top.totodile.pokemmo.client.ui.common.FrameRateCounter;
import top.totodile.pokemmo.client.ui.login.AnnouncementsPanel;
import top.totodile.pokemmo.client.ui.login.LoginController;
import top.totodile.pokemmo.client.ui.login.LoginPanel;
import top.totodile.pokemmo.client.ui.login.ServerSelectionPanel;
import top.totodile.pokemmo.client.ui.management.ClientManagementWindow;
import top.totodile.pokemmo.client.ui.management.ModManagementWindow;
import top.totodile.pokemmo.client.ui.menu.DesktopMenu;
import top.totodile.pokemmo.client.ui.menu.MobileSideMenu;
import top.totodile.pokemmo.client.ui.menu.MobileStatusIcons;
import top.totodile.pokemmo.client.ui.settings.SettingsWindow;
import top.totodile.pokemmo.client.ui.pokedex.PokedexWindow;
import top.totodile.pokemmo.client.ui.common.PlayerWindow;
import top.totodile.pokemmo.client.ui.inventory.InventoryItems;
import top.totodile.pokemmo.client.ui.inventory.InventoryWindow;
import top.totodile.pokemmo.client.ui.trainer.TrainerCardWindow;
import top.totodile.pokemmo.client.ui.mail.MailWindow;
import top.totodile.pokemmo.client.ui.mail.PcMenuWindow;
import top.totodile.pokemmo.client.ui.pokemon.*;
import top.totodile.pokemmo.client.ui.world.WorldToolsWindow;
import top.totodile.pokemmo.server.game.GameService;
import top.totodile.pokemmo.server.game.PlayerData;

/** 客户端公共界面、页面导航及 TWL 渲染和输入生命周期。 */
public final class ClientUi implements Disposable {
    private final GdxTwlRenderer renderer;
    private final GUI gui;
    private final ThemeManager theme;
    private final ClientResources resources;
    private final boolean ownsResources;
    private final Widget logo = new Widget();
    private final java.util.Map<Integer,de.matthiasmann.twl.renderer.Image> itemIcons = new java.util.HashMap<>();
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
    private final PokemonController pokemon;
    private final PokemonStorageWindow storage;
    private final PokemonSummaryWindow pokemonSummary;
    private final WorldToolsWindow worldTools;
    private java.util.function.Function<GameService.Entry,java.util.concurrent.CompletionStage<Void>> teleportWorld;
    public void onTeleport(java.util.function.Function<GameService.Entry,java.util.concurrent.CompletionStage<Void>> action){teleportWorld=action;}
    private PartyPanel party;
    private GameService.Entry playerEntry;
    private final java.util.Set<Integer> playerKeys=new java.util.HashSet<>();
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
    private Runnable worldExit;
    private final top.totodile.pokemmo.client.ui.common.ActionConfirmDialog worldLogout;
    private final java.util.Map<Widget,Boolean> worldHidden=new java.util.IdentityHashMap<>();
    private Runnable leaveWorld=() -> {};

    public void onLeaveWorld(Runnable action) { leaveWorld=java.util.Objects.requireNonNull(action); }

    public void showCharacters() {
        if(worldActions==null)return;
        playerKeys.clear();repeatingPlayerKey=0;suppressedPlayerCharacter=0;
        characterCustomization.closePopup();inventory.closePopup();trainerCard.closePopup();mail.forceClose();pcMenu.closePopup();storage.forceClose();pokemonSummary.closePopup();worldTools.forceClose();pokemon.bind(null,null);
        if(playerEntry!=null&&gameService!=null&&controller.selection()!=null)gameService.leave(controller.selection(),playerEntry.character().id());
        playerEntry=null;
        pokedex.closePopup();
        pokemon.removeParty(party);party=null;
        root.removeChild(worldHud);worldHud=null;worldScene=null;hotkeyBar=null;
        root.removeChild(worldActions);worldActions=null;
        worldHidden.forEach(Widget::setVisible);worldHidden.clear();
        enteringWorld=false;
        refreshLoginState();
        if(controller.selection()!=null)loadCharacters(controller.selection());
        Gdx.input.setInputProcessor(input);
        root.invalidateLayout();
    }

    /** Reference ROM loadItems runs before the world HUD becomes visible. */
    public void prepareWorldData() throws java.io.IOException {
        if(!mobile&&resources.roms().get(top.totodile.pokemmo.client.rom.RomType.BLACK_WHITE)!=null)inventoryItems.prepare();
    }
    public void prepareWorldIcons() throws java.io.IOException {
        if(mobile||resources.roms().get(top.totodile.pokemmo.client.rom.RomType.BLACK_WHITE)==null)return;
        inventoryItems.load();
        for(int id:new int[]{5436,5432,5437,5001,5431,5471,5452,5459,5626}){
            inventoryItems.tabIcon(id,false);inventoryItems.tabIcon(id,true);
        }
    }
    public com.badlogic.gdx.InputProcessor showWorld(top.totodile.pokemmo.client.world.WorldScene world) {
        return showWorld(world,playerEntry);
    }
    public com.badlogic.gdx.InputProcessor showWorld(top.totodile.pokemmo.client.world.WorldScene world,GameService.Entry entry) {
        playerKeys.clear();repeatingPlayerKey=0;suppressedPlayerCharacter=0;
        boolean replacing=worldScene!=null;
        if(replacing){
            characterCustomization.closePopup();inventory.closePopup();trainerCard.closePopup();mail.forceClose();pcMenu.closePopup();storage.forceClose();pokemonSummary.closePopup();worldTools.closeForTeleport();pokedex.closePopup();
            pokemon.removeParty(party);root.removeChild(worldHud);root.removeChild(worldActions);
        }
        playerEntry=entry;enteringWorld=false;
        world.surfSprites(resources.roms().get(top.totodile.pokemmo.client.rom.RomType.FIRE_RED));
        inventoryItems.previewAppearance(entry==null?null:entry.character().appearance());
        displayedPokedexState=world.pokedex().pokedexState();pokedex.setState(displayedPokedexState);
        worldActions=new top.totodile.pokemmo.client.ui.world.WorldActions(world,mobile);
        worldActions.onTeleport(()->openWorldTool(WorldToolsWindow.Tool.TELEPORT));
        worldActions.onItem(()->openWorldTool(WorldToolsWindow.Tool.ITEM));
        worldActions.onRandomItems(this::generateRandomItems);
        worldActions.onRandomOutfit(this::generateRandomOutfit);
        worldActions.onPortablePc(this::openPortablePc);
        worldActions.onPokemon(()->openWorldTool(WorldToolsWindow.Tool.POKEMON));
        worldScene=world;
        worldHud=new top.totodile.pokemmo.client.ui.world.WorldHud(world,strings,mobile,resources.preferences(),this::itemIcon,
                ()->{world.setInputBlocked(true);settings.open();},
                ()->{world.setInputBlocked(true);worldLogout.show(strings.get(1160),()->{leaveWorld.run();controller.back();});},worldExit,batteryPercent);
        hotkeyBar=new top.totodile.pokemmo.client.ui.inventory.HotkeyBar(inventoryItems,resources.preferences(),mobile);
        hotkeyBar.onUse(item->inventory.activateItem(item,InventoryWindow.ItemAction.USE));
        inventory.onItemDrop((item,x,y)->hotkeyBar!=null&&hotkeyBar.drop(item,x,y));
        worldHud.hotkeyBar(hotkeyBar);reloadHotkeyInventory();
        worldHud.onBag(()->toggle(inventory));
        worldHud.onTrainer(()->toggle(trainerCard));
        worldHud.onMail(()->toggle(mail));
        pokemon.onFollower(value->{if(worldScene==world)world.follower(resources,value,this::showNotification);});
        pokemon.onFollowerDrop((x,y)->worldScene==world&&!enteringWorld&&!entryError.isOpen()&&(!mobile||!pokedex.isOpen()&&!playerWindowsOpen())
                &&!playerWindowsOwnPointer(x,y)&&!gui.hasOpenPopups()&&!pokemon.hasMenu()&&!pokedex.showingMessage()&&!settings.blocksInput()
                &&!worldLogout.isOpen()&&(exitConfirmation==null||!exitConfirmation.isOpen())
                &&!worldActions.isInside(x,y)&&!worldHud.ownsPointer(x,y));
        storage.reset();pokemon.bind(controller.selection(),playerEntry==null?null:playerEntry.character().id());
        party=pokemon.party(false);worldHud.party(party);
        if(playerEntry!=null){inventory.region(playerEntry.region());inventory.money(world.worldInfo().money());mail.bind(controller.selection(),playerEntry.character());}
        worldHud.onCustomization(()->{if(playerEntry!=null){while(worldHud.closeOverlay()){}characterCustomization.openCustomization(playerEntry.character().appearance());restoreWorldInput();}});
        worldHud.onPokedex(()->{while(worldHud.closeOverlay()){}if(mobile)world.setInputBlocked(true);pokedex.open();restoreWorldInput();});
        if(!replacing)for(int i=0;i<root.getNumChildren();i++) {
            Widget child=root.getChild(i);worldHidden.put(child,child.isVisible());child.setVisible(false);
        }
        root.add(worldHud);root.add(worldActions); fps.setVisible(true);
        root.invalidateLayout();
        return new com.badlogic.gdx.InputMultiplexer(input,world);
    }
    private java.util.function.Function<top.totodile.pokemmo.server.game.GameService.Entry,java.util.concurrent.CompletionStage<Void>> worldEntry;
    private final top.totodile.pokemmo.client.ui.common.MessageDialog entryError;

    public void onRomRestart(Runnable restart) { clientManagement.onRestart(restart); mods.onRestart(restart); }
    public void showNotification(String text){loginPanel.showNotification(text);}

    public void onEnterWorld(java.util.function.Function<top.totodile.pokemmo.server.game.GameService.Entry,java.util.concurrent.CompletionStage<Void>> action) {
        worldEntry=java.util.Objects.requireNonNull(action);
    }

    ClientUi(Viewport viewport, Function<String, FileHandle> assets, boolean mobile,
               SavedAccounts savedAccounts, LoginController controller) {
        this(viewport, assets, mobile, savedAccounts, () -> System.nanoTime() / 1_000_000L, controller);
    }

    ClientUi(Viewport viewport, Function<String, FileHandle> assets, boolean mobile,
               SavedAccounts savedAccounts, LongSupplier notificationClock, LoginController controller) {
        this(viewport, assets, mobile, savedAccounts, notificationClock, controller, () -> -1);
    }

    ClientUi(Viewport viewport, Function<String, FileHandle> assets, boolean mobile,
               SavedAccounts savedAccounts, LongSupplier notificationClock, LoginController controller,
               java.util.function.IntSupplier batteryPercent) {
        this(viewport, assets, mobile, savedAccounts, notificationClock, controller, batteryPercent, () -> Gdx.app.exit());
    }

    ClientUi(Viewport viewport, Function<String, FileHandle> assets, boolean mobile,
             SavedAccounts savedAccounts, LongSupplier notificationClock, LoginController controller,
             java.util.function.IntSupplier batteryPercent, Runnable exit) {
        this(viewport, assets, mobile, savedAccounts, notificationClock, controller, batteryPercent, exit, null);
    }

    public ClientUi(Viewport viewport, Function<String, FileHandle> assets, boolean mobile,
             SavedAccounts savedAccounts, LongSupplier notificationClock, LoginController controller,
             java.util.function.IntSupplier batteryPercent, Runnable exit,
             top.totodile.pokemmo.server.game.GameService gameService) {
        this.gameService = gameService;
        this.batteryPercent = batteryPercent;
        this.controller = controller;
        this.mobile = mobile;
        this.notificationClock = notificationClock;
        ownsResources = !(assets instanceof ClientResources);
        resources = assets instanceof ClientResources ? (ClientResources) assets
                : new ClientResources(assets, savedAccounts.directory(), mobile);
        strings = resources.strings();
        renderer = new GdxTwlRenderer(viewport, strings.typeface());
        serverFrame = new ServerSelectionPanel(strings, controller);
        characterPage = new CharacterSelectionPanel(strings, controller);
        romInfo = new Label(romSummary());
        announcements = new AnnouncementsPanel(strings, mobile);
        desktopMenu = mobile ? null : new DesktopMenu(strings);
        mobileStatus = mobile ? new MobileStatusIcons(batteryPercent) : null;
        mobileMenu = mobile ? new MobileSideMenu(strings, mobileStatus, notificationClock, this::itemIcon) : null;
        fps.setTheme("/fpscounter");
        logo.setTheme("logo");
        root = new Widget() {
            @Override protected void paintOverlay(GUI gui){if(pokemon!=null)pokemon.paintDrag(getAnimationState());}
            @Override protected void layout() {
                if(worldActions!=null) {
                    loginPanel.layoutNotifications();
                    worldHud.position(getInnerX(),getInnerY(),getInnerWidth(),getInnerHeight());
                    worldActions.position(getInnerX()+(mobile?100:0),getInnerY(),getInnerWidth()-(mobile?100:0),getInnerHeight());
                    fps.adjustSize(); fps.setPosition(mobile?getInnerX():getInnerRight()-fps.getWidth(),mobile?getInnerBottom()-fps.getHeight():getInnerY());
                    return;
                }
                logo.setSize(484,143);
                logo.setPosition(getInnerX()+(getInnerWidth()-484)/2,getInnerY()+(getInnerHeight()-143)/2-200);
                romInfo.adjustSize();
                romInfo.setPosition(getInnerX() + 3, getInnerY() + 3);
                fps.adjustSize();
                fps.setPosition(mobile ? getInnerX() : getInnerRight() - fps.getWidth(),
                        mobile ? getInnerBottom() - fps.getHeight() : getInnerY());
                announcements.adjustSize();
                announcements.setPosition(getInnerX(), getInnerBottom() - announcements.getHeight()
                        - (mobile ? fps.getHeight() : 0));
                if (desktopMenu != null) {
                    desktopMenu.adjustSize();
                    desktopMenu.setPosition(getInnerRight() - desktopMenu.getWidth() - 4,
                            getInnerBottom() - desktopMenu.getHeight() - 4);
                }
                if (mobileStatus != null) {
                    mobileStatus.adjustSize();
                    mobileStatus.setPosition(getInnerRight() - mobileStatus.getWidth() - 5, getInnerY() + 5);
                }
                if (mobileMenu != null) mobileMenu.position();
                if (loginPanel.isVisible()) {
                    loginPanel.adjustSize();
                    loginPanel.setPosition((getInnerWidth()-loginPanel.getWidth())/2+getInnerX()+offsetX,
                        (getInnerHeight()-loginPanel.getHeight())/2+getInnerY()-offsetY);
                }
                if (serverFrame.isVisible()) {
                    serverFrame.adjustSize();
                    serverFrame.setPosition(getInnerX() + (getInnerWidth()-serverFrame.getWidth())/2,
                            getInnerY() + (getInnerHeight()-serverFrame.getHeight())/2);
                }
                characterPage.setPosition(getInnerX(), getInnerY());
                characterPage.setSize(getInnerWidth(), getInnerHeight());
                loginPanel.layoutNotifications();
            }
        };
        loginPanel = new LoginPanel(root, strings, mobile, savedAccounts, notificationClock, controller);
        if (mobileMenu != null) loginPanel.onInputRequested(mobileMenu::close);
        if (mobileStatus != null) mobileStatus.onMenu(() -> {
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
        entryError=new top.totodile.pokemmo.client.ui.common.MessageDialog(root,strings,mobile,mobileBackground,this::restoreWindowFocus);
        worldLogout=new top.totodile.pokemmo.client.ui.common.ActionConfirmDialog(root,strings,mobile,mobileBackground,this::restoreWindowFocus);
        characterPage.onEnter(this::enterWorld);
        clientManagement = new ClientManagementWindow(root, strings, mobile, restoreFocus, mobileBackground,
                resources.roms(), resources.romFilePicker(), () -> romInfo.setText(romSummary()),
                loginPanel::showNotification);
        mods = new ModManagementWindow(root, strings, mobile, restoreFocus, mobileBackground,
                resources.modImporter(),resources.romFilePicker(),renderer,resources.preferences(),loginPanel::showNotification);
        settings = new SettingsWindow(root, strings, mobile, restoreFocus, mobileBackground, resources.preferences(),loginPanel::showNotification);
        pokedex = new PokedexWindow(root,resources,renderer,mobile,mobileBackground,()->{
            restoreWorldInput();
        });
        if(gameService!=null)pokedex.setTierData(gameService.pokedexTiers());
        characterCreation = new CharacterCreationWindow(root, strings, mobile, () -> {
            characterPage.setVisible(controller.state() == LoginController.State.SELECTED);
            restoreWindowFocus();
        }, mobileBackground, resources.newAppearance(), renderer, notificationClock,loginPanel::showNotification);
        characterCustomization=new CharacterCreationWindow(root,strings,mobile,this::restoreWorldInput,mobileBackground,resources.newAppearance(),renderer,notificationClock,loginPanel::showNotification,true);
        characterCustomization.onCreate(this::saveCustomization);
        inventoryItems=new InventoryItems(resources,renderer);
        inventory=new InventoryWindow(root,strings,mobile,inventoryItems,id->{
            if(resources.apply("sprites/itemicons/"+id+".png").exists())return itemIcon(id);
            return inventoryItems.icon(id);
        },completed->{
            try { inventoryItems.load(); }
            catch(IOException failure){Gdx.app.error("inventory","Cannot load item ROM",failure);entryError.show(strings.format(1186,strings.get(92)));return;}
            playerRequest((selection,id)->gameService.inventory(selection,id),value->{if(hotkeyBar!=null&&playerEntry!=null)hotkeyBar.inventory(value,playerEntry.region());completed.accept(value);});
        },this::restoreWorldInput);
        inventory.onItemAction(this::useInventoryItem);
        trainerCard=new TrainerCardWindow(root,resources,renderer,mobile,characterCreation::savedAppearance,inventoryItems::icon,
                completed->playerRequest((selection,id)->gameService.trainer(selection,id),completed),this::restoreWorldInput);
        mail=new MailWindow(root,resources,renderer,mobile,gameService,mobileBackground,this::restoreWorldInput);
        pokemon=new PokemonController(resources,renderer,mobile,gameService,text->entryError.show(text),id->{pokedex.showSpecies(id);restoreWorldInput();});
        pokemon.itemIcons(inventoryItems::icon);
        storage=new PokemonStorageWindow(root,pokemon,mobile,this::restoreWorldInput);
        pokemonSummary=new PokemonSummaryWindow(root,pokemon,inventoryItems::icon,this::restoreWorldInput);
        if(!mobile)pokemon.onSummary(value->{try{inventoryItems.gameData(gameService);}catch(IOException failure){Gdx.app.error("pokemon","Cannot register species data",failure);return;}pokemonSummary.show(value,playerEntry==null?null:playerEntry.character().name());restoreWorldInput();});
        worldTools=new WorldToolsWindow(root,mobile,resources,inventoryItems,gameService,this::executeWorldTool,text->{
            if(worldActions!=null){pokemon.reload();inventory.reloadIfOpen();reloadHotkeyInventory();}
        },text->Gdx.app.error("world-tools",text),this::restoreWorldInput);
        pokemon.onChanged(()->pokedex.setOwnedSpecies(pokemon.state().pokemons().stream().filter(p->!p.egg()).map(top.totodile.pokemmo.server.game.PokemonData.Pokemon::species).collect(java.util.stream.Collectors.toSet())));
        pcMenu=new PcMenuWindow(root,strings,mobile,this::openPortablePc,this::openPcMail,this::restoreWorldInput);
        characterPage.onNewCharacter(() -> {
            characterCreation.availableRegions(resources.roms().loaded().stream().map(rom->rom.type().id).sorted().toList());
            characterCreation.open();
            if (characterCreation.isOpen()) characterPage.setVisible(false);
        });
        characterCreation.onCreate(this::createCharacter);
        characterDelete = new CharacterDeleteWindow(root, strings, mobile, restoreFocus, mobileBackground, this::deleteCharacter);
        characterPage.onDeleteCharacter(() -> characterDelete.open(characterPage.characters()));
        exitConfirmation = mobile ? new ExitConfirmation(root, strings, exit, restoreFocus, mobileBackground) : null;
        Runnable requestExit = () -> {
            loginPanel.hideKeyboard();
            characterCreation.hideKeyboard();
            if (mobile) exitConfirmation.open(); else exit.run();
        };
        worldExit=requestExit;
        if (desktopMenu != null) desktopMenu.onExit(requestExit);
        if (mobileMenu != null) mobileMenu.onExit(requestExit);
        Runnable openManagement = () -> { loginPanel.hideKeyboard(); characterCreation.hideKeyboard(); clientManagement.open(); };
        if (desktopMenu != null) desktopMenu.onClientManagement(openManagement);
        if (mobileMenu != null) mobileMenu.onClientManagement(openManagement);
        Runnable openMods = () -> { hideEditors(); mods.open(); };
        if (desktopMenu != null) desktopMenu.onMods(openMods);
        if (mobileMenu != null) mobileMenu.onMods(openMods);
        Runnable openSettings = () -> { loginPanel.hideKeyboard(); characterCreation.hideKeyboard(); settings.open(); };
        if (desktopMenu != null) desktopMenu.onSettings(openSettings);
        if (mobileMenu != null) mobileMenu.onSettings(openSettings);
        theme = loadTheme(resources.selectedTheme());
        gui.applyTheme(theme);
        if (!mobile) renderer.setFullscreenCursors(theme.getImage("fullscreen.cursor.arrow"), theme.getImage("fullscreen.cursor.text"));
        input = new InputAdapter() {
            private boolean windowPointerCapture;
            private boolean pointer(int x,int y,int button,boolean pressed) {
                if(enteringWorld) return true;
                Vector2 point=viewport.unproject(new Vector2(x,y));
                int px=Math.round(point.x),py=renderer.getHeight()-Math.round(point.y);
                boolean captured=windowPointerCapture;
                if(pressed&&button>=0){windowPointerCapture=playerWindowsOwnPointer(px,py)||gui.hasOpenPopups()||hotkeyBar!=null&&hotkeyBar.isInside(px,py);captured=windowPointerCapture;if(worldScene!=null&&!mobile&&!windowPointerCapture&&!worldHud.ownsPointer(px,py))releaseWindowFocus();}
                if(!pressed&&button>=0)windowPointerCapture=false;
                if(!pressed&&button<0)pokemonSummary.hoverMenus(Math.round(point.x),renderer.getHeight()-Math.round(point.y));
                if(worldScene!=null&&pressed&&button==0&&!entryError.isOpen()&&!settings.blocksInput()&&(!mobile||!pokedex.isOpen()&&!mail.isOpen()&&!inventory.isOpen()&&!trainerCard.isOpen())&&!characterCustomization.isOpen()&&(mobile||(!playerWindowsOwnPointer(px,py)||storage.isOpen()&&storage.ownsPointer(px,py)&&topPlayerWindow()==storage)&&!gui.hasOpenPopups()))pokemon.touchDown(Math.round(point.x),renderer.getHeight()-Math.round(point.y),button);
                if(pressed&&button==0&&pokedex.isOpen())pokedex.touchDown(Math.round(point.x),renderer.getHeight()-Math.round(point.y));
                if(worldActions!=null && !captured && (!mobile||!pokedex.isOpen()&&!playerWindowsOpen()) && !playerWindowsOwnPointer(px,py) && !gui.hasOpenPopups() && !pokemon.hasMenu() && !pokedex.showingMessage() && !settings.blocksInput() && !worldLogout.isOpen() && (exitConfirmation==null||!exitConfirmation.isOpen())
                        && !worldActions.isInside(Math.round(point.x),renderer.getHeight()-Math.round(point.y))
                        && !worldHud.ownsPointer(Math.round(point.x),renderer.getHeight()-Math.round(point.y))) {
                    gui.handleMouse(-1,-1,button,false);return false;
                }
                boolean handled=gui.handleMouse(Math.round(point.x), renderer.getHeight()-Math.round(point.y),button,pressed);if(pressed)restoreWorldInput();return handled;
            }
            @Override public boolean touchDown(int x,int y,int pointer,int button){
                if (pointer != 0) return false;
                if(button==0&&mods.isOpen()) {
                    Vector2 p=viewport.unproject(new Vector2(x,y));
                    mods.pointerDown(Math.round(p.x),renderer.getHeight()-Math.round(p.y));
                }
                if(button==0){
                    Vector2 point=viewport.unproject(new Vector2(x,y));int px=Math.round(point.x),py=renderer.getHeight()-Math.round(point.y);
                    Widget target=gui.getWidgetAt(px,py);
                    while(target!=null&&!(target instanceof top.totodile.pokemmo.client.ui.common.TouchScrollPane)){
                        if(target instanceof Scrollbar){target=null;break;}
                        target=target.getParent();
                    }
                    playerTouchScroll=(top.totodile.pokemmo.client.ui.common.TouchScrollPane)target;
                    if(playerTouchScroll!=null)playerTouchScroll.beginTouch(px,py,mobile);
                }
                if(worldHud!=null&&(!mobile||!pokedex.isOpen()&&!playerWindowsOpen())&&!pokedex.showingMessage()&&!settings.blocksInput()&&!worldLogout.isOpen()) {
                    Vector2 point=viewport.unproject(new Vector2(x,y));
                    if(worldHud.dismissMenuOutside(Math.round(point.x),renderer.getHeight()-Math.round(point.y)))return true;
                }
                if(worldActions!=null && !settings.blocksInput()) return pointer(x,y,button,true);
                Vector2 point = viewport.unproject(new Vector2(x,y));
                if ((exitConfirmation == null || !exitConfirmation.isOpen()) && !characterCreation.isAbove(settings))
                    settings.touchDown(Math.round(point.x), renderer.getHeight()-Math.round(point.y), x, y, button);
                return pointer(x,y,button,true);
            }
            @Override public boolean touchUp(int x,int y,int pointer,int button){
                if (pointer != 0) return false;
                if(button==0&&mods.pointerUp())return true;
                if(playerTouchScroll!=null){Vector2 p=viewport.unproject(new Vector2(x,y));boolean pan=playerTouchScroll.endTouch(Math.round(p.x),renderer.getHeight()-Math.round(p.y));playerTouchScroll=null;if(pan)return true;}
                if(button==0&&worldScene!=null){Vector2 p=viewport.unproject(new Vector2(x,y));if(pokemon.touchUp(Math.round(p.x),renderer.getHeight()-Math.round(p.y))){gui.handleMouse(-1,-1,button,false);return true;}}
                if(pokedex.isOpen()){Vector2 p=viewport.unproject(new Vector2(x,y));if(pokedex.touchUp(Math.round(p.x),renderer.getHeight()-Math.round(p.y)))return true;}
                if(worldActions!=null && !settings.blocksInput()) return pointer(x,y,button,false);
                if (settings.touchUp(x,y,button)) return true;
                return pointer(x,y,button,false);
            }
            @Override public boolean touchDragged(int x,int y,int pointer){
                if (pointer != 0) return false;
                if(mods.isOpen()) {
                    Vector2 p=viewport.unproject(new Vector2(x,y));
                    if(mods.pointerDrag(Math.round(p.x),renderer.getHeight()-Math.round(p.y)))return true;
                }
                if(playerTouchScroll!=null&&!pokemon.hasDragCandidate()){Vector2 p=viewport.unproject(new Vector2(x,y));if(playerTouchScroll.drag(Math.round(p.x),renderer.getHeight()-Math.round(p.y)))return true;}
                if(worldScene!=null){Vector2 p=viewport.unproject(new Vector2(x,y));if(pokemon.touchDragged(Math.round(p.x),renderer.getHeight()-Math.round(p.y)))return true;}
                if(pokedex.isOpen()){Vector2 p=viewport.unproject(new Vector2(x,y));if(pokedex.touchDragged(Math.round(p.x),renderer.getHeight()-Math.round(p.y)))return true;}
                if(worldActions!=null && !settings.blocksInput()) return pointer(x,y,-1,false);
                if (settings.touchDragged(x,y)) return true;
                return pointer(x,y,-1,false);
            }
            @Override public boolean touchCancelled(int x,int y,int pointer,int button){
                if(pointer==0)mods.pointerUp();
                if(pointer==0&&playerTouchScroll!=null){playerTouchScroll.cancelTouch();playerTouchScroll=null;gui.handleMouse(-1,-1,button,false);}
                if(pointer==0)pokemon.cancelDrag();
                if(pointer==0&&pokedex.isOpen()){pokedex.cancelTouch();return gui.handleMouse(-1,-1,button,false);}
                if (pointer != 0 || !settings.blocksInput()) return false;
                settings.cancelTouch();
                return gui.handleMouse(-1,-1,button,false);
            }
            @Override public boolean mouseMoved(int x,int y){return pointer(x,y,-1,false);}
            @Override public boolean keyDown(int key){
                if(enteringWorld)return true;
                if(settings.showingMessage()) {
                    int code=keyCode(key);if(code!=0)gui.handleKey(code,'\0',true);return true;
                }
                if(worldActions!=null) {
                    if(characterCustomization.isOpen()){if(key==Input.Keys.ESCAPE||key==Input.Keys.BACK){characterCustomization.back();return true;}int code=keyCode(key);return code!=0&&gui.handleKey(code,'\0',true);}
                    if(entryError.isOpen()){
                        if(key==Input.Keys.ESCAPE||key==Input.Keys.BACK||key==Input.Keys.ENTER)entryError.closePopup();return true;
                    }
                    if(worldLogout.isOpen()) {
                        int code=keyCode(key);if(code!=0)gui.handleKey(code,'\0',true);return true;
                    }
                    if(exitConfirmation!=null&&exitConfirmation.isOpen()) {
                        if(key==Input.Keys.ESCAPE||key==Input.Keys.BACK)exitConfirmation.closePopup();
                        return true;
                    }
                    if(settings.blocksInput()) {
                        if(settings.captureKey(key))return true;
                        if(key==Input.Keys.ESCAPE||key==Input.Keys.BACK){settings.back();return true;}
                        int code=keyCode(key);return code!=0&&gui.handleKey(code,'\0',true);
                    }
                    if((key==Input.Keys.ESCAPE||key==Input.Keys.BACK)&&pokemon.closeMenu())return true;
                    if(pokemon.hasMenu()){int code=keyCode(key);if(code!=0)gui.handleKey(code,'\0',true);return true;}
                    if(inventory.hasMenu()){
                        if(key==Input.Keys.ESCAPE||key==Input.Keys.BACK)inventory.closeMenu();
                        else {int code=keyCode(key);if(code!=0)gui.handleKey(code,'\0',true);}return true;
                    }
                    if(worldHud.editingChat()) {
                        if(key==Input.Keys.ESCAPE||key==Input.Keys.BACK){worldHud.closeOverlay();restoreWorldInput();return true;}
                        if(key==Input.Keys.ENTER){worldHud.submitChat();restoreWorldInput();return true;}
                        int code=keyCode(key);if(code!=0)gui.handleKey(code,'\0',true);return true;
                    }
                    if(!worldInputBlocked()&&hotkeyBar!=null){
                        if(playerKeys.contains(key))return true;
                        if(hotkeyBar.keyDown(key)){playerKeys.add(key);return true;}
                    }
                    if(handlePlayerShortcut(key))return true;
                    if(!mobile&&!focusedTextInput()&&!gui.hasOpenPopups()){
                        if(key==Input.Keys.ENTER){worldHud.openChat();return true;}
                        int action=Integer.parseInt(resources.preferences().get("client.controls.gdx.key_a",Integer.toString(Input.Keys.J)));
                        if(key==action&&playerEntry!=null&&PlayerData.pcTarget(worldScene.movement().position(),playerEntry.region())){pcMenu.playerName(strings,playerEntry.character().name());pcMenu.open();restoreWorldInput();return true;}
                    }
                    var top=topPlayerWindow();
                    if(top!=null){
                        if(key==Input.Keys.ESCAPE||key==Input.Keys.BACK){if(top==mail)mail.back();else if(top==worldTools)worldTools.back();else top.closePopup();return true;}
                        int code=keyCode(key);return code!=0&&gui.handleKey(code,'\0',true);
                    }
                    if(pokedex.showingMessage()) {
                        if(key==Input.Keys.ESCAPE||key==Input.Keys.BACK||key==Input.Keys.ENTER)pokedex.back();
                        return true;
                    }
                    if(pokedex.isOpen()) {
                        if(key==Input.Keys.ESCAPE||key==Input.Keys.BACK){pokedex.back();return true;}
                        int code=keyCode(key);return code!=0&&gui.handleKey(code,'\0',true);
                    }
                    if(key==Input.Keys.ESCAPE || key==Input.Keys.BACK) {
                        if(!worldHud.closeOverlay())leaveWorld.run();return true;
                    }
                    if(key==Input.Keys.ENTER){worldHud.openChat();return true;}
                    if(key==Integer.parseInt(resources.preferences().get("client.controls.gdx.key_a",Integer.toString(Input.Keys.J)))){
                        if(playerEntry!=null&&PlayerData.pcTarget(worldScene.movement().position(),playerEntry.region())){
                            pcMenu.playerName(strings,playerEntry.character().name());pcMenu.open();restoreWorldInput();return true;
                        }
                    }
                    return false;
                }
                if(enteringWorld) return true;
                if(entryError.isOpen()) {
                    if(key==Input.Keys.ESCAPE || key==Input.Keys.BACK || key==Input.Keys.ENTER) entryError.closePopup();
                    return true;
                }
                if (mods.isImporting() || clientManagement.isImporting()) return true;
                if (settings.captureKey(key)) return true;
                if(key==Input.Keys.ESCAPE || key==Input.Keys.BACK){
                    if(clientManagement.closeMessage()) return true;
                    if(mods.closeMessage()) return true;
                    if(exitConfirmation != null && exitConfirmation.isOpen()) exitConfirmation.closePopup();
                    else if(characterDelete.isOpen()) characterDelete.closePopup();
                    else if(settings.isAbove(clientManagement) && settings.isAbove(characterCreation) && settings.isAbove(mods)) {
                        settings.back();
                    }
                    else if(mods.isAbove(clientManagement) && mods.isAbove(characterCreation))mods.closePopup();
                    else if(clientManagement.isAbove(characterCreation))clientManagement.closePopup();
                    else if(mobileMenu!=null && mobileMenu.isVisible())mobileMenu.close();
                    else if(characterCreation.isOpen())characterCreation.back();
                    else if(loginPanel.hasNativeEditor())loginPanel.hideKeyboard();
                    else if(controller.state()!=LoginController.State.LOGIN)controller.back();
                    else if(!gui.handleKey(Event.KEY_ESCAPE,'\0',true))loginPanel.hideKeyboard();
                    return true;
                }
                int code=keyCode(key);
                return code!=0 && gui.handleKey(code,'\0',true);
            }
            @Override public boolean keyUp(int key){
                if(worldScene!=null)worldScene.keyUp(key);
                if(playerKeys.remove(key)){if(repeatingPlayerKey==key)repeatingPlayerKey=0;suppressedPlayerCharacter=0;return true;}
                int code=keyCode(key);return code!=0 && gui.handleKey(code,'\0',false);
            }
            @Override public boolean keyTyped(char value){
                for(int held:playerKeys)if(held>=Input.Keys.A&&held<=Input.Keys.Z&&java.lang.Character.toLowerCase(value)=='a'+held-Input.Keys.A)return true;
                if(suppressedPlayerCharacter!=0){char suppressed=suppressedPlayerCharacter;suppressedPlayerCharacter=0;if(java.lang.Character.toLowerCase(value)==suppressed)return true;}
                if(worldActions!=null&&!worldHud.editingChat()&&(!mobile||!pokedex.isOpen()&&!playerWindowsOpen())&&!focusedTextInput()&&!gui.hasOpenPopups()&&!settings.blocksInput())return false;
                if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))return false;
                if(value<32 || value==127)return false;
                boolean handled=gui.handleKey(Event.KEY_NONE,value,true);
                // 字符输入是一次完整事件，必须释放以免 TWL 持续自动重复末尾字符。
                gui.handleKey(Event.KEY_NONE,value,false);
                return handled;
            }
            @Override public boolean scrolled(float x,float y){
                if(worldActions!=null&&!pokedex.isOpen()&&!playerWindowsOpen()&&!settings.blocksInput())return false;
                return gui.handleMouseWheel(-(int)y);
            }
        };
        Gdx.input.setInputProcessor(input);
        Gdx.input.setCatchKey(Input.Keys.BACK,true);
        controller.listen(this::refreshLoginState);
        loginPanel.focusDefault();
    }

    private boolean playerWindowsOpen(){return inventory!=null&&(characterCustomization.isOpen()||inventory.isOpen()||trainerCard.isOpen()||mail.isOpen()||pcMenu.isOpen()||storage.isOpen()||worldTools.isOpen()||pokemonSummary.isOpen());}
    private PlayerWindow topPlayerWindow(){
        PlayerWindow top=null;
        for(var window:new PlayerWindow[]{inventory,trainerCard,mail,pcMenu,storage,worldTools,pokemonSummary})if(window.isOpen()&&window.isAbove(pokedex)&&(top==null||window.isAbove(top)))top=window;
        return top;
    }
    private boolean playerWindowsOwnPointer(int x,int y){
        for(var window:new top.totodile.pokemmo.client.ui.common.ClientWindow[]{pokedex,inventory,trainerCard,mail,pcMenu,storage,worldTools,pokemonSummary,characterCustomization})if(window!=null&&window.isOpen()&&window.ownsPointer(x,y))return true;
        return false;
    }
    private void releaseWindowFocus(){for(var window:new top.totodile.pokemmo.client.ui.common.ClientWindow[]{pokedex,inventory,trainerCard,mail,pcMenu,storage,worldTools,pokemonSummary})if(window!=null)window.giveupKeyboardFocus();restoreWorldInput();}
    private boolean focusedTextInput(){return focusedTextInput(gui);}
    private boolean focusedTextInput(Widget parent){if(parent instanceof EditField)return true;for(int i=0;i<parent.getNumChildren();i++){var child=parent.getChild(i);if(child.isVisible()&&child.hasKeyboardFocus()&&focusedTextInput(child))return true;}return false;}
    private boolean worldInputBlocked(){return enteringWorld||mobile&&(playerWindowsOpen()||pokedex.isOpen())||characterCustomization.isOpen()||gui.hasOpenPopups()||focusedTextInput()||pokemon.hasMenu()||entryError.isOpen()||pokedex.showingMessage()||worldHud.menuOpen()||worldHud.editingChat()||settings.blocksInput()||worldLogout.isOpen()||exitConfirmation!=null&&exitConfirmation.isOpen();}
    private void restoreWorldInput(){if(worldScene!=null)worldScene.setInputBlocked(worldInputBlocked());}
    private void toggle(PlayerWindow window){if(window.isOpen())window.closePopup();else{while(worldHud.closeOverlay()){}window.open();}restoreWorldInput();}
    private boolean handlePlayerShortcut(int key){
        return handlePlayerShortcut(key,false);
    }
    private boolean handlePlayerShortcut(int key,boolean repeated){
        if(entryError.isOpen()||pokedex.showingMessage()||worldHud.editingChat()||focusedTextInput()||mail.isOpen()&&mail.hasDialog()||mobile&&worldTools.isOpen())return false;
        int bag=Integer.parseInt(resources.preferences().get("client.controls.gdx.bag",Integer.toString(Input.Keys.B)));
        int trainer=Integer.parseInt(resources.preferences().get("client.controls.gdx.trainer",Integer.toString(Input.Keys.C)));
        int dex=Integer.parseInt(resources.preferences().get("client.controls.gdx.codex",Integer.toString(Input.Keys.N)));
        int menu=Integer.parseInt(resources.preferences().get("client.controls.gdx.gamemenu","0"));
        if(key<=0||key!=bag&&key!=trainer&&key!=dex&&key!=menu)return false;
        if(repeated||playerKeys.add(key)){
            if(!repeated){repeatingPlayerKey=key;playerRepeatAt=gui.getCurrentTime()+250;}
            suppressedPlayerCharacter=key>=Input.Keys.A&&key<=Input.Keys.Z?(char)('a'+key-Input.Keys.A):0;
            if(key==bag||key==trainer)toggle(key==bag?inventory:trainerCard);
            else if(key==dex){if(pokedex.isOpen())pokedex.closePopup();else{while(worldHud.closeOverlay()){}pokedex.open();}restoreWorldInput();}
            else{worldHud.toggleMenu();restoreWorldInput();}
        }
        return true;
    }
    private void repeatPlayerShortcut(){
        if(repeatingPlayerKey==0||gui.getCurrentTime()<playerRepeatAt)return;
        // TWL GUI key repeat: 250 ms initial delay, then 30 repeats per second.
        playerRepeatAt=gui.getCurrentTime()+1000/30;
        if(!enteringWorld&&!settings.blocksInput()&&!worldLogout.isOpen()&&!pokemon.hasMenu()&&!inventory.hasMenu()&&(exitConfirmation==null||!exitConfirmation.isOpen()))handlePlayerShortcut(repeatingPlayerKey,true);
    }
    private <T> void playerRequest(java.util.function.BiFunction<top.totodile.pokemmo.server.login.LoginService.Selection,String,java.util.concurrent.CompletionStage<T>> operation,java.util.function.Consumer<T> completed){
        var entry=playerEntry;var selection=controller.selection();if(entry==null||selection==null||gameService==null)return;
        operation.apply(selection,entry.character().id()).whenComplete((result,error)->Gdx.app.postRunnable(()->{
            if(disposed||playerEntry!=entry||controller.selection()!=selection)return;
            if(error!=null){Gdx.app.error("player","Cannot load player data",error);entryError.show(strings.get(2103));}else completed.accept(result);
        }));
    }
    private void useInventoryItem(PlayerData.Item item,InventoryWindow.ItemAction action){
        if(action!=InventoryWindow.ItemAction.USE||worldScene==null||enteringWorld||item.amount()<=0)return;
        // ItemUseService.useBike toggles transportation without consuming the item.
        // WorldState already persists this character's travel state.
        if(inventoryItems.bicycle(item.itemId()))worldScene.motion().toggleBicycle();
    }
    private void reloadHotkeyInventory(){
        if(hotkeyBar==null||playerEntry==null)return;
        try{inventoryItems.load();}catch(IOException error){Gdx.app.error("hotbar","Cannot load item resources",error);return;}
        var target=hotkeyBar;int region=playerEntry.region();
        playerRequest((selection,id)->gameService.inventory(selection,id),value->{if(target==hotkeyBar)target.inventory(value,region);});
    }
    private void openPcMail(){
        if(playerEntry==null||gameService==null)return;
        var entry=playerEntry;var selection=controller.selection();var position=worldScene.movement().position();
        if(!PlayerData.pcTarget(position,entry.region()))return;
        gameService.savePosition(selection,entry.character().id(),position).thenCompose(ignored->gameService.openMailbox(selection,entry.character().id())).whenComplete((allowed,error)->Gdx.app.postRunnable(()->{
            if(disposed||playerEntry!=entry||worldScene==null||!position.equals(worldScene.movement().position())){gameService.closeMailbox(selection,entry.character().id());return;}
            if(error!=null||!Boolean.TRUE.equals(allowed)){entryError.show(strings.get(5801));return;}
            mail.bind(selection,entry.character());mail.mailboxAccess(true);mail.open();restoreWorldInput();
        }));
    }
    private void openPortablePc(){
        if(playerEntry==null||gameService==null)return;
        pokemon.closeMenu();pokemon.cancelDrag();while(worldHud.closeOverlay()){}storage.open();restoreWorldInput();
    }
    private void generateRandomOutfit(){
        if(playerEntry==null||gameService==null)return;
        var entry=playerEntry;worldActions.randomOutfitEnabled(false);
        var appearance=characterCustomization.randomAppearance(entry.character().appearance());
        gameService.saveAppearance(controller.selection(),entry.character().id(),appearance).whenComplete((character,error)->Gdx.app.postRunnable(()->{
            if(worldActions!=null)worldActions.randomOutfitEnabled(true);
            if(disposed||playerEntry!=entry)return;
            if(error!=null){Gdx.app.error("customization","Cannot randomize outfit",error);entryError.show(strings.get(2103));return;}
            playerEntry=new GameService.Entry(character,entry.position(),entry.travelState(),entry.pokedexState(),entry.worldInfo(),entry.region());
            if(worldScene!=null)worldScene.appearance(character.appearance());trainerCard.reloadIfOpen();
        }));
    }
    private void saveCustomization(){
        if(playerEntry==null||gameService==null||!characterCustomization.isOpen())return;
        var entry=playerEntry;long version=characterCustomization.version();characterCustomization.saving(true);
        gameService.saveAppearance(controller.selection(),entry.character().id(),characterCustomization.selectedAppearance()).whenComplete((character,error)->Gdx.app.postRunnable(()->{
            characterCustomization.saving(false);if(disposed||playerEntry!=entry)return;
            if(error!=null){Gdx.app.error("customization","Cannot save character outfit",error);entryError.show(strings.get(2103));return;}
            playerEntry=new GameService.Entry(character,entry.position(),entry.travelState(),entry.pokedexState(),entry.worldInfo(),entry.region());
            if(worldScene!=null)worldScene.appearance(character.appearance());trainerCard.reloadIfOpen();
            if(version==characterCustomization.version())characterCustomization.closePopup();restoreWorldInput();
        }));
    }
    private void generateRandomItems(){
        if(generatingRandomItems||playerEntry==null||gameService==null)return;
        final java.util.List<top.totodile.pokemmo.client.ui.inventory.RandomInventoryItems.Grant> grants;
        try{
            inventoryItems.gameData(gameService);
            grants=top.totodile.pokemmo.client.ui.inventory.RandomInventoryItems.sample(inventoryItems,inventory.randomItemPageHeight(),new java.util.Random());
            if(grants.isEmpty())throw new IllegalStateException("No available item templates for random inventory");
        }catch(Exception error){Gdx.app.error("inventory","Cannot prepare random items",error);entryError.show(strings.get(2103));return;}
        var entry=playerEntry;var selection=controller.selection();generatingRandomItems=true;
        var replacement=new java.util.LinkedHashMap<Integer,Integer>();
        for(var grant:grants)replacement.put(grant.itemId(),grant.amount());
        gameService.replaceInventory(selection,entry.character().id(),replacement).whenComplete((ignored,error)->Gdx.app.postRunnable(()->{
            generatingRandomItems=false;if(disposed||playerEntry!=entry)return;
            if(error!=null){Gdx.app.error("inventory","Cannot generate random items",error);entryError.show(strings.get(2103));}
            else {inventory.reloadIfOpen();reloadHotkeyInventory();}
        }));
    }
    private void openWorldTool(WorldToolsWindow.Tool tool){
        if(playerEntry==null||gameService==null)return;
        pokemon.closeMenu();while(worldHud.closeOverlay()){}worldTools.show(tool,playerEntry.region(),worldScene.movement().position());restoreWorldInput();
    }
    private java.util.concurrent.CompletionStage<String> executeWorldTool(WorldToolsWindow.Tool tool,int[] values){
        var entry=playerEntry;var selection=controller.selection();
        if(entry==null||selection==null||gameService==null)return java.util.concurrent.CompletableFuture.failedFuture(new IllegalStateException("The character is not ready."));
        if(tool==WorldToolsWindow.Tool.ITEM)return gameService.createItem(selection,entry.character().id(),values[0],values[1]).thenApply(item->"");
        if(tool==WorldToolsWindow.Tool.POKEMON)return gameService.addPokemon(selection,entry.character().id(),values[0],values[1]).thenApply(p->"");
        if(teleportWorld==null)return java.util.concurrent.CompletableFuture.failedFuture(new IllegalStateException("The character is not ready for movement."));
        if(values[0]<0||values[0]>4)throw new IllegalArgumentException("Unknown region: "+values[0]);
        var old=worldScene;var position=new GameService.Position(values[1],values[2],values[3],values[4],old.movement().position().direction());
        var state=new GameService.TravelState(old.movement().noClip(),old.motion().speed(),old.motion().bicycle());
        var next=new GameService.Entry(entry.character(),position,state,old.pokedex().pokedexState(),old.worldInfo(),values[0]);
        enteringWorld=true;old.setInputBlocked(true);
        java.util.concurrent.CompletionStage<Void> operation;
        try{operation=teleportWorld.apply(next);}catch(RuntimeException failure){enteringWorld=false;throw failure;}
        return operation.whenComplete((ignored,error)->Gdx.app.postRunnable(()->{enteringWorld=false;restoreWorldInput();})).thenApply(ignored->"");
    }
    private String romSummary() {
        // 参考 ChatHistoryViewportComponent.Nq：主卡带缺失时保留 --，其余按固定顺序追加。
        var roms=resources.roms();
        RomInfo blackWhite=roms.get(RomType.BLACK_WHITE);
        StringBuilder text=new StringBuilder(strings.get(1170)).append("\nNDS: ")
                .append(blackWhite==null ? "--" : blackWhite.title());
        for (var type:java.util.List.of(RomType.PLATINUM,
                RomType.HEART_GOLD_SOUL_SILVER)) {
            RomInfo rom=roms.get(type);
            if (rom!=null) text.append(", ").append(rom.title().replaceAll("\\P{Print}", ""));
        }
        RomInfo fireRed=roms.get(RomType.FIRE_RED);
        text.append("\nGBA: ").append(fireRed==null ? "--" : fireRed.code()+" v1."+fireRed.revision());
        RomInfo emerald=roms.get(RomType.EMERALD);
        if (emerald!=null) text.append(", ").append(emerald.code()).append(" v1.").append(emerald.revision());
        return text.toString();
    }
    private void hideEditors() {
        loginPanel.hideKeyboard();
        characterCreation.hideKeyboard();
    }
    private void restoreWindowFocus() {
        if (settings.isAbove(clientManagement) && settings.isAbove(characterCreation) && settings.isAbove(mods)) settings.raiseWindow();
        else if (mods.isAbove(clientManagement) && mods.isAbove(characterCreation)) mods.raiseWindow();
        else if (clientManagement.isAbove(characterCreation)) clientManagement.raiseWindow();
        else if (characterCreation.isOpen()) characterCreation.raiseWindow();
        else if (controller.state() == LoginController.State.LOGIN) loginPanel.focusDefault();
        else if (controller.state() == LoginController.State.SELECTED) characterPage.focusDefault();
        else if (controller.state() == LoginController.State.SERVERS) serverFrame.focusDefault();
    }

    private ThemeManager loadTheme(ClientResources.Theme selected) {
        try {
            return ThemeManager.createThemeManager(resources.themeUrl(selected), renderer, renderer.createNewCacheContext(),
                    new java.util.HashMap<String,Object>(resources.imageConstants(selected,strings.language())));
        } catch (IOException | RuntimeException error) {
            if (!selected.name().equals("default") && !selected.name().equals("android")) {
                java.util.logging.Logger.getLogger(ClientUi.class.getName()).log(java.util.logging.Level.WARNING,
                        "Unable to load theme " + selected.name() + "; using default theme", error);
                return loadTheme(resources.defaultTheme());
            }
            throw new IllegalStateException("加载登录主题失败",error);
        }
    }
    private de.matthiasmann.twl.renderer.Image itemIcon(int id) {
        return itemIcons.computeIfAbsent(id,key->new de.matthiasmann.twl.renderer.Image(){
            public int getWidth(){return 24;}
            public int getHeight(){return 24;}
            public void draw(de.matthiasmann.twl.renderer.AnimationState state,int x,int y){draw(state,x,y,24,24);}
            public void draw(de.matthiasmann.twl.renderer.AnimationState state,int x,int y,int w,int h){
                if(inventoryItems==null)return;
                var image=inventoryItems.tabIcon(key,state.getAnimationState(Button.STATE_HOVER)||state.getAnimationState(Widget.STATE_KEYBOARD_FOCUS));
                if(image!=null)image.draw(state,x,y,w,h);
            }
            public de.matthiasmann.twl.renderer.Image createTintedVersion(Color tint){var image=inventoryItems.icon(key);return image==null?this:image.createTintedVersion(tint);}
        });
    }
    private static int keyCode(int key) {
        return switch(key) {
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
            case Input.Keys.C -> Event.KEY_C;
            case Input.Keys.V -> Event.KEY_V;
            case Input.Keys.X -> Event.KEY_X;
            default -> 0;
        };
    }
    private void refreshLoginState() {
        loginPanel.refreshLoginState();
        boolean loginPage = controller.state() == LoginController.State.LOGIN
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

    private void loadCharacters(top.totodile.pokemmo.server.login.LoginService.Selection selection) {
        if (gameService == null) return;
        long request = ++characterLoadVersion;
        characterPage.beginLoading();
        gameService.characters(selection).thenCompose(characters->{
            var parties=characters.stream().map(character->gameService.pokemon(selection,character.id()).handle((party,error)->{
                if(error!=null){Gdx.app.error("game","Cannot load character party",error);return top.totodile.pokemmo.server.game.PokemonData.Containers.empty();}
                return party;
            }).toCompletableFuture()).toList();
            return java.util.concurrent.CompletableFuture.allOf(parties.toArray(java.util.concurrent.CompletableFuture[]::new))
                    .thenApply(ignored->java.util.stream.IntStream.range(0,characters.size())
                            .mapToObj(i->new CharacterRoster(characters.get(i),parties.get(i).join())).toList());
        }).whenComplete((rosters, error) -> Gdx.app.postRunnable(() -> {
            if (disposed || selection != controller.selection() || request != characterLoadVersion) return;
            if (error != null) {
                characterPage.loadFailed();
                Gdx.app.error("game", "Cannot load local characters", error);
            } else {
                characterPage.characters(rosters.stream().map(CharacterRoster::character).toList(), strings, mobile, renderer, characterCreation::savedAppearance);
                for(var roster:rosters)characterPage.party(roster.character().id(),roster.party(),pokemon::characterPartyIcon,pokemon::characterPartyName);
            }
            if (!characterCreation.isOpen()) {
                // 不把上一个页面遗留的鼠标/触点位置解释为新按钮的悬停。
                gui.handleMouse(-1, -1, -1, false);
                restoreWindowFocus();
            }
        }));
    }
    private record CharacterRoster(top.totodile.pokemmo.server.game.GameService.Character character,
                                   top.totodile.pokemmo.server.game.PokemonData.Containers party){}
    private void enterWorld(top.totodile.pokemmo.server.game.GameService.Character character) {
        var selection=controller.selection();
        if(disposed || enteringWorld || worldEntry==null || gameService==null || selection==null) return;
        enteringWorld=true;
        characterPage.beginLoading();
        java.util.concurrent.CompletionStage<top.totodile.pokemmo.server.game.GameService.Entry> operation;
        try { operation=gameService.enter(selection,character.id()); }
        catch(RuntimeException failure) { worldEntryFailed(selection,failure); return; }
        operation.whenComplete((entry,error) -> Gdx.app.postRunnable(() -> {
            if(disposed || selection!=controller.selection()) { enteringWorld=false; return; }
            if(error!=null) { worldEntryFailed(selection,error); return; }
            try {
                playerEntry=entry;
                worldEntry.apply(entry).whenComplete((ignored,failure) -> Gdx.app.postRunnable(() -> {
                    if(disposed) return;
                    enteringWorld=false;
                    if(failure!=null && selection==controller.selection()) worldEntryFailed(selection,failure);
                }));
            } catch(RuntimeException failure) { worldEntryFailed(selection,failure); }
        }));
    }
    private void worldEntryFailed(top.totodile.pokemmo.server.login.LoginService.Selection selection,Throwable failure) {
        enteringWorld=false;
        Throwable cause=failure;
        while((cause instanceof java.util.concurrent.CompletionException || cause instanceof java.util.concurrent.ExecutionException)
                && cause.getCause()!=null)cause=cause.getCause();
        boolean missingRom=cause instanceof top.totodile.pokemmo.client.rom.MissingRomException;
        if(!missingRom)Gdx.app.error("world","Cannot enter local character",failure);
        loadCharacters(selection);
        entryError.show(strings.get(missingRom?6805:2103));
    }
    private void createCharacter() {
        var selection = controller.selection();
        if (gameService == null || creatingCharacter || selection == null || !characterCreation.isOpen()) return;
        creatingCharacter = true;
        long version = characterCreation.version();
        gameService.create(selection, characterCreation.characterName(), characterCreation.selectedAppearance())
                .whenComplete((character, error) -> Gdx.app.postRunnable(() -> {
                    if (disposed) return;
                    creatingCharacter = false;
                    if (selection != controller.selection()) return;
                    if (error == null) {
                        loadCharacters(selection);
                        if (version == characterCreation.version()) characterCreation.closePopup();
                    } else if (version == characterCreation.version()) {
                        Throwable cause = error;
                        while (cause instanceof java.util.concurrent.CompletionException && cause.getCause() != null) cause = cause.getCause();
                        int id = cause instanceof top.totodile.pokemmo.server.game.GameService.Rejected rejected
                                ? switch (rejected.reason) {
                                    case INVALID_NAME -> 2101;
                                    case NAME_TAKEN -> 2102;
                                    case ACCOUNT_FULL -> 2100;
                                    default -> 2103;
                                } : 2103;
                        characterCreation.creationError(id);
                        if (id == 2103) Gdx.app.error("game", "Cannot create local character", cause);
                    }
                }));
    }

    private void deleteCharacter(top.totodile.pokemmo.server.game.GameService.Character character) {
        var selection = controller.selection();
        if (gameService == null || selection == null) return;
        long version = characterDelete.version();
        gameService.delete(selection, character.id()).whenComplete((ignored,error) -> Gdx.app.postRunnable(() -> {
            if (disposed || selection != controller.selection()) return;
            if (error == null) {
                if (version == characterDelete.version()) characterDelete.closePopup();
                loadCharacters(selection);
            } else if (version == characterDelete.version()) {
                characterDelete.failure(2103); Gdx.app.error("game","Cannot delete local character",error);
            }
        }));
    }

    public void draw(float width,float height,float x,float y) {
        loginPanel.updateNotifications();
        if(worldActions!=null) {
            repeatPlayerShortcut();
            var dexState=worldScene.pokedex().pokedexState();
            if(!dexState.equals(displayedPokedexState)){displayedPokedexState=dexState;pokedex.setState(dexState);trainerCard.reloadIfOpen();}
            worldActions.update();
            worldHud.update();
            worldScene.setInputBlocked(worldInputBlocked());
            fps.setVisible(Boolean.parseBoolean(resources.preferences().get("client.graphics.show_fps","true")));
            fps.setText(frameRate.frame());
            if(renderer.updateFontScale()) invalidateFontLayout(gui);
            pokedex.positionWindow();inventory.positionWindow();trainerCard.positionWindow();mail.positionWindow();pcMenu.positionWindow();storage.positionWindow();pokemonSummary.positionWindow();worldTools.positionWindow();gui.update();settings.positionWindow();worldLogout.positionWindow();
            if(exitConfirmation!=null)exitConfirmation.positionWindow();return;
        }
        fps.setVisible(Boolean.parseBoolean(resources.preferences().get("client.graphics.show_fps","true")));
        if (mobileStatus != null) mobileStatus.update(System.nanoTime());
        if (mobileMenu != null) mobileMenu.update();
        fps.setText(frameRate.frame());
        if (renderer.updateFontScale()) invalidateFontLayout(gui);
        int nextX=Math.round(x),nextY=Math.round(y);
        if(nextX!=offsetX || nextY!=offsetY){offsetX=nextX;offsetY=nextY;root.invalidateLayout();}
        gui.update();
        clientManagement.positionWindow();
        mods.positionWindow();
        settings.positionWindow();
        characterCreation.positionWindow();characterCustomization.positionWindow();
        characterDelete.positionWindow();
        entryError.positionWindow();
        if (exitConfirmation != null) exitConfirmation.positionWindow();
    }
    private static void invalidateFontLayout(Widget widget) {
        if (widget instanceof TextWidget text) text.setFont(text.getFont());
        for (int index = 0; index < widget.getNumChildren(); index++) invalidateFontLayout(widget.getChild(index));
        widget.invalidateLayout();
    }
    @Override public void dispose() {
        clientManagement.dispose();
        mods.dispose();
        inventory.closePopup();trainerCard.close();mail.close();pcMenu.closePopup();storage.forceClose();pokemonSummary.closePopup();worldTools.forceClose();pokemon.close();inventoryItems.close();
        pokedex.close();
        disposed = true;
        characterDelete.closePopup();
        characterPage.clearCards();
        loginPanel.dispose();
        controller.close();
        characterCreation.dispose();characterCustomization.dispose();
        if(Gdx.input.getInputProcessor()==input)Gdx.input.setInputProcessor(null);
        gui.destroy();theme.destroy();renderer.close();
        if (ownsResources) resources.close();
    }
}
