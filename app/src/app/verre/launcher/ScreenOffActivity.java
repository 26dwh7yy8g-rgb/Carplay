package app.verre.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

/** Écran noir, rétroéclairage au minimum. Touchez n'importe où pour revenir. */
public final class ScreenOffActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        View v = new View(this);
        v.setBackgroundColor(0xFF000000);
        v.setOnClickListener(x -> finish());
        v.setContentDescription("Écran éteint. Touchez pour rallumer.");
        setContentView(v);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
        getWindow().setAttributes(lp);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
