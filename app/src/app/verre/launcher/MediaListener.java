package app.verre.launcher;

import android.service.notification.NotificationListenerService;

/**
 * Service vide : il sert uniquement à obtenir l'autorisation Android de lire la musique en cours
 * (titre, pochette). Verre ne lit ni ne garde aucune notification.
 */
public final class MediaListener extends NotificationListenerService {
}
