package app.verre.launcher;

import android.content.Context;
import android.graphics.Typeface;

/** Couleurs « système » façon Apple en mode sombre, mode nuit et polices (Inter). */
final class Style {
    static final int INK = 0xFFFFFFFF;
    static final int INK2 = 0xC7EBEBF5;      // texte secondaire
    static final int CARD = 0xFF1C1C1E;      // widget plein (sans fond coloré)
    static final int CARD_NIGHT = 0xFF141416;
    static final int FILL = 0xFF2C2C2E;      // bouton / piste sur widget plein
    static final int DARK = 0xFF1C1C1E;      // pictogramme sur fond blanc
    static final int GREEN = 0xFF30D158;
    static final int BLUE = 0xFF0A84FF;
    static final int RED = 0xFFFF453A;
    static final int RED_SOFT = 0xFFFF6B61;  // lisible sur le verre
    static final int ORANGE = 0xFFFF9F0A;
    static final int PINK = 0xFFFF375F;
    static final int GRAY = 0xFF636366;
    static final int GRAY2 = 0xFF8E8E93;

    static Typeface MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    static Typeface SEMIBOLD = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    static Typeface BOLD = Typeface.create("sans-serif", Typeface.BOLD);
    static Typeface REGULAR = MEDIUM;

    static boolean night;
    private static boolean fontsLoaded;

    private Style() {}

    static void load(Context c) {
        night = Prefs.night(c);
        if (!fontsLoaded) {
            fontsLoaded = true;
            MEDIUM = font(c, "Inter-Medium.ttf", MEDIUM);
            SEMIBOLD = font(c, "Inter-SemiBold.ttf", SEMIBOLD);
            BOLD = font(c, "Inter-Bold.ttf", BOLD);
            REGULAR = MEDIUM;
        }
    }

    private static Typeface font(Context c, String file, Typeface fallback) {
        try {
            return Typeface.createFromAsset(c.getAssets(), "fonts/" + file);
        } catch (Exception e) {
            return fallback;
        }
    }
}
