package top.totodile.pokemmo;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** 新进程等待旧客户端完成资源释放和退出后再初始化。 */
public final class ClientRestart {
    private static String[] arguments = new String[0];
    private static boolean pending;

    private ClientRestart() {
    }

    static void initialize(String[] args) {
        arguments = args.clone();
    }

    public static void request() throws IOException {
        if (pending) return;
        Path log = ClientFiles.data("../config/client-restart.log").toAbsolutePath().normalize();
        Files.createDirectories(log.getParent());
        String executable = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("Windows") ? "javaw.exe" : "java").toString();
        List<String> command = new ArrayList<>();
        command.add(executable);
        for (String option : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            // IDE 注入的代理及调试端口只属于当前进程，不能由新进程复用。
            if (!option.startsWith("-agentlib:") && !option.startsWith("-agentpath:")
                    && !option.startsWith("-javaagent:") && !option.startsWith("-Xrunjdwp:")
                    && !option.equals("-Xdebug")) command.add(option);
        }
        command.addAll(List.of("-cp", System.getProperty("java.class.path"), ClientRestart.class.getName(),
                Long.toString(ProcessHandle.current().pid())));
        command.addAll(Arrays.asList(arguments));
        new ProcessBuilder(command).directory(Path.of(System.getProperty("user.dir")).toFile())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.appendTo(log.toFile())).start();
        pending = true;
    }

    static boolean pending() {
        return pending;
    }

    public static void main(String[] args) throws Exception {
        var parent = ProcessHandle.of(Long.parseLong(args[0]));
        if (parent.isPresent()) parent.get().onExit().get(60, TimeUnit.SECONDS);
        Main.main(Arrays.copyOfRange(args, 1, args.length));
    }
}
