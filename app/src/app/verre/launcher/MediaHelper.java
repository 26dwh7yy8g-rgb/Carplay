package app.verre.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.KeyEvent;

import java.util.HashMap;
import java.util.List;

/**
 * Lit la musique en cours (titre, pochette, position) via les sessions média Android.
 * Sans l'accès aux notifications, les boutons marchent quand même (touches média).
 */
final class MediaHelper implements MediaSessionManager.OnActiveSessionsChangedListener {
    interface Listener {
        void onMediaChanged();
    }

    private final Context ctx;
    private final Listener listener;
    private final MediaSessionManager msm;
    private final ComponentName cn;
    private final Handler h = new Handler(Looper.getMainLooper());
    private final HashMap<String, String> labels = new HashMap<>();
    private MediaController ctrl;
    private boolean started;

    private final MediaController.Callback cb = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            listener.onMediaChanged();
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            listener.onMediaChanged();
        }

        @Override
        public void onSessionDestroyed() {
            setCtrl(null);
            refresh();
        }
    };

    MediaHelper(Context c, Listener l) {
        ctx = c;
        listener = l;
        msm = (MediaSessionManager) c.getSystemService(Context.MEDIA_SESSION_SERVICE);
        cn = new ComponentName(c, MediaListener.class);
    }

    boolean hasAccess() {
        try {
            String flat = Settings.Secure.getString(ctx.getContentResolver(), "enabled_notification_listeners");
            return flat != null && flat.contains(ctx.getPackageName() + "/");
        } catch (Exception e) {
            return false;
        }
    }

    void start() {
        if (started) return;
        started = true;
        if (msm == null || !hasAccess()) {
            listener.onMediaChanged();
            return;
        }
        try {
            msm.addOnActiveSessionsChangedListener(this, cn, h);
        } catch (Exception ignored) {
        }
        refresh();
    }

    void stop() {
        if (!started) return;
        started = false;
        try {
            if (msm != null) msm.removeOnActiveSessionsChangedListener(this);
        } catch (Exception ignored) {
        }
        setCtrl(null);
    }

    private void refresh() {
        if (msm == null || !started) return;
        try {
            onActiveSessionsChanged(msm.getActiveSessions(cn));
        } catch (Exception e) {
            setCtrl(null);
            listener.onMediaChanged();
        }
    }

    @Override
    public void onActiveSessionsChanged(List<MediaController> list) {
        MediaController pick = null;
        if (list != null) {
            for (MediaController m : list) {
                PlaybackState s = m.getPlaybackState();
                if (s != null && s.getState() == PlaybackState.STATE_PLAYING) {
                    pick = m;
                    break;
                }
            }
            if (pick == null && !list.isEmpty()) pick = list.get(0);
        }
        setCtrl(pick);
        listener.onMediaChanged();
    }

    private void setCtrl(MediaController m) {
        if (ctrl != null && m != null && ctrl.getSessionToken().equals(m.getSessionToken())) return;
        if (ctrl != null) {
            try {
                ctrl.unregisterCallback(cb);
            } catch (Exception ignored) {
            }
        }
        ctrl = m;
        if (m != null) {
            try {
                m.registerCallback(cb, h);
            } catch (Exception ignored) {
            }
        }
    }

    private MediaMetadata meta() {
        try {
            return ctrl == null ? null : ctrl.getMetadata();
        } catch (Exception e) {
            return null;
        }
    }

    private PlaybackState state() {
        try {
            return ctrl == null ? null : ctrl.getPlaybackState();
        } catch (Exception e) {
            return null;
        }
    }

    String packageName() {
        return ctrl == null ? null : ctrl.getPackageName();
    }

    String title() {
        MediaMetadata m = meta();
        if (m == null) return null;
        CharSequence t = m.getText(MediaMetadata.METADATA_KEY_TITLE);
        if (t == null) t = m.getText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
        return t == null || t.length() == 0 ? null : t.toString();
    }

    String artist() {
        MediaMetadata m = meta();
        if (m == null) return null;
        CharSequence t = m.getText(MediaMetadata.METADATA_KEY_ARTIST);
        if (t == null) t = m.getText(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
        if (t == null) t = m.getText(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE);
        return t == null || t.length() == 0 ? null : t.toString();
    }

    Bitmap art() {
        MediaMetadata m = meta();
        if (m == null) return null;
        Bitmap b = m.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
        if (b == null) b = m.getBitmap(MediaMetadata.METADATA_KEY_ART);
        if (b == null) b = m.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
        return b;
    }

    long duration() {
        MediaMetadata m = meta();
        return m == null ? -1 : m.getLong(MediaMetadata.METADATA_KEY_DURATION);
    }

    long position() {
        PlaybackState s = state();
        if (s == null) return -1;
        long p = s.getPosition();
        if (p < 0) return -1;
        if (s.getState() == PlaybackState.STATE_PLAYING) {
            long dt = SystemClock.elapsedRealtime() - s.getLastPositionUpdateTime();
            if (dt > 0) p += (long) (dt * s.getPlaybackSpeed());
        }
        long d = duration();
        if (d > 0 && p > d) p = d;
        return p;
    }

    boolean playing() {
        PlaybackState s = state();
        return s != null && s.getState() == PlaybackState.STATE_PLAYING;
    }

    String source() {
        String pkg = packageName();
        if (pkg == null) return null;
        String l = labels.get(pkg);
        if (l == null) {
            try {
                PackageManager pm = ctx.getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                l = pm.getApplicationLabel(ai).toString();
            } catch (Exception e) {
                l = pkg;
            }
            labels.put(pkg, l);
        }
        return l;
    }

    void playPause() {
        if (ctrl != null) {
            try {
                if (playing()) ctrl.getTransportControls().pause();
                else ctrl.getTransportControls().play();
                return;
            } catch (Exception ignored) {
            }
        }
        key(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
    }

    void next() {
        if (ctrl != null) {
            try {
                ctrl.getTransportControls().skipToNext();
                return;
            } catch (Exception ignored) {
            }
        }
        key(KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    void prev() {
        if (ctrl != null) {
            try {
                ctrl.getTransportControls().skipToPrevious();
                return;
            } catch (Exception ignored) {
            }
        }
        key(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }

    private void key(int code) {
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;
        long t = SystemClock.uptimeMillis();
        try {
            am.dispatchMediaKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_DOWN, code, 0));
            am.dispatchMediaKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_UP, code, 0));
        } catch (Exception ignored) {
        }
    }

    /** Ouvre l'appli qui joue (hors vidéo : MainActivity filtre avant). */
    boolean openApp() {
        String pkg = packageName();
        if (pkg == null) return false;
        Intent i = ctx.getPackageManager().getLaunchIntentForPackage(pkg);
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                ctx.startActivity(i);
                return true;
            } catch (Exception ignored) {
            }
        }
        try {
            if (ctrl.getSessionActivity() != null) {
                ctrl.getSessionActivity().send();
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
