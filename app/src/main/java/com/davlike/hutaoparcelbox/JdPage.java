package com.davlike.hutaoparcelbox;

import android.content.Context;
import android.graphics.Color;
import android.net.http.SslError;
import android.view.View;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Official JD logistics H5 page, projecting the received-parcel list into the card. */
final class JdPage extends LinearLayout {
    private WebView web;
    private final TextView status;
    private final LinearLayout actions;
    private final FrameLayout content;
    private final Palette palette;
    private final java.util.function.Consumer<Boolean> sessionChanged;
    private String layoutScript;
    private boolean destroyed;
    private boolean needLogin;

    JdPage(Context context, java.util.function.Consumer<Boolean> sessionChanged) {
        super(context);
        this.sessionChanged = sessionChanged;
        palette = Palette.of(context);
        setOrientation(VERTICAL);
        setBackgroundColor(palette.bg);

        actions = new LinearLayout(context);
        actions.setPadding(dp(12), dp(6), dp(12), dp(6));
        actions.setGravity(android.view.Gravity.CENTER_VERTICAL);
        status = new TextView(context);
        status.setTextSize(12);
        status.setTextColor(palette.muted);
        status.setText("正在打开京东待取包裹…");
        actions.addView(status, new LinearLayout.LayoutParams(0, -2, 1f));
        Button retry = new Button(context);
        retry.setText("重试");
        retry.setTextSize(12);
        retry.setTextColor(palette.accent);
        retry.setBackgroundColor(Color.TRANSPARENT);
        retry.setAllCaps(false);
        retry.setMinWidth(0);
        retry.setMinimumWidth(0);
        retry.setOnClickListener(v -> reload());
        actions.addView(retry);
        actions.setVisibility(View.GONE);
        addView(actions);

        content = new FrameLayout(context);
        addView(content, new LinearLayout.LayoutParams(-1, 0, 1f));
        try {
            layoutScript = readAsset("jd-layout.js");
        } catch (IOException unavailable) {
            actions.setVisibility(View.VISIBLE);
            status.setText("无法读取京东兼容资源，请重新安装");
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String readAsset(String name) throws IOException {
        try (InputStream input = getContext().getAssets().open(name)) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) bytes.write(buffer, 0, count);
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    void showPage(boolean needLogin) {
        this.needLogin = needLogin;
        if (destroyed || layoutScript == null) return;
        if (web != null) {
            web.onResume();
            return;
        }
        web = new WebView(getContext());
        web.setBackgroundColor(palette.surface);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSafeBrowsingEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        applyWebDark(settings);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (UrlPolicy.isJdAllowed(request.getUrl().toString())) return false;
                if (request.isForMainFrame()) blocked();
                return true;
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (UrlPolicy.isJdAllowed(url)) return false;
                blocked();
                return true;
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                if (!UrlPolicy.isJdAllowed(url)) {
                    view.stopLoading();
                    blocked();
                    return;
                }
                actions.setVisibility(View.GONE);
                if (UrlPolicy.isJdLoginPage(url) || UrlPolicy.isJdMyPage(url)) {
                    sessionChanged.accept(false);
                    status.setText("请在京东官方网页完成登录");
                } else {
                    status.setText("正在加载京东待取包裹…");
                }
            }
            @Override public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().flush();
                if (!UrlPolicy.isJdAllowed(url)) return;
                if (UrlPolicy.isJdSearch(url) || UrlPolicy.isJdLoginPage(url)
                        || UrlPolicy.isJdMyPage(url)) injectLayout(view);
                if (UrlPolicy.isJdLoginPage(url) || UrlPolicy.isJdMyPage(url)) {
                    sessionChanged.accept(false);
                    status.setText("请在京东官方网页完成登录");
                }
            }
            @Override public void onPageCommitVisible(WebView view, String url) {
                if (UrlPolicy.isJdSearch(url) || UrlPolicy.isJdMyPage(url)) injectLayout(view);
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) fail("京东网页未打开，请检查网络后重试");
            }
            @Override public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse response) {
                if (request.isForMainFrame()) fail("京东网页暂时不可用，请稍后重试");
            }
            @Override public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
                fail("安全连接验证失败，请检查网络或手机日期");
            }
            @Override public boolean onRenderProcessGone(WebView view, android.webkit.RenderProcessGoneDetail detail) {
                if (web == view) {
                    content.removeView(view);
                    view.destroy();
                    web = null;
                    fail("网页组件已停止，请点重试");
                }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onConsoleMessage(android.webkit.ConsoleMessage message) {
                String value = message.message();
                if (value == null || !value.startsWith("__HANDYCODE_JD__:")) return false;
                String phase = value.substring("__HANDYCODE_JD__:".length());
                if ("cards".equals(phase)) {
                    sessionChanged.accept(true);
                    status.setText("京东待取包裹");
                    actions.setVisibility(View.GONE);
                } else if ("login".equals(phase)) {
                    sessionChanged.accept(false);
                    status.setText("未登录，请在京东官方网页完成登录");
                    actions.setVisibility(View.GONE);
                    if (web != null) {
                        String current = web.getUrl();
                        if (current != null && !UrlPolicy.isJdMyPage(current)
                                && !UrlPolicy.isJdLoginPage(current)) {
                            web.loadUrl(UrlPolicy.JD_LOGIN_ENTRY);
                        }
                    }
                } else if ("loading".equals(phase)) {
                    status.setText("正在加载京东待取包裹…");
                } else if ("empty".equals(phase)) {
                    sessionChanged.accept(true);
                    status.setText("暂无京东待取包裹");
                    actions.setVisibility(View.GONE);
                } else if ("source".equals(phase)) {
                    status.setText("正在打开京东官方网页…");
                } else if ("unavailable".equals(phase)) {
                    fail("暂时无法整理京东内容，请点重试");
                }
                return true;
            }
            @Override public void onPermissionRequest(android.webkit.PermissionRequest request) {
                request.deny();
            }
        });
        content.addView(web, 0, new FrameLayout.LayoutParams(-1, -1));
        web.loadUrl(needLogin ? UrlPolicy.JD_LOGIN_ENTRY : UrlPolicy.JD_PARCELS_ENTRY);
    }

    private void injectLayout(WebView view) {
        if (layoutScript == null) return;
        view.evaluateJavascript(layoutScript, null);
        String color = String.format(java.util.Locale.ROOT, "#%06X", palette.surface & 0xFFFFFF);
        view.evaluateJavascript("(function(){if(window.HandyJdBackground)"
            + "window.HandyJdBackground('" + color + "');})()", null);
    }

    private void applyWebDark(WebSettings settings) {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            settings.setAlgorithmicDarkeningAllowed(palette.dark);
        } else if (android.os.Build.VERSION.SDK_INT >= 29) {
            //noinspection deprecation
            settings.setForceDark(palette.dark ? WebSettings.FORCE_DARK_ON : WebSettings.FORCE_DARK_OFF);
        }
    }

    private void fail(String message) {
        actions.setVisibility(View.VISIBLE);
        status.setText(message);
    }

    private void blocked() {
        fail("京东网页请求打开其他地址，请使用官方登录页");
    }

    private void reload() {
        actions.setVisibility(View.GONE);
        if (web == null) showPage(needLogin);
        else web.reload();
    }

    void refresh() {
        reload();
    }

    boolean goBack() {
        if (web == null || !web.canGoBack()) return false;
        web.goBack();
        return true;
    }

    void scrollFocusedInput() {
        if (web != null) {
            web.evaluateJavascript("(function(){var e=document.activeElement;if(e&&e.matches('input,textarea'))e.scrollIntoView({block:'center'});})()", null);
        }
    }

    void idlePage() { }

    void pausePage() {
        if (web != null) web.onPause();
    }

    void resumePage(boolean active) {
        if (web != null) web.onResume();
    }

    void resetSessionView() {
        sessionChanged.accept(false);
        if (web != null) {
            web.stopLoading();
            web.setWebChromeClient(null);
            web.setWebViewClient(new WebViewClient());
            content.removeView(web);
            web.destroy();
            web = null;
        }
        actions.setVisibility(View.GONE);
        status.setText("正在打开京东待取包裹…");
    }

    void destroyPage() {
        destroyed = true;
        if (web != null) {
            content.removeView(web);
            web.destroy();
            web = null;
        }
    }
}
