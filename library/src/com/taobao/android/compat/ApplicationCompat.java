package com.taobao.android.compat;

import java.util.ArrayList;

import javax.annotation.NonNull;
import javax.annotation.NonNullByDefault;
import javax.annotation.Nullable;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import com.taobao.android.compat.ApplicationCompat.ActivityLifecycleCallbacksCompat;

/**
 * Provide activity life-cycle callback APIs for API level ~13.
 *
 * <p><b>This class (or its derivation) MUST be explicitly declared in the AndroidManifest.xml</b>
 *
 * @author Oasis
 */
@NonNullByDefault
public class ApplicationCompat extends Application {

	/** Backward-compatible version of {@link Application.ActivityLifecycleCallbacks} */
    public interface ActivityLifecycleCallbacksCompat {
        void onActivityCreated(Activity activity, @Nullable Bundle savedInstanceState);
        void onActivityStarted(Activity activity);
        void onActivityResumed(Activity activity);
        void onActivityPaused(Activity activity);
        void onActivityStopped(Activity activity);
        void onActivitySaveInstanceState(Activity activity, Bundle outState);
        void onActivityDestroyed(Activity activity);
    }

    /** Empty implementation of {@link ActivityLifecycleCallbacksCompat} for subset overriding */
    public static class AbstractActivityLifecycleCallbacks implements ActivityLifecycleCallbacksCompat {

		@Override public void onActivityCreated(Activity activity, @Nullable Bundle savedInstanceState) {}
		@Override public void onActivityStarted(Activity activity) {}
		@Override public void onActivityResumed(Activity activity) {}
		@Override public void onActivityPaused(Activity activity) {}
		@Override public void onActivityStopped(Activity activity) {}
		@Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
		@Override public void onActivityDestroyed(Activity activity) {}
    }

	public SharedPreferencesCompat getSharedPreferencesCompat(String name, int mode) {
		@NonNull @SuppressWarnings("null")	// SDK lacks @NonNull declaration
		SharedPreferences prefs = super.getSharedPreferences(name, mode);
		return new SharedPreferencesWrapper(prefs);
	}

    /** Provide this method for API level ~13 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacksCompat callback) {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
			super.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksWrapper(callback));
		} else synchronized (mActivityLifecycleCallbacks) {
            mActivityLifecycleCallbacks.add(callback);
        }
	}

    /** Provide this method for API level ~13 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacksCompat callback) {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
			super.unregisterActivityLifecycleCallbacks(new ActivityLifecycleCallbacksWrapper(callback));
		} else synchronized (mActivityLifecycleCallbacks) {
            mActivityLifecycleCallbacks.remove(callback);
        }
	}

	/* package */ void dispatchActivityCreatedCompat(Activity activity, @Nullable Bundle savedInstanceState) {
		ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivityCreated(activity, savedInstanceState);
            }
        }
    }

    /* package */ void dispatchActivityStartedCompat(Activity activity) {
    	ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivityStarted(activity);
            }
        }
    }

    /* package */ void dispatchActivityResumedCompat(Activity activity) {
    	ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivityResumed(activity);
            }
        }
    }

    /* package */ void dispatchActivityPausedCompat(Activity activity) {
    	ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivityPaused(activity);
            }
        }
    }

    /* package */ void dispatchActivityStoppedCompat(Activity activity) {
    	ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivityStopped(activity);
            }
        }
    }

    /* package */ void dispatchActivitySaveInstanceStateCompat(Activity activity, Bundle outState) {
    	ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivitySaveInstanceState(activity, outState);
            }
        }
    }

    /* package */ void dispatchActivityDestroyedCompat(Activity activity) {
    	ActivityLifecycleCallbacksCompat[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i=0; i<callbacks.length; i++) {
                callbacks[i].onActivityDestroyed(activity);
            }
        }
    }

    private @Nullable ActivityLifecycleCallbacksCompat[] collectActivityLifecycleCallbacks() {
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

@NonNullByDefault @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class ActivityLifecycleCallbacksWrapper implements ActivityLifecycleCallbacks {

	/* SDK lacks @NonNull declaration */
	@SuppressWarnings("null") @Override public void onActivityCreated(Activity activity, @Nullable Bundle savedInstanceState) { mCompat.onActivityCreated(activity, savedInstanceState); }
	@SuppressWarnings("null") @Override public void onActivityStarted(Activity activity) { mCompat.onActivityStarted(activity); }
	@SuppressWarnings("null") @Override public void onActivityResumed(Activity activity) { mCompat.onActivityResumed(activity); }
	@SuppressWarnings("null") @Override public void onActivityPaused(Activity activity) { mCompat.onActivityPaused(activity); }
	@SuppressWarnings("null") @Override public void onActivityStopped(Activity activity) { mCompat.onActivityStopped(activity); }
	@SuppressWarnings("null") @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { mCompat.onActivitySaveInstanceState(activity, outState); }
	@SuppressWarnings("null") @Override public void onActivityDestroyed(Activity activity) { mCompat.onActivityDestroyed(activity); }

	@Override public int hashCode() { return mCompat.hashCode(); }

	@Override public boolean equals(@Nullable Object obj) {
		if (this == obj) return true;
		if (! (obj instanceof ActivityLifecycleCallbacksWrapper)) return false;
		return mCompat.equals(((ActivityLifecycleCallbacksWrapper) obj).mCompat);
	}

	ActivityLifecycleCallbacksWrapper(ActivityLifecycleCallbacksCompat compat) {
		mCompat = compat;
	}

	private final ActivityLifecycleCallbacksCompat mCompat;
}
