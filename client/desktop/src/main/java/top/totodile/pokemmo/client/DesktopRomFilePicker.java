package top.totodile.pokemmo.client;

import top.totodile.pokemmo.client.rom.RomFilePicker;


import java.awt.Desktop;
import java.nio.file.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

final class DesktopRomFilePicker implements RomFilePicker {
    @Override public void choose(String extension, Consumer<Source> selected, Consumer<String> failed) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFileChooser chooser=new JFileChooser(Path.of(ClientAssets.configDirectory()).resolveSibling("roms").toFile());
                chooser.setDialogTitle("选择 ROM");
                chooser.setFileFilter(new FileNameExtensionFilter(extension.toUpperCase()+" ROM",extension));
                if (chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
                    Path path=chooser.getSelectedFile().toPath();
                    selected.accept(() -> Files.newInputStream(path));
                } else selected.accept(null);
            } catch (Exception error) { failed.accept("无法选择 ROM: "+error.getMessage()); }
        });
    }
    @Override public void openDirectory(String path, Consumer<String> failed) {
        SwingUtilities.invokeLater(() -> {
            try { Desktop.getDesktop().open(Path.of(path).toFile()); }
            catch (Exception error) { failed.accept("无法打开 ROM 文件夹: "+error.getMessage()); }
        });
    }
}
