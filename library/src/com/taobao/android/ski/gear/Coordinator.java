package com.taobao.android.ski.gear;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Debug;
import android.util.Log;

/** @author Oasis */
public class Coordinator {

	public static abstract class TaggedRunnable implements Runnable {
		public TaggedRunnable(String tag) { this.tag = tag; }
		private final String tag;
	}

	public static void postTask(TaggedRunnable runnable) {
		StandaloneTask task = new StandaloneTask(runnable);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			task.execute();
		else
			task.executeOnExecutor(mExecutor);
//		mTasks.add(task);
	}

	public static void postTasks(TaggedRunnable... runnables) {
		for (TaggedRunnable runnable : runnables)
			postTask(runnable);
	}

	public static void addFinisher(Runnable runnable) {
		mFinisher.add(runnable);
	}

	public static void runTasks(TaggedRunnable... runnables) {
		for (TaggedRunnable runnable : runnables)
			runWithTiming(runnable);
	}

	private static void runWithTiming(TaggedRunnable runnable) {
		boolean failed = false;
		long time = System.nanoTime();
		long cputime = Debug.threadCpuTimeNanos();
		try {
			runnable.run();
		} catch(Exception e) {
			failed = true;
			e.printStackTrace(System.err);
		} finally {
			cputime = Debug.threadCpuTimeNanos() - cputime;
			time = System.nanoTime() - time;
			Log.i(TAG, "Timing - " + runnable.tag + (failed ? " (failed): " : ": ")
					+ cputime / 1000000 + "ms (cpu) / " + time / 1000000 + "ms (real)");
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static Executor getDefaultAsyncTaskExecutor() {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
			return AsyncTask.SERIAL_EXECUTOR;
		else try {
			Field field = AsyncTask.class.getDeclaredField("sExecutor");
			field.setAccessible(true);
			return (Executor) field.get(null);
		} catch(Exception e) { return null; }
	}

//	private static final List<AsyncTask<?,?,?>> mTasks = new LinkedList<AsyncTask<?,?,?>>();
	private static final List<Runnable> mFinisher = new LinkedList<Runnable>();
	private static final Executor mExecutor;
	private static final String TAG = "Coord";

	static {
		SaturativeExecutor executor = new SaturativeExecutor();
		if (SaturativeExecutor.installAsDefaultAsyncTaskExecutor(executor))
			mExecutor = executor;
		else {
			Executor default_executor = getDefaultAsyncTaskExecutor();
			mExecutor = default_executor != null ? default_executor : new SaturativeExecutor();
		}
	}

	static class StandaloneTask extends AsyncTask<Void, Void, Void> {

		public StandaloneTask(TaggedRunnable runnable) {
			mRunnable = runnable;
		}

		@Override protected Void doInBackground(Void... params) {
			runWithTiming(mRunnable);
			return null;
		}

		private final TaggedRunnable mRunnable;
	}
}
