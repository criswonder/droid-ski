package com.taobao.android.task;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;

import javax.annotation.NonNullByDefault;
import javax.annotation.Nullable;

import com.taobao.android.base.Tools;
import com.taobao.android.base.Versions;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Debug;
import android.os.Looper;
import android.os.MessageQueue.IdleHandler;
import android.util.Log;

/** @author Oasis */
@NonNullByDefault
public class Coordinator {

	public static abstract class TaggedRunnable implements Runnable {
		public TaggedRunnable(String tag) { this.tag = tag; }
		private final String tag;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void postTask(TaggedRunnable runnable) {
		StandaloneTask task = new StandaloneTask(runnable);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			task.execute();
		else
			task.executeOnExecutor(mExecutor);
	}

	public static void postTasks(TaggedRunnable... runnables) {
		for (TaggedRunnable runnable : runnables)
			if (runnable != null) postTask(runnable);
	}

	public static void postIdleTask(TaggedRunnable runnable) {
		mIdleTasks.add(runnable);
	}

	public static void runTask(TaggedRunnable runnable) {
		runWithTiming(runnable);
	}

	public static void runTasks(TaggedRunnable... runnables) {
		for (TaggedRunnable runnable : runnables)
			if (runnable != null) runWithTiming(runnable);
	}

	/** Must be called on the main thread. */
	public static void scheduleIdleTasks() {
		Looper.myQueue().addIdleHandler(new IdleHandler() {

			@Override public boolean queueIdle() {
				TaggedRunnable task = mIdleTasks.poll();	// One at a time to avoid congestion.
				if (task == null) return false;
				postTask(task);
				return ! mIdleTasks.isEmpty();
			}});
	}

	private static void runWithTiming(TaggedRunnable runnable) {
		boolean failed = false;
		long time = System.nanoTime();
		long cputime = Debug.threadCpuTimeNanos();
		try {
			runnable.run();
		} catch(RuntimeException e) {
			failed = true;
			Log.w(TAG, "Exception in " + runnable.tag, e);
		} finally {
			cputime = (Debug.threadCpuTimeNanos() - cputime) / 1000000;
			time = (System.nanoTime() - time) / 1000000;
			
			if(mTimeingCallback != null){
				mTimeingCallback.onTimingCallback(runnable.tag, cputime, time);
			}
			
			if(Versions.isDebug()) {
				Log.i(TAG, "Timing - " + runnable.tag + (failed ? " (failed): " : ": ")
						+ cputime / 1000000 + "ms (cpu) / " + time / 1000000 + "ms (real)");
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	static @Nullable Executor getDefaultAsyncTaskExecutor() {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
			return AsyncTask.SERIAL_EXECUTOR;
		else try {
			Field field = AsyncTask.class.getDeclaredField("sExecutor");
			field.setAccessible(true);
			return (Executor) field.get(null);
		} catch(Exception e) { return null; }
	}

	static Executor getCurrentExecutor() {
		return mExecutor;
	}

	public static void setTimingCallback(CoordinatorTimingCallback timeingCallback) {
		mTimeingCallback = timeingCallback;
	}
	
	private static final Queue<TaggedRunnable> mIdleTasks = new LinkedList<TaggedRunnable>();
	private static final Executor mExecutor;
	private static CoordinatorTimingCallback mTimeingCallback = null;
	private static final String TAG = "Coord";

	static {
		SaturativeExecutor executor = new SaturativeExecutor();
		if (SaturativeExecutor.installAsDefaultAsyncTaskExecutor(executor))
			mExecutor = executor;
		else {
			Executor default_executor = getDefaultAsyncTaskExecutor();
			mExecutor = default_executor != null ? default_executor : executor;
		}
	}

	static class StandaloneTask extends AsyncTask<Void, Void, Void> {

		public StandaloneTask(TaggedRunnable runnable) {
			mRunnable = runnable;
		}

		@Override @Nullable protected Void doInBackground(Void... params) {
			runWithTiming(mRunnable);
			return null;
		}

		private final TaggedRunnable mRunnable;
	}
}
