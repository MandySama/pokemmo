package top.totodile.pokemmo.world;

import java.util.*;
import top.totodile.pokemmo.world.StoryReplayCatalog.Data;

/** 重播状态完全驻留内存，任何分支与奖励均不接触账号、网络或数据库。 */
public final class StoryReplay {
    public record Event(String type, int text, Data action) { }
    private final StoryReplayCatalog catalog;
    private final Data chapter;
    private final Map<String, Data> nodes;
    private final Map<Integer, Integer> items = new LinkedHashMap<>();
    private final Set<Integer> badges = new HashSet<>(), bits = new HashSet<>(), extraBits = new HashSet<>();
    private final Set<String> flags = new HashSet<>();
    private List<Data> actions = List.of();
    private int cursor, stage, starter, sex, parcel = 3, elite, instructions;
    private boolean champion, healed, ended, captured;
    private String nodeName, map;
    private Event waiting;
    private final List<Integer> pokemon = new ArrayList<>();

    public StoryReplay(StoryReplayCatalog catalog, Data scene, int sex) {
        this.catalog = catalog; this.sex = sex;
        chapter = catalog.chapter(scene.text("chapter")); nodes = catalog.nodes(chapter);
        map = scene.text("map");
        for (var condition : scene.entries("conditions")) seed(condition);
        jump(scene.text("node"));
    }
    public boolean ended() { return ended; }
    public Data chapter() { return chapter; }
    public String map() { return map; }
    public int starter() { return starter; }
    public int stage() { return stage; }
    public Map<Integer, Integer> items() { return Map.copyOf(items); }
    public List<Integer> pokemon() { return List.copyOf(pokemon); }
    public boolean champion() { return champion; }
    public boolean healed() { return healed; }

    private void jump(String node) {
        var data = nodes.get(node);
        if (data == null) throw new IllegalArgumentException("Unknown replay node " + node);
        nodeName = node; actions = data.entries("actions"); cursor = 0;
    }

    public Event next(int choice) {
        if (ended) return null;
        if (waiting != null) {
            var action = waiting.action();
            switch (waiting.type()) {
                case "YES_NO" -> jump(action.text(choice == 0 ? "yes" : "no"));
                case "SELECT_STARTER" -> {
                    if (choice < 0 || choice > 2) throw new IllegalArgumentException("Invalid starter choice");
                    starter = choice; stage = 3;
                    pokemon.add(chapter.child("resources").entries("starters").get(choice).number("species"));
                    jump(action.text("next"));
                }
                case "START_BATTLE", "START_TRAINER_BATTLE" -> {
                    if (choice != 0) { ended = true; waiting = null; return null; }
                    if (action.text("battleType").equals("OPENING_RIVAL")) stage = 4;
                    if (action.text("battleType").equals("CAPTURE_TUTORIAL")) captured = true;
                    jump(action.text("finishNode"));
                }
                case "CHANGE_MAP" -> { map = action.text("map"); mapReady(); }
                default -> { }
            }
            waiting = null;
        }
        for (int budget = 0; budget < 512; budget++) {
            if (++instructions > 8192) throw new IllegalStateException("Replay instruction budget exceeded");
            if (cursor >= actions.size()) {
                String next = nodes.get(nodeName).text("next");
                if (next.isEmpty()) { ended = true; return null; }
                jump(next); continue;
            }
            Data action = actions.get(cursor++);
            String type = action.text("type");
            switch (type) {
                case "GOTO" -> jump(action.text("node"));
                case "BRANCH" -> jump(action.text(condition(action.child("condition")) ? "yes" : "no"));
                case "SET_OPENING_STAGE" -> stage = action.number("nextStage");
                case "SET_STORY_BIT" -> bits.add(action.number("bit"));
                case "SET_EXTRA_STORY_BIT" -> extraBits.add(action.number("bit"));
                case "SET_BADGE" -> badges.add(action.number("bit"));
                case "SET_ELITE_STAGE" -> elite = action.number("stage");
                case "SET_CHAMPION" -> champion = true;
                case "CLOSE_DIALOG" -> { }
                case "RUN_MAP_READY" -> { if (!mapReady()) { ended = true; return null; } }
                case "CLOSE_SCENE" -> { ended = true; return null; }
                case "GRANT_ITEM" -> { items.merge(action.number("itemId"), action.number("amount"), Integer::sum); return waitFor("GRANT_ITEM", 0, action); }
                case "REMOVE_ITEM" -> { items.compute(action.number("itemId"), (id, n) -> Math.max(0, (n == null ? 0 : n) - action.number("amount"))); return waitFor(type, 0, action); }
                case "GRANT_POKEMON" -> { pokemon.add(action.number("species")); return waitFor(type, 0, action); }
                case "HEAL_PARTY" -> { healed = true; return waitFor(type, 0, action); }
                case "PARCEL_OPERATION" -> {
                    switch (action.text("operation")) {
                        case "PICKUP" -> { parcel = 1; items.put(349, 1); }
                        case "DELIVER" -> { parcel = 2; stage = 5; items.remove(349); }
                        case "RECEIVE_POKEDEX" -> { stage = 6; items.merge(5004, 5, Integer::sum); }
                        default -> throw new IllegalArgumentException("Unknown parcel operation");
                    }
                    return waitFor(type, 0, action);
                }
                case "DIALOGUE", "YES_NO" -> { return waitFor(type, chapter.child("text").number(action.text("text")), action); }
                case "NOTIFY" -> { return waitFor(type, action.number("textId"), action); }
                case "SELECT_STARTER", "START_BATTLE", "START_TRAINER_BATTLE", "MOVE_PLAYER", "MOVE_NPC", "GUIDE", "CHANGE_MAP", "PLAY_MUSIC", "START_SAFARI", "REJECT_MOVE" -> { return waitFor(type, 0, action); }
                default -> throw new IllegalArgumentException("Unsupported replay action " + type);
            }
        }
        throw new IllegalStateException("Replay synchronous budget exceeded");
    }
    private Event waitFor(String type, int text, Data action) { return waiting = new Event(type, text, action); }
    private boolean mapReady() {
        for (var trigger : chapter.entries("triggers")) {
            if (trigger.text("type").equals("MAP_READY") && trigger.text("map").equals(map)
                    && trigger.entries("conditions").stream().allMatch(this::condition)) {
                jump(trigger.text("startNode")); return true;
            }
        }
        return false;
    }
    private int conditionValue(Data condition) {
        return switch (condition.text("value")) {
            case "PICKUP" -> 3; case "DELIVER" -> 1; case "POKEDEX" -> 2; case "COMPLETE" -> 4;
            default -> condition.number("value");
        };
    }
    private void seed(Data c) {
        int value = conditionValue(c);
        switch (c.text("type")) {
            case "STORY_STAGE_EQUALS", "STORY_STAGE_AT_LEAST" -> stage = value;
            case "STARTER_EQUALS" -> starter = value;
            case "HAS_ITEM" -> items.put(value, 1);
            case "HAS_CAPTURE_BALL" -> items.put(5004, 1);
            case "BADGE_SET" -> badges.add(value);
            case "STORY_BIT_SET" -> bits.add(value);
            case "EXTRA_STORY_BIT_SET" -> extraBits.add(value);
            case "EVENT_FLAG_SET" -> flags.add(c.text("flag"));
            case "ELITE_STAGE_EQUALS" -> elite = value;
            case "PARCEL_EQUALS", "PARCEL_STATUS_EQUALS", "PARCEL_PHASE_EQUALS" -> parcel = value;
            case "CHAMPION_SET" -> champion = true;
            case "CAPTURE_TUTORIAL_COMPLETE" -> captured = true;
            default -> { }
        }
    }
    private boolean condition(Data c) {
        int value = conditionValue(c);
        return switch (c.text("type")) {
            case "HAS_ITEM" -> items.getOrDefault(value, 0) > 0;
            case "NO_ITEM" -> items.getOrDefault(value, 0) == 0;
            case "STARTER_EQUALS" -> starter == value;
            case "SEX_EQUALS" -> sex == value;
            case "STORY_STAGE_EQUALS" -> stage == value;
            case "STORY_STAGE_AT_LEAST" -> stage >= value;
            case "BADGE_SET" -> badges.contains(value);
            case "BADGE_CLEAR" -> !badges.contains(value);
            case "STORY_BIT_SET" -> bits.contains(value);
            case "STORY_BIT_CLEAR" -> !bits.contains(value);
            case "EXTRA_STORY_BIT_SET" -> extraBits.contains(value);
            case "EXTRA_STORY_BIT_CLEAR" -> !extraBits.contains(value);
            case "EVENT_FLAG_SET" -> flags.contains(c.text("flag"));
            case "EVENT_FLAG_CLEAR" -> !flags.contains(c.text("flag"));
            case "ELITE_STAGE_EQUALS" -> elite == value;
            case "PARCEL_EQUALS", "PARCEL_STATUS_EQUALS", "PARCEL_PHASE_EQUALS" -> parcel == value;
            case "PARCEL_NEEDS_PICKUP" -> parcel == 3 || parcel == 0;
            case "CHAMPION_SET" -> champion;
            case "CHAMPION_CLEAR" -> !champion;
            case "NOT_IN_BATTLE", "NOT_IN_TRADE" -> true;
            case "HAS_CAPTURE_BALL" -> items.getOrDefault(5004, 0) > 0;
            case "NO_CAPTURE_BALL" -> items.getOrDefault(5004, 0) == 0;
            case "CAPTURE_TUTORIAL_INCOMPLETE" -> !captured;
            case "CAPTURE_TUTORIAL_COMPLETE" -> captured;
            default -> throw new IllegalArgumentException("Unsupported replay condition " + c.text("type"));
        };
    }
}
