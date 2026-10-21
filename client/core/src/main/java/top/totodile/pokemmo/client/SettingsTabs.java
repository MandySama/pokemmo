package top.totodile.pokemmo.client;

import de.matthiasmann.twl.*;
import java.util.ArrayList;
import java.util.List;

/** 对应参考设置页 tabbedpane/tabbox/container 的布局与分类切换。 */
public final class SettingsTabs extends Widget {
    private final BoxLayout tabbox = new BoxLayout(BoxLayout.Direction.VERTICAL);
    private final Container container = new Container() {
        @Override public int getPreferredInnerHeight() { return pages.isEmpty() ? 0 : selectedPage().getMinHeight(); }
    };
    private final List<ToggleButton> buttons = new ArrayList<>();
    private final List<Widget> pages = new ArrayList<>();
    private TabPosition position = TabPosition.LEFT;
    private int selected;

    SettingsTabs() {
        setTheme("tabbedpane");
        tabbox.setTheme("tabbox");
        container.setTheme("container");
        add(container);
        add(tabbox);
        addActionMapping("nextTab", "nextTab");
        addActionMapping("prevTab", "prevTab");
    }

    public void nextTab() { if (!pages.isEmpty()) select((selected + 1) % pages.size()); }
    public void prevTab() { if (!pages.isEmpty()) select((selected + pages.size() - 1) % pages.size()); }

    void addTab(String title, Widget page) {
        int index = pages.size();
        var button = new ToggleButton(title);
        button.setTheme("tabbutton");
        button.addCallback(() -> select(index));
        buttons.add(button);
        pages.add(page);
        tabbox.add(button);
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).getAnimationState().setAnimationState(AnimationState.StateKey.get("firstTab"), i == 0);
            buttons.get(i).getAnimationState().setAnimationState(AnimationState.StateKey.get("lastTab"), i == buttons.size() - 1);
        }
        if (pages.size() == 1) select(0);
    }

    void select(int index) {
        selected = index;
        for (int i = 0; i < buttons.size(); i++) buttons.get(i).setActive(i == index);
        container.removeAllChildren();
        container.add(pages.get(index));
        invalidateLayout();
    }

    int selected() { return selected; }
    int count() { return pages.size(); }
    ScrollPane selectedPage() { return (ScrollPane) pages.get(selected); }

    @Override protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        position = theme.getParameter("tabPosition", TabPosition.LEFT);
        tabbox.setDirection(position.horizontal ? BoxLayout.Direction.HORIZONTAL : BoxLayout.Direction.VERTICAL);
    }
    @Override public int getPreferredInnerWidth() {
        return position.horizontal ? Math.max(tabbox.getPreferredWidth(), container.getPreferredWidth())
                : tabbox.getPreferredWidth() + container.getPreferredWidth();
    }
    @Override public int getPreferredInnerHeight() {
        return position.horizontal ? tabbox.getPreferredHeight() + container.getPreferredHeight()
                : Math.max(tabbox.getPreferredHeight(), container.getPreferredHeight());
    }
    @Override protected void layout() {
        int tabWidth = Math.min(getInnerWidth(), tabbox.getPreferredWidth());
        int tabHeight = Math.min(getInnerHeight(), tabbox.getPreferredHeight());
        if (position.horizontal) {
            tabbox.setPosition(getInnerX(), position == TabPosition.BOTTOM ? getInnerBottom() - tabHeight : getInnerY());
            tabbox.setSize(getInnerWidth(), tabHeight);
            container.setPosition(getInnerX(), position == TabPosition.BOTTOM ? getInnerY() : getInnerY() + tabHeight);
            container.setSize(getInnerWidth(), Math.max(0, getInnerHeight() - tabHeight));
        } else {
            tabbox.setPosition(position == TabPosition.RIGHT ? getInnerRight() - tabWidth : getInnerX(), getInnerY());
            tabbox.setSize(tabWidth, getInnerHeight());
            container.setPosition(position == TabPosition.RIGHT ? getInnerX() : getInnerX() + tabWidth, getInnerY());
            container.setSize(Math.max(0, getInnerWidth() - tabWidth), getInnerHeight());
        }
    }
}
