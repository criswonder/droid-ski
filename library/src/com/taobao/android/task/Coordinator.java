package com.taobao.android.task;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;

import javax.annotation.NonNullByDefault;
import javax.annotation.Nullable;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Debug;
import android.os.Looper;
import android.os.MessageQueue.IdleHandler;
import android.util.Log;

import com.taobao.android.base.Versions;

/** @author Oasis */
@NonNullByDefault
public class Coordinator {

	public static abstract class TaggedRunnable implements Runnable {
		public TaggedRunnable(final String tag) { this.tag = tag; }
		private final String tag;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void postTask(final TaggedRunnable runnable) {
		final StandaloneTask task = new StandaloneTask(runnable);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			task.execute();
		else
			task.executeOnExecutor(mExecutor);
	}

	public static void postTasks(final TaggedRunnable... runnables) {
		for (final TaggedRunnable runnable : runnables)
			if (runnable != null) postTask(runnable);
	}

	public static void postIdleTask(final TaggedRunnable runnable) {
		mIdleTasks.add(runnable);
	}

	public static void runTask(final TaggedRunnable runnable) {
		runWithTiming(runnable);
	}

	public static void runTasks(final TaggedRunnable... runnables) {
		for (final TaggedRunnable runnable : runnables)
			if (runnable != null) runWithTiming(runnable);
	}

	/** Must be called on the main thread. */
	public static void scheduleIdleTasks() {
		Looper.myQueue().addIdleHandler(new IdleHandler() {

			@Override public boolean queueIdle() {
				final TaggedRunnable task = mIdleTasks.poll();	// One at a time to avoid congestion.
				if (task == null) return false;
				postTask(task);
				return ! mIdleTasks.isEmpty();
			}
		});
	}

	private static void runWithTiming(final TaggedRunnable runnable) {
		boolean failed = false;
		final boolean debug = Versions.isDebug();
		long time = 0, cputime = 0;
		if (debug) {
			time = System.nanoTime();
			cputime = Debug.threadCpuTimeNanos();
		}

		try {
			runnable.run();
		} catch(final RuntimeException e) {
			failed = true;
			Log.w(TAG, "Exception in " + runnable.tag, e);
		} finally {
			if (debug) {
				cputime = (Debug.threadCpuTimeNanos() - cputime) / 1000000;
				time = (System.nanoTime() - time) / 1000000;
				Log.i(TAG, "Timing - " + runnable.tag + (failed ? " (failed): " : ": ")
						+ cputime + "ms (cpu) / " + time + "ms (real)");
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	static @Nullable Executor getDefaultAsyncTaskExecutor() {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
			return AsyncTask.SERIAL_EXECUTOR;
		else try {
			final Field field = AsyncTask.class.getDeclaredField("sExecutor");
			field.setAccessible(true);
			return (Executor) field.get(null);
		} catch(final Exception e) { return null; }
	}

	static Executor getCurrentExecutor() {
		return mExecutor;
	}

	private static final Queue<TaggedRunnable> mIdleTasks = new LinkedList<TaggedRunnable>();
	private static final Executor mExecutor;
	private static final String TAG = "Coord";

	static {
		final SaturativeExecutor executor = new SaturativeExecutor();
		if (SaturativeExecutor.installAsDefaultAsyncTaskExecutor(executor))
			mExecutor = executor;
		else {
			final Executor default_executor = getDefaultAsyncTaskExecutor();
			mExecutor = default_executor != null ? default_executor : executor;
		}
	}

	static class StandaloneTask extends AsyncTask<Void, Void, Void> {

		public StandaloneTask(final TaggedRunnable runnable) {
			mRunnable = runnable;
		}

		@Override @Nullable protected Void doInBackground(final Void... params) {
			runWithTiming(mRunnable);
			return null;
		}

		private final TaggedRunnable mRunnable;
	}
}
