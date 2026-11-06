package app.verre.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;

/** Écran « Applis » en verre dépoli, par-dessus le fond. */
final class AppsPanel extends Abs {
    final HeaderView header;
    final EditText search;
    final ScrollView scroll;
    final AppsList list;
    private final MainActivity host;
    private final android.graphics.RectF panel = new android.graphics.RectF();

    AppsPanel(MainActivity h) {
        super(h);
        host = h;
        setWillNotDraw(false);
        setClickable(true);

        header = new HeaderView(h);
        add(header, 56, 52, 1168, 56);

        search = new EditText(h);
        search.setBackground(null);
        search.setTextColor(0xFFFFFFFF);
        search.setHintTextColor(Style.INK2);
        search.setHint("Rechercher");
        search.setSingleLine(true);
        search.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        search.setPadding(0, 0, 0, 0);
        search.setGravity(Gravity.CENTER_VERTICAL);
        search.setTypeface(Style.MEDIUM);
        add(search, 56 + HeaderView.PILL_L + 48, 54, HeaderView.PILL_R - HeaderView.PILL_L - 64, 52);

        scroll = new ScrollView(h);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        list = new AppsList(h);
        scroll.addView(list, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        add(scroll, 56, 128, 1168, 548);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                list.setQuery(s.toString());
                scroll.scrollTo(0, 0);
            }
        });
        search.setOnEditorActionListener((v, actionId, ev) -> {
            hideKeyboard();
            return true;
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        search.setTextSize(TypedValue.COMPLEX_UNIT_PX, 17 * U.s);
    }

    @Override
    protected void onDraw(Canvas c) {
        c.save();
        c.scale(U.s, U.s);
        panel.set(24, 24, 1256, 696);
        Glass.panel(c, this, panel, 34, 0.09f);
        c.restore();
    }

    void refresh() {
        list.rebuild();
    }

    void invalidateAll() {
        invalidate();
        header.invalidate();
        list.invalidate();
    }

    void show() {
        if (search.length() > 0) search.setText("");
        list.rebuild();
        scroll.scrollTo(0, 0);
        animate().cancel();
        setAlpha(0f);
        setVisibility(VISIBLE);
        animate().alpha(1f).setDuration(160).setListener(null).start();
        host.setHomeVisible(false);
    }

    void hide() {
        if (getVisibility() != VISIBLE) return;
        hideKeyboard();
        host.setHomeVisible(true);
        animate().cancel();
        animate().alpha(0f).setDuration(140).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
            }
        }).start();
    }

    void focusSearch() {
        search.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
    }

    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
        search.clearFocus();
    }
}
