package app.verre.launcher;

/** Reconnaît les applis de streaming vidéo (bloquées en roulant) et audio. */
final class Streaming {
    private static final String[] AUDIO_PKG = {
            "com.spotify.music", "deezer.android.app", "com.google.android.apps.youtube.music",
            "com.apple.android.music", "com.soundcloud.android", "com.aspiro.tidal", "com.qobuz.music",
            "com.amazon.mp3", "com.audible.application", "tunein.player"};
    private static final String[] VIDEO_PKG = {
            "com.netflix.mediaclient", "com.google.android.youtube", "com.disney.disneyplus",
            "com.amazon.avod.thirdpartyclient", "com.canal.android.canal", "fr.francetv.pluzz",
            "fr.m6.m6replay", "fr.tf1.mytf1", "tv.twitch.android.app", "com.plexapp.android",
            "org.videolan.vlc", "com.crunchyroll.crunchyroid", "tv.molotov.app"};
    private static final String[] AUDIO_KW = {
            "spotify", "deezer", "youtube music", "apple music", "soundcloud", "tidal", "qobuz",
            "amazon music", "audible", "tunein", "podcast", "podcasts"};
    private static final String[] VIDEO_KW = {
            "netflix", "youtube", "disney", "prime video", "canal", "mycanal", "france tv", "twitch",
            "plex", "vlc", "molotov", "crunchyroll", "m6", "tf1", "apple tv", "paramount", "arte",
            "replay", "video", "videos", "film", "films", "tv"};

    private Streaming() {}

    static int classify(String pkg, String norm) {
        for (String p : AUDIO_PKG) if (p.equals(pkg)) return AppInfo.AUDIO;
        for (String p : VIDEO_PKG) if (p.equals(pkg)) return AppInfo.VIDEO;
        String padded = " " + norm + " ";
        for (String k : AUDIO_KW) if (padded.contains(" " + k + " ")) return AppInfo.AUDIO;
        for (String k : VIDEO_KW) if (padded.contains(" " + k + " ")) return AppInfo.VIDEO;
        return AppInfo.NORMAL;
    }
}
