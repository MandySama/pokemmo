package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.protocol.EncounterCatalog;
import top.totodile.pokemmo.protocol.BattlePackets;
import top.totodile.pokemmo.*;
import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.battle.WildEncounter;
import top.totodile.pokemmo.battle.MovePresentation;
import top.totodile.pokemmo.battle.MoveAnimations;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import top.totodile.pokemmo.protocol.CharacterPackets.Appearance;
import top.totodile.pokemmo.protocol.PartyMember;
import top.totodile.pokemmo.protocol.ItemCatalog;
import top.totodile.pokemmo.protocol.PokemonCatalog;
import top.totodile.pokemmo.rom.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** 单次野生遭遇的展示层，纹理上传与释放均在 GUI 线程。 */
final class WildEncounterPanel extends Widget implements AutoCloseable {
    private java.util.function.Function<BattlePackets.Request, CompletableFuture<BattlePackets.State>> battleTransport;
    private Consumer<List<PartyMember>> partyChanged = list -> { };
    private CompletableFuture<BattlePackets.State> pendingBattle;
    private BattlePackets.State battleState, resultToApply;
    private final java.util.ArrayDeque<BattlePackets.Event> serverEvents = new java.util.ArrayDeque<>();
    private long eventUntil;
    private String serverMessage = "";
    private boolean enemyCasting, selectingServerMove;
    private int serverMoveSlot = 1;
    private int enemySlot, lastOutcome;
    int lastOutcome() { return lastOutcome; }

    void connectBattle(java.util.function.Function<BattlePackets.Request, CompletableFuture<BattlePackets.State>> transport,
                       Consumer<List<PartyMember>> listener) {
        battleTransport = transport; partyChanged = listener;
        pendingBattle = transport.apply(new BattlePackets.Request(BattlePackets.START,0,0,0));
    }
    private void submitBattle(int action, int slot) {
        if(pendingBattle!=null || resultToApply!=null || battleState==null || battleState.outcome()!=0)return;
        pendingBattle=battleTransport.apply(new BattlePackets.Request(action,battleState.battle(),battleState.revision(),slot));
    }
    private void configureServerMoves() {
        if(battleState==null || self==null)return;
        selectingServerMove=true;
        var labels=new java.util.ArrayList<String>();
        for(var option:battleState.options()) {
            String title=ClientStrings.get(option.id()==0?9832:110000+option.id());
            labels.add(title+"  "+ClientStrings.get(9357)+" "+option.pp()+" / "+option.maxPp());
        }
        testMove.setModel(new SimpleChangableListModel<>(labels.toArray(String[]::new)));
        if(!labels.isEmpty()){testMove.setSelected(0);serverMoveSlot=battleState.legalMoves().getFirst();}
        move.setText(ClientStrings.get(9820));testMoveLabel.setText(ClientStrings.get(9821));
        selectingServerMove=false;
    }
    private void tickBattle() {
        if(battleTransport==null)return;
        if(pendingBattle!=null && pendingBattle.isDone()) {
            try {
                var result=pendingBattle.join();pendingBattle=null;
                resultToApply=result;serverEvents.clear();serverEvents.addAll(result.events());eventUntil=now;
            }catch(RuntimeException error){pendingBattle=null;close();finished.run();failure.accept(ClientStrings.get(9822));return;}
        }
        if(pendingMove!=null || pendingSwitch!=null || pendingEnemy!=null || resultToApply==null || encounter==null || encounter.update(now).ordinal()<WildEncounter.Phase.READY.ordinal() || now<eventUntil)return;
        if(!serverEvents.isEmpty()) {
            var event=serverEvents.removeFirst();enemyCasting=event.side()==1;
            String actor=name(enemyCasting?enemy:self);
            if(event.kind()==5) {
                if(event.side()==1 && trainerBattle && event.value()!=enemySlot && event.value()<scriptedOpponents.size()) {
                    enemySlot=event.value();loadEnemy(scriptedOpponents.get(enemySlot));eventUntil=now+600;return;
                }
                if(event.side()==0 && event.value()!=encounter.activeSlot()) {
                    encounter.serverParty(members.stream().mapToInt(PartyMember::hp).toArray(),true);
                    loadParty(event.value());eventUntil=now+600;
                }
                return;
            }
            if(event.kind()==4) {
                if(enemyCasting) enemy=new PartyMember(enemy.species(),enemy.shiny(),enemy.alpha(),Math.min(event.value(),enemy.maxHp()),enemy.maxHp(),enemy.level());
                else updateSelf(new PartyMember(self.species(),self.shiny(),self.alpha(),Math.min(event.value(),self.maxHp()),self.maxHp(),self.level(),self.individual()));
                eventUntil=now+350;return;
            }
            if(event.kind()==0) {
                if(pendingMove!=null)pendingMove.cancel(false);pendingMove=null;
                clearMovePresentation();
                if(MoveAnimations.supports(event.value())) {
                    int moveId=event.value();Path rom=battleRom;boolean reverse=enemyCasting;
                    pendingMove=CompletableFuture.supplyAsync(()->{
                        try{return BattleParticles.load(rom,moveId,reverse);}catch(IOException error){throw new java.util.concurrent.CompletionException(error);}
                    });
                }
                encounter.playServerMove(event.value(),now);
                serverMessage=ClientStrings.get(252005).replace("{00}",actor).replace("{01}",ClientStrings.get(110000+event.value()));
                eventUntil=now+MoveAnimations.playback(encounter.moveId()).duration();
            }else {
                serverMessage=ClientStrings.get(event.kind()==1?9823:event.kind()==2?9824:9825).replace("{00}",actor);
                eventUntil=now+800;
            }
            return;
        }
        var result=resultToApply;resultToApply=null;battleState=result;members=result.party();
        if(result.active()!=encounter.activeSlot()) {
            encounter.serverParty(members.stream().mapToInt(PartyMember::hp).toArray(),true);
            loadParty(result.active());
        } else updateSelf(members.get(result.active()));
        if(trainerBattle && result.enemyActive()!=enemySlot && result.enemyActive()<scriptedOpponents.size()) {
            enemySlot=result.enemyActive();loadEnemy(scriptedOpponents.get(enemySlot));
        }
        enemy=new PartyMember(enemy.species(),enemy.shiny(),enemy.alpha(),result.enemyHp(),result.enemyMaxHp(),enemy.level());
        partyChanged.accept(members);enemyCasting=false;
        if(pendingSwitch==null) encounter.serverParty(members.stream().mapToInt(PartyMember::hp).toArray(),result.forceSwitch());
        configureServerMoves();
        if(result.outcome()!=0) {
            serverMessage=ClientStrings.get(result.outcome()==1?9826:result.outcome()==2?9827:result.outcome()==4?9828:result.outcome()==5?9822:9829);
            eventUntil=now+1800;
        } else if(!result.accepted()) serverMessage=ClientStrings.get(9830);
        else serverMessage="";
    }

    private record Pixels(List<BufferedImage> front, List<BufferedImage> back,
                          List<BufferedImage> trainer, BufferedImage ball, BufferedImage party, BattleScene.Resources scene, List<List<BufferedImage>> horde, List<BattleParticles.Definition> shiny) { }

    private record SwitchPixels(PartyMember member, List<BufferedImage> frames) { }
    private final PartyChoiceWindow partyChoice = new PartyChoiceWindow();
    private List<PartyMember> members = List.of();
    private Path battleRom;
    private CompletableFuture<SwitchPixels> pendingSwitch;
    private long switchRequested;
    private Texture backTexture, frontTexture;
    private record EnemyPixels(PartyMember member, List<BufferedImage> frames) { }
    private CompletableFuture<EnemyPixels> pendingEnemy;
    private long enemyRequested;
    private List<PartyMember> scriptedOpponents = List.of();
    private boolean scripted, trainerBattle;
    private boolean escapeLocked;
    void setEscapeLocked(boolean locked) { escapeLocked=locked; }
    private final ComboBox<String> opponentChoice = new ComboBox<>(new SimpleChangableListModel<>());
    private long backStarted;
    private Runnable openBag = () -> { };
    private final Button[] actions = new Button[4];
    private final Button move = new Button(ClientStrings.get(110033));
    private final Button moveBack = new Button(ClientStrings.get(58));
    private final List<Integer> testMoveIds = MoveAnimations.ids();
    private final ComboBox<String> testMove = new ComboBox<>(new SimpleChangableListModel<>(testMoveIds.stream().map(id -> id + "  " + ClientStrings.get(110000+id)).toArray(String[]::new))) {
        @Override protected void setPopupSize() {
            ListBox<?> list = (ListBox<?>)popup.getChild(0);
            int height = Math.min(8,list.getNumEntries())*list.getCellHeight()+list.getBorderVertical();
            popup.setMinSize(popup.getMinWidth(),height);popup.setMaxSize(popup.getMaxWidth(),height);super.setPopupSize();
        }
    };
    private final Label testMoveLabel = new Label(ClientStrings.get(252009));
    private int selectedMove = MovePresentation.TACKLE;
    private CompletableFuture<List<BattleParticles.Definition>> pendingMove;
    private BattleParticles moveParticles;
    private OriginalActorAnimation moveActors;
    private boolean moveActorsReversed;
    private final List<Texture> moveTextures = new ArrayList<>();
    private long cameraTime, frozenCameraTime, cameraPaused;
    private boolean cameraFrozen;
    private final Label enemyName = new Label(), selfName = new Label(), hp = new Label(), message = new Label();
    private final Consumer<String> failure;
    private final Runnable finished;
    private final List<Texture> textures = new ArrayList<>();
    private CompletableFuture<Pixels> pending;
    private WildEncounter encounter;
    private BattleScene scene;
    private PartyMember self, enemy;
    private List<PartyMember> wildOpponents = List.of();
    private record HordeSprite(Image[] frames, int bottom, float center, float zoom) { }
    private List<HordeSprite> hordeSprites = List.of();
    private final Label[] hordeNames = new Label[5];
    private final GenderLabel[] hordeGenders = new GenderLabel[5];
    private Image hordeHud;
    private float hordePlatformScale = 1.2f;
    private int hordeHudWidth = 239, hordeHudGap = 12, hordeHudTop = 86, hordeRowGap = 56;
    private int hordeCenter = 740, hordeSpacing = 50, hordeThreeSpacing = 75;
    private BattleParticles shinyParticles;
    private Image[] front, back, trainer, party;
    private Image ball, releaseGlow, white, enemyHud, selfHud, healthImage, healthBackground;
    private Image experienceBackground, experienceImage;
    private float experienceFraction;
    private int actionWidth = 209, actionHeight = 52, actionGap = 2;
    private int footerTop = 474, footerHeight = 116;
    private int healthWidth = 200, enemyHealthX = 70, selfHealthX = 80;
    private int partyIconSize = 36, partyIconGap = 4;
    private int frontBottom, backBottom;
    private float frontCenter, backCenter, frontZoom, backZoom;
    private float gigantamaxFrontScale = 1, gigantamaxBackScale = 1;
    private float gigantamaxCameraVerticalOffset, gigantamaxCameraBlend, gigantamaxCameraFieldOfView = 50;
    private int gigantamaxCameraTransitionMillis = 600;
    private long gigantamaxCameraTime;
    private boolean gigantamaxCameraRaised;
    private float backdropOpacity = .68f;
    private final GenderLabel enemyGender = new GenderLabel(), selfGender = new GenderLabel();
    private long now;
    private float scale;
    private int left, top;
    private int enemyHudX, enemyHudY, selfHudX, selfHudY;
    private long requested;

    WildEncounterPanel(Runnable finished, Consumer<String> failure) {
        this.finished = finished;
        this.failure = failure;
        setTheme("/wild-encounter");
        setClip(true);
        int[] titles = {5100, 5104, 0, 5106};
        int[] descriptions = {5101, 5105, 5103, 5107};
        for (int i = 0; i < actions.length; i++) {
            final int index = i;
            actions[i] = new ActionButton(ClientStrings.get(titles[i]), ClientStrings.get(descriptions[i]));
            actions[i].setTheme("action");
            actions[i].addCallback(() -> {
                if (encounter != null) {
                    if(battleTransport!=null && index==3){submitBattle(BattlePackets.RUN,0);return;}
                    encounter.choose(WildEncounter.Action.values()[index], now);
                    if (encounter.update(now) == WildEncounter.Phase.BAG) openBag.run();
                    if (battleTransport == null && index == 0 && encounter.update(now) == WildEncounter.Phase.SELECT_MOVE && moveParticles == null && pendingMove == null) replaceTestMove(selectedMove);
                }
            });
            add(actions[i]);
        }
        move.setTheme("move");
        moveBack.setTheme("move");
        move.addCallback(() -> { if (battleTransport!=null) submitBattle(BattlePackets.MOVE,serverMoveSlot); else if (encounter != null) encounter.chooseMove(selectedMove, now); });
        moveBack.addCallback(() -> { if (encounter != null) encounter.cancelMove(now); });
        testMove.setTheme("test-move");testMove.setTooltipContent(ClientStrings.get(252009));
        testMove.setSelected(testMoveIds.indexOf(selectedMove));
        testMove.addCallback(() -> {
            int index=testMove.getSelected();
            if(battleTransport!=null) {
                if(!selectingServerMove && battleState!=null && index>=0 && index<battleState.legalMoves().size())serverMoveSlot=battleState.legalMoves().get(index);
                return;
            }
            if (index>=0) replaceTestMove(testMoveIds.get(index));
        });
        add(move); add(moveBack); add(testMove); add(testMoveLabel); testMoveLabel.setTheme("test-label");testMoveLabel.setVisible(false);testMove.setVisible(false);
        move.setVisible(false); moveBack.setVisible(false);
        for (Label label : new Label[]{enemyName, selfName, hp, message}) { label.setTheme("label"); add(label); }
        enemyName.setTheme("name"); selfName.setTheme("name"); hp.setTheme("name"); message.setTheme("message");
        add(enemyGender); add(selfGender);
        for (int i = 0; i < hordeNames.length; i++) {
            hordeNames[i] = new Label(); hordeNames[i].setTheme("horde-name");
            hordeNames[i].setVisible(false); add(hordeNames[i]);
            hordeGenders[i] = new GenderLabel(); hordeGenders[i].setVisible(false); add(hordeGenders[i]);
        }
        add(partyChoice);
        opponentChoice.setTheme("test-move");
        opponentChoice.addCallback(this::chooseEnemy);
        opponentChoice.setVisible(false);
        add(opponentChoice);
        setVisible(false);
    }

    void replaceTestMove(int id) {
        if (!MoveAnimations.supports(id) || encounter == null || encounter.update(now) != WildEncounter.Phase.SELECT_MOVE) return;
        if (pendingMove != null) pendingMove.cancel(false);
        pendingMove = null;
        clearMovePresentation();
        selectedMove = id;
        move.setText(ClientStrings.get(110000 + id));
        Path rom = battleRom;
        boolean reverse = enemyCasting;
        pendingMove = CompletableFuture.supplyAsync(() -> {
            try { return BattleParticles.load(rom, id, reverse); }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
        move.setEnabled(false);
    }

    private void clearMovePresentation() {
        for (var texture : moveTextures) texture.destroy();
        moveTextures.clear();
        if (moveParticles != null) moveParticles.close();
        moveParticles = null;
        if (moveActors != null) moveActors.close();
        moveActors = null;
    }

    boolean active() { return encounter != null; }

    void setBagAction(Runnable action) { openBag = action; }
    void closeBag() { if (encounter != null) encounter.closeBag(now); }
    boolean bagOpen() { return encounter != null && encounter.update(now) == WildEncounter.Phase.BAG; }

    private void chooseParty(int slot) {
        if(battleTransport!=null) {
            if(slot<0||slot>=members.size()||slot==encounter.activeSlot()||members.get(slot).hp()==0)return;
            submitBattle(BattlePackets.SWITCH,slot);return;
        }
        loadParty(slot);
    }
    private void loadParty(int slot) {
        if (encounter == null || pendingSwitch != null || !encounter.selectParty(slot, now)) return;
        PartyMember selected = members.get(slot);
        Path rom = battleRom;
        switchRequested = now;
        pendingSwitch = CompletableFuture.supplyAsync(() -> {
            try {
                List<BufferedImage> frames;
                try { frames = animation(rom, selected, true, new PokedexData(rom)); }
                catch (IOException | RuntimeException unsupported) { frames = List.of(new PokedexData(rom).sprite(selected.species(), true, selected.shiny())); }
                return new SwitchPixels(selected, frames);
            }
            catch (IOException exception) { throw new java.util.concurrent.CompletionException(exception); }
        });
    }

    private void updateSelf(PartyMember member) {
        self = member;
        selfName.setText(name(self) + "  Lv. " + self.level());
        hp.setText(self.hp() + " / " + self.maxHp());
        setGender(selfGender, self.individual() == null ? 2 : self.individual().gender());
        experienceFraction = 0;
        if (self.individual() != null && self.level() < 100) {
            int growth = PokemonCatalog.get(self.species()).growth();
            long min = PokemonCatalog.experience(growth, self.level());
            long next = PokemonCatalog.experience(growth, self.level()+1);
            experienceFraction = Math.max(0, Math.min(1, (self.individual().experience()-min)/(float)(next-min)));
        }
    }

    void start(Path rom, AvatarAssets assets, Appearance appearance, List<PartyMember> members, long milliseconds) {
        start(rom, assets, appearance, members, List.of(), false, milliseconds, null);
    }

    void startScripted(Path rom, AvatarAssets assets, Appearance appearance, List<PartyMember> members,
                       List<PartyMember> opponents, boolean trainer, long milliseconds) {
        if (opponents.isEmpty()) throw new IllegalArgumentException("Empty scripted opponent team");
        start(rom, assets, appearance, members, opponents, trainer, milliseconds, null);
    }

    void startWild(Path rom, AvatarAssets assets, Appearance appearance, List<PartyMember> members,
                   EncounterCatalog.Selection selected, long milliseconds) {
        if (active()) return;
        wildOpponents = selected.selections().stream().map(EncounterCatalog.Selection::member).toList();
        start(rom, assets, appearance, members, List.of(), false, milliseconds, selected.member());
    }

    boolean readyForOutcome() {
        return encounter != null && encounter.update(now) == WildEncounter.Phase.READY && pendingEnemy == null;
    }

    private void start(Path rom, AvatarAssets assets, Appearance appearance, List<PartyMember> members,
                       List<PartyMember> opponents, boolean trainerBattle, long milliseconds, PartyMember selectedWild) {
        if (active()) return;
        lastOutcome=0;enemySlot=0;
        self = members.stream().filter(member -> member.hp() > 0).findFirst().orElse(null);
        if (self == null) { failure.accept(ClientStrings.get(252004)); return; }
        var random = java.util.concurrent.ThreadLocalRandom.current();
        if (opponents.isEmpty() && selectedWild == null) { failure.accept(ClientStrings.get(9732)); return; }
        scripted = !opponents.isEmpty();
        this.trainerBattle = trainerBattle;
        scriptedOpponents = List.copyOf(opponents);
        enemy = scripted ? scriptedOpponents.getFirst() : selectedWild;
        opponentChoice.setModel(new SimpleChangableListModel<>(scriptedOpponents.stream()
                .map(member -> name(member) + " Lv." + member.level()).toArray(String[]::new)));
        if (scripted) opponentChoice.setSelected(0);
        ((ActionButton) actions[3]).setLabels(scripted ? 9553 : 5106, scripted ? 9554 : 5107);
        actions[1].setTooltipContent(scripted ? ClientStrings.get(9556) : null);
        int ratio = PokemonCatalog.get(enemy.species()).gender();
        setGender(enemyGender, ratio == 255 ? 2 : ratio == 254 ? 1 : ratio == 0 ? 0 : random.nextInt(256) < ratio ? 1 : 0);
        setGender(selfGender, self.individual() == null ? 2 : self.individual().gender());
        experienceFraction = 0;
        if (self.individual() != null && self.level() < 100) {
            int growth = PokemonCatalog.get(self.species()).growth();
            long min = PokemonCatalog.experience(growth, self.level());
            long next = PokemonCatalog.experience(growth, self.level()+1);
            experienceFraction = Math.max(0, Math.min(1, (self.individual().experience()-min)/(float)(next-min)));
        }
        PartyMember selected = self;
        PartyMember wild = enemy;
        List<PartyMember> wildSnapshot = wildOpponents;
        for (int i = 0; i < hordeNames.length; i++) {
            if (i < wildSnapshot.size()) {
                var opponent = wildSnapshot.get(i);
                hordeNames[i].setText(name(opponent) + " Lv. " + opponent.level());
                int genderRatio = PokemonCatalog.get(opponent.species()).gender();
                setGender(hordeGenders[i], genderRatio == 255 ? 2 : genderRatio == 254 ? 1 : genderRatio == 0 ? 0 : random.nextInt(256) < genderRatio ? 1 : 0);
            }
        }
        List<PartyMember> snapshot = List.copyOf(members);
        this.members = snapshot;
        battleRom = rom;
        backStarted = 3600;
        cameraFrozen = false;
        cameraTime = frozenCameraTime = cameraPaused = 0;
        requested = milliseconds;
        encounter = new WildEncounter();
        encounter.configureParty(snapshot.stream().mapToInt(PartyMember::hp).toArray(), snapshot.indexOf(self));
        setVisible(true);
        enemyName.setText(name(enemy) + "  Lv. " + enemy.level());
        selfName.setText(name(self) + "  Lv. " + self.level());
        hp.setText(self.hp() + " / " + self.maxHp());
        message.setText("");
        message.setVisible(false);
        for (Button button : actions) button.setVisible(false);
        move.setVisible(false); moveBack.setVisible(false);
        pending = CompletableFuture.supplyAsync(() -> {
            try {
                if (rom == null) throw new IOException("Missing Black/White ROM");
                PokedexData data = new PokedexData(rom);
                List<BufferedImage> frontFrames = animation(rom, wild, false, data);
                List<BufferedImage> backFrames = animation(rom, selected, true, data);
                AvatarAssets.Item[] items = new AvatarAssets.Item[12];
                int[] ids = appearance.items();
                for (int category = 1; category < items.length; category++) {
                    for (var item : assets.choices(category, appearance.gender(), true)) {
                        if (item.id == ids[category]) { items[category] = item; break; }
                    }
                }
                List<BufferedImage> throwsFrames = new ArrayList<>();
                // 外观包 view 1 的五帧对应背面投球动作。
                for (int frame = 0; frame < 5; frame++)
                    throwsFrames.add(assets.render(appearance.skin(), items, appearance.colors(), 1, frame));
                var iconSource = new ItemIconSources(new RomFiles(ClientFiles.data("../roms")));
                int ballId = selected.individual() == null ? 5004 : selected.individual().ball();
                if (ballId > 0 && ballId < 5000) ballId += 5000;
                var ballItem = ItemCatalog.get(ballId);
                if (ballItem == null || ballId < 5001) ballItem = ItemCatalog.get(5004);
                List<List<BufferedImage>> hordeFrames = new ArrayList<>();
                if (wildSnapshot.size() > 1) for (var opponent : wildSnapshot)
                    hordeFrames.add(animation(rom, opponent, false, data));
                var shiny = BattleParticles.loadShiny(rom);
                return new Pixels(frontFrames, backFrames, throwsFrames, iconSource.image(ballItem),
                        PartyIcons.loadStrip(rom, snapshot, 0, 0, 0, 1.25f), BattleScene.load(rom), hordeFrames, shiny);
            } catch (IOException exception) { throw new java.util.concurrent.CompletionException(exception); }
        });
        invalidateLayout();
    }

    private void chooseEnemy() {
        int index = opponentChoice.getSelected();
        if (!readyForOutcome() || index < 0 || index >= scriptedOpponents.size()
                || scriptedOpponents.get(index) == enemy) return;
        PartyMember selected = scriptedOpponents.get(index);
        loadEnemy(selected);
    }

    private void loadEnemy(PartyMember selected) {
        Path rom = battleRom;
        enemyRequested = now;
        pendingEnemy = CompletableFuture.supplyAsync(() -> {
            try { return new EnemyPixels(selected, animation(rom, selected, false, new PokedexData(rom))); }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    private static List<BufferedImage> animation(Path rom, PartyMember member, boolean back, PokedexData data) throws IOException {
        try {
            var costume = PokemonCostumes.battleImage(member);
            if (costume != null) return List.of(costume);
            if (PokemonForms.active(member)) {
                var form = PokemonForms.load(member);
                return back ? form.back() : form.front();
            }
            return PokemonAnimation.loadBattle(rom, member.species(), member.shiny(), back).images();
        }
        catch (IOException | RuntimeException unsupported) { return List.of(data.sprite(member.species(), back, member.shiny())); }
    }

    private float formScale(PartyMember member, boolean back) {
        if (!PokemonCostumes.selected(member).isEmpty()) return 1;
        if (!PokemonForms.active(member) || member.individual().form() != 3) return 1;
        return back ? gigantamaxBackScale : gigantamaxFrontScale;
    }

    private static boolean gigantamax(PartyMember member) {
        return member != null && PokemonForms.active(member) && member.individual().form() == 3;
    }

    private void updateGigantamaxCamera(long elapsed, WildEncounter.Phase phase) {
        boolean visible = elapsed >= 2000 && (gigantamax(enemy) || wildOpponents.stream().anyMatch(WildEncounterPanel::gigantamax))
                || elapsed >= 3600 && phase != WildEncounter.Phase.SWITCH_LOADING && gigantamax(self);
        float target = visible ? 1 : 0;
        if (visible != gigantamaxCameraRaised) {
            gigantamaxCameraRaised = visible;
            gigantamaxCameraTime = now;
        }
        float step = Math.max(0, now - gigantamaxCameraTime) / (float) gigantamaxCameraTransitionMillis;
        gigantamaxCameraTime = now;
        gigantamaxCameraBlend += Math.clamp(target - gigantamaxCameraBlend, -step, step);
        scene.setGigantamaxFraming(gigantamaxCameraFieldOfView, gigantamaxCameraVerticalOffset, gigantamaxCameraBlend);
    }

    private static String name(PartyMember member) {
        return member.individual() != null && !member.individual().nickname().isBlank()
                ? member.individual().nickname() : ClientStrings.get(150000 + member.species());
    }

    private static void setGender(GenderLabel label, int gender) {
        label.value = gender;
    }

    void tick(long milliseconds) {
        now = milliseconds;
        if (encounter == null) return;
        if (pendingMove != null && pendingMove.isDone()) {
            var result=pendingMove;pendingMove=null;
            try {
                var definitions=result.join();List<Image> images=new ArrayList<>();
                var renderer=(GlfwTwlRenderer)getGUI().getRenderer();
                var uploaded = new java.util.IdentityHashMap<BufferedImage, Texture>();
                for(var definition:definitions) for (var pixels : definition.textures()) {
                    var texture = uploaded.get(pixels);
                    if (texture == null) { texture=renderer.uploadImage(pixels);moveTextures.add(texture);uploaded.put(pixels,texture); }
                    images.add(texture.getImage(0,0,pixels.getWidth(),pixels.getHeight(),Color.WHITE,false,Texture.Rotation.NONE));
                }
                moveParticles=new BattleParticles(definitions,images);move.setEnabled(true);
                if(battleTransport!=null && resultToApply!=null && encounter.update(now)==WildEncounter.Phase.MOVE) {
                    encounter.playServerMove(encounter.moveId(),now);eventUntil=now+MoveAnimations.playback(encounter.moveId()).duration();
                }
            } catch(RuntimeException error) {
                clearMovePresentation();
                move.setEnabled(false);failure.accept(ClientStrings.get(252003));
            }
        }
        if (pending != null && pending.isDone()) {
            try {
                Pixels pixels = pending.join();
                pending = null;
                GlfwTwlRenderer renderer = (GlfwTwlRenderer) getGUI().getRenderer();
                front = uploadFrames(renderer, pixels.front());
                frontTexture = textures.getLast();
                back = uploadFrames(renderer, pixels.back());
                backTexture = textures.getLast();
                trainer = uploadFrames(renderer, pixels.trainer());
                var frontBounds = bounds(pixels.front()); var backBounds = bounds(pixels.back());
                frontBottom = frontBounds[3]; backBottom = backBounds[3];
                frontCenter = (frontBounds[0] + frontBounds[2]) / 2f;
                backCenter = (backBounds[0] + backBounds[2]) / 2f;
                frontZoom = Math.min(1.9f, Math.min(180f / Math.max(1, frontBounds[2]-frontBounds[0]), 130f / Math.max(1, frontBounds[3]-frontBounds[1])));
                backZoom = Math.min(self.alpha() ? 4.3f : 3.6f, Math.min(360f / Math.max(1, backBounds[2]-backBounds[0]), 260f / Math.max(1, backBounds[3]-backBounds[1])));
                ball = upload(renderer, pixels.ball());
                Texture partyTexture = renderer.uploadImage(pixels.party()); textures.add(partyTexture);
                party = new Image[pixels.party().getWidth() / PartyIcons.TILE_SIZE];
                for (int i = 0; i < party.length; i++) party[i] = partyTexture.getImage(i * 42 + PartyIcons.PADDING, PartyIcons.PADDING, 32, 32, Color.WHITE, false, Texture.Rotation.NONE);
                scene = new BattleScene(pixels.scene());
                releaseGlow = upload(renderer, glowPixels());
                List<HordeSprite> nextHorde = new ArrayList<>();
                for (var frames : pixels.horde()) {
                    int[] box = bounds(frames);
                    nextHorde.add(new HordeSprite(uploadFrames(renderer, frames), box[3], (box[0]+box[2])/2f,
                            Math.min(1.5f, Math.min(105f/Math.max(1,box[2]-box[0]),105f/Math.max(1,box[3]-box[1])))));
                }
                hordeSprites = List.copyOf(nextHorde);
                List<Image> stars = new ArrayList<>();
                for (var definition : pixels.shiny()) for (var image : definition.textures()) stars.add(upload(renderer, image));
                shinyParticles = stars.isEmpty() ? null : new BattleParticles(pixels.shiny(), stars);
                encounter.loaded(now);
            } catch (RuntimeException error) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Unable to load encounter", error);
                close(); finished.run(); failure.accept(ClientStrings.get(252003)); return;
            }
        }
        if (pending != null && now - requested > 30000) {
            close(); finished.run(); failure.accept(ClientStrings.get(252003)); return;
        }
        if (pendingSwitch != null && (pendingSwitch.isDone() || now-switchRequested > 30000)) {
            try {
                if (!pendingSwitch.isDone()) throw new IllegalStateException("Battle switch timed out");
                SwitchPixels pixels = pendingSwitch.join();
                Image[] next = uploadFrames((GlfwTwlRenderer)getGUI().getRenderer(), pixels.frames());
                Texture previous = backTexture;
                backTexture = textures.getLast();
                back = next;
                int[] box = bounds(pixels.frames());
                backBottom = box[3]; backCenter = (box[0]+box[2])/2f;
                backZoom = Math.min(pixels.member().alpha() ? 4.3f : 3.6f,
                        Math.min(360f/Math.max(1,box[2]-box[0]),260f/Math.max(1,box[3]-box[1])));
                updateSelf(pixels.member());
                if(battleTransport!=null)configureServerMoves();
                encounter.switchLoaded(now);
                backStarted = encounter.elapsed(now);
                if (previous != null) { previous.destroy(); textures.remove(previous); }
            } catch (RuntimeException error) {
                pendingSwitch.cancel(true);
                encounter.switchFailed();
                failure.accept(ClientStrings.get(252003));
            }
            pendingSwitch = null;
        }
        if (pendingEnemy != null && (pendingEnemy.isDone() || now - enemyRequested > 30000)) {
            var result = pendingEnemy; pendingEnemy = null;
            try {
                if (!result.isDone()) { result.cancel(true); throw new IllegalStateException("Scripted opponent load timed out"); }
                var pixels = result.join();
                var previous = frontTexture;
                front = uploadFrames((GlfwTwlRenderer) getGUI().getRenderer(), pixels.frames());
                frontTexture = textures.getLast();
                enemy = pixels.member();
                int[] box = bounds(pixels.frames());
                frontBottom = box[3]; frontCenter = (box[0] + box[2]) / 2f;
                frontZoom = Math.min(1.9f, Math.min(180f / Math.max(1, box[2] - box[0]), 130f / Math.max(1, box[3] - box[1])));
                enemyName.setText(name(enemy) + "  Lv. " + enemy.level());
                setGender(enemyGender, enemy.individual() == null ? 2 : enemy.individual().gender());
                if (previous != null) { previous.destroy(); textures.remove(previous); }
            } catch (RuntimeException error) {
                close(); failure.accept(ClientStrings.get(252003)); return;
            }
        }
        tickBattle();
        if(encounter==null)return;
        if(battleState!=null && battleState.outcome()!=0 && resultToApply==null && now>=eventUntil && encounter.update(now)!=WildEncounter.Phase.ENDING && encounter.update(now)!=WildEncounter.Phase.CLOSED)encounter.finish(now);
        WildEncounter.Phase phase = encounter.update(now);
        if (phase == WildEncounter.Phase.CLOSED) { close(); finished.run(); return; }
        partyChoice.setHardVisible(phase == WildEncounter.Phase.SELECT_PARTY);
        partyChoice.refresh();
        boolean ready = phase == WildEncounter.Phase.READY;
        boolean wasReady = actions[0].isVisible();
        for (Button action : actions) { action.setVisible(ready); action.setEnabled(ready && pendingEnemy == null && pendingBattle==null && resultToApply==null && (battleState==null||battleState.outcome()==0)); }
        if (scripted || battleTransport!=null) actions[1].setEnabled(false);
        if((trainerBattle||escapeLocked) && battleTransport!=null)actions[3].setEnabled(false);
        opponentChoice.setVisible(battleTransport==null && scriptedOpponents.size() > 1 && phase.ordinal() >= WildEncounter.Phase.READY.ordinal());
        opponentChoice.setEnabled(ready && pendingEnemy == null);
        boolean selecting = phase == WildEncounter.Phase.SELECT_MOVE;
        boolean wasSelecting = move.isVisible();
        move.setEnabled(pendingMove==null && pendingBattle==null && resultToApply==null && (battleTransport==null||battleState!=null&&!battleState.legalMoves().isEmpty()));
        testMove.setEnabled(pendingBattle==null && resultToApply==null);
        move.setVisible(selecting); moveBack.setVisible(selecting); testMove.setVisible(selecting); testMoveLabel.setVisible(selecting);
        if (selecting && !wasSelecting) move.requestKeyboardFocus();
        if (ready && !wasReady) actions[0].requestKeyboardFocus();
        boolean horde = wildOpponents.size() > 1;
        for (int i = 0; i < hordeNames.length; i++) {
            boolean visible = horde && i < wildOpponents.size() && phase.ordinal() >= WildEncounter.Phase.ANNOUNCE.ordinal();
            hordeNames[i].setVisible(visible); hordeGenders[i].setVisible(visible);
        }
        enemyName.setVisible(!horde && phase.ordinal() >= WildEncounter.Phase.ANNOUNCE.ordinal());
        enemyGender.setVisible(enemyName.isVisible());
        boolean ownVisible = phase.ordinal() >= WildEncounter.Phase.RELEASE.ordinal();
        selfName.setVisible(ownVisible); hp.setVisible(ownVisible); selfGender.setVisible(ownVisible);
        message.setVisible(phase == WildEncounter.Phase.ANNOUNCE
                || phase == WildEncounter.Phase.THROW || phase == WildEncounter.Phase.RELEASE || phase == WildEncounter.Phase.MOVE || phase == WildEncounter.Phase.SWITCH_LOADING || phase == WildEncounter.Phase.SWITCHING);
        if (phase != WildEncounter.Phase.LOADING)
            message.setText(ClientStrings.get(phase.ordinal() >= WildEncounter.Phase.THROW.ordinal() ? 252001 : trainerBattle ? 9555 : 252000).replace("{00}", name(phase.ordinal() >= WildEncounter.Phase.THROW.ordinal() ? self : enemy)));
        if (phase == WildEncounter.Phase.ANNOUNCE && horde) message.setText(ClientStrings.get(9748));
        if (phase == WildEncounter.Phase.SWITCH_LOADING) message.setText(ClientStrings.get(252007).replace("{00}",name(self)));
        if (phase == WildEncounter.Phase.MOVE) message.setText(ClientStrings.get(252005)
                .replace("{00}", name(self)).replace("{01}", ClientStrings.get(110000+encounter.moveId())));
        if(battleTransport!=null && (pendingBattle!=null || !serverMessage.isEmpty())) {
            message.setVisible(true);message.setText(pendingBattle!=null?ClientStrings.get(9831):serverMessage);
        }
        invalidateLayout();
    }

    private Image[] uploadFrames(GlfwTwlRenderer renderer, List<BufferedImage> frames) {
        int cell = Math.max(96, Math.max(frames.getFirst().getWidth(), frames.getFirst().getHeight()));
        int columns = Math.max(1, (int) Math.ceil(Math.sqrt(frames.size())));
        int limit = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_MAX_TEXTURE_SIZE);
        if (columns * cell > limit || (frames.size()+columns-1)/columns*cell > limit)
            throw new IllegalArgumentException("Battle atlas exceeds texture limits");
        BufferedImage atlas = new BufferedImage(columns * cell, (frames.size() + columns - 1) / columns * cell, BufferedImage.TYPE_INT_ARGB);
        var graphics = atlas.createGraphics();
        for (int i = 0; i < frames.size(); i++) graphics.drawImage(frames.get(i), i % columns * cell, i / columns * cell, null);
        graphics.dispose();
        Texture texture = renderer.uploadImage(atlas); textures.add(texture);
        Image[] result = new Image[frames.size()];
        for (int i = 0; i < result.length; i++) result[i] = texture.getImage(i % columns * cell, i / columns * cell, frames.get(i).getWidth(), frames.get(i).getHeight(), Color.WHITE, false, Texture.Rotation.NONE);
        return result;
    }

    private Image upload(GlfwTwlRenderer renderer, BufferedImage pixels) {
        if (pixels == null) throw new IllegalArgumentException("Missing encounter image");
        Texture texture = renderer.uploadImage(pixels); textures.add(texture);
        return texture.getImage(0, 0, pixels.getWidth(), pixels.getHeight(), Color.WHITE, false, Texture.Rotation.NONE);
    }

    private static int[] bounds(List<BufferedImage> frames) {
        int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE, right = 0, bottom = 0;
        for (BufferedImage image : frames) for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) if ((image.getRGB(x, y) >>> 24) != 0) {
                left = Math.min(left, x); top = Math.min(top, y);
                right = Math.max(right, x + 1); bottom = Math.max(bottom, y + 1);
            }
        return left == Integer.MAX_VALUE ? new int[]{0,0,1,1} : new int[]{left,top,right,bottom};
    }

    private static BufferedImage glowPixels() {
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) {
            double distance = Math.hypot(x - 63.5, y - 63.5) / 64;
            int alpha = (int) (255 * Math.pow(Math.max(0, 1 - distance), 1.5));
            image.setRGB(x, y, alpha << 24 | 0x00ffffff);
        }
        return image;
    }

    @Override protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        white = theme.getImage("whiteImage"); enemyHud = theme.getImage("enemyHud");
        selfHud = theme.getImage("selfHud"); healthImage = theme.getImage("healthImage");
        healthBackground = theme.getImage("healthBackground");
        hordeHud = theme.getImage("hordeHud");
        hordePlatformScale = theme.getParameter("hordePlatformScale", 1.2f);
        hordeHudWidth = theme.getParameter("hordeHudWidth", 239);
        hordeHudGap = theme.getParameter("hordeHudGap", 12);
        hordeHudTop = theme.getParameter("hordeHudTop", 86);
        hordeRowGap = theme.getParameter("hordeRowGap", 56);
        hordeCenter = theme.getParameter("hordeCenter", 740);
        hordeSpacing = theme.getParameter("hordeSpacing", 50);
        hordeThreeSpacing = theme.getParameter("hordeThreeSpacing", 75);
        backdropOpacity = theme.getParameter("backdropOpacity", .68f);
        gigantamaxFrontScale = theme.getParameter("gigantamaxFrontScale", 1f);
        gigantamaxBackScale = theme.getParameter("gigantamaxBackScale", 1f);
        gigantamaxCameraFieldOfView = theme.getParameter("gigantamaxCameraFieldOfView", 50f);
        gigantamaxCameraVerticalOffset = theme.getParameter("gigantamaxCameraVerticalOffset", 0f);
        gigantamaxCameraTransitionMillis = Math.max(1, theme.getParameter("gigantamaxCameraTransitionMillis", 600));
        actionWidth = theme.getParameter("actionWidth", 209); actionHeight = theme.getParameter("actionHeight", 52);
        actionGap = theme.getParameter("actionGap", 2);
        footerTop = theme.getParameter("footerTop", 474);
        footerHeight = theme.getParameter("footerHeight", 116);
        healthWidth = theme.getParameter("healthWidth", 200);
        enemyHealthX = theme.getParameter("enemyHealthX", 70); selfHealthX = theme.getParameter("selfHealthX", 80);
        partyIconSize = theme.getParameter("partyIconSize", 36); partyIconGap = theme.getParameter("partyIconGap", 4);
        experienceBackground = theme.getImage("experienceBackground"); experienceImage = theme.getImage("experienceImage");
    }

    @Override protected void layout() {
        scale = Math.min(1, Math.min(getWidth() / 1024f, getHeight() / 768f));
        left = getX() + (getWidth()-size(1024))/2; top = getY();
        enemyHudX = x(134); enemyHudY = y(148); selfHudX = x(595); selfHudY = y(353);
        placeName(enemyName, enemyGender, enemyHudX+size(50), enemyHudY-size(22), size(257));
        placeName(selfName, selfGender, selfHudX+size(65), selfHudY-size(22), size(234));
        hp.setPosition(selfHudX+size(80), selfHudY+size(16)); hp.setSize(size(200), size(24));
        partyChoice.setPosition(x(234), y(150)); partyChoice.setSize(size(560), size(318));
        move.setPosition(x(144), y(footerTop+10)); move.setSize(size(actionWidth), controlHeight());
        moveBack.setPosition(x(144 + actionWidth + actionGap), y(footerTop+10)); moveBack.setSize(size(actionWidth), controlHeight());
        testMoveLabel.setPosition(x(144),y(footerTop+10)+controlHeight()+size(actionGap));testMoveLabel.setSize(size(150),size(30));
        testMove.setPosition(x(294),y(footerTop+10)+controlHeight()+size(actionGap));testMove.setSize(size(actionWidth*2+actionGap-150),size(30));
        opponentChoice.setPosition(x(554), y(180)); opponentChoice.setSize(size(330), size(28));
        for (int i = 0; i < hordeNames.length; i++) {
            int center = 514 + (hordeColumn(i)-1)*(hordeHudWidth+hordeHudGap);
            int genderWidth = hordeGenders[i].value == 2 ? 0 : size(20);
            int textWidth = Math.min(size(hordeHudWidth-18)-genderWidth, hordeNames[i].getPreferredWidth());
            int nameX = x(center)-(textWidth+genderWidth)/2;
            int nameHeight = Math.max(size(24), hordeNames[i].getPreferredHeight());
            int nameY = y(hordeHudTop+hordeRow(i)*hordeRowGap)-nameHeight;
            hordeNames[i].setPosition(nameX, nameY);
            hordeNames[i].setSize(textWidth, nameHeight);
            hordeGenders[i].setPosition(nameX+textWidth+size(4), nameY+(nameHeight-size(16))/2);
            hordeGenders[i].setSize(size(16), size(16));
        }
        message.setPosition(x(134), y(footerTop)); message.setSize(size(760), footerPixels());
        for (int i = 0; i < actions.length; i++) {
            actions[i].setPosition(x(144 + i % 2 * (actionWidth+actionGap)), y(footerTop+10) + i / 2 * (controlHeight()+size(actionGap)));
            actions[i].setSize(size(actionWidth), controlHeight());
        }
    }

    private void actorSprite(GUI gui, Image image, float center, float bottom, int foot, float zoom, float anchor, MovePresentation.Actor actor) {
        var renderer=gui.getRenderer();renderer.pushGlobalTintColor(actor.tint(),actor.tint(),actor.tint(),actor.opacity());
        boolean overlay = actor.overlay() > 0;
        if (overlay) {
            org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_TEXTURE_BIT);
            org.lwjgl.opengl.GL11.glTexEnvi(org.lwjgl.opengl.GL11.GL_TEXTURE_ENV, org.lwjgl.opengl.GL11.GL_TEXTURE_ENV_MODE, org.lwjgl.opengl.GL13.GL_COMBINE);
            org.lwjgl.opengl.GL11.glTexEnvi(org.lwjgl.opengl.GL11.GL_TEXTURE_ENV, org.lwjgl.opengl.GL13.GL_COMBINE_RGB, org.lwjgl.opengl.GL13.GL_INTERPOLATE);
            org.lwjgl.opengl.GL11.glTexEnvi(org.lwjgl.opengl.GL11.GL_TEXTURE_ENV, org.lwjgl.opengl.GL13.GL_SOURCE0_RGB, org.lwjgl.opengl.GL13.GL_CONSTANT);
            org.lwjgl.opengl.GL11.glTexEnvi(org.lwjgl.opengl.GL11.GL_TEXTURE_ENV, org.lwjgl.opengl.GL13.GL_SOURCE1_RGB, org.lwjgl.opengl.GL11.GL_TEXTURE);
            org.lwjgl.opengl.GL11.glTexEnvi(org.lwjgl.opengl.GL11.GL_TEXTURE_ENV, org.lwjgl.opengl.GL13.GL_SOURCE2_RGB, org.lwjgl.opengl.GL13.GL_CONSTANT);
            org.lwjgl.opengl.GL11.glTexEnvi(org.lwjgl.opengl.GL11.GL_TEXTURE_ENV, org.lwjgl.opengl.GL13.GL_OPERAND2_RGB, org.lwjgl.opengl.GL11.GL_SRC_ALPHA);
            org.lwjgl.opengl.GL11.glTexEnvi(org.lwjgl.opengl.GL11.GL_TEXTURE_ENV, org.lwjgl.opengl.GL13.GL_COMBINE_ALPHA, org.lwjgl.opengl.GL11.GL_MODULATE);
            org.lwjgl.opengl.GL11.glTexEnvi(org.lwjgl.opengl.GL11.GL_TEXTURE_ENV, org.lwjgl.opengl.GL13.GL_SOURCE0_ALPHA, org.lwjgl.opengl.GL11.GL_TEXTURE);
            org.lwjgl.opengl.GL11.glTexEnvi(org.lwjgl.opengl.GL11.GL_TEXTURE_ENV, org.lwjgl.opengl.GL13.GL_SOURCE1_ALPHA, org.lwjgl.opengl.GL13.GL_PRIMARY_COLOR);
            org.lwjgl.opengl.GL11.glTexEnvfv(org.lwjgl.opengl.GL11.GL_TEXTURE_ENV, org.lwjgl.opengl.GL11.GL_TEXTURE_ENV_COLOR,
                    new float[]{actor.red(), actor.green(), actor.blue(), actor.overlay()});
        }
        org.lwjgl.opengl.GL11.glPushMatrix();
        try {
            org.lwjgl.opengl.GL11.glTranslatef(x(center+actor.x()),y(bottom+actor.y()),0);
            org.lwjgl.opengl.GL11.glScalef(actor.scaleX(),actor.scaleY(),1);
            org.lwjgl.opengl.GL11.glTranslatef(-x(center),-y(bottom),0);
            sprite(image,center,bottom,foot,zoom,anchor);
        } finally {
            org.lwjgl.opengl.GL11.glPopMatrix();
            if (overlay) org.lwjgl.opengl.GL11.glPopAttrib();
            renderer.popGlobalTintColor();
        }
    }

    private MovePresentation.Actor projectActor(MovePresentation.Actor actor, boolean own) {
        float[] movement = scene.actorMovement(own, actor.x(), actor.y(), actor.z());
        return new MovePresentation.Actor(movement[0], movement[1], actor.scaleX() * movement[2], actor.scaleY() * movement[2], actor.tint(),
                actor.red(), actor.green(), actor.blue(), actor.overlay(), 0, actor.opacity());
    }

    private int controlHeight() {
        ActionButton action = (ActionButton) actions[0];
        return Math.max(size(actionHeight), action.title.getPreferredHeight()
                + action.description.getPreferredHeight() + size(3)*2);
    }

    private int footerPixels() {
        return Math.max(size(footerHeight), controlHeight()*2+size(actionGap)+size(20));
    }

    private int hordeColumn(int index) {
        if (wildOpponents.size() == 5) return index == 0 ? 0 : index == 4 ? 2 : index-1;
        return index;
    }

    private int hordeRow(int index) {
        return wildOpponents.size() == 5 && (index == 0 || index == 4) ? 1 : 0;
    }

    private float ownAnchor() { return 377; }

    private void placeName(Label name, Label gender, int x, int y, int width) {
        int genderWidth = ((GenderLabel) gender).value == 2 ? 0 : size(20);
        int textWidth = Math.min(width - genderWidth, name.getPreferredWidth());
        int left = x;
        name.setPosition(left, y); name.setSize(textWidth, size(24));
        gender.setPosition(left + textWidth + size(4), y + size(4)); gender.setSize(size(16), size(16));
    }

    private int x(float value) { return left + Math.round(value * scale); }
    private int y(float value) { return top + Math.round(value * scale); }
    private int size(float value) { return Math.max(1, Math.round(value * scale)); }

    private void shade(GUI gui, float alpha, boolean bright) {
        gui.getRenderer().pushGlobalTintColor(bright ? 1 : 0, bright ? 1 : 0, bright ? 1 : 0, alpha);
        try { white.draw(getAnimationState(), getX(), getY(), getWidth(), getHeight()); }
        finally { gui.getRenderer().popGlobalTintColor(); }
    }

    @Override protected void paintWidget(GUI gui) {
        if (encounter == null || white == null) return;
        var phase = encounter.update(now);
        if (phase == WildEncounter.Phase.LOADING) { shade(gui, .2f, false); return; }
        long elapsed = encounter.elapsed(now);
        float opacity = 1 - encounter.exitProgress(now);
        shade(gui, backdropOpacity * opacity * Math.min(1, Math.max(0, (elapsed - 800) / 500f)), false);
        if (elapsed < 1300) { shade(gui, WildEncounter.flash(elapsed), true); return; }
        gui.getRenderer().pushGlobalTintColor(1, 1, 1, opacity);
        gui.getRenderer().clipEnter(x(134), y(50), size(760), size(footerTop-50)+footerPixels());
        try {
            float slide = Math.min(1, (elapsed - 1300) / 700f);
            slide = 1 - (float) Math.pow(1 - slide, 3);
            float enemyX = 740 + (1 - slide) * 700;
            float ownX = ownAnchor() - (1 - slide) * 800;
            boolean casting = phase == WildEncounter.Phase.MOVE;
            boolean freezeCamera = casting || phase == WildEncounter.Phase.SWITCH_LOADING || phase == WildEncounter.Phase.SWITCHING;
            if (freezeCamera && !cameraFrozen) { frozenCameraTime = cameraTime; cameraFrozen = true; }
            if (!freezeCamera && cameraFrozen) { cameraPaused = elapsed - frozenCameraTime; cameraFrozen = false; }
            if (!freezeCamera) cameraTime = elapsed - cameraPaused;
            updateGigantamaxCamera(elapsed, phase);
            scene.drawPlatforms(x(134), y(50), size(760), size(540), getWidth(), getHeight(),
                    Math.max(0, (freezeCamera ? frozenCameraTime : cameraTime)-1300)/1000f, slide,
                    hordeSprites.isEmpty() ? 1 : hordePlatformScale);
            long attackTime=casting?encounter.moveElapsed(now):MoveAnimations.playback(encounter.moveId()).duration();
            var ownAttack = new MovePresentation.Actor(0, 0, 1, 1, 1);
            var enemyAttack = new MovePresentation.Actor(0, 0, 1, 1, 1);
            if (casting) {
                if (moveActors == null || moveActors.move() != encounter.moveId() || moveActorsReversed != enemyCasting) {
                    if (moveActors != null) moveActors.close();
                    var animation = MoveAnimations.supports(encounter.moveId())
                            ? MoveAnimations.get(encounter.moveId(), enemyCasting ? "enemy" : "") : MoveAnimations.playback(encounter.moveId());
                    moveActors = new OriginalActorAnimation(animation);
                    moveActorsReversed = enemyCasting;
                }
                var actors = moveActors.sample(attackTime);
                ownAttack = projectActor(actors[enemyCasting ? 1 : 0], true);
                enemyAttack = projectActor(actors[enemyCasting ? 0 : 1], false);
            }
            var backdrop = casting ? MovePresentation.backdropTint(encounter.moveId(), attackTime) : new MovePresentation.Tint(0, 0, 0, 0);
            if(backdrop.alpha()>0) {gui.getRenderer().pushGlobalTintColor(backdrop.red(),backdrop.green(),backdrop.blue(),backdrop.alpha());try{white.draw(getAnimationState(),x(134),y(50),size(760),size(540));}finally{gui.getRenderer().popGlobalTintColor();}}
            var enemyPose = scene.actorPose(false);
            var ownPose = scene.actorPose(true);
            enemyX += enemyPose.dx();
            ownX += ownPose.dx();
            if (hordeSprites.isEmpty()) {
            if(enemy.hp()>0) actorSprite(gui,front[(int) (Math.max(0, elapsed - 2000) * 30 / 1000) % front.length], enemyX, 311 + enemyPose.dy(), frontBottom, frontZoom * enemyPose.scale() * formScale(enemy, false), frontCenter, enemyAttack);
                if (enemy.shiny() && shinyParticles != null)
                    shinyParticles.draw(this, elapsed-2000, x(ownX), y(450), x(enemyX), y(260+enemyPose.dy()), scale*2);
            } else {
                int[] order = hordeSprites.size() == 5 ? new int[]{0,2,4,1,3} : new int[]{0,1,2};
                for (int i : order) {
                    var sprite = hordeSprites.get(i);
                    int spacing = hordeSprites.size() == 3 ? hordeThreeSpacing : hordeSpacing;
                    float center = hordeCenter+(i-(hordeSprites.size()-1)/2f)*spacing+(1-slide)*700+enemyPose.dx();
                    float floor = 311+(i%2==0?-8:6)+enemyPose.dy();
                    var actor = i == 0 ? enemyAttack : new MovePresentation.Actor(0,0,1,1,1);
                    actorSprite(gui, sprite.frames()[(int)(Math.max(0,elapsed-2000)*30/1000)%sprite.frames().length],
                            center, floor, sprite.bottom(), sprite.zoom()*enemyPose.scale()*formScale(wildOpponents.get(i), false), sprite.center(), actor);
                    if (wildOpponents.get(i).shiny() && shinyParticles != null)
                        shinyParticles.draw(this,elapsed-2000,x(ownX),y(450),x(center),y(floor-40),scale*1.6f);
                }
            }
            if (elapsed < 3600) {
                int frame = Math.max(0, Math.min(4, (int) ((elapsed - 3000) / 120)));
                sprite(trainer[frame], ownX - 55 - Math.max(0, elapsed - 3300) * .25f, 510, 80, 3);
                if (elapsed >= 3200) {
                    float progress = Math.min(1, (elapsed - 3200) / 400f);
                    org.lwjgl.opengl.GL11.glPushMatrix();
                    try {
                        org.lwjgl.opengl.GL11.glTranslatef(x(ownX - 55 + 120 * progress), y(390 - 100 * (float) Math.sin(Math.PI * progress)), 0);
                        org.lwjgl.opengl.GL11.glRotatef(progress * 720, 0, 0, 1);
                        ball.draw(getAnimationState(), -size(11), -size(11), size(22), size(22));
                    } finally { org.lwjgl.opengl.GL11.glPopMatrix(); }
                }
            } else if (phase != WildEncounter.Phase.SWITCH_LOADING && self.hp()>0) {
                float release = phase == WildEncounter.Phase.SWITCHING ? Math.min(1,encounter.switchElapsed(now)/600f)
                        : Math.min(1, (elapsed - 3600) / 600f);
                actorSprite(gui,back[(int) (Math.max(0, elapsed - backStarted) * 30 / 1000) % back.length], ownX, 572 + ownPose.dy(), backBottom,
                        backZoom * ownPose.scale() * Math.max(.01f, release) * formScale(self, true), backCenter, ownAttack);
                if (release < .4f) {
                    gui.getRenderer().pushGlobalTintColor(1, 1, 1, 1 - release / .4f);
                    try { releaseGlow.draw(getAnimationState(), x(ownX - 100), y(400), size(200), size(200)); }
                    finally { gui.getRenderer().popGlobalTintColor(); }
                }
            }
            if (self.shiny() && shinyParticles != null && elapsed >= 4200)
                shinyParticles.draw(this, elapsed-backStarted-600, x(ownX), y(450), x(ownX), y(450), scale*2);
            if (casting && moveParticles != null)
                moveParticles.drawOriginal(this, encounter.moveElapsed(now), scene, enemyCasting,
                        x(134), y(50), size(760), size(540));
            gui.getRenderer().pushGlobalTintColor(0, 0, 0, .35f);
            try { white.draw(getAnimationState(), x(134), y(footerTop), size(760), footerPixels()); }
            finally { gui.getRenderer().popGlobalTintColor(); }
            if (elapsed >= 2000 && hordeSprites.isEmpty()) {
                enemyHud.draw(getAnimationState(), enemyHudX, enemyHudY, size(307), size(28));
                healthBackground.draw(getAnimationState(), enemyHudX + size(enemyHealthX), enemyHudY + size(9), size(healthWidth), size(8));
                healthImage.draw(getAnimationState(), enemyHudX + size(enemyHealthX+1), enemyHudY + size(10), size((healthWidth-2f)*enemy.hp()/enemy.maxHp()), size(6));
            }
            if (elapsed >= 2000 && !hordeSprites.isEmpty()) for (int i = 0; i < hordeSprites.size(); i++) {
                int center = 514+(hordeColumn(i)-1)*(hordeHudWidth+hordeHudGap);
                int hx = x(center-hordeHudWidth/2f), hy = y(hordeHudTop+hordeRow(i)*hordeRowGap);
                hordeHud.draw(getAnimationState(),hx,hy,size(hordeHudWidth),size(28));
                healthBackground.draw(getAnimationState(),hx+size(20),hy+size(10),size(hordeHudWidth-40),size(8));
                healthImage.draw(getAnimationState(),hx+size(21),hy+size(11),size(hordeHudWidth-42),size(6));
            }
            if (elapsed >= 3600) {
                selfHud.draw(getAnimationState(), selfHudX, selfHudY, size(299), size(40));
                healthBackground.draw(getAnimationState(), selfHudX + size(selfHealthX), selfHudY + size(8), size(healthWidth), size(8));
                healthImage.draw(getAnimationState(), selfHudX + size(selfHealthX+1), selfHudY + size(9), size((healthWidth-2f) * self.hp() / self.maxHp()), size(6));
                if (self.individual() != null && experienceBackground != null && experienceImage != null) {
                    experienceBackground.draw(getAnimationState(), selfHudX+size(80), selfHudY+size(41), size(200), size(6));
                    if (experienceFraction > 0) experienceImage.draw(getAnimationState(), selfHudX+size(85), selfHudY+size(43), size(190*experienceFraction), size(2));
                }
                for (int i = 0; i < party.length; i++) party[i].draw(getAnimationState(),
                        selfHudX + size(55+i*(partyIconSize+partyIconGap)), selfHudY + size(59), size(partyIconSize), size(partyIconSize));
            }
        } finally { gui.getRenderer().clipLeave(); gui.getRenderer().popGlobalTintColor(); }
    }

    private void sprite(Image image, float center, float floor, int bottom, float zoom) {
        sprite(image, center, floor, bottom, zoom, image.getWidth() / 2f);
    }

    private void sprite(Image image, float center, float floor, int bottom, float zoom, float anchor) {
        image.draw(getAnimationState(), x(center) - size(anchor * zoom), y(floor) - size(bottom * zoom),
                size(image.getWidth() * zoom), size(image.getHeight() * zoom));
    }

    private static final class GenderLabel extends Label {
        private int value = 2;
        private Image male, female;

        GenderLabel() { setTheme("gender"); setCanAcceptKeyboardFocus(false); }

        @Override protected void applyTheme(ThemeInfo theme) {
            super.applyTheme(theme);
            male = theme.getImage("maleImage"); female = theme.getImage("femaleImage");
        }

        @Override protected void paintWidget(GUI gui) {
            Image image = value == 0 ? male : value == 1 ? female : null;
            if (image != null) image.draw(getAnimationState(), getX(), getY(), getWidth(), getHeight());
        }
    }

    private final class PartyChoiceWindow extends ResizableFrame {
        private final MemberCard[] cards = new MemberCard[6];
        private final Button cancel = new Button(ClientStrings.get(58));
        PartyChoiceWindow() {
            setTheme("party-window");
            setTitle(ClientStrings.get(0));
            setResizableAxis(ResizableAxis.NONE);
            addCloseCallback(() -> { if (encounter != null && (battleState==null||!battleState.forceSwitch())) encounter.cancelParty(now); });
            for (int i = 0; i < cards.length; i++) { cards[i] = new MemberCard(i); add(cards[i]); }
            cancel.setTheme("button");
            cancel.addCallback(() -> { if (encounter != null && (battleState==null||!battleState.forceSwitch())) encounter.cancelParty(now); });
            add(cancel);
            setHardVisible(false);
        }
        void refresh() {
            cancel.setEnabled(battleState==null||!battleState.forceSwitch());
            for (int i = 0; i < cards.length; i++) {
                boolean present = i < members.size();
                cards[i].setVisible(present);
                cards[i].setEnabled(present && encounter != null && pendingBattle==null && resultToApply==null && i != encounter.activeSlot() && members.get(i).hp() > 0);
                if (present) {
                    PartyMember member = members.get(i);
                    String state = encounter != null && i == encounter.activeSlot() ? " · " + ClientStrings.get(252006) : "";
                    cards[i].info.setText(name(member) + " Lv. " + member.level() + state + "\n" + member.hp() + " / " + member.maxHp());
                }
            }
        }
        @Override protected void layout() {
            super.layout();
            for (int i = 0; i < cards.length; i++) {
                cards[i].setPosition(getX()+size(12+i%2*274), getY()+size(42+i/2*76));
                cards[i].setSize(size(262),size(68));
            }
            cancel.setPosition(getX()+size(170),getY()+size(276)); cancel.setSize(size(220),size(30));
        }
    }

    private final class MemberCard extends Button {
        private final int slot;
        private final Label info = new Label() { @Override protected boolean handleEvent(Event event) { return false; } };
        MemberCard(int slot) {
            this.slot = slot;
            setTheme("member");
            info.setTheme("label"); add(info);
            addCallback(() -> chooseParty(slot));
        }
        @Override protected void layout() {
            super.layout();
            info.setPosition(getX()+size(48),getY()+size(5)); info.setSize(getWidth()-size(54),size(46));
        }
        @Override protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if (party != null && slot < party.length) party[slot].draw(getAnimationState(),getX()+size(8),getY()+size(12),size(32),size(32));
            if (slot < members.size() && healthBackground != null) {
                var member = members.get(slot);
                healthBackground.draw(getAnimationState(),getX()+size(49),getY()+size(54),size(200),size(6));
                if (member.hp()>0) healthImage.draw(getAnimationState(),getX()+size(50),getY()+size(55),size(198f*member.hp()/member.maxHp()),size(4));
            }
        }
    }

    private final class ActionButton extends Button {
        private final Label title = new Label() {
            @Override protected boolean handleEvent(Event event) { return false; }
        };
        private final Label description = new Label() {
            @Override protected boolean handleEvent(Event event) { return false; }
        };

        ActionButton(String titleText, String descriptionText) {
            setText("");
            title.setText(titleText); title.setTheme("title");
            description.setText(descriptionText); description.setTheme("description");
            title.setCanAcceptKeyboardFocus(false); description.setCanAcceptKeyboardFocus(false);
            add(title); add(description);
        }

        void setLabels(int titleId, int descriptionId) {
            title.setText(ClientStrings.get(titleId));
            description.setText(ClientStrings.get(descriptionId));
        }

        @Override protected void layout() {
            super.layout();
            int padding = size(3);
            int lineHeight = (getHeight()-padding*2)/2;
            title.setPosition(getX()+padding, getY()+padding);
            title.setSize(getWidth()-padding*2, lineHeight);
            description.setPosition(getX()+padding, getY()+padding+lineHeight);
            description.setSize(getWidth()-padding*2, lineHeight);
        }

    }

    @Override protected boolean handleEvent(Event event) {
        if (active() && event.isKeyPressedEvent() && event.getKeyCode() == Event.KEY_ESCAPE
                && encounter.update(now) == WildEncounter.Phase.SELECT_MOVE) {
            encounter.cancelMove(now);
            return true;
        }
        if (active() && event.isKeyPressedEvent() && event.getKeyCode() == Event.KEY_ESCAPE
                && encounter.update(now) == WildEncounter.Phase.SELECT_PARTY) {
            if(battleState==null||!battleState.forceSwitch()) encounter.cancelParty(now);
            return true;
        }
        return active() || super.handleEvent(event);
    }

    @Override public void close() {
        if(battleState!=null)lastOutcome=battleState.outcome();
        battleTransport=null;pendingBattle=null;battleState=resultToApply=null;serverEvents.clear();serverMessage="";enemyCasting=false;
        testMove.setModel(new SimpleChangableListModel<>(testMoveIds.stream().map(id -> id + "  " + ClientStrings.get(110000+id)).toArray(String[]::new)));
        testMoveLabel.setText(ClientStrings.get(252009));
        gigantamaxCameraBlend = 0;
        gigantamaxCameraRaised = false;
        gigantamaxCameraTime = now;
        if (pendingEnemy != null) pendingEnemy.cancel(true);
        pendingEnemy = null;
        opponentChoice.setVisible(false);
        scriptedOpponents = List.of(); scripted = trainerBattle = escapeLocked = false; frontTexture = null;
        if (pending != null) pending.cancel(true);
        pending = null;
        if (pendingSwitch != null) pendingSwitch.cancel(true);
        pendingSwitch = null;
        partyChoice.setHardVisible(false);
        wildOpponents = List.of(); hordeSprites = List.of(); shinyParticles = null;
        for (var label : hordeNames) label.setVisible(false);
        for (var label : hordeGenders) label.setVisible(false);
        members = List.of(); battleRom = null; backTexture = null;
        if (scene != null) scene.close();
        scene = null;
        for (Texture texture : textures) texture.destroy();
        textures.clear();
        if(pendingMove!=null)pendingMove.cancel(false);pendingMove=null;
        clearMovePresentation();
        selectedMove=MovePresentation.TACKLE;testMove.setSelected(testMoveIds.indexOf(selectedMove));move.setText(ClientStrings.get(110033));move.setEnabled(true);testMove.setVisible(false);testMoveLabel.setVisible(false);
        front = back = trainer = party = null;
        ball = releaseGlow = null; self = enemy = null; encounter = null;
        setVisible(false);
    }
}
