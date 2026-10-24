package top.totodile.pokemmo.client.world;

import java.util.Map;
import top.totodile.pokemmo.client.config.ClientSettings;
import top.totodile.pokemmo.server.game.GameService.TravelState;
import java.util.function.Consumer;

/** 跑步沿用参考客户端配置；穿墙、倍率、骑车属于当前角色存档。 */
final class WorldState {
    private static final String RUNNING="client.gameplay.autosprint.enabled";
    static void bind(ClientSettings settings,PlayerMotion motion,TravelState state,Consumer<TravelState> saveCharacter) {
        if(motion.running()!=Boolean.parseBoolean(settings.get(RUNNING,"true")))motion.toggleRunning();
        if(motion.bicycle()!=state.bicycle())motion.toggleBicycle();
        // 穿墙状态按角色存档恢复，与角色实际坐标一同保留。
        boolean noClip=state.noClip();
        if(motion.movement().noClip()!=noClip)motion.movement().setNoClip(noClip);
        motion.setSpeed(state.speed());
        TravelState[] previous={state};
        Runnable save=() -> {
            TravelState next=new TravelState(motion.movement().noClip(),motion.speed(),motion.bicycle());
            if(!next.equals(previous[0])) {saveCharacter.accept(next);previous[0]=next;}
        };
        motion.onStateChanged(() -> {
            String running=Boolean.toString(motion.running());
            if(!running.equals(settings.get(RUNNING,"true")))settings.save(Map.of(RUNNING,running));
            save.run();
        });
        motion.movement().onStateChanged(save);
    }
    private WorldState() {}
}
