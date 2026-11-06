package app.verre.launcher;

/** Échelle de l'interface : tout est dessiné en « unités maquette » (écran de 1280×720). */
final class U {
    static float s = 1f;

    private U() {}

    static int a(int rgb, float alpha) {
        int al = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
        return (al << 24) | (rgb & 0x00FFFFFF);
    }

    static int w(float alpha) {
        return a(0xFFFFFF, alpha);
    }

    static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    static String two(int v) {
        return v < 10 ? "0" + v : String.valueOf(v);
    }
}
