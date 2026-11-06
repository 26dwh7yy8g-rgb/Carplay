package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** Curseurs Volume et Luminosité (on glisse le doigt dessus). */
final class SlidersCard extends BaseCard {
    static final int VOL = 1, LUM = 2;

    float vol = 0.45f, lum = 0.7f;
    private boolean lumBlocked;
    private final RectF r = new RectF();

    SlidersCard(Context c) {
        super(c);
        set(VOL, 20, 20, 445, 116, 24).drag = true;
        set(LUM, 461, 20, 886, 116, 24).drag = true;
        setContentDescription("Volume et luminosité");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        r.set(0, 0, w, h);
        Draw.glass(c, r, 30);
        float sw = (w - 56) / 2f;
        slider(c, 20, sw, vol, R.drawable.g_volume, "Volume");
        slider(c, 20 + sw + 16, sw, lum, R.drawable.g_sun, "Luminosité");
    }

    private void slider(Canvas c, float x, float sw, float pct, int glyph, String label) {
        r.set(x, 20, x + sw, 116);
        Draw.fill(c, r, 24, U.w(0.10f));
        float fw = sw * U.clamp(pct, 0, 1);
        if (fw > 0.5f) {
            c.save();
            c.clipRect(x, 20, x + fw, 116);
            Draw.fill(c, r, 24, U.w(0.32f));
            c.restore();
        }
        Glyphs.draw(c, getContext(), glyph, x + 38, 68, 28, 0xFFFFFFFF);
        Draw.text(c, label, x + 66, 74, 18, Style.MEDIUM, 0xFFFFFFFF, 0, Paint.Align.LEFT);
        Draw.text(c, Math.round(U.clamp(pct, 0, 1) * 100) + " %", x + sw - 24, 74, 18, Style.MEDIUM, 0xFFFFFFFF, 0, Paint.Align.RIGHT);
    }

    @Override
    protected void onDrag(int id, float x, float y, boolean first) {
        MainActivity a = host();
        Hit hh = hit(id);
        float pct = U.clamp((x - hh.r.left) / hh.r.width(), 0, 1);
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
