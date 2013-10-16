package com.taobao.android.lifecycle;

import static android.content.pm.PackageManager.GET_ACTIVITIES;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.NonNullByDefault;
import javax.annotation.Nullable;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.taobao.android.base.Versions;
import com.taobao.android.compat.ApplicationCompat;
import com.taobao.android.hurdle.Hurdle;
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
	 *
	 * @deprecated pending design reconsideration, DO NOT use it at present.
	 */
	@Deprecated
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
		 * Called before {@link android.app.Activity#onStart()}
		 *
		 * <p>Note: This relies on all sub-classes of {@link PanguActivity} calling <code>super.onStart()</code> <b>first</b>.
		 */
		void onActivityPreStart(PanguActivity activity);

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

		/** Called before {@link android.app.Activity#onWindowFocusChanged(boolean hasFocus) */
	    void onWindowFocusChanged(boolean hasFocus);
	}

    /**
     * Empty implementation of {@link ActivityLifecycleCallbacks2} for subset overriding
     *
	 * @deprecated pending design reconsideration, DO NOT use it at present.
     */
	@Deprecated
	public static class AbstractActivityLifecycleCallbacks2 extends AbstractActivityLifecycleCallbacks implements ActivityLifecycleCallbacks2 {

		@Override public void onActivityPreCreate(final PanguActivity activity, @Nullable final Bundle savedInstanceState) {}
		@Override public void onActivityPostCreate(final PanguActivity activity, @Nullable final Bundle savedInstanceState) {}
		@Override public void onActivityPreStart(final PanguActivity activity) {}
		@Override public void onActivityPreRestart(final PanguActivity activity) {}
		@Override public void onActivityPreResume(final PanguActivity activity) {}
		@Override public void onActivityPostResume(final PanguActivity activity) {}
		@Override public void onWindowFocusChanged(final boolean hasFocus) {}
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
	 *
	 * @deprecated pending design reconsideration, DO NOT use it at present.
	 */
	@Deprecated
	public void registerActivityLifecycleCallbacks(final ActivityLifecycleCallbacks2 callbacks) {
		super.registerActivityLifecycleCallbacks(callbacks);
		mActivityLifecycleCallbacks.add(callbacks);
	}

	/** @deprecated pending design reconsideration, DO NOT use it at present. */
	@Deprecated
	public void unregisterActivityLifecycleCallbacks(final ActivityLifecycleCallbacks2 callbacks) {
		super.unregisterActivityLifecycleCallbacks(callbacks);
		mActivityLifecycleCallbacks.remove(callbacks);
	}

	public void registerCrossActivityLifecycleCallback(final CrossActivityLifecycleCallback callback) {
		mCrossActivityLifecycleCallbacks.add(callback);
	}

	public void unregisterCrossActivityLifecycleCallback(final CrossActivityLifecycleCallback callback) {
		mCrossActivityLifecycleCallbacks.remove(callback);
	}

	/** Similar to {@link android.app.Activity#runOnUiThread(Runnable)}, in static manner. */
	public static void runOnUiThread(final Runnable runnable) {
		mAppHandler.post(runnable);
	}

	@Override public void onCreate() {
		super.onCreate();
		Versions.init(this);
		registerActivityLifecycleCallbacks(new CrossActivityLifecycleCallbacks());
		SafeAsyncTask.init();

		if (Versions.isDebug())
			verifyDerivation();
	}

	private void verifyDerivation() {
		final PackageInfo pkg_info;
		try {
			pkg_info = getPackageManager().getPackageInfo(getPackageName(), GET_ACTIVITIES);
		} catch (final NameNotFoundException e) { return; /* Should never happen */ }

		final StringBuilder activities = new StringBuilder();
		for (final ActivityInfo activity_info : pkg_info.activities) {
			Class<?> activity_class;
			try {
				//didn't check remote activities.
				if(!activity_info.processName.equals(activity_info.applicationInfo.processName))
					continue;
				
				activity_class = Class.forName(activity_info.name);
			} catch (final ClassNotFoundException e) { continue; }
			if (! PanguActivity.class.isAssignableFrom(activity_class))
				activities.append(',').append(activity_info.name);
		}
		if (activities.length() > 0)
			Hurdle.block(TAG, "Not derived from " + PanguActivity.class.getSimpleName() + ": " + activities.substring(1));

	}

	void dispatchActivityPreCreate(final PanguActivity activity, @Nullable final Bundle savedInstanceState) {
		if (! mActivityLifecycleCallbacks.isEmpty())
			for (final ActivityLifecycleCallbacks2 callbacks : mActivityLifecycleCallbacks)
	            callbacks.onActivityPreCreate(activity, savedInstanceState);
	}

	void dispatchActivityPostCreate(final PanguActivity activity, @Nullable final Bundle savedInstanceState) {
		if (! mActivityLifecycleCallbacks.isEmpty())
			for (final ActivityLifecycleCallbacks2 callbacks : mActivityLifecycleCallbacks)
	            callbacks.onActivityPostCreate(activity, savedInstanceState);
    }

	void dispatchActivityPreStart(final PanguActivity activity) {
		if (! mActivityLifecycleCallbacks.isEmpty())
			for (final ActivityLifecycleCallbacks2 callbacks : mActivityLifecycleCallbacks)
	            callbacks.onActivityPreStart(activity);
	}

	void dispatchActivityPreRestart(final PanguActivity activity) {
		if (! mActivityLifecycleCallbacks.isEmpty())
			for (final ActivityLifecycleCallbacks2 callbacks : mActivityLifecycleCallbacks)
	            callbacks.onActivityPreRestart(activity);
	}

	void dispatchActivityPreResume(final PanguActivity activity) {
		if (! mActivityLifecycleCallbacks.isEmpty())
			for (final ActivityLifecycleCallbacks2 callbacks : mActivityLifecycleCallbacks)
	            callbacks.onActivityPreResume(activity);
	}

	void dispatchActivityPostResumed(final PanguActivity activity) {
		if (! mActivityLifecycleCallbacks.isEmpty())
			for (final ActivityLifecycleCallbacks2 callbacks : mActivityLifecycleCallbacks)
	            callbacks.onActivityPostResume(activity);
    }

	void dispatchActivityWindowFocusChanged(final boolean hasFocus) {
		if (! mActivityLifecycleCallbacks.isEmpty())
			for (final ActivityLifecycleCallbacks2 callbacks : mActivityLifecycleCallbacks)
	            callbacks.onWindowFocusChanged(hasFocus);
	}

	private final List<ActivityLifecycleCallbacks2> mActivityLifecycleCallbacks = new CopyOnWriteArrayList<ActivityLifecycleCallbacks2>();
	private final List<CrossActivityLifecycleCallback> mCrossActivityLifecycleCallbacks = new CopyOnWriteArrayList<CrossActivityLifecycleCallback>();
	private final AtomicInteger mCreationCount = new AtomicInteger();
	private final AtomicInteger mStartCount = new AtomicInteger();
	private static final Handler mAppHandler = new Handler();
	private static final String TAG = "PanguApp";

	class CrossActivityLifecycleCallbacks implements ActivityLifecycleCallbacksCompat {

		@Override public void onActivityCreated(final Activity activity, @Nullable final Bundle savedInstanceState) {
			if (mCreationCount.getAndIncrement() == 0 && ! mCrossActivityLifecycleCallbacks.isEmpty())
	            for (final CrossActivityLifecycleCallback callback : mCrossActivityLifecycleCallbacks)
	            	callback.onCreated(activity);
		}

		@Override public void onActivityStarted(final Activity activity) {
			if (mStartCount.getAndIncrement() == 0 && ! mCrossActivityLifecycleCallbacks.isEmpty())
	            for (final CrossActivityLifecycleCallback callback : mCrossActivityLifecycleCallbacks)
	            	callback.onStarted(activity);
		}

		@Override public void onActivityStopped(final Activity activity) {
			if (mStartCount.decrementAndGet() == 0 && ! mCrossActivityLifecycleCallbacks.isEmpty())
	            for (final CrossActivityLifecycleCallback callback : mCrossActivityLifecycleCallbacks)
	                callback.onStopped(activity);
		}

		@Override public void onActivityDestroyed(final Activity activity) {
			if (mCreationCount.decrementAndGet() == 0 && ! mCrossActivityLifecycleCallbacks.isEmpty())
	            for (final CrossActivityLifecycleCallback callback : mCrossActivityLifecycleCallbacks)
	                callback.onDestroyed(activity);
		}

		@Override public void onActivityResumed(final Activity activity) {}
		@Override public void onActivityPaused(final Activity activity) {}
		@Override public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {}
	}
}
