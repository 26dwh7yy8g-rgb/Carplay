package app.verre.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.Locale;

/** Widget musique : pochette, titre, progression, précédent / lecture / suivant. */
final class MediaCard extends BaseCard {
    static final int INFO = 1, PREV = 2, PLAY = 3, NEXT = 4;

    private final RectF r = new RectF();
    private final Matrix mx = new Matrix();
    private Bitmap shaderBmp;
    private BitmapShader shader;
    private LinearGradient grad;
    private int gradC0, gradC1;

    MediaCard(Context c) {
        super(c);
        set(INFO, 26, 26, 532, 190, 26);
        set(PREV, 151, 286, 207, 342, 28);
        set(PLAY, 243, 278, 315, 350, 36);
        set(NEXT, 351, 286, 407, 342, 28);
        setContentDescription("Musique en cours");
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        MediaHelper m = host().mediaHelper;
        r.set(0, 0, w, h);
        Draw.glass(c, r, 30);

        r.set(26, 26, 190, 190);
        Bitmap art = m == null ? null : m.art();
        if (art != null && !art.isRecycled() && art.getWidth() > 0 && art.getHeight() > 0) {
            if (art != shaderBmp) {
                shaderBmp = art;
                shader = new BitmapShader(art, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            }
            float sc = Math.max(164f / art.getWidth(), 164f / art.getHeight());
            mx.setScale(sc, sc);
            mx.postTranslate(26 + (164 - art.getWidth() * sc) / 2f, 26 + (164 - art.getHeight() * sc) / 2f);
            shader.setLocalMatrix(mx);
            Draw.P.setStyle(Paint.Style.FILL);
            Draw.P.setColor(0xFFFFFFFF);
            Draw.P.setShader(shader);
            c.drawRoundRect(r, 26, 26, Draw.P);
            Draw.P.setShader(null);
        } else {
            int c0 = Style.colors[0], c1 = Style.colors[1];
            if (grad == null || c0 != gradC0 || c1 != gradC1) {
                grad = new LinearGradient(26, 26, 190, 190, c0, c1, Shader.TileMode.CLAMP);
                gradC0 = c0;
                gradC1 = c1;
            }
            Draw.P.setStyle(Paint.Style.FILL);
            Draw.P.setColor(0xFFFFFFFF);
            Draw.P.setShader(grad);
            c.drawRoundRect(r, 26, 26, Draw.P);
            Draw.P.setShader(null);
            Glyphs.draw(c, getContext(), R.drawable.g_music, 108, 108, 64, 0xFFFFFFFF);
        }

        boolean access = m != null && m.hasAccess();
        String src = m == null ? null : m.source();
        String title = m == null ? null : m.title();
        String artist = m == null ? null : m.artist();
        if (title == null) title = access ? "Aucune lecture" : "Musique en cours";
        if (artist == null) artist = access ? "Lance un son dans CarPlay, la radio ou le Bluetooth" : "Touchez ici pour afficher le titre";
        float tx = 214, tw = w - 26 - tx;
        String label = (src == null ? "Musique" : src).toUpperCase(Locale.FRENCH);
        Draw.text(c, Draw.fit(label, tw, 13, Style.MEDIUM, 0.06f), tx, 79, 13, Style.MEDIUM, U.w(0.75f), 0.06f, Paint.Align.LEFT);
        Draw.text(c, Draw.fit(title, tw, 28, Style.BOLD, -0.015f), tx, 117, 28, Style.BOLD, 0xFFFFFFFF, -0.015f, Paint.Align.LEFT);
        Draw.text(c, Draw.fit(artist, tw, 18, Style.REGULAR, 0), tx, 145, 18, Style.REGULAR, U.w(0.75f), 0, Paint.Align.LEFT);

        long dur = m == null ? -1 : m.duration();
        long pos = m == null ? -1 : m.position();
        r.set(26, 212, w - 26, 218);
        Draw.fill(c, r, 3, U.w(0.22f));
        if (dur > 0 && pos >= 0) {
            float pct = U.clamp(pos / (float) dur, 0, 1);
            if (pct > 0) {
                r.set(26, 212, 26 + (w - 52) * pct, 218);
                Draw.fill(c, r, 3, 0xFFFFFFFF);
            }
            Draw.text(c, fmt(pos), 26, 240, 13, Style.MEDIUM, U.w(0.75f), 0, Paint.Align.LEFT);
            Draw.text(c, "-" + fmt(dur - pos), w - 26, 240, 13, Style.MEDIUM, U.w(0.75f), 0, Paint.Align.RIGHT);
        }

        boolean playing = m != null && m.playing();
        Glyphs.draw(c, getContext(), R.drawable.g_prev, 179, 314, 30, 0xFFFFFFFF);
        Draw.circle(c, 279, 314, 36, 0xFFFFFFFF);
        Glyphs.draw(c, getContext(), playing ? R.drawable.g_pause : R.drawable.g_play, playing ? 279 : 281, 314, 28, 0xFF0B0D17);
        Glyphs.draw(c, getContext(), R.drawable.g_next, 379, 314, 30, 0xFFFFFFFF);
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
