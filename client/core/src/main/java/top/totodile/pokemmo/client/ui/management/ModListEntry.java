package top.totodile.pokemmo.client.ui.management;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.resources.ModImporter;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

/** 按参考四列显示 MOD；排序独立于启用状态。 */
final class ModListEntry extends DialogLayout implements AutoCloseable {
    private Texture texture;
    private de.matthiasmann.twl.renderer.Image iconImage;

    ModListEntry(
            ModImporter.Mod mod,
            ClientStrings strings,
            byte[] png,
            GdxTwlRenderer renderer,
            boolean enabled,
            int index,
            int count,
            Runnable moveUp,
            Runnable moveDown,
            java.util.function.Consumer<Boolean> enable,
            Runnable delete) {
        setTheme("dialoglayout");
        setAddDefaultGaps(false);
        Widget icon =
                new Widget() {
                    @Override
                    protected boolean handleEvent(Event event) {
                        return event.isMouseEvent() && event.getType() != Event.Type.MOUSE_WHEEL;
                    }

                    @Override
                    protected void applyTheme(ThemeInfo theme) {
                        super.applyTheme(theme);
                        setMinSize(64, 64);
                        setMaxSize(64, 64);
                    }

                    @Override
                    public int getPreferredWidth() {
                        return 64;
                    }

                    @Override
                    public int getPreferredHeight() {
                        return 64;
                    }

                    @Override
                    protected void paintWidget(GUI gui) {
                        if (iconImage != null)
                            iconImage.draw(getAnimationState(), getX(), getY(), 64, 64);
                    }
                };
        icon.setTheme("");
        icon.setMinSize(64, 64);
        icon.setMaxSize(64, 64);
        if (png != null && renderer != null) {
            Pixmap pixels = new Pixmap(png, 0, png.length);
            try {
                texture = new Texture(pixels);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                iconImage = renderer.image(texture);
            } finally {
                pixels.dispose();
            }
        }
        Label title =
                new Label(
                        mod.name()
                                + "\n"
                                + strings.format(1192, mod.author())
                                + "\n"
                                + strings.format(1193, mod.version()));
        title.setTheme("label-settings-title");
        if (!mod.description().isBlank()) {
            icon.setTooltipContent(mod.description());
            title.setTooltipContent(mod.description());
            icon.setTooltipDelay(250);
            title.setTooltipDelay(250);
        }
        ToggleButton selected = new ToggleButton();
        selected.setSize(80, 30);
        selected.setCanAcceptKeyboardFocus(false);
        selected.setActive(enabled);
        selected.addCallback(() -> enable.accept(selected.isActive()));
        Button remove = new Button(strings.get(1228));
        remove.addCallback(delete);
        Button website = new Button(strings.get(1195));
        remove.setCanAcceptKeyboardFocus(false);
        website.setCanAcceptKeyboardFocus(false);
        DialogLayout actions = verticalButtons(selected, remove, website);
        Button up = orderButton("↑"), down = orderButton("↓");
        up.setEnabled(index > 0);
        down.setEnabled(index + 1 < count);
        up.setVisible(index > 0);
        down.setVisible(index + 1 < count);
        up.addCallback(moveUp);
        down.addCallback(moveDown);
        DialogLayout order = verticalButtons(up, down);
        // 参考 Ll(false) 仅隐藏；保留两格布局，让上下箭头的位置不随边界改变。
        order.setIncludeInvisibleWidgets(true);
        // 与参考 row.C7 / row.hb 相同：横向四列，纵向同一行，图标居中。
        setHorizontalGroup(
                createParallelGroup()
                        .addGroup(
                                createSequentialGroup()
                                        .addWidget(icon)
                                        .addGap(DEFAULT_GAP)
                                        .addWidget(title)
                                        .addGap(DEFAULT_GAP)
                                        .addWidget(actions)
                                        .addGap(DEFAULT_GAP)
                                        .addWidget(order)));
        setVerticalGroup(
                createSequentialGroup().addGroup(createParallelGroup(icon, title, actions, order)));
        setWidgetAlignment(icon, Alignment.CENTER);
    }

    private static Button orderButton(String text) {
        Button button = new Button(text);
        button.setTheme("button-symbol");
        // 对应参考 g2(10,40)，属于初始最大尺寸；最终尺寸由 PC/Android 主题决定。
        button.setMaxSize(10, 40);
        button.setCanAcceptKeyboardFocus(false);
        return button;
    }

    private static DialogLayout verticalButtons(Widget... widgets) {
        DialogLayout panel =
                new DialogLayout() {
                    private int columnWidth = 100;

                    @Override
                    protected void applyTheme(ThemeInfo info) {
                        super.applyTheme(info);
                        columnWidth = info.getParameter("minWidth", 100);
                    }

                    @Override
                    public int getMinWidth() {
                        return columnWidth;
                    }

                    @Override
                    public int getPreferredWidth() {
                        return getMinWidth();
                    }

                    @Override
                    public int getMaxWidth() {
                        return getMinWidth();
                    }
                };
        panel.setTheme("label-settings-value-modbuttons");
        panel.setAddDefaultGaps(false);
        panel.setHorizontalGroup(panel.createParallelGroup(widgets));
        var rows = panel.createSequentialGroup();
        for (int index = 0; index < widgets.length; index++) {
            if (index > 0) rows.addGap(DEFAULT_GAP);
            rows.addWidget(widgets[index]);
        }
        panel.setVerticalGroup(rows);
        return panel;
    }

    @Override
    public void close() {
        iconImage = null;
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
