package com.davlike.hutaoparcelbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

/** Checks the public GitHub release list; no account or device identifiers are sent. */
final class UpdateChecker {
    static final String KEY_AUTO_UPDATE = "auto_update";
    private static final String RELEASES_API =
        "https://api.github.com/repos/DavidLWB/HuTaoParcelBox/releases?per_page=10";
    private static final String RELEASES_URL =
        "https://github.com/DavidLWB/HuTaoParcelBox/releases";
    private static final Pattern VERSION = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static boolean checking;

    private UpdateChecker() {}

    private static final class Release {
        final String version;
        final String url;

        Release(String version, String url) {
            this.version = version;
            this.url = url;
        }
    }

    static void check(Activity activity, boolean manual) {
        if (checking) {
            if (manual) toast(activity, "正在检查更新…");
            return;
        }
        checking = true;
        final String current = versionName(activity);
        new Thread(() -> {
            Release release = null;
            String failure = null;
            try {
                release = fetchLatest(current);
            } catch (Exception unavailable) {
                failure = "network";
            }
            final Release found = release;
            final String error = failure;
            MAIN.post(() -> {
                checking = false;
                if (activity.isFinishing() || activity.isDestroyed()) return;
                if (error != null) {
                    if (manual) toast(activity, "检查更新失败，请检查网络后重试");
                    return;
                }
                if (found == null) {
                    if (manual) toast(activity, "当前已是最新版本");
                    return;
                }
                showUpdateDialog(activity, current, found);
            });
        }, "update-check").start();
    }

    private static Release fetchLatest(String current) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(RELEASES_API).openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("User-Agent", "DieYinSuQu/" + current);
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) throw new IOException("HTTP " + code);
            JSONArray releases = new JSONArray(read(connection.getInputStream()));
            Release best = null;
            for (int i = 0; i < releases.length(); i++) {
                JSONObject item = releases.optJSONObject(i);
                if (item == null || item.optBoolean("draft")) continue;
                String tag = item.optString("tag_name", "");
                if (!isNewer(tag, current)) continue;
                String version = tag.startsWith("v") || tag.startsWith("V") ? tag.substring(1) : tag;
                String url = item.optString("html_url", RELEASES_URL);
                if (best == null || compare(parse(version), parse(best.version)) > 0) {
                    best = new Release(version, url);
                }
            }
            return best;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static boolean isNewer(String remote, String current) {
        int[] left = parse(remote);
        int[] right = parse(current);
        return left != null && right != null && compare(left, right) > 0;
    }

    private static int compare(int[] left, int[] right) {
        if (left == null || right == null) return 0;
        for (int i = 0; i < 3; i++) {
            if (left[i] != right[i]) return left[i] > right[i] ? 1 : -1;
        }
        return 0;
    }

    private static int[] parse(String value) {
        Matcher matcher = VERSION.matcher(value == null ? "" : value);
        if (!matcher.find()) return null;
        return new int[]{
            Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2)),
            Integer.parseInt(matcher.group(3))
        };
    }

    private static String read(InputStream input) throws IOException {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int total = 0;
            int count;
            while ((count = stream.read(buffer)) != -1) {
                total += count;
                if (total > 512 * 1024) break;
                output.write(buffer, 0, count);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static void showUpdateDialog(Activity activity, String current, Release release) {
        Palette palette = Palette.of(activity);
        new AlertDialog.Builder(activity,
            palette.dark ? R.style.AppDialogThemeDark : R.style.AppDialogThemeLight)
            .setTitle("发现新版本 " + release.version)
            .setMessage("当前版本：" + current + "\n最新版本：" + release.version
                + "\n\n是否前往 GitHub 下载更新？")
            .setNegativeButton("稍后", null)
            .setPositiveButton("前往更新", (dialog, which) -> {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(release.url)));
                } catch (ActivityNotFoundException | SecurityException unavailable) {
                    toast(activity, "无法打开浏览器");
                }
            })
            .show();
    }

    private static String versionName(Activity activity) {
        try {
            return activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException unavailable) {
            return "";
        }
    }

    private static void toast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }
}
