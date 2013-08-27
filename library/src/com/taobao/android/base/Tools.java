package com.taobao.android.base;

import javax.annotation.NonNullByDefault;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Looper;

/** @author baiyi */
@NonNullByDefault
public class Tools {

	public static boolean isMainThread() {
		return Thread.currentThread() == Looper.getMainLooper().getThread();
	}

	public static String currentProcessName(Context context) {
		int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
	}
}
