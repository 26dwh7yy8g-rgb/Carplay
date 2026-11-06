package app.verre.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Liste des applis installées. Chargée en arrière-plan pour ne jamais bloquer l'écran. */
final class AppRepo {
    static volatile List<AppInfo> apps = new ArrayList<>();
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private AppRepo() {}

    static void reload(Context ctx, final Runnable done) {
        final Context c = ctx.getApplicationContext();
        EXEC.execute(() -> {
            List<AppInfo> l = query(c);
            HashMap<String, android.graphics.Bitmap> old = new HashMap<>();
            for (AppInfo a : apps) if (a.icon != null) old.put(a.key, a.icon);
            for (AppInfo a : l) a.icon = old.get(a.key);
            apps = l;
            MAIN.post(done);
            int size = Math.max(48, Math.round(96 * U.s));
            PackageManager pm = c.getPackageManager();
            int n = 0;
            for (AppInfo a : l) {
                if (a.icon != null && a.icon.getWidth() == size) continue;
                try {
                    a.icon = Icons.render(a.ri.loadIcon(pm), size);
                } catch (Throwable ignored) {
                }
                if (++n % 12 == 0) MAIN.post(done);
            }
            MAIN.post(done);
        });
    }

    static List<AppInfo> snapshot(Context c) {
        List<AppInfo> l = apps;
        if (l.isEmpty()) {
            l = query(c.getApplicationContext());
            apps = l;
        }
        return l;
    }

    static AppInfo find(String pkg) {
        if (pkg == null) return null;
        for (AppInfo a : apps) if (pkg.equals(a.pkg)) return a;
        return null;
    }

    static AppInfo find(Context c, ComponentName cn) {
        for (AppInfo a : snapshot(c)) if (a.cn.equals(cn)) return a;
        return null;
    }

    private static List<AppInfo> query(Context c) {
        PackageManager pm = c.getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ris;
        try {
            ris = pm.queryIntentActivities(i, 0);
        } catch (Exception e) {
            ris = new ArrayList<>();
        }
        ArrayList<AppInfo> out = new ArrayList<>();
        for (ResolveInfo ri : ris) {
            if (ri.activityInfo == null) continue;
            if (c.getPackageName().equals(ri.activityInfo.packageName)) continue;
            CharSequence lab = null;
            try {
                lab = ri.loadLabel(pm);
            } catch (Exception ignored) {
            }
            out.add(new AppInfo(ri, lab == null ? ri.activityInfo.packageName : lab.toString().trim()));
        }
        final Collator col = Collator.getInstance(Locale.FRENCH);
        col.setStrength(Collator.PRIMARY);
        Collections.sort(out, (a, b) -> col.compare(a.label, b.label));
        return out;
    }
}
