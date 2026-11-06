package app.verre.launcher;

import android.content.Context;
import android.content.SharedPreferences;

final class Prefs {
    private Prefs() {}

    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences("verre", Context.MODE_PRIVATE);
    }

    static int palette(Context c) {
        int v = p(c).getInt("palette", 0);
        return (v < 0 || v >= Style.PALETTES.length) ? 0 : v;
    }

    static void setPalette(Context c, int v) {
        p(c).edit().putInt("palette", v).apply();
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
