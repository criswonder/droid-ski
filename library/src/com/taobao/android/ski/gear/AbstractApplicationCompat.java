package com.taobao.android.ski.gear;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

/** @author Oasis */
public class AbstractApplicationCompat extends Application {

    public interface ActivityLifecycleCallbacksCompat {
        void onActivityCreated(Activity activity, Bundle savedInstanceState);
        void onActivityStarted(Activity activity);
        void onActivityResumed(Activity activity);
        void onActivityPaused(Activity activity);
        void onActivityStopped(Activity activity);
        void onActivitySaveInstanceState(Activity activity, Bundle outState);
        void onActivityDestroyed(Activity activity);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private class ActivityLifecycleCallbacksWrapper implements ActivityLifecycleCallbacks {

    	private ActivityLifecycleCallbacksWrapper(ActivityLifecycleCallbacksCompat compat) { mCompat = compat; }
    	@Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { mCompat.onActivityCreated(activity, savedInstanceState); }
		@Override public void onActivityStarted(Activity activity) { mCompat.onActivityStarted(activity); }
		@Override public void onActivityResumed(Activity activity) { mCompat.onActivityResumed(activity); }
		@Override public void onActivityPaused(Activity activity) { mCompat.onActivityPaused(activity); }
		@Override public void onActivityStopped(Activity activity) { mCompat.onActivityStopped(activity); }
		@Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { mCompat.onActivitySaveInstanceState(activity, outState); }
		@Override public void onActivityDestroyed(Activity activity) { mCompat.onActivityDestroyed(activity); }

    	private final ActivityLifecycleCallbacksCompat mCompat;
    }

    /** Provide this method for pre-ICS */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacksCompat callback) {
		if (Build.VERSION.SDK_INT < VERSION_CODES.ICE_CREAM_SANDWICH) {
	        synchronized (mActivityLifecycleCallbacks) {
	            mActivityLifecycleCallbacks.add(callback);
	        }
		} else
			super.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksWrapper(callback));

	}

	/* package */ void dispatchActivityCreated(Activity activity, Bundle savedInstanceState) {
		ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivityCreated(activity,
                        savedInstanceState);
            }
        }
    }

    /* package */ void dispatchActivityStarted(Activity activity) {
    	ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivityStarted(activity);
            }
        }
    }

    /* package */ void dispatchActivityResumed(Activity activity) {
    	ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivityResumed(activity);
            }
        }
    }

    /* package */ void dispatchActivityPaused(Activity activity) {
    	ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivityPaused(activity);
            }
        }
    }

    /* package */ void dispatchActivityStopped(Activity activity) {
    	ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivityStopped(activity);
            }
        }
    }

    /* package */ void dispatchActivitySaveInstanceState(Activity activity, Bundle outState) {
    	ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivitySaveInstanceState(activity, outState);
            }
        }
    }

    /* package */ void dispatchActivityDestroyed(Activity activity) {
    	ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivityDestroyed(activity);
            }
        }
    }

    private ActivityLifecycleCallbacksCompat[] collectActivityLifecycleCallbacks() {
    	ActivityLifecycleCallbacksCompat[] callbacks = null;
        synchronized (mActivityLifecycleCallbacks) {
            if (mActivityLifecycleCallbacks.size() > 0) {
                callbacks = mActivityLifecycleCallbacks.toArray(new ActivityLifecycleCallbacksCompat[mActivityLifecycleCallbacks.size()]);
            }
        }
        return callbacks;
    }

    private final ArrayList<ActivityLifecycleCallbacksCompat> mActivityLifecycleCallbacks = new ArrayList<ActivityLifecycleCallbacksCompat>();
}
