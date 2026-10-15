package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** 载入各地区目录；落点顺序与分区由外部文件定义，按钮复用已入图角色的位置保存流程。 */
final class TeleportMenuWindow extends ResizableFrame {
    record Destination(int region, int section, int group, int map, int x, int y, int name) { }

    /** 一个地区的目录；两边控件都在构造时建好，切换地区只改变可见性。 */
    private static final class Catalog {
        final int region;
        final int[] sectionTitles;
        final List<Destination> destinations = new ArrayList<>();
        final List<Button> buttons = new ArrayList<>();
        final List<Label> labels = new ArrayList<>();

        Catalog(int region, int[] sectionTitles) {
            this.region = region;
            this.sectionTitles = sectionTitles;
        }
    }

    private static final int[] REGION_TITLES = {250000, 250001, 250002, 250003, 250004, 250005};
    private static final int[][] SECTION_TITLES = {{9262, 9263}, {9273, 9274, 9275, 9276}, {9492, 9493}, {9492, 9493}, {9273, 9274, 9275, 9276, 250010}, {9492, 9493}};
    private static final String[] CATALOGS = {"maps/fire-red-teleports.xml", "maps/emerald-teleports.xml", "maps/unova-teleports.xml", "maps/sinnoh-teleports.xml", "maps/emerald2-teleports.xml", "maps/johto-teleports.xml"};
    private final Catalog[] catalogs = new Catalog[6];
    private final ToggleButton[] regions = new ToggleButton[6];
    private int selected = -1;
    private int expansionPage;
    private static final int PAGE_SIZE = 24;
    private final Button previousPage = new Button(ClientStrings.get(250011));
    private final Button nextPage = new Button(ClientStrings.get(250012));
    private final Label pageLabel = new Label();

    private int lastPage() {
        return (int) ((catalogs[4].destinations.stream().filter(d -> d.section() == 5).count() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    private void changePage(int delta) {
        expansionPage = Math.max(0, Math.min(lastPage(), expansionPage + delta));
        select(selected);
    }

    private boolean onPage(Catalog catalog, int index) {
        if (catalog.region != 4) return true;
        Destination d = catalog.destinations.get(index);
        if (expansionPage == 0) return d.section() != 5;
        if (d.section() != 5) return false;
        int first = 0;
        while (first < catalog.destinations.size() && catalog.destinations.get(first).section() != 5) first++;
        int offset = index - first;
        return offset >= (expansionPage - 1) * PAGE_SIZE && offset < expansionPage * PAGE_SIZE;
    }

    TeleportMenuWindow(Runnable close, Consumer<Destination> teleport) throws IOException {
        setTheme("/teleport-menu");
        setTitle(ClientStrings.get(9260));
        setResizableAxis(ResizableAxis.NONE);
        addCloseCallback(close);
        for (int i = 0; i < catalogs.length; i++) {
            final int target = i;
            catalogs[i] = new Catalog(i, SECTION_TITLES[i]);
            regions[i] = new ToggleButton(ClientStrings.get(REGION_TITLES[i]));
            regions[i].setTheme("category");
            regions[i].addCallback(() -> select(target));
            add(regions[i]);
            load(catalogs[i], teleport);
        }
        previousPage.addCallback(() -> changePage(-1));
        nextPage.addCallback(() -> changePage(1));
        add(previousPage);
        add(nextPage);
        add(pageLabel);
        select(0);
    }

    /** 打开前由世界按角色地区选择目录；切换时改左侧分类标题与右侧分区。 */
    void select(int region) {
        if (region < 0 || region >= catalogs.length) return;
        if (selected != region) expansionPage = 0;
        selected = region;
        for (Catalog catalog : catalogs) {
            boolean visible = catalog.region == region;
            for (int section = 0; section < catalog.labels.size(); section++) {
                final int targetSection = section + 1;
                boolean present = false;
                for (int i = 0; i < catalog.destinations.size(); i++) {
                    if (catalog.destinations.get(i).section() == targetSection && onPage(catalog, i)) present = true;
                }
                catalog.labels.get(section).setVisible(visible && present);
            }
            for (int i = 0; i < catalog.buttons.size(); i++) catalog.buttons.get(i).setVisible(visible && onPage(catalog, i));
        }
        for (int i = 0; i < regions.length; i++) regions[i].setActive(i == region);
        previousPage.setVisible(region == 4);
        nextPage.setVisible(region == 4);
        pageLabel.setVisible(region == 4);
        previousPage.setEnabled(expansionPage > 0);
        nextPage.setEnabled(expansionPage < lastPage());
        pageLabel.setText(expansionPage == 0 ? ClientStrings.get(250013)
                : ClientStrings.get(250014).replace("{00}", Integer.toString(expansionPage)).replace("{01}", Integer.toString(lastPage())));
        invalidateLayout();
    }

    private void load(Catalog catalog, Consumer<Destination> teleport) throws IOException {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            var nodes = factory.newDocumentBuilder().parse(ClientFiles.data(CATALOGS[catalog.region]).toFile())
                    .getElementsByTagName("destination");
            for (int i = 0; i < nodes.getLength(); i++) {
                var node = (org.w3c.dom.Element) nodes.item(i);
                Destination d = new Destination(catalog.region, value(node, "section"), value(node, "group"), value(node, "map"),
                        value(node, "x"), value(node, "y"), value(node, "name"));
                if (d.section() < 1 || d.section() > catalog.sectionTitles.length)
                    throw new IOException("Invalid teleport section");
                if (!top.totodile.pokemmo.protocol.FireRedMaps.contains(catalog.region, d.group(), d.map(), d.x(), d.y()))
                    throw new IOException("Invalid teleport destination");
                catalog.destinations.add(d);
                Button button = new Button(ClientStrings.get(d.name()));
                button.addCallback(() -> teleport.accept(d));
                catalog.buttons.add(button);
                add(button);
            }
            for (int id : catalog.sectionTitles) {
                Label label = new Label(ClientStrings.get(id));
                catalog.labels.add(label);
                add(label);
            }
        } catch (Exception exception) { throw new IOException("Unable to load teleport catalog", exception); }
    }

    private static int value(org.w3c.dom.Element node, String name) {
        return Integer.parseInt(node.getAttribute(name));
    }

    @Override
    protected void layout() {
        super.layout();
        for (int i = 0; i < regions.length; i++) {
            regions[i].setPosition(getX() + 10, getY() + 50 + i * 34);
            regions[i].setSize(140, 30);
        }
        previousPage.setPosition(getX() + 170, getY() + getHeight() - 38);
        previousPage.setSize(90, 26);
        pageLabel.setPosition(getX() + 270, getY() + getHeight() - 36);
        pageLabel.setSize(300, 24);
        nextPage.setPosition(getX() + getWidth() - 110, getY() + getHeight() - 38);
        nextPage.setSize(90, 26);
        int contentX = getX() + 160;
        int width = getWidth() - 172;
        Catalog catalog = catalogs[selected];
        // 分区依次排列：标题一行，随后三列按钮网格，行距沿用原关都目录。
        int y = getY() + 50;
        for (int section = 1; section <= catalog.sectionTitles.length; section++) {
            if (!catalog.labels.get(section - 1).isVisible()) continue;
            catalog.labels.get(section - 1).setPosition(contentX + 10, y);
            catalog.labels.get(section - 1).setSize(width, 24);
            y += 24;
            int index = 0;
            for (int i = 0; i < catalog.destinations.size(); i++) {
                if (catalog.destinations.get(i).section() != section || !onPage(catalog, i)) continue;
                Button button = catalog.buttons.get(i);
                button.setPosition(contentX + index % 3 * (width / 3), y + index / 3 * 29);
                button.setSize(width / 3 - 5, 26);
                index++;
            }
            y += (index + 2) / 3 * 29;
        }
    }
}
