package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Horloge, date et semaine en cours. Appui : menu « Personnaliser ». */
final class ClockCard extends BaseCard {
    private static final String[] LETTERS = {"L", "M", "M", "J", "V", "S", "D"};
    private final SimpleDateFormat fmt = new SimpleDateFormat("EEEE d MMMM", Locale.FRENCH);
    private final RectF r = new RectF();

    ClockCard(Context c) {
        super(c);
        set(1, 0, 0, 330, 376, 30).overlay = false;
        setContentDescription("Horloge. Touchez pour personnaliser.");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        r.set(0, 0, w, h);
        Draw.glass(c, r, 30);
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY), min = cal.get(Calendar.MINUTE);
        Draw.text(c, hour >= 5 && hour < 18 ? "Bonjour" : "Bonsoir", 26, 41, 15, Style.MEDIUM, U.w(0.75f), 0, Paint.Align.LEFT);
        Draw.text(c, U.two(hour) + ":" + U.two(min), 22, 128, 96, Style.LIGHT, 0xFFFFFFFF, -0.03f, Paint.Align.LEFT);
        String date = Text.cap(fmt.format(cal.getTime()));
        Draw.text(c, Draw.fit(date, w - 52, 20, Style.MEDIUM, 0), 26, 172, 20, Style.MEDIUM, 0xFFFFFFFF, 0, Paint.Align.LEFT);

        int dow = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7; // lundi = 0
        Calendar d = (Calendar) cal.clone();
        d.add(Calendar.DAY_OF_MONTH, -dow);
        float top = h - 26 - 58, cw = (w - 52 - 24) / 7f;
        for (int i = 0; i < 7; i++) {
            float x = 26 + i * (cw + 4);
            boolean on = i == dow;
            if (on) {
                r.set(x, top, x + cw, top + 58);
                Draw.fill(c, r, 14, 0xFFFFFFFF);
            }
            int fg = on ? 0xFF0B0D17 : U.w(0.8f);
            Draw.text(c, LETTERS[i], x + cw / 2, top + 22, 11, Style.MEDIUM, fg, 0.04f, Paint.Align.CENTER);
            Draw.text(c, String.valueOf(d.get(Calendar.DAY_OF_MONTH)), x + cw / 2, top + 46, 17, Style.MEDIUM, fg, 0, Paint.Align.CENTER);
            d.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    @Override
    protected void onTap(int id) {
        host().showMenu();
    }

    @Override
    protected void onLongTap(int id) {
        host().showMenu();
    }
}
