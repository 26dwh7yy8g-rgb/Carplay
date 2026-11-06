package app.verre.launcher;

import android.content.Context;
import android.content.SharedPreferences;

final class Prefs {
    /** Fond : pochette en cours (sinon ma photo, sinon noir) / toujours ma photo / toujours noir. */
    static final int BG_AUTO = 0, BG_PHOTO = 1, BG_BLACK = 2;

    private Prefs() {}

    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences("verre", Context.MODE_PRIVATE);
    }

    static int bgMode(Context c) {
        int v = p(c).getInt("bg_mode", BG_AUTO);
        return (v < BG_AUTO || v > BG_BLACK) ? BG_AUTO : v;
    }

    static void setBgMode(Context c, int v) {
        p(c).edit().putInt("bg_mode", v).apply();
    }

    static boolean night(Context c) {
        return p(c).getBoolean("night", false);
    }

    static void setNight(Context c, boolean v) {
        p(c).edit().putBoolean("night", v).apply();
    }

    static boolean autoOpen(Context c) {
        return p(c).getBoolean("auto_open", true);
    }

    static void setAutoOpen(Context c, boolean v) {
        p(c).edit().putBoolean("auto_open", v).apply();
    }

    static boolean setupDone(Context c) {
        return p(c).getBoolean("setup_done", false);
    }

    static void setSetupDone(Context c, boolean v) {
        p(c).edit().putBoolean("setup_done", v).apply();
    }

    static String role(Context c, String role) {
        return p(c).getString("role_" + role, null);
    }

    static void setRole(Context c, String role, String value) {
        SharedPreferences.Editor e = p(c).edit();
        if (value == null) e.remove("role_" + role);
        else e.putString("role_" + role, value);
        e.apply();
    }
}
