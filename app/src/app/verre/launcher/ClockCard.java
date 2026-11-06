package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Jour (en rouge), date et heure en très grand. Toucher : menu « Personnaliser ». */
final class ClockCard extends BaseCard {
    private final SimpleDateFormat day = new SimpleDateFormat("EEEE", Locale.FRENCH);
    private final SimpleDateFormat date = new SimpleDateFormat("d MMMM", Locale.FRENCH);
    private final RectF r = new RectF();

    ClockCard(Context c) {
        super(c);
        set(1, 0, 0, 296, 260, 28).overlay = false;
        setContentDescription("Date et heure. Touchez pour personnaliser.");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        r.set(0, 0, w, h);
        glass(c, r, 28);
        Calendar cal = Calendar.getInstance();
        Draw.text(c, Text.cap(day.format(cal.getTime())), 24, 43, 20, Style.SEMIBOLD,
                Glass.on() ? Style.RED_SOFT : Style.RED, 0, Paint.Align.LEFT);
        Draw.text(c, date.format(cal.getTime()), 24, 69, 20, Style.SEMIBOLD, Style.INK, 0, Paint.Align.LEFT);
        String t = U.two(cal.get(Calendar.HOUR_OF_DAY)) + ":" + U.two(cal.get(Calendar.MINUTE));
        Draw.num(c, t, 21, h - 32, 90, Style.SEMIBOLD, Style.INK, -0.045f, Paint.Align.LEFT);
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
