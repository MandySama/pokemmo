package top.totodile.pokemmo.client.world;

import java.util.function.Consumer;
import top.totodile.pokemmo.server.game.GameService.PokedexState;

/** 仅作用于本地地图的调试命令。timescale 沿用参考控制台的命名。 */
public final class WorldCommands {
    private final PlayerMotion motion;
    private final Consumer<PokedexState> savePokedex;
    private boolean pokedexUnlocked;
    private Runnable openPc;
    public void onPc(Runnable action){openPc=action;}
    public WorldCommands(PlayerMotion motion) { this(motion,PokedexState.defaults(),state -> {}); }
    public WorldCommands(PlayerMotion motion,PokedexState state,Consumer<PokedexState> savePokedex) {
        this.motion=motion;this.pokedexUnlocked=state.unlocked();this.savePokedex=savePokedex;
    }
    public boolean pokedexUnlocked(){return pokedexUnlocked;}
    public String execute(String line) {
        String command=line.trim().replaceFirst("^/+","");
        String[] args=command.split("\\s+");
        try {
            switch(args[0].toLowerCase(java.util.Locale.ROOT)) {
                case "pc":
                    if(args.length!=1)return "Usage: //pc";
                    if(openPc==null)return "Unable to open the PC.";
                    openPc.run();return "PC opened.";
                case "pokedex":
                    if(args.length>2||args.length==2&&!args[1].equalsIgnoreCase("on")&&!args[1].equalsIgnoreCase("off"))
                        return "用法: pokedex [on|off]";
                    boolean unlocked=args.length==1?!pokedexUnlocked:args[1].equalsIgnoreCase("on");
                    if(unlocked!=pokedexUnlocked){
                        savePokedex.accept(new PokedexState(unlocked));
                        pokedexUnlocked=unlocked;
                    }
                    return "图鉴: "+(pokedexUnlocked?"全解锁":"全零");
                case "noclip":
                    if(args.length>2 || args.length==2 && !args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))
                        return "用法: noclip [on|off]";
                    motion.movement().setNoClip(args.length==1?!motion.movement().noClip():args[1].equalsIgnoreCase("on"));
                    return "穿墙: "+(motion.movement().noClip()?"开启":"关闭");
                case "speed", "timescale":
                    if(args.length!=2) return "用法: speed <倍率>，例如 speed 2；speed 1 恢复正常。";
                    motion.setSpeed(Float.parseFloat(args[1]));
                    return "地图移动倍率: "+motion.speed();
                default: return "可用指令: noclip [on|off]、speed <倍率>、timescale <倍率>、pokedex [on|off]";
            }
        } catch(NumberFormatException invalid) { return "倍率格式错误。"; }
        catch(IllegalArgumentException | IllegalStateException invalid) { return invalid.getMessage(); }
    }
}
