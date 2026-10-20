package top.totodile.pokemmo.client;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.XmlReader;

/** 从 UTF-8 XML 语言文件按 ID 读取界面文本。 */
final class ClientStrings {
    private final IntMap<String> messages = new IntMap<>();

    ClientStrings(FileHandle file) {
        XmlReader.Element root = new XmlReader().parse(file.readString("UTF-8"));
        if (!"strings".equals(root.getName())) {
            throw new IllegalArgumentException("语言文件根节点必须为 strings：" + file.path());
        }
        for (XmlReader.Element entry : root.getChildrenByName("string")) {
            int id = entry.getIntAttribute("id");
            if (messages.containsKey(id)) {
                throw new IllegalArgumentException("语言文件包含重复文本 ID：" + id);
            }
            messages.put(id, entry.getText() == null ? "" : entry.getText());
        }
    }

    String get(int id) {
        String message = messages.get(id);
        if (message == null) {
            throw new IllegalArgumentException("语言文件缺少文本 ID：" + id);
        }
        return message;
    }
}
