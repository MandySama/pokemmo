package top.totodile.pokemmo.client.ui.login;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleListModel;

import java.util.List;
import java.util.function.Consumer;

/** 保存账号选择器，最后一项切回手动输入。 */
final class AccountPicker extends Button {
    private final Label display =
            new Label(getAnimationState()) {
                @Override
                protected boolean handleEvent(Event event) {
                    return false;
                }
            };
    private final Widget arrow = new Widget(getAnimationState());
    private final PopupWindow popup = new PopupWindow(this);
    private List<String> names = List.of();
    private ListBox<String> list;
    private final String other;
    private final Consumer<String> onSelected;

    AccountPicker(String other, Consumer<String> onSelected) {
        this.other = other;
        this.onSelected = onSelected;
        setTheme("combobox");
        display.setTheme("display");
        display.setClip(true);
        add(display);
        arrow.setTheme("button");
        add(arrow);
        popup.setTheme("/comboboxPopup");
        addCallback(this::openAccounts);
    }

    void setAccounts(List<String> names, String selected) {
        this.names = List.copyOf(names);
        setText(selected == null ? other : selected);
        display.setText(getText());
    }

    @Override
    public int getPreferredInnerWidth() {
        return display.getPreferredWidth() + arrow.getPreferredWidth();
    }

    @Override
    public int getPreferredInnerHeight() {
        return Math.max(display.getPreferredHeight(), arrow.getPreferredHeight());
    }

    // 背景由 Button 绘制，文字使用独立区域，为右侧箭头留出空间。
    @Override
    protected void paintWidget(GUI gui) {}

    @Override
    protected void layout() {
        int arrowWidth = arrow.getPreferredWidth();
        arrow.setPosition(getInnerRight() - arrowWidth, getInnerY());
        arrow.setSize(arrowWidth, getInnerHeight());
        display.setPosition(getInnerX(), getInnerY());
        display.setSize(Math.max(0, getInnerWidth() - arrowWidth), getInnerHeight());
        if (popup.isOpen()) positionPopup();
    }

    private void openAccounts() {
        if (popup.isOpen()) {
            popup.closePopup();
            return;
        }
        list =
                new ListBox<String>(
                        new SimpleListModel<String>() {
                            public int getNumEntries() {
                                return names.size() + 1;
                            }

                            public String getEntry(int index) {
                                return index == names.size() ? other : names.get(index);
                            }
                        }) {
                    @Override
                    protected ListBoxDisplay createDisplay() {
                        return new ListBoxLabel() {
                            @Override
                            protected boolean handleListBoxEvent(Event event) {
                                if (event.getType() == Event.Type.MOUSE_BTNUP
                                        && event.getMouseButton() == Event.MOUSE_LBUTTON) {
                                    // 单击当前已选账号也能关闭列表，避免按下鼠标时穿透到登录表单。
                                    doListBoxCallback(CallbackReason.MOUSE_DOUBLE_CLICK);
                                    return true;
                                }
                                return super.handleListBoxEvent(event);
                            }
                        };
                    }
                };
        list.setTheme("listbox");
        list.setSelected(names.indexOf(getText()));
        list.addCallback(
                reason -> {
                    if (!reason.actionRequested()) return;
                    int index = list.getSelected();
                    if (index < 0) return;
                    popup.closePopup();
                    onSelected.accept(index == names.size() ? null : names.get(index));
                });
        popup.removeAllChildren();
        popup.add(list);
        if (popup.openPopup()) {
            positionPopup();
            popup.validateLayout();
            list.setSelected(names.indexOf(getText()), true);
            list.requestKeyboardFocus();
        }
    }

    private void positionPopup() {
        int height =
                Math.min(
                        list.getMaxHeight(),
                        (names.size() + 1) * list.getCellHeight() + list.getBorderVertical());
        int availableBelow = getGUI().getHeight() - getBottom();
        int availableAbove = getY();
        boolean below = availableBelow >= height || availableBelow >= availableAbove;
        popup.setSize(getWidth(), Math.min(height, below ? availableBelow : availableAbove));
        popup.setPosition(
                Math.max(0, Math.min(getX(), getGUI().getWidth() - popup.getWidth())),
                below ? getBottom() : Math.max(0, getY() - popup.getHeight()));
    }
}
