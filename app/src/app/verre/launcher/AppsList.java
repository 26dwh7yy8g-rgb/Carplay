package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

/**
 * Contenu défilant de l'écran Applis : section Streaming (applis vidéo / audio détectées)
 * puis la grille de toutes les applis. Appui long sur une appli : infos / désinstaller.
 */
final class AppsList extends BaseCard {
    private static final int HINT = 999, STREAM = 1000;
    private static final float W = 1168f, COL = W / 8f;

    private final ArrayList<AppInfo> grid = new ArrayList<>();
    private final ArrayList<AppInfo> stream = new ArrayList<>();
    private String query = "";
    private boolean showStream = true, noResults;
    private float streamTitleY, gridTitleY, noResY, contentH = 200;
    private String gridTitle = "Toutes les applis";
    private final RectF r = new RectF();

    AppsList(Context c) {
        super(c);
        setContentDescription("Liste des applis");
    }

    void setQuery(String q) {
        query = q == null ? "" : q;
        rebuild();
    }

    void rebuild() {
        hits.clear();
        grid.clear();
        stream.clear();
        String nq = Text.norm(query);
        List<AppInfo> all = AppRepo.apps;
        for (AppInfo a : all) {
            if (nq.isEmpty() || a.norm.contains(nq)) grid.add(a);
            if (nq.isEmpty() && a.kind != AppInfo.NORMAL) stream.add(a);
        }
        float y = 0;
        showStream = nq.isEmpty();
        if (showStream) {
            streamTitleY = 20;
            y = 24 + 14;
            float cw = (W - 3 * 16) / 4f;
            if (stream.isEmpty()) {
                set(HINT, 0, y, W, y + 76, 22);
                y += 76;
            } else {
                for (int i = 0; i < stream.size(); i++) {
                    int col = i % 4, row = i / 4;
                    float x = col * (cw + 16), yy = y + row * (76 + 16);
                    set(STREAM + i, x, yy, x + cw, yy + 76, 22);
                }
                int rows = (stream.size() + 3) / 4;
                y += rows * 76 + (rows - 1) * 16;
            }
            y += 32;
        }
        gridTitle = showStream ? "Toutes les applis" : "Résultats";
        gridTitleY = y + 20;
        y += 24 + 16;
        for (int i = 0; i < grid.size(); i++) {
            int col = i % 8, row = i / 8;
            float x = col * COL, yy = y + row * (125 + 24);
            set(i, x, yy, x + COL, yy + 125, 20);
        }
        int rows = (grid.size() + 7) / 8;
        if (rows > 0) y += rows * 125 + (rows - 1) * 24;
        noResults = grid.isEmpty() && !nq.isEmpty();
        if (noResults) {
            noResY = y + 24;
            y += 40;
        }
        contentH = y + 24;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int wms, int hms) {
        setMeasuredDimension(MeasureSpec.getSize(wms), Math.round(contentH * U.s));
    }

    @Override
    protected void draw(Canvas c, float w, float h) {
        if (showStream) {
            Draw.text(c, "Streaming", 0, streamTitleY, 20, Style.SEMIBOLD, Style.INK, 0, Paint.Align.LEFT);
            if (stream.isEmpty()) {
                RectF hr = hit(HINT).r;
                Glass.chip(c, this, hr, 22, 0.10f, Style.FILL);
                Draw.text(c, "Aucune appli de streaming installée sur l'autoradio", hr.left + 22, hr.top + 33, 17, Style.SEMIBOLD, Style.INK, 0, Paint.Align.LEFT);
                Draw.text(c, "Touchez pour ouvrir le Play Store (Netflix, YouTube, Spotify, Deezer…)", hr.left + 22, hr.top + 56, 14, Style.MEDIUM, Style.INK2, 0, Paint.Align.LEFT);
            }
            for (int i = 0; i < stream.size(); i++) {
                RectF cr = hit(STREAM + i).r;
                AppInfo a = stream.get(i);
                Glass.chip(c, this, cr, 22, 0.10f, Style.FILL);
                r.set(cr.left + 14, cr.top + 14, cr.left + 62, cr.top + 62);
                icon(c, a, r, 12);
                float tw = cr.width() - 76 - 14;
                Draw.text(c, Draw.fit(a.label, tw, 17, Style.SEMIBOLD, 0), cr.left + 76, cr.top + 35, 17, Style.SEMIBOLD, Style.INK, 0, Paint.Align.LEFT);
                String sub = a.kind == AppInfo.VIDEO ? "Vidéo · à l'arrêt" : "Audio";
                Draw.text(c, sub, cr.left + 76, cr.top + 57, 14, Style.MEDIUM, Style.INK2, 0, Paint.Align.LEFT);
            }
        }
        Draw.text(c, gridTitle, 0, gridTitleY, 20, Style.SEMIBOLD, Style.INK, 0, Paint.Align.LEFT);
        for (int i = 0; i < grid.size(); i++) {
            RectF cell = hit(i).r;
            AppInfo a = grid.get(i);
            r.set(cell.centerX() - 48, cell.top, cell.centerX() + 48, cell.top + 96);
            icon(c, a, r, 24);
            Draw.text(c, Draw.fit(a.label, COL - 8, 15, Style.MEDIUM, 0), cell.centerX(), cell.top + 121, 15, Style.MEDIUM, Style.INK, 0, Paint.Align.CENTER);
        }
        if (noResults) {
            Draw.text(c, "Aucune appli ne correspond à « " + query + " »", 0, noResY, 18, Style.MEDIUM, Style.INK2, 0, Paint.Align.LEFT);
        }
    }

    private void icon(Canvas c, AppInfo a, RectF dst, float rad) {
        if (a.icon != null && !a.icon.isRecycled()) Draw.bitmap(c, a.icon, dst);
        else Glass.chip(c, this, dst, rad, 0.12f, Style.FILL);
    }

    private AppInfo appFor(int id) {
        if (id >= STREAM) {
            int k = id - STREAM;
            return k < stream.size() ? stream.get(k) : null;
        }
        return id >= 0 && id < grid.size() ? grid.get(id) : null;
    }

    @Override
    protected void onTap(int id) {
        if (id == HINT) {
            host().openPlayStore();
            return;
        }
        AppInfo a = appFor(id);
        if (a != null) host().launchApp(a);
    }

    @Override
    protected void onLongTap(int id) {
        if (id == HINT) return;
        AppInfo a = appFor(id);
        if (a != null) host().showAppMenu(a);
    }
}
