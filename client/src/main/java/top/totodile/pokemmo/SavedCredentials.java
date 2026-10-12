package top.totodile.pokemmo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/** 仅持久化服务端登录凭据，不保存密码或密码摘要。 */
public final class SavedCredentials {
    public record Entry(String username, byte[] key) {
    }

    private final Path file;
    private final List<Entry> entries = new ArrayList<>();
    private String lastUsername = "";

    public SavedCredentials(Path file) throws IOException {
        this.file = file;
        if (!Files.isRegularFile(file)) return;
        Properties values = new Properties();
        try (var input = Files.newInputStream(file)) {
            values.load(input);
        }
        lastUsername = values.getProperty("client.saved_credentials.lastusername", "");
        String[] names = values.getProperty("client.saved_credentials.usernames", "").split(",");
        String[] keys = values.getProperty("client.saved_credentials.keys", "").split(",");
        for (int i = 0; i < Math.min(names.length, keys.length); i++) {
            try {
                byte[] key = Base64.getDecoder().decode(keys[i]);
                if (!names[i].isBlank() && key.length > 0 && key.length <= 255) entries.add(new Entry(names[i], key));
            } catch (IllegalArgumentException ignored) {
                // 损坏的单条凭据不影响其他账号。
            }
        }
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public String lastUsername() {
        return lastUsername;
    }

    public void save(String username, byte[] key) throws IOException {
        if (username.contains(",")) throw new IOException("Invalid saved username");
        List<Entry> next = new ArrayList<>(entries);
        next.removeIf(entry -> entry.username().equalsIgnoreCase(username));
        next.add(new Entry(username, key.clone()));
        write(next, username);
    }

    public void select(String username) throws IOException {
        write(entries, username);
    }

    public void remove(String username) throws IOException {
        List<Entry> next = new ArrayList<>(entries);
        next.removeIf(entry -> entry.username().equalsIgnoreCase(username));
        write(next, "");
    }

    private void write(List<Entry> next, String selected) throws IOException {
        Properties values = new Properties();
        values.setProperty("client.saved_credentials.usernames", String.join(",", next.stream().map(Entry::username).toList()));
        values.setProperty("client.saved_credentials.keys", String.join(",", next.stream()
                .map(entry -> Base64.getEncoder().encodeToString(entry.key())).toList()));
        values.setProperty("client.saved_credentials.lastusername", selected);
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temporary = Files.createTempFile(file.toAbsolutePath().getParent(), "credentials-", ".tmp");
        try {
            try (var output = Files.newOutputStream(temporary)) {
                values.store(output, null);
            }
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            List<Entry> copy = new ArrayList<>(next);
            entries.clear();
            entries.addAll(copy);
            lastUsername = selected;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
