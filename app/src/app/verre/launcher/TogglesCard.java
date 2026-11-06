package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** Boutons ronds : Nuit, Muet, Caméra, Veille. */
final class TogglesCard extends BaseCard {
    static final int NIGHT = 0, MUTE = 1, CAMERA = 2, SCREEN = 3;
    private static final String[] LABELS = {"Nuit", "Muet", "Caméra", "Veille"};
    private static final int[] GLYPHS = {R.drawable.g_moon_fill, R.drawable.g_mute_fill, R.drawable.g_camera, R.drawable.g_screen};
    private static final float[] CX = {83, 213, 83, 213};
    private static final float[] CY = {56, 56, 171, 171};

    final boolean[] on = new boolean[4];
    private final RectF r = new RectF();

    TogglesCard(Context c) {
        super(c);
        for (int i = 0; i < 4; i++) set(i, CX[i] - 62, CY[i] - 44, CX[i] + 62, CY[i] + 66, 22);
        setContentDescription("Raccourcis : nuit, muet, caméra, veille");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        r.set(0, 0, w, h);
        glass(c, r, 28);
        for (int i = 0; i < 4; i++) {
            r.set(CX[i] - 34, CY[i] - 34, CX[i] + 34, CY[i] + 34);
            Glass.floatShadow(c, CX[i], CY[i], 34);
            if (on[i]) {
                Draw.circle(c, CX[i], CY[i], 34, i == MUTE ? Style.RED : Style.BLUE);
            } else {
                Glass.chip(c, this, r, 34, 0.16f, Style.FILL);
            }
            Glyphs.draw(c, getContext(), GLYPHS[i], CX[i], CY[i], 28, Style.INK);
            Draw.text(c, LABELS[i], CX[i], CY[i] + 57, 15, Style.MEDIUM, Style.INK, 0, Paint.Align.CENTER);
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
