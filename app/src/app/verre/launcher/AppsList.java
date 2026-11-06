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
    private static final float W = 1160f;

    private final ArrayList<AppInfo> grid = new ArrayList<>();
    private final ArrayList<AppInfo> stream = new ArrayList<>();
    private String query = "";
    private boolean showStream = true, noResults;
    private float streamTitleY, gridTitleY, noResY, contentH = 200;
    private String gridTitle = "TOUTES LES APPLIS";
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
            streamTitleY = 14;
            y = 18 + 12;
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
            y += 28;
        }
        gridTitle = showStream ? "TOUTES LES APPLIS" : "RÉSULTATS";
        gridTitleY = y + 14;
        y += 18 + 14;
        for (int i = 0; i < grid.size(); i++) {
            int col = i % 8, row = i / 8;
            float x = col * 145f, yy = y + row * (125 + 24);
            set(i, x, yy, x + 145, yy + 125, 20);
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
            Draw.text(c, "STREAMING", 0, streamTitleY, 14, Style.MEDIUM, U.w(0.75f), 0.06f, Paint.Align.LEFT);
            if (stream.isEmpty()) {
                RectF hr = hit(HINT).r;
                Draw.fill(c, hr, 22, U.w(0.10f));
                Draw.stroke(c, hr, 22, U.w(0.14f));
                Draw.text(c, "Aucune appli de streaming installée sur l'autoradio", hr.left + 22, hr.top + 33, 17, Style.MEDIUM, 0xFFFFFFFF, 0, Paint.Align.LEFT);
                Draw.text(c, "Touchez pour ouvrir le Play Store (Netflix, YouTube, Spotify, Deezer…)", hr.left + 22, hr.top + 56, 13, Style.REGULAR, U.w(0.75f), 0, Paint.Align.LEFT);
            }
            for (int i = 0; i < stream.size(); i++) {
                RectF cr = hit(STREAM + i).r;
                AppInfo a = stream.get(i);
                Draw.fill(c, cr, 22, U.w(0.10f));
                Draw.stroke(c, cr, 22, U.w(0.14f));
                r.set(cr.left + 16, cr.top + 14, cr.left + 64, cr.top + 62);
                icon(c, a, r, 14);
                float tw = cr.width() - 78 - 14;
                Draw.text(c, Draw.fit(a.label, tw, 17, Style.MEDIUM, 0), cr.left + 78, cr.top + 35, 17, Style.MEDIUM, 0xFFFFFFFF, 0, Paint.Align.LEFT);
                String sub = a.kind == AppInfo.VIDEO ? "Vidéo · à l'arrêt" : "Audio";
                Draw.text(c, sub, cr.left + 78, cr.top + 56, 13, Style.REGULAR, U.w(0.75f), 0, Paint.Align.LEFT);
            }
        }
        Draw.text(c, gridTitle, 0, gridTitleY, 14, Style.MEDIUM, U.w(0.75f), 0.06f, Paint.Align.LEFT);
        for (int i = 0; i < grid.size(); i++) {
            RectF cell = hit(i).r;
            AppInfo a = grid.get(i);
            r.set(cell.left + 24.5f, cell.top, cell.left + 120.5f, cell.top + 96);
            icon(c, a, r, 26);
            Draw.text(c, Draw.fit(a.label, 138, 15, Style.MEDIUM, 0), cell.centerX(), cell.top + 121, 15, Style.MEDIUM, 0xFFFFFFFF, 0, Paint.Align.CENTER);
        }
        if (noResults) {
            Draw.text(c, "Aucune appli ne correspond à « " + query + " »", 0, noResY, 18, Style.REGULAR, U.w(0.8f), 0, Paint.Align.LEFT);
        }
    }

    private void icon(Canvas c, AppInfo a, RectF dst, float rad) {
        if (a.icon != null && !a.icon.isRecycled()) Draw.bitmap(c, a.icon, dst);
        else Draw.fill(c, dst, rad, U.w(0.12f));
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
