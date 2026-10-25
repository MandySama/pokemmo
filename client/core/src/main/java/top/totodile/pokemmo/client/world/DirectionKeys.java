package top.totodile.pokemmo.client.world;

import java.util.LinkedHashMap;
import java.util.Map;

/** 对应参考 GlfwDebugConsole 的方向栈：最后按下优先，释放后恢复仍按住的方向。 */
final class DirectionKeys {
    private final Map<Integer, Integer> held = new LinkedHashMap<>();

    void press(int key, int direction) {
        // 系统自动重复不是一次新按键，不能抢回已让给其它方向的优先级。
        held.putIfAbsent(key, direction);
    }

    boolean release(int key) {
        return held.remove(key) != null;
    }

    void clear() {
        held.clear();
    }

    int direction() {
        int last = -1;
        for (int direction : held.values()) last = direction;
        return last;
    }
}
