package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.protocol.EncounterCatalog;
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
    private record Pixels(List<BufferedImage> front, List<BufferedImage> back,
                          List<BufferedImage> trainer, BufferedImage ball, BufferedImage party, List<BufferedImage> impact, BattleScene.Resources scene, List<List<BufferedImage>> horde, List<BattleParticles.Definition> shiny) { }

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
    private final List<Texture> moveTextures = new ArrayList<>();
    private Image[] impact;
    private Color impactColor = Color.WHITE;
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
    private int hordeHudWidth = 120, hordeHudGap = 12, hordeHudTop = 86;
    private int hordeCenter = 740, hordeSpacing = 40;
    private BattleParticles shinyParticles;
    private Image[] front, back, trainer, party;
    private Image ball, releaseGlow, white, enemyHud, selfHud, healthImage, healthBackground;
    private Image experienceBackground, experienceImage;
    private float experienceFraction;
    private int actionWidth = 209, actionHeight = 52, actionGap = 2;
    private int healthWidth = 200, enemyHealthX = 70, selfHealthX = 80;
    private int partyIconSize = 36, partyIconGap = 4;
    private int frontBottom, backBottom;
    private float frontCenter, backCenter, frontZoom, backZoom;
    private float gigantamaxFrontScale = 1, gigantamaxBackScale = 1;
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
                    encounter.choose(WildEncounter.Action.values()[index], now);
                    if (encounter.update(now) == WildEncounter.Phase.BAG) openBag.run();
                }
            });
            add(actions[i]);
        }
        move.setTheme("move");
        moveBack.setTheme("move");
        move.addCallback(() -> { if (encounter != null) encounter.chooseMove(selectedMove, now); });
        moveBack.addCallback(() -> { if (encounter != null) encounter.cancelMove(now); });
        testMove.setTheme("test-move");testMove.setTooltipContent(ClientStrings.get(252009));
        testMove.setSelected(testMoveIds.indexOf(selectedMove));
        testMove.addCallback(() -> {
            int index=testMove.getSelected();
            if (index>=0) replaceTestMove(testMoveIds.get(index));
        });
        add(move); add(moveBack); add(testMove); add(testMoveLabel); testMoveLabel.setTheme("test-label");testMoveLabel.setVisible(false);testMove.setVisible(false);
        move.setVisible(false); moveBack.setVisible(false);
        for (Label label : new Label[]{enemyName, selfName, hp, message}) { label.setTheme("label"); add(label); }
        enemyName.setTheme("name"); selfName.setTheme("name"); hp.setTheme("name"); message.setTheme("message");
        add(enemyGender); add(selfGender);
        for (int i = 0; i < hordeNames.length; i++) {
            hordeNames[i] = new Label(); hordeNames[i].setTheme("name");
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
        for (var texture : moveTextures) texture.destroy();moveTextures.clear();moveParticles=null;
        selectedMove=id;move.setText(ClientStrings.get(110000+id));
        if (id != MovePresentation.TACKLE) {
            var rom=battleRom;
            pendingMove=CompletableFuture.supplyAsync(() -> {
                try {return BattleParticles.load(rom,id);}catch(IOException error){throw new java.util.concurrent.CompletionException(error);}
            });
            move.setEnabled(false);
        } else move.setEnabled(true);
    }

    boolean active() { return encounter != null; }

    void setBagAction(Runnable action) { openBag = action; }
    void closeBag() { if (encounter != null) encounter.closeBag(now); }
    boolean bagOpen() { return encounter != null && encounter.update(now) == WildEncounter.Phase.BAG; }

    private void chooseParty(int slot) {
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
                        PartyIcons.loadStrip(rom, snapshot, 0, 0, 0, 1.25f), ParticleTextures.load(rom, 195), BattleScene.load(rom), hordeFrames, shiny);
            } catch (IOException exception) { throw new java.util.concurrent.CompletionException(exception); }
        });
        invalidateLayout();
    }

    private void chooseEnemy() {
        int index = opponentChoice.getSelected();
        if (!readyForOutcome() || index < 0 || index >= scriptedOpponents.size()
                || scriptedOpponents.get(index) == enemy) return;
        PartyMember selected = scriptedOpponents.get(index);
        Path rom = battleRom;
        enemyRequested = now;
        pendingEnemy = CompletableFuture.supplyAsync(() -> {
            try { return new EnemyPixels(selected, animation(rom, selected, false, new PokedexData(rom))); }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    private static List<BufferedImage> animation(Path rom, PartyMember member, boolean back, PokedexData data) throws IOException {
        try {
            if (PokemonForms.active(member)) {
                var form = PokemonForms.load(member);
                return back ? form.back() : form.front();
            }
            var costume = PokemonCostumes.image(member, back);
            if (costume != null) return List.of(costume);
            return PokemonAnimation.loadBattle(rom, member.species(), member.shiny(), back).images();
        }
        catch (IOException | RuntimeException unsupported) { return List.of(data.sprite(member.species(), back, member.shiny())); }
    }

    private float formScale(PartyMember member, boolean back) {
        if (!PokemonForms.active(member) || member.individual().form() != 3) return 1;
        return back ? gigantamaxBackScale : gigantamaxFrontScale;
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
                for(var definition:definitions) {
                    var texture=renderer.uploadImage(definition.particle().texture());moveTextures.add(texture);
                    images.add(texture.getImage(0,0,definition.particle().texture().getWidth(),definition.particle().texture().getHeight(),Color.WHITE,false,Texture.Rotation.NONE));
                }
                moveParticles=new BattleParticles(definitions,images);move.setEnabled(true);
            } catch(RuntimeException error) {
                for(var texture:moveTextures)texture.destroy();moveTextures.clear();
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
                impact = new Image[pixels.impact().size()];
                for (int i = 0; i < impact.length; i++) impact[i] = upload(renderer, pixels.impact().get(i));
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
                for (var definition : pixels.shiny()) stars.add(upload(renderer, definition.particle().texture()));
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
        WildEncounter.Phase phase = encounter.update(now);
        if (phase == WildEncounter.Phase.CLOSED) { close(); finished.run(); return; }
        partyChoice.setHardVisible(phase == WildEncounter.Phase.SELECT_PARTY);
        partyChoice.refresh();
        boolean ready = phase == WildEncounter.Phase.READY;
        boolean wasReady = actions[0].isVisible();
        for (Button action : actions) { action.setVisible(ready); action.setEnabled(ready && pendingEnemy == null); }
        if (scripted) actions[1].setEnabled(false);
        opponentChoice.setVisible(scriptedOpponents.size() > 1 && phase.ordinal() >= WildEncounter.Phase.READY.ordinal());
        opponentChoice.setEnabled(ready && pendingEnemy == null);
        boolean selecting = phase == WildEncounter.Phase.SELECT_MOVE;
        boolean wasSelecting = move.isVisible();
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
        impactColor = theme.getParameter("impactColor", Color.WHITE);
        white = theme.getImage("whiteImage"); enemyHud = theme.getImage("enemyHud");
        selfHud = theme.getImage("selfHud"); healthImage = theme.getImage("healthImage");
        healthBackground = theme.getImage("healthBackground");
        hordeHud = theme.getImage("hordeHud");
        hordeHudWidth = theme.getParameter("hordeHudWidth", 120);
        hordeHudGap = theme.getParameter("hordeHudGap", 12);
        hordeHudTop = theme.getParameter("hordeHudTop", 86);
        hordeCenter = theme.getParameter("hordeCenter", 740);
        hordeSpacing = theme.getParameter("hordeSpacing", 40);
        backdropOpacity = theme.getParameter("backdropOpacity", .68f);
        gigantamaxFrontScale = theme.getParameter("gigantamaxFrontScale", 1f);
        gigantamaxBackScale = theme.getParameter("gigantamaxBackScale", 1f);
        actionWidth = theme.getParameter("actionWidth", 209); actionHeight = theme.getParameter("actionHeight", 52);
        actionGap = theme.getParameter("actionGap", 2);
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
        move.setPosition(x(144), y(484)); move.setSize(size(actionWidth), size(actionHeight));
        moveBack.setPosition(x(144 + actionWidth + actionGap), y(484)); moveBack.setSize(size(actionWidth), size(actionHeight));
        testMoveLabel.setPosition(x(144),y(542));testMoveLabel.setSize(size(150),size(30));
        testMove.setPosition(x(294),y(542));testMove.setSize(size(actionWidth*2+actionGap-150),size(30));
        opponentChoice.setPosition(x(554), y(180)); opponentChoice.setSize(size(330), size(28));
        for (int i = 0; i < hordeNames.length; i++) {
            int center = 514 + (hordeColumn(i)-1)*(hordeHudWidth+hordeHudGap);
            placeName(hordeNames[i], hordeGenders[i], x(center-hordeHudWidth/2f+6),
                    y(hordeHudTop+hordeRow(i)*28-18), size(hordeHudWidth-12));
        }
        message.setPosition(x(134), y(474)); message.setSize(size(760), size(116));
        for (int i = 0; i < actions.length; i++) {
            actions[i].setPosition(x(144 + i % 2 * (actionWidth+actionGap)), y(484) + size(i / 2 * (actionHeight+actionGap)));
            actions[i].setSize(size(actionWidth), size(actionHeight));
        }
    }

    private void actorSprite(GUI gui, Image image, float center, float bottom, int foot, float zoom, float anchor, MovePresentation.Actor actor) {
        var renderer=gui.getRenderer();renderer.pushGlobalTintColor(actor.tint(),actor.tint(),actor.tint(),1);
        org.lwjgl.opengl.GL11.glPushMatrix();
        try {
            org.lwjgl.opengl.GL11.glTranslatef(x(center+actor.x()),y(bottom+actor.y()),0);
            org.lwjgl.opengl.GL11.glScalef(actor.scaleX(),actor.scaleY(),1);
            org.lwjgl.opengl.GL11.glTranslatef(-x(center),-y(bottom),0);
            sprite(image,center,bottom,foot,zoom,anchor);
        } finally {org.lwjgl.opengl.GL11.glPopMatrix();renderer.popGlobalTintColor();}
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
        gui.getRenderer().clipEnter(x(134), y(50), size(760), size(540));
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
            scene.drawPlatforms(x(134), y(50), size(760), size(540), getWidth(), getHeight(),
                    Math.max(0, (freezeCamera ? frozenCameraTime : cameraTime)-1300)/1000f, slide);
            long attackTime=casting?encounter.moveElapsed(now):MoveAnimations.get(encounter.moveId()).duration();
            var ownAttack=casting?MovePresentation.actor(encounter.moveId(),attackTime,14):new MovePresentation.Actor(0,0,1,1,1);
            var enemyAttack=casting?MovePresentation.actor(encounter.moveId(),attackTime,16):new MovePresentation.Actor(0,0,1,1,1);
            float dim=casting?MovePresentation.backdrop(encounter.moveId(),attackTime):0;
            if(dim>0) {gui.getRenderer().pushGlobalTintColor(0,0,0,dim);try{white.draw(getAnimationState(),x(134),y(50),size(760),size(540));}finally{gui.getRenderer().popGlobalTintColor();}}
            var attack = MovePresentation.sample(encounter.moveId(), casting ? encounter.moveElapsed(now) : MoveAnimations.get(encounter.moveId()).duration());
            var enemyPose = scene.actorPose(false);
            var ownPose = scene.actorPose(true);
            enemyX += enemyPose.dx() + attack.shake();
            ownX += ownPose.dx();
            if (casting && attack.flash()) gui.getRenderer().pushGlobalTintColor(1, 1, 1, .3f);
            if (hordeSprites.isEmpty()) {
            actorSprite(gui,front[(int) (Math.max(0, elapsed - 2000) * 30 / 1000) % front.length], enemyX, 311 + enemyPose.dy(), frontBottom, frontZoom * enemyPose.scale() * formScale(enemy, false), frontCenter, enemyAttack);
                if (enemy.shiny() && shinyParticles != null)
                    shinyParticles.draw(this, elapsed-2000, x(ownX), y(450), x(enemyX), y(260+enemyPose.dy()), scale*2);
            } else {
                int[] order = hordeSprites.size() == 5 ? new int[]{0,2,4,1,3} : new int[]{0,1,2};
                for (int i : order) {
                    var sprite = hordeSprites.get(i);
                    float center = hordeCenter+(i-(hordeSprites.size()-1)/2f)*hordeSpacing+(1-slide)*700+enemyPose.dx();
                    float floor = 311+(i%2==0?-8:6)+enemyPose.dy();
                    var actor = i == 0 ? enemyAttack : new MovePresentation.Actor(0,0,1,1,1);
                    actorSprite(gui, sprite.frames()[(int)(Math.max(0,elapsed-2000)*30/1000)%sprite.frames().length],
                            center, floor, sprite.bottom(), sprite.zoom()*enemyPose.scale(), sprite.center(), actor);
                    if (wildOpponents.get(i).shiny() && shinyParticles != null)
                        shinyParticles.draw(this,elapsed-2000,x(ownX),y(450),x(center),y(floor-40),scale*1.6f);
                }
            }
            if (casting && attack.flash()) gui.getRenderer().popGlobalTintColor();
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
            } else if (phase != WildEncounter.Phase.SWITCH_LOADING) {
                float release = phase == WildEncounter.Phase.SWITCHING ? Math.min(1,encounter.switchElapsed(now)/600f)
                        : Math.min(1, (elapsed - 3600) / 600f);
                actorSprite(gui,back[(int) (Math.max(0, elapsed - backStarted) * 30 / 1000) % back.length], ownX + (enemyX-ownX)*.32f*attack.advance(), 572 + ownPose.dy() - 55*attack.advance(), backBottom,
                        backZoom * ownPose.scale() * Math.max(.01f, release) * formScale(self, true), backCenter, ownAttack);
                if (release < .4f) {
                    gui.getRenderer().pushGlobalTintColor(1, 1, 1, 1 - release / .4f);
                    try { releaseGlow.draw(getAnimationState(), x(ownX - 100), y(400), size(200), size(200)); }
                    finally { gui.getRenderer().popGlobalTintColor(); }
                }
            }
            if (self.shiny() && shinyParticles != null && elapsed >= 4200)
                shinyParticles.draw(this, elapsed-backStarted-600, x(ownX), y(450), x(ownX), y(450), scale*2);
            if (casting && encounter.moveId() == MovePresentation.TACKLE && attack.alpha() > 0 && impact != null) {
                float cx = enemyX, cy = 311 + enemyPose.dy() - 45*enemyPose.scale();
                float diameter = (65 + 90*attack.impact())*enemyPose.scale();
                gui.getRenderer().pushGlobalTintColor(impactColor.getRed()/255f, impactColor.getGreen()/255f,
                        impactColor.getBlue()/255f, attack.alpha());
                try {
                    impact[2].draw(getAnimationState(), x(cx)-size(diameter/2), y(cy)-size(diameter/2), size(diameter), size(diameter));
                    impact[3].draw(getAnimationState(), x(cx)-size(diameter*.65f), y(cy)-size(diameter*.65f), size(diameter*1.3f), size(diameter*1.3f));
                } finally { gui.getRenderer().popGlobalTintColor(); }
            }
            if (casting && encounter.moveId() != MovePresentation.TACKLE && moveParticles != null)
                moveParticles.draw(this,encounter.moveElapsed(now),x(ownX),y(572+ownPose.dy()-50),x(enemyX),y(311+enemyPose.dy()-45*enemyPose.scale()),scale);
            gui.getRenderer().pushGlobalTintColor(0, 0, 0, .35f);
            try { white.draw(getAnimationState(), x(134), y(474), size(760), size(116)); }
            finally { gui.getRenderer().popGlobalTintColor(); }
            if (elapsed >= 2000 && hordeSprites.isEmpty()) {
                enemyHud.draw(getAnimationState(), enemyHudX, enemyHudY, size(307), size(28));
                healthBackground.draw(getAnimationState(), enemyHudX + size(enemyHealthX), enemyHudY + size(9), size(healthWidth), size(8));
                healthImage.draw(getAnimationState(), enemyHudX + size(enemyHealthX+1), enemyHudY + size(10), size(healthWidth-2), size(6));
            }
            if (elapsed >= 2000 && !hordeSprites.isEmpty()) for (int i = 0; i < hordeSprites.size(); i++) {
                int center = 514+(hordeColumn(i)-1)*(hordeHudWidth+hordeHudGap);
                int hx = x(center-hordeHudWidth/2f), hy = y(hordeHudTop+hordeRow(i)*28);
                hordeHud.draw(getAnimationState(),hx,hy,size(hordeHudWidth),size(14));
                healthBackground.draw(getAnimationState(),hx+size(10),hy+size(5),size(hordeHudWidth-20),size(5));
                healthImage.draw(getAnimationState(),hx+size(11),hy+size(6),size(hordeHudWidth-22),size(3));
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
            addCloseCallback(() -> { if (encounter != null) encounter.cancelParty(now); });
            for (int i = 0; i < cards.length; i++) { cards[i] = new MemberCard(i); add(cards[i]); }
            cancel.setTheme("button");
            cancel.addCallback(() -> { if (encounter != null) encounter.cancelParty(now); });
            add(cancel);
            setHardVisible(false);
        }
        void refresh() {
            for (int i = 0; i < cards.length; i++) {
                boolean present = i < members.size();
                cards[i].setVisible(present);
                cards[i].setEnabled(present && encounter != null && i != encounter.activeSlot() && members.get(i).hp() > 0);
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
            title.setPosition(getX(), getY() + 6); title.setSize(getWidth(), 20);
            description.setPosition(getX(), getY() + 24); description.setSize(getWidth(), 20);
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
            encounter.cancelParty(now);
            return true;
        }
        return active() || super.handleEvent(event);
    }

    @Override public void close() {
        if (pendingEnemy != null) pendingEnemy.cancel(true);
        pendingEnemy = null;
        opponentChoice.setVisible(false);
        scriptedOpponents = List.of(); scripted = trainerBattle = false; frontTexture = null;
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
        for(var texture:moveTextures)texture.destroy();moveTextures.clear();moveParticles=null;
        selectedMove=MovePresentation.TACKLE;testMove.setSelected(testMoveIds.indexOf(selectedMove));move.setText(ClientStrings.get(110033));move.setEnabled(true);testMove.setVisible(false);testMoveLabel.setVisible(false);
        front = back = trainer = party = impact = null;
        ball = releaseGlow = null; self = enemy = null; encounter = null;
        setVisible(false);
    }
}
