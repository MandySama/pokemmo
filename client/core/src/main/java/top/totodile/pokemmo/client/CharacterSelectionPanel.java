package top.totodile.pokemmo.client;

import de.matthiasmann.twl.*;

/** 无角色时沿用参考的纵向按钮布局，角色创建界面另行实现。 */
final class CharacterSelectionPanel extends Widget {
    private final ResizableFrame frame = new ResizableFrame();
    private final Button newCharacter;
    private final Button logout;

    CharacterSelectionPanel(ClientStrings strings, LoginController controller) {
        setTheme("/characterselectgui");
        frame.setTheme("login-panel");
        frame.setTitle(strings.get(1049));
        frame.setDraggable(false);
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        DialogLayout content = new DialogLayout();
        content.setTheme("container");
        newCharacter = new Button(strings.get(1050));
        logout = new Button(strings.get(1159));
        logout.addCallback(controller::back);
        content.setHorizontalGroup(content.createParallelGroup()
                .addGroup(content.createSequentialGroup())
                .addGroup(content.createParallelGroup(newCharacter, logout)));
        content.setVerticalGroup(content.createSequentialGroup()
                .addGroup(content.createParallelGroup())
                .addGroup(content.createSequentialGroup(newCharacter, logout)));
        frame.add(content);
        add(frame);
        setVisible(false);
    }

    void focusDefault() { newCharacter.requestKeyboardFocus(); }

    @Override protected void layout() {
        frame.adjustSize();
        frame.setPosition(getInnerX() + (getInnerWidth() - frame.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - frame.getHeight()) / 2);
    }
}
