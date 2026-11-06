package com.davlike.hutaoparcelbox;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private static final int SECTION_IDENTITY = 0, SECTION_PARCELS = 1, SECTION_MORE = 2;
    private static final int PLATFORM_TAOBAO = 0, PLATFORM_PDD = 1, PLATFORM_CAINIAO = 2, PLATFORM_JD = 3;
    private static final String[] PLATFORM_NAMES = {"淘宝", "拼多多", "菜鸟", "京东"};
    private static final int[] PLATFORM_ICONS = {
        R.drawable.ic_taobao, R.drawable.ic_pinduoduo,
        R.drawable.ic_cainiao, R.drawable.ic_jd
    };
    private static final String PDD_PACKAGE = "com.xunmeng.pinduoduo";
    private static final String CAINIAO_PACKAGE = "com.cainiao.wireless";
    private static final String CAINIAO_IDENTITY_ACTIVITY =
        "com.cainiao.wireless.identity_code.IdentityCodeActivity";
    private static final String CAINIAO_HOME_ACTIVITY =
        "com.cainiao.wireless.homepage.view.activity.HomePageActivity";
    private static final String CAINIAO_WEB_FALLBACK =
        "https://market.m.taobao.com/app/cn-yz/multi-activity/authCode.html?bizEntry=ALIPAY_GUOGUO";
    private static final String JD_PACKAGE = "com.jingdong.app.mall";
    private static final String JD_PENDING_DEEPLINK =
        "openjd://virtual?params=%7B%22category%22%3A%22jump%22%2C%22des%22%3A%22orderlist%22%7D";
    private static final String JD_WEB_FALLBACK = "https://order.jd.com/center/list.action";
    private static final String TAOBAO_PACKAGE = "com.taobao.taobao";
    private static final String TAOBAO_ORDER_ACTIVITY =
        "com.taobao.android.order.bundle.TBOrderListActivity";
    private static final String TAOBAO_ORDER_DEEPLINK =
        "taobao://m.taobao.com/tbopen/index.html?h5Url=https%3A%2F%2Fh5.m.taobao.com%2Fawp%2Fbase%2Forder.htm";
    private static final String TAOBAO_ORDER_DEEPLINK_FALLBACK =
        "tbopen://m.taobao.com/tbopen/index.html?h5Url=https%3A%2F%2Fh5.m.taobao.com%2Fawp%2Fbase%2Forder.htm";
    private static final String PDD_ORDER_DEEPLINK =
        "pinduoduo://com.xunmeng.pinduoduo/orders.html?type=1&comment_tab=1&combine_orders=1&main_orders=1&refer_page_name=personal";
    private static final String XHS_PACKAGE = "com.xingin.xhs";
    private static final String XHS_ORDER_DEEPLINK = "xhsdiscover://rn/lancer-order/order/list";
    private static final String MEITUAN_PACKAGE = "com.sankuai.meituan";
    private static final String MEITUAN_ORDER_DEEPLINK = "imeituan://www.meituan.com/orderTab";
    private static final String DOUYIN_PACKAGE = "com.ss.android.ugc.aweme";
    private static final String DOUYIN_ORDER_DEEPLINK = "snssdk1128://mall/xtab";
    private static final String PDD_IDENTITY_DEEPLINK =
        "pinduoduo://com.xunmeng.pinduoduo/mdkd/package?tab=ID_CODE&entry_source=11&refer_page_name=login&refer_page_sn=10169";
    private static final String PDD_IDENTITY_DEEPLINK_FALLBACK =
        "pinduoduo://com.xunmeng.pinduoduo/mdkd/package?tab=ID_CODE&entry_source=11";
    private static final String PDD_PACKAGE_DEEPLINK =
        "pinduoduo://com.xunmeng.pinduoduo/mdkd/package";
    private static final String PDD_WEB_FALLBACK =
        "https://m.pinduoduo.net/mdkd/package?entry_source=18&extra_params=from_wx_jump%3D1&p_channel=0";
    private static final String PREF_UI = Palette.PREF_UI;
    private static final String KEY_SECTION = "last_section";
    private static final String KEY_START_SECTION = "start_section";
    private static final String KEY_IDENTITY_ORDER = "identity_order";
    private static final String KEY_PARCELS_ORDER = "parcels_order";
    private static final String KEY_IDENTITY_HIDDEN = "identity_hidden";
    private static final String KEY_PARCELS_HIDDEN = "parcels_hidden";
    private static final String KEY_LOGIN_TAOBAO = "login_taobao";
    private static final String KEY_LOGIN_PDD = "login_pdd";
    private static final String KEY_LOGIN_JD = "login_jd";
    private static final String KEY_PDD_APP_MODE = "pdd_app_mode";

    private Palette palette;
    private int section = SECTION_IDENTITY;
    private boolean accountOpen;
    private boolean orderOpen;
    private int[] identityOrder = {PLATFORM_TAOBAO, PLATFORM_PDD, PLATFORM_CAINIAO};
    private int[] parcelsOrder = {PLATFORM_TAOBAO, PLATFORM_PDD, PLATFORM_JD};
    private final boolean[] identityHidden = new boolean[4];
    private final boolean[] parcelsHidden = new boolean[4];
    private boolean pddAppMode;
    private AlertDialog pddModeDialog;
    private View pddAppPanel;
    private final boolean[] identityExpanded = new boolean[4];
    private final boolean[] parcelsExpanded = new boolean[4];
    private final boolean[] signedIn = new boolean[4];

    private WebView browser;
    private FrameLayout browserFrame, pinduoduoFrame, jdFrame, contentHost;
    private PddPage pddPage;
    private JdPage jdPage;
    private LinearLayout bottomBar;
    private final LinearLayout[] navItems = new LinearLayout[3];
    private final ImageView[] navIcons = new ImageView[3];
    private final TextView[] navLabels = new TextView[3];
    private TextView headerTitle;
    private ScrollView identityScroll, parcelsScroll;
    private PlatformCard[] identityCards = new PlatformCard[4];
    private PlatformCard[] parcelsCards = new PlatformCard[4];
    private int pendingScrollPlatform = -1;

    private String combinedMarkup;
    private String combinedGateScript;
    private String officialIdentityScript;
    private String taobaoLoginChromeScript;
    private int combineGeneration;
    private int popupGeneration;
    private boolean combinedDisplayed;
    private View transitionPanel;
    private long transitionStarted;
    private int transitionGeneration;
    private final java.util.List<ShimmerBlock> shimmerBlocks = new java.util.ArrayList<>();
    private android.animation.ValueAnimator shimmerAnimation;
    private Dialog popupDialog;
    private WebView popupView;
    private long lastBlockedNotice = 0;
    private boolean activityResumed;
    private int pageTransitionGeneration;

    private final class PlatformCard {
        final int section;
        final int platform;
        final LinearLayout card;
        final FrameLayout body;
        final int bodyHeight;
        final TextView status;
        final ImageView refresh;
        final ImageView chevron;
        ValueAnimator bodyAnimator;

        PlatformCard(int section, int platform, LinearLayout card, FrameLayout body, int bodyHeight,
                     TextView status, ImageView refresh, ImageView chevron) {
            this.section = section;
            this.platform = platform;
            this.card = card;
            this.body = body;
            this.bodyHeight = bodyHeight;
            this.status = status;
            this.refresh = refresh;
            this.chevron = chevron;
        }
    }

    private static final class CardBody extends FrameLayout {
        CardBody(Context context) { super(context); }

        @Override public boolean onInterceptTouchEvent(MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN && getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return false;
        }
    }

    private final SiteDataClearer.Observer logoutListener = new SiteDataClearer.Observer() {
        @Override public void onStarted(int page) {
            setSignedIn(page, false);
            if (page == PLATFORM_TAOBAO) {
                ++combineGeneration;
                ++popupGeneration;
                combinedMarkup = null;
                combinedDisplayed = false;
                if (popupDialog != null) popupDialog.dismiss();
                if (browser != null) {
                    browser.stopLoading();
                    browser.setWebViewClient(new WebViewClient());
                    browser.setWebChromeClient(null);
                    if (browserFrame.indexOfChild(browser) >= 0) browserFrame.removeView(browser);
                    browser.destroy();
                    browser = null;
                }
                hideError();
                hideTransition(false);
            } else if (page == PLATFORM_PDD) {
                pddPage.resetSessionView();
            } else if (page == PLATFORM_JD) {
                jdPage.resetSessionView();
            }
            stopShimmer();
            updateAllCardStates();
            updateChrome();
            if (section == SECTION_MORE && accountOpen) renderContent();
        }
        @Override public void onFinished(int page, boolean success) {
            updateAllCardStates();
            updateChrome();
            if (!success) toast("退出未完成，请检查 WebView 更新后重试");
            if (!isFinishing() && !isDestroyed()) renderContent();
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        palette = Palette.of(this);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        WebView.setWebContentsDebuggingEnabled(false);
        restoreState(state);
        buildUi();
        applySystemBars();
        SiteDataClearer.observe(logoutListener);
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::goBack);
        }
        renderContent();
        if (pendingScrollPlatform >= 0) {
            final int platform = pendingScrollPlatform;
            pendingScrollPlatform = -1;
            contentHost.post(() -> scrollToCard(identityScroll, identityCards[platform].card));
        }
        if (state == null && getIntent() != null
                && getIntent().getStringExtra("provider") == null
                && getSharedPreferences(PREF_UI, MODE_PRIVATE)
                    .getBoolean(UpdateChecker.KEY_AUTO_UPDATE, true)) {
            contentHost.postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) UpdateChecker.check(this, false);
            }, 2500);
        }
    }

    private void applySystemBars() {
        getWindow().setStatusBarColor(palette.bg);
        getWindow().setNavigationBarColor(palette.navBg);
        int flags = palette.dark ? 0
            : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            android.view.WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                int mask = android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(palette.dark ? 0 : mask, mask);
            }
        } else {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private void restoreState(Bundle state) {
        android.content.SharedPreferences prefs = getSharedPreferences(PREF_UI, MODE_PRIVATE);
        signedIn[PLATFORM_TAOBAO] = prefs.getBoolean(KEY_LOGIN_TAOBAO, false);
        signedIn[PLATFORM_PDD] = prefs.getBoolean(KEY_LOGIN_PDD, false);
        signedIn[PLATFORM_JD] = prefs.getBoolean(KEY_LOGIN_JD, false);
        pddAppMode = prefs.getBoolean(KEY_PDD_APP_MODE, false);
        identityOrder = loadOrder(prefs, KEY_IDENTITY_ORDER,
            new int[]{PLATFORM_TAOBAO, PLATFORM_PDD, PLATFORM_CAINIAO});
        parcelsOrder = loadOrder(prefs, KEY_PARCELS_ORDER,
            new int[]{PLATFORM_TAOBAO, PLATFORM_PDD, PLATFORM_JD});
        loadHidden(identityHidden, prefs, KEY_IDENTITY_HIDDEN);
        loadHidden(parcelsHidden, prefs, KEY_PARCELS_HIDDEN);
        if (state != null) {
            section = clampSection(state.getInt("section", SECTION_IDENTITY));
            accountOpen = state.getBoolean("accountOpen", false);
            orderOpen = state.getBoolean("orderOpen", false);
            for (int i = 0; i < 4; i++) {
                identityExpanded[i] = state.getBoolean("identityExpanded" + i, false);
                parcelsExpanded[i] = state.getBoolean("parcelsExpanded" + i, false);
            }
            return;
        }
        for (int i = 0; i < 4; i++) {
            identityExpanded[i] = false;
            parcelsExpanded[i] = false;
        }
        resetExpansion(SECTION_IDENTITY);
        resetExpansion(SECTION_PARCELS);
        int startSection = prefs.getInt(KEY_START_SECTION, SECTION_IDENTITY);
        section = startSection < 0 ? clampSection(prefs.getInt(KEY_SECTION, SECTION_IDENTITY))
            : clampSection(startSection);
        String provider = getIntent() == null ? null : getIntent().getStringExtra("provider");
        if ("pinduoduo".equals(provider)) {
            section = SECTION_IDENTITY;
            identityHidden[PLATFORM_PDD] = false;
            persistHidden(SECTION_IDENTITY);
            for (int i = 0; i < 4; i++) identityExpanded[i] = false;
            identityExpanded[PLATFORM_PDD] = true;
            pendingScrollPlatform = PLATFORM_PDD;
        } else if ("taobao".equals(provider)) {
            section = SECTION_IDENTITY;
            identityHidden[PLATFORM_TAOBAO] = false;
            persistHidden(SECTION_IDENTITY);
            for (int i = 0; i < 4; i++) identityExpanded[i] = false;
            identityExpanded[PLATFORM_TAOBAO] = true;
            pendingScrollPlatform = PLATFORM_TAOBAO;
        }
    }

    private int[] loadOrder(android.content.SharedPreferences prefs, String key, int[] fallback) {
        String raw = prefs.getString(key, null);
        if (raw == null) return fallback.clone();
        String[] parts = raw.split(",");
        if (parts.length != fallback.length) return fallback.clone();
        int[] result = new int[parts.length];
        boolean[] seen = new boolean[4];
        for (int i = 0; i < parts.length; i++) {
            int value;
            try {
                value = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException invalid) {
                return fallback.clone();
            }
            if (value < 0 || value >= 4 || seen[value] || !orderContains(fallback, value)) {
                return fallback.clone();
            }
            seen[value] = true;
            result[i] = value;
        }
        return result;
    }

    private boolean orderContains(int[] values, int value) {
        for (int candidate : values) if (candidate == value) return true;
        return false;
    }

    private void loadHidden(boolean[] target, android.content.SharedPreferences prefs, String key) {
        for (int i = 0; i < target.length; i++) target[i] = false;
        String raw = prefs.getString(key, "");
        if (raw == null || raw.isEmpty()) return;
        for (String part : raw.split(",")) {
            try {
                int platform = Integer.parseInt(part.trim());
                if (platform >= 0 && platform < target.length) target[platform] = true;
            } catch (NumberFormatException invalid) {
                // Ignore malformed entries and keep the rest of the display preference.
            }
        }
    }

    private void persistHidden(int targetSection) {
        boolean[] hidden = targetSection == SECTION_IDENTITY ? identityHidden : parcelsHidden;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hidden.length; i++) {
            if (!hidden[i]) continue;
            if (builder.length() > 0) builder.append(',');
            builder.append(i);
        }
        getSharedPreferences(PREF_UI, MODE_PRIVATE).edit()
            .putString(targetSection == SECTION_IDENTITY ? KEY_IDENTITY_HIDDEN : KEY_PARCELS_HIDDEN,
                builder.toString())
            .apply();
    }

    private int clampSection(int value) {
        return value < 0 || value > SECTION_MORE ? SECTION_IDENTITY : value;
    }

    private void rememberState() {
        getSharedPreferences(PREF_UI, MODE_PRIVATE).edit().putInt(KEY_SECTION, section).apply();
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

    private GradientDrawable cardShape() {
        GradientDrawable shape = background(palette.surface, 18);
        shape.setStroke(Math.max(1, dp(1)), palette.border);
        return shape;
    }

    private Button button(String text, boolean primary) {
        Button view = new Button(this);
        view.setText(text);
        view.setAllCaps(false);
        view.setTextSize(14);
        view.setTextColor(primary ? onAccent() : palette.ink);
        view.setBackground(background(primary ? palette.accent : palette.surfaceAlt, 12));
        view.setMinHeight(0);
        view.setMinimumHeight(0);
        view.setPadding(dp(10), 0, dp(10), 0);
        return view;
    }

    private int onAccent() {
        return palette.dark ? palette.bg : Color.WHITE;
    }

    private LinearLayout.LayoutParams size(int width, int height) {
        return new LinearLayout.LayoutParams(width < 0 ? width : dp(width),
            height < 0 ? height : dp(height));
    }

    private void addDivider(ViewGroup parent) {
        View divider = new View(this);
        divider.setBackgroundColor(palette.border);
        if (parent instanceof LinearLayout) {
            parent.addView(divider, new LinearLayout.LayoutParams(-1, Math.max(1, dp(1))));
        } else {
            parent.addView(divider, new FrameLayout.LayoutParams(-1, Math.max(1, dp(1)), Gravity.BOTTOM));
        }
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(palette.bg);
        root.setFitsSystemWindows(false);
        if (Build.VERSION.SDK_INT >= 30) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets bars = insets.getInsets(android.view.WindowInsets.Type.systemBars()
                    | android.view.WindowInsets.Type.displayCutout());
                int keyboard = insets.getInsets(android.view.WindowInsets.Type.ime()).bottom;
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                applyKeyboardPadding(keyboard, bars.bottom);
                return insets;
            });
        } else {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                view.setPadding(insets.getStableInsetLeft(), insets.getStableInsetTop(),
                    insets.getStableInsetRight(), insets.getStableInsetBottom());
                return insets;
            });
            root.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                android.graphics.Rect visible = new android.graphics.Rect();
                root.getWindowVisibleDisplayFrame(visible);
                int[] position = new int[2];
                root.getLocationOnScreen(position);
                applyKeyboardPadding(Math.max(0, position[1] + root.getHeight() - visible.bottom),
                    root.getPaddingBottom());
            });
        }
        setContentView(root);

        buildHeader(root);
        contentHost = new FrameLayout(this);
        contentHost.setBackgroundColor(palette.bg);
        root.addView(contentHost, new LinearLayout.LayoutParams(-1, 0, 1f));
        buildBottomNav(root);
        buildWebContent();
        identityPage();
        parcelsPage();
        root.post(root::requestApplyInsets);
    }

    private void buildHeader(LinearLayout root) {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(10), dp(10), dp(4));
        header.setBackgroundColor(palette.bg);

        headerTitle = label("", 22, palette.ink, true);
        headerTitle.setSingleLine(true);
        header.addView(headerTitle, new LinearLayout.LayoutParams(0, -2, 1f));

        FrameLayout overflow = new FrameLayout(this);
        overflow.setClickable(true);
        overflow.setFocusable(true);
        overflow.setContentDescription("更多操作");
        ImageView dots = new ImageView(this);
        dots.setImageResource(R.drawable.ic_more_vertical);
        dots.setColorFilter(palette.muted, PorterDuff.Mode.SRC_IN);
        dots.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        overflow.addView(dots, new FrameLayout.LayoutParams(dp(22), dp(22), Gravity.CENTER));
        overflow.setOnClickListener(this::showOverflowMenu);
        header.addView(overflow, size(40, 40));

        root.addView(header);
    }

    private void showOverflowMenu(View anchor) {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable shape = background(palette.surface, 14);
        shape.setStroke(Math.max(1, dp(1)), palette.border);
        menu.setBackground(shape);
        menu.setElevation(dp(8));

        String[] items = {"切换主题", "排序", "关于"};
        final PopupWindow popup = new PopupWindow(menu, dp(168), LinearLayout.LayoutParams.WRAP_CONTENT, true);
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                View divider = new View(this);
                divider.setBackgroundColor(palette.border);
                menu.addView(divider, new LinearLayout.LayoutParams(-1, Math.max(1, dp(1))));
            }
            TextView item = label(items[i], 14, palette.ink, false);
            item.setPadding(dp(18), dp(14), dp(18), dp(14));
            item.setClickable(true);
            item.setFocusable(true);
            final int index = i;
            item.setOnClickListener(v -> {
                popup.dismiss();
                if (index == 0) showThemeDialog();
                else if (index == 1) openOrderPage();
                else startAbout();
            });
            menu.addView(item, new LinearLayout.LayoutParams(-1, -2));
        }
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(8));
        popup.showAsDropDown(anchor, anchor.getWidth() - dp(168), dp(4));
    }

    private void showThemeDialog() {
        final String[] modes = {"auto", "light", "dark"};
        final String[] labels = {"跟随系统", "浅色", "深色"};
        String current = Palette.mode(this);
        int checked = 0;
        for (int i = 0; i < modes.length; i++) if (modes[i].equals(current)) checked = i;
        new AlertDialog.Builder(this, dialogTheme())
            .setTitle("切换主题")
            .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                dialog.dismiss();
                if (!modes[which].equals(Palette.mode(MainActivity.this))) {
                    Palette.setMode(MainActivity.this, modes[which]);
                    recreate();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private String themeModeLabel() {
        String mode = Palette.mode(this);
        String label = "light".equals(mode) ? "浅色" : "dark".equals(mode) ? "深色" : "跟随系统";
        return "当前：" + label;
    }

    private String startSectionLabel() {
        int value = getSharedPreferences(PREF_UI, MODE_PRIVATE)
            .getInt(KEY_START_SECTION, SECTION_IDENTITY);
        String label = value == SECTION_IDENTITY ? "身份码"
            : value == SECTION_PARCELS ? "待取包裹"
            : value == SECTION_MORE ? "更多" : "上次停留";
        return "当前：" + label;
    }

    private void showStartSectionDialog() {
        final String[] labels = {"身份码", "待取包裹", "更多", "上次停留"};
        final int[] values = {SECTION_IDENTITY, SECTION_PARCELS, SECTION_MORE, -1};
        int stored = getSharedPreferences(PREF_UI, MODE_PRIVATE)
            .getInt(KEY_START_SECTION, SECTION_IDENTITY);
        int checked = 0;
        for (int i = 0; i < values.length; i++) if (values[i] == stored) checked = i;
        new AlertDialog.Builder(this, dialogTheme())
            .setTitle("默认页面")
            .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                getSharedPreferences(PREF_UI, MODE_PRIVATE).edit()
                    .putInt(KEY_START_SECTION, values[which]).apply();
                dialog.dismiss();
                renderContent();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private int dialogTheme() {
        return palette.dark ? R.style.AppDialogThemeDark : R.style.AppDialogThemeLight;
    }

    private void buildBottomNav(LinearLayout root) {
        addDivider(root);
        bottomBar = new LinearLayout(this);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setPadding(dp(8), dp(5), dp(8), dp(5));
        bottomBar.setBackgroundColor(palette.navBg);
        String[] labels = {"身份码", "待取包裹", "更多"};
        int[] icons = {R.drawable.ic_tab_identity, R.drawable.ic_tab_parcels, R.drawable.ic_tab_more};
        for (int i = 0; i < labels.length; i++) {
            final int target = i;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(4), dp(5), dp(4), dp(5));
            item.setClickable(true);
            item.setFocusable(true);
            item.setContentDescription("打开" + labels[i]);
            ImageView icon = new ImageView(this);
            icon.setImageResource(icons[i]);
            icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            TextView text = label(labels[i], 11, palette.muted, false);
            text.setGravity(Gravity.CENTER);
            item.addView(icon, size(22, 22));
            LinearLayout.LayoutParams textLayout = new LinearLayout.LayoutParams(-2, -2);
            textLayout.topMargin = dp(4);
            item.addView(text, textLayout);
            item.setOnClickListener(v -> selectSection(target));
            navItems[i] = item;
            navIcons[i] = icon;
            navLabels[i] = text;
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(0, dp(52), 1f);
            layout.setMargins(dp(4), 0, dp(4), 0);
            bottomBar.addView(item, layout);
        }
        root.addView(bottomBar);
    }

    private void buildWebContent() {
        browserFrame = new FrameLayout(this);
        browserFrame.setBackgroundColor(palette.surface);
        buildErrorPanel();
        buildTransitionPanel();

        pinduoduoFrame = new FrameLayout(this);
        pinduoduoFrame.setBackgroundColor(palette.surface);
        pddPage = new PddPage(this, this::openPinduoduoIdentity,
            value -> setSignedIn(PLATFORM_PDD, value));
        pinduoduoFrame.addView(pddPage, new FrameLayout.LayoutParams(-1, -1));

        jdFrame = new FrameLayout(this);
        jdFrame.setBackgroundColor(palette.surface);
        jdPage = new JdPage(this, value -> setSignedIn(PLATFORM_JD, value));
        jdFrame.addView(jdPage, new FrameLayout.LayoutParams(-1, -1));
    }

    private void identityPage() {
        if (identityScroll != null) return;
        identityScroll = buildSectionPage(SECTION_IDENTITY);
    }

    private void parcelsPage() {
        if (parcelsScroll != null) return;
        parcelsScroll = buildSectionPage(SECTION_PARCELS);
    }

    private ScrollView buildSectionPage(int targetSection) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(palette.bg);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(12), dp(12), dp(12), dp(12));
        PlatformCard[] cards = new PlatformCard[4];
        int[] order = targetSection == SECTION_IDENTITY ? identityOrder : parcelsOrder;
        boolean[] hidden = targetSection == SECTION_IDENTITY ? identityHidden : parcelsHidden;
        int visible = 0;
        for (int platform : order) {
            if (hidden[platform]) continue;
            cards[platform] = buildPlatformCard(targetSection, platform);
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(-1, -2);
            layout.bottomMargin = dp(12);
            column.addView(cards[platform].card, layout);
            visible++;
        }
        if (visible == 0) {
            LinearLayout empty = new LinearLayout(this);
            empty.setOrientation(LinearLayout.VERTICAL);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(24), dp(24), dp(24), dp(24));
            TextView emptyText = label("当前页面暂无显示的平台", 14, palette.muted, false);
            emptyText.setGravity(Gravity.CENTER);
            empty.addView(emptyText);
            TextView action = pill("去排序", true);
            action.setOnClickListener(v -> openOrderPage());
            LinearLayout.LayoutParams actionLayout = new LinearLayout.LayoutParams(-2, -2);
            actionLayout.topMargin = dp(14);
            empty.addView(action, actionLayout);
            column.addView(empty, new LinearLayout.LayoutParams(-1, dp(220)));
        }
        scroll.addView(column);
        if (targetSection == SECTION_IDENTITY) identityCards = cards;
        else parcelsCards = cards;
        return scroll;
    }

    private PlatformCard buildPlatformCard(int targetSection, int platform) {
        boolean webPlatform = platform <= PLATFORM_PDD;
        boolean cainiaoIdentity = targetSection == SECTION_IDENTITY && platform == PLATFORM_CAINIAO;
        boolean jdParcels = targetSection == SECTION_PARCELS && platform == PLATFORM_JD;
        boolean embedded = webPlatform || jdParcels;
        boolean available = embedded || cainiaoIdentity;
        boolean[] expanded = targetSection == SECTION_IDENTITY ? identityExpanded : parcelsExpanded;
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(cardShape());
        card.setPadding(dp(1), dp(1), dp(1), dp(1));
        card.setClipToOutline(true);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(12), dp(14), dp(12));
        header.setClickable(available);
        header.setFocusable(available);

        ImageView icon = new ImageView(this);
        icon.setImageResource(PLATFORM_ICONS[platform]);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        header.addView(icon, size(26, 26));

        TextView title = label(cardTitle(targetSection, platform), 15, palette.ink, true);
        LinearLayout.LayoutParams titleLayout = new LinearLayout.LayoutParams(0, -2, 1f);
        titleLayout.setMarginStart(dp(10));
        header.addView(title, titleLayout);

        String statusValue = available ? cardStatus(platform) : "即将上线";
        int statusColor = available ? cardStatusColor(platform) : palette.muted;
        TextView status = label(statusValue, 12, statusColor, false);
        LinearLayout.LayoutParams statusLayout = new LinearLayout.LayoutParams(-2, -2);
        statusLayout.setMarginEnd(dp(4));
        header.addView(status, statusLayout);

        if (platform == PLATFORM_PDD) {
            ImageView modeSwitch = new ImageView(this);
            modeSwitch.setImageResource(R.drawable.ic_switch_mode);
            modeSwitch.setColorFilter(palette.muted, PorterDuff.Mode.SRC_IN);
            modeSwitch.setPadding(dp(6), dp(6), dp(6), dp(6));
            modeSwitch.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            modeSwitch.setContentDescription("切换拼多多显示模式");
            modeSwitch.setClickable(true);
            modeSwitch.setFocusable(true);
            modeSwitch.setOnClickListener(v -> showPddModeDialog(false));
            LinearLayout.LayoutParams modeLayout = new LinearLayout.LayoutParams(dp(30), dp(30));
            modeLayout.setMarginStart(dp(2));
            header.addView(modeSwitch, modeLayout);
        }

        ImageView refresh = null;
        ImageView chevron = null;
        if (available) {
            final int target = targetSection;
            final int index = platform;

            if (embedded) {
                refresh = new ImageView(this);
                refresh.setImageResource(R.drawable.ic_refresh);
                refresh.setColorFilter(palette.muted, PorterDuff.Mode.SRC_IN);
                refresh.setPadding(dp(6), dp(6), dp(6), dp(6));
                refresh.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                refresh.setContentDescription("刷新" + cardTitle(targetSection, platform));
                refresh.setClickable(true);
                refresh.setFocusable(true);
                refresh.setVisibility(expanded[platform] && !(platform == PLATFORM_PDD && pddAppMode)
                    ? View.VISIBLE : View.GONE);
                refresh.setOnClickListener(v -> refreshPlatform(target, index));
                LinearLayout.LayoutParams refreshLayout = new LinearLayout.LayoutParams(dp(30), dp(30));
                refreshLayout.setMarginStart(dp(6));
                header.addView(refresh, refreshLayout);
            }

            chevron = new ImageView(this);
            chevron.setImageResource(R.drawable.ic_chevron_down);
            chevron.setColorFilter(palette.muted, PorterDuff.Mode.SRC_IN);
            chevron.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            chevron.setRotation(expanded[platform] ? 180f : 0f);
            LinearLayout.LayoutParams chevronLayout = new LinearLayout.LayoutParams(dp(18), dp(18));
            chevronLayout.setMarginStart(dp(4));
            header.addView(chevron, chevronLayout);

            header.setOnClickListener(v -> toggleCard(target, index));
        }
        card.addView(header);

        FrameLayout body = null;
        int bodyHeight = 0;
        if (embedded) {
            body = new CardBody(this);
            body.setBackgroundColor(palette.surface);
            bodyHeight = dp(300);
            body.setVisibility(expanded[platform] ? View.VISIBLE : View.GONE);
            card.addView(body, new LinearLayout.LayoutParams(-1, bodyHeight));
        } else if (cainiaoIdentity) {
            body = new FrameLayout(this);
            body.setBackgroundColor(palette.surface);
            bodyHeight = dp(240);
            body.setVisibility(expanded[platform] ? View.VISIBLE : View.GONE);
            body.addView(buildCainiaoPanel(), new FrameLayout.LayoutParams(-1, -1));
            card.addView(body, new LinearLayout.LayoutParams(-1, bodyHeight));
        } else {
            TextView hint = label("入口已预留，功能即将上线", 12, palette.muted, false);
            hint.setPadding(dp(50), 0, dp(14), dp(14));
            card.addView(hint);
            card.setAlpha(0.78f);
        }
        return new PlatformCard(targetSection, platform, card, body, bodyHeight, status, refresh, chevron);
    }

    private String cardTitle(int targetSection, int platform) {
        return targetSection == SECTION_IDENTITY
            ? PLATFORM_NAMES[platform] + "身份码"
            : PLATFORM_NAMES[platform] + "待取";
    }

    private String cardStatus(int platform) {
        if (platform == PLATFORM_CAINIAO) return "官方 App";
        if (platform == PLATFORM_PDD && pddAppMode) return "App 模式";
        return signedIn[platform] ? "已登录" : "未登录";
    }

    private int cardStatusColor(int platform) {
        if (platform == PLATFORM_CAINIAO) return palette.muted;
        if (platform == PLATFORM_PDD && pddAppMode) return palette.muted;
        return signedIn[platform] ? palette.accent : palette.muted;
    }

    private void toggleCard(int targetSection, int platform) {
        if (targetSection == SECTION_IDENTITY && platform == PLATFORM_JD) return;
        if (targetSection == SECTION_PARCELS && platform == PLATFORM_CAINIAO) return;
        boolean[] expanded = targetSection == SECTION_IDENTITY ? identityExpanded : parcelsExpanded;
        expanded[platform] = !expanded[platform];
        applyCardState(targetSection, platform, true);
        if (section != targetSection) return;
        if (!expanded[platform]) {
            InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(contentHost.getWindowToken(), 0);
            if (platform == PLATFORM_PDD) pddPage.idlePage();
            return;
        }
        if (platform == PLATFORM_PDD && !pddAppMode && !signedIn[PLATFORM_PDD]) {
            showPddModeDialog(true);
            return;
        }
        activatePlatform(platform);
    }

    private void showPddModeDialog(boolean fromLoginPrompt) {
        if (pddModeDialog != null && pddModeDialog.isShowing()) pddModeDialog.dismiss();

        final int[] choice = {pddAppMode ? 1 : 0};
        final String[] titles = {"内嵌显示（默认）", "跳转官方 App"};
        final String[] details = {
            "在应用内登录拼多多，可同时查看身份码和包裹；登录后可能挤掉拼多多 App 的登录。",
            "不在应用内登录，直接打开官方 App 查看；可与官方 App 同时保持登录。"
        };

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(4), dp(20), dp(4));

        TextView intro = label("拼多多同一账号只允许一个客户端在线，两种模式无法同时保持应用内和官方 App 的登录。",
            13, palette.muted, false);
        intro.setLineSpacing(dp(4), 1f);
        content.addView(intro);

        final TextView[] marks = new TextView[2];
        final LinearLayout[] rows = new LinearLayout[2];
        for (int i = 0; i < 2; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(12), dp(12), dp(12));
            row.setClickable(true);
            row.setFocusable(true);

            TextView mark = label("", 16, palette.muted, true);
            mark.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams markLayout = new LinearLayout.LayoutParams(dp(24), -2);
            markLayout.setMarginEnd(dp(8));
            row.addView(mark, markLayout);
            marks[i] = mark;

            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            TextView title = label(titles[i], 14, palette.ink, true);
            TextView detail = label(details[i], 12, palette.muted, false);
            detail.setLineSpacing(dp(3), 1f);
            LinearLayout.LayoutParams detailLayout = new LinearLayout.LayoutParams(-2, -2);
            detailLayout.topMargin = dp(4);
            texts.addView(title);
            texts.addView(detail, detailLayout);
            row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1f));

            final int index = i;
            row.setOnClickListener(v -> {
                choice[0] = index;
                for (int j = 0; j < 2; j++) {
                    boolean selected = j == index;
                    marks[j].setText(selected ? "●" : "○");
                    marks[j].setTextColor(selected ? palette.accent : palette.muted);
                    rows[j].setBackground(selected ? background(palette.accentSoft, 12) : null);
                }
            });
            rows[i] = row;
            LinearLayout.LayoutParams rowLayout = new LinearLayout.LayoutParams(-1, -2);
            rowLayout.topMargin = dp(10);
            content.addView(row, rowLayout);
        }
        for (int j = 0; j < 2; j++) {
            boolean selected = j == choice[0];
            marks[j].setText(selected ? "●" : "○");
            marks[j].setTextColor(selected ? palette.accent : palette.muted);
            rows[j].setBackground(selected ? background(palette.accentSoft, 12) : null);
        }

        AlertDialog dialog = new AlertDialog.Builder(this, dialogTheme())
            .setTitle("拼多多显示模式")
            .setView(content)
            .setNegativeButton("取消", (d, which) -> {
                if (fromLoginPrompt) collapsePddCard();
            })
            .setPositiveButton("确定", (d, which) -> setPddAppMode(choice[0] == 1))
            .create();
        dialog.setOnCancelListener(d -> {
            if (fromLoginPrompt) collapsePddCard();
        });
        dialog.setOnDismissListener(d -> {
            if (pddModeDialog == d) pddModeDialog = null;
        });
        dialog.show();
        pddModeDialog = dialog;
    }

    private void setPddAppMode(boolean app) {
        if (pddAppMode == app) {
            renderContent();
            return;
        }
        pddAppMode = app;
        getSharedPreferences(PREF_UI, MODE_PRIVATE).edit()
            .putBoolean(KEY_PDD_APP_MODE, app).apply();
        if (app) {
            if (!SiteDataClearer.isBusy() && SiteDataClearer.isSupported()) {
                SiteDataClearer.start(PLATFORM_PDD);
            } else {
                setSignedIn(PLATFORM_PDD, false);
            }
        }
        updateAllCardStates();
        renderContent();
    }

    private void collapsePddCard() {
        if (section == SECTION_MORE) return;
        boolean[] expanded = section == SECTION_IDENTITY ? identityExpanded : parcelsExpanded;
        expanded[PLATFORM_PDD] = false;
        applyCardState(section, PLATFORM_PDD, true);
        pddPage.idlePage();
    }

    private void refreshPlatform(int targetSection, int platform) {
        if (section != targetSection) return;
        String part = targetSection == SECTION_IDENTITY ? "identity" : "parcels";
        if (platform == PLATFORM_TAOBAO) {
            if (browser == null) {
                ensureBrowser();
                return;
            }
            browser.evaluateJavascript("(function(){return !!(window.HandyCodeRefreshPart&&window.HandyCodeRefreshPart('"
                + part + "'));})()", null);
        } else if (platform == PLATFORM_PDD) {
            pddPage.refreshPart(part);
        } else if (platform == PLATFORM_JD) {
            jdPage.refresh();
        }
    }

    private void applyCardState(int targetSection, int platform) {
        applyCardState(targetSection, platform, false);
    }

    private void applyCardState(int targetSection, int platform, boolean animate) {
        PlatformCard[] cards = targetSection == SECTION_IDENTITY ? identityCards : parcelsCards;
        boolean[] expanded = targetSection == SECTION_IDENTITY ? identityExpanded : parcelsExpanded;
        PlatformCard card = cards[platform];
        if (card == null) return;
        boolean open = expanded[platform];
        if (card.body != null) {
            if (animate) {
                animateCardBody(card, open);
            } else {
                if (card.bodyAnimator != null) {
                    ValueAnimator previous = card.bodyAnimator;
                    card.bodyAnimator = null;
                    previous.cancel();
                }
                card.body.animate().cancel();
                ViewGroup.LayoutParams params = card.body.getLayoutParams();
                if (params != null) {
                    params.height = card.bodyHeight;
                    card.body.setLayoutParams(params);
                }
                card.body.setAlpha(1f);
                card.body.setVisibility(open ? View.VISIBLE : View.GONE);
            }
        }
        if (card.refresh != null) {
            boolean showRefresh = open && !(platform == PLATFORM_PDD && pddAppMode);
            card.refresh.setVisibility(showRefresh ? View.VISIBLE : View.GONE);
        }
        if (card.chevron != null) {
            card.chevron.animate().rotation(open ? 180f : 0f)
                .setDuration(animate ? 220 : 160)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
        }
    }

    private void animateCardBody(PlatformCard card, boolean expand) {
        if (card.body == null || card.bodyHeight <= 0) return;
        if (card.bodyAnimator != null) {
            ValueAnimator previous = card.bodyAnimator;
            card.bodyAnimator = null;
            previous.cancel();
        }
        card.body.animate().cancel();
        ViewGroup.LayoutParams params = card.body.getLayoutParams();
        if (params == null) return;
        final int startHeight;
        if (expand) {
            params.height = 0;
            card.body.setLayoutParams(params);
            card.body.setVisibility(View.VISIBLE);
            card.body.setAlpha(0f);
            startHeight = 0;
        } else {
            startHeight = Math.max(0, card.body.getHeight());
            if (startHeight == 0) {
                card.body.setVisibility(View.GONE);
                card.body.setAlpha(1f);
                params.height = card.bodyHeight;
                card.body.setLayoutParams(params);
                return;
            }
        }
        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(expand ? 210 : 180);
        animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int height = expand
                ? Math.round(card.bodyHeight * fraction)
                : Math.round(startHeight * (1f - fraction));
            ViewGroup.LayoutParams bodyParams = card.body.getLayoutParams();
            if (bodyParams != null) {
                bodyParams.height = height;
                card.body.setLayoutParams(bodyParams);
            }
            card.body.setAlpha(expand ? fraction : 1f - fraction);
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                if (card.bodyAnimator != animator) return;
                card.bodyAnimator = null;
                ViewGroup.LayoutParams bodyParams = card.body.getLayoutParams();
                if (bodyParams != null) {
                    bodyParams.height = card.bodyHeight;
                    card.body.setLayoutParams(bodyParams);
                }
                card.body.setAlpha(1f);
                card.body.setVisibility(expand ? View.VISIBLE : View.GONE);
            }
        });
        card.bodyAnimator = animator;
        animator.start();
    }

    private void activatePlatform(int platform) {
        if (section == SECTION_MORE) return;
        if (platform == PLATFORM_TAOBAO) {
            if (!SiteDataClearer.isBusy()) ensureBrowser();
            applyTaobaoMode();
        } else if (platform == PLATFORM_PDD) {
            if (pddAppMode) return;
            if (!SiteDataClearer.isBusy()) {
                pddPage.showPage();
                pddPage.resumePage(true);
                applyPddMode();
            }
        } else if (platform == PLATFORM_JD) {
            if (section == SECTION_PARCELS && !SiteDataClearer.isBusy()) {
                jdPage.showPage(!signedIn[PLATFORM_JD]);
                jdPage.resumePage(true);
            }
        }
    }

    private boolean isExpanded(int platform) {
        if (section == SECTION_MORE || platform < 0 || platform >= 4) return false;
        boolean[] hidden = section == SECTION_IDENTITY ? identityHidden : parcelsHidden;
        if (hidden[platform]) return false;
        return (section == SECTION_IDENTITY ? identityExpanded : parcelsExpanded)[platform];
    }

    private void selectSection(int target) {
        if (SiteDataClearer.isBusy()) return;
        if (popupDialog != null) popupDialog.dismiss();
        boolean subpage = accountOpen || orderOpen;
        if (target == section && !subpage) return;
        int direction = target == section ? -1 : (target > section ? 1 : -1);
        runPageTransition(direction, () -> {
            accountOpen = false;
            orderOpen = false;
            section = target;
            rememberState();
            InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(contentHost.getWindowToken(), 0);
            renderContent();
        });
    }

    private void openAccountPage() {
        if (accountOpen && section == SECTION_MORE) return;
        runPageTransition(1, () -> {
            section = SECTION_MORE;
            accountOpen = true;
            orderOpen = false;
            rememberState();
            renderContent();
        });
    }

    private void openOrderPage() {
        if (orderOpen && section == SECTION_MORE) return;
        runPageTransition(1, () -> {
            section = SECTION_MORE;
            accountOpen = false;
            orderOpen = true;
            rememberState();
            renderContent();
        });
    }

    private void closeMoreSubpage() {
        runPageTransition(-1, () -> {
            accountOpen = false;
            orderOpen = false;
            renderContent();
        });
    }

    private void runPageTransition(int direction, Runnable render) {
        if (contentHost == null) {
            render.run();
            return;
        }
        final int generation = ++pageTransitionGeneration;
        final View old = contentHost.getChildAt(0);
        if (old == null) {
            render.run();
            return;
        }
        old.animate().cancel();
        old.animate()
            .alpha(0f)
            .translationX(-direction * dp(18))
            .setDuration(100)
            .setInterpolator(new android.view.animation.DecelerateInterpolator())
            .withEndAction(() -> {
                if (generation != pageTransitionGeneration || old.getParent() != contentHost) return;
                render.run();
                if (generation != pageTransitionGeneration) return;
                View page = contentHost.getChildAt(0);
                if (page == null) return;
                page.animate().cancel();
                page.setAlpha(0f);
                page.setTranslationX(direction * dp(18));
                page.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(200)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
            })
            .start();
    }

    private void startAbout() {
        startActivity(new android.content.Intent(this, AboutActivity.class));
    }

    private void preparePage(View page) {
        page.animate().cancel();
        page.setAlpha(1f);
        page.setTranslationX(0f);
    }

    private void renderContent() {
        if (contentHost == null) return;
        contentHost.removeAllViews();
        detachWebViews();
        if (pddAppPanel != null && pddAppPanel.getParent() instanceof ViewGroup) {
            ((ViewGroup) pddAppPanel.getParent()).removeView(pddAppPanel);
        }
        pddAppPanel = null;
        if (section == SECTION_MORE) {
            pddPage.idlePage();
            View page = orderOpen ? buildOrderView()
                : accountOpen ? buildAccountView() : buildMoreView();
            preparePage(page);
            contentHost.addView(page, new FrameLayout.LayoutParams(-1, -1));
            updateAllCardStates();
            updateChrome();
            return;
        }
        if (section == SECTION_IDENTITY) identityPage();
        else parcelsPage();
        View page = section == SECTION_IDENTITY ? identityScroll : parcelsScroll;
        preparePage(page);
        contentHost.addView(page, new FrameLayout.LayoutParams(-1, -1));
        attachWebViews(section == SECTION_IDENTITY ? identityCards : parcelsCards);
        if (pddAppMode) addPddAppPanel(section);
        updateAllCardStates();
        updateChrome();
        if (!SiteDataClearer.isBusy()) {
            if (isExpanded(PLATFORM_TAOBAO)) activatePlatform(PLATFORM_TAOBAO);
            if (isExpanded(PLATFORM_PDD) && !pddAppMode) activatePlatform(PLATFORM_PDD);
            else pddPage.idlePage();
            if (section == SECTION_PARCELS && isExpanded(PLATFORM_JD)) {
                activatePlatform(PLATFORM_JD);
            } else {
                jdPage.idlePage();
            }
        }
        applyWebBackground();
    }

    private void detachWebViews() {
        if (browserFrame != null && browserFrame.getParent() instanceof ViewGroup) {
            ((ViewGroup) browserFrame.getParent()).removeView(browserFrame);
        }
        if (pinduoduoFrame != null && pinduoduoFrame.getParent() instanceof ViewGroup) {
            ((ViewGroup) pinduoduoFrame.getParent()).removeView(pinduoduoFrame);
        }
        if (jdFrame != null && jdFrame.getParent() instanceof ViewGroup) {
            ((ViewGroup) jdFrame.getParent()).removeView(jdFrame);
        }
    }

    private void attachWebViews(PlatformCard[] cards) {
        PlatformCard taobao = cards[PLATFORM_TAOBAO];
        if (browserFrame != null && taobao != null && taobao.body != null) {
            taobao.body.addView(browserFrame, new FrameLayout.LayoutParams(-1, -1));
        }
        PlatformCard pdd = cards[PLATFORM_PDD];
        if (!pddAppMode && pinduoduoFrame != null && pdd != null && pdd.body != null) {
            pdd.body.addView(pinduoduoFrame, new FrameLayout.LayoutParams(-1, -1));
        }
        PlatformCard jd = cards[PLATFORM_JD];
        if (jd != null && jd.body != null && jdFrame != null) {
            jd.body.addView(jdFrame, new FrameLayout.LayoutParams(-1, -1));
        }
    }

    private void addPddAppPanel(int targetSection) {
        PlatformCard[] cards = targetSection == SECTION_IDENTITY ? identityCards : parcelsCards;
        PlatformCard card = cards[PLATFORM_PDD];
        if (card == null || card.body == null) return;
        pddAppPanel = buildPddAppPanel(targetSection);
        card.body.addView(pddAppPanel, new FrameLayout.LayoutParams(-1, -1));
    }

    private View buildPddAppPanel(int targetSection) {
        boolean parcels = targetSection == SECTION_PARCELS;
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setBackgroundColor(palette.surface);
        panel.setPadding(dp(20), dp(14), dp(20), dp(14));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_pinduoduo);
        panel.addView(icon, size(36, 36));

        TextView title = label(parcels ? "拼多多 · App 模式" : "拼多多 · 官方 App 模式",
            14, palette.ink, true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLayout = new LinearLayout.LayoutParams(-2, -2);
        titleLayout.topMargin = dp(8);
        panel.addView(title, titleLayout);

        TextView hint = label(parcels
            ? "当前模式不支持查看待取包裹。\n切换到内嵌模式后可在应用内查看。"
            : "应用内不登录拼多多，\n避免与官方 App 互相挤下线。", 12, palette.muted, false);
        hint.setGravity(Gravity.CENTER);
        hint.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams hintLayout = new LinearLayout.LayoutParams(-2, -2);
        hintLayout.topMargin = dp(6);
        panel.addView(hint, hintLayout);

        TextView action = pill(parcels ? "切换模式" : "打开拼多多看身份码", true);
        if (parcels) action.setOnClickListener(v -> showPddModeDialog(false));
        else action.setOnClickListener(v -> openPddApp(SECTION_IDENTITY));
        LinearLayout.LayoutParams actionLayout = new LinearLayout.LayoutParams(-2, -2);
        actionLayout.topMargin = dp(14);
        panel.addView(action, actionLayout);
        return panel;
    }

    private void openPddApp(int targetSection) {
        String[] appUris = targetSection == SECTION_IDENTITY
            ? new String[]{PDD_IDENTITY_DEEPLINK, PDD_IDENTITY_DEEPLINK_FALLBACK, PDD_PACKAGE_DEEPLINK}
            : new String[]{PDD_PACKAGE_DEEPLINK};
        for (String uri : appUris) {
            if (tryOpenPddUri(uri, true)) return;
        }
        android.content.Intent launch = getPackageManager().getLaunchIntentForPackage(PDD_PACKAGE);
        if (launch != null) {
            launch.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
            try {
                startActivity(launch);
                toast("已打开拼多多，请在 App 内查看");
                return;
            } catch (android.content.ActivityNotFoundException | SecurityException unavailable) {
                // Fall through to the web page.
            }
        }
        if (tryOpenPddUri(PDD_WEB_FALLBACK, true)) {
            toast("已在拼多多中打开官方页面");
            return;
        }
        if (tryOpenPddUri(PDD_WEB_FALLBACK, false)) {
            toast("已用浏览器打开拼多多页面");
            return;
        }
        toast("未安装拼多多官方 App，可切换为内嵌显示");
    }

    private boolean tryOpenPddUri(String uri, boolean limitToPdd) {
        android.content.Intent intent = new android.content.Intent(
            android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri));
        if (limitToPdd) intent.setPackage(PDD_PACKAGE);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (intent.resolveActivity(getPackageManager()) == null) return false;
        try {
            startActivity(intent);
            return true;
        } catch (android.content.ActivityNotFoundException | SecurityException
                 | IllegalArgumentException unavailable) {
            return false;
        }
    }

    private View buildCainiaoPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setBackgroundColor(palette.surface);
        panel.setPadding(dp(20), dp(12), dp(20), dp(12));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_cainiao);
        panel.addView(icon, size(36, 36));

        TextView title = label("菜鸟 · 官方 App 直达", 14, palette.ink, true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLayout = new LinearLayout.LayoutParams(-2, -2);
        titleLayout.topMargin = dp(8);
        panel.addView(title, titleLayout);

        TextView hint = label("菜鸟身份码需要在菜鸟 App 内查看，\n应用会直接打开身份码页面。", 12, palette.muted, false);
        hint.setGravity(Gravity.CENTER);
        hint.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams hintLayout = new LinearLayout.LayoutParams(-2, -2);
        hintLayout.topMargin = dp(6);
        panel.addView(hint, hintLayout);

        TextView open = pill("打开菜鸟看身份码", true);
        open.setOnClickListener(v -> openCainiaoIdentity());
        LinearLayout.LayoutParams openLayout = new LinearLayout.LayoutParams(-2, -2);
        openLayout.topMargin = dp(14);
        panel.addView(open, openLayout);
        return panel;
    }

    private void openCainiaoIdentity() {
        if (tryOpenCainiaoActivity(CAINIAO_IDENTITY_ACTIVITY)) return;
        if (tryOpenCainiaoActivity(CAINIAO_HOME_ACTIVITY)) {
            toast("已打开菜鸟，请在 App 内查看身份码");
            return;
        }
        android.content.Intent launch = getPackageManager().getLaunchIntentForPackage(CAINIAO_PACKAGE);
        if (launch != null) {
            launch.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
            try {
                startActivity(launch);
                toast("已打开菜鸟 App，请在 App 内查看身份码");
                return;
            } catch (android.content.ActivityNotFoundException | SecurityException unavailable) {
                // Fall through to the web page.
            }
        }
        try {
            android.content.Intent web = new android.content.Intent(android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(CAINIAO_WEB_FALLBACK));
            if (web.resolveActivity(getPackageManager()) != null) {
                startActivity(web);
                toast("已用浏览器打开菜鸟身份码页面");
                return;
            }
        } catch (android.content.ActivityNotFoundException | SecurityException
                 | IllegalArgumentException unavailable) {
            // Fall through to the final message.
        }
        toast("未安装菜鸟 App，可稍后重试");
    }

    private boolean tryOpenCainiaoActivity(String activityName) {
        return tryOpenExplicitActivity(CAINIAO_PACKAGE, activityName);
    }

    private boolean tryOpenExplicitActivity(String packageName, String activityName) {
        android.content.Intent intent = new android.content.Intent();
        intent.setComponent(new android.content.ComponentName(packageName, activityName));
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (intent.resolveActivity(getPackageManager()) == null) return false;
        try {
            startActivity(intent);
            return true;
        } catch (android.content.ActivityNotFoundException | SecurityException
                 | IllegalArgumentException unavailable) {
            return false;
        }
    }

    private void openJdPending() {
        if (tryOpenJdUri(JD_PENDING_DEEPLINK, true)) return;
        android.content.Intent launch = getPackageManager().getLaunchIntentForPackage(JD_PACKAGE);
        if (launch != null) {
            launch.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
            try {
                startActivity(launch);
                toast("已打开京东，请在订单页查看");
                return;
            } catch (android.content.ActivityNotFoundException | SecurityException unavailable) {
                // Fall through to the web page.
            }
        }
        if (tryOpenJdUri(JD_WEB_FALLBACK, true)) {
            toast("已在京东中打开订单列表");
            return;
        }
        if (tryOpenJdUri(JD_WEB_FALLBACK, false)) {
            toast("已用浏览器打开京东订单列表");
            return;
        }
        toast("未安装京东 App，可稍后重试");
    }

    private boolean tryOpenJdUri(String uri, boolean limitToJd) {
        android.content.Intent intent = new android.content.Intent(
            android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri));
        if (limitToJd) intent.setPackage(JD_PACKAGE);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (intent.resolveActivity(getPackageManager()) == null) return false;
        try {
            startActivity(intent);
            return true;
        } catch (android.content.ActivityNotFoundException | SecurityException
                 | IllegalArgumentException unavailable) {
            return false;
        }
    }

    private void openTaobaoOrders() {
        if (tryOpenExplicitActivity(TAOBAO_PACKAGE, TAOBAO_ORDER_ACTIVITY)) return;
        openAppOrderPage("淘宝", TAOBAO_PACKAGE,
            new String[]{TAOBAO_ORDER_DEEPLINK, TAOBAO_ORDER_DEEPLINK_FALLBACK});
    }

    private void openPinduoduoOrders() {
        openAppOrderPage("拼多多", PDD_PACKAGE,
            new String[]{PDD_ORDER_DEEPLINK});
    }

    private void openXhsOrders() {
        openAppOrderPage("小红书", XHS_PACKAGE,
            new String[]{XHS_ORDER_DEEPLINK});
    }

    private void openMeituanOrders() {
        openAppOrderPage("美团", MEITUAN_PACKAGE,
            new String[]{MEITUAN_ORDER_DEEPLINK});
    }

    private void openDouyinOrders() {
        openAppOrderPage("抖音", DOUYIN_PACKAGE,
            new String[]{DOUYIN_ORDER_DEEPLINK});
    }

    private void openCainiaoHome() {
        if (tryOpenCainiaoActivity(CAINIAO_HOME_ACTIVITY)) return;
        if (launchPackage(CAINIAO_PACKAGE)) {
            toast("已打开菜鸟 App");
            return;
        }
        toast("未安装菜鸟 App");
    }

    private void openAppOrderPage(String appLabel, String packageName, String[] deepLinks) {
        for (String deepLink : deepLinks) {
            if (tryOpenPackageUri(deepLink, packageName)) return;
        }
        if (launchPackage(packageName)) {
            toast("已打开" + appLabel + "，请在订单页查看");
            return;
        }
        toast("未安装" + appLabel + " App");
    }

    private boolean tryOpenPackageUri(String uri, String packageName) {
        android.content.Intent intent = new android.content.Intent(
            android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri));
        intent.setPackage(packageName);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (intent.resolveActivity(getPackageManager()) == null) return false;
        try {
            startActivity(intent);
            return true;
        } catch (android.content.ActivityNotFoundException | SecurityException
                 | IllegalArgumentException unavailable) {
            return false;
        }
    }

    private boolean launchPackage(String packageName) {
        android.content.Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launch == null) return false;
        launch.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            startActivity(launch);
            return true;
        } catch (android.content.ActivityNotFoundException | SecurityException
                 | IllegalArgumentException unavailable) {
            return false;
        }
    }

    private void updateAllCardStates() {
        updateCards(SECTION_IDENTITY, identityCards, identityExpanded);
        updateCards(SECTION_PARCELS, parcelsCards, parcelsExpanded);
    }

    private void updateCards(int targetSection, PlatformCard[] cards, boolean[] expanded) {
        if (cards == null) return;
        boolean identity = targetSection == SECTION_IDENTITY;
        for (int platform = 0; platform < cards.length; platform++) {
            PlatformCard card = cards[platform];
            if (card == null) continue;
            boolean cainiaoIdentity = identity && platform == PLATFORM_CAINIAO;
            boolean jdParcels = !identity && platform == PLATFORM_JD;
            if (platform > PLATFORM_PDD && !cainiaoIdentity && !jdParcels) {
                card.status.setText("即将上线");
                card.status.setTextColor(palette.muted);
                continue;
            }
            card.status.setText(cardStatus(platform));
            card.status.setTextColor(cardStatusColor(platform));
            applyCardState(targetSection, platform);
        }
    }

    private void updateChrome() {
        if (headerTitle != null) {
            String title = section == SECTION_IDENTITY ? "身份码"
                : section == SECTION_PARCELS ? "待取包裹"
                : orderOpen ? "排序"
                : accountOpen ? "账号绑定管理" : "更多";
            if (!title.contentEquals(headerTitle.getText())) {
                headerTitle.animate().cancel();
                headerTitle.setText(title);
                headerTitle.setAlpha(0f);
                headerTitle.setTranslationY(-dp(4));
                headerTitle.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(180)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
            }
        }
        for (int i = 0; i < navItems.length; i++) {
            boolean active = section == i;
            navItems[i].setBackground(active ? background(palette.accentSoft, 16) : null);
            navIcons[i].setColorFilter(active ? palette.accent : palette.muted, PorterDuff.Mode.SRC_IN);
            navIcons[i].animate().cancel();
            navIcons[i].animate()
                .scaleX(active ? 1.06f : 1f)
                .scaleY(active ? 1.06f : 1f)
                .setDuration(150)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
            navLabels[i].setTextColor(active ? palette.accent : palette.muted);
            navLabels[i].setTypeface(Typeface.DEFAULT, active ? Typeface.BOLD : Typeface.NORMAL);
            navItems[i].setSelected(active);
        }
    }

    private void scrollToCard(ScrollView scroll, View card) {
        if (scroll == null || card == null) return;
        scroll.post(() -> scroll.smoothScrollTo(0, Math.max(0, card.getTop() - dp(12))));
    }

    private View buildMoreView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(palette.bg);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(16), dp(18), dp(16), dp(18));

        TextView group = label("账号与登录", 12, palette.muted, false);
        group.setLetterSpacing(0.08f);
        column.addView(group);

        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(cardShape());
        card.setClickable(true);
        card.setFocusable(true);
        card.setContentDescription("账号绑定管理");

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_account);
        icon.setColorFilter(palette.accent, PorterDuff.Mode.SRC_IN);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        card.addView(icon, size(24, 24));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textsLayout = new LinearLayout.LayoutParams(0, -2, 1f);
        textsLayout.setMarginStart(dp(12));
        TextView title = label("账号绑定管理", 15, palette.ink, true);
        TextView subtitle = label("管理淘宝、拼多多等平台的登录状态", 12, palette.muted, false);
        LinearLayout.LayoutParams subtitleLayout = new LinearLayout.LayoutParams(-2, -2);
        subtitleLayout.topMargin = dp(4);
        texts.addView(title);
        texts.addView(subtitle, subtitleLayout);
        card.addView(texts, textsLayout);

        TextView chevron = label("›", 22, palette.muted, false);
        card.addView(chevron);
        card.setOnClickListener(v -> openAccountPage());

        LinearLayout.LayoutParams cardLayout = new LinearLayout.LayoutParams(-1, -2);
        cardLayout.topMargin = dp(10);
        column.addView(card, cardLayout);

        TextView entryGroup = label("订单快捷入口", 12, palette.muted, false);
        entryGroup.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams entryGroupLayout = new LinearLayout.LayoutParams(-1, -2);
        entryGroupLayout.topMargin = dp(22);
        column.addView(entryGroup, entryGroupLayout);

        addShortcutCard(column, R.drawable.ic_cainiao, "打开菜鸟",
            "跳过开屏广告，直接进入菜鸟首页", this::openCainiaoHome);
        addShortcutCard(column, R.drawable.ic_taobao, "淘宝订单",
            "使用淘宝 App 查看订单", this::openTaobaoOrders);
        addShortcutCard(column, R.drawable.ic_pinduoduo, "拼多多订单",
            "使用拼多多 App 查看订单", this::openPinduoduoOrders);
        addShortcutCard(column, R.drawable.ic_jd, "京东订单",
            "使用京东 App 查看订单", this::openJdPending);
        addShortcutCard(column, R.drawable.ic_xiaohongshu, "小红书订单",
            "使用小红书 App 查看订单", this::openXhsOrders);
        addShortcutCard(column, R.drawable.ic_meituan, "美团订单",
            "使用美团 App 查看订单", this::openMeituanOrders);
        addShortcutCard(column, R.drawable.ic_douyin, "抖音订单",
            "使用抖音 App 查看订单", this::openDouyinOrders);

        TextView displayGroup = label("显示", 12, palette.muted, false);
        displayGroup.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams displayLayout = new LinearLayout.LayoutParams(-1, -2);
        displayLayout.topMargin = dp(22);
        column.addView(displayGroup, displayLayout);

        addShortcutCard(column, R.drawable.ic_sort, "平台排序",
            "调整身份码与待取包裹的平台顺序", this::openOrderPage, true);
        addShortcutCard(column, R.drawable.ic_theme, "主题切换",
            themeModeLabel(), this::showThemeDialog, true);
        addShortcutCard(column, R.drawable.ic_home, "默认页面",
            startSectionLabel(), this::showStartSectionDialog, true);

        TextView aboutGroup = label("关于", 12, palette.muted, false);
        aboutGroup.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams aboutGroupLayout = new LinearLayout.LayoutParams(-1, -2);
        aboutGroupLayout.topMargin = dp(22);
        column.addView(aboutGroup, aboutGroupLayout);

        addShortcutCard(column, R.drawable.ic_about, "关于",
            "版本、开发者与项目地址", this::startAbout, true);
        addShortcutCard(column, R.drawable.ic_refresh, "检查更新",
            "与 GitHub 最新版本比对", () -> UpdateChecker.check(this, true), true);

        TextView footer = label(getString(R.string.app_name) + "  " + versionName(), 11,
            palette.muted, false);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerLayout = new LinearLayout.LayoutParams(-1, -2);
        footerLayout.topMargin = dp(28);
        column.addView(footer, footerLayout);
        scroll.addView(column);
        return scroll;
    }

    private void addShortcutCard(LinearLayout column, int iconRes, String titleText,
                                 String subtitleText, Runnable action) {
        addShortcutCard(column, iconRes, titleText, subtitleText, action, false);
    }

    private void addShortcutCard(LinearLayout column, int iconRes, String titleText,
                                 String subtitleText, Runnable action, boolean tintIcon) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(cardShape());
        card.setClickable(true);
        card.setFocusable(true);
        card.setContentDescription(titleText);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        if (tintIcon) icon.setColorFilter(palette.accent, PorterDuff.Mode.SRC_IN);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        card.addView(icon, size(24, 24));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textsLayout = new LinearLayout.LayoutParams(0, -2, 1f);
        textsLayout.setMarginStart(dp(12));
        TextView title = label(titleText, 15, palette.ink, true);
        TextView subtitle = label(subtitleText, 12, palette.muted, false);
        LinearLayout.LayoutParams subtitleLayout = new LinearLayout.LayoutParams(-2, -2);
        subtitleLayout.topMargin = dp(4);
        texts.addView(title);
        texts.addView(subtitle, subtitleLayout);
        card.addView(texts, textsLayout);

        card.addView(label("›", 22, palette.muted, false));
        card.setOnClickListener(v -> action.run());

        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(-1, -2);
        layout.topMargin = dp(10);
        column.addView(card, layout);
    }

    private View buildAccountView() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(palette.bg);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(8), dp(6), dp(14), dp(2));
        bar.setBackgroundColor(palette.bg);
        TextView back = label("‹ 返回", 14, palette.accent, true);
        back.setPadding(dp(8), dp(8), dp(8), dp(8));
        back.setClickable(true);
        back.setFocusable(true);
        back.setContentDescription("返回更多");
        back.setOnClickListener(v -> closeMoreSubpage());
        bar.addView(back);
        page.addView(bar);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(16), dp(16), dp(16), dp(18));

        TextView note = label("登录会在各平台官方网页中完成，应用不保存账号、密码或验证码。",
            12, palette.muted, false);
        note.setLineSpacing(dp(4), 1f);
        column.addView(note);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(cardShape());
        LinearLayout.LayoutParams cardLayout = new LinearLayout.LayoutParams(-1, -2);
        cardLayout.topMargin = dp(14);
        for (int platform = 0; platform < PLATFORM_NAMES.length; platform++) {
            if (platform > 0) {
                View divider = new View(this);
                divider.setBackgroundColor(palette.border);
                card.addView(divider, new LinearLayout.LayoutParams(-1, Math.max(1, dp(1))));
            }
            card.addView(buildAccountRow(platform));
        }
        column.addView(card, cardLayout);
        scroll.addView(column);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        return page;
    }

    private View buildOrderView() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(palette.bg);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(8), dp(6), dp(14), dp(2));
        bar.setBackgroundColor(palette.bg);
        TextView back = label("‹ 返回", 14, palette.accent, true);
        back.setPadding(dp(8), dp(8), dp(8), dp(8));
        back.setClickable(true);
        back.setFocusable(true);
        back.setContentDescription("返回更多");
        back.setOnClickListener(v -> closeMoreSubpage());
        bar.addView(back);
        page.addView(bar);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(16), dp(12), dp(16), dp(18));

        TextView note = label("每个页面排序第一的平台默认展开，其他平台默认折叠。可隐藏不需要的平台，排序与显示设置会即时保存。",
            12, palette.muted, false);
        note.setLineSpacing(dp(4), 1f);
        column.addView(note);

        addOrderSection(column, SECTION_IDENTITY, "身份码");
        addOrderSection(column, SECTION_PARCELS, "待取包裹");

        scroll.addView(column);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        return page;
    }

    private void addOrderSection(LinearLayout column, int targetSection, String titleText) {
        TextView group = label(titleText, 12, palette.muted, false);
        group.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams groupLayout = new LinearLayout.LayoutParams(-1, -2);
        groupLayout.topMargin = dp(20);
        column.addView(group, groupLayout);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(cardShape());
        int[] order = targetSection == SECTION_IDENTITY ? identityOrder : parcelsOrder;
        for (int i = 0; i < order.length; i++) {
            if (i > 0) {
                View divider = new View(this);
                divider.setBackgroundColor(palette.border);
                card.addView(divider, new LinearLayout.LayoutParams(-1, Math.max(1, dp(1))));
            }
            card.addView(buildOrderRow(targetSection, order, i));
        }
        LinearLayout.LayoutParams cardLayout = new LinearLayout.LayoutParams(-1, -2);
        cardLayout.topMargin = dp(10);
        column.addView(card, cardLayout);
    }

    private View buildOrderRow(int targetSection, int[] order, int position) {
        int platform = order[position];
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));

        ImageView icon = new ImageView(this);
        icon.setImageResource(PLATFORM_ICONS[platform]);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(icon, size(24, 24));

        TextView name = label(PLATFORM_NAMES[platform], 15, palette.ink, true);
        LinearLayout.LayoutParams nameLayout = new LinearLayout.LayoutParams(0, -2, 1f);
        nameLayout.setMarginStart(dp(10));
        row.addView(name, nameLayout);

        boolean[] hidden = targetSection == SECTION_IDENTITY ? identityHidden : parcelsHidden;
        boolean isHidden = hidden[platform];
        TextView visibility = label(isHidden ? "显示" : "隐藏", 11,
            isHidden ? palette.accent : palette.muted, true);
        visibility.setGravity(Gravity.CENTER);
        visibility.setPadding(dp(9), dp(4), dp(9), dp(4));
        visibility.setBackground(background(isHidden ? palette.accentSoft : palette.surfaceAlt, 10));
        visibility.setClickable(true);
        visibility.setFocusable(true);
        visibility.setContentDescription((isHidden ? "显示" : "隐藏") + PLATFORM_NAMES[platform]);
        visibility.setOnClickListener(v -> togglePlatformVisibility(targetSection, platform));
        LinearLayout.LayoutParams visibilityLayout = new LinearLayout.LayoutParams(-2, -2);
        visibilityLayout.setMarginStart(dp(4));
        row.addView(visibility, visibilityLayout);

        TextView up = orderArrow("↑", position > 0);
        up.setContentDescription("上移" + PLATFORM_NAMES[platform]);
        if (position > 0) {
            up.setOnClickListener(v -> moveOrderPlatform(targetSection, platform, -1));
        }
        LinearLayout.LayoutParams upLayout = new LinearLayout.LayoutParams(-2, -2);
        upLayout.setMarginStart(dp(6));
        row.addView(up, upLayout);

        TextView down = orderArrow("↓", position < order.length - 1);
        down.setContentDescription("下移" + PLATFORM_NAMES[platform]);
        if (position < order.length - 1) {
            down.setOnClickListener(v -> moveOrderPlatform(targetSection, platform, 1));
        }
        LinearLayout.LayoutParams downLayout = new LinearLayout.LayoutParams(-2, -2);
        downLayout.setMarginStart(dp(6));
        row.addView(down, downLayout);
        return row;
    }

    private TextView orderArrow(String arrow, boolean enabled) {
        TextView view = label(arrow, 15, enabled ? palette.accent : palette.muted, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(10), dp(2), dp(10), dp(2));
        view.setBackground(background(palette.accentSoft, 10));
        view.setAlpha(enabled ? 1f : .3f);
        view.setClickable(enabled);
        view.setFocusable(enabled);
        return view;
    }

    private void moveOrderPlatform(int targetSection, int platform, int delta) {
        int[] order = targetSection == SECTION_IDENTITY ? identityOrder : parcelsOrder;
        int position = -1;
        for (int i = 0; i < order.length; i++) {
            if (order[i] == platform) {
                position = i;
                break;
            }
        }
        int target = position + delta;
        if (position < 0 || target < 0 || target >= order.length) return;
        int temporary = order[position];
        order[position] = order[target];
        order[target] = temporary;
        persistOrder(targetSection);
        resetExpansion(targetSection);
        identityScroll = null;
        parcelsScroll = null;
        renderContent();
    }

    private void togglePlatformVisibility(int targetSection, int platform) {
        boolean[] hidden = targetSection == SECTION_IDENTITY ? identityHidden : parcelsHidden;
        hidden[platform] = !hidden[platform];
        persistHidden(targetSection);
        resetExpansion(targetSection);
        identityScroll = null;
        parcelsScroll = null;
        renderContent();
    }

    private void persistOrder(int targetSection) {
        int[] order = targetSection == SECTION_IDENTITY ? identityOrder : parcelsOrder;
        StringBuilder builder = new StringBuilder();
        for (int platform : order) {
            if (builder.length() > 0) builder.append(',');
            builder.append(platform);
        }
        getSharedPreferences(PREF_UI, MODE_PRIVATE).edit()
            .putString(targetSection == SECTION_IDENTITY ? KEY_IDENTITY_ORDER : KEY_PARCELS_ORDER,
                builder.toString())
            .apply();
    }

    private void resetExpansion(int targetSection) {
        boolean[] expanded = targetSection == SECTION_IDENTITY ? identityExpanded : parcelsExpanded;
        boolean[] hidden = targetSection == SECTION_IDENTITY ? identityHidden : parcelsHidden;
        for (int i = 0; i < 4; i++) expanded[i] = false;
        int[] order = targetSection == SECTION_IDENTITY ? identityOrder : parcelsOrder;
        for (int platform : order) {
            if (!hidden[platform]) {
                expanded[platform] = true;
                break;
            }
        }
    }

    private View buildAccountRow(int platform) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon = new ImageView(this);
        icon.setImageResource(PLATFORM_ICONS[platform]);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        top.addView(icon, size(28, 28));

        TextView name = label(PLATFORM_NAMES[platform], 15, palette.ink, true);
        LinearLayout.LayoutParams nameLayout = new LinearLayout.LayoutParams(0, -2, 1f);
        nameLayout.setMarginStart(dp(10));
        top.addView(name, nameLayout);

        boolean cainiao = platform == PLATFORM_CAINIAO;
        boolean pddExternal = platform == PLATFORM_PDD && pddAppMode;
        String status = cainiao ? "官方 App"
            : pddExternal ? "App 模式"
            : signedIn[platform] ? "已登录" : "未登录";
        TextView statusView = label(status, 12,
            cainiao || pddExternal ? palette.muted
                : signedIn[platform] ? palette.accent : palette.muted, false);
        top.addView(statusView);
        row.addView(top);

        if (cainiao) {
            LinearLayout cainiaoActions = new LinearLayout(this);
            cainiaoActions.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams cainiaoLayout = new LinearLayout.LayoutParams(-2, -2);
            cainiaoLayout.topMargin = dp(10);
            cainiaoLayout.setMarginStart(dp(38));
            TextView openApp = pill("打开菜鸟", true);
            openApp.setOnClickListener(v -> openCainiaoIdentity());
            cainiaoActions.addView(openApp);
            row.addView(cainiaoActions, cainiaoLayout);
            return row;
        }

        if (pddExternal) {
            LinearLayout externalActions = new LinearLayout(this);
            externalActions.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams externalLayout = new LinearLayout.LayoutParams(-2, -2);
            externalLayout.topMargin = dp(10);
            externalLayout.setMarginStart(dp(38));
            TextView openApp = pill("打开拼多多", true);
            openApp.setOnClickListener(v -> openPddApp(SECTION_IDENTITY));
            externalActions.addView(openApp);
            row.addView(externalActions, externalLayout);
            return row;
        }

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams actionsLayout = new LinearLayout.LayoutParams(-2, -2);
        actionsLayout.topMargin = dp(10);
        actionsLayout.setMarginStart(dp(38));

        boolean authenticated = signedIn[platform];
        TextView primary = pill(authenticated ? "管理登录" : "去登录", true);
        primary.setOnClickListener(v -> openPlatformLogin(platform));
        actions.addView(primary);

        if (authenticated) {
            TextView logout = pill("退出登录", false);
            LinearLayout.LayoutParams logoutLayout = new LinearLayout.LayoutParams(-2, -2);
            logoutLayout.setMarginStart(dp(8));
            logout.setOnClickListener(v -> requestLogout(platform));
            actions.addView(logout, logoutLayout);
        }
        row.addView(actions, actionsLayout);
        return row;
    }

    private TextView pill(String text, boolean primary) {
        TextView view = label(text, 12,
            primary ? onAccent() : palette.accent, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(14), dp(7), dp(14), dp(7));
        view.setBackground(background(primary ? palette.accent : palette.accentSoft, 16));
        view.setClickable(true);
        view.setFocusable(true);
        return view;
    }

    private void openPlatformLogin(int platform) {
        if (platform == PLATFORM_CAINIAO) {
            openCainiaoIdentity();
            return;
        }
        if (platform == PLATFORM_PDD && pddAppMode) {
            openPddApp(SECTION_IDENTITY);
            return;
        }
        boolean hidden = isPlatformHidden(platform);
        if (hidden) {
            showHiddenPlatformDialog(platform, () -> continuePlatformLogin(platform));
            return;
        }
        continuePlatformLogin(platform);
    }

    private boolean isPlatformHidden(int platform) {
        return platform == PLATFORM_JD
            ? parcelsHidden[platform] : identityHidden[platform];
    }

    private void showHiddenPlatformDialog(int platform, Runnable continueAction) {
        new AlertDialog.Builder(this, dialogTheme())
            .setTitle("该平台已被隐藏")
            .setMessage(PLATFORM_NAMES[platform]
                + "平台已被隐藏，管理账号状态需先取消隐藏，是否恢复显示该平台？")
            .setNegativeButton("保持隐藏", null)
            .setPositiveButton("恢复显示", (dialog, which) -> {
                restorePlatformVisibility(platform);
                continueAction.run();
            })
            .show();
    }

    private void restorePlatformVisibility(int platform) {
        if (platform == PLATFORM_JD) {
            parcelsHidden[platform] = false;
            persistHidden(SECTION_PARCELS);
            parcelsScroll = null;
        } else {
            identityHidden[platform] = false;
            persistHidden(SECTION_IDENTITY);
            identityScroll = null;
        }
    }

    private void requestLogout(int platform) {
        if (isPlatformHidden(platform)) {
            showHiddenPlatformDialog(platform, () -> confirmLogout(platform));
            return;
        }
        confirmLogout(platform);
    }

    private void continuePlatformLogin(int platform) {
        if (platform == PLATFORM_JD) {
            section = SECTION_PARCELS;
            parcelsExpanded[platform] = true;
            accountOpen = false;
            rememberState();
            renderContent();
            PlatformCard card = parcelsCards[platform];
            if (card != null) scrollToCard(parcelsScroll, card.card);
            return;
        }
        section = SECTION_IDENTITY;
        identityExpanded[platform] = true;
        accountOpen = false;
        rememberState();
        renderContent();
        PlatformCard card = identityCards[platform];
        if (card != null) scrollToCard(identityScroll, card.card);
        if (platform == PLATFORM_PDD && !pddAppMode && !signedIn[PLATFORM_PDD]) {
            showPddModeDialog(true);
        }
    }

    private void confirmLogout(int platform) {
        if (!SiteDataClearer.isSupported()) {
            toast("请先更新 Android System WebView / Chrome，再退出登录");
            return;
        }
        new AlertDialog.Builder(this, dialogTheme())
            .setTitle("退出" + PLATFORM_NAMES[platform] + "登录？")
            .setMessage("将清除该平台在本应用中的登录状态与网页数据。")
            .setNegativeButton("取消", null)
            .setPositiveButton("退出", (dialog, which) -> SiteDataClearer.start(platform))
            .show();
    }

    private String versionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException unavailable) {
            return "";
        }
    }

    private void toast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
    }

    private void setSignedIn(int platform, boolean value) {
        if (platform < 0 || platform >= 4) return;
        if (value && SiteDataClearer.isBusy()) return;
        boolean changed = signedIn[platform] != value;
        signedIn[platform] = value;
        String key = platform == PLATFORM_TAOBAO ? KEY_LOGIN_TAOBAO
            : platform == PLATFORM_PDD ? KEY_LOGIN_PDD : KEY_LOGIN_JD;
        getSharedPreferences(PREF_UI, MODE_PRIVATE).edit()
            .putBoolean(key, value)
            .apply();
        if (!changed) return;
        updateAllCardStates();
        if (section == SECTION_MORE && accountOpen) renderContent();
    }

    private String webMode() {
        return section == SECTION_PARCELS ? "parcels" : "identity";
    }

    private void applyTaobaoMode() {
        if (browser == null) return;
        String mode = webMode();
        browser.evaluateJavascript("(function(){return !!(window.HandyCodeMode&&window.HandyCodeMode('"
            + mode + "'));})()", null);
    }

    private void applyPddMode() {
        pddPage.setMode(webMode());
    }

    private String surfaceColor() {
        return String.format(java.util.Locale.ROOT, "#%06X", palette.surface & 0xFFFFFF);
    }

    private void applyWebBackground() {
        String color = surfaceColor();
        if (browser != null) {
            browser.evaluateJavascript("(function(){return !!(window.HandyCodeBackground"
                + "&&window.HandyCodeBackground('" + color + "'));})()", null);
        }
        pddPage.setBackgroundColor(color);
    }

    private void applyKeyboardPadding(int keyboardBottom, int systemBottom) {
        if (contentHost == null) return;
        int footer = bottomBar == null || bottomBar.getHeight() == 0
            ? dp(60) : bottomBar.getHeight();
        int padding = Math.max(0, keyboardBottom - systemBottom - footer - dp(1));
        if (contentHost.getPaddingBottom() == padding) return;
        contentHost.setPadding(0, 0, 0, padding);
        if (padding > 0 && isExpanded(PLATFORM_PDD)) {
            pddPage.post(pddPage::scrollFocusedInput);
        }
        if (padding > 0 && section == SECTION_PARCELS && isExpanded(PLATFORM_JD)) {
            jdPage.post(jdPage::scrollFocusedInput);
        }
        if (padding > 0 && isExpanded(PLATFORM_TAOBAO)
                && browser != null && !combinedDisplayed) {
            final WebView web = browser;
            web.post(() -> {
                if (isFinishing() || isDestroyed() || browser != web) return;
                web.evaluateJavascript("(function(){var e=document.activeElement;if(e&&e.matches('input,textarea,[contenteditable=true]'))e.scrollIntoView({block:'center',inline:'nearest'});})()", null);
            });
        }
    }

    private void openPinduoduoIdentity() {
        openPddApp(SECTION_IDENTITY);
    }

    private void buildTransitionPanel() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(18), dp(18), dp(18));
        content.setBackgroundColor(palette.surface);
        content.setClickable(true);
        content.setContentDescription("正在准备取件信息");
        addShimmer(content, 120, 14, 0);
        addShimmer(content, -1, 96, 14);
        addShimmer(content, 200, 12, 14);
        addShimmer(content, 150, 12, 10);
        addShimmer(content, -1, 64, 16);
        TextView note = label("正在准备取件信息…", 12, palette.muted, false);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams noteLayout = new LinearLayout.LayoutParams(-1, -2);
        noteLayout.topMargin = dp(14);
        content.addView(note, noteLayout);
        content.setVisibility(View.GONE);
        transitionPanel = content;
        browserFrame.addView(content, new FrameLayout.LayoutParams(-1, -1));
    }

    private void addShimmer(LinearLayout parent, int widthDp, int heightDp, int topMarginDp) {
        ShimmerBlock block = new ShimmerBlock(this, dp(heightDp > 20 ? 10 : 6));
        shimmerBlocks.add(block);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
            widthDp < 0 ? -1 : dp(widthDp), dp(heightDp));
        layout.topMargin = dp(topMarginDp);
        parent.addView(block, layout);
    }

    private void startShimmer() {
        if (!activityResumed || section == SECTION_MORE || !isExpanded(PLATFORM_TAOBAO)) return;
        if (!android.animation.ValueAnimator.areAnimatorsEnabled()) return;
        if (shimmerAnimation == null) {
            shimmerAnimation = android.animation.ValueAnimator.ofFloat(0f, 1f);
            shimmerAnimation.setDuration(1400);
            shimmerAnimation.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            shimmerAnimation.setInterpolator(new android.view.animation.LinearInterpolator());
            shimmerAnimation.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                for (ShimmerBlock block : shimmerBlocks) block.setPhase(value);
            });
        }
        if (!shimmerAnimation.isStarted()) shimmerAnimation.start();
    }

    private void stopShimmer() {
        if (shimmerAnimation != null) shimmerAnimation.cancel();
    }

    private void showTransition() {
        if (transitionPanel == null || combinedMarkup == null) return;
        transitionGeneration++;
        transitionPanel.animate().cancel();
        if (transitionPanel.getVisibility() != View.VISIBLE) {
            transitionStarted = android.os.SystemClock.elapsedRealtime();
        }
        transitionPanel.setAlpha(1f);
        transitionPanel.setVisibility(View.VISIBLE);
        transitionPanel.bringToFront();
        startShimmer();
    }

    private void hideTransition(boolean fade) {
        if (transitionPanel == null) return;
        final int token = ++transitionGeneration;
        transitionPanel.animate().cancel();
        if (!fade) {
            stopShimmer();
            transitionPanel.setVisibility(View.GONE);
            transitionPanel.setAlpha(1f);
            return;
        }
        transitionPanel.animate().alpha(0f).setDuration(160).withEndAction(() -> {
            if (token == transitionGeneration) {
                stopShimmer();
                transitionPanel.setVisibility(View.GONE);
                transitionPanel.setAlpha(1f);
            }
        }).start();
    }

    private void waitForMergedPresentation(WebView view, final int generation) {
        if (isFinishing() || isDestroyed() || generation != combineGeneration || browser != view
                || combinedMarkup == null) return;
        String ready = "(function(){var a=document.getElementById('identity-content'),b=document.getElementById('parcels-content');return !!(a&&b&&a.dataset.ready==='true'&&b.dataset.ready==='true');})()";
        view.evaluateJavascript(ready, complete -> {
            if (generation != combineGeneration || browser != view || isFinishing() || isDestroyed()) return;
            if ("true".equals(complete)
                    || android.os.SystemClock.elapsedRealtime() - transitionStarted >= 12000) {
                hideTransition(true);
            } else {
                view.postDelayed(() -> waitForMergedPresentation(view, generation), 200);
            }
        });
    }

    private void buildErrorPanel() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout errorPanel = new LinearLayout(this);
        errorPanel.setGravity(Gravity.CENTER);
        errorPanel.setOrientation(LinearLayout.VERTICAL);
        errorPanel.setPadding(dp(28), dp(24), dp(28), dp(24));
        errorPanel.setBackgroundColor(palette.surface);
        TextView errorTitle = label("", 16, palette.ink, true);
        errorTitle.setTag("error_title");
        errorTitle.setGravity(Gravity.CENTER);
        errorPanel.addView(errorTitle, size(-1, -2));
        TextView errorDetail = label("", 12, palette.muted, false);
        errorDetail.setTag("error_detail");
        errorDetail.setPadding(0, dp(16), 0, dp(24));
        errorDetail.setGravity(Gravity.CENTER);
        errorDetail.setLineSpacing(dp(5), 1f);
        errorPanel.addView(errorDetail, size(-1, -2));
        Button retry = button("重新打开网页", true);
        retry.setOnClickListener(v -> reloadTaobao());
        errorPanel.addView(retry, size(-1, 48));
        scroll.addView(errorPanel);
        scroll.setTag("error_overlay");
        scroll.setVisibility(View.GONE);
        browserFrame.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
    }

    private void ensureBrowser() {
        if (browser != null) return;
        browser = createBrowser(false);
        browserFrame.addView(browser, 0, new FrameLayout.LayoutParams(-1, -1));
        loadCombined();
    }

    private String readAssetText(String name) throws java.io.IOException {
        try (java.io.InputStream stream = getAssets().open(name)) {
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = stream.read(buffer)) != -1) output.write(buffer, 0, count);
            return new String(output.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private void loadCombined() {
        combineGeneration++;
        combinedMarkup = null;
        try {
            officialIdentityScript = readAssetText("official-identity.js");
            taobaoLoginChromeScript = readAssetText("taobao-login-chrome.js");
            combinedGateScript = readAssetText("login-ready.js").trim();
            combinedMarkup = readAssetText("taobao.html")
                .replace("@@REFRESH_CONTROLLER@@", readAssetText("refresh-controller.js"))
                .replace("@@OFFICIAL_IDENTITY@@", officialIdentityScript)
                .replace("@@IDENTITY_URL@@", android.text.TextUtils.htmlEncode(UrlPolicy.TAOBAO_ENTRY))
                .replace("@@PARCELS_URL@@", android.text.TextUtils.htmlEncode(UrlPolicy.TAOBAO_PARCELS_ENTRY));
            combinedDisplayed = false;
            showTransition();
            browser.loadUrl(UrlPolicy.TAOBAO_ENTRY);
        } catch (java.io.IOException error) {
            loadFailure(false, "无法读取本地取件页面，请重新安装应用后重试。");
        }
    }

    private boolean sameIdentityPage(String url) {
        if (!UrlPolicy.isAllowed(url)) return false;
        android.net.Uri actual = android.net.Uri.parse(url);
        android.net.Uri expected = android.net.Uri.parse(UrlPolicy.TAOBAO_ENTRY);
        return java.util.Objects.equals(actual.getHost(), expected.getHost())
            && java.util.Objects.equals(actual.getPath(), expected.getPath());
    }

    private void renderCombined(WebView view, String url) {
        if (combinedMarkup == null || combinedGateScript == null || !UrlPolicy.isAllowed(url)) return;
        waitForOfficialIdentity(view, ++combineGeneration);
    }

    private void waitForOfficialIdentity(WebView view, final int generation) {
        if (isFinishing() || isDestroyed() || generation != combineGeneration
                || browser != view || combinedMarkup == null || !UrlPolicy.isAllowed(view.getUrl())) return;
        view.evaluateJavascript(officialIdentityScript + "\n" + combinedGateScript, ready -> {
            if (generation != combineGeneration || browser != view || isFinishing() || isDestroyed()) return;
            if ("true".equals(ready) && sameIdentityPage(view.getUrl())) applyCombined(view, generation);
            else retryOfficialIdentity(view, generation, false);
        });
    }

    private void applyCombined(WebView view, final int generation) {
        if (combinedMarkup == null || !sameIdentityPage(view.getUrl())) return;
        // Keep the real official login/verification page visible until it has
        // produced a usable identity code; recheck inside the replacement too.
        String script = officialIdentityScript + "\n(function(){if(document.getElementById('handycode-merged'))return true;"
            + "if(!(" + combinedGateScript + "))return false;"
            + "document.open();document.write(" + org.json.JSONObject.quote(combinedMarkup)
            + ");document.close();return true;})()";
        view.evaluateJavascript(script, result -> {
            if (generation != combineGeneration || browser != view || isFinishing() || isDestroyed()) return;
            combinedDisplayed = "true".equals(result);
            if (combinedDisplayed) {
                setSignedIn(PLATFORM_TAOBAO, true);
                applyTaobaoMode();
                applyWebBackground();
            }
            if (combinedDisplayed) waitForMergedPresentation(view, generation);
            if (!combinedDisplayed) retryOfficialIdentity(view, generation, false);
        });
    }

    private void refreshAfterLogin() {
        final WebView view = browser;
        final int generation = combineGeneration;
        view.evaluateJavascript("(function(){if(typeof window.HandyCodeRefresh==='function'){window.HandyCodeRefresh();return true;}return false;})()", handled -> {
            if (view != browser || generation != combineGeneration || isFinishing() || isDestroyed()) return;
            if (!"true".equals(handled)) loadCombined();
        });
    }

    private WebView createBrowser(boolean popup) {
        WebView web = new WebView(this);
        web.setBackgroundColor(palette.surface);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSafeBrowsingEnabled(true);
        applyWebDark(settings);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (UrlPolicy.isAllowed(request.getUrl().toString())) return false;
                if (request.isForMainFrame()) blockedNavigation();
                return true;
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (UrlPolicy.isAllowed(url)) return false;
                blockedNavigation();
                return true;
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                if ("about:blank".equals(url) && popup) return;
                if (!UrlPolicy.isAllowed(url)) {
                    view.stopLoading();
                    blockedNavigation();
                    return;
                }
                if (!popup) {
                    combinedDisplayed = false;
                    combineGeneration++;
                    if (combinedMarkup != null) showTransition();
                    hideError();
                } else popupGeneration++;
            }
            @Override public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().flush();
                if (popup) installPresentationObserver(view);
                if (!popup) {
                    installPresentationObserver(view);
                    renderCombined(view, url);
                } else waitForPopupIdentity(view, ++popupGeneration);
            }
            @Override public void onPageCommitVisible(WebView view, String url) {
                installPresentationObserver(view);
            }
            @Override public void doUpdateVisitedHistory(WebView view, String url, boolean reload) {
                if (combinedMarkup != null) {
                    if (popup) waitForPopupIdentity(view, ++popupGeneration);
                    else if (!combinedDisplayed) renderCombined(view, url);
                }
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) loadFailure(popup, "请检查网络后重试，或稍后重新打开网页。");
            }
            @Override public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse response) {
                if (request.isForMainFrame()) loadFailure(popup, "官方页面返回 HTTP " + response.getStatusCode() + "，请稍后重试。");
            }
            @Override public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
                loadFailure(popup, "页面的安全连接验证失败。请检查手机日期、网络或稍后重试。");
            }
            @Override public boolean onRenderProcessGone(WebView view, android.webkit.RenderProcessGoneDetail detail) {
                view.post(() -> recoverBrowser(view, popup));
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onConsoleMessage(android.webkit.ConsoleMessage message) {
                String value = message.message();
                if ((popup ? popupView == web : browser == web) && combinedMarkup != null
                        && value != null && value.startsWith("__HANDYCODE_PHASE__:")) {
                    String phase = value.substring("__HANDYCODE_PHASE__:".length());
                    if ("login".equals(phase)) setSignedIn(PLATFORM_TAOBAO, false);
                    else if ("ready".equals(phase) || "merged".equals(phase)) setSignedIn(PLATFORM_TAOBAO, true);
                    if (popup) return true;
                }
                if (!popup && browser == web && combinedMarkup != null
                        && value != null && value.startsWith("__HANDYCODE_PHASE__:")) {
                    String phase = value.substring("__HANDYCODE_PHASE__:".length());
                    if ("login".equals(phase)) revealOfficialLogin(web);
                    else if ("loading".equals(phase) && !combinedDisplayed) showTransition();
                    else if ("ready".equals(phase)) { showTransition(); renderCombined(web, web.getUrl()); }
                    else if ("merged".equals(phase)) {
                        combinedDisplayed = true;
                        applyTaobaoMode();
                        applyWebBackground();
                        waitForMergedPresentation(web, combineGeneration);
                    }
                    return true;
                }
                return false;
            }
            @Override public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message message) {
                if (!userGesture || popup || popupDialog != null) return false;
                openPopup(message);
                return true;
            }
            @Override public void onCloseWindow(WebView window) {
                if (popupDialog != null && window == popupView) popupDialog.dismiss();
            }
            @Override public void onPermissionRequest(android.webkit.PermissionRequest request) {
                request.deny();
            }
        });
        return web;
    }

    private void applyWebDark(WebSettings settings) {
        if (Build.VERSION.SDK_INT >= 33) {
            settings.setAlgorithmicDarkeningAllowed(palette.dark);
        } else if (Build.VERSION.SDK_INT >= 29) {
            //noinspection deprecation
            settings.setForceDark(palette.dark ? WebSettings.FORCE_DARK_ON : WebSettings.FORCE_DARK_OFF);
        }
    }

    private void openPopup(Message message) {
        popupDialog = new Dialog(this);
        popupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setBackgroundColor(palette.surface);
        TextView title = label("官方网页登录", 18, palette.ink, true);
        title.setPadding(dp(16), dp(16), dp(16), dp(16));
        body.addView(title);
        TextView trustedDomains = label("淘宝官方网页登录", 12, palette.muted, false);
        trustedDomains.setPadding(dp(16), 0, dp(16), dp(10));
        body.addView(trustedDomains);
        popupView = createBrowser(true);
        body.addView(popupView, new LinearLayout.LayoutParams(-1, 0, 1f));
        Button close = button("关闭登录窗口", false);
        close.setOnClickListener(v -> popupDialog.dismiss());
        body.addView(close, size(-1, 48));
        popupDialog.setContentView(body);
        popupDialog.setOnDismissListener(d -> {
            popupGeneration++;
            CookieManager.getInstance().flush();
            if (popupView != null) {
                popupView.stopLoading();
                ((ViewGroup) popupView.getParent()).removeView(popupView);
                popupView.destroy();
            }
            popupView = null;
            popupDialog = null;
            if (browser != null && combinedMarkup != null) {
                final WebView parent = browser;
                final int generation = combineGeneration;
                parent.postDelayed(() -> {
                    if (isFinishing() || isDestroyed() || browser != parent || generation != combineGeneration) return;
                    if (combinedDisplayed) refreshAfterLogin();
                    else parent.loadUrl(UrlPolicy.TAOBAO_ENTRY);
                }, 300);
            }
        });
        popupDialog.show();
        Window window = popupDialog.getWindow();
        if (window != null) window.setLayout(-1, -1);
        WebView.WebViewTransport transport = (WebView.WebViewTransport) message.obj;
        transport.setWebView(popupView);
        message.sendToTarget();
    }

    private void waitForPopupIdentity(WebView view, final int generation) {
        if (isFinishing() || isDestroyed() || generation != popupGeneration || popupView != view
                || combinedMarkup == null || combinedGateScript == null || !UrlPolicy.isAllowed(view.getUrl())) return;
        view.evaluateJavascript(officialIdentityScript + "\n" + combinedGateScript, ready -> {
            if (generation != popupGeneration || popupView != view || isFinishing() || isDestroyed()) return;
            if ("true".equals(ready) && sameIdentityPage(view.getUrl())) {
                CookieManager.getInstance().flush();
                if (popupDialog != null) popupDialog.dismiss();
            } else retryOfficialIdentity(view, generation, true);
        });
    }

    private void retryOfficialIdentity(WebView view, final int generation, boolean popup) {
        Runnable next = () -> {
            if (popup) waitForPopupIdentity(view, generation);
            else waitForOfficialIdentity(view, generation);
        };
        if (UrlPolicy.isAllowed(view.getUrl()) && officialIdentityScript != null) {
            view.evaluateJavascript(officialIdentityScript + "\nHandyCodeOfficialIdentity.recover(document,Date.now())",
                status -> {
                    if (!popup && generation == combineGeneration && "\"login\"".equals(status)) revealOfficialLogin(view);
                    view.postDelayed(next, 650);
                });
        } else view.postDelayed(next, 650);
    }

    private void installPresentationObserver(WebView view) {
        if (combinedMarkup == null || officialIdentityScript == null || !UrlPolicy.isAllowed(view.getUrl())) return;
        view.evaluateJavascript(taobaoLoginChromeScript + "\n" + officialIdentityScript
            + "\nHandyCodeOfficialIdentity.watch(document)", null);
    }

    private void revealOfficialLogin(WebView view) {
        if (browser != view || taobaoLoginChromeScript == null
                || transitionPanel.getVisibility() != View.VISIBLE) return;
        final int expected = combineGeneration;
        view.evaluateJavascript(taobaoLoginChromeScript, ignored -> {
            if (browser != view || expected != combineGeneration || isFinishing() || isDestroyed()) return;
            view.postVisualStateCallback(expected, new WebView.VisualStateCallback() {
                @Override public void onComplete(long requestId) {
                    if (browser == view && expected == combineGeneration
                            && !isFinishing() && !isDestroyed()) hideTransition(false);
                }
            });
        });
    }

    private void loadFailure(boolean popup, String description) {
        if (popup) {
            new AlertDialog.Builder(this, dialogTheme()).setTitle("登录网页暂时无法打开")
                .setMessage(description).setPositiveButton("知道了", null).show();
        } else {
            hideTransition(false);
            showError("网页暂时无法打开", description);
        }
    }

    private void recoverBrowser(WebView view, boolean popup) {
        if (popup) {
            if (popupDialog != null) popupDialog.dismiss();
            return;
        }
        if (browser != view) return;
        combineGeneration++;
        hideTransition(false);
        browserFrame.removeView(view);
        view.destroy();
        browser = null;
        showError("网页组件已停止", "点击“重新打开网页”即可恢复。");
    }

    private void blockedNavigation() {
        long now = android.os.SystemClock.elapsedRealtime();
        if (now - lastBlockedNotice < 2500) return;
        lastBlockedNotice = now;
        new AlertDialog.Builder(this, dialogTheme()).setTitle("无法打开此链接")
            .setMessage("此网页链接需要其他应用，或不在淘宝官方网页范围内。")
            .setPositiveButton("留在当前页面", null).show();
    }

    private void hideError() {
        View overlay = browserFrame.findViewWithTag("error_overlay");
        if (overlay != null) overlay.setVisibility(View.GONE);
    }

    private void showError(String title, String detail) {
        ((TextView) browserFrame.findViewWithTag("error_title")).setText(title);
        ((TextView) browserFrame.findViewWithTag("error_detail")).setText(detail);
        View overlay = browserFrame.findViewWithTag("error_overlay");
        overlay.setVisibility(View.VISIBLE);
        overlay.bringToFront();
    }

    private void reloadTaobao() {
        hideError();
        if (browser == null) ensureBrowser();
        else loadCombined();
    }

    private void goBack() {
        if (SiteDataClearer.isBusy()) return;
        if (popupView != null) {
            if (popupView.canGoBack()) popupView.goBack();
            else if (popupDialog != null) popupDialog.dismiss();
            return;
        }
        if (section == SECTION_MORE && orderOpen) {
            closeMoreSubpage();
            return;
        }
        if (section == SECTION_MORE && accountOpen) {
            closeMoreSubpage();
            return;
        }
        if (section != SECTION_IDENTITY) {
            runPageTransition(-1, () -> {
                section = SECTION_IDENTITY;
                rememberState();
                renderContent();
            });
            return;
        }
        if (isExpanded(PLATFORM_PDD) && pddPage.goBack()) return;
        if (section == SECTION_PARCELS && isExpanded(PLATFORM_JD) && jdPage.goBack()) return;
        if (isExpanded(PLATFORM_TAOBAO) && browser != null && browser.canGoBack()) {
            hideError();
            browser.goBack();
            return;
        }
        finish();
    }

    @Override public void onBackPressed() { goBack(); }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("section", section);
        out.putBoolean("accountOpen", accountOpen);
        out.putBoolean("orderOpen", orderOpen);
        for (int i = 0; i < 4; i++) {
            out.putBoolean("identityExpanded" + i, identityExpanded[i]);
            out.putBoolean("parcelsExpanded" + i, parcelsExpanded[i]);
        }
        super.onSaveInstanceState(out);
    }

    @Override protected void onPause() {
        activityResumed = false;
        stopShimmer();
        CookieManager.getInstance().flush();
        if (browser != null) browser.onPause();
        pddPage.pausePage();
        jdPage.pausePage();
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        activityResumed = true;
        if (SiteDataClearer.isBusy()) {
            updateChrome();
            return;
        }
        if (browser != null) browser.onResume();
        boolean pddActive = isExpanded(PLATFORM_PDD) && !pddAppMode;
        pddPage.resumePage(pddActive);
        boolean jdActive = section == SECTION_PARCELS && isExpanded(PLATFORM_JD);
        jdPage.resumePage(jdActive);
        if (section != SECTION_MORE) {
            if (isExpanded(PLATFORM_TAOBAO) && browser == null) ensureBrowser();
            if (pddActive) pddPage.showPage();
            if (jdActive) jdPage.showPage(!signedIn[PLATFORM_JD]);
        }
        if (transitionPanel != null && transitionPanel.getVisibility() == View.VISIBLE) startShimmer();
    }

    @Override protected void onDestroy() {
        SiteDataClearer.removeObserver(logoutListener);
        stopShimmer();
        combineGeneration++;
        popupGeneration++;
        pddPage.destroyPage();
        jdPage.destroyPage();
        if (pddModeDialog != null) pddModeDialog.dismiss();
        if (popupDialog != null) popupDialog.dismiss();
        if (browser != null) {
            browserFrame.removeView(browser);
            browser.destroy();
            browser = null;
        }
        super.onDestroy();
    }
}
