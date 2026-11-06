package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.util.HashMap;

/** Dessine les pictogrammes vectoriels (res/drawable/g_*.xml) dans la couleur voulue. */
final class Glyphs {
    private static final HashMap<Long, Drawable> CACHE = new HashMap<>();

    private Glyphs() {}

    static void draw(Canvas c, Context ctx, int res, float cx, float cy, float size, int color) {
        long key = ((long) res << 32) ^ (color & 0xFFFFFFFFL);
        Drawable d = CACHE.get(key);
        if (d == null) {
            Drawable base = ctx.getDrawable(res);
            if (base == null) return;
            d = base.mutate();
            d.setTint(color);
            CACHE.put(key, d);
        }
        int half = Math.round(size / 2f);
        int x = Math.round(cx), y = Math.round(cy);
        d.setBounds(x - half, y - half, x + half, y + half);
        d.draw(c);
    }
}
