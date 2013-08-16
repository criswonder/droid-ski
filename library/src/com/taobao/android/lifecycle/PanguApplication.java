package com.taobao.android.lifecycle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.NonNullByDefault;
import javax.annotation.Nullable;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.taobao.android.base.Versions;
import com.taobao.android.compat.ApplicationCompat;
import com.taobao.android.task.SafeAsyncTask;

/**
 * Optional dependent base class of Application with enhanced features
 *
 * @author Oasis
 */
@NonNullByDefault
public class PanguApplication extends ApplicationCompat {

	/**
	 * Expanded version of original ActivityLifecycleCallbacksCompat.
	 *
	 * <p>Since this interface may be further expanded to provide more events,
	 * it is strongly encouraged to extend {@link AbstractActivityLifecycleCallbacks2} instead,
	 * to keep <b>forward-compatibility</b>.
	 */
	public interface ActivityLifecycleCallbacks2 extends ActivityLifecycleCallbacksCompat {
		/**
		 * Called before {@link android.app.Activity#onCreate(Bundle savedInstanceState)}
		 *
		 * <p>Note: This relies on all sub-classes of {@link PanguActivity} calling <code>super.onCreate()</code> <b>first</b>.
		 */
		void onActivityPreCreate(PanguActivity activity, @Nullable Bundle savedInstanceState);
		/** Called before {@link android.app.Activity#onPostCreate(Bundle savedInstanceState)} */
		void onActivityPostCreate(PanguActivity activity, @Nullable Bundle savedInstanceState);
		/**
		 * Called before {@link android.app.Activity#onRestart()}
		 *
		 * <p>Note: This relies on all sub-classes of {@link PanguActivity} calling <code>super.onRestart()</code> <b>first</b>.
		 */
		void onActivityPreRestart(PanguActivity activity);
		/**
		 * Called before {@link android.app.Activity#onResume()}
		 *
		 * <p>Note: This relies on all sub-classes of {@link PanguActivity} calling <code>super.onResume()</code> <b>first</b>.
		 */
		void onActivityPreResume(PanguActivity activity);
		/** Called before {@link android.app.Activity#onPostResume(Bundle savedInstanceState)} */
		void onActivityPostResume(PanguActivity activity);
	}

    /** Empty implementation of {@link ActivityLifecycleCallbacks2} for subset overriding */
	public static class AbstractActivityLifecycleCallbacks2 extends AbstractActivityLifecycleCallbacks implements ActivityLifecycleCallbacks2 {

		@Override public void onActivityPreCreate(PanguActivity activity, @Nullable Bundle savedInstanceState) {}
		@Override public void onActivityPostCreate(PanguActivity activity, @Nullable Bundle savedInstanceState) {}
		@Override public void onActivityPreRestart(PanguActivity activity) {}
		@Override public void onActivityPreResume(PanguActivity activity) {}
		@Override public void onActivityPostResume(PanguActivity activity) {}
	}

	public interface CrossActivityLifecycleCallback {
		/** First activity within this application is created
		 *  @see android.app.Activity#onCreate(Bundle savedInstanceState) */
        void onCreated(Activity activity);
        /** First activity within this application is started */
        void onStarted(Activity activity);
        /** All activities within this application are stopped */
        void onStopped(Activity activity);
        /** All activities within this application are destroyed */
        void onDestroyed(Activity activity);
	}

	/**
	 * Expanded callback compared to the original one.
	 * Only work for {@link PanguActivity} derived activities.
	 */
	public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks2 callbacks) {
		super.registerActivityLifecycleCallbacks(callbacks);
		mActivityLifecycleCallbacks.add(callbacks);
	}

	public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks2 callbacks) {
		super.unregisterActivityLifecycleCallbacks(callbacks);
		mActivityLifecycleCallbacks.remove(callbacks);
	}

	public void registerCrossActivityLifecycleCallback(CrossActivityLifecycleCallback callback) {
		mCrossActivityLifecycleCallbacks.add(callback);
	}

	public void unregisterCrossActivityLifecycleCallback(CrossActivityLifecycleCallback callback) {
		mCrossActivityLifecycleCallbacks.remove(callback);
	}

	/** Similar to {@link android.app.Activity#runOnUiThread(Runnable)}, in static manner. */
	public static void runOnUiThread(Runnable runnable) {
		mAppHandler.post(runnable);
	}

	@Override public void onCreate() {
		super.onCreate();
		Versions.init(this);
		registerActivityLifecycleCallbacks(new CrossActivityLifecycleCallbacks());
		SafeAsyncTask.init();
	}

	void dispatchActivityPreCreate(PanguActivity activity, @Nullable Bundle savedInstanceState) {
		if (! mActivityLifecycleCallbacks.isEmpty())
			for (ActivityLifecycleCallbacks2 callbacks : mActivityLifecycleCallbacks)
	            callbacks.onActivityPreCreate(activity, savedInstanceState);
	}

	void dispatchActivityPostCreated(PanguActivity activity, @Nullable Bundle savedInstanceState) {
		if (! mActivityLifecycleCallbacks.isEmpty())
			for (ActivityLifecycleCallbacks2 callbacks : mActivityLifecycleCallbacks)
	            callbacks.onActivityPostCreate(activity, savedInstanceState);
    }

	void dispatchActivityPostResumed(PanguActivity activity) {
		if (! mActivityLifecycleCallbacks.isEmpty())
			for (ActivityLifecycleCallbacks2 callbacks : mActivityLifecycleCallbacks)
	            callbacks.onActivityPostResume(activity);
    }

	private final List<ActivityLifecycleCallbacks2> mActivityLifecycleCallbacks = new CopyOnWriteArrayList<ActivityLifecycleCallbacks2>();
	private final List<CrossActivityLifecycleCallback> mCrossActivityLifecycleCallbacks = new CopyOnWriteArrayList<CrossActivityLifecycleCallback>();
	private final AtomicInteger mCreationCount = new AtomicInteger();
	private final AtomicInteger mStartCount = new AtomicInteger();
	private static final Handler mAppHandler = new Handler();

	class CrossActivityLifecycleCallbacks implements ActivityLifecycleCallbacksCompat {

		@Override public void onActivityCreated(Activity activity, @Nullable Bundle savedInstanceState) {
			if (mCreationCount.getAndIncrement() == 0 && ! mCrossActivityLifecycleCallbacks.isEmpty())
	            for (CrossActivityLifecycleCallback callback : mCrossActivityLifecycleCallbacks)
	            	callback.onCreated(activity);
		}

		@Override public void onActivityStarted(Activity activity) {
			if (mStartCount.getAndIncrement() == 0 && ! mCrossActivityLifecycleCallbacks.isEmpty())
	            for (CrossActivityLifecycleCallback callback : mCrossActivityLifecycleCallbacks)
	            	callback.onStarted(activity);
		}

		@Override public void onActivityStopped(Activity activity) {
			if (mStartCount.decrementAndGet() == 0 && ! mCrossActivityLifecycleCallbacks.isEmpty())
	            for (CrossActivityLifecycleCallback callback : mCrossActivityLifecycleCallbacks)
	                callback.onStopped(activity);
		}

		@Override public void onActivityDestroyed(Activity activity) {
			if (mCreationCount.decrementAndGet() == 0 && ! mCrossActivityLifecycleCallbacks.isEmpty())
	            for (CrossActivityLifecycleCallback callback : mCrossActivityLifecycleCallbacks)
	                callback.onDestroyed(activity);
		}

		@Override public void onActivityResumed(Activity activity) {}
		@Override public void onActivityPaused(Activity activity) {}
		@Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
	}
}
