package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

/** Grosse tuile CarPlay + interrupteur « Ouverture auto ». Appui long : choisir l'appli CarPlay. */
final class CarPlayCard extends BaseCard {
    static final int MAIN = 1, AUTO = 2;

    boolean autoOn = true;
    private final RectF r = new RectF();
    private LinearGradient grad;
    private int g0, g1;
    private float gw, gh;

    CarPlayCard(Context c) {
        super(c);
        set(MAIN, 22, 52, 278, 284, 24);
        set(AUTO, 22, 298, 278, 354, 18);
        setContentDescription("Ouvrir CarPlay");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        int c0 = U.a(Style.colors[0], 0.55f), c1 = U.a(Style.colors[2], 0.30f);
        if (grad == null || c0 != g0 || c1 != g1 || w != gw || h != gh) {
            grad = new LinearGradient(w * 0.33f, 0, w * 0.67f, h, c0, c1, Shader.TileMode.CLAMP);
            g0 = c0;
            g1 = c1;
            gw = w;
            gh = h;
        }
        r.set(0, 0, w, h);
        Draw.fill(c, r, 30, U.w(Style.night ? 0.03f : 0.05f));
        Draw.P.setStyle(Paint.Style.FILL);
        Draw.P.setColor(0xFFFFFFFF);
        Draw.P.setShader(grad);
        c.drawRoundRect(r, 30, 30, Draw.P);
        Draw.P.setShader(null);
        Draw.stroke(c, r, 30, U.w(0.22f));
        Draw.highlight(c, r, 30, U.w(0.3f));

        Draw.text(c, "SANS FIL", 22, 35, 13, Style.MEDIUM, U.w(0.85f), 0.06f, Paint.Align.LEFT);
        float cx = w / 2;
        r.set(cx - 56, 68, cx + 56, 180);
        Draw.fill(c, r, 30, U.w(0.96f));
        Glyphs.draw(c, getContext(), R.drawable.g_play, cx + 3, 124, 52, Style.colors[0]);
        Draw.text(c, "CarPlay", cx, 223, 34, Style.BOLD, 0xFFFFFFFF, -0.015f, Paint.Align.CENTER);
        Draw.text(c, "Appuyer pour ouvrir", cx, 253, 15, Style.MEDIUM, U.w(0.85f), 0, Paint.Align.CENTER);

        r.set(22, 298, w - 22, 354);
        Draw.fill(c, r, 18, U.a(0x000000, 0.22f));
        Draw.text(c, "Ouverture auto", 36, 331, 15, Style.MEDIUM, 0xFFFFFFFF, 0, Paint.Align.LEFT);
        float tl = w - 36 - 52;
        r.set(tl, 310, tl + 52, 342);
        Draw.fill(c, r, 16, autoOn ? 0xFF3FCF7A : U.w(0.25f));
        Draw.circle(c, tl + (autoOn ? 23 : 3) + 13, 326, 13, 0xFFFFFFFF);
    }

    @Override
    protected void onTap(int id) {
        if (id == MAIN) host().openRole(Roles.CARPLAY, true);
        else host().toggleAutoOpen();
    }

    @Override
    protected void onLongTap(int id) {
        if (id == MAIN) host().pickRole(Roles.CARPLAY);
    }
}
