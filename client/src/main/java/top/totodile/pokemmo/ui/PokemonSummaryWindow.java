package top.totodile.pokemmo.ui;

import top.totodile.pokemmo.protocol.PokemonConfigPackets;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.*;
import top.totodile.pokemmo.protocol.PartyMember;
import top.totodile.pokemmo.protocol.PokemonCatalog;
import top.totodile.pokemmo.rom.PokedexData;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** 队伍个体资料独立于图鉴；缺失的个体属性不使用种族基础值代替。 */
final class PokemonSummaryWindow extends ResizableFrame implements AutoCloseable {
    private final Button[] tabs = new Button[9];
    private final Label name = new Label(), level = new Label(), health = new Label();
    private final ProgressBar hp = new ProgressBar();
    private final ProgressBar friendship = new ProgressBar();
    private final List<Widget> pageWidgets = new ArrayList<>();
    private final List<Texture> effectTextures = new ArrayList<>();
    private final List<Image> effects = new ArrayList<>();
    private Image[] animation = new Image[0];
    private Image[] backAnimation = new Image[0];
    /** 图集格尺寸与原 96 画布中心在格内的锚点，动画帧按锚点摆回原脚底位置。 */
    private int cellWidth = 96, cellHeight = 96, anchorX = 48, anchorY = 48;
    /** 点击图像区在正／背面之间切换，翻转动画用时 360ms。 */
    private boolean showingBack;
    private long flipStart = -1;
    private static final long FLIP_NANOS = 360_000_000L;
    private Runnable closeAction = () -> { };
    private SummaryParticles particleEffect;
    private Image[] ribbonImages = new Image[0];
    private long animationStart;
    private final Widget shiny = new Widget();
    private final Widget heldItem = new Widget();
    private final Widget[] moveTypes = {new PokemonTypeBadge(),new PokemonTypeBadge(),new PokemonTypeBadge(),new PokemonTypeBadge()};
    private final Button[] moveUp = new Button[4],moveDown = new Button[4];
    private final Button ivHelp = new Button();
    private String historyText = "";
    private final Label item = new Label(ClientStrings.get(1842)+" --");
    private final Widget[] types = {new PokemonTypeBadge(),new PokemonTypeBadge()};
    private final Widget alpha = new Widget();
    private final Widget[] marks = {new Widget(),new Widget(),new Widget(),new Widget()};
    private Image dexImage, ballImage;
    private final Widget gender = new Widget();
    private final Widget ball = new Widget() {
        @Override protected void paintWidget(GUI gui) {if(ballImage!=null)ballImage.draw(getAnimationState(),getX(),getY(),16,16);}
    };
    private final Button dex = new Button() {
        @Override protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if(dexImage!=null)dexImage.draw(getAnimationState(),getX(),getY(),24,24);
        }
    };
    private final Button rename = new Button();
    private final Image[] backgrounds = new Image[18], nameplates = new Image[18];
    private java.util.function.IntConsumer openDex = id -> { };
    private final List<Label[]> rows = new ArrayList<>();
    private final Widget picture = new Widget() {
        @Override protected void paintWidget(GUI gui) {
            int type=species==null?0:species.type1();
            if(backgrounds[type]!=null)backgrounds[type].draw(getAnimationState(),getX(),getY(),getWidth(),getHeight());
            int size=member!=null&&member.alpha()?221:192;
            double seconds=(System.nanoTime()-animationStart)/1_000_000_000.0;
            if(particleEffect!=null)particleEffect.draw(this,System.nanoTime());
            // 翻转动画把宽度按 |cos| 收窄再展开，中点切换朝向，视觉上等同于转身。
            double flip=flipStart<0?1:Math.min(1,(System.nanoTime()-flipStart)/(double)FLIP_NANOS);
            if(flipStart>=0&&flip>=1)flipStart=-1;
            boolean back=showingBack;
            if(flipStart>=0&&flip<0.5)back=!back;
            Image[] frames=back?backAnimation:animation;
            Image current=frames.length==0?image:frames[(int)(seconds*30)%frames.length];
            if (current != null) {
                // 显示尺寸沿用 96 画布的约定（普通 2 倍、异色闪光个体更大），锚点是原画布中心
                // 在裁剪格内的位置，脚底仍落在旧实现的画布中心线上；收缩围绕身体锚点进行。
                double scale=size/96.0;
                double shrink=flipStart<0?1:Math.abs(Math.cos(Math.PI*flip));
                int width=(int)Math.round(cellWidth*scale*shrink);
                int height=(int)Math.round(cellHeight*scale);
                if(width>=1)current.draw(getAnimationState(),
                        (int)Math.round(getX()+getWidth()/2.0-anchorX*scale*shrink),
                        getY()+(getHeight()-size)/2+72+size/2-(int)Math.round(anchorY*scale),width,height);
            }
        }
    };
    /** 覆盖图像区的透明按钮，只负责把点击转成正／背面切换。 */
    private final Button pictureHit = new Button() {
        @Override
        public boolean handleEvent(Event event) {
            if (event.isMouseEvent() && event.getMouseButton() == Event.MOUSE_RBUTTON) {
                if (event.getType() == Event.Type.MOUSE_BTNUP) openConfigurationMenu(event.getMouseX(), event.getMouseY());
                return true;
            }
            return super.handleEvent(event);
        }
    };
    private MenuManager configurationMenu;
    private boolean configuring;
    private java.util.function.Consumer<PokemonConfigPackets.Request> configure;

    private record Loaded(PokedexData.Species species, List<BufferedImage> frames, List<BufferedImage> backFrames,
                          int cellWidth, int cellHeight, int anchorX, int anchorY,
                          BufferedImage dex, BufferedImage ball,List<SummaryParticles.Definition> effects,List<BufferedImage> ribbons) { }
    private PokedexData.Species species;
    private CompletableFuture<Loaded> pending;
    private Texture texture;
    private Image image;
    private PartyMember member;
    private int page;

    PokemonSummaryWindow() {
        setTheme("/pokemon-summary");
        setResizableAxis(ResizableAxis.NONE);
        addCloseCallback(() -> closeAction.run());
        add(picture); add(ball); add(gender); add(name); add(level); add(health); add(hp); add(item);
        friendship.setTheme("xp");add(friendship);shiny.setTheme("shiny");add(shiny);heldItem.setTheme("held-item");add(heldItem);
        for(Widget type:types)add(type);
        add(alpha);alpha.setTheme("alpha");
        for(int i=0;i<marks.length;i++){marks[i].setTheme("mark-"+i);add(marks[i]);}
        dex.setTheme("dex");rename.setTheme("rename");rename.setEnabled(false);
        dex.addCallback(() -> {if(member!=null)openDex.accept(member.species());});
        rename.setTooltipContent(ClientStrings.get(9415));
        dex.setTooltipContent(ClientStrings.get(1729)); add(dex);add(rename);
        item.setTheme("item");
        name.setTheme("name"); level.setTheme("name"); health.setTheme("name"); hp.setTheme("hp");
        int[] labels = {1801,9400,1800,9401,1704,9402,9403,9408,9409};
        for (int i=0;i<tabs.length;i++) {
            int selected=i;
            tabs[i]=new Button(); tabs[i].setTheme("tab-"+i);
            tabs[i].setTooltipContent(ClientStrings.get(labels[i]));
            tabs[i].addCallback(() -> {page=selected;refresh();}); add(tabs[i]);
        }
        for(int i=0;i<4;i++){
            moveTypes[i].setTheme("type-0");add(moveTypes[i]);
            moveUp[i]=new Button(ClientStrings.get(9687));moveDown[i]=new Button(ClientStrings.get(9688));
            moveUp[i].setTheme("move-arrow");moveDown[i].setTheme("move-arrow");add(moveUp[i]);add(moveDown[i]);
        }
        ivHelp.setText(ClientStrings.get(1836));ivHelp.setTheme("iv-help");ivHelp.setTooltipContent(ClientStrings.get(1837).replace("\\n","\n"));add(ivHelp);
        // 放在最后保证命中测试优先，图像区整块都能触发正／背面切换。
        pictureHit.setTheme("picture-hit");
        pictureHit.setTooltipContent(ClientStrings.get(9711));
        pictureHit.addCallback(() -> {if(member==null)return;requestKeyboardFocus();showingBack=!showingBack;flipStart=System.nanoTime();});
        add(pictureHit);
        setHardVisible(false);
    }

    void setConfigure(java.util.function.Consumer<PokemonConfigPackets.Request> action) {
        configure = action;
    }

    private void openConfigurationMenu(int x, int y) {
        if (configure == null || configuring || pendingCostume != null || member == null || member.individual() == null) return;
        requestKeyboardFocus();
        if (configurationMenu != null) configurationMenu.closePopup();
        configurationMenu = new PokemonConfigurationMenu(member, request -> {
            if (!configuring && configure != null) configure.accept(request);
        }).withCostumes(this::applyCostume).build().openPopupMenu(this, x, y);
    }

    private Runnable costumeChanged = () -> { };

    void setCostumeChanged(Runnable action) { costumeChanged = action; }

    private CompletableFuture<String> pendingCostume;

    private void applyCostume(String id) {
        if (member == null || pendingCostume != null) return;
        if (id.isEmpty()) { commitCostume(id); return; }
        configuring = true;
        item.setText(ClientStrings.get(9811));
        pendingCostume = CompletableFuture.supplyAsync(() -> {
            try { top.totodile.pokemmo.rom.PokemonCostumes.prepare(id); return id; }
            catch (java.io.IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    private void commitCostume(String id) {
        try {
            top.totodile.pokemmo.rom.PokemonCostumes.select(member, id);
            updateIndividual(member);
            costumeChanged.run();
        } catch (java.io.IOException | IllegalArgumentException error) {
            item.setText(ClientStrings.get(9795));
            System.getLogger(PokemonSummaryWindow.class.getName()).log(System.Logger.Level.WARNING, "Unable to save Pokemon costume", error);
        }
    }

    void setConfiguring(boolean value) { configuring = value; }

    void updateIndividual(PartyMember updated) {
        if (member == null || member.individual() == null || updated.individual() == null
                || member.individual().id() != updated.individual().id()) return;
        int selectedPage = page;
        boolean back = showingBack;
        open(updated);
        page = selectedPage;
        showingBack = back;
        refresh();
    }

    void setCloseAction(Runnable action) {closeAction=action;}

    void open(PartyMember member) {
        close(); configuring=false; this.member=member; page=0;
        top.totodile.pokemmo.rom.PokemonCostumes.preloadCatalog();
        showingBack=false; flipStart=-1;
        String title=ClientStrings.get(150000+member.species());
        setTitle(ClientStrings.get(0)+" "+ClientStrings.get(1801));
        animationStart=System.nanoTime();
        var individual=member.individual();
        shiny.setVisible(member.shiny());
        name.setText(individual==null||individual.nickname().isEmpty()?title:individual.nickname()); alpha.setVisible(member.alpha());
        item.setText(ClientStrings.get(1842)+" "+(individual==null?"--":individual.heldItem()==0?ClientStrings.get(9365):ClientStrings.get(top.totodile.pokemmo.protocol.ItemCatalog.get(individual.heldItem()).nameId())));
        gender.setVisible(individual!=null&&individual.gender()!=2);
        if(individual!=null){gender.setTheme(individual.gender()==0?"male":"female");gender.reapplyTheme();}
        level.setText(ClientStrings.get(9410)+" "+(member.level() == 0 ? "--" : member.level()));
        health.setText(member.hp()+" / "+member.maxHp());
        float ratio=member.hp()/(float)member.maxHp();
        hp.setTheme(ratio>0.5f?"hp":ratio>0.2f?"hp-orange":"hp-red");
        hp.setValue(ratio);
        pending=CompletableFuture.supplyAsync(() -> {
            try {
                var rom=new RomFiles(ClientFiles.data("../roms")).selected(0);
                if(rom==null)throw new java.io.IOException("Missing BW ROM");
                var data=new PokedexData(rom);
                var species=data.species(member.species());
                List<BufferedImage> frames,backFrames;int cellWidth=96,cellHeight=96,anchorX=48,anchorY=48;
                try {
                    // 大画布渲染后按内容裁剪；高个体（如盖诺赛克特）在固定 96 画布下头部会被截。
                    var summary=top.totodile.pokemmo.rom.PokemonForms.active(member)
                            && top.totodile.pokemmo.rom.PokemonCostumes.selected(member).isEmpty()
                            ? top.totodile.pokemmo.rom.PokemonForms.load(member)
                            : top.totodile.pokemmo.rom.PokemonCostumes.summary(
                                    top.totodile.pokemmo.rom.PokemonAnimation.loadSummary(rom,member.species(),member.shiny()), member);
                    frames=summary.front();backFrames=summary.back();
                    cellWidth=summary.cellWidth();cellHeight=summary.cellHeight();
                    anchorX=summary.anchorX();anchorY=summary.anchorY();
                }
                catch(java.io.IOException|RuntimeException error){
                    frames=List.of(data.sprite(member.species(),false,member.shiny()));backFrames=frames;
                }
                var particles=SummaryParticles.load(rom,species.type1()==17?9:species.type1()>=9?species.type1()+1:species.type1());
                return new Loaded(species,frames,backFrames,cellWidth,cellHeight,anchorX,anchorY,new top.totodile.pokemmo.rom.BlackWhiteItemIcons(rom).image(5431),individual==null?null:new top.totodile.pokemmo.rom.BlackWhiteItemIcons(rom).image(5000+individual.ball()),particles,top.totodile.pokemmo.rom.RibbonSprites.load(rom,individual==null?List.of():individual.ribbons()));
            } catch(java.io.IOException e) {throw new java.util.concurrent.CompletionException(e);}
        });
        refresh(); setHardVisible(true); requestKeyboardFocus();
    }

    void setOpenDex(java.util.function.IntConsumer action) {openDex=action;}

    @Override protected void applyTheme(ThemeInfo info) {
        super.applyTheme(info);
        for(int i=0;i<18;i++) {backgrounds[i]=info.getImage("typeBackground"+i);nameplates[i]=info.getImage("nameplate"+i);}
    }

    private static String type(int value) { return ClientStrings.get(230000+(value==17?9:value>=9?value+1:value)); }

    private void line(int title,String value) {
        Label key=new Label(ClientStrings.get(title)), body=new Label(value);
        key.setTheme("field-title");body.setTheme("field-value");
        add(key);add(body);rows.add(new Label[]{key,body});
    }

    private void refresh() {
        for(var row:rows)for(var label:row)removeChild(label);
        for(Widget widget:pageWidgets)removeChild(widget);pageWidgets.clear();
        rows.clear();
        for(int i=0;i<tabs.length;i++)tabs[i].getModel().setSelected(i==page);
        var d=member.individual();
        if(page==0) {
            line(9412,String.format(java.util.Locale.ROOT,"%03d",member.species()));
            line(9413,name.getText());
            line(9416,d==null?"--":ClientStrings.get(180000+d.nature()));
            line(9414,d==null?"--":d.originalTrainer());
            line(1810,d==null?"--":number(d.experience()));
            line(1829,d==null?"--":member.level()==100?"-":number(nextExperience()-d.experience()));
        } else if(page<=3) {
            int[] labels=page==2?new int[]{1817,1818,1819,1820,1821,1822}:page==3?new int[]{1830,1831,1832,1833,1834,1835}:new int[]{1809,1812,1813,1814,1815,1816};
            if(page==1){line(1809,member.hp()+" / "+member.maxHp());line(1811,d==null?"--":d.friendship()*100/255+"%");}
            for(int i=page==1?1:0;i<labels.length;i++)line(labels[i],d==null?"--":page==1?(i==0?member.hp()+" / "+member.maxHp():Integer.toString(d.stat(member.species(),member.level(),i))):Integer.toString((page==2?d.evs():d.ivs()).get(i)));
            if(page==1){}
            else line(1726,d==null?"--":Integer.toString((page==2?d.evs():d.ivs()).stream().mapToInt(Integer::intValue).sum())+" / "+(page==2?510:186));
        } else if(page==4) {
            for(int i=0;i<4;i++) {
                if(d==null||i>=d.moves().size()){line(9354,ClientStrings.get(9365));continue;}
                var move=d.moves().get(i);var rule=PokemonCatalog.move(member.species(),move.id());
                line(9354,ClientStrings.get(110000+move.id())+"\nPP "+move.pp()+" / "+move.maxPp());
                rows.getLast()[1].setTheme("move-card");rows.getLast()[1].reapplyTheme();
                rows.getLast()[1].setTooltipContent(ClientStrings.get(1706)+" "+type(rule.type())+"\n"+ClientStrings.get(9355)+" "+rule.power()+"\n"+ClientStrings.get(9356)+" "+rule.accuracy()+"%\n"+ClientStrings.get(9417)+" "+move.ppUps());
            }
            line(1733,d==null?"--":ClientStrings.get(210000+d.ability()));
            Label description=new Label(d==null?"--":ClientStrings.get(220000+d.ability()).replace("\\n","\n"));description.setTheme("description");add(description);pageWidgets.add(description);
        } else if(page==5) {
            String text="--";
            if(d!=null){text=ClientStrings.get(9681).replace("{00}",ClientStrings.get(PokemonCatalog.location(d.metRegion(),d.metGroup(),d.metMap())))
                    .replace("{01}",java.time.format.DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss").withZone(java.time.ZoneId.systemDefault()).format(java.time.Instant.ofEpochSecond(d.metAt())))
                    .replace("{02}",Integer.toString(d.metLevel())).replace("{03}",species!=null&&d.ability()==species.hiddenAbility()?ClientStrings.get(1885):"");}
            text=text.stripTrailing();
            if(d!=null){int plus=d.nature()/5,minus=d.nature()%5;if(plus==minus)text+="\n\n"+ClientStrings.get(1883);else text+="\n\n"+ClientStrings.get(1881).replace("{00}",ClientStrings.get(new int[]{9690,9691,9692,9693,9694}[plus])).replace("{01}",ClientStrings.get(new int[]{9690,9691,9692,9693,9694}[minus])).replace("\\n","\n");}
            historyText=text;line(9682,text);rows.getLast()[1].setTheme("history");rows.getLast()[1].reapplyTheme();
        } else if(page==6){
            for(int i=0;i<35;i++){final int index=i;Widget ribbon=new Widget(){@Override protected void paintWidget(GUI gui){if(index<ribbonImages.length)ribbonImages[index].draw(getAnimationState(),getX(),getY(),32,32);}};ribbon.setTheme("ribbon");add(ribbon);pageWidgets.add(ribbon);}
        } else if(page==7){int[] labels={9427,9428,9429,9430,9431,1726};for(int i=0;i<6;i++){
            line(labels[i],"");ProgressBar value=new ProgressBar();value.setTheme("xp");value.setValue(d==null?0:i==5?d.contest().subList(0,5).stream().mapToInt(Integer::intValue).sum()/1275f:Math.min(1,d.contest().get(i)/255f));add(value);pageWidgets.add(value);
        }} else {
            line(9684,"");rows.getLast()[1].setVisible(false);
            if(d!=null)for(int id:d.particles()){
                ToggleButton particle=new ToggleButton();particle.setTheme("particle-slot");
                particle.setTooltipContent(ClientStrings.get(9689).replace("{00}",Integer.toString(id)));
                particle.setActive(pageWidgets.isEmpty());particle.addCallback(()->{for(Widget widget:pageWidgets)if(widget instanceof ToggleButton button)button.setActive(button==particle);});
                add(particle);pageWidgets.add(particle);
            }
        }
        if(page==3&&d!=null)for(int i=0;i<6;i++)if(d.ivs().get(i)==31){rows.get(i)[1].setTheme("perfect-value");rows.get(i)[1].reapplyTheme();}
        ivHelp.setVisible(page==3);
        for(int i=0;i<4;i++){
            moveTypes[i].setVisible(page==4&&d!=null&&i<d.moves().size());
            moveUp[i].setVisible(page==4&&i>0&&d!=null&&i<d.moves().size());
            moveDown[i].setVisible(page==4&&i<3&&d!=null&&i<d.moves().size()-1);
            if(page==4&&d!=null&&i<d.moves().size()){var move=PokemonCatalog.move(member.species(),d.moves().get(i).id());moveTypes[i].setTheme("type-"+move.type());moveTypes[i].reapplyTheme();}
        }
        friendship.setVisible(page==1);friendship.setValue(d==null?0:d.friendship()/255f);
        dex.setVisible(page==0);rename.setVisible(page==0);
        moveChild(getChildIndex(dex),getNumChildren()-1);
        moveChild(getChildIndex(rename),getNumChildren()-1);
        health.setVisible(false);hp.setVisible(page==0||page==1);
        hp.setTheme(page==0?"xp":member.hp()/(float)member.maxHp()>0.5f?"hp":member.hp()/(float)member.maxHp()>0.2f?"hp-orange":"hp-red");
        hp.reapplyTheme();
        float experience=0;
        if(d!=null){long base=PokemonCatalog.experience(PokemonCatalog.get(member.species()).growth(),member.level());long next=nextExperience();experience=member.level()==100?1:(float)(d.experience()-base)/(next-base);}
        hp.setValue(page==1?member.hp()/(float)member.maxHp():Math.max(0,Math.min(1,experience)));
        for(int i=0;i<2;i++) {
            types[i].setVisible(species!=null&&(i==0||species.type1()!=species.type2()));
            if(species!=null){types[i].setTheme("type-"+(i==0?species.type1():species.type2()));types[i].reapplyTheme();}
        }
        invalidateLayout();
    }

    private static String number(long value){return java.text.NumberFormat.getIntegerInstance().format(value);}
    private long nextExperience(){return PokemonCatalog.experience(PokemonCatalog.get(member.species()).growth(),Math.min(100,member.level()+1));}

    @Override protected void layout() {
        super.layout(); int x=getX(),y=getY();
        name.setPosition(x+298,y+31);name.setSize(118,24);
        ball.setPosition(x+278,y+35);ball.setSize(16,16);
        gender.setPosition(x+427,y+35);gender.setSize(16,16);
        level.setPosition(x+444,y+31);level.setSize(55,24);
        picture.setPosition(x+275,y+31);picture.setSize(224,233);picture.setClip(true);
        pictureHit.setPosition(x+275,y+31);pictureHit.setSize(224,233);
        item.setPosition(x+282,y+274);item.setSize(171,26);
        health.setPosition(x+150,y+40);health.setSize(110,22);
        hp.setPosition(x+15,y+(page==1?76:226));hp.setSize(238,8);
        friendship.setPosition(x+15,y+116);friendship.setSize(238,6);
        shiny.setPosition(x+479,y+61);shiny.setSize(16,16);heldItem.setPosition(x+461,y+272);heldItem.setSize(35,35);
        for(int i=0;i<tabs.length;i++){tabs[i].setPosition(x+9+i*22,y+7);tabs[i].setSize(20,20);}
        for(int i=0;i<rows.size();i++) {
            int step=page==4?48:page==0?30:page==1?30:34,height=page==4?38:26;
            int rowY=y+41+i*step;
            if(page==1)rowY=y+(i==0?41:i==1?91:126+(i-2)*30);
            if(page==4&&i==4)rowY=y+235;
            Label[] row=rows.get(i);row[0].setPosition(x+10,rowY);row[0].setSize(110,height);
            row[1].setPosition(x+125,rowY);row[1].setSize(125,height);
            if(page==4&&i<4){row[0].setVisible(false);row[1].setPosition(x+60,rowY);row[1].setSize(164,42);}
            if(page==4&&i==4)row[1].setSize(125,25);
        }
        for(int i=0;i<pageWidgets.size();i++){
            Widget widget=pageWidgets.get(i);
            if(page==6){int col=i<15?i%6:(i-15)%5;int row=i<15?i/6:3+(i-15)/5;widget.setPosition(x+16+col*37,y+42+row*35);widget.setSize(32,32);}
            else if(page==8){widget.setPosition(x+15+i%6*42,y+76+i/6*42);widget.setSize(32,32);}
            else if(page==7){widget.setPosition(x+15,y+68+i*42);widget.setSize(238,6);}
            else {widget.setPosition(x+14,y+267);widget.setSize(244,38);}
        }
        ivHelp.setPosition(x+15,y+281);ivHelp.setSize(180,24);
        for(int i=0;i<4;i++){
            int ry=y+40+i*48;moveTypes[i].setPosition(x+197,ry+10);moveTypes[i].setSize(49,22);
            moveUp[i].setPosition(x+30,ry);moveUp[i].setSize(18,18);moveDown[i].setPosition(x+30,ry+20);moveDown[i].setSize(18,18);
        }
        if(page==5&&!rows.isEmpty()){
            rows.getFirst()[0].setPosition(x+15,y+42);rows.getFirst()[0].setSize(230,22);
            rows.getFirst()[1].setPosition(x+15,y+69);rows.getFirst()[1].setSize(212,176);
            Label body=rows.getFirst()[1];StringBuilder text=new StringBuilder(),line=new StringBuilder();
            for(char c:historyText.toCharArray()){
                if(c=='\n'){text.append(line).append('\n');line.setLength(0);continue;}
                line.append(c);if(body.getFont()!=null&&body.getFont().computeTextWidth(line)>200&&line.length()>1){line.setLength(line.length()-1);text.append(line).append('\n');line.setLength(0);line.append(c);}
            }
            body.setText(text.append(line).toString());
        }
        if(page==7)for(int i=0;i<rows.size();i++){rows.get(i)[0].setPosition(x+15,y+43+i*42);rows.get(i)[1].setVisible(false);}
        if(page==8&&!rows.isEmpty()){rows.getFirst()[0].setPosition(x+15,y+43);rows.getFirst()[1].setPosition(x+15,y+75);rows.getFirst()[1].setSize(235,60);}
        dex.setPosition(x+221,y+44);dex.setSize(24,24);
        rename.setPosition(x+225,y+81);rename.setSize(16,16);
        for(int i=0;i<2;i++){types[i].setPosition(x+277+i*50,y+246);types[i].setSize(49,14);}
        int markX=x+277+(species!=null&&species.type1()!=species.type2()?100:50)+2;
        for(int i=0;i<marks.length;i++){marks[i].setVisible(false);marks[i].setPosition(markX+i*16,y+245);marks[i].setSize(16,16);}
        alpha.setPosition(x+476,y+80);alpha.setSize(24,24);

    }

    @Override protected void paintWidget(GUI gui) {
        if (pendingCostume != null && pendingCostume.isDone()) {
            var completed = pendingCostume;
            pendingCostume = null;
            configuring = false;
            try { commitCostume(completed.join()); }
            catch (java.util.concurrent.CompletionException | java.util.concurrent.CancellationException error) {
                item.setText(ClientStrings.get(9812));
                System.getLogger(PokemonSummaryWindow.class.getName()).log(System.Logger.Level.WARNING, "Unable to load fusion costume", error);
            }
        }
        if(pending!=null&&pending.isDone()) {
            var result=pending;pending=null;
            try {
                var loaded=result.join();species=loaded.species();int columns=8;
                cellWidth=loaded.cellWidth();cellHeight=loaded.cellHeight();anchorX=loaded.anchorX();anchorY=loaded.anchorY();
                int iconX=columns*cellWidth;
                // 正面帧先占若干行，背面帧接在其后，图集尾部同一行右侧仍放图鉴与精灵球图标。
                int frontRows=Math.max(1,(loaded.frames().size()+columns-1)/columns);
                int backRows=(loaded.backFrames().size()+columns-1)/columns;
                var pixels=new BufferedImage(iconX+48,Math.max(96,(frontRows+backRows)*cellHeight),BufferedImage.TYPE_INT_ARGB);
                var graphics=pixels.createGraphics();
                for(int i=0;i<loaded.frames().size();i++)graphics.drawImage(loaded.frames().get(i),(i%columns)*cellWidth,(i/columns)*cellHeight,null);
                for(int i=0;i<loaded.backFrames().size();i++)graphics.drawImage(loaded.backFrames().get(i),(i%columns)*cellWidth,(frontRows+i/columns)*cellHeight,null);
                graphics.drawImage(loaded.dex(),iconX,0,null);if(loaded.ball()!=null)graphics.drawImage(loaded.ball(),iconX+24,0,null);graphics.dispose();
                refresh();texture=((GlfwTwlRenderer)gui.getRenderer()).uploadImage(pixels);
                animation=new Image[loaded.frames().size()];for(int i=0;i<animation.length;i++)animation[i]=texture.getImage(i%columns*cellWidth,i/columns*cellHeight,cellWidth,cellHeight,Color.WHITE,false,Texture.Rotation.NONE);
                backAnimation=new Image[loaded.backFrames().size()];for(int i=0;i<backAnimation.length;i++)backAnimation[i]=texture.getImage(i%columns*cellWidth,(frontRows+i/columns)*cellHeight,cellWidth,cellHeight,Color.WHITE,false,Texture.Rotation.NONE);
                image=animation[0];
                dexImage=texture.getImage(iconX,0,24,24,Color.WHITE,false,Texture.Rotation.NONE);ballImage=texture.getImage(iconX+24,0,24,24,Color.WHITE,false,Texture.Rotation.NONE);
                var ribbons=new BufferedImage(35*32,32,BufferedImage.TYPE_INT_ARGB);var ribbonGraphics=ribbons.createGraphics();for(int i=0;i<loaded.ribbons().size();i++)ribbonGraphics.drawImage(loaded.ribbons().get(i),i*32,0,null);ribbonGraphics.dispose();
                var ribbonTexture=((GlfwTwlRenderer)gui.getRenderer()).uploadImage(ribbons);effectTextures.add(ribbonTexture);ribbonImages=new Image[35];for(int i=0;i<35;i++)ribbonImages[i]=ribbonTexture.getImage(i*32,0,32,32,Color.WHITE,false,Texture.Rotation.NONE);
                for(var definition:loaded.effects()){var particle=definition.texture();var effectTexture=((GlfwTwlRenderer)gui.getRenderer()).uploadImage(particle);effectTextures.add(effectTexture);effects.add(effectTexture.getImage(0,0,particle.getWidth(),particle.getHeight(),Color.WHITE,false,Texture.Rotation.NONE));}
                particleEffect=new SummaryParticles(loaded.effects(),effects);
            } catch(java.util.concurrent.CompletionException e) {
                picture.setTooltipContent(ClientStrings.get(9364));
            }
        }
        if(species!=null&&nameplates[species.type1()]!=null)
            nameplates[species.type1()].draw(getAnimationState(),getX()+274,getY()+34,226,24);
        super.paintWidget(gui);
    }

    @Override public void close() {
        if (pendingCostume != null) pendingCostume.cancel(false);
        pendingCostume = null;
        if (configurationMenu != null) { configurationMenu.closePopup(); configurationMenu = null; }
        if(pending!=null)pending.cancel(true);pending=null;
        if(texture!=null)texture.destroy();texture=null;image=null;animation=new Image[0];backAnimation=new Image[0];ribbonImages=new Image[0];
        cellWidth=96;cellHeight=96;anchorX=48;anchorY=48;
        showingBack=false;flipStart=-1;
        for(Texture effect:effectTextures)effect.destroy();effectTextures.clear();effects.clear();particleEffect=null;dexImage=null;ballImage=null;member=null;
        picture.setTooltipContent(null);species=null;
    }
}
