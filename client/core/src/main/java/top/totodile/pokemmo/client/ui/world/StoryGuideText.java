package top.totodile.pokemmo.client.ui.world;

import com.badlogic.gdx.utils.JsonReader;

/** Presentation of existing script conditions; never evaluates or changes story state. */
final class StoryGuideText {
    private StoryGuideText(){}
    static String condition(String json){
        try{
            var c=new JsonReader().parse(json);
            String type=c.getString("type"),v=c.has("value")?c.getString("value"):"";
            String flag=c.has("flag")?c.getString("flag"):"";
            return switch(type){
                case "HAS_BATTLE_POKEMON"->"队伍中至少有一只可战斗的宝可梦";
                case "HAS_CAPTURE_BALL"->"背包中有可用的精灵球";
                case "NO_CAPTURE_BALL"->"背包中没有可用的精灵球";
                case "CAPTURE_TUTORIAL_COMPLETE"->"已经完成捕捉教学";
                case "CAPTURE_TUTORIAL_INCOMPLETE"->"尚未完成捕捉教学";
                case "HAS_ITEM"->"持有所需道具（编号 "+v+"）";
                case "NO_ITEM"->"尚未持有所需道具（编号 "+v+"）";
                case "BADGE_SET"->"已获得"+badge(v);
                case "BADGE_CLEAR"->"尚未获得"+badge(v);
                case "CHAMPION_SET"->"已成为冠军";
                case "CHAMPION_CLEAR"->"尚未成为冠军";
                case "ELITE_STAGE_EQUALS"->"联盟连续挑战进度为第 "+v+" 阶段";
                case "STORY_BIT_SET"->"已完成对应主线前置（标记 "+v+"）";
                case "STORY_BIT_CLEAR"->"尚未完成对应主线事件（标记 "+v+"）";
                case "EXTRA_STORY_BIT_SET"->"已完成对应支线前置（标记 "+v+"）";
                case "EXTRA_STORY_BIT_CLEAR"->"尚未完成对应支线事件（标记 "+v+"）";
                case "EVENT_FLAG_SET","HOENN_FLAG_SET"->"已完成前置事件："+event(flag);
                case "EVENT_FLAG_CLEAR","HOENN_FLAG_CLEAR"->"尚未完成事件："+event(flag);
                case "STORY_STAGE_EQUALS"->"剧情进度处于第 "+v+" 阶段";
                case "STORY_STAGE_AT_LEAST"->"剧情进度至少达到第 "+v+" 阶段";
                case "STORY_STAGE_BELOW"->"剧情进度尚未达到第 "+v+" 阶段";
                case "KANTO_OWNED_AT_LEAST"->"已拥有至少 "+v+" 种关都宝可梦";
                case "NOT_IN_BATTLE"->"当前没有进行对战";
                case "NOT_IN_TRADE"->"当前没有进行交易";
                case "INTERACTION_IDLE"->"当前没有进行其他交互";
                default->"需满足额外剧情条件（展开技术信息查看原始条件）";
            };
        }catch(RuntimeException invalid){return "该条件暂时无法转换为指引，请查看技术信息";}
    }
    private static String badge(String value){try{return "第 "+(Integer.parseInt(value)+1)+" 枚道馆徽章";}catch(NumberFormatException invalid){return "对应道馆徽章";}}
    static String event(String flag){
        String last=flag.substring(flag.lastIndexOf('/')+1);
        String title=switch(last){
            case "FLAG_DEFEATED_ROCKETS_IN_WAREHOUSE"->"击败火箭队仓库干部";
            case "FLAG_GOT_MOON_STONE_FROM_JOYFUL_GAME_CORNER"->"向洛丝丽父亲交付陨石并领取月之石";
            case "FLAG_GOT_RUBY"->"取得红宝石";case "FLAG_GOT_TEA"->"取得茶";
            case "FLAG_HIDE_CERULEAN_CAVE_GUARD"->"华蓝洞窟守卫：隐藏";
            case "FLAG_HIDE_FIVE_ISLAND_ROCKETS"->"五之岛火箭队成员：隐藏";
            case "FLAG_HIDE_ICEFALL_CAVE_LORELEI"->"冻瀑洞窟科拿：隐藏";
            case "FLAG_HIDE_ICEFALL_CAVE_ROCKETS"->"冻瀑洞窟火箭队成员：隐藏";
            case "FLAG_HIDE_LORELEI_IN_HER_HOUSE"->"科拿家中的科拿：隐藏";
            case "FLAG_HIDE_LOSTELLE_IN_BERRY_FOREST"->"树果森林中的洛丝丽：隐藏";
            case "FLAG_HIDE_MEWTWO"->"华蓝洞窟超梦：隐藏";
            case "FLAG_HIDE_ROUTE_12_SNORLAX"->"12号道路卡比兽：隐藏";
            case "FLAG_HIDE_ROUTE_16_SNORLAX"->"16号道路卡比兽：隐藏";
            case "FLAG_HIDE_RUBY"->"红宝石拾取点：隐藏";
            case "FLAG_HIDE_SAPPHIRE"->"蓝宝石拾取点：隐藏";
            case "FLAG_HIDE_RUIN_VALLEY_SCIENTIST"->"遗迹山谷研究员：隐藏";
            case "FLAG_HIDE_THREE_ISLAND_BIKERS"->"三之岛暴走族群体：隐藏";
            case "FLAG_HIDE_THREE_ISLAND_LONE_BIKER"->"三之岛单独的暴走族：隐藏";
            case "FLAG_LEARNED_GOLDEEN_NEED_LOG"->"获知火箭队仓库第一段密码";
            case "FLAG_LEARNED_YES_NAH_CHANSEY"->"获知火箭队仓库第二段密码";
            case "FLAG_RECOVERED_SAPPHIRE"->"夺回蓝宝石";
            case "FLAG_RESCUED_LOSTELLE"->"救出洛丝丽";
            case "FLAG_SEVII_DETOUR_FINISHED"->"完成七岛初次旅行";
            case "FLAG_SYS_CAN_LINK_WITH_RS"->"解锁与丰缘的通讯";
            case "FLAG_SYS_NATIONAL_DEX"->"解锁全国图鉴";
            case "FLAG_UNLOCKED_ROCKET_WAREHOUSE"->"解锁火箭队仓库";
            case "FLAG_USED_CUT_ON_RUIN_VALLEY_BRAILLE"->"对遗迹山谷盲文门使用居合斩";
            case "FLAG_WORLD_MAP_ONE_ISLAND"->"发现一之岛";
            case "CeladonCity_Condominiums_1F_EventScript_TeaWoman"->"玉虹公寓老奶奶赠茶";
            case "marowak_appeased"->"安抚嘎啦嘎啦";
            case "saffron_guards"->"向金黄市守卫交茶";
            case "letter"->"交付给大吾的信";
            case "running_shoe_status"->"已领取跑步鞋";
            case "vermilion_locks"->"解除枯叶道馆电流";
            case "mansion"->"宝可梦屋雕像开关";
            case "bikers_done"->"完成三之岛暴走族事件";
            case "bill_departed"->"正辉已离开";
            case "celio_met"->"已与锦辉见面";
            case "ember_grunt1"->"击败灯火山第一名火箭队成员";
            case "ember_grunt2"->"击败灯火山第二名火箭队成员";
            case "ruby_delivered"->"向锦辉交付红宝石";
            case "ruby_requested"->"接受寻找红宝石的委托";
            case "warehouse_2"->"击败火箭队仓库第二名对手";
            case "warehouse_3"->"击败火箭队仓库第三名对手";
            default->null;
        };
        if(title!=null)return title;
        var door=java.util.regex.Pattern.compile("kanto/middle/door/SilphCo_(\\d+)F_EventScript_Door(\\d*)").matcher(flag);
        if(door.matches())return "西尔佛公司"+door.group(1)+"楼"+(door.group(2).isEmpty()?"":door.group(2)+"号")+"门已打开";
        var floor=java.util.regex.Pattern.compile("kanto/mechanism/VictoryRoad_(\\d+)F_EventScript_FloorSwitch(\\d*)").matcher(flag);
        if(floor.matches())return "冠军之路"+floor.group(1)+"楼"+(floor.group(2).isEmpty()?"":floor.group(2)+"号")+"地板开关";
        if(flag.matches("kanto/mechanism/cinnabar/[1-6]"))return "红莲道馆第"+last+"道问答机关";
        if(flag.matches("kanto/mechanism/current/SeafoamIslands_B[34]F"))return "双子岛地下"+last.charAt(last.length()-2)+"层水流机关";
        if(flag.startsWith("kanto/trainer/CinnabarIsland_Gym/")){
            String position=switch(last){case "CinnabarIsland_Gym_EventScript_Quinn"->"25,11";case "CinnabarIsland_Gym_EventScript_Avery"->"17,5";case "CinnabarIsland_Gym_EventScript_Ramon"->"16,11";case "CinnabarIsland_Gym_EventScript_Derek"->"16,18";case "CinnabarIsland_Gym_EventScript_Dusty"->"4,19";case "CinnabarIsland_Gym_EventScript_Zac"->"4,11";default->null;};
            if(position!=null)return "击败红莲道馆训练家（"+position+"）";
        }
        if(flag.equals("kanto/trainer/RocketHideout_B4F/RocketHideout_B4F_EventScript_Grunt1"))return "击败火箭队基地地下4层成员（4,2）";
        return "未整理的剧情事件";
    }
}
