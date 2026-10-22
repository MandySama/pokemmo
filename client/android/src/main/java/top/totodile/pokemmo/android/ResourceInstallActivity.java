package top.totodile.pokemmo.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.File;

/** 在创建 GL 游戏窗口前安装内置资源，沿用参考项目的后台解压和版本检查流程。 */
public final class ResourceInstallActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(android.graphics.Color.rgb(48, 48, 48));
        int padding = Math.round(40 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        TextView text = new TextView(this);
        text.setTextColor(android.graphics.Color.WHITE);
        text.setText(R.string.preparing_resources);
        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        layout.addView(text);
        layout.addView(progress, new LinearLayout.LayoutParams(-1, -2));
        setContentView(layout);
        new Thread(() -> {
            try {
                File root = getExternalFilesDir(null);
                if (root == null) throw new java.io.IOException("External application storage is unavailable");
                final int[] lastPercent = {-1};
                BundledResourceInstaller.install(getAssets()::open, root, percent -> {
                    if (percent == lastPercent[0]) return;
                    lastPercent[0] = percent;
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        progress.setProgress(percent);
                        text.setText(getString(R.string.preparing_resources_progress, percent));
                    });
                });
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    startActivity(new Intent(this, AndroidLauncher.class));
                    finish();
                });
            } catch (Exception error) {
                android.util.Log.e("PokeMMO", "Resource installation failed", error);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    progress.setVisibility(View.GONE);
                });
            }
        }, "PokeMMO-resource-install").start();
    }
}
