package com.taobao.android.base;

import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;

import javax.annotation.NonNullByDefault;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import com.taobao.android.ski.BuildConfig;

/** @author Oasis */
@NonNullByDefault
public class Versions {

	public static boolean isDebug() {
		return DEBUG;
	}

    /** Call this method in Application.onCreate() */
    public static void init(final Application application) {
    	
        if (! DEBUG) return;
        // To workaround the unreliable "BuildConfig.DEBUG".
        //   See http://code.google.com/p/android/issues/detail?id=27940
        try {		// This will never be executed on RELEASE build, thus no performance issue.
            final ApplicationInfo app_info = application.getApplicationInfo();
            DEBUG = (app_info.flags & FLAG_DEBUGGABLE) != 0;
        } catch (final Exception e) {}      // Including NPE
    }

    private static boolean DEBUG = BuildConfig.DEBUG;

}
