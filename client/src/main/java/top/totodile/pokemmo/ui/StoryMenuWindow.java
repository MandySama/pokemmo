package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** 地图分类由外部目录定义，只展示已接通剧情的调试操作。 */
final class StoryMenuWindow extends ResizableFrame {
    record Command(int region, int group, int map, int action, String scene) { }

    private static final class MapGroup {
        final int region, group, map;
        final ToggleButton tab;
        final List<Widget> rows = new ArrayList<>();
        final List<Button> commands = new ArrayList<>();

        MapGroup(int region, int group, int map, int name) {
            this.region = region;
            this.group = group;
            this.map = map;
            tab = new ToggleButton(ClientStrings.get(name));
            tab.setTheme("category");
        }
    }

    private final List<MapGroup> maps = new ArrayList<>();
    private MapGroup selected;
    private int region;
    private boolean busy;
    private java.util.Set<String> completed = java.util.Set.of();
    private java.util.Set<String> available = java.util.Set.of();
    private final java.util.Map<Button, Command> operations = new java.util.HashMap<>();
    private final Widget mapList = new Widget() {
        @Override public int getPreferredInnerHeight() { return maps.size() * 36; }
        @Override protected void layout() {
            for (int i = 0; i < maps.size(); i++) {
                maps.get(i).tab.setPosition(getInnerX(), getInnerY() + i * 36);
                maps.get(i).tab.setSize(getInnerWidth(), 30);
            }
        }
    };
    private final Widget storyList = new Widget() {
        @Override public int getPreferredInnerHeight() { return selected == null ? 0 : selected.rows.size() * 38; }
        @Override protected void layout() {
            if (selected == null) return;
            int y = getInnerY();
            for (var row : selected.rows) {
                row.setPosition(getInnerX(), y);
                row.setSize(row instanceof Button ? Math.min(getInnerWidth(), row.getPreferredWidth()) : getInnerWidth(), 28);
                y += 38;
            }
        }
    };
    private final ScrollPane mapScroll = new ScrollPane(mapList);
    private final ScrollPane storyScroll = new ScrollPane(storyList);

    StoryMenuWindow(Runnable close, Consumer<Command> command) {
        setTheme("/story-menu");
        setTitle(ClientStrings.get(9523));
        setResizableAxis(ResizableAxis.NONE);
        addCloseCallback(close);
        mapScroll.setTheme("entries"); storyScroll.setTheme("entries");
        mapList.setTheme("content"); storyList.setTheme("content");
        mapScroll.setFixed(ScrollPane.Fixed.HORIZONTAL); storyScroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        add(mapScroll); add(storyScroll);
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            var nodes = factory.newDocumentBuilder().parse(ClientFiles.data("maps/story-commands.xml").toFile())
                    .getElementsByTagName("map");
            for (int i = 0; i < nodes.getLength(); i++) {
                var node = (org.w3c.dom.Element) nodes.item(i);
                var group = new MapGroup(value(node, "region"), value(node, "group"), value(node, "map"), value(node, "name"));
                maps.add(group);
                group.tab.addCallback(() -> select(group));
                mapList.add(group.tab);
                var stories = node.getElementsByTagName("story");
                for (int j = 0; j < stories.getLength(); j++) {
                    var story = (org.w3c.dom.Element) stories.item(j);
                    Label title = new Label(ClientStrings.get(value(story, "name")));
                    title.setTheme("heading");
                    group.rows.add(title);
                    storyList.add(title);
                    var commands = story.getElementsByTagName("command");
                    for (int k = 0; k < commands.getLength(); k++) {
                        var entry = (org.w3c.dom.Element) commands.item(k);
                        var operation = new Command(group.region, group.group, group.map, value(entry, "action"), story.getAttribute("scene"));
                        Button button = new Button(ClientStrings.get(value(entry, "name")));
                        button.addCallback(() -> command.accept(operation));
                        group.commands.add(button);
                        operations.put(button, operation);
                        group.rows.add(button);
                        storyList.add(button);
                    }
                }
            }
            if (maps.isEmpty()) throw new IllegalArgumentException("Empty story command catalog");
            select(maps.getFirst());
        } catch (Exception error) {
            throw new IllegalStateException("Unable to load story command catalog", error);
        }
    }

    void selectMap(int region, int group, int map) {
        this.region = region;
        select(maps.stream().filter(m -> m.region == region && m.group == group && m.map == map)
                .findFirst().orElseGet(() -> maps.stream().filter(m -> m.region == region).findFirst().orElse(maps.getFirst())));
        mapScroll.setScrollPositionY(Math.max(0, maps.indexOf(selected) * 36 - 72));
    }

    void setBusy(boolean busy) {
        if (this.busy == busy) return;
        this.busy = busy;
        updateCommands();
    }

    void setCompleted(java.util.Set<String> completed) {
        this.completed = java.util.Set.copyOf(completed);
        updateCommands();
    }
    void setAvailable(java.util.Set<String> available){this.available=java.util.Set.copyOf(available);updateCommands();}

    private void updateCommands() {
        for (var group : maps) for (var button : group.commands) {
            var operation = operations.get(button);
            boolean done = completed.contains(operation.scene());
            boolean connected = operation.scene().equals(top.totodile.pokemmo.protocol.StoryProgressPackets.OPENING)
                    || operation.scene().equals(top.totodile.pokemmo.protocol.StoryProgressPackets.LAB_INTRODUCTION);
            button.setEnabled(group.region == region && !busy && connected
                    && (operation.action() != 4 || available.contains(operation.scene())));
            button.setTooltipContent(operation.action() == 4 && done ? ClientStrings.get(9563) : null);
        }
    }

    private void select(MapGroup selected) {
        this.selected = selected;
        for (var group : maps) {
            group.tab.setActive(group == selected);
            for (var row : group.rows) row.setVisible(group == selected);
        }
        updateCommands();
        storyScroll.setScrollPositionY(0);
        storyList.invalidateLayout();
        invalidateLayout();
    }

    private static int value(org.w3c.dom.Element node, String name) {
        return Integer.parseInt(node.getAttribute(name));
    }

    @Override
    protected void layout() {
        super.layout();
        mapScroll.setPosition(getInnerX() + 12, getY() + 50);
        mapScroll.setSize(230, getHeight() - 64);
        storyScroll.setPosition(getInnerX() + 254, getY() + 50);
        storyScroll.setSize(getInnerWidth() - 266, getHeight() - 64);
    }
}
