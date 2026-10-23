package top.totodile.pokemmo.client.ui.trainer;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import java.text.*;
import java.util.*;
import java.util.function.*;
import top.totodile.pokemmo.client.resources.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.character.*;
import top.totodile.pokemmo.client.ui.common.PlayerWindow;
import top.totodile.pokemmo.server.game.*;

/** TrainerCardWindow.K8 coordinates and fields, backed by the selected local character. */
public final class TrainerCardWindow extends PlayerWindow implements AutoCloseable {
    private final ClientStrings strings;
    private final GdxTwlRenderer renderer;
    private final Consumer<Consumer<PlayerData.Trainer>> load;
    private final Function<GameService.Appearance,CharacterAppearance> appearance;
    private final Label name=new Label(),date=new Label();
    private final Label[] values=new Label[5];
    private final Widget[] icons=new Widget[5];
    private final Widget body=new Widget(){@Override protected void layout(){layoutCard();}};
    private final TextureAtlas atlas;
    private CharacterPreview portrait;
    private CharacterAppearance saved;
    public TrainerCardWindow(Widget owner,ClientResources resources,GdxTwlRenderer renderer,boolean mobile,
                             Function<GameService.Appearance,CharacterAppearance> appearance,IntFunction<Image> itemIcons,
                             Consumer<Consumer<PlayerData.Trainer>> load,Runnable onClose){
        super(owner,mobile,"/trainercard",454,317,onClose);this.strings=resources.strings();this.renderer=renderer;this.appearance=appearance;this.load=load;
        atlas=new TextureAtlas(resources.apply("sprites/atlas/main.atlas"));body.setTheme("/player-trainer-card");frame.setTitle("");frame.add(body);
        name.setTheme("/player-trainer-name");date.setTheme("/player-trainer-date");body.add(name);body.add(date);
        String[] regions={null,"CoinSprite","BPSprite","ClockSprite",null};
        for(int i=0;i<5;i++){
            final int index=i;values[i]=new Label();values[i].setTheme("/player-trainer-text");body.add(values[i]);
            icons[i]=new Widget(){@Override protected void paintWidget(GUI gui){
                Image image;if(index==0)image=itemIcons.apply(5431);else if(index==4)image=itemIcons.apply(5216);else image=renderer.image(atlas.findRegion(regions[index]));
                if(image!=null)image.draw(getAnimationState(),getX()+4,getY()+4,24,24);
            }};icons[i].setTheme("");body.add(icons[i]);
        }
    }
    @Override protected void refresh(){
        name.setText("");date.setText("");for(var value:values)value.setText("");if(portrait!=null)portrait.setVisible(false);
        load.accept(this::setData);
    }
    // Reference K8 fixes the card to 454x317 on both platforms.
    @Override protected boolean fullScreen(){return false;}
    private void setData(PlayerData.Trainer data){
        if(!isOpen())return;var character=data.character();var number=NumberFormat.getInstance();name.setText(character.name());
        if(mobile)frame.setTitle(character.name());
        date.setText(strings.get(1606)+" "+new SimpleDateFormat("yyyy-MM-dd").format(new Date(character.createdAt())));
        values[0].setText(strings.get(1)+": "+data.caught());values[1].setText(strings.get(1602)+" $"+number.format(data.money()));
        values[2].setText(strings.get(1605)+" "+number.format(data.battlePoints()));values[3].setText(strings.format(1603,number.format(data.playedSeconds()/3600)));
        values[4].setText(strings.format(1607,Integer.toString(data.obedience())));
        if(portrait!=null){body.removeChild(portrait);portrait.close();saved.dispose();}
        saved=appearance.apply(character.appearance());portrait=new CharacterPreview(saved,renderer,false,()->character.appearance().skin(),()->character.appearance().gender(),()->0L);portrait.portrait(2);body.add(portrait);body.invalidateLayout();
    }
    private void layoutCard(){
        // Coordinates are relative to the frame, as in TrainerCardWindow.K8.
        int x=frame.getX(),y=frame.getY();
        name.setPosition(x+14,y+45);name.setSize(290,24);date.setPosition(x+87,y+87);date.setSize(350,18);
        for(int i=0;i<5;i++){icons[i].setPosition(x+170,y+99+i*28);icons[i].setSize(32,32);values[i].setPosition(x+196,y+111+i*28);values[i].setSize(250,values[i].getPreferredHeight());}
        if(portrait!=null){portrait.setPosition(x+5,y+80);portrait.setSize(160,176);}
    }
    @Override public void close(){closePopup();if(portrait!=null){portrait.close();saved.dispose();}atlas.dispose();}
}
