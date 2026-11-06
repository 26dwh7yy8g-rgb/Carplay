package app.verre.launcher;

import java.text.Normalizer;
import java.util.Locale;

final class Text {
    private Text() {}

    /** minuscules, sans accents, seulement lettres/chiffres séparés par des espaces. */
    static String norm(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    static String[] tokens(String s) {
        String n = norm(s);
        return n.isEmpty() ? new String[0] : n.split(" ");
    }

    static String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
