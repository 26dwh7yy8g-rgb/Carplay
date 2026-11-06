package app.verre.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;

import java.io.File;

/**
 * Le fond « Liquid Glass » : la pochette en cours (ou ta photo) réduite à 128×72 et floutée
 * UNE fois par changement. Ensuite on se contente de l'afficher agrandie : aucun flou en direct.
 */
final class Backdrop {
    static final int W = 128, H = 72;
    static volatile Bitmap bmp;

    private static Object key;
    private static Bitmap photo;
    private static long photoStamp = -1;

    private Backdrop() {}

    static File photoFile(Context c) {
        return new File(c.getFilesDir(), "fond.jpg");
    }

    /** Recalcule le fond si sa source a changé. Renvoie vrai s'il faut redessiner. */
    static boolean update(Context c, Bitmap art) {
        int mode = Prefs.bgMode(c);
        Object k = null;
        Bitmap src = null;
        if (mode == Prefs.BG_AUTO && art != null && !art.isRecycled()) {
            k = art;
            src = art;
        } else if (mode != Prefs.BG_BLACK) {
            Bitmap p = photo(c);
            if (p != null) {
                k = p;
                src = p;
            }
        }
        if (k == key) return false;
        key = k;
        bmp = src == null ? null : blur(src);
        return true;
    }

    static void forget() {
        key = null;
        photo = null;
        photoStamp = -1;
    }

    private static Bitmap photo(Context c) {
        File f = photoFile(c);
        if (!f.exists()) {
            photo = null;
            return null;
        }
        if (photo == null || photoStamp != f.lastModified()) {
            try {
                photo = BitmapFactory.decodeFile(f.getPath());
            } catch (Throwable t) {
                photo = null;
            }
            photoStamp = f.lastModified();
        }
        return photo;
    }

    static Bitmap blur(Bitmap src) {
        try {
            if (Build.VERSION.SDK_INT >= 26 && src.getConfig() == Bitmap.Config.HARDWARE) {
                src = src.copy(Bitmap.Config.ARGB_8888, false);
            }
            int sw = src.getWidth(), sh = src.getHeight();
            if (sw <= 0 || sh <= 0) return null;
            float target = (float) W / H;
            int cw = sw, ch = sh;
            if ((float) sw / sh > target) cw = Math.round(sh * target);
            else ch = Math.round(sw / target);
            int cx = (sw - cw) / 2, cy = (sh - ch) / 2;
            Bitmap small = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
            new Canvas(small).drawBitmap(src, new Rect(cx, cy, cx + cw, cy + ch), new Rect(0, 0, W, H),
                    new Paint(Paint.FILTER_BITMAP_FLAG));
            int[] px = new int[W * H];
            small.getPixels(px, 0, W, 0, 0, W, H);
            for (int i = 0; i < 3; i++) {
                boxH(px, W, H, 5);
                boxV(px, W, H, 5);
            }
            small.setPixels(px, 0, W, 0, 0, W, H);
            return small;
        } catch (Throwable t) {
            return null;
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private static void boxH(int[] px, int w, int h, int r) {
        int[] row = new int[w];
        int div = 2 * r + 1;
        for (int y = 0; y < h; y++) {
            int off = y * w, sr = 0, sg = 0, sb = 0;
            for (int i = -r; i <= r; i++) {
                int c = px[off + clamp(i, 0, w - 1)];
                sr += (c >> 16) & 255;
                sg += (c >> 8) & 255;
                sb += c & 255;
            }
            for (int x = 0; x < w; x++) {
                row[x] = 0xFF000000 | ((sr / div) << 16) | ((sg / div) << 8) | (sb / div);
                int out = px[off + clamp(x - r, 0, w - 1)];
                int in = px[off + clamp(x + r + 1, 0, w - 1)];
                sr += ((in >> 16) & 255) - ((out >> 16) & 255);
                sg += ((in >> 8) & 255) - ((out >> 8) & 255);
                sb += (in & 255) - (out & 255);
            }
            System.arraycopy(row, 0, px, off, w);
        }
    }

    private static void boxV(int[] px, int w, int h, int r) {
        int[] col = new int[h];
        int div = 2 * r + 1;
        for (int x = 0; x < w; x++) {
            int sr = 0, sg = 0, sb = 0;
            for (int i = -r; i <= r; i++) {
                int c = px[clamp(i, 0, h - 1) * w + x];
                sr += (c >> 16) & 255;
                sg += (c >> 8) & 255;
                sb += c & 255;
            }
            for (int y = 0; y < h; y++) {
                col[y] = 0xFF000000 | ((sr / div) << 16) | ((sg / div) << 8) | (sb / div);
                int out = px[clamp(y - r, 0, h - 1) * w + x];
                int in = px[clamp(y + r + 1, 0, h - 1) * w + x];
                sr += ((in >> 16) & 255) - ((out >> 16) & 255);
                sg += ((in >> 8) & 255) - ((out >> 8) & 255);
                sb += (in & 255) - (out & 255);
            }
            for (int y = 0; y < h; y++) px[y * w + x] = col[y];
        }
    }
}
