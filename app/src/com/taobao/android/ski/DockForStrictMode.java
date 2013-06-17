package com.taobao.android.ski;

import com.taobao.android.ski.radar.StrictModeMon;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/** @author baiyi */
public class DockForStrictMode extends Dock {

	public DockForStrictMode() {
		super();

		Log.e(TAG, "Create dock ex");
		StrictModeMon.start(this);
	}

	@Override public void onCreate(final Bundle arguments) {
		super.onCreate(arguments);
	}

	@Override public void onStart() {
		super.onStart();
		
//		StrictModeMon.stop();
	}

	@Override public void callActivityOnResume(Activity activity) {
		super.callActivityOnResume(activity);
	}

	private static final String TAG = "DockForStrictMode";
}
