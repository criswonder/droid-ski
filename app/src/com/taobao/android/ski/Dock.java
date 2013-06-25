package com.taobao.android.ski;

import static android.content.pm.PackageManager.GET_INSTRUMENTATION;

import java.lang.reflect.Field;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;

import com.taobao.android.ski.hud.Toto;
import com.taobao.android.ski.radar.ActivityPerfMon;
import com.taobao.android.ski.radar.AnimationPerfMon;
import com.taobao.android.ski.radar.SysLogMon;

/** @author Oasis */
public class Dock extends Instrumentation {

	public static final String KEY_FLAGS = "flags";
	public static final String KEY_ATTACH_DEBUGGER = "attach-debugger";

	public static final int FLAG_LAUNCH_TIMING = 1 << 0;
	public static final int FLAG_LAUNCH_PROFILING = 1 << 1;
	public static final int FLAG_MONITOR_ANIMATION_PERF = 1 << 2;
	public static final int FLAG_MONITOR_ACTIVITY_PERF = 1 << 3;
	
	public static final String KEY_ACTIVITY_LAUNCH_TIME = "am_launch_time";
	public static final String KEY_THREASHOLD = "choreographer";


	public static Dock getInstrumentationOf(Activity activity) {
		try {
			Field f = Activity.class.getDeclaredField("mInstrumentation");
			f.setAccessible(true);
			Object instrumentation = f.get(activity);
			if (instrumentation instanceof Dock) return (Dock) instrumentation;
			else Log.w(TAG, "Incompatible instrumentation: " + instrumentation);
		} catch (Exception e) {
			Log.e(TAG, "Cannot get instrumentation of activity " + activity);
		}
		return null;
	}

	@Override public void onCreate(final Bundle arguments) {
		if (arguments.getBoolean(KEY_ATTACH_DEBUGGER)) Debug.waitForDebugger();
		super.onCreate(arguments);
		mFlags = arguments == null ? 0 : arguments.getInt(KEY_FLAGS, 0);

		if ((mFlags & FLAG_MONITOR_ANIMATION_PERF) != 0)
			AnimationPerfMon.install(getTargetContext(), 15);

		mThreshold = arguments == null ? 5 : arguments.getInt(KEY_THREASHOLD, 5);

		if ((mFlags & FLAG_MONITOR_ACTIVITY_PERF) != 0){
			int launchtime = arguments == null ? 500 : arguments.getInt(KEY_ACTIVITY_LAUNCH_TIME, 500);
			ActivityPerfMon.install(getTargetContext(), launchtime);
		}
		start();
	}

	@Override public void onStart() {
		super.onStart();
		final PackageManager pm = getContext().getPackageManager();
		String target_pkg;
		try {
			final PackageInfo info = pm.getPackageInfo(getContext().getPackageName(), GET_INSTRUMENTATION);
			final InstrumentationInfo instru_info = info.instrumentation[0];
			target_pkg = instru_info.targetPackage;
		} catch (final NameNotFoundException e) { /* Should never happen */ return; }	
		
		final Intent intent = pm.getLaunchIntentForPackage(target_pkg);

		waitForIdleSync();		// TODO: Wait for CPU to be real idle

		ActivityMonitor monitor = new ActivityMonitor("com.taobao.tao.MainActivity2", null, false);
		addMonitor(monitor);

		//mCollector.beginSnapshot("Startup");
		if ((mFlags & FLAG_LAUNCH_PROFILING) != 0)
			Debug.startMethodTracing(Environment.getExternalStorageDirectory().getPath() + "/ski.trace", 128 * 1024 * 1024);
		
		SysLogMon.start(this, mThreshold);
		
		// Start launch intent of the app.
		long start = System.currentTimeMillis();
		getTargetContext().startActivity(intent);
		waitForMonitor(monitor);
		waitForIdleSync();
		long duration = System.currentTimeMillis() - start;
		//mCollector.endSnapshot();
		if ((mFlags & FLAG_LAUNCH_TIMING) != 0) notify("App Start Time", duration + "ms");

//		SysLogMon.stop();
		

		
		Toto.get(this.getTargetContext()).showToast("Ski Load finish");
	}

	@Override public void callActivityOnResume(Activity activity) {
		super.callActivityOnResume(activity);
        if ((mFlags & FLAG_LAUNCH_PROFILING) != 0
        		&& activity.getClass().getName().equals("com.taobao.tao.MainActivity2"))
        	Debug.stopMethodTracing();
	}

	@SuppressWarnings("deprecation")
	private void notify(String title, String msg) {
		Context context = getTargetContext();
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification(android.R.drawable.stat_notify_error, msg, System.currentTimeMillis());
		n.setLatestEventInfo(context, title, msg, PendingIntent.getActivity(context, 0, new Intent(), 0));
		nm.notify(msg.hashCode(), n);
	}

	private int mFlags;
	private int mThreshold;
	private static final String TAG = "Dock";
}
