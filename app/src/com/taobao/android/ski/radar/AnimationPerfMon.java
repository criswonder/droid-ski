package com.taobao.android.ski.radar;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.util.Printer;
import android.view.Choreographer;

/** @author Oasis */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public final class AnimationPerfMon {

	public static void install(Context context, int tolerable_frames_skipped) {
		AnimationPerfMon instance = new AnimationPerfMon(context, tolerable_frames_skipped);
		instance.start();
	}

	private void start() {
		mLooper.setMessageLogging(new LooperMonitor());
	}

	public AnimationPerfMon(Context context, int tolerable_frames_skipped) {
		mChoreographer = Choreographer.getInstance();
		mLooper = Looper.myLooper();
		mMainThread = mLooper.getThread();
		
		long frame_interval;
		try {
			frame_interval = (Long) Choreographer.class.getDeclaredField("mFrameIntervalNanos").get(mChoreographer);
		} catch (Exception e) {
			frame_interval = 1000000000 / 60;
		}
		mWatchdogTimeout = frame_interval * tolerable_frames_skipped / 1000000;
	}

	private final TimerTask mWatchdogTask = new TimerTask() { @Override public void run() {
		// Dump the current call stack of main thread
		StackTraceElement[] stack = mMainThread.getStackTrace();
		Log.w("Lag", Arrays.deepToString(stack));
	}};

	private final Choreographer mChoreographer;
	private final Looper mLooper;
	private final long mWatchdogTimeout;
	private final Timer mWatchdog = new Timer("Ski/AnimationPerfMon", true);
	private final Thread mMainThread;

	private static final int MATCH_PREFIX_LENGTH = ">>>>> Dispatching to Handler (".length();
	private static final String FRAME_HANDLER_NAME = "android.view.Choreographer$FrameHandler";

	private class LooperMonitor implements Printer {

		@Override public void println(String log) {

			if (log.charAt(0) == '>' && log.length() > MATCH_PREFIX_LENGTH
					&& log.substring(MATCH_PREFIX_LENGTH).startsWith(FRAME_HANDLER_NAME)) try {		// Before message delivery
				if (mScheduled) {
					mScheduled = false;
					mWatchdogTask.cancel();
				}
				mWatchdog.schedule(mWatchdogTask, mWatchdogTimeout);
				mScheduled = true;
			} catch(RuntimeException e) { /* Ignore IllegalStateException due to task already running */ }
			//System.out.println(log);
		}

		private boolean mScheduled = false;
	}
	
	private static final String TAG = "AnimationPerf";
}
