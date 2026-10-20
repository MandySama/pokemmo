package top.totodile.pokemmo.android;

import android.content.Context;
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.DefaultAndroidInput;
import com.badlogic.gdx.input.NativeInputConfiguration;

/** 管理原生输入栏的输入法关联，以及普通键盘与厂商安全键盘的切换。 */
final class LoginAndroidInput extends DefaultAndroidInput {
    private volatile boolean awaitingKeyboard;
    private final Activity activity;

    LoginAndroidInput(Application app,Context context,View view,AndroidApplicationConfiguration config){
        super(app,context,view,config);
        activity=(Activity)context;
    }

    @Override public void openTextInputField(NativeInputConfiguration config){
        awaitingKeyboard=true;
        activity.runOnUiThread(() -> activity.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM));
        super.openTextInputField(config);
        // 窗口重新成为 IME 目标后，等原生输入栏完成挂载再请求键盘。
        // LibGDX 的首次请求可能发生在输入法尚未接管该 View 时。
        activity.getWindow().getDecorView().post(() -> {
            View editor = activity.getCurrentFocus();
            if (editor instanceof android.widget.EditText) {
                editor.post(() -> {
                    if (editor.hasFocus() && isTextInputFieldOpened()) {
                        InputMethodManager manager = (InputMethodManager)
                                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        manager.showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });
    }

    @Override public void closeTextInputField(boolean confirm,NativeInputConfiguration.NativeInputCloseCallback callback){
        awaitingKeyboard=false;
        super.closeTextInputField(confirm,callback);
        // LibGDX 将关闭操作投递到主线程；排在其后，确保输入栏已实际关闭。
        activity.getWindow().getDecorView().post(() -> {
            if (!isTextInputFieldOpened()) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            }
        });
    }

    @Override public void onKeyboardHeightChanged(boolean visible,int height,int leftInset,int rightInset,int orientation){
        if(!visible && awaitingKeyboard)return;
        if(visible)awaitingKeyboard=false;
        super.onKeyboardHeightChanged(visible,height,leftInset,rightInset,orientation);
    }
}
