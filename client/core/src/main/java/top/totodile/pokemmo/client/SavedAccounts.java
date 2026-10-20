package top.totodile.pokemmo.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** 本地保存账号；密码加密存放，安装密钥也保存在应用本地配置中。 */
final class SavedAccounts {
    private static final String PREFIX = "account.";
    private final Properties preferences = new Properties();
    private final FileHandle file;
    private final LinkedHashMap<String, String> accounts = new LinkedHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final OutputFactory outputFactory;
    private final Properties committedPreferences = new Properties();
    private final LinkedHashMap<String, String> committedAccounts = new LinkedHashMap<>();

    @FunctionalInterface
    interface OutputFactory { FileOutputStream open(java.io.File file) throws IOException; }

    static SavedAccounts open() {
        return open(Gdx.files.local("config"));
    }

    static SavedAccounts open(FileHandle directory) {
        return new SavedAccounts(directory.child("savedcredentials.properties"));
    }

    SavedAccounts(FileHandle file) {
        this(file, FileOutputStream::new);
    }

    SavedAccounts(FileHandle file, OutputFactory outputFactory) {
        this.file = file;
        this.outputFactory = outputFactory;
        if (file.exists()) {
            try (InputStream input = file.read()) {
                preferences.load(input);
            } catch (IOException error) { throw new IllegalStateException("读取已存账号失败", error); }
        }
        for (String key : preferences.getProperty("order", "").split(",")) {
            if (!key.startsWith(PREFIX)) continue;
            if (!preferences.containsKey(key)) continue;
            try {
                String name = new String(Base64.getUrlDecoder().decode(key.substring(PREFIX.length())), StandardCharsets.UTF_8);
                String encrypted = preferences.getProperty(key);
                // 损坏的记录不进入下拉列表，也不将其当成有效的已存密码。
                if (!name.isBlank() && !decrypt(encrypted, name).isEmpty()) accounts.put(name, encrypted);
            } catch (RuntimeException | GeneralSecurityException ignored) { }
        }
        rememberCommittedState();
    }

    List<String> names() { return new ArrayList<>(accounts.keySet()); }
    String selected() {
        String selected = find(preferences.getProperty("selected", ""));
        return selected != null ? selected : accounts.keySet().stream().findFirst().orElse(null);
    }
    private String find(String name) {
        return accounts.keySet().stream().filter(value -> value.equalsIgnoreCase(name)).findFirst().orElse(null);
    }
    boolean contains(String name) { return find(name) != null; }

    String password(String name) {
        String stored = find(name);
        if (stored == null) return null;
        try { return decrypt(accounts.get(stored), stored); }
        catch (GeneralSecurityException | RuntimeException error) { throw new IllegalStateException("读取已存密码失败", error); }
    }

    void save(String name, String password) {
        if (name.isBlank() || password.isEmpty()) throw new IllegalArgumentException("账号和密码不能为空");
        try {
            byte[] key;
            String encoded = preferences.getProperty("key", "");
            if (encoded.isEmpty()) {
                key = new byte[32];
                random.nextBytes(key);
                preferences.setProperty("key", Base64.getEncoder().encodeToString(key));
            } else key = Base64.getDecoder().decode(encoded);
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            cipher.updateAAD(name.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
            String encrypted = Base64.getEncoder().encodeToString(iv) + ":"
                    + Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
            String previous = find(name);
            if (previous != null) {
                accounts.remove(previous);
                preferences.remove(key(previous));
            }
            accounts.put(name, encrypted);
            preferences.setProperty(key(name), encrypted);
            preferences.setProperty("selected", name);
            flush();
        } catch (GeneralSecurityException error) {
            restoreCommittedState();
            throw new IllegalStateException("保存账号失败", error);
        }
    }

    void select(String name) {
        if (contains(name)) {
            preferences.setProperty("selected", find(name));
            flush();
        }
    }

    void remove(String name) {
        String stored = find(name);
        if (stored == null) return;
        accounts.remove(stored);
        preferences.remove(key(stored));
        preferences.setProperty("selected", accounts.keySet().stream().findFirst().orElse(""));
        if (accounts.isEmpty()) preferences.remove("key");
        flush();
    }

    private String decrypt(String encrypted, String name) throws GeneralSecurityException {
        String[] parts = encrypted.split(":", -1);
        if (parts.length != 2) throw new IllegalArgumentException("无效的账号记录");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(Base64.getDecoder().decode(preferences.getProperty("key")), "AES"),
                new GCMParameterSpec(128, Base64.getDecoder().decode(parts[0])));
        cipher.updateAAD(name.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
        return new String(cipher.doFinal(Base64.getDecoder().decode(parts[1])), StandardCharsets.UTF_8);
    }

    private void flush() {
        preferences.setProperty("order", accounts.keySet().stream().map(SavedAccounts::key).collect(Collectors.joining(",")));
        Path temporary = null;
        try {
            Path target = file.file().toPath().toAbsolutePath();
            Files.createDirectories(target.getParent());
            temporary = Files.createTempFile(target.getParent(), target.getFileName() + ".", ".tmp");
            try (FileOutputStream output = outputFactory.open(temporary.toFile())) {
                preferences.store(output, null);
                output.getFD().sync();
            }
            // 同目录原子替换；写入、同步或替换失败时始终保留原文件。
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            rememberCommittedState();
        } catch (IOException | RuntimeException error) {
            restoreCommittedState();
            throw new IllegalStateException("保存账号失败", error);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); }
                catch (IOException ignored) { }
            }
        }
    }

    private void rememberCommittedState() {
        committedPreferences.clear();
        committedPreferences.putAll(preferences);
        committedAccounts.clear();
        committedAccounts.putAll(accounts);
    }

    private void restoreCommittedState() {
        preferences.clear();
        preferences.putAll(committedPreferences);
        accounts.clear();
        accounts.putAll(committedAccounts);
    }

    private static String key(String name) {
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(name.getBytes(StandardCharsets.UTF_8));
    }
}
