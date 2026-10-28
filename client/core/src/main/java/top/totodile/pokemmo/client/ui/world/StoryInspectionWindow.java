package top.totodile.pokemmo.client.ui.world;

import com.badlogic.gdx.Gdx;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleListModel;
import top.totodile.pokemmo.client.ui.common.*;
import top.totodile.pokemmo.server.game.StoryInspection;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.*;

/** User-requested map/script viewer; shares the existing admin frame and scroll widgets. */
public final class StoryInspectionWindow extends PlayerWindow {
    private final Supplier<CompletionStage<StoryInspection.Report>> load;
    private final BiFunction<Integer,Integer,String> text;
    private final Button reload=new Button("刷新");
    private final Button guide=new Button("主线／支线指引");
    private final ToggleButton dialogue=new ToggleButton("查看对白"), technical=new ToggleButton("技术信息");
    private top.totodile.pokemmo.client.ui.pokedex.PokedexChoice branches;
    private boolean overview=true;
    private int branchIndex;
    private Supplier<String> mapName=()->"";
    public void mapName(Supplier<String> value){mapName=value;}
    private IntFunction<String> itemName=id->"道具（编号 "+id+"）";
    public void itemNames(IntFunction<String> value){itemName=value;}
    private final Button edit=new Button("修改条件／重复测试");
    public void onEdit(Runnable action){edit.addCallback(action);}
    private final Label heading=new Label(),detail=new Label();
    private final Model model=new Model();
    private final ListBox<String> npcs=new ListBox<>(model){
        @Override protected ListBoxDisplay createDisplay(){return new ListBoxLabel(){
            @Override protected boolean handleListBoxEvent(Event event){
                if(event.getType()==Event.Type.MOUSE_BTNDOWN&&event.getMouseButton()==Event.MOUSE_LBUTTON){doListBoxCallback(CallbackReason.MOUSE_CLICK);return true;}
                return super.handleListBoxEvent(event);
            }
        };}
    };
    private final Widget body=new Widget(){
        @Override public int getPreferredInnerHeight(){return detail.getPreferredHeight()+16;}
        @Override protected void layout(){layoutDetail();}
    };
    private final ScrollPane scroll=new TouchScrollPane();
    private final Widget content=new Widget(){@Override protected void layout(){layoutContent();}};
    private StoryInspection.Report report;
    private String raw="";
    private long version;
    private int wrappedWidth=-1;
    private class Model extends SimpleListModel<String>{
        public int getNumEntries(){return report==null?0:report.npcs().size();}
        public String getEntry(int index){var n=report.npcs().get(index);return npcName(n)+(n.visible()?"":" · 未出现")+"  #"+n.id();}
        void changed(){fireAllChanged();}
    }
    public StoryInspectionWindow(Widget owner,boolean mobile,Supplier<CompletionStage<StoryInspection.Report>> load,
                                 BiFunction<Integer,Integer,String> text,Runnable onClose){
        super(owner,mobile,"/adminframe-nontab",960,620,onClose);this.load=load;this.text=text;
        frame.setTitle("地图剧情");content.setTheme("");body.setTheme("");
        for(var label:new Label[]{heading,detail}){label.setTheme("/label");label.setAlignment(Alignment.TOPLEFT);}
        reload.setTheme("/button");reload.addCallback(this::refresh);
        edit.setTheme("/button");content.add(edit);
        npcs.setTheme("/comboboxPopup.listbox");npcs.addCallback(reason->{if(npcs.getSelected()>=0){overview=false;selectNpc();}});
        scroll.setTheme("/scrollpane");scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);scroll.setContent(body);body.add(detail);
        guide.setTheme("/button");guide.addCallback(()->{overview=true;renderGuide();});content.add(guide);
        for(var button:new ToggleButton[]{dialogue,technical}){button.setTheme("/button");button.addCallback(this::renderGuide);content.add(button);}
        content.add(heading);content.add(reload);content.add(npcs);content.add(scroll);frame.add(content);
    }
    @Override protected void refresh(){
        long request=++version;reload.setEnabled(false);heading.setText("正在读取当前地图剧情…");
        try{load.get().whenComplete((value,error)->Gdx.app.postRunnable(()->{
            if(request!=version||!isOpen())return;reload.setEnabled(true);
            if(error!=null){heading.setText("读取失败");raw="无法读取剧情："+error.getMessage();wrappedWidth=-1;body.invalidateLayout();return;}
            int old=npcs.getSelected();boolean wasOverview=overview;report=value;model.changed();
            String localized=mapName.get();heading.setText(localized==null||localized.isBlank()?friendlyMap(value.mapKey()):localized);
            if(!value.npcs().isEmpty())npcs.setSelected(Math.max(0,Math.min(old,value.npcs().size()-1)));
            overview=wasOverview;selectNpc();content.invalidateLayout();
        }));}catch(Throwable error){reload.setEnabled(true);heading.setText("读取失败");raw=error.getMessage();wrappedWidth=-1;body.invalidateLayout();}
    }
    private void selectNpc(){
        if(branches!=null){branches.closeChoices();content.removeChild(branches);branches=null;}
        var npc=selectedNpc();branchIndex=0;
        if(npc!=null&&!npc.branches().isEmpty()){
            for(int i=0;i<npc.branches().size();i++)if(!npc.branches().get(i).match().equals("条件未满足")){branchIndex=i;break;}
            String[] names=new String[npc.branches().size()];
            for(int i=0;i<names.length;i++){var b=npc.branches().get(i);names[i]=status(b)+" · "+branchName(b,i);}
            branches=new top.totodile.pokemmo.client.ui.pokedex.PokedexChoice(()->{branchIndex=branches.selected();renderGuide();},names);
            branches.setTheme("/combobox");branches.select(branchIndex);content.add(branches);
        }
        renderGuide();content.invalidateLayout();
    }
    private StoryInspection.Npc selectedNpc(){int i=npcs.getSelected();return report!=null&&i>=0&&i<report.npcs().size()?report.npcs().get(i):null;}
    private void renderGuide(){
        if(report==null)return;
        var value=new StringBuilder();
        guide.setText(overview?"主线／支线指引 ●":"主线／支线指引");
        dialogue.setText(dialogue.isActive()?"收起对白":"查看对白");
        technical.setText(technical.isActive()?"收起技术信息":"技术信息");
        if(branches!=null)branches.setVisible(!overview);
        dialogue.setVisible(!overview);technical.setVisible(!overview);
        if(overview){
            value.append(mainGoal(report.state())).append("\n\n接下来做什么\n\n");
            var side=new ArrayList<String>();int number=0;
            for(String step:report.nextSteps()){
                if(step.contains("支线")){side.add(step);continue;}
                value.append(++number).append(". ").append(friendlyStep(step)).append("\n\n");
            }
            if(number==0)value.append("当前没有可确认的主线指引。\n\n");
            if(!side.isEmpty()){value.append("可选支线\n\n");for(var step:side)value.append("• ").append(step).append("\n\n");}
            value.append("当前进度\n").append(friendlyState(report.state()));
            value.append("\n\n选择左侧人物，查看触发条件、分支和结果。\n仅查看指引不会推进剧情或发放奖励。");
        }else{
            var n=selectedNpc();
            if(n==null)value.append("当前地图没有可查看的交互对象。");
            else{
                value.append(npcName(n)).append("\n").append(n.visible()?"人物已在地图中出现":"人物尚未出现；需先满足前置剧情").append("\n\n");
                value.append("触发方式\n前往当前地图 (").append(n.x()).append(", ").append(n.y()).append(") 附近，面向该人物，按交互键。\n");
                if(n.branches().isEmpty())value.append("\n").append(n.kind().equals("未接入")?"尚未接入：暂无可靠的条件、分支和结果指引。":"此人物由地图事件控制，没有独立对话分支。");
                else{
                    var b=n.branches().get(Math.min(branchIndex,n.branches().size()-1));
                    value.append("\n").append(status(b)).append(" · ").append(branchName(b,branchIndex)).append("\n\n");
                    value.append("需要满足\n");
                    for(String condition:b.condition().split("；"))if(!condition.isBlank()){
                        String[] parts=condition.startsWith("{")?new String[]{condition}:condition.split("、");
                        for(String part:parts)value.append("• ").append(conditionText(part)).append('\n');
                    }
                    value.append("\n触发结果\n");
                    var results=new LinkedHashSet<String>();
                    for(String action:b.actions()){String human=resultText(action);if(!human.isBlank())results.add(human);}
                    if(!b.lines().isEmpty())value.append("• 进行").append(b.lines().size()).append("段对话").append(results.isEmpty()?"，不改变剧情状态":"").append('\n');
                    if(results.isEmpty()&&b.lines().isEmpty())value.append("• 本分支没有直接对白或独立动作。\n");
                    for(String result:results)value.append("• ").append(result).append('\n');
                    value.append("\n").append(b.match().equals("条件未满足")?"先完成上述前置条件，再尝试触发。":"存档条件已满足；仍需正确的位置、朝向或对话选择。");
                    if(dialogue.isActive()){
                        value.append("\n\n对白预览\n");
                        for(var line:b.lines()){
                            try{value.append(line.text()==0?line.message():text.apply(line.text(),line.species())).append("\n\n");}
                            catch(RuntimeException invalid){value.append("该段对白暂时无法读取。\n");}
                        }
                    }
                    if(technical.isActive())value.append("\n\n技术信息\n分支：").append(b.name()).append("\n原始条件：").append(b.condition()).append("\n动作：").append(String.join(" → ",b.actions()));
                }
                if(technical.isActive())value.append("\n\nNPC #").append(n.id()).append(" · 图像 ").append(n.graphics()).append("\n脚本：").append(n.script()).append("\n地图：").append(report.mapKey()).append(" (").append(report.bank()).append(':').append(report.map()).append(')');
            }
        }
        raw=value.toString();wrappedWidth=-1;scroll.setScrollPositionY(0);body.invalidateLayout();content.invalidateLayout();
    }
    private static String status(StoryInspection.Branch b){return switch(b.match()){case "当前匹配"->"当前可触发";case "等待玩家选择"->"等待玩家选择";default->"前置条件未满足";};}
    private static String npcName(StoryInspection.Npc n){
        String key=shortName(n.script());
        if(key.contains("SignLady"))return "提示牌前的女孩 · 支线";
        if(key.equals("FatMan"))return "镇上居民 · 固定对话";
        if(key.contains("Mom"))return "妈妈 · 休息与提醒";
        if(key.contains("Nurse"))return "护士 · 队伍治疗";
        if(key.contains("Oak"))return "大木博士 · 主线";
        if(key.contains("Rival"))return "劲敌 · 剧情分支";
        if(key.contains("Starter")||key.contains("PokeBall"))return "初始宝可梦 · 选择";
        if(key.contains("Aide"))return "博士助手";
        if(key.contains("Brock"))return "小刚 · 道馆挑战";
        if(key.contains("Birch"))return "小田卷博士 · 主线";
        if(key.contains("Brendan")||key.contains("May"))return "劲敌 · 主线";
        return n.kind().equals("固定对话")?"附近居民 · 固定对话":n.kind().equals("未接入")?"交互对象 · 尚未接入":"地图人物 · 剧情分支";
    }
    private static String branchName(StoryInspection.Branch b,int index){
        String name=b.name();String key=name.split(" / ")[0];
        String title=switch(key){case "first_right"->"初次交谈 · 面朝右";case "first_left"->"初次交谈 · 其他朝向";case "read"->"让路后，去读提示牌";case "show"->"获得伙伴后，镇口教学";case "useful"->"刚读完提示牌";case "done"->"读牌教学完成";case "mother_boy","mother_girl","mother"->b.actions().contains("恢复队伍")?"休息并恢复队伍":"妈妈的提醒";case "mother_heal"->"休息并恢复队伍";case "护士"->"护士治疗";case "固定对话"->"日常交谈";default->"剧情分支 "+(index+1);};
        if(name.contains("选择是"))title+=" · 选择是";else if(name.contains("选择否"))title+=" · 选择否";
        else if(name.endsWith(" / 确认"))title+=" · 确认";else if(name.endsWith(" / 取消"))title+=" · 取消";
        return title;
    }
    private static String friendlyState(String state){return state.replace("开场阶段=0，初始选择=未选择","尚未获得初始宝可梦；博士开场尚未触发").replace("开场阶段=","开场进度：").replace("初始选择=","初始伙伴：");}
    private static String mainGoal(String state){
        if(state.startsWith("开场阶段=")){
            if(state.startsWith("开场阶段=0")||state.startsWith("开场阶段=1")||state.startsWith("开场阶段=2"))return "主线目标 · 领取初始宝可梦";
            if(state.startsWith("开场阶段=3"))return "主线目标 · 完成首次劲敌对战";
            if(state.startsWith("开场阶段=4")||state.startsWith("开场阶段=5"))return "主线目标 · 交付包裹并领取图鉴";
            return state.contains("冠军=是")?"主线目标 · 冠军收尾与后续冒险":"主线目标 · 道馆挑战与宝可梦联盟";
        }
        return "剧情指引";
    }
    private static String friendlyStep(String step){
        if(step.contains(" → ")){String[] pair=step.split(" → ",2);return "前往 "+friendlyMap(pair[0])+"，继续可用的剧情（到达后选择人物查看条件）。";}
        return step.replaceAll("；序号 \\d+", "").replace(" NPC #4、#5 或 #6", "三个初始精灵球");
    }
    private static String friendlyMap(String key){return switch(key){case "PalletTown"->"真新镇";case "PalletTown_PlayersHouse_1F"->"玩家家 · 一楼";case "PalletTown_ProfessorOaksLab"->"大木博士研究所";case "ViridianCity_Mart"->"常磐市商店";default->key.replace('_',' ');};}
    private String conditionText(String condition){
        if(condition.startsWith("{")){
            try{var c=new com.badlogic.gdx.utils.JsonReader().parse(condition);String type=c.getString("type");if(type.equals("HAS_ITEM")||type.equals("NO_ITEM"))return (type.equals("HAS_ITEM")?"持有":"尚未持有")+itemName.apply(c.getInt("value"));}catch(RuntimeException ignored){}
            return StoryGuideText.condition(condition);
        }
        return condition.replace("男角色","玩家为男主角").replace("女角色","玩家为女主角").replace("初始选择 = 0","已选择妙蛙种子").replace("初始选择 = 1","已选择小火龙").replace("初始选择 = 2","已选择杰尼龟");
    }
    private String resultText(String action){
        if(action.startsWith("NPC 转向")||action.startsWith("等待 ")||action.equals("显示惊叹号")||action.equals("播放音乐"))return "";
        if(action.startsWith("NPC 移动至"))return "人物按剧情移动到指定位置";
        if(action.startsWith("剧情训练家战："))return "与剧情训练家对战；获胜后继续后续剧情";
        if(action.startsWith("获得道具 ")){
            var m=java.util.regex.Pattern.compile("获得道具 (\\d+) × (\\d+)").matcher(action);
            if(m.find())return "获得"+itemName.apply(Integer.parseInt(m.group(1)))+" × "+m.group(2);
        }
        if(action.startsWith("包裹事务："))return switch(action.substring(5)){case "PICKUP"->"领取大木的包裹，返回研究所交给博士";case "DELIVER"->"交付包裹，开启图鉴领取流程";case "RECEIVE_POKEDEX"->"领取图鉴和五个精灵球";default->"推进包裹任务";};
        if(action.startsWith("保存事件标记："))return "记录事件："+StoryGuideText.event(action.substring("保存事件标记：".length()));
        if(action.startsWith("SET_"))return "保存本分支进度，后续对话或地图状态可能变化";
        if(action.matches("[A-Z_]+"))return "包含其他剧情动作（可展开技术信息查看）";
        return action.replace("记录本次地图已让路","女孩让出道路；本次在地图内可继续读牌").replace("记录已读提示牌","保存已读提示牌状态，重新进镇后进入后续对话").replace("（仅预览，不领取）", "");
    }
    private void layoutContent(){
        content.setPosition(frame.getInnerX(),frame.getInnerY());content.setSize(frame.getInnerWidth(),frame.getInnerHeight());
        int x=content.getInnerX()+8,y=content.getInnerY()+6,w=Math.max(0,content.getInnerWidth()-16),h=Math.max(0,content.getInnerHeight()-12);
        int header=Math.max(28,reload.getPreferredHeight());reload.setPosition(x+w-80,y);reload.setSize(80,header);heading.setPosition(x,y);heading.setSize(Math.max(0,w-90),header);
        y+=header+8;h-=header+8;
        guide.setPosition(x,y);guide.setSize(Math.min(w/2,200),header);
        edit.setPosition(x+Math.min(w/2,200)+8,y);edit.setSize(Math.min(w/2-8,220),header);y+=header+8;h-=header+8;
        if(mobile){int rows=report==null?1:Math.max(1,Math.min(3,report.npcs().size()));int listHeight=Math.min(Math.max(80,h/2),Math.max(80,npcs.getCellHeight()*rows+npcs.getBorderVertical()));npcs.setPosition(x,y);npcs.setSize(w,listHeight);y+=listHeight+8;h-=listHeight+8;layoutRight(x,y,w,h,header);}
        else{int listWidth=Math.min(280,w/3);npcs.setPosition(x,y);npcs.setSize(listWidth,Math.max(0,h));layoutRight(x+listWidth+8,y,Math.max(0,w-listWidth-8),h,header);}
    }
    private void layoutRight(int x,int y,int w,int h,int row){
        if(!overview){
            if(branches!=null){branches.setPosition(x,y);branches.setSize(w,row);y+=row+6;h-=row+6;}
            dialogue.setPosition(x,y);dialogue.setSize(Math.min(130,w/2),row);
            technical.setPosition(x+Math.min(130,w/2)+8,y);technical.setSize(Math.min(130,w/2-8),row);y+=row+8;h-=row+8;
        }
        scroll.setPosition(x,y);scroll.setSize(w,Math.max(0,h));
    }
    @Override protected void layout(){super.layout();layoutContent();}
    private void layoutDetail(){
        int width=Math.max(20,body.getInnerWidth()-16);
        if(width!=wrappedWidth&&detail.getFont()!=null){wrappedWidth=width;detail.setText(wrap(raw,width));}
        detail.setPosition(body.getInnerX()+8,body.getInnerY()+8);detail.setSize(width,detail.getPreferredHeight());
    }
    private String wrap(String input,int width){
        var out=new StringBuilder();var line=new StringBuilder();
        for(int at=0;at<input.length();){int code=input.codePointAt(at);at+=Character.charCount(code);
            if(code=='\n'){out.append(line).append('\n');line.setLength(0);continue;}
            var next=line.toString()+new String(Character.toChars(code));
            if(!line.isEmpty()&&detail.getFont().computeTextWidth(next)>width){out.append(line).append('\n');line.setLength(0);}
            line.appendCodePoint(code);
        }
        return out.append(line).toString();
    }
    private static String shortName(String script){int at=script.lastIndexOf("EventScript_");return at<0?(script.equals("0x0")?"地图事件":script):script.substring(at+12);}
    @Override public void closePopup(){version++;if(branches!=null)branches.closeChoices();super.closePopup();}
}
