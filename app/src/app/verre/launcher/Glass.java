package app.verre.launcher;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;

/**
 * Le verre. Chaque forme redessine la portion du fond flouté qui est derrière elle :
 * - Android 13+ : vraie réfraction calculée par la carte graphique (GlassShader, d'après liquid-glass-js) ;
 * - avant : le fond légèrement grossi (effet de lentille), un voile et un reflet.
 * Sans fond coloré, les panneaux redeviennent des widgets pleins façon Apple en mode sombre.
 */
final class Glass {
    private static final Paint WP = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
    private static final Paint SP = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
    private static final Paint RIM = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint SHADOW = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Matrix M = new Matrix();
    private static final RectF R = new RectF();
    private static Bitmap shaderBmp;
    private static BitmapShader shader;
    private static final ColorMatrixColorFilter SAT;

    static {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(1.7f);
        SAT = new ColorMatrixColorFilter(cm);
        WP.setColorFilter(SAT);
        RIM.setStyle(Paint.Style.STROKE);
        RIM.setStrokeWidth(1.5f);
    }

    private Glass() {}

    static boolean on() {
        return Backdrop.bmp != null;
    }

    private static float veil() {
        return Style.night ? 0.62f : 0.46f;
    }

    /** Fond plein écran (en pixels). */
    static void wallpaper(Canvas c, int w, int h) {
        c.drawColor(0xFF000000);
        Bitmap b = Backdrop.bmp;
        if (b == null) return;
        c.save();
        c.scale(w / (float) b.getWidth(), h / (float) b.getHeight());
        c.drawBitmap(b, 0, 0, WP);
        c.restore();
        c.drawColor(U.a(0x000000, veil()));
    }

    /** Racine (Stage) si v en est un enfant direct : on sait alors exactement ce qu'il y a derrière. */
    private static View root(View v) {
        if (!(v.getParent() instanceof Stage)) return null;
        View r = (View) v.getParent();
        return r.getWidth() > 0 ? r : null;
    }

    /** Prépare le fond flouté dans le repère de v (unités maquette). lens = grossissement au centre de r. */
    private static BitmapShader backdrop(View v, View root, Bitmap b, RectF r, float lens) {
        if (b != shaderBmp) {
            shaderBmp = b;
            shader = new BitmapShader(b, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        }
        M.setScale(root.getWidth() / (float) b.getWidth(), root.getHeight() / (float) b.getHeight());
        M.postTranslate(-v.getLeft(), -v.getTop());
        M.postScale(1f / U.s, 1f / U.s);
        if (lens != 1f) M.postScale(lens, lens, r.centerX(), r.centerY());
        shader.setLocalMatrix(M);
        return shader;
    }

    private static boolean agsl(Canvas c) {
        return Build.VERSION.SDK_INT >= 33 && c.isHardwareAccelerated();
    }

    /** Grand panneau de verre. r est en unités maquette dans le repère de v. */
    static void panel(Canvas c, View v, RectF r, float rad, float tint) {
        Bitmap b = Backdrop.bmp;
        View root = root(v);
        if (b == null || root == null) {
            Draw.fill(c, r, rad, Style.night ? Style.CARD_NIGHT : Style.CARD);
            return;
        }
        if (agsl(c) && GlassShader.draw(c, backdrop(v, root, b, r, 1f), r, rad,
                root.getWidth() / U.s, root.getHeight() / U.s, veil(), tint)) {
            rim(c, r, rad, 0.40f, 0.08f);
            return;
        }
        SP.setShader(backdrop(v, root, b, r, 1.06f));
        SP.setColorFilter(SAT);
        c.drawRoundRect(r, rad, rad, SP);
        Draw.fill(c, r, rad, U.a(0x000000, veil()));
        Draw.fill(c, r, rad, U.w(tint));
        rim(c, r, rad, 0.55f, 0.14f);
    }

    /**
     * Petit élément de verre posé sur un panneau (bouton rond, piste, tuile).
     * Sur Android 13+, il réfracte lui aussi le fond (« verre dans le verre »).
     */
    static void chip(Canvas c, View v, RectF r, float rad, float glassAlpha, int solid) {
        Bitmap b = Backdrop.bmp;
        if (b == null) {
            Draw.fill(c, r, rad, solid);
            return;
        }
        View root = v == null ? null : root(v);
        if (root != null && agsl(c) && GlassShader.draw(c, backdrop(v, root, b, r, 1f), r, rad,
                root.getWidth() / U.s, root.getHeight() / U.s, veil(), 0.09f + glassAlpha)) {
            rim(c, r, rad, 0.45f, 0.06f);
            return;
        }
        Draw.fill(c, r, rad, U.w(glassAlpha));
        rim(c, r, rad, 0.42f, 0.06f);
    }

    /** Ombre douce sous un bouton rond qui « flotte » (seulement quand le verre est visible). */
    static void floatShadow(Canvas c, float cx, float cy, float r) {
        if (!on()) return;
        float sr = r + 14;
        SHADOW.setShader(new RadialGradient(cx, cy + 6, sr, new int[]{0x47000000, 0x00000000},
                new float[]{r / sr, 1f}, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy + 6, sr, SHADOW);
    }

    /** Reflet : bord du haut lumineux, bas presque éteint. */
    static void rim(Canvas c, RectF r, float rad, float top, float bottom) {
        RIM.setShader(new LinearGradient(0, r.top, 0, r.bottom,
                new int[]{U.w(top), U.w((top + bottom) / 3f), U.w(bottom)},
                new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
        R.set(r.left + 0.75f, r.top + 0.75f, r.right - 0.75f, r.bottom - 0.75f);
        c.drawRoundRect(R, Math.max(0, rad - 0.75f), Math.max(0, rad - 0.75f), RIM);
    }
}
