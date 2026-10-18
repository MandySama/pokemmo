package top.totodile.pokemmo.android;

import android.os.Bundle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import top.totodile.pokemmo.client.ClientApplication;

/** Android 入口，资源从 APK 的 assets 目录读取。 */
public final class AndroidLauncher extends AndroidApplication {
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
    }
}
