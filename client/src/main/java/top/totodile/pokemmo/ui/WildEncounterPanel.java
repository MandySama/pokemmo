package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.*;
import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.battle.WildEncounter;
import top.totodile.pokemmo.protocol.CharacterPackets.Appearance;
import top.totodile.pokemmo.protocol.PartyMember;
import top.totodile.pokemmo.protocol.ItemCatalog;
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
                          List<BufferedImage> trainer, BufferedImage ball, BufferedImage party) { }

    private final Button[] actions = new Button[4];
    private final Label enemyName = new Label(), selfName = new Label(), hp = new Label(), message = new Label();
    private final Consumer<String> failure;
    private final Runnable finished;
    private final List<Texture> textures = new ArrayList<>();
    private CompletableFuture<Pixels> pending;
    private WildEncounter encounter;
    private PartyMember self, enemy;
    private Image[] front, back, trainer, party;
    private Image ball, disc, white, enemyHud, selfHud, healthImage;
    private int frontBottom, backBottom;
    private Color outer = new Color(0xff00564d), inner = new Color(0xff53bdb3);
    private long now;
    private float scale;
    private int left, top, viewWidth, viewHeight;
    private int enemyHudX, enemyHudY, selfHudX, selfHudY;
    private long requested;

    WildEncounterPanel(Runnable finished, Consumer<String> failure) {
        this.finished = finished;
        this.failure = failure;
        setTheme("/wild-encounter");
        setClip(true);
        int[] titles = {5100, 5104, 5102, 5106};
        int[] descriptions = {5101, 5105, 5103, 5107};
        for (int i = 0; i < actions.length; i++) {
            final int index = i;
            actions[i] = new Button(ClientStrings.get(titles[i]) + "\n" + ClientStrings.get(descriptions[i]));
            actions[i].setTheme("action");
            actions[i].addCallback(() -> {
                if (encounter != null) encounter.choose(WildEncounter.Action.values()[index], now);
            });
            add(actions[i]);
        }
        for (Label label : new Label[]{enemyName, selfName, hp, message}) { label.setTheme("label"); add(label); }
        setVisible(false);
    }

    boolean active() { return encounter != null; }

    void start(Path rom, AvatarAssets assets, Appearance appearance, List<PartyMember> members, long milliseconds) {
        if (active()) return;
        self = members.stream().filter(member -> member.hp() > 0).findFirst().orElse(null);
        if (self == null) { failure.accept(ClientStrings.get(252004)); return; }
        // 首期固定野生小拉达，尚未接地区遇敌表及服务端抽取。
        enemy = new PartyMember(19, false, false, 18, 18, 5);
        PartyMember selected = self;
        PartyMember wild = enemy;
        List<PartyMember> snapshot = List.copyOf(members);
        requested = milliseconds;
        encounter = new WildEncounter();
        setVisible(true);
        enemyName.setText(name(enemy) + "  Lv. " + enemy.level());
        selfName.setText(name(self) + "  Lv. " + self.level() + gender(self));
        hp.setText(self.hp() + " / " + self.maxHp());
        message.setText(ClientStrings.get(252002));
        for (Button button : actions) button.setVisible(false);
        pending = CompletableFuture.supplyAsync(() -> {
            try {
                if (rom == null) throw new IOException("Missing Black/White ROM");
                PokedexData data = new PokedexData(rom);
                List<BufferedImage> frontFrames = animation(rom, wild, false, data);
                List<BufferedImage> backFrames = animation(rom, selected, true, data);
                AvatarAssets.Item[] items = new AvatarAssets.Item[12];
                int[] ids = appearance.items();
                for (int category = 1; category < items.length; category++) {
                    for (var item : assets.choices(category, appearance.gender())) {
                        if (item.id == ids[category]) { items[category] = item; break; }
                    }
                }
                List<BufferedImage> throwsFrames = new ArrayList<>();
                // 外观包 view 1 的五帧对应背面投球动作。
                for (int frame = 0; frame < 5; frame++)
                    throwsFrames.add(assets.render(appearance.skin(), items, appearance.colors(), 1, frame));
                var iconSource = new ItemIconSources(new RomFiles(ClientFiles.data("../roms")));
                int ballId = selected.individual() == null ? 5004 : selected.individual().ball();
                var ballItem = ItemCatalog.get(ballId);
                if (ballItem == null || ballId < 5001) ballItem = ItemCatalog.get(5004);
                return new Pixels(frontFrames, backFrames, throwsFrames, iconSource.image(ballItem),
                        PartyIcons.loadStrip(rom, snapshot, 0xff0000ff, 0xffff00ff, 0, 1.25f));
            } catch (IOException exception) { throw new java.util.concurrent.CompletionException(exception); }
        });
        invalidateLayout();
    }

    private static List<BufferedImage> animation(Path rom, PartyMember member, boolean back, PokedexData data) throws IOException {
        try { return PokemonAnimation.load(rom, member.species(), member.shiny(), back).images(); }
        catch (IOException | RuntimeException unsupported) { return List.of(data.sprite(member.species(), back, member.shiny())); }
    }

    private static String name(PartyMember member) {
        return member.individual() != null && !member.individual().nickname().isBlank()
                ? member.individual().nickname() : ClientStrings.get(150000 + member.species());
    }

    private static String gender(PartyMember member) {
        return member.individual() == null ? "" : member.individual().gender() == 0 ? "  ♂"
                : member.individual().gender() == 1 ? "  ♀" : "";
    }

    void tick(long milliseconds) {
        now = milliseconds;
        if (encounter == null) return;
        if (pending != null && pending.isDone()) {
            try {
                Pixels pixels = pending.join();
                pending = null;
                GlfwTwlRenderer renderer = (GlfwTwlRenderer) getGUI().getRenderer();
                front = uploadFrames(renderer, pixels.front());
                back = uploadFrames(renderer, pixels.back());
                trainer = uploadFrames(renderer, pixels.trainer());
                frontBottom = bottom(pixels.front()); backBottom = bottom(pixels.back());
                ball = upload(renderer, pixels.ball());
                Texture partyTexture = renderer.uploadImage(pixels.party()); textures.add(partyTexture);
                party = new Image[pixels.party().getWidth() / PartyIcons.TILE_SIZE];
                for (int i = 0; i < party.length; i++) party[i] = partyTexture.getImage(i * 42, 0, 42, 42, Color.WHITE, false, Texture.Rotation.NONE);
                disc = upload(renderer, discPixels());
                encounter.loaded(now);
            } catch (RuntimeException error) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Unable to load encounter", error);
                close(); finished.run(); failure.accept(ClientStrings.get(252003)); return;
            }
        }
        if (pending != null && now - requested > 30000) {
            close(); finished.run(); failure.accept(ClientStrings.get(252003)); return;
        }
        WildEncounter.Phase phase = encounter.update(now);
        if (phase == WildEncounter.Phase.CLOSED) { close(); finished.run(); return; }
        boolean ready = phase == WildEncounter.Phase.READY;
        for (Button action : actions) { action.setVisible(ready); action.setEnabled(ready); }
        enemyName.setVisible(phase.ordinal() >= WildEncounter.Phase.SLIDE.ordinal());
        boolean ownVisible = phase.ordinal() >= WildEncounter.Phase.RELEASE.ordinal();
        selfName.setVisible(ownVisible); hp.setVisible(ownVisible);
        message.setVisible(!ready && phase != WildEncounter.Phase.ENDING);
        if (phase != WildEncounter.Phase.LOADING)
            message.setText(ClientStrings.get(ownVisible ? 252001 : 252000).replace("{00}", name(ownVisible ? self : enemy)));
        invalidateLayout();
    }

    private Image[] uploadFrames(GlfwTwlRenderer renderer, List<BufferedImage> frames) {
        int cell = Math.max(96, frames.getFirst().getWidth());
        int columns = 16;
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

    private static int bottom(List<BufferedImage> frames) {
        int result = 1;
        for (BufferedImage image : frames) for (int y = result; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) if ((image.getRGB(x, y) >>> 24) != 0) { result = Math.max(result, y + 1); break; }
        return result;
    }

    private BufferedImage discPixels() {
        BufferedImage image = new BufferedImage(512, 256, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 256; y++) for (int x = 0; x < 512; x++) {
            double distance = Math.hypot((x - 256) / 256.0, (y - 128) / 128.0);
            if (distance > 1) continue;
            float fraction = (float) Math.pow(1 - distance, .7);
            fraction = Math.max(0, fraction - ((int) (distance * 14) % 2) * .09f);
            int r = (int) (outer.getRed() + (inner.getRed() - outer.getRed()) * fraction);
            int g = (int) (outer.getGreen() + (inner.getGreen() - outer.getGreen()) * fraction);
            int b = (int) (outer.getBlue() + (inner.getBlue() - outer.getBlue()) * fraction);
            image.setRGB(x, y, 0xee000000 | r << 16 | g << 8 | b);
        }
        return image;
    }

    @Override protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        outer = theme.getParameter("discOuter", outer); inner = theme.getParameter("discInner", inner);
        white = theme.getImage("whiteImage"); enemyHud = theme.getImage("enemyHud");
        selfHud = theme.getImage("selfHud"); healthImage = theme.getImage("healthImage");
    }

    @Override protected void layout() {
        scale = Math.min(getWidth() / 1024f, getHeight() / 768f);
        viewWidth = Math.round(1024 * scale); viewHeight = Math.round(768 * scale);
        left = getX() + (getWidth() - viewWidth) / 2; top = getY() + (getHeight() - viewHeight) / 2;
        enemyHudX = x(134); enemyHudY = y(160); selfHudX = x(595); selfHudY = y(350);
        place(enemyName, 134, 134, 307, 24); place(selfName, 595, 324, 299, 24); place(hp, 595, 366, 299, 24);
        place(message, 145, 480, 410, 50);
        for (int i = 0; i < actions.length; i++) place(actions[i], 145 + i % 2 * 211, 484 + i / 2 * 54, 205, 48);
    }

    private void place(Widget widget, int x, int y, int width, int height) {
        widget.setPosition(x(x), y(y)); widget.setSize(Math.round(width * scale), Math.round(height * scale));
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
        shade(gui, .48f * opacity * Math.min(1, Math.max(0, (elapsed - 800) / 500f)), false);
        if (elapsed < 1300) { shade(gui, WildEncounter.flash(elapsed), true); return; }
        gui.getRenderer().clipEnter(x(134), y(90), size(760), size(510));
        gui.getRenderer().pushGlobalTintColor(1, 1, 1, opacity);
        try {
            float slide = Math.min(1, (elapsed - 1300) / 700f);
            slide = 1 - (float) Math.pow(1 - slide, 3);
            float enemyX = 740 + (1 - slide) * 700, ownX = 380 - (1 - slide) * 800;
            disc.draw(getAnimationState(), x(enemyX - 165), y(280), size(330), size(75));
            disc.draw(getAnimationState(), x(ownX - 390), y(380), size(780), size(220));
            sprite(front[(int) (Math.max(0, elapsed - 2000) * 30 / 1000) % front.length], enemyX, 311, frontBottom, 2.2f);
            if (elapsed < 2600) {
                int frame = Math.max(0, Math.min(4, (int) ((elapsed - 2000) / 120)));
                sprite(trainer[frame], ownX - 55 - Math.max(0, elapsed - 2300) * .25f, 510, 80, 3);
                if (elapsed >= 2200) {
                    float progress = Math.min(1, (elapsed - 2200) / 400f);
                    ball.draw(getAnimationState(), x(ownX - 55 + 120 * progress), y(390 - 100 * (float) Math.sin(Math.PI * progress)), size(22), size(22));
                }
            } else {
                float release = Math.min(1, (elapsed - 2600) / 600f);
                sprite(back[(int) ((elapsed - 2600) * 30 / 1000) % back.length], ownX, 572, backBottom,
                        (self.alpha() ? 4.8f : 4f) * Math.max(.01f, release));
                if (release < .4f) {
                    gui.getRenderer().pushGlobalTintColor(1, 1, 1, 1 - release / .4f);
                    try { white.draw(getAnimationState(), x(ownX - 60), y(470), size(120), size(100)); }
                    finally { gui.getRenderer().popGlobalTintColor(); }
                }
            }
            enemyHud.draw(getAnimationState(), enemyHudX, enemyHudY, size(307), size(28));
            healthImage.draw(getAnimationState(), enemyHudX + size(43), enemyHudY + size(8), size(220), size(7));
            if (elapsed >= 2600) {
                selfHud.draw(getAnimationState(), selfHudX, selfHudY, size(299), size(40));
                healthImage.draw(getAnimationState(), selfHudX + size(38), selfHudY + size(6), size(220f * self.hp() / self.maxHp()), size(7));
                for (int i = 0; i < party.length; i++) party[i].draw(getAnimationState(), x(650 + i * 36), y(409), size(32), size(32));
            }
        } finally { gui.getRenderer().popGlobalTintColor(); gui.getRenderer().clipLeave(); }
    }

    private void sprite(Image image, float center, float floor, int bottom, float zoom) {
        image.draw(getAnimationState(), x(center - image.getWidth() * zoom / 2), y(floor - bottom * zoom),
                size(image.getWidth() * zoom), size(image.getHeight() * zoom));
    }

    @Override protected boolean handleEvent(Event event) { return active() || super.handleEvent(event); }

    @Override public void close() {
        if (pending != null) pending.cancel(true);
        pending = null;
        for (Texture texture : textures) texture.destroy();
        textures.clear();
        front = back = trainer = party = null;
        ball = disc = null; self = enemy = null; encounter = null;
        setVisible(false);
    }
}
