package top.totodile.pokemmo.client.ui.common;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import java.util.Locale;
import java.util.function.Consumer;

/** 参考 qj0_2：验证/复制时的模态进度弹窗，不提供取消或确认按钮。 */
public final class ImportProgressDialog extends PopupWindow {
    private final DialogLayout panel=new DialogLayout();
    private final Label message=new Label();
    private final Progress progress=new Progress();
    private final boolean mobile;
    private final Consumer<Image> screenBackground;

    public ImportProgressDialog(Widget owner, boolean mobile, Consumer<Image> screenBackground) {
        super(owner);
        this.mobile=mobile;
        this.screenBackground=screenBackground;
        setTheme("/confirm-widget");
        setCloseOnClickedOutside(false);
        setCloseOnEscape(false);
        panel.setTheme("/confirm-panel");
        progress.setTheme("progressbar");
        panel.setHorizontalGroup(panel.createParallelGroup(message,progress));
        panel.setVerticalGroup(panel.createSequentialGroup(message,progress));
        add(panel);
    }
    public void show(String text) {
        message.setText(text);
        progress.value=0;
        progress.setText("0.00%");
        openPopup();
        positionWindow();
    }
    public void update(String text, double value) {
        if (!message.getText().equals(text)) message.setText(text);
        float next=value<0 ? progress.value+(1-progress.value)*0.01f : (float)value;
        progress.value=Math.max(0,Math.min(0.9999f,next));
        progress.setText(String.format(Locale.ROOT,"%.2f%%",progress.value*100));
        positionWindow();
    }
    private void positionWindow() {
        if (!isOpen()) return;
        Widget owner=getOwner();
        setPosition(owner.getInnerX(),owner.getInnerY());
        setSize(owner.getInnerWidth(),owner.getInnerHeight());
    }
    @Override protected void layout() {
        panel.adjustSize();
        panel.setPosition(getInnerX()+(getInnerWidth()-panel.getWidth())/2,getInnerY()+(getInnerHeight()-panel.getHeight())/2);
    }
    @Override protected void paintBackground(GUI gui) {
        if (mobile) screenBackground.accept(getBackground()); else super.paintBackground(gui);
    }
    private static final class Progress extends Label {
        private Image fill;
        private float value;
        @Override protected void applyTheme(ThemeInfo info) { super.applyTheme(info); fill=info.getImage("progressImage"); }
        @Override protected void paintWidget(GUI gui) {
            if (fill!=null) {
                int minimum=fill.getWidth(), extra=Math.max(0,getInnerWidth()-minimum);
                fill.draw(getAnimationState(),getInnerX(),getInnerY(),minimum+(int)(extra*value),getInnerHeight());
            }
            super.paintWidget(gui);
        }
    }
}
