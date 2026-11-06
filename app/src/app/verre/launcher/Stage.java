package app.verre.launcher;

import android.content.Context;

/** Racine : calcule l'échelle pour que la maquette 1280×720 tienne et soit centrée sur tout écran. */
final class Stage extends Abs {
    private float ox, oy;

    Stage(Context c) {
        super(c);
    }

    @Override
    protected void onScale(int w, int h) {
        if (w <= 0 || h <= 0) return;
        U.s = Math.min(w / 1280f, h / 720f);
        ox = (w - 1280 * U.s) / 2f;
        oy = (h - 720 * U.s) / 2f;
    }

    @Override
    protected float ox() {
        return ox;
    }

    @Override
    protected float oy() {
        return oy;
    }
}
