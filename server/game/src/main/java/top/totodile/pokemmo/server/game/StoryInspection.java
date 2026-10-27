package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import top.totodile.pokemmo.db.Database;
import java.util.*;

/** Read-only projection of the scripts actually wired into KantoStory, not a script executor. */
public final class StoryInspection {
    private StoryInspection() {}
    public record Line(int text,int species,String message) {public Line(int text,int species){this(text,species,"");}}
    public record Branch(String name,String condition,String match,List<Line> lines,List<String> actions) {
        public Branch { lines=List.copyOf(lines);actions=List.copyOf(actions); }
    }
    public record Npc(int id,int graphics,int x,int y,boolean visible,String script,String kind,List<Branch> branches) {
        public Npc { branches=List.copyOf(branches); }
    }
    public record Report(int region,int bank,int map,String mapKey,String state,List<String> nextSteps,List<Npc> npcs) {
        public Report { nextSteps=List.copyOf(nextSteps);npcs=List.copyOf(npcs); }
    }
    static Report inspect(Database db,String owner,int region,GameService.Position p,KantoStory session) {
        if(region!=0)return new Report(region,p.bank(),p.map(),"","该地区尚未接入地图剧情查看",List.of("无法根据当前已接入脚本判断下一步。"),List.of());
        var rows=db.query("SELECT stage,starter FROM character_kanto_story WHERE character_id=?",owner);
        var state=rows.isEmpty()?new StoryData.State(false,0,3):new StoryData.State(true,Integer.parseInt(rows.get(0)[0]),Integer.parseInt(rows.get(0)[1]));
        int gender=Integer.parseInt(db.query("SELECT gender FROM characters WHERE id=?",owner).get(0)[0]);
        boolean hasParty=!db.query("SELECT id FROM pokemon WHERE trainer_id=? AND container_id=1 AND is_egg=0 LIMIT 1",owner).isEmpty();
        var girl=session==null?PalletSignLady.inspect(db,owner):session.signLadyView();
        var parcelPhase=OakParcel.phase(db,owner,state);
        var projected=session==null?MapEntities.projectStory(db,owner,parcelPhase.equals("DELIVER")?new StoryData.State(true,5,state.starter()):state):session.entities();
        var visible=projected.stream().filter(m->m.bank()==p.bank()&&m.map()==p.map()).flatMap(m->m.npcs().stream()).toList();
        var original=MapEntities.definitions(p.bank(),p.map());
        String key=MapEntities.mapKey(p.bank(),p.map());
        var result=new ArrayList<Npc>();
        for(var definition:original){
            var actual=visible.stream().filter(n->n.id()==definition.id()).findFirst();
            var npc=actual.orElse(definition);
            if(p.bank()==3&&p.map()==0&&npc.id()==0)npc=PalletSignLady.project(npc,girl);
            String script=MapEntities.script(p.bank(),p.map(),npc.id());
            var branches=new ArrayList<Branch>();String kind="未接入";
            var fixed=FixedNpcDialogues.find(p.bank(),p.map(),npc.id());
            if(p.bank()==3&&p.map()==0&&npc.id()==0){
                kind="条件对话";
                String current=PalletSignLady.conversationNode(girl,p.direction());
                String[] nodes={"first_right","first_left","read","show","useful","done"};
                String[] descriptions={"未读牌、未让路、未等待镇口展示；玩家朝右交谈", "未读牌、未让路、未等待镇口展示；玩家非朝右交谈", "本次地图已让路，尚未读牌", "已领取初始宝可梦，进镇后在镇口等待展示", "刚读完提示牌，尚未重新进入真新镇", "已读牌并重新进入真新镇"};
                for(int i=0;i<nodes.length;i++)walk(KantoStory.signLadyProgram(),nodes[i],nodes[i],descriptions[i],actual.isPresent()&&current.equals(nodes[i]),false,npc.id(),state,gender,branches,new ArrayList<>(),new ArrayList<>(),new HashSet<>());
            } else if(state.enabled() && (key.equals("ViridianCity_Mart")&&npc.id()==0 || key.equals("PalletTown_ProfessorOaksLab")&&npc.id()==3&&state.stage()>=4)) {
                kind="条件对话";
                for(var value:KantoStory.parcelProgram().getAsJsonArray("triggers")) {
                    var t=value.getAsJsonObject();
                    if(!t.get("type").getAsString().equals("NPC")||!t.get("map").getAsString().equals(key)||t.get("entityIdx").getAsInt()!=npc.id())continue;
                    boolean eligible=true;
                    for(var cv:t.getAsJsonArray("conditions")) {
                        var c=cv.getAsJsonObject();String type=c.get("type").getAsString();
                        eligible &= type.equals("PARCEL_NEEDS_PICKUP")?OakParcel.needsPickup(db,owner,state):type.equals("PARCEL_PHASE_EQUALS")?parcelPhase.equals(c.get("value").getAsString()):matches(c,state,gender);
                    }
                    String node=t.get("startNode").getAsString();
                    walk(KantoStory.parcelProgram(),node,node,describe(t.getAsJsonArray("conditions")),eligible&&actual.isPresent(),false,npc.id(),state,gender,branches,new ArrayList<>(),new ArrayList<>(),new HashSet<>());
                }
            } else if(state.enabled()&&state.stage()>=6&&mainlineNpc(key,npc.id())) {
                kind="条件剧情";
                for(var chapter:KantoMainline.PROGRAMS)for(var value:chapter.getAsJsonArray("triggers")) {
                    var t=value.getAsJsonObject();
                    if(!t.get("type").getAsString().equals("NPC")||!t.get("map").getAsString().equals(key)||t.get("entityIdx").getAsInt()!=npc.id())continue;
                    boolean eligible=true;
                    for(var cv:t.getAsJsonArray("conditions"))eligible &= liveCondition(db,owner,cv.getAsJsonObject(),state,gender);
                    var frozen=chapter.deepCopy();
                    for(var nv:frozen.getAsJsonObject("nodes").entrySet())for(var av:nv.getValue().getAsJsonObject().getAsJsonArray("actions")) {
                        var a=av.getAsJsonObject();if(a.get("type").getAsString().equals("BRANCH")){
                            boolean match=liveCondition(db,owner,a.getAsJsonObject("condition"),state,gender);
                            var c=new JsonObject();c.addProperty("display",describe(a.getAsJsonObject("condition")));c.addProperty("type","STORY_STAGE_EQUALS");c.addProperty("value",match?state.stage():-1);a.add("condition",c);
                        }
                    }
                    String node=t.get("startNode").getAsString();
                    walk(frozen,node,node,describe(t.getAsJsonArray("conditions")),eligible&&actual.isPresent(),false,npc.id(),state,gender,branches,new ArrayList<>(),new ArrayList<>(),new HashSet<>());
                }
            } else if(fixed!=null){kind="固定对话";walk(fixed,"talk","固定对话","无剧情分支",actual.isPresent(),false,npc.id(),state,gender,branches,new ArrayList<>(),new ArrayList<>(),new HashSet<>());}
            else if(MapEntities.nurse(p.bank(),p.map(),npc.id())){kind="条件对话";walk(KantoStory.nurseProgram(),"nurse","护士","与护士交谈",actual.isPresent(),false,npc.id(),state,gender,branches,new ArrayList<>(),new ArrayList<>(),new HashSet<>());}
            else {
                var opening=KantoStory.openingProgram();
                if(!state.enabled()&&p.bank()==4&&p.map()==0&&npc.id()==0){
                    kind="条件对话";
                    for(String node:List.of("mother_boy","mother_girl","mother_heal")){
                        boolean eligible=node.equals("mother_heal")?hasParty:!hasParty&&(node.equals("mother_boy")== (gender==0));
                        String condition=node.equals("mother_heal")?"旧角色；队伍有非蛋宝可梦":node.equals("mother_boy")?"旧角色；队伍无非蛋宝可梦；男角色":"旧角色；队伍无非蛋宝可梦；女角色";
                        walk(opening,node,node,condition,eligible&&actual.isPresent(),false,npc.id(),state,gender,branches,new ArrayList<>(),new ArrayList<>(),new HashSet<>());
                    }
                } else {
                    for(var value:opening.getAsJsonArray("triggers")){
                        var trigger=value.getAsJsonObject();
                        if(!trigger.get("type").getAsString().equals("NPC")||!trigger.get("map").getAsString().equals(key)||trigger.get("entityIdx").getAsInt()!=npc.id())continue;
                        String node=trigger.get("startNode").getAsString();
                        boolean enabled=state.enabled()&&matches(trigger.getAsJsonArray("conditions"),state,gender)&&actual.isPresent();
                        kind="条件对话";
                        walk(opening,node,node,describe(trigger.getAsJsonArray("conditions")),enabled,false,npc.id(),state,gender,branches,new ArrayList<>(),new ArrayList<>(),new HashSet<>());
                    }
                    // Explicit opening fallback routes in KantoStory.request. Parcel branches are handled above.
                    if(p.bank()==4&&p.map()==3){
                        String fallback=null;
                        if(!state.enabled())fallback=npc.id()<=2||npc.id()==7?"aide":npc.id()==3?"choose_again":npc.id()==8?"dex":"ball";
                        else if(npc.id()==2||npc.id()==7)fallback="aide";
                        else if(npc.id()>=4&&npc.id()<=6)fallback="ball";
                        if(fallback!=null){kind="条件对话";boolean triggered=branches.stream().anyMatch(b->!b.match().equals("条件未满足"));walk(opening,fallback,"兜底："+fallback,state.enabled()?"未命中前述剧情触发分支":"旧角色，未启用开场记录",actual.isPresent()&&!triggered,false,npc.id(),state,gender,branches,new ArrayList<>(),new ArrayList<>(),new HashSet<>());}
                    }
                }
            }
            if(branches.isEmpty()&&(script.isBlank()||script.equals("0x0")))kind="无独立交互脚本";
            result.add(new Npc(npc.id(),npc.graphics(),npc.x(),npc.y(),actual.isPresent(),script,kind,branches));
        }
        var next=new ArrayList<String>();
        var waiting=session==null?null:session.waitingAction();
        if(waiting!=null)next.add("当前剧情正在等待："+actionName(waiting.type())+"。关闭查看窗口后继续；序号 "+waiting.sequence());
        if(!state.enabled())next.add("此角色没有开场剧情记录，无法可靠推断主线下一步。不会自动补进度或重置存档。");
        else switch(state.stage()){
            case 0 -> next.add("前往真新镇北出口 (12,1) 或 (13,1)，触发博士开场。");
            case 1 -> next.add("继续博士护送，进入大木博士研究所 (4,3)，完成研究所开场对话。");
            case 2 -> next.add("在大木博士研究所与初始精灵球 NPC #4、#5 或 #6 交互，选择初始宝可梦。");
            case 3 -> next.add("向研究所出口 (5～7,8) 行走，触发劲敌对战；与博士交谈也可触发。");
            case 4,5 -> next.add(switch(parcelPhase) {
                case "DELIVER" -> OakParcel.hasParcel(db,owner)?"携带大木的包裹返回真新镇研究所，与博士交谈。":"包裹不在主背包，返回常磐市商店补领。";
                case "POKEDEX" -> "回大木博士研究所继续领取图鉴和五个精灵球；重新进入研究所可恢复。";
                default -> "前往常磐市商店，领取交给大木博士的包裹。";
            });
            case 6 -> {
                var progress=KantoMainline.progress(db,owner);
                if(progress.champion())next.add("关都冠军标记已保存，可进入名人堂继续收尾。");
                else {
                    next.add("主线已接入至冠军；以下为存档条件已满足的剧情入口（仍需前往对应位置）。");
                    var available=new LinkedHashSet<String>();
                    for(var chapter:KantoMainline.PROGRAMS)for(var value:chapter.getAsJsonArray("triggers")) {
                        var t=value.getAsJsonObject();boolean eligible=true;
                        for(var c:t.getAsJsonArray("conditions"))eligible &= liveCondition(db,owner,c.getAsJsonObject(),state,gender);
                        if(eligible)available.add(t.get("map").getAsString()+" → "+t.get("startNode").getAsString());
                    }
                    next.addAll(available.stream().limit(12).toList());
                }
            }
            default -> next.add("当前阶段超出已接入开场范围，尚不能推断可靠的下一步。");
        }
        if(p.bank()==3&&p.map()==0){
            if(girl.scene()==0)next.add(girl.ready()?"女孩支线：在镇口 (12,2) 与她交谈，或走到 (13,2) 触发展示。":girl.aside()?"女孩支线：面向 (5,14) 提示牌交互阅读。":"女孩支线：与提示牌前的女孩交谈，让她让路。");
            else if(girl.scene()==1)next.add("女孩支线：读牌状态已保存，重新进入真新镇后切换为后续对话。");
            else next.add("女孩支线：读牌教学已完成。");
        }
        String summary=state.enabled()?"开场阶段="+state.stage()+"，初始选择="+(state.starter()==3?"未选择":state.starter()):"旧角色：没有开场记录";
        if(state.enabled()&&state.stage()>=6){var progress=KantoMainline.progress(db,owner);summary+="，徽章="+Integer.bitCount(progress.badges())+"/8，四天王阶段="+progress.elite()+"，冠军="+(progress.champion()?"是":"否");}
        return new Report(region,p.bank(),p.map(),key,summary,next,result);
    }
    private static boolean mainlineNpc(String map,int npc){
        return KantoMainline.PROGRAMS.stream().anyMatch(p->p.getAsJsonArray("triggers").asList().stream().map(JsonElement::getAsJsonObject).anyMatch(t->t.get("type").getAsString().equals("NPC")&&t.get("map").getAsString().equals(map)&&t.get("entityIdx").getAsInt()==npc));
    }
    private static boolean liveCondition(Database db,String owner,JsonObject c,StoryData.State state,int gender){
        return Set.of("STORY_STAGE_EQUALS","STORY_STAGE_AT_LEAST","STARTER_EQUALS","SEX_EQUALS").contains(c.get("type").getAsString())?matches(c,state,gender):KantoMainline.condition(db,owner,c);
    }
    private static boolean matches(JsonArray list,StoryData.State state,int gender){if(list==null)return true;for(var c:list)if(!matches(c.getAsJsonObject(),state,gender))return false;return true;}
    private static boolean matches(JsonObject c,StoryData.State s,int gender){int v=c.get("value").getAsInt();return switch(c.get("type").getAsString()){case "STORY_STAGE_EQUALS"->s.stage()==v;case "STORY_STAGE_AT_LEAST"->s.stage()>=v;case "STARTER_EQUALS"->s.starter()==v;case "SEX_EQUALS"->gender==v;default->false;};}
    private static String describe(JsonArray list){if(list==null||list.isEmpty())return "无额外存档条件";var parts=new ArrayList<String>();for(var c:list)parts.add(describe(c.getAsJsonObject()));return String.join("；",parts);}
    private static String describe(JsonObject c){if(c.has("display"))return c.get("display").getAsString();String value=c.has("value")?c.get("value").getAsString():"";return switch(c.get("type").getAsString()){case "PARCEL_NEEDS_PICKUP"->"尚未领取或包裹遗失，可补领";case "PARCEL_PHASE_EQUALS"->"包裹阶段 = "+value;case "STORY_STAGE_EQUALS"->"开场阶段 = "+value;case "STORY_STAGE_AT_LEAST"->"开场阶段 ≥ "+value;case "STARTER_EQUALS"->value.equals("3")?"尚未选择初始宝可梦":"初始选择 = "+value;case "SEX_EQUALS"->value.equals("0")?"男角色":"女角色";default->c.toString();};}
    private static void walk(JsonObject program,String node,String name,String condition,boolean eligible,boolean choice,int npc,StoryData.State state,int gender,List<Branch> out,List<Line> lines,List<String> steps,Set<String> path){
        if(out.size()>=64||!path.add(node))return;
        var nextLines=new ArrayList<>(lines);var nextSteps=new ArrayList<>(steps);
        for(var value:program.getAsJsonObject("nodes").getAsJsonObject(node).getAsJsonArray("actions")){
            var a=value.getAsJsonObject();String type=a.get("type").getAsString();
            if(type.equals("BRANCH")){
                var c=a.getAsJsonObject("condition");boolean yes=matches(c,state,gender);
                walk(program,a.get("yes").getAsString(),name+" / 是",condition+"；"+describe(c),eligible&&yes,choice,npc,state,gender,out,nextLines,nextSteps,new HashSet<>(path));
                walk(program,a.get("no").getAsString(),name+" / 否",condition+"；不满足（"+describe(c)+"）",eligible&&!yes,choice,npc,state,gender,out,nextLines,nextSteps,new HashSet<>(path));return;
            }
            if(type.equals("GOTO")){walk(program,a.get("node").getAsString(),name,condition,eligible,choice,npc,state,gender,out,nextLines,nextSteps,new HashSet<>(path));return;}
            if(type.equals("DIALOGUE")||type.equals("YES_NO"))nextLines.add(a.has("text")?new Line(program.getAsJsonObject("text").get(a.get("text").getAsString()).getAsInt(),0):new Line(0,0,a.get("message").getAsString()));
            if(type.equals("YES_NO")){
                walk(program,a.get("yes").getAsString(),name+" / 选择是",condition+"；选择是",eligible,true,npc,state,gender,out,nextLines,nextSteps,new HashSet<>(path));
                walk(program,a.get("no").getAsString(),name+" / 选择否",condition+"；选择否",eligible,true,npc,state,gender,out,nextLines,nextSteps,new HashSet<>(path));return;
            }
            if(type.equals("SELECT_STARTER")){
                int selected=npc==4?0:npc==5?2:1;
                for(var v:program.getAsJsonObject("resources").getAsJsonArray("starters")){var starter=v.getAsJsonObject();if(starter.get("choice").getAsInt()==selected)nextLines.add(new Line(starter.get("prompt").getAsInt(),starter.get("species").getAsInt()));}
                var reject=new ArrayList<>(nextSteps);reject.add("取消选择，关闭对话");out.add(new Branch(name+" / 取消",condition,eligible?"等待玩家选择":"条件未满足",nextLines,reject));
                nextSteps.add("确认选择初始宝可梦（仅预览，不领取）");walk(program,a.get("next").getAsString(),name+" / 确认",condition,eligible,true,npc,state,gender,out,nextLines,nextSteps,new HashSet<>(path));return;
            }
            if(!Set.of("DIALOGUE","CLOSE_SCENE","CLOSE_DIALOG","NOTIFY").contains(type))nextSteps.add(actionDescription(a));
        }
        out.add(new Branch(name,condition,eligible?(choice?"等待玩家选择":"当前匹配"):"条件未满足",nextLines,nextSteps));
    }
    private static String actionDescription(JsonObject a){String type=a.get("type").getAsString();return switch(type){case "SET_EVENT_FLAG"->"保存事件标记："+a.get("flag").getAsString();case "HIDE_TEMP_NPC"->"清除当前地图障碍，重新进入地图后恢复";case "START_TRAINER_BATTLE"->"剧情训练家战："+a.get("battleId")+"；胜后 → "+a.get("finishNode");case "GRANT_ITEM"->"获得道具 "+a.get("itemId")+" × "+a.get("amount");case "SET_BADGE"->"获得徽章位 "+a.get("bit");case "SET_STORY_BIT","SET_EXTRA_STORY_BIT"->type+" "+a.get("bit");case "SET_CHAMPION"->"保存冠军标记";case "PARCEL_OPERATION"->"包裹事务："+a.get("operation").getAsString();case "MOVE_NPC"->"NPC 移动至 "+a.get("target");case "MOVE_PLAYER"->"玩家移动至 ("+a.get("x")+","+a.get("y")+")";case "SET_OPENING_STAGE"->"剧情阶段 "+a.get("expected")+" → "+a.get("nextStage");case "WAIT"->"等待 "+a.get("duration")+"ms";case "NPC_REACTION"->"显示惊叹号";case "FACE_NPC"->"NPC 转向 "+a.get("toward");case "SIGN_LADY_ASIDE"->"记录本次地图已让路";case "SIGN_LADY_READ"->"记录已读提示牌";default->actionName(type);};}
    private static String actionName(String type){return switch(type){case "DIALOGUE"->"对话确认";case "SELECT_STARTER"->"选择初始宝可梦";case "YES_NO"->"是／否选择";case "START_BATTLE"->"开始对战";case "HEAL_PARTY"->"恢复队伍";case "PLAY_MUSIC"->"播放音乐";case "GUIDE"->"NPC 护送玩家";case "CHANGE_MAP"->"切换地图";case "FACE_PLAYER"->"玩家转向";default->type;};}
}
