package top.totodile.pokemmo.client.ui.startup;

/** 平台登录场景：PC 提供 ROM 3D 与音乐，移动端保留启动背景。 */
public interface LoginScene extends AutoCloseable {
    /** 返回 true 表示本帧已绘制背景；资源未就绪时由调用方绘制原有背景。 */
    boolean draw(float delta, int width, int height);
    default void setFocused(boolean focused) {}
    @Override void close();
}
