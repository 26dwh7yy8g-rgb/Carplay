package app.verre.launcher;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** Écran d'accueil « Verre » (design D). */
public final class MainActivity extends Activity implements MediaHelper.Listener {
    private static final int REQ_LOCATION = 7;
    private static final int REQ_PHOTO = 8;
    static final int THEME = android.R.style.Theme_Material_Dialog_Alert;

    Stage stage;
    WallpaperView wall;
    ClockCard clock;
    MediaCard media;
    CarPlayCard carplay;
    SlidersCard sliders;
    TogglesCard toggles;
    DockCard dock;
    AppsPanel apps;
    MediaHelper mediaHelper;
    private AudioManager am;
    private final Handler h = new Handler(Looper.getMainLooper());
    private int lastMinute = -1;
    private boolean reopenSetup;
    private BroadcastReceiver volumeRx, packageRx;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            tick();
            h.postDelayed(this, 1000);
        }
    };

    private final Runnable autoOpen = () -> openRole(Roles.CARPLAY, false);

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        Style.load(this);

        stage = new Stage(this);
        wall = new WallpaperView(this);
        stage.addFull(wall);
        clock = new ClockCard(this);
        media = new MediaCard(this);
        carplay = new CarPlayCard(this);
        sliders = new SlidersCard(this);
        toggles = new TogglesCard(this);
        dock = new DockCard(this);
        stage.add(media, 24, 24, 608, 536);
        stage.add(clock, 648, 24, 296, 260);
        stage.add(carplay, 960, 24, 296, 260);
        stage.add(sliders, 648, 300, 296, 260);
        stage.add(toggles, 960, 300, 296, 260);
        stage.add(dock, 24, 576, 1232, 120);
        for (View card : new View[]{media, clock, carplay, sliders, toggles, dock}) floatCard(card);
        apps = new AppsPanel(this);
        apps.setVisibility(View.GONE);
        stage.add(apps, 0, 0, 1280, 720);
        setContentView(stage);

        mediaHelper = new MediaHelper(this, this);

        packageRx = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                reloadApps();
            }
        };
        IntentFilter pf = new IntentFilter();
        pf.addAction(Intent.ACTION_PACKAGE_ADDED);
        pf.addAction(Intent.ACTION_PACKAGE_REMOVED);
        pf.addAction(Intent.ACTION_PACKAGE_CHANGED);
        pf.addAction(Intent.ACTION_PACKAGE_REPLACED);
        pf.addDataScheme("package");
        Sys.register(this, packageRx, pf);
        reloadApps();

        if (!Prefs.setupDone(this)) h.postDelayed(this::showSetup, 900);
    }

    private void reloadApps() {
        AppRepo.reload(this, () -> {
            if (apps.getVisibility() == View.VISIBLE) apps.refresh();
            else apps.list.rebuild();
            dock.invalidate();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Style.load(this);
        applyStyle();
        mediaHelper.start();
        refreshAudio();
        volumeRx = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                refreshAudio();
            }
        };
        Sys.register(this, volumeRx, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
        lastMinute = -1;
        h.removeCallbacks(ticker);
        h.post(ticker);
        SpeedGate.start(this);

        if (VerreApp.pendingAutoOpen) {
            VerreApp.pendingAutoOpen = false;
            if (Prefs.autoOpen(this) && Prefs.setupDone(this) && apps.getVisibility() != View.VISIBLE
                    && Roles.intentFor(this, Roles.CARPLAY) != null) {
                h.postDelayed(autoOpen, 1500);
                Sys.toast(this, "Ouverture de CarPlay…");
            }
        }
        if (reopenSetup) {
            reopenSetup = false;
            h.postDelayed(this::showSetup, 400);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(ticker);
        h.removeCallbacks(autoOpen);
        if (volumeRx != null) {
            try {
                unregisterReceiver(volumeRx);
            } catch (Exception ignored) {
            }
            volumeRx = null;
        }
        mediaHelper.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(packageRx);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        h.removeCallbacks(autoOpen);
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        apps.hide();
    }

    @Override
    public void onBackPressed() {
        apps.hide();
    }

    private void tick() {
        Calendar c = Calendar.getInstance();
        int m = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        if (m != lastMinute) {
            lastMinute = m;
            clock.invalidate();
            apps.header.invalidate();
        }
        if (mediaHelper.playing()) media.invalidate();
    }

    void applyStyle() {
        toggles.on[TogglesCard.NIGHT] = Style.night;
        carplay.autoOn = Prefs.autoOpen(this);
        Backdrop.update(this, mediaHelper == null ? null : mediaHelper.art());
        redrawAll();
    }

    /** Le fond suit la pochette en cours : recalculé seulement quand elle change. */
    private void updateBackdrop() {
        if (Backdrop.update(this, mediaHelper.art())) redrawAll();
    }

    private void redrawAll() {
        wall.invalidate();
        clock.invalidate();
        media.invalidate();
        carplay.invalidate();
        sliders.invalidate();
        toggles.invalidate();
        dock.invalidate();
        apps.invalidateAll();
    }

    /** Ombre portée native (GPU) sous chaque widget, comme le verre qui flotte de liquid-glass-js. */
    private void floatCard(View v) {
        v.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline o) {
                o.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 28 * U.s);
            }
        });
        v.setElevation(12 * getResources().getDisplayMetrics().density);
    }

    void setHomeVisible(boolean v) {
        int vis = v ? View.VISIBLE : View.INVISIBLE;
        clock.setVisibility(vis);
        media.setVisibility(vis);
        carplay.setVisibility(vis);
        sliders.setVisibility(vis);
        toggles.setVisibility(vis);
        dock.setVisibility(vis);
    }

    void showApps() {
        apps.show();
    }

    @Override
    public void onMediaChanged() {
        updateBackdrop();
        media.invalidate();
    }

    // ---------------------------------------------------------------- son et luminosité

    void refreshAudio() {
        try {
            int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            sliders.vol = max > 0 ? am.getStreamVolume(AudioManager.STREAM_MUSIC) / (float) max : 0f;
            if (Build.VERSION.SDK_INT >= 23) toggles.on[TogglesCard.MUTE] = am.isStreamMute(AudioManager.STREAM_MUSIC);
        } catch (Exception ignored) {
        }
        try {
            sliders.lum = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) / 255f;
        } catch (Exception e) {
            sliders.lum = 0.7f;
        }
        sliders.invalidate();
        toggles.invalidate();
    }

    void setVolume(float pct) {
        try {
            int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(pct * max), 0);
        } catch (Exception ignored) {
        }
    }

    boolean canWriteBrightness() {
        return Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(this);
    }

    void setBrightness(float pct) {
        if (!canWriteBrightness()) return;
        try {
            ContentResolver cr = getContentResolver();
            Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, Math.max(5, Math.round(pct * 255)));
        } catch (Exception ignored) {
        }
    }

    void askWriteSettings() {
        new AlertDialog.Builder(this, THEME)
                .setTitle("Luminosité")
                .setMessage("Pour régler la luminosité depuis Verre, autorise « Modifier les paramètres système » pour Verre.")
                .setPositiveButton("Autoriser", (d, w) -> openSettings(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()))))
                .setNegativeButton("Plus tard", null)
                .show();
    }

    // ---------------------------------------------------------------- boutons rapides

    void toggle(int i) {
        switch (i) {
            case TogglesCard.NIGHT:
                Prefs.setNight(this, !Style.night);
                Style.load(this);
                applyStyle();
                break;
            case TogglesCard.MUTE:
                try {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, 0);
                } catch (Exception ignored) {
                }
                refreshAudio();
                break;
            case TogglesCard.CAMERA:
                openRole(Roles.CAMERA, true);
                break;
            case TogglesCard.SCREEN:
                start(new Intent(this, ScreenOffActivity.class));
                break;
            default:
                break;
        }
    }

    void toggleAutoOpen() {
        boolean v = !Prefs.autoOpen(this);
        Prefs.setAutoOpen(this, v);
        carplay.autoOn = v;
        carplay.invalidate();
    }

    // ---------------------------------------------------------------- ouverture des applis

    void openRole(String role, boolean pickIfMissing) {
        Intent i = Roles.intentFor(this, role);
        if (i == null) {
            if (pickIfMissing) pickRole(role);
            return;
        }
        launch(i);
    }

    void launchApp(AppInfo a) {
        launch(Roles.launchIntent(a.cn));
    }

    private void launch(Intent i) {
        ComponentName cn = i.getComponent();
        AppRepo.snapshot(this);
        AppInfo a = AppRepo.find(cn != null ? cn.getPackageName() : i.getPackage());
        if (a != null && a.kind == AppInfo.VIDEO) {
            videoGate(i);
            return;
        }
        start(i);
    }

    private void start(Intent i) {
        try {
            startActivity(i);
        } catch (Exception e) {
            Sys.toast(this, "Impossible d'ouvrir cette appli");
        }
    }

    /** Vidéo seulement à l'arrêt. */
    private void videoGate(Intent i) {
        int st = SpeedGate.state();
        if (st == SpeedGate.MOVING) {
            Sys.toast(this, "Vidéo disponible uniquement à l'arrêt");
            return;
        }
        if (st == SpeedGate.STOPPED) {
            start(i);
            return;
        }
        new AlertDialog.Builder(this, THEME)
                .setTitle("Vidéo à l'arrêt uniquement")
                .setMessage("Verre ne reçoit pas la vitesse du GPS. Regarder une vidéo en conduisant est interdit et dangereux.\n\nLa voiture est-elle à l'arrêt ?")
                .setPositiveButton("Je suis à l'arrêt", (d, w) -> start(i))
                .setNegativeButton("Annuler", null)
                .show();
    }

    void openMedia() {
        if (!mediaHelper.hasAccess()) {
            new AlertDialog.Builder(this, THEME)
                    .setTitle("Afficher la musique en cours")
                    .setMessage("Pour voir le titre et la pochette, autorise l'accès aux notifications pour Verre. Verre ne lit pas tes messages : cet accès sert uniquement à la musique.")
                    .setPositiveButton("Autoriser", (d, w) -> openSettings(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")))
                    .setNegativeButton("Plus tard", null)
                    .show();
            return;
        }
        AppInfo a = AppRepo.find(mediaHelper.packageName());
        if (a != null && a.kind == AppInfo.VIDEO) launchApp(a);
        else if (!mediaHelper.openApp()) openRole(Roles.MUSIC, false);
    }

    void openPlayStore() {
        Intent i = getPackageManager().getLaunchIntentForPackage("com.android.vending");
        if (i != null) start(i);
        else Sys.toast(this, "Play Store introuvable sur cet autoradio");
    }

    void openSettings(Intent i) {
        try {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            Sys.toast(this, "Ce réglage n'existe pas sur cet autoradio");
        }
    }

    void openHomeSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            openSettings(new Intent(Settings.ACTION_SETTINGS));
            Sys.toast(this, "Applis > Applis par défaut > Écran d'accueil : choisis Verre");
        }
    }

    // ---------------------------------------------------------------- menus

    void pickRole(String role) {
        final List<AppInfo> list = AppRepo.snapshot(this);
        AppInfo guess = Roles.guess(this, role);
        String auto = "Automatique";
        if (guess != null) auto += " (" + guess.label + ")";
        else if (Roles.SETTINGS.equals(role)) auto += " (Réglages Android)";
        String[] items = new String[list.size() + 1];
        items[0] = auto;
        for (int k = 0; k < list.size(); k++) items[k + 1] = list.get(k).label;
        new AlertDialog.Builder(this, THEME)
                .setTitle("Appli pour « " + Roles.title(role) + " »")
                .setItems(items, (d, which) -> {
                    if (which == 0) Prefs.setRole(this, role, null);
                    else Prefs.setRole(this, role, list.get(which - 1).cn.flattenToString());
                    dock.invalidate();
                    Sys.toast(this, "Enregistré");
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    void showMenu() {
        String[] items = {
                "Fond d'écran : " + BG_NAMES[Prefs.bgMode(this)],
                "Configuration de Verre",
                "Choisir l'appli CarPlay",
                "Réglages Android",
                "Choisir l'écran d'accueil"};
        new AlertDialog.Builder(this, THEME)
                .setTitle("Personnaliser")
                .setItems(items, (d, w) -> {
                    switch (w) {
                        case 0: pickBackground(); break;
                        case 1: showSetup(); break;
                        case 2: pickRole(Roles.CARPLAY); break;
                        case 3: openSettings(new Intent(Settings.ACTION_SETTINGS)); break;
                        default: openHomeSettings(); break;
                    }
                })
                .show();
    }

    private static final String[] BG_NAMES = {"pochette du morceau", "ma photo", "noir"};

    private void pickBackground() {
        String[] items = {"Pochette du morceau en cours", "Ma photo", "Noir"};
        new AlertDialog.Builder(this, THEME)
                .setTitle("Fond d'écran")
                .setSingleChoiceItems(items, Prefs.bgMode(this), (d, w) -> {
                    d.dismiss();
                    if (w == Prefs.BG_PHOTO && !Backdrop.photoFile(this).exists()) {
                        pickPhoto();
                        return;
                    }
                    Prefs.setBgMode(this, w);
                    applyStyle();
                })
                .setNeutralButton("Choisir une photo…", (d, w) -> pickPhoto())
                .show();
    }

    private void pickPhoto() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(i, "Choisir une photo"), REQ_PHOTO);
        } catch (Exception e) {
            Sys.toast(this, "Aucune galerie trouvée sur cet autoradio");
        }
    }

    @Override
    protected void onActivityResult(int code, int result, Intent data) {
        super.onActivityResult(code, result, data);
        if (code != REQ_PHOTO || result != RESULT_OK || data == null || data.getData() == null) return;
        final Uri uri = data.getData();
        final android.content.Context app = getApplicationContext();
        new Thread(() -> {
            boolean ok = Photos.save(app, uri, Backdrop.photoFile(app));
            h.post(() -> {
                if (!ok) {
                    Sys.toast(this, "Impossible de lire cette photo");
                    return;
                }
                Prefs.setBgMode(this, Prefs.BG_PHOTO);
                Backdrop.forget();
                applyStyle();
            });
        }).start();
    }

    void showAppMenu(AppInfo a) {
        ArrayList<String> items = new ArrayList<>();
        items.add("Infos sur l'appli");
        if (!a.system) items.add("Désinstaller");
        new AlertDialog.Builder(this, THEME)
                .setTitle(a.label)
                .setItems(items.toArray(new String[0]), (d, w) -> {
                    if (w == 0) {
                        openSettings(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + a.pkg)));
                    } else {
                        openSettings(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + a.pkg)));
                    }
                })
                .show();
    }

    void showSetup() {
        if (isFinishing()) return;
        boolean home = isDefaultHome(), music = mediaHelper.hasAccess(), lum = canWriteBrightness(), gps = hasLocation();
        String[] items = {
                mark(home) + "Verre comme écran d'accueil",
                mark(music) + "Afficher la musique en cours",
                mark(lum) + "Régler la luminosité depuis Verre",
                mark(gps) + "Bloquer les vidéos quand la voiture roule (GPS)"};
        new AlertDialog.Builder(this, THEME)
                .setTitle("Configurer Verre")
                .setItems(items, (d, w) -> {
                    reopenSetup = true;
                    switch (w) {
                        case 0:
                            openHomeSettings();
                            break;
                        case 1:
                            openSettings(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                            break;
                        case 2:
                            openSettings(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName())));
                            break;
                        default:
                            reopenSetup = false;
                            if (Build.VERSION.SDK_INT >= 23) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOCATION);
                            }
                            break;
                    }
                })
                .setPositiveButton("Terminé", (d, w) -> Prefs.setSetupDone(this, true))
                .show();
    }

    private static String mark(boolean ok) {
        return ok ? "✓  " : "○  ";
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == REQ_LOCATION) {
            SpeedGate.start(this);
            h.postDelayed(this::showSetup, 300);
        }
    }

    private boolean hasLocation() {
        return Build.VERSION.SDK_INT < 23
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isDefaultHome() {
        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo ri = getPackageManager().resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY);
        return ri != null && ri.activityInfo != null && getPackageName().equals(ri.activityInfo.packageName);
    }
}
