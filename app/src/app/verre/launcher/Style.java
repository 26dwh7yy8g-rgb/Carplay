package app.verre.launcher;

import android.content.Context;
import android.graphics.Typeface;

/** Couleurs du fond, mode nuit et polices. */
final class Style {
    static final String[] NAMES = {"Aurore", "Océan", "Braise"};
    static final int[][] PALETTES = {
            {0xFF7B5CFF, 0xFFFF4D8D, 0xFF22C7FF},
            {0xFF1E6BFF, 0xFF00C2B8, 0xFF6A5CFF},
            {0xFFFF6B2C, 0xFFFF2D6F, 0xFFFFB938}};

    static final Typeface LIGHT = Typeface.create("sans-serif-light", Typeface.NORMAL);
    static final Typeface REGULAR = Typeface.create("sans-serif", Typeface.NORMAL);
    static final Typeface MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    static final Typeface BOLD = Typeface.create("sans-serif", Typeface.BOLD);

    static int[] colors = PALETTES[0];
    static boolean night;

    private Style() {}

    static void load(Context c) {
        colors = PALETTES[Prefs.palette(c)];
        night = Prefs.night(c);
    }
}
