package com.davlike.hutaoparcelbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.widget.TextView;

/** Standalone About screen without the main header or bottom navigation. */
public final class AboutActivity extends Activity {
    private static final String GITHUB_URL = "https://github.com/DavidLWB/HuTaoParcelBox";
    private static final String DEVELOPER = "Davlike";
    private static final String CONTACT_QQ = "536511760";

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
        column.setGravity(Gravity.CENTER);
        column.setPadding(dp(24), dp(24), dp(24), dp(24));

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

        TextView name = label(getString(R.string.app_name), 24, palette.ink, true);
        name.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nameLayout = new LinearLayout.LayoutParams(-2, -2);
        nameLayout.topMargin = dp(16);
        column.addView(name, nameLayout);

        TextView slogan = label(getString(R.string.app_slogan), 14, palette.muted, false);
        slogan.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sloganLayout = new LinearLayout.LayoutParams(-2, -2);
        sloganLayout.topMargin = dp(8);
        column.addView(slogan, sloganLayout);

        TextView beta = label("Beta 版本 · 仅供内部测试", 12, palette.accent, true);
        beta.setGravity(Gravity.CENTER);
        beta.setPadding(dp(14), dp(6), dp(14), dp(6));
        beta.setBackground(background(palette.accentSoft, 14));
        LinearLayout.LayoutParams betaLayout = new LinearLayout.LayoutParams(-2, -2);
        betaLayout.topMargin = dp(20);
        column.addView(beta, betaLayout);

        TextView betaNote = label("正式上线前将移除该提示", 11, palette.muted, false);
        betaNote.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams betaNoteLayout = new LinearLayout.LayoutParams(-2, -2);
        betaNoteLayout.topMargin = dp(6);
        column.addView(betaNote, betaNoteLayout);

        TextView version = label("版本 " + versionName(), 12, palette.muted, false);
        version.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams versionLayout = new LinearLayout.LayoutParams(-2, -2);
        versionLayout.topMargin = dp(22);
        column.addView(version, versionLayout);

        TextView developer = label("开发者：" + DEVELOPER, 13, palette.ink, true);
        developer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams developerLayout = new LinearLayout.LayoutParams(-2, -2);
        developerLayout.topMargin = dp(18);
        column.addView(developer, developerLayout);

        TextView contact = label("QQ：" + CONTACT_QQ, 13, palette.ink, false);
        contact.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams contactLayout = new LinearLayout.LayoutParams(-2, -2);
        contactLayout.topMargin = dp(6);
        column.addView(contact, contactLayout);

        TextView github = label("访问 GitHub 仓库", 13, onAccent(), true);
        github.setGravity(Gravity.CENTER);
        github.setPadding(dp(20), dp(10), dp(20), dp(10));
        github.setBackground(background(palette.accent, 18));
        github.setClickable(true);
        github.setFocusable(true);
        github.setOnClickListener(v -> openGithub());
        LinearLayout.LayoutParams githubLayout = new LinearLayout.LayoutParams(-2, -2);
        githubLayout.topMargin = dp(24);
        column.addView(github, githubLayout);

        scroll.addView(column);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        root.addView(back, backLayout);
        root.post(root::requestApplyInsets);
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
            .setMessage("可恶，这都被你发现啦！\n\n首批测试人员：\n行者无敌\n人之常情\n汤臣一品518全体成员")
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
