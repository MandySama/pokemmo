package top.totodile.pokemmo.android;

import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import top.totodile.pokemmo.client.ClientApplication;
import top.totodile.pokemmo.db.android.AndroidDatabase;

/** Android 入口，资源从首次启动解压后的 files/data 目录读取。 */
public final class AndroidLauncher extends AndroidApplication {
    private static final int PICK_ROM = 814;
    private java.util.function.Consumer<top.totodile.pokemmo.client.rom.RomFilePicker.Source>
            romSelected;

    @Override
    protected void onActivityResult(int request, int result, android.content.Intent data) {
        super.onActivityResult(request, result, data);
        if (request != PICK_ROM || romSelected == null) return;
        var selected = romSelected;
        romSelected = null;
        if (result != RESULT_OK || data == null || data.getData() == null) {
            selected.accept(null);
            return;
        }
        android.net.Uri uri = data.getData();
        String displayName = null;
        try (android.database.Cursor cursor =
                getContentResolver()
                        .query(
                                uri,
                                new String[] {android.provider.OpenableColumns.DISPLAY_NAME},
                                null,
                                null,
                                null)) {
            if (cursor != null && cursor.moveToFirst()) displayName = cursor.getString(0);
        } catch (RuntimeException ignored) {
        }
        final String filename = displayName;
        selected.accept(
                new top.totodile.pokemmo.client.rom.RomFilePicker.Source() {
                    @Override
                    public java.io.InputStream open() throws java.io.IOException {
                        java.io.InputStream stream = getContentResolver().openInputStream(uri);
                        if (stream == null) throw new java.io.IOException("无法读取选择的文件");
                        return stream;
                    }

                    @Override
                    public String name() {
                        return filename;
                    }
                });
    }

    private final top.totodile.pokemmo.client.rom.RomFilePicker romPicker =
            new top.totodile.pokemmo.client.rom.RomFilePicker() {
                @Override
                public void choose(
                        String extension,
                        java.util.function.Consumer<Source> selected,
                        java.util.function.Consumer<String> failed) {
                    runOnUiThread(
                            () -> {
                                if (romSelected != null) {
                                    failed.accept("文件选择器已打开");
                                    return;
                                }
                                romSelected = selected;
                                try {
                                    android.content.Intent intent =
                                            new android.content.Intent(
                                                    android.content.Intent.ACTION_OPEN_DOCUMENT);
                                    intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
                                    intent.setType("*/*");
                                    startActivityForResult(intent, PICK_ROM);
                                } catch (RuntimeException error) {
                                    romSelected = null;
                                    failed.accept("无法选择 ROM: " + error.getMessage());
                                }
                            });
                }
            };

    @Override
    protected void onUserLeaveHint() {
        // 离开应用前先收起原生输入层，让最近任务捕获游戏画面。
        // 不修改安全键盘的截图保护，也不把明文密码保留在预览里。
        if (getInput() != null && getInput().isTextInputFieldOpened()) {
            getInput().closeTextInputField(false);
            android.view.View focus = getCurrentFocus();
            if (focus instanceof android.widget.AutoCompleteTextView) {
                android.view.inputmethod.InputMethodManager manager =
                        (android.view.inputmethod.InputMethodManager)
                                getSystemService(INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
        }
        super.onUserLeaveHint();
    }

    @Override
    public com.badlogic.gdx.backends.android.AndroidInput createInput(
            com.badlogic.gdx.Application app,
            android.content.Context context,
            Object view,
            AndroidApplicationConfiguration config) {
        return new LoginAndroidInput(app, context, (android.view.View) view, config);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        config.disableAudio = false;
        config.useAccelerometer = false;
        config.useCompass = false;
        java.io.File externalFiles = getExternalFilesDir(null);
        if (externalFiles == null)
            throw new IllegalStateException("External application storage is unavailable");
        // 配置、存档和解压后的资源共用应用外部 files 目录。
        java.io.File storageRoot = externalFiles;
        for (String name : new String[] {"config", "save"}) {
            java.io.File directory = new java.io.File(storageRoot, name);
            if (!directory.isDirectory() && !directory.mkdirs())
                throw new IllegalStateException(
                        "Cannot create application storage directory: " + name);
        }
        android.os.BatteryManager battery =
                (android.os.BatteryManager) getSystemService(BATTERY_SERVICE);
        // 单调时钟包含后台和设备休眠时间，不受系统时间调整影响。
        ClientApplication client =
                new ClientApplication(
                                path -> {
                                    var file =
                                            new com.badlogic.gdx.files.FileHandle(
                                                    new java.io.File(storageRoot, "data/" + path));
                                    return file.exists()
                                            ? file
                                            : Gdx.files.internal("startup/" + path);
                                },
                                true,
                                android.os.SystemClock::elapsedRealtimeNanos,
                                () ->
                                        new com.badlogic.gdx.files.FileHandle(
                                                new java.io.File(storageRoot, "config")),
                                top.totodile.pokemmo.server.login.LoginServices.create(),
                                () ->
                                        battery == null
                                                ? -1
                                                : battery.getIntProperty(
                                                        android.os.BatteryManager
                                                                .BATTERY_PROPERTY_CAPACITY))
                        .withLoginScene(top.totodile.pokemmo.android.audio.AndroidLoginAudio::new)
                        .withAudio(resources -> resources.audio(new top.totodile.pokemmo.client.audio.RomAudio(
                                resources,()->{System.loadLibrary("sseqj");System.loadLibrary("agbplayj");},183)))
                        .withRomFilePicker(romPicker)
                        .withGameService(
                                new top.totodile.pokemmo.server.game.LocalGameService(
                                        () ->
                                                new AndroidDatabase(
                                                        new java.io.File(
                                                                storageRoot, "save/game.db"))));
        initialize(
                new ResourceLoadingApplication(
                        client, getAssets()::open, storageRoot, getString(R.string.decompressing)),
                config);
        // Activity 创建后进入黑色加载阶段，系统主题底色仅作为启动窗口的兜底。
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        // 游戏画面本身不接收 IME，避免隐藏的安全键盘被并入任务快照的安全检查。
        // 原生输入栏打开时由 LoginAndroidInput 恢复与输入法的关联。
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        // 与参考应用一致，停留在前台时保持屏幕亮起；进入后台后由系统管理。
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 游戏画面保持原位；LibGDX 根据键盘高度仅移动原生输入栏。
        getWindow()
                .setSoftInputMode(
                        android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        // LibGDX 原生输入栏默认使用“完成”；用户名与参考应用一致，显示“下一步”。
        getWindow()
                .getDecorView()
                .getViewTreeObserver()
                .addOnGlobalFocusChangeListener(
                        (previous, current) -> {
                            if (current instanceof android.widget.AutoCompleteTextView editor) {
                                LoginInputStyle.apply(editor);
                                boolean password =
                                        (editor.getInputType()
                                                        & android.text.InputType
                                                                .TYPE_MASK_VARIATION)
                                                == android.text.InputType
                                                        .TYPE_TEXT_VARIATION_PASSWORD;
                                int action =
                                        password
                                                ? android.view.inputmethod.EditorInfo
                                                        .IME_ACTION_DONE
                                                : android.view.inputmethod.EditorInfo
                                                        .IME_ACTION_NEXT;
                                editor.setImeOptions(
                                        android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN
                                                | action);
                                editor.setOnEditorActionListener(
                                        (view, id, event) -> {
                                            if (id
                                                            == android.view.inputmethod.EditorInfo
                                                                    .IME_ACTION_NEXT
                                                    || id
                                                            == android.view.inputmethod.EditorInfo
                                                                    .IME_ACTION_DONE) {
                                                Gdx.input.closeTextInputField(true);
                                                return true;
                                            }
                                            return false;
                                        });
                            }
                        });
    }
}
