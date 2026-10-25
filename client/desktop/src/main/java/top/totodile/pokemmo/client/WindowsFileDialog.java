package top.totodile.pokemmo.client;

import static org.lwjgl.system.libffi.LibFFI.*;

import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libffi.FFICIF;
import org.lwjgl.system.windows.Kernel32;
import org.lwjgl.system.windows.User32;
import org.lwjgl.system.windows.WindowsLibrary;

import java.nio.ByteBuffer;

/** 按官方 PC 的实际表现打开 Windows 原生文件框，并定位到屏幕左上角。 */
final class WindowsFileDialog {
    // 官方实测约 840×530 的可见区域；这里包含系统窗口边框，单位为 96 DPI。
    private static final int DEFAULT_WIDTH = 860;
    private static final int DEFAULT_HEIGHT = 537;

    private WindowsFileDialog() {}

    static String open(boolean mod) {
        boolean wide = org.lwjgl.system.Pointer.POINTER_SIZE == 8;
        ByteBuffer filename = MemoryUtil.memCalloc(32768 * 2);
        // 原生框按所在屏幕的 DPI 创建，避免 Java 线程的 DPI 模式导致尺寸单位不一致。
        long previousDpi = User32.SetThreadDpiAwarenessContext(-4L);
        try (MemoryStack stack = MemoryStack.stackPush();
                WindowsLibrary library = new WindowsLibrary("comdlg32.dll");
                WindowsLibrary user32 = new WindowsLibrary("user32.dll");
                InitialPosition hook = new InitialPosition(user32)) {
            // OPENFILENAMEW：Win32 / Win64 的指针对齐和结构长度不同。
            ByteBuffer request = stack.calloc(wide ? 152 : 88);
            request.putInt(0, request.capacity());
            var filter = stack.UTF16(mod ? "mod, zip\0*.mod;*.zip\0" : "All files\0*.*\0");
            pointer(request, wide ? 24 : 12, MemoryUtil.memAddress(filter));
            request.putInt(wide ? 44 : 24, 1);
            pointer(request, wide ? 48 : 28, MemoryUtil.memAddress(filename));
            request.putInt(wide ? 56 : 32, 32768);
            // Explorer / FILEMUSTEXIST / PATHMUSTEXIST / NOCHANGEDIR / HIDEREADONLY。
            request.putInt(
                    wide ? 96 : 52, 0x00080000 | 0x00001000 | 0x00000800 | 0x00000008 | 0x00000004);
            int selected =
                    JNI.callPI(
                            MemoryUtil.memAddress(request),
                            library.getFunctionAddress("GetOpenFileNameW"));
            if (selected != 0) return MemoryUtil.memUTF16(MemoryUtil.memAddress(filename));
            int error = JNI.callI(library.getFunctionAddress("CommDlgExtendedError"));
            if (error != 0) throw new IllegalStateException("Unable to open file dialog: " + error);
            return null;
        } finally {
            if (previousDpi != 0) User32.SetThreadDpiAwarenessContext(previousDpi);
            MemoryUtil.memFree(filename);
        }
    }

    /** 仅监听当前选择线程首次显示窗口；OFN_ENABLEHOOK 会将现代文件框降级为旧式界面。 */
    private static final class InitialPosition implements CallbackI, AutoCloseable {
        private static final FFICIF CIF =
                APIUtil.apiCreateCIF(
                        APIUtil.apiStdcall(),
                        ffi_type_pointer,
                        ffi_type_sint32,
                        ffi_type_pointer,
                        ffi_type_pointer);
        private final long next;
        private final long unhook;
        private final long callback;
        private final long handle;
        private boolean positioned;

        InitialPosition(WindowsLibrary library) {
            next = library.getFunctionAddress("CallNextHookEx");
            unhook = library.getFunctionAddress("UnhookWindowsHookEx");
            callback = CallbackI.super.address();
            handle =
                    JNI.callPPP(
                            12,
                            callback,
                            0L,
                            Kernel32.GetCurrentThreadId(),
                            library.getFunctionAddress("SetWindowsHookExW"));
            if (handle == 0) {
                Callback.free(callback);
                throw new IllegalStateException("Unable to position file dialog");
            }
        }

        @Override
        public FFICIF getCallInterface() {
            return CIF;
        }

        @Override
        public void callback(long result, long args) {
            int size = org.lwjgl.system.Pointer.POINTER_SIZE;
            int code = MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args));
            long wParam = MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args + size));
            long param = MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args + size * 2L));
            // CWPRETSTRUCT：lResult / lParam / wParam / message / hwnd。
            if (code >= 0
                    && !positioned
                    && MemoryUtil.memGetInt(param + size * 3L) == User32.WM_SHOWWINDOW
                    && MemoryUtil.memGetAddress(param + size * 2L) != 0) {
                long hwnd = MemoryUtil.memGetAddress(param + (size == 8 ? 32 : 16));
                if ((User32.GetWindowLongPtr(null, hwnd, User32.GWL_STYLE) & User32.WS_CHILD)
                        == 0) {
                    positioned = true;
                    int dpi = User32.GetDpiForWindow(hwnd);
                    float scale = (dpi > 0 ? dpi : 96) / 96f;
                    int width =
                            Math.min(
                                    Math.round(DEFAULT_WIDTH * scale),
                                    User32.GetSystemMetrics(User32.SM_CXSCREEN));
                    int height =
                            Math.min(
                                    Math.round(DEFAULT_HEIGHT * scale),
                                    User32.GetSystemMetrics(User32.SM_CYSCREEN));
                    // 只在首次显示时设置大小，后续允许用户自由拖动和缩放。
                    User32.SetWindowPos(null, hwnd, 0, 0, 0, width, height, 0x0004 | 0x0010);
                }
            }
            APIUtil.apiClosureRetP(result, JNI.callPPPP(handle, code, wParam, param, next));
        }

        @Override
        public void close() {
            JNI.callPI(handle, unhook);
            Callback.free(callback);
        }
    }

    private static void pointer(ByteBuffer buffer, int offset, long value) {
        if (org.lwjgl.system.Pointer.POINTER_SIZE == 8) buffer.putLong(offset, value);
        else buffer.putInt(offset, (int) value);
    }
}
