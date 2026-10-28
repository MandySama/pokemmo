package top.totodile.pokemmo.client.ui.world;

import com.badlogic.gdx.Gdx;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleListModel;
import top.totodile.pokemmo.client.ui.common.PlayerWindow;
import top.totodile.pokemmo.client.ui.common.PlayerTextInput;
import top.totodile.pokemmo.server.game.StoryDebug;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.*;

/** User-requested local test tools, deliberately separate from normal story scripts. */
public final class StoryDebugWindow extends PlayerWindow {
    private final Supplier<CompletionStage<StoryDebug.State>> load;
    private final Function<StoryDebug.Request,CompletionStage<Void>> apply;
    private StoryDebug.State state;
    private final Map<String,Integer> changes=new LinkedHashMap<>();
    private final Label status=new Label(), selected=new Label(), staged=new Label();
    private final EditField value;
    private final PlayerTextInput textInput;
    private final ToggleButton confirm=new ToggleButton("确认永久写入当前角色存档");
    private final Button add=new Button("加入修改"), temporary=new Button("临时应用"), permanent=new Button("永久应用"), restore=new Button("结束临时测试并恢复"), reload=new Button("刷新／清空修改");
    private final Fields model=new Fields();
    private final ListBox<String> fields=new ListBox<>(model);
    private final Widget content=new Widget(){@Override protected void layout(){arrange();}};
    private boolean loading;
    private long version;
    private final class Fields extends SimpleListModel<String> {
        public int getNumEntries(){return state==null?0:state.fields().size();}
        public String getEntry(int i){var f=state.fields().get(i);int v=changes.getOrDefault(f.key(),f.value());return label(f)+" = "+v+(f.key().startsWith("event:")?(v==0?"（未设置）":"（已设置）"):"");}
        @Override public Object getEntryTooltip(int i){return state.fields().get(i).key();}
        void changed(){fireAllChanged();}
    }
    private static String label(StoryDebug.Field field){return field.key().startsWith("event:")?"事件："+StoryGuideText.event(field.key().substring(6)):field.label();}
    public StoryDebugWindow(Widget root,boolean mobile,Supplier<CompletionStage<StoryDebug.State>> load,
                            Function<StoryDebug.Request,CompletionStage<Void>> apply,Runnable close) {
        super(root,mobile,"/adminframe-nontab",960,620,close);this.load=load;this.apply=apply;
        textInput=new PlayerTextInput(mobile,this::isOpen);value=textInput.field(10,false);
        frame.setTitle("剧情状态调试");content.setTheme("");frame.add(content);
        for(var label:new Label[]{status,selected,staged})label.setTheme("/label");
        fields.setTheme("/comboboxPopup.listbox");fields.addCallback(r->selection());
        value.setTheme("/editfield");value.setMaxTextLength(10);
        confirm.setTheme("/togglebutton");confirm.addCallback(this::buttons);
        for(var button:new Button[]{add,temporary,permanent,restore,reload})button.setTheme("/button");
        add.addCallback(this::stage);reload.addCallback(this::refresh);
        temporary.addCallback(()->submit(StoryDebug.Mode.TEMPORARY));
        permanent.addCallback(()->submit(StoryDebug.Mode.PERMANENT));
        restore.addCallback(()->submit(StoryDebug.Mode.RESTORE));
        for(var widget:new Widget[]{status,fields,selected,value,add,staged,confirm,temporary,permanent,restore,reload})content.add(widget);
        buttons();
    }
    @Override protected void refresh() {
        long token=++version;loading=true;changes.clear();state=null;model.changed();confirm.setActive(false);status.setText("正在读取剧情状态…");buttons();
        load.get().whenComplete((result,error)->Gdx.app.postRunnable(()->{
            if(token!=version||!isOpen())return;
            loading=false;
            if(error!=null){status.setText(message(error));buttons();return;}
            state=result;model.changed();if(!result.fields().isEmpty())fields.setSelected(0);
            status.setText(result.temporary()?"临时测试中：本轮所有存档变化将在结束测试或离开角色时恢复。":"当前使用真实存档；临时应用会先建立回滚点。");
            staged.setText("修改条件后，回到地图按交互键或走到触发点，执行真实剧情。");selection();buttons();
        }));
    }
    private void selection(){
        int i=fields.getSelected();if(state==null||i<0||i>=state.fields().size())return;
        var f=state.fields().get(i);selected.setText(label(f)+"  范围 "+f.min()+"～"+f.max());selected.setTooltipContent(f.key());
        value.setText(Integer.toString(changes.getOrDefault(f.key(),f.value())));
    }
    private void stage(){
        int i=fields.getSelected();if(state==null||i<0)return;var f=state.fields().get(i);
        try {
            int number=Integer.parseInt(value.getText().trim());
            if(number<f.min()||number>f.max())throw new NumberFormatException();
            changes.put(f.key(),number);model.changed();fields.setSelected(i);
            staged.setText("待应用 "+changes.size()+" 项；“加入修改”只暂存，点击应用后才生效。");buttons();
        } catch(NumberFormatException e){staged.setText("请输入 "+f.min()+"～"+f.max()+" 之间的整数。");}
    }
    private void buttons(){
        boolean ready=!loading&&state!=null;
        fields.setEnabled(ready);value.setEnabled(ready);add.setEnabled(ready);
        temporary.setEnabled(ready&&!changes.isEmpty());
        permanent.setEnabled(ready&&!state.temporary()&&!changes.isEmpty()&&confirm.isActive());
        restore.setEnabled(ready&&state.temporary());reload.setEnabled(!loading);
        confirm.setEnabled(ready&&!state.temporary());
    }
    private void submit(StoryDebug.Mode mode){
        if(loading)return;
        if(mode==StoryDebug.Mode.PERMANENT&&(!confirm.isActive()||state.temporary()))return;
        loading=true;buttons();status.setText("正在应用剧情状态并重新加载地图…");
        long token=version;
        var request=new StoryDebug.Request(mode,mode==StoryDebug.Mode.RESTORE?Map.of():changes);
        apply.apply(request).whenComplete((ignored,error)->Gdx.app.postRunnable(()->{
            if(token!=version)return;
            loading=false;
            if(error!=null){status.setText(message(error));buttons();}
            else {changes.clear();closePopup();}
        }));
    }
    private static String message(Throwable error){while(error.getCause()!=null)error=error.getCause();return "操作失败："+error.getMessage();}
    private void arrange(){
        content.setPosition(frame.getInnerX(),frame.getInnerY());content.setSize(frame.getInnerWidth(),frame.getInnerHeight());
        int x=content.getInnerX()+8,y=content.getInnerY()+8,w=Math.max(0,content.getInnerWidth()-16),h=content.getInnerHeight()-16;
        int row=mobile?48:30;
        status.setPosition(x,y);status.setSize(w,row);y+=row+6;
        int listHeight=Math.max(40,h-row*6-40);fields.setPosition(x,y);fields.setSize(w,listHeight);y+=listHeight+6;
        selected.setPosition(x,y);selected.setSize(w,row);y+=row+4;
        value.setPosition(x,y);value.setSize(Math.min(180,w/3),row);
        add.setPosition(x+Math.min(180,w/3)+8,y);add.setSize(Math.min(140,w/3),row);
        reload.setPosition(x+w-Math.min(180,w/3),y);reload.setSize(Math.min(180,w/3),row);y+=row+4;
        staged.setPosition(x,y);staged.setSize(w,row);y+=row+4;
        confirm.setPosition(x,y);confirm.setSize(w,row);y+=row+4;
        Button[] buttons={temporary,permanent,restore};
        for(int i=0;i<buttons.length;i++){buttons[i].setPosition(x+i*(w/3),y);buttons[i].setSize(Math.max(0,w/3-5),row);}
    }
    @Override protected void layout(){super.layout();arrange();}
    @Override public void closePopup(){version++;textInput.close();super.closePopup();}
}
