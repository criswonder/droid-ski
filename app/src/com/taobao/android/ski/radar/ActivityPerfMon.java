package com.taobao.android.ski.radar;

import static android.Manifest.permission.READ_LOGS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.EventLog;
import android.util.EventLog.Event;
import android.util.Log;

import com.taobao.android.ski.hud.Yell;

/** @author Oasis */
public class ActivityPerfMon {

	private static final long PATROL_INTERVAL = 3000;

	public static void install(final Context context, final long threashold_ms) {
//	    if (PERMISSION_GRANTED != context.getPackageManager().checkPermission(READ_LOGS, context.getPackageName()))
//	        throw new IllegalStateException(context.getPackageName() + " Permission not granted: " + READ_LOGS);

	    mAppContext = context.getApplicationContext();
		mYell = Yell.get(context);
		mThreshold = threashold_ms;
		mHandler = new Handler(Looper.getMainLooper());
		mHandler.postDelayed(mPatrol, PATROL_INTERVAL);
	}

	private static void scanActivityEvents() throws IOException {
		int tag_activity_time = EventLog.getTagCode("activity_launch_time");
		if (tag_activity_time < 0)
			tag_activity_time = EventLog.getTagCode("am_activity_launch_time");
		if (tag_activity_time < 0) throw new IllegalStateException("Can't detect event log tag. (Incompatible ROM)");

		final List<Event> events = new LinkedList<Event>();
		EventLog.readEvents(new int[] { tag_activity_time }, events);
		final String self_pkg = mAppContext.getPackageName();
		final int idx = Build.VERSION.SDK_INT >= 17/* Build.VERSION_CODES.JELLY_BEAN_MR1 */? 2 : 1;
		for (final Event event : events) {
			final long time = event.getTimeNanos();
			if (time < mLastScannedEventTimeNanos) continue;
			mLastScannedEventTimeNanos = time;

			final Object[] data = (Object[]) event.getData();
			final String cmp = (String) data[idx];
			final ComponentName component = ComponentName.unflattenFromString(cmp);
			if (! self_pkg.equals(component.getPackageName())) continue;

			final long duration = (Long) data[idx + 1];
			Log.v(TAG, component.getShortClassName()+":"+duration);
			if (duration > mThreshold)
			    mYell.showNotification(TAG, "Too slow loading activity", duration + "ms " + component.getShortClassName());
		}
	}

	public static void stop(){
		mStop = true;
	}
	
	private static Runnable mPatrol = new Runnable() { @Override public void run() {
		try {
			if(!mStop) {
				scanActivityEvents();
				mHandler.postDelayed(this, PATROL_INTERVAL);
			}
		} catch (final IOException e) { Log.e(TAG, "Failed to scan activity events.", e); }
	}};

	private static Context mAppContext;
	private static Handler mHandler;
	private static long mThreshold;
	private static Yell mYell;
	private static long mLastScannedEventTimeNanos = System.currentTimeMillis() * 1000000;
	private static boolean mStop = false;

	private static final String TAG = "ActvtPerf";
}
