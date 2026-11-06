package app.verre.launcher;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/** Mise en page absolue en unités maquette : chaque enfant a x, y, largeur, hauteur. */
class Abs extends ViewGroup {
    static final class LP extends ViewGroup.LayoutParams {
        final float x, y, w, h;

        LP(float x, float y, float w, float h) {
            super(0, 0);
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    Abs(Context c) {
        super(c);
    }

    void add(View v, float x, float y, float w, float h) {
        addView(v, new LP(x, y, w, h));
    }

    void addFull(View v) {
        addView(v, new LP(0, 0, -1, -1));
    }

    protected void onScale(int w, int h) {}

    protected float ox() {
        return 0;
    }

    protected float oy() {
        return 0;
    }

    @Override
    protected void onMeasure(int wms, int hms) {
        int W = MeasureSpec.getSize(wms), H = MeasureSpec.getSize(hms);
        onScale(W, H);
        for (int i = 0; i < getChildCount(); i++) {
            View ch = getChildAt(i);
            if (ch.getVisibility() == GONE) continue;
            LP lp = (LP) ch.getLayoutParams();
            int cw = lp.w < 0 ? W : Math.round(lp.w * U.s);
            int chh = lp.h < 0 ? H : Math.round(lp.h * U.s);
            ch.measure(MeasureSpec.makeMeasureSpec(cw, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(chh, MeasureSpec.EXACTLY));
        }
        setMeasuredDimension(W, H);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View ch = getChildAt(i);
            if (ch.getVisibility() == GONE) continue;
            LP lp = (LP) ch.getLayoutParams();
            if (lp.w < 0) {
                ch.layout(0, 0, ch.getMeasuredWidth(), ch.getMeasuredHeight());
            } else {
                int x = Math.round(ox() + lp.x * U.s), y = Math.round(oy() + lp.y * U.s);
                ch.layout(x, y, x + ch.getMeasuredWidth(), y + ch.getMeasuredHeight());
            }
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LP;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LP(0, 0, -1, -1);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LP(0, 0, -1, -1);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }
}
