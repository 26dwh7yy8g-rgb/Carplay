package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** Boutons ronds : Nuit, Muet, Caméra, Écran off. */
final class TogglesCard extends BaseCard {
    static final int NIGHT = 0, MUTE = 1, CAMERA = 2, SCREEN = 3;
    private static final String[] LABELS = {"Nuit", "Muet", "Caméra", "Écran off"};
    private static final int[] GLYPHS = {R.drawable.g_moon, R.drawable.g_mute, R.drawable.g_camera, R.drawable.g_screen};

    final boolean[] on = new boolean[4];
    private final RectF r = new RectF();

    TogglesCard(Context c) {
        super(c);
        float cw = (300 - 36) / 4f;
        for (int i = 0; i < 4; i++) {
            float x = 12 + i * (cw + 4);
            set(i, x, 14, x + cw, 122, 20);
        }
        setContentDescription("Raccourcis : nuit, muet, caméra, écran éteint");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        r.set(0, 0, w, h);
        Draw.glass(c, r, 30);
        for (int i = 0; i < 4; i++) {
            float cx = hit(i).r.centerX();
            boolean o = on[i];
            Draw.circle(c, cx, 56, 29, o ? 0xFFFFFFFF : U.w(0.14f));
            Glyphs.draw(c, getContext(), GLYPHS[i], cx, 56, 26, o ? 0xFF0B0D17 : 0xFFFFFFFF);
            Draw.text(c, LABELS[i], cx, 106, 12, Style.MEDIUM, 0xFFFFFFFF, 0, Paint.Align.CENTER);
        }
    }

    @Override
    protected void onTap(int id) {
        host().toggle(id);
    }

    @Override
    protected void onLongTap(int id) {
        if (id == CAMERA) host().pickRole(Roles.CAMERA);
    }
}
