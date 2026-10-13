package top.totodile.pokemmo.world;

import top.totodile.pokemmo.ClientFiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

/** 游戏通用精灵包：包头之后依次存放带长度的 PNG 图像。 */
final class GameSprites {
    private GameSprites() { }

    static BufferedImage shadow() throws IOException {
        byte[] bytes;
        try (var stream = new GZIPInputStream(Files.newInputStream(ClientFiles.data("sprites/game.pak")))) {
            bytes = stream.readNBytes(1024 * 1024 + 1);
        }
        if (bytes.length < 9 || bytes.length > 1024 * 1024) throw new IOException("Invalid game sprite pack");
        ByteBuffer data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        byte[] magic = {7, 'P', 'O', 'K', 'E', 'M', 'M', 'O'};
        for (byte value : magic) if (data.get() != value) throw new IOException("Invalid game sprite header");
        int count = Byte.toUnsignedInt(data.get());
        if (count < 3 || count > 64) throw new IOException("Invalid game sprite count");
        for (int index = 0; index < count; index++) {
            if (data.remaining() < 4) throw new IOException("Truncated game sprite length");
            int length = data.getInt();
            if (length <= 0 || length > data.remaining()) throw new IOException("Invalid game sprite length");
            if (index == 2) {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes, data.position(), length));
                if (image == null || image.getWidth() > 256 || image.getHeight() > 256) throw new IOException("Invalid shadow image");
                return image;
            }
            data.position(data.position() + length);
        }
        throw new IOException("Missing shadow image");
    }
}
