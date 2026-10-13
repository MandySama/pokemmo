package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** 地点顺序与落点由外部目录定义，按钮复用已入图角色的位置保存流程。 */
final class TeleportMenuWindow extends ResizableFrame {
    record Destination(int section, int group, int map, int x, int y, int name) { }
    private final List<Destination> destinations = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private final ToggleButton region = new ToggleButton(ClientStrings.get(9261));
    private final Label first = new Label(ClientStrings.get(9262));
    private final Label second = new Label(ClientStrings.get(9263));

    TeleportMenuWindow(Runnable close, Consumer<Destination> teleport) throws IOException {
        setTheme("/teleport-menu");
        setTitle(ClientStrings.get(9260));
        setResizableAxis(ResizableAxis.NONE);
        addCloseCallback(close);
        region.setTheme("category");
        region.setActive(true);
        region.addCallback(() -> region.setActive(true));
        add(region);
        add(first);
        add(second);
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            var nodes = factory.newDocumentBuilder().parse(ClientFiles.data("maps/fire-red-teleports.xml").toFile())
                    .getElementsByTagName("destination");
            for (int i = 0; i < nodes.getLength(); i++) {
                var node = (org.w3c.dom.Element) nodes.item(i);
                Destination d = new Destination(value(node, "section"), value(node, "group"), value(node, "map"),
                        value(node, "x"), value(node, "y"), value(node, "name"));
                if (!top.totodile.pokemmo.protocol.FireRedMaps.contains(d.group(), d.map(), d.x(), d.y()))
                    throw new IOException("Invalid teleport destination");
                destinations.add(d);
                Button button = new Button(ClientStrings.get(d.name()));
                button.addCallback(() -> teleport.accept(d));
                buttons.add(button);
                add(button);
            }
        } catch (Exception exception) { throw new IOException("Unable to load teleport catalog", exception); }
    }

    private static int value(org.w3c.dom.Element node, String name) {
        return Integer.parseInt(node.getAttribute(name));
    }

    @Override
    protected void layout() {
        super.layout();
        region.setPosition(getX() + 10, getY() + 50);
        region.setSize(140, 30);
        int contentX = getX() + 160;
        int width = getWidth() - 172;
        first.setPosition(contentX + 10, getY() + 50);
        first.setSize(width, 24);
        second.setPosition(contentX + 10, getY() + 50 + 24 + 4 * 29);
        second.setSize(width, 24);
        int a = 0, b = 0;
        for (int i = 0; i < buttons.size(); i++) {
            Destination d = destinations.get(i);
            int index = d.section() == 1 ? a++ : b++;
            int top = d.section() == 1 ? first.getBottom() : second.getBottom();
            Button button = buttons.get(i);
            button.setPosition(contentX + index % 3 * (width / 3), top + index / 3 * 29);
            button.setSize(width / 3 - 5, 26);
        }
    }
}
