package app.verre.launcher;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;

final class AppInfo {
    static final int NORMAL = 0, VIDEO = 1, AUDIO = 2;

    final ResolveInfo ri;
    final ComponentName cn;
    final String pkg, label, norm, key;
    final int kind;
    final boolean system;
    volatile Bitmap icon;

    AppInfo(ResolveInfo ri, String label) {
        this.ri = ri;
        this.pkg = ri.activityInfo.packageName;
        this.cn = new ComponentName(pkg, ri.activityInfo.name);
        this.label = label;
        this.norm = Text.norm(label);
        this.key = cn.flattenToString();
        this.kind = Streaming.classify(pkg, norm);
        ApplicationInfo ai = ri.activityInfo.applicationInfo;
        this.system = ai != null && (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
