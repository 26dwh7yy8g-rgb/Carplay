package app.verre.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import java.util.ArrayList;

/**
 * Base des widgets : dessin en unités maquette (canvas mis à l'échelle) et zones tactiles
 * (appui, appui long, glisser). Un seul View par widget = peu de mémoire, rendu rapide.
 */
abstract class BaseCard extends View {
    static final class Hit {
        final int id;
        final RectF r = new RectF();
        float radius;
        boolean drag;
        boolean overlay = true;

        Hit(int id) {
            this.id = id;
        }
    }

    protected final ArrayList<Hit> hits = new ArrayList<>();
    private Hit pressed;
    private boolean longFired;
    private final RectF slop = new RectF();

    private final Runnable longPress = new Runnable() {
        @Override
        public void run() {
            Hit h = pressed;
            if (h == null || h.drag) return;
            longFired = true;
            pressed = null;
            invalidate();
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            onLongTap(h.id);
        }
    };

    BaseCard(Context c) {
        super(c);
    }

    protected MainActivity host() {
        return (MainActivity) getContext();
    }

    /** Panneau de verre (ou widget plein quand il n'y a pas de fond coloré). */
    protected void glass(Canvas c, RectF r, float rad) {
        Glass.panel(c, this, r, rad, 0.09f);
    }

    protected Hit hit(int id) {
        for (int i = 0; i < hits.size(); i++) {
            Hit h = hits.get(i);
            if (h.id == id) return h;
        }
        Hit h = new Hit(id);
        hits.add(h);
        return h;
    }

    protected Hit set(int id, float l, float t, float r, float b, float radius) {
        Hit h = hit(id);
        h.r.set(l, t, r, b);
        h.radius = radius;
        return h;
    }

    @Override
    protected void onDraw(Canvas c) {
        float s = U.s;
        c.save();
        c.scale(s, s);
        draw(c, getWidth() / s, getHeight() / s);
        Hit p = pressed;
        if (p != null && !p.drag && p.overlay) {
            Draw.fill(c, p.r, p.radius, U.w(0.12f));
        }
        c.restore();
    }

    protected abstract void draw(Canvas c, float w, float h);

    protected void onTap(int id) {}

    protected void onLongTap(int id) {}

    protected void onDrag(int id, float x, float y, boolean first) {}

    protected void onDragEnd(int id) {}

    private Hit find(float x, float y) {
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (h.r.contains(x, y)) return h;
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float s = U.s;
        float x = e.getX() / s, y = e.getY() / s;
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                pressed = find(x, y);
                if (pressed == null) return false;
                longFired = false;
                if (pressed.drag) {
                    ViewParent vp = getParent();
                    if (vp != null) vp.requestDisallowInterceptTouchEvent(true);
                    onDrag(pressed.id, x, y, true);
                } else {
                    postDelayed(longPress, ViewConfiguration.getLongPressTimeout());
                }
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                Hit p = pressed;
                if (p == null) return false;
                if (p.drag) {
                    onDrag(p.id, x, y, false);
                } else {
                    float m = 12f;
                    slop.set(p.r.left - m, p.r.top - m, p.r.right + m, p.r.bottom + m);
                    if (!slop.contains(x, y)) {
                        removeCallbacks(longPress);
                        pressed = null;
                        invalidate();
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                removeCallbacks(longPress);
                Hit p = pressed;
                pressed = null;
                invalidate();
                if (p != null && !longFired) {
                    if (p.drag) {
                        onDragEnd(p.id);
                    } else {
                        playSoundEffect(SoundEffectConstants.CLICK);
                        onTap(p.id);
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_CANCEL: {
                removeCallbacks(longPress);
                Hit p = pressed;
                pressed = null;
                if (p != null && p.drag) onDragEnd(p.id);
                invalidate();
                return true;
            }
            default:
                return pressed != null;
        }
    }
}
