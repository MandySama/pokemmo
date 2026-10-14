package top.totodile.pokemmo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * 快捷道具栏绑定只管理自身的键，写入前重新读取以保留音量、FPS、ROM 路径等其他配置。
 * 与 ClientSettings 分开，避免两个实例互相覆盖对方的键。
 */
public final class HotbarSettings {
    private static final String PREFIX = "client.hotbar.item_id";
    private static final int SLOTS = 9;
    /** 自行车道具编号，默认占用栏位 2。 */
    public static final int BICYCLE_ITEM = 5450;
    /** 城镇地图道具编号，默认占用栏位 5。 */
    public static final int TOWN_MAP_ITEM = 5442;
    public static final int SWEET_SCENT_ITEM = 1179;
    private static final int BICYCLE_SLOT = 1;
    private static final int TOWN_MAP_SLOT = 4;
    private final Path file;
    private final int[] slots = new int[SLOTS];

    public HotbarSettings() {
        this(ClientFiles.data("../config/main.properties").normalize());
    }

    public HotbarSettings(Path file) {
        this.file = file;
        try {
            Properties values = read();
            for (int i = 0; i < SLOTS; i++) {
                String key = PREFIX + (i + 1);
                // 自行车与城镇地图是首次运行的默认绑定，已写入的栏位不再套用默认值。
                slots[i] = i == 2 ? SWEET_SCENT_ITEM : values.containsKey(key) ? parseItemId(values.getProperty(key)) : defaultItem(i);
            }
        } catch (IOException exception) {
            System.getLogger(HotbarSettings.class.getName()).log(System.Logger.Level.WARNING,
                    "Unable to load hotbar settings: {0}", exception.getClass().getSimpleName());
        }
    }

    /** 第 index（0 起）个栏位绑定的道具编号，0 表示空栏位。 */
    public int slot(int index) {
        return index >= 0 && index < SLOTS ? slots[index] : 0;
    }

    public void setSlot(int index, int itemId) {
        if (index >= 0 && index < SLOTS) slots[index] = normalize(itemId);
    }

    public void save() throws IOException {
        Properties values = read();
        for (int i = 0; i < SLOTS; i++) {
            values.setProperty(PREFIX + (i + 1), Integer.toString(slots[i]));
        }
        Path directory = file.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "hotbar-", ".tmp");
        try {
            try (var output = Files.newOutputStream(temporary)) {
                values.store(output, null);
            }
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static int parseItemId(String value) {
        try {
            return normalize(Integer.parseInt(value.trim()));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    /** 只接受空栏位或黑白道具编号范围，避免非法值写入配置。 */
    private static int normalize(int itemId) {
        var item = top.totodile.pokemmo.protocol.ItemCatalog.get(itemId);
        return item != null && item.addable() ? itemId : 0;
    }

    private static int defaultItem(int index) {
        if (index == 2) return SWEET_SCENT_ITEM;
        if (index == BICYCLE_SLOT) return BICYCLE_ITEM;
        if (index == TOWN_MAP_SLOT) return TOWN_MAP_ITEM;
        return 0;
    }

    private Properties read() throws IOException {
        Properties values = new Properties();
        if (Files.isRegularFile(file)) {
            try (var input = Files.newInputStream(file)) {
                values.load(input);
            }
        }
        return values;
    }
}
