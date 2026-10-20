package top.totodile.pokemmo.android;

import android.os.Bundle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import top.totodile.pokemmo.client.ClientApplication;

/** Android 入口，资源从 APK 的 assets 目录读取。 */
public final class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onUserLeaveHint() {
        // 离开应用前先收起原生输入层，让最近任务捕获游戏画面。
        // 不修改安全键盘的截图保护，也不把明文密码保留在预览里。
        if (getInput() != null && getInput().isTextInputFieldOpened()) {
            getInput().closeTextInputField(false);
            android.view.View focus = getCurrentFocus();
            if (focus instanceof android.widget.AutoCompleteTextView) {
                android.view.inputmethod.InputMethodManager manager =
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
        }
        super.onUserLeaveHint();
    }
    @Override public com.badlogic.gdx.backends.android.AndroidInput createInput(
            com.badlogic.gdx.Application app,android.content.Context context,Object view,
            AndroidApplicationConfiguration config){
        return new LoginAndroidInput(app,context,(android.view.View)view,config);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        config.disableAudio = true;
        config.useAccelerometer = false;
        config.useCompass = false;
        // 单调时钟包含后台和设备休眠时间，不受系统时间调整影响。
        initialize(new ClientApplication(path -> Gdx.files.internal(path), true,
                android.os.SystemClock::elapsedRealtimeNanos), config);
        // 系统启动窗口为白色，Activity 创建后进入黑色加载阶段。
        // 只更改窗口底板，最近任务的主题背景色仍保持白色。
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        // 游戏画面保持原位；LibGDX 根据键盘高度仅移动原生输入栏。
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        // LibGDX 原生输入栏默认使用“完成”；用户名与参考应用一致，显示“下一步”。
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener((previous,current)->{
            if(current instanceof android.widget.AutoCompleteTextView editor){
                LoginInputStyle.apply(editor);
                boolean password=(editor.getInputType() & android.text.InputType.TYPE_MASK_VARIATION)
                        == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
                int action=password?android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                        :android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;
                editor.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN | action);
                editor.setOnEditorActionListener((view,id,event)->{
                    if(id==android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
                            || id==android.view.inputmethod.EditorInfo.IME_ACTION_DONE){
                        Gdx.input.closeTextInputField(true);
                        return true;
                    }
                    return false;
                });
            }
        });
    }
}
