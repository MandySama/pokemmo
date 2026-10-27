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
    private final Label heading=new Label(),detail=new Label();
    private final Model model=new Model();
    private final ListBox<String> npcs=new ListBox<>(model);
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
        public String getEntry(int index){var n=report.npcs().get(index);return "#"+n.id()+" "+shortName(n.script())+" · "+n.kind()+(n.visible()?"":" · 隐藏");}
        void changed(){fireAllChanged();}
    }
    public StoryInspectionWindow(Widget owner,boolean mobile,Supplier<CompletionStage<StoryInspection.Report>> load,
                                 BiFunction<Integer,Integer,String> text,Runnable onClose){
        super(owner,mobile,"/adminframe-nontab",960,620,onClose);this.load=load;this.text=text;
        frame.setTitle("地图剧情");content.setTheme("");body.setTheme("");
        for(var label:new Label[]{heading,detail}){label.setTheme("/label");label.setAlignment(Alignment.TOPLEFT);}
        reload.setTheme("/button");reload.addCallback(this::refresh);
        npcs.setTheme("/comboboxPopup.listbox");npcs.addCallback(reason->{if(npcs.getSelected()>=0)showSelection();});
        scroll.setTheme("/scrollpane");scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);scroll.setContent(body);body.add(detail);
        content.add(heading);content.add(reload);content.add(npcs);content.add(scroll);frame.add(content);
    }
    @Override protected void refresh(){
        long request=++version;reload.setEnabled(false);heading.setText("正在读取当前地图剧情…");
        try{load.get().whenComplete((value,error)->Gdx.app.postRunnable(()->{
            if(request!=version||!isOpen())return;reload.setEnabled(true);
            if(error!=null){heading.setText("读取失败");raw="无法读取剧情："+error.getMessage();wrappedWidth=-1;body.invalidateLayout();return;}
            int old=npcs.getSelected();report=value;model.changed();
            heading.setText(value.mapKey().isBlank()?"地图 "+value.bank()+":"+value.map():value.mapKey()+" ("+value.bank()+":"+value.map()+")");
            if(!value.npcs().isEmpty())npcs.setSelected(Math.max(0,Math.min(old,value.npcs().size()-1)));
            showSelection();content.invalidateLayout();
        }));}catch(Throwable error){reload.setEnabled(true);heading.setText("读取失败");raw=error.getMessage();wrappedWidth=-1;body.invalidateLayout();}
    }
    private void showSelection(){
        if(report==null)return;
        var value=new StringBuilder("当前状态\n").append(report.state()).append("\n\n下一步剧情\n");
        for(var step:report.nextSteps())value.append("• ").append(step).append('\n');
        value.append("\n匹配仅指存档条件；实际触发还需正确的位置和朝向。查看台词不会执行动作或修改进度。\n");
        int index=npcs.getSelected();
        if(index>=0&&index<report.npcs().size()){
            var n=report.npcs().get(index);value.append("\nNPC #").append(n.id()).append(" · 图像 ").append(n.graphics()).append(" · 脚本坐标 (").append(n.x()).append(',').append(n.y()).append(") · ").append(n.visible()?"显示":"隐藏").append('\n').append(n.script()).append('\n');
            if(n.branches().isEmpty())value.append(n.kind().equals("未接入")?"该 NPC 的交互脚本尚未接入，无法预览条件分支。":"该 NPC 没有独立交互脚本，可能由地图事件控制。").append('\n');
            for(var branch:n.branches()){
                value.append("\n【").append(branch.match()).append("】").append(branch.name()).append('\n').append("条件：").append(branch.condition()).append('\n');
                if(branch.lines().isEmpty())value.append("此分支没有直接台词。\n");
                for(var line:branch.lines()){
                    String message;
                    try{message=line.text()==0?line.message():text.apply(line.text(),line.species());}catch(RuntimeException invalid){message="无法加载原始台词（"+line.text()+"）："+invalid.getMessage();}
                    value.append(message).append("\n\n");
                }
                if(!branch.actions().isEmpty())value.append("后续动作：").append(String.join(" → ",branch.actions())).append('\n');
            }
        }else value.append("\n当前地图没有可查看的 NPC。\n");
        raw=value.toString();wrappedWidth=-1;scroll.setScrollPositionY(0);body.invalidateLayout();
    }
    private void layoutContent(){
        content.setPosition(frame.getInnerX(),frame.getInnerY());content.setSize(frame.getInnerWidth(),frame.getInnerHeight());
        int x=content.getInnerX()+8,y=content.getInnerY()+6,w=Math.max(0,content.getInnerWidth()-16),h=Math.max(0,content.getInnerHeight()-12);
        int header=Math.max(28,reload.getPreferredHeight());reload.setPosition(x+w-80,y);reload.setSize(80,header);heading.setPosition(x,y);heading.setSize(Math.max(0,w-90),header);
        y+=header+8;h-=header+8;
        if(mobile){int rows=report==null?1:Math.max(1,Math.min(3,report.npcs().size()));int listHeight=Math.min(Math.max(80,h/2),Math.max(80,npcs.getCellHeight()*rows+npcs.getBorderVertical()));npcs.setPosition(x,y);npcs.setSize(w,listHeight);y+=listHeight+8;h-=listHeight+8;scroll.setPosition(x,y);scroll.setSize(w,Math.max(0,h));}
        else{int listWidth=Math.min(280,w/3);npcs.setPosition(x,y);npcs.setSize(listWidth,Math.max(0,h));scroll.setPosition(x+listWidth+8,y);scroll.setSize(Math.max(0,w-listWidth-8),Math.max(0,h));}
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
    @Override public void closePopup(){version++;super.closePopup();}
}
