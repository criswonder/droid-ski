package com.taobao.android.ski.gear;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Backward-compatibility for functionalities of new Android platform versions.
 * MUST be used together with {@link AbstractApplicationCompat}
 *
 * @author Oasis
 */
public class AbstractActivityCompat extends FragmentActivity {

	private static final boolean COMPAT = Build.VERSION.SDK_INT < VERSION_CODES.ICE_CREAM_SANDWICH;

	@Override public SharedPreferences getSharedPreferences(String name, int mode) {
		return SharedPreferencesCompat.wrap(super.getSharedPreferences(name, mode));
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
		super.onDestroy();
		if (COMPAT) getApplicationCompat().dispatchActivityDestroyed(this);
	}
	
	private AbstractApplicationCompat getApplicationCompat() {
		return (AbstractApplicationCompat) getApplication();
	}
}
