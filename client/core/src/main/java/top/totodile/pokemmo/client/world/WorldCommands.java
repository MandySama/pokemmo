package top.totodile.pokemmo.client.world;

/** 仅作用于本地地图的调试命令。timescale 沿用参考控制台的命名。 */
public final class WorldCommands {
    private final PlayerMotion motion;
    public WorldCommands(PlayerMotion motion) { this.motion=motion; }
    public String execute(String line) {
        String command=line.trim(); if(command.startsWith("/")) command=command.substring(1);
        String[] args=command.split("\\s+");
        try {
            switch(args[0].toLowerCase(java.util.Locale.ROOT)) {
                case "noclip":
                    if(args.length>2 || args.length==2 && !args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))
                        return "用法: noclip [on|off]";
                    motion.movement().setNoClip(args.length==1?!motion.movement().noClip():args[1].equalsIgnoreCase("on"));
                    return "穿墙: "+(motion.movement().noClip()?"开启":"关闭");
                case "speed", "timescale":
                    if(args.length!=2) return "用法: speed <倍率>，例如 speed 2；speed 1 恢复正常。";
                    motion.setSpeed(Float.parseFloat(args[1]));
                    return "地图移动倍率: "+motion.speed();
                default: return "可用指令: noclip [on|off]、speed <倍率>、timescale <倍率>";
            }
        } catch(NumberFormatException invalid) { return "倍率格式错误。"; }
        catch(IllegalArgumentException | IllegalStateException invalid) { return invalid.getMessage(); }
    }
}
