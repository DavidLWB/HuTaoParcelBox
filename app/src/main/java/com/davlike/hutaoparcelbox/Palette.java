package com.davlike.hutaoparcelbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

/** Hu Tao inspired palette: deep red, warm cream and gold, with a system-following dark mode. */
final class Palette {
    static final String PREF_UI = "ui_state";
    private static final String KEY_THEME = "theme_mode";

    final boolean dark;
    final int bg, surface, surfaceAlt, ink, muted, accent, accentSoft, gold, border, navBg;

    private Palette(boolean dark) {
        this.dark = dark;
        if (dark) {
            bg = 0xff000000;
            surface = 0xff221a16;
            surfaceAlt = 0xff2b211c;
            ink = 0xfff3e8da;
            muted = 0xffb5a294;
            accent = 0xffd97a6c;
            accentSoft = 0xff3a2723;
            gold = 0xffd6b274;
            border = 0xff3a2c25;
            navBg = 0xff1c1512;
        } else {
            bg = 0xffffffff;
            surface = 0xfffff8ef;
            surfaceAlt = 0xfff4ead9;
            ink = 0xff3b2a25;
            muted = 0xff8c7a6d;
            accent = 0xffa63b34;
            accentSoft = 0xfff2dcd3;
            gold = 0xffc39a52;
            border = 0xffe9dcc6;
            navBg = 0xfffffdf8;
        }
    }

    static Palette of(Context context) {
        return new Palette(resolveDark(context));
    }

    private static boolean resolveDark(Context context) {
        String mode = mode(context);
        if ("dark".equals(mode)) return true;
        if ("light".equals(mode)) return false;
        int ui = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return ui == Configuration.UI_MODE_NIGHT_YES;
    }

    static String mode(Context context) {
        return prefs(context).getString(KEY_THEME, "auto");
    }

    /**
     * Reserved for the future settings page. The manual switch is intentionally
     * not exposed yet; callers should recreate the Activity after changing it.
     */
    static void setMode(Context context, String mode) {
        String value = "dark".equals(mode) || "light".equals(mode) ? mode : "auto";
        prefs(context).edit().putString(KEY_THEME, value).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_UI, Context.MODE_PRIVATE);
    }
}
