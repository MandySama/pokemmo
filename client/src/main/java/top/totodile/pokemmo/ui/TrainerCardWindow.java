package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.protocol.CharacterPackets.CharacterInfo;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** 名片背景自带八个空徽章槽；无统计来源的字段显示空值，不生成示例数据。 */
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
    private Texture texture;
    private Image image;
    private boolean failed;

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
                int left = pixels.getWidth(), top = pixels.getHeight(), right = -1, bottom = -1;
                for (int y = 0; y < pixels.getHeight(); y++) for (int x = 0; x < pixels.getWidth(); x++) {
                    if ((pixels.getRGB(x, y) >>> 24) == 0) continue;
                    left = Math.min(left, x); top = Math.min(top, y);
                    right = Math.max(right, x); bottom = Math.max(bottom, y);
                }
                if (right >= left) pixels = pixels.getSubimage(left, top, right - left + 1, bottom - top + 1);
                texture = ((GlfwTwlRenderer) gui.getRenderer()).uploadImage(pixels);
                image = texture.getImage(0, 0, pixels.getWidth(), pixels.getHeight(), Color.WHITE, false, Texture.Rotation.NONE);
            } catch (IOException exception) {
                failed = true;
                System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Unable to render trainer card", exception);
                return;
            }
        }
        float scale = Math.min(112f / image.getWidth(), 156f / image.getHeight());
        int width = Math.round(image.getWidth() * scale), height = Math.round(image.getHeight() * scale);
        image.draw(getAnimationState(), getX() + 25 + (124 - width) / 2,
                getY() + 55 + (166 - height) / 2, width, height);
    }

    @Override
    protected boolean handleEvent(Event event) {
        if (event.getType() == Event.Type.MOUSE_BTNUP && event.getMouseButton() == Event.MOUSE_RBUTTON) {
            onClose.run();
            return true;
        }
        return super.handleEvent(event);
    }

    @Override
    public void close() {
        if (texture != null) texture.destroy();
        texture = null;
        image = null;
    }
}
