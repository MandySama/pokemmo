package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.RomFiles;
import top.totodile.pokemmo.rom.PokedexIcons;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/** 遇见与捕获记录为空时遵守隐藏资料规则，不凭本地图标授予图鉴记录。 */
final class PokedexWindow extends ResizableFrame implements AutoCloseable {
    private final ComboBox<String> region = new ComboBox<>(new SimpleChangableListModel<>(
            ClientStrings.get(9300), ClientStrings.get(9301)));
    private final EditField search = new EditField();
    private final ComboBox<String> tier = new ComboBox<>(new SimpleChangableListModel<>(ClientStrings.get(5749)));
    private final Grid grid = new Grid();
    private final ScrollPane scroll = new ScrollPane(grid);
    private final Label[] counters = new Label[4];
    private final ProgressBar[] progress = new ProgressBar[4];
    private CompletableFuture<BufferedImage> iconResult;
    private Texture texture;
    private final Image[] icons = new Image[PokedexIcons.SPECIES + 1];
    private final PokedexDetailPanel details = new PokedexDetailPanel(this::backToList, this::showDetails);
    private boolean loaded;
    private boolean unlocked;
    private final Image[] coloredIcons = new Image[PokedexIcons.SPECIES + 1];

    boolean isUnlocked() { return unlocked; }

    void setAllUnlocked(boolean value) {
        unlocked = value;
        if(details.isVisible()) showDetails(details.species());
        filter();
    }

    PokedexWindow(Runnable close) {
        setTheme("/pokedex-window");
        setTitle(ClientStrings.get(9308));
        setResizableAxis(ResizableAxis.NONE);
        addCloseCallback(close);
        region.setSelected(0);
        tier.setSelected(0);
        search.setMaxTextLength(64);
        search.setTooltipContent(ClientStrings.get(9304));
        add(region);
        add(search);
        add(tier);
        scroll.setTheme("entries");
        scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        add(scroll);
        String[] styles = {"progress-blue", "progress-green", "progress-pink", "progress-white"};
        for (int i = 0; i < counters.length; i++) {
            counters[i] = new Label();
            progress[i] = new ProgressBar();
            progress[i].setTheme(styles[i]);
            progress[i].setValue(0);
            add(counters[i]);
            add(progress[i]);
        }
        region.addCallback(this::filter);
        search.addCallback(key -> filter());
        add(details);
        filter();
    }

    void open() {
        backToList();
        search.setText("");
        region.setSelected(0);
        filter();
        setHardVisible(true);
        requestKeyboardFocus();
        if (iconResult == null && !loaded) iconResult = CompletableFuture.supplyAsync(() -> {
            try {
                var rom = new RomFiles(ClientFiles.data("../roms")).selected(0);
                if (rom == null) return null;
                return PokedexIcons.load(rom);
            } catch (IOException exception) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Unable to load Pokedex icons", exception);
                return null;
            }
        });
    }

    void selectSpecies(int species) { showDetails(species); }

    private void showDetails(int species) {
        if (species < 1 || species > PokedexIcons.SPECIES) return;
        if(species>count()) region.setSelected(0);
        region.setVisible(false);search.setVisible(false);tier.setVisible(false);scroll.setVisible(false);
        for(int i=0;i<counters.length;i++){counters[i].setVisible(false);progress[i].setVisible(false);}
        details.showSpecies(species,unlocked,count());
        setTitle(ClientStrings.get(1705)+String.format(java.util.Locale.ROOT," %03d ",species)+(unlocked?ClientStrings.get(150000+species):ClientStrings.get(9302)));
    }

    boolean backToList() {
        boolean wasVisible=details.isVisible();
        details.hideDetails();
        setTitle(ClientStrings.get(9308));
        region.setVisible(true);search.setVisible(true);tier.setVisible(true);scroll.setVisible(true);
        for(int i=0;i<counters.length;i++){counters[i].setVisible(true);progress[i].setVisible(true);}
        return wasVisible;
    }
    boolean editingSearch() { return search.hasKeyboardFocus(); }
    int count() { return region.getSelected() == 1 ? 151 : PokedexIcons.SPECIES; }

    private void filter() {
        grid.removeAllChildren();
        // 未遇见种族不参与名称搜索，也不能通过编号搜索绕过隐藏规则。
        String query = search.getText().strip().toLowerCase(java.util.Locale.ROOT);
        for (int i = 1; i <= count(); i++) {
            String name = unlocked ? ClientStrings.get(150000 + i) : ClientStrings.get(9302);
            if (query.isEmpty() || unlocked && name.toLowerCase(java.util.Locale.ROOT).contains(query)) grid.addEntry(i);
        }
        int[] ids = {1701,1702,1728,1762};
        for (int i = 0; i < counters.length; i++) {
            counters[i].setText(ClientStrings.get(ids[i]) + " " + (unlocked ? count() : 0) + " / " + count());
            progress[i].setValue(unlocked ? 1 : 0);
        }
        grid.invalidateLayout();
        scroll.setScrollPositionY(0);
        invalidateLayout();
    }

    private Image icon(int species) {
        if (species <= 0 || species >= icons.length) return null;
        return unlocked ? coloredIcons[species] : icons[species];
    }

    @Override protected void paintWidget(GUI gui) {
        super.paintWidget(gui);
        if (!loaded && iconResult != null && iconResult.isDone() && !iconResult.isCancelled()) {
            BufferedImage pixels = iconResult.getNow(null);
            loaded = true;
            if (pixels != null) {
                texture = ((GlfwTwlRenderer) gui.getRenderer()).uploadImage(pixels);
                for (int i = 1; i < icons.length; i++) {
                    int x = (i - 1) % 32 * 32, y = (i - 1) / 32 * 32;
                    icons[i] = texture.getImage(x, y, 32, 32, Color.WHITE, false, Texture.Rotation.NONE);
                    coloredIcons[i] = texture.getImage(x + 1024, y, 32, 32, Color.WHITE, false, Texture.Rotation.NONE);
                }
            }
        }
    }

    @Override
    protected void layout() {
        super.layout();
        details.setPosition(getX()+10,getY()+33);
        details.setSize(getWidth()-20,getHeight()-43);
        int x = getX() + 10;
        int y = getY() + 33;
        region.setPosition(x, y);
        region.setSize(160, 32);
        search.setPosition(x + 170, y);
        search.setSize(getWidth() - 350, 32);
        tier.setPosition(getRight() - 160, y);
        tier.setSize(150, 32);
        scroll.setPosition(getX() + 15, getY() + 79);
        scroll.setSize(getWidth() - 30, getHeight() - 142);
        int columnWidth = (getWidth() - 32) / counters.length;
        for (int i = 0; i < counters.length; i++) {
            int columnX = getX() + 8 + i * columnWidth;
            counters[i].setPosition(columnX, getBottom() - 54);
            counters[i].setSize(columnWidth - 8, 24);
            progress[i].setPosition(columnX, getBottom() - 26);
            progress[i].setSize(columnWidth - 12, 8);
        }
    }

    @Override public void close() {
        details.close();
        if (iconResult != null) iconResult.cancel(true);
        iconResult = null;
        loaded = false;
        if (texture != null) texture.destroy();
        texture = null;
        java.util.Arrays.fill(icons,null);
        java.util.Arrays.fill(coloredIcons,null);
    }

    private final class Grid extends Widget {
        Grid() { setTheme("grid"); }
        void addEntry(int id) { add(new Entry(id)); }
        @Override public int getPreferredInnerWidth() { return 12 * 54 - 8; }
        @Override public int getPreferredInnerHeight() { return Math.max(0, (getNumChildren() + 11) / 12 * 54 - 8); }
        @Override protected void layout() {
            for (int i = 0; i < getNumChildren(); i++) {
                Widget child = getChild(i);
                child.setPosition(getX() + i % 12 * 54, getY() + i / 12 * 54); child.setSize(46,46);
            }
        }
    }

    private final class Entry extends Button {
        private final int id;
        Entry(int id) {
            super(String.format("%03d",id)); this.id=id;
            setTheme("entry");
            addCallback(() -> showDetails(id));
            getAnimationState().setAnimationState(de.matthiasmann.twl.renderer.AnimationState.StateKey.get("owned"), unlocked);
            setTooltipContent(ClientStrings.get(unlocked ? 150000 + id : 9302));
        }
        @Override protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            Image image = icon(id);
            if (image != null) image.draw(getAnimationState(),getX()+7,getY()-2);
        }
    }
}
