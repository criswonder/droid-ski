package com.taobao.android.base;

import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.taobao.android.ski.BuildConfig;

/** @author Oasis */
public class Versions {

	public static boolean isDebug() {
		return DEBUG;
	}

	public static int code(final Context context) {
        if (sVersionCode == 0) loadVersionInfo(context);
        return sVersionCode;
    }

    public static String name(final Context context) {
        if (sVersionName == null) loadVersionInfo(context);
        return sVersionName;
    }

    @SuppressLint("DefaultLocale")
    public static boolean isVersionOf(final Context context, final String tag) {
        return name(context).toLowerCase().contains(tag);
    }

    private static void loadVersionInfo(final Context context) {
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            sVersionCode = info.versionCode;
            sVersionName = info.versionName;
        } catch (final NameNotFoundException e) { /* Should never happen */ }
    }

    /** Call this method in Application.onCreate() */
    public static void init(Application application) {
        if (! DEBUG) return;
        // To workaround the unreliable "BuildConfig.DEBUG".
        //   See http://code.google.com/p/android/issues/detail?id=27940
        try {		// This will never be executed on RELEASE build, thus no performance issue.
            final ApplicationInfo app_info = application.getApplicationInfo();
            DEBUG = (app_info.flags & FLAG_DEBUGGABLE) != 0;
        } catch (final Exception e) {}      // Including NPE
    }

    private static boolean DEBUG = BuildConfig.DEBUG;
    private static int sVersionCode;
    private static String sVersionName;
}
