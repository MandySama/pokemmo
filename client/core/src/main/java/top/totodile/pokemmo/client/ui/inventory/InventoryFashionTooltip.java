package top.totodile.pokemmo.client.ui.inventory;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import java.util.List;
import java.util.function.IntFunction;
import top.totodile.pokemmo.client.ui.character.CharacterPreview;
import top.totodile.pokemmo.client.ui.common.WordWrapTextFormatter;

/** ChatScrollablePanel: name, addon_flags metadata, release information and three avatar views. */
final class InventoryFashionTooltip extends DialogLayout implements AutoCloseable {
    record Line(int icon,String text) {}
    private final CharacterPreview preview;
    InventoryFashionTooltip(String name,List<Line> lines,IntFunction<Image> icons,CharacterPreview preview){
        this.preview=preview;setTheme("/inventory-fashion-tooltip");setAddDefaultGaps(false);
        var horizontal=createParallelGroup();var vertical=createSequentialGroup();
        var title=new Label(WordWrapTextFormatter.w70(name,45));horizontal.addWidget(title);vertical.addWidget(title).addGap(5);
        for(var line:lines){
            Label text=new Label(WordWrapTextFormatter.w70(line.text(),45));
            if(line.icon()<0){horizontal.addWidget(text);vertical.addWidget(text).addGap(5);continue;}
            Widget icon=new Widget(){final Image image=icons.apply(line.icon());
                {setTheme("");setMinSize(16,16);setMaxSize(16,16);}
                @Override protected void paintWidget(GUI gui){if(image!=null){int lineHeight=text.getFont()==null?16:text.getFont().getLineHeight();image.draw(getAnimationState(),getX(),text.getInnerY()+(lineHeight-16)/2+2,16,16);}}
            };
            horizontal.addGroup(createSequentialGroup(icon).addGap(5).addWidget(text));vertical.addGroup(createParallelGroup(icon,text)).addGap(5);
        }
        horizontal.addWidget(preview);vertical.addWidget(preview);setHorizontalGroup(horizontal);setVerticalGroup(vertical);
    }
    @Override protected boolean handleEvent(Event event){return false;}
    @Override public void close(){preview.close();}
}
