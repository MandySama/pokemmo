package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import org.lwjgl.glfw.GLFW;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.protocol.PartyMember;
import top.totodile.pokemmo.protocol.PokemonCatalog;
import java.nio.file.Path;
import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.protocol.CharacterPackets.Position;
import top.totodile.pokemmo.protocol.ItemCatalog;
import top.totodile.pokemmo.rom.FireRedAtlas;
import top.totodile.pokemmo.world.*;
import top.totodile.pokemmo.world.StoryReplayCatalog.Data;

import java.io.IOException;
import java.util.*;

/** 独立离线世界承载调试演出，退出只释放副本，不修改在线世界的位置与资源。 */
final class StoryReplayWindow extends Widget implements AutoCloseable {
    private final StoryReplayCatalog catalog;
    private final NpcDialog dialog = new NpcDialog();
    private final WildEncounterPanel battle = new WildEncounterPanel(this::battleFinished, this::battleFailed);
    private Path battleRom;
    private AvatarAssets battleAssets;
    private List<PartyMember> liveParty = List.of();
    private java.util.function.Consumer<String> battleFailure = text -> { };
    private final DialogLayout controls = new DialogLayout();
    private final Label title = new Label();
    private final Button exit = new Button(ClientStrings.get(9532));
    private final Button next = new Button(ClientStrings.get(9533));
    private final Button[] choices = {new Button(), new Button(), new Button()};
    private KantoWorld shadow;
    private KantoWorld source;
    private StoryReplay runtime;
    private StoryReplay.Event event;
    private String characterName;
    private int choiceCount;
    private boolean suppressClose;
    private java.util.function.Consumer<Boolean> completion;
    private boolean completedNormally;
    private long nextStep;
    private long autoPageRevision = -1, autoAdvanceAt;
    private final List<Walk> walks = new ArrayList<>();
    private final Map<Integer, Actor> actors = new LinkedHashMap<>();

    private static final class Actor {
        final int id, graphics, elevation;
        int x, y, facing;
        Actor(int id, int graphics, int elevation, int x, int y, int facing) {
            this.id = id; this.graphics = graphics; this.elevation = elevation;
            this.x = x; this.y = y; this.facing = facing;
        }
    }
    private record Walk(int actor, Deque<int[]> route) { }

    StoryReplayWindow() throws IOException {
        catalog = new StoryReplayCatalog();
        setTheme(""); setVisible(false); setCanAcceptKeyboardFocus(false);
        controls.setTheme("/story-replay-controls");
        var row = controls.createSequentialGroup().addWidget(next);
        for (int i = 0; i < choices.length; i++) {
            final int choice = i;
            choices[i].addCallback(() -> choose(choice));
            row.addGap(8).addWidget(choices[i]);
        }
        controls.setHorizontalGroup(controls.createParallelGroup()
                .addGroup(controls.createSequentialGroup().addWidget(title).addGap(16).addWidget(exit))
                .addGroup(row));
        var buttons = controls.createParallelGroup().addWidget(next);
        for (var button : choices) buttons.addWidget(button);
        controls.setVerticalGroup(controls.createSequentialGroup()
                .addGroup(controls.createParallelGroup().addWidget(title).addWidget(exit)).addGap(8).addGroup(buttons));
        add(battle); add(controls); add(dialog);
        exit.addCallback(this::close);
        next.addCallback(this::advancePage);
        dialog.setOnAdvance(this::advancePage);
        dialog.setOnClose(() -> { if (!suppressClose && shadow != null) advance(0); });
    }

    boolean active() { return shadow != null; }
    void onCompletion(java.util.function.Consumer<Boolean> completion) { this.completion = completion; }
    boolean battleActive() { return battle.active(); }
    void setBattleContext(Path rom, List<PartyMember> party, java.util.function.Consumer<String> failure) {
        battleRom = rom; liveParty = List.copyOf(party); battleFailure = failure;
    }
    KantoWorld source() { return source; }
    StoryReplayCatalog catalog() { return catalog; }
    KantoWorld replayWorld() { return shadow; }

    void start(String sceneId, KantoWorld original, AvatarAssets assets, String name, long now) throws IOException {
        close();
        var scene = catalog.scene(sceneId);
        source = original; characterName = name;
        try {
            battleAssets = assets;
            runtime = new StoryReplay(catalog, scene, original.appearance().gender());
            shadow = new KantoWorld(original.atlas(), assets, original.appearance());
            shadow.setReplayMode();
            selectMap(scene.text("map"), scene.number("x"), scene.number("y"), 1, now);
            if (scene.number("npc") > 0) addActor(scene.number("npc"), now);
            title.setText(ClientStrings.get(9531) + " · " + ClientStrings.get(scene.number("name")));
            setVisible(true);
            updateButtons();
        } catch (IOException | RuntimeException error) { close(); throw error; }
    }

    private void selectMap(String key, int x, int y, int facing, long now) throws IOException {
        var map = catalog.map(key);
        var floor = source.atlas().load(new FireRedAtlas.Key(map.number("group"), map.number("map")), true);
        x = Math.max(0, Math.min(floor.width() / 16 - 1, x));
        y = Math.max(0, Math.min(floor.height() / 16 - 1, y));
        shadow.restore(new Position(map.number("group"), map.number("map"), x, y, floor.elevation(x, y) + 1, facing));
        shadow.clearReplayActors(); actors.clear();
        for (var entries : runtime.chapter().child("actors").children().values()) {
            for (var actor : entries) if (actor.text("map").equals(key)) addActor(actor.number("entityIdx") + 1, now);
        }
        shadow.beginEntry(now);
    }

    private Actor addActor(int id, long now) throws IOException {
        if (actors.containsKey(id)) return actors.get(id);
        var p = shadow.position();
        var npc = source.atlas().npcs(new FireRedAtlas.Key(p.group(), p.map())).stream().filter(n -> n.id() == id).findFirst()
                .orElseThrow(() -> new IOException("Replay NPC missing: " + id));
        int graphics = npc.graphics();
        if (graphics >= 0xf0) {
            var original = catalog.map(runtime.map()).entries("npcs").stream()
                    .filter(n -> n.number("entityIdx") + 1 == id).findFirst().orElseThrow();
            graphics = original.number("graphicsId");
            if (graphics >= 0xf0) throw new IOException("Unresolved replay actor graphics");
        }
        var actor = new Actor(id, graphics, npc.elevation(), npc.x(), npc.y(), npc.direction());
        actors.put(id, actor);
        shadow.replayActor(id, actor.graphics, actor.elevation, actor.x, actor.y, actor.x, actor.y, actor.facing, now);
        return actor;
    }

    private int actorId(String name) {
        var actor = runtime.chapter().child("actors").child(name);
        if (!actor.text("map").equals(runtime.map())) throw new IllegalArgumentException("Replay actor on another map");
        return actor.number("entityIdx") + 1;
    }

    private void advance(int choice) {
        try {
            walks.clear(); choiceCount = 0; autoPageRevision = -1;
            boolean failedBattle = event != null && (event.type().equals("START_BATTLE") || event.type().equals("START_TRAINER_BATTLE")) && choice != 0;
            event = runtime.next(choice);
            if (event == null) { completedNormally = !failedBattle; close(); return; }
            var action = event.action();
            long now = System.nanoTime();
            String text = event.text() > 0 ? ClientStrings.get(event.text()) : "";
            switch (event.type()) {
                case "DIALOGUE", "NOTIFY" -> showText(text);
                case "YES_NO" -> { showText(text); options(ClientStrings.get(50), ClientStrings.get(51)); }
                case "SELECT_STARTER" -> {
                    var starters = runtime.chapter().child("resources").entries("starters");
                    showText(String.join("\n\n", starters.stream().map(s -> ClientStrings.get(s.number("prompt"))).toList()));
                    options(ClientStrings.get(9549), ClientStrings.get(9550), ClientStrings.get(9551));
                }
                case "START_BATTLE", "START_TRAINER_BATTLE" -> {
                    battle.startScripted(battleRom, battleAssets, source.appearance(), replayParty(), opponentTeam(),
                            event.type().equals("START_TRAINER_BATTLE") || action.text("battleType").equals("OPENING_RIVAL"),
                            now / 1_000_000L);
                }
                case "MOVE_PLAYER" -> walkPlayer(action, "x", "y");
                case "MOVE_NPC" -> {
                    int id = actorId(action.text("actor")); var actor = addActor(id, now);
                    Data target = action.child("target");
                    walks.add(new Walk(id, route(actor.x, actor.y, coordinate(target, "x"), coordinate(target, "y"))));
                    nextStep = now;
                }
                case "GUIDE" -> {
                    int id = actorId(action.text("actor")); var actor = addActor(id, now);
                    walks.add(new Walk(id, route(actor.x, actor.y, action.number("npcX"), action.number("npcY"))));
                    walkPlayer(action, "playerX", "playerY");
                }
                case "CHANGE_MAP" -> {
                    selectMap(action.text("map"), action.number("x"), action.number("y"), action.number("toward", 1), now);
                    nextStep = now + 500_000_000L;
                }
                case "GRANT_ITEM", "REMOVE_ITEM" -> showText(ClientStrings.get(event.type().equals("GRANT_ITEM") ? 9537 : 9538)
                        .replace("{00}", itemName(action.number("itemId"))).replace("{01}", action.text("amount")));
                case "GRANT_POKEMON" -> showText(ClientStrings.get(9539).replace("{00}", pokemonName(action.number("species"))).replace("{01}", action.text("level")));
                case "HEAL_PARTY" -> showText(ClientStrings.get(9540));
                case "PARCEL_OPERATION" -> showText(ClientStrings.get(switch (action.text("operation")) {
                    case "PICKUP" -> 9541; case "DELIVER" -> 9542; case "RECEIVE_POKEDEX" -> 9543;
                    default -> throw new IllegalArgumentException("Unknown parcel operation");
                }));
                case "START_SAFARI" -> showText(ClientStrings.get(9544).replace("{00}", action.text("steps")).replace("{01}", action.text("balls")));
                case "PLAY_MUSIC" -> showText(ClientStrings.get(9547).replace("{00}", action.text("musicId")));
                case "REJECT_MOVE" -> showText(ClientStrings.get(9546));
                default -> throw new IllegalArgumentException("Unsupported replay event " + event.type());
            }
            updateButtons();
        } catch (IOException | RuntimeException error) {
            System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Story replay failed", error);
            close();
            throw new IllegalStateException("Story replay failed", error);
        }
    }

    private static PartyMember temporaryPokemon(int species, int level) {
        int hp = PokemonCatalog.stat(species, level, 0, 0, 15, 0);
        return new PartyMember(species, false, false, hp, hp, level);
    }

    private List<PartyMember> replayParty() {
        List<PartyMember> result = new ArrayList<>(liveParty);
        for (int species : runtime.pokemon()) {
            if (result.size() == 6) break;
            result.add(temporaryPokemon(species, 5));
        }
        if (result.stream().noneMatch(member -> member.hp() > 0))
            result = new ArrayList<>(List.of(temporaryPokemon(new int[]{1, 4, 7}[Math.min(2, runtime.starter())], 5)));
        return List.copyOf(result);
    }

    private List<PartyMember> opponentTeam() {
        var team = event.action().entries("team").stream()
                .map(member -> temporaryPokemon(member.number("species"), member.number("level"))).toList();
        if (!team.isEmpty()) return team;
        var resources = runtime.chapter().child("resources");
        boolean capture = event.action().text("battleType").equals("CAPTURE_TUTORIAL");
        int species = capture ? resources.number("wildSpecies")
                : resources.entries("starters").get(Math.min(2, runtime.starter())).number("rivalSpecies");
        return List.of(temporaryPokemon(species, capture ? resources.number("wildLevel", 5) : 5));
    }

    private void battleFinished() {
        if (shadow == null) return;
        showText(ClientStrings.get(9545));
        options(ClientStrings.get(9534), ClientStrings.get(9535));
        updateButtons();
    }

    private void battleFailed(String message) {
        close();
        battleFailure.accept(message);
    }

    private static String pokemonName(int id) { return ClientStrings.get(150000 + id); }
    private static String itemName(int id) {
        try { return ClientStrings.get(ItemCatalog.get(id).nameId()); }
        catch (IllegalArgumentException ignored) { return "#" + id; }
    }
    private void showText(String text) {
        int choice = Math.min(2, runtime.starter());
        int species = new int[]{1, 4, 7}[choice];
        if (event != null && event.text() == 1629522) species = new int[]{4, 7, 1}[choice];
        dialog.open(List.of(KantoDialogueText.format(text, characterName,
                Map.of(0, new top.totodile.pokemmo.rom.FireRedDialogue.Buffer(0x7d, species)), replayParty())));
    }
    private void options(String... labels) {
        choiceCount = labels.length;
        for (int i = 0; i < labels.length; i++) choices[i].setText(labels[i]);
    }
    private int coordinate(Data data, String field) {
        return data.text(field).equals("PLAYER_X") ? shadow.position().x() : data.text(field).equals("PLAYER_Y") ? shadow.position().y() : data.number(field);
    }
    private void walkPlayer(Data action, String x, String y) throws IOException {
        var p = shadow.position();
        walks.add(new Walk(-1, route(p.x(), p.y(), coordinate(action, x), coordinate(action, y))));
        nextStep = System.nanoTime();
    }
    private Deque<int[]> route(int x, int y, int tx, int ty) throws IOException {
        var position = shadow.position();
        var floor = source.atlas().load(new FireRedAtlas.Key(position.group(), position.map()), true);
        int width = floor.width() / 16, height = floor.height() / 16;
        if (tx < 0 || ty < 0 || tx >= width || ty >= height) throw new IOException("Replay movement target outside map");
        int start = y * width + x, end = ty * width + tx;
        int[] previous = new int[width * height]; Arrays.fill(previous, -1);
        Deque<Integer> queue = new ArrayDeque<>(); queue.add(start); previous[start] = start;
        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        while (!queue.isEmpty() && previous[end] < 0) {
            int at = queue.remove();
            for (var d : directions) {
                int nx = at % width + d[0], ny = at / width + d[1];
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                int next = ny * width + nx;
                if (previous[next] >= 0 || next != end && !floor.walkable(nx, ny)) continue;
                previous[next] = at; queue.add(next);
            }
        }
        if (previous[end] < 0) throw new IOException("No replay movement route");
        Deque<int[]> result = new ArrayDeque<>();
        for (int at = end; at != start; at = previous[at]) result.addFirst(new int[]{at % width, at / width});
        return result;
    }

    void tick(long now) {
        if (shadow == null) return;
        if (battle.active()) {
            battle.tick(now / 1_000_000L);
            updateButtons();
            return;
        }
        shadow.update(now);
        // 先完成触发位置的落地淡入，再开始执行第一条剧情。
        if (event == null) {
            if (!shadow.replayMoving()) advance(0);
            return;
        }
        if (!walks.isEmpty() && now >= nextStep && !shadow.replayMoving()) {
            boolean moved = false;
            for (var walk : walks) {
                if (walk.route().isEmpty()) continue;
                int[] p = walk.route().remove(); moved = true;
                if (walk.actor() == -1) shadow.replayPlayerStep(p[0], p[1], now);
                else {
                    var actor = actors.get(walk.actor());
                    int direction = p[0] < actor.x ? 2 : p[0] > actor.x ? 3 : p[1] < actor.y ? 1 : 0;
                    shadow.replayActor(actor.id, actor.graphics, actor.elevation, p[0], p[1], actor.x, actor.y, direction, now);
                    actor.x = p[0]; actor.y = p[1]; actor.facing = direction;
                }
            }
            nextStep = now + 275_000_000L;
            if (!moved) advance(0);
        } else if (event != null && event.type().equals("CHANGE_MAP") && now >= nextStep && !shadow.replayMoving()) advance(0);
        if (shadow != null && dialog.pageDisplayed()) {
            if (autoPageRevision != dialog.pageRevision()) {
                autoPageRevision = dialog.pageRevision();
                autoAdvanceAt = now + dialog.autoAdvanceDelayNanos();
            } else if (now >= autoAdvanceAt && (choiceCount == 0 || !dialog.lastPage())) {
                advancePage();
                autoPageRevision = -1;
            }
        }
        updateButtons();
    }
    private void updateButtons() {
        next.setVisible(false);
        for (int i = 0; i < choices.length; i++) {
            choices[i].setVisible(!battle.active() && i < choiceCount);
            choices[i].setEnabled(dialog.lastPage());
        }
        controls.invalidateLayout();
    }
    private void advancePage() { if (dialog.isVisible() && (choiceCount == 0 || !dialog.lastPage())) dialog.advance(); }
    private void choose(int choice) {
        if (!dialog.isVisible() || !dialog.lastPage() || choice >= choiceCount) return;
        suppressClose = true; dialog.close(); suppressClose = false;
        advance(choice);
    }
    void key(int key, int action) {
        if (battle.active() && key != GLFW.GLFW_KEY_ESCAPE) {
            int translated = GlfwKeyMap.translate(key);
            if (translated != Event.KEY_NONE) getGUI().handleKey(translated, Event.CHAR_NONE, action != GLFW.GLFW_RELEASE);
            return;
        }
        if (action != GLFW.GLFW_PRESS) return;
        if (key == GLFW.GLFW_KEY_ESCAPE) close();
        else if (key == GLFW.GLFW_KEY_J || key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_SPACE) advancePage();
        else if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_3) choose(key - GLFW.GLFW_KEY_1);
    }

    @Override protected void layout() {
        battle.setPosition(getInnerX(), getInnerY()); battle.setSize(getInnerWidth(), getInnerHeight());
        dialog.setPosition(getInnerX(), getInnerY()); dialog.setSize(getInnerWidth(), getInnerHeight());
        controls.setSize(Math.min(900, getInnerWidth() - 24), controls.getPreferredHeight());
        controls.setPosition(getInnerX() + (getInnerWidth() - controls.getWidth()) / 2, getInnerY() + 76);
    }
    @Override public void close() {
        var callback = completion;
        completion = null;
        boolean completed = completedNormally;
        completedNormally = false;
        battle.close();
        battleAssets = null;
        suppressClose = true; dialog.close(); suppressClose = false;
        if (shadow != null) shadow.close();
        shadow = null; source = null; runtime = null; event = null; walks.clear(); actors.clear(); choiceCount = 0;
        autoPageRevision = -1; autoAdvanceAt = 0;
        setVisible(false);
        if (callback != null) callback.accept(completed);
    }
}
