package app.verre.launcher;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

/** Petites aides de dessin partagées (toujours sur le thread de l'interface). */
final class Draw {
    static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    static final TextPaint T = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

    private Draw() {}

    static void fill(Canvas c, RectF r, float rad, int color) {
        P.setShader(null);
        P.setStyle(Paint.Style.FILL);
        P.setColor(color);
        c.drawRoundRect(r, rad, rad, P);
    }

    static void circle(Canvas c, float cx, float cy, float r, int color) {
        P.setShader(null);
        P.setStyle(Paint.Style.FILL);
        P.setColor(color);
        c.drawCircle(cx, cy, r, P);
    }

    static void highlight(Canvas c, RectF r, float rad, int color) {
        P.setShader(null);
        P.setStyle(Paint.Style.FILL);
        P.setColor(color);
        c.drawRect(r.left + rad * 0.8f, r.top + 1f, r.right - rad * 0.8f, r.top + 2f, P);
    }

    static void bitmap(Canvas c, Bitmap b, RectF dst) {
        P.setShader(null);
        P.setColor(0xFFFFFFFF);
        c.drawBitmap(b, null, dst, P);
    }

    private static void setup(float size, Typeface tf, int color, float ls, Paint.Align al, boolean tnum) {
        T.setTextSize(size);
        T.setTypeface(tf);
        T.setColor(color);
        T.setLetterSpacing(ls);
        T.setTextAlign(al);
        T.setFontFeatureSettings(tnum ? "tnum" : null);
    }

    static void text(Canvas c, String s, float x, float y, float size, Typeface tf, int color, float ls, Paint.Align al) {
        if (s == null) return;
        setup(size, tf, color, ls, al, false);
        c.drawText(s, x, y, T);
    }

    /** Chiffres à chasse fixe (l'heure ne « danse » pas quand elle change). */
    static void num(Canvas c, String s, float x, float y, float size, Typeface tf, int color, float ls, Paint.Align al) {
        if (s == null) return;
        setup(size, tf, color, ls, al, true);
        c.drawText(s, x, y, T);
    }

    static String fit(String s, float max, float size, Typeface tf, float ls) {
        if (s == null) return "";
        setup(size, tf, 0, ls, Paint.Align.LEFT, false);
        return TextUtils.ellipsize(s, T, max, TextUtils.TruncateAt.END).toString();
    }

    /** Coupe un texte en deux lignes au plus (la seconde est raccourcie avec « … »). */
    static String[] wrap2(String s, float max, float size, Typeface tf, float ls) {
        if (s == null) return new String[]{""};
        setup(size, tf, 0, ls, Paint.Align.LEFT, false);
        if (T.measureText(s) <= max) return new String[]{s};
        int cut = -1;
        for (int i = s.length() - 1; i > 0; i--) {
            if (s.charAt(i) == ' ' && T.measureText(s, 0, i) <= max) {
                cut = i;
                break;
            }
        }
        if (cut < 0) return new String[]{fit(s, max, size, tf, ls)};
        return new String[]{s.substring(0, cut), fit(s.substring(cut + 1), max, size, tf, ls)};
    }
}
