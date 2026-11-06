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
    private static final RectF R2 = new RectF();

    private Draw() {}

    static void fill(Canvas c, RectF r, float rad, int color) {
        P.setShader(null);
        P.setStyle(Paint.Style.FILL);
        P.setColor(color);
        c.drawRoundRect(r, rad, rad, P);
    }

    static void stroke(Canvas c, RectF r, float rad, int color) {
        P.setShader(null);
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(1f);
        P.setColor(color);
        R2.set(r.left + 0.5f, r.top + 0.5f, r.right - 0.5f, r.bottom - 0.5f);
        c.drawRoundRect(R2, rad - 0.5f, rad - 0.5f, P);
        P.setStyle(Paint.Style.FILL);
    }

    static void highlight(Canvas c, RectF r, float rad, int color) {
        P.setShader(null);
        P.setStyle(Paint.Style.FILL);
        P.setColor(color);
        c.drawRect(r.left + rad * 0.8f, r.top + 1f, r.right - rad * 0.8f, r.top + 2f, P);
    }

    /** Panneau « verre dépoli » : voile blanc, liseré et reflet en haut. */
    static void glass(Canvas c, RectF r, float rad, float alpha) {
        fill(c, r, rad, U.w(Style.night ? alpha * 0.7f : alpha));
        stroke(c, r, rad, U.w(0.16f));
        highlight(c, r, rad, U.w(0.22f));
    }

    static void glass(Canvas c, RectF r, float rad) {
        glass(c, r, rad, 0.09f);
    }

    static void circle(Canvas c, float cx, float cy, float r, int color) {
        P.setShader(null);
        P.setStyle(Paint.Style.FILL);
        P.setColor(color);
        c.drawCircle(cx, cy, r, P);
    }

    static void bitmap(Canvas c, Bitmap b, RectF dst) {
        P.setShader(null);
        P.setColor(0xFFFFFFFF);
        c.drawBitmap(b, null, dst, P);
    }

    static void text(Canvas c, String s, float x, float y, float size, Typeface tf, int color, float ls, Paint.Align al) {
        if (s == null) return;
        T.setTextSize(size);
        T.setTypeface(tf);
        T.setColor(color);
        T.setLetterSpacing(ls);
        T.setTextAlign(al);
        c.drawText(s, x, y, T);
    }

    static String fit(String s, float max, float size, Typeface tf, float ls) {
        if (s == null) return "";
        T.setTextSize(size);
        T.setTypeface(tf);
        T.setLetterSpacing(ls);
        return TextUtils.ellipsize(s, T, max, TextUtils.TruncateAt.END).toString();
    }
}
