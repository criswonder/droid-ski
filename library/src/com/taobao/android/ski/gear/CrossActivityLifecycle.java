package com.taobao.android.ski.gear;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.taobao.android.ski.gear.ApplicationCompat.ActivityLifecycleCallbacksCompat;

/**
 * Cross-activity life-cycle observation
 *
 * @author Oasis
 */
public class CrossActivityLifecycle {

	interface CrossActivityLifecycleCallback {
		/** First activity within this application is created */
        void onCreated(Activity activity);
        /** First activity within this application is started */
        void onStarted(Activity activity);
        /** All activities within this application are stopped */
        void onStopped(Activity activity);
        /** All activities within this application are destroyed */
        void onDestroyed(Activity activity);
	}

	/** This object <b>MUST be created only once</b> for each application. It is suggested to only instantiate
	 *  the singleton in your {@link android.app.Application} derived class */
	public CrossActivityLifecycle(ApplicationCompat application) {
		mApplication = application;

		// A insufficient but effective-for-most-cases violation check
		synchronized(getClass()) {
			Application binded_app = mBindedApplication != null ? mBindedApplication.get() : null;
			if (binded_app == null)
				mBindedApplication = new WeakReference<Application>(application);
			else if (binded_app == application)		// Equation of application must be checked by reference.
				throw new IllegalStateException("Duplicate instantiation of CrossActivityLifecycle for " + application);
		}
	}

	public void registerCallback(CrossActivityLifecycleCallback callback) {
		synchronized (mCallbacks) {
			mCallbacks.add(callback);
			if (mCallbacks.size() == 1)
				mApplication.registerActivityLifecycleCallbacks(mObserver = new ActivityLifecycleObserver());
        }
	}

	public void unregisterCallback(CrossActivityLifecycleCallback callback) {
		synchronized (mCallbacks) {
			mCallbacks.remove(callback);
			if (mCallbacks.isEmpty()) {
				mApplication.unregisterActivityLifecycleCallbacks(mObserver);
				mObserver = null;
			}
		}
	}

	private CrossActivityLifecycleCallback[] collectCallbacks() {
        synchronized (mCallbacks) {
            return mCallbacks.toArray(new CrossActivityLifecycleCallback[mCallbacks.size()]);
        }
    }

	private final ApplicationCompat mApplication;
	private final AtomicInteger mCreationCount = new AtomicInteger();
	private final AtomicInteger mStartCount = new AtomicInteger();
	private final List<CrossActivityLifecycleCallback> mCallbacks = new ArrayList<CrossActivityLifecycleCallback>();
	private volatile ActivityLifecycleObserver mObserver;
	private static WeakReference<Application> mBindedApplication;

	private class ActivityLifecycleObserver implements ActivityLifecycleCallbacksCompat {

		@Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
			if (mCreationCount.getAndIncrement() == 0)
	            for (CrossActivityLifecycleCallback callback : collectCallbacks())
	            	callback.onCreated(activity);
		}

		@Override public void onActivityDestroyed(Activity activity) {
			if (mCreationCount.decrementAndGet() == 0)
	            for (CrossActivityLifecycleCallback callback : collectCallbacks())
	                callback.onDestroyed(activity);
		}

		@Override public void onActivityStarted(Activity activity) {
			if (mStartCount.getAndIncrement() == 0)
	            for (CrossActivityLifecycleCallback callback : collectCallbacks())
	            	callback.onStarted(activity);
		}

		@Override public void onActivityStopped(Activity activity) {
			if (mStartCount.decrementAndGet() == 0)
	            for (CrossActivityLifecycleCallback callback : collectCallbacks())
	                callback.onStopped(activity);
		}

		@Override public void onActivityResumed(Activity activity) {}
		@Override public void onActivityPaused(Activity activity) {}
		@Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
	}
}
