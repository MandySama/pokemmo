package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.protocol.CharacterPackets.CharacterInfo;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.RomFiles;
import top.totodile.pokemmo.rom.BlackWhiteItemIcons;
import java.time.Instant;
import java.text.NumberFormat;
import top.totodile.pokemmo.protocol.TrainerStats;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** 名片背景自带八个徽章槽；角色外观及统计均来自当前角色。 */
final class TrainerCardWindow extends ResizableFrame implements AutoCloseable {
    /** 文字不截获鼠标事件，让名片整体接收拖动和点击。 */
    private static final class CardLabel extends Label {
        CardLabel() { setTheme("label"); }
        CardLabel(String text) { super(text); setTheme("label"); }
        @Override protected boolean handleEvent(Event event) { return false; }
    }

    private final Label name = new CardLabel();
    private final Label date = new CardLabel();
    private final Label[] stats = new Label[5];
    private final Widget[] icons = new Widget[3];
    private final Runnable onClose;
    private CharacterInfo character;
    private AvatarAssets assets;
    private CompletableFuture<BufferedImage> statIconResult;
    private Texture statIconTexture;
    private final Image[] statIcons = new Image[2];
    private Texture texture;
    private Image image;
    private boolean failed;
    private int avatarScale = 2, avatarX = 5, avatarY = 50;
    private boolean leftPressed, dragged;
    private int pressX, pressY;

    @Override
    protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        avatarScale = theme.getParameter("avatarScale", 2);
        avatarX = theme.getParameter("avatarX", 5);
        avatarY = theme.getParameter("avatarY", 50);
    }

    void setStats(TrainerStats value) {
        if (!value.valid()) throw new IllegalArgumentException("Invalid trainer stats");
        NumberFormat numbers = NumberFormat.getIntegerInstance(java.util.Locale.ROOT);
        stats[0].setText(ClientStrings.get(9280).replace("{00}", numbers.format(value.caught())));
        stats[1].setText(ClientStrings.get(1602) + " $" + numbers.format(value.money()));
        stats[2].setText(ClientStrings.get(1605) + " " + numbers.format(value.battlePoints()));
        stats[3].setText(ClientStrings.get(1603).replace("{00}", numbers.format(value.onlineSeconds() / 3600)));
        stats[4].setText(ClientStrings.get(9281).replace("{00}", numbers.format(value.obedience())));
    }

    TrainerCardWindow(Runnable close) {
        onClose = close;
        setTheme("/trainer-card");
        setResizableAxis(ResizableAxis.NONE);
        setBackgroundDraggable(true);
        name.setTheme("trainer-name");
        add(name);
        add(date);
        String missing = ClientStrings.get(9282);
        String[] text = {ClientStrings.get(9280).replace("{00}", missing),
                ClientStrings.get(1602) + " $" + missing, ClientStrings.get(1605) + " " + missing,
                ClientStrings.get(1603).replace("{00}", missing), ClientStrings.get(9281).replace("{00}", missing)};
        for (int i = 0; i < stats.length; i++) { stats[i] = new CardLabel(text[i]); add(stats[i]); }
        String[] themes = {"icon-money", "icon-bp", "icon-time"};
        for (int i = 0; i < icons.length; i++) { icons[i] = new Widget(); icons[i].setTheme(themes[i]); add(icons[i]); }
    }

    void setCharacter(CharacterInfo value, AvatarAssets assets) {
        close();
        character = value;
        this.assets = assets;
        failed = false;
        name.setText(value.appearance().name());
        setStats(value.stats());
        String created = value.createdAt() > 0 ? DateTimeFormatter.ISO_LOCAL_DATE.format(
                Instant.ofEpochSecond(value.createdAt()).atZone(ZoneId.systemDefault())) : ClientStrings.get(9282);
        date.setText(ClientStrings.get(1606) + " " + created);
    }

    @Override
    protected void layout() {
        super.layout();
        name.setPosition(getX() + 14, getY() + 15);
        name.setSize(280, 26);
        date.adjustSize();
        date.setPosition(getRight() - date.getWidth() - 23, getY() + 48);
        for (int i = 0; i < stats.length; i++) {
            stats[i].setPosition(getX() + 196, getY() + 73 + i * 28);
            stats[i].setSize(248, 24);
        }
        for (int i = 0; i < icons.length; i++) {
            icons[i].setPosition(getX() + 170, getY() + 98 + i * 28);
            icons[i].setSize(24, 24);
        }
    }

    @Override
    protected void paintWidget(GUI gui) {
        super.paintWidget(gui);
        paintStatIcons(gui);
        if (character == null || assets == null || failed) return;
        if (image == null) {
            try {
                var appearance = character.appearance();
                AvatarAssets.Item[] selected = new AvatarAssets.Item[12];
                int[] ids = appearance.items();
                for (int category = 1; category < selected.length; category++) {
                    for (var item : assets.choices(category, appearance.gender()))
                        if (item.id == ids[category]) { selected[category] = item; break; }
                }
                var pixels = assets.render(appearance.skin(), selected, appearance.colors(), 2, appearance.gender());
                texture = ((GlfwTwlRenderer) gui.getRenderer()).uploadImage(pixels);
                image = texture.getImage(0, 0, pixels.getWidth(), pixels.getHeight(), Color.WHITE, false, Texture.Rotation.NONE);
            } catch (IOException exception) {
                failed = true;
                System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Unable to render trainer card", exception);
                return;
            }
        }
        image.draw(getAnimationState(), getX() + avatarX, getY() + avatarY,
                image.getWidth() * avatarScale, image.getHeight() * avatarScale);
    }

    /** 两个物品图标共享纹理；异步任务只生成 CPU 图像。 */
    private void paintStatIcons(GUI gui) {
        if (statIconResult == null) {
            statIconResult = CompletableFuture.supplyAsync(() -> {
                try {
                    var rom = new RomFiles(ClientFiles.data("../roms")).selected(0);
                    if (rom == null) return null;
                    var source = new BlackWhiteItemIcons(rom);
                    var pixels = new BufferedImage(48, 24, BufferedImage.TYPE_INT_ARGB);
                    int[] ids = {5431, 5216};
                    for (int i = 0; i < ids.length; i++) {
                        var icon = source.image(ids[i]);
                        pixels.setRGB(i * 24, 0, 24, 24, icon.getRGB(0, 0, 24, 24, null, 0, 24), 0, 24);
                    }
                    return pixels;
                } catch (IOException error) {
                    System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING,
                            "Unable to load trainer card stat icons", error);
                    return null;
                }
            });
        }
        if (statIconTexture == null && statIconResult.isDone() && !statIconResult.isCancelled()
                && !statIconResult.isCompletedExceptionally()) {
            var pixels = statIconResult.getNow(null);
            if (pixels != null) {
                statIconTexture = ((GlfwTwlRenderer) gui.getRenderer()).uploadImage(pixels);
                for (int i = 0; i < statIcons.length; i++)
                    statIcons[i] = statIconTexture.getImage(i * 24, 0, 24, 24, Color.WHITE, false, Texture.Rotation.NONE);
            }
        }
        for (int i = 0; i < statIcons.length; i++) {
            if (statIcons[i] != null)
                statIcons[i].draw(getAnimationState(), getX() + 170, getY() + 70 + i * 112);
        }
    }

    @Override
    protected boolean handleEvent(Event event) {
        if (event.getType() == Event.Type.MOUSE_BTNDOWN && event.getMouseButton() == Event.MOUSE_LBUTTON) {
            leftPressed = true; dragged = false;
            pressX = event.getMouseX(); pressY = event.getMouseY();
        }
        if (leftPressed && event.isMouseEvent()
                && (event.getMouseX() != pressX || event.getMouseY() != pressY)) dragged = true;
        if (event.getType() == Event.Type.MOUSE_BTNUP) {
            boolean close = event.getMouseButton() == Event.MOUSE_RBUTTON
                    || event.getMouseButton() == Event.MOUSE_LBUTTON && leftPressed && !dragged;
            leftPressed = false;
            boolean handled = super.handleEvent(event);
            if (close) { onClose.run(); return true; }
            return handled;
        }
        return super.handleEvent(event);
    }

    @Override
    public void close() {
        if (statIconResult != null) statIconResult.cancel(true);
        statIconResult = null;
        if (statIconTexture != null) statIconTexture.destroy();
        statIconTexture = null;
        java.util.Arrays.fill(statIcons, null);
        if (texture != null) texture.destroy();
        texture = null;
        image = null;
    }
}
