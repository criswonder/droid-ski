package com.taobao.android.ski.gear;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.taobao.android.ski.gear.SharedPreferencesCompat.SharedPreferencesWrapper;

/**
 * Backward-compatibility for functionalities of new Android platform versions.
 *
 * <p><b>MUST be used together with {@link ApplicationCompat}</b>
 *
 * @author Oasis
 */
public class FragmentActivityCompat extends FragmentActivity {

	private static final boolean COMPAT = Build.VERSION.SDK_INT < VERSION_CODES.ICE_CREAM_SANDWICH;

	public SharedPreferencesCompat getSharedPreferencesCompat(String name, int mode) {
		return new SharedPreferencesWrapper(super.getSharedPreferences(name, mode));
	}

	/**
	 * Provide this method for API level ~16.
	 *
	 * <p>Note: It should <b>NEVER</b> be used in {@link Fragment#onDestroy()},
	 *   since the return value may be different from the one in API level 17.
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override public boolean isDestroyed() {
		if (Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR1) return mDestroyed;
		return super.isDestroyed();
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (COMPAT) getApplicationCompat().dispatchActivityCreated(this, savedInstanceState);
	}

	@Override protected void onStart() {
		super.onStart();
		if (COMPAT) getApplicationCompat().dispatchActivityStarted(this);
	}

	@Override protected void onResume() {
		super.onResume();
		if (COMPAT) getApplicationCompat().dispatchActivityResumed(this);
	}

	@Override protected void onPause() {
		super.onPause();
		if (COMPAT) getApplicationCompat().dispatchActivityPaused(this);
	}

	@Override protected void onStop() {
		super.onStop();
		if (COMPAT) getApplicationCompat().dispatchActivityStopped(this);
	}

	@Override protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (COMPAT) getApplicationCompat().dispatchActivitySaveInstanceState(this, outState);
	}

	@Override protected void onDestroy() {
        mDestroyed = true;
		super.onDestroy();
		if (COMPAT) getApplicationCompat().dispatchActivityDestroyed(this);
	}

	private ApplicationCompat getApplicationCompat() {
		return (ApplicationCompat) getApplication();
	}

	private boolean mDestroyed;			// Needed for API level ~16
}
