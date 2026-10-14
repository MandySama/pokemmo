package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.RomFiles;
import top.totodile.pokemmo.rom.PokedexData;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;

/** 图鉴详情与列表共用窗口；资料和精灵解码异步进行，迟到结果不覆盖新条目。 */
final class PokedexDetailPanel extends Widget implements AutoCloseable {
    private final Button back = new Button(ClientStrings.get(58));
    private final Button previous = new Button("<"), next = new Button(">");
    private final Label title = new Label();
    private final Label category = new Label();
    private final Button flip = new Button(ClientStrings.get(9362));
    private final Button cry = new Button(ClientStrings.get(1727));
    private final Button[] tabs = new Button[5];
    private final Content content = new Content();
    private final ScrollPane scroll = new ScrollPane(content);
    private final Widget picture = new Widget() {
        @Override protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            Image image = images[backView ? 1 : 0];
            if (image != null) image.draw(getAnimationState(),getX()+(getWidth()-192)/2,getY()+(getHeight()-192)/2,192,192);
        }
    };
    private final IntConsumer select;
    private CompletableFuture<PokedexData> dataResult;
    private PokedexData data;
    private CompletableFuture<BufferedImage[]> spriteResult;
    private final Image[] images = new Image[2];
    private Texture texture;
    private int species, maximum = 649, tab;
    private boolean known, backView, failed;

    PokedexDetailPanel(Runnable returnToList, IntConsumer select) {
        this.select = select;
        setTheme("/pokedex-detail");
        back.addCallback(returnToList);
        previous.addCallback(() -> select.accept(species - 1));
        next.addCallback(() -> select.accept(species + 1));
        flip.addCallback(() -> { backView = !backView; flip.setText(ClientStrings.get(backView ? 9361 : 9362)); });
        add(back); add(previous); add(next); add(picture); add(title); add(category); add(flip); add(cry);
        cry.setEnabled(false);
        picture.setTheme("picture"); picture.setClip(true);
        int[] labels = {1703,1704,1717,1775,2550};
        for (int i=0;i<tabs.length;i++) {
            int page=i;
            tabs[i]=new Button(ClientStrings.get(labels[i]));
            tabs[i].addCallback(() -> { tab=page; refresh(); });
            if (i==3) { tabs[i].setEnabled(false); tabs[i].setTooltipContent(ClientStrings.get(9360)); }
            add(tabs[i]);
        }
        scroll.setTheme("entries"); scroll.setFixed(ScrollPane.Fixed.HORIZONTAL); add(scroll);
        setVisible(false);
    }

    void showSpecies(int id, boolean known, int maximum) {
        releaseSprite();
        this.species=id; this.known=known; this.maximum=maximum;
        tab=0; backView=false; failed=false;
        flip.setText(ClientStrings.get(9362));
        title.setText(String.format(Locale.ROOT,"No.%03d  %s",id,known?text(150000+id):text(9302)));
        previous.setEnabled(id>1); next.setEnabled(id<maximum); flip.setEnabled(known);
        setVisible(true);
        if (known && data==null && dataResult==null) dataResult=CompletableFuture.supplyAsync(() -> {
            try {
                var rom=new RomFiles(ClientFiles.data("../roms")).selected(0);
                if(rom==null)throw new java.io.IOException("Missing BW ROM");
                return new PokedexData(rom);
            } catch(java.io.IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
        refresh();
        requestSprite();
    }

    private void requestSprite() {
        if (!known || data==null || spriteResult!=null || texture!=null) return;
        int id=species; PokedexData source=data;
        spriteResult=CompletableFuture.supplyAsync(() -> {
            try { return new BufferedImage[]{source.sprite(id,false),source.sprite(id,true)}; }
            catch(java.io.IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    private static String text(int id) { return ClientStrings.get(id); }
    private static String optional(int id) { try { return text(id); } catch(IllegalArgumentException error) { return text(9365); } }
    private static String format(int id,Object value) { return text(id).replace("{00}",String.valueOf(value)); }
    private static String type(int id) { return optional(230000+(id>=9?id+1:id)); }
    private static String item(int id) { return optional(245000+id); }

    private void refresh() {
        content.clear(); category.setText(known?optional(351000+species):text(9302));
        for(int i=0;i<5;i++)tabs[i].getModel().setSelected(i==tab);
        if(!known)content.line("",text(9359));
        else if(failed)content.line("",text(9364));
        else if(data==null)content.line("",text(9363));
        else {
            PokedexData.Species record=data.species(species);
            if(tab==0) {
                content.line(text(1706),optional(351000+species)+"  "+type(record.type1())+(record.type1()==record.type2()?"":" / "+type(record.type2())));
                content.line(text(1707),optional(350000+species).replace("\\n",""));
                content.line(text(1709),String.format(Locale.ROOT,"%.1f %s",record.height()/10.0,text(9382)));
                content.line(text(1710),String.format(Locale.ROOT,"%.1f %s",record.weight()/10.0,text(9383)));
                content.line(text(1733),optional(210000+record.ability1())+(record.ability2()==0||record.ability2()==record.ability1()?"":" / "+optional(210000+record.ability2())));
                content.line(text(1886),record.hiddenAbility()==0?text(9365):optional(210000+record.hiddenAbility()));
                content.line(text(1735),record.heldItems().stream().filter(id->id>0).distinct().map(PokedexDetailPanel::item).reduce((a,b)->a+" / "+b).orElse(text(9365)));
                content.line(text(1730),optional(181000+record.egg1())+(record.egg1()==record.egg2()?"":" / "+optional(181000+record.egg2())));
                int[] effortLabels={1720,1721,1722,1724,1725,1723};
                List<String> effort=new ArrayList<>();
                for(int i=0;i<6;i++)if(record.effort().get(i)>0)effort.add(text(effortLabels[i])+" +"+record.effort().get(i));
                content.line(text(1708),String.join(" / ",effort));
                content.line(text(1734),"--");
            } else if(tab==1) {
                content.line("",text(9397));
                content.columns(new String[]{text(1711),text(9354),text(1706),text(9358),text(9355),text(9356),text(9357)});
                for(var move:record.moves())content.columns(new String[]{Integer.toString(move.level()),optional(110000+move.id()),type(move.type()),text(move.category()==0?9377:move.category()==1?9375:9376),move.power()==0?"--":Integer.toString(move.power()),move.accuracy()>100?"--":Integer.toString(move.accuracy()),Integer.toString(move.pp())});
            } else if(tab==2) {
                int[] labels={1720,1721,1722,1724,1725,1723,1726};
                int total=record.stats().stream().mapToInt(Integer::intValue).sum();
                for(int i=0;i<7;i++)content.stat(text(labels[i]),i<6?record.stats().get(i):total,i<6?255:680,i);
            } else if(tab==4) {
                var family=data.family(species);
                if(family.isEmpty())content.line("",text(9378));
                for(var evolution:family) {
                    content.link(optional(150000+evolution.from())+" → "+optional(150000+evolution.target()),()->select.accept(evolution.target()));
                    content.line("",condition(evolution));
                }
            }
        }
        scroll.setScrollPositionY(0); content.invalidateLayout(); invalidateLayout();
    }

    private static String condition(PokedexData.Evolution e) {
        return switch(e.method()) {
            case 1 -> text(9367); case 2 -> text(9368); case 3 -> text(9369);
            case 4 -> format(9366,e.parameter()); case 5 -> text(9370); case 6 -> format(9371,item(e.parameter()));
            case 7 -> text(9372); case 8 -> format(9373,item(e.parameter()));
            case 9 -> format(9384,e.parameter()); case 10 -> format(9385,e.parameter()); case 11 -> format(9386,e.parameter());
            case 17 -> format(9393,item(e.parameter())); case 18 -> format(9394,item(e.parameter()));
            case 19 -> format(9391,item(e.parameter())); case 20 -> format(9392,item(e.parameter()));
            case 21 -> format(9387,optional(110000+e.parameter())); case 22 -> format(9388,optional(150000+e.parameter()));
            case 23 -> format(9389,e.parameter()); case 24 -> format(9390,e.parameter());
            case 25,26,27 -> text(9395); default -> text(9374);
        };
    }

    @Override protected void paintWidget(GUI gui) {
        if(dataResult!=null&&dataResult.isDone()) {
            var result=dataResult; dataResult=null;
            try { data=result.join(); } catch(java.util.concurrent.CompletionException error) { failed=true; }
            refresh(); requestSprite();
        }
        if(spriteResult!=null&&spriteResult.isDone()) {
            var result=spriteResult; spriteResult=null;
            try {
                var frames=result.join(); BufferedImage pixels=new BufferedImage(192,96,BufferedImage.TYPE_INT_ARGB);
                var g=pixels.createGraphics();g.drawImage(frames[0],0,0,null);g.drawImage(frames[1],96,0,null);g.dispose();
                texture=((GlfwTwlRenderer)gui.getRenderer()).uploadImage(pixels);
                images[0]=texture.getImage(0,0,96,96,Color.WHITE,false,Texture.Rotation.NONE);
                images[1]=texture.getImage(96,0,96,96,Color.WHITE,false,Texture.Rotation.NONE);
            } catch(java.util.concurrent.CompletionException error) { failed=true; refresh(); }
        }
        super.paintWidget(gui);
    }

    @Override protected void layout() {
        int x=getInnerX(),y=getInnerY(),w=getInnerWidth(),h=getInnerHeight();
        for(int i=0;i<5;i++){tabs[i].setPosition(x+i*100,y);tabs[i].setSize(100,28);}
        boolean info=tab==0;
        picture.setVisible(info); title.setVisible(info); category.setVisible(false); flip.setVisible(false); cry.setVisible(info);
        previous.setVisible(false);next.setVisible(false);
        picture.setPosition(x+5,y+38);picture.setSize(170,185);
        title.setPosition(x+10,y+42);title.setSize(160,26);
        cry.setPosition(x+5,y+231);cry.setSize(170,28);
        int right=info?x+185:x+(w-450)/2;
        scroll.setPosition(right,y+38);scroll.setSize(info?w-190:450,h-80);
        back.setPosition(x+w-85,y+h-32);back.setSize(80,28);

    }

    int species() { return species; }

    void hideDetails() { setVisible(false); releaseSprite(); }
    private void releaseSprite() {
        if(spriteResult!=null)spriteResult.cancel(true);spriteResult=null;
        if(texture!=null)texture.destroy();texture=null;java.util.Arrays.fill(images,null);
    }
    @Override public void close() {
        hideDetails();if(dataResult!=null)dataResult.cancel(true);dataResult=null;data=null;
    }

    private static String wrap(String text) {
        StringBuilder result=new StringBuilder();int column=0;
        for(char c:text.toCharArray()){if(c=='\n'){column=0;result.append(c);continue;}if(column>=22){result.append('\n');column=0;}result.append(c);column++;}
        return result.toString();
    }
    private final class Content extends Widget {
        private final List<Widget[]> rows=new ArrayList<>();
        private final List<Integer> heights=new ArrayList<>();
        Content(){setTheme("content");}
        void clear(){removeAllChildren();rows.clear();heights.clear();}
        void line(String key,String value){String wrapped=wrap(value);Label label=new Label(key),body=new Label(wrapped);label.setTheme("field-title");body.setTheme("field-value");add(label);add(body);rows.add(new Widget[]{label,body});heights.add(Math.max(26,(int)wrapped.chars().filter(c->c=='\n').count()*18+26));}
        void columns(String[] values){Widget[] widgets=new Widget[values.length];for(int i=0;i<values.length;i++){widgets[i]=new Label(values[i]);add(widgets[i]);}rows.add(widgets);heights.add(26);}
        void link(String text,Runnable action){Button button=new Button(text);button.addCallback(action);add(button);rows.add(new Widget[]{button});heights.add(30);}
        void stat(String key,int value,int max,int color){Label label=new Label(key),number=new Label(Integer.toString(value));ProgressBar bar=new ProgressBar();bar.setTheme("stat-"+color);bar.setValue(value/(float)max);add(label);add(number);add(bar);rows.add(new Widget[]{label,number,bar});heights.add(36);}
        @Override public int getPreferredInnerWidth(){return 425;}
        @Override public int getPreferredInnerHeight(){return heights.stream().mapToInt(v->v+5).sum();}
        @Override protected void layout(){int y=getInnerY();for(int r=0;r<rows.size();r++){Widget[] row=rows.get(r);int height=heights.get(r),x=getInnerX(),width=getInnerWidth();
            if(row.length==3&&row[2] instanceof ProgressBar){row[0].setPosition(x,y);row[0].setSize(82,height);row[1].setPosition(x+82,y);row[1].setSize(35,height);row[2].setPosition(x+120,y+12);row[2].setSize(Math.min(250,Math.max(0,width-126)),8);}
            else if(row.length==2){row[0].setPosition(x,y);row[0].setSize(90,height);row[1].setPosition(x+95,y);row[1].setSize(Math.max(0,width-95),height);}
            else {int[] columns=row.length==7?new int[]{40,110,55,50,48,48,35}:new int[]{width};for(int i=0;i<row.length;i++){row[i].setPosition(x,y);row[i].setSize(columns[i],height);x+=columns[i];}}
            y+=height+5;
        }}
    }
}