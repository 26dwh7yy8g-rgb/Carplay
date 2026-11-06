package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

/** Dock en verre : icônes en couleur + accès à toutes les applis. Appui long : choisir l'appli. */
final class DockCard extends BaseCard {
    static final String[] ROLES = {Roles.RADIO, Roles.MUSIC, Roles.PHONE, Roles.SOUND, Roles.CAMERA, Roles.SETTINGS};
    private static final String[] LABELS = {"Radio", "Musique", "Téléphone", "Son", "Caméra", "Réglages"};
    private static final int[] GLYPHS = {R.drawable.g_radio, R.drawable.g_music, R.drawable.g_phone,
            R.drawable.g_sound, R.drawable.g_camera, R.drawable.g_settings};
    private static final int[][] GRADS = {
            {0xFFFFB347, 0xFFFF6A3D}, {0xFFFF6B9A, 0xFFE0245E}, {0xFF63E6A0, 0xFF1FA35F},
            {0xFFFF7AC6, 0xFF8B5CFF}, {0xFF9AA5B8, 0xFF4E5868}, {0xFFA8B0BD, 0xFF5A6170}};
    static final int APPS = 6;

    private final LinearGradient[] shaders = new LinearGradient[6];
    private final RectF r = new RectF();

    DockCard(Context c) {
        super(c);
        for (int i = 0; i < 6; i++) {
            float l = 16 + i * 94;
            set(i, l, 6, l + 84, 110, 18);
            float il = l + 10;
            shaders[i] = new LinearGradient(il + 21, 15.5f, il + 43, 79.5f, GRADS[i][0], GRADS[i][1], Shader.TileMode.CLAMP);
        }
        set(APPS, 591, 6, 675, 110, 18);
        setContentDescription("Dock des applis");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        r.set(0, 0, w, h);
        Draw.glass(c, r, 30, 0.12f);
        for (int i = 0; i < 6; i++) {
            float l = 16 + i * 94;
            r.set(l + 10, 15.5f, l + 74, 79.5f);
            Draw.P.setStyle(Paint.Style.FILL);
            Draw.P.setColor(0xFFFFFFFF);
            Draw.P.setShader(shaders[i]);
            c.drawRoundRect(r, 18, 18, Draw.P);
            Draw.P.setShader(null);
            Draw.highlight(c, r, 18, U.w(0.35f));
            Glyphs.draw(c, getContext(), GLYPHS[i], l + 42, 47.5f, 30, 0xFFFFFFFF);
            Draw.text(c, LABELS[i], l + 42, 98, 12, Style.MEDIUM, 0xFFFFFFFF, 0, Paint.Align.CENTER);
        }
        r.set(580, 30, 581, 86);
        Draw.fill(c, r, 0, U.w(0.22f));
        r.set(601, 15.5f, 665, 79.5f);
        Draw.fill(c, r, 18, U.w(0.16f));
        Draw.stroke(c, r, 18, U.w(0.25f));
        Glyphs.draw(c, getContext(), R.drawable.g_apps, 633, 47.5f, 30, 0xFFFFFFFF);
        Draw.text(c, "Applis", 633, 98, 12, Style.MEDIUM, 0xFFFFFFFF, 0, Paint.Align.CENTER);
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
