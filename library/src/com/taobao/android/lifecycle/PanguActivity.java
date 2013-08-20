package com.taobao.android.lifecycle;

import javax.annotation.NonNullByDefault;
import javax.annotation.Nullable;

import android.os.Bundle;

import com.taobao.android.compat.FragmentActivityCompat;

/**
 * Provide additional capabilities, must be used together with {@link PanguApplication). }
 *
 * @author Oasis
 */
@NonNullByDefault
public class PanguActivity extends FragmentActivityCompat {

	@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
		getPanguApplication().dispatchActivityPreCreate(this, savedInstanceState);
		super.onCreate(savedInstanceState);
	}

	@Override protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
        getPanguApplication().dispatchActivityPostCreate(this, savedInstanceState);
	}

	@Override protected void onStart() {
		getPanguApplication().dispatchActivityPreStart(this);
		super.onStart();
	}
	
	@Override protected void onRestart() {
		getPanguApplication().dispatchActivityPreRestart(this);
		super.onRestart();
	}

	@Override protected void onResume() {
		getPanguApplication().dispatchActivityPreResume(this);
		super.onResume();
	}

	@Override protected void onPostResume() {
		super.onPostResume();
		getPanguApplication().dispatchActivityPostResumed(this);
	}
	
	@Override public void onWindowFocusChanged(boolean hasFocus) {
		getPanguApplication().dispatchActivityWindowFocusChanged(hasFocus);
		super.onWindowFocusChanged(hasFocus);
    }

	@SuppressWarnings("null")	// SDK lacks @NonNull declaration
	private PanguApplication getPanguApplication() {
		return (PanguApplication) getApplication();
	}
}
