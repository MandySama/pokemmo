package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.avatar.AvatarAssets;

import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** 新角色外观表单；服务端创建接口接通前不允许提交。 */
final class CharacterCreationWindow extends ResizableFrame {
    private static final int[] CATEGORIES = {1, 2, 3, 4, 5, 6, 7, 8, 9, 11};
    private static final int[][] WALK_FRAMES = {
            {0, 3, 0, 4}, {1, 5, 1, 6}, {2, 7, 2, 8},
            {9, 10, 9, 11}, {12, 13, 12, 14}, {15, 16, 15, 17},
            {18, 21, 18, 22}, {19, 23, 19, 24}, {20, 25, 20, 26}
    };
    private static final int[] PORTRAIT_FRAMES = {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 4, 4, 4, 4, 0, 0, 0};
    private final List<Label> labels = new ArrayList<>();
    private final List<ComboBox<String>> options = new ArrayList<>();
    private final Label nameLabel = label(1053);
    private final Label regionLabel = label(1060);
    private final EditField name = new CharacterNameField();
    private final ComboBox<String> region = combo(ClientStrings.get(250000));
    private final Button create = new Button(ClientStrings.get(1051));
    private final Button previous = new Button();
    private final Button next = new Button();
    private final Button finishColor = new Button(ClientStrings.get(2801));
    private final List<Button> colorButtons = new ArrayList<>();
    private final List<Button> swatches = new ArrayList<>();
    private final List<List<AvatarAssets.Item>> choices = new ArrayList<>();
    private final AvatarAssets.Item[] selected = new AvatarAssets.Item[12];
    private final int[] palette = new int[12];
    private final Map<Integer, Integer> itemColors = new HashMap<>();
    private final Map<Integer, Image> previewImages = new HashMap<>();
    private final List<Texture> previewTextures = new ArrayList<>();
    private final List<Texture> swatchTextures = new ArrayList<>();
    private AvatarAssets assets;
    private java.util.function.Consumer<top.totodile.pokemmo.protocol.CharacterPackets.Appearance> onCreate;
    private boolean submitting;
    private boolean rebuilding;
    private boolean previewDirty = true;
    private int previewMode;
    private int coloringCategory = -1;
    private int labelWidth;
    private int optionWidth;
    private int rowHeight;
    private int rowStep;
    private int rowTop;
    private int columnGap;
    private int previewGap;
    private int submitHeight;
    private Dimension previewOrigin;
    private Dimension arrowOrigin;
    private Dimension paletteOrigin;
    private Dimension colorPreviewOrigin;
    private Dimension colorConfirmSize;
    private int previewStep;
    private int spriteScale;
    private int portraitScale;
    private int arrowSize;
    private int arrowSeparation;
    private int paletteCell;
    private int paletteStep;
    private int colorArrowY;
    private int colorArrowSeparation;
    private int colorConfirmY;

    CharacterCreationWindow(Runnable onClose) {
        setTheme("charactercreate-window");
        setTitle(ClientStrings.get(1050));
        setResizableAxis(ResizableAxis.NONE);
        setDraggable(false);
        addCloseCallback(() -> {
            if (submitting) return;
            if (coloringCategory >= 0) closeColors();
            else onClose.run();
        });
        try {
            assets = new AvatarAssets(ClientFiles.data("sprites/addons.pak"));
        } catch (IOException exception) {
            System.getLogger(CharacterCreationWindow.class.getName()).log(System.Logger.Level.ERROR,
                    "Unable to load avatar resources", exception);
        }
        addRow(1055, combo(ClientStrings.get(1056), ClientStrings.get(1057)));
        String[] skins = new String[5];
        for (int i = 0; i < skins.length; i++) {
            skins[i] = ClientStrings.get(2808).replace("{00}", Integer.toString(i + 1));
        }
        addRow(2870, combo(skins));
        int[] categories = {2872, 2873, 2874, 2875, 2876, 2877, 2878, 2879, 2880, 2882};
        int[] initialItems = {2803, 22006, 23011, 2803, 2803, 26005, 2803, 28000, 29000, 31000};
        for (int i = 0; i < categories.length; i++) {
            ComboBox<String> choice = combo(ClientStrings.get(initialItems[i]));
            choice.setEnabled(false);
            addRow(categories[i], choice);
            int category = CATEGORIES[i];
            Button color = new Button();
            color.setTheme("color-wheel");
            color.setVisible(false);
            color.addCallback(() -> openColors(category));
            colorButtons.add(color);
            add(color);
            choices.add(List.of());
            choice.addCallback(() -> selectAppearance(category));
        }
        options.get(0).addCallback(() -> {
            if (rebuilding) return;
            itemColors.clear();
            options.get(1).setSelected(ThreadLocalRandom.current().nextInt(5));
            rebuildChoices(true);
        });
        options.get(1).addCallback(() -> previewDirty = true);
        name.setTheme("editfield");
        name.setMaxTextLength(16);
        nameLabel.setLabelFor(name);
        region.setEnabled(false);
        create.setTheme("submit");
        create.setEnabled(false);
        create.addCallback(() -> {
            if (onCreate != null && !submitting) onCreate.accept(appearance());
        });
        name.addCallback((key) -> updateSubmit());
        add(nameLabel);
        add(name);
        add(regionLabel);
        add(region);
        add(create);
        previous.setTheme("arrow-left");
        next.setTheme("arrow-right");
        previous.addCallback(() -> previewMode = Math.floorMod(previewMode - 1, 5));
        next.addCallback(() -> previewMode = (previewMode + 1) % 5);
        previous.setEnabled(assets != null);
        next.setEnabled(assets != null);
        add(previous);
        add(next);
        finishColor.setTheme("color-confirm");
        finishColor.setVisible(false);
        finishColor.addCallback(this::closeColors);
        add(finishColor);
        if (assets != null) {
            for (AvatarAssets.Color color : assets.colors()) {
                Button swatch = new SwatchButton(color.swatch());
                swatch.setTheme("color-swatch");
                swatch.setTooltipContent(ClientStrings.get(32000 + color.id()));
                swatch.setVisible(false);
                swatch.addCallback(() -> {
                    palette[coloringCategory] = color.id();
                    AvatarAssets.Item item = selected[coloringCategory];
                    itemColors.put(item.category * 65536 + item.id, color.id());
                    previewDirty = true;
                });
                swatches.add(swatch);
                add(swatch);
            }
        }
    }

    AvatarAssets assets() { return assets; }

    void reset() {
        rebuilding = true;
        submitting = false;
        name.setText("");
        Arrays.fill(selected, null);
        itemColors.clear();
        closeColors();
        previewMode = 0;
        options.get(0).setSelected(ThreadLocalRandom.current().nextInt(2));
        options.get(1).setSelected(ThreadLocalRandom.current().nextInt(5));
        rebuilding = false;
        rebuildChoices(true);
        setSubmitting(false);
    }

    void setOnCreate(java.util.function.Consumer<top.totodile.pokemmo.protocol.CharacterPackets.Appearance> callback) {
        onCreate = callback;
        updateSubmit();
    }

    private top.totodile.pokemmo.protocol.CharacterPackets.Appearance appearance() {
        int[] items = new int[12];
        java.util.Arrays.fill(items, -1);
        items[10] = 0;
        for (int category : CATEGORIES) items[category] = selected[category] == null ? -1 : selected[category].id;
        return new top.totodile.pokemmo.protocol.CharacterPackets.Appearance(name.getText(),
                options.get(0).getSelected(), options.get(1).getSelected(), 0, items, palette);
    }

    void setSubmitting(boolean value) {
        submitting = value;
        create.setText(ClientStrings.get(value ? 9024 : 1051));
        name.setEnabled(!value);
        previous.setEnabled(!value && assets != null);
        next.setEnabled(!value && assets != null);
        for (int i = 0; i < options.size(); i++) {
            options.get(i).setEnabled(options.get(i).getModel().getNumEntries() > 1);
        }
        if (assets != null) updateHat();
        colorButtons.forEach(button -> button.setEnabled(true));
        updateSubmit();
    }

    private void updateSubmit() {
        create.setEnabled(onCreate != null && !submitting && assets != null && name.getText().matches("[\\p{L}0-9_]{2,16}"));
    }

    private void rebuildChoices(boolean randomize) {
        if (rebuilding || assets == null) return;
        rebuilding = true;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int category = CATEGORIES[i];
            AvatarAssets.Item old = selected[category];
            List<AvatarAssets.Item> entries = assets.choices(category, options.get(0).getSelected());
            choices.set(i, entries);
            ComboBox<String> combo = options.get(i + 2);
            combo.setModel(new SimpleChangableListModel<>(entries.stream().map(Object::toString).toArray(String[]::new)));
            int index = -1;
            if (!randomize && old != null) {
                for (int j = 0; j < entries.size(); j++) if (entries.get(j).id == old.id) index = j;
            }
            if (index < 0 && !entries.isEmpty()) {
                index = category == 4 ? 0 : ThreadLocalRandom.current().nextInt(entries.size());
            }
            combo.setSelected(index);
            combo.setEnabled(entries.size() > 1);
            updateSelected(category, i, index);
        }
        rebuilding = false;
        updateHat();
        previewDirty = true;
    }

    private void selectAppearance(int category) {
        if (rebuilding || assets == null) return;
        int row = rowFor(category);
        updateSelected(category, row, options.get(row + 2).getSelected());
        updateHat();
        previewDirty = true;
    }

    private void updateSelected(int category, int row, int index) {
        AvatarAssets.Item item = index < 0 ? null : choices.get(row).get(index);
        selected[category] = item;
        palette[category] = item == null || !item.colorable() ? 0
                : itemColors.computeIfAbsent(category * 65536 + item.id, key -> ThreadLocalRandom.current().nextInt(48));
        colorButtons.get(row).setVisible(item != null && item.colorable() && coloringCategory < 0);
    }

    private void updateHat() {
        AvatarAssets.Item hair = selected[2];
        options.get(2).setEnabled(choices.get(0).size() > 1 && (hair == null || (hair.flags & 65536) == 0));
    }

    private static int rowFor(int category) {
        for (int i = 0; i < CATEGORIES.length; i++) if (CATEGORIES[i] == category) return i;
        throw new IllegalArgumentException("Invalid appearance category");
    }

    private void openColors(int category) {
        coloringCategory = category;
        setIdentityVisible(false);
        labels.forEach(label -> label.setVisible(false));
        options.forEach(option -> option.setVisible(false));
        colorButtons.forEach(button -> button.setVisible(false));
        swatches.forEach(button -> button.setVisible(true));
        finishColor.setVisible(true);
        create.setVisible(false);
        swatches.get(palette[category]).requestKeyboardFocus();
        invalidateLayout();
    }

    private void closeColors() {
        coloringCategory = -1;
        setIdentityVisible(true);
        labels.forEach(label -> label.setVisible(true));
        options.forEach(option -> option.setVisible(true));
        for (int i = 0; i < colorButtons.size(); i++) {
            AvatarAssets.Item item = selected[CATEGORIES[i]];
            colorButtons.get(i).setVisible(item != null && item.colorable());
        }
        swatches.forEach(button -> button.setVisible(false));
        finishColor.setVisible(false);
        create.setVisible(true);
        invalidateLayout();
    }

    private void setIdentityVisible(boolean visible) {
        nameLabel.setVisible(visible);
        name.setVisible(visible);
        regionLabel.setVisible(visible);
        region.setVisible(visible);
    }

    private void addRow(int stringId, ComboBox<String> choice) {
        Label title = label(stringId);
        title.setLabelFor(choice);
        labels.add(title);
        options.add(choice);
        add(title);
        add(choice);
    }

    private static Label label(int stringId) {
        Label label = new Label(ClientStrings.get(stringId));
        label.setTheme("customization-label");
        return label;
    }

    private static ComboBox<String> combo(String... values) {
        ComboBox<String> combo = new AppearanceComboBox(values);
        combo.setSelected(0);
        return combo;
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        labelWidth = themeInfo.getParameter("labelWidth", 150);
        optionWidth = themeInfo.getParameter("optionWidth", 260);
        rowHeight = themeInfo.getParameter("rowHeight", 40);
        rowStep = themeInfo.getParameter("rowStep", 49);
        rowTop = themeInfo.getParameter("rowTop", 46);
        columnGap = themeInfo.getParameter("columnGap", 10);
        previewGap = themeInfo.getParameter("previewGap", 24);
        submitHeight = themeInfo.getParameter("submitHeight", 50);
        previewOrigin = themeInfo.getParameterValue("previewOrigin", true, Dimension.class, new Dimension(620, 120));
        arrowOrigin = themeInfo.getParameterValue("arrowOrigin", true, Dimension.class, new Dimension(530, 450));
        paletteOrigin = themeInfo.getParameterValue("paletteOrigin", true, Dimension.class, new Dimension(25, 50));
        colorPreviewOrigin = themeInfo.getParameterValue("colorPreviewOrigin", true, Dimension.class, new Dimension(160, 250));
        colorConfirmSize = themeInfo.getParameterValue("colorConfirmSize", true, Dimension.class, new Dimension(150, 50));
        previewStep = themeInfo.getParameter("previewStep", 160);
        spriteScale = themeInfo.getParameter("spriteScale", 4);
        portraitScale = themeInfo.getParameter("portraitScale", 3);
        arrowSize = themeInfo.getParameter("arrowSize", 64);
        arrowSeparation = themeInfo.getParameter("arrowSeparation", 200);
        paletteCell = themeInfo.getParameter("paletteCell", 30);
        paletteStep = themeInfo.getParameter("paletteStep", 35);
        colorArrowY = themeInfo.getParameter("colorArrowY", 500);
        colorArrowSeparation = themeInfo.getParameter("colorArrowSeparation", 350);
        colorConfirmY = themeInfo.getParameter("colorConfirmY", 200);
    }

    @Override
    protected void layout() {
        super.layout();
        int left = getInnerX();
        for (int i = 0; i < labels.size(); i++) {
            int y = getY() + rowTop + rowStep * i;
            place(labels.get(i), left, y, labelWidth, rowHeight);
            place(options.get(i), left + labelWidth + columnGap, y, optionWidth, rowHeight);
            if (i >= 2) place(colorButtons.get(i - 2), left + labelWidth + columnGap + optionWidth + 2,
                    y + (rowHeight - 20) / 2, 20, 20);
        }
        int right = left + labelWidth + columnGap + optionWidth + previewGap;
        int inputLeft = right + labelWidth + columnGap;
        int inputWidth = Math.max(1, getInnerRight() - inputLeft - columnGap);
        place(nameLabel, right, getY() + rowTop, labelWidth, rowHeight);
        place(name, inputLeft, getY() + rowTop, inputWidth, rowHeight);
        place(regionLabel, right, getY() + rowTop + rowStep, labelWidth, rowHeight);
        place(region, inputLeft, getY() + rowTop + rowStep, inputWidth, rowHeight);
        place(create, right, getInnerBottom() - submitHeight,
                Math.max(1, getInnerRight() - right - columnGap), submitHeight);
        boolean coloring = coloringCategory >= 0;
        int separation = coloring ? colorArrowSeparation : arrowSeparation;
        int arrowX = coloring ? getX() + (getWidth() - separation - arrowSize) / 2
                : getX() + arrowOrigin.getX();
        int arrowY = getY() + (coloring ? colorArrowY : arrowOrigin.getY());
        place(previous, arrowX, arrowY, arrowSize, arrowSize);
        place(next, arrowX + separation, arrowY, arrowSize, arrowSize);
        int paletteWidth = 15 * paletteStep + paletteCell;
        for (int i = 0; i < swatches.size(); i++) {
            place(swatches.get(i), getX() + (getWidth() - paletteWidth) / 2 + i / 3 * paletteStep,
                    getY() + paletteOrigin.getY() + i % 3 * paletteStep, paletteCell, paletteCell);
        }
        place(finishColor, getX() + (getWidth() - colorConfirmSize.getX()) / 2,
                getY() + colorConfirmY, colorConfirmSize.getX(), colorConfirmSize.getY());
    }

    @Override
    protected void paintWidget(GUI gui) {
        super.paintWidget(gui);
        if (assets == null) return;
        if (previewDirty) {
            previewTextures.forEach(Texture::destroy);
            previewTextures.clear();
            previewImages.clear();
            previewDirty = false;
        }
        int tick = (int) (System.nanoTime() / 160_000_000L % 4);
        for (int direction = 0; direction < 3; direction++) {
            if (previewMode >= 3 && direction != 1) continue;
            int view = previewMode == 3 ? 2 : previewMode == 4 ? 1 : 0;
            int frame = previewMode < 3 ? WALK_FRAMES[previewMode * 3 + direction][tick]
                    : previewMode == 3 ? options.get(0).getSelected()
                    : PORTRAIT_FRAMES[(int) (System.nanoTime() / 150_000_000L % PORTRAIT_FRAMES.length)];
            int key = view * 256 + frame;
            Image image = previewImages.get(key);
            if (image == null) {
                try {
                    BufferedImage pixels = assets.render(options.get(1).getSelected(), selected, palette, view, frame);
                    Texture texture = ((GlfwTwlRenderer) gui.getRenderer()).uploadImage(pixels);
                    previewTextures.add(texture);
                    image = texture.getImage(0, 0, pixels.getWidth(), pixels.getHeight(),
                            Color.WHITE, false, Texture.Rotation.NONE);
                    previewImages.put(key, image);
                } catch (IOException exception) {
                    System.getLogger(CharacterCreationWindow.class.getName()).log(System.Logger.Level.ERROR,
                            "Unable to render avatar preview", exception);
                    assets = null;
                    return;
                }
            }
            int scale = previewMode < 3 ? spriteScale : portraitScale;
            int center = (previous.getX() + next.getX() + arrowSize) / 2;
            if (coloringCategory >= 0) center += (direction - 1) * previewStep;
            int x = center - image.getWidth() * scale / 2;
            int y = getY() + (coloringCategory >= 0 ? colorPreviewOrigin.getY() : previewOrigin.getY() + direction * previewStep);
            image.draw(getAnimationState(), x, y, image.getWidth() * scale, image.getHeight() * scale);
        }
    }

    void close() {
        previewTextures.forEach(Texture::destroy);
        previewTextures.clear();
        previewImages.clear();
        swatchTextures.forEach(Texture::destroy);
        swatchTextures.clear();
        previewDirty = true;
    }

    private final class SwatchButton extends Button {
        private final int argb;
        private Image image;

        private SwatchButton(int argb) {
            this.argb = argb;
        }

        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if (image == null) {
                BufferedImage pixel = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                pixel.setRGB(0, 0, argb);
                Texture texture = ((GlfwTwlRenderer) gui.getRenderer()).uploadImage(pixel);
                swatchTextures.add(texture);
                image = texture.getImage(0, 0, 1, 1, Color.WHITE, false, Texture.Rotation.NONE);
            }
            image.draw(getAnimationState(), getX() + 2, getY() + 2, getWidth() - 4, getHeight() - 4);
        }
    }

    private static void place(Widget widget, int x, int y, int width, int height) {
        widget.setPosition(x, y);
        widget.setSize(width, height);
    }

    private static final class AppearanceComboBox extends ComboBox<String> {
        private int maximumRows = 8;

        private AppearanceComboBox(String[] values) {
            super(new SimpleChangableListModel<>(values));
            setTheme("combobox");
        }

        @Override
        protected void applyTheme(ThemeInfo themeInfo) {
            super.applyTheme(themeInfo);
            maximumRows = Math.max(1, themeInfo.getParameter("maximumRows", 8));
        }

        @Override
        protected void setPopupSize() {
            ListBox<?> list = (ListBox<?>) popup.getChild(0);
            int rows = Math.max(1, Math.min(maximumRows, getModel().getNumEntries()));
            int height = rows * list.getCellHeight() + list.getBorderVertical() + popup.getBorderVertical();
            popup.setMinSize(0, height);
            popup.setMaxSize(Integer.MAX_VALUE, height);
            super.setPopupSize();
        }
    }

    private static final class CharacterNameField extends LoginEditField {
        @Override
        protected void insertChar(char character) {
            if (Character.isLetterOrDigit(character) || character == '_') super.insertChar(character);
        }

        @Override
        public void insertText(String text) {
            StringBuilder filtered = new StringBuilder(text.length());
            for (int i = 0; i < text.length(); i++) {
                char character = text.charAt(i);
                if (Character.isLetterOrDigit(character) || character == '_') filtered.append(character);
            }
            if (!filtered.isEmpty()) super.insertText(filtered.toString());
        }
    }
}
