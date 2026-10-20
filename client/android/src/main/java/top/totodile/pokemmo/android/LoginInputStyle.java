package top.totodile.pokemmo.android;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.ImageView;

/** 登录原生输入栏的外观。 */
final class LoginInputStyle {
    private LoginInputStyle() {}

    static void apply(AutoCompleteTextView editor) {
        float density = editor.getResources().getDisplayMetrics().density;
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(10 * density);
        editor.setBackground(background);
        editor.setTextColor(Color.BLACK);
        editor.setHintTextColor(Color.LTGRAY);
        editor.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        // 去除字体额外的上下留白，让占位文字和输入文字在栏内垂直居中。
        editor.setIncludeFontPadding(false);
        // 禁用回退字体额外行距，让文字与光标使用同一行高度量。
        if (android.os.Build.VERSION.SDK_INT >= 28) editor.setFallbackLineSpacing(false);
        // Android 15 默认按系统语言预留最小行高，中文环境下会扩大行框并使字形偏上。
        if (android.os.Build.VERSION.SDK_INT >= 35) editor.setLocalePreferredLineHeightForMinimumUsed(false);
        if (editor.getId() == View.NO_ID) editor.setId(View.generateViewId());
        int padding = Math.round(10 * density);
        editor.setPadding(padding, padding, padding, padding);
        View parent = (View) editor.getParent();
        // 键盘后端随后会修改安全区边距，布局完成后再扣除父容器的偏移。
        if (editor.getTag() == null) {
            parent.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob) -> layout(editor));
            editor.setTag(LoginInputStyle.class);
        }
        layout(editor);
    }

    private static void layout(AutoCompleteTextView editor) {
        View root = editor.getRootView();
        View parent = (View) editor.getParent();
        if (root.getWidth() == 0 || parent.getWidth() == 0) return;
        int[] position = new int[2];
        parent.getLocationInWindow(position);
        int side = Math.round(root.getWidth() * 0.05f);
        int safeLeft = 0, safeRight = 0, safeBottom = 0;
        if (android.os.Build.VERSION.SDK_INT >= 28 && root.getRootWindowInsets() != null) {
            android.view.DisplayCutout cutout = root.getRootWindowInsets().getDisplayCutout();
            if (cutout != null) {
                safeLeft = cutout.getSafeInsetLeft();
                safeRight = cutout.getSafeInsetRight();
                safeBottom = cutout.getSafeInsetBottom();
            }
        }
        int left = Math.max(0, Math.max(side, safeLeft) - position[0]);
        int right = Math.max(0, Math.max(side, safeRight) - (root.getWidth() - position[0] - parent.getWidth()));
        // 密码键盘上方保留 10dp 间距；用户名悬浮键盘沿用已核对的位置。
        boolean password = (editor.getInputType() & android.text.InputType.TYPE_MASK_VARIATION)
                == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
        int bottom = password ? Math.round(10 * editor.getResources().getDisplayMetrics().density) : 0;
        bottom = Math.max(bottom, safeBottom - (root.getHeight() - position[1] - parent.getHeight()));
        // 手机沿用横屏比例；平板限制高度，避免输入栏随大屏幕过度放大。
        float density = editor.getResources().getDisplayMetrics().density;
        int height = Math.min(Math.round(root.getHeight() * 2f / 15f), Math.round(56 * density));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editor.getLayoutParams();
        if (params.leftMargin != left || params.rightMargin != right || params.bottomMargin != bottom || params.height != height) {
            params.setMargins(left, 0, right, bottom);
            params.height = height;
            editor.setLayoutParams(params);
        }
        RelativeLayout container = (RelativeLayout) parent;
        int padding = Math.round(10 * editor.getResources().getDisplayMetrics().density);
        boolean hasEye = false;
        for (int i = 0; i < container.getChildCount(); i++) {
            if (!(container.getChildAt(i) instanceof ImageView eye)) continue;
            hasEye = true;
            // 原生后端提供眼睛按钮和切换逻辑，这里将按钮约束到输入栏右侧。
            RelativeLayout.LayoutParams eyeParams = new RelativeLayout.LayoutParams(height, height);
            eyeParams.addRule(RelativeLayout.ALIGN_RIGHT, editor.getId());
            eyeParams.addRule(RelativeLayout.ALIGN_TOP, editor.getId());
            eye.setLayoutParams(eyeParams);
            eye.setColorFilter(Color.DKGRAY);
            eye.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int inset = Math.round(height * 0.23f);
            eye.setPadding(inset, inset, inset, inset);
            eye.setContentDescription("显示或隐藏密码");
        }
        editor.setPadding(padding, padding, hasEye ? height : padding, padding);
    }
}

