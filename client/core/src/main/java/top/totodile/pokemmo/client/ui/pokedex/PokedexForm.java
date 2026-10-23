package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;
import java.util.*;

/** The dex's paired labels use shared DialogLayout columns and content-sized rows. */
final class PokedexForm extends DialogLayout {
    private final List<Widget[]> rows=new ArrayList<>();
    private final Group titles=createParallelGroup(),values=createParallelGroup(),vertical=createSequentialGroup();

    PokedexForm(){
        setTheme("/pokedex-information-rows");setAddDefaultGaps(false);
        setHorizontalGroup(createSequentialGroup().addGroup(titles).addGap(DEFAULT_GAP).addGroup(values));
        setVerticalGroup(vertical);
    }
    void row(Widget title,Widget value){
        if(!rows.isEmpty())vertical.addGap(DEFAULT_GAP);
        titles.addWidget(title);values.addWidget(value);
        vertical.addGroup(createParallelGroup(title,value));rows.add(new Widget[]{title,value});
    }
    void values(String title,String value){row(title(title),value(value));}
    static Label title(String text){var label=new Label(text);label.setTheme("/pokedex-information-title");return label;}
    static PokedexDetails.WrappedLabel value(String text){
        var label=new PokedexDetails.WrappedLabel(text.isBlank()?"—":text){
            private int width=315;
            @Override protected void applyTheme(ThemeInfo theme){super.applyTheme(theme);width=theme.getParameter("minWidth",315);}
            @Override public int getPreferredInnerWidth(){return Math.max(0,width-getBorderHorizontal());}
        };
        label.setTheme("/pokedex-information-value");return label;
    }
    @Override protected void layout(){
        super.layout();
        // Wrapping changes the preferred row height; DialogLayout then gives both labels that height.
        for(var row:rows)if(row[1] instanceof PokedexDetails.WrappedLabel label)label.wrap(Math.max(1,label.getInnerWidth()));
    }
}
