package app.verre.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

/** Grand widget musique : pochette, titre sur deux lignes, progression, commandes. */
final class MediaCard extends BaseCard {
    static final int INFO = 1, PREV = 2, PLAY = 3, NEXT = 4;

    private final RectF r = new RectF();
    private final Matrix mx = new Matrix();
    private final Paint artPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Bitmap shaderBmp;
    private BitmapShader shader;

    MediaCard(Context c) {
        super(c);
        set(INFO, 28, 28, 580, 328, 18);
        set(PREV, 105, 418, 195, 508, 45);
        set(PLAY, 259, 418, 349, 508, 45);
        set(NEXT, 413, 418, 503, 508, 45);
        setContentDescription("Musique en cours");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        MediaHelper m = host().mediaHelper;
        r.set(0, 0, w, h);
        glass(c, r, 28);

        r.set(28, 28, 328, 328);
        Bitmap art = m == null ? null : m.art();
        if (art != null && !art.isRecycled() && art.getWidth() > 0 && art.getHeight() > 0) {
            if (art != shaderBmp) {
                shaderBmp = art;
                shader = new BitmapShader(art, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            }
            float sc = Math.max(300f / art.getWidth(), 300f / art.getHeight());
            mx.setScale(sc, sc);
            mx.postTranslate(28 + (300 - art.getWidth() * sc) / 2f, 28 + (300 - art.getHeight() * sc) / 2f);
            shader.setLocalMatrix(mx);
            artPaint.setShader(shader);
            c.drawRoundRect(r, 18, 18, artPaint);
        } else {
            Glass.chip(c, this, r, 18, 0.10f, Style.FILL);
            Glyphs.draw(c, getContext(), R.drawable.g_music_fill, 178, 178, 96, U.w(0.55f));
        }

        boolean access = m != null && m.hasAccess();
        String src = m == null ? null : m.source();
        String title = m == null ? null : m.title();
        String artist = m == null ? null : m.artist();
        if (title == null) title = access ? "Aucune lecture" : "Musique en cours";
        if (artist == null) artist = access ? "Lance un son dans CarPlay ou en Bluetooth" : "Touchez pour afficher le titre";
        float tx = 352, tw = w - 28 - tx;
        String[] lines = Draw.wrap2(title, tw, 30, Style.BOLD, -0.02f);
        float artistY = 322, lastTitle = artistY - 34, firstTitle = lastTitle - 34 * (lines.length - 1);
        Draw.text(c, Draw.fit(src == null ? "Musique" : src, tw, 16, Style.MEDIUM, 0), tx, firstTitle - 38, 16,
                Style.MEDIUM, Style.INK2, 0, Paint.Align.LEFT);
        for (int i = 0; i < lines.length; i++) {
            Draw.text(c, lines[i], tx, firstTitle + 34 * i, 30, Style.BOLD, Style.INK, -0.02f, Paint.Align.LEFT);
        }
        Draw.text(c, Draw.fit(artist, tw, 20, Style.MEDIUM, 0), tx, artistY, 20, Style.MEDIUM, Style.INK2, 0, Paint.Align.LEFT);

        long dur = m == null ? -1 : m.duration();
        long pos = m == null ? -1 : m.position();
        r.set(28, 360, w - 28, 366);
        Draw.fill(c, r, 3, U.w(0.22f));
        if (dur > 0 && pos >= 0) {
            float pct = U.clamp(pos / (float) dur, 0, 1);
            if (pct > 0) {
                r.set(28, 360, 28 + (w - 56) * pct, 366);
                Draw.fill(c, r, 3, Style.INK);
            }
            Draw.num(c, fmt(pos), 28, 392, 14, Style.MEDIUM, Style.INK2, 0, Paint.Align.LEFT);
            Draw.num(c, "-" + fmt(dur - pos), w - 28, 392, 14, Style.MEDIUM, Style.INK2, 0, Paint.Align.RIGHT);
        }

        boolean playing = m != null && m.playing();
        Glyphs.draw(c, getContext(), R.drawable.g_prev, 150, 463, 48, Style.INK);
        Glyphs.draw(c, getContext(), playing ? R.drawable.g_pause_round : R.drawable.g_play, playing ? 304 : 308, 463, 64, Style.INK);
        Glyphs.draw(c, getContext(), R.drawable.g_next, 458, 463, 48, Style.INK);
    }

    private static String fmt(long ms) {
        long s = Math.max(0, ms / 1000);
        return (s / 60) + ":" + U.two((int) (s % 60));
    }

    @Override
    protected void onTap(int id) {
        MainActivity a = host();
        MediaHelper m = a.mediaHelper;
        if (m == null) return;
        switch (id) {
            case INFO:
                a.openMedia();
                break;
            case PREV:
                m.prev();
                break;
            case PLAY:
                m.playPause();
                break;
            case NEXT:
                m.next();
                break;
            default:
                break;
        }
        invalidate();
    }
}
