package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

/**
 * Fond aux couleurs floues. Les « taches » sont des dégradés radiaux : même rendu qu'un flou,
 * sans aucun calcul de flou en direct (c'est ce qui garde l'autoradio fluide).
 */
final class WallpaperView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

    WallpaperView(Context c) {
        super(c);
    }

    @Override
    protected void onDraw(Canvas c) {
        float s = U.s;
        float ox = (getWidth() - 1280 * s) / 2f, oy = (getHeight() - 720 * s) / 2f;
        c.drawColor(0xFF070812);
        int[] col = Style.colors;
        blob(c, ox + 170 * s, oy + 130 * s, 480 * s, col[0], 0.75f);
        blob(c, ox + 1090 * s, oy + 590 * s, 560 * s, col[1], 0.6f);
        blob(c, ox + 750 * s, oy + 310 * s, 380 * s, col[2], 0.5f);
        c.drawColor(U.a(0x04050C, Style.night ? 0.62f : 0.4f));
    }

    private void blob(Canvas c, float x, float y, float r, int color, float a) {
        p.setShader(new RadialGradient(x, y, r,
                new int[]{U.a(color, a), U.a(color, a * 0.6f), U.a(color, a * 0.22f), U.a(color, 0f)},
                new float[]{0f, 0.38f, 0.72f, 1f}, Shader.TileMode.CLAMP));
        c.drawCircle(x, y, r, p);
    }
}
