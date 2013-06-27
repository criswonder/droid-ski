package com.taobao.android.ski;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.taobao.android.ski.radar.StrictModeMon;

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
		
//		StrictModeMon.init(this);
		
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.taobao.android.ski", "com.taobao.android.ski.hud.rose.RoseService"));
		ComponentName name = this.getTargetContext().startService(intent);
		Log.v("tag", name.toString());
		
		super.onStart();
//		StrictModeMon.stop();
	}

	@Override public void callActivityOnResume(Activity activity) {
		super.callActivityOnResume(activity);
	}

	private static final String TAG = "DockForStrictMode";
}
