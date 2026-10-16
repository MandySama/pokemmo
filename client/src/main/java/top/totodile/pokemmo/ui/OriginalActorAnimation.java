package top.totodile.pokemmo.ui;

import top.totodile.pokemmo.battle.MoveAnimations;
import top.totodile.pokemmo.battle.MovePresentation;
import java.lang.reflect.*;

/** 原时间轴和属性访问器维护角色状态；重复播放重新创建 CPU 实例。 */
final class OriginalActorAnimation implements AutoCloseable {
    private static final Constructor<?> CONSTRUCTOR;
    private static final Method SAMPLE;
    private static final Method CLOSE;
    static {
        try {
            var type = OriginalBattleParticles.runtimeClass("top.totodile.particlebridge.PoseBridge");
            CONSTRUCTOR = type.getConstructor(String[].class, long[].class, float[][].class);
            SAMPLE = type.getMethod("sample", float.class);
            CLOSE = type.getMethod("close");
        } catch (ReflectiveOperationException error) { throw new ExceptionInInitializerError(error); }
    }
    private final MoveAnimations.Animation animation;
    private Object runtime;
    private long previous = -1;

    OriginalActorAnimation(MoveAnimations.Animation animation) { this.animation = animation; }
    int move() { return animation.id(); }

    MovePresentation.Actor[] sample(long milliseconds) {
        try {
            if (runtime == null || milliseconds < previous) {
                close();
                var commands = animation.commands();
                String[] operations = new String[commands.size()];
                long[] starts = new long[commands.size()];
                float[][] arguments = new float[commands.size()][];
                for (int i = 0; i < commands.size(); i++) {
                    var command = commands.get(i);
                    operations[i] = command.operation();
                    starts[i] = command.start();
                    arguments[i] = new float[command.arguments().size()];
                    for (int j = 0; j < arguments[i].length; j++) arguments[i][j] = command.arguments().get(j);
                }
                runtime = CONSTRUCTOR.newInstance(operations, starts, arguments);
            }
            previous = milliseconds;
            float[][] states = (float[][]) SAMPLE.invoke(runtime, milliseconds / 1000f);
            MovePresentation.Actor[] result = new MovePresentation.Actor[2];
            for (int i = 0; i < result.length; i++) {
                float[] state = states[i];
                result[i] = new MovePresentation.Actor(state[0], state[1], state[3], state[4], 1,
                        state[5], state[6], state[7], state[8], state[2], state[9]);
            }
            return result;
        } catch (ReflectiveOperationException error) {
            Throwable cause = error instanceof InvocationTargetException invocation ? invocation.getCause() : error;
            throw new IllegalStateException("Unable to sample move actors " + animation.id(), cause);
        }
    }

    @Override public void close() {
        if (runtime == null) return;
        try { CLOSE.invoke(runtime); }
        catch (ReflectiveOperationException error) { throw new IllegalStateException("Unable to release move actors", error); }
        finally { runtime = null; }
    }
}
