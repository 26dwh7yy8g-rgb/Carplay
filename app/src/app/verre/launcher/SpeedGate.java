package app.verre.launcher;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;

/**
 * Vitesse GPS : les applis vidéo ne s'ouvrent qu'à l'arrêt (code de la route : pas d'écran
 * vidéo dans le champ de vision du conducteur en roulant).
 */
final class SpeedGate implements LocationListener {
    static final int UNKNOWN = 0, STOPPED = 1, MOVING = 2;

    private static SpeedGate instance;
    private static volatile float speed = -1f;
    private static volatile long fixAt;

    static void start(Context ctx) {
        if (instance != null) return;
        Context c = ctx.getApplicationContext();
        if (Build.VERSION.SDK_INT >= 23
                && c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return;
        try {
            SpeedGate g = new SpeedGate();
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, g, Looper.getMainLooper());
            instance = g;
        } catch (Exception ignored) {
        }
    }

    static int state() {
        long at = fixAt;
        if (at == 0 || SystemClock.elapsedRealtime() - at > 6000) return UNKNOWN;
        return speed > 2.0f ? MOVING : STOPPED; // 2 m/s ≈ 7 km/h
    }

    @Override
    public void onLocationChanged(Location l) {
        if (l != null && l.hasSpeed()) {
            speed = l.getSpeed();
            fixAt = SystemClock.elapsedRealtime();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
