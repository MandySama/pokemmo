package top.totodile.pokemmo.client.resources;

import com.badlogic.gdx.files.FileHandle;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.function.Function;

/** 对齐参考 ResourceUrlStreamHandler / XmlDataUrlConnection 的资源对象协议。 */
public final class AssetUrlStreamHandler extends URLStreamHandler {
    private final Function<String, FileHandle> assets;

    public AssetUrlStreamHandler(Function<String, FileHandle> assets) {
        this.assets = assets;
    }

    @Override
    protected URLConnection openConnection(URL url) {
        FileHandle file = assets.apply(url.getPath().substring(1));
        return new URLConnection(url) {
            @Override
            public void connect() {}

            @Override
            public Object getContent() {
                return file;
            }

            // 资源探测返回资源对象，不让 JDK 默认 ContentHandler 为探测额外打开文件流。
            @Override
            public Object getContent(Class<?>[] types) {
                for (Class<?> type : types) if (type.isInstance(file)) return file;
                return null;
            }

            @Override
            public InputStream getInputStream() {
                return "xml".equals(file.extension()) ? file.read(1024) : null;
            }
        };
    }
}
