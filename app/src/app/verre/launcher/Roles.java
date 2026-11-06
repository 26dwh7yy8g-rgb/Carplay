package app.verre.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import java.util.HashMap;

/**
 * Trouve l'appli de l'autoradio pour chaque rôle (CarPlay, Radio, Son…). Les noms changent d'un
 * modèle à l'autre : on devine par mots-clés, et l'utilisateur peut choisir (appui long).
 */
final class Roles {
    static final String CARPLAY = "carplay", RADIO = "radio", MUSIC = "music", PHONE = "phone",
            SOUND = "sound", CAMERA = "camera", SETTINGS = "settings";

    /** [0] mots du nom, [1] morceaux du nom de paquet, [2] paquets exacts. */
    private static final HashMap<String, String[][]> KW = new HashMap<>();

    static {
        KW.put(CARPLAY, new String[][]{
                {"carplay", "zlink", "autokit", "carlink", "tlink", "phonelink", "smartlink", "easyconnection", "easyconn"},
                {"zlink", "carplay", "autokit", "carlink", "tlink", "phonelink", "easyconn"},
                {"com.zjinnova.zlink"}});
        KW.put(RADIO, new String[][]{{"radio", "dab", "fm", "tuner"}, {"radio", "dab", "fm", "tuner"}, {}});
        KW.put(MUSIC, new String[][]{{"musique", "music", "lecteur", "player"}, {"music", "musicplayer", "player"}, {}});
        KW.put(PHONE, new String[][]{{"telephone", "phone", "bluetooth", "bt", "appel", "appels", "dialer"},
                {"dialer", "btphone", "bluetooth", "phone", "bt"}, {}});
        KW.put(SOUND, new String[][]{{"dsp", "egaliseur", "equaliseur", "equalizer", "eq", "son", "sound", "audio"},
                {"dsp", "eq", "equalizer", "sound", "audio", "audioeffect"}, {}});
        KW.put(CAMERA, new String[][]{{"camera", "cam", "avm", "recul", "dvr", "360", "retroviseur"},
                {"camera", "avm", "reverse", "dvr", "backcar", "rearview"}, {}});
    }

    private Roles() {}

    static String title(String role) {
        switch (role) {
            case CARPLAY: return "CarPlay";
            case RADIO: return "Radio";
            case MUSIC: return "Musique";
            case PHONE: return "Téléphone";
            case SOUND: return "Son";
            case CAMERA: return "Caméra";
            default: return "Réglages";
        }
    }

    static Intent launchIntent(ComponentName cn) {
        return new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(cn)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }

    static Intent intentFor(Context c, String role) {
        String saved = Prefs.role(c, role);
        if (saved != null) {
            ComponentName cn = ComponentName.unflattenFromString(saved);
            if (cn != null && AppRepo.find(c, cn) != null) return launchIntent(cn);
        }
        if (SETTINGS.equals(role)) {
            return new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        AppInfo a = guess(c, role);
        return a == null ? null : launchIntent(a.cn);
    }

    static AppInfo guess(Context c, String role) {
        String[][] kw = KW.get(role);
        if (kw == null) return null;
        AppInfo best = null;
        float bestScore = 0;
        for (AppInfo a : AppRepo.snapshot(c)) {
            if (a.kind != AppInfo.NORMAL) continue;
            float s = 0;
            for (String p : kw[2]) if (p.equals(a.pkg)) s = 10;
            String[] lt = Text.tokens(a.label);
            for (String k : kw[0]) {
                for (String t : lt) {
                    if (t.equals(k)) s = Math.max(s, 3);
                    else if (k.length() >= 4 && t.startsWith(k)) s = Math.max(s, 2);
                }
            }
            String[] pt = Text.tokens(a.pkg);
            for (String k : kw[1]) {
                for (String t : pt) {
                    if (t.equals(k) || (k.length() >= 4 && t.startsWith(k))) s = Math.max(s, 1);
                }
            }
            if (s > 0 && a.system) s += 0.5f;
            if (s > bestScore) {
                bestScore = s;
                best = a;
            }
        }
        return best;
    }
}
