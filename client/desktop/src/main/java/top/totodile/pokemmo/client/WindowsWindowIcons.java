package top.totodile.pokemmo.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.JNI;
import org.lwjgl.system.Platform;
import org.lwjgl.system.windows.User32;
import org.lwjgl.system.windows.WindowsLibrary;

/** 同步窗口类图标，供 Windows 外壳回退读取。 */
final class WindowsWindowIcons extends Lwjgl3WindowAdapter implements LifecycleListener {
    private long hwnd;
    private long previousBig, previousSmall;

    @Override public void created(Lwjgl3Window window) {
        if (Platform.get() != Platform.WINDOWS) return;
        hwnd = GLFWNativeWin32.glfwGetWin32Window(window.getWindowHandle());
        try (WindowsLibrary user32 = new WindowsLibrary("user32.dll")) {
            long sendMessage = user32.getFunctionAddress("SendMessageW");
            long big = JNI.callPPPP(hwnd, User32.WM_GETICON, 1L, 0L, sendMessage);
            long small = JNI.callPPPP(hwnd, User32.WM_GETICON, 0L, 0L, sendMessage);
            if (big == 0 || small == 0) throw new IllegalStateException("窗口图标尚未加载");
            previousBig = User32.SetClassLongPtr(null, hwnd, User32.GCL_HICON, big);
            previousSmall = User32.SetClassLongPtr(null, hwnd, User32.GCL_HICONSM, small);
        }
        Gdx.app.addLifecycleListener(this);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override public void dispose() {
        if (hwnd == 0) return;
        // GLFW 销毁窗口时会释放窗口图标，须先恢复类图标以免留下失效句柄。
        User32.SetClassLongPtr(null, hwnd, User32.GCL_HICON, previousBig);
        User32.SetClassLongPtr(null, hwnd, User32.GCL_HICONSM, previousSmall);
        hwnd = 0;
    }
}
