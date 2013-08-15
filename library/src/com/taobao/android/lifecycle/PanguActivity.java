package com.taobao.android.lifecycle;

import javax.annotation.NonNullByDefault;
import javax.annotation.Nullable;

import android.os.Bundle;

import com.taobao.android.compat.FragmentActivityCompat;
import com.taobao.android.lifecycle.PanguApplication.ActivityLifecycleCallbacks2;

/**
 * Provide additional capabilities, must be used together with {@link PanguApplication). }
 *
 * @author Oasis
 */
@NonNullByDefault
public class PanguActivity extends FragmentActivityCompat {

	@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
		// This assumes all sub-classes call super.onCreate() first.
		getPanguApplication().dispatchActivityPreCreate(this, savedInstanceState);
		super.onCreate(savedInstanceState);
	}

	/** @see PanguApplication#registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks2) */
	@Override protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
        getPanguApplication().dispatchActivityPostCreated(this, savedInstanceState);
	}

	/** @see PanguApplication#registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks2) */
	@Override protected void onPostResume() {
		super.onPostResume();
		getPanguApplication().dispatchActivityPostResumed(this);
	}

	@SuppressWarnings("null")	// SDK lacks @NonNull declaration
	private PanguApplication getPanguApplication() {
		return (PanguApplication) getApplication();
	}
}
