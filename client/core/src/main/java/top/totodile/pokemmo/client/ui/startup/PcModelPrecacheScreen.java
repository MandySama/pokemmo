package top.totodile.pokemmo.client.ui.startup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.theme.ThemeManager;
import java.util.Locale;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

/** SpectatorModePanelComponent(-1)：黑底、Logo、真实模型预缓存进度及 JVM 内存。 */
public final class PcModelPrecacheScreen implements AutoCloseable {
    private final ScreenViewport viewport=new ScreenViewport();
    private final GdxTwlRenderer renderer;
    private final ThemeManager theme;
    private final GUI gui;
    private final Widget root;
    private final Widget logo=new Widget();
    private final Label message=new Label(),fps=new Label("000.00");
    private final Progress progress=new Progress();
    private final DialogLayout panel=new DialogLayout();
    private final String text;
    public PcModelPrecacheScreen(ClientResources resources) {
        var strings=resources.strings();text=strings.get(925);
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        renderer=new GdxTwlRenderer(viewport,strings.typeface());
        root=new Widget() {
            @Override protected void layout() {
                logo.setSize(484,143);
                logo.setPosition(getInnerX()+(getInnerWidth()-484)/2,getInnerY()+(getInnerHeight()-143)/2-200);
                panel.adjustSize();panel.setPosition(getInnerX()+(getInnerWidth()-panel.getWidth())/2,
                        getInnerY()+(getInnerHeight()-panel.getHeight())/2);
                fps.adjustSize();fps.setPosition(getInnerRight()-fps.getWidth(),getInnerY());
            }
        };
        root.setTheme("/logingui");logo.setTheme("logo");panel.setTheme("login-panel");
        progress.setTheme("progressbar");progress.setMinSize(192,20);
        message.setText(text);fps.setTheme("/fpscounter");
        panel.setHorizontalGroup(panel.createParallelGroup(message,progress));
        panel.setVerticalGroup(panel.createSequentialGroup(message).addGap(20).addWidget(progress));
        root.add(logo);root.add(panel);root.add(fps);
        try {
            var selected=resources.selectedTheme();
            theme=ThemeManager.createThemeManager(resources.themeUrl(selected),renderer,renderer.createNewCacheContext(),
                    new java.util.HashMap<String,Object>(resources.imageConstants(selected,strings.language())));
        }catch(Exception error){renderer.close();throw new IllegalStateException("Cannot load model precache theme",error);}
        gui=new GUI(root,renderer,null);gui.applyTheme(theme);
        warmFonts();
    }
    private void warmFonts() {
        // 内存值和百分比每帧变化；提前生成全部数字，避免批量绘制中增补字体纹理。
        if(message.getFont()!=null)message.getFont().computeTextWidth(text+"\nMemory Info: 0123456789 / MB");
        if(progress.getFont()!=null)progress.getFont().computeTextWidth("0123456789.%");
        if(fps.getFont()!=null)fps.getFont().computeTextWidth("0123456789.");
    }
    public void draw(double fraction) {
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);viewport.apply();
        if(renderer.updateFontScale()){invalidateFonts(root);warmFonts();}
        Runtime runtime=Runtime.getRuntime();
        message.setText(text+"\nMemory Info: "+(runtime.totalMemory()-runtime.freeMemory())/1048576L+" / "+runtime.maxMemory()/1048576L+" MB");
        progress.value=(float)Math.max(0,Math.min(1,fraction));
        progress.setText(String.format(Locale.ROOT,"%.2f%%",progress.value*100));
        gui.update();
    }
    private static void invalidateFonts(Widget widget) {
        if(widget instanceof TextWidget text)text.setFont(text.getFont());
        for(int i=0;i<widget.getNumChildren();i++)invalidateFonts(widget.getChild(i));
        widget.invalidateLayout();
    }
    @Override public void close(){gui.destroy();theme.destroy();renderer.close();}
    private static final class Progress extends Label {
        private Image fill;private float value;
        @Override protected void applyTheme(ThemeInfo theme){super.applyTheme(theme);fill=theme.getImage("progressImage");}
        @Override protected void paintWidget(GUI gui){
            if(fill!=null)fill.draw(getAnimationState(),getInnerX(),getInnerY(),fill.getWidth()+(int)(Math.max(0,getInnerWidth()-fill.getWidth())*value),getInnerHeight());
            super.paintWidget(gui);
        }
    }
}
