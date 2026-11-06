package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** Dock : icônes de couleur unie + « Applis ». Appui long : choisir l'appli de chaque icône. */
final class DockCard extends BaseCard {
    static final String[] ROLES = {Roles.RADIO, Roles.MUSIC, Roles.PHONE, Roles.SOUND, Roles.CAMERA, Roles.SETTINGS};
    private static final String[] LABELS = {"Radio", "Musique", "Téléphone", "Son", "Caméra", "Réglages", "Applis"};
    private static final int[] GLYPHS = {R.drawable.g_radio, R.drawable.g_music_fill, R.drawable.g_phone_fill,
            R.drawable.g_sound, R.drawable.g_camera, R.drawable.g_settings};
    private static final int[] COLORS = {Style.ORANGE, Style.PINK, Style.GREEN, Style.BLUE, Style.GRAY, Style.GRAY2};
    static final int APPS = 6;
    private static final float CW = (1232f - 48f) / 7f;

    private final RectF r = new RectF();

    DockCard(Context c) {
        super(c);
        for (int i = 0; i < 7; i++) set(i, 24 + CW * i, 4, 24 + CW * (i + 1), 116, 20);
        setContentDescription("Dock des applis");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        r.set(0, 0, w, h);
        glass(c, r, 28);
        for (int i = 0; i < 7; i++) {
            float cx = 24 + CW * (i + 0.5f);
            r.set(cx - 32, 16, cx + 32, 80);
            if (i < 6) {
                Draw.fill(c, r, 16, COLORS[i]);
                Draw.highlight(c, r, 16, U.w(0.45f));
                Glyphs.draw(c, getContext(), GLYPHS[i], cx, 48, 34, Style.INK);
            } else {
                Glass.chip(c, this, r, 16, 0.18f, 0xFF3A3A3C);
                mini(c, cx - 11.5f, 36.5f, Style.ORANGE);
                mini(c, cx + 1.5f, 36.5f, Style.BLUE);
                mini(c, cx - 11.5f, 49.5f, Style.GREEN);
                mini(c, cx + 1.5f, 49.5f, Style.PINK);
            }
            Draw.text(c, LABELS[i], cx, 104, 14, Style.MEDIUM, Style.INK, 0, Paint.Align.CENTER);
        }
    }

    private void mini(Canvas c, float x, float y, int color) {
        r.set(x, y, x + 10, y + 10);
        Draw.fill(c, r, 3, color);
    }

    @Override
    protected void onTap(int id) {
        if (id == APPS) host().showApps();
        else host().openRole(ROLES[id], true);
    }

    @Override
    protected void onLongTap(int id) {
        if (id == APPS) host().showApps();
        else host().pickRole(ROLES[id]);
    }
}
