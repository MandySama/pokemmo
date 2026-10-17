package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.protocol.EncounterCatalog;
import top.totodile.pokemmo.protocol.BattlePackets;
import top.totodile.pokemmo.protocol.BattleResumePackets;
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
    private boolean pendingTestMove;
    private final java.util.ArrayDeque<BattlePackets.Event> serverEvents = new java.util.ArrayDeque<>();
    private long eventUntil;
    private String serverMessage = "";
    private Consumer<String> battleLog = message -> { };
    private final java.util.Set<String> loggedEvents = new java.util.HashSet<>();
    private int logEventIndex;
    private String logKey = "";
    private boolean suppressBattleLog;
    private final top.totodile.pokemmo.battle.BattleHpAnimation selfHp = new top.totodile.pokemmo.battle.BattleHpAnimation();
    private final top.totodile.pokemmo.battle.BattleHpAnimation enemyHp = new top.totodile.pokemmo.battle.BattleHpAnimation();
    private int hpAnimationMinimum = 350, hpAnimationMaximum = 1000;

    void setBattleLog(Consumer<String> listener) { battleLog = listener; }
    private void logBattle(String message) {
        if (!suppressBattleLog && !message.isBlank() && loggedEvents.add(logKey)) battleLog.accept(message);
    }
    private void battleMessage(String message) { serverMessage = message; logBattle(message); }
    private boolean enemyCasting, selectingServerMove;
    private final Label selfStatusLabel = new Label(), enemyStatusLabel = new Label();
    private int selfStatus, enemyStatus, lastMoveSide;
    private static String statusName(int status) { return status > 0 && status <= 6 ? ClientStrings.get(252130 + status) : ""; }
    private void refreshStatusLabels() {
        selfStatusLabel.setText(statusName(selfStatus)); enemyStatusLabel.setText(statusName(enemyStatus));
        selfStatusLabel.setVisible(selfName.isVisible() && selfStatus != 0 && self != null && selfHp.value(now) > 0);
        enemyStatusLabel.setVisible(enemyName.isVisible() && enemyStatus != 0 && enemy != null && enemyHp.value(now) > 0);
    }
    private static int memberStatus(PartyMember member) {
        return member.individual() == null ? 0 : Math.max(0, BattlePackets.statusId(member.individual().status()));
    }
    private void eventMessage(BattlePackets.Event event, String actor) {
        int kind = event.kind(), value = event.value();
        String text;
        if (kind == 2 || kind == BattlePackets.CURE_STATUS) {
            if (event.side() == 1) enemyStatus = kind == 2 ? value : 0;
            else selfStatus = kind == 2 ? value : 0;
            text = ClientStrings.get(kind == 2 ? 252137 : 252138).replace("{01}", statusName(value));
        } else if (kind == BattlePackets.FAIL) {
            text = ClientStrings.get(value > 0 && value <= 6 ? 252139 : 252140).replace("{01}", statusName(value));
        } else if (kind == BattlePackets.IMMUNE) text = ClientStrings.get(252141);
        else if (kind == BattlePackets.BLOCK) text = ClientStrings.get(value > 0 ? 252142 : 252143)
                .replace("{01}", value > 0 ? ClientStrings.get(110000 + value) : "");
        else if (kind == BattlePackets.NOTHING) text = ClientStrings.get(252144);
        else if (kind == BattlePackets.CANT) text = ClientStrings.get(value > 0 && value <= 16 ? 252150 + value : 252150);
        else text = ClientStrings.get(kind == 1 ? 9823 : 9825);
        battleMessage(text.replace("{00}", actor));
        refreshStatusLabels();
    }
    private int enemySlot, lastOutcome;
    private BattleResumePackets.Snapshot restoringBattle;
    private Runnable recoveryFailure = () -> { };
    int lastOutcome() { return lastOutcome; }

    void connectBattle(java.util.function.Function<BattlePackets.Request, CompletableFuture<BattlePackets.State>> transport,
                       Consumer<List<PartyMember>> listener) {
        battleTransport = transport; partyChanged = listener;
        pendingBattle = transport.apply(new BattlePackets.Request(BattlePackets.START,0,0,0));
    }

    void resumeBattle(Path rom, AvatarAssets assets, Appearance appearance, BattleResumePackets.Snapshot snapshot,
                      java.util.function.Function<BattlePackets.Request, CompletableFuture<BattlePackets.State>> transport,
                      Consumer<List<PartyMember>> listener, Runnable recoveryFailure, long milliseconds) {
        restoringBattle = snapshot;
        this.recoveryFailure = recoveryFailure;
        if (snapshot.trainerBattle()) startScripted(rom, assets, appearance, snapshot.state().party(), snapshot.opponents(), true, milliseconds);
        else startWild(rom, assets, appearance, snapshot.state().party(), snapshot.encounter(), milliseconds);
        if (encounter == null) { restoringBattle = null; return; }
        battleTransport = transport; partyChanged = listener;
        suppressBattleLog = true;
        pendingBattle = CompletableFuture.completedFuture(snapshot.state());
    }
    private void submitBattle(int action, int slot) {
        if(pendingBattle!=null || resultToApply!=null || battleState==null || battleState.outcome()!=0)return;
        int replacement = action == BattlePackets.MOVE ? (customMoveOverride > 0 ? customMoveOverride : BattlePackets.RESTORE_MOVE) : 0;
        pendingTestMove = action == BattlePackets.MOVE && customMoveOverride > 0;
        suppressBattleLog = false;
        pendingBattle=battleTransport.apply(new BattlePackets.Request(action,battleState.battle(),battleState.revision(),slot,replacement));
    }
    private void configureServerMoves() {
        if(battleState==null || self==null)return;
        refreshMoveSlots();
    }

    private void refreshMoveSlots() {
        if (self == null) return;
        List<BattlePackets.MoveOption> options;
        if (battleTransport != null) options = battleState == null ? List.of() : battleState.options();
        else {
            var learned = self.individual() == null ? PokemonCatalog.moves(self.species(), self.level()) : self.individual().moves();
            var entries = new ArrayList<BattlePackets.MoveOption>();
            for (int i = 0; i < learned.size() && i < 4; i++) {
                var m = learned.get(i); entries.add(new BattlePackets.MoveOption(i + 1, m.id(), m.pp(), m.maxPp()));
            }
            options = entries;
        }
        for (int i = 0; i < moveSlots.length; i++) {
            var option = i < options.size() ? options.get(i) : null;
            moveSlots[i].configure(option);
            boolean legal = option != null && (battleTransport != null ? !battleState.forceSwitch() && (customMoveOverride > 0 || battleState.legalMoves().contains(option.slot()))
                    : option.pp() > 0 && MoveAnimations.supports(customMoveOverride > 0 ? customMoveOverride : option.id()));
            moveSlots[i].setEnabled(legal && pendingMove == null && pendingBattle == null && resultToApply == null);
        }
    }

    private void activateMoveSlot(int index) {
        var option = moveSlots[index].option;
        if (option == null || !moveSlots[index].isEnabled() || encounter == null
                || encounter.update(now) != WildEncounter.Phase.SELECT_MOVE) return;
        if (battleTransport != null) submitBattle(BattlePackets.MOVE, option.slot());
        else {
            int id = customMoveOverride > 0 ? customMoveOverride : option.id();
            if (id == selectedMove && moveParticles != null) encounter.chooseMove(id, now);
            else { requestedPreviewMove = id; replaceTestMove(id); }
        }
    }

    private void cancelMoveSelection() {
        if (encounter == null) return;
        if (requestedPreviewMove > 0) {
            if (pendingMove != null) pendingMove.cancel(false);
            pendingMove = null; requestedPreviewMove = 0;
        }
        encounter.cancelMove(now);
    }
    private void tickBattle() {
        if(battleTransport==null)return;
        if(pendingBattle!=null && pendingBattle.isDone()) {
            try {
                var result=pendingBattle.join();pendingBattle=null;
                if (!result.accepted() && pendingTestMove) failure.accept(ClientStrings.get(252010));
                pendingTestMove = false;
                resultToApply=result;logEventIndex=0;serverEvents.clear();serverEvents.addAll(result.events());eventUntil=now;
            }catch(RuntimeException error){pendingBattle=null;close();finished.run();failure.accept(ClientStrings.get(9822));return;}
        }
        if(pendingMove!=null || pendingSwitch!=null || pendingEnemy!=null || resultToApply==null || encounter==null || encounter.update(now).ordinal()<WildEncounter.Phase.READY.ordinal() || now<eventUntil)return;
        if(!serverEvents.isEmpty()) {
            var event=serverEvents.removeFirst();
            logKey = resultToApply.battle()+":"+resultToApply.revision()+":"+logEventIndex++;
            if (event.kind() == 1 && event.side() == 1 && trainerBattle && enemySlot < enemyFainted.length) enemyFainted[enemySlot] = true;
            if (event.kind() == BattlePackets.EXPERIENCE || event.kind() == BattlePackets.LEVEL_UP || event.kind() == BattlePackets.PRIZE_MONEY) {
                enemyCasting = false;
                if (event.kind() == BattlePackets.PRIZE_MONEY) battleMessage(ClientStrings.get(252112).replace("{00}", Integer.toString(event.value())));
                else {
                    var member = resultToApply.party().get(event.side());
                    battleMessage(ClientStrings.get(event.kind() == BattlePackets.EXPERIENCE ? 252110 : 252111)
                            .replace("{00}", name(member)).replace("{01}", Integer.toString(event.value())));
                    if (event.side() == encounter.activeSlot()) updateSelf(member);
                }
                eventUntil = now + 1400;
                return;
            }
            enemyCasting=event.side()==1;
            String actor=name(enemyCasting?enemy:self);
            if(event.kind()==5) {
                if(event.side()==1 && trainerBattle && event.value()!=enemySlot && event.value()<scriptedOpponents.size()) {
                    logBattle(ClientStrings.get(252123).replace("{00}",name(scriptedOpponents.get(event.value()))));
                    enemySlot=event.value();loadEnemy(scriptedOpponents.get(enemySlot));eventUntil=now+600;return;
                }
                if(event.side()==0 && event.value()!=encounter.activeSlot()) {
                    encounter.serverParty(members.stream().mapToInt(PartyMember::hp).toArray(),true);
                    logBattle(ClientStrings.get(252124).replace("{00}",name(members.get(event.value()))));
                    loadParty(event.value());eventUntil=now+600;
                }
                return;
            }
            if(event.kind()==4) {
                int oldHp = enemyCasting ? enemy.hp() : self.hp();
                int targetHp = Math.max(0, Math.min(event.value(), enemyCasting ? enemy.maxHp() : self.maxHp()));
                double displayedHp = (enemyCasting ? enemyHp : selfHp).value(now);
                if(enemyCasting) enemy=new PartyMember(enemy.species(),enemy.shiny(),enemy.alpha(),Math.min(event.value(),enemy.maxHp()),enemy.maxHp(),enemy.level(),enemy.individual());
                else updateSelf(new PartyMember(self.species(),self.shiny(),self.alpha(),Math.min(event.value(),self.maxHp()),self.maxHp(),self.level(),self.individual()));
                var animation = enemyCasting ? enemyHp : selfHp;
                // 成员更新会重置显示值；伤害动画从这一帧已经显示的 HP 开始。
                if (!enemyCasting) animation.reset((int)Math.round(displayedHp));
                long duration = animation.animate(targetHp, enemyCasting ? enemy.maxHp() : self.maxHp(), now,
                        hpAnimationMinimum, hpAnimationMaximum);
                if (oldHp != targetHp) logBattle(ClientStrings.get(targetHp < oldHp ? 252121 : 252122)
                        .replace("{00}", actor).replace("{01}", Integer.toString(Math.abs(targetHp-oldHp)))
                        .replace("{02}", Integer.toString(targetHp)).replace("{03}", Integer.toString(enemyCasting ? enemy.maxHp() : self.maxHp())));
                eventUntil=now+duration;return;
            }
            if(event.kind()==0) {
                lastMoveSide = event.side();
                if(pendingMove!=null)pendingMove.cancel(false);pendingMove=null;
                clearMovePresentation();
                boolean missed = false;
                for (var next : serverEvents) {
                    if (next.kind() == 0) break;
                    if (next.kind() == 3 || next.kind() == BattlePackets.FAIL || next.kind() == BattlePackets.IMMUNE
                            || next.kind() == BattlePackets.BLOCK || next.kind() == BattlePackets.NOTHING) { missed = true; break; }
                }
                if (missed) {
                    battleMessage(ClientStrings.get(252005).replace("{00}", actor).replace("{01}", ClientStrings.get(110000+event.value())));
                    eventUntil = now;
                    return;
                }
                int presentationMove = event.value();
                if(MoveAnimations.supports(presentationMove)) {
                    int moveId=presentationMove;Path rom=battleRom;boolean reverse=enemyCasting;
                    pendingMove=CompletableFuture.supplyAsync(()->{
                        try{return BattleParticles.load(rom,moveId,reverse);}catch(IOException error){throw new java.util.concurrent.CompletionException(error);}
                    });
                }
                encounter.playServerMove(presentationMove,now);
                battleMessage(ClientStrings.get(252005).replace("{00}",actor).replace("{01}",ClientStrings.get(110000+event.value())));
                eventUntil=now+MoveAnimations.playback(encounter.moveId()).duration();
            }else {
                eventMessage(event, event.kind() == 3 ? name(lastMoveSide == 1 ? enemy : self) : actor);
                eventUntil=now+800;
            }
            return;
        }
        var result=resultToApply;logKey=result.battle()+":"+result.revision()+":outcome";resultToApply=null;battleState=result;members=result.party();
        if(result.active()!=encounter.activeSlot()) {
            encounter.serverParty(members.stream().mapToInt(PartyMember::hp).toArray(),true);
            loadParty(result.active());
        } else updateSelf(members.get(result.active()));
        if(trainerBattle && result.enemyActive()!=enemySlot && result.enemyActive()<scriptedOpponents.size()) {
            enemySlot=result.enemyActive();loadEnemy(scriptedOpponents.get(enemySlot));
        }
        enemy=new PartyMember(enemy.species(),enemy.shiny(),enemy.alpha(),result.enemyHp(),result.enemyMaxHp(),enemy.level());
        enemyHp.reset(enemy.hp());
        if (trainerBattle && enemySlot < enemyFainted.length) enemyFainted[enemySlot] = result.enemyHp() == 0;
        selfStatus = memberStatus(members.get(result.active())); enemyStatus = result.enemyStatus();
        partyChanged.accept(members);enemyCasting=false;
        if(pendingSwitch==null) encounter.serverParty(members.stream().mapToInt(PartyMember::hp).toArray(),result.forceSwitch());
        configureServerMoves();
        if(result.outcome()!=0) {
            battleMessage(ClientStrings.get(result.outcome()==1?9826:result.outcome()==2?9827:result.outcome()==4?9828:result.outcome()==5?9822:9829));
            eventUntil=now+1800;
        } else if(!result.accepted()) battleMessage(ClientStrings.get(9830));
        else serverMessage="";
    }

    private record Pixels(List<BufferedImage> front, List<BufferedImage> back,
                          List<BufferedImage> trainer, BufferedImage ball, BufferedImage party, BattleScene.Resources scene, List<List<BufferedImage>> horde, List<BattleParticles.Definition> shiny,
                          BufferedImage enemyTrainer, BufferedImage enemyParty) { }

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
    private boolean trainerIntroduction;
    private Image enemyTrainer;
    private Image[] enemyParty;
    private boolean[] enemyFainted = new boolean[0];
    private boolean escapeLocked;
    void setEscapeLocked(boolean locked) { escapeLocked=locked; }
    private final ComboBox<String> opponentChoice = new ComboBox<>(new SimpleChangableListModel<>());
    private long backStarted;
    private Runnable openBag = () -> { };
    private final Button[] actions = new Button[4];
    private final Button moveBack = new Button(ClientStrings.get(58));
    private final MoveButton[] moveSlots = new MoveButton[4];
    private int customMoveOverride, requestedPreviewMove;
    private int customMoveWidth = 200, customMoveGap = 10, moveBackWidth = 88;
    private final List<Integer> testMoveIds = MoveAnimations.ids();
    private String[] customMoveLabels() {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of(ClientStrings.get(252012)),
                testMoveIds.stream().map(id -> id + "  " + ClientStrings.get(110000+id))).toArray(String[]::new);
    }
    private final ComboBox<String> testMove = new ComboBox<>(new SimpleChangableListModel<>(customMoveLabels())) {
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
    private float maximumScale = 2f;
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
                }
            });
            add(actions[i]);
        }
        moveBack.setTheme("move");
        moveBack.addCallback(this::cancelMoveSelection);
        for (int i = 0; i < moveSlots.length; i++) {
            final int index = i;
            moveSlots[i] = new MoveButton(); moveSlots[i].setTheme("move-slot");
            moveSlots[i].addCallback(() -> activateMoveSlot(index));
            moveSlots[i].setVisible(false); add(moveSlots[i]);
        }
        testMove.setTheme("test-move");testMove.setTooltipContent(ClientStrings.get(252011));
        testMove.setSelected(0);
        testMove.addCallback(() -> {
            int index=testMove.getSelected();
            if (!selectingServerMove && index >= 0) {
                customMoveOverride = index == 0 ? 0 : testMoveIds.get(index - 1);
                refreshMoveSlots();
            }
        });
        add(moveBack); add(testMove); add(testMoveLabel); testMoveLabel.setTheme("test-label");testMoveLabel.setVisible(false);testMove.setVisible(false);
        moveBack.setVisible(false);
        for (Label label : new Label[]{enemyName, selfName, hp, message}) { label.setTheme("label"); add(label); }
        enemyName.setTheme("name"); selfName.setTheme("name"); hp.setTheme("name"); message.setTheme("message");
        add(enemyGender); add(selfGender);
        selfStatusLabel.setTheme("status"); enemyStatusLabel.setTheme("status");
        add(selfStatusLabel); add(enemyStatusLabel);
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
        Path rom = battleRom;
        boolean reverse = enemyCasting;
        pendingMove = CompletableFuture.supplyAsync(() -> {
            try { return BattleParticles.load(rom, id, reverse); }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
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
        selfHp.reset(self.hp());
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
        lastOutcome=0;enemySlot=restoringBattle == null ? 0 : restoringBattle.state().enemyActive();
        self = restoringBattle == null ? members.stream().filter(member -> member.hp() > 0).findFirst().orElse(null)
                : members.get(restoringBattle.state().active());
        if (self == null) { failure.accept(ClientStrings.get(252004)); return; }
        var random = java.util.concurrent.ThreadLocalRandom.current();
        if (opponents.isEmpty() && selectedWild == null) { failure.accept(ClientStrings.get(9732)); return; }
        scripted = !opponents.isEmpty();
        this.trainerBattle = trainerBattle;
        scriptedOpponents = List.copyOf(opponents);
        enemyFainted = new boolean[opponents.size()];
        if (restoringBattle != null) for (int i = 0; i < enemyFainted.length; i++) enemyFainted[i] = (restoringBattle.faintMask() & 1 << i) != 0;
        enemy = scripted ? scriptedOpponents.get(enemySlot) : restoringBattle == null ? selectedWild : restoringBattle.opponents().get(enemySlot);
        if (restoringBattle != null) enemy = new PartyMember(enemy.species(), enemy.shiny(), enemy.alpha(), restoringBattle.state().enemyHp(), restoringBattle.state().enemyMaxHp(), enemy.level(), enemy.individual());
        trainerIntroduction = trainerBattle && enemy.individual() != null
                && enemy.individual().originalTrainerId() > 0 && enemy.individual().originalTrainerId() < 743;
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
        if (trainerIntroduction) encounter.trainerIntroduction();
        if (restoringBattle == null) encounter.configureParty(snapshot.stream().mapToInt(PartyMember::hp).toArray(), snapshot.indexOf(self));
        else encounter.restoreParty(snapshot.stream().mapToInt(PartyMember::hp).toArray(), restoringBattle.state().active());
        setVisible(true);
        enemyName.setText(name(enemy) + "  Lv. " + enemy.level());
        selfName.setText(name(self) + "  Lv. " + self.level());
        hp.setText(self.hp() + " / " + self.maxHp());
        selfHp.reset(self.hp()); enemyHp.reset(enemy.hp());
        selfStatus = memberStatus(self); enemyStatus = restoringBattle == null ? memberStatus(enemy) : restoringBattle.state().enemyStatus();
        message.setText("");
        message.setVisible(false);
        for (Button button : actions) button.setVisible(false);
        moveBack.setVisible(false);
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
                BufferedImage trainerImage = null, opponentIcons = null;
                if (trainerBattle) {
                    if (trainerIntroduction) {
                        int trainerId = (int) wild.individual().originalTrainerId();
                        var files = new RomFiles(ClientFiles.data("../roms"));
                        trainerImage = FireRedTrainers.portrait(files.selected(appearance.region() == 1 ? 2 : 1), trainerId);
                    }
                    opponentIcons = PartyIcons.loadStrip(rom, opponents, 0, 0, 0, 1.25f);
                }
                return new Pixels(frontFrames, backFrames, throwsFrames, iconSource.image(ballItem),
                        PartyIcons.loadStrip(rom, snapshot, 0, 0, 0, 1.25f), BattleScene.load(rom), hordeFrames, shiny,
                        trainerImage, opponentIcons);
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
                moveParticles=new BattleParticles(definitions,images);
                if (requestedPreviewMove > 0 && battleTransport == null && encounter.update(now) == WildEncounter.Phase.SELECT_MOVE) {
                    encounter.chooseMove(requestedPreviewMove, now); requestedPreviewMove = 0;
                }
                if(battleTransport!=null && resultToApply!=null && encounter.update(now)==WildEncounter.Phase.MOVE) {
                    encounter.playServerMove(encounter.moveId(),now);eventUntil=now+MoveAnimations.playback(encounter.moveId()).duration();
                }
            } catch(RuntimeException error) {
                clearMovePresentation();
                requestedPreviewMove = 0;
                failure.accept(ClientStrings.get(252003));
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
                if (pixels.enemyTrainer() != null) {
                    enemyTrainer = upload(renderer, pixels.enemyTrainer());
                }
                if (pixels.enemyParty() != null) {
                    Texture opponentTexture = renderer.uploadImage(pixels.enemyParty()); textures.add(opponentTexture);
                    enemyParty = new Image[scriptedOpponents.size()];
                    for (int i = 0; i < enemyParty.length; i++) enemyParty[i] = opponentTexture.getImage(
                            i * 42 + PartyIcons.PADDING, PartyIcons.PADDING, 32, 32, Color.WHITE, false, Texture.Rotation.NONE);
                }
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
                encounter.loaded(restoringBattle == null ? now : now - (trainerIntroduction ? 6000 : 4200));
                restoringBattle = null;
            } catch (RuntimeException error) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Unable to load encounter", error);
                boolean recovering = restoringBattle != null; Runnable disconnected = recoveryFailure;
                close(); if (recovering) disconnected.run(); else finished.run();
                failure.accept(ClientStrings.get(252003)); return;
            }
        }
        if (pending != null && now - requested > 30000) {
            boolean recovering = restoringBattle != null; Runnable disconnected = recoveryFailure;
            close(); if (recovering) disconnected.run(); else finished.run();
            failure.accept(ClientStrings.get(252003)); return;
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
                selfStatus = memberStatus(pixels.member());
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
                enemyStatus = memberStatus(enemy);
                enemyHp.reset(enemy.hp());
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
        if (self != null) hp.setText(selfHp.displayed(now) + " / " + self.maxHp());
        if(battleState!=null && battleState.outcome()!=0 && resultToApply==null && now>=eventUntil && encounter.update(now)!=WildEncounter.Phase.ENDING && encounter.update(now)!=WildEncounter.Phase.CLOSED)encounter.finish(now);
        WildEncounter.Phase phase = encounter.update(now);
        if (phase == WildEncounter.Phase.CLOSED) { close(); finished.run(); return; }
        partyChoice.setHardVisible(phase == WildEncounter.Phase.SELECT_PARTY);
        partyChoice.refresh();
        boolean ready = phase == WildEncounter.Phase.READY && (battleTransport == null
                || pendingBattle == null && resultToApply == null && (battleState == null || battleState.outcome() == 0));
        boolean wasReady = actions[0].isVisible();
        for (Button action : actions) { action.setVisible(ready); action.setEnabled(ready && pendingEnemy == null && pendingBattle==null && resultToApply==null && (battleState==null||battleState.outcome()==0)); }
        if (scripted || battleTransport!=null) actions[1].setEnabled(false);
        if((trainerBattle||escapeLocked) && battleTransport!=null)actions[3].setEnabled(false);
        opponentChoice.setVisible(battleTransport==null && scriptedOpponents.size() > 1 && phase.ordinal() >= WildEncounter.Phase.READY.ordinal());
        opponentChoice.setEnabled(ready && pendingEnemy == null);
        boolean selecting = phase == WildEncounter.Phase.SELECT_MOVE;
        boolean wasSelecting = moveSlots[0].isVisible();
        testMove.setEnabled(pendingBattle==null && resultToApply==null);
        moveBack.setVisible(selecting); testMove.setVisible(selecting); testMoveLabel.setVisible(selecting);
        refreshMoveSlots();
        for (var slot : moveSlots) slot.setVisible(selecting);
        if (selecting && !wasSelecting) for (var slot : moveSlots) if (slot.isEnabled()) { slot.requestKeyboardFocus(); break; }
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
        refreshStatusLabels();
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
        maximumScale = Math.max(1f, theme.getParameter("maximumScale", 2f));
        customMoveWidth = theme.getParameter("customMoveWidth", 200);
        customMoveGap = theme.getParameter("customMoveGap", 10);
        moveBackWidth = theme.getParameter("moveBackWidth", 88);
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
        hpAnimationMinimum = Math.max(1, theme.getParameter("hpAnimationMinDuration", 350));
        hpAnimationMaximum = Math.max(hpAnimationMinimum, theme.getParameter("hpAnimationMaxDuration", 1000));
        enemyHealthX = theme.getParameter("enemyHealthX", 70); selfHealthX = theme.getParameter("selfHealthX", 80);
        partyIconSize = theme.getParameter("partyIconSize", 36); partyIconGap = theme.getParameter("partyIconGap", 4);
        experienceBackground = theme.getImage("experienceBackground"); experienceImage = theme.getImage("experienceImage");
    }

    @Override protected void layout() {
        scale = Math.min(maximumScale, Math.min(getWidth() / 1024f, getHeight() / 768f));
        left = getX() + (getWidth()-size(1024))/2; top = getY();
        enemyHudX = x(134); enemyHudY = y(148); selfHudX = x(595); selfHudY = y(353);
        placeName(enemyName, enemyGender, enemyHudX+size(50), enemyHudY-size(22), size(257));
        placeName(selfName, selfGender, selfHudX+size(65), selfHudY-size(22), size(234));
        enemyStatusLabel.setPosition(enemyHudX+size(8), enemyHudY); enemyStatusLabel.setSize(size(44),size(20));
        selfStatusLabel.setPosition(selfHudX+size(15), selfHudY); selfStatusLabel.setSize(size(44),size(20));
        hp.setPosition(selfHudX+size(80), selfHudY+size(16)); hp.setSize(size(200), size(24));
        partyChoice.setPosition(x(234), y(150)); partyChoice.setSize(size(560), size(318));
        for (int i = 0; i < moveSlots.length; i++) {
            moveSlots[i].setPosition(x(144+i%2*(actionWidth+actionGap)), y(footerTop+10)+i/2*(controlHeight()+size(actionGap)));
            moveSlots[i].setSize(size(actionWidth), controlHeight());
        }
        int customX = 144 + 2 * (actionWidth + actionGap) + customMoveGap;
        testMoveLabel.setPosition(x(customX), y(footerTop+10)); testMoveLabel.setSize(size(customMoveWidth), size(26));
        testMove.setPosition(x(customX), y(footerTop+10)+controlHeight()+size(actionGap)); testMove.setSize(size(customMoveWidth), size(30));
        moveBack.setPosition(x(884-moveBackWidth), y(footerTop+10)+controlHeight()+size(actionGap)); moveBack.setSize(size(moveBackWidth), size(30));
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
            long introductionElapsed = elapsed;
            if (trainerIntroduction) elapsed = Math.max(1300, elapsed - 1800);
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
            if (trainerIntroduction && introductionElapsed < 3500 && enemyTrainer != null) {
                float retreat = Math.max(0, Math.min(1, (introductionElapsed - 3000) / 500f));
                sprite(enemyTrainer, enemyX + retreat * 240, 311, 64, 2.5f);
                if (introductionElapsed >= 3100) {
                    float progress = Math.min(1, (introductionElapsed - 3100) / 400f);
                    ball.draw(getAnimationState(), x(enemyX - 11), y(220 + 60 * progress), size(22), size(22));
                }
            }
            float enemyRelease = trainerIntroduction ? Math.max(0, Math.min(1, (introductionElapsed - 3500) / 300f)) : 1;
            if(enemyHp.value(now)>0 && enemyRelease > 0) actorSprite(gui,front[(int) (Math.max(0, elapsed - 2000) * 30 / 1000) % front.length], enemyX, 311 + enemyPose.dy(), frontBottom, frontZoom * enemyPose.scale() * formScale(enemy, false) * enemyRelease, frontCenter, enemyAttack);
                if (enemy.shiny() && shinyParticles != null && enemyRelease >= 1)
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
            } else if (phase != WildEncounter.Phase.SWITCH_LOADING && selfHp.value(now)>0) {
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
                healthImage.draw(getAnimationState(), enemyHudX + size(enemyHealthX+1), enemyHudY + size(10), size((float)((healthWidth-2f)*enemyHp.value(now)/Math.max(1,enemy.maxHp()))), size(6));
                if (trainerBattle && enemyParty != null) for (int i = 0; i < enemyParty.length; i++) {
                    float tint = enemyFainted[i] ? .35f : 1;
                    gui.getRenderer().pushGlobalTintColor(tint, tint, tint, 1);
                    try { enemyParty[i].draw(getAnimationState(), enemyHudX + size(50+i*(partyIconSize+partyIconGap)),
                            enemyHudY + size(32), size(partyIconSize), size(partyIconSize)); }
                    finally { gui.getRenderer().popGlobalTintColor(); }
                }
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
                healthImage.draw(getAnimationState(), selfHudX + size(selfHealthX+1), selfHudY + size(9), size((float)((healthWidth-2f) * selfHp.value(now) / Math.max(1,self.maxHp()))), size(6));
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

    private boolean navigateGrid(Event event, Button source, Button[] grid) {
        if (!event.isKeyPressedEvent()) return false;
        if (event.getKeyCode() == Event.KEY_J) {
            if (!event.isKeyRepeated() && source.isEnabled()) source.getModel().fireActionCallback();
            return true;
        }
        int bit = switch (event.getKeyCode()) {
            case Event.KEY_LEFT, Event.KEY_RIGHT -> 1;
            case Event.KEY_UP, Event.KEY_DOWN -> 2;
            default -> 0;
        };
        if (bit == 0) return false;
        int current = java.util.Arrays.asList(grid).indexOf(source);
        if (current < 0) return false;
        int next = current ^ bit;
        if (!grid[next].isVisible() || !grid[next].isEnabled()) next ^= bit == 1 ? 2 : 1;
        if (grid[next].isVisible() && grid[next].isEnabled()) grid[next].requestKeyboardFocus();
        return true;
    }

    private final class MoveButton extends Button {
        private final Label title = passiveLabel("title"), category = passiveLabel("description"), pp = passiveLabel("pp");
        private final Widget type = new Widget();
        private BattlePackets.MoveOption option;

        MoveButton() {
            setText(""); type.setTheme(""); type.setCanAcceptKeyboardFocus(false);
            add(title); add(category); add(pp); add(type);
        }

        void configure(BattlePackets.MoveOption value) {
            if (java.util.Objects.equals(option, value) && !title.getText().isEmpty()) return;
            option = value;
            title.setText(value == null ? "--" : ClientStrings.get(value.id() == 0 ? 9832 : 110000 + value.id()));
            pp.setText(value == null ? "" : ClientStrings.get(9357) + ": " + value.pp() + " / " + value.maxPp());
            PokemonCatalog.LearnedMove details = null;
            if (value != null) for (int species = 1; species <= PokemonCatalog.MAX_SPECIES && details == null; species++) {
                for (var learned : PokemonCatalog.get(species).moves()) if (learned.id() == value.id()) { details = learned; break; }
            }
            category.setText(details == null ? "" : ClientStrings.get(details.category() == 0 ? 9377 : details.category() == 1 ? 9375 : 9376));
            String tooltip = null;
            if (value != null) {
                var text = new StringBuilder(title.getText());
                if (details != null) text.append('\n').append(ClientStrings.get(1706)).append(": ")
                        .append(ClientStrings.get(230000 + (details.type() == 17 ? 9 : details.type() >= 9 ? details.type() + 1 : details.type()))).append(" / ").append(category.getText())
                        .append('\n').append(ClientStrings.get(9355)).append(": ").append(details.power() == 0 ? "--" : details.power())
                        .append("    ").append(ClientStrings.get(9356)).append(": ")
                        .append(details.accuracy() == 0 || details.accuracy() > 100 ? "--" : details.accuracy() + "%");
                text.append('\n').append(pp.getText());
                try {
                    String description = ClientStrings.get(120000 + value.id());
                    text.append("\n\n").append(description.replaceAll("([^\\n]{28})(?=[^\\n])", "$1\n"));
                }
                catch (IllegalArgumentException missing) { }
                tooltip = text.toString();
            }
            setTooltipContent(tooltip);
            for (Widget child : new Widget[]{title, category, pp, type}) child.setTooltipContent(tooltip);
            type.setVisible(details != null);
            if (details != null) { type.setTheme("/pokemon-summary.type-" + details.type()); type.reapplyTheme(); }
        }

        @Override protected boolean handleEvent(Event event) {
            return navigateGrid(event, this, moveSlots) || super.handleEvent(event);
        }

        @Override protected void layout() {
            super.layout();
            int padding = size(6), line = (getHeight() - padding * 2) / 2;
            title.setPosition(getX() + padding, getY() + padding); title.setSize(getWidth() - size(65), line);
            type.setPosition(getRight() - size(55), getY() + padding); type.setSize(size(49), size(14));
            category.setPosition(getX() + padding, getY() + padding + line); category.setSize(size(90), line);
            pp.setPosition(getX() + size(95), getY() + padding + line); pp.setSize(getWidth() - size(101), line);
        }
    }

    private Label passiveLabel(String theme) {
        var label = new Label() { @Override protected boolean handleEvent(Event event) { return false; } };
        label.setTheme(theme); label.setCanAcceptKeyboardFocus(false); return label;
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

        @Override protected boolean handleEvent(Event event) {
            return navigateGrid(event, this, actions) || super.handleEvent(event);
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
        if (super.handleEvent(event)) return true;
        if (active() && event.isKeyPressedEvent() && event.getKeyCode() == Event.KEY_ESCAPE
                && encounter.update(now) == WildEncounter.Phase.SELECT_MOVE) {
            cancelMoveSelection();
            return true;
        }
        if (active() && event.isKeyPressedEvent() && event.getKeyCode() == Event.KEY_ESCAPE
                && encounter.update(now) == WildEncounter.Phase.SELECT_PARTY) {
            if(battleState==null||!battleState.forceSwitch()) encounter.cancelParty(now);
            return true;
        }
        return active();
    }

    @Override public void close() {
        restoringBattle = null;
        recoveryFailure = () -> { };
        if(battleState!=null)lastOutcome=battleState.outcome();
        battleTransport=null;pendingBattle=null;battleState=resultToApply=null;serverEvents.clear();serverMessage="";enemyCasting=false;
        loggedEvents.clear(); suppressBattleLog=false; selfStatus=enemyStatus=lastMoveSide=0; refreshStatusLabels(); selfHp.reset(0); enemyHp.reset(0);
        pendingTestMove = false;
        selectingServerMove = true;
        testMove.setModel(new SimpleChangableListModel<>(customMoveLabels()));
        testMove.setSelected(0); customMoveOverride = requestedPreviewMove = 0; selectingServerMove = false;
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
        selectedMove=MovePresentation.TACKLE;testMove.setVisible(false);testMoveLabel.setVisible(false);
        for (var slot : moveSlots) slot.setVisible(false);
        front = back = trainer = party = null;
        enemyTrainer = null; enemyParty = null; enemyFainted = new boolean[0]; trainerIntroduction = false;
        ball = releaseGlow = null; self = enemy = null; encounter = null;
        setVisible(false);
    }
}
