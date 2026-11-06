package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** Bouton CarPlay + interrupteur « Ouverture auto ». Appui long : choisir l'appli CarPlay. */
final class CarPlayCard extends BaseCard {
    static final int MAIN = 1, AUTO = 2;

    boolean autoOn = true;
    private final RectF r = new RectF();

    CarPlayCard(Context c) {
        super(c);
        set(MAIN, 0, 0, 296, 194, 28);
        set(AUTO, 0, 194, 296, 260, 28);
        setContentDescription("Ouvrir CarPlay");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        r.set(0, 0, w, h);
        glass(c, r, 28);

        Draw.circle(c, 74, 74, 50, Style.GREEN);
        if (Glass.on()) {
            r.set(24, 24, 124, 124);
            Glass.rim(c, r, 50, 0.5f, 0f);
        }
        Glyphs.draw(c, getContext(), R.drawable.g_play, 77, 74, 44, Style.INK);
        Draw.text(c, "CarPlay", 24, 176, 28, Style.BOLD, Style.INK, -0.02f, Paint.Align.LEFT);

        Draw.text(c, "Ouverture auto", 24, 229, 16, Style.MEDIUM, Style.INK2, 0, Paint.Align.LEFT);
        r.set(w - 24 - 51, 207, w - 24, 238);
        Draw.fill(c, r, 15.5f, autoOn ? Style.GREEN : (Glass.on() ? U.w(0.22f) : 0xFF39393D));
        float kx = autoOn ? r.right - 2 - 13.5f : r.left + 2 + 13.5f;
        Draw.circle(c, kx, 222.5f, 13.5f, Style.INK);
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
