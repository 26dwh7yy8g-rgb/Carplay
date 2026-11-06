package app.verre.launcher;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public final class VerreApp extends Application {
    /** Vrai après un démarrage ou un réveil de l'écran (contact mis) : on peut ouvrir CarPlay. */
    static volatile boolean pendingAutoOpen = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Sys.register(this, new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                pendingAutoOpen = true;
            }
        }, new IntentFilter(Intent.ACTION_SCREEN_ON));
        SpeedGate.start(this);
    }
}
