package top.totodile.pokemmo.android;

import android.content.Context;
import android.view.View;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.DefaultAndroidInput;
import com.badlogic.gdx.input.NativeInputConfiguration;

/** 切换普通键盘与厂商安全键盘时，忽略新输入栏尚未显示期间的旧键盘隐藏通知。 */
final class LoginAndroidInput extends DefaultAndroidInput {
    private volatile boolean awaitingKeyboard;

    LoginAndroidInput(Application app,Context context,View view,AndroidApplicationConfiguration config){
        super(app,context,view,config);
    }

    @Override public void openTextInputField(NativeInputConfiguration config){
        awaitingKeyboard=true;
        super.openTextInputField(config);
    }

    @Override public void closeTextInputField(boolean confirm,NativeInputConfiguration.NativeInputCloseCallback callback){
        awaitingKeyboard=false;
        super.closeTextInputField(confirm,callback);
    }

    @Override public void onKeyboardHeightChanged(boolean visible,int height,int leftInset,int rightInset,int orientation){
        if(!visible && awaitingKeyboard)return;
        if(visible)awaitingKeyboard=false;
        super.onKeyboardHeightChanged(visible,height,leftInset,rightInset,orientation);
    }
}
