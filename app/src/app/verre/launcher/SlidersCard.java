package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;

/** Deux curseurs verticaux (volume, luminosité) qu'on fait glisser vers le haut ou le bas. */
final class SlidersCard extends BaseCard {
    static final int VOL = 1, LUM = 2;
    private static final float TOP = 24, BOTTOM = 236;

    float vol = 0.6f, lum = 0.7f;
    private boolean lumBlocked;
    private final RectF r = new RectF();

    SlidersCard(Context c) {
        super(c);
        set(VOL, 24, TOP, 140, BOTTOM, 26).drag = true;
        set(LUM, 156, TOP, 272, BOTTOM, 26).drag = true;
        setContentDescription("Volume et luminosité");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        r.set(0, 0, w, h);
        glass(c, r, 28);
        slider(c, hit(VOL).r, vol, R.drawable.g_volume_fill);
        slider(c, hit(LUM).r, lum, R.drawable.g_sun_fill);
    }

    private void slider(Canvas c, RectF box, float pct, int glyph) {
        Glass.chip(c, this, box, 26, 0.14f, Style.FILL);
        float p = U.clamp(pct, 0, 1);
        float top = box.bottom - box.height() * p;
        if (p > 0.005f) {
            c.save();
            c.clipRect(box.left, top, box.right, box.bottom);
            Draw.fill(c, box, 26, Style.INK);
            c.restore();
        }
        float gy = box.bottom - 37;
        Glyphs.draw(c, getContext(), glyph, box.centerX(), gy, 30, top < gy ? Style.DARK : Style.INK);
    }

    @Override
    protected void onDrag(int id, float x, float y, boolean first) {
        MainActivity a = host();
        float pct = U.clamp((BOTTOM - y) / (BOTTOM - TOP), 0, 1);
        if (id == VOL) {
            vol = pct;
            a.setVolume(pct);
        } else {
            if (first) {
                lumBlocked = !a.canWriteBrightness();
                if (lumBlocked) {
                    a.askWriteSettings();
                    return;
                }
            }
            if (lumBlocked) return;
            lum = Math.max(0.02f, pct);
            a.setBrightness(lum);
        }
        invalidate();
    }
}
