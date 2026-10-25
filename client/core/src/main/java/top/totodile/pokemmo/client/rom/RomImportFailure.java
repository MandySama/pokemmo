package top.totodile.pokemmo.client.rom;

import java.io.IOException;

/** 导入错误映射到参考客户端的语言资源，UI 不依赖底层异常文案。 */
public final class RomImportFailure extends IOException {
    public final int stringId;
    public final String detail;

    public RomImportFailure(int stringId, String detail, Throwable cause) {
        super(detail, cause);
        this.stringId = stringId;
        this.detail = detail;
    }
}
