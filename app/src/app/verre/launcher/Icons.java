package app.verre.launcher;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

/** Prépare une fois pour toutes chaque icône d'appli en carré arrondi (fait en arrière-plan). */
final class Icons {
    private Icons() {}

    static Bitmap render(Drawable d, int size) {
        Bitmap content = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(content);
        if (isAdaptive(d)) {
            drawAdaptive(cv, d, size);
        } else {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(0x26FFFFFF);
            cv.drawRect(0, 0, size, size, p);
            int in = Math.round(size * 0.14f);
            d.setBounds(in, in, size - in, size - in);
            d.draw(cv);
        }
        Bitmap out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas oc = new Canvas(out);
        Paint m = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        m.setColor(0xFFFFFFFF);
        float r = size * 0.27f;
        oc.drawRoundRect(new RectF(0, 0, size, size), r, r, m);
        m.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        oc.drawBitmap(content, 0, 0, m);
        content.recycle();
        return out;
    }

    private static boolean isAdaptive(Drawable d) {
        return Build.VERSION.SDK_INT >= 26 && d instanceof AdaptiveIconDrawable;
    }

    @TargetApi(26)
    private static void drawAdaptive(Canvas cv, Drawable d, int size) {
        AdaptiveIconDrawable a = (AdaptiveIconDrawable) d;
        int pad = size / 4;
        Drawable bg = a.getBackground(), fg = a.getForeground();
        if (bg != null) {
            bg.setBounds(-pad, -pad, size + pad, size + pad);
            bg.draw(cv);
        } else {
            cv.drawColor(0xFFFFFFFF);
        }
        if (fg != null) {
            fg.setBounds(-pad, -pad, size + pad, size + pad);
            fg.draw(cv);
        }
    }
}
