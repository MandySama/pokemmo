package top.totodile.pokemmo.client.ui.character;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.input.TextInputWrapper;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleListModel;
import de.matthiasmann.twl.utils.TintAnimator;

import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.common.ClientWindow;

import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/** 创建角色表单、起始地区、外观染色与动态预览。 */
public final class CharacterCreationWindow extends ClientWindow {
    private final boolean mobile;
    private final ClientStrings strings;
    private final Runnable onClose;
    private final Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground;
    private final NameField name = new NameField();
    private final Choice gender, skin, region;
    private List<Integer> regionIds = List.of(0);
    private final CharacterAppearance appearanceState;
    private final CharacterPreview preview;
    private final Button previousPreview = new Button(""), nextPreview = new Button("");
    private final EnumMap<CharacterAppearance.Slot, Choice> appearanceChoices =
            new EnumMap<>(CharacterAppearance.Slot.class);
    private final EnumMap<CharacterAppearance.Slot, Button> colorActions =
            new EnumMap<>(CharacterAppearance.Slot.class);
    private final DialogLayout colorPanel = new DialogLayout();
    private final Button applyColor;
    private final Button[] colorButtons = new Button[AppearancePalette.COLORS.length];
    private CharacterAppearance.Slot colorSlot;
    private int pendingColor;
    private final Button create;
    private final Label nameLabel, regionLabel;
    private final DialogLayout appearance = new DialogLayout();
    private final DialogLayout details = new DialogLayout();
    private final Container content =
            new Container() {
                @Override
                protected void layout() {
                    layoutForm();
                }
            };
    private boolean nativeEditor;
    private boolean customization;
    private Runnable submit = () -> {};
    private long version;
    private final LongSupplier clock;
    private final Consumer<String> notification;
    private final ResizableFrame frame =
            new ResizableFrame() {
                private int headerActionRight;

                @Override
                protected void applyTheme(ThemeInfo theme) {
                    super.applyTheme(theme);
                    if (mobile) headerActionRight = theme.getParameter("headerActionRight", 68);
                }

                @Override
                protected void paintBackground(GUI gui) {
                    if (mobile) mobileBackground.accept(getBackground());
                    else super.paintBackground(gui);
                }

                @Override
                protected boolean isFrameElement(Widget child) {
                    return mobile && child == create || super.isFrameElement(child);
                }

                @Override
                protected void layout() {
                    super.layout();
                    if (mobile) {
                        create.adjustSize();
                        create.setPosition(
                                getInnerRight() - create.getWidth() - headerActionRight,
                                getInnerY());
                    }
                }
            };

    public CharacterCreationWindow(
            Widget owner,
            ClientStrings strings,
            boolean mobile,
            Runnable onClose,
            Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground,
            CharacterAppearance appearanceState,
            GdxTwlRenderer renderer,
            LongSupplier clock,
            Consumer<String> notification) {
        this(
                owner,
                strings,
                mobile,
                onClose,
                mobileBackground,
                appearanceState,
                renderer,
                clock,
                notification,
                false);
    }

    public CharacterCreationWindow(
            Widget owner,
            ClientStrings strings,
            boolean mobile,
            Runnable onClose,
            Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground,
            CharacterAppearance appearanceState,
            GdxTwlRenderer renderer,
            LongSupplier clock,
            Consumer<String> notification,
            boolean customization) {
        super(owner);
        this.customization = customization;
        this.mobile = mobile;
        this.strings = strings;
        this.onClose = onClose;
        this.mobileBackground = mobileBackground;
        this.appearanceState = appearanceState;
        this.clock = clock;
        this.notification = notification;
        frame.setTheme("/customize-widget");
        frame.setTitle(strings.get(1050));
        frame.setDraggable(false);
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        frame.addCloseCallback(this::back);
        content.setTheme("dialoglayout");
        appearance.setTheme("dialoglayout");
        appearance.setAddDefaultGaps(false);
        Label genderLabel = label(strings.get(1055));
        Label skinLabel = label(strings.get(2870));
        gender = new Choice(strings.get(1056), strings.get(1057));
        String[] colors = new String[5];
        for (int i = 0; i < colors.length; i++)
            colors[i] = strings.format(2808, Integer.toString(i + 1));
        skin = new Choice(colors);
        preview =
                new CharacterPreview(
                        appearanceState,
                        renderer,
                        mobile,
                        () -> skin.selected,
                        () -> gender.selected,
                        clock);
        preview.customization(customization);
        var labels = appearance.createParallelGroup(genderLabel, skinLabel);
        var choices = appearance.createParallelGroup(gender, skin);
        var actions = appearance.createParallelGroup();
        var rows = appearance.createSequentialGroup();
        rows.addGroup(appearance.createParallelGroup(genderLabel, gender))
                .addGap(DialogLayout.DEFAULT_GAP);
        rows.addGroup(appearance.createParallelGroup(skinLabel, skin));
        for (var slot : CharacterAppearance.Slot.values()) {
            if (slot.id == 0 || !customization && slot.id == 10) continue;
            Label title = label(strings.get(2871 + slot.id));
            Choice choice = new Choice(strings.get(2803));
            Button color =
                    new Button("") {
                        private de.matthiasmann.twl.renderer.Image icon;

                        @Override
                        protected void applyTheme(ThemeInfo theme) {
                            super.applyTheme(theme);
                            icon = theme.getImage("colorIcon");
                        }

                        @Override
                        public int getPreferredInnerWidth() {
                            return mobile ? 32 : 16;
                        }

                        @Override
                        public int getPreferredInnerHeight() {
                            return mobile ? 32 : 16;
                        }

                        @Override
                        protected void paintWidget(GUI gui) {
                            if (icon != null) {
                                int size = mobile ? 32 : 16;
                                icon.draw(
                                        getAnimationState(),
                                        getInnerX() + (getInnerWidth() - size) / 2,
                                        getInnerY() + (getInnerHeight() - size) / 2,
                                        size,
                                        size);
                            }
                        }
                    };
            color.setTheme("/customization-color");
            if (mobile) color.setCanAcceptKeyboardFocus(false);
            color.addCallback(() -> openColorPicker(slot));
            appearanceChoices.put(slot, choice);
            colorActions.put(slot, color);
            choice.onSelected =
                    () -> {
                        appearanceState.select(slot, choice.selected);
                        updateAppearanceControls();
                    };
            labels.addWidget(title);
            choices.addWidget(choice);
            actions.addWidget(color);
            rows.addGap(DialogLayout.DEFAULT_GAP)
                    .addGroup(appearance.createParallelGroup(title, choice, color));
        }
        appearance.setHorizontalGroup(
                appearance
                        .createSequentialGroup()
                        .addGroup(labels)
                        .addGap(DialogLayout.DEFAULT_GAP)
                        .addGroup(choices)
                        .addGap(DialogLayout.DEFAULT_GAP)
                        .addGroup(actions));
        appearance.setVerticalGroup(rows);
        gender.onSelected =
                () -> {
                    if (customization) return;
                    skin.select(ThreadLocalRandom.current().nextInt(5));
                    resetAppearance(strings);
                };
        name.setTheme("editfield");
        name.setMaxTextLength(16);
        nameLabel = label(strings.get(1053));
        nameLabel.setLabelFor(name);
        regionLabel = label(strings.get(1060));
        region = new Choice(strings.get(250000));
        region.setEnabled(false);
        create = new Button(strings.get(1051));
        create.addCallback(
                () -> {
                    hideKeyboard();
                    submit.run();
                });
        if (mobile) {
            create.setText("");
            create.setTheme("/mobile-save-icon");
            create.setCanAcceptKeyboardFocus(false);
            frame.add(create);
        } else content.add(create);
        content.add(appearance);
        if (mobile) {
            details.setTheme("dialoglayout");
            details.setAddDefaultGaps(false);
            details.setHorizontalGroup(
                    details.createSequentialGroup()
                            .addGroup(details.createParallelGroup(nameLabel, regionLabel))
                            .addGap(DialogLayout.DEFAULT_GAP)
                            .addGroup(details.createParallelGroup(name, region)));
            details.setVerticalGroup(
                    details.createSequentialGroup()
                            .addGroup(details.createParallelGroup(nameLabel, name))
                            .addGap(DialogLayout.DEFAULT_GAP)
                            .addGroup(details.createParallelGroup(regionLabel, region)));
            content.add(details);
        } else {
            content.add(nameLabel);
            content.add(name);
            content.add(regionLabel);
            content.add(region);
        }
        colorPanel.setTheme("/color-dialog");
        colorPanel.setAddDefaultGaps(false);
        var colorColumns = colorPanel.createSequentialGroup().addGap();
        var colorRows = colorPanel.createParallelGroup();
        for (int column = 0; column < colorButtons.length / 3; column++) {
            var columnWidth = colorPanel.createParallelGroup();
            var columnRows = colorPanel.createSequentialGroup();
            for (int row = 0; row < 3; row++) {
                int index = column * 3 + row;
                Button swatch =
                        new Button("") {
                            @Override
                            public int getPreferredWidth() {
                                return mobile ? 70 : 30;
                            }

                            @Override
                            public int getPreferredHeight() {
                                return mobile ? 70 : 30;
                            }
                        };
                swatch.setTheme("/color-button");
                swatch.setTintAnimator(
                        new TintAnimator(
                                swatch, new Color(0xFF000000 | AppearancePalette.COLORS[index])));
                swatch.addCallback(
                        () -> {
                            pendingColor = index;
                            selectColorButton();
                        });
                if (mobile) swatch.setCanAcceptKeyboardFocus(false);
                colorButtons[index] = swatch;
                columnWidth.addWidget(swatch);
                if (row > 0) columnRows.addGap(DialogLayout.DEFAULT_GAP);
                columnRows.addWidget(swatch);
            }
            if (column > 0) colorColumns.addGap(DialogLayout.DEFAULT_GAP);
            colorColumns.addGroup(columnWidth);
            colorRows.addGroup(columnRows);
        }
        colorPanel.setHorizontalGroup(colorColumns.addGap());
        colorPanel.setVerticalGroup(colorRows);
        colorPanel.setVisible(false);
        content.add(colorPanel);
        applyColor = new Button(strings.get(2801));
        applyColor.setVisible(false);
        applyColor.addCallback(this::closeColorPicker);
        if (!mobile) content.add(applyColor);
        content.add(preview);
        previousPreview.setTheme("/customization-preview-previous");
        nextPreview.setTheme("/customization-preview-next");
        previousPreview.addCallback(() -> preview.turn(-1));
        nextPreview.addCallback(() -> preview.turn(1));
        if (mobile) {
            previousPreview.setCanAcceptKeyboardFocus(false);
            nextPreview.setCanAcceptKeyboardFocus(false);
        }
        content.add(previousPreview);
        content.add(nextPreview);
        frame.add(content);
        add(frame);
    }

    private static Label label(String text) {
        Label label = new Label(text);
        label.setTheme("/customization-label");
        return label;
    }

    private void layoutForm() {
        appearance.adjustSize();
        appearance.setPosition(content.getInnerX(), content.getInnerY());
        int x = appearance.getRight() + 4;
        if (mobile) {
            details.adjustSize();
            details.setPosition(x, content.getInnerY());
        } else {
            int labelWidth = 148;
            int inputX = x + labelWidth + 4;
            int inputWidth = Math.max(0, content.getInnerRight() - inputX);
            int y = content.getInnerY();
            nameLabel.setPosition(x, y);
            nameLabel.setSize(labelWidth, 40);
            name.setPosition(inputX, y);
            name.setSize(inputWidth, 40);
            regionLabel.setPosition(x, y + 46);
            regionLabel.setSize(labelWidth, 40);
            region.setPosition(inputX, y + 46);
            region.setSize(inputWidth, 40);
            create.setPosition(x, content.getInnerBottom() - 50);
            int available = Math.max(0, content.getInnerRight() - x);
            create.setSize(available, 50);
        }

        colorPanel.setPosition(content.getX(), content.getY());
        colorPanel.setSize(content.getWidth(), colorPanel.getPreferredHeight());
        if (!mobile) {
            applyColor.setSize(150, 50);
            applyColor.setPosition(
                    content.getX() + (content.getWidth() - 150) / 2, content.getY() + 200);
        }
        preview.setPosition(getX(), getY());
        preview.setSize(getWidth(), getHeight());
        previousPreview.adjustSize();
        nextPreview.adjustSize();
        int spacing = mobile ? 520 : customization ? 220 : 300;
        int centerX =
                mobile
                        ? 640 + spacing / 2 + previousPreview.getWidth() / 2
                        : (x + content.getInnerRight()) / 2 - getX();
        int arrowX = centerX - spacing / 2 - previousPreview.getWidth() / 2;
        int arrowY = mobile ? 550 : customization ? 550 : 450;
        if (customization && !mobile) arrowX = 440;
        if (preview.coloring()) {
            arrowX = getWidth() / 2 - spacing / 2 - previousPreview.getWidth() / 2;
            arrowY = mobile ? 550 : 500;
        }
        int rightX = Math.max(0, Math.min(getWidth() - nextPreview.getWidth(), arrowX + spacing));
        previousPreview.setPosition(getX() + arrowX, getY() + arrowY);
        nextPreview.setPosition(getX() + rightX, getY() + arrowY);
        preview.centerBetween(previousPreview.getRight(), nextPreview.getX());
    }

    private void resetAppearance(ClientStrings strings) {
        appearanceState.reset(gender.selected);
        for (var slot : CharacterAppearance.Slot.values()) {
            if (slot.id == 0 || !customization && slot.id == 10) continue;
            Choice choice = appearanceChoices.get(slot);
            List<CharacterAppearance.Entry> options = appearanceState.choices(slot);
            choice.setOptions(
                    options.stream().map(entry -> entry.name(strings)).toArray(String[]::new));
            choice.select(options.indexOf(appearanceState.selected(slot)));
        }
        updateAppearanceControls();
    }

    private void updateAppearanceControls() {
        for (var slot : CharacterAppearance.Slot.values()) {
            if (slot.id == 0 || !customization && slot.id == 10) continue;
            appearanceChoices.get(slot).setEnabled(appearanceState.enabled(slot));
            colorActions.get(slot).setVisible(appearanceState.selected(slot).colorable());
        }
    }

    private void openColorPicker(CharacterAppearance.Slot slot) {
        hideKeyboard();
        if (!appearanceState.selected(slot).colorable()) return;
        colorSlot = slot;
        pendingColor = Math.max(0, appearanceState.color(slot));
        preview.color(slot, pendingColor);
        selectColorButton();
        showForm(false);
        colorPanel.setVisible(true);
        applyColor.setVisible(!mobile);
        if (!mobile) colorButtons[pendingColor].requestKeyboardFocus();
        content.invalidateLayout();
    }

    private void selectColorButton() {
        if (colorSlot != null) preview.color(colorSlot, pendingColor);
        for (int index = 0; index < colorButtons.length; index++)
            colorButtons[index].getModel().setSelected(index == pendingColor);
    }

    private void closeColorPicker() {
        if (colorSlot == null) return;
        var slot = colorSlot;
        appearanceState.color(slot, pendingColor);
        colorSlot = null;
        preview.color(null, 0);
        colorPanel.setVisible(false);
        applyColor.setVisible(false);
        showForm(true);
        if (!mobile) colorActions.get(slot).requestKeyboardFocus();
        content.invalidateLayout();
    }

    private void showForm(boolean visible) {
        appearance.setVisible(visible);
        nameLabel.setVisible(visible && !customization);
        name.setVisible(visible && !customization);
        regionLabel.setVisible(visible && !customization);
        region.setVisible(visible && !customization);
        create.setVisible(visible);
    }

    public void openCustomization(top.totodile.pokemmo.server.game.GameService.Appearance saved) {
        if (!customization) throw new IllegalStateException();
        version++;
        gender.select(saved.gender());
        skin.select(saved.skin());
        regionIds = List.of(saved.region());
        region.setOptions(new String[] {strings.get(250000 + saved.region())});
        region.select(0);
        appearanceState.loadSaved(saved, true);
        for (var slot : appearanceChoices.keySet()) {
            var options = appearanceState.choices(slot);
            var choice = appearanceChoices.get(slot);
            choice.setOptions(options.stream().map(e -> e.name(strings)).toArray(String[]::new));
            choice.select(options.indexOf(appearanceState.selected(slot)));
        }
        gender.setEnabled(true);
        skin.setEnabled(true);
        frame.setTitle(strings.get(2800));
        create.setText(mobile ? "" : strings.get(2802));
        showForm(true);
        updateAppearanceControls();
        preview.reset();
        if (openPopup()) {
            positionWindow();
            frame.setHardVisible(true);
        }
    }

    public top.totodile.pokemmo.server.game.GameService.Appearance randomAppearance(
            top.totodile.pokemmo.server.game.GameService.Appearance saved) {
        var model = appearanceState.copy();
        model.loadSaved(saved, true);
        int gender = ThreadLocalRandom.current().nextInt(2),
                skin = ThreadLocalRandom.current().nextInt(5);
        model.reset(gender);
        return model.snapshot(gender, skin, saved.region());
    }

    public void saving(boolean saving) {
        create.setEnabled(!saving);
    }

    public void open() {
        if (isOpen()) {
            raiseWindow();
            return;
        }
        version++;
        name.setErrorMessage(null);
        name.setText("");
        gender.select(ThreadLocalRandom.current().nextInt(2));
        skin.select(ThreadLocalRandom.current().nextInt(5));
        resetAppearance(strings);
        preview.reset();
        if (openPopup()) {
            positionWindow();
            frame.setHardVisible(true);
            // 移动端不因打开页面自动弹出输入法。
            if (!mobile) name.requestKeyboardFocus();
        }
    }

    public void positionWindow() {
        if (!isOpen()) return;
        Widget owner = getOwner();
        int width =
                mobile
                        ? owner.getInnerWidth()
                        : Math.min(customization ? 740 : 880, owner.getInnerWidth());
        int height = mobile ? owner.getInnerHeight() : Math.min(680, owner.getInnerHeight());
        setSize(width, height);
        setPosition(
                owner.getInnerX() + (owner.getInnerWidth() - width) / 2,
                owner.getInnerY() + (owner.getInnerHeight() - height) / 2);
    }

    public void hideKeyboard() {
        if (nativeEditor) Gdx.input.closeTextInputField(false);
    }

    public void onCreate(Runnable submit) {
        this.submit = submit;
    }

    public long version() {
        return version;
    }

    public String characterName() {
        return name.getText();
    }

    public void availableRegions(List<Integer> regions) {
        int previous = regionIds.isEmpty() ? -1 : regionIds.get(region.selected);
        if (regions.stream().anyMatch(id -> id < 0 || id > 4)
                || regions.stream().distinct().count() != regions.size())
            throw new IllegalArgumentException("Invalid starting regions");
        regionIds = List.copyOf(regions);
        region.setOptions(
                regionIds.isEmpty()
                        ? new String[] {""}
                        : regionIds.stream()
                                .map(id -> strings.get(250000 + id))
                                .toArray(String[]::new));
        int selected = regionIds.indexOf(previous);
        if (selected >= 0) region.select(selected);
        region.setEnabled(regionIds.size() > 1);
        create.setEnabled(!regionIds.isEmpty());
    }

    public top.totodile.pokemmo.server.game.GameService.Appearance selectedAppearance() {
        if (regionIds.isEmpty())
            throw new IllegalStateException("No starting region ROM is loaded");
        return appearanceState.snapshot(
                gender.selected, skin.selected, regionIds.get(region.selected));
    }

    public CharacterAppearance savedAppearance(
            top.totodile.pokemmo.server.game.GameService.Appearance value) {
        return appearanceState.restore(value);
    }

    public void creationError(int stringId) {
        notification.accept(strings.get(stringId));
        if (!mobile) name.requestKeyboardFocus();
    }

    public void dispose() {
        preview.close();
        appearanceState.dispose();
    }

    public void back() {
        for (var choice : appearanceChoices.values()) if (choice.closeChoices()) return;
        if (gender.closeChoices() || skin.closeChoices() || region.closeChoices()) return;
        if (colorSlot != null) {
            closeColorPicker();
            return;
        }
        if (nativeEditor) hideKeyboard();
        else closePopup();
    }

    @Override
    public void closePopup() {
        hideKeyboard();
        closeColorPicker();
        for (var choice : appearanceChoices.values()) choice.closeChoices();
        gender.closeChoices();
        skin.closeChoices();
        region.closeChoices();
        boolean wasOpen = isOpen();
        if (wasOpen) version++;
        super.closePopup();
        if (wasOpen) onClose.run();
    }

    private final class NameField extends EditField {
        @Override
        public de.matthiasmann.twl.renderer.MouseCursor getMouseCursor(Event event) {
            var cursor = super.getMouseCursor(event);
            return mobile || cursor != null ? cursor : GdxTwlRenderer.SystemMouseCursor.TEXT;
        }

        @Override
        public void insertText(String text) {
            StringBuilder filtered = new StringBuilder();
            text.codePoints()
                    .filter(c -> Character.isLetter(c) || c >= '0' && c <= '9' || c == '_')
                    .forEach(filtered::appendCodePoint);
            super.insertText(filtered.toString());
        }

        @Override
        public boolean handleEvent(Event event) {
            boolean handled = super.handleEvent(event);
            if (event.getType() == Event.Type.MOUSE_BTNDOWN
                    && mobile
                    && !nativeEditor
                    && Gdx.app.getType() == ApplicationType.Android) {
                nativeEditor = true;
                String text = getText();
                int cursor = Math.min(getCursorPos(), text.length());
                Gdx.input.openTextInputField(
                        new NativeInputConfiguration()
                                .setType(Input.OnscreenKeyboardType.Default)
                                .setPreventCorrection(true)
                                .setMultiLine(false)
                                .setMaxLength(16)
                                .setPlaceholder(nameLabel.getText())
                                .setValidator(value -> value.matches("[\\p{L}0-9_]*"))
                                .setTextInputWrapper(
                                        new TextInputWrapper() {
                                            public String getText() {
                                                return text;
                                            }

                                            public int getSelectionStart() {
                                                return cursor;
                                            }

                                            public int getSelectionEnd() {
                                                return cursor;
                                            }

                                            public void writeResults(
                                                    String value, int start, int end) {
                                                if (!isOpen()) return;
                                                setText(value);
                                                setSelection(
                                                        Math.min(start, value.length()),
                                                        Math.min(end, value.length()));
                                            }
                                        })
                                .setCloseCallback(
                                        confirm -> {
                                            nativeEditor = false;
                                            return false;
                                        }));
            }
            return handled;
        }
    }

    private final class Choice extends Button {
        private String[] options;
        private Runnable onSelected = () -> {};
        private final Label display =
                new Label(getAnimationState()) {
                    @Override
                    protected boolean handleEvent(Event event) {
                        return false;
                    }
                };
        private final Widget arrow = new Widget(getAnimationState());
        private final PopupWindow popup = new PopupWindow(this);
        private int selected;

        Choice(String... options) {
            this.options = options;
            setTheme("combobox");
            display.setTheme("display");
            display.setClip(true);
            arrow.setTheme("button");
            add(display);
            add(arrow);
            popup.setTheme("/comboboxPopup");
            select(0);
            if (mobile) setCanAcceptKeyboardFocus(false);
            addCallback(this::openChoices);
        }

        void select(int index) {
            selected = index;
            display.setText(options[index]);
        }

        void setOptions(String[] options) {
            closeChoices();
            this.options = options;
            select(0);
        }

        boolean closeChoices() {
            if (!popup.isOpen()) return false;
            popup.closePopup();
            return true;
        }

        @Override
        public int getPreferredInnerWidth() {
            return display.getPreferredWidth() + arrow.getPreferredWidth();
        }

        @Override
        public int getPreferredInnerHeight() {
            return Math.max(display.getPreferredHeight(), arrow.getPreferredHeight());
        }

        @Override
        protected void paintWidget(GUI gui) {}

        @Override
        protected void layout() {
            int width = arrow.getPreferredWidth();
            arrow.setPosition(getInnerRight() - width, getInnerY());
            arrow.setSize(width, getInnerHeight());
            display.setPosition(getInnerX(), getInnerY());
            display.setSize(Math.max(0, getInnerWidth() - width), getInnerHeight());
        }

        private void openChoices() {
            hideKeyboard();
            if (closeChoices()) return;
            ListBox<String> list =
                    new ListBox<>(
                            new SimpleListModel<String>() {
                                public int getNumEntries() {
                                    return options.length;
                                }

                                public String getEntry(int index) {
                                    return options[index];
                                }
                            }) {
                        @Override
                        protected ListBoxDisplay createDisplay() {
                            return new ListBoxLabel() {
                                @Override
                                protected boolean handleListBoxEvent(Event event) {
                                    if (event.getType() == Event.Type.MOUSE_BTNUP
                                            && event.getMouseButton() == Event.MOUSE_LBUTTON) {
                                        doListBoxCallback(CallbackReason.MOUSE_DOUBLE_CLICK);
                                        return true;
                                    }
                                    return super.handleListBoxEvent(event);
                                }
                            };
                        }
                    };
            list.setTheme("listbox");
            list.setSelected(selected);
            list.addCallback(
                    reason -> {
                        if (reason.actionRequested() && list.getSelected() >= 0) {
                            select(list.getSelected());
                            popup.closePopup();
                            onSelected.run();
                        }
                    });
            popup.removeAllChildren();
            popup.add(list);
            if (popup.openPopup()) {
                int height =
                        Math.min(
                                list.getMaxHeight(),
                                options.length * list.getCellHeight() + list.getBorderVertical());
                int below = getGUI().getHeight() - getBottom();
                boolean down = below >= height || below >= getY();
                popup.setSize(getWidth(), Math.min(height, down ? below : getY()));
                popup.setPosition(
                        Math.max(0, Math.min(getX(), getGUI().getWidth() - popup.getWidth())),
                        down ? getBottom() : Math.max(0, getY() - popup.getHeight()));
                list.requestKeyboardFocus();
            }
        }
    }
}
