package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Calendar;

/** En-tête de l'écran Applis : retour, titre, champ de recherche, heure. */
final class HeaderView extends BaseCard {
    static final int BACK = 1, SEARCH = 2;
    static final float PILL_L = 698, PILL_R = 1078;
    private final RectF r = new RectF();

    HeaderView(Context c) {
        super(c);
        set(SEARCH, PILL_L, 2, PILL_R, 54, 26).overlay = false;
        set(BACK, 0, 0, 56, 56, 28);
        setContentDescription("Retour à l'accueil");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        r.set(0, 0, 56, 56);
        Glass.chip(c, this, r, 28, 0.16f, Style.FILL);
        Glyphs.draw(c, getContext(), R.drawable.g_back, 27, 28, 26, Style.INK);
        Draw.text(c, "Applis", 74, 40, 34, Style.BOLD, Style.INK, -0.02f, Paint.Align.LEFT);

        r.set(PILL_L, 2, PILL_R, 54);
        Glass.chip(c, this, r, 26, 0.14f, Style.FILL);
        Glyphs.draw(c, getContext(), R.drawable.g_search, PILL_L + 28, 28, 20, Style.INK2);

        Calendar cal = Calendar.getInstance();
        String t = U.two(cal.get(Calendar.HOUR_OF_DAY)) + ":" + U.two(cal.get(Calendar.MINUTE));
        Draw.num(c, t, w, 36, 20, Style.SEMIBOLD, Style.INK, 0, Paint.Align.RIGHT);
    }

    @Override
    protected void onTap(int id) {
        if (id == BACK) host().apps.hide();
        else host().apps.focusSearch();
    }
}
