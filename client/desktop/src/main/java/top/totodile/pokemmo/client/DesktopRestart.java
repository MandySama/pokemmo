package top.totodile.pokemmo.client;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/** 对齐参考 Pd0：启动同一客户端，再退出当前进程；失败时保留当前界面。 */
final class DesktopRestart {
    private DesktopRestart() {}

    static void restart() {
        var command = new ArrayList<String>();
        String packaged = System.getProperty("jpackage.app-path");
        if (packaged != null && !packaged.isBlank()) command.add(packaged);
        else {
            Path bin = Path.of(System.getProperty("java.home"), "bin");
            Path java =
                    bin.resolve(
                            System.getProperty("os.name").startsWith("Windows")
                                    ? "javaw.exe"
                                    : "java");
            if (!Files.isRegularFile(java)) java = bin.resolve("java.exe");
            command.add(java.toString());
            for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments())
                if (!argument.startsWith("-agentlib:")
                        && !argument.startsWith("-javaagent:")
                        && !argument.startsWith("-Xrunjdwp")) command.add(argument);
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add(DesktopLauncher.class.getName());
        }
        try {
            new ProcessBuilder(command).start();
        } catch (IOException error) {
            throw new IllegalStateException("Unable to restart client", error);
        }
        Gdx.app.exit();
    }
}
