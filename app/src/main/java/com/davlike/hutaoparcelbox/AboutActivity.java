package com.davlike.hutaoparcelbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

/** Standalone About screen without the main header or bottom navigation. */
public final class AboutActivity extends Activity {
    private static final String GITHUB_URL = "https://github.com/DavidLWB/HuTaoParcelBox";
    private static final String DEVELOPER = "Davlike";
    private static final String CONTACT_QQ = "536511760";
    private static final String FEEDBACK_EMAIL = "fankui@davlike.qzz.io";

    private Palette palette;
    private int eggTaps;
    private long lastEggTap;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        palette = Palette.of(this);
        buildUi();
        applySystemBars();
    }

    private void applySystemBars() {
        getWindow().setStatusBarColor(palette.bg);
        getWindow().setNavigationBarColor(palette.bg);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            android.view.WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                int mask = android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(palette.dark ? 0 : mask, mask);
            }
        } else {
            int flags = palette.dark ? 0
                : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void buildUi() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(palette.bg);
        root.setFitsSystemWindows(false);
        if (Build.VERSION.SDK_INT >= 30) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets bars = insets.getInsets(android.view.WindowInsets.Type.systemBars()
                    | android.view.WindowInsets.Type.displayCutout());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
        } else {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                view.setPadding(insets.getStableInsetLeft(), insets.getStableInsetTop(),
                    insets.getStableInsetRight(), insets.getStableInsetBottom());
                return insets;
            });
        }
        setContentView(root);

        TextView back = label("‹ 返回", 14, palette.accent, true);
        back.setPadding(dp(12), dp(10), dp(12), dp(10));
        back.setClickable(true);
        back.setFocusable(true);
        back.setContentDescription("返回");
        back.setOnClickListener(v -> finish());
        FrameLayout.LayoutParams backLayout = new FrameLayout.LayoutParams(-2, -2,
            Gravity.TOP | Gravity.START);
        backLayout.setMargins(dp(8), dp(8), 0, 0);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        column.setPadding(dp(28), dp(24), dp(28), dp(20));

        View topSpace = new View(this);
        column.addView(topSpace, new LinearLayout.LayoutParams(-1, dp(44)));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_launcher_artwork);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setContentDescription("应用图标");
        icon.setClickable(true);
        icon.setFocusable(true);
        icon.setOnClickListener(v -> registerEggTap());
        LinearLayout.LayoutParams iconLayout = new LinearLayout.LayoutParams(dp(104), dp(104));
        iconLayout.gravity = Gravity.CENTER_HORIZONTAL;
        column.addView(icon, iconLayout);

        TextView name = label(getString(R.string.app_name), 26, palette.ink, true);
        name.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nameLayout = new LinearLayout.LayoutParams(-2, -2);
        nameLayout.topMargin = dp(18);
        column.addView(name, nameLayout);

        TextView slogan = label(getString(R.string.app_slogan), 14, palette.muted, false);
        slogan.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sloganLayout = new LinearLayout.LayoutParams(-2, -2);
        sloganLayout.topMargin = dp(8);
        column.addView(slogan, sloganLayout);

        View flexibleSpace = new View(this);
        column.addView(flexibleSpace, new LinearLayout.LayoutParams(-1, 0, 1f));

        TextView version = label("版本 " + versionName(), 12, palette.muted, false);
        version.setGravity(Gravity.CENTER);
        column.addView(version, new LinearLayout.LayoutParams(-2, -2));

        TextView developer = label("Produced by " + DEVELOPER, 13, palette.ink, true);
        developer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams developerLayout = new LinearLayout.LayoutParams(-2, -2);
        developerLayout.topMargin = dp(10);
        column.addView(developer, developerLayout);

        TextView contact = label("QQ：" + CONTACT_QQ, 12, palette.muted, false);
        contact.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams contactLayout = new LinearLayout.LayoutParams(-2, -2);
        contactLayout.topMargin = dp(6);
        column.addView(contact, contactLayout);

        TextView feedback = label("反馈：" + FEEDBACK_EMAIL, 12, palette.accent, false);
        feedback.setGravity(Gravity.CENTER);
        feedback.setPadding(dp(8), dp(4), dp(8), dp(4));
        feedback.setClickable(true);
        feedback.setFocusable(true);
        feedback.setOnClickListener(v -> openFeedback());
        LinearLayout.LayoutParams feedbackLayout = new LinearLayout.LayoutParams(-2, -2);
        feedbackLayout.topMargin = dp(2);
        column.addView(feedback, feedbackLayout);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        TextView update = label("检查更新", 13, palette.accent, true);
        update.setGravity(Gravity.CENTER);
        update.setPadding(dp(18), dp(10), dp(18), dp(10));
        update.setBackground(background(palette.accentSoft, 18));
        update.setClickable(true);
        update.setFocusable(true);
        update.setOnClickListener(v -> UpdateChecker.check(this, true));
        actions.addView(update);

        TextView github = label("GitHub 仓库", 13, onAccent(), true);
        github.setGravity(Gravity.CENTER);
        github.setPadding(dp(18), dp(10), dp(18), dp(10));
        github.setBackground(background(palette.accent, 18));
        github.setClickable(true);
        github.setFocusable(true);
        github.setOnClickListener(v -> openGithub());
        LinearLayout.LayoutParams githubLayout = new LinearLayout.LayoutParams(-2, -2);
        githubLayout.setMarginStart(dp(10));
        actions.addView(github, githubLayout);
        LinearLayout.LayoutParams actionsLayout = new LinearLayout.LayoutParams(-2, -2);
        actionsLayout.topMargin = dp(20);
        column.addView(actions, actionsLayout);

        Switch autoUpdate = new Switch(this);
        autoUpdate.setText("启动时自动检测更新");
        autoUpdate.setTextSize(12);
        autoUpdate.setTextColor(palette.ink);
        autoUpdate.setChecked(getSharedPreferences(Palette.PREF_UI, MODE_PRIVATE)
            .getBoolean(UpdateChecker.KEY_AUTO_UPDATE, true));
        autoUpdate.setOnCheckedChangeListener((button, checked) ->
            getSharedPreferences(Palette.PREF_UI, MODE_PRIVATE).edit()
                .putBoolean(UpdateChecker.KEY_AUTO_UPDATE, checked).apply());
        LinearLayout.LayoutParams autoUpdateLayout = new LinearLayout.LayoutParams(-2, -2);
        autoUpdateLayout.topMargin = dp(14);
        column.addView(autoUpdate, autoUpdateLayout);

        TextView updateHint = label("检查更新会访问 GitHub Releases", 10, palette.muted, false);
        updateHint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams updateHintLayout = new LinearLayout.LayoutParams(-2, -2);
        updateHintLayout.topMargin = dp(2);
        column.addView(updateHint, updateHintLayout);

        TextView legal = label("Copyright © 2026 Davlike\n"
            + "非官方取件辅助工具，与各平台无隶属关系。\n"
            + "平台名称与标识归各平台所有。\n"
            + "本项目以 AGPL-3.0-only 开源；商业/闭源授权请联系 fankui@davlike.qzz.io。\n"
            + "AndroidX WebKit 遵循 Apache-2.0。",
            10, palette.muted, false);
        legal.setGravity(Gravity.CENTER);
        legal.setLineSpacing(dp(4), 1f);
        LinearLayout.LayoutParams legalLayout = new LinearLayout.LayoutParams(-2, -2);
        legalLayout.topMargin = dp(26);
        column.addView(legal, legalLayout);

        TextView license = label("查看开源许可", 11, palette.accent, false);
        license.setGravity(Gravity.CENTER);
        license.setPadding(dp(14), dp(10), dp(14), dp(10));
        license.setClickable(true);
        license.setFocusable(true);
        license.setOnClickListener(v -> showLicenseDialog());
        LinearLayout.LayoutParams licenseLayout = new LinearLayout.LayoutParams(-2, -2);
        licenseLayout.topMargin = dp(4);
        column.addView(license, licenseLayout);

        scroll.addView(column);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        root.addView(back, backLayout);
        root.post(root::requestApplyInsets);
    }

    private void openFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + FEEDBACK_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, "蝶引速取反馈");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException unavailable) {
            android.widget.Toast.makeText(this, "未找到邮件应用", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void showLicenseDialog() {
        new AlertDialog.Builder(this,
            palette.dark ? R.style.AppDialogThemeDark : R.style.AppDialogThemeLight)
            .setTitle("开源许可")
            .setMessage("本项目以 GNU AGPL-3.0-only 许可证开源，完整文本见项目仓库的 LICENSE 文件。\n\n"
                + "商业或闭源使用无法遵守 AGPL-3.0 时，请联系 fankui@davlike.qzz.io 获取商业授权。\n\n"
                + "内置 AndroidX WebKit 采用 Apache-2.0 License；部分实现方案参考 HandyCode（MIT License）。\n\n"
                + "源码：https://github.com/DavidLWB/HuTaoParcelBox")
            .setPositiveButton("知道了", null)
            .show();
    }

    private void registerEggTap() {
        long now = SystemClock.elapsedRealtime();
        if (now - lastEggTap > 1500L) eggTaps = 0;
        lastEggTap = now;
        eggTaps++;
        if (eggTaps < 5) return;
        eggTaps = 0;
        new AlertDialog.Builder(this,
            palette.dark ? R.style.AppDialogThemeDark : R.style.AppDialogThemeLight)
            .setTitle("彩蛋")
            .setMessage("可恶，这都被你发现啦！\n\n初版测试人员：\n行者无敌\n人之常情\n汤臣一品518全体成员\n\n金牌测试员：L1ght")
            .setPositiveButton("哈哈", null)
            .show();
    }

    private void openGithub() {
        if (GITHUB_URL.isEmpty()) {
            android.widget.Toast.makeText(this, "GitHub 仓库即将上线", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)));
        } catch (ActivityNotFoundException | SecurityException unavailable) {
            android.widget.Toast.makeText(this, "无法打开浏览器", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private String versionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException unavailable) {
            return "";
        }
    }

    private int onAccent() {
        return palette.dark ? palette.bg : Color.WHITE;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView label(String text, float size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setIncludeFontPadding(false);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private GradientDrawable background(int fill, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setCornerRadius(dp(radius));
        return shape;
    }
}
