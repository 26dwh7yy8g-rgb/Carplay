package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/** Fond : pochette floutée (ou ta photo), sinon noir. */
final class WallpaperView extends View {
    WallpaperView(Context c) {
        super(c);
    }

    @Override
    protected void onDraw(Canvas c) {
        Glass.wallpaper(c, getWidth(), getHeight());
    }
}
