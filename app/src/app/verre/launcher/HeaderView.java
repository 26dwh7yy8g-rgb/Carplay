package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Calendar;

/** En-tête de l'écran Applis : retour, titre, champ de recherche, heure. */
final class HeaderView extends BaseCard {
    static final int BACK = 1, SEARCH = 2;
    private final RectF r = new RectF();

    HeaderView(Context c) {
        super(c);
        set(SEARCH, 690, 2, 1070, 54, 26).overlay = false;
        set(BACK, 0, 0, 56, 56, 28);
        setContentDescription("Retour à l'accueil");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        r.set(0, 0, 56, 56);
        Draw.fill(c, r, 28, U.w(0.14f));
        Draw.stroke(c, r, 28, U.w(0.2f));
        Glyphs.draw(c, getContext(), R.drawable.g_back, 27, 28, 26, 0xFFFFFFFF);
        Draw.text(c, "Applis", 74, 40, 34, Style.BOLD, 0xFFFFFFFF, -0.015f, Paint.Align.LEFT);

        r.set(690, 2, 1070, 54);
        Draw.fill(c, r, 26, U.w(0.12f));
        Draw.stroke(c, r, 26, U.w(0.18f));
        Glyphs.draw(c, getContext(), R.drawable.g_search, 718, 28, 20, U.w(0.8f));

        Calendar cal = Calendar.getInstance();
        String t = U.two(cal.get(Calendar.HOUR_OF_DAY)) + ":" + U.two(cal.get(Calendar.MINUTE));
        Draw.text(c, t, w, 36, 20, Style.MEDIUM, 0xFFFFFFFF, 0, Paint.Align.RIGHT);
    }

    @Override
    protected void onTap(int id) {
        if (id == BACK) host().apps.hide();
        else host().apps.focusSearch();
    }
}
