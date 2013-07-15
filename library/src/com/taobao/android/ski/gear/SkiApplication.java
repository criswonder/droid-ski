package com.taobao.android.ski.gear;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable.Nullable;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

/**
 * Optional dependent base class of Application with enhanced features
 *
 * @author Oasis
 */
public class SkiApplication extends ApplicationCompat {

	public interface CrossActivityLifecycleCallback {
		/** First activity within this application is created */
        void onCreated(Activity activity);
        /** First activity within this application is started */
        void onStarted(Activity activity);
        /** All activities within this application are stopped */
        void onStopped(Activity activity);
        /** All activities within this application are destroyed */
        void onDestroyed(Activity activity);
	}

	public void registerCrossActivityLifecycleCallback(CrossActivityLifecycleCallback callback) {
		mCallbacks.add(callback);
	}

	public void unregisterCrossActivityLifecycleCallback(CrossActivityLifecycleCallback callback) {
		mCallbacks.remove(callback);
	}

	/** Similar to {@link android.app.Activity#runOnUiThread(Runnable)}, in static manner. */
	public static void runOnUiThread(Runnable runnable) {
		mAppHandler.post(runnable);
	}

	@Override public void onCreate() {
		super.onCreate();
		registerActivityLifecycleCallbacks(new CrossActivityLifecycleCallbacks());
		SafeAsyncTask.init();
	}

	private final List<CrossActivityLifecycleCallback> mCallbacks = new CopyOnWriteArrayList<CrossActivityLifecycleCallback>();
	private final AtomicInteger mCreationCount = new AtomicInteger();
	private final AtomicInteger mStartCount = new AtomicInteger();
	private static final Handler mAppHandler = new Handler();

	class CrossActivityLifecycleCallbacks implements ActivityLifecycleCallbacksCompat {

		@Override public void onActivityCreated(Activity activity, @Nullable Bundle savedInstanceState) {
			if (mCreationCount.getAndIncrement() == 0)
	            for (CrossActivityLifecycleCallback callback : mCallbacks)
	            	callback.onCreated(activity);
		}

		@Override public void onActivityStarted(Activity activity) {
			if (mStartCount.getAndIncrement() == 0)
	            for (CrossActivityLifecycleCallback callback : mCallbacks)
	            	callback.onStarted(activity);
		}

		@Override public void onActivityStopped(Activity activity) {
			if (mStartCount.decrementAndGet() == 0)
	            for (CrossActivityLifecycleCallback callback : mCallbacks)
	                callback.onStopped(activity);
		}

		@Override public void onActivityDestroyed(Activity activity) {
			if (mCreationCount.decrementAndGet() == 0)
	            for (CrossActivityLifecycleCallback callback : mCallbacks)
	                callback.onDestroyed(activity);
		}

		@Override public void onActivityResumed(Activity activity) {}
		@Override public void onActivityPaused(Activity activity) {}
		@Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
	}
}
