package top.totodile.pokemmo.client;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import top.totodile.pokemmo.client.rom.RomFilePicker;

import java.awt.Desktop;
import java.nio.file.*;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

final class DesktopRomFilePicker implements RomFilePicker {
    @Override
    public void choose(String extension, Consumer<Source> selected, Consumer<String> failed) {
        // 参考 mJ(callback)：ROM 选择未传扩展名，原生窗口使用空标题、空默认路径和全部文件。
        String file;
        boolean mod = extension.equals("mod,zip");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (org.lwjgl.system.Platform.get() == org.lwjgl.system.Platform.WINDOWS) {
                // tinyfd 3.3.6 会绑定 GetForegroundWindow，位置随游戏窗口移动。
                file = WindowsFileDialog.open(mod);
            } else {
                PointerBuffer filters =
                        mod ? stack.pointers(stack.UTF8("*.mod"), stack.UTF8("*.zip")) : null;
                file =
                        TinyFileDialogs.tinyfd_openFileDialog(
                                "", "", filters, mod ? "mod, zip" : "", false);
            }
        } catch (RuntimeException | LinkageError unavailable) {
            java.util.logging.Logger.getLogger(DesktopRomFilePicker.class.getName())
                    .log(
                            java.util.logging.Level.WARNING,
                            "Native file dialog unavailable; using Swing fallback",
                            unavailable);
            chooseFallback(mod, selected, failed);
            return;
        }
        selected.accept(file == null ? null : source(Path.of(file)));
    }

    private static Source source(Path path) {
        return new Source() {
            @Override
            public java.io.InputStream open() throws java.io.IOException {
                return Files.newInputStream(path);
            }

            @Override
            public Path path() {
                return path;
            }
        };
    }

    /** 与参考一致：仅在原生选择器不可用时退回 Swing。 */
    private static void chooseFallback(
            boolean mod, Consumer<Source> selected, Consumer<String> failed) {
        SwingUtilities.invokeLater(
                () -> {
                    try {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setCurrentDirectory(new java.io.File("."));
                        chooser.setSelectedFile(new java.io.File(""));
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        if (mod)
                            chooser.setFileFilter(
                                    new FileNameExtensionFilter("mod, zip", "mod", "zip"));
                        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                            Path path = chooser.getSelectedFile().toPath();
                            selected.accept(source(path));
                        } else selected.accept(null);
                    } catch (Exception error) {
                        failed.accept("无法选择 ROM: " + error.getMessage());
                    }
                });
    }

    @Override
    public void openDirectory(String path, Consumer<String> failed) {
        SwingUtilities.invokeLater(
                () -> {
                    try {
                        Desktop.getDesktop().open(Path.of(path).toFile());
                    } catch (Exception error) {
                        failed.accept("无法打开 ROM 文件夹: " + error.getMessage());
                    }
                });
    }
}
